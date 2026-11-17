package com.nutriplanner;

import com.nutriplanner.exception.AuthenticationException;
import com.nutriplanner.exception.ValidationException;
import com.nutriplanner.model.ActivityLevel;
import com.nutriplanner.model.Gender;
import com.nutriplanner.model.HealthGoal;
import com.nutriplanner.model.User;
import com.nutriplanner.service.Authentication;
import com.nutriplanner.service.PasswordHasher;
import com.nutriplanner.service.UserManager;
import com.nutriplanner.storage.DataStorage;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;

import static com.nutriplanner.TestSupport.assertContains;
import static com.nutriplanner.TestSupport.assertEquals;
import static com.nutriplanner.TestSupport.assertNotContains;
import static com.nutriplanner.TestSupport.assertThrows;
import static com.nutriplanner.TestSupport.check;

/** Tests for registration, login, logout, passwords and saving users. */
public final class UserTests {

    private UserTests() {
    }

    public static void run(TestSupport t) throws Exception {
        Path folder = TestSupport.tempFolder();
        try {
            PasswordHasher hasher = new PasswordHasher();
            UserManager users = new UserManager(new DataStorage(folder), hasher, Clock.systemDefaultZone());
            Authentication auth = new Authentication(users, hasher);

            t.test("New user registration", () -> {
                User user = users.register("Priya", "Priya123", CalculatorTests.profile(Gender.FEMALE, 21, 160, 55,
                        ActivityLevel.LIGHTLY_ACTIVE, HealthGoal.WEIGHT_LOSS));
                assertEquals("priya", user.getUsername(), "username stored in lower case");
                assertEquals(1, users.getUserCount(), "one user");
            });

            t.test("Duplicate usernames are rejected, whatever the case", () -> {
                assertThrows(ValidationException.class, () -> users.register("PRIYA", "Other123",
                        CalculatorTests.profile(Gender.MALE, 30, 170, 70, ActivityLevel.SEDENTARY,
                                HealthGoal.WEIGHT_GAIN)), "duplicate");
            });

            t.test("Invalid usernames and weak passwords are rejected", () -> {
                assertThrows(ValidationException.class, () -> UserManager.validateUsername(""), "empty");
                assertThrows(ValidationException.class, () -> UserManager.validateUsername("ab"), "too short");
                assertThrows(ValidationException.class, () -> UserManager.validateUsername("priya sharma"), "space");
                assertThrows(ValidationException.class, () -> UserManager.validateUsername("priya@1"), "symbol");
                assertThrows(ValidationException.class, () -> UserManager.validatePassword("abc1"), "too short");
                assertThrows(ValidationException.class, () -> UserManager.validatePassword("abcdefgh"), "no digit");
                assertThrows(ValidationException.class, () -> UserManager.validatePassword("12345678"), "no letter");
                UserManager.validatePassword("Priya123");
            });

            t.test("Existing user login works and logout ends the session", () -> {
                User user = auth.login("priya", "Priya123");
                check(auth.isLoggedIn(), "logged in");
                assertEquals("Test User", user.getDisplayName(), "display name");
                auth.logout();
                check(!auth.isLoggedIn(), "logged out");
            });

            t.test("Invalid login: wrong password, unknown user and empty fields", () -> {
                AuthenticationException e = assertThrows(AuthenticationException.class,
                        () -> auth.login("priya", "wrong123"), "wrong password");
                assertEquals("Invalid username or password.", e.getMessage(), "same message for both cases");
                assertThrows(AuthenticationException.class, () -> auth.login("nobody", "Priya123"), "unknown user");
                assertThrows(AuthenticationException.class, () -> auth.login("", ""), "empty");
                check(!auth.isLoggedIn(), "still logged out");
            });

            t.test("Passwords are stored only as salted hashes", () -> {
                String file = new String(Files.readAllBytes(folder.resolve("users.csv")), StandardCharsets.UTF_8);
                assertNotContains(file, "Priya123", "plain password in users.csv");
                assertContains(file, "pbkdf2$100000$", "hash format");
                String first = hasher.hash("Same1234");
                String second = hasher.hash("Same1234");
                check(!first.equals(second), "the same password gives different hashes (random salt)");
                check(hasher.verify("Same1234", first) && hasher.verify("Same1234", second), "both verify");
                check(!hasher.verify("same1234", first), "passwords are case-sensitive");
                check(!hasher.verify("Same1234", "damaged-hash"), "damaged hash is rejected safely");
            });

            t.test("Change password checks the current password", () -> {
                User user = users.findUser("priya").get();
                assertThrows(ValidationException.class, () -> users.changePassword(user, "wrong123", "NewPass99"),
                        "wrong current password");
                users.changePassword(user, "Priya123", "NewPass99");
                assertThrows(AuthenticationException.class, () -> auth.login("priya", "Priya123"), "old password");
                auth.login("priya", "NewPass99");
                users.changePassword(user, "NewPass99", "Priya123");
            });

            t.test("Users and profile changes are still there after a restart", () -> {
                User user = users.findUser("priya").get();
                user.getProfile().setWeightKg(54.5);
                users.saveChanges();
                UserManager reloaded = new UserManager(new DataStorage(folder), hasher, Clock.systemDefaultZone());
                User again = reloaded.findUser("PRIYA").orElseThrow(() -> new AssertionError("user not reloaded"));
                assertEquals(54.5, again.getProfile().getWeightKg(), "updated weight");
                assertEquals(HealthGoal.WEIGHT_LOSS, again.getProfile().getHealthGoal(), "goal");
                new Authentication(reloaded, hasher).login("priya", "Priya123");
            });
        } finally {
            TestSupport.deleteFolder(folder);
        }
    }
}
