package resolutedesk.stories;

import static resolutedesk.core.Num.*;
import static resolutedesk.data.StateData.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.model.NewsKinds.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.model.Tracking.*;
import static resolutedesk.stories.StoryData.*;

import java.util.ArrayList;
import java.util.List;
import resolutedesk.history.History;
import resolutedesk.model.Econ;
import resolutedesk.model.Game;
import resolutedesk.model.News;
import resolutedesk.world.Diplo;
import resolutedesk.world.War;

/**
 * The news beyond the White House: more than 500 stories of their era (science, culture, sports, business, the weather,
 * daily life) and stories that follow the state of the country. A story runs once a presidency, filling the paper
 * and the ticker on quiet weeks. Some move what voters care about.
 */
public final class Stories {
    private static List<StoryDef> defs;

    private Stories() {
    }

    /** Every ambient story, in table order. */
    public static List<StoryDef> defs() {
        if (defs == null) {
            List<StoryDef> l = new ArrayList<StoryDef>();
            for (String[] table : new String[][]{AmbientEarly.ROWS, AmbientMiddle.ROWS, AmbientRecent.ROWS, AmbientAny.ROWS})
                for (String row : table) l.add(new StoryDef(row));
            defs = l;
        }
        return defs;
    }

    public static void setup(Game g) {
        StoryState s = new StoryState();
        s.used = new boolean[defs().size()];
        s.cool = new int[StorylineTable.LINES.length];
        g.stories = s;
    }

    /** A game saved before this version gets an empty record; a longer table keeps what has already run. */
    public static void upgrade(Game g) {
        if (g.stories == null) { setup(g); return; }
        StoryState s = g.stories;
        if (s.used.length != defs().size()) { boolean[] u = new boolean[defs().size()]; for (int i = 0; i < Math.min(u.length, s.used.length); i++) u[i] = s.used[i]; s.used = u; }
        if (s.cool.length != StorylineTable.LINES.length) { int[] c = new int[StorylineTable.LINES.length]; for (int i = 0; i < Math.min(c.length, s.cool.length); i++) c[i] = s.cool[i]; s.cool = c; }
        if (s.lines == null) s.lines = new ArrayList<Storyline>();
    }

    public static void week(Game g) {
        ambient(g);
        Storylines.week(g);
    }

    // ---- the era's stories ----

    /** Runs one or two of the era's stories: two on a quiet week, often none on a busy one. */
    static void ambient(Game g) {
        double busy = 0;
        int from = g.day() - 7;
        for (News n : g.news) if (n.day >= from && n.section == null) busy += n.weight;
        double chance = busy > 12 ? 0.5 : busy > 6 ? 0.75 : 0.95;
        int n = busy < 5 ? 2 : 1;
        for (int k = 0; k < n; k++) if (g.rng.nextDouble() < chance) run(g, pick(g));
    }

    /** Which story runs: those that follow the news of the moment weigh three times as much as the era's background. */
    static int pick(Game g) {
        StoryState s = g.stories;
        int y = yearNow(g);
        List<StoryDef> d = defs();
        double total = 0;
        double[] w = new double[d.size()];
        for (int i = 0; i < w.length; i++) {
            StoryDef x = d.get(i);
            if (s.used[i] || y < x.from || y > x.to || !holds(g, x.when)) continue;
            w[i] = x.when.isEmpty() ? 1 : 3;
            total += w[i];
        }
        if (total <= 0) return -1;
        double r = g.rng.nextDouble() * total;
        for (int i = 0; i < w.length; i++) { r -= w[i]; if (w[i] > 0 && r <= 0) return i; }
        return -1;
    }

    static void run(Game g, int i) {
        if (i < 0) return;
        StoryDef x = defs().get(i);
        g.stories.used[i] = true;
        g.stories.ran++;
        String state = STATES[someState(g)].name;
        int kind = x.section == 1 || x.section == 8 ? NK_ECON : x.section == 2 ? NK_WORLD : NK_INFO;
        g.log(kind, fill(x.head, state, null), x.body.isEmpty() ? null : fill(x.body, state, null), 0.5 + 0.4 * g.rng.nextDouble());
        g.news.get(g.news.size() - 1).section = SECTION[x.section];
        fx(g, x.fx);
    }

    /** A state for a story to happen in: not the capital, and not Alaska or Hawaii before they became states in 1959. */
    static int someState(Game g) {
        while (true) {
            int s = g.rng.nextInt(NS);
            String ab = STATES[s].ab;
            if (!ab.equals("DC") && (yearNow(g) >= 1959 || !ab.equals("AK") && !ab.equals("HI"))) return s;
        }
    }

    /** Fills in a story's blanks: {state}, and for a storyline {who} (its made-up figure), {place} and {st} (its state's name and postal code). */
    static String fill(String s, String state, Storyline l) {
        String t = s.replace("{state}", state);
        if (l != null) t = t.replace("{who}", l.who).replace("{place}", STATES[l.state].name).replace("{st}", STATES[l.state].ab);
        return t;
    }

