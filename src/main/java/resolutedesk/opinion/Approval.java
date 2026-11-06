package resolutedesk.opinion;

import static resolutedesk.core.Num.*;
import static resolutedesk.data.ElectoralMap.*;
import static resolutedesk.data.StateData.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.model.Attributes.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.opinion.Demographics.*;
import static resolutedesk.opinion.Issues.*;

import java.util.Random;
import resolutedesk.economy.Economy;
import resolutedesk.model.Cand;
import resolutedesk.model.Econ;
import resolutedesk.model.Era;
import resolutedesk.model.Game;

/** Approval: the weekly national number and its breakdown by issue, by group and by state. */
public final class Approval {
    static double squash(Game g, double t) {
        double pol = g.era.polarization;
        double lo = 50 - (22 + 18 * (1 - pol)), hi = 50 + (12 + 30 * (1 - pol));
        double c = (lo + hi) / 2, s = (hi - lo) / 4;
        return lo + (hi - lo) * logistic((t - c) / s);
    }

    static double issueWeighted(Game g) {
        double v = 0, w = 0;
        for (int i = 0; i < NPI; i++) { v += g.issueSal[i] * g.issueAdj[i]; w += g.issueSal[i]; }
        return w > 0 ? v / w : 0;
    }

    /** Where approval is heading, before partisanship squeezes it. */
    static double target(Game g) {
        Econ e = g.econ;
        double t = g.era.approvalBase + 0.8 * (e.gdp - 2) - 1.6 * (e.unemp - e.uRef) - 1.5 * (Economy.piEff(e.infl) - e.piRef) - 3.0 * (e.gas / e.priceLevel - e.gasRef)
                + 0.06 * (e.sentiment - 80) + g.perf + g.honeymoon + g.rally - g.scandal + 1.5 * issueWeighted(g) + personal(g);
        if (g.set.difficulty == 0) t += 2;
        if (g.set.difficulty == 2) t -= 2;
        return t;
    }

    /** What the president is like: crossover appeal and charm help; scandal baggage hurts. */
    static double personal(Game g) {
        Cand p = g.pres;
        if (p == null) return 0;
        return (p.a[A_XOV] - 60) * 0.08 + (p.a[A_CHA] - 60) * 0.03 - (p.a[A_SCA] - 40) * 0.03;
    }

    public static void init(Game g) {
        Era er = g.era;
        for (int i = 0; i < NPI; i++) g.issueSal[i] = 0.5;
        if (er.issues != null) for (String p : er.issues.split(" ")) { String[] kv = p.split("="); g.issueSal[Integer.parseInt(kv[0])] = Double.parseDouble(kv[1]); }
        if (!er.ai) g.issueSal[PI_TECH] = 0.05;
        g.honeymoon = 5 + (g.set.scenario == 4 ? 3 : 0);
        if (g.set.scenario == 2) g.rally = 6;
        double t = target(g);
        g.approval = squash(g, t);
        if (!Double.isNaN(g.set.cApproval)) { g.approval = g.set.cApproval; g.perf = g.approval - squash(g, t); }
        g.approval0 = g.approval;
        for (int i = 0; i < NPI; i++) g.command[i] = 30 + (g.pres != null ? g.pres.a[A_INT] * 0.2 : 10);
        issues(g);
        cells(g);
    }

    public static void week(Game g) {
        Random r = g.rng;
        double t = target(g);
        g.approval += (squash(g, t) - g.approval) * 0.07 + r.nextGaussian() * 0.35;
        g.approval = clamp(g.approval, 8, 92);
        g.honeymoon *= 0.975;
        g.rally *= 0.95;
        g.perf *= 0.96;
        g.scandal *= 0.985;
        for (int i = 0; i < NPI; i++) { g.issueAdj[i] *= 0.97; g.issueSal[i] += (base(g, i) - g.issueSal[i]) * 0.02; }
        for (int s = 0; s < NS; s++) g.stateBump[s] *= 0.9;
        for (int gr = 0; gr < NG; gr++) g.groupAdj[gr] *= 0.99;
        issues(g);
        cells(g);
    }

