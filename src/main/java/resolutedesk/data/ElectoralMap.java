package resolutedesk.data;

import static resolutedesk.data.StateData.*;

import resolutedesk.opinion.Elect;

/** Electoral votes by census and how each state leaned in each era. */
public final class ElectoralMap {
    private ElectoralMap() {
    }

    // ---- the Electoral College, census by census ------------------------------------------------------------
    public static final int[] EV_FROM = {1932, 1944, 1952, 1960, 1964, 1972, 1984, 1992, 2004, 2012, 2024};

    public static final String[] EV_TABLE = {
            "AL:11 AZ:3 AR:9 CA:22 CO:6 CT:8 DE:3 FL:7 GA:12 ID:4 IL:29 IN:14 IA:11 KS:9 KY:11 LA:10 ME:5 MD:8 MA:17 MI:19 MN:11 MS:9 MO:15 MT:4 NE:7 NV:3 NH:4 NJ:16 NM:3 NY:47 NC:13 ND:4 OH:26 OK:11 OR:5 PA:36 RI:4 SC:8 SD:4 TN:11 TX:23 UT:4 VT:3 VA:11 WA:8 WV:8 WI:12 WY:3",
            "AL:11 AZ:4 AR:9 CA:25 CO:6 CT:8 DE:3 FL:8 GA:12 ID:4 IL:28 IN:13 IA:10 KS:8 KY:11 LA:10 ME:5 MD:8 MA:16 MI:19 MN:11 MS:9 MO:15 MT:4 NE:6 NV:3 NH:4 NJ:16 NM:4 NY:47 NC:14 ND:4 OH:25 OK:10 OR:6 PA:35 RI:4 SC:8 SD:4 TN:12 TX:23 UT:4 VT:3 VA:11 WA:8 WV:8 WI:12 WY:3",
            "AL:11 AZ:4 AR:8 CA:32 CO:6 CT:8 DE:3 FL:10 GA:12 ID:4 IL:27 IN:13 IA:10 KS:8 KY:10 LA:10 ME:5 MD:9 MA:16 MI:20 MN:11 MS:8 MO:13 MT:4 NE:6 NV:3 NH:4 NJ:16 NM:4 NY:45 NC:14 ND:4 OH:25 OK:8 OR:6 PA:32 RI:4 SC:8 SD:4 TN:11 TX:24 UT:4 VT:3 VA:12 WA:9 WV:8 WI:12 WY:3",
            "AL:11 AK:3 AZ:4 AR:8 CA:32 CO:6 CT:8 DE:3 FL:10 GA:12 HI:3 ID:4 IL:27 IN:13 IA:10 KS:8 KY:10 LA:10 ME:5 MD:9 MA:16 MI:20 MN:11 MS:8 MO:13 MT:4 NE:6 NV:3 NH:4 NJ:16 NM:4 NY:45 NC:14 ND:4 OH:25 OK:8 OR:6 PA:32 RI:4 SC:8 SD:4 TN:11 TX:24 UT:4 VT:3 VA:12 WA:9 WV:8 WI:12 WY:3",
            "AL:10 AK:3 AZ:5 AR:6 CA:40 CO:6 CT:8 DE:3 DC:3 FL:14 GA:12 HI:4 ID:4 IL:26 IN:13 IA:9 KS:7 KY:9 LA:10 ME:4 MD:10 MA:14 MI:21 MN:10 MS:7 MO:12 MT:4 NE:5 NV:3 NH:4 NJ:17 NM:4 NY:43 NC:13 ND:4 OH:26 OK:8 OR:6 PA:29 RI:4 SC:8 SD:4 TN:11 TX:25 UT:4 VT:3 VA:12 WA:9 WV:7 WI:12 WY:3",
            "AL:9 AK:3 AZ:6 AR:6 CA:45 CO:7 CT:8 DE:3 DC:3 FL:17 GA:12 HI:4 ID:4 IL:26 IN:13 IA:8 KS:7 KY:9 LA:10 ME:4 MD:10 MA:14 MI:21 MN:10 MS:7 MO:12 MT:4 NE:5 NV:3 NH:4 NJ:17 NM:4 NY:41 NC:13 ND:3 OH:25 OK:8 OR:6 PA:27 RI:4 SC:8 SD:4 TN:10 TX:26 UT:4 VT:3 VA:12 WA:9 WV:6 WI:11 WY:3",
            "AL:9 AK:3 AZ:7 AR:6 CA:47 CO:8 CT:8 DE:3 DC:3 FL:21 GA:12 HI:4 ID:4 IL:24 IN:12 IA:8 KS:7 KY:9 LA:10 ME:4 MD:10 MA:13 MI:20 MN:10 MS:7 MO:11 MT:4 NE:5 NV:4 NH:4 NJ:16 NM:5 NY:36 NC:13 ND:3 OH:23 OK:8 OR:7 PA:25 RI:4 SC:8 SD:3 TN:11 TX:29 UT:5 VT:3 VA:12 WA:10 WV:6 WI:11 WY:3",
            "AL:9 AK:3 AZ:8 AR:6 CA:54 CO:8 CT:8 DE:3 DC:3 FL:25 GA:13 HI:4 ID:4 IL:22 IN:12 IA:7 KS:6 KY:8 LA:9 ME:4 MD:10 MA:12 MI:18 MN:10 MS:7 MO:11 MT:3 NE:5 NV:4 NH:4 NJ:15 NM:5 NY:33 NC:14 ND:3 OH:21 OK:8 OR:7 PA:23 RI:4 SC:8 SD:3 TN:11 TX:32 UT:5 VT:3 VA:13 WA:11 WV:5 WI:11 WY:3",
            "AL:9 AK:3 AZ:10 AR:6 CA:55 CO:9 CT:7 DE:3 DC:3 FL:27 GA:15 HI:4 ID:4 IL:21 IN:11 IA:7 KS:6 KY:8 LA:9 ME:4 MD:10 MA:12 MI:17 MN:10 MS:6 MO:11 MT:3 NE:5 NV:5 NH:4 NJ:15 NM:5 NY:31 NC:15 ND:3 OH:20 OK:7 OR:7 PA:21 RI:4 SC:8 SD:3 TN:11 TX:34 UT:5 VT:3 VA:13 WA:11 WV:5 WI:10 WY:3",
            "AL:9 AK:3 AZ:11 AR:6 CA:55 CO:9 CT:7 DE:3 DC:3 FL:29 GA:16 HI:4 ID:4 IL:20 IN:11 IA:6 KS:6 KY:8 LA:8 ME:4 MD:10 MA:11 MI:16 MN:10 MS:6 MO:10 MT:3 NE:5 NV:6 NH:4 NJ:14 NM:5 NY:29 NC:15 ND:3 OH:18 OK:7 OR:7 PA:20 RI:4 SC:9 SD:3 TN:11 TX:38 UT:6 VT:3 VA:13 WA:12 WV:5 WI:10 WY:3",
            null};

