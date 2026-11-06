package resolutedesk.world;

import static resolutedesk.congress.CongressRules.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.executive.Posts.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.model.NewsKinds.*;
import static resolutedesk.opinion.Issues.*;
import static resolutedesk.world.Countries.*;
import static resolutedesk.world.StatecraftData.*;
import static resolutedesk.world.WorldData.*;

import resolutedesk.congress.Bloc;
import resolutedesk.executive.Cabinet;
import resolutedesk.model.Game;

/**
 * The tools of foreign policy, country by country: military aid, arms sales, troops and bases, peacekeeping,
 * diplomatic ties, trade agreements, defense treaties and covert action. Each changes relations, the hot spots,
 * the budget and the politics at home; treaties and trade deals need Congress.
 */
public final class Statecraft {
    private Statecraft() {
    }

    /** Diplomatic ties that were broken off in history: country, first year without, last year without. */
    static final String[] NO_TIES = {"CU:1961:2014", "IR:1980:2100", "KP:1948:2100", "CN:1949:1978", "VN:1975:1994", "SY:2012:2024", "VE:2019:2100", "IQ:1991:2003", "RU:1933:1933"};
    /** American troops based abroad at the start of each era (thousands): "from|to|CC=n ...". */
    static final String[] BASES = {"1946|1949|DE=100 JP=115 GB=20 IT=15", "1950|1989|DE=250 JP=46 GB=25 IT=12 KR=40 TR=10 PH=15",
            "1990|2008|DE=70 JP=45 GB=12 IT=12 KR=37 TR=2 SA=5", "2009|2021|DE=35 JP=54 GB=10 IT=12 KR=28 TR=2 QA=8 AE=3 PL=4 AU=1",
            "2022|2100|DE=35 JP=54 GB=10 IT=12 KR=28 TR=2 QA=10 AE=3.5 SA=2.7 PL=10 IQ=2.5 SY=2 AU=2 PH=0.5 DK=0.2 FI=0.5"};
    /** Military aid and arms in the modern era: "from|CC=aid:arms ...". */
    static final String[] SUPPORT = {"2022|UA=3:1 IL=3:2 TW=1:1 EG=2:1 JO=1:1 SA=0:1 AE=0:1 PL=1:1", "2001|IL=3:2 EG=2:1 JO=1:1 SA=0:1 AE=0:1 TW=0:1",
            "1980|IL=3:2 EG=2:1 SA=0:1 PK=1:0", "1948|GB=1:1 TR=1:1"};

    // ---- the start of a game ----

    /** For a save from before statecraft: empty tools, ties and bases as the era had them, and the year's objectives. */
    public static void upgrade(Game g) {
        World w = g.world;
        w.milAid = new int[NC]; w.arms = new int[NC]; w.ties = new int[NC]; w.pact = new int[NC]; w.peace = new int[NC]; w.cvProg = new int[NC]; w.cvDue = new int[NC];
        w.troopsIn = new double[NC]; w.unrest = new double[NC]; w.toppled = new boolean[NC]; w.authorized = new int[NC];
        w.refugees = 20;
        w.objectives = new java.util.ArrayList<Objective>();
        w.objectivesYear = -1;
        setup(g);
        Objectives.refresh(g);
    }

    public static void setup(Game g) {
        World w = g.world;
        int y = g.era.year;
        for (int c = 0; c < NC; c++) w.ties[c] = 2;
        for (String s : NO_TIES) {
            String[] t = s.split(":");
            int c = ctIdx(t[0]);
            if (c >= 0 && y >= Integer.parseInt(t[1]) && y <= Integer.parseInt(t[2])) w.ties[c] = 0;
        }
        if (y >= 1979) w.ties[CC_TW] = 1;                              // unofficial ties since the switch to Beijing
        for (String s : BASES) {
            String[] t = s.split("\\|");
            if (y < Integer.parseInt(t[0]) || y > Integer.parseInt(t[1])) continue;
            for (String kv : t[2].split(" ")) { int c = ctIdx(kv.split("=")[0]); if (c >= 0) w.troopsIn[c] = Double.parseDouble(kv.split("=")[1]); }
        }
        for (String s : SUPPORT) {
            String[] t = s.split("\\|");
            if (y < Integer.parseInt(t[0])) continue;
            for (String kv : t[1].split(" ")) {
                int c = ctIdx(kv.split("=")[0]);
                String[] v = kv.split("=")[1].split(":");
                if (c >= 0) { w.milAid[c] = Integer.parseInt(v[0]); w.arms[c] = Integer.parseInt(v[1]); }
            }
            break;
        }
        for (int c = 0; c < NC; c++) w.unrest[c] = c == CC_SY && y >= 2011 && y < 2025 ? 70 : c == CC_IQ && y >= 2004 ? 45 : c == CC_VE && y >= 2017 ? 40 : c == CC_NG && y >= 2010 ? 35 : 15;
    }

