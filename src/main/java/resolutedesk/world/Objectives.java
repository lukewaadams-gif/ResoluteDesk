package resolutedesk.world;

import static resolutedesk.core.Dates.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.model.NewsKinds.*;
import static resolutedesk.world.Countries.*;
import static resolutedesk.world.StatecraftData.*;
import static resolutedesk.world.WorldData.*;

import java.util.ArrayList;
import java.util.List;
import resolutedesk.model.Game;

/**
 * Objectives: concrete goals with each country, each a few steps long. A step is tried from the Situation Room;
 * it may need a tool in place first (military aid, sanctions, troops), costs something when tried, and works or
 * fails on odds set by the step, the relationship and the people doing it. Three failures stall an objective for
 * half a year. Every country has two to four objectives in its era.
 */
public final class Objectives {
    private static List<ObjectiveDef> defs;

    private Objectives() {
    }

    public static List<ObjectiveDef> defs() {
        if (defs == null) {
            List<ObjectiveDef> l = new ArrayList<ObjectiveDef>();
            for (String row : ObjectiveTable.ROWS) l.add(new ObjectiveDef(row));
            defs = l;
        }
        return defs;
    }

    public static ObjectiveDef def(Objective o) {
        String id = o.id.contains(":") ? o.id.substring(0, o.id.indexOf(':')) : o.id;
        for (ObjectiveDef d : defs()) if (d.id.equals(id)) return d;
        return null;
    }

    static boolean has(World w, String id) {
        for (Objective o : w.objectives) if (o.id.equals(id)) return true;
        return false;
    }

    /** Once a year: objectives whose time has come are added; stalled ones reopen; every country gets at least two. */
    public static void refresh(Game g) {
        World w = g.world;
        int y = yearNow(g);
        if (w.objectivesYear == y) return;
        w.objectivesYear = y;
        for (ObjectiveDef d : defs()) {
            if (d.generic() || y < d.from || y > d.to) continue;
            int c = ctIdx(d.who);
            if (c < 0 || gone(g, c) || has(w, d.id)) continue;
            add(w, d.id, c);
        }
        for (int c = 0; c < NC; c++) {
            if (gone(g, c)) continue;
            int n = 0;
            for (Objective o : w.objectives) if (o.country == c && inEra(g, o)) n++;
            if (n >= 2) continue;
            String code = "*" + "NPTA".charAt(align(g, c));
            List<ObjectiveDef> fit = new ArrayList<ObjectiveDef>();
            for (ObjectiveDef d : defs()) if (d.who.equals(code) && y >= d.from && y <= d.to) fit.add(d);
            for (int i = 0; i < fit.size() && n < 2; i++) {         // each country starts at a different template
                String id = fit.get((c + i) % fit.size()).id + ":" + CT_ID[c];
                if (!has(w, id)) { add(w, id, c); n++; }
            }
        }
        for (Objective o : w.objectives) if (o.status == 2 && g.week >= o.tryAfter) { o.status = 0; o.fails = 0; }
    }

    static void add(World w, String id, int c) {
        Objective o = new Objective();
        o.id = id; o.country = c;
        w.objectives.add(o);
    }

    static boolean inEra(Game g, Objective o) {
        ObjectiveDef d = def(o);
        int y = yearNow(g);
        return d != null && y >= d.from && y <= d.to;
    }

    /** A country's objectives for this era. */
    public static List<Objective> of(Game g, int c) {
        List<Objective> l = new ArrayList<Objective>();
        for (Objective o : g.world.objectives) if (o.country == c && (inEra(g, o) || o.status == 1)) l.add(o);
        return l;
    }

    public static String name(Game g, Objective o) { return def(o).name.replace("{C}", cnameIn(g, o.country)); }

    public static String why(Game g, Objective o) { return def(o).why.replace("{C}", cnameIn(g, o.country)); }

    public static String[] step(Objective o) { ObjectiveDef d = def(o); return o.step < d.steps.length ? d.steps[o.step] : null; }

    /** Why the next step can't be tried now, or null. */
    public static String cannot(Game g, Objective o) {
        if (o.status == 1) return "Achieved.";
        if (o.status == 2) return "Stalled after three failures; it can be tried again after " + dateStr(o.tryAfter * 7) + ".";
        if (g.week < o.tryAfter) return "Give it time: the next try can come after " + dateStr(o.tryAfter * 7) + ".";
        if (atWar(g, o.country)) return "Not while the two countries are at war.";
        String[] s = step(o);
        return s == null ? "Achieved." : unmet(g, o.country, s[3]);
    }

