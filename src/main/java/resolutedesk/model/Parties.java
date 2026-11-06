package resolutedesk.model;

/** The parties, their names and abbreviations. */
public final class Parties {
    private Parties() {
    }

    // parties
    public static final int DEM = 0, REP = 1, GRN = 2, LBT = 3, FWD = 4, AMP = 5, IND = 6;

    public static final String[] PARTY = {"Democratic", "Republican", "Green", "Libertarian", "Forward", "America Party", "Independent", "Your party"};

    public static final String[] PARTY_SHORT = {"D", "R", "G", "L", "F", "A", "I", "U"};

    public static final int CUS = 7;                             // a party of the player's own design (unused here, kept for shared code)

    public static final String[] PARTIES = {"Democrats", "Republicans", "Greens", "Libertarians", "Forward", "America Party", "Independents", "Your party"};

    public static int other(int p) { return p == REP ? DEM : REP; }
}
