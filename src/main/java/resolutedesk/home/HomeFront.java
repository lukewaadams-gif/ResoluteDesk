package resolutedesk.home;

import static resolutedesk.core.Dates.*;
import static resolutedesk.core.Fmt.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.data.StateData.*;
import static resolutedesk.executive.Posts.*;
import static resolutedesk.executive.ScandalData.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.home.DisasterData.*;
import static resolutedesk.home.EventData.*;
import static resolutedesk.home.PoliticsData.*;
import static resolutedesk.model.Attributes.*;
import static resolutedesk.model.NewsKinds.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.opinion.Issues.*;
import static resolutedesk.press.Outlets.*;
import static resolutedesk.sim.DecisionKinds.*;
import static resolutedesk.world.WorldTables.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import resolutedesk.congress.Bloc;
import resolutedesk.congress.Hill;
import resolutedesk.congress.Midterms;
import resolutedesk.economy.Economy;
import resolutedesk.executive.Cabinet;
import resolutedesk.executive.Trouble;
import resolutedesk.model.Econ;
import resolutedesk.model.Game;
import resolutedesk.press.Speech;
import resolutedesk.sim.Decision;
import resolutedesk.sim.Decisions;
import resolutedesk.world.Diplo;
import resolutedesk.world.War;
import resolutedesk.world.World;

/** Events at home, disasters, the president's party and the opposition, and the daily brief. */
public final class HomeFront {
    public static void setup(Game g) {
        Home h = new Home();
        g.home = h;
        if (g.era.year >= 2029) { h.crime = 90; h.od = 55; h.grid = 50; }
        h.followers = g.pres != null ? 10 + g.pres.a[A_SMD] * 0.5 : 30;
        for (int i = 0; i < h.tone.length; i++) h.tone[i] = toneTarget(g, i);
        for (int i = 0; i < EV_ID.length; i++) h.cool[i] = 6 + i % 7;
    }

    static double toneTarget(Game g, int o) {
        Home h = g.home;
        double side = g.party == REP ? 1 : -1;
        return clamp(50 + OUT_LEAN[o] * side * 32 + (g.approval - 45) * 0.4 + (h.press - 50) * 0.3, 5, 95);
    }

    public static String[] factions(Game g) {
        int y = yearNow(g);
        if (g.party == DEM) return y < 1965 ? new String[]{"New Dealers", "Southern Democrats"} : y < 1993 ? new String[]{"Liberals", "Moderates"} : y < 2016 ? new String[]{"Progressives", "New Democrats"} : FAC_D;
        return y < 1965 ? new String[]{"Old Guard conservatives", "Eastern moderates"} : y < 1981 ? new String[]{"Conservatives", "Rockefeller Republicans"} : y < 2010 ? new String[]{"Reagan conservatives", "Moderates"} : y < 2016 ? new String[]{"Tea Party conservatives", "Establishment Republicans"} : FAC_R;
    }

    /** Records where an action lands, for your party's two wings. */
    public static void act(Game g, double econ, double cult, double w) {
        Home h = g.home;
        if (h == null) return;
        h.recE = (h.recE * h.recW + econ * w) / (h.recW + w);
        h.recC = (h.recC * h.recW + cult * w) / (h.recW + w);
        h.recW = Math.min(12, h.recW + w);
    }

    public static double var(Game g, String k) {
        Home h = g.home;
        Econ e = g.econ;
        if (k.equals("month")) return dateOf(g.day())[1] + 1;
        if (k.equals("year")) return yearNow(g);
        if (k.equals("crimeidx")) return h.crime; if (k.equals("od")) return h.od; if (k.equals("protest")) return h.protest; if (k.equals("grid")) return h.grid;
        if (k.equals("pand")) return h.pand; if (k.equals("pandw")) return h.pandWeek < 0 ? 99 : g.week - h.pandWeek;
        if (k.equals("ai")) return g.era.ai ? 1 : 0; if (k.equals("infl")) return e.infl; if (k.equals("app")) return g.approval;
        return g.world != null ? Diplo.var(g, k) : 0;
    }

