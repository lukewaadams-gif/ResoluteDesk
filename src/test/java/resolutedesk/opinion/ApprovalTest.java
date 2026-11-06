package resolutedesk.opinion;

import resolutedesk.model.Game;
import resolutedesk.testkit.Check;
import resolutedesk.testkit.Games;
import resolutedesk.testkit.Test;

public class ApprovalTest {
    @Test
    public void approvalStaysOnTheScaleForAWholeTerm() {
        Game g = Games.start(11, 2029);
        for (int w = 0; w < 200; w += 10) {
            Games.play(g, 10);
            Check.between(8, 92, g.approval, "approval at week " + g.week);
            for (double v : g.issueApp) Check.between(0, 100, v, "approval by issue");
        }
    }

    @Test
    public void aScandalPullsTheTargetDown() {
        Game g = Games.start(3, 2029);
        double before = Approval.target(g);
        g.scandal += 8;
        Check.near(before - 8, Approval.target(g), 1e-9, "every point of scandal comes off the target");
    }

    @Test
    public void aHoneymoonFadesWithTime() {
        Game g = Games.start(3, 2029);
        double start = g.honeymoon;
        Games.play(g, 52);
        Check.isTrue(g.honeymoon < start / 2, "a year in, most of the honeymoon is gone");
    }
}
