package resolutedesk.congress;

import static resolutedesk.data.StateData.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** The state of Congress in a game: the members, the blocs, the bills and the budget fights. */
public final class Congress implements Serializable {
    private static final long serialVersionUID = 1L;
    public List<Bloc> blocs = new ArrayList<Bloc>();
    public List<Member> members = new ArrayList<Member>();
    public List<Bill> bills = new ArrayList<Bill>();
    public int reconFy = -1, fy;                           // the fiscal year whose reconciliation bill has been used
    boolean[] floorUsed = new boolean[2];
    // the budget
    public boolean budgetSent;
    public double reqDefense, reqNondefense, reqTax, reqMandatory;
    public int[] approps = new int[12];                    // 0 none, 1 one chamber, 2 both, 3 enacted, 4 on a CR
    public int crUntil = -1, xWeek = -1;
    public boolean shutdown;
    public int shutdownWeeks;
    public double blame;                                   // share of the public blaming you for a shutdown (0-1)
    public double debtLimit;                               // $ trillions
    public boolean xWarned, defaulted, crMinority;
    // midterms
    public boolean midDone;
    public double[] campaign = new double[NS];             // your campaigning per state in the midterm season
    double midMargin;
    public String midSummary;
    public int overrides, vetoes, laws, shutdowns, shutdownTotal;
}
