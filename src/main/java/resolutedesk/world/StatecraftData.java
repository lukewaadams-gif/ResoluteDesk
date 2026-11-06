package resolutedesk.world;

/** The settings of each foreign-policy tool: aid and arms levels, troop levels, ties, and the covert programs. */
public final class StatecraftData {
    private StatecraftData() {
    }

    public static final String[] MIL_AID = {"None", "Some", "Substantial", "Major"};
    public static final String[] ARMS = {"None", "Approved", "A major package"};
    public static final String[] TIES = {"Severed", "Downgraded", "Full"};
    /** Troop levels a president can base in a country, thousands. */
    public static final double[] TROOP_STEP = {0, 2, 5, 20, 60};
    public static final String[] TROOP_NAME = {"None", "Advisers (2,000)", "A brigade (5,000)", "A division (20,000)", "A corps (60,000)"};
    /** Covert programs: name, odds, chance of leaking, weeks to play out. */
    public static final int CV2_INFLUENCE = 0, CV2_CYBER = 1, CV2_ARM = 2, CV2_COUP = 3;
    public static final String[] CV2_NAME = {"An influence campaign", "Cyber sabotage", "Arm the opposition", "Regime change"};
    public static final String[] CV2_DESC = {"Money and messages for friendly parties and press.", "Disable military or nuclear systems from a distance.",
            "Guns and training for armed groups fighting the government.", "Help the opposition and the army bring the government down."};
    public static final double[] CV2_ODDS = {0.6, 0.55, 0.45, 0.25}, CV2_LEAK = {0.2, 0.2, 0.35, 0.45};
    public static final int[] CV2_WEEKS = {8, 6, 12, 16};
}
