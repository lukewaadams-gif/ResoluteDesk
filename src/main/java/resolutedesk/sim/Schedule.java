package resolutedesk.sim;

/** The president's weekly calendar: slots and the activities that fill them. */
public final class Schedule {
    private Schedule() {
    }

    // ---- the president's week: seven days of three slots -----------------------------------------------------
    public static final int NSLOT = 21;

    public static final String[] SLOT = {"Morning", "Afternoon", "Evening"};

    public static final int AC_FREE = 0, AC_REST = 1, AC_EXERCISE = 2, AC_FAMILY = 3, AC_POLICY = 4, AC_REMARKS = 5, AC_TRIP = 6, AC_FUNDRAISER = 7,
            AC_CEREMONY = 8, AC_CONGRESS = 9, AC_CAMPDAVID = 10, AC_WORKLATE = 11, AC_TRAVEL = 12, AC_CAMPAIGN = 13, AC_CABINET = 14, AC_ABROAD = 15, NAC = 16;

    public static final String[] AC_NAME = {"Open time", "Rest", "Exercise", "Family time", "Policy time", "White House remarks", "Trip", "Fundraiser",
            "Ceremony", "Hill meetings", "Camp David", "Work late", "Travel", "Campaign stop", "Cabinet meeting", "Trip abroad"};

    public static final String[] AC_DESC = {
            "Nothing scheduled. Staff fill it with calls and paperwork.",
            "Sleep in or nap. Restores stamina and lowers stress.",
            "A workout. Restores a little stamina, lowers stress and helps your health over time.",
            "Time with your family. Lowers stress a lot; the public likes seeing it.",
            "Meetings with your policy staff on one issue. Builds your command of it and your standing on it.",
            "A speech at the White House on one issue. Moves the news and your standing on that issue.",
            "Fly to a state for an event. Lifts your standing there and nationally, but long flights are tiring.",
            "Raise money for your party. Fills the war chest; too many look bad.",
            "Bill signings, medals, championship teams. A small, safe lift.",
            "Meet members of Congress. Improves your relationships on the Hill.",
            "A weekend at Camp David. Big stamina boost, but bad optics in a crisis.",
            "Stay at your desk into the night. Gets work done but costs sleep.",
            "In the air.",
            "Rally in a state: for your party's candidates before the midterms, or for your own ticket in the fall of an election year.",
            "Meet the whole cabinet. Lifts their morale and their departments' readiness.",
            "Fly abroad to meet a foreign leader. A big lift in relations, but long flights and jet lag are hard on stamina."};

    public static final double[] AC_STAMINA = {0, 6, 2.5, 3, -2.5, -3, -5, -3.5, -1.5, -3, 8, -4.5, -2, -5, -2, -4};

    public static final double[] AC_STRESS = {0, -1.2, -1.2, -1.8, 0.6, 0.6, 0.8, 0.6, -0.3, 0.8, -2.5, 1.5, 0.5, 0.8, 0.3, 1.0};

    public static boolean TRACE;                   // the test harness prints every headline

    public static boolean isTrip(int a) { return a == AC_TRIP || a == AC_CAMPAIGN; }

    public static final boolean[] AC_ISSUE = {false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false};

    public static final boolean[] AC_STATE = {false, false, false, false, false, false, true, false, false, false, false, false, false, true, false, false};
}
