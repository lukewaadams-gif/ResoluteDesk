package resolutedesk.transition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import resolutedesk.executive.Person;

/** Someone on the shortlist for a job in the new administration. */
public final class Prospect implements Serializable {
    private static final long serialVersionUID = 1L;
    public Person person;                            // who they would be in the job
    public int job, kind;                            // the job (see TransitionData), and what kind of pick they are
    public String profile;                           // who they are, in a line
    public List<String> strengths = new ArrayList<String>(), weaknesses = new ArrayList<String>();
    public List<String> hidden = new ArrayList<String>();    // weaknesses an interview brings out
    public double skillSeen, loyaltySeen;            // the team's rough read before an interview
    public double guessOff;                          // how far off the team's early Senate count is
    public boolean interviewed, floated, leaked;
    public int vetDue = -1;                          // the week the background check is done, or -1 if none was ordered
    public double senate;                            // goodwill from meeting senators (logit points)
}
