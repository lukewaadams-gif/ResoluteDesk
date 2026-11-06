package resolutedesk.press;

/** News outlets of each era, their audiences and their slant. */
public final class Outlets {
    private Outlets() {
    }

    // ---- the media ----
    public static final String[] OUT_NAME = {"The New York Times", "The Washington Post", "The Wall Street Journal", "The Associated Press", "Reuters", "CNN", "MSNBC", "Fox News", "NBC News",
            "CBS News", "ABC News", "Politico", "Newsmax", "NPR", "The Joe Rogan Experience", "Pod Save America", "The Ben Shapiro Show", "60 Minutes"};

    public static final double[] OUT_LEAN = {-0.5, -0.5, 0.2, 0, 0, -0.4, -0.8, 0.8, -0.2, -0.1, -0.2, -0.1, 0.9, -0.4, 0.3, -0.8, 0.8, -0.1};

    public static final double[] OUT_AUD = {10, 6, 4, 20, 10, 5, 3, 6, 8, 7, 8, 3, 2, 8, 15, 2, 3, 9};

    public static final boolean[] OUT_SIT = {false, false, false, false, false, true, true, true, true, true, true, false, true, false, true, true, true, true};   // takes sit-down interviews

    // ---- the press corps of each era: name|lean|audience (millions)|takes sit-down interviews ----------------------
    public static final String[] PRESS_1933 = {"The New York Times|-0.2|1|n", "The Washington Post|-0.1|0.3|n", "The Wall Street Journal|0.4|0.2|n", "The Associated Press|0|20|n", "United Press|0|15|n",
            "Time|0.3|3|y", "The Nation|-0.8|0.1|n", "The Chicago Tribune|0.8|1|n", "NBC Radio|0|25|y", "CBS Radio|-0.1|25|y", "Mutual Radio|0.2|10|y", "Life|0.3|5|n", "The Hearst papers|0.7|8|n",
            "Newsweek|0|2|n", "The Saturday Evening Post|0.4|4|y", "The New Republic|-0.7|0.1|n", "Reader's Digest|0.4|8|y", "The March of Time newsreel|0|20|y"};

    public static final String[] PRESS_1953 = {"The New York Times|-0.3|1.5|n", "The Washington Post|-0.3|0.6|n", "The Wall Street Journal|0.4|1|n", "The Associated Press|0|30|n", "United Press International|0|20|n",
            "Time|0.2|5|y", "Newsweek|-0.1|4|y", "U.S. News and World Report|0.4|2|n", "NBC News|-0.1|20|y", "CBS News|-0.2|22|y", "ABC News|0|12|y", "The Los Angeles Times|0.1|1|n", "National Review|0.8|0.2|n",
            "The Christian Science Monitor|0|0.3|n", "The Tonight Show|0|12|y", "The New Republic|-0.6|0.2|n", "Human Events|0.9|0.1|n", "Meet the Press|0|6|y"};

    public static final String[] PRESS_1981 = {"The New York Times|-0.4|1.5|n", "The Washington Post|-0.4|0.8|n", "The Wall Street Journal|0.3|2|n", "The Associated Press|0|30|n", "Reuters|0|10|n",
            "CNN|-0.1|4|y", "Time|0|4|y", "Newsweek|-0.1|3|y", "NBC News|-0.2|15|y", "CBS News|-0.2|15|y", "ABC News|-0.1|14|y", "USA Today|0|5|n", "Conservative talk radio|0.9|15|y",
            "NPR|-0.3|8|y", "Late-night television|0|10|y", "The New Republic|-0.5|0.2|n", "National Review|0.8|0.3|n", "60 Minutes|-0.1|25|y"};

    public static final String[] PRESS_1997 = {"The New York Times|-0.5|1.5|n", "The Washington Post|-0.5|0.8|n", "The Wall Street Journal|0.3|2|n", "The Associated Press|0|30|n", "Reuters|0|10|n",
            "CNN|-0.2|3|y", "MSNBC|-0.4|1|y", "Fox News|0.8|2|y", "NBC News|-0.2|11|y", "CBS News|-0.2|9|y", "ABC News|-0.1|10|y", "The Drudge Report|0.6|3|n", "Conservative talk radio|0.9|15|y",
            "NPR|-0.4|9|y", "Late-night television|0|8|y", "The Daily Show|-0.6|1.5|y", "The Weekly Standard|0.7|0.2|n", "60 Minutes|-0.2|15|y"};

    public static final String[] OUT_NAME0 = OUT_NAME.clone();

    public static final double[] OUT_LEAN0 = OUT_LEAN.clone(), OUT_AUD0 = OUT_AUD.clone();

    public static final boolean[] OUT_SIT0 = OUT_SIT.clone();
}
