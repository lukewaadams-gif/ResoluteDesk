package resolutedesk.history;

import static resolutedesk.data.ElectoralMap.*;
import static resolutedesk.data.Eras.ERAS;
import static resolutedesk.history.EraRules.termYear;

import resolutedesk.model.Era;
import resolutedesk.model.Game;
import resolutedesk.testkit.Check;
import resolutedesk.testkit.Games;
import resolutedesk.testkit.Test;

public class EraRulesTest {
    @Test
    public void everyInaugurationFrom1933IsPlayable() {
        Check.equal(25, ERAS.size(), "eras");
        int last = 0;
        for (Era e : ERAS) {
            Check.isTrue(e.year > last, "eras in order");
            Check.equal(0, (e.year - 1933) % 4, e.year + " is an inauguration year");
            last = e.year;
        }
    }

    @Test
    public void electoralVotesFollowTheCensus() {
        Check.equal(531, evTotal(evFor(1956)), "48 states");
        Check.equal(537, evTotal(evFor(1960)), "Alaska and Hawaii, before DC");
        Check.equal(538, evTotal(evFor(1964)), "DC votes from 1964");
        Check.equal(538, evTotal(evFor(2024)), "today");
    }

    @Test
    public void theTermEndsOnTheRightDay() {
        Game g = Games.start(1, 1933);
        Check.equal(1933, termYear(g), "Roosevelt's first term began in 1933");
        Check.isTrue(g.termWeeks > 200 && g.termWeeks < 205, "March 1933 to January 1937 is a short term: " + g.termWeeks + " weeks");
    }
}
