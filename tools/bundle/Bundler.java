import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Turns the multi-file sources into the single file that BlueJ and the browser translator use.
 *
 * <p>The application panel {@code resolutedesk.ResoluteDesk} becomes the one top-level class. Every
 * other class becomes a static nested class of it. Classes that the code reaches through static
 * imports (the holders of constants, tables and helpers) are merged into the top-level class
 * itself, in an order where each comes after the holders its initializers use, so their members
 * stay reachable by their simple names. Package and project imports disappear; JDK imports are
 * merged.
 *
 * <p>Usage: {@code java tools/bundle/Bundler.java <source root> <output file>}
 */
public final class Bundler {
    private static final String APP = "resolutedesk.ResoluteDesk";
    private static final Pattern PACKAGE = Pattern.compile("^package ([\\w.]+);\\s*$");
    private static final Pattern IMPORT = Pattern.compile("^import (static )?([\\w.]+?)(\\.\\*)?;\\s*$");
    private static final Pattern DECL = Pattern.compile("^public ((?:final |abstract )*)(class|interface|enum) (\\w+)\\b(.*)$");

    /** One source file, split into the parts the bundle rearranges. */
    private static final class Unit {
        String fq, simple;
        final List<String> imports = new ArrayList<>();
        final List<String> staticImports = new ArrayList<>();
        final List<String> doc = new ArrayList<>();
        String kind, modifiers, rest;
        final List<String> body = new ArrayList<>();
    }

