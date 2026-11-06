package resolutedesk.sim;

import static resolutedesk.model.Parties.REP;

import resolutedesk.executive.Courts;
import resolutedesk.executive.Justice;
import resolutedesk.model.Game;
import resolutedesk.model.Settings;
import resolutedesk.testkit.Check;
import resolutedesk.testkit.Games;
import resolutedesk.testkit.Test;

/** The world a projected 2029 inherits, set with the setup screen's sliders. */
public class InheritTest {
    static Settings s2029() { return Games.settings(12, 2029, null, 0); }

    @Test
    public void theCourtCanLeanEitherWay() {
        Settings s = s2029();
        s.cCourtR = 3;
        Game g = Games.start(s);
        Check.equal(9, g.ex.court.size(), "nine justices");
        Check.equal(3, Courts.count(g, REP), "three appointed by Republicans");
        int made = 0;
        for (Justice j : g.ex.court) if (!j.real) made++;
        Check.equal(3, made, "the justices who retired were replaced by made-up ones");
        Check.equal(6, Courts.count(Games.start(s2029()), REP), "left alone, the Court is as it is today");
    }

    @Test
    public void tensionsAbroadSetTheHotSpots() {
        Settings s = s2029();
        s.cTaiwan = 90; s.cUkraine = 10; s.cMideast = 80;
        Game g = Games.start(s);
        Check.near(90, g.world.twt, 1e-9, "tension in the Strait");
        Check.equal(2, g.world.twStage, "Beijing drilling around the island");
        Check.equal(0, g.world.uaStage, "a peace agreement in Ukraine");
        Check.near(80, g.world.iran, 1e-9, "Iran's program");
    }

    @Test
    public void aBiggerWinBringsABiggerMandate() {
        Settings narrow = s2029(), wide = s2029();
        narrow.cMargin = 0.5;
        wide.cMargin = 12;
        Check.isTrue(Games.start(wide).cong.mandate > Games.start(narrow).cong.mandate + 3, "the mandate grows with the margin");
    }

    @Test
    public void theDeficitIsWhatYouSet() {
        Settings s = s2029();
        s.cDeficit = 2;
        Check.near(2, Games.start(s).econ.deficit, 0.01, "deficit");
    }
}
