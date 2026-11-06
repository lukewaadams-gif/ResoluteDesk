package resolutedesk.executive;

import static resolutedesk.opinion.Issues.*;

/** White House staff and cabinet posts, and which existed in each era. */
public final class Posts {
    private Posts() {
    }

    //  In 2025 the real cabinet and senior staff are the default picks. Everyone else is fictional,
    //  and only fictional people are ever caught up in personal misconduct.
    // the White House staff (no Senate confirmation)
    public static final int S_COS = 0, S_NSA = 1, S_PRESS = 2, S_COUNSEL = 3, S_NEC = 4, S_DPC = 5, S_LEG = 6, S_POL = 7, S_COMMS = 8, NSTAFF = 9;

    public static final String[] STAFF_TITLE = {"Chief of staff", "National security adviser", "Press secretary", "White House counsel", "Economic adviser",
            "Domestic policy adviser", "Legislative director", "Political director", "Communications director"};

    public static final String[] STAFF_DOES = {
            "Runs the building and your schedule. A good one lowers your stress and keeps the staff from leaking.",
            "Coordinates foreign policy and the Situation Room.",
            "Briefs the press every day. A good one takes the sting out of bad stories.",
            "Your lawyer for the office. Keeps orders on solid legal ground and handles investigations.",
            "Runs the National Economic Council. Sharpens your economic message.",
            "Runs the Domestic Policy Council. Makes your policy time count for more.",
            "Your link to Congress. Makes deals stick and keeps relationships warm.",
            "Watches the map and the money. Helps your fundraising and the midterms.",
            "Plans your message. Makes your remarks and speeches land harder."};

    // the cabinet: 15 departments and 6 cabinet-level posts, all confirmed by the Senate
    public static final int P_STATE = 0, P_TREAS = 1, P_DEF = 2, P_DOJ = 3, P_INT = 4, P_AG = 5, P_COM = 6, P_LAB = 7, P_HHS = 8, P_HUD = 9,
            P_DOT = 10, P_DOE = 11, P_ED = 12, P_VA = 13, P_DHS = 14, P_EPA = 15, P_OMB = 16, P_USTR = 17, P_UN = 18, P_DNI = 19, P_CIA = 20, NPOST = 21;

    public static final String[] POST_NAME = {"State", "Treasury", "Defense", "Justice", "Interior", "Agriculture", "Commerce", "Labor", "Health and Human Services",
            "Housing and Urban Development", "Transportation", "Energy", "Education", "Veterans Affairs", "Homeland Security",
            "Environmental Protection Agency", "Office of Management and Budget", "Trade Representative", "Ambassador to the UN", "Director of National Intelligence", "CIA"};

    public static final String[] POST_SHORT = {"State", "Treasury", "Defense", "Justice", "Interior", "Agriculture", "Commerce", "Labor", "HHS", "HUD",
            "Transportation", "Energy", "Education", "VA", "Homeland Security", "EPA", "OMB", "USTR", "UN", "DNI", "CIA"};

    public static final String[] POST_TITLE = {"Secretary of State", "Treasury secretary", "Defense secretary", "Attorney general", "Interior secretary", "Agriculture secretary",
            "Commerce secretary", "Labor secretary", "Health secretary", "Housing secretary", "Transportation secretary", "Energy secretary", "Education secretary",
            "Veterans Affairs secretary", "Homeland Security secretary", "EPA administrator", "Budget director", "Trade representative", "UN ambassador",
            "Director of national intelligence", "CIA director"};

    /** Civilian employees, thousands. */
    public static final double[] POST_WF = {77, 100, 770, 115, 70, 100, 47, 15, 90, 8, 55, 15, 4, 470, 260, 16, 0.5, 0.2, 0.2, 2, 21};

    public static final int[] POST_AREA = {PI_FOREIGN, PI_ECON, PI_SECURITY, PI_CRIME, PI_ENERGY, PI_ECON, PI_TECH, PI_JOBS, PI_HEALTH, PI_PRICES,
            PI_JOBS, PI_ENERGY, PI_EDU, PI_SECURITY, PI_IMMIG, PI_ENERGY, PI_ECON, PI_TRADE, PI_FOREIGN, PI_SECURITY, PI_SECURITY};

    /** The department that carries out policy on each issue. */
    public static final int[] AREA_POST = {P_TREAS, P_COM, P_LAB, P_HHS, P_DHS, P_DOJ, P_STATE, P_DEF, P_USTR, P_DOE, P_DOJ, P_COM, P_ED, P_DOJ};