    private Bundler() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("usage: java tools/bundle/Bundler.java <source root> <output file>");
            System.exit(2);
        }
        String out = bundle(Paths.get(args[0]));
        Files.write(Paths.get(args[1]), out.getBytes(StandardCharsets.UTF_8));
        System.out.println("wrote " + args[1] + " (" + out.length() + " characters)");
    }

    static String bundle(Path root) throws IOException {
        List<Unit> units = new ArrayList<>();
        try (Stream<Path> s = Files.walk(root)) {
            for (Path p : s.filter(f -> f.toString().endsWith(".java")).sorted().collect(Collectors.toList())) units.add(parse(root, p));
        }
        Map<String, Unit> byName = new LinkedHashMap<>();
        for (Unit u : units) byName.put(u.fq, u);
        Unit app = byName.get(APP);
        if (app == null) throw new IllegalStateException("no " + APP);

        // holders: the classes any file imports statically (other than the application itself)
        Set<String> holders = new TreeSet<>();
        for (Unit u : units) for (String h : u.staticImports) if (!h.equals(APP) && byName.containsKey(h)) holders.add(h);
        topologicalOrder(holders, byName);      // fails fast if holders import each other in a cycle

        Set<String> projectSimple = new HashSet<>();
        for (Unit u : units) projectSimple.add(u.simple);
        Set<String> holderSimple = new HashSet<>();
        for (String h : holders) holderSimple.add(byName.get(h).simple);

        TreeSet<String> jdkImports = new TreeSet<>();
        for (Unit u : units) {
            for (String imp : u.imports) {
                if (imp.startsWith("resolutedesk.")) continue;
                String simple = imp.substring(imp.lastIndexOf('.') + 1);
                if (!projectSimple.contains(simple)) jdkImports.add(imp);
            }
        }

        StringBuilder b = new StringBuilder();
        b.append(header());
        for (String imp : jdkImports) b.append("import ").append(imp).append(";\n");
        b.append('\n');
        for (String d : app.doc) b.append(d).append('\n');
        b.append("public ").append(app.modifiers).append(app.kind).append(' ').append(app.simple).append(app.rest).append('\n');
        appendLines(b, dataMethods(app.body), "", holderSimple);
        for (Unit u : initOrder(units, app, holders)) {
            b.append('\n');
            if (holders.contains(u.fq)) {
                b.append("    // ---------------------------------------------------------------------\n");
                b.append("    //  ").append(u.simple).append(" (").append(u.fq).append(")");
                String summary = docSummary(u.doc);
                if (!summary.isEmpty()) b.append(": ").append(summary);
                b.append('\n');
                b.append("    // ---------------------------------------------------------------------\n");
                appendLines(b, dataMethods(withoutPrivateConstructor(u)), "", holderSimple);
            } else {
                for (String d : u.doc) b.append("    ").append(d).append('\n');
                String mods = u.kind.equals("class") ? "static " + u.modifiers : u.modifiers;
                b.append("    ").append(mods).append(u.kind).append(' ').append(u.simple).append(u.rest).append('\n');
                appendLines(b, u.body, "    ", holderSimple);
                b.append("    }\n");
            }
        }
        b.append("}\n");
        return b.toString();
    }

    /**
     * Every class except the application, ordered so that each comes after the classes its static
     * initializers use. Java initializes nested classes lazily and doesn't need this, but the
     * browser translator initializes them in file order. A class's initializers include, one level
     * at a time, the bodies of its own methods they call.
     */
    private static List<Unit> initOrder(List<Unit> units, Unit app, Set<String> holders) {
        Map<String, Unit> bySimple = new HashMap<>();
        for (Unit u : units) if (u != app) bySimple.put(u.simple, u);
        Map<String, String> memberHolder = new HashMap<>();
        for (Unit u : units) if (holders.contains(u.fq)) for (String m : declaredNames(u).keySet()) memberHolder.put(m, u.simple);
        Map<String, Set<String>> deps = new LinkedHashMap<>();
        Pattern ident = Pattern.compile("(?<![\\w$.])([A-Za-z_]\\w*)");
        for (Unit u : units) {
            if (u == app) continue;
            Set<String> d = new TreeSet<>();
            Matcher m = ident.matcher(blankLiteralsAndComments(initializerText(u)));
            while (m.find()) {
                String id = m.group(1);
                if (bySimple.containsKey(id) && !id.equals(u.simple)) d.add(id);
                String h = memberHolder.get(id);
                if (h != null && !h.equals(u.simple)) d.add(h);
            }
            deps.put(u.simple, d);
        }
        List<Unit> order = new ArrayList<>();
        Set<String> done = new HashSet<>(), active = new HashSet<>();
        for (Unit u : units) if (u != app) place(u.simple, bySimple, deps, done, active, order);
        return order;
    }

    private static void place(String name, Map<String, Unit> bySimple, Map<String, Set<String>> deps, Set<String> done, Set<String> active, List<Unit> order) {
        if (done.contains(name) || active.contains(name)) return;      // a cycle: keep source order from here
        active.add(name);
        for (String d : deps.get(name)) place(d, bySimple, deps, done, active, order);
        active.remove(name);
        done.add(name);
        order.add(bySimple.get(name));
    }

    /** The top-level members of a class: name to source text (methods by name, all overloads together). */
    private static Map<String, String> declaredNames(Unit u) {
        Map<String, String> out = new LinkedHashMap<>();
        for (String member : topLevelMembers(u)) {
            String head = blankLiteralsAndComments(member).trim().split("[{=;]", 2)[0];
            Matcher mm = Pattern.compile("(\\w+)\\s*\\($").matcher(head.split("\\(", 2)[0] + "(");
            if (head.contains("(") && mm.find()) out.merge(mm.group(1), member, (a, c) -> a + "\n" + c);
            else for (String name : fieldNames(member)) out.merge(name, member, (a, c) -> a + "\n" + c);
        }
        return out;
    }

    /** Every name a field declaration declares, as in {@code static final int A = 1, B = 2;}. */
    static List<String> fieldNames(String member) {
        String code = blankLiteralsAndComments(member);
        String prev = null;
        while (!code.equals(prev)) { prev = code; code = code.replaceAll("<[\\w\\s,.?\\[\\]]*>", ""); }
        StringBuilder flat = new StringBuilder();
        int depth = 0;
        for (char c : code.toCharArray()) {
            if (c == '(' || c == '[' || c == '{') depth++;
            else if (c == ')' || c == ']' || c == '}') depth--;
            else if (depth == 0) flat.append(c);
        }
        String decl = flat.toString().split(";", 2)[0];
        List<String> names = new ArrayList<>();
        String[] parts = decl.split(",");
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i].split("=", 2)[0].trim();
            Matcher m = Pattern.compile(i == 0 ? "(\\w+)\\s*$" : "^(\\w+)").matcher(p);
            if (m.find()) names.add(m.group(1));
        }
        return names;
    }

    /** Static field initializers and static blocks, plus the bodies of the class's own methods they call. */
    private static String initializerText(Unit u) {
        StringBuilder init = new StringBuilder();
        Map<String, String> methods = new HashMap<>();
        for (String member : topLevelMembers(u)) {
            String code = blankLiteralsAndComments(member).trim();
            String head = code.split("[{;]", 2)[0];
            boolean isStatic = Pattern.compile("(^|\\s)static(\\s|$)").matcher(head).find() || code.startsWith("static {");
            boolean isMethod = head.contains("(") && !head.split("\\(", 2)[0].contains("=");
            if (isMethod) {
                Matcher mm = Pattern.compile("(\\w+)\\s*$").matcher(head.split("\\(", 2)[0]);
                if (mm.find()) methods.merge(mm.group(1), member, (a, c) -> a + "\n" + c);
            } else if (isStatic && (code.startsWith("static {") || head.contains("=") || code.contains("="))) {
                init.append(member).append('\n');
            }
        }
        Set<String> seen = new HashSet<>();
        String text = init.toString();
        for (int round = 0; round < 6; round++) {
            StringBuilder more = new StringBuilder();
            for (Map.Entry<String, String> e : methods.entrySet()) {
                if (!seen.contains(e.getKey()) && Pattern.compile("(?<![\\w$.])" + e.getKey() + "\\s*\\(").matcher(text).find()) {
                    seen.add(e.getKey());
                    more.append(e.getValue()).append('\n');
                }
            }
            if (more.length() == 0) break;
            text += more;
        }
        return text;
    }

    /** The class body split into its top-level members, each with its full text. */
    private static List<String> topLevelMembers(Unit u) {
        List<String> out = new ArrayList<>();
        String body = String.join("\n", u.body);
        String code = blankLiteralsAndComments(body);
        int depth = 0, start = 0;
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) {
                    int j = i + 1;
                    while (j < code.length() && (code.charAt(j) == ' ' || code.charAt(j) == ';')) j++;
                    if (isMemberEnd(code, start, i)) { out.add(body.substring(start, j)); start = j; }
                }
            } else if (c == ';' && depth == 0) {
                out.add(body.substring(start, i + 1));
                start = i + 1;
            }
        }
        return out;
    }

    /** A closing brace at depth zero ends a member unless it closes an array initializer of a field. */
    private static boolean isMemberEnd(String code, int start, int close) {
        String head = code.substring(start, close);
        int brace = head.indexOf('{');
        String before = brace < 0 ? head : head.substring(0, brace);
        return !before.contains("=");
    }

    private static Unit parse(Path root, Path file) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        Unit u = new Unit();
        String pkg = null;
        int i = 0;
        for (; i < lines.size(); i++) {
            String ln = lines.get(i);
            Matcher m = PACKAGE.matcher(ln);
            if (m.matches()) { pkg = m.group(1); continue; }
            m = IMPORT.matcher(ln);
            if (m.matches()) {
                if (m.group(1) != null) u.staticImports.add(m.group(2));
                else u.imports.add(m.group(2) + (m.group(3) != null ? ".*" : ""));
                continue;
            }
            if (ln.trim().isEmpty()) continue;
            break;
        }
        if (pkg == null) throw new IllegalStateException(file + ": no package");
        for (; i < lines.size(); i++) {
            Matcher m = DECL.matcher(lines.get(i));
            if (m.matches()) {
                u.modifiers = m.group(1);
                u.kind = m.group(2);
                u.simple = m.group(3);
                u.rest = m.group(4);
                break;
            }
            u.doc.add(lines.get(i));
        }
        if (u.simple == null) throw new IllegalStateException(file + ": no public top-level type");
        int end = lines.size() - 1;
        while (end > i && lines.get(end).trim().isEmpty()) end--;
        if (!lines.get(end).equals("}")) throw new IllegalStateException(file + ": expected a closing brace on the last line");
        u.body.addAll(lines.subList(i + 1, end));
        u.fq = pkg + "." + u.simple;
        String expected = root.relativize(file).toString().replace(java.io.File.separatorChar, '.');
        if (!expected.equals(u.fq + ".java")) throw new IllegalStateException(file + ": file and class names differ");
        return u;
    }

    /** Holders in an order where every holder follows the holders it imports. */
    private static List<String> topologicalOrder(Set<String> holders, Map<String, Unit> byName) {
        List<String> order = new ArrayList<>();
        Map<String, Integer> state = new HashMap<>();
        for (String h : holders) visit(h, holders, byName, state, order, new ArrayList<>());
        return order;
    }

    private static void visit(String h, Set<String> holders, Map<String, Unit> byName, Map<String, Integer> state, List<String> order, List<String> path) {
        Integer st = state.get(h);
        if (st != null && st == 2) return;
        if (st != null && st == 1) throw new IllegalStateException("holders import each other in a cycle: " + path + " -> " + h);
        state.put(h, 1);
        path.add(h);
        List<String> deps = new ArrayList<>(byName.get(h).staticImports);
        Collections.sort(deps);
        for (String d : deps) if (holders.contains(d) && !d.equals(h)) visit(d, holders, byName, state, order, path);
        path.remove(path.size() - 1);
        state.put(h, 2);
        order.add(h);
    }

    private static List<String> withoutPrivateConstructor(Unit u) {
        List<String> out = new ArrayList<>(u.body);
        for (int k = 0; k + 1 < out.size(); k++) {
            if (out.get(k).trim().equals("private " + u.simple + "() {") && out.get(k + 1).trim().equals("}")) {
                out.remove(k);
                out.remove(k);
                if (k < out.size() && out.get(k).trim().isEmpty()) out.remove(k);
                break;
            }
        }
        return out;
    }

    /** Initializers longer than this move into methods of their own (see dataMethods). */
    static final int DATA_METHOD_CHARS = 1500;

    /**
     * Moves long array initializers of static fields into methods of their own. Every holder is
     * merged into the top-level class, and Java compiles all of a class's static initializers into one
     * method, which may not exceed 64 KB of bytecode; the game's tables would overflow it. A field such as
     * {@code static final String[] ROWS = {...};} becomes {@code static final String[] ROWS = dataROWS();}
     * with the array built in {@code dataROWS()}, so each table counts against its own method instead.
     * The fields keep their names and their order of initialization.
     */
    static List<String> dataMethods(List<String> body) {
        String text = String.join("\n", body);
        String code = blankLiteralsAndComments(text);
        Matcher m = Pattern.compile("(?m)^( *)((?:public |protected |private )?static final )([\\w.]+(?:\\[\\])+) (\\w+) = \\{").matcher(code);
        List<int[]> spans = new ArrayList<>();
        List<String[]> parts = new ArrayList<>();
        while (m.find()) {
            int open = m.end() - 1, depth = 0, i = open;
            for (; i < code.length(); i++) {
                char c = code.charAt(i);
                if (c == '{') depth++;
                else if (c == '}' && --depth == 0) break;
            }
            int semi = i + 1;
            while (semi < code.length() && Character.isWhitespace(code.charAt(semi))) semi++;
            if (semi >= code.length() || code.charAt(semi) != ';' || i - open < DATA_METHOD_CHARS) continue;
            spans.add(new int[]{m.start(), open, i + 1, semi + 1});
            parts.add(new String[]{m.group(1), m.group(2), m.group(3), m.group(4)});
        }
        StringBuilder out = new StringBuilder(text);
        for (int k = spans.size() - 1; k >= 0; k--) {
            int[] sp = spans.get(k);
            String[] pt = parts.get(k);
            String ind = pt[0], type = pt[2], name = pt[3];
            String decl = ind + pt[1] + type + " " + name + " = data" + name + "();\n\n"
                    + ind + "private static " + type + " data" + name + "() {\n"
                    + ind + "    return new " + type + text.substring(sp[1], sp[2]) + ";\n"
                    + ind + "}";
            out.replace(sp[0], sp[3], decl);
        }
        return new ArrayList<>(Arrays.asList(out.toString().split("\n", -1)));
    }

    /** Appends body lines, re-indented, with qualified references to merged holders pointed at the top-level class. */
    private static void appendLines(StringBuilder b, List<String> body, String indent, Set<String> holderSimple) {
        String text = String.join("\n", body);
        text = requalify(text, holderSimple);
        for (String ln : text.split("\n", -1)) {
            if (ln.isEmpty()) b.append('\n');
            else b.append(indent).append(ln).append('\n');
        }
    }

    /** Rewrites {@code Holder.member} in code (not in strings or comments) to {@code ResoluteDesk.member}. */
    static String requalify(String text, Set<String> holderSimple) {
        if (holderSimple.isEmpty()) return text;
        String code = blankLiteralsAndComments(text);
        Pattern p = Pattern.compile("(?<![\\w$.])(" + String.join("|", holderSimple) + ")\\.(?=[A-Za-z_])");
        Matcher m = p.matcher(code);
        StringBuilder out = new StringBuilder();
        int last = 0;
        while (m.find()) {
            out.append(text, last, m.start(1)).append("ResoluteDesk");
            last = m.end(1);
        }
        out.append(text.substring(last));
        return out.toString();
    }

    /** The text with comments and string and character literals replaced by spaces (same length). */
    static String blankLiteralsAndComments(String s) {
        char[] c = s.toCharArray();
        int i = 0, n = c.length;
        while (i < n) {
            if (c[i] == '/' && i + 1 < n && c[i + 1] == '/') {
                while (i < n && c[i] != '\n') c[i++] = ' ';
            } else if (c[i] == '/' && i + 1 < n && c[i + 1] == '*') {
                while (i < n && !(c[i] == '*' && i + 1 < n && c[i + 1] == '/')) { if (c[i] != '\n') c[i] = ' '; i++; }
                if (i < n) { c[i] = ' '; c[i + 1] = ' '; i += 2; }
            } else if (c[i] == '"' || c[i] == '\'') {
                char q = c[i++];
                while (i < n && c[i] != q) { if (c[i] == '\\') c[i++] = ' '; if (i < n) c[i++] = ' '; }
                i++;
            } else {
                i++;
            }
        }
        return new String(c);
    }

    private static String docSummary(List<String> doc) {
        String joined = doc.stream().map(String::trim).collect(Collectors.joining(" "));
        Matcher m = Pattern.compile("/\\*\\*\\s*(.*?)\\s*\\*/").matcher(joined);
        if (!m.find()) return "";
        String s = m.group(1).replaceAll("\\s*\\*\\s*", " ").trim();
        int dot = s.indexOf(". ");
        return dot > 0 ? s.substring(0, dot + 1) : s;
    }

    private static String header() {
        return String.join("\n",
                "/*",
                " * ============================================================================",
                " *  ResoluteDesk.java  -  \"RESOLUTE DESK: A Presidency Simulator\"",
                " *  The whole game in one file, for BlueJ (Java 8 or newer).",
                " * ============================================================================",
                " *",
                " *  HOW TO RUN IN BLUEJ",
                " *    1. Project > New Project (any name).",
                " *    2. Edit > Add Class from File...  and choose this file  (or create a class",
                " *       named  ResoluteDesk  and paste the whole file into it).",
                " *    3. Compile, then right-click ResoluteDesk and choose void main(String[] args).",
                " *",
                " *  This file is generated. The source of the game is the multi-file project in",
                " *  src/main/java; tools/bundle/Bundler.java merges it into this one file. Edit",
                " *  the project, not this file.",
                " * ============================================================================",
                " */",
                "",
                "");
    }
}
