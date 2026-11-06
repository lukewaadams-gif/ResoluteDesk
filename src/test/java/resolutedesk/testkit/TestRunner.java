package resolutedesk.testkit;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Finds every class whose name ends in "Test" under a directory of compiled tests, runs each of
 * its {@link Test} methods on a fresh instance, and reports the results. Exits with status 1 if
 * any test fails.
 *
 * <p>Usage: {@code java -cp <classes>:<test classes> resolutedesk.testkit.TestRunner <test classes dir>}
 */
public final class TestRunner {
    private TestRunner() {
    }

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.US);           // the game formats numbers with String.format
        File root = new File(args.length > 0 ? args[0] : "build/test-classes");
        List<String> names = new ArrayList<String>();
        collect(root, root, names);
        Collections.sort(names);
        int passed = 0, failed = 0;
        long t0 = System.currentTimeMillis();
        for (String name : names) {
            Class<?> c = Class.forName(name);
            List<Method> tests = new ArrayList<Method>();
            for (Method m : c.getDeclaredMethods()) {
                if (m.isAnnotationPresent(Test.class) && Modifier.isPublic(m.getModifiers()) && m.getParameterTypes().length == 0) tests.add(m);
            }
            Collections.sort(tests, (a, b) -> a.getName().compareTo(b.getName()));
            for (Method m : tests) {
                long s = System.currentTimeMillis();
                try {
                    m.invoke(c.getDeclaredConstructor().newInstance());
                    passed++;
                    System.out.printf("  ok    %-60s %5d ms%n", c.getSimpleName() + "." + m.getName(), System.currentTimeMillis() - s);
                } catch (InvocationTargetException e) {
                    failed++;
                    Throwable cause = e.getCause();
                    System.out.printf("  FAIL  %-60s%n        %s%n", c.getSimpleName() + "." + m.getName(), cause);
                    if (!(cause instanceof AssertionError)) {
                        for (StackTraceElement el : cause.getStackTrace()) {
                            if (el.getClassName().startsWith("resolutedesk")) System.out.println("          at " + el);
                        }
                    }
                }
            }
        }
        System.out.printf("%d passed, %d failed in %.1f s%n", passed, failed, (System.currentTimeMillis() - t0) / 1000.0);
        if (failed > 0) System.exit(1);
    }

    private static void collect(File root, File dir, List<String> out) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) collect(root, f, out);
            else if (f.getName().endsWith("Test.class") && !f.getName().contains("$")) {
                String rel = root.toURI().relativize(f.toURI()).getPath();
                out.add(rel.substring(0, rel.length() - ".class".length()).replace('/', '.'));
            }
        }
    }
}
