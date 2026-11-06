package resolutedesk.opinion;

import static resolutedesk.core.Num.*;
import static resolutedesk.data.StateData.*;
import static resolutedesk.opinion.Demographics.*;
import static resolutedesk.opinion.VoterIssues.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import resolutedesk.model.StateDef;

/** The electorate: voter cells in every state and how they lean. */
public final class Elect {
    static final double NAT24 = 0.4924, NAT20 = 0.5226, NAT22 = 0.4864;   // national two-party Democratic share
    static double[][] cvap = new double[NS][NCELL];       // eligible voters, 2028 composition
    static double[][] cvap24 = new double[NS][NCELL];     // eligible voters, 2024 composition
    static double[][] baseL = new double[NS][NCELL];      // calibrated Democratic logit (2024 environment)
    static double[][] turnB = new double[NS][NCELL];      // base turnout
    static double[][][] ov = new double[NS][NCELL][NO];   // overlay fractions
    static double[][][] ideal = new double[NS][NCELL][NI];
    static double[][][] sal = new double[NS][NCELL][NI];
    static double[][] elast = new double[NS][NCELL];
    static double[] offset = new double[NS], turnScale = new double[NS], target = new double[NS];
    static double[] blocK = new double[NB];
    static double[][] ageSexK = new double[NAGE][NSEX];
    /** 2024 two-party Democratic share by age and sex (AP VoteCast / Pew validated voters). */
    static final double[][] AGESEX_D24 = {{0.42, 0.59}, {0.425, 0.525}, {0.44, 0.52}};
    static double[] stateVotes24 = new double[NS];
    public static double[] lean = new double[NS];                // blended D-minus-R lean relative to the nation (points)
    static double[][] dmaShare;                           // [market][state] share of the state's TV households
    static String[] marketName;
    static double[] marketHH;
    static int[][] marketStates;
    static int NM;
    static boolean ready;

    /**
     * Group swings since 2024 that outlast the 2026 midterms (logit), beyond the national mood: Hispanic and young voters
     * swung furthest back toward the Democrats in 2026 (about +28 and +32 points against +15 nationally in early
     * estimates); only part of a midterm swing carries into a presidential year.
     */
    static double swing2026(int c) {
        int b = cellBloc(c), a = cellAge(c);
        double v = 0;
        if (hispanic(b)) v += 0.15;
        if (b == B_BLK) v += 0.04;
        if (b == B_ASN) v += 0.06;
        if (a == AGE_YOUNG) v += 0.11;
        if (b == B_WOT_C || b == B_WCA_C) v += 0.03;
        return v;
    }

    public static synchronized void init() {
        if (ready) return;
        for (int s = 0; s < NS; s++) compose(s);
        calibrate();
        markets();
        ready = true;
    }

