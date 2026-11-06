package resolutedesk.policy;

import static resolutedesk.congress.BillData.BILLS;
import static resolutedesk.congress.BillData.HBILL_SPEC;
import static resolutedesk.congress.CongressRules.ST_COMMITTEE;
import static resolutedesk.executive.ActionData.A_LIVE;
import static resolutedesk.opinion.Demographics.GROUP;
import static resolutedesk.opinion.Issues.PI_HEALTH;
import static resolutedesk.policy.Outcomes.O_UNINS;
import static resolutedesk.policy.PolicyData.*;

import resolutedesk.congress.Bill;
import resolutedesk.congress.BillDef;
import resolutedesk.congress.Legis;
import resolutedesk.executive.Action;
import resolutedesk.executive.Actions;
import resolutedesk.model.Game;
import resolutedesk.testkit.Check;
import resolutedesk.testkit.Games;
import resolutedesk.testkit.Test;

/** Policies: the table, the law of each era, the channels of change, and what changes do. */
public class PolicyTest {
    static int k(String id) {
        int k = Policies.index(id);
        Check.isTrue(k >= 0, "no policy " + id);
        return k;
    }

    static int group(String name) {
        for (int i = 0; i < GROUP.length; i++) if (GROUP[i].equals(name)) return i;
        throw new AssertionError("no group " + name);
    }

    @Test
    public void everyAreaHasAFullSetOfPolicies() {
        Check.isTrue(Policies.count() >= 140, "about 150 policies: " + Policies.count());
        int[] n = new int[NAREA];
        for (PolicyDef d : Policies.all()) {
            n[d.area]++;
            Check.between(2, 5, d.levels.length, d.id + " levels");
            for (int y = 1933; y <= 2029; y += 4) Check.between(0, d.levels.length - 1, d.levelIn(y), d.id + " in " + y);
            Check.isTrue(d.can(CH_BILL) || d.can(CH_ORDER) || d.can(CH_RULE) || d.can(CH_BUDGET), d.id + " can be changed somehow");
        }
        for (int a = 0; a < NAREA; a++) Check.isTrue(n[a] >= 10, AREA_NAME[a] + " has " + n[a]);
    }

    @Test
    public void namedBillsMapToRealPoliciesAndLevels() {
        for (String row : PolicyTable.BILL_POLICY) {
            String[] t = row.split(" ");
            boolean found = false;
            for (BillDef b : BILLS) if (b.id.equals(t[0])) found = true;
            for (String h : HBILL_SPEC) if (h.startsWith(t[0] + "|")) found = true;
            Check.isTrue(found, "a bill called " + t[0]);
            for (int i = 1; i < t.length; i++) {
                String[] kv = t[i].split("=");
                PolicyDef d = Policies.def(k(kv[0]));
                Check.between(0, d.levels.length - 1, Integer.parseInt(kv[1]), t[0] + " sets " + kv[0]);
            }
        }
    }

    @Test
    public void eachEraStartsWithTheLawOfItsDay() {
        Check.equal(0, Games.start(1, 1961).pol.level[k("medicare")], "no Medicare in 1961");
        Check.equal(1, Games.start(1, 1969).pol.level[k("medicare")], "Medicare by 1969");
        Check.equal(4, Games.start(1, 1961).pol.level[k("top_rate")], "a 91% top rate in 1961");
        Check.equal(3, Games.start(1, 1969).pol.level[k("min_wage")], "the minimum wage at its peak in 1969");
        Check.equal(3, Games.start(1, 1977).pol.level[k("abortion")], "Roe in 1977");
        Check.equal(2, Games.start(1, 2025).pol.level[k("abortion")], "left to the states after Dobbs");
    }

