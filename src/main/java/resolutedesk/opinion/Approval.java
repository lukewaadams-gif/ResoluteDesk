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
import resolutedesk.policy.Policies;

/**
 * Approval: the weekly national number and its breakdown by issue, by group and by state.
 *
 * <p>The national number follows a target built in two parts. The fundamentals are the era's baseline, the
 * economy (growth, jobs, prices and gas) and your standing on the issues people care about. On top of those
 * come the things that happen: the honeymoon, which an election margin makes bigger; your record of wins and
 * failures; scandals; the wear of office; and a slow drift in the public mood. Partisanship then squeezes the
 * result: since about 1990 your own party keeps you above a floor near 30 and the other party keeps you below a
 * ceiling near 70, both a little tighter the more polarized the era. Before then there is no floor, as Truman
 * and Nixon found. A rally around the flag passes through the ceiling, which is how approval reaches 90.
 */
public final class Approval {
    /** How much each kind of event moves the target, and how fast the effects fade each week. */
    public static final double PERF_WEIGHT = 1.6, SCANDAL_WEIGHT = 1.2, RALLY_WEIGHT = 1.6;
    static final double HONEYMOON_FADE = 0.975, RALLY_FADE = 0.965, PERF_FADE = 0.96, SCANDAL_FADE = 0.975;
    /** The public mood: a slow random drift, about two points either way. */
    static final double MOOD_FADE = 0.97, MOOD_STEP = 0.45;
    /** How much of the gap to the target closes each week, and the week-to-week noise of the polls. */
    static final double PULL = 0.15, NOISE = 0.55;

    /** How sorted the era's politics are, from 0 (before 1990, or a mild era) to 1 (today). */
    static double sorted(Game g) { return yearNow(g) < 1990 ? 0 : clamp((g.era.polarization - 0.5) / 0.5, 0, 1); }

    /** The partisan floor and ceiling {lo, hi}: none to speak of before 1990, about 30 and 70 after, tighter as polarization rises. */
    static double[] bounds(Game g) {
        if (yearNow(g) < 1990) return new double[]{12, 82};
        double s = sorted(g);
        return new double[]{30 + 6 * s, 72 - 8 * s};
    }

    /** How far below the floor approval can sink, at most: a catastrophe today bottoms out in the mid-20s. */
    static final double FLOOR_DEPTH = 16;

    /**
     * Squeezes a raw target into the range partisanship allows. Past the ceiling each point counts for 0.35. Past the
     * floor the first points count for 0.3, and fewer after that, so approval levels off about 16 points below it.
     */
    static double squash(Game g, double raw, double rally) {
        double[] b = bounds(g);
        double x = raw;
        if (raw < b[0]) x = b[0] - FLOOR_DEPTH * (1 - Math.exp(-0.3 * (b[0] - raw) / FLOOR_DEPTH));
        else if (raw > b[1]) x = b[1] + (raw - b[1]) * 0.35;
        x += rally * (1 - Math.max(0, x - 50) / 100);                       // a rally passes through the ceiling
        if (x > 86) x = 86 + (x - 86) * 0.45;
        if (x < 14) x = 14 - (14 - x) * 0.45;
        return clamp(x, 4, 96);
    }

    static double issueWeighted(Game g) {
        double v = 0, w = 0;
        for (int i = 0; i < NPI; i++) { v += g.issueSal[i] * (g.issueAdj[i] + Policies.result(g, i)); w += g.issueSal[i]; }
        return w > 0 ? v / w : 0;
    }

    /** The economy's part: growth, jobs, prices and the price of gas against what people of the era expected. */
    static double economy(Game g) {
        Econ e = g.econ;
        return 1.2 * (e.gdp - 2) - 2.4 * (e.unemp - e.uRef) - 2.0 * (Economy.piEff(e.infl) - e.piRef) - 3.0 * (e.gas / e.priceLevel - e.gasRef) + 0.08 * (e.sentiment - 80);
    }

    /** The fundamentals: the era, the economy, the issues and the president's own appeal. */
    static double fundamentals(Game g) {
        double t = g.era.approvalBase + economy(g) + 1.5 * issueWeighted(g) + personal(g);
        if (g.set.difficulty == 0) t += 2;
        if (g.set.difficulty == 2) t -= 2;
        return t;
    }

    /** What has happened lately, apart from rallies: the honeymoon, your record, scandal, the wear of office and the public mood. */
    static double events(Game g) {
        return g.honeymoon + PERF_WEIGHT * g.perf - SCANDAL_WEIGHT * g.scandal - wear(g) + g.mood;
    }

    /**
     * The wear of office: nothing for six months, then a slow slide of up to nine points a term, and about five in
     * today's sorted politics, where fewer minds change. Second terms start a little worn.
     */
    static double wear(Game g) {
        double cap = 9 * (1 - 0.45 * sorted(g));
        return Math.min(cap, 0.045 * Math.max(0, g.tw() - 26)) + (g.term > 1 ? cap / 3 : 0);
    }

    /** Where approval is heading. */
    static double target(Game g) { return squash(g, fundamentals(g) + events(g), RALLY_WEIGHT * g.rally); }

    /** What the president is like: crossover appeal and charm help; scandal baggage hurts. */
    static double personal(Game g) {
        Cand p = g.pres;
        if (p == null) return 0;
        return (p.a[A_XOV] - 60) * 0.08 + (p.a[A_CHA] - 60) * 0.03 - (p.a[A_SCA] - 40) * 0.03;
    }

    /** The honeymoon a new president starts with: bigger after a bigger win. */
    public static double honeymoonFor(Game g) {
        double m = g.cong != null ? g.cong.mandate : 3;
        return 4 + 0.6 * m;
    }

    public static void init(Game g) {
        Era er = g.era;
        for (int i = 0; i < NPI; i++) g.issueSal[i] = 0.5;
        if (er.issues != null) for (String p : er.issues.split(" ")) { String[] kv = p.split("="); g.issueSal[Integer.parseInt(kv[0])] = Double.parseDouble(kv[1]); }
        if (!er.ai) g.issueSal[PI_TECH] = 0.05;
        g.honeymoon = honeymoonFor(g);
        if (g.set.scenario == 2) g.rally = 6;
        double t = target(g);
        g.approval = t;
        if (!Double.isNaN(g.set.cApproval)) { g.approval = g.set.cApproval; g.perf = (g.approval - t) / PERF_WEIGHT; }
        g.approval0 = g.approval;
        for (int i = 0; i < NPI; i++) g.command[i] = 30 + (g.pres != null ? g.pres.a[A_INT] * 0.2 : 10);
        issues(g);
        cells(g);
    }

    /** Starts approval over at its target, after the transition has changed the honeymoon (a level set before the game stays). */
    public static void restart(Game g) {
        if (Double.isNaN(g.set.cApproval)) g.approval = target(g);
        g.approval0 = g.approval;
        issues(g);
        cells(g);
    }

    public static void week(Game g) {
        Random r = g.rng;
        g.mood = g.mood * MOOD_FADE + r.nextGaussian() * MOOD_STEP;
        double t = target(g);
        g.approval += (t - g.approval) * PULL + r.nextGaussian() * NOISE;
        g.approval = clamp(g.approval, 4, 96);
        g.honeymoon *= HONEYMOON_FADE;
        g.rally *= RALLY_FADE;
        g.perf *= PERF_FADE;
        g.scandal *= SCANDAL_FADE;
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
            double v = a + 4 * (g.issueAdj[i] + Policies.result(g, i)) + (g.command[i] - 50) * 0.04;
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
