package resolutedesk.policy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Where every policy stands in a game, how far each change has worked through, and the outcomes it has produced. */
public final class PolicyState implements Serializable {
    private static final long serialVersionUID = 1L;
    public int[] level, base;                  // the law now, and on the first day
    public double[] eff;                       // how far each policy's effect has come (it moves toward the level over the policy's lag)
    public int[] levelEcon;                    // the level counting only changes whose economic effects this model applies
    public double[] effEcon;
    public double[] strength;                  // how firmly the change was made: 1 a law, 0.85 a rule, 0.6 an order
    public int[] changed, via;                 // the week and the channel of each policy's last change
    public double[] aGdp, aUnemp, aInfl, aOil, aFiscal;   // what each policy has added to the economy
    public double[] out, out0;                 // the outcomes now, and on the first day
    public double unemp0, growthIdx = 1, homeCrime0 = 100, homeOd0 = 80;
    public double[] result;                    // what the outcomes do to approval, by issue
    public List<double[]> hist = new ArrayList<double[]>();  // the outcomes every four weeks
    public List<int[]> budget = new ArrayList<int[]>();      // changes in your budget request: {policy, level}
    public int fy, changes;
}