    // ---- effects and conditions ----

    /**
     * Effects in Diplo.fx's language, plus "sal:CRIME=0.2" (a story that makes voters care more, or less, about an
     * issue) and "housing=-0.3" (housing starts, millions a year).
     */
    static void fx(Game g, String fx) {
        if (fx == null || fx.isEmpty() || fx.equals("-")) return;
        StringBuilder rest = new StringBuilder();
        for (String tok : fx.split(" ")) {
            if (tok.startsWith("housing=")) g.econ.housing = Math.max(0.3, g.econ.housing + Double.parseDouble(tok.substring(8)));
            else if (tok.startsWith("sal:")) {
                int eq = tok.indexOf('=');
                int issue = issue(tok.substring(4, eq));
                if (issue >= 0) g.issueSal[issue] = clamp(g.issueSal[issue] + Double.parseDouble(tok.substring(eq + 1)), 0.05, 2.5);
            } else if (!tok.isEmpty()) rest.append(rest.length() > 0 ? " " : "").append(tok);
        }
        if (rest.length() > 0) Diplo.fx(g, rest.toString());
    }

    /** An issue by its short code: ECON, PRICES, JOBS, HEALTH, IMMIG, CRIME, FOREIGN, SECURITY, TRADE, ENERGY, RIGHTS, TECH, EDU or ETHICS. */
    public static int issue(String code) {
        String[] codes = {"ECON", "PRICES", "JOBS", "HEALTH", "IMMIG", "CRIME", "FOREIGN", "SECURITY", "TRADE", "ENERGY", "RIGHTS", "TECH", "EDU", "ETHICS"};
        for (int i = 0; i < codes.length; i++) if (codes[i].equals(code)) return i;
        return -1;
    }

    /**
     * The state of the country, for a story's conditions. Beyond what events and turning points can test: jobs,
     * growth, prices at the pump, the markets, the deficit, wars, the election calendar and the president's party.
     */
    public static double var(Game g, String k) {
        Econ e = g.econ;
        switch (k) {
            case "unemp": return e.unemp;
            case "gdp": return e.gdp;
            case "fed": return e.fed;
            case "deficit": return e.deficit;
            case "gasup": return e.gas / e.priceLevel - e.gasRef;
            case "mkt": return market(g, 13);
            case "uchg": return g.hist.size() > 26 ? e.unemp - g.hist.get(g.hist.size() - 27)[H_UNEMP] : 0;
            case "wars": { int n = 0; if (g.world != null && g.world.wars != null) for (War x : g.world.wars) if (x.stage == 2) n++; return n; }
            case "kia": return g.world != null ? g.world.kia : 0;
            case "elect": return yearNow(g) % 4 == 0 ? 1 : 0;
            case "midterm": return yearNow(g) % 4 == 2 ? 1 : 0;
            case "tw": return g.tw();
            case "dem": return g.party == DEM ? 1 : 0;
            case "mortgage": return e.mortgage;
            case "oil": return e.oil / e.priceLevel;
            case "fin": return e.finShock;
            case "base": return g.home != null ? (g.home.fac[0] + g.home.fac[1]) / 2 : 50;
            case "farm": return e.farm;
            case "sent": return e.sentiment;
            default: return History.var(g, k);
        }
    }

    /** The stock market's change over the last n weeks, in percent. */
    static double market(Game g, int n) {
        if (g.hist.size() <= n) return 0;
        double then = g.hist.get(g.hist.size() - 1 - n)[H_STOCKS];
        return then > 0 ? (g.econ.stocks / then - 1) * 100 : 0;
    }

    /** "unemp>7,month<4": every clause must hold. "p=0.02" is a weekly chance, drawn only when everything before it holds. */
    public static boolean holds(Game g, String c) {
        if (c == null || c.isEmpty()) return true;
        for (String t : c.split(",")) {
            if (t.startsWith("p=")) { if (g.rng.nextDouble() >= Double.parseDouble(t.substring(2))) return false; continue; }
            int i = t.indexOf('>'), j = t.indexOf('<');
            if (i > 0) { if (!(var(g, t.substring(0, i)) > Double.parseDouble(t.substring(i + 1)))) return false; }
            else if (j > 0) { if (!(var(g, t.substring(0, j)) < Double.parseDouble(t.substring(j + 1)))) return false; }
        }
        return true;
    }

    /** Stories of this week that came from beyond the White House. */
    public static List<News> thisWeek(Game g) {
        List<News> l = new ArrayList<News>();
        int from = g.day() - 7;
        for (News n : g.news) if (n.day >= from && n.section != null) l.add(n);
        return l;
    }

    /** How many of the table's stories could run at some point in a year, whatever the conditions. */
    public static int inYear(int y) {
        int n = 0;
        for (StoryDef d : defs()) if (y >= d.from && y <= d.to) n++;
        return n;
    }

    /** The section a story is filed under. */
    public static String section(StoryDef d) { return SECTION[d.section]; }
}
