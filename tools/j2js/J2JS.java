import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.code.Type.*;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.Context;

import javax.tools.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Translates ResoluteDesk.java into the JavaScript used by the browser version.
 * Output: four sections (classes, $CLASSES registry, functions, static initialisation) written to files.
 * Uses javac's attributed trees so that types decide integer division, string conversion and overloads.
 */
public class J2JS {
    static final String MAIN = "ResoluteDesk";
    Types types;
    CharSequence source;
    Symtab syms;
    ClassSymbol mainSym;
    final Map<Symbol, String> localNames = new HashMap<>();
    final Set<String> globalNames = new HashSet<>();
    final List<String> warnings = new ArrayList<>();
    final Map<String, String> classRename = new HashMap<>();
    final Set<String> skipFunctions = new HashSet<>(Arrays.asList("dateOf", "dowOf", "pickFont", "registerFonts", "launch", "main"));
    final Set<String> skipMethods = new HashSet<>(Arrays.asList("SaveGame.dir"));
    final Set<String> emptyClasses = new HashSet<>();
    final Set<String> topFieldsRenamed = new HashSet<>();
    String curClass = "";
    int lambdaDepth = 0;

    static final Set<String> RESERVED = new HashSet<>(Arrays.asList(
            "break", "case", "catch", "class", "const", "continue", "debugger", "default", "delete", "do", "else", "export", "extends",
            "finally", "for", "function", "if", "import", "in", "instanceof", "new", "return", "super", "switch", "this", "throw", "try",
            "typeof", "var", "void", "while", "with", "yield", "let", "static", "enum", "await", "implements", "package", "protected",
            "interface", "private", "public", "null", "true", "false", "arguments", "eval", "of", "undefined", "NaN", "Infinity"));
    static final String[] JS_GLOBALS = {"Math", "String", "Number", "Object", "Array", "Map", "Set", "Date", "JSON", "console", "window",
            "document", "parseInt", "parseFloat", "isNaN", "isFinite", "Promise", "Symbol", "Error", "RegExp", "Function", "Boolean",
            "Proxy", "Reflect", "Intl", "globalThis", "performance", "setTimeout", "requestAnimationFrame", "Color", "Font", "FontMetrics",
            "BasicStroke", "GradientPaint", "LinearGradientPaint", "RadialGradientPaint", "AlphaComposite", "RenderingHints", "Rect2D", "Rectangle2D",
            "RoundRectangle2D", "Ellipse2D", "Line2D", "Arc2D", "Point2D", "Path2D", "JPath", "Area", "AffineTransform", "Graphics2D",
            "BufferedImage", "Dimension", "Timer", "JPanelBase", "KeyEvent", "SwingUtilities", "HashMap", "JSet", "StringBuilder", "Random",
            "LinkedHashMap", "HAS_DOM", "NativePath2D", "Float64Array", "Int32Array", "WeakMap", "label"};

    public static void main(String[] a) throws Exception {
        new J2JS().run(a[0], a[1]);
    }

    // =================================================================================================== driver
    void run(String src, String outDir) throws Exception {
        JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fm = jc.getStandardFileManager(null, null, StandardCharsets.UTF_8);
        JavacTask task = (JavacTask) jc.getTask(null, fm, null, Arrays.asList("-proc:none", "-encoding", "UTF-8", "-nowarn", "-Xlint:none"), null,
                fm.getJavaFileObjects(new File(src)));
        List<CompilationUnitTree> cus = new ArrayList<>();
        for (CompilationUnitTree cu : task.parse()) cus.add(cu);
        task.analyze();
        Context ctx = ((JavacTaskImpl) task).getContext();
        types = Types.instance(ctx);
        syms = Symtab.instance(ctx);
        JCCompilationUnit cu = (JCCompilationUnit) cus.get(0);
        source = cu.getSourceFile().getCharContent(true);
        JCClassDecl main = null;
        for (JCTree t : cu.defs) if (t instanceof JCClassDecl && ((JCClassDecl) t).name.toString().equals(MAIN)) main = (JCClassDecl) t;
        mainSym = main.sym;
        classRename.put("Promise", "JPromise");
        classRename.put("Tutorial.Step", "TutorialStep");
        // global names: classes, top-level functions and fields
        List<JCClassDecl> classes = new ArrayList<>();
        collectClasses(main, classes);
        for (JCClassDecl c : classes) globalNames.add(jsName(c.sym));
        Set<String> topMethods = new HashSet<>();
        for (JCTree t : main.defs) if (t instanceof JCMethodDecl && ((JCMethodDecl) t).sym.isStatic()) topMethods.add(((JCMethodDecl) t).name.toString());
        for (JCTree t : main.defs) if (t instanceof JCVariableDecl && ((JCVariableDecl) t).sym.isStatic()) {
            String n = ((JCVariableDecl) t).name.toString();
            if (topMethods.contains(n)) topFieldsRenamed.add(n);
        }
        for (JCTree t : main.defs) {
            if (t instanceof JCMethodDecl && ((JCMethodDecl) t).sym.isStatic()) globalNames.add(methodName(((JCMethodDecl) t).sym));
            if (t instanceof JCVariableDecl && ((JCVariableDecl) t).sym.isStatic()) globalNames.add(topFieldName(((JCVariableDecl) t).sym));
        }
        globalNames.addAll(topMethods);
        globalNames.addAll(Arrays.asList(JS_GLOBALS));

        StringBuilder cls = new StringBuilder();
        for (JCClassDecl c : classes) if (c.sym != mainSym) emitClass(c, cls);
        emitClass(main, cls);
        StringBuilder reg = new StringBuilder("const $CLASSES = {");
        reg.append(MAIN);
        for (JCClassDecl c : classes) if (c.sym != mainSym) reg.append(", ").append(jsName(c.sym));
        reg.append("};\n");
        // Java's default values for every instance field: a field missing from an old save reads as 0, false or null, as in Java
        reg.append("const $JDEF = {");
        boolean firstC = true;
        for (JCClassDecl c : classes) {
            StringBuilder f = new StringBuilder();
            for (JCTree t : c.defs) if (t instanceof JCVariableDecl && !((JCVariableDecl) t).sym.isStatic()) {
                JCVariableDecl v = (JCVariableDecl) t;
                if (f.length() > 0) f.append(", ");
                f.append(v.name).append(": ").append(defaultValue(v.sym.type));
            }
            if (f.length() == 0) continue;
            reg.append(firstC ? "" : ",\n").append(jsName(c.sym)).append(": {").append(f).append("}");
            firstC = false;
        }
        reg.append("};\n");
        StringBuilder fns = new StringBuilder();
        Map<String, List<JCMethodDecl>> over = new LinkedHashMap<>();
        for (JCTree t : main.defs) if (t instanceof JCMethodDecl) {
            JCMethodDecl m = (JCMethodDecl) t;
            if (!m.sym.isStatic() || m.name.toString().equals("<init>")) continue;
            if (skipFunctions.contains(m.name.toString())) continue;
            curClass = MAIN;
            emitFunction(m, fns);
            over.computeIfAbsent(m.name.toString(), k -> new ArrayList<>()).add(m);
        }
        for (Map.Entry<String, List<JCMethodDecl>> e : over.entrySet()) if (e.getValue().size() > 1) {
            fns.append("function ").append(e.getKey()).append("(...a) { switch (a.length) {\n");
            for (JCMethodDecl m : e.getValue()) fns.append("case ").append(m.params.size()).append(": return ").append(methodName(m.sym)).append("(...a);\n");
            fns.append("} }\n");
        }
        StringBuilder st = new StringBuilder();
        emitStatics(main, st);
        Files.write(Paths.get(outDir, "classes.js"), cls.toString().getBytes(StandardCharsets.UTF_8));
        Files.write(Paths.get(outDir, "registry.js"), reg.toString().getBytes(StandardCharsets.UTF_8));
        Files.write(Paths.get(outDir, "functions.js"), fns.toString().getBytes(StandardCharsets.UTF_8));
        Files.write(Paths.get(outDir, "statics.js"), st.toString().getBytes(StandardCharsets.UTF_8));
        Set<String> seen = new LinkedHashSet<>(warnings);
        for (String w : seen) System.err.println("WARN " + w);
    }

