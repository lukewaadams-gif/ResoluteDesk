package resolutedesk.model;

/** A faction within a party, with its positions on the issues. */
public final class Faction {
    final String name, desc, impact;
    final int party;
    public final double econ, cult;
    public final double[] pos;

    Faction(String name, int party, double econ, double cult, double[] pos, String desc, String impact) {
        this.name = name; this.party = party; this.econ = econ; this.cult = cult; this.pos = pos; this.desc = desc; this.impact = impact;
    }
}