    /** The troop step nearest a country's current level. */
    public static int troopStep(World w, int c) {
        int best = 0;
        for (int i = 0; i < TROOP_STEP.length; i++) if (Math.abs(TROOP_STEP[i] - w.troopsIn[c]) < Math.abs(TROOP_STEP[best] - w.troopsIn[c])) best = i;
        return best;
    }

    /** Countries a country's enemies care about: Russia for Ukraine and Europe's East, China for Taiwan, the Philippines and Japan, Iran for Israel and the Gulf. */
    static int rivalOf(int c) {
        if (c == CC_UA || c == CC_PL || c == CC_FI) return CC_RU;
        if (c == CC_TW || c == CC_PH || c == CC_JP || c == CC_VN || c == CC_IN) return CC_CN;
        if (c == CC_IL || c == CC_SA || c == CC_AE || c == CC_IQ) return CC_IR;
        if (c == CC_KR) return CC_KP;
        return -1;
    }

    static boolean autocracy(int c) { return c == CC_SA || c == CC_AE || c == CC_QA || c == CC_EG || c == CC_VN || c == CC_JO; }

    static void rel(World w, int c, double v) { if (c >= 0) w.rel[c] = clamp(w.rel[c] + v, -100, 100); }

    // ---- the tools ----

    public static String setMilAid(Game g, int c, int level) {
        World w = g.world;
        if (atWar(g, c)) return "Not to a country the United States is fighting.";
        int d = level - w.milAid[c];
        if (d == 0) return null;
        w.milAid[c] = level;
        g.econ.fiscal += d * 0.012;
        rel(w, c, 6 * d);
        rel(w, rivalOf(c), -4 * d);
        if (d > 0 && autocracy(c)) g.issueAdj[PI_RIGHTS] = clamp(g.issueAdj[PI_RIGHTS] - 0.03 * d, -1.5, 1.5);
        g.log(NK_WORLD, (d > 0 ? "The president increases military aid to " : "The president cuts military aid to ") + cnameIn(g, c), null, 1.0);
        return "Military aid to " + cnameIn(g, c) + ": " + MIL_AID[level].toLowerCase() + ".";
    }

    public static String setArms(Game g, int c, int level) {
        World w = g.world;
        if (atWar(g, c)) return "Not to a country the United States is fighting.";
        int d = level - w.arms[c];
        if (d == 0) return null;
        w.arms[c] = level;
        rel(w, c, 8 * d);
        rel(w, rivalOf(c), -5 * d);
        if (d > 0) { g.issueAdj[PI_JOBS] = clamp(g.issueAdj[PI_JOBS] + 0.03 * d, -1.5, 1.5); if (autocracy(c)) g.issueAdj[PI_RIGHTS] = clamp(g.issueAdj[PI_RIGHTS] - 0.05 * d, -1.5, 1.5); }
        if (c == CC_TW && d > 0) { w.twt = clamp(w.twt + 2 * d, 0, 100); w.deter = clamp(w.deter + 2 * d, 0, 100); }
        g.log(NK_WORLD, d > 0 ? "The administration approves arms sales to " + cnameIn(g, c) : "The administration halts arms sales to " + cnameIn(g, c), null, 1.0);
        return "Arms sales to " + cnameIn(g, c) + ": " + ARMS[level].toLowerCase() + ".";
    }