    static double[] blocShares(StateDef d, int years) {
        double W = d.white, B = d.black, H = d.hisp;
        double A = d.asian, N = d.nativeAm, O = Math.max(0.6, d.other - A - N);
        if (d.ab.equals("HI")) { A = Math.min(A, d.other - 1); O = Math.max(2, d.other - A - N); }
        W *= Math.pow(1 - 0.0075, years); H *= Math.pow(1.02, years); A *= Math.pow(1.022, years); O *= Math.pow(1.03, years); B *= Math.pow(1.002, years);
        // share of each group that is an adult citizen
        double wc = W * 0.80, bc = B * 0.70, hc = H * 0.43, ac = A * 0.55, nc = N * 0.66, oc = O * 0.50;
        double tot = wc + bc + hc + ac + nc + oc;
        wc /= tot; bc /= tot; hc /= tot; ac /= tot; nc /= tot; oc /= tot;
        double adj = 1.05;
        double wev = Math.min(0.55 * wc, d.wev / 100 * adj), wca = Math.min(0.5 * wc, d.wca / 100 * adj);
        double jew = Math.min(0.2 * wc, d.jew / 100 * 1.1), lds = Math.min(0.75 * wc, d.lds / 100 * 0.9 * adj), arb = Math.min(0.1 * wc, d.arab / 100);
        double wot = wc - wev - wca - jew - lds - arb;
        if (wot < 0.05 * wc) {
            double f = (wc * 0.95) / (wev + wca + jew + lds + arb);
            wev *= f; wca *= f; jew *= f; lds *= f; arb *= f; wot = 0.05 * wc;
        }
        double cw = clamp(1 - d.nocolW / 100 + 0.012 * years, 0.12, 0.85);
        double fot = Math.min(0.85, cw + cw * 0.4 * wev / Math.max(0.01, wot));
        double[] sh = new double[NB];
        sh[B_WEV_NC] = wev * (1 - 0.6 * cw); sh[B_WEV_C] = wev * 0.6 * cw;
        sh[B_WCA_NC] = wca * (1 - cw); sh[B_WCA_C] = wca * cw;
        sh[B_WOT_NC] = wot * (1 - fot); sh[B_WOT_C] = wot * fot;
        sh[B_JEW] = jew; sh[B_LDS] = lds; sh[B_ARB] = arb;
        sh[B_BLK] = bc;
        sh[B_HCU] = hc * d.cuban; sh[B_HPR] = hc * d.pr; sh[B_HMX] = hc * (1 - d.cuban - d.pr);
        sh[B_ASN] = ac; sh[B_NAT] = nc; sh[B_OTH] = oc;
        double t = 0;
        for (double v : sh) t += v;
        for (int b = 0; b < NB; b++) sh[b] /= t;
        return sh;
    }

    static void compose(int s) {
        StateDef d = STATES[s];
        double grow = d.region.equals("S") || d.region.equals("W") ? 1.25 : 0.7;
        double cv24 = d.cvap * (1 + 0.007 * 8 * grow), cv28 = d.cvap * (1 + 0.007 * 12 * grow);
        double[] sh24 = blocShares(d, 10), sh28 = blocShares(d, 14);
        double fy = clamp(d.u29 / 39.9, 0.7, 1.4), fo = clamp(d.o65 / 14.9, 0.6, 1.5);
        for (int b = 0; b < NB; b++) {
            double[] ag = {BLOC_AGE[b][0] * fy, BLOC_AGE[b][1], BLOC_AGE[b][2] * fo};
            double at = ag[0] + ag[1] + ag[2];
            for (int a = 0; a < NAGE; a++) {
                double men = b == B_BLK ? 0.45 : b == B_ARB ? 0.52 : 0.48;
                for (int x = 0; x < NSEX; x++) {
                    int c = cell(b, a, x);
                    double f = ag[a] / at * (x == 0 ? men : 1 - men);
                    cvap24[s][c] = cv24 * sh24[b] * f;
                    cvap[s][c] = cv28 * sh28[b] * f;
                    overlays(d, s, c, b, a, x);
                    opinions(s, c, b, a, x);
                    elast[s][c] = BLOC_ELAST[b] * AGE_ELAST[a];
                }
            }
        }
    }

