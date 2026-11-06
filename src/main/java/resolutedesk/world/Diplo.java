package resolutedesk.world;

import static resolutedesk.congress.CongressRules.*;
import static resolutedesk.core.Dates.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.data.StateData.*;
import static resolutedesk.executive.ActionData.*;
import static resolutedesk.executive.Posts.*;
import static resolutedesk.executive.ScandalData.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.model.Attributes.*;
import static resolutedesk.model.NewsKinds.*;
import static resolutedesk.opinion.Issues.*;
import static resolutedesk.sim.DecisionKinds.*;
import static resolutedesk.world.Countries.*;
import static resolutedesk.world.CrisisData.*;
import static resolutedesk.world.WorldData.*;
import static resolutedesk.world.WorldTables.*;

import java.util.Random;
import resolutedesk.congress.Bloc;
import resolutedesk.congress.Hill;
import resolutedesk.economy.Economy;
import resolutedesk.executive.Cabinet;
import resolutedesk.executive.Trouble;
import resolutedesk.history.History;
import resolutedesk.model.Econ;
import resolutedesk.model.Game;
import resolutedesk.sim.Decision;
import resolutedesk.sim.Decisions;

/** Diplomacy: relations, calls, visits, summits, trade, sanctions, crises and operations. */
public final class Diplo {
    public static void setup(Game g) {
        World w = new World();
        g.world = w;
        boolean y29 = g.era.year >= 2029;
        for (int c = 0; c < NC; c++) {
            String[] t = (y29 ? LEADER29 : LEADER25)[c].split("\\|");
            w.leader[c] = t[0].replace("*", ""); w.ltitle[c] = t[1];
            w.rel[c] = CT_REL[c] + (y29 ? 0 : 0);
            w.lastCall[c] = -10; w.visitAt[c] = -1;
        }
        int[] sanc = {CC_RU, CC_IR, CC_KP, CC_CU, CC_VE, CC_SY};
        int[] lvl = {3, 3, 3, 3, 2, 2};
        for (int i = 0; i < sanc.length; i++) w.sanction[sanc[i]] = lvl[i];
        w.sanction[CC_CN] = 1;
        w.aid[CC_UA] = g.era.year == 2025 ? 2 : 1; w.aid[CC_IL] = 2; w.aid[CC_TW] = 1; w.aid[CC_EG] = 1; w.aid[CC_JO] = 1;
        if (y29) { w.twt = 55; w.iran = 45; w.ukr = 45; w.ruGain = 30; w.nato = 50; w.allySpend = 2.6; w.migr = 40; w.redsea = 35; }
        if (g.set.scenario == 2) { w.twt = Math.max(w.twt, 50); w.proxy = 60; w.terror = 50; }
        // what's already in the economy's tariff stays; tariffs set here are on top of it
        w.tariffAdd = 0;
        History.world(g, g.era.year, true);
        Statecraft.setup(g);
        Objectives.refresh(g);
    }

    static double totalTrade() { double t = 0; for (double v : CT_TRADE) t += v; return t + 900; }

    public static void setTariff(Game g, int c, int level) {
        World w = g.world;
        w.tariff[c] = level;
        double add = 0, tot = totalTrade();
        for (int i = 0; i < NC; i++) add += TARIFF_VAL[w.tariff[i]] * CT_TRADE[i] / tot;
        g.econ.tariff += add - w.tariffAdd;
        w.tariffAdd = add;
        if (level > 0) { w.rel[c] -= 4 + level * 3; g.log(NK_ECON, "New " + TARIFF_VAL[level] + "% tariffs on goods from " + cnameIn(g, c), null, tradeOf(g, c) > 100 ? 1.6 : 0.7); }
        else g.log(NK_ECON, "Tariffs on " + cname(g, c) + " are lifted", null, 0.8);
    }

    public static void setSanction(Game g, int c, int level) {
        World w = g.world;
        int old = w.sanction[c];
        w.sanction[c] = level;
        w.rel[c] -= (level - old) * 6;
        if (c == CC_RU || c == CC_IR || c == CC_VE) oil(g, (level - old) * (c == CC_RU ? 2.0 : 1.0));
        g.log(NK_WORLD, (level > old ? "Tougher" : "Lighter") + " sanctions on " + cnameIn(g, c), null, power(g, c) >= 5 ? 1.4 : 0.6);
    }