    @Test
    public void aBillMovesThePolicyWhenItBecomesLaw() {
        Game g = Games.start(4, 2025);
        int mw = k("min_wage");
        Check.isTrue(Policies.cannot(g, mw, 2, CH_ORDER) != null, "the minimum wage can't be raised by order");
        Check.isTrue(Policies.propose(g, mw, 3, CH_BILL).contains("Congress"), "a bill goes to Congress");
        Bill b = g.cong.bills.get(g.cong.bills.size() - 1);
        Check.equal(ST_COMMITTEE, b.stage, "in committee");
        Check.isTrue(Policies.cannot(g, mw, 3, CH_BILL) != null, "one bill at a time");
        double union = g.groupAdj[group("Union households")];
        Legis.decide(g, b, 0);
        Check.equal(3, g.pol.level[mw], "the law raised it two steps");
        Check.isTrue(g.groupAdj[group("Union households")] > union, "union households cheer");
    }

    @Test
    public void anOrderIsImmediateAndCanBeStruckDown() {
        Game g = Games.start(4, 2025);
        int dr = k("drilling");
        int before = g.pol.level[dr];
        Policies.propose(g, dr, before + 1, CH_ORDER);
        Check.equal(before + 1, g.pol.level[dr], "in force at once");
        Check.near(CH_STRENGTH[CH_ORDER], g.pol.strength[dr], 1e-9, "at an order's strength");
        Action a = null;
        for (Action x : g.ex.actions) if (x.policy == dr && x.status == A_LIVE) a = x;
        Actions.apply(g, a, false);
        Check.equal(before, g.pol.level[dr], "struck down, the policy goes back");
    }

    @Test
    public void effectsWorkThroughOverTime() {
        Game g = Games.start(4, 2025);
        int md = k("medicaid");
        Policies.set(g, md, 3, CH_BILL, 1, true, 2);
        Games.play(g, 4);
        double early = -Policies.contribution(g, md, O_UNINS);
        Games.play(g, 100);
        double later = -Policies.contribution(g, md, O_UNINS);
        Check.isTrue(early > 0 && early < 1, "a month in, a little of the effect: " + early);
        Check.near(Policies.def(md).fx[O_UNINS] * -1, later, 0.1, "two years in, nearly all of it");
    }

    @Test
    public void votersCreditResults() {
        Game g = Games.start(4, 2029);
        double before = Policies.result(g, PI_HEALTH);
        Policies.set(g, k("medicaid"), 3, CH_BILL, 1, true, 2);
        Policies.set(g, k("aca_aid"), 2, CH_BILL, 1, true, 2);
        for (int w = 0; w < 80; w++) { Policies.week(g); g.week++; }
        Check.isTrue(Policies.result(g, PI_HEALTH) > before + 0.2, "fewer uninsured: credit on health care");
    }

    @Test
    public void aBudgetChangeWaitsForTheFiscalYear() {
        Game g = Games.start(4, 2029);
        int nih = k("nih");
        int before = g.pol.level[nih];
        Policies.propose(g, nih, before + 1, CH_BUDGET);
        Check.equal(before, g.pol.level[nih], "nothing changes in January");
        int y = 0;
        while (g.pol.level[nih] == before && y++ < 60) Games.play(g, 1);
        Check.isTrue(g.pol.budget.isEmpty(), "the request is settled by the new fiscal year");
    }

    @Test
    public void signaturePoliciesMoveTheCountryUpToFivePoints() {
        PolicyDef paid = Policies.def(k("paid_leave")), wic = Policies.def(k("wic"));
        Check.isTrue(paid.signature && !wic.signature, "paid leave is a signature issue; WIC is not");
        Check.isTrue(Policies.national(paid, 1, 1) > 3, "paid leave, which 70% want, is worth points");
        Check.isTrue(Policies.national(paid, 1, 1) <= 5, "but no more than five");
        Check.isTrue(Policies.national(wic, 1, 1) < 1.5, "a popular but small policy is worth less");
        Check.isTrue(Policies.national(paid, -1, 1) < 0, "taking it away costs");
    }
}
