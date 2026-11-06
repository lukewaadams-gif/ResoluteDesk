package resolutedesk.congress;

import java.io.Serializable;
import resolutedesk.model.Cand;

/** A named member of Congress: a leader, a committee chair or a swing vote. */
public final class Member implements Serializable {
    private static final long serialVersionUID = 1L;
    public String name, title, committee;
    public int party, chamber, bloc, state, role, price;
    public double rel = 50, econ, cult;
    public Cand face;
    public int lastDeal = -99;
    boolean promised;
}
