package resolutedesk.world;

import static resolutedesk.congress.CongressRules.*;
import static resolutedesk.core.Dates.*;
import static resolutedesk.core.Fmt.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.model.NewsKinds.*;
import static resolutedesk.opinion.Issues.*;
import static resolutedesk.sim.DecisionKinds.*;
import static resolutedesk.world.Countries.*;
import static resolutedesk.world.WarData.*;
import static resolutedesk.world.WorldData.*;

import resolutedesk.congress.Bloc;
import resolutedesk.congress.Hill;
import resolutedesk.history.History;
import resolutedesk.model.Game;
import resolutedesk.sim.Decision;
import resolutedesk.sim.Decisions;

/**
 * Wars of choice: strikes, a blockade, a no-fly zone or an invasion, against any country, at any time. Each comes
 * with hard consequences (casualties, markets and oil, refugees, terror, allies, protests at home) and an invasion
 * that succeeds becomes an occupation, usually with an insurgency. Wars end in many ways: winning, a settlement,
 * a timetable, handing over, declaring victory, or simply leaving.
 */
public final class Wars {
    private Wars() {
    }

    static boolean oilProducer(int c) { return c == CC_IR || c == CC_IQ || c == CC_SA || c == CC_QA || c == CC_AE || c == CC_VE || c == CC_RU; }

    static boolean bigProducer(int c) { return c == CC_SA || c == CC_RU; }

    /** Troops the generals ask for (thousands). */
    public static double need(Game g, int c, int kind) {
        double p = power(g, c);
        return kind == WK_STRIKES ? 4 + 1.5 * p : kind == WK_BLOCKADE ? 8 + 3 * p : kind == WK_NOFLY ? 6 + p : 40 + 30 * p;
    }

    public static String warName(Game g, int c, int kind) {
        String n = cnameIn(g, c);
        return kind == WK_STRIKES ? "The air war against " + n : kind == WK_BLOCKADE ? "The blockade of " + n : kind == WK_NOFLY ? "The no-fly zone over " + n
                : kind == WK_INVASION ? "The invasion of " + n : "The occupation of " + n;
    }

    /** Why this war can't be started, or null. */
    public static String cannot(Game g, int c, int kind) {
        World w = g.world;
        if (gone(g, c)) return cname(g, c) + " doesn't exist in this era.";
        for (War x : w.wars) if (x.stage == WS_FIGHTING && x.enemy == c && x.kind == kind) return "That war is already under way.";
        if (kind == WK_INVASION && nuke(g, c)) return "The Joint Chiefs won't plan an invasion of a nuclear power. Strikes and a blockade are still on the table, at grave risk.";
        if (kind == WK_NOFLY && w.unrest[c] < 40) return "There's no fighting inside " + cnameIn(g, c) + " for a no-fly zone to stop.";
        return null;
    }

    /** What the country's people and markets expect to happen, for the warning before you go ahead. */
    public static String expect(Game g, int c, int kind) {
        StringBuilder s = new StringBuilder();
        s.append("About ").append(thousands(Math.round(need(g, c, kind) * 1000))).append(" troops. ");
        if (oilProducer(c) && kind != WK_NOFLY) s.append("Oil prices will jump. ");
        if (power(g, c) >= 6) s.append("Markets will fall hard. ");
        if (nuke(g, c)) s.append("A nuclear-armed enemy: DEFCON 2, and the war could spiral. ");
        if (CT_REGION[c] == RG_MIDEAST || CT_REGION[c] == RG_SOUTHASIA) s.append("Expect terror attacks in revenge. ");
        if (kind == WK_INVASION) s.append("If the government falls, you own the occupation. ");
        int pr = protector(g, c);
        if (kind == WK_INVASION && pr >= 0) s.append(cname(g, pr)).append(" has promised to fight for ").append(cnameIn(g, c)).append(nuke(g, pr) ? ", and it has the bomb. " : ". ");
        if (align(g, c) == AL_TREATY) s.append("Attacking a treaty ally would shatter the alliances. ");
        else if (align(g, c) == AL_PARTNER) s.append("Attacking a partner will alarm every ally. ");
        if (!authorized(g, c)) s.append("Congress hasn't authorized it.");
        return s.toString().trim();
    }

