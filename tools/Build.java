import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/**
 * Builds, tests and packages Resolute Desk with nothing but a JDK (11 or newer).
 *
 * <pre>
 *   java tools/Build.java compile      compile the game into build/classes
 *   java tools/Build.java test         compile and run the unit tests and whole-presidency simulations
 *   java tools/Build.java bundle       merge the sources into the one-file BlueJ version, build/ResoluteDesk.java
 *   java tools/Build.java web          translate the bundle into the browser version, build/index.html
 *   java tools/Build.java jar          package the game as build/ResoluteDesk.jar
 *   java tools/Build.java dist         all of the above, then copy the results to dist/ and index.html
 *   java tools/Build.java equivalence  run the same simulations in Java and in the browser build (needs Node)
 *   java tools/Build.java run          play
 * </pre>
 */
public final class Build {
    private static final Path ROOT = Paths.get(System.getProperty("user.dir"));
    private static final Path MAIN = ROOT.resolve("src/main/java");
    private static final Path TEST = ROOT.resolve("src/test/java");
    private static final Path OUT = ROOT.resolve("build");
    private static final Path CLASSES = OUT.resolve("classes");
    private static final Path TEST_CLASSES = OUT.resolve("test-classes");
    private static final Path BUNDLE = OUT.resolve("ResoluteDesk.java");
    private static final Path BUNDLE_CLASSES = OUT.resolve("bundle-classes");
    private static final Path WEB = OUT.resolve("index.html");
    private static final Path JAR = OUT.resolve("ResoluteDesk.jar");
    private static final Path J2JS = ROOT.resolve("tools/j2js");
    private static final String MAIN_CLASS = "resolutedesk.ResoluteDesk";
    /** The translator reads javac's internal trees. */
    private static final List<String> JAVAC_EXPORTS = Arrays.asList(
            "--add-exports", "jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
            "--add-exports", "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
            "--add-exports", "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
            "--add-exports", "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED");
    /** Simulations compared in the equivalence check: seeds, era, president (- for the real one), scenario, weeks. */
    private static final String[] EQUIVALENCE = {"check", "sim 1 2029 - 0 210", "sim 1 2025 - 3 210", "sim 1 1961 - 0 420", "sim 1 1933 - 0 300", "transition 2025 52", "transition 1953 40", "policies 2025 104", "policies 1965 104", "wars 2025 80", "wars 1961 60", "stories 2025 104", "stories 1961 104"};

    private Build() {
    }

    public static void main(String[] args) throws Exception {
        String cmd = args.length > 0 ? args[0] : "dist";
        switch (cmd) {
            case "compile": compile(); break;
            case "test": test(); break;
            case "bundle": bundle(); break;
            case "web": web(); break;
            case "jar": jar(); break;
            case "dist": dist(); break;
            case "equivalence": equivalence(); break;
            case "run": compile(); exec(Arrays.asList(java(), "-cp", CLASSES.toString(), MAIN_CLASS), true); break;
            default:
                System.err.println("unknown command " + cmd + "; see the comment at the top of tools/Build.java");
                System.exit(2);
        }
    }

    static void compile() throws IOException {
        javac(sources(MAIN), CLASSES, null, true);
        System.out.println("compiled " + sources(MAIN).size() + " files into " + ROOT.relativize(CLASSES));
    }

    static void test() throws Exception {
        compile();
        javac(sources(TEST), TEST_CLASSES, CLASSES.toString(), true);
        String cp = CLASSES + File.pathSeparator + TEST_CLASSES;
        int code = exec(Arrays.asList(java(), "-Djava.awt.headless=true", "-cp", cp, "resolutedesk.testkit.TestRunner", TEST_CLASSES.toString()), true);
        if (code != 0) fail("tests failed");
    }