    void collectClasses(JCClassDecl c, List<JCClassDecl> out) {
        for (JCTree t : c.defs) if (t instanceof JCClassDecl) {
            JCClassDecl n = (JCClassDecl) t;
            if ((n.sym.flags() & Flags.INTERFACE) != 0) continue;
            collectClasses(n, out);
            out.add(n);
        }
        if (c.sym == mainSym) out.add(c);
    }

    // =================================================================================================== naming
    boolean isGame(Symbol s) {
        if (s == null) return false;
        Symbol o = s;
        while (o != null && !(o instanceof PackageSymbol)) { if (o == mainSym) return true; o = o.owner; }
        return false;
    }

    String jsName(ClassSymbol c) {
        if (c == mainSym) return MAIN;
        String simple = c.name.toString();
        String full = c.owner instanceof ClassSymbol && c.owner != mainSym ? ((ClassSymbol) c.owner).name + "." + simple : simple;
        if (classRename.containsKey(full)) return classRename.get(full);
        if (classRename.containsKey(simple)) return classRename.get(simple);
        return simple;
    }

    String typeRef(Symbol s) {
        if (!(s instanceof ClassSymbol)) return s.name.toString();
        ClassSymbol c = (ClassSymbol) s;
        if (isGame(c)) return jsName(c);
        String q = c.getQualifiedName().toString();
        switch (q) {
            case "java.lang.String": return "String";
            case "java.lang.Math": return "Math";
            case "java.util.ArrayList": case "java.util.List": return "Array";
            case "java.awt.geom.GeneralPath": return "Path2D.Double";
            default: break;
        }
        // nested library classes like Rectangle2D.Double keep their outer name
        if (c.owner instanceof ClassSymbol) return ((ClassSymbol) c.owner).name + "." + c.name;
        return c.name.toString();
    }

    String topFieldName(VarSymbol v) {
        String n = v.name.toString();
        return topFieldsRenamed.contains(n) ? "$" + n : n;
    }

    /** Methods with the same name in a game class (and its game superclasses) get the parameter count appended. */
    String methodName(MethodSymbol m) {
        String n = m.name.toString();
        if (!(m.owner instanceof ClassSymbol)) return n;
        ClassSymbol c = (ClassSymbol) m.owner;
        if (!isGame(c)) return n;
        if (overridesLibrary(m)) return n;
        int count = 0;
        for (ClassSymbol k = c; k != null && isGame(k); k = k.getSuperclass() != null && k.getSuperclass().tsym instanceof ClassSymbol ? (ClassSymbol) k.getSuperclass().tsym : null)
            for (Symbol s : k.members().getSymbolsByName(m.name)) if (s instanceof MethodSymbol && !s.isConstructor()) count++;
        if (count > 1) return n + "$" + m.params().size();
        // a method sharing its name with a field of the class (or with a Function property, for statics) gets a trailing $
        if (c != mainSym || !m.isStatic()) for (Symbol s : c.members().getSymbolsByName(m.name)) if (s instanceof VarSymbol && (c != mainSym || !s.isStatic())) return n + "$";
        if (m.isStatic() && FN_PROPS.contains(n)) return n + "$";
        if (c == mainSym && m.isStatic()) return n;
        return n;
    }

    static final Set<String> FN_PROPS = new HashSet<>(Arrays.asList("apply", "call", "bind", "name", "length", "prototype", "caller", "arguments", "toString"));

    boolean overridesLibrary(MethodSymbol m) {
        ClassSymbol c = (ClassSymbol) m.owner;
        for (Type st : types.closure(c.type)) {
            if (st.tsym == c || isGame(st.tsym)) continue;
            for (Symbol s : st.tsym.members().getSymbolsByName(m.name)) if (s instanceof MethodSymbol && m.overrides(s, c, types, false)) return true;
        }
        return false;
    }

    String localName(Symbol s) {
        String n = localNames.get(s);
        if (n != null) return n;
        n = s.name.toString();
        if (RESERVED.contains(n) || globalNames.contains(n)) n = n + "_";
        localNames.put(s, n);
        return n;
    }

    boolean isLocal(Symbol s) { return s instanceof VarSymbol && s.owner != null && s.owner.kind == Kinds.Kind.MTH; }

    // =================================================================================================== classes
    void emitClass(JCClassDecl cd, StringBuilder o) {
        String name = jsName(cd.sym);
        curClass = name;
        boolean empty = emptyClasses.contains(name);
        String ext = "";
        if (cd.sym == mainSym) ext = " extends JPanelBase";
        else if (cd.extending != null && isGame(cd.extending.type.tsym)) ext = " extends " + jsName((ClassSymbol) cd.extending.type.tsym);
        o.append("class ").append(name).append(ext).append(" {\n");
        List<JCMethodDecl> ctors = new ArrayList<>(), methods = new ArrayList<>();
        List<JCTree> inits = new ArrayList<>();
        for (JCTree t : cd.defs) {
            if (t instanceof JCMethodDecl) {
                JCMethodDecl m = (JCMethodDecl) t;
                if (m.name.toString().equals("<init>")) { if ((m.sym.flags() & Flags.GENERATEDCONSTR) == 0) ctors.add(m); }
                else if (cd.sym != mainSym || !m.sym.isStatic()) methods.add(m);
            } else if (t instanceof JCVariableDecl && !((JCVariableDecl) t).sym.isStatic()) inits.add(t);
            else if (t instanceof JCBlock && (((JCBlock) t).flags & Flags.STATIC) == 0) inits.add(t);
        }
        if (!empty && (!inits.isEmpty() || !ctors.isEmpty() || !ext.isEmpty())) {
            if (ctors.size() <= 1) {
                JCMethodDecl k = ctors.isEmpty() ? null : ctors.get(0);
                o.append("constructor(").append(k == null ? "" : params(k.params)).append(") {\n");
                List<JCStatement> body = k == null ? new ArrayList<>() : new ArrayList<>(k.body.stats);
                if (!ext.isEmpty()) {
                    if (!body.isEmpty() && isSuperCall(body.get(0))) { o.append(stmt(body.remove(0))); }
                    else o.append("super();\n");
                } else if (!body.isEmpty() && isSuperCall(body.get(0))) body.remove(0);
                fieldInits(inits, o);
                if (!body.isEmpty()) o.append("{\n").append(stmts(body)).append("}\n");
                o.append("}\n");
            } else {
                o.append("constructor(...a) {\n").append(ext.isEmpty() ? "" : "super();\n").append("this.$fields();\nswitch (a.length) {\n");
                for (JCMethodDecl k : ctors) o.append("case ").append(k.params.size()).append(": this.$ctor").append(k.params.size()).append("(...a); break;\n");
                o.append("}\n}\n$fields() {\n");
                fieldInits(inits, o);
                o.append("}\n");
                for (JCMethodDecl k : ctors) {
                    o.append("$ctor").append(k.params.size()).append("(").append(params(k.params)).append(") {\n");
                    for (JCStatement s : k.body.stats) if (!isSuperCall(s)) o.append(stmt(s));
                    o.append("}\n");
                }
            }
        }
        Map<String, List<JCMethodDecl>> over = new LinkedHashMap<>();
        if (!empty) for (JCMethodDecl m : methods) {
            if (m.body == null) continue;
            if (skipMethods.contains(name + "." + m.name)) continue;
            String mn = methodName(m.sym);
            o.append(m.sym.isStatic() ? "static " : "").append(mn).append("(").append(params(m.params)).append(") ");
            o.append(block(m.body));
            over.computeIfAbsent((m.sym.isStatic() ? "static " : "") + m.name, x -> new ArrayList<>()).add(m);
        }
        for (Map.Entry<String, List<JCMethodDecl>> e : over.entrySet()) if (e.getValue().size() > 1) {
            boolean stat = e.getKey().startsWith("static ");
            o.append(e.getKey()).append("(...a) { switch (a.length) {\n");
            for (JCMethodDecl m : e.getValue()) o.append("case ").append(m.params.size()).append(": return ").append(stat ? name : "this").append(".").append(methodName(m.sym)).append("(...a);\n");
            o.append("} }\n");
        }
        o.append("}\n");
    }