    static double base(Game g, int i) {
        Era er = g.era;
        if (er.issues != null) for (String p : er.issues.split(" ")) { String[] kv = p.split("="); if (Integer.parseInt(kv[0]) == i) return Double.parseDouble(kv[1]); }
        return 0.5;
    }

    static void issues(Game g) {
        Econ e = g.econ;
        double a = g.approval;
        for (int i = 0; i < NPI; i++) {
            double v = a + 4 * g.issueAdj[i] + (g.command[i] - 50) * 0.04;
            if (i == PI_ECON) v += 1.6 * (e.gdp - 2) - 1.5 * (e.unemp - e.uRef) + 0.08 * (e.sentiment - 80);
            if (i == PI_PRICES) v += -2.5 * (Economy.piEff(e.infl) - e.piRef) - 5 * (e.gas / e.priceLevel - e.gasRef) - 0.6 * (e.groceries - 2.5) - 0.4 * (e.rent - 3.5) - 4;
            if (i == PI_JOBS) v += -2.2 * (e.unemp - e.uRef) + 0.01 * (e.jobsLast - 150);
            g.issueApp[i] = clamp(v, 10, 88);
        }
    }

    /** Approval in each voter cell, shifted so the nation matches the headline number. */
    static void cells(Game g) {
        if (!Groups.ready) return;
        double sign = g.party == REP ? -1 : 1;
        double k = (g.party == DEM || g.party == REP ? 0.8 + 0.6 * g.era.polarization : 0.35);
        Econ e = g.econ;
        double[] local = new double[NS];
        for (int s = 0; s < NS; s++) local[s] = -0.07 * (e.stUnemp[s] - e.unemp) - 0.10 * (e.stGas[s] - e.gas) / e.priceLevel + 0.06 * g.stateBump[s];
        int yr = yearNow(g);
        if (yr < 2024 && (g.party == DEM || g.party == REP)) for (int s = 0; s < NS; s++) local[s] += sign * 0.016 * leanShift(s, yr);
        double lo = -6, hi = 6, off = g.offset;
        for (int it = 0; it < 30; it++) {
            double v = national(g, sign, k, off, local);
            if (Math.abs(v - g.approval / 100) < 0.0005) break;
            if (v < g.approval / 100) lo = off; else hi = off;
            off = (lo + hi) / 2;
        }
        g.offset = off;
        double[] gs = new double[NG], gw = new double[NG];
        for (int s = 0; s < NS; s++) {
            double num = 0, den = 0;
            for (int c = 0; c < NCELL; c++) {
                double w = Elect.cvap24[s][c];
                if (w <= 0) continue;
                double l = k * sign * Elect.baseL[s][c] + off + local[s];
                for (int gr = 0; gr < NG; gr++) if (g.groupAdj[gr] != 0) l += g.groupAdj[gr] * Groups.mem[gr][s][c];
                double p = logistic(l);
                num += w * p; den += w;
                for (int gr = 0; gr < NG; gr++) { double m = Groups.mem[gr][s][c]; if (m > 0) { gs[gr] += w * m * p; gw[gr] += w * m; } }
            }
            g.stateApp[s] = den > 0 ? num / den * 100 : g.approval;
        }
        for (int gr = 0; gr < NG; gr++) g.groupApp[gr] = gw[gr] > 0 ? gs[gr] / gw[gr] * 100 : g.approval;
    }

    static double national(Game g, double sign, double k, double off, double[] local) {
        double num = 0, den = 0;
        for (int s = 0; s < NS; s++) for (int c = 0; c < NCELL; c++) {
            double w = Elect.cvap24[s][c];
            if (w <= 0) continue;
            double l = k * sign * Elect.baseL[s][c] + off + local[s];
            for (int gr = 0; gr < NG; gr++) if (g.groupAdj[gr] != 0) l += g.groupAdj[gr] * Groups.mem[gr][s][c];
            num += w * logistic(l); den += w;
        }
        return num / den;
    }
}
