package resolutedesk.sim;

import static resolutedesk.data.ElectoralMap.*;
import static resolutedesk.data.StateData.*;
import static resolutedesk.sim.Schedule.*;

import java.util.Arrays;
import resolutedesk.campaign.Reelect;
import resolutedesk.campaign.Reelection;
import resolutedesk.model.Game;
import resolutedesk.ui.Rooms;

/** What a hands-off president does each week: used by the test harness and by watch mode. */
public final class Auto {
    public static void week(Game g) {
        int w = g.week;
        if (w % 4 == 0) Week.routine(g);
        if (w % 8 == 3) Rooms.roadWeek(g);
        if (g.stamina < 35) Rooms.restWeek(g);
        if (Reelection.season(g)) campaign(g);
        if (w % 6 == 1) Decisions.autoAgenda(g);
        Decisions.autoWhip(g);
        Decisions.autoExec(g);
    }

    /** In the fall of an election year: two stops a week in the closest big states. */
    static void campaign(Game g) {
        Reelect re = g.re;
        Integer[] ord = new Integer[NS];
        for (int i = 0; i < NS; i++) ord[i] = i;
        Arrays.sort(ord, (a, b) -> Double.compare(Math.abs(re.st[a]) - STATES[a].ev * 0.15, Math.abs(re.st[b]) - STATES[b].ev * 0.15));
        int k = 0;
        int[] slots = {4, 7};
        for (int n = 0; n < NS && k < slots.length; n++) {
            int s = ord[n];
            if (!votes(s, re.year) || STATES[s].ab.equals("DC")) continue;
            g.act[slots[k]] = AC_CAMPAIGN; g.arg[slots[k]] = s;
            k++;
        }
    }
}