    void fieldInits(List<JCTree> inits, StringBuilder o) {
        for (JCTree t : inits) {
            if (t instanceof JCVariableDecl) {
                JCVariableDecl v = (JCVariableDecl) t;
                o.append("this.").append(v.name).append(" = ").append(v.init != null ? expr(v.init) : defaultValue(v.sym.type)).append(";\n");
            } else o.append(block((JCBlock) t));
        }
    }

    boolean isSuperCall(JCStatement s) {
        if (!(s instanceof JCExpressionStatement)) return false;
        JCExpression e = ((JCExpressionStatement) s).expr;
        if (!(e instanceof JCMethodInvocation)) return false;
        JCExpression m = ((JCMethodInvocation) e).meth;
        return m instanceof JCIdent && (((JCIdent) m).name.toString().equals("super"));
    }

    String defaultValue(Type t) {
        switch (t.getTag()) {
            case BOOLEAN: return "false";
            case INT: case LONG: case SHORT: case BYTE: case CHAR: case DOUBLE: case FLOAT: return "0";
            default: return "null";
        }
    }

    String params(List<JCVariableDecl> ps) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < ps.size(); i++) {
            if (i > 0) b.append(", ");
            if (i == ps.size() - 1 && (ps.get(i).mods.flags & Flags.VARARGS) != 0) b.append("...");
            b.append(localName(ps.get(i).sym));
        }
        return b.toString();
    }

    void emitFunction(JCMethodDecl m, StringBuilder o) {
        o.append("function ").append(methodName(m.sym)).append("(").append(params(m.params)).append(") ").append(block(m.body));
    }

    // =================================================================================================== statics
    void emitStatics(JCClassDecl cd, StringBuilder o) {
        String owner = cd.sym == mainSym ? null : jsName(cd.sym);
        for (JCTree t : cd.defs) {
            if (t instanceof JCVariableDecl && ((JCVariableDecl) t).sym.isStatic()) {
                JCVariableDecl v = (JCVariableDecl) t;
                curClass = owner == null ? MAIN : owner;
                if (emptyClasses.contains(curClass) && !v.name.toString().equals("TEXT")) continue;
                String val = v.init != null ? expr(v.init) : defaultValue(v.sym.type);
                if (owner == null) o.append("var ").append(topFieldName(v.sym)).append(" = ").append(val).append(";\n");
                else o.append(owner).append(".").append(v.name).append(" = ").append(val).append(";\n");
            } else if (t instanceof JCBlock && (((JCBlock) t).flags & Flags.STATIC) != 0) {
                curClass = owner == null ? MAIN : owner;
                o.append(block((JCBlock) t));
            } else if (t instanceof JCClassDecl) {
                JCClassDecl n = (JCClassDecl) t;
                if ((n.sym.flags() & Flags.INTERFACE) != 0) continue;
                emitStatics(n, o);
            }
        }
    }

    // =================================================================================================== statements
    String block(JCBlock b) { return "{\n" + stmts(b.stats) + "}\n"; }

    /** Statements in order; declarations that shared one Java statement share one let. */
    String stmts(List<JCStatement> l) {
        StringBuilder o = new StringBuilder();
        for (int i = 0; i < l.size(); i++) {
            JCStatement s = l.get(i);
            if (s instanceof JCVariableDecl) {
                JCVariableDecl v = (JCVariableDecl) s;
                StringBuilder d = new StringBuilder("let " + localName(v.sym) + (v.init != null ? " = " + expr(v.init) : ""));
                while (i + 1 < l.size() && l.get(i + 1) instanceof JCVariableDecl && ((JCVariableDecl) l.get(i + 1)).vartype == v.vartype) {
                    JCVariableDecl w = (JCVariableDecl) l.get(++i);
                    d.append(", ").append(localName(w.sym)).append(w.init != null ? " = " + expr(w.init) : "");
                }
                o.append(d).append(";\n");
            } else o.append(stmt(s));
        }
        return o.toString();
    }

    /** A loop or if body: blocks as blocks, single statements wrapped in braces on one line. */
    String body(JCStatement s) {
        if (s instanceof JCBlock) return block((JCBlock) s);
        String x = stmt(s).trim();
        return "{ " + x + " }\n";
    }

    String stmt(JCStatement s) {
        if (s instanceof JCVariableDecl) {
            JCVariableDecl v = (JCVariableDecl) s;
            return "let " + localName(v.sym) + (v.init != null ? " = " + expr(v.init) : "") + ";\n";
        }
        if (s instanceof JCExpressionStatement) return expr(((JCExpressionStatement) s).expr) + ";\n";
        if (s instanceof JCBlock) return block((JCBlock) s);
        if (s instanceof JCIf) {
            JCIf f = (JCIf) s;
            String r = "if " + expr(f.cond) + " " + body(f.thenpart);
            if (f.elsepart != null) {
                r = r.substring(0, r.length() - 1);
                if (f.elsepart instanceof JCIf) r += " else { " + stmt(f.elsepart).trim() + " }\n";
                else r += " else " + body(f.elsepart);
            }
            return r;
        }
        if (s instanceof JCForLoop) {
            JCForLoop f = (JCForLoop) s;
            StringBuilder b = new StringBuilder("for (");
            boolean decl = !f.init.isEmpty() && f.init.get(0) instanceof JCVariableDecl;
            if (decl) b.append("let ");
            for (int i = 0; i < f.init.size(); i++) {
                if (i > 0) b.append(", ");
                JCStatement x = f.init.get(i);
                if (x instanceof JCVariableDecl) { JCVariableDecl v = (JCVariableDecl) x; b.append(localName(v.sym)).append(v.init != null ? " = " + expr(v.init) : ""); }
                else b.append(expr(((JCExpressionStatement) x).expr));
            }
            b.append("; ").append(f.cond == null ? "" : expr(f.cond)).append("; ");
            for (int i = 0; i < f.step.size(); i++) { if (i > 0) b.append(", "); b.append(expr(f.step.get(i).expr)); }
            b.append(") ").append(body(f.body));
            return b.toString();
        }
        if (s instanceof JCEnhancedForLoop) {
            JCEnhancedForLoop f = (JCEnhancedForLoop) s;
            boolean assigned = assignedIn(f.body, f.var.sym);
            return "for (" + (assigned ? "let " : "const ") + localName(f.var.sym) + " of " + iterable(f.expr) + ") " + body(f.body);
        }
        if (s instanceof JCWhileLoop) { JCWhileLoop w = (JCWhileLoop) s; return "while " + expr(w.cond) + " " + body(w.body); }
        if (s instanceof JCDoWhileLoop) { JCDoWhileLoop w = (JCDoWhileLoop) s; String b = body(w.body); return "do " + b.substring(0, b.length() - 1) + " while " + expr(w.cond) + ";\n"; }
        if (s instanceof JCSwitch) {
            JCSwitch w = (JCSwitch) s;
            StringBuilder b = new StringBuilder("switch " + expr(w.selector) + " {\n");
            for (JCCase c : w.cases) {
                if (c.labels.isEmpty() || c.labels.head instanceof JCDefaultCaseLabel) b.append("default:\n");
                else for (JCCaseLabel l : c.labels) {
                    if (l instanceof JCDefaultCaseLabel) b.append("default:\n");
                    else b.append("case ").append(expr(((JCConstantCaseLabel) l).expr)).append(":\n");
                }
                b.append(stmts(c.stats));
            }
            b.append("}\n");
            return b.toString();
        }
        if (s instanceof JCBreak) { JCBreak x = (JCBreak) s; return "break" + (x.label != null ? " " + x.label : "") + ";\n"; }
        if (s instanceof JCContinue) { JCContinue x = (JCContinue) s; return "continue" + (x.label != null ? " " + x.label : "") + ";\n"; }
        if (s instanceof JCReturn) { JCReturn x = (JCReturn) s; return "return" + (x.expr != null ? " " + expr(x.expr) : "") + ";\n"; }
        if (s instanceof JCThrow) return "throw " + expr(((JCThrow) s).expr) + ";\n";
        if (s instanceof JCTry) {
            JCTry t = (JCTry) s;
            StringBuilder b = new StringBuilder("try {\n");
            for (JCTree r : t.resources) if (r instanceof JCVariableDecl) b.append(stmt((JCVariableDecl) r));
            String inner = block(t.body);
            b.append(inner.substring(2));
            b.setLength(b.length() - 1);
            if (!t.catchers.isEmpty()) {
                JCCatch c = t.catchers.head;
                b.append(" catch (").append(localName(c.param.sym)).append(") ");
                String cb = block(c.body);
                b.append(cb, 0, cb.length() - 1);
            }
            if (t.finalizer != null) { String fb = block(t.finalizer); b.append(" finally ").append(fb, 0, fb.length() - 1); }
            if (t.catchers.isEmpty() && t.finalizer == null) b.append(" catch ($e) { }");
            return b.append("\n").toString();
        }
        if (s instanceof JCLabeledStatement) { JCLabeledStatement l = (JCLabeledStatement) s; return l.label + ": " + stmt(l.body); }
        if (s instanceof JCSkip) return ";\n";
        if (s instanceof JCSynchronized) return block(((JCSynchronized) s).body);
        if (s instanceof JCAssert) return "";
        if (s instanceof JCClassDecl) { warn("local class " + ((JCClassDecl) s).name); return "/* local class */\n"; }
        warn("statement " + s.getClass().getSimpleName());
        return "/* ? */;\n";
    }

    boolean assignedIn(JCTree t, Symbol v) {
        final boolean[] r = {false};
        new TreeScanner() {
            public void visitAssign(JCAssign a) { if (TreeInfo.symbol(a.lhs) == v) r[0] = true; super.visitAssign(a); }
            public void visitAssignop(JCAssignOp a) { if (TreeInfo.symbol(a.lhs) == v) r[0] = true; super.visitAssignop(a); }
            public void visitUnary(JCUnary u) { if ((u.getTag() == JCTree.Tag.PREINC || u.getTag() == JCTree.Tag.PREDEC || u.getTag() == JCTree.Tag.POSTINC || u.getTag() == JCTree.Tag.POSTDEC) && TreeInfo.symbol(u.arg) == v) r[0] = true; super.visitUnary(u); }
        }.scan(t);
        return r[0];
    }

    String iterable(JCExpression e) {
        Type t = e.type;
        if (t != null && t.tsym != null) {
            String q = t.tsym.getQualifiedName().toString();
            if (q.equals("java.util.Map") || q.equals("java.util.HashMap")) warn("iterating a map directly");
        }
        return expr(e);
    }

    void warn(String w) { warnings.add(curClass + ": " + w); }

    // =================================================================================================== expressions
    boolean integral(Type t) {
        if (t == null) return false;
        Type u = types.unboxedTypeOrType(t);
        switch (u.getTag()) { case INT: case LONG: case SHORT: case BYTE: case CHAR: return true; default: return false; }
    }

    boolean floating(Type t) {
        if (t == null) return false;
        Type u = types.unboxedTypeOrType(t);
        return u.getTag() == TypeTag.DOUBLE || u.getTag() == TypeTag.FLOAT;
    }

    boolean isChar(Type t) { return t != null && types.unboxedTypeOrType(t).getTag() == TypeTag.CHAR; }

    boolean isString(Type t) { return t != null && t.tsym != null && t.tsym.getQualifiedName().toString().equals("java.lang.String"); }

    /** A value converted for string concatenation. */
    String str(JCExpression e) {
        String x = expr(e);
        if (floating(e.type)) return "$ds(" + x + ")";
        if (isChar(e.type)) return "String.fromCharCode(" + x + ")";
        return x;
    }

    String expr(JCExpression e) {
        if (e instanceof JCParens) return "(" + expr(((JCParens) e).expr) + ")";
        if (e instanceof JCLiteral) return literal((JCLiteral) e);
        if (e instanceof JCIdent) return ident((JCIdent) e);
        if (e instanceof JCFieldAccess) return select((JCFieldAccess) e);
        if (e instanceof JCMethodInvocation) return call((JCMethodInvocation) e);
        if (e instanceof JCNewClass) return newClass((JCNewClass) e);
        if (e instanceof JCNewArray) return newArray((JCNewArray) e);
        if (e instanceof JCAssign) { JCAssign a = (JCAssign) e; return expr(a.lhs) + " = " + expr(a.rhs); }
        if (e instanceof JCAssignOp) return assignOp((JCAssignOp) e);
        if (e instanceof JCUnary) return unary((JCUnary) e);
        if (e instanceof JCBinary) return binary((JCBinary) e);
        if (e instanceof JCConditional) { JCConditional c = (JCConditional) e; return "(" + expr(c.cond) + " ? " + expr(c.truepart) + " : " + expr(c.falsepart) + ")"; }
        if (e instanceof JCTypeCast) return cast((JCTypeCast) e);
        if (e instanceof JCInstanceOf) {
            JCInstanceOf i = (JCInstanceOf) e;
            Type t = i.pattern.type;
            String q = t.tsym.getQualifiedName().toString();
            if (q.equals("java.lang.String")) return "(typeof " + expr(i.expr) + " === 'string')";
            return "(" + expr(i.expr) + " instanceof " + typeRef(t.tsym) + ")";
        }
        if (e instanceof JCArrayAccess) { JCArrayAccess a = (JCArrayAccess) e; return expr(a.indexed) + "[" + expr(a.index) + "]"; }
        if (e instanceof JCLambda) return lambda((JCLambda) e);
        if (e instanceof JCMemberReference) return memberRef((JCMemberReference) e);
        warn("expression " + e.getClass().getSimpleName());
        return "null";
    }

    String literal(JCLiteral l) {
        Object v = l.value;
        switch (l.typetag) {
            case BOT: return "null";
            case BOOLEAN: return ((Integer) v) != 0 ? "true" : "false";
            case CHAR: return String.valueOf(((Integer) v).intValue());
            case INT: case LONG: case SHORT: case BYTE: case DOUBLE: case FLOAT: {
                String t = numText(l.pos);
                if (t != null) return t;
                if (l.typetag == TypeTag.DOUBLE) return num(((Number) v).doubleValue());
                if (l.typetag == TypeTag.FLOAT) return Float.toString(((Number) v).floatValue());
                return String.valueOf(v);
            }
            case CLASS: return jsString((String) v);
            default: return String.valueOf(v);
        }
    }

    static final java.util.regex.Pattern NUM = java.util.regex.Pattern.compile("0[xX][0-9a-fA-F_]+[lL]?|(?:[0-9][0-9_]*\\.?[0-9_]*|\\.[0-9_]+)(?:[eE][+-]?[0-9]+)?[fFdDlL]?");

    /** The literal as written in the source (suffixes and underscores dropped). */
    String numText(int pos) {
        if (pos < 0 || pos >= source.length()) return null;
        java.util.regex.Matcher m = NUM.matcher(source);
        m.region(pos, Math.min(source.length(), pos + 40));
        if (!m.lookingAt()) return null;
        String t = m.group().replace("_", "");
        if (!t.startsWith("0x") && !t.startsWith("0X")) t = t.replaceAll("[fFdDlL]$", "");
        else t = t.replaceAll("[lL]$", "");
        if (t.endsWith(".")) t = t + "0";
        if (t.startsWith(".")) t = "0" + t;
        if (t.length() > 1 && t.charAt(0) == '0' && Character.isDigit(t.charAt(1)) && !t.contains(".") && !t.contains("e") && !t.contains("E")) t = String.valueOf(Long.parseLong(t, 8));
        return t;
    }

    static String num(double d) {
        if (Double.isNaN(d)) return "NaN";
        if (Double.isInfinite(d)) return d > 0 ? "Infinity" : "-Infinity";
        return Double.toString(d);
    }

    static String jsString(String s) {
        StringBuilder b = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': b.append("\\\""); break;
                case '\\': b.append("\\\\"); break;
                case '\n': b.append("\\n"); break;
                case '\r': b.append("\\r"); break;
                case '\t': b.append("\\t"); break;
                default:
                    if (c < 32 || c > 126) b.append(String.format("\\u%04x", (int) c)); else b.append(c);
            }
        }
        return b.append("\"").toString();
    }

    /** A compile-time constant from a library class, written as a literal. */
    String libConst(VarSymbol v) {
        Object c = v.getConstValue();
        if (c == null) return null;
        if (c instanceof String) return jsString((String) c);
        if (c instanceof Double || c instanceof Float) return num(((Number) c).doubleValue());
        if (c instanceof Character) return String.valueOf((int) (Character) c);
        if (v.type.getTag() == TypeTag.BOOLEAN) return ((Integer) c) != 0 ? "true" : "false";
        return String.valueOf(c);
    }

    String ident(JCIdent id) {
        Symbol s = id.sym;
        String n = id.name.toString();
        if (n.equals("this")) return "this";
        if (n.equals("super")) return "super";
        if (s instanceof VarSymbol) {
            if (isLocal(s)) return localName(s);
            if (s.isStatic()) return staticField((VarSymbol) s);
            return "this." + s.name;
        }
        if (s instanceof ClassSymbol) return typeRef(s);
        warn("ident " + n + " kind " + (s == null ? "null" : s.kind));
        return n;
    }

    String staticField(VarSymbol v) {
        ClassSymbol c = (ClassSymbol) v.owner;
        if (isGame(c)) return c == mainSym ? topFieldName(v) : jsName(c) + "." + v.name;
        if (c.getQualifiedName().toString().equals("java.lang.Math")) return "Math." + v.name;
        String k = libConst(v);
        if (k != null) return k;
        String q = c.getQualifiedName() + "." + v.name;
        switch (q) {
            case "java.lang.System.out": return "console";
            case "java.lang.System.err": return "console";
            default: break;
        }
        return typeRef(c) + "." + v.name;
    }

    String select(JCFieldAccess f) {
        Symbol s = f.sym;
        String n = f.name.toString();
        if (n.equals("this")) return "this";
        if (s instanceof VarSymbol) {
            if (n.equals("length") && f.selected.type != null && f.selected.type.getTag() == TypeTag.ARRAY) return expr(f.selected) + ".length";
            if (s.isStatic()) return staticField((VarSymbol) s);
            return expr(f.selected) + "." + n;
        }
        if (s instanceof ClassSymbol) return typeRef(s);
        if (s instanceof PackageSymbol) return "";
        warn("select " + n);
        return expr(f.selected) + "." + n;
    }

    String args(List<JCExpression> as, MethodSymbol m, Type varargsElement) {
        StringBuilder b = new StringBuilder();
        boolean spreadLast = m.isVarArgs() && varargsElement == null && isGame(m) && as.size() == m.params().size();
        for (int i = 0; i < as.size(); i++) {
            if (i > 0) b.append(", ");
            if (spreadLast && i == as.size() - 1) b.append("...");
            b.append(expr(as.get(i)));
        }
        return b.toString();
    }

    String call(JCMethodInvocation mi) {
        Symbol sym = TreeInfo.symbol(mi.meth);
        if (!(sym instanceof MethodSymbol)) { warn("call to non-method"); return "null"; }
        MethodSymbol m = (MethodSymbol) sym;
        String name = m.name.toString();
        JCExpression recv = mi.meth instanceof JCFieldAccess ? ((JCFieldAccess) mi.meth).selected : null;
        boolean superRecv = recv instanceof JCIdent && ((JCIdent) recv).name.toString().equals("super");
        ClassSymbol owner = (ClassSymbol) m.owner;
        if (name.equals("<init>")) {
            // super(...) / this(...) inside constructors
            JCIdent id = (JCIdent) mi.meth;
            if (id.name.toString().equals("super")) return "super(" + args(mi.args, m, mi.varargsElement) + ")";
            return "this.$ctor" + mi.args.size() + "(" + args(mi.args, m, mi.varargsElement) + ")";
        }
        if (isGame(owner)) {
            if ((owner.flags() & Flags.INTERFACE) != 0 && types.isFunctionalInterface(owner)) return (recv == null ? "this" : expr(recv)) + "(" + args(mi.args, m, mi.varargsElement) + ")";
            String mn = methodName(m);
            if (m.isStatic()) return (owner == mainSym ? "" : jsName(owner) + ".") + mn + "(" + args(mi.args, m, mi.varargsElement) + ")";
            if (recv == null) return "this." + mn + "(" + args(mi.args, m, mi.varargsElement) + ")";
            return (superRecv ? "super" : expr(recv)) + "." + mn + "(" + args(mi.args, m, mi.varargsElement) + ")";
        }
        if (superRecv) return "/*super*/undefined";
        // inherited library methods called without a receiver (JPanel's repaint(), getWidth() ...)
        if (recv == null && !m.isStatic()) return "this." + name + "(" + args(mi.args, m, null) + ")";
        if ((owner.flags() & Flags.INTERFACE) != 0 && types.isFunctionalInterface(owner) && !owner.getQualifiedName().toString().startsWith("java.util.List")) {
            Symbol fn = types.findDescriptorSymbol(owner);
            if (fn != null && fn.name.equals(m.name)) return expr(recv) + "(" + args(mi.args, m, null) + ")";
        }
        if (name.equals("clone") && recv != null && recv.type != null && recv.type.getTag() == TypeTag.ARRAY) return expr(recv) + ".slice()";
        return lib(mi, m, owner, recv, name);
    }

    String a(JCMethodInvocation mi, int i) { return expr(mi.args.get(i)); }

    String allArgs(JCMethodInvocation mi) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < mi.args.size(); i++) { if (i > 0) b.append(", "); b.append(expr(mi.args.get(i))); }
        return b.toString();
    }

    String lib(JCMethodInvocation mi, MethodSymbol m, ClassSymbol owner, JCExpression recv, String name) {
        String q = owner.getQualifiedName().toString();
        String r = recv == null ? null : expr(recv);
        int n = mi.args.size();
        Type p0 = n > 0 ? m.params().get(0).type : null;
        switch (q) {
            case "java.lang.Math":
                switch (name) {
                    case "signum": return "Math.sign(" + a(mi, 0) + ")";
                    case "toRadians": return "((" + a(mi, 0) + ") * Math.PI / 180)";
                    case "toDegrees": return "((" + a(mi, 0) + ") * 180 / Math.PI)";
                    case "floorMod": return "((((" + a(mi, 0) + ") % (" + a(mi, 1) + ")) + (" + a(mi, 1) + ")) % (" + a(mi, 1) + "))";
                    case "floorDiv": return "Math.floor((" + a(mi, 0) + ") / (" + a(mi, 1) + "))";
                    case "rint": return "Math.round(" + a(mi, 0) + ")";
                    default: return "Math." + name + "(" + allArgs(mi) + ")";
                }
            case "java.lang.String":
                if (m.isStatic()) {
                    switch (name) {
                        case "format": return "$fmt(" + allArgs(mi) + ")";
                        case "join": return "$join(" + allArgs(mi) + ")";
                        case "valueOf": {
                            JCExpression x = mi.args.get(0);
                            if (floating(x.type)) return "$ds(" + expr(x) + ")";
                            if (isChar(x.type)) return "String.fromCharCode(" + expr(x) + ")";
                            if (x.type.getTag() == TypeTag.ARRAY) return "String.fromCharCode(..." + expr(x) + ")";
                            return "String(" + expr(x) + ")";
                        }
                        default: break;
                    }
                    break;
                }
                switch (name) {
                    case "equals": return "$eq(" + r + ", " + a(mi, 0) + ")";
                    case "equalsIgnoreCase": return "(" + r + ".toLowerCase() === (" + a(mi, 0) + ").toLowerCase())";
                    case "length": return r + ".length";
                    case "charAt": return r + ".charCodeAt(" + a(mi, 0) + ")";
                    case "isEmpty": return "(" + r + ".length === 0)";
                    case "contains": return r + ".includes(" + a(mi, 0) + ")";
                    case "split": return "$split(" + r + ", " + allArgs(mi) + ")";
                    case "replace": if (isChar(p0)) return "$replace(" + r + ", String.fromCharCode(" + a(mi, 0) + "), String.fromCharCode(" + a(mi, 1) + "))";
                        return "$replace(" + r + ", " + allArgs(mi) + ")";
                    case "replaceAll": return "$replaceAll(" + r + ", " + allArgs(mi) + ")";
                    case "indexOf": case "lastIndexOf":
                        if (isChar(p0) || (p0 != null && p0.getTag() == TypeTag.INT)) return r + "." + name + "(String.fromCharCode(" + a(mi, 0) + ")" + (n > 1 ? ", " + a(mi, 1) : "") + ")";
                        return r + "." + name + "(" + allArgs(mi) + ")";
                    case "toCharArray": return "$chars(" + r + ")";
                    case "hashCode": return "$hash(" + r + ")";
                    case "compareTo": return "$natcmp(" + r + ", " + a(mi, 0) + ")";
                    case "matches": return "new RegExp('^(?:' + " + a(mi, 0) + " + ')$').test(" + r + ")";
                    case "toString": case "intern": return r;
                    case "trim": return r + ".trim()";
                    default: return r + "." + name + "(" + allArgs(mi) + ")";
                }
            case "java.lang.StringBuilder": case "java.lang.AbstractStringBuilder": case "java.lang.StringBuffer":
                switch (name) {
                    case "append": return r + ".append(" + str(mi.args.get(0)) + ")";
                    case "insert": return r + ".insert(" + a(mi, 0) + ", " + str(mi.args.get(1)) + ")";
                    case "length": return r + ".length";
                    case "charAt": return r + ".toString().charCodeAt(" + a(mi, 0) + ")";
                    default: return r + "." + name + "(" + allArgs(mi) + ")";
                }
            case "java.util.Arrays":
                switch (name) {
                    case "fill": return n == 2 ? a(mi, 0) + ".fill(" + a(mi, 1) + ")" : a(mi, 0) + ".fill(" + a(mi, 3) + ", " + a(mi, 1) + ", " + a(mi, 2) + ")";
                    case "asList": if (mi.varargsElement == null && n == 1) return "$list(" + a(mi, 0) + ")"; return "[" + allArgs(mi) + "]";
                    case "copyOfRange": return a(mi, 0) + ".slice(" + a(mi, 1) + ", " + a(mi, 2) + ")";
                    case "copyOf": return "$copyOf(" + a(mi, 0) + ", " + a(mi, 1) + ")";
                    case "sort":
                        if (n == 1) return a(mi, 0) + ".sort($cmp)";
                        if (n == 2) return a(mi, 0) + ".sort(" + a(mi, 1) + ")";
                        break;
                    case "toString": return "('[' + " + a(mi, 0) + ".join(', ') + ']')";
                    case "equals": return "$arrEq(" + allArgs(mi) + ")";
                    default: break;
                }
                break;
            case "java.util.Collections":
                switch (name) {
                    case "sort": return n == 1 ? a(mi, 0) + ".sort($natcmp)" : a(mi, 0) + ".sort(" + a(mi, 1) + ")";
                    case "shuffle": return "$shuffle(" + allArgs(mi) + ")";
                    case "reverse": return a(mi, 0) + ".reverse()";
                    case "emptyList": return "[]";
                    case "unmodifiableList": return a(mi, 0);
                    case "swap": return "$swap(" + allArgs(mi) + ")";
                    case "max": return "Math.max(..." + a(mi, 0) + ")";
                    case "min": return "Math.min(..." + a(mi, 0) + ")";
                    default: break;
                }
                break;
            case "java.lang.Integer": case "java.lang.Long": case "java.lang.Short": case "java.lang.Byte":
                switch (name) {
                    case "parseInt": case "parseLong": return "parseInt(" + a(mi, 0) + ", 10)";
                    case "valueOf": return isString(p0) ? "parseInt(" + a(mi, 0) + ", 10)" : a(mi, 0);
                    case "toString": return m.isStatic() ? "String(" + a(mi, 0) + ")" : "String(" + r + ")";
                    case "compare": return "$natcmp(" + allArgs(mi) + ")";
                    case "intValue": case "longValue": return r;
                    case "doubleValue": return r;
                    case "max": case "min": return "Math." + name + "(" + allArgs(mi) + ")";
                    case "signum": return "Math.sign(" + a(mi, 0) + ")";
                    case "toHexString": return "((" + a(mi, 0) + ") >>> 0).toString(16)";
                    case "equals": return "$eq(" + r + ", " + a(mi, 0) + ")";
                    case "hashCode": return m.isStatic() ? "$hash(" + a(mi, 0) + ")" : "$hash(" + r + ")";
                    case "sum": return "(" + a(mi, 0) + " + " + a(mi, 1) + ")";
                    default: break;
                }
                break;
            case "java.lang.Double": case "java.lang.Float":
                switch (name) {
                    case "parseDouble": case "parseFloat": return "parseFloat(" + a(mi, 0) + ")";
                    case "valueOf": return isString(p0) ? "parseFloat(" + a(mi, 0) + ")" : a(mi, 0);
                    case "compare": return "$cmp(" + allArgs(mi) + ")";
                    case "isNaN": return m.isStatic() ? "Number.isNaN(" + a(mi, 0) + ")" : "Number.isNaN(" + r + ")";
                    case "isInfinite": return "(Math.abs(" + (m.isStatic() ? a(mi, 0) : r) + ") === Infinity)";
                    case "isFinite": return "Number.isFinite(" + a(mi, 0) + ")";
                    case "doubleValue": case "floatValue": return r;
                    case "intValue": return "$i(" + r + ")";
                    case "toString": return m.isStatic() ? "$ds(" + a(mi, 0) + ")" : "$ds(" + r + ")";
                    case "max": case "min": return "Math." + name + "(" + allArgs(mi) + ")";
                    case "sum": return "(" + a(mi, 0) + " + " + a(mi, 1) + ")";
                    case "hashCode": return "$hash(" + (m.isStatic() ? a(mi, 0) : r) + ")";
                    default: break;
                }
                break;
            case "java.lang.Boolean":
                switch (name) {
                    case "parseBoolean": return "(" + a(mi, 0) + " === \"true\")";
                    case "booleanValue": return r;
                    case "valueOf": return isString(p0) ? "(" + a(mi, 0) + " === \"true\")" : a(mi, 0);
                    case "toString": return "String(" + (m.isStatic() ? a(mi, 0) : r) + ")";
                    default: break;
                }
                break;
            case "java.lang.Character":
                switch (name) {
                    case "isDigit": return "$isDigit(" + a(mi, 0) + ")";
                    case "isLetter": return "$isLetter(" + a(mi, 0) + ")";
                    case "isLetterOrDigit": return "($isLetter(" + a(mi, 0) + ") || $isDigit(" + a(mi, 0) + "))";
                    case "isUpperCase": return "$isUpper(" + a(mi, 0) + ")";
                    case "isLowerCase": return "$isLower(" + a(mi, 0) + ")";
                    case "isWhitespace": case "isSpaceChar": return "$isSpace(" + a(mi, 0) + ")";
                    case "toUpperCase": return "$ucChar(" + a(mi, 0) + ")";
                    case "toLowerCase": return "$lcChar(" + a(mi, 0) + ")";
                    case "charValue": return r;
                    case "toString": return "String.fromCharCode(" + (m.isStatic() ? a(mi, 0) : r) + ")";
                    case "getNumericValue": return "((" + a(mi, 0) + ") - 48)";
                    default: break;
                }
                break;
            case "java.lang.Object":
                switch (name) {
                    case "equals": return "$eq(" + r + ", " + a(mi, 0) + ")";
                    case "hashCode": return "$ihash(" + r + ")";
                    case "toString": return "String(" + r + ")";
                    default: break;
                }
                break;
            case "java.lang.System":
                switch (name) {
                    case "currentTimeMillis": return "Date.now()";
                    case "nanoTime": return "$nano()";
                    case "arraycopy": return "$arraycopy(" + allArgs(mi) + ")";
                    case "getProperty": return "null";
                    case "exit": return "undefined";
                    case "identityHashCode": return "$ihash(" + a(mi, 0) + ")";
                    default: break;
                }
                break;
            case "java.io.PrintStream":
                switch (name) {
                    case "println": case "print": return "console.log(" + (n == 0 ? "" : isString(mi.args.get(0).type) ? str(mi.args.get(0)) : "String(" + str(mi.args.get(0)) + ")") + ")";
                    case "printf": case "format": return "console.log($fmt(" + allArgs(mi) + "))";
                    case "flush": return "undefined";
                    default: break;
                }
                break;
            case "java.lang.Thread":
                return "undefined";
            case "java.lang.Throwable":
                if (name.equals("printStackTrace")) return "console.error(" + r + ")";
                if (name.equals("getMessage")) return "String(" + r + ")";
                break;
            case "java.util.Set": case "java.util.HashSet":
                switch (name) {
                    case "contains": return r + ".has(" + a(mi, 0) + ")";
                    case "add": return r + ".add(" + a(mi, 0) + ")";
                    case "remove": return r + ".delete(" + a(mi, 0) + ")";
                    case "size": return r + ".size";
                    case "isEmpty": return "(" + r + ".size === 0)";
                    case "clear": return r + ".clear()";
                    default: break;
                }
                break;
            case "java.util.List": case "java.util.ArrayList": case "java.util.Collection": case "java.util.AbstractList": case "java.util.LinkedList":
            case "java.util.AbstractCollection": case "java.lang.Iterable": case "java.util.SequencedCollection":
                switch (name) {
                    case "add": return n == 1 ? r + ".add(" + a(mi, 0) + ")" : r + ".splice(" + a(mi, 0) + ", 0, " + a(mi, 1) + ")";
                    case "get": return r + "[" + a(mi, 0) + "]";
                    case "size": return r + ".length";
                    case "isEmpty": return "(" + r + ".length === 0)";
                    case "contains": return r + ".includes(" + a(mi, 0) + ")";
                    case "clear": return r + ".clear()";
                    case "remove":
                        if (p0 != null && p0.getTag() == TypeTag.INT) return r + ".splice(" + a(mi, 0) + ", 1)[0]";
                        return "$lrem(" + r + ", " + a(mi, 0) + ")";
                    case "indexOf": case "lastIndexOf": return r + "." + name + "(" + a(mi, 0) + ")";
                    case "set": return "$lset(" + r + ", " + a(mi, 0) + ", " + a(mi, 1) + ")";
                    case "addAll": return r + ".addAll(" + a(mi, 0) + ")";
                    case "toArray": return r + ".slice()";
                    case "sort": return r + ".sort(" + (n == 0 || mi.args.get(0) instanceof JCLiteral ? "$natcmp" : a(mi, 0)) + ")";
                    case "subList": return r + ".slice(" + a(mi, 0) + ", " + a(mi, 1) + ")";
                    case "forEach": return r + ".forEach(" + a(mi, 0) + ")";
                    case "removeIf": return "$removeIf(" + r + ", " + a(mi, 0) + ")";
                    case "equals": return "$eq(" + r + ", " + a(mi, 0) + ")";
                    case "getFirst": return r + "[0]";
                    case "getLast": return r + "[" + r + ".length - 1]";
                    default: break;
                }
                break;
            case "java.util.Map": case "java.util.HashMap": case "java.util.LinkedHashMap": case "java.util.AbstractMap":
                switch (name) {
                    case "getOrDefault": return "$mget(" + r + ", " + a(mi, 0) + ", " + a(mi, 1) + ")";
                    case "get": case "put": case "containsKey": case "remove": case "size": case "isEmpty": case "clear": case "values": case "keySet":
                        return r + "." + name + "(" + allArgs(mi) + ")";
                    default: break;
                }
                break;
            case "java.util.Random":
                return r + "." + name + "(" + allArgs(mi) + ")";
            case "java.lang.Enum":
                break;
            default:
                if (q.startsWith("java.awt") || q.startsWith("javax.swing") || q.startsWith("java.awt.image") || q.startsWith("java.awt.geom")) {
                    if (r == null) return typeRef(owner) + "." + name + "(" + allArgs(mi) + ")";
                    return r + "." + name + "(" + allArgs(mi) + ")";
                }
                break;
        }
        warn("library call " + q + "." + name + "/" + n);
        if (m.isStatic()) return typeRef(owner) + "." + name + "(" + allArgs(mi) + ")";
        return (r == null ? "this" : r) + "." + name + "(" + allArgs(mi) + ")";
    }

    String newClass(JCNewClass nc) {
        ClassSymbol c = (ClassSymbol) nc.type.tsym;
        if (nc.def != null) return anonymous(nc);
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < nc.args.size(); i++) { if (i > 0) b.append(", "); b.append(expr(nc.args.get(i))); }
        String args = b.toString();
        if (isGame(c)) return "new " + jsName(c) + "(" + args + ")";
        String q = c.getQualifiedName().toString();
        switch (q) {
            case "java.util.ArrayList": case "java.util.LinkedList": case "java.util.Vector": case "java.util.ArrayDeque":
                return nc.args.isEmpty() ? "[]" : "$list(" + args + ")";
            case "java.util.HashMap": case "java.util.LinkedHashMap": case "java.util.TreeMap": return "new HashMap()";
            case "java.util.HashSet": case "java.util.LinkedHashSet": case "java.util.TreeSet": return nc.args.isEmpty() ? "new JSet()" : "new JSet(" + args + ")";
            case "java.lang.StringBuilder": case "java.lang.StringBuffer": return "new StringBuilder(" + args + ")";
            case "java.util.Random": return "new Random(" + args + ")";
            case "java.lang.String": return nc.args.isEmpty() ? "\"\"" : "String.fromCharCode(..." + args + ")";
            case "java.lang.RuntimeException": case "java.lang.IllegalStateException": case "java.lang.IllegalArgumentException": case "java.lang.Exception":
            case "java.lang.Error": case "java.lang.UnsupportedOperationException":
                return "new Error(" + args + ")";
            case "java.awt.geom.GeneralPath": return "new Path2D.Double(" + args + ")";
            default: break;
        }
        if (q.startsWith("java.awt") || q.startsWith("javax.swing")) return "new " + typeRef(c) + "(" + args + ")";
        warn("new " + q);
        return "new " + typeRef(c) + "(" + args + ")";
    }

    String anonymous(JCNewClass nc) {
        JCClassDecl d = nc.def;
        List<JCMethodDecl> ms = new ArrayList<>();
        for (JCTree t : d.defs) if (t instanceof JCMethodDecl && !((JCMethodDecl) t).name.toString().equals("<init>")) ms.add((JCMethodDecl) t);
        if (ms.size() == 1) {
            JCMethodDecl m = ms.get(0);
            return "(" + params(m.params) + ") => " + block(m.body).trim();
        }
        warn("anonymous class with " + ms.size() + " methods");
        StringBuilder b = new StringBuilder("{");
        for (JCMethodDecl m : ms) b.append(m.name).append(": (").append(params(m.params)).append(") => ").append(block(m.body).trim()).append(", ");
        return b.append("}").toString();
    }

    String newArray(JCNewArray na) {
        if (na.elems != null) {
            StringBuilder b = new StringBuilder("[");
            for (int i = 0; i < na.elems.size(); i++) { if (i > 0) b.append(", "); b.append(expr(na.elems.get(i))); }
            return b.append("]").toString();
        }
        Type t = na.type;
        int rank = 0;
        while (t.getTag() == TypeTag.ARRAY) { t = ((ArrayType) t).elemtype; rank++; }
        String def = defaultValue(t);
        if (na.dims.size() == 1) return "$arr(" + expr(na.dims.get(0)) + ", " + (rank == 1 ? def : "null") + ")";
        StringBuilder b = new StringBuilder("$arrN([");
        for (int i = 0; i < na.dims.size(); i++) { if (i > 0) b.append(", "); b.append(expr(na.dims.get(i))); }
        return b.append("], ").append(rank == na.dims.size() ? def : "null").append(")").toString();
    }

    String opText(JCTree.Tag t) {
        switch (t) {
            case PLUS: case PLUS_ASG: return "+";
            case MINUS: case MINUS_ASG: return "-";
            case MUL: case MUL_ASG: return "*";
            case DIV: case DIV_ASG: return "/";
            case MOD: case MOD_ASG: return "%";
            case BITAND: case BITAND_ASG: return "&";
            case BITOR: case BITOR_ASG: return "|";
            case BITXOR: case BITXOR_ASG: return "^";
            case SL: case SL_ASG: return "<<";
            case SR: case SR_ASG: return ">>";
            case USR: case USR_ASG: return ">>>";
            case EQ: return "===";
            case NE: return "!==";
            case LT: return "<";
            case GT: return ">";
            case LE: return "<=";
            case GE: return ">=";
            case AND: return "&&";
            case OR: return "||";
            default: return "?";
        }
    }

    String binary(JCBinary b) {
        JCTree.Tag t = b.getTag();
        if (t == JCTree.Tag.PLUS && isString(b.type)) return "(" + str(b.lhs) + " + " + str(b.rhs) + ")";
        if (t == JCTree.Tag.DIV && integral(b.lhs.type) && integral(b.rhs.type)) return "$idiv(" + expr(b.lhs) + ", " + expr(b.rhs) + ")";
        boolean bool = b.lhs.type != null && types.unboxedTypeOrType(b.lhs.type).getTag() == TypeTag.BOOLEAN;
        if (bool && t == JCTree.Tag.BITAND) return "(" + expr(b.lhs) + " && " + expr(b.rhs) + ")";
        if (bool && t == JCTree.Tag.BITOR) return "(" + expr(b.lhs) + " || " + expr(b.rhs) + ")";
        if (bool && t == JCTree.Tag.BITXOR) return "(" + expr(b.lhs) + " !== " + expr(b.rhs) + ")";
        if (t == JCTree.Tag.MUL && types.unboxedTypeOrType(b.type).getTag() == TypeTag.INT && !(b.lhs instanceof JCLiteral) && !(b.rhs instanceof JCLiteral) && hashy(b))
            return "Math.imul(" + expr(b.lhs) + ", " + expr(b.rhs) + ")";
        // comparisons with null are loose, so fields missing from an old save (undefined) count as null
        if ((t == JCTree.Tag.EQ || t == JCTree.Tag.NE) && (isNullLit(b.lhs) || isNullLit(b.rhs))) return "(" + expr(b.lhs) + (t == JCTree.Tag.EQ ? " == " : " != ") + expr(b.rhs) + ")";
        return "(" + expr(b.lhs) + " " + opText(t) + " " + expr(b.rhs) + ")";
    }

    static boolean isNullLit(JCExpression e) { while (e instanceof JCParens) e = ((JCParens) e).expr; return e instanceof JCLiteral && ((JCLiteral) e).typetag == TypeTag.BOT; }

    /** int multiplications that are part of hashing (both sides int, inside an expression that also XORs or shifts) - kept exact. */
    boolean hashy(JCBinary b) { return false; }

    String assignOp(JCAssignOp a) {
        JCTree.Tag t = a.getTag();
        String l = expr(a.lhs);
        if (t == JCTree.Tag.PLUS_ASG && isString(a.lhs.type)) return l + " += " + str(a.rhs);
        if (integral(a.lhs.type) && !isChar(a.lhs.type)) {
            if (t == JCTree.Tag.DIV_ASG && integral(a.rhs.type)) return l + " = $idiv(" + l + ", " + expr(a.rhs) + ")";
            if (floating(a.rhs.type)) return l + " = $i(" + l + " " + opText(t) + " " + expr(a.rhs) + ")";
        }
        return l + " " + opText(t) + "= " + expr(a.rhs);
    }

    String unary(JCUnary u) {
        String x = expr(u.arg);
        switch (u.getTag()) {
            case PREINC: return "++" + x;
            case PREDEC: return "--" + x;
            case POSTINC: return x + "++";
            case POSTDEC: return x + "--";
            case NOT: return "!" + x;
            case NEG: return "-" + x;
            case POS: return x;
            case COMPL: return "~" + x;
            default: return x;
        }
    }

    String cast(JCTypeCast c) {
        Type to = c.clazz.type, from = c.expr.type;
        String x = expr(c.expr);
        if (to.isPrimitive()) {
            TypeTag tt = to.getTag();
            if ((tt == TypeTag.INT || tt == TypeTag.LONG || tt == TypeTag.SHORT || tt == TypeTag.BYTE || tt == TypeTag.CHAR) && floating(from)) return "$i(" + x + ")";
            return x;
        }
        return x;
    }

    String lambda(JCLambda l) {
        StringBuilder b = new StringBuilder("(");
        for (int i = 0; i < l.params.size(); i++) { if (i > 0) b.append(", "); b.append(localName(l.params.get(i).sym)); }
        b.append(") => ");
        if (l.body instanceof JCBlock) b.append(block((JCBlock) l.body).trim());
        else b.append("(").append(expr((JCExpression) l.body)).append(")");
        return b.toString();
    }

    String memberRef(JCMemberReference r) {
        Symbol s = r.sym;
        if (s instanceof MethodSymbol) {
            MethodSymbol m = (MethodSymbol) s;
            if (m.isStatic() && isGame(m)) return "((...a) => " + (m.owner == mainSym ? "" : jsName((ClassSymbol) m.owner) + ".") + methodName(m) + "(...a))";
            if (!m.isStatic() && isGame(m)) return "((...a) => " + expr(r.expr) + "." + methodName(m) + "(...a))";
        }
        warn("method reference " + r);
        return "null";
    }
}
