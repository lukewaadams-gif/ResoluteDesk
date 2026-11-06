package resolutedesk.sim;

import static resolutedesk.core.Dates.*;
import static resolutedesk.core.Fmt.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.data.Candidates.*;
import static resolutedesk.data.ElectionResults.*;
import static resolutedesk.data.Eras.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.model.Attributes.*;
import static resolutedesk.model.NewsKinds.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.sim.Rosters.*;

import java.util.Random;
import resolutedesk.congress.Hill;
import resolutedesk.congress.Relations;
import resolutedesk.economy.Economy;
import resolutedesk.executive.Cabinet;
import resolutedesk.executive.Courts;
import resolutedesk.history.History;
import resolutedesk.home.HomeFront;
import resolutedesk.model.Game;
import resolutedesk.model.Settings;
import resolutedesk.opinion.Approval;
import resolutedesk.policy.Policies;
import resolutedesk.stories.Stories;
import resolutedesk.transition.Transitions;
import resolutedesk.world.Diplo;
import resolutedesk.world.World;

/** Starting a new presidency from the settings chosen before the game. */
public final class Setup {
    /** The popular-vote margin the president won by: the real one for the real winner, a narrow win for a what-if, or the one set before the game. */
    static double margin(Game g) {
        Settings s = g.set;
        if (!Double.isNaN(s.cMargin)) return s.cMargin;
        double m = realMargin(g.era.year);
        boolean realWinner = g.pres != null && g.pres.id != null && g.pres.id.equals(g.era.winnerId);
        if (Double.isNaN(m)) m = 2.0;
        else if (!realWinner) m = WHAT_IF_MARGIN;
        if (s.scenario == 4) m = Math.max(m, 12);                       // a landslide
        return m;
    }

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
        Relations.elected(g, margin(g));
        Cabinet.setup(g);
        Diplo.setup(g);
        inherit(g);
        Approval.init(g);
        HomeFront.setup(g);
        Policies.setup(g);
        Stories.setup(g);
        History.setup(g);
        Week.routine(g);
        if (s.transition) Transitions.begin(g);         // the oath comes when the transition ends
        else inaugurate(g);
        return g;
    }

    /**
     * The world a game inherits, where the player has set it (a projected era's sliders): the balance of the
     * Supreme Court and the tension in the Taiwan Strait, Ukraine and the Middle East.
     */
    static void inherit(Game g) {
        Settings s = g.set;
        if (s.cCourtR >= 0) Courts.reshape(g, s.cCourtR);
        World w = g.world;
        if (!Double.isNaN(s.cTaiwan)) {
            w.twt = s.cTaiwan;
            w.twStage = s.cTaiwan >= 85 ? 2 : s.cTaiwan >= 55 ? 1 : 0;
            w.deter = clamp(75 - s.cTaiwan * 0.35, 30, 80);
        }
        if (!Double.isNaN(s.cUkraine)) {
            double v = s.cUkraine;
            w.uaStage = v < 25 ? 0 : v < 55 ? 1 : 2;
            w.talks = v < 25 ? 100 : v < 55 ? 60 : Math.max(5, 40 - (v - 55));
            w.ukr = v < 55 ? 60 : clamp(75 - (v - 55) * 0.8, 20, 75);
            w.ruGain = v < 55 ? 20 : 20 + (v - 55) * 0.4;
        }
        if (!Double.isNaN(s.cMideast)) {
            double v = s.cMideast;
            w.iran = v; w.gaza = v * 0.9; w.proxy = v * 0.8; w.redsea = v * 0.8; w.terror = 25 + v * 0.3;
        }
    }

    /** Noon on Inauguration Day: the transition's choices take office, and the first news of the presidency. */
    public static void inaugurate(Game g) {
        if (g.trans != null && !g.trans.done) Transitions.finish(g);
        if (g.trans != null) Approval.restart(g);
        Week.record(g);
        String vp = g.vp != null ? " " + g.vp.name() + " is sworn in as vice president." : "";
        g.log(NK_BIG, g.pres.name().toUpperCase() + " IS SWORN IN AS PRESIDENT",
                g.pres.name() + " took the oath of office at noon on " + dateLong(0) + "." + vp + " " + PARTIES[g.party] + " hold " + g.myHouse() + " House seats and " + g.mySenate() + " Senate seats.", 5);
        if (g.trans != null) Decisions.info(g, "Your government", "Your nominees go to the Senate today, the top jobs first, and your White House staff starts work. "
                + "The transition earned a grade of " + Transitions.grade(Transitions.score(g)) + ". Until each nominee is confirmed, a career official runs the department. Review any of them in the West Wing (press 5).");
        else Decisions.info(g, "Your cabinet", "Your transition team has sent the Senate a full slate of nominees, and your White House staff starts today. "
                + "Until each nominee is confirmed, a career official runs the department. Review or change any of them in the West Wing (press 5).");
        g.log(NK_ECON, "The new president inherits unemployment of " + fmt1(g.econ.unemp) + "% and inflation of " + fmt1(g.econ.infl) + "%", null, 1.5);
    }
}
