package resolutedesk.executive;

import static resolutedesk.executive.ActionData.*;
import static resolutedesk.executive.Delegation.*;
import static resolutedesk.executive.Posts.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Everything the executive branch keeps between weeks. */
public final class Exec implements Serializable {
    private static final long serialVersionUID = 1L;
    public Person[] staff = new Person[NSTAFF];
    public Person[] post = new Person[NPOST];
    public Person[] nominee = new Person[NPOST];
    public int[] voteAt = new int[NPOST];
    public double[] ready = new double[NPOST];
    public double[] wf = new double[NPOST];
    public List<Action> actions = new ArrayList<Action>();
    public boolean[] emergency = new boolean[NEMERG];
    // courts
    public List<Justice> court = new ArrayList<Justice>();
    public List<Case> cases = new ArrayList<Case>();
    public double dDistrict = 0.52, dCircuit = 0.47;      // share of active judges appointed by Democrats
    public double vacDistrict = 40, vacCircuit = 6;
    public int judges, judgeStrategy = 1, justices;
    double judgeFrac;
    public Justice nomineeJ;
    public int scotusVoteAt = -1, scotusVacancies;
    // trouble
    public List<Scandal> scandals = new ArrayList<Scandal>();
    public List<Probe> probes = new ArrayList<Probe>();
    public int contempt, defiance, pardons, nextPardon = 8, nextProbe = 14;
    public int impStage, impWeek = -1, impeachments;
    public double impPressure;
    public String impResult;
    // the vice president
    public int vpMission, vpArg = -1, vpUntil = -1, vpMissions;
    public double vpLoyalty = 75;
    public boolean[] deleg = new boolean[NDG];
    public int lastCabinet = -10, departures, failures, leaks;
    boolean transitionDone;
}
