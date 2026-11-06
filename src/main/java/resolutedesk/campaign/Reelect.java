package resolutedesk.campaign;

import static resolutedesk.data.StateData.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import resolutedesk.model.Cand;

/** The state of a re-election campaign: the tickets, the money, the debates, the polls and the result. */
public final class Reelect implements Serializable {
    private static final long serialVersionUID = 1L;
    public int year, stage;                    // stage: 0 not yet, 1 asked, 2 running, 3 a successor runs, 4 decided
    public boolean running, eligible, denied, done, won, nightSeen;
    public Cand nominee, nomVp, opp, oppVp;    // the two tickets
    public String challenger;                  // a primary challenger from your own party
    public double chal, chalShare;             // the challenger's strength and his share of the primary vote so far
    public int theme, adsMix = 55;             // spending split: % on television
    double conv, oppConv;               // convention bounces, decaying
    double[] debate = new double[3];
    int debates, debatesHeld;
    public double[] effort = new double[NS];
    public double nat, mood, money;            // national margin (your party), the year's mood, money advantage
    public double[] st = new double[NS];       // state margins in the polls
    public int evMe, evOpp, evTotal;
    public double popMe, popOpp;
    public double[] margin = new double[NS];
    int houseD0, senateD0, houseD1, senateD1;
    List<String> news = new ArrayList<String>();
}