    public static void setAid(Game g, int c, int level) {
        World w = g.world;
        int old = w.aid[c];
        w.aid[c] = level;
        w.rel[c] += (level - old) * 6;
        g.econ.fiscal += (level - old) * (c == CC_UA ? 0.06 : 0.015);
        g.log(NK_WORLD, (level > old ? "More" : "Less") + " American aid for " + cname(g, c), null, c == CC_UA || c == CC_IL ? 1.3 : 0.5);
    }

    static void oil(Game g, double d) { g.econ.oilBase += d; g.world.oilAdd += d; }

    /** A phone call: warmer relations, with diminishing returns. */
    public static String call(Game g, int c) {
        World w = g.world;
        if (w.callWeek != g.week) { w.callWeek = g.week; w.callsThisWeek = 0; }
        if (w.callsThisWeek >= 3) return "You've made three calls this week.";
        w.callsThisWeek++;
        double k = (g.pres.a[A_CHA] + g.pres.a[A_FPC]) / 140.0;
        double gain = (3 + 4 * k) * (g.week - w.lastCall[c] < 4 ? 0.4 : 1) * (align(g, c) == AL_ADVERSARY ? 0.6 : 1);
        w.rel[c] = clamp(w.rel[c] + gain, -100, 100);
        w.lastCall[c] = g.week;
        if (power(g, c) >= 6 || align(g, c) == AL_ADVERSARY) g.log(NK_WORLD, "President " + g.pres.last + " speaks with " + w.leader[c], null, 0.8);
        if (c == CC_CN) w.twt = Math.max(0, w.twt - 1.5);
        if (c == CC_RU && w.uaStage == 2) w.talks = Math.min(100, w.talks + 2);
        return "You speak with " + w.leader[c] + ". Relations " + (gain >= 4 ? "warm noticeably" : "warm a little") + ".";
    }

    public static String invite(Game g, int c) {
        World w = g.world;
        if (w.visitAt[c] >= 0) return w.leader[c] + " is already coming.";
        w.visitAt[c] = g.week + 3 + g.rng.nextInt(4);
        return w.leader[c] + " accepts an invitation to the White House around " + dateStr(w.visitAt[c] * 7) + ".";
    }

    public static String proposeSummit(Game g, int c) {
        World w = g.world;
        if (w.summitCountry >= 0) return "You already have a summit planned with " + cname(g, w.summitCountry) + ".";
        if (w.rel[c] < -70 && g.rng.nextDouble() < 0.5) { w.rel[c] -= 2; return cname(g, c) + " turns down the invitation."; }
        w.summitCountry = c; w.summitWeek = g.week + 4 + g.rng.nextInt(5); w.summitRound = 0;
        g.log(NK_WORLD, "The White House announces a summit with " + w.leader[c] + " around " + dateStr(w.summitWeek * 7), null, power(g, c) >= 6 ? 2.0 : 1.0);
        return "Summit with " + w.leader[c] + " set for around " + dateStr(w.summitWeek * 7) + ".";
    }

    /** A state visit or a trip abroad. */
    public static void visit(Game g, int c, boolean abroad) {
        World w = g.world;
        double k = (g.pres.a[A_CHA] * 0.6 + g.pres.a[A_FPC] * 0.4) / 70.0;
        w.rel[c] = clamp(w.rel[c] + (abroad ? 9 : 7) * k, -100, 100);
        w.visits[c]++;
        g.issueAdj[PI_FOREIGN] = clamp(g.issueAdj[PI_FOREIGN] + 0.03 * k, -1.5, 1.5);
        if (align(g, c) == AL_TREATY) { if (CT_REGION[c] == RG_EUROPE) w.nato = Math.min(100, w.nato + 2); else w.pacific = Math.min(100, w.pacific + 2); }
        g.log(NK_WORLD, abroad ? "President " + g.pres.last + " visits " + cname(g, c) : w.leader[c] + " visits the White House", null, power(g, c) >= 6 ? 1.4 : 0.8);
    }

