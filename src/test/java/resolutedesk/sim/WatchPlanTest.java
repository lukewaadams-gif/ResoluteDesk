package resolutedesk.sim;

import static resolutedesk.sim.DecisionKinds.*;
import static resolutedesk.sim.WatchPlan.*;
import static resolutedesk.world.CrisisData.CR_OPTS;
import static resolutedesk.world.WarData.WK_STRIKES;
import static resolutedesk.world.WorldData.CC_VE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import resolutedesk.congress.Bill;
import resolutedesk.congress.BillDef;
import resolutedesk.model.Game;
import resolutedesk.testkit.Check;
import resolutedesk.testkit.Games;
import resolutedesk.testkit.Test;
import resolutedesk.ui.HillUI;
import resolutedesk.world.War;
import resolutedesk.world.Wars;

/** Watch mode's plan: presets, priorities, the bill list and the decisions it stops for. */
public class WatchPlanTest {
    @Test
    public void theMiddleOfEveryPriorityIsTheOldSteadyHand() {
        WatchPlan p = new WatchPlan();
        Check.equal(2, p.activeBills(), "two bills at once");
        Check.equal(9, p.orderWeeks(), "an order every nine weeks");
        Check.equal(8, p.roadWeeks(), "a road trip every eight weeks");
        Check.near(0.12, p.forcePenalty(), 1e-9, "force held back a little");
        Check.near(55, p.giveUpFront(), 1e-9, "a failing war ended below 55");
        Check.near(0.45, p.surePenalty(), 1e-9, "the sure thing held back");
    }

    @Test
    public void presetsSetThePrioritiesAndTouchingOneMakesThePlanYourOwn() {
        WatchPlan p = new WatchPlan();
        p.use(1);
        Check.equal(4, p.activeBills(), "a big agenda keeps four bills moving");
        Check.isTrue(p.orderWeeks() < 9, "and signs more orders");
        p.set(P_HAWK, 0.9);
        Check.equal(CUSTOM, p.preset, "now it's the player's own");
        p.use(0);
        Check.near(0.5, p.p[P_HAWK], 1e-9, "the steady hand again");
        for (int k = 0; k <= 1; k++) for (int i = 0; i < NPRIORITY; i++) { p.use(k); Check.between(0, 1, p.p[i], PRESET[k] + " " + PRIORITY[i]); }
    }

    @Test
    public void watchModeStopsOnlyForWhatThePlanSays() {
        WatchPlan p = new WatchPlan();
        Check.isTrue(!p.stops(D_SIGN), "nothing by default");
        p.stop[S_SIGN] = true;
        p.stop[S_STORY] = true;
        Check.isTrue(p.stops(D_SIGN), "bills to sign");
        Check.isTrue(p.stops(D_STORY), "storylines");
        Check.isTrue(!p.stops(D_WORLD), "not crises");
        Check.isTrue(!p.stops(D_INFO), "never a notice");
    }

    @Test
    public void theBillListDecidesWhatGoesToCongress() {
        Game g = Games.start(8, 2025);
        List<BillDef> agenda = HillUI.agenda(g);
        BillDef last = null;
        for (BillDef d : agenda) if (d.lean == g.party) last = d;
        g.plan.bills.add(last.id);
        int before = g.cong.bills.size();
        Decisions.autoAgenda(g);
        Check.isTrue(g.cong.bills.size() > before, "a bill was sent");
        Bill b = g.cong.bills.get(g.cong.bills.size() - 1);
        Check.equal(last.id, b.defId, "the one on the list, though it comes last in the agenda");
        Check.equal(1, Decisions.agendaFor(g, g.plan).size(), "and nothing else");
    }

    @Test
    public void aHawkReachesForForceAndADoveDoesNot() {
        Game g = Games.start(8, 2025);
        boolean differs = false;
        for (int i = 0; i < CR_OPTS.length; i++) {
            Decision d = new Decision();
            d.kind = D_WORLD;
            d.a = i;
            g.plan.use(3);
            int hawk = Decisions.auto(g, d);
            g.plan.use(4);
            int dove = Decisions.auto(g, d);
            String[] o = CR_OPTS[i].split(";;");
            boolean hm = o[hawk].split("\\|")[2].equals("m"), dm = o[dove].split("\\|")[2].equals("m");
            Check.isTrue(!dm || hm, "a dove never uses force where a hawk wouldn't: crisis " + i);
            if (hm && !dm) differs = true;
        }
        Check.isTrue(differs, "in some crisis the two choose differently");
    }

    @Test
    public void aDoveEndsAFailingWarSoonerThanAHawk() {
        Game g = Games.start(8, 2025);
        Wars.start(g, CC_VE, WK_STRIKES);
        War x = g.world.wars.get(g.world.wars.size() - 1);
        Decision d = new Decision();
        d.kind = D_WARPOWERS;
        d.war = x;
        x.front = 45;
        Check.equal(1, Wars.auto(g, d), "the steady hand signs at 45");
        g.plan.use(3);
        Check.equal(0, Wars.auto(g, d), "a hawk vetoes");
        g.plan.use(4);
        x.front = 70;
        Check.equal(1, Wars.auto(g, d), "a dove signs even at 70");
    }

    @Test
    public void thePlanIsSavedWithTheGame() throws Exception {
        Game g = Games.start(8, 2025);
        g.plan.use(2);
        g.plan.stop[S_WORLD] = true;
        g.plan.bills.add("x");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bytes);
        out.writeObject(g);
        out.close();
        Game copy = (Game) new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();
        Check.equal(2, copy.plan.preset, "preset");
        Check.isTrue(copy.plan.stop[S_WORLD], "stops");
        Check.equal("x", copy.plan.bills.get(0), "bills");
    }
}
