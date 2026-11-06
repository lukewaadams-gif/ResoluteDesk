package resolutedesk.testkit;

import static resolutedesk.data.Candidates.candById;
import static resolutedesk.data.Eras.era;
import static resolutedesk.sim.Rosters.defaultVp;
import static resolutedesk.sim.Rosters.eraRoster;

import resolutedesk.model.Era;
import resolutedesk.model.Game;
import resolutedesk.model.Settings;
import resolutedesk.opinion.Elect;
import resolutedesk.sim.Auto;
import resolutedesk.sim.Decisions;
import resolutedesk.sim.Setup;
import resolutedesk.sim.Week;

/** Games for the tests: new presidencies, and weeks played the way the hands-off president plays them. */
public final class Games {
    private static boolean ready;

    private Games() {
    }

    /** A new game in the given era, with the real winner unless a roster id is given, and scenario 0 ("As it was"). */
    public static Game start(long seed, int year) {
        return start(seed, year, null, 0);
    }

    public static Game start(long seed, int year, String presId, int scenario) {
        return start(settings(seed, year, presId, scenario));
    }

    /** The settings for a new game, to change before starting it. */
    public static Settings settings(long seed, int year, String presId, int scenario) {
        Settings s = new Settings();
        s.year = year;
        s.seed = seed;
        s.scenario = scenario;
        Era e = era(year);
        s.presId = presId != null ? presId : eraRoster(e).get(0).id;
        s.vpId = defaultVp(e, candById(s.presId));
        return s;
    }

    public static Game start(Settings s) {
        if (!ready) { Elect.init(); ready = true; }
        return Setup.newGame(s);
    }

    /** Plays up to n weeks, stopping early if the presidency ends. */
    public static void play(Game g, int weeks) {
        for (int w = 0; w < weeks && !g.over && !g.removed; w++) {
            Auto.week(g);
            Week.resolve(g);
            Decisions.autoAll(g);
        }
    }

    /** Plays until the presidency ends (at most twelve years). */
    public static void playToEnd(Game g) {
        play(g, 52 * 12 + 10);
    }
}
