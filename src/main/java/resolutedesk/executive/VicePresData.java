package resolutedesk.executive;

/** Missions a president can give the vice president. */
public final class VicePresData {
    private VicePresData() {
    }

    // ---- the vice president ----
    public static final int VM_NONE = 0, VM_TOUR = 1, VM_TASK = 2, VM_SENATE = 3, VM_MONEY = 4, VM_ABROAD = 5, NVM = 6;

    public static final String[] VM_NAME = {"Stay in Washington", "Tour the swing states", "Lead a task force", "Work the Senate", "Raise money for the party", "Represent you abroad"};

    public static final String[] VM_DESC = {"Sits in on meetings and stands by to break ties in the Senate.", "Six weeks of rallies and town halls in the battleground states.",
            "Eight weeks leading the administration's work on one issue.", "Lobbies senators personally. Improves your relationships in the Senate.",
            "Six weeks of fundraisers for the party's candidates.", "Four weeks of foreign trips to reassure allies."};

    public static final int[] VM_WEEKS = {0, 6, 8, 0, 6, 4};
}
