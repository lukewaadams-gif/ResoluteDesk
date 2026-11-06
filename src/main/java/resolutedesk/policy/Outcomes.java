package resolutedesk.policy;

import static resolutedesk.core.Fmt.*;

/**
 * The measurable results of policy: the numbers a government is judged by. The first five come from the economy
 * model; the rest the policy model keeps, starting from their real values in the year a game begins.
 */
public final class Outcomes {
    private Outcomes() {
    }

    public static final int O_UNEMP = 0, O_INFL = 1, O_GROWTH = 2, O_DEFICIT = 3, O_GAS = 4, O_UNINS = 5, O_POV = 6, O_CRIME = 7, O_OD = 8, O_BORDER = 9,
            O_EMIS = 10, O_SCORES = 11, NOUT = 12;
    public static final String[] OC_NAME = {"Unemployment", "Inflation", "Growth", "Deficit", "Gas", "Uninsured", "Poverty", "Violent crime", "Overdose deaths",
            "Border crossings", "Carbon emissions", "Test scores"};
    public static final String[] OC_UNIT = {"%", "%", "%", "% of GDP", "a gallon", "%", "%", "per 100,000", "a year", "a month", "tons a year", "index"};
    public static final String[] OC_DESC = {"Share of the workforce looking for work.", "Prices, over the past year.", "Real GDP growth, a year's pace.",
            "Federal borrowing as a share of the economy.", "Average price of regular gasoline.", "Share of Americans without health insurance.",
            "Share of Americans below the poverty line.", "Violent crimes per 100,000 people.", "Deaths from drug overdoses a year.",
            "Crossings and apprehensions at the southern border a month.", "Carbon dioxide from burning fuel.", "Student achievement: 100 is the level of 2019."};
    /** The key each outcome goes by in the policy table (the deficit moves through each policy's cost). */
    public static final String[] OC_KEY = {"unemp", "infl", "gdp", "def", "gas", "unins", "pov", "crime", "od", "border", "emis", "scores"};
    /** Whether higher is better (1) or worse (-1). */
    public static final int[] OC_GOOD = {-1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, 1};
    /** Outcomes whose effects in the table are in percent of their level. */
    public static final boolean[] OC_PCT = {false, false, false, false, false, false, false, true, true, true, true, false};
    /** Outcomes the economy model owns. */
    public static boolean outFromEconomy(int o) { return o <= O_GAS; }

    /** An outcome's real value in a year, between the nearest data points. */
    public static double outHistorical(int o, int year) {
        double[] h = PolicyTable.HISTORY[o];
        if (h == null) return 0;
        if (year <= h[0]) return h[1];
        for (int i = 2; i < h.length; i += 2) {
            if (year <= h[i]) {
                double t = (year - h[i - 2]) / (h[i] - h[i - 2]);
                return h[i - 1] + t * (h[i + 1] - h[i - 1]);
            }
        }
        return h[h.length - 1];
    }

    /** An outcome written for the screen, with its unit. */
    public static String outShow(int o, double v) {
        switch (o) {
            case O_GAS: return "$" + fmt2(v);
            case O_CRIME: return String.valueOf(Math.round(v));
            case O_OD: return Math.round(v) + ",000";
            case O_BORDER: return v >= 10 ? Math.round(v) + ",000" : fmt1(v) + " thousand";
            case O_EMIS: return fmt2(v) + " billion";
            case O_SCORES: return fmt1(v);
            default: return fmt1(v) + "%";
        }
    }
}
