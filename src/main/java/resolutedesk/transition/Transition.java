package resolutedesk.transition;

import static resolutedesk.transition.TransitionData.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** The weeks from Election Day to the inauguration, while the president-elect builds a government. */
public final class Transition implements Serializable {
    private static final long serialVersionUID = 1L;
    public int week, weeks;                          // weeks gone, and the whole transition
    public int startDay;                             // the game day the transition begins (before day 0, the inauguration)
    public double hoursUsed;                         // the team's hours spent this week
    public List<Prospect> prospects = new ArrayList<Prospect>();
    public Prospect[] pick = new Prospect[NJOB];     // the announced choice for each job
    public int[] namedWeek = new int[NJOB];          // the week each pick was announced
    public List<String> news = new ArrayList<String>();      // what the press reports
    public List<String> notes = new ArrayList<String>();     // what only the team knows
    public int leaks, surprises;                     // stories the team didn't want, and problems found too late
    public boolean done;
    public Random rng;
}