    // ---- the weekly turn ----
    public static void week(Game g) {
        World w = g.world;
        Random r = g.rng;
        Econ e = g.econ;
        Statecraft.week(g);
        Objectives.refresh(g);
        if (g.era.year == 2025) for (String ch : CHANGES25) {
            String[] t = ch.split("\\|");
            if (Integer.parseInt(t[1]) != g.week) continue;
            int c = ctIdx(t[0]);
            w.leader[c] = t[2]; w.ltitle[c] = t[3];
            g.log(NK_WORLD, t[2] + " becomes " + t[3].toLowerCase() + " of " + cnameIn(g, c), null, power(g, c) >= 6 ? 1.6 : 0.8);
        }
        // relations drift toward what policy earns
        double fpc = g.pres != null ? (g.pres.a[A_FPC] - 60) / 4.0 : 0;
        for (int c = 0; c < NC; c++) {
            if (w.gone != null && w.gone[c]) continue;
            double target = (w.base != null ? w.base[c] : CT_REL[c]) + w.aid[c] * 4 - w.sanction[c] * 5 - TARIFF_VAL[w.tariff[c]] / 2.0 + fpc
                    + (align(g, c) == AL_TREATY ? ((CT_REGION[c] == RG_EUROPE ? w.nato : w.pacific) - 60) / 8 : 0);
            if (c == CC_TW || c == CC_JP || c == CC_KR || c == CC_PH || c == CC_AU) target += (w.deter - 55) / 4;
            if (c == CC_UA) target += w.aid[c] * 4;
            w.rel[c] = clamp(w.rel[c] + (target - w.rel[c]) * 0.012 + r.nextGaussian() * 0.4, -100, 100);
        }
        // tariffs invite retaliation
        for (int c = 0; c < NC; c++) if (w.tariff[c] >= 2 && power(g, c) >= 5 && r.nextDouble() < 0.015) {
            e.farm = Math.max(40, e.farm - 4); e.finShock -= 0.25;
            g.log(NK_ECON, cname(g, c) + " retaliates with tariffs on American farm goods", null, 1.4);
            w.rel[c] -= 3;
        }
        theaters(g);
        // the economy feels the world
        double oilT = (w.redsea - 40) * 0.08 + (w.twStage >= 3 ? 4 : 0) + (w.uaStage == 2 ? 2 : 0) + (w.iran > 90 ? 3 : 0);
        double dOil = (oilT - w.oilAdd) * 0.05;
        oil(g, dOil);
        if (w.twStage >= 3) e.supplyShock += 0.004 * (w.twStage - 2);
        // standing on foreign policy and security
        double ally = 0; int n = 0;
        for (int c = 0; c < NC; c++) if (align(g, c) == AL_TREATY && !gone(g, c)) { ally += w.rel[c]; n++; }
        ally /= Math.max(1, n);
        g.issueAdj[PI_FOREIGN] = clamp(g.issueAdj[PI_FOREIGN] + (ally - 55) * 0.0002 + (w.uaStage < 2 ? 0.002 : 0) - (w.twStage >= 3 ? 0.004 : 0), -1.5, 1.5);
        g.issueAdj[PI_SECURITY] = clamp(g.issueAdj[PI_SECURITY] - (w.terror - 40) * 0.0002 - (w.defcon < 5 ? 0.004 : 0), -1.5, 1.5);
        g.issueAdj[PI_IMMIG] = clamp(g.issueAdj[PI_IMMIG] - (w.migr - 50) * 0.0002, -1.5, 1.5);
        g.issueSal[PI_FOREIGN] = clamp(g.issueSal[PI_FOREIGN] * 0.995 + (w.twStage >= 3 || w.defcon < 5 ? 0.02 : 0), 0.2, 2);
        if (w.defcon < 5 && r.nextDouble() < 0.25) { w.defcon++; if (w.defcon == 5) g.log(NK_WORLD, "The Pentagon returns to normal readiness", null, 1.0); }
        for (int i = 0; i < w.cool.length; i++) if (w.cool[i] > 0) w.cool[i]--;
        for (int i = 0; i < w.opCool.length; i++) if (w.opCool[i] > 0) w.opCool[i]--;
        for (int i = 0; i < w.cvCool.length; i++) if (w.cvCool[i] > 0) w.cvCool[i]--;
        crises(g);
        // visits and summits
        for (int c = 0; c < NC; c++) if (w.visitAt[c] >= 0 && g.week >= w.visitAt[c]) { w.visitAt[c] = -1; visit(g, c, false); g.stamina = Math.max(0, g.stamina - 2); }
        if (w.summitCountry >= 0 && g.week >= w.summitWeek && w.summitRound == 0) Summit.start(g);
        int[] d = dateOf(g.day());
        for (String s : SUMMITS) {
            String[] t = s.split("\\|");
            int yr = yearNow(g);
            if (t[0].equals("G7") && yr < 1975 || t[0].equals("G20") && yr < 2008 || t[0].equals("NATO") && yr < 1957 || t[0].startsWith("UN") && yr < 1946) continue;
            int m = Integer.parseInt(t[1]) - 1, day = Integer.parseInt(t[2]);
            if (Economy.weekHas(g, m, day)) Summit.multilateral(g, t[0]);
        }
    }