    /** The odds the next step works, after the relationship and the people working on it. */
    public static double odds(Game g, Objective o) {
        String[] s = step(o);
        double base = Double.parseDouble(s[2]);
        if (base >= 1) return 1;
        return Diplo.p(g, s[1], base + g.world.rel[o.country] / 500);
    }

    /** Tries the next step, and says what happened. */
    public static String attempt(Game g, Objective o) {
        String no = cannot(g, o);
        if (no != null) return no;
        String[] s = step(o);
        String code = CT_ID[o.country];
        fx(g, s[4], code);
        boolean ok = g.rng.nextDouble() < odds(g, o);
        if (!ok) {
            fx(g, s[6], code);
            o.fails++;
            o.tryAfter = g.week + 4;
            if (o.fails >= 3) { o.status = 2; o.tryAfter = g.week + 26; }
            g.log(NK_WORLD, name(g, o) + ": " + s[0].toLowerCase() + " fails", null, 0.9);
            return s[0] + " didn't work." + (o.status == 2 ? " After three failures this is stalled for six months." : " You can try again in a month.");
        }
        fx(g, s[5], code);
        o.step++;
        o.fails = 0;
        o.tryAfter = g.week + 1;
        if (o.step < def(o).steps.length) return s[0] + ": it worked. Next: " + def(o).steps[o.step][0].toLowerCase() + ".";
        o.status = 1;
        fx(g, def(o).payoff, code);
        g.world.done++;
        g.world.deals++;
        g.world.record.add(dateStr(g.day()) + ": " + name(g, o));
        g.log(NK_GOOD, "Achieved: " + name(g, o).toLowerCase(), why(g, o), 2.2);
        return "Achieved: " + name(g, o) + ".";
    }

    static void fx(Game g, String fx, String code) {
        if (fx == null || fx.isEmpty() || fx.equals("-")) return;
        Diplo.fx(g, fx.replace("{C}", code));
    }

    // ---- requirements: the tools a step needs in place ----

    /** What a requirement such as "milaid>=2" or "troops@PL>=5" still needs, or null if it's met. */
    static String unmet(Game g, int c, String req) {
        if (req == null || req.isEmpty()) return null;
        int op = req.indexOf(">=") >= 0 ? req.indexOf(">=") : req.indexOf("<=");
        boolean atLeast = req.indexOf(">=") >= 0;
        String key = req.substring(0, op);
        double v = Double.parseDouble(req.substring(op + 2));
        int at = key.indexOf('@');
        if (at >= 0) { c = ctIdx(key.substring(at + 1)); key = key.substring(0, at); }
        if (c < 0) return null;
        double now = value(g, c, key);
        if (atLeast ? now >= v : now <= v) return null;
        return "First: " + describe(g, c, key, v, atLeast) + ".";
    }

    static double value(Game g, int c, String key) {
        World w = g.world;
        switch (key) {
            case "milaid": return w.milAid[c];
            case "arms": return w.arms[c];
            case "aid": return w.aid[c];
            case "sanction": return w.sanction[c];
            case "troops": return w.troopsIn[c];
            case "ties": return w.ties[c];
            case "tariff": return w.tariff[c];
            default: return w.rel[c];
        }
    }

    static String describe(Game g, int c, String key, double v, boolean atLeast) {
        String n = cnameIn(g, c);
        switch (key) {
            case "milaid": return "military aid to " + n + " at " + MIL_AID[(int) v].toLowerCase() + " or more";
            case "arms": return v >= 2 ? "a major arms package for " + n : "arms sales to " + n + " approved";
            case "aid": return "foreign aid to " + n + (v >= 2 ? " at a substantial level" : "");
            case "sanction": return atLeast ? "sanctions on " + n + " at level " + (int) v + " or more" : "sanctions on " + n + " eased to level " + (int) v + " or less";
            case "troops": return "at least " + Math.round(v * 1000) + " American troops in " + n;
            case "ties": return "full diplomatic relations with " + n;
            case "tariff": return "tariffs on " + n + " brought down";
            default: return "better relations with " + n;
        }
    }
}