    public static boolean authorized(Game g, int c) { return g.world.authorized[c] > 0 && g.week - g.world.authorized[c] < 52; }

    /** Goes to war. */
    public static String start(Game g, int c, int kind) {
        String no = cannot(g, c, kind);
        if (no != null) return no;
        World w = g.world;
        War x = new War();
        x.id = "choice:" + CT_ID[c] + ":" + kind + ":" + g.week;
        x.kind = kind; x.enemy = c; x.name = warName(g, c, kind);
        x.need = need(g, c, kind); x.troops = x.need;
        x.drift = pace(g, c, kind);
        x.lethal = WK_LETHAL[kind] * (kind == WK_INVASION && protector(g, c) >= 0 ? 4 : 1); x.cost = WK_COST[kind];
        x.authorized = authorized(g, c);
        x.stakes = 0.7 + (w.rel[c] < -50 ? 0.2 : 0) + (x.authorized ? 0.2 : 0);
        int al = align(g, c);
        x.legit = clamp(38 + (x.authorized ? 14 : 0) + (w.rel[c] < -50 ? 8 : w.rel[c] < 0 ? 3 : -10) - (al == AL_TREATY ? 30 : al == AL_PARTNER ? 15 : 0) - (nuke(g, c) ? 15 : 0), 5, 70);
        x.support = clamp(x.legit + 2 + (g.approval - 50) * 0.2, 8, 85);
        x.front = WK_FRONT[kind]; x.stage = WS_FIGHTING; x.started = g.week; x.strategy = 1;
        w.wars.add(x);
        g.warsStarted++;
        g.log(NK_BIG, ("President " + g.pres.last + " orders " + WK_NAME[kind].toLowerCase().replace("an invasion", "the invasion of " + cnameIn(g, c))
                .replace("a naval blockade", "a naval blockade of " + cnameIn(g, c)).replace("air and missile strikes", "strikes on " + cnameIn(g, c))
                .replace("a no-fly zone", "a no-fly zone over " + cnameIn(g, c))).toUpperCase(), x.authorized ? "Congress has authorized the use of force." : "The White House says the president has the authority to act on his own.", 5);
        shock(g, x);
        g.econ.war = warSpend(w);
        return x.name + " begins. Run it from the Wars tab.";
    }

    /**
     * How fast the front moves at full strength. A small country's army collapses in weeks; a large one fights for
     * months. An occupation slowly loses ground to the insurgency unless troops and strategy hold it.
     */
    public static double pace(Game g, int c, int kind) {
        double p = power(g, c);
        boolean ground = kind == WK_INVASION || kind == WK_OCCUPATION, guarded = ground && protector(g, c) >= 0;
        if (kind == WK_OCCUPATION) return WK_PACE[kind] - 0.015 * p - (guarded ? 0.15 : 0);
        double v = WK_PACE[kind] / (1 + (kind == WK_INVASION ? 0.6 : 0.3) * p);
        if (p >= 7) v = v * 0.5 - 0.15;              // a great power fights back everywhere it can
        if (nuke(g, c)) v = v * 0.3 - 0.25;          // and a nuclear one has nothing it values at real risk
        if (guarded) v = v * 0.1 - 0.6;              // its protector's armies join the fight: a stalemate at best
        return v;
    }

    /** The great power that would fight for a country if American troops invaded it, or -1. */
    public static int protector(Game g, int c) {
        int y = yearNow(g);
        if (c == CC_KP && y >= 1950) return CC_CN;
        if (c == CC_CU && y >= 1960 && y < 1992) return CC_RU;
        if (c == CC_SY && y >= 2015) return CC_RU;
        return -1;
    }

    /** Whether a war could escalate into a clash with a nuclear power: the enemy itself, or the protector of an invaded country. */
    static boolean nuclearRisk(Game g, War x) {
        int pr = x.kind == WK_INVASION || x.kind == WK_OCCUPATION ? protector(g, x.enemy) : -1;
        return nuke(g, x.enemy) || pr >= 0 && nuke(g, pr);
    }

    /**
     * The support a war of choice settles toward: its legitimacy at the start, less about six points for every
     * tenfold rise in American deaths, and a slow weariness once it has gone on for six months.
     */
    public static double supportTarget(War x) {
        return x.legit - 6 * Math.log10(1 + x.kia / 10) - 0.05 * Math.max(0, x.weeks - 26);
    }