    /** Bases troops in a country: deterrence and a closer ally, a cost, and an adversary that notices. */
    public static String setTroops(Game g, int c, int step) {
        World w = g.world;
        if (atWar(g, c)) return "Troops fighting a war are managed in the Wars tab.";
        if (align(g, c) == AL_ADVERSARY) return cname(g, c) + " won't host American forces.";
        double now = w.troopsIn[c], to = TROOP_STEP[step], d = to - now;
        if (Math.abs(d) < 0.01) return null;
        w.troopsIn[c] = to;
        g.econ.fiscal += d * 0.0015;
        rel(w, c, d > 0 ? Math.min(10, d * 0.4) : Math.max(-10, d * 0.4));
        rel(w, rivalOf(c), d > 0 ? -Math.min(8, d * 0.3) : Math.min(5, -d * 0.2));
        int rg = CT_REGION[c];
        if (rg == RG_EUROPE) w.nato = clamp(w.nato + clamp(d * 0.15, -6, 6), 0, 100);
        if (rg == RG_ASIA) { w.pacific = clamp(w.pacific + clamp(d * 0.15, -6, 6), 0, 100); if (c == CC_TW || c == CC_JP || c == CC_PH) w.deter = clamp(w.deter + clamp(d * 0.2, -6, 6), 0, 100); }
        if (c == CC_TW && d > 0) w.twt = clamp(w.twt + d * 0.6, 0, 100);         // Beijing sees troops on Taiwan as a provocation
        g.log(NK_WORLD, d > 0 ? Math.round(d * 1000) + " more American troops go to " + cnameIn(g, c) : Math.round(-d * 1000) + " American troops leave " + cnameIn(g, c), null, 1.1);
        return "American forces in " + cnameIn(g, c) + ": " + TROOP_NAME[step].toLowerCase() + ".";
    }

    /** Peacekeepers: only where there is a conflict to keep apart, under the UN or with allies. */
    public static String setPeace(Game g, int c, boolean on) {
        World w = g.world;
        if (on && w.unrest[c] < 35 && !(c == CC_UA && w.uaStage >= 3)) return cnameIn(g, c) + " has no conflict for peacekeepers to keep apart.";
        if (on && atWar(g, c)) return "Not while American forces are fighting there.";
        w.peace[c] = on ? 1 : 0;
        g.econ.fiscal += on ? 0.006 : -0.006;
        if (on) { w.unrest[c] = Math.max(0, w.unrest[c] - 8); g.issueAdj[PI_FOREIGN] = clamp(g.issueAdj[PI_FOREIGN] + 0.05, -1.5, 1.5); }
        g.log(NK_WORLD, on ? "American troops join a peacekeeping mission in " + cnameIn(g, c) : "American peacekeepers leave " + cnameIn(g, c), null, 1.0);
        return on ? "Peacekeepers go to " + cnameIn(g, c) + "." : "The peacekeepers come home.";
    }

    /** Diplomatic ties: full, downgraded or severed. Recognizing Taiwan as a country is the most explosive version. */
    public static String setTies(Game g, int c, int level) {
        World w = g.world;
        int d = level - w.ties[c];
        if (d == 0) return null;
        w.ties[c] = level;
        if (c == CC_TW && level == 2 && yearNow(g) >= 1979) {
            rel(w, CC_CN, -60);
            rel(w, CC_TW, 30);
            w.twt = clamp(w.twt + 30, 0, 100);
            w.ties[CC_CN] = Math.min(w.ties[CC_CN], 1);
            g.econ.finShock -= 1.5;
            g.log(NK_BIG, "UNITED STATES RECOGNIZES TAIWAN AS AN INDEPENDENT COUNTRY", "Beijing recalls its ambassador and puts its forces on alert across the Strait. Markets fall.", 4);
            return "You recognize Taiwan. Beijing calls it a declaration of hostility.";
        }
        rel(w, c, d * (level == 2 ? 15 : 12));
        if (level == 0) { w.sanction[c] = Math.max(w.sanction[c], 1); g.issueAdj[PI_FOREIGN] = clamp(g.issueAdj[PI_FOREIGN] - 0.05, -1.5, 1.5); }
        String what = level == 2 ? "The United States restores full diplomatic relations with " : level == 1 ? "The United States downgrades relations with " : "The United States breaks off relations with ";
        g.log(level == 2 ? NK_GOOD : NK_WORLD, what + cnameIn(g, c), null, 1.6);
        return what + cnameIn(g, c) + ".";
    }

    // ---- agreements that need Congress ----

    /**
     * Expected yes votes in one chamber: each bloc's lean is the merit of the question for your party and for the
     * other, how hawkish the bloc is, and a modest pull from your standing and your relationship with it.
     */
    static double yes(Game g, int chamber, double partyPull, double otherPull, double hawkPull) {
        double y = 0;
        for (Bloc b : g.cong.blocs) {
            if (b.chamber != chamber) continue;
            double l = (b.party == g.party ? partyPull * b.loyalty : otherPull - 0.6 * g.era.polarization) + hawkPull * (b.hawk - 0.45) + (g.approval - 50) / 50 + (b.rel - 50) / 100;
            y += b.seats * logistic(l);
        }
        return y;
    }

    /** How much harder free trade is to sell in Congress: little in the postwar consensus, a lot since the China shock. */
    static double protectionism(Game g) { int y = yearNow(g); return y >= 2016 ? 0.75 : y >= 1993 ? 0.3 : 0.1; }

