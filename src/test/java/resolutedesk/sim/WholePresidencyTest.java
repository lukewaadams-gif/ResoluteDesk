package resolutedesk.sim;

import static resolutedesk.model.Tracking.H_APP;

import resolutedesk.model.Game;
import resolutedesk.testkit.Check;
import resolutedesk.testkit.Games;
import resolutedesk.testkit.Test;

/** Whole presidencies, played by the hands-off president from inauguration to the last day. */
public class WholePresidencyTest {
    private static Game finish(long seed, int year) {
        Game g = Games.start(seed, year);
        Games.playToEnd(g);
        Check.isTrue(g.over || g.removed, year + ": the presidency ends");
        Check.isTrue(g.week >= 200 || g.removed, year + ": the presidency lasts at least a term, or ends in removal");
        Check.isTrue(g.hist.size() >= g.week, year + ": a weekly record is kept");
        for (double[] h : g.hist) Check.between(0, 100, h[H_APP], year + ": recorded approval");
        return g;
    }

    /** Before the 22nd Amendment there is no limit, so a popular Roosevelt can keep winning. */
    @Test
    public void theNewDeal() {
        Game g = Games.start(1, 1933);
        Games.play(g, 52 * 8);
        Check.isTrue(g.over || g.term >= 2, "Roosevelt either leaves or wins again");
        Check.isTrue(g.hist.size() >= g.week, "a weekly record is kept");
    }

    @Test
    public void theColdWar() {
        finish(2, 1961);
    }

    @Test
    public void theEighties() {
        finish(3, 1981);
    }

    @Test
    public void thePresent() {
        finish(4, 2025);
    }

    @Test
    public void theWorldOfBattleground2028() {
        Game g = finish(5, 2029);
        Check.isTrue(g.re != null && g.re.done, "an election is held in 2032");
    }

    @Test
    public void lawsPassAndTheCabinetFillsUp() {
        Game g = Games.start(6, 2029);
        Games.play(g, 104);
        Check.isTrue(g.cong.laws > 0, "some laws pass in two years");
        int filled = 0;
        for (Object p : g.ex.post) if (p != null) filled++;
        Check.isTrue(filled >= 18, "most of the cabinet is confirmed after two years: " + filled);
    }
}
