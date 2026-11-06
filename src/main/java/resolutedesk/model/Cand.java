package resolutedesk.model;

import static resolutedesk.model.Attributes.*;
import static resolutedesk.opinion.VoterIssues.*;

import java.io.Serializable;

/** A politician who can be president or vice president: ratings, positions, biography and look. */
public final class Cand implements Serializable {
    private static final long serialVersionUID = 1L;
    public String id, first, last, role, homeAb, bio, pros, cons, spouse;
    public int party, born, faction, home;
    public double econ, cult;
    public int[] a = new int[NA];
    public double nameId, poll, cash, list, selfMax;
    public double[] plat = new double[NI];
    public String[] record = new String[0];
    public int[] recIssue = new int[0];
    public double[] recCred = new double[0];
    public int[] apG = new int[0];
    public double[] apV = new double[0];
    public Look look;
    public boolean custom, fictional;

    public String name() { return first + " " + last; }

    int age2028() { return 2028 - born - 1 + (born > 0 ? 1 : 0); }
}