    static double warSpend(World w) { double s = 0; for (War x : w.wars) if (x.stage == WS_FIGHTING) s += x.troops * x.cost; return s; }

    /** The first shock: rally, markets, oil, allies, refugees, terror and protest. */
    static void shock(Game g, War x) {
        World w = g.world;
        int c = x.enemy, al = align(g, c), kind = x.kind;
        double p = power(g, c);
        if (nuke(g, c)) { g.perf -= 4; g.stress = Math.min(100, g.stress + 15); }     // fear, not a rally
        else g.rally += x.authorized ? 3 + p * 0.3 : 1 + p * 0.15;
        g.econ.finShock -= 0.2 * p + (nuke(g, c) ? 2 : 0);
        if (oilProducer(c) && kind != WK_NOFLY) Diplo.oil(g, (kind == WK_STRIKES ? 6 : kind == WK_BLOCKADE ? 10 : 12) * (bigProducer(c) ? 2 : 1) * g.econ.priceLevel);
        w.rel[c] = -100; w.ties[c] = 0; w.sanction[c] = 3;
        if (!x.authorized) { for (int o = 0; o < NC; o++) if (align(g, o) == AL_TREATY) Statecraft.rel(w, o, -4); w.nato = clamp(w.nato - 3, 0, 100); }
        if (al == AL_TREATY) { w.nato = clamp(w.nato - 25, 0, 100); w.pacific = clamp(w.pacific - 20, 0, 100); for (int o = 0; o < NC; o++) if (o != c && align(g, o) == AL_TREATY) Statecraft.rel(w, o, -30); }
        else if (al == AL_PARTNER) for (int o = 0; o < NC; o++) if (CT_REGION[o] == CT_REGION[c] && o != c) Statecraft.rel(w, o, -10);
        int[] bloc = {CC_RU, CC_CN, CC_IR, CC_KP, CC_VE, CC_CU, CC_SY};
        for (int o : bloc) if (o != c && !gone(g, o) && (c == CC_RU || c == CC_CN || c == CC_IR || c == CC_KP || c == CC_VE || c == CC_CU || c == CC_SY)) Statecraft.rel(w, o, -8);
        w.refugees = clamp(w.refugees + (kind == WK_INVASION ? 15 : 3), 0, 100);
        w.unrest[c] = clamp(w.unrest[c] + (kind == WK_INVASION ? 30 : 10), 0, 100);
        if (CT_REGION[c] == RG_MIDEAST || CT_REGION[c] == RG_SOUTHASIA) w.terror = clamp(w.terror + (kind == WK_INVASION ? 15 : 8), 0, 100);
        if (CT_REGION[c] == RG_AMERICAS) w.migr = clamp(w.migr + (kind == WK_INVASION ? 10 : 4), 0, 100);
        if (g.home != null) g.home.protest = clamp(g.home.protest + (x.authorized ? 5 : 12), 0, 100);
        if (nuke(g, c) || p >= 8) w.defcon = Math.min(w.defcon, nuke(g, c) ? 2 : 3);
        int pr = kind == WK_INVASION ? protector(g, c) : -1;
        if (pr >= 0) {
            Statecraft.rel(w, pr, -40);
            if (nuke(g, pr)) w.defcon = Math.min(w.defcon, 2);
            g.log(NK_BIG, (cname(g, pr) + " sends its forces to defend " + cnameIn(g, c)).toUpperCase(), "American commanders now face " + cnameIn(g, pr) + "'s army as well.", 4);
        }
        g.issueSal[PI_SECURITY] = Math.min(2.2, g.issueSal[PI_SECURITY] + 0.4);
        if (!x.authorized && g.cong != null) for (Bloc b : g.cong.blocs) if (b.party != g.party) b.rel = Math.max(0, b.rel - 5);
    }

    // ---- each week of a war of choice ----

