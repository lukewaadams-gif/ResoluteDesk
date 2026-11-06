package resolutedesk.world;

/** Wars: how they start, are fought and end. */
public final class WarData {
    private WarData() {
    }

    public static final String[] WAR_NAME = {"The war against Germany", "The war against Japan", "The Korean War", "The Vietnam War", "The Gulf War", "The war in Afghanistan", "The Iraq War", "The campaign against ISIS"};

    public static final String[] WAR_ID = {"ww2e", "ww2p", "korea", "vietnam", "gulf", "afghan", "iraq", "isis"};

    /** enemy|ally|troops needed to hold (thousands)|drift|deaths per 1,000 troops a week|cost (% of GDP per 1,000 troops)|starting support|stakes */
    public static final String[] WAR_SPEC = {"DE|GB|5000|0.0|0.18|0.0028|92|3", "JP|AU|3500|0.0|0.22|0.0028|92|3", "KP|KR|300|0.0|0.6|0.02|65|1.2", "-|VN|550|-0.06|0.5|0.006|60|0.8",
            "IQ|SA|450|0.7|0.05|0.002|75|1.5", "-|-|90|-0.02|0.04|0.012|88|1.5", "-|IQ|160|-0.05|0.07|0.007|70|0.9", "-|IQ|6|0.15|0.05|0.04|72|1.2"};

    public static final String[] STRATEGY = {"Hold the line", "Balanced", "On the offensive", "Seek a settlement"};

    /** Where a war stands. Wars of choice can also end on a timetable, by handing over, or by declaring victory. */
    public static final int WS_NONE = 0, WS_FIGHTING = 2, WS_WON = 3, WS_LOST = 4, WS_AGREED = 5, WS_TIMETABLE = 6, WS_HANDED = 7, WS_DECLARED = 8;
    public static final String[] WAR_STAGE = {"Not begun", "", "Fighting", "Won", "Lost", "Ended by agreement", "Withdrawn on a timetable", "Handed over", "Victory declared"};

    /** The kinds of war a president can start: strikes from the air and sea, a blockade, a no-fly zone, an invasion, and the occupation that follows. */
    public static final int WK_STRIKES = 0, WK_BLOCKADE = 1, WK_NOFLY = 2, WK_INVASION = 3, WK_OCCUPATION = 4;
    public static final String[] WK_NAME = {"Air and missile strikes", "A naval blockade", "A no-fly zone", "An invasion", "An occupation"};
    public static final String[] WK_DESC = {"Strike military targets from the air and sea. Few American troops at risk; the enemy will look for ways to hit back.",
            "Stop ships going in or out until the government gives in. An act of war: it squeezes the economy, and the price of anything they export.",
            "Keep the government's aircraft out of the sky to protect civilians. Cheap, but pilots can be shot down, and it can drag you in deeper.",
            "Send in ground forces to remove the government. If it falls, you own what comes next: an occupation, and very likely an insurgency.",
            "Hold the country while a new government takes over. Insurgents fight on; casualties mount; support at home wears thin."};
    /**
     * Front-line progress a week at full strength against an enemy of no military power (it falls as their power rises;
     * an occupation loses ground instead); deaths per 1,000 troops a week; cost (% of GDP per 1,000 troops a year).
     */
    public static final double[] WK_PACE = {4, 2.5, 1.5, 26, -0.06}, WK_LETHAL = {0.01, 0.005, 0.012, 0.08, 0.05}, WK_COST = {0.01, 0.008, 0.01, 0.007, 0.007};
    public static final double[] WK_FRONT = {50, 40, 55, 30, 50};

    /** The wars under way on inauguration day: "year|id:front:troops:support:talks ...". */
    public static final String[] WAR_START = {"1945|ww2e:94:5500:88:0 ww2p:70:3500:90:0", "1953|korea:50:300:40:60", "1965|vietnam:45:23:70:0", "1969|vietnam:45:536:38:20",
            "1973|vietnam:50:24:30:92", "2005|iraq:45:150:45:0 afghan:60:20:70:0", "2009|iraq:62:140:35:60 afghan:48:34:55:0", "2013|afghan:50:66:35:30",
            "2017|afghan:50:8.4:40:10 isis:55:5:70:0", "2021|afghan:45:2.5:40:80"};
}
