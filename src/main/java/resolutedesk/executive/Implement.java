package resolutedesk.executive;

import static resolutedesk.core.Num.*;
import static resolutedesk.executive.Posts.*;
import static resolutedesk.executive.ScandalData.*;
import static resolutedesk.model.NewsKinds.*;

import resolutedesk.model.Game;

/** When the government does something, does it work? Depends on the department's readiness. */
public final class Implement {
    public static boolean roll(Game g, String what, int area, double weight) {
        if (g.ex == null) return false;
        int post = ep(AREA_POST[area]);
        double rd = g.ex.ready[post];
        double p = clamp((66 - rd) / 110, 0.02, 0.45);
        if (g.rng.nextDouble() >= p) return false;
        g.ex.failures++;
        String[] how = {"a botched rollout", "computer systems that crash on day one", "confusing guidance and long lines", "missed deadlines and lost paperwork"};
        g.log(NK_BAD, POST_SHORT[post] + " stumbles: " + how[g.rng.nextInt(how.length)] + " for " + what.toLowerCase(), "Officials blame staffing and old systems. The department's readiness is " + Math.round(rd) + " of 100.", 1.4 * weight + 0.4);
        g.issueAdj[area] = clamp(g.issueAdj[area] - 0.15 * weight, -1.5, 1.5);
        g.perf -= 0.4 * weight;
        if (g.rng.nextDouble() < 0.4) Trouble.add(g, SK_FAILURE, "The " + what.toLowerCase() + " rollout", "Inspectors general and reporters dig into what went wrong at " + POST_NAME[post] + ".", 1.5, null, false);
        return true;
    }
}
