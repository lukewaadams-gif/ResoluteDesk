package resolutedesk.model;

import static resolutedesk.core.Dates.*;
import static resolutedesk.data.StateData.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.opinion.Demographics.*;
import static resolutedesk.opinion.Issues.*;
import static resolutedesk.sim.Schedule.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import resolutedesk.campaign.Reelect;
import resolutedesk.congress.Congress;
import resolutedesk.executive.Exec;
import resolutedesk.home.Home;
import resolutedesk.sim.Decision;
import resolutedesk.transition.Transition;
import resolutedesk.world.World;

/** Everything about one presidency: the calendar, the president, approval, the economy, Congress, the cabinet, the world and the record. Saved games are this object, serialized. */
public final class Game implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int VERSION = 1;
    public int version = VERSION;
    public Settings set;
    public Random rng;
    public Era era;
    public int week, termWeeks = 208, term = 1;
    public Cand pres, vp;
    public int party;
    int age;
    // the calendar
    public int[] act = new int[NSLOT], arg = new int[NSLOT];
    public int location;                       // state the president is in (DC by default)
    // health
    public double stamina = 85, sleep = 6.5, stress = 25, health = 92, fitness = 50;
    public int sick;                           // weeks of illness left
    // standing with the public
    public double approval, approval0, honeymoon, rally, perf, scandal;
    public double mood;                        // the public mood: a slow drift in approval, a few points either way
    public double[] issueApp = new double[NPI], issueAdj = new double[NPI], issueSal = new double[NPI], command = new double[NPI];
    public double[] stateApp = new double[NS], stateBump = new double[NS];
    public double[] groupApp = new double[NG], groupAdj = new double[NG];
    public double offset;                      // the cell model's national shift
    public double warChest, funds;             // party money ($M)
    int fundraisers;
    public double hill = 50;                   // relations with Congress (0-100), deepened in a later version
    public int houseD, senateD;                // seats held by Democrats (and the independents who caucus with them)
    public Econ econ = new Econ();
    public Congress cong;
    public Exec ex;
    public World world;
    public Home home;
    public boolean removed;                    // convicted by the Senate
    public int termStart, prior;               // the week the current term began; terms the president had already served before the game
    public int ending;                         // how the presidency ended (END_*)
    public Reelect re;                         // the campaign for another term
    public Transition trans;                   // the weeks before the inauguration, if they were played
    public int tut = -1;                       // the tutorial's step (-1: off)
    public boolean watch;                      // watch mode: the game plays itself
    public int watchSpeed = 1;
    public int okCount, failCount;             // crises and events that went well or badly
    public double justice;                     // the running record on equal justice
    public List<String> histOk = new ArrayList<String>();   // turning points that went your way
    public int warsStarted, minApprovalWeek;
    public double minApproval = 100, maxApproval;
    public boolean recorded;                   // added to the Hall of Fame
    public boolean night;                      // election night results are waiting to be shown
    public boolean midGain;                    // gained House seats in a midterm
    public int lastEvWon, lastEvTotal;         // the last re-election won
    public int legacyScore, legacyRank;
    public List<String> achievements, epilogue;
    public List<String> milestones = new ArrayList<String>();
    public List<Decision> inbox = new ArrayList<Decision>();
    public boolean filibusterGone;
    // history: one row a week
    public List<double[]> hist = new ArrayList<double[]>();
    // the news
    public List<News> news = new ArrayList<News>();
    int paperWeek = -1;
    public List<String> ticker = new ArrayList<String>();
    public boolean over;
    public transient boolean dirty = true;

    public int day() { return week * 7; }

    /** Weeks into the current term. */
    public int tw() { return week - termStart; }

    public int presAge() { return pres == null || pres.born <= 0 ? 60 : dateOf(day())[0] - pres.born; }

    boolean dem() { return party == DEM; }

    public int myHouse() { return party == REP ? 435 - houseD : houseD; }

    public int mySenate() { return party == REP ? 100 - senateD : senateD; }

    public void log(int kind, String head, String body, double weight) {
        News n = new News();
        n.day = day(); n.kind = kind; n.head = head; n.body = body; n.weight = weight;
        news.add(n);
        if (TRACE) System.out.println("    [" + dateStr(n.day) + "] " + head);
        if (news.size() > 400) news.remove(0);
        ticker.add(head);
        if (ticker.size() > 40) ticker.remove(0);
    }

    public void log(int kind, String head) { log(kind, head, null, 1); }
}
