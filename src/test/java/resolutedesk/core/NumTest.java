package resolutedesk.core;

import static resolutedesk.core.Num.*;

import resolutedesk.testkit.Check;
import resolutedesk.testkit.Test;

public class NumTest {
    @Test
    public void clampKeepsValuesInRange() {
        Check.equal(5.0, clamp(7, 0, 5), "above the range");
        Check.equal(0.0, clamp(-2, 0, 5), "below the range");
        Check.equal(3.0, clamp(3, 0, 5), "inside the range");
        Check.equal(4, clampi(9, 1, 4), "integers");
    }

    @Test
    public void logisticIsSymmetricAndInvertedByLogit() {
        Check.near(0.5, logistic(0), 1e-12, "centre");
        Check.near(1.0, logistic(2) + logistic(-2), 1e-12, "symmetry");
        Check.near(0.73, logistic(logit(0.73)), 1e-9, "logit inverts logistic");
    }

    @Test
    public void easingStartsAndEndsInPlace() {
        Check.equal(0.0, ease(0), "ease at 0");
        Check.equal(1.0, ease(1), "ease at 1");
        Check.equal(1.0, easeOut(3), "easeOut clamps");
        Check.near(2.5, lerp(2, 3, 0.5), 1e-12, "lerp");
    }
}
