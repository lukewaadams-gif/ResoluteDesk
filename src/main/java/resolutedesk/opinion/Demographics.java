package resolutedesk.opinion;

import static resolutedesk.opinion.VoterIssues.*;

import java.util.Arrays;

/** Voter blocs, ages, sexes and overlapping groups, and how each leans. */
public final class Demographics {
    private Demographics() {
    }

    // ---- voter blocs (16) x age (3) x sex (2) = 96 cells per state ---------------------------------------
    public static final int B_WEV_NC = 0, B_WEV_C = 1, B_WCA_NC = 2, B_WCA_C = 3, B_WOT_NC = 4, B_WOT_C = 5, B_JEW = 6, B_LDS = 7,
            B_BLK = 8, B_HMX = 9, B_HCU = 10, B_HPR = 11, B_ASN = 12, B_NAT = 13, B_ARB = 14, B_OTH = 15, NB = 16;

    public static final String[] BLOC = {"White evangelicals (no degree)", "White evangelicals (degree)", "White Catholics (no degree)",
            "White Catholics (degree)", "Other whites (no degree)", "Other whites (degree)", "Jewish", "Latter-day Saints",
            "Black", "Hispanic (Mexican & other)", "Cuban & Venezuelan", "Puerto Rican & Dominican", "Asian American",
            "Native American", "Arab & Muslim American", "Multiracial & other"};

    public static final int AGE_YOUNG = 0, AGE_MID = 1, AGE_OLD = 2, NAGE = 3;

    public static final String[] AGE = {"18-29", "30-64", "65+"};

    public static final int NSEX = 2;                       // 0 men, 1 women

    public static final int NCELL = NB * NAGE * NSEX;

    public static int cell(int b, int a, int s) { return (b * NAGE + a) * NSEX + s; }

    public static int cellBloc(int c) { return c / (NAGE * NSEX); }

    public static int cellAge(int c) { return (c / NSEX) % NAGE; }

    public static int cellSex(int c) { return c % NSEX; }

    public static boolean white(int b) { return b <= B_WOT_C; }

    public static boolean hispanic(int b) { return b == B_HMX || b == B_HCU || b == B_HPR; }

    public static boolean college(int b) { return b == B_WEV_C || b == B_WCA_C || b == B_WOT_C; }

    /** National two-party Democratic share of each bloc in 2024 (Pew validated voters, AP VoteCast, exit polls). */
    public static final double[] BLOC_D24 = {0.15, 0.25, 0.34, 0.47, 0.41, 0.63, 0.69, 0.32, 0.86, 0.55, 0.32, 0.63, 0.59, 0.57, 0.52, 0.57};

    /** Weight of the state's own partisan offset for each bloc (regional variation is largest among whites). */
    public static final double[] BLOC_STATE_W = {1, 1, 1, 1, 1, 1, 0.45, 0.8, 0.3, 0.6, 0.6, 0.6, 0.6, 0.6, 0.5, 0.8};

    /** How far each bloc moves with the national environment and campaigns (persuadability). */
    public static final double[] BLOC_ELAST = {0.55, 0.75, 1.0, 1.0, 1.0, 1.0, 0.8, 0.85, 0.65, 1.25, 0.95, 1.15, 1.1, 0.9, 1.05, 1.0};

    /** Base turnout of eligible citizens. */
    public static final double[] BLOC_TURN = {0.70, 0.84, 0.70, 0.83, 0.60, 0.80, 0.82, 0.74, 0.60, 0.48, 0.60, 0.50, 0.58, 0.50, 0.55, 0.55};

    /** Gender gap multiplier (2024: very large among Black and Hispanic voters). */
    public static final double[] BLOC_GENDER = {0.6, 0.6, 0.8, 0.8, 0.85, 0.85, 0.6, 0.6, 2.0, 1.5, 1.0, 1.4, 0.9, 0.9, 0.7, 1.0};