    /** The odds Congress approves a trade agreement with a country. */
    public static double tradeOdds(Game g, int c) {
        double m = align(g, c) == AL_ADVERSARY ? -4 : w(g).rel[c] / 100 - protectionism(g);
        double h = yes(g, HOUSE, 1.1 + m, 0 + m, 0), s = yes(g, SENATE, 1.1 + m, 0 + m, 0);
        return logistic((h - 218) / 9) * logistic((s - 50.5) / 3);
    }

    /** A trade agreement: a majority in both chambers. */
    public static String tradeDeal(Game g, int c) {
        World w = g.world;
        if (w.pact[c] > 0) return "There's already a trade agreement with " + cnameIn(g, c) + ".";
        if (atWar(g, c)) return "Not with a country the United States is fighting.";
        if (g.rng.nextDouble() >= tradeOdds(g, c)) {
            rel(w, c, -4);
            g.log(NK_BAD, "Congress rejects the trade agreement with " + cnameIn(g, c), null, 1.4);
            return "Congress votes it down.";
        }
        w.pact[c] = 1;
        rel(w, c, 10);
        g.econ.polGdp += 0.01 * Math.min(1, tradeOf(g, c) / 200);
        g.econ.farm = clamp(g.econ.farm + 2, 30, 160);
        g.log(NK_GOOD, "Congress approves a trade agreement with " + cnameIn(g, c), null, 1.6);
        return "Congress approves the trade agreement.";
    }

    static World w(Game g) { return g.world; }

    /** A country already fighting, or claimed by, a nuclear power: a treaty would commit America to that fight. */
    static boolean exposed(Game g, int c) { World w = g.world; return (c == CC_UA && w.modern && w.uaStage == 2) || (c == CC_TW && yearNow(g) >= 1979); }

    /** The odds two-thirds of the Senate ratifies a defense treaty with a country. */
    public static double treatyOdds(Game g, int c) {
        double m = align(g, c) == AL_ADVERSARY ? -4 : w(g).rel[c] / 80 - (exposed(g, c) ? 1.8 : 0) - (CT_REGION[c] == RG_MIDEAST ? 0.6 : 0)
                - (nuke(g, c) ? 0.3 : 0) - (yearNow(g) >= 1990 ? 0.3 : 0);
        double s = yes(g, SENATE, 1.2 + m, 0.4 + m, 0.8);
        return logistic((s - 66.5) / 3);
    }

    /** A defense treaty: two-thirds of the Senate. The country becomes a treaty ally, and an attack on it is an attack on America. */
    public static String defensePact(Game g, int c) {
        World w = g.world;
        if (align(g, c) == AL_TREATY) return cname(g, c) + " is already a treaty ally.";
        if (align(g, c) == AL_ADVERSARY) return "The Senate won't consider a defense treaty with an adversary.";
        if (w.rel[c] < 40) return cname(g, c) + " isn't interested in an alliance with the United States. Relations need to be warmer (40 or better).";
        if (g.rng.nextDouble() >= treatyOdds(g, c)) {
            rel(w, c, -6);
            g.log(NK_BAD, "The Senate rejects a defense treaty with " + cnameIn(g, c), "It fell short of the two-thirds a treaty needs.", 2);
            return "The Senate falls short of two-thirds.";
        }
        if (w.align != null) w.align[c] = AL_TREATY;
        rel(w, c, 15);
        rel(w, rivalOf(c), -15);
        w.deter = clamp(w.deter + 3, 0, 100);
        if (CT_REGION[c] == RG_EUROPE) w.nato = clamp(w.nato + 4, 0, 100);
        if (CT_REGION[c] == RG_ASIA) w.pacific = clamp(w.pacific + 4, 0, 100);
        g.log(NK_BIG, ("The Senate ratifies a defense treaty with " + cnameIn(g, c)).toUpperCase(), "An attack on " + cnameIn(g, c) + " is now an attack on the United States.", 3);
        return "The Senate ratifies the treaty.";
    }

    // ---- covert action ----

    public static double covertOdds(Game g, int c, int kind) {
        double base = CV2_ODDS[kind] - power(g, c) * 0.02 + (kind == CV2_COUP ? g.world.unrest[c] / 250 : 0);
        return Diplo.p(g, "i", base);
    }

    public static double leakOdds(Game g, int kind) { return CV2_LEAK[kind] * (1.3 - Cabinet.pq(g, P_CIA) * 0.6); }

