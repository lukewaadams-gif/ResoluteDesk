package resolutedesk.campaign;

/** How a term ends, campaign themes and debate appearances. */
public final class CampaignData {
    private CampaignData() {
    }

    //  The re-election campaign: whether to run, a primary challenge if you're weak, the other party's nominee,
    //  the conventions, the debates, the fall campaign and election night. Then a second term, or the end.
    public static final int END_NONE = 0, END_SERVED = 1, END_LOST = 2, END_RETIRED = 3, END_DENIED = 4, END_REMOVED = 5;

    public static final String[] END_NAME = {"", "Served out the term", "Defeated for re-election", "Stepped aside", "Denied renomination", "Removed from office"};

    public static final String[] THEME = {"Run on the record", "Draw the contrast", "The next four years"};

    public static final String[] THEME_DESC = {"Works when times are good and people approve of you.", "Works when the other side is weaker than you are, or you are behind.", "Works for a fresh face, or when the country wants change."};

    public static final String[] DEBATE_APP = {"Defend the record", "Attack their plan", "Talk about the future", "Go for the memorable line"};

    /** The House a party can expect at an even split, and how fast it moves with the national vote. */
    public static double houseBase(int y) { return y < 1939 ? 300 : y < 1995 ? 255 : 222; }

    public static double houseSlope(int y) { return y < 1995 ? 2.0 : 3.8; }
}
