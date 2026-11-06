package resolutedesk.press;

import static resolutedesk.congress.CongressRules.*;
import static resolutedesk.core.Fmt.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.data.Names.*;
import static resolutedesk.executive.ActionData.*;
import static resolutedesk.model.Attributes.*;
import static resolutedesk.model.NewsKinds.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.opinion.Issues.*;
import static resolutedesk.press.Outlets.*;
import static resolutedesk.sim.DecisionKinds.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import resolutedesk.congress.Bill;
import resolutedesk.executive.Scandal;
import resolutedesk.home.Home;
import resolutedesk.home.HomeFront;
import resolutedesk.model.Cand;
import resolutedesk.model.Game;
import resolutedesk.sim.Decision;
import resolutedesk.sim.Decisions;
import resolutedesk.sim.Week;
import resolutedesk.world.World;

/** Press conferences and interviews: three questions each. */
public final class QA {
    static final String[] ANSWERS = {"Answer it straight", "Pivot to your message", "Push back on the premise", "Make news: announce something"};

    static String reporter(Game g, int outlet, int n) {
        Random r = new Random(g.set.seed * 13 + outlet * 101 + n);
        boolean f = genFemale(r, 0.5);
        return genFirst(r, f) + " " + genLast(r);
    }

    static List<String> topics(Game g) {
        List<String> t = new ArrayList<String>();
        for (Scandal s : g.ex.scandals) if (!s.over && s.severity >= 2) { t.add("about " + s.title.toLowerCase()); break; }
        if (g.econ.infl > 3) t.add("about prices: inflation is " + fmt1(g.econ.infl) + "%");
        if (g.econ.unemp > 5) t.add("about jobs: unemployment is " + fmt1(g.econ.unemp) + "%");
        World w = g.world;
        if (w != null && w.twStage >= 2) t.add("about China's pressure on Taiwan");
        if (w != null && w.uaStage == 2) t.add("about the war in Ukraine");
        if (w != null && w.iran > 75) t.add("about Iran's nuclear program");
        for (Bill b : g.cong.bills) if (b.ours && b.stage >= ST_COMMITTEE && b.stage <= ST_FLOOR2) { t.add("about " + b.name + " in Congress"); break; }
        int worst = HomeFront.worstIssue(g);
        t.add("about " + PISSUE[worst].toLowerCase() + ", where you're weakest");
        t.add("about your approval rating, " + Math.round(g.approval) + "%");
        t.add("about your health and stamina");
        return t;
    }

    /** type 0: a press conference; 1: an interview with one outlet. */
    public static void start(Game g, int type, int outlet) {
        Home h = g.home;
        h.qaType = type; h.qaStep = 0; h.qaScore = 0; h.qaOutlet = outlet;
        List<String> t = topics(g);
        for (int i = 0; i < 3; i++) h.qaTopic[i] = t.get(Math.min(i, t.size() - 1));
        if (t.size() > 3) { Collections.shuffle(t, new Random(g.week * 7 + type)); for (int i = 0; i < 3; i++) h.qaTopic[i] = t.get(i); }
        g.stamina = Math.max(0, g.stamina - (type == 0 ? 3 : 2));
        ask(g);
    }

    static void ask(Game g) {
        Home h = g.home;
        int o = h.qaType == 1 ? h.qaOutlet : new int[]{3, 7, 0, 5, 2, 11, 9}[(g.week + h.qaStep * 3) % 7];
        String who = h.qaType == 1 ? (OUT_NAME[o].startsWith("The ") ? "The host" : "The interviewer") : reporter(g, o, h.qaStep + g.week) + " of " + OUT_NAME[o];
        String q = h.qaTopic[h.qaStep];
        Decision d = Decisions.add(g, D_QA, (h.qaType == 0 ? "Press conference: question " : "Interview with " + OUT_NAME[o] + ": question ") + (h.qaStep + 1) + " of 3",
                who + " asks " + q + ".", ANSWERS,
                new String[]{"Press secretary: \"Simple and honest. They respect that.\"", "Communications director: \"Get back to what we want to talk about.\"", "Political director: \"Don't accept their framing.\"", "Chief of staff: \"Give them a headline of our choosing.\""});
        d.a = o;
    }

    public static void answer(Game g, Decision d, int k) {
        Home h = g.home;
        Cand p = g.pres;
        Random r = g.rng;
        int o = d.a;
        double side = g.party == REP ? 1 : -1;
        double hostile = Math.max(0, -OUT_LEAN[o] * side);
        double skill = k == 0 ? p.a[A_INT] : k == 1 ? p.a[A_DIS] : k == 2 ? p.a[A_DEB] : p.a[A_CHA];
        double pOk = clamp(0.35 + (skill - 55) / 80 - hostile * 0.2, 0.1, 0.92);
        boolean ok = r.nextDouble() < pOk;
        h.qaScore += ok ? (k == 2 ? 0.6 : k == 3 ? 0.8 : 0.5) * (1 + hostile * 0.5) : (k == 3 ? -1.0 : -0.6);
        if (k == 2) { h.press = clamp(h.press - 2, 0, 100); h.fac[0] = Math.min(100, h.fac[0] + 1); }
        if (k == 3 && ok) { int i = Week.topIssue(g); g.issueSal[i] = Math.min(2, g.issueSal[i] + 0.1); g.issueAdj[i] = clamp(g.issueAdj[i] + 0.05, -1.5, 1.5); }
        if (!ok && p.a[A_DIS] < 55 && r.nextDouble() < 0.4) { g.log(NK_BAD, "A gaffe at the " + (h.qaType == 0 ? "press conference" : "interview") + " dominates the news", null, 1.8); h.qaScore -= 0.6; }
        h.qaStep++;
        if (h.qaStep < 3) { ask(g); return; }
        double s = h.qaScore + g.rng.nextGaussian() * 0.3;
        double k2 = h.qaType == 1 ? OUT_AUD[h.qaOutlet] / 8.0 : 1.2;
        g.perf += clamp(s, -2, 2.5) * 0.35 * Math.min(1.6, k2);
        h.press = clamp(h.press + (h.qaType == 0 ? 2 : 0.5), 0, 100);
        if (h.qaType == 0) { h.pressers++; h.lastPresser = g.week; } else { h.interviews++; h.lastInterview = g.week; if (hostile > 0.3 && s > 0) for (int gi : new int[]{21, 4}) groupBump(g, gi, 0.5); }
        String verdict = s > 1.5 ? "sharp and in command" : s > 0.3 ? "steady" : s > -0.8 ? "uneven" : "rattled";
        g.log(s > 0.3 ? NK_GOOD : NK_BAD, "Reviews of the president's " + (h.qaType == 0 ? "press conference" : "interview with " + OUT_NAME[h.qaOutlet]) + ": " + verdict, null, 1.5);
        Decisions.info(g, h.qaType == 0 ? "After the press conference" : "After the interview", "The verdict: " + verdict + ".");
        h.qaType = -1;
    }
}
