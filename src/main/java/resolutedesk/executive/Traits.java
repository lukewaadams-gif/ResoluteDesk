package resolutedesk.executive;

/** Traits and backgrounds of the people who work for the president. */
public final class Traits {
    private Traits() {
    }

    // traits. Real people only ever get the first six.
    public static final int T_STEADY = 0, T_EXPERT = 1, T_LOYAL = 2, T_WORKHORSE = 3, T_DEALMAKER = 4, T_MEDIA = 5, T_LEAKER = 6, T_ABRASIVE = 7, T_AMBITIOUS = 8, T_RISK = 9, NTRAIT = 10;

    public static final String[] TRAIT = {"Steady", "Expert", "Loyalist", "Workhorse", "Dealmaker", "Media-savvy", "Talks to reporters", "Abrasive", "Ambitious", "Cuts corners"};

    public static final String[] TRAIT_DESC = {"Calm under pressure; rarely makes news.", "Deep knowledge of the job: better results, easier confirmation.", "Loyal to you above all. Rarely leaks, rarely resigns.",
            "Long hours: the agency runs better.", "Good at cutting deals with Congress.", "Good on television.", "Leaks to the press when unhappy.",
            "Clashes with colleagues: other staff are less happy.", "Has plans of their own; may leave to run for office.", "Loose with the rules. Ethics stories are more likely."};

    public static final String[] BACKGROUNDS = {"Former governor", "Senator", "House member", "Business executive", "Retired general", "Law professor", "Career civil servant",
            "Former ambassador", "Mayor", "Campaign veteran", "Think-tank scholar", "State attorney general", "Federal judge", "Economist", "Lobbyist"};
}
