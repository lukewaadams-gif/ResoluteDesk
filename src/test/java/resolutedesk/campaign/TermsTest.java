package resolutedesk.campaign;

import resolutedesk.model.Game;
import resolutedesk.testkit.Check;
import resolutedesk.testkit.Games;
import resolutedesk.testkit.Test;

/** The two-term limit of the 22nd Amendment, which applies to elections from 1952. */
public class TermsTest {
    @Test
    public void rooseveltCouldRunForAThirdTerm() {
        Game g = Games.start(1, 1937);
        Check.isTrue(Terms.eligible(g), "no limit before 1952");
    }

    @Test
    public void aFirstTermPresidentCanRunAgain() {
        Check.isTrue(Terms.eligible(Games.start(1, 2029)), "2032");
        Check.isTrue(Terms.eligible(Games.start(1, 1961)), "Kennedy in 1964");
    }

    @Test
    public void aSecondTermPresidentCannot() {
        Check.isTrue(!Terms.eligible(Games.start(1, 2025)), "Trump's second term");
        Check.isTrue(!Terms.eligible(Games.start(1, 1973)), "Nixon's second term");
    }
}