    static void theaters(Game g) {
        World w = g.world;
        Random r = g.rng;
        double dq = Cabinet.pq(g, P_DEF), iq = (Cabinet.pq(g, P_CIA) + Cabinet.pq(g, P_DNI)) / 2;
        if (!w.modern) { oldWorld(g, iq); return; }
        // Taiwan
        double detT = 30 + w.posture[0] * 9 + w.aid[CC_TW] * 6 + w.pacific * 0.25 + dq * 10;
        w.deter += (detT - w.deter) * 0.03;
        w.twt = clamp(w.twt + 0.10 - (w.deter - 55) * 0.006 - (w.rel[CC_CN] + 30) * 0.002 + r.nextGaussian() * 0.9, 0, 100);
        if (w.twStage < 5 && w.twt > 42 + 12 * w.twStage && r.nextDouble() < 0.05) { w.twStage++; g.log(w.twStage >= 3 ? NK_BIG : NK_WORLD, ("China escalates around Taiwan: " + TW_STAGE[w.twStage].toLowerCase()).toUpperCase(), null, w.twStage >= 3 ? 3.5 : 1.4); }
        else if (w.twStage > 0 && w.twStage < 5 && w.twt < 26 + 12 * w.twStage && r.nextDouble() < 0.08) { w.twStage--; g.log(NK_WORLD, "Tensions ease around Taiwan", null, 1.0); }
        if (w.twStage == 5 && w.deter > 60 && r.nextDouble() < 0.05) { w.twStage = 3; g.log(NK_BIG, "A CEASEFIRE HALTS THE FIGHTING AROUND TAIWAN", null, 4); }
        // Ukraine
        if (w.uaStage == 2) {
            w.ukr = clamp(w.ukr - 0.32 + w.aid[CC_UA] * 0.1 + (w.nato - 60) * 0.003 + (w.posture[1] - 1) * 0.03 + (40 - w.ukr) * 0.004 + r.nextGaussian() * 0.7, 5, 95);
            w.ruGain = clamp(w.ruGain + (50 - w.ukr) * 0.006 + r.nextGaussian() * 0.25, 0, 100);
            w.talks = clamp(w.talks + 0.01 + (w.ukr < 35 ? 0.04 : 0) - (w.talks > 50 ? 0.04 : 0) + r.nextGaussian() * 0.4, 0, 100);
            if (w.talks > 80 && r.nextDouble() < 0.03) {
                w.uaStage = 1;
                g.log(NK_BIG, "RUSSIA AND UKRAINE AGREE TO A CEASEFIRE", "The guns fall silent along the front for the first time in years.", 4.5);
                g.perf += 1.5; g.issueAdj[PI_FOREIGN] = clamp(g.issueAdj[PI_FOREIGN] + 0.3, -1.5, 1.5); oil(g, -2);
                w.record.add(dateStr(g.day()) + ": a ceasefire in Ukraine");
            }
        } else if (w.uaStage == 1) {
            w.talks = clamp(w.talks + 0.1 + r.nextGaussian() * 0.5, 0, 100);
            if (w.talks > 95 && r.nextDouble() < 0.02) { w.uaStage = 0; g.log(NK_BIG, "A PEACE AGREEMENT ENDS THE WAR IN UKRAINE", null, 5); g.perf += 2; w.record.add(dateStr(g.day()) + ": peace in Ukraine"); }
            else if (r.nextDouble() < 0.004 + Math.max(0, 50 - w.talks) * 0.0004) { w.uaStage = 2; w.talks = 15; g.log(NK_BIG, "THE CEASEFIRE IN UKRAINE COLLAPSES", null, 4); g.perf -= 1; }
        }
        // the Middle East
        w.gaza = clamp(w.gaza + (45 - w.gaza) * 0.01 + r.nextGaussian() * 1.2, 0, 100);
        w.iran = clamp(w.iran + 0.12 - w.sanction[CC_IR] * 0.02 + r.nextGaussian() * 0.5, 0, 100);
        w.redsea = clamp(w.redsea + (w.gaza * 0.6 + 15 - w.redsea) * 0.02 - w.posture[2] * 0.12 + r.nextGaussian() * 1.0, 0, 100);
        w.proxy = clamp(w.proxy + (w.iran * 0.3 + w.gaza * 0.2 - w.proxy) * 0.015 - (w.posture[2] - 1) * 0.15 + r.nextGaussian() * 1.0, 0, 100);
        if (w.iran >= 100) { w.iran = 99; if (r.nextDouble() < 0.1) { g.log(NK_BIG, "IRAN TESTS A NUCLEAR WEAPON", null, 5); g.perf -= 2; w.iran = 80; w.record.add(dateStr(g.day()) + ": Iran goes nuclear"); } }
        // Korea, terror, cyber, the hemisphere
        w.nk = clamp(w.nk + (55 - w.nk) * 0.01 + 0.04 + r.nextGaussian() * 1.0, 0, 100);
        w.terror = clamp(w.terror + (30 + w.proxy * 0.15 - w.terror) * 0.02 - (iq - 0.55) * 0.4 + r.nextGaussian() * 1.0, 0, 100);
        w.cyber = clamp(w.cyber + (42 + (w.rel[CC_RU] < -50 ? 6 : 0) - w.cyber) * 0.02 + r.nextGaussian() * 1.1, 0, 100);
        w.migr = clamp(w.migr + (48 - (w.rel[CC_MX] - 30) / 4 - w.migr) * 0.02 + r.nextGaussian() * 1.0, 0, 100);
        w.cartel = clamp(w.cartel + (55 - w.cartel) * 0.01 + r.nextGaussian() * 0.8, 0, 100);
        // alliances
        double natoT = 40 + (avgRel(w, RG_EUROPE) - 40) * 0.3 + w.posture[1] * 4 + w.aid[CC_UA] * 2 - (w.allySpend - 2) * 4;
        w.nato = clamp(w.nato + (natoT - w.nato) * 0.02, 0, 100);
        double pacT = 40 + (w.rel[CC_JP] + w.rel[CC_KR] + w.rel[CC_AU] + w.rel[CC_PH]) / 4 * 0.3 + w.posture[0] * 4;
        w.pacific = clamp(w.pacific + (pacT - w.pacific) * 0.02, 0, 100);
    }

