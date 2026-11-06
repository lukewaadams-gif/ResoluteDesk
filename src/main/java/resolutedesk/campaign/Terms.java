package resolutedesk.campaign;

import static resolutedesk.campaign.CampaignData.*;
import static resolutedesk.core.Dates.*;
import static resolutedesk.executive.Posts.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.model.NewsKinds.*;
import static resolutedesk.model.Parties.*;

import java.util.Random;
import resolutedesk.congress.Relations;
import resolutedesk.executive.Cabinet;
import resolutedesk.executive.Person;
import resolutedesk.model.Game;
import resolutedesk.sim.Decisions;

/** Terms of office: the two-term limit, the end of a term, and the start of a second one. */
public final class Terms {
    /** Has the president already served the maximum the Constitution allows by the year of the coming election? */
    public static boolean eligible(Game g) {
        int y = Reelection.year(g);
        if (y < 1952) return true;
        return g.prior + g.term < 2;
    }

    /** Called when the week reaches the end of a term. */
    public static void check(Game g) {
        if (g.week < g.termWeeks || g.over) return;
        Reelect re = g.re;
        if (re != null && re.done && re.won && re.running) startTerm(g);
        else {
            g.over = true;
            if (g.ending == END_NONE) g.ending = re == null || !re.done ? END_SERVED : re.denied ? END_DENIED : !re.running ? (re.eligible ? END_RETIRED : END_SERVED) : END_LOST;
        }
    }

    static void startTerm(Game g) {
        Reelect re = g.re;
        g.term++;
        g.termStart = g.week;
        g.termWeeks = termEnd(g);
        g.re = null;
        g.cong.midDone = false;
        g.honeymoon += 2;
        if (re != null) Relations.elected(g, re.popMe - re.popOpp);
        if (re != null && re.nomVp != null && re.nomVp != g.vp) g.vp = re.nomVp;
        // second-term departures: a few secretaries and senior staff move on
        Random r = g.rng;
        int gone = 0;
        for (int k = 0; k < NPOST && gone < 4; k++) {
            Person p = g.ex.post[k];
            if (p == null || p.real || r.nextDouble() > 0.22) continue;
            Cabinet.remove(g, p);
            g.ex.departures++;
            g.log(NK_INFO, p.name + " steps down as " + lcTitle(p.title()) + " at the end of the term", null, 1.0);
            gone++;
        }
        int[] dt = dateOf(g.day());
        g.log(NK_BIG, ("President " + g.pres.last + " is sworn in for a " + (g.term == 2 ? "second" : g.term == 3 ? "third" : "fourth") + " term").toUpperCase(),
                "The oath is taken again at noon on January 20, " + dt[0] + ". " + PARTIES[g.party] + " hold " + g.myHouse() + " House seats and " + g.mySenate() + " Senate seats.", 5);
        g.milestones.add(dateStr(g.day()) + ": sworn in for term " + g.term);
        Decisions.info(g, "Four More Years", "You took the oath again today. " + (gone > 0 ? gone + (gone == 1 ? " member" : " members") + " of your cabinet are leaving; their deputies are acting until you name successors in the West Wing. " : "")
                + "Second terms are hard: the honeymoon is short, the other party smells a lame duck, and history is watching.");
    }
}
