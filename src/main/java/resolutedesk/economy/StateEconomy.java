package resolutedesk.economy;

import static resolutedesk.data.StateData.*;

/** Economic conditions state by state. */
public final class StateEconomy {
    private StateEconomy() {
    }

    //  A small macro model run weekly: an output gap pushed by fiscal policy, interest rates, tariffs, confidence
    //  and shocks; unemployment by Okun's law; inflation by an expectations-anchored Phillips curve; a Fed that
    //  follows a Taylor rule at its eight meetings a year; markets, gas and the prices people feel; the budget.
    /** State unemployment relative to the nation (2024 averages, rounded). */
    public static final String ST_UNEMP = "AL:-1.0 AK:0.6 AZ:-0.4 AR:-0.6 CA:1.3 CO:0.2 CT:0.0 DE:0.0 DC:1.5 FL:-0.7 GA:-0.5 HI:-1.0 ID:-0.5 IL:0.9 IN:0.2 IA:-1.0 KS:-0.6 KY:0.9 LA:0.3 ME:-0.9 MD:-0.9 MA:-0.2 MI:0.7 MN:-0.5 MS:0.2 MO:-0.3 MT:-1.0 NE:-1.3 NV:1.5 NH:-1.4 NJ:0.6 NM:0.0 NY:0.4 NC:-0.3 ND:-1.8 OH:0.4 OK:-0.8 OR:0.1 PA:-0.6 RI:0.4 SC:0.5 SD:-2.0 TN:-0.6 TX:0.1 UT:-0.6 VT:-1.7 VA:-1.1 WA:0.4 WV:0.0 WI:-1.0 WY:-1.0";

    /** State gas prices relative to the national average (2024, dollars). */
    public static final String ST_GAS = "AL:-0.35 AK:0.60 AZ:0.20 AR:-0.40 CA:1.40 CO:0.00 CT:0.10 DE:-0.05 DC:0.30 FL:0.00 GA:-0.30 HI:1.30 ID:0.20 IL:0.40 IN:0.00 IA:-0.20 KS:-0.30 KY:-0.20 LA:-0.40 ME:0.10 MD:0.05 MA:0.10 MI:0.10 MN:-0.10 MS:-0.50 MO:-0.35 MT:0.10 NE:-0.20 NV:0.70 NH:-0.05 NJ:0.00 NM:-0.10 NY:0.20 NC:-0.20 ND:-0.20 OH:-0.10 OK:-0.45 OR:0.70 PA:0.25 RI:0.05 SC:-0.35 SD:-0.15 TN:-0.35 TX:-0.40 UT:0.10 VT:0.20 VA:-0.10 WA:0.90 WV:0.00 WI:-0.15 WY:-0.05";

    /** Which states lean on which industries: manufacturing, farming, energy, tech, tourism. */
    public static final String ST_MFG = "MI IN OH WI IA KY AL SC TN NC AR MS", ST_FARM = "IA NE KS SD ND MN MT ID AR", ST_ENERGY = "TX ND WY OK NM AK WV LA CO PA",
            ST_TECH = "CA WA MA NY VA CO UT TX NC OR MD", ST_TOUR = "NV HI FL";

    public static double[] stateTable(String s) {
        double[] v = new double[NS];
        for (String t : s.split(" ")) { String[] p = t.split(":"); int k = stateIdx(p[0]); if (k >= 0) v[k] = Double.parseDouble(p[1]); }
        return v;
    }

    public static boolean[] stateSet(String s) {
        boolean[] v = new boolean[NS];
        for (String t : s.split(" ")) { int k = stateIdx(t); if (k >= 0) v[k] = true; }
        return v;
    }
}