    /** Lower-cases a job title for use mid-sentence, keeping acronyms: "CIA director", "UN ambassador". */
    public static String lcTitle(String t) {
        StringBuilder b = new StringBuilder();
        for (String w : t.split(" ")) { if (b.length() > 0) b.append(' '); b.append(w.length() > 1 && w.equals(w.toUpperCase()) ? w : w.toLowerCase()); }
        return b.toString();
    }

    /** Who speaks for each adviser label in decisions: staff roles, or 100 + a cabinet post. */
    public static final String[] ADV_LABEL = {"Chief of staff", "National security adviser", "Press secretary", "Counsel", "Economic adviser", "Domestic policy adviser", "Legislative director",
            "Political director", "Communications director", "Budget director", "Treasury secretary", "Attorney general", "Defense secretary", "Secretary of State",
            "Homeland Security secretary", "Director of national intelligence", "Commerce secretary", "Energy secretary", "Labor secretary", "Transportation secretary",
            "EPA administrator", "Education secretary", "Health secretary", "Trade representative"};

    public static final int[] ADV_WHO = {S_COS, S_NSA, S_PRESS, S_COUNSEL, S_NEC, S_DPC, S_LEG, S_POL, S_COMMS, 100 + P_OMB, 100 + P_TREAS, 100 + P_DOJ, 100 + P_DEF, 100 + P_STATE,
            100 + P_DHS, 100 + P_DNI, 100 + P_COM, 100 + P_DOE, 100 + P_LAB, 100 + P_DOT,
            100 + P_EPA, 100 + P_ED, 100 + P_HHS, 100 + P_USTR};

    public static final String[] POST_NAME0 = POST_NAME.clone(), POST_SHORT0 = POST_SHORT.clone(), POST_TITLE0 = POST_TITLE.clone();

    /** The year each cabinet post first existed, and where its work was done before that. */
    public static final int[] POST_FROM = {0, 0, 0, 0, 0, 0, 0, 0, 1939, 1966, 1967, 1978, 1980, 0, 2003, 1971, 0, 1963, 1946, 2005, 1947};

    public static final int[] POST_PARENT = {-1, -1, -1, -1, -1, -1, -1, -1, P_LAB, P_COM, P_COM, P_INT, P_HHS, -1, P_DOJ, P_INT, -1, P_COM, P_STATE, P_CIA, P_DEF};

    /** Posts that don't exist yet this year (all exist in the modern game). */
    public static final boolean[] POST_OFF = new boolean[NPOST];

    public static boolean postOn(int k) { return !POST_OFF[k]; }

    /** The department that actually does a post's work this year. */
    public static int ep(int k) { while (POST_OFF[k] && POST_PARENT[k] >= 0) k = POST_PARENT[k]; return k; }

    public static void eraPosts(int year) {
        for (int k = 0; k < NPOST; k++) { POST_NAME[k] = POST_NAME0[k]; POST_SHORT[k] = POST_SHORT0[k]; POST_TITLE[k] = POST_TITLE0[k]; POST_OFF[k] = year < POST_FROM[k]; }
        if (year < 1947) { POST_NAME[P_DEF] = "War"; POST_SHORT[P_DEF] = "War"; POST_TITLE[P_DEF] = "Secretary of War"; }
        if (year < 1953) { POST_NAME[P_HHS] = "Federal Security Agency"; POST_SHORT[P_HHS] = "FSA"; POST_TITLE[P_HHS] = "Federal Security administrator"; }
        else if (year < 1980) { POST_NAME[P_HHS] = "Health, Education, and Welfare"; POST_SHORT[P_HHS] = "HEW"; POST_TITLE[P_HHS] = "HEW secretary"; }
        if (year < 1989) { POST_NAME[P_VA] = "Veterans Administration"; POST_TITLE[P_VA] = "Veterans administrator"; }
        if (year < 1970) { POST_NAME[P_OMB] = "Bureau of the Budget"; POST_SHORT[P_OMB] = "Budget"; }
        if (year < 1980) { POST_NAME[P_USTR] = "Special Trade Representative"; POST_SHORT[P_USTR] = "STR"; POST_TITLE[P_USTR] = "Special trade representative"; }
        if (year < 2005) POST_TITLE[P_CIA] = "Director of Central Intelligence";
    }
}
