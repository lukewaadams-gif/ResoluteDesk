package resolutedesk.stories;

import java.io.Serializable;

/** One storyline under way: how far it has gone, how hot it is, and how it ended. */
public final class Storyline implements Serializable {
    private static final long serialVersionUID = 1L;
    public int def;                                        // its row in StorylineTable
    public int stage;                                      // 0 is the first stage
    public double heat;                                    // 0-100: how much it dominates the news; 60 and up, it grows
    public int status;                                     // SL_* in StoryData
    public int stageWeek, started, ended = -1;
    public int lingers;                                    // checks at the end of a stage that neither grew nor faded
    public String who;                                     // the made-up figure at its center
    public int state;                                      // the state where it starts
    public int choices;                                    // decisions made about it
}
