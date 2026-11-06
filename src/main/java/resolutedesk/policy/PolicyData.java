package resolutedesk.policy;

import static resolutedesk.opinion.Issues.*;

/**
 * The policies of the United States government, in 12 areas. Each is a setting with two to five levels; the law
 * in force on the first day of a game is the level each policy really had that year.
 *
 * <p>The effects are the game's estimates of what one step up does once it has fully worked through, drawn from
 * the research where there is research. Each carries a rating of how sure anyone can be, and the impacts panel
 * shows them as ranges.
 */
public final class PolicyData {
    private PolicyData() {
    }

    public static final int A_TAX = 0, A_NET = 1, A_JOBS = 2, A_HEALTH = 3, A_HOUSING = 4, A_ENERGY = 5, A_IMMIG = 6, A_CRIME = 7, A_EDU = 8, A_RIGHTS = 9,
            A_TECH = 10, A_DEFENSE = 11, NAREA = 12;
    public static final String[] AREA_NAME = {"Taxes and the budget", "The safety net", "Jobs and work", "Health care", "Housing and transportation",
            "Energy and the environment", "Immigration", "Crime and justice", "Education", "Rights and democracy", "Technology and business", "Defense and trade"};
    /** The issue voters judge each area by. */
    public static final int[] AREA_ISSUE = {PI_ECON, PI_PRICES, PI_JOBS, PI_HEALTH, PI_PRICES, PI_ENERGY, PI_IMMIG, PI_CRIME, PI_EDU, PI_RIGHTS, PI_TECH, PI_SECURITY};

    /** How a change can be made: a law, an executive order, an agency rule or the annual budget. */
    public static final int CH_BILL = 0, CH_ORDER = 1, CH_RULE = 2, CH_BUDGET = 3, NCH = 4;
    public static final String[] CH_NAME = {"A bill in Congress", "An executive order", "An agency rule", "The budget"};
    /** How much of a change's effect each channel delivers: an order is the easiest to undo and the weakest. */
    public static final double[] CH_STRENGTH = {1, 0.6, 0.85, 1};

    /** How sure the evidence is. */
    public static final String[] EV_NAME = {"Strong", "Moderate", "Weak", "Contested"};
    public static final String[] EV_DESC = {"Most studies agree on the size and direction.", "Studies agree on the direction more than the size.",
            "Little hard evidence; this is closer to a judgment call.", "Economists and researchers disagree, sometimes on the direction."};
    /** How wide the range around each estimate is, as a share of it, by evidence rating. */
    public static final double[] EV_SPREAD = {0.25, 0.5, 0.8, 1.0};

}
