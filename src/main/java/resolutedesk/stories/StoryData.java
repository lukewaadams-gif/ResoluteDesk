package resolutedesk.stories;

/** Constants for the era's news and the storylines that grow out of it. */
public final class StoryData {
    private StoryData() {
    }

    /** Sections of the paper, by the letter each ambient story is filed under. */
    public static final String SECTION_CODES = "PEWSCXLNBHDT";
    public static final String[] SECTION = {"Politics", "The economy", "The world", "Science", "Culture", "Sports", "Crime and the courts", "Weather and the land",
            "Business", "Health", "Daily life", "Technology"};

    /** A storyline is under way, has faded, has run away with the presidency, or was brought to an end. */
    public static final int SL_ACTIVE = 0, SL_FADED = 1, SL_RUNAWAY = 2, SL_ENDED = 3;
    public static final String[] SL_STATUS = {"Developing", "Faded", "Took over the presidency", "Brought to an end"};

    /** Heat at the end of a stage: at or above this the story grows; at or below the other it fades. */
    public static final double SL_GROW = 60, SL_FADE = 30;
    /** Heat a storyline starts with, and how much it cools each week on its own. */
    public static final double SL_START = 52, SL_COOL = 0.5;
    /** Heat a gamble adds when it fails, on top of the option's own: a botched response is news in itself. */
    public static final double SL_BACKFIRE = 6;
    /** Weeks before a storyline that has ended can start again, and how many can run at once. */
    public static final int SL_REST = 104, SL_MAX = 2;
}
