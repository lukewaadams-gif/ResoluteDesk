package resolutedesk.transition;

import static resolutedesk.executive.Posts.*;

/**
 * The text and numbers of the transition: who the prospects are, what they're good and bad at, what a background
 * check can turn up, and what happens in the weeks before the inauguration. Every prospect described here is
 * fictional; in 2025 the real cabinet appears as one choice for each job, and only with its strengths.
 */
public final class TransitionData {
    private TransitionData() {
    }

    /** Jobs: the cabinet posts, then the White House staff. */
    public static final int JOB_STAFF = NPOST, NJOB = NPOST + NSTAFF;

    /** The kinds of prospect a transition team puts forward. */
    public static final int PK_EXPERT = 0, PK_LOYAL = 1, PK_POL = 2, PK_OUTSIDER = 3, PK_CROSS = 4, PK_REAL = 5;
    public static final String[] PK_NAME = {"The expert", "The loyalist", "The politician", "The outsider", "The crossover pick", "The real nominee"};

    /** Fields of work, which pick the profiles that fit a job. */
    public static final int DOM_DIPLO = 0, DOM_ECON = 1, DOM_SECURITY = 2, DOM_LAW = 3, DOM_DOMESTIC = 4, DOM_POLITICS = 5;

    /** The field of each cabinet post (in post order), then of each staff role. */
    static final int[] POST_DOM = {DOM_DIPLO, DOM_ECON, DOM_SECURITY, DOM_LAW, DOM_DOMESTIC, DOM_DOMESTIC, DOM_ECON, DOM_DOMESTIC, DOM_DOMESTIC, DOM_DOMESTIC,
            DOM_DOMESTIC, DOM_DOMESTIC, DOM_DOMESTIC, DOM_SECURITY, DOM_SECURITY, DOM_DOMESTIC, DOM_ECON, DOM_ECON, DOM_DIPLO, DOM_SECURITY, DOM_SECURITY};
    static final int[] STAFF_DOM = {DOM_POLITICS, DOM_SECURITY, DOM_POLITICS, DOM_LAW, DOM_ECON, DOM_DOMESTIC, DOM_POLITICS, DOM_POLITICS, DOM_POLITICS};

    public static int domain(int job) { return job < JOB_STAFF ? POST_DOM[job] : STAFF_DOM[job - JOB_STAFF]; }

    /**
     * Profiles by kind (rows: expert, loyalist, politician, outsider, crossover) and field (columns, in DOM order).
     * {S} is a state, {P} the president's party, {O} the other party.
     */
    static final String[][][] PROFILE = {
            {   // the expert
                    {"Career diplomat and former ambassador to three countries", "Deputy secretary of state in the last {P} administration"},
                    {"Former Federal Reserve governor", "Chief economist at a large bank", "Deputy Treasury secretary in the last {P} administration"},
                    {"Retired four-star general", "Former deputy defense secretary", "Twenty-year veteran of the intelligence agencies"},
                    {"Federal appeals judge", "Former U.S. attorney and law professor", "Former deputy attorney general"},
                    {"Ran the department's largest agency for six years", "State commissioner in {S} with a national reputation", "University president and former deputy secretary"},
                    {"Veteran of two administrations", "Former chief of staff to a cabinet secretary", "A Hill veteran who ran a committee staff"}},
            {   // the loyalist
                    {"Your campaign's foreign-policy adviser", "Your closest friend in politics, a former ambassador"},
                    {"Your campaign's chief economic adviser", "The investor who ran your campaign's finances"},
                    {"Your campaign's national-security adviser, a retired colonel", "A longtime aide who handled defense on your staff"},
                    {"Your personal lawyer for twenty years", "The campaign's general counsel"},
                    {"Your campaign's policy director", "A {P} donor and organizer from {S}"},
                    {"Your campaign manager", "Your longest-serving aide", "Ran your campaign in {S}"}},
            {   // the politician
                    {"Senator from {S} on the Foreign Relations Committee", "Two-term governor of {S} with a trade record abroad"},
                    {"Governor of {S} who balanced eight budgets", "Senator from {S} on the Finance Committee"},
                    {"Senator from {S} on the Armed Services Committee", "House member from {S} and former Marine officer"},
                    {"Attorney general of {S}", "Senator from {S} on the Judiciary Committee"},
                    {"Governor of {S}", "Mayor of the biggest city in {S}", "House member from {S} who chairs a subcommittee"},
                    {"Former House member from {S} with friends on both sides", "Lieutenant governor of {S}"}},
            {   // the outsider
                    {"Chief executive of a global company", "President of a big charity working abroad"},
                    {"Chief executive of a big manufacturer", "Founder of an investment firm"},
                    {"Retired admiral who now runs a defense company", "Former police commissioner of a big city"},
                    {"Partner at a top law firm", "Civil-rights lawyer with a national practice"},
                    {"Chief executive of a big hospital system", "Founder of a large company in {S}"},
                    {"Television news anchor", "Founder of a polling firm"}},
            {   // the crossover pick
                    {"A {O} former secretary of state's deputy", "A {O} senator, now retired, respected abroad"},
                    {"A {O} former budget director known for deficit deals", "An independent economist who advised both parties"},
                    {"A {O} former defense secretary's deputy", "A retired general who has never voted in a primary"},
                    {"A {O} former prosecutor known for independence", "A retired federal judge appointed by a {O} president"},
                    {"A {O} former governor of {S}", "A {O} mayor known for working across the aisle"},
                    {"A {O} veteran of the White House", "An independent strategist who has worked for both parties"}}};

