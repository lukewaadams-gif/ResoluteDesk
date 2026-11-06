package resolutedesk.ui;

/** Map modes and callouts for the map of the states. */
public final class MapData {
    private MapData() {
    }

    public static final int MM_BLANK = 0, MM_MONEY = 1, MM_INTERNAL = 2, MM_PUBLIC = 3, MM_FIELD = 4, MM_CAL = 5, MM_BALLOT = 6;

    public static final String[] CALLOUTS = {"VT", "NH", "MA", "RI", "CT", "NJ", "DE", "MD", "DC"};
}
