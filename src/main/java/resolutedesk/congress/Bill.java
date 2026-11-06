package resolutedesk.congress;

import static resolutedesk.congress.BillData.*;
import static resolutedesk.congress.CongressRules.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.model.Parties.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** A bill in Congress: its terms, its stage and the votes it has won. */
public final class Bill implements Serializable {
    private static final long serialVersionUID = 1L;
    public String defId, name;
    public int stage, first = HOUSE, stageWeek, introduced = -1, lawWeek = -1, payFor;
    public double size = 1, scope = 0.5, strength = 1;
    public boolean recon, ours = true, vetoed, bottled, publicPush, held;
    public int houseYes, houseNo, senYes, senNo;
    String note = "";
    public List<String> deals = new ArrayList<String>();
    double[] sweet = new double[12];                // extra support per bloc from deals

    public BillDef def() { return billDef(defId); }

    public int second() { return first == HOUSE ? SENATE : HOUSE; }

    public int chamberNow() { return stage <= ST_FLOOR ? first : second(); }

    public double netCost() {
        BillDef d = def();
        double c = d.cost * size * (0.7 + 0.6 * scope) * COST_SCALE;
        if (c > 0 && payFor != 0) c *= 0.05;
        return c;
    }

    /** Where the bill sits on the left-right scales, after its provisions. */
    public double econ() {
        BillDef d = def();
        double dir = d.lean == REP ? 1 : d.lean == DEM ? -1 : (d.econ >= 0 ? 0.5 : -0.5);
        return clamp(d.econ + dir * 0.15 * (size - 1) + dir * 0.18 * (scope - 0.5) + (payFor == 1 ? -0.15 : payFor == 2 ? 0.15 : 0), -1, 1);
    }

    public double cult() {
        BillDef d = def();
        double dir = d.lean == REP ? 1 : d.lean == DEM ? -1 : 0;
        return clamp(d.cult + dir * 0.18 * (scope - 0.5), -1, 1);
    }

    public double support() { return def().support - 4 * (scope - 0.5) - (payFor == 0 && def().cost > 30 ? 2 : 0) + (payFor == 1 ? 2 : 0); }
}
