package resolutedesk.home;

/** Natural disasters and where they strike. */
public final class DisasterData {
    private DisasterData() {
    }

    // ---- natural disasters ----
    public static final String[] DIS_NAME = {"hurricane", "wildfire", "tornado outbreak", "flood", "winter storm", "earthquake"};

    public static final String[] DIS_STATES = {"FL TX LA NC SC GA AL MS", "CA OR WA CO AZ NM", "OK KS MO AL TN MS AR TX", "TX MO IA KY WV LA", "TX NE MN NY MI PA", "CA OR WA AK"};

    public static final int[] DIS_M0 = {5, 5, 2, 2, 11, 0}, DIS_M1 = {10, 9, 5, 8, 1, 11};

    public static final double[] DIS_P = {0.06, 0.03, 0.035, 0.02, 0.03, 0.002};
}