    public static String startCovert(Game g, int c, int kind) {
        World w = g.world;
        if (w.cvProg[c] > 0) return "A program in " + cnameIn(g, c) + " is already under way.";
        if (align(g, c) == AL_TREATY && kind >= CV2_ARM) return "The CIA won't arm rebels against a treaty ally.";
        w.cvProg[c] = kind + 1;
        w.cvDue[c] = g.week + CV2_WEEKS[kind];
        g.world.covert++;
        return "You sign the finding. The CIA expects results within " + CV2_WEEKS[kind] + " weeks.";
    }

    static void covertResult(Game g, int c) {
        World w = g.world;
        int kind = w.cvProg[c] - 1;
        w.cvProg[c] = 0;
        boolean ok = g.rng.nextDouble() < covertOdds(g, c, kind), leak = g.rng.nextDouble() < leakOdds(g, kind);
        String n = cnameIn(g, c);
        if (leak) {
            w.exposed++;
            rel(w, c, -15);
            g.scandal += 1.5 + kind;
            g.issueAdj[PI_FOREIGN] = clamp(g.issueAdj[PI_FOREIGN] - 0.1, -1.5, 1.5);
            g.log(NK_BIG, ("Report: CIA behind a secret operation in " + n).toUpperCase(), "The administration won't comment. " + cname(g, c) + " calls it an act of war.", 3);
        }
        if (!ok) { g.log(NK_WORLD, "Intelligence officials say a covert effort in " + n + " has failed", null, leak ? 1 : 0.4); return; }
        switch (kind) {
            case CV2_INFLUENCE: rel(w, c, 6); w.unrest[c] = clamp(w.unrest[c] + 4, 0, 100); break;
            case CV2_CYBER:
                if (c == CC_IR) w.iran = clamp(w.iran - 15, 0, 100);
                if (c == CC_KP) w.nk = clamp(w.nk - 10, 0, 100);
                if (c == CC_CN || c == CC_RU) w.cyber = clamp(w.cyber + 8, 0, 100);
                break;
            case CV2_ARM: w.unrest[c] = clamp(w.unrest[c] + 25, 0, 100); w.refugees = clamp(w.refugees + 4, 0, 100); break;
            default: topple(g, c, "covert action"); break;
        }
        if (kind != CV2_COUP) g.log(NK_WORLD, "Officials say quietly that a covert program in " + n + " has worked", null, 0.5);
    }

    /** A government falls to American action: a transitional authority friendly to Washington, and a country in turmoil. */
    public static void topple(Game g, int c, String how) {
        World w = g.world;
        w.toppled[c] = true;
        w.leader[c] = "a transitional authority";
        w.ltitle[c] = "Transitional government";
        if (w.align != null) w.align[c] = AL_PARTNER;
        w.rel[c] = 35;
        w.sanction[c] = 0;
        w.ties[c] = 2;
        w.unrest[c] = clamp(w.unrest[c] + 35, 0, 100);
        w.refugees = clamp(w.refugees + 8, 0, 100);
        for (int o = 0; o < NC; o++) if (align(g, o) == AL_ADVERSARY && o != c) rel(w, o, -10);
        g.log(NK_BIG, ("The government of " + cnameIn(g, c) + " falls").toUpperCase(), "A transitional authority takes over after " + how + ". Fighting and looting are reported in the capital.", 4);
    }

    // ---- the week ----

    public static void week(Game g) {
        World w = g.world;
        for (int c = 0; c < NC; c++) {
            if (w.cvProg[c] > 0 && g.week >= w.cvDue[c]) covertResult(g, c);
            // aid at work in the hot spots
            if (c == CC_UA && w.milAid[c] > 0 && w.uaStage == 2) w.ukr = clamp(w.ukr + 0.06 * w.milAid[c], 0, 100);
            if (c == CC_TW && w.milAid[c] > 0) w.deter = clamp(w.deter + 0.02 * w.milAid[c], 0, 100);
            // unrest settles slowly, faster with peacekeepers
            w.unrest[c] = clamp(w.unrest[c] + (15 - w.unrest[c]) * (w.peace[c] > 0 ? 0.03 : 0.006), 0, 100);
            if (w.unrest[c] > 60) w.refugees = clamp(w.refugees + 0.03 * (w.unrest[c] - 60) / 10, 0, 100);
        }
        // refugees go somewhere: Europe's politics, and the southern border
        w.refugees = clamp(w.refugees + (20 - w.refugees) * 0.01, 0, 100);
        if (w.refugees > 35) { w.nato = clamp(w.nato - 0.02, 0, 100); w.migr = clamp(w.migr + 0.01 * (w.refugees - 35), 0, 100); }
    }
}
