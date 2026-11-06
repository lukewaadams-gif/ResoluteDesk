package resolutedesk.congress;

import static resolutedesk.congress.BillData.BILLS;
import static resolutedesk.congress.CongressRules.RO_SWING;
import static resolutedesk.sim.Schedule.AC_CONGRESS;
import static resolutedesk.sim.Schedule.AC_FREE;
import static resolutedesk.sim.Schedule.AC_TRIP;

import java.util.Arrays;
import resolutedesk.model.Game;
import resolutedesk.sim.Decision;
import resolutedesk.testkit.Check;
import resolutedesk.testkit.Games;
import resolutedesk.testkit.Test;

/** Relationships with Congress: the mandate, the president's time, favors and promises. */
public class RelationsTest {
    private static Bill ourBill(Game g) {
        for (BillDef d : BILLS) if (d.lean < 0 || d.lean == g.party) { Bill b = Legis.draft(g, d); Legis.introduce(g, b); return b; }
        throw new AssertionError("no bill for the president's party");
    }

    private static Member swing(Game g) {
        for (Member m : g.cong.members) if (m.role == RO_SWING && m.party == g.party) return m;
        throw new AssertionError("no swing member");
    }

    @Test
    public void aBiggerWinGivesABiggerMandate() {
        Check.isTrue(Relations.mandateFor(22.6) > 12, "a landslide is a sweeping mandate");
        Check.isTrue(Relations.mandateFor(0.2) < 3, "a squeaker is a thin one");
        Game g = Games.start(2, 1965);
        Check.near(Relations.mandateFor(22.6), g.cong.mandate, 1e-9, "1965 starts with Johnson's real margin");
    }

    @Test
    public void theMandateFadesWithinAYear() {
        Game g = Games.start(2, 2029);
        double start = g.cong.mandate;
        Games.play(g, 52);
        Check.isTrue(g.cong.mandate < start / 5, "a year in, little of the mandate is left");
    }

    @Test
    public void hillMeetingsOnTheCalendarGiveTheHours() {
        Game g = Games.start(2, 2029);
        Arrays.fill(g.act, AC_FREE);
        double bare = Relations.hoursMax(g);
        g.act[1] = AC_CONGRESS;
        g.act[4] = AC_CONGRESS;
        Check.near(bare + 2 * Relations.HOURS_PER_MEETING, Relations.hoursMax(g), 1e-9, "two Hill meetings add five hours");
    }

    @Test
    public void dealsTakeTimeAndStopWhenItRunsOut() {
        Game g = Games.start(2, 2029);
        Arrays.fill(g.act, AC_FREE);
        Bill b = ourBill(g);
        int calls = 0;
        for (Member m : g.cong.members) {
            if (m.role != RO_SWING) continue;
            if (Legis.cannot(g, b, m, Legis.DL_CALL) == null) { Legis.deal(g, b, m, Legis.DL_CALL); calls++; }
        }
        Check.equal((int) Math.floor(Relations.hoursMax(g)), calls, "one call per hour of the week, then no more");
    }

    @Test
    public void sayingYesEarnsAFavorThatCanBeCalledIn() {
        Game g = Games.start(2, 2029);
        Member m = swing(g);
        Decision d = new Decision();
        d.member = m;
        d.a = Relations.ASK_APPOINT;
        int before = g.cong.appointments;
        Relations.answer(g, d, 0);
        Check.equal(before - 1, g.cong.appointments, "the appointment is given away");
        Check.isTrue(Relations.owed(g, m) != null, "and the member owes you");
        Bill b = ourBill(g);
        double sweet = b.sweet[m.bloc];
        Legis.deal(g, b, m, Legis.DL_FAVOR);
        Check.isTrue(b.sweet[m.bloc] > sweet, "calling in the favor moves their bloc");
        Check.isTrue(Relations.owed(g, m) == null, "and uses it up");
    }

    @Test
    public void aBrokenPromiseCostsTrust() {
        Game g = Games.start(2, 2029);
        Member m = swing(g);
        Relations.promise(g, m, "A visit", m.state, g.week + 1);
        double rel = m.rel;
        Arrays.fill(g.act, AC_FREE);
        for (int i = 0; i < 3; i++) { Relations.week(g); g.week++; }
        Check.equal(1, g.cong.broken, "the deadline passed without a visit");
        Check.isTrue(m.rel < rel - 15, "and the member remembers");
    }

    @Test
    public void aTripToTheirStateKeepsThePromise() {
        Game g = Games.start(2, 2029);
        Member m = swing(g);
        Relations.promise(g, m, "A visit", m.state, g.week + 4);
        Arrays.fill(g.act, AC_FREE);
        g.act[4] = AC_TRIP;
        g.arg[4] = m.state;
        Relations.week(g);
        Check.equal(1, g.cong.kept, "the promise is kept");
        Check.isTrue(Relations.owed(g, m) != null, "and earns a favor");
    }
}
