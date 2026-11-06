package resolutedesk.model;

/** The 22 ratings that describe a politician. */
public final class Attributes {
    private Attributes() {
    }

    // ---- candidate attributes (0-100) -------------------------------------------------------------------
    public static final int A_CHA = 0, A_POD = 1, A_SPE = 2, A_DEB = 3, A_INT = 4, A_DIS = 5, A_RET = 6, A_SMD = 7, A_BIG = 8,
            A_SELF = 9, A_VOL = 10, A_VIR = 11, A_AUT = 12, A_STA = 13, A_SCA = 14, A_EXE = 15, A_FPC = 16, A_XOV = 17,
            A_EST = 18, A_HOM = 19, A_BAS = 20, NA = 21;

    public static final String[] ATTR = {"Charisma", "Long-form / podcasts", "Speeches", "Debating", "Intellect & policy depth",
            "Message discipline", "Retail politics", "Small-dollar fundraising", "Big-donor network", "Self-funding",
            "Volunteer energy", "Media virality", "Authenticity", "Stamina", "Scandal baggage", "Executive record",
            "Foreign-policy credibility", "Crossover appeal", "Establishment ties", "Home-state strength", "Base fire"};

    public static final String[] ATTR_SHORT = {"CHA", "POD", "SPE", "DEB", "INT", "DIS", "RET", "SMD", "BIG", "SELF", "VOL", "VIR",
            "AUT", "STA", "SCA", "EXE", "FPC", "XOV", "EST", "HOM", "BASE"};

    public static final String[] ATTR_DESC = {
            "Presence and likeability on stage and on screen.",
            "How well the candidate does in long, unscripted interviews and podcasts - where young men now get their news.",
            "Set-piece oratory: rallies, conventions, big moments.",
            "Head-to-head debating under pressure.",
            "Command of policy and quickness of mind.",
            "Staying on message; low gaffe risk.",
            "Handshakes, diners and town halls - the Iowa and New Hampshire arts.",
            "Ability to raise from millions of small online donors.",
            "Access to bundlers and high-dollar donors.",
            "Personal fortune that can be spent on the race.",
            "Ability to inspire volunteers to knock doors and make calls.",
            "Ability to go viral and dominate the attention economy.",
            "How genuine the candidate seems. High authenticity blunts flip-flop attacks.",
            "Ability to keep a punishing schedule without mistakes.",
            "Hidden risk of damaging revelations (higher is worse).",
            "Governing record: running a state, a department or a business.",
            "Credibility as a potential commander-in-chief.",
            "Appeal to the other party's voters and independents.",
            "Ties to party leaders, donors and endorsers.",
            "Strength at home - matters most if the home state is competitive.",
            "How strongly the party's activist base sees the candidate as a fighter."};
}