    /** Age profile of each bloc's eligible voters {18-29, 30-64, 65+}. */
    public static final double[][] BLOC_AGE = {
            {0.13, 0.54, 0.33}, {0.12, 0.56, 0.32}, {0.15, 0.54, 0.31}, {0.14, 0.56, 0.30}, {0.18, 0.54, 0.28}, {0.20, 0.57, 0.23},
            {0.17, 0.50, 0.33}, {0.25, 0.55, 0.20}, {0.24, 0.58, 0.18}, {0.31, 0.56, 0.13}, {0.20, 0.55, 0.25}, {0.29, 0.56, 0.15},
            {0.23, 0.59, 0.18}, {0.26, 0.58, 0.16}, {0.31, 0.57, 0.12}, {0.38, 0.52, 0.10}};

    /** Age x sex logit adjustments (2024: young men swung right, young women stayed left). */
    public static final double[][] AGESEX_ADJ = {{-0.12, 0.50}, {-0.22, 0.16}, {-0.28, 0.10}};

    public static final double[] AGE_ELAST = {1.30, 1.0, 0.85};

    public static final double[] AGE_TURN = {0.70, 1.0, 1.15};

    // ---- overlays: fractions of each cell with a trait ----------------------------------------------------
    public static final int O_RURAL = 0, O_SUBURB = 1, O_URBAN = 2, O_UNION = 3, O_VET = 4, O_GUN = 5, O_LGBT = 6, O_STUDENT = 7,
            O_PARENT = 8, NO = 9;

    public static final String[] OVERLAY = {"Rural", "Suburban", "Urban", "Union household", "Veteran household", "Gun owner",
            "LGBTQ", "College student", "Parent of minor"};

    /** Partisan lean of each overlay relative to its cell (logit). */
    public static final double[] OV_LEAN = {-0.55, 0.05, 0.45, 0.35, -0.35, -0.55, 1.2, 0.45, -0.10};

    // ---- the 30 voter groups shown to the player ----------------------------------------------------------
    public static final int NG = 30;

    public static final String[] GROUP = {"Gen Z (18-29)", "Seniors (65+)", "Young men", "Young women", "Women", "Men",
            "White evangelicals", "White Catholics", "White, no degree", "White, college degree", "Black voters", "Black men",
            "Hispanic voters", "Hispanic men", "Cuban & Venezuelan", "Puerto Rican & Dominican", "Asian Americans",
            "Jewish voters", "Arab & Muslim Americans", "Latter-day Saints", "Native Americans", "Suburban voters",
            "Rural voters", "Urban voters", "Union households", "Veterans & families", "Gun owners", "LGBTQ voters",
            "College students", "Parents"};