    /** The consequences of a war of choice this week, and its timetable or handover. Returns true if the war ended. */
    public static boolean step(Game g, War x) {
        World w = g.world;
        int c = x.enemy;
        double intensity = x.need > 0 ? x.troops / x.need : 0;
        if (x.kind != WK_OCCUPATION) x.drift = pace(g, c, x.kind) * Math.min(1.3, intensity);
        boolean mideast = CT_REGION[c] == RG_MIDEAST || CT_REGION[c] == RG_SOUTHASIA;
        w.terror = clamp(w.terror + 0.03 * intensity * (mideast ? 2 : 1), 0, 100);
        w.refugees = clamp(w.refugees + (x.kind == WK_INVASION || x.kind == WK_OCCUPATION ? 0.12 : 0.02) * intensity, 0, 100);
        if (g.home != null) g.home.protest = clamp(g.home.protest + (x.support < 45 ? 0.08 : 0) + x.lethal * intensity, 0, 100);
        g.perf -= 0.007 * Math.max(0, 55 - x.support);                          // an unpopular war drags on the president
        if (!x.authorized && x.support < 45) for (int o = 0; o < NC; o++) if (align(g, o) == AL_TREATY) Statecraft.rel(w, o, -0.05);
        if (oilProducer(c) && (x.kind == WK_BLOCKADE || x.kind == WK_INVASION || x.kind == WK_OCCUPATION)) Diplo.oil(g, 0.15 * g.econ.priceLevel);
        if (x.kind == WK_OCCUPATION) w.unrest[c] = clamp(w.unrest[c] + (50 - x.front) * 0.01, 0, 100);
        if (nuclearRisk(g, x) && g.rng.nextDouble() < 0.015 * intensity) { spiral(g, x); return true; }
        // Congress and the War Powers Resolution
        if (!x.authorized && x.weeks >= 9 && !x.wpVoted && g.cong != null) {
            x.wpVoted = true;
            if (!Hill.majority(g, HOUSE, g.party) || !Hill.majority(g, SENATE, g.party) || x.support < 45) {
                Decision d = Decisions.add(g, D_WARPOWERS, "Congress orders the troops home", "Under the War Powers Resolution, both chambers have voted to end " + x.name.toLowerCase()
                        + " within thirty days. The war was never authorized.", new String[]{"Veto it and fight on", "Sign it and bring the troops home"},
                        new String[]{"Counsel: \"You can veto it; they probably can't override. But expect hearings, and worse.\"", "Chief of staff: \"The country is tired of this. Take the exit Congress is offering.\""});
                d.war = x;
            }
        }
        // a timetable or a handover winding the war down
        if (x.timetable >= 0) {
            double span = Math.max(1, x.timetable - x.timetableStart), left = Math.max(0, x.timetable - g.week);
            x.troops = x.troops0 * left / span;
            x.front = clamp(x.front - 0.05, 0, 100);
            if (g.week >= x.timetable) { finish(g, x, WS_TIMETABLE); return true; }
        }
        if (x.handoff >= 0) {
            x.troops = Math.max(x.troops * 0.85, x.need * 0.2);
            if (g.week >= x.handoff) { finish(g, x, WS_HANDED); return true; }
        }
        return false;
    }

    /** The front reaches 100: strikes and no-fly zones meet their goals, a blockade wins concessions, and an invasion topples the government. */
    public static boolean victory(Game g, War x) {
        if (x.kind == WK_INVASION) {
            int c = x.enemy;
            Statecraft.topple(g, c, "the invasion");
            x.kind = WK_OCCUPATION;
            x.name = warName(g, c, WK_OCCUPATION);
            x.front = 50;
            x.drift = pace(g, c, WK_OCCUPATION);
            x.lethal = WK_LETHAL[WK_OCCUPATION] * (protector(g, c) >= 0 ? 3 : 1);
            g.rally += 3;
            g.perf += 2;
            Decisions.info(g, "The government has fallen", "American forces hold the capital, and a transitional authority is in charge. Now comes the hard part: insurgents are already attacking, "
                    + "and the country will need years of help. You can hand over to the new government once it can hold its ground, set a timetable, or stay.");
            return false;
        }
        finish(g, x, x.kind == WK_BLOCKADE ? WS_AGREED : WS_WON);
        return true;
    }