    public static int[] EV_NOW = new int[NS];

    public static int EV_YEAR = -1;

    /** Electoral votes by state for an election year; the 2024 map from then on. */
    public static int[] evFor(int year) {
        if (STATE_EV24[0] == 0) for (int s = 0; s < NS; s++) STATE_EV24[s] = STATES[s].ev;
        int k = 0;
        for (int i = 0; i < EV_FROM.length; i++) if (year >= EV_FROM[i]) k = i;
        int[] v = new int[NS];
        if (EV_TABLE[k] == null) { for (int s = 0; s < NS; s++) v[s] = STATE_EV24[s]; return v; }
        for (String tok : EV_TABLE[k].split(" ")) { String[] p = tok.split(":"); int s = stateIdx(p[0]); if (s >= 0) v[s] = Integer.parseInt(p[1]); }
        return v;
    }

    public static int[] STATE_EV24 = new int[NS];

    public static int evTotal(int[] ev) { int t = 0; for (int x : ev) t += x; return t; }

    /** Is a state part of the Union (and voting for president) in a year? */
    public static boolean votes(int s, int year) {
        String ab = STATES[s].ab;
        if (ab.equals("AK") || ab.equals("HI")) return year >= 1959;
        if (ab.equals("DC")) return year >= 1964;
        return true;
    }

    // ---- how each state leaned: D-minus-R margin against the nation, in points, at anchor elections ------------
    public static final int[] LEAN_YEAR = {1936, 1960, 1976, 1992, 2008};

