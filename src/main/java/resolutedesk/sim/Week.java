package resolutedesk.sim;

import static resolutedesk.core.Num.*;
import static resolutedesk.data.StateData.*;
import static resolutedesk.executive.Posts.*;
import static resolutedesk.model.Attributes.*;
import static resolutedesk.model.NewsKinds.*;
import static resolutedesk.opinion.Issues.*;
import static resolutedesk.sim.Schedule.*;
import static resolutedesk.world.WorldData.*;

import java.util.Arrays;
import java.util.Random;
import resolutedesk.campaign.Reelection;
import resolutedesk.campaign.Terms;
import resolutedesk.congress.Budget;
import resolutedesk.congress.Hill;
import resolutedesk.congress.Legis;
import resolutedesk.congress.Midterms;
import resolutedesk.congress.Relations;
import resolutedesk.economy.Economy;
import resolutedesk.executive.Actions;
import resolutedesk.executive.Cabinet;
import resolutedesk.executive.Courts;
import resolutedesk.executive.Delegate;
import resolutedesk.executive.Trouble;
import resolutedesk.history.History;
import resolutedesk.home.HomeFront;
import resolutedesk.model.Cand;
import resolutedesk.model.Econ;
import resolutedesk.model.Game;
import resolutedesk.opinion.Approval;
import resolutedesk.policy.Policies;
import resolutedesk.world.Diplo;

/** The president's calendar plays out slot by slot; then the economy, opinion and the news move. */
public final class Week {
    public static int dc() { return stateIdx("DC"); }

    /** Flight time from Washington, in hours. */
    public static double flight(int s) {
        if (s < 0) return 0;
        String ab = STATES[s].ab;
        if (ab.equals("HI")) return 10.5;
        if (ab.equals("AK")) return 8.5;
        int d = dc();
        double dx = STATES[s].cx - STATES[d].cx, dy = STATES[s].cy - STATES[d].cy;
        double miles = Math.sqrt(dx * dx + dy * dy) * 2.9;
        return miles < 60 ? 0.3 : 0.6 + miles / 480;
    }

    static double ageFactor(Game g) { return 1 + Math.max(0, g.presAge() - 60) * 0.012; }

    /** A routine week: policy, remarks, a trip, rest on the weekend. */
    public static void routine(Game g) {
        Arrays.fill(g.act, AC_FREE);
        Arrays.fill(g.arg, -1);
        int top = topIssue(g);
        int[] pri = priorities(g);
        for (int d = 0; d < 5; d++) {
            g.act[d * 3] = AC_POLICY; g.arg[d * 3] = pri[d % pri.length];
            g.act[d * 3 + 1] = d == 1 ? AC_REMARKS : d == 2 ? AC_CONGRESS : d == 3 ? AC_TRIP : AC_CEREMONY;
            if (d == 1) g.arg[d * 3 + 1] = top;
            if (d == 3) { int ps = Relations.promisedState(g); g.arg[d * 3 + 1] = ps >= 0 ? ps : swingState(g); }
            g.act[d * 3 + 2] = d == 4 ? AC_FAMILY : AC_FREE;
        }
        g.act[15] = AC_EXERCISE; g.act[16] = AC_FAMILY; g.act[17] = AC_REST;
        g.act[18] = AC_REST; g.act[19] = AC_FAMILY; g.act[20] = AC_FREE;
    }

    /** Issues in order of how much attention they need: salience times the room to improve. */
    static int[] priorities(Game g) {
        Integer[] idx = new Integer[NPI];
        for (int i = 0; i < NPI; i++) idx[i] = i;
        final Game gg = g;
        Arrays.sort(idx, (a, b) -> Double.compare(gg.issueSal[b] * (62 - gg.issueApp[b]), gg.issueSal[a] * (62 - gg.issueApp[a])));
        int[] out = new int[4];
        for (int i = 0; i < 4; i++) out[i] = idx[i];
        return out;
    }

    public static int topIssue(Game g) {
        int best = 0;
        for (int i = 1; i < NPI; i++) if (g.issueSal[i] * (60 - g.issueApp[i]) > g.issueSal[best] * (60 - g.issueApp[best])) best = i;
        return best;
    }

    static int swingState(Game g) {
        int best = stateIdx("PA");
        double bv = -1;
        for (int s = 0; s < NS; s++) {
            double v = STATES[s].ev * (1 - Math.abs(g.stateApp[s] - 47) / 25) - g.stateBump[s] * 3;
            if (v > bv) { bv = v; best = s; }
        }
        return best;
    }