    /** Before the modern theaters: terror, spies, migration and the alliances, quietly. */
    static void oldWorld(Game g, double iq) {
        World w = g.world;
        Random r = g.rng;
        int y = yearNow(g);
        w.terror = clamp(w.terror + ((y >= 1993 ? 32 : 10) - w.terror) * 0.02 - (iq - 0.55) * 0.3 + r.nextGaussian() * 0.8, 0, 100);
        w.migr = clamp(w.migr + ((y >= 1975 ? 42 : 20) - w.migr) * 0.02 + r.nextGaussian() * 0.8, 0, 100);
        if (y >= 1949) {
            double natoT = 40 + (avgRel(w, RG_EUROPE) - 40) * 0.3 + w.posture[1] * 4;
            w.nato = clamp(w.nato + (natoT - w.nato) * 0.02, 0, 100);
        }
        if (y >= 1951) {
            double pacT = 40 + (w.rel[CC_JP] + (gone(g, CC_KR) ? 50 : w.rel[CC_KR]) + w.rel[CC_AU] + (gone(g, CC_PH) ? 50 : w.rel[CC_PH])) / 4 * 0.3 + w.posture[0] * 4;
            w.pacific = clamp(w.pacific + (pacT - w.pacific) * 0.02, 0, 100);
        }
    }

    static double avgRel(World w, int region) { double s = 0; int n = 0; for (int c = 0; c < NC; c++) if (CT_REGION[c] == region && (w.align != null ? w.align[c] : CT_ALIGN[c]) == AL_TREATY && (w.gone == null || !w.gone[c])) { s += w.rel[c]; n++; } return n == 0 ? 0 : s / n; }

    public static double var(Game g, String k) {
        World w = g.world;
        if (k.equals("twstage")) return w.twStage; if (k.equals("twt")) return w.twt; if (k.equals("ua")) return w.uaStage; if (k.equals("ukr")) return w.ukr;
        if (k.equals("talks")) return w.talks; if (k.equals("gaza")) return w.gaza; if (k.equals("iran")) return w.iran; if (k.equals("redsea")) return w.redsea;
        if (k.equals("proxy")) return w.proxy; if (k.equals("nk")) return w.nk; if (k.equals("terror")) return w.terror; if (k.equals("cyber")) return w.cyber;
        if (k.equals("migr")) return w.migr; if (k.equals("cartel")) return w.cartel; if (k.equals("tcn")) return TARIFF_VAL[w.tariff[CC_CN]] + g.econ.tariff - g.era.tariff;
        return 0;
    }

    /** "a>5,b<3,p=0.01": every clause must hold; p is a weekly chance. */
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

