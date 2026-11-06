package resolutedesk.opinion;

/** The issues voters weigh, and where each party stands on them. */
public final class VoterIssues {
    private VoterIssues() {
    }

    // ---- the 18 issues; positions run from -1 (left pole) to +1 (right pole) -----------------------------
    public static final int I_CHINA = 0, I_AI = 1, I_COL = 2, I_JOBS = 3, I_TRADE = 4, I_ISRAEL = 5, I_HOUSING = 6, I_IMMIG = 7,
            I_ABORT = 8, I_HEALTH = 9, I_SOCSEC = 10, I_CRIME = 11, I_CLIMATE = 12, I_DEMOC = 13, I_GUNS = 14, I_EDU = 15,
            I_UKRAINE = 16, I_TRANS = 17, NI = 18;

    public static final String[] ISSUE = {"China", "AI & data centers", "Cost of living", "Jobs & AI disruption", "Tariffs & trade",
            "Israel, Gaza & Iran", "Housing", "Immigration", "Abortion", "Health care", "Social Security & Medicare",
            "Crime & policing", "Climate & energy", "Democracy & corruption", "Guns", "Education & student debt",
            "Ukraine & Russia", "Transgender issues"};

    public static final String[][] ISSUE_POLES = {
            {"De-escalate and trade", "Confront and decouple"},
            {"Regulate AI, pause data centers", "Accelerate AI, build data centers"},
            {"Government action on prices", "Cut spending and deregulate"},
            {"Safety net, worker protections", "Markets and retraining only"},
            {"Free trade", "Tariffs and protection"},
            {"Condition or cut aid to Israel", "Unconditional support for Israel"},
            {"Rent limits and public housing", "Build, build, build: deregulate"},
            {"Path to citizenship", "Restrict and deport"},
            {"Abortion rights", "Restrict abortion"},
            {"Single-payer / public option", "Market-based, repeal mandates"},
            {"Expand benefits", "Trim and reform benefits"},
            {"Reform policing", "Tough on crime"},
            {"Clean-energy transition", "Drill and burn"},
            {"Limit presidential power, ethics reform", "Strong presidency, loyalty"},
            {"Gun safety laws", "Gun rights"},
            {"Debt relief, free college", "School choice, no bailouts"},
            {"Back Ukraine to win", "Make a deal with Russia"},
            {"Protect trans rights", "Restrict (sports, youth care)"}};

    /** Base national salience of each issue (share of the vote decision driven by it). */
    public static final double[] ISSUE_SAL = {0.035, 0.045, 0.20, 0.07, 0.045, 0.025, 0.055, 0.09, 0.06, 0.08, 0.06, 0.045,
            0.04, 0.07, 0.03, 0.03, 0.02, 0.025};

    /** Where the typical Democratic and Republican voter stands (2026 polling). */
    public static final double[] IDEAL_D = {0.10, -0.40, -0.70, -0.55, -0.30, -0.45, -0.30, -0.45, -0.80, -0.65, -0.70, -0.25, -0.70, -0.85, -0.70, -0.55, -0.60, -0.40};

    public static final double[] IDEAL_R = {0.60, 0.05, 0.30, 0.25, 0.45, 0.60, 0.40, 0.75, 0.45, 0.40, -0.35, 0.75, 0.60, 0.35, 0.75, 0.60, 0.30, 0.80};
}