    /** Hours of sleep the evening slots allow. */
    public static double sleepOf(Game g) {
        double h = 0;
        for (int d = 0; d < 7; d++) {
            int a = g.act[d * 3 + 2];
            double v = a == AC_REST || a == AC_FAMILY || a == AC_CAMPDAVID ? 7.4 : a == AC_FREE || a == AC_EXERCISE ? 6.8 : a == AC_WORKLATE ? 4.8 : a == AC_TRIP || a == AC_FUNDRAISER ? 5.6 : 6.2;
            int m = g.act[d * 3];
            if (isTrip(m) && g.arg[d * 3] >= 0 && flight(g.arg[d * 3]) > 3.5) v -= 1;
            if (a == AC_ABROAD || m == AC_ABROAD) v -= 1.2;
            h += v;
        }
        return h / 7;
    }

    /** Stamina the week will cost, before sleep. */
    static double cost(Game g) {
        double c = 0;
        for (int i = 0; i < NSLOT; i++) {
            double v = AC_STAMINA[g.act[i]];
            if (isTrip(g.act[i]) && g.arg[i] >= 0) v -= Math.min(6, flight(g.arg[i]) * 0.9);
            if (g.act[i] == AC_ABROAD && g.arg[i] >= 0 && (i == 0 || g.act[i - 1] != AC_ABROAD)) v -= Math.min(12, flightAbroad(g.arg[i]) * 0.8);
            c += v < 0 ? v * ageFactor(g) : v;
        }
        return c;
    }

    public static double projectedStamina(Game g) {
        return clamp(g.stamina + cost(g) + 4 + (sleepOf(g) - 6.5) * 3 - g.stress / 25 + (g.fitness - 50) / 25, 0, 100);
    }