    public static void crises(Game g) {
        World w = g.world;
        if (!w.modern) return;
        for (int i = 0; i < CR_ID.length; i++) {
            if (w.cool[i] > 0) continue;
            if (!cond(g, CR_COND[i])) continue;
            w.cool[i] = CR_COOL[i];
            w.crises++;
            g.log(NK_WORLD, CR_TITLE[i], null, 2.0);
            String[] opts = CR_OPTS[i].split(";;");
            String[] lab = new String[opts.length], adv = new String[opts.length];
            for (int k = 0; k < opts.length; k++) {
                String[] t = opts[k].split("\\|");
                lab[k] = t[0];
                adv[k] = t[1] + ": \"" + odds(g, t[2], Double.parseDouble(t[3])) + "\"";
            }
            Decision d = Decisions.add(g, D_WORLD, CR_TITLE[i], CR_BODY[i] + " Your advisers each have a view; the odds are their best guess.", lab, adv);
            d.a = i;
            if (i == 2 || i == 6) g.stress = Math.min(100, g.stress + 12);
            return;
        }
    }

    /** How likely an option is to work, given who's doing it. */
    public static double p(Game g, String kind, double base) {
        if (base >= 1) return 1;
        double fpc = g.pres != null && (kind.equals("m") || kind.equals("d") || kind.equals("i")) ? (g.pres.a[A_FPC] - 60) / 250.0 : 0;
        double m = kind.equals("m") ? (Cabinet.rd(g, P_DEF) - 60) / 300.0 + (Cabinet.pq(g, P_DEF) - 0.6) * 0.2
                : kind.equals("d") ? (Cabinet.pq(g, P_STATE) - 0.6) * 0.3 + (Cabinet.sq(g, S_NSA) - 0.6) * 0.15
                : kind.equals("i") ? ((Cabinet.pq(g, P_CIA) + Cabinet.pq(g, P_DNI)) / 2 - 0.6) * 0.35
                : kind.equals("h") ? (Cabinet.rd(g, P_DHS) - 60) / 250.0 + (Cabinet.pq(g, P_DHS) - 0.6) * 0.25
                : kind.equals("j") ? (Cabinet.pq(g, P_DOJ) - 0.6) * 0.3 + (Cabinet.rd(g, P_DOJ) - 60) / 400.0
                : kind.equals("x") ? (Cabinet.rd(g, P_HHS) - 60) / 250.0 + (Cabinet.pq(g, P_HHS) - 0.6) * 0.25
                : kind.equals("c") ? (Cabinet.sq(g, S_COMMS) + Cabinet.sq(g, S_PRESS) - 1.2) * 0.15 + (g.pres.a[A_SPE] - 60) / 250.0
                : (Cabinet.sq(g, S_COS) - 0.6) * 0.15;
        return clamp(base + fpc + m, 0.05, 0.97);
    }

    public static String odds(Game g, String kind, double base) {
        double q = p(g, kind, base);
        return q >= 0.99 ? "This one is in our hands." : q >= 0.75 ? "I think it works: maybe " + Math.round(q * 100) + "%." : q >= 0.5 ? "Better than even, about " + Math.round(q * 100) + "%." : "A long shot, maybe " + Math.round(q * 100) + "%.";
    }

    public static void crisisDecision(Game g, int i, int k) {
        String[] t = CR_OPTS[i].split(";;")[k].split("\\|");
        boolean ok = g.rng.nextDouble() < p(g, t[2], Double.parseDouble(t[3]));
        if (ok) g.okCount++; else g.failCount++;
        fx(g, ok ? t[4] : t[5]);
        String story = ok ? t[6] : t[7];
        g.log(ok ? NK_GOOD : NK_BAD, story, null, 1.8);
        Decisions.info(g, ok ? "It worked" : "It didn't go as hoped", story);
    }