    static void overlays(StateDef d, int s, int c, int b, int a, int x) {
        double[] o = ov[s][c];
        double rb, ub;
        switch (b) {
            case B_WEV_NC: case B_WEV_C: rb = 1.5; ub = 0.4; break;
            case B_WCA_NC: case B_WCA_C: rb = 1.0; ub = 0.7; break;
            case B_WOT_NC: rb = 1.3; ub = 0.6; break;
            case B_WOT_C: rb = 0.75; ub = 1.25; break;
            case B_BLK: rb = 0.55; ub = 1.8; break;
            case B_HMX: rb = 0.45; ub = 1.5; break;
            case B_HCU: rb = 0.2; ub = 1.3; break;
            case B_HPR: rb = 0.3; ub = 1.8; break;
            case B_ASN: rb = 0.2; ub = 1.6; break;
            case B_NAT: rb = 2.0; ub = 0.5; break;
            case B_JEW: rb = 0.15; ub = 1.6; break;
            case B_LDS: rb = 1.0; ub = 0.6; break;
            case B_ARB: rb = 0.2; ub = 1.6; break;
            default: rb = 0.6; ub = 1.3; break;
        }
        double rural = clamp(d.rural / 100 * rb * (a == 0 ? 0.85 : a == 2 ? 1.15 : 1), 0, 0.92);
        double urban = clamp(d.urban / 100 * ub * (a == 0 ? 1.3 : a == 2 ? 0.8 : 1), 0, 0.97 - rural);
        o[O_RURAL] = rural; o[O_URBAN] = urban; o[O_SUBURB] = Math.max(0, 1 - rural - urban);
        boolean col = college(b);
        o[O_UNION] = clamp(d.union / 100 * (col ? 0.8 : 1.15) * (b == B_BLK ? 1.3 : hispanic(b) ? 0.9 : 1) * (a == 0 ? 0.6 : a == 1 ? 1.1 : 1.0), 0, 0.7);
        o[O_VET] = clamp(d.vet / 100 * 1.4 * (x == 0 ? 1.8 : 0.35) * (a == 0 ? 0.4 : a == 1 ? 1.0 : 1.8), 0, 0.6);
        double gb = b <= B_WEV_C ? 1.3 : b == B_WOT_NC ? 1.15 : b == B_WOT_C ? 0.75 : b == B_WCA_NC || b == B_WCA_C ? 1.0 : b == B_LDS ? 1.1 : b == B_NAT ? 1.0
                : b == B_JEW || b == B_ARB ? 0.3 : b == B_ASN ? 0.4 : 0.6;
        o[O_GUN] = clamp(d.gun / 100 * gb * (x == 0 ? 1.25 : 0.8) * (0.8 + rural * 0.6), 0, 0.92);
        o[O_LGBT] = clamp(d.lgbt / 100 * (a == 0 ? 2.6 : a == 1 ? 0.75 : 0.3) * (b <= B_WEV_C ? 0.3 : b == B_LDS ? 0.4 : 1), 0, 0.4);
        o[O_STUDENT] = a == 0 ? (col ? 0.55 : 0.28) : 0;
        o[O_PARENT] = a == 1 ? 0.48 : a == 0 ? 0.15 : 0.02;
    }

    static void opinions(int s, int c, int b, int a, int x) {
        double p = BLOC_D24[b];
        double[] id = ideal[s][c], sl = sal[s][c];
        double tot = 0;
        for (int i = 0; i < NI; i++) {
            double v = IDEAL_R[i] + (IDEAL_D[i] - IDEAL_R[i]) * p + blocIdealShift(b, i) + AGE_IDEAL[a][i] + SEX_IDEAL[x][i];
            double m = ISSUE_SAL[i] * blocSalMul(b, i) * AGE_SAL[a][i] * SEX_SAL[x][i];
            for (int o = 0; o < NO; o++) {
                double f = ov[s][c][o];
                v += f * OV_IDEAL[o][i];
                m *= 1 + f * (OV_SAL[o][i] - 1);
            }
            id[i] = clamp(v, -1, 1);
            sl[i] = m;
            tot += m;
        }
        for (int i = 0; i < NI; i++) sl[i] /= tot;
    }

    static double cellLogitRaw(int s, int c) {
        int b = cellBloc(c), a = cellAge(c), x = cellSex(c);
        double mean = (ageSexK[a][0] + ageSexK[a][1]) / 2;
        double adj = mean + (ageSexK[a][x] - mean) * BLOC_GENDER[b];
        double l = blocK[b] + adj;
        for (int o = 0; o < NO; o++) l += ov[s][c][o] * OV_LEAN[o];
        return l;
    }

