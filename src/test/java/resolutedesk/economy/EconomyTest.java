package resolutedesk.economy;

import resolutedesk.model.Game;
import resolutedesk.testkit.Check;
import resolutedesk.testkit.Games;
import resolutedesk.testkit.Test;

public class EconomyTest {
    @Test
    public void theSameSeedGivesTheSameEconomy() {
        Game a = Games.start(99, 2029), b = Games.start(99, 2029);
        Games.play(a, 60);
        Games.play(b, 60);
        Check.equal(a.econ.unemp, b.econ.unemp, "unemployment");
        Check.equal(a.econ.infl, b.econ.infl, "inflation");
        Check.equal(a.econ.debt, b.econ.debt, "debt");
    }

    @Test
    public void theEconomyStaysWithinRealisticBoundsInEveryEra() {
        for (int year : new int[]{1933, 1945, 1977, 2009, 2025}) {
            Game g = Games.start(5, year);
            for (int w = 0; w < 4; w++) {
                Games.play(g, 52);
                Check.between(1, 30, g.econ.unemp, year + " unemployment");
                Check.between(-12, 25, g.econ.infl, year + " inflation");
                Check.between(0, 30, g.econ.fed, year + " the Fed's rate");
            }
        }
    }

    @Test
    public void theDepressionStartsWithAQuarterOfWorkersIdle() {
        Game g = Games.start(1, 1933);
        Check.between(20, 27, g.econ.unemp, "unemployment in March 1933");
    }
}
