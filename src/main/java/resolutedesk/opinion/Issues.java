package resolutedesk.opinion;

/** The policy areas that make up the president's approval. */
public final class Issues {
    private Issues() {
    }

    // ---- the issues voters judge a president on -----------------------------------------------------------
    public static final int PI_ECON = 0, PI_PRICES = 1, PI_JOBS = 2, PI_HEALTH = 3, PI_IMMIG = 4, PI_CRIME = 5, PI_FOREIGN = 6, PI_SECURITY = 7,
            PI_TRADE = 8, PI_ENERGY = 9, PI_RIGHTS = 10, PI_TECH = 11, PI_EDU = 12, PI_ETHICS = 13, NPI = 14;

    public static final String[] PISSUE = {"The economy", "Prices", "Jobs", "Health care", "Immigration", "Crime", "Foreign policy", "National security",
            "Trade", "Energy & climate", "Civil rights", "Technology & AI", "Education", "Ethics & government"};

    public static final String[] PISSUE_SHORT = {"Economy", "Prices", "Jobs", "Health", "Immigration", "Crime", "Foreign", "Security", "Trade", "Energy", "Rights", "Tech & AI", "Education", "Ethics"};
}
