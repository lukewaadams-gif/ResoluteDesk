package resolutedesk.stories;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** The news beyond the White House: which of the era's stories have run, and the storylines under way. */
public final class StoryState implements Serializable {
    private static final long serialVersionUID = 1L;
    public boolean[] used;                                 // each ambient story runs once a presidency
    public List<Storyline> lines = new ArrayList<Storyline>();
    public int[] cool;                                     // weeks until each storyline can start again
    public int ran;                                        // ambient stories run so far
}
