package resolutedesk.executive;

import static resolutedesk.data.StateData.*;
import static resolutedesk.executive.Delegation.*;
import static resolutedesk.executive.Posts.*;
import static resolutedesk.sim.Schedule.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import resolutedesk.campaign.Reelection;
import resolutedesk.congress.Midterms;
import resolutedesk.model.Game;
import resolutedesk.sim.Week;
import resolutedesk.ui.Rooms;

/** Delegation: what the chief of staff and others do with the areas you hand them. */
public final class Delegate {
    public static void plan(Game g) {
        if (g.ex == null || !g.ex.deleg[DG_SCHEDULE]) return;
        double q = Cabinet.sq(g, S_COS);
        Week.routine(g);
        if (g.stamina < 35 + (1 - q) * 15) Rooms.restWeek(g);
        else if (g.week % 5 == 2 && q > 0.5) {
            List<Integer> sw = new ArrayList<Integer>();
            for (int s = 0; s < NS; s++) sw.add(s);
            final Game gg = g;
            Collections.sort(sw, (a, b) -> Double.compare(Math.abs(gg.stateApp[a] - 48) - STATES[a].ev * 0.15, Math.abs(gg.stateApp[b] - 48) - STATES[b].ev * 0.15));
            g.act[7] = AC_TRIP; g.arg[7] = sw.get(0);
            if (Midterms.season(g) || Reelection.season(g)) { g.act[10] = AC_CAMPAIGN; g.arg[10] = sw.get(1); }
        }
        if (g.week - g.ex.lastCabinet > 6) g.act[4] = AC_CABINET;
    }
}