    static boolean cond(Game g, String c) {
        if (c.isEmpty()) return true;
        for (String t : c.split(",")) {
            if (t.startsWith("p=")) { if (g.rng.nextDouble() >= Double.parseDouble(t.substring(2))) return false; continue; }
            int i = t.indexOf('>'), j = t.indexOf('<');
            if (i > 0) { if (!(var(g, t.substring(0, i)) > Double.parseDouble(t.substring(i + 1)))) return false; }
            else if (j > 0) { if (!(var(g, t.substring(0, j)) < Double.parseDouble(t.substring(j + 1)))) return false; }
        }
        return true;
    }

    public static void week(Game g) {
        Home h = g.home;
        Random r = g.rng;
        Econ e = g.econ;
        double yrs = g.week / 52.0;
        // crime and overdoses
        double crT = (g.era.year >= 2029 ? 90 : 100) - 2.5 * Math.min(yrs, 3) + (e.unemp - 4.3) * 3 + (h.protest - 30) * 0.05;
        h.crime = clamp(h.crime + (crT - h.crime) * 0.012 + r.nextGaussian() * 0.35, 60, 160);
        double odT = (g.era.year >= 2029 ? 55 : 80) * Math.pow(0.93, Math.min(yrs, 4)) + (g.world != null ? (g.world.cartel - 55) * 0.25 : 0);
        h.od = clamp(h.od + (odT - h.od) * 0.01 + r.nextGaussian() * 0.25, 20, 150);
        g.issueAdj[PI_CRIME] = clamp(g.issueAdj[PI_CRIME] - (h.crime - 97) * 0.0002, -1.5, 1.5);
        // protest, the grid, the press
        h.protest = clamp(h.protest + (20 + Math.max(0, 45 - g.approval) * 0.6 - h.protest) * 0.05 + r.nextGaussian() * 1.2, 0, 100);
        h.grid = clamp(h.grid + (30 + (e.aiInvest - 100) * 0.25 - h.grid) * 0.01 + r.nextGaussian() * 0.6, 0, 100);
        h.press = clamp(h.press + (50 + (Cabinet.sq(g, S_PRESS) - 0.6) * 30 - h.press) * 0.03, 0, 100);
        for (int i = 0; i < h.tone.length; i++) h.tone[i] += (toneTarget(g, i) - h.tone[i]) * 0.05;
        // the party's wings
        double[] fe = g.party == DEM ? FAC_E_D : FAC_E_R, fc = g.party == DEM ? FAC_C_D : FAC_C_R;
        for (int f = 0; f < 2; f++) {
            double dist = h.recW < 0.5 ? 0.3 : Math.sqrt(Math.pow(h.recE - fe[f], 2) + Math.pow(h.recC - fc[f], 2));
            double t = 68 - 34 * dist + (g.approval - 45) * 0.5;
            h.fac[f] = clamp(h.fac[f] + (t - h.fac[f]) * 0.03 + r.nextGaussian() * 0.5, 0, 100);
        }
        for (Bloc b : g.cong.blocs) {
            if (b.party != g.party) continue;
            boolean wing = b.name.contains("Progressive") || b.name.contains("Freedom") || b.name.contains("Hardliners");
            b.rel = clamp(b.rel + (h.fac[wing ? 0 : 1] - 55) * 0.006, 0, 100);
        }
        // the opposition picks a strategy every quarter
        if (g.week - h.oppWeek >= 13) {
            h.oppWeek = g.week;
            int s = g.approval < 43 ? 2 : g.approval > 55 ? 0 : 1;
            if (Midterms.season(g) || g.tw() > 170) s = Math.min(2, s + 1);
            if (s != h.opp) g.log(NK_INFO, "The " + PARTY[Hill.other(g.party)].toLowerCase() + " opposition's new strategy: " + OPP_STRAT[s].toLowerCase(), null, 0.9);
            h.opp = s;
        }
        for (Bloc b : g.cong.blocs) if (b.party != g.party) b.rel = clamp(b.rel + (1 - h.opp) * 0.06, 0, 100);
        if (h.opp == 2 && r.nextDouble() < 0.04) {
            int w = worstIssue(g);
            g.issueAdj[w] = clamp(g.issueAdj[w] - 0.05, -1.5, 1.5);
            g.log(NK_BAD, PARTIES[Hill.other(g.party)] + " hammer you on " + PISSUE[w].toLowerCase(), null, 1.0);
        }
        // the pandemic
        pandemic(g);
        // disasters and events
        disasters(g);
        for (int i = 0; i < h.cool.length; i++) if (h.cool[i] > 0) h.cool[i]--;
        events(g);
        // the State of the Union
        int[] d = dateOf(g.day());
        boolean sotu = g.week < 52 && g.term == 1 ? Economy.weekHas(g, 1, 25) : Economy.weekHas(g, 1, 4);
        if (sotu && h.sotuYear != d[0] && h.spType < 0) { h.sotuYear = d[0]; Speech.start(g, 1); }
        brief(g);
    }