    /** A war with a nuclear power spirals: the worst losses of the war, a crash, and a ceasefire the world imposes. */
    static void spiral(Game g, War x) {
        World w = g.world;
        double dead = 3000 + g.rng.nextDouble() * 3000;
        x.kia += dead;
        w.kia += (int) Math.round(dead);
        w.defcon = 1;
        g.econ.finShock -= 5;
        Diplo.oil(g, 20 * g.econ.priceLevel);
        g.perf -= 8;
        g.log(NK_BIG, (x.name + " spirals out of control").toUpperCase(), "Thousands of Americans are killed in a single day of escalation. The world's leaders force a ceasefire.", 6);
        History.end(g, x, WS_AGREED);
        w.defcon = 2;
    }

    // ---- ending a war ----

    /** Brings a war to an end, with what each way of ending it means for the president. */
    static void finish(Game g, War x, int how) {
        World w = g.world;
        int c = x.enemy;
        double f = x.front;
        History.end(g, x, how);
        if (how == WS_TIMETABLE) { if (f >= 55) g.perf += 1; else { g.perf -= 1.5; w.unrest[c] = clamp(w.unrest[c] + 15, 0, 100); w.deter = clamp(w.deter - 3, 0, 100); } }
        if (how == WS_HANDED) { g.perf += 1; if (f < 70) w.unrest[c] = clamp(w.unrest[c] + 20, 0, 100); }
        if (how == WS_DECLARED) {
            if (f >= 70) { g.perf += 1; g.rally += 1; }
            else { g.perf -= 2; w.deter = clamp(w.deter - 6, 0, 100); for (int o = 0; o < NC; o++) if (align(g, o) == AL_TREATY) Statecraft.rel(w, o, -4); }
        }
        if (how == WS_WON) won(g, x);
        if (x.kind >= 0) { w.rel[c] = Math.max(w.rel[c], -60); if (!w.toppled[c]) w.sanction[c] = Math.max(1, w.sanction[c] - 1); }
        g.econ.war = warSpend(w);
    }

    /** What winning buys: strikes set back the weapons programs, a no-fly zone calms the fighting, and enemies learn to fear you. */
    static void won(Game g, War x) {
        World w = g.world;
        int c = x.enemy;
        if (x.kind < 0 || c < 0) return;
        w.deter = clamp(w.deter + 4, 0, 100);
        if (x.kind == WK_STRIKES && c == CC_IR) { w.iran = clamp(w.iran - 30, 0, 100); w.proxy = clamp(w.proxy + 8, 0, 100); }
        if (x.kind == WK_STRIKES && c == CC_KP) w.nk = clamp(w.nk - 20, 0, 100);
        if (x.kind == WK_STRIKES && c == CC_SY) w.unrest[c] = clamp(w.unrest[c] + 10, 0, 100);
        if (x.kind == WK_NOFLY) { w.unrest[c] = clamp(w.unrest[c] - 25, 0, 100); w.refugees = clamp(w.refugees - 5, 0, 100); }
        if (x.kind == WK_OCCUPATION) { w.unrest[c] = clamp(w.unrest[c] - 30, 0, 100); w.rel[c] = Math.max(w.rel[c], 40); }
    }

    public static String declareVictory(Game g, War x) {
        if (x.stage != WS_FIGHTING) return null;
        boolean real = x.front >= 70;
        finish(g, x, WS_DECLARED);
        return real ? "You declare victory and bring the troops home. Most of the country agrees." : "You declare victory and leave. Few believe it, and allies and enemies take note.";
    }

    public static String timetable(Game g, War x, int weeks) {
        if (x.stage != WS_FIGHTING) return null;
        x.timetable = g.week + weeks;
        x.timetableStart = g.week;
        x.troops0 = x.troops;
        x.support = clamp(x.support + 4, 0, 100);
        g.log(NK_WORLD, "The president sets a date to bring the troops home from " + x.name.toLowerCase().replace("the ", ""), "The last troops are to leave by " + dateStr(x.timetable * 7) + ".", 1.8);
        return "The last troops leave by " + dateStr(x.timetable * 7) + ". The enemy can count the days too.";
    }

    /** Who can take over: the new government after an occupation, allies or the UN otherwise. */
    public static String successor(War x) { return x.kind == WK_OCCUPATION ? "the new government's forces" : "allies and UN peacekeepers"; }

    public static String cannotHand(War x) { return x.front < 55 ? "Nobody can take over until the front is steadier (55 or better)." : x.handoff >= 0 ? "The handover is under way." : null; }