    static void bundle() throws Exception {
        Path tool = ROOT.resolve("tools/bundle/Bundler.java");
        if (exec(Arrays.asList(java(), tool.toString(), MAIN.toString(), BUNDLE.toString()), true) != 0) fail("bundling failed");
        String text = new String(Files.readAllBytes(BUNDLE), StandardCharsets.UTF_8);
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) > 126) fail("the bundle must be plain ASCII for the translator: character " + (int) text.charAt(i) + " at offset " + i);
        }
        javac(Arrays.asList(BUNDLE), BUNDLE_CLASSES, null, true);
        System.out.println("the one-file version compiles: " + ROOT.relativize(BUNDLE));
    }

    static void web() throws Exception {
        bundle();
        Path cls = OUT.resolve("j2js-classes");
        List<String> opts = new ArrayList<>(JAVAC_EXPORTS);
        opts.addAll(Arrays.asList("-d", cls.toString(), J2JS.resolve("J2JS.java").toString()));
        clean(cls);
        if (ToolProvider.getSystemJavaCompiler().run(null, null, null, opts.toArray(new String[0])) != 0) fail("the translator did not compile");
        Path parts = OUT.resolve("j2js-out");
        clean(parts);
        List<String> cmd = new ArrayList<>();
        cmd.add(java());
        cmd.addAll(JAVAC_EXPORTS);
        cmd.addAll(Arrays.asList("-cp", cls.toString(), "J2JS", BUNDLE.toString(), parts.toString()));
        StringBuilder log = new StringBuilder();
        if (exec(cmd, log) != 0) { System.out.print(log); fail("translation failed"); }
        for (String ln : log.toString().split("\n")) if (!ln.isEmpty() && !expectedWarning(ln)) System.out.println("  " + ln);
        StringBuilder html = new StringBuilder();
        html.append(read(J2JS.resolve("runtime/head.js")));
        html.append(read(parts.resolve("classes.js")));
        html.append(read(parts.resolve("registry.js")));
        html.append("// ---- functions\n").append(read(parts.resolve("functions.js")));
        html.append(read(J2JS.resolve("runtime/mid.js")));
        html.append("// ---- static initialisation\n").append(read(parts.resolve("statics.js")));
        html.append(read(J2JS.resolve("runtime/tail.js")));
        Files.write(WEB, html.toString().getBytes(StandardCharsets.UTF_8));
        System.out.println("wrote " + ROOT.relativize(WEB) + " (" + Files.size(WEB) + " bytes)");
    }

    /**
     * The translator reports Java library calls it leaves to the hand-written browser runtime.
     * These classes are replaced wholesale in runtime/mid.js, so their reports are expected.
     */
    private static boolean expectedWarning(String line) {
        for (String c : new String[]{"Sfx:", "Diagnostics:", "SaveGame:", "JSer:", "BgImport:", "HallStore:",
                "ResoluteDesk: new java.lang.Thread", "ResoluteDesk: library call java.lang.Thread"}) {
            if (line.contains(c)) return true;
        }
        return false;
    }

    static void jar() throws IOException {
        compile();
        Manifest mf = new Manifest();
        mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        mf.getMainAttributes().put(Attributes.Name.MAIN_CLASS, MAIN_CLASS);
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(JAR), mf);
             Stream<Path> files = Files.walk(CLASSES)) {
            for (Path p : files.filter(Files::isRegularFile).sorted().collect(Collectors.toList())) {
                out.putNextEntry(new JarEntry(CLASSES.relativize(p).toString().replace(File.separatorChar, '/')));
                Files.copy(p, out);
                out.closeEntry();
            }
        }
        System.out.println("wrote " + ROOT.relativize(JAR) + " (" + Files.size(JAR) + " bytes)");
    }

    static void dist() throws Exception {
        test();
        web();
        jar();
        Path dist = ROOT.resolve("dist");
        Files.createDirectories(dist);
        Files.copy(BUNDLE, dist.resolve("ResoluteDesk.java"), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(JAR, dist.resolve("ResoluteDesk.jar"), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(WEB, ROOT.resolve("index.html"), StandardCopyOption.REPLACE_EXISTING);
        System.out.println("copied the one-file version and the jar to dist/, and the browser version to index.html");
    }

    static void equivalence() throws Exception {
        web();
        int failures = 0;
        for (String sim : EQUIVALENCE) {
            List<String> args = new ArrayList<>(Arrays.asList(sim.split(" ")));
            args.add(0, "test");
            List<String> jcmd = new ArrayList<>(Arrays.asList(java(), "-cp", BUNDLE_CLASSES.toString(), "ResoluteDesk"));
            jcmd.addAll(args);
            StringBuilder jout = new StringBuilder(), nout = new StringBuilder();
            exec(jcmd, jout);
            exec(Arrays.asList("node", J2JS.resolve("noderun.js").toString(), WEB.toString(), "test " + sim), nout);
            boolean same = withoutTimings(jout.toString()).equals(withoutTimings(nout.toString()));
            System.out.println((same ? "same     " : "DIFFERENT") + "  " + sim);
            if (!same) failures++;
        }
        if (failures > 0) fail(failures + " simulations differ between Java and the browser build");
    }

    private static String withoutTimings(String s) {
        return Arrays.stream(s.split("\n")).filter(l -> !l.startsWith("init ") && !l.startsWith("Picked up JAVA_TOOL_OPTIONS")).collect(Collectors.joining("\n"));
    }

    // ---- helpers ------------------------------------------------------------------------------

    private static void javac(List<Path> files, Path out, String classpath, boolean java8) throws IOException {
        clean(out);
        List<String> opts = new ArrayList<>(Arrays.asList("-encoding", "UTF-8", "-d", out.toString(), "-Xlint:-options", "-Xmaxerrs", "50"));
        if (java8) opts.addAll(Arrays.asList("--release", "8"));
        if (classpath != null) opts.addAll(Arrays.asList("-cp", classpath));
        for (Path f : files) opts.add(f.toString());
        JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
        StringWriter err = new StringWriter();
        int code = jc.run(null, null, new java.io.OutputStream() {
            @Override public void write(int b) { err.write(b); }
        }, opts.toArray(new String[0]));
        String log = err.toString();
        if (!log.trim().isEmpty()) System.err.print(log);
        if (code != 0) fail("compilation failed");
    }

    private static List<Path> sources(Path root) throws IOException {
        if (!Files.exists(root)) return new ArrayList<>();
        try (Stream<Path> s = Files.walk(root)) {
            return s.filter(p -> p.toString().endsWith(".java")).sorted().collect(Collectors.toList());
        }
    }

    private static void clean(Path dir) throws IOException {
        if (Files.exists(dir)) {
            try (Stream<Path> s = Files.walk(dir)) {
                for (Path p : s.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) Files.delete(p);
            }
        }
        Files.createDirectories(dir);
    }

    private static String java() {
        return Paths.get(System.getProperty("java.home"), "bin", "java").toString();
    }

    private static int exec(List<String> cmd, boolean inherit) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(cmd).directory(ROOT.toFile());
        if (inherit) pb.inheritIO();
        return pb.start().waitFor();
    }

    private static int exec(List<String> cmd, StringBuilder out) throws IOException, InterruptedException {
        Process p = new ProcessBuilder(cmd).directory(ROOT.toFile()).redirectErrorStream(true).start();
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try (InputStream in = p.getInputStream()) {
            byte[] b = new byte[8192];
            for (int n; (n = in.read(b)) > 0; ) buf.write(b, 0, n);
        }
        int code = p.waitFor();
        out.append(new String(buf.toByteArray(), StandardCharsets.UTF_8));
        return code;
    }

    private static String read(Path p) throws IOException {
        return new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
    }

    private static void fail(String msg) {
        System.err.println("build: " + msg);
        System.exit(1);
    }
}
