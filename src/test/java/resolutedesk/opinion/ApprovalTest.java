package resolutedesk.opinion;

import resolutedesk.model.Game;
import resolutedesk.testkit.Check;
import resolutedesk.testkit.Games;
import resolutedesk.testkit.Test;

/** Approval: its range, what moves it, and the partisan floor and ceiling. */
public class ApprovalTest {
    @Test
    public void approvalStaysOnTheScaleForAWholeTerm() {
        Game g = Games.start(11, 2029);
        for (int w = 0; w < 200; w += 10) {
            Games.play(g, 10);
            Check.between(4, 96, g.approval, "approval at week " + g.week);
            for (double v : g.issueApp) Check.between(0, 100, v, "approval by issue");
        }
    }

    @Test
    public void aScandalPullsTheTargetDown() {
        Game g = Games.start(3, 2029);
        double before = Approval.target(g);
        g.scandal += 8;
        Check.near(before - 8 * Approval.SCANDAL_WEIGHT, Approval.target(g), 1e-9, "inside the partisan bounds, every point of scandal comes off the target");
    }

    @Test
    public void todaysPartisansKeepAFloorUnderAPresident() {
        Game g = Games.start(3, 2025);
        g.scandal += 30;
        Check.between(26, 35, Approval.target(g), "a ruinous scandal in 2025: the president's own voters hold the line around 30");
        g.scandal += 100;
        Check.isTrue(Approval.target(g) > 18, "even with everything going wrong, a partisan floor remains");
    }

    @Test
    public void beforeNinetyThereWasNoFloor() {
        Game g = Games.start(3, 1949);
        g.scandal += 40;
        Check.isTrue(Approval.target(g) < 25, "in 1949 a collapse goes all the way down, as Truman's did: " + Approval.target(g));
    }

    @Test
    public void aRallyBreaksThroughTheCeiling() {
        Game g = Games.start(3, 2001);
        double ceiling = Approval.bounds(g)[1];
        g.rally += 22;
        Check.isTrue(Approval.target(g) > ceiling + 10, "after an attack, approval goes well past the partisan ceiling of " + Math.round(ceiling));
        Check.isTrue(Approval.target(g) < 96, "but never to 100");
    }

    @Test
    public void aHoneymoonFadesWithTime() {
        Game g = Games.start(3, 2029);
        double start = g.honeymoon;
        Games.play(g, 52);
        Check.isTrue(g.honeymoon < start / 2, "a year in, most of the honeymoon is gone");
    }

    @Test
    public void aLandslideBringsABiggerHoneymoon() {
        Game jfk = Games.start(3, 1961), lbj = Games.start(3, 1965);
        Check.isTrue(lbj.honeymoon > jfk.honeymoon + 5, "1964's landslide gives a far bigger honeymoon than 1960's squeaker");
    }

    @Test
    public void approvalMovesAGoodDealOverATerm() {
        double swing = 0;
        for (int seed = 1; seed <= 6; seed++) {
            Game g = Games.start(seed, 2025);
            double lo = g.approval, hi = g.approval;
            for (int w = 0; w < 200 && !g.over; w++) { Games.play(g, 1); lo = Math.min(lo, g.approval); hi = Math.max(hi, g.approval); }
            swing += (hi - lo) / 6;
        }
        Check.isTrue(swing > 12, "in a polarized era a term still spans more than a dozen points on average: " + swing);
    }
}
