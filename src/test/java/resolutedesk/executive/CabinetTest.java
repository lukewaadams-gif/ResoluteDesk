package resolutedesk.executive;

import static resolutedesk.executive.Posts.*;

import java.util.ArrayList;
import java.util.List;
import resolutedesk.model.Game;
import resolutedesk.testkit.Check;
import resolutedesk.testkit.Games;
import resolutedesk.testkit.Test;

public class CabinetTest {
    /** Regression: everyone in a new administration used to share a handful of surnames. */
    @Test
    public void everyoneInANewAdministrationHasTheirOwnSurname() {
        for (int year : new int[]{1933, 1961, 1981, 2009, 2029}) {
            for (long seed = 1; seed <= 6; seed++) {
                Game g = Games.start(seed * 1000, year);
                List<String> surnames = new ArrayList<String>();
                for (Person p : g.ex.staff) if (p != null) surnames.add(Cabinet.surname(p.name));
                for (Person p : g.ex.nominee) if (p != null) surnames.add(Cabinet.surname(p.name));
                Check.distinct(surnames, year + " seed " + seed);
                Check.isTrue(!surnames.contains(g.pres.last), year + ": no one shares the president's surname");
            }
        }
    }

    @Test
    public void eachJobOffersThreeDifferentPeople() {
        Game g = Games.start(4242, 2029);
        for (int post = 0; post < NPOST; post++) {
            List<String> names = new ArrayList<String>();
            for (Person p : Cabinet.options(g, post, 1)) names.add(Cabinet.surname(p.name));
            Check.equal(3, names.size(), "choices for " + POST_NAME[post]);
            Check.distinct(names, "choices for " + POST_NAME[post]);
        }
    }

    @Test
    public void theRealCabinetServesTheRealPresidentIn2025() {
        Game g = Games.start(7, 2025);
        Check.equal("Marco Rubio", g.ex.nominee[P_STATE].name, "secretary of state");
        Check.isTrue(g.ex.nominee[P_STATE].real, "a real person");
    }

    @Test
    public void departmentsThatDidNotExistYetHandTheirWorkToOlderOnes() {
        Game g = Games.start(1, 1941);
        Check.isTrue(!postOn(P_DHS), "no Homeland Security in 1941");
        Check.equal(P_DOJ, ep(P_DHS), "Justice does its work");
        Check.equal(P_HHS, ep(P_ED), "in 1941 the Federal Security Agency does Education's work");
        Games.start(1, 1933);
        Check.equal(P_LAB, ep(P_ED), "in 1933, before the agency existed, Labor does it");
    }
}