    public static String handoff(Game g, War x) {
        String no = cannotHand(x);
        if (no != null || x.stage != WS_FIGHTING) return no;
        x.handoff = g.week + 8;
        g.log(NK_WORLD, x.name + ": American forces begin handing over to " + successor(x), null, 1.6);
        return "The handover to " + successor(x) + " takes about two months.";
    }

    public static String escalate(Game g, War x) {
        if (x.stage != WS_FIGHTING) return null;
        History.setTroops(g, x, Math.min(x.need * 1.6, x.troops * 1.5));
        History.setStrategy(g, x, 2);
        x.front = clamp(x.front + 4, 0, 100);
        x.support = clamp(x.support - 4, 0, 100);
        if (nuke(g, x.enemy)) g.world.defcon = Math.max(1, g.world.defcon - 1);
        g.log(NK_BIG, ("President " + g.pres.last + " escalates " + x.name.toLowerCase()).toUpperCase(), null, 2.5);
        return "More troops, a harder offensive, and higher casualties.";
    }

    // ---- Congress ----

    /**
     * The odds Congress authorizes force: likely against a hostile regime, hard against anyone else, close to
     * impossible against a friend. Since the 1970s it takes sixty senators to get past a filibuster.
     */
    public static double authorizeOdds(Game g, int c) {
        World w = g.world;
        double host = w.rel[c] < -70 ? 0.8 : w.rel[c] < -40 ? 0.3 : w.rel[c] < 0 ? -1.5 : -2.8;
        int al = align(g, c);
        if (al == AL_TREATY) host -= 2.5; else if (al == AL_PARTNER) host -= 1;
        if (nuke(g, c)) host -= 0.9;
        if (yearNow(g) >= 2004) host -= 0.2;   // after Iraq, Congress is warier
        double h = Statecraft.yes(g, HOUSE, 1.2 + host, host - 0.3, 1.8), s = Statecraft.yes(g, SENATE, 1.2 + host, host - 0.3, 1.8);
        int sNeed = yearNow(g) >= 1975 ? 60 : 51;
        return logistic((h - 218) / 9) * logistic((s - sNeed + 0.5) / 3);
    }

    /** Asks Congress to authorize the use of force against a country. */
    public static String authorize(Game g, int c) {
        World w = g.world;
        if (authorized(g, c)) return "Congress has already authorized force against " + cnameIn(g, c) + ".";
        String n = cnameIn(g, c);
        if (g.rng.nextDouble() < authorizeOdds(g, c)) {
            w.authorized[c] = Math.max(1, g.week);
            for (War x : w.wars) if (x.stage == WS_FIGHTING && x.enemy == c) { x.authorized = true; x.legit = clamp(x.legit + 12, 5, 70); x.support = clamp(x.support + 10, 0, 100); }
            g.log(NK_BIG, ("Congress authorizes the use of force against " + n).toUpperCase(), null, 3.5);
            g.rally += 1;
            return "Congress votes to authorize force against " + n + ".";
        }
        g.perf -= 0.5;
        for (War x : w.wars) if (x.stage == WS_FIGHTING && x.enemy == c) x.support = clamp(x.support - 8, 0, 100);
        g.log(NK_BIG, ("Congress refuses to authorize force against " + n).toUpperCase(), null, 3);
        return "Congress refuses.";
    }

    /** Your answer to a War Powers resolution: veto it and fight on, or bring the troops home within thirty days. */
    public static void warPowers(Game g, Decision d, int k) {
        War x = d.war;
        if (x == null || x.stage != WS_FIGHTING) return;
        if (k == 1) { timetable(g, x, 4); return; }
        g.cong.vetoes++;
        g.perf -= 0.5;
        if (g.ex != null) g.ex.impPressure += 8;
        x.support = clamp(x.support - 3, 0, 100);
        g.log(NK_BIG, "PRESIDENT VETOES THE WAR POWERS RESOLUTION; THE WAR GOES ON", "Critics in Congress call it a constitutional crisis.", 3);
    }

    /** What the hands-off president does with a War Powers resolution: obeys it if the war is going badly. */
    public static int auto(Game g, Decision d) { return d.war != null && d.war.front < 55 ? 1 : 0; }
}
