package resolutedesk.executive;

import static resolutedesk.executive.Posts.*;

/** Areas of work the president can hand to staff. */
public final class Delegation {
    private Delegation() {
    }

    // ---- delegation ----
    public static final int DG_SCHEDULE = 0, DG_WHIP = 1, DG_BUDGET = 2, DG_PARDONS = 3, DG_PEOPLE = 4, NDG = 5;

    public static final String[] DG_NAME = {"Your chief of staff plans your week", "Your legislative director works the votes", "Your budget director writes the budget",
            "Your counsel handles pardon requests", "Your chief of staff fills vacancies"};

    public static final String[] DG_DESC = {"Each new week starts with a balanced schedule, built around your top issue and your stamina.",
            "Spends your Hill time and the favors you're owed when one of your bills is short of votes.", "Sends a budget that follows your party's agenda when it's due.",
            "Grants the safe requests and turns down the risky ones.", "Picks replacements for anyone who leaves, and sends the Senate a nominee for any empty cabinet post."};

    public static final int[] DG_ROLE = {S_COS, S_LEG, -1, S_COUNSEL, S_COS};
}
