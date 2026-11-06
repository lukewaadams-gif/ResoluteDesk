package resolutedesk.congress;

/** A bill that can be proposed: what it does, who backs it, what it costs and what it changes. */
public final class BillDef {
    public final String id, name, desc, impact;
    public final int area, lean;                       // lean: the party whose agenda it is, or -1 for bipartisan
    public final double econ, cult, cost, support, gdp, unemp, infl, base;
    public final boolean budget, origRevenue;          // reconciliation-eligible; a revenue bill must start in the House
    final int fmt;
    public int from = 2021, to = 2100;                 // the calendar years the bill belongs to

    BillDef(String id, String name, String desc, int area, int lean, double econ, double cult, double cost, boolean budget, double support,
            double gdp, double unemp, double infl, String impact, double base, int fmt) {
        this(id, name, desc, area, lean, econ, cult, cost, budget, support, gdp, unemp, infl, impact, base, fmt, cost < 0 || id.contains("tax") || id.equals("tariffs"));
    }

    BillDef(String id, String name, String desc, int area, int lean, double econ, double cult, double cost, boolean budget, double support,
            double gdp, double unemp, double infl, String impact, double base, int fmt, boolean revenue) {
        this.id = id; this.name = name; this.desc = desc; this.area = area; this.lean = lean; this.econ = econ; this.cult = cult; this.cost = cost; this.budget = budget;
        this.support = support; this.gdp = gdp; this.unemp = unemp; this.infl = infl; this.impact = impact; this.base = base; this.fmt = fmt;
        this.origRevenue = revenue;
    }
}