    /** What a background check can turn up: what it is, how much it costs in the Senate, and how the story is told. */
    public static final String[] BAG_WHAT = {"unpaid taxes on consulting income", "lobbying for a foreign government", "an old essay calling for the department to be abolished",
            "stock trades in companies they would regulate", "a failed company that left investors short", "paid speeches to a Wall Street bank",
            "a household employee paid off the books", "years of inflammatory posts and remarks"};
    public static final double[] BAG_WEIGHT = {0.9, 1.4, 0.6, 1.1, 0.6, 0.5, 0.8, 1.0};
    /** The year each kind of problem could first come up (posts online are a modern thing). */
    static final int[] BAG_FROM = {0, 0, 0, 0, 0, 0, 0, 2005};

    /** Strengths, by what makes them true. */
    static final String S_SKILL = "A record of results", S_LOYAL = "Completely loyal to you", S_EXPERT = "Knows the job inside out", S_STEADY = "Steady under pressure",
            S_WORK = "Works around the clock", S_DEAL = "A dealmaker with Congress", S_MEDIA = "A natural on television", S_SENATE = "Knows the Senate; friends on both sides",
            S_EASY = "The other side likes them: an easy confirmation", S_UNITY = "A signal of unity", S_ORG = "Has run a large organization", S_FRESH = "Fresh eyes, no ties to Washington";
    /** Weaknesses: only fictional prospects have them. Hidden ones show only after an interview. */
    static final String W_THIN = "Thin experience for the job", W_PARTISAN = "Seen as a partisan by the other side", W_INDEP = "Little personal loyalty to you",
            W_BASE = "Your own base will grumble", W_AMBITION = "Has ambitions of their own", W_NEVER = "Has never worked in government", W_CONGRESS = "No feel for Congress",
            W_VOTES = "Old votes the other side will use", W_LEAKS = "Talks to reporters more than you'd like", W_ABRASIVE = "Abrasive; clashes with colleagues", W_CORNERS = "Loose with the rules";

    /** What each cabinet post does in the game, for the shortlist screen. */
    public static final String[] POST_DOES = {"Runs diplomacy. A strong secretary makes your calls, visits and summits count for more.",
            "Manages the government's money and its debt, and shapes how markets read you.", "Runs the military. The department's readiness decides how operations and wars go.",
            "The attorney general: prosecutions, the FBI, and the law behind your orders.", "Public lands, national parks and leases for drilling and mining.",
            "Farm aid, food aid and rural America.", "Trade enforcement, the census, industry and technology.", "Workers, wages, safety and job training.",
            "Health care, Medicare and Medicaid, and the public-health agencies.", "Housing and the cities.", "Roads, rails, airports and air traffic.",
            "The power grid, the nuclear weapons labs and energy research.", "Schools, student aid and civil rights in education.", "Care and benefits for veterans.",
            "The border, disaster relief through FEMA, and security at home.", "Rules for clean air and water, and the cleanup of polluted land.",
            "Writes your budget and reviews every new rule.", "Negotiates trade deals and carries out tariffs.", "Speaks for you at the United Nations.",
            "Oversees the intelligence agencies and your daily brief.", "Spies, analysis and covert action."};

    /** Hours of the transition team's week. */
    public static final double TR_HOURS = 8;
    /** Background checks the FBI runs at once, and the weeks each takes. */
    public static final int TR_VET_SLOTS = 4, TR_VET_WEEKS = 2;
}
