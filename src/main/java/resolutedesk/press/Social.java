package resolutedesk.press;

import static resolutedesk.core.Num.*;
import static resolutedesk.executive.ActionData.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.model.Attributes.*;
import static resolutedesk.model.NewsKinds.*;

import java.util.Random;
import resolutedesk.congress.Bloc;
import resolutedesk.home.Home;
import resolutedesk.model.Cand;
import resolutedesk.model.Game;

/** The president's posts: what they say and how the country reacts. */
public final class Social {
    public static String post(Game g, int issue, int tone) {
        Home h = g.home;
        Cand p = g.pres;
        Random r = g.rng;
        if (h.postWeek != g.week) { h.postWeek = g.week; h.postsThisWeek = 0; }
        if (h.postsThisWeek >= 5) return yearNow(g) < 2009 ? "Five statements a week is plenty; the press office needs a rest." : "Your staff take your phone away: five posts a week is plenty.";
        h.postsThisWeek++; h.posts++;
        boolean old = yearNow(g) < 2009;
        double reach = ((old ? (p.a[A_SPE] + p.a[A_CHA]) / 2.0 : p.a[A_SMD]) / 70.0) * (1 - 0.15 * (h.postsThisWeek - 1));
        double risk = (tone >= 2 ? 0.08 : 0.02) + Math.max(0, 60 - p.a[A_DIS]) / 300.0;
        if (r.nextDouble() < risk) {
            g.perf -= 0.5;
            g.log(NK_BAD, "The president's " + (yearNow(g) < 2009 ? "remarks " : "post ") + (tone == 4 ? "fall flat and" : "") + " set off a backlash", null, 1.5);
            return "It backfired. The post is the story of the day, and not in a good way.";
        }
        h.followers += 0.15 * reach;
        switch (tone) {
            case 0: g.perf += 0.12 * reach; g.issueAdj[issue] = clamp(g.issueAdj[issue] + 0.02 * reach, -1.5, 1.5); break;
            case 1: g.issueAdj[issue] = clamp(g.issueAdj[issue] + 0.03 * reach, -1.5, 1.5); break;
            case 2: h.fac[0] = Math.min(100, h.fac[0] + 1.5 * reach); for (Bloc b : g.cong.blocs) if (b.party != g.party) b.rel = Math.max(0, b.rel - 1); g.issueSal[issue] = Math.min(2, g.issueSal[issue] + 0.04); break;
            case 3: g.perf += 0.08 * reach; groupBump(g, 0, 0.2 * reach); break;
            default: g.perf += 0.1 * reach * (p.a[A_CHA] / 70.0); groupBump(g, 0, 0.3 * reach); break;
        }
        g.issueSal[issue] = Math.min(2, g.issueSal[issue] + 0.03);
        return (yearNow(g) < 2009 ? "Done. " : "Posted. ") + (reach > 0.9 ? "It's everywhere within the hour." : reach > 0.6 ? "It gets solid engagement." : "It gets a modest response.");
    }
}
