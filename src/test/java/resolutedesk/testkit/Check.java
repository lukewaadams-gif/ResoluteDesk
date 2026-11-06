package resolutedesk.testkit;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/** Assertions for the tests. Each throws an AssertionError with a readable message when it fails. */
public final class Check {
    private Check() {
    }

    public static void isTrue(boolean cond, String what) {
        if (!cond) throw new AssertionError(what);
    }

    public static void equal(Object expected, Object actual, String what) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(what + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }

    public static void near(double expected, double actual, double tolerance, String what) {
        if (Math.abs(expected - actual) > tolerance) {
            throw new AssertionError(what + ": expected " + expected + " +/- " + tolerance + " but was " + actual);
        }
    }

    public static void between(double lo, double hi, double actual, String what) {
        if (actual < lo || actual > hi) throw new AssertionError(what + ": expected between " + lo + " and " + hi + " but was " + actual);
    }

    public static <T> void distinct(Collection<T> items, String what) {
        Set<T> seen = new HashSet<T>();
        for (T t : items) if (!seen.add(t)) throw new AssertionError(what + ": <" + t + "> appears more than once in " + items);
    }
}