    static void calibrate() {
        double[] nat = {0, 0, 0};
        // 2026: each state's statewide margin relative to the states that voted, centred so the 2026 term moves
        // states relative to one another without shifting the group as a whole
        double w26 = 0, sum26 = 0, sum24 = 0;
        for (int s = 0; s < NS; s++) {
            if (Double.isNaN(m26(s))) continue;
            StateDef d = STATES[s];
            w26 += d.t24; sum26 += d.t24 * m26(s); sum24 += d.t24 * (d.d24 / (d.d24 + d.r24) - NAT24) * 200;
        }
        double nat26 = w26 > 0 ? (sum26 - sum24) / w26 : 0;
        for (int s = 0; s < NS; s++) {
            StateDef d = STATES[s];
            double sh24 = d.d24 / (d.d24 + d.r24), sh20 = d.d20 / (d.d20 + d.r20), sh22 = clamp(0.5 + d.m22 / 200, 0.05, 0.95);
            double rel = 0.65 * (sh24 - NAT24) + 0.25 * (sh20 - NAT20) + 0.10 * (sh22 - NAT22);
            if (!Double.isNaN(m26(s))) rel = 0.60 * (sh24 - NAT24) + 0.22 * (sh20 - NAT20) + 0.08 * (sh22 - NAT22) + 0.10 * (m26(s) - nat26) / 200;
            target[s] = clamp(NAT24 + rel, 0.03, 0.97);
            lean[s] = rel * 200;
            stateVotes24[s] = d.t24;
        }
        for (int b = 0; b < NB; b++) blocK[b] = logit(BLOC_D24[b]);
        for (int a = 0; a < NAGE; a++) for (int x = 0; x < NSEX; x++) ageSexK[a][x] = AGESEX_ADJ[a][x];
        // targets: the national result must equal 2024 exactly (the blend shifts relative positions only)
        double tw0 = 0, tv0 = 0;
        for (int s = 0; s < NS; s++) { tw0 += stateVotes24[s] * target[s]; tv0 += stateVotes24[s]; }
        double shift = NAT24 - tw0 / tv0;
        for (int s = 0; s < NS; s++) { target[s] = clamp(target[s] + shift, 0.03, 0.97); lean[s] = (target[s] - NAT24) * 200; }
        // turnout scale per state to reproduce 2024 total votes
        for (int s = 0; s < NS; s++) {
            double lo = 0.3, hi = 2.0;
            for (int it = 0; it < 40; it++) {
                double mid = (lo + hi) / 2, v = 0;
                for (int c = 0; c < NCELL; c++) v += cvap24[s][c] * Math.min(0.97, baseTurn(c) * mid);
                if (v > stateVotes24[s]) hi = mid; else lo = mid;
            }
            turnScale[s] = (lo + hi) / 2;
            for (int c = 0; c < NCELL; c++) turnB[s][c] = Math.min(0.97, baseTurn(c) * turnScale[s]);
        }
        // raking: state offsets reproduce the blended state results, bloc intercepts reproduce national bloc votes
        for (int pass = 0; pass < 30; pass++) {
            for (int s = 0; s < NS; s++) {
                double lo = -4, hi = 4;
                for (int it = 0; it < 40; it++) {
                    double mid = (lo + hi) / 2, dv = 0, tv = 0;
                    for (int c = 0; c < NCELL; c++) {
                        double w = cvap24[s][c] * turnB[s][c];
                        dv += w * logistic(cellLogitRaw(s, c) + mid * BLOC_STATE_W[cellBloc(c)]);
                        tv += w;
                    }
                    if (dv / tv > target[s]) hi = mid; else lo = mid;
                }
                offset[s] = (lo + hi) / 2;
            }
            double[] dv = new double[NB], tv = new double[NB];
            for (int s = 0; s < NS; s++)
                for (int c = 0; c < NCELL; c++) {
                    int b = cellBloc(c);
                    double w = cvap24[s][c] * turnB[s][c];
                    dv[b] += w * logistic(cellLogitRaw(s, c) + offset[s] * BLOC_STATE_W[b]);
                    tv[b] += w;
                }
            for (int b = 0; b < NB; b++) blocK[b] += 0.7 * (logit(BLOC_D24[b]) - logit(dv[b] / tv[b]));
            // age and sex
            double[][] ad = new double[NAGE][NSEX], at = new double[NAGE][NSEX];
            for (int s = 0; s < NS; s++)
                for (int c = 0; c < NCELL; c++) {
                    double w = cvap24[s][c] * turnB[s][c];
                    ad[cellAge(c)][cellSex(c)] += w * logistic(cellLogitRaw(s, c) + offset[s] * BLOC_STATE_W[cellBloc(c)]);
                    at[cellAge(c)][cellSex(c)] += w;
                }
            for (int a = 0; a < NAGE; a++) for (int x = 0; x < NSEX; x++) ageSexK[a][x] += 0.7 * (logit(AGESEX_D24[a][x]) - logit(ad[a][x] / at[a][x]));
        }
        for (int s = 0; s < NS; s++)
            for (int c = 0; c < NCELL; c++) baseL[s][c] = cellLogitRaw(s, c) + offset[s] * BLOC_STATE_W[cellBloc(c)];
    }