    public static void resolve(Game g) {
        Random r = g.rng;
        Cand p = g.pres;
        int trips = 0, funds = 0, campDavid = 0;
        double speech = p != null ? p.a[A_SPE] : 60, cha = p != null ? p.a[A_CHA] : 60;
        for (int i = 0; i < NSLOT; i++) {
            int a = g.act[i], arg = g.arg[i];
            g.stress = clamp(g.stress + AC_STRESS[a], 0, 100);
            switch (a) {
                case AC_POLICY:
                    if (arg >= 0) { double dq = 0.85 + 0.3 * Cabinet.sq(g, S_DPC); g.command[arg] = Math.min(100, g.command[arg] + 5 * dq * (1 - g.command[arg] / 110)); g.issueAdj[arg] = Math.min(1.5, g.issueAdj[arg] + 0.02 * dq * (1 - g.issueAdj[arg] / 1.5)); }
                    break;
                case AC_REMARKS:
                    if (arg >= 0) {
                        g.issueSal[arg] = Math.min(2, g.issueSal[arg] + 0.08);
                        g.issueAdj[arg] = clamp(g.issueAdj[arg] + (0.04 * speech / 70) * (0.85 + 0.3 * Cabinet.sq(g, S_COMMS)) * (1 - Math.abs(g.issueAdj[arg]) / 1.5) + r.nextGaussian() * 0.02, -1.5, 1.5);
                        g.perf += 0.012;
                    }
                    break;
                case AC_TRIP:
                    if (arg >= 0) {
                        trips++;
                        g.stateBump[arg] += 2.2 * cha / 70;
                        g.perf += 0.015;
                        g.location = arg;
                        if (r.nextDouble() < 0.5) g.log(NK_INFO, g.pres.last + " visits " + STATES[arg].name, null, 0.4);
                    }
                    break;
                case AC_FUNDRAISER:
                    funds++;
                    double raised = (1.5 + r.nextDouble() * 2) * (p != null ? p.a[A_BIG] : 60) / 70 * (0.85 + 0.3 * Cabinet.sq(g, S_POL));
                    g.funds += raised;
                    break;
                case AC_CEREMONY: g.perf += 0.012; break;
                case AC_CONGRESS: g.hill = Math.min(100, g.hill + 1.2 * (p != null ? p.a[A_EST] : 60) / 70); Hill.meeting(g); break;
                case AC_FAMILY: g.perf += 0.004; break;
                case AC_EXERCISE: g.fitness = Math.min(100, g.fitness + 1.2); break;
                case AC_CAMPDAVID: campDavid++; break;
                case AC_CABINET: Cabinet.meeting(g); break;
                case AC_ABROAD: if (arg >= 0 && g.world != null && (i == 0 || g.act[i - 1] != AC_ABROAD || g.arg[i - 1] != arg)) Diplo.visit(g, arg, true); break;
                case AC_CAMPAIGN: if (arg >= 0 && g.cong != null) { g.cong.campaign[arg] += cha / 70; if (g.re != null && Reelection.season(g)) g.re.effort[arg] += cha / 70; g.stateBump[arg] += 0.8; g.location = arg; trips++; } break;
                case AC_WORKLATE: { int t = topIssue(g); g.command[t] = Math.min(100, g.command[t] + 1.5); break; }
                default: break;
            }
        }
        if (funds > 3) { g.perf -= 0.06 * (funds - 3); if (funds >= 5) g.log(NK_BAD, "Critics count " + funds + " fundraisers in a week for " + g.pres.last, null, 0.7); }
        if (trips == 0) g.location = Week.dc();
        g.fitness = Math.max(0, g.fitness - 0.4);
        // sleep and stamina
        double sl = sleepOf(g);
        g.sleep = sl;
        g.stamina = clamp(g.stamina + cost(g) + 4 + (sl - 6.5) * 3 - g.stress / 25 + (g.fitness - 50) / 25, 0, 100);
        if (sl < 5.5) g.stress = clamp(g.stress + 3, 0, 100);
        g.stress += 9.5 + (50 - g.approval) * 0.08 + (g.stamina < 30 ? 3 : 0);       // the job itself
        g.stress = clamp(g.stress * 0.85, 0, 100);
        health(g);
        Economy.week(g);
        Policies.week(g);
        Approval.week(g);
        if (g.cong != null) {
            Hill.drift(g);
            Legis.week(g);
            Budget.week(g);
            Midterms.week(g);
            Reelection.week(g);
            Relations.week(g);
        }
        if (g.ex != null) {
            Cabinet.week(g);
            Actions.week(g);
            Courts.week(g);
            Trouble.week(g);
            if (g.world != null) Diplo.week(g);
            if (g.world != null) History.week(g);
            if (g.home != null) HomeFront.week(g);
            g.issueAdj[PI_ECON] = clamp(g.issueAdj[PI_ECON] + 0.003 * (Cabinet.sq(g, S_NEC) - 0.6), -1.5, 1.5);
        }
        record(g);
        g.week++;
        Terms.check(g);
        Delegate.plan(g);
        g.dirty = true;
    }

    static void health(Game g) {
        Random r = g.rng;
        int age = g.presAge();
        g.health = clamp(g.health + (g.stamina - 50) * 0.01 - (g.stress - 40) * 0.01 + (g.fitness - 50) * 0.005 - Math.max(0, age - 70) * 0.01, 20, 100);
        if (g.sick > 0) { g.sick--; g.stamina = Math.min(g.stamina, 55); return; }
        double risk = 0.004 + 0.0007 * Math.max(0, age - 60) + 0.0006 * Math.max(0, g.stress - 60) + (g.stamina < 20 ? 0.02 : 0) + (100 - g.health) * 0.0004;
        if (r.nextDouble() < risk) {
            if (r.nextDouble() < 0.8) {
                g.sick = 1 + r.nextInt(2);
                g.stamina = Math.max(10, g.stamina - 15);
                g.log(NK_INFO, "White House: the president has a cold and is working from the residence", null, 0.6);
            } else {
                g.sick = 2;
                g.stamina = Math.max(10, g.stamina - 30);
                g.perf -= 1.0;
                g.log(NK_BAD, "President " + g.pres.last + " spends a night at Walter Reed for tests", "The White House physician says the president is doing well and will be back at work within days.", 2.2);
            }
        }
    }

    static void record(Game g) {
        Econ e = g.econ;
        if (g.approval < g.minApproval) { g.minApproval = g.approval; g.minApprovalWeek = g.week; }
        g.maxApproval = Math.max(g.maxApproval, g.approval);
        g.hist.add(new double[]{g.week, g.approval, e.unemp, e.infl, e.gdp, e.stocks, e.gas, e.deficit, e.debt, e.fed, e.sentiment, g.stamina, g.stress, e.tenYr, g.issueApp[PI_ECON], g.issueApp[PI_PRICES]});
    }
}
