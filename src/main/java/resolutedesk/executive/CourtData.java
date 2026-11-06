package resolutedesk.executive;

/** Lawsuits, the stages they pass through, and who brings them. */
public final class CourtData {
    private CourtData() {
    }

    public static final String[] SCOTUS25 = {"John Roberts|1955|R|0.3", "Clarence Thomas|1948|R|0.9", "Samuel Alito|1950|R|0.85", "Sonia Sotomayor|1954|D|-0.8",
            "Elena Kagan|1960|D|-0.6", "Neil Gorsuch|1967|R|0.6", "Brett Kavanaugh|1965|R|0.45", "Amy Coney Barrett|1972|R|0.5", "Ketanji Brown Jackson|1970|D|-0.85"};

    public static final int CS_DISTRICT = 0, CS_CIRCUIT = 1, CS_SCOTUS = 2, CS_DONE = 3;

    public static final String[] CS_STAGE = {"District court", "Court of appeals", "Supreme Court", "Decided"};

    public static final String[] PLAINTIFFS = {"A coalition of states", "The ACLU", "A business group", "Twenty state attorneys general", "A labor union", "An advocacy group", "A trade association", "Three cities"};
}
