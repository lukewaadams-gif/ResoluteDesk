package resolutedesk.sim;

import static resolutedesk.core.Dates.*;
import static resolutedesk.core.Fmt.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.data.Candidates.*;
import static resolutedesk.data.Eras.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.model.Attributes.*;
import static resolutedesk.model.NewsKinds.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.sim.Rosters.*;

import java.util.Random;
import resolutedesk.congress.Hill;
import resolutedesk.economy.Economy;
import resolutedesk.executive.Cabinet;
import resolutedesk.history.History;
import resolutedesk.home.HomeFront;
import resolutedesk.model.Game;
import resolutedesk.model.Settings;
import resolutedesk.opinion.Approval;
import resolutedesk.world.Diplo;

/** Starting a new presidency from the settings chosen before the game. */
public final class Setup {
    public static Game newGame(Settings s) {
        Game g = new Game();
        g.set = s;
        g.era = era(s.year);
        applyEra(g.era);
        g.rng = new Random(s.seed);
        g.pres = s.custom != null ? s.custom : candById(s.presId);
        if (g.pres == null) g.pres = eraRoster(g.era).get(0);
        g.party = g.pres.party;
        g.vp = s.vpId != null ? candById(s.vpId) : candById(defaultVp(g.era, g.pres));
        if (g.vp == g.pres) g.vp = null;
        g.termWeeks = termEnd(g);
        g.prior = priorTerms(g);
        int[] seats = seatsFor(g.era, g.party);
        int house = g.party == REP ? 435 - seats[0] : seats[0], senate = g.party == REP ? 100 - seats[1] : seats[1];
        if (s.scenario == 3) { house = Math.min(house, 205); senate = Math.min(senate, 46); }
        if (s.scenario == 4) { house += 20; senate += 4; }
        if (s.cHouse >= 0) house = s.cHouse;
        if (s.cSenate >= 0) senate = s.cSenate;
        house = clampi(house, 100, 335); senate = clampi(senate, 20, 80);
        g.houseD = g.party == REP ? 435 - house : house;
        g.senateD = g.party == REP ? 100 - senate : senate;
        g.location = Week.dc();
        g.hill = 45 + (g.pres.a[A_EST] - 60) * 0.3;
        g.stamina = 80 + (g.pres.a[A_STA] - 60) * 0.25;
        g.fitness = 40 + (g.pres.a[A_STA] - 60) * 0.5;
        Economy.init(g);
        Hill.setup(g);
        Cabinet.setup(g);
        Diplo.setup(g);
        Approval.init(g);
        HomeFront.setup(g);
        History.setup(g);
        Week.routine(g);
        Week.record(g);
        String vp = g.vp != null ? " " + g.vp.name() + " is sworn in as vice president." : "";
        g.log(NK_BIG, g.pres.name().toUpperCase() + " IS SWORN IN AS PRESIDENT",
                g.pres.name() + " took the oath of office at noon on " + dateLong(0) + "." + vp + " " + PARTIES[g.party] + " hold " + g.myHouse() + " House seats and " + g.mySenate() + " Senate seats.", 5);
        Decisions.info(g, "Your cabinet", "Your transition team has sent the Senate a full slate of nominees, and your White House staff starts today. "
                + "Until each nominee is confirmed, a career official runs the department. Review or change any of them in the West Wing (press 5).");
        g.log(NK_ECON, "The new president inherits unemployment of " + fmt1(g.econ.unemp) + "% and inflation of " + fmt1(g.econ.infl) + "%", null, 1.5);
        return g;
    }
}
