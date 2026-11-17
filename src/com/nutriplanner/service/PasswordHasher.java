package com.nutriplanner.service;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Turns passwords into salted hashes and checks them, using PBKDF2 with
 * HMAC-SHA256 from the standard Java library.
 *
 * Why: the real password is never saved. Each user gets a random "salt", so two
 * people with the same password still get different hashes, and the hash is
 * repeated many times so guessing passwords is slow for an attacker.
 *
 * Stored format: pbkdf2$iterations$salt$hash (salt and hash in Base64).
 */
public class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String PREFIX = "pbkdf2";
    private static final int ITERATIONS = 100_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;

    private final SecureRandom random = new SecureRandom();

    /** Creates a new salted hash for a password. */
    public String hash(String password) {
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        byte[] key = derive(password, salt, ITERATIONS);
        Base64.Encoder base64 = Base64.getEncoder();
        return PREFIX + "$" + ITERATIONS + "$" + base64.encodeToString(salt) + "$" + base64.encodeToString(key);
    }

    /** Returns true when the password matches the stored hash. */
    public boolean verify(String password, String storedHash) {
        if (password == null || storedHash == null) {
            return false;
        }
        String[] parts = storedHash.split("\\$");
        if (parts.length != 4 || !PREFIX.equals(parts[0])) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = derive(password, salt, iterations);
            // MessageDigest.isEqual takes the same time for every input,
            // so the comparison does not leak how many bytes matched.
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException e) {
            return false;   // damaged hash in the data file
        }
    }

    private static byte[] derive(String password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 is not available in this Java installation", e);
        } finally {
            spec.clearPassword();
        }
    }
}
