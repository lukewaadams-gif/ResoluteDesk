package resolutedesk.executive;

import java.io.Serializable;

/** An order, rule or emergency you have issued. */
public final class Action implements Serializable {
    private static final long serialVersionUID = 1L;
    public int def = -1, kind, area, lean, status, issued, finalWeek = -1, statusWeek;
    public String name;
    public double support, legal, scope = 1;
    public boolean custom, failed, sued, applied;
    double aGdp, aUnemp, aInfl, aTariff, aOil, aDef;           // what it applied, so it can be undone
}
