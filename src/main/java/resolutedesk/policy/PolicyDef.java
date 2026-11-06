package resolutedesk.policy;

import static resolutedesk.model.Parties.*;
import static resolutedesk.opinion.Demographics.*;
import static resolutedesk.policy.Outcomes.*;
import static resolutedesk.policy.PolicyData.*;

/** One policy, read from its row in PolicyData: its levels, the law in each era, its channels, effects and politics. */
public final class PolicyDef {
    public final String id, name;
    public final int area;
    public final String[] levels;
    final int[] defYear, defLevel;
    final boolean[] channel = new boolean[NCH];
    public final int from;
    /** Legal risk of making the change by order or rule, before the counsel's work. */
    public final double legal;
    /** Per step up: cost ($ billions a year, today's dollars), economic and cultural shift, and effects on each outcome. */
    public final double cost, econ, cult;
    public final double[] fx = new double[NOUT];
    /** Approval among each voter group, in points, per step up. */
    public final double[] groups = new double[NG];
    /** Public support for a step up (%), and the party that wants it higher (or -1). */
    public final double support;
    public final int lean;
    public final boolean signature;
    public final int lag, evidence;
    public final String winners, losers, precedent;

    PolicyDef(String row) {
        String[] t = row.split("\\|");
        id = t[0];
        area = Integer.parseInt(t[1]);
        name = t[2];
        levels = t[3].split(";");
        String[] dl = t[4].split(" ");
        defYear = new int[dl.length];
        defLevel = new int[dl.length];
        for (int i = 0; i < dl.length; i++) { String[] kv = dl[i].split(":"); defYear[i] = Integer.parseInt(kv[0]); defLevel[i] = Integer.parseInt(kv[1]); }
        channel[CH_BILL] = t[5].contains("B");
        channel[CH_ORDER] = t[5].contains("O");
        channel[CH_RULE] = t[5].contains("R");
        channel[CH_BUDGET] = t[5].contains("$");
        from = Integer.parseInt(t[6]);
        legal = Double.parseDouble(t[7]);
        cost = Double.parseDouble(t[8]);
        econ = Double.parseDouble(t[9]);
        cult = Double.parseDouble(t[10]);
        support = Double.parseDouble(t[11]);
        lean = t[12].equals("D") ? DEM : t[12].equals("R") ? REP : -1;
        signature = t[13].equals("1");
        if (!t[14].equals("-")) for (String kv : t[14].split(" ")) fx[outcome(kv.split("=")[0])] = Double.parseDouble(kv.split("=")[1]);
        if (!t[15].equals("-")) for (String kv : t[15].split(" ")) groups[group(kv.split("=")[0])] = Double.parseDouble(kv.split("=")[1]);
        lag = Integer.parseInt(t[16]);
        evidence = "SMWC".indexOf(t[17].charAt(0));
        winners = t[18];
        losers = t[19];
        precedent = t[20];
    }

    static int outcome(String key) {
        for (int o = 0; o < NOUT; o++) if (OC_KEY[o].equals(key)) return o;
        throw new IllegalArgumentException("no outcome " + key);
    }

    static int group(String code) {
        for (int i = 0; i < PolicyTable.GROUP_CODE.length; i++) if (PolicyTable.GROUP_CODE[i].equals(code)) return i;
        throw new IllegalArgumentException("no voter group " + code);
    }

    /** The level in force at the start of a year. */
    public int levelIn(int year) {
        int l = defLevel[0];
        for (int i = 0; i < defYear.length; i++) if (year >= defYear[i]) l = defLevel[i];
        return l;
    }

    public boolean can(int ch) { return channel[ch]; }

    /** The party that wants a move in this direction, or -1. */
    public int leanFor(int steps) {
        if (lean < 0) return -1;
        return steps > 0 ? lean : lean == DEM ? REP : DEM;
    }

    /** Public support for a move in this direction. */
    public double supportFor(int steps) { return steps > 0 ? support : 100 - support; }
}