    static double baseTurn(int c) {
        int b = cellBloc(c), a = cellAge(c), x = cellSex(c);
        return BLOC_TURN[b] * AGE_TURN[a] * (x == 1 ? 1.03 : 0.97);
    }

    /** Builds the TV market table: real DMAs plus a 'rest of state' market where coverage is incomplete. */
    static void markets() {
        List<String> names = new ArrayList<String>();
        List<Double> hh = new ArrayList<Double>();
        List<double[]> shares = new ArrayList<double[]>();
        double[] cov = new double[NS];
        for (int m = 0; m < DMA_NAMES.length; m++) {
            double[] sh = new double[NS];
            for (String p : DMA_STATES[m].split(" ")) {
                String[] kv = p.split(":");
                int s = stateIdx(kv[0]);
                double h = DMA_HH[m] * Double.parseDouble(kv[1]);
                sh[s] = h / STATE_TVHH[s];
                cov[s] += sh[s];
            }
            names.add(DMA_NAMES[m]); hh.add(DMA_HH[m]); shares.add(sh);
        }
        for (int s = 0; s < NS; s++) {
            if (cov[s] > 1) for (double[] sh : shares) sh[s] /= cov[s];
            else if (cov[s] < 0.93) {
                double[] sh = new double[NS];
                sh[s] = 1 - cov[s];
                names.add("Rest of " + STATES[s].name);
                hh.add(STATE_TVHH[s] * (1 - cov[s]));
                shares.add(sh);
            }
        }
        NM = names.size();
        marketName = names.toArray(new String[0]);
        marketHH = new double[NM];
        dmaShare = new double[NM][];
        marketStates = new int[NM][];
        for (int m = 0; m < NM; m++) {
            marketHH[m] = hh.get(m);
            dmaShare[m] = shares.get(m);
            int k = 0;
            for (int s = 0; s < NS; s++) if (dmaShare[m][s] > 0.001) k++;
            marketStates[m] = new int[k];
            k = 0;
            for (int s = 0; s < NS; s++) if (dmaShare[m][s] > 0.001) marketStates[m][k++] = s;
        }
    }

    /** Markets covering a state, largest first. */
    static List<Integer> marketsIn(int s) {
        List<Integer> l = new ArrayList<Integer>();
        for (int m = 0; m < NM; m++) if (dmaShare[m][s] > 0.001) l.add(m);
        final int ss = s;
        Collections.sort(l, new Comparator<Integer>() { public int compare(Integer a, Integer b) { return Double.compare(dmaShare[b][ss], dmaShare[a][ss]); } });
        return l;
    }
}
