package com.nutriplanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * A very small test framework written in plain Java, so the project needs no
 * external testing library. Each test is a named block of code; a failed check
 * throws an AssertionError with a clear message.
 */
public final class TestSupport {

    /** A piece of test code that may throw any exception. */
    public interface TestBody {
        void run() throws Exception;
    }

    private int passed;
    private final List<String> failures = new ArrayList<>();

    /** Runs one test and records whether it passed. */
    public void test(String name, TestBody body) {
        try {
            body.run();
            passed++;
            System.out.println("  PASS  " + name);
        } catch (Throwable t) {
            failures.add(name + ": " + t);
            System.out.println("  FAIL  " + name);
            System.out.println("        " + t);
        }
    }

    public int getPassed() {
        return passed;
    }

    public List<String> getFailures() {
        return failures;
    }

    // ------------------------------------------------------------- checks

    public static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + " (expected " + expected + " but was " + actual + ")");
        }
    }

    public static void assertNear(double expected, double actual, double tolerance, String message) {
        if (Math.abs(expected - actual) > tolerance) {
            throw new AssertionError(message + " (expected " + expected + " but was " + actual + ")");
        }
    }

    public static void assertContains(String text, String expected, String message) {
        if (text == null || !text.contains(expected)) {
            throw new AssertionError(message + " (text does not contain \"" + expected + "\")");
        }
    }

    public static void assertNotContains(String text, String unexpected, String message) {
        if (text != null && text.contains(unexpected)) {
            throw new AssertionError(message + " (text unexpectedly contains \"" + unexpected + "\")");
        }
    }

    /** Checks that the code throws the given type of exception, and returns it. */
    public static <T extends Throwable> T assertThrows(Class<T> type, TestBody body, String message) {
        try {
            body.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) {
                return type.cast(t);
            }
            throw new AssertionError(message + " (expected " + type.getSimpleName() + " but got " + t + ")");
        }
        throw new AssertionError(message + " (expected " + type.getSimpleName() + " but nothing was thrown)");
    }

    // ------------------------------------------------------------- helpers

    /** Creates a new empty folder for test data. */
    public static Path tempFolder() throws IOException {
        return Files.createTempDirectory("nutriplanner-test");
    }

    /** Deletes a test folder and everything in it. */
    public static void deleteFolder(Path folder) throws IOException {
        if (folder == null || !Files.exists(folder)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(folder)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> path.toFile().delete());
        }
    }
}