    public static final String[] LEAN_TABLE = {
            "AL:49 AK:-2 AZ:19 AR:40 CA:11 CO:-1 CT:-9 DE:-13 DC:60 FL:28 GA:50 HI:0 ID:7 IL:-6 IN:-10 IA:-12 KS:-16 KY:-5 LA:54 ME:-38 MD:1 MA:-15 MI:-6 MN:6 MS:70 MO:-2 MT:18 NE:-8 NV:21 NH:-22 NJ:-4 NM:2 NY:-5 NC:23 ND:9 OH:-4 OK:10 OR:14 PA:-8 RI:-11 SC:72 SD:-12 TN:14 TX:51 UT:16 VT:-37 VA:17 WA:13 WV:-3 WI:10 WY:-1",
            "AL:15 AK:-2 AZ:-11 AR:7 CA:-1 CO:-10 CT:7 DE:1 DC:60 FL:-3 GA:25 HI:0 ID:-8 IL:0 IN:-11 IA:-13 KS:-22 KY:-7 LA:22 ME:-14 MD:7 MA:20 MI:2 MN:1 MS:30 MO:0 MT:-3 NE:-24 NV:2 NH:-7 NJ:1 NM:1 NY:5 NC:4 ND:-11 OH:-7 OK:-18 OR:-5 PA:2 RI:27 SC:2 SD:-16 TN:-7 TX:2 UT:-10 VT:-17 VA:-6 WA:-3 WV:5 WI:-4 WY:-10",
            "AL:11 AK:-24 AZ:-19 AR:28 CA:-4 CO:-14 CT:-7 DE:3 DC:63 FL:3 GA:32 HI:1 ID:-25 IL:-4 IN:-10 IA:-3 KS:-10 KY:5 LA:4 ME:-3 MD:4 MA:14 MI:-8 MN:11 MS:0 MO:2 MT:-10 NE:-23 NV:-7 NH:-13 NJ:-4 NM:-5 NY:2 NC:9 ND:-8 OH:-2 OK:-3 OR:-2 PA:1 RI:9 SC:11 SD:-4 TN:11 TX:1 UT:-30 VT:-13 VA:-3 WA:-6 WV:14 WI:0 WY:-22",
            "AL:-12 AK:-15 AZ:-8 AR:12 CA:8 CO:-1 CT:1 DE:3 DC:70 FL:-7 GA:-5 HI:6 ID:-19 IL:9 IN:-12 IA:0 KS:-11 KY:-2 LA:-1 ME:3 MD:9 MA:13 MI:2 MN:6 MS:-15 MO:5 MT:-3 NE:-23 NV:-3 NH:-4 NJ:-3 NM:3 NY:10 NC:-6 ND:-18 OH:-4 OK:-14 OR:5 PA:3 RI:12 SC:-14 SD:-9 TN:-1 TX:-9 UT:-24 VT:10 VA:-10 WA:6 WV:7 WI:-1 WY:-11",
            "AL:-29 AK:-29 AZ:-16 AR:-27 CA:17 CO:2 CT:15 DE:18 DC:79 FL:-5 GA:-12 HI:38 ID:-33 IL:18 IN:-6 IA:2 KS:-22 KY:-23 LA:-26 ME:10 MD:18 MA:19 MI:9 MN:3 MS:-21 MO:-7 MT:-10 NE:-22 NV:5 NH:2 NJ:8 NM:8 NY:20 NC:-7 ND:-16 OH:-3 OK:-39 OR:9 PA:3 RI:21 SC:-16 SD:-16 TN:-22 TX:-19 UT:-35 VT:30 VA:-1 WA:10 WV:-20 WI:7 WY:-40"};

    public static double[][] LEANS;

    /** A state's lean (D-minus-R points against the nation) in a year: the anchors blended, then today's electorate from 2008 to 2024. */
    public static double leanIn(int s, int year) {
        if (LEANS == null) {
            LEANS = new double[LEAN_TABLE.length][NS];
            for (int k = 0; k < LEAN_TABLE.length; k++) for (String tok : LEAN_TABLE[k].split(" ")) { String[] p = tok.split(":"); int i = stateIdx(p[0]); if (i >= 0) LEANS[k][i] = Double.parseDouble(p[1]); }
        }
        double now = Elect.lean[s];
        if (year >= 2024) return now;
        if (year <= LEAN_YEAR[0]) return LEANS[0][s];
        if (year >= 2008) { double f = (year - 2008) / 16.0; return LEANS[4][s] * (1 - f) + now * f; }
        for (int k = 0; k < LEAN_YEAR.length - 1; k++) if (year >= LEAN_YEAR[k] && year <= LEAN_YEAR[k + 1]) {
            double f = (year - LEAN_YEAR[k]) / (double) (LEAN_YEAR[k + 1] - LEAN_YEAR[k]);
            return LEANS[k][s] * (1 - f) + LEANS[k + 1][s] * f;
        }
        return now;
    }

    /** How far a state's politics then sit from today's electorate, in points. */
    public static double leanShift(int s, int year) { return year >= 2024 ? 0 : leanIn(s, year) - Elect.lean[s]; }

    /** The largest city in each state, for the presidential library. */
    public static final String CITIES = "AL:Birmingham AK:Anchorage AZ:Phoenix AR:Little Rock CA:Simi Valley CO:Denver CT:Hartford DE:Wilmington DC:Washington FL:Miami GA:Atlanta HI:Honolulu ID:Boise "
            + "IL:Chicago IN:Indianapolis IA:Des Moines KS:Abilene KY:Louisville LA:New Orleans ME:Portland MD:Baltimore MA:Boston MI:Grand Rapids MN:Minneapolis MS:Jackson MO:Independence "
            + "MT:Billings NE:Omaha NV:Las Vegas NH:Manchester NJ:Newark NM:Albuquerque NY:Hyde Park NC:Charlotte ND:Medora OH:Columbus OK:Oklahoma City OR:Portland PA:Philadelphia "
            + "RI:Providence SC:Charleston SD:Sioux Falls TN:Nashville TX:Austin UT:Salt Lake City VT:Burlington VA:Richmond WA:Seattle WV:Charleston WI:Milwaukee WY:Cheyenne";

    public static String cityOf(int s) {
        if (s < 0) return "Washington";
        String ab = STATES[s].ab;
        for (String tok : CITIES.split(" (?=[A-Z][A-Z]:)")) if (tok.startsWith(ab + ":")) return tok.substring(3);
        return STATES[s].name;
    }
}
