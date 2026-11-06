package resolutedesk.world;

/** One objective, read from its row in ObjectiveTable. */
public final class ObjectiveDef {
    public final String id, who, name, why, payoff;
    public final int from, to;
    /** Each step: label, odds kind, base odds, requirement, cost, effect on success, effect on failure. */
    public final String[][] steps;

    ObjectiveDef(String row) {
        String[] t = row.split("\\|", 8);
        id = t[0]; who = t[1]; from = Integer.parseInt(t[2]); to = Integer.parseInt(t[3]); name = t[4]; why = t[5]; payoff = t[6];
        String[] st = t[7].split(";;");
        steps = new String[st.length][];
        for (int i = 0; i < st.length; i++) steps[i] = st[i].split("\\|", -1);
    }

    /** A template for any country of one alignment (*A adversary, *N neutral, *P partner, *T treaty ally), rather than one country. */
    public boolean generic() { return who.startsWith("*"); }
}
