package resolutedesk.home;

import static resolutedesk.home.EventData.*;
import static resolutedesk.press.Outlets.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** The state of the home front in a game: crime, drugs, protests, the grid, disasters, a pandemic, the parties. */
public final class Home implements Serializable {
    private static final long serialVersionUID = 1L;
    public double crime = 100, od = 80, protest = 20, grid = 35, press = 50;
    public double recE, recC, recW;                       // the record: where your actions have landed, on average
    public double[] fac = {60, 60};
    public int opp = 1, oppWeek;
    public int pand, pandWeek = -1, pandSlow, pandFast, deaths;
    public double pandSev = 1;
    public int[] cool = new int[EV_ID.length];
    public double followers = 30;
    public int posts, postWeek = -1, postsThisWeek, pressers, interviews, speeches, polls, sotuYear = -1, lastPresser = -9, lastInterview = -9, lastAddress = -9;
    public double[] tone = new double[OUT_NAME.length];
    public List<String> disasters = new ArrayList<String>(), brief = new ArrayList<String>();
    public String lastPoll;
    // a speech, a press conference or an interview in progress
    public int spType = -1, spStep, spOpen, spIssue = -1, spAttack;
    public double spScore;
    public int qaType = -1, qaStep, qaOutlet;
    public double qaScore;
    public String[] qaTopic = new String[3];
    int disState = -1, disKind, disSev;
}
