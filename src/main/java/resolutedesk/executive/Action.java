package resolutedesk.executive;

import java.io.Serializable;

/** An order, rule or emergency you have issued. */
public final class Action implements Serializable {
    private static final long serialVersionUID = 1L;
    public int def = -1, kind, area, lean, status, issued, finalWeek = -1, statusWeek;
    public String name;
    public double support, legal, scope = 1;
    public boolean custom, failed, sued, applied;
    public int policy = -1, polFrom, polTo;                    // an order or rule that moves a policy (see policy.Policies)
    public boolean reacted;                                    // the public has already reacted to it
    double aGdp, aUnemp, aInfl, aTariff, aOil, aDef;           // what it applied, so it can be undone
}