    /** For overlay-defined groups, which overlay; otherwise -1. */
    public static final int[] GROUP_OV = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            O_SUBURB, O_RURAL, O_URBAN, O_UNION, O_VET, O_GUN, O_LGBT, O_STUDENT, O_PARENT};

    /** Membership weight of cell c in group g (for non-overlay groups). */
    public static double inGroup(int g, int c) {
        int b = cellBloc(c), a = cellAge(c), s = cellSex(c);
        switch (g) {
            case 0: return a == AGE_YOUNG ? 1 : 0;
            case 1: return a == AGE_OLD ? 1 : 0;
            case 2: return a == AGE_YOUNG && s == 0 ? 1 : 0;
            case 3: return a == AGE_YOUNG && s == 1 ? 1 : 0;
            case 4: return s == 1 ? 1 : 0;
            case 5: return s == 0 ? 1 : 0;
            case 6: return b == B_WEV_NC || b == B_WEV_C ? 1 : 0;
            case 7: return b == B_WCA_NC || b == B_WCA_C ? 1 : 0;
            case 8: return b == B_WEV_NC || b == B_WCA_NC || b == B_WOT_NC ? 1 : 0;
            case 9: return b == B_WEV_C || b == B_WCA_C || b == B_WOT_C ? 1 : 0;
            case 10: return b == B_BLK ? 1 : 0;
            case 11: return b == B_BLK && s == 0 ? 1 : 0;
            case 12: return hispanic(b) ? 1 : 0;
            case 13: return hispanic(b) && s == 0 ? 1 : 0;
            case 14: return b == B_HCU ? 1 : 0;
            case 15: return b == B_HPR ? 1 : 0;
            case 16: return b == B_ASN ? 1 : 0;
            case 17: return b == B_JEW ? 1 : 0;
            case 18: return b == B_ARB ? 1 : 0;
            case 19: return b == B_LDS ? 1 : 0;
            case 20: return b == B_NAT ? 1 : 0;
            default: return 0;
        }
    }

    // ---- issue opinion by bloc, age, sex and overlay ---------------------------------------------------------
    /** Bloc-specific shifts of the ideal point (added to the partisan blend). */
    public static double blocIdealShift(int b, int i) {
        switch (b) {
            case B_WEV_NC: case B_WEV_C:
                return i == I_ISRAEL ? 0.40 : i == I_ABORT ? 0.40 : i == I_TRANS ? 0.30 : i == I_GUNS ? 0.20 : 0;
            case B_WCA_NC: case B_WCA_C:
                return i == I_ABORT ? 0.15 : 0;
            case B_JEW:
                return i == I_ISRAEL ? 0.70 : i == I_DEMOC ? -0.10 : i == I_GUNS ? -0.15 : 0;
            case B_LDS:
                return i == I_IMMIG ? -0.25 : i == I_DEMOC ? -0.30 : i == I_ABORT ? 0.20 : i == I_UKRAINE ? -0.20 : 0;
            case B_BLK:
                return i == I_ISRAEL ? -0.20 : i == I_TRANS ? 0.40 : i == I_ABORT ? 0.15 : i == I_IMMIG ? 0.20 : i == I_CRIME ? 0.05 : 0;
            case B_HMX:
                return i == I_IMMIG ? -0.15 : i == I_ABORT ? 0.15 : i == I_CRIME ? 0.10 : i == I_TRANS ? 0.15 : 0;
            case B_HCU:
                return i == I_CHINA ? 0.30 : i == I_ISRAEL ? 0.20 : i == I_HEALTH ? -0.10 : 0;
            case B_HPR:
                return i == I_HEALTH ? -0.10 : i == I_TRANS ? 0.10 : 0;
            case B_ASN:
                return i == I_CHINA ? -0.10 : i == I_EDU ? 0.15 : i == I_GUNS ? -0.20 : i == I_CRIME ? 0.15 : 0;
            case B_NAT:
                return i == I_CLIMATE ? -0.20 : i == I_GUNS ? 0.20 : 0;
            case B_ARB:
                return i == I_ISRAEL ? -1.10 : i == I_TRANS ? 0.50 : i == I_ABORT ? 0.30 : 0;
            default:
                return 0;
        }
    }

    public static double blocSalMul(int b, int i) {
        switch (b) {
            case B_WEV_NC: case B_WEV_C: return i == I_ABORT ? 1.7 : i == I_TRANS ? 1.9 : i == I_ISRAEL ? 1.4 : 1;
            case B_WOT_C: case B_WCA_C: return i == I_DEMOC ? 1.5 : i == I_CLIMATE ? 1.3 : 1;
            case B_JEW: return i == I_ISRAEL ? 3.2 : i == I_DEMOC ? 1.3 : 1;
            case B_ARB: return i == I_ISRAEL ? 6.5 : 1;
            case B_BLK: return i == I_CRIME ? 1.2 : i == I_COL ? 1.1 : i == I_HEALTH ? 1.2 : 1;
            case B_HMX: case B_HPR: return i == I_IMMIG ? 1.3 : i == I_COL ? 1.2 : 1;
            case B_HCU: return i == I_CHINA ? 1.8 : i == I_IMMIG ? 1.2 : 1;
            case B_ASN: return i == I_CHINA ? 1.5 : i == I_EDU ? 1.3 : 1;
            case B_LDS: return i == I_DEMOC ? 1.3 : 1;
            default: return 1;
        }
    }

    public static final double[][] AGE_IDEAL = {
            {-0.15, -0.10, -0.05, -0.05, -0.05, -0.45, -0.10, -0.10, -0.05, -0.05, 0.05, -0.10, -0.20, 0, -0.10, -0.30, 0.10, -0.25},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0.10, 0.05, 0.05, 0, 0, 0.25, 0.05, 0.05, 0, 0, -0.15, 0.10, 0.05, 0, 0, 0.05, -0.15, 0.10}};

    public static final double[][] AGE_SAL = {
            {0.8, 1.3, 1.0, 1.3, 0.8, 2.5, 2.0, 0.9, 1.1, 0.8, 0.35, 0.8, 1.5, 1.0, 1.1, 2.0, 0.7, 1.2},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1.2, 0.9, 1.0, 0.7, 1.0, 1.1, 0.5, 1.1, 0.9, 1.4, 2.4, 1.2, 0.8, 1.2, 0.9, 0.4, 1.3, 0.9}};

    public static final double[][] SEX_IDEAL = {
            {0.05, 0.10, 0.03, 0.03, 0.03, 0.05, 0.03, 0.05, 0.10, 0.05, 0, 0.05, 0.05, 0.05, 0.12, 0.03, 0.05, 0.08},
            {-0.05, -0.10, -0.03, -0.03, -0.03, -0.05, -0.03, -0.05, -0.12, -0.05, 0, -0.05, -0.05, -0.05, -0.12, -0.03, -0.05, -0.08}};

    public static final double[][] SEX_SAL = {
            {1.2, 1.1, 0.95, 1.05, 1.1, 1.0, 0.95, 1.05, 0.7, 0.9, 0.95, 1.1, 0.9, 1.0, 1.2, 0.9, 1.1, 1.0},
            {0.85, 0.9, 1.05, 0.95, 0.9, 1.0, 1.05, 0.95, 1.4, 1.15, 1.05, 0.95, 1.1, 1.0, 0.85, 1.1, 0.9, 1.0}};

    /** Overlay ideal shifts and salience multipliers (indexed [overlay][issue]). */
    public static final double[][] OV_IDEAL = new double[NO][NI];

    public static final double[][] OV_SAL = new double[NO][NI];

    static {
        for (int o = 0; o < NO; o++) Arrays.fill(OV_SAL[o], 1);
        OV_IDEAL[O_RURAL][I_AI] = -0.25; OV_SAL[O_RURAL][I_AI] = 1.4; OV_SAL[O_RURAL][I_GUNS] = 1.4; OV_IDEAL[O_RURAL][I_CLIMATE] = 0.2;
        OV_SAL[O_URBAN][I_CRIME] = 1.3; OV_SAL[O_URBAN][I_HOUSING] = 1.5; OV_IDEAL[O_URBAN][I_HOUSING] = -0.1;
        OV_SAL[O_SUBURB][I_COL] = 1.1; OV_SAL[O_SUBURB][I_ABORT] = 1.1; OV_SAL[O_SUBURB][I_EDU] = 1.1;
        OV_SAL[O_UNION][I_JOBS] = 1.6; OV_SAL[O_UNION][I_TRADE] = 1.8; OV_IDEAL[O_UNION][I_TRADE] = 0.3; OV_IDEAL[O_UNION][I_JOBS] = -0.3; OV_IDEAL[O_UNION][I_AI] = -0.2;
        OV_SAL[O_VET][I_CHINA] = 1.5; OV_SAL[O_VET][I_UKRAINE] = 1.3; OV_IDEAL[O_VET][I_CHINA] = 0.15;
        OV_SAL[O_GUN][I_GUNS] = 3.0; OV_IDEAL[O_GUN][I_GUNS] = 0.40;
        OV_SAL[O_LGBT][I_TRANS] = 4.0; OV_IDEAL[O_LGBT][I_TRANS] = -0.60; OV_SAL[O_LGBT][I_DEMOC] = 1.3;
        OV_SAL[O_STUDENT][I_EDU] = 3.0; OV_SAL[O_STUDENT][I_ISRAEL] = 1.6; OV_IDEAL[O_STUDENT][I_ISRAEL] = -0.3; OV_SAL[O_STUDENT][I_HOUSING] = 1.5;
        OV_SAL[O_PARENT][I_EDU] = 1.4; OV_SAL[O_PARENT][I_COL] = 1.2; OV_SAL[O_PARENT][I_TRANS] = 1.3;
    }

    /** Groups shown in the approval tables. */
    public static final int[] SHOW_GROUPS = {0, 1, 4, 5, 8, 9, 10, 12, 16, 6, 7, 21, 22, 23, 24, 25, 29};
}