    /** Applies "key=value" effects to the world and the country. */
    public static void fx(Game g, String fx) {
        World w = g.world;
        Econ e = g.econ;
        for (String tok : fx.split(" ")) {
            if (tok.isEmpty()) continue;
            String[] kv = tok.split("=");
            String k = kv[0];
            if (History.fxKey(g, k, kv[1])) continue;
            double v = Double.parseDouble(kv[1]);
            if (k.startsWith("rel:")) { int c = ctIdx(k.substring(4)); if (c >= 0) w.rel[c] = clamp(w.rel[c] + v, -100, 100); }
            else if (k.equals("twt")) w.twt = clamp(w.twt + v, 0, 100); else if (k.equals("deter")) w.deter = clamp(w.deter + v, 0, 100);
            else if (k.equals("twstage")) w.twStage = (int) v; else if (k.equals("ua")) w.uaStage = (int) v; else if (k.equals("defcon")) w.defcon = (int) v;
            else if (k.equals("ukr")) w.ukr = clamp(w.ukr + v, 0, 100); else if (k.equals("talks")) w.talks = clamp(w.talks + v, 0, 100);
            else if (k.equals("gaza")) w.gaza = clamp(w.gaza + v, 0, 100); else if (k.equals("iran")) w.iran = clamp(w.iran + v, 0, 100);
            else if (k.equals("redsea")) w.redsea = clamp(w.redsea + v, 0, 100); else if (k.equals("proxy")) w.proxy = clamp(w.proxy + v, 0, 100);
            else if (k.equals("nk")) w.nk = clamp(w.nk + v, 0, 100); else if (k.equals("terror")) w.terror = clamp(w.terror + v, 0, 100);
            else if (k.equals("cyber")) w.cyber = clamp(w.cyber + v, 0, 100); else if (k.equals("migr")) w.migr = clamp(w.migr + v, 0, 100);
            else if (k.equals("cartel")) w.cartel = clamp(w.cartel + v, 0, 100);
            else if (k.equals("nato")) w.nato = clamp(w.nato + v, 0, 100); else if (k.equals("pacific")) w.pacific = clamp(w.pacific + v, 0, 100);
            else if (k.equals("perf")) g.perf += v; else if (k.equals("rally")) g.rally += v;
            else if (k.equals("for")) g.issueAdj[PI_FOREIGN] = clamp(g.issueAdj[PI_FOREIGN] + v, -1.5, 1.5);
            else if (k.equals("sec")) g.issueAdj[PI_SECURITY] = clamp(g.issueAdj[PI_SECURITY] + v, -1.5, 1.5);
            else if (k.equals("imm")) g.issueAdj[PI_IMMIG] = clamp(g.issueAdj[PI_IMMIG] + v, -1.5, 1.5);
            else if (k.equals("crime")) g.issueAdj[PI_CRIME] = clamp(g.issueAdj[PI_CRIME] + v, -1.5, 1.5);
            else if (k.equals("oil")) oil(g, v); else if (k.equals("fin")) e.finShock += v; else if (k.equals("def")) e.fiscal += v;
            else if (k.equals("stress")) g.stress = clamp(g.stress + v, 0, 100);
            else if (k.equals("econ")) g.issueAdj[PI_ECON] = clamp(g.issueAdj[PI_ECON] + v, -1.5, 1.5);
            else if (k.equals("prices")) g.issueAdj[PI_PRICES] = clamp(g.issueAdj[PI_PRICES] + v, -1.5, 1.5);
            else if (k.equals("jobs")) g.issueAdj[PI_JOBS] = clamp(g.issueAdj[PI_JOBS] + v, -1.5, 1.5);
            else if (k.equals("health")) g.issueAdj[PI_HEALTH] = clamp(g.issueAdj[PI_HEALTH] + v, -1.5, 1.5);
            else if (k.equals("rights")) { g.issueAdj[PI_RIGHTS] = clamp(g.issueAdj[PI_RIGHTS] + v, -1.5, 1.5); g.justice += v; }
            else if (k.equals("tech")) g.issueAdj[PI_TECH] = clamp(g.issueAdj[PI_TECH] + v, -1.5, 1.5);
            else if (k.equals("energy")) g.issueAdj[PI_ENERGY] = clamp(g.issueAdj[PI_ENERGY] + v, -1.5, 1.5);
            else if (k.equals("edu")) g.issueAdj[PI_EDU] = clamp(g.issueAdj[PI_EDU] + v, -1.5, 1.5);
            else if (k.startsWith("st:")) { int s = stateIdx(k.substring(3)); if (s >= 0) g.stateBump[s] += v; }
            else if (k.length() > 1 && k.charAt(0) == 'g' && Character.isDigit(k.charAt(1))) { groupBump(g, Integer.parseInt(k.substring(1)), v); }
            else if (k.equals("base")) { if (g.home != null) { g.home.fac[0] = clamp(g.home.fac[0] + v, 0, 100); g.home.fac[1] = clamp(g.home.fac[1] + v * 0.6, 0, 100); } for (Bloc b : g.cong.blocs) if (b.party == g.party) b.rel = clamp(b.rel + v * 0.5, 0, 100); }
            else if (k.equals("opp")) { for (Bloc b : g.cong.blocs) if (b.party != g.party) b.rel = clamp(b.rel + v, 0, 100); }
            else if (k.equals("scandal")) g.scandal += v;
            else if (k.equals("farm")) e.farm = clamp(e.farm + v, 30, 160);
            else if (k.equals("ethics")) g.issueAdj[PI_ETHICS] = clamp(g.issueAdj[PI_ETHICS] + v, -1.5, 1.5);
            else if (k.equals("infl")) e.infl += v;
            else if (k.equals("demand")) e.demandShock += v; else if (k.equals("supply")) e.supplyShock -= v;
            else if (g.home != null && k.equals("grid")) g.home.grid = clamp(g.home.grid + v, 0, 100);
            else if (g.home != null && k.equals("protest")) g.home.protest = clamp(g.home.protest + v, 0, 100);
            else if (g.home != null && k.equals("od")) g.home.od = clamp(g.home.od + v, 10, 200);
            else if (g.home != null && k.equals("crimeidx")) g.home.crime = clamp(g.home.crime + v, 50, 200);
            else if (g.home != null && k.equals("pandslow")) g.home.pandSlow = 1;
            else if (g.home != null && k.equals("pandfast")) g.home.pandFast = 1;
            else if (g.home != null && k.equals("pandsev")) g.home.pandSev *= 1.4;
            else if (g.home != null && k.equals("pandend")) g.home.pandWeek = Math.min(g.home.pandWeek, g.week - 30);
            else if (k.equals("tcn")) { if (w.tariff[CC_CN] > 0) setTariff(g, CC_CN, Math.max(0, w.tariff[CC_CN] - 1)); }
            else if (k.equals("kia") && v > 0) {
                int n = (int) v;
                w.kia += n;
                g.perf -= Math.min(6, 0.3 + n * 0.12);
                g.log(NK_BIG, n + (n == 1 ? " AMERICAN SERVICE MEMBER KILLED" : " AMERICAN SERVICE MEMBERS KILLED"), "The president will meet the families at Dover.", 3);
            }
        }
    }

