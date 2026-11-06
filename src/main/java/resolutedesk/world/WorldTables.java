package resolutedesk.world;

/** Postures, tariffs, summits and the long-running flashpoints. */
public final class WorldTables {
    private WorldTables() {
    }

    //  Relations drift toward what your policies earn. Six hot spots evolve week by week and throw up crises.
    //  Crises, operations and covert actions are tables of options whose odds depend on your people and your posture.
    public static final String[] TW_STAGE = {"Calm", "Pressure campaign", "Large-scale drills", "Customs quarantine", "Blockade", "War"};

    public static final String[] UA_STAGE = {"Peace agreement", "Ceasefire", "Full-scale war"};

    public static final String[] POSTURE_AREA = {"The Pacific", "Europe", "The Middle East"};

    public static final String[] POSTURE = {"Light", "Normal", "Heavy", "Surge"};

    public static final String[] TARIFF_OPT = {"0%", "10%", "25%", "50%"};

    public static final int[] TARIFF_VAL = {0, 10, 25, 50};

    public static final String[] LEVEL = {"None", "Light", "Heavy", "Maximum"};

    public static final String[] SUMMITS = {"G7|6|14", "NATO|7|9", "UN General Assembly|9|23", "G20|11|18"};
}