    public static int worstIssue(Game g) { int w = 0; for (int i = 1; i < NPI; i++) if (g.issueApp[i] * g.issueSal[i] - 45 * g.issueSal[i] < g.issueApp[w] * g.issueSal[w] - 45 * g.issueSal[w]) w = i; return w; }

    static void pandemic(Game g) {
        Home h = g.home;
        Random r = g.rng;
        if (h.pand == 0 && r.nextDouble() < 0.0006) { h.pand = 1; h.pandWeek = g.week; h.pandSev = 0.6 + r.nextDouble(); g.log(NK_WORLD, "Health officials track a new virus spreading abroad", null, 1.6); return; }
        if (h.pand == 1 && g.week - h.pandWeek > 2 && r.nextDouble() < (h.pandSlow > 0 ? 0.05 : 0.14)) { h.pand = 2; h.pandWeek = g.week; g.log(NK_BIG, "THE NEW VIRUS IS SPREADING IN THE UNITED STATES", null, 4); }
        if (h.pand == 1 && g.week - h.pandWeek > 20) { h.pand = 0; g.log(NK_GOOD, "The overseas outbreak burns out", null, 1.2); }
        if (h.pand == 2) {
            double hq = Cabinet.pq(g, P_HHS) * 0.5 + Cabinet.rd(g, P_HHS) / 200;
            g.econ.demandShock -= 0.05 * h.pandSev * (h.pandSlow > 0 ? 1.2 : 0.8);
            g.issueSal[PI_HEALTH] = Math.min(2, g.issueSal[PI_HEALTH] + 0.03);
            g.issueAdj[PI_HEALTH] = clamp(g.issueAdj[PI_HEALTH] + (hq - 0.55) * 0.02, -1.5, 1.5);
            h.deaths += (int) Math.round((800 + r.nextInt(600)) * h.pandSev * (1.4 - hq) * (h.pandSlow > 0 ? 0.7 : 1));
            g.stress = Math.min(100, g.stress + 1.5);
            int len = h.pandFast > 0 ? 26 : 40;
            if (g.week - h.pandWeek > len) { h.pand = 3; g.log(NK_BIG, "THE PANDEMIC IS WANING", "About " + String.format("%,d", h.deaths) + " Americans have died.", 3.5); }
        }
    }

