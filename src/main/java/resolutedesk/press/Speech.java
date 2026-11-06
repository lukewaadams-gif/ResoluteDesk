package resolutedesk.press;

import static resolutedesk.congress.CongressRules.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.model.Attributes.*;
import static resolutedesk.model.NewsKinds.*;
import static resolutedesk.opinion.Issues.*;
import static resolutedesk.press.SpeechData.*;
import static resolutedesk.sim.DecisionKinds.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import resolutedesk.congress.Bill;
import resolutedesk.congress.Bloc;
import resolutedesk.home.Home;
import resolutedesk.model.Cand;
import resolutedesk.model.Game;
import resolutedesk.sim.Decision;
import resolutedesk.sim.Decisions;

/** Speeches and addresses, built line by line. */
public final class Speech {
    /** type 0: an Oval Office address on an issue; 1: the State of the Union. */
    public static void start(Game g, int type) {
        Home h = g.home;
        h.spType = type; h.spStep = 0; h.spScore = 0; h.spAttack = 0; h.spIssue = -1;
        String t = type == 1 ? (g.week < 52 && g.term == 1 ? "Your address to a joint session of Congress" : "The State of the Union") : "An address from the Oval Office";
        Decision d = Decisions.add(g, D_SPEECH, t + ": the opening", (type == 1 ? "Tonight you speak to Congress and about 30 million Americans. " : "The networks have given you fifteen minutes in prime time. ") + "Your speechwriters have drafts of every line. How do you open?",
                SPEECH_OPEN, new String[]{"Communications director: \"Unity plays well with independents.\"", "Political director: \"Give our people something to cheer.\"",
                        "Chief of staff: \"Honesty about the hard parts earns trust.\"", "Press secretary: \"A good laugh up top relaxes the room.\""});
        d.a = 0;
    }

    public static void step(Game g, int k) {
        Home h = g.home;
        Cand p = g.pres;
        double spe = p.a[A_SPE] / 70.0;
        Random r = g.rng;
        if (h.spStep == 0) {
            h.spOpen = k;
            h.spScore += k == 3 ? (p.a[A_CHA] - 55) / 20.0 + r.nextGaussian() * 0.6 : 0.4;
            h.spStep = 1;
            lines(g);
        } else if (h.spStep >= 1 && h.spStep <= 3) {
            int code = k;
            int issue = code / 4, kind = code % 4;     // kind 0 record, 1 plan, 2 attack, 3 push a bill
            if (kind == 3) {
                for (Bill b : g.cong.bills) if (b.ours && b.stage >= ST_COMMITTEE && b.stage <= ST_FLOOR2) { b.publicPush = true; g.issueSal[b.def().area] = Math.min(2, g.issueSal[b.def().area] + 0.1); h.spScore += 0.4; break; }
            } else {
                g.issueSal[issue] = Math.min(2, g.issueSal[issue] + 0.08 * (h.spType == 1 ? 1.5 : 1));
                double rec = (g.issueApp[issue] - 48) / 10;
                if (kind == 0) h.spScore += rec * 0.5;
                else if (kind == 1) { h.spScore += 0.3; g.issueAdj[issue] = clamp(g.issueAdj[issue] + 0.05 * spe, -1.5, 1.5); }
                else { h.spAttack++; h.spScore += h.spOpen == 1 ? 0.5 : h.spOpen == 0 ? -0.5 : 0; }
                if (h.spIssue < 0) h.spIssue = issue;
            }
            h.spStep++;
            if (h.spStep <= 3) lines(g);
            else {
                Decision d = Decisions.add(g, D_SPEECH, "The close", "Your speechwriters have three endings. Which one do you deliver?", SPEECH_CLOSE,
                        new String[]{"Communications director: \"Leave them hopeful.\"", "Legislative director: \"Tell them exactly what to ask Congress for.\"", "Political director: \"Make them feel what's at stake.\""});
                d.a = 1;
            }
        } else {
            h.spScore += k == 0 ? (h.spOpen == 1 ? -0.2 : 0.5) : k == 1 ? 0.4 : (h.spOpen == 1 ? 0.6 : 0.1);
            finish(g);
        }
    }

    static void lines(Game g) {
        Home h = g.home;
        Integer[] iss = new Integer[NPI];
        for (int i = 0; i < NPI; i++) iss[i] = i;
        final Game gg = g;
        Arrays.sort(iss, (a, b) -> Double.compare(gg.issueSal[b], gg.issueSal[a]));
        int a = iss[(h.spStep - 1) * 2 % NPI], b = iss[((h.spStep - 1) * 2 + 1) % NPI];
        List<String> lab = new ArrayList<String>();
        List<Integer> codes = new ArrayList<Integer>();
        lab.add("Your record on " + PISSUE[a].toLowerCase() + " (" + Math.round(g.issueApp[a]) + "% approve)"); codes.add(a * 4);
        lab.add("A new plan on " + PISSUE[b].toLowerCase()); codes.add(b * 4 + 1);
        lab.add("Take the fight to the other side on " + PISSUE[a].toLowerCase()); codes.add(a * 4 + 2);
        for (Bill bl : g.cong.bills) if (bl.ours && bl.stage >= ST_COMMITTEE && bl.stage <= ST_FLOOR2 && !bl.publicPush) { lab.add("Ask Congress to pass " + bl.name); codes.add(3); break; }
        Decision d = Decisions.add(g, D_SPEECH, "Line " + h.spStep + " of 3", "What comes next?", lab.toArray(new String[0]), null);
        d.codes = new int[codes.size()];
        for (int i = 0; i < d.codes.length; i++) d.codes[i] = codes.get(i);
    }

    static void finish(Game g) {
        Home h = g.home;
        Cand p = g.pres;
        double spe = p.a[A_SPE] / 70.0;
        boolean sotu = h.spType == 1;
        double score = h.spScore * spe + (p.a[A_SPE] - 60) / 25.0 + g.rng.nextGaussian() * 0.5;
        double k = sotu ? 1.4 : 1.0;
        g.perf += clamp(score, -2, 3) * 0.5 * k;
        if (h.spAttack > 0) { for (Bloc b : g.cong.blocs) b.rel = clamp(b.rel + (b.party == g.party ? 1.5 : -2.5) * h.spAttack, 0, 100); h.fac[0] = Math.min(100, h.fac[0] + 2 * h.spAttack); }
        else for (Bloc b : g.cong.blocs) if (b.party != g.party) b.rel = Math.min(100, b.rel + 1);
        h.speeches++;
        if (!sotu) h.lastAddress = g.week;
        g.stamina = Math.max(0, g.stamina - 3);
        String verdict = score > 1.5 ? "a triumph" : score > 0.5 ? "a solid success" : score > -0.5 ? "unremarkable" : "a flop";
        String head = (sotu ? "Critics call the State of the Union " : "Critics call the address ") + verdict;
        int ov = sotu ? (int) Math.round(clamp(30 + score * 12 + (h.spAttack > 0 ? 6 : 0), 5, 90)) : 0;
        g.log(score > 0.5 ? NK_GOOD : score < -0.5 ? NK_BAD : NK_INFO, head, sotu ? ov + " standing ovations; the other party sat on its hands for most of them." : null, sotu ? 3 : 1.8);
        Decisions.info(g, sotu ? "After the speech" : "After the address", head + "." + (sotu ? " You drew " + ov + " standing ovations." : "") + " Your speechwriters say your delivery " + (p.a[A_SPE] >= 70 ? "carried it." : p.a[A_SPE] >= 55 ? "was steady." : "let some lines fall flat."));
        h.spType = -1;
    }
}