    // ---- operations and covert action ----
    public static boolean opReady(Game g, int i) { return g.world.modern && g.world.opCool[i] == 0 && cond(g, OP_COND[i]); }

    public static double opOdds(Game g, int i) { return p(g, "m", Double.parseDouble(OP_FX[i].split("\\|")[0])); }

    public static String order(Game g, int i) {
        World w = g.world;
        String[] t = OP_FX[i].split("\\|");
        boolean ok = g.rng.nextDouble() < opOdds(g, i);
        w.ops++; w.opCool[i] = 8;
        g.ex.ready[P_DEF] = Math.max(5, g.ex.ready[P_DEF] - 1.5);
        g.econ.fiscal += 0.01;
        fx(g, ok ? t[1] : t[2]);
        String s = ok ? OP_NAME[i] + ": the operation succeeds." : OP_NAME[i] + ": the operation falls short.";
        g.log(ok ? NK_GOOD : NK_BAD, s, null, 2.4);
        if (!Hill.majority(g, HOUSE, g.party) && i <= 3 && g.rng.nextDouble() < 0.3) g.log(NK_BAD, "Lawmakers say the president needed Congress's approval under the War Powers Act", null, 1.2);
        return s;
    }

    public static double cvOdds(Game g, int i) { return p(g, "i", CV_ODDS[i]); }

    public static String covert(Game g, int i) {
        World w = g.world;
        Random r = g.rng;
        w.covert++; w.cvCool[i] = 26;
        boolean ok = r.nextDouble() < cvOdds(g, i);
        if (ok) fx(g, CV_FX[i]);
        boolean exp = r.nextDouble() < CV_EXPOSE[i] * (1.3 - Cabinet.pq(g, P_CIA) * 0.6);
        if (exp) {
            w.exposed++;
            fx(g, CV_EXPFX[i]);
            Trouble.add(g, SK_POWER, "A secret program is exposed", "Reporters reveal a covert operation: " + CV_NAME[i].toLowerCase() + ".", 2.5, null, false);
        }
        return ok ? (exp ? "It worked, but the program has leaked." : "The program is under way, and it's working.") : (exp ? "It failed, and the program has leaked." : "It isn't working yet.");
    }

    public static void setPosture(Game g, int area, int level) {
        World w = g.world;
        int old = w.posture[area];
        w.posture[area] = level;
        g.econ.fiscal += (level - old) * 0.04;
        g.ex.ready[P_DEF] = clamp(g.ex.ready[P_DEF] - Math.max(0, level - old) * 2, 5, 100);
        if (area == 0) { w.rel[CC_CN] -= (level - old) * 5; w.pacific += (level - old) * 3; }
        if (area == 1) { w.rel[CC_RU] -= (level - old) * 4; w.nato += (level - old) * 3; }
        g.log(NK_WORLD, "The Pentagon " + (level > old ? "adds forces in " : "draws down forces in ") + POSTURE_AREA[area].toLowerCase().replace("the ", ""), null, 1.0);
    }
}