    static void disasters(Game g) {
        Home h = g.home;
        Random r = g.rng;
        int m = dateOf(g.day())[1];
        for (int k = 0; k < DIS_NAME.length; k++) {
            boolean season = DIS_M0[k] <= DIS_M1[k] ? m >= DIS_M0[k] && m <= DIS_M1[k] : m >= DIS_M0[k] || m <= DIS_M1[k];
            double p = DIS_P[k] * (k == 0 && m >= 7 && m <= 9 ? 1.6 : 1);
            if (!season || r.nextDouble() > p) continue;
            String[] st = DIS_STATES[k].split(" ");
            int s = stateIdx(st[r.nextInt(st.length)]);
            int sev = 1 + (r.nextDouble() < 0.5 ? 0 : r.nextDouble() < 0.5 ? 1 : r.nextDouble() < 0.6 ? 2 : 3);
            h.disState = s; h.disKind = k; h.disSev = sev;
            String what = (k == 0 ? "A Category " + (sev + 1) + " hurricane" : sev >= 3 ? "A catastrophic " + DIS_NAME[k] : sev == 2 ? "A severe " + DIS_NAME[k] : "A " + DIS_NAME[k]) + " strikes " + STATES[s].name;
            h.disasters.add(dateStr(g.day()) + ": " + what);
            g.log(sev >= 3 ? NK_BIG : NK_BAD, sev >= 3 ? what.toUpperCase() : what, null, 1 + sev * 0.7);
            if (sev >= 2) {
                Decision d = Decisions.add(g, D_DISASTER, what, "The governor has asked for a federal disaster declaration. FEMA is " + (Cabinet.rd(g, P_DHS) >= 60 ? "ready to move." : "stretched thin."),
                        new String[]{"Declare a major disaster and go there yourself", "Declare a major disaster and send the vice president", "Let FEMA handle it"},
                        new String[]{"Chief of staff: \"People need to see you there.\"", "Homeland Security secretary: \"Your visit ties up resources for a day. The vice president can go.\"", "Budget director: \"FEMA knows what it's doing.\""});
                d.a = s; d.b = sev;
            }
            return;
        }
    }

    public static void disasterDecision(Game g, Decision d, int k) {
        int s = d.a, sev = d.b;
        double q = (Cabinet.pq(g, P_DHS) + Cabinet.rd(g, P_DHS) / 100) / 2;
        boolean good = g.rng.nextDouble() < 0.35 + q * 0.6;
        g.econ.fiscal += 0.004 * sev * (k < 2 ? 1 : 0.6);
        if (k == 0) { g.stamina = Math.max(0, g.stamina - 5); g.stateBump[s] += 1.5 * sev * (good ? 1 : 0.3); g.perf += good ? 0.4 : -0.2; }
        else if (k == 1) { g.stateBump[s] += 0.8 * sev * (good ? 1 : 0.3); }
        else { g.stateBump[s] -= good ? 0 : sev * 0.8; }
        if (good) g.log(NK_GOOD, "FEMA's response in " + STATES[s].name + " wins praise from the governor", null, 1.0);
        else {
            g.log(NK_BAD, "Victims in " + STATES[s].name + " say federal help is too slow", null, 1.6);
            g.perf -= 0.3 * sev;
            if (sev >= 3) Trouble.add(g, SK_FAILURE, "The disaster response in " + STATES[s].name, "Survivors are still waiting for help weeks later.", 1.5 + sev * 0.5, null, false);
        }
    }

    public static void events(Game g) {
        Home h = g.home;
        for (int i = 0; i < EV_ID.length; i++) {
            if (h.cool[i] > 0) continue;
            if (!cond(g, EV_COND[i])) continue;
            h.cool[i] = EV_COOL[i];
            if (EV_ID[i].equals("violence") || EV_ID[i].equals("fed_bldg")) g.stress = Math.min(100, g.stress + 5);
            if (EV_ID[i].equals("police")) h.protest = Math.min(100, h.protest + 25);
            if (EV_ID[i].equals("strike")) g.econ.supplyShock += 0.05;
            g.log(NK_BAD, EV_TITLE[i], null, 1.8);
            String[] opts = EV_OPTS[i].split(";;");
            String[] lab = new String[opts.length], adv = new String[opts.length];
            for (int k = 0; k < opts.length; k++) {
                String[] t = opts[k].split("\\|");
                lab[k] = t[0];
                adv[k] = t[1] + ": \"" + Diplo.odds(g, t[2], Double.parseDouble(t[3])) + "\"";
            }
            Decision d = Decisions.add(g, D_HOME, EV_TITLE[i], EV_BODY[i], lab, adv);
            d.a = i;
            return;
        }
    }

