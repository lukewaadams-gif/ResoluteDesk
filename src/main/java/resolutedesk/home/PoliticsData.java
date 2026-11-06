package resolutedesk.home;

/** Party factions and the opposition's strategies. */
public final class PoliticsData {
    private PoliticsData() {
    }

    public static final String[] FAC_D = {"Progressives", "Moderates"}, FAC_R = {"MAGA populists", "Traditional conservatives"};

    public static final double[] FAC_E_D = {-0.7, -0.2}, FAC_C_D = {-0.6, -0.2}, FAC_E_R = {0.3, 0.7}, FAC_C_R = {0.8, 0.4};

    public static final String[] OPP_STRAT = {"Cooperate where they can", "Pick their fights", "Obstruct everything"};
}
