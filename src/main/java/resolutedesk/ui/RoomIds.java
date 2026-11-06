package resolutedesk.ui;

/** The rooms of the White House and what each holds. */
public final class RoomIds {
    private RoomIds() {
    }

    //  The home screen: click the desk, the windows, the doors, the globe or the papers to go where the work is.
    public static final int R_DESK = 0, R_COUNTRY = 1, R_ECON = 2, R_RESIDENCE = 3, R_WESTWING = 4, R_CONGRESS = 5, R_SITROOM = 6, R_PRESS = 7, R_POLICY = 8, NROOM = 9;

    public static final String[] ROOM_NAME = {"The Resolute Desk", "The Country", "The Economy", "The Residence", "The West Wing", "The Roosevelt Room", "The Situation Room", "The Press Briefing Room", "The Cabinet Room"};

    public static final String[] ROOM_SUB = {"Your week, slot by slot", "Approval, by issue, group and state", "Jobs, prices, markets and the budget", "Your health, your sleep and your family",
            "Your staff and your cabinet", "Congress: the leaders, the votes, the budget", "The world: allies, rivals and crises", "The press corps and the media", "Policy: the law, its effects, and how it all connects"};

    public static final boolean[] ROOM_OPEN = {true, true, true, true, true, true, true, true, true};

    public static final String[] ROOM_LATER = {"", "", "", "",
            "Your chief of staff, national security adviser, press secretary and counsel, each with skills, traits and loyalty; all 15 departments and their secretaries; cabinet meetings; leaks and resignations; and your vice president's missions.",
            "Named leaders, committee chairs and swing votes; bills you negotiate provision by provision; the filibuster, reconciliation and the Byrd rule; your budget, the 12 spending bills, shutdowns, the debt ceiling and vetoes.",
            "About forty countries and their leaders; calls, summits, state visits, treaties, aid and sanctions; operations planned with the Pentagon; covert action; alliances; and the crises that start overseas.",
            "Daily briefings by your press secretary, press conferences with named reporters, interviews, your own social media, the State of the Union and addresses to the nation."};
}