    public static void eventDecision(Game g, int i, int k) {
        String[] t = EV_OPTS[i].split(";;")[k].split("\\|");
        boolean ok = g.rng.nextDouble() < Diplo.p(g, t[2], Double.parseDouble(t[3]));
        if (Double.parseDouble(t[3]) < 1) { if (ok) g.okCount++; else g.failCount++; }
        Diplo.fx(g, ok ? t[4] : t[5]);
        g.log(ok ? NK_GOOD : NK_BAD, ok ? t[6] : t[7], null, 1.4);
        if (Double.parseDouble(t[3]) < 1) Decisions.info(g, ok ? "It worked" : "It didn't go as hoped", ok ? t[6] : t[7]);
    }

    /** The President's Daily Brief: what the intelligence community and your staff want you to know this week. */
    public static void brief(Game g) {
        Home h = g.home;
        World w = g.world;
        List<String> b = new ArrayList<String>();
        if (w != null && w.wars != null) for (War x : w.wars) if (x.stage == 2)
            b.add((x.front < 35 ? "3" : "2") + "|" + x.name.toUpperCase().replace("THE ", "") + ": " + (x.front >= 70 ? "Our forces are advancing." : x.front >= 45 ? "The front is holding." : "The enemy is gaining ground.")
                    + " " + thousands(Math.round(x.troops * 1000)) + " Americans deployed; " + thousands(Math.round(x.kia)) + " killed so far. Support at home: " + Math.round(x.support) + "%.");
        if (w != null && w.modern) {
            if (w.twStage >= 2) b.add("3|TAIWAN: The PLA is " + (w.twStage >= 3 ? "enforcing a " + TW_STAGE[w.twStage].toLowerCase() : "drilling around the island") + ". Our deterrence index is " + Math.round(w.deter) + ".");
            else if (w.twt > 45) b.add("2|CHINA: Pressure on Taiwan is building: more incursions into its air-defense zone.");
            if (w.uaStage == 2) b.add((w.ukr < 35 ? "3" : "2") + "|UKRAINE: " + (w.ukr < 35 ? "Ukrainian lines are under severe strain." : "The front is grinding but holding.") + " Talks index " + Math.round(w.talks) + ".");
            if (w.iran > 80) b.add("3|IRAN: Enrichment is near weapons grade. Breakout could be weeks away.");
            else if (w.iran > 60) b.add("2|IRAN: Enrichment continues at a steady pace.");
            if (w.redsea > 55) b.add("2|RED SEA: Houthi attacks on shipping are rising; insurers are raising rates.");
            if (w.proxy > 55) b.add("2|IRAQ AND SYRIA: Militias are planning more attacks on US bases.");
            if (w.nk > 70) b.add("2|NORTH KOREA: Activity at the nuclear test site.");
            if (w.terror > 50) b.add("3|TERRORISM: Increased chatter about plots against Americans.");
            if (w.cyber > 55) b.add("2|CYBER: Ransomware groups are probing pipelines and hospitals.");
        }
        if (h.pand == 1) b.add("3|HEALTH: A new virus is spreading abroad. Its severity is still unclear.");
        if (h.pand == 2) b.add("3|HEALTH: The virus is spreading here. About " + String.format("%,d", h.deaths) + " deaths so far.");
        if (h.grid > 55) b.add("1|ENERGY: The grid has little spare capacity; a heat wave or a storm could cause blackouts.");
        if (h.od > 85) b.add("2|FENTANYL: Overdose deaths are rising again.");
        if (h.protest > 55) b.add("1|DOMESTIC: Protests are expected in several cities this weekend.");
        if (b.isEmpty()) b.add("1|A quiet week. Nothing urgent overnight.");
        h.brief = b;
    }
}
