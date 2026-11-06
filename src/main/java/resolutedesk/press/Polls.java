package resolutedesk.press;

import static resolutedesk.core.Num.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.opinion.Demographics.*;
import static resolutedesk.opinion.Issues.*;

import resolutedesk.home.Home;
import resolutedesk.model.Game;

/** Polls the president can commission, and the public polls. */
public final class Polls {
    public static String commission(Game g, int issue) {
        Home h = g.home;
        if (g.funds < 0.5) return "Your party can't pay for a poll right now (" + partyMoney(0.5) + ").";
        g.funds -= 0.5; h.polls++;
        StringBuilder b = new StringBuilder("Your pollster's numbers on " + PISSUE[issue].toLowerCase() + ": " + Math.round(g.issueApp[issue]) + "% approve. ");
        int[] gs = {21, 4, 5, 0, 1, 12, 10, 8, 9, 22};
        double base = g.issueApp[issue] - g.approval;
        for (int i = 0; i < gs.length; i++) b.append(i > 0 ? ", " : "By group: ").append(GROUP[gs[i]]).append(" ").append(Math.round(clamp(g.groupApp[gs[i]] + base, 2, 98))).append("%");
        b.append(". It matters to ").append(g.issueSal[issue] > 1 ? "a lot of" : g.issueSal[issue] > 0.6 ? "many" : "relatively few").append(" voters right now.");
        h.lastPoll = b.toString();
        return h.lastPoll;
    }
}
