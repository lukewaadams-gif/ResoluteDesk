package resolutedesk.congress;

import static resolutedesk.congress.BillData.BILLS;
import static resolutedesk.congress.CongressRules.SENATE;

import resolutedesk.model.Game;
import resolutedesk.testkit.Check;
import resolutedesk.testkit.Games;
import resolutedesk.testkit.Test;

/** How many votes the Senate needs to end debate, which changed over the years. */
public class LegisTest {
    private static Bill bill(String id) {
        for (BillDef d : BILLS) {
            if (d.id.equals(id)) {
                Bill b = new Bill();
                b.defId = id;
                b.ours = true;
                b.name = d.name;
                return b;
            }
        }
        throw new AssertionError("no bill " + id);
    }

    @Test
    public void todayItTakesSixtyVotes() {
        Game g = Games.start(1, 2029);
        Check.equal(60, Legis.need(g, bill("tcja"), SENATE, false), "a bill outside reconciliation in 2029");
    }

    @Test
    public void beforeTheSeventiesMostBillsPassedByMajority() {
        Game g = Games.start(1, 1961);
        Check.equal(50, Legis.need(g, bill("medicare"), SENATE, false), "Medicare needed a majority");
        Check.equal(67, Legis.need(g, bill("civil_rights64"), SENATE, false), "civil rights faced the filibuster and two-thirds cloture");
    }
}
