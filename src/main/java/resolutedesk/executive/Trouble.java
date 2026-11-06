package resolutedesk.executive;

import static resolutedesk.congress.CongressRules.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.executive.ActionData.*;
import static resolutedesk.executive.Delegation.*;
import static resolutedesk.executive.Posts.*;
import static resolutedesk.executive.ScandalData.*;
import static resolutedesk.executive.Traits.*;
import static resolutedesk.model.Attributes.*;
import static resolutedesk.model.NewsKinds.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.opinion.Issues.*;
import static resolutedesk.sim.DecisionKinds.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import resolutedesk.congress.Bloc;
import resolutedesk.congress.Hill;
import resolutedesk.congress.Member;
import resolutedesk.model.Cand;
import resolutedesk.model.Game;
import resolutedesk.sim.Decision;
import resolutedesk.sim.Decisions;
import resolutedesk.sim.Week;

/** Scandals, leaks, investigations, pardons and impeachment. */
public final class Trouble {
    public static Scandal add(Game g, int kind, String title, String body, double sev, Person who, boolean impeachable) {
        Scandal s = new Scandal();
        s.kind = kind; s.title = title; s.body = body; s.severity = sev; s.person = who; s.who = who != null ? who.name : null;
        s.started = g.week; s.impeachable = impeachable;
        g.ex.scandals.add(s);
        g.scandal += sev * 0.35;
        if (sev >= 3) {
            g.log(NK_BIG, title.toUpperCase(), body, 1.5 + sev * 0.4);
            respond(g, s);
        } else g.log(NK_BAD, title, body, 0.8 + sev * 0.3);
        return s;
    }

    /** Asks how to handle a serious story. */
    static void respond(Game g, Scandal s) {
        List<String> o = new ArrayList<String>(), adv = new ArrayList<String>();
        List<Integer> codes = new ArrayList<Integer>();
        o.add("Release everything and get ahead of it"); adv.add("Communications director: \"Take the hit now and it's a one-week story.\""); codes.add(0);
        o.add("Deny it and attack the coverage"); adv.add("Political director: \"Our voters don't trust these outlets anyway.\""); codes.add(1);
        if (s.person != null && (Cabinet.everyone(g).contains(s.person))) { o.add("Ask " + s.person.name + " to resign"); adv.add("Chief of staff: \"Cut it off before it reaches you.\""); codes.add(2); }
        o.add("Order an independent review"); adv.add("Counsel: \"Let someone credible look at it. It buys time and it looks serious.\""); codes.add(3);
        String[] oa = o.toArray(new String[0]), aa = adv.toArray(new String[0]);
        Decision d = Decisions.add(g, D_SCANDAL, s.title, s.body + " About " + Math.round(20 + s.severity * 6) + "% of Americans say they've heard a lot about it.", oa, aa);
        d.scandalRef = s;
        d.codes = new int[codes.size()];
        for (int i = 0; i < d.codes.length; i++) d.codes[i] = codes.get(i);
    }

    public static void scandalDecision(Game g, Scandal s, int code) {
        Random r = g.rng;
        s.answered = true;
        if (code == 0) { s.severity = Math.max(0.5, s.severity - 1.2); g.perf -= 0.3; g.log(NK_INFO, "The White House releases documents on " + s.title.toLowerCase(), null, 0.9); }
        else if (code == 1) {
            for (Bloc b : g.cong.blocs) if (b.party == g.party) b.rel = Math.min(100, b.rel + 1);
            if (r.nextDouble() < 0.35 + s.severity * 0.04) { s.severity += 2; g.log(NK_BAD, "New reporting contradicts the White House on " + s.title.toLowerCase(), null, 1.8); }
        } else if (code == 2 && s.person != null) {
            Cabinet.remove(g, s.person);
            g.ex.departures++;
            s.severity = Math.max(0.5, s.severity - 2.2);
            for (Person p : Cabinet.everyone(g)) p.morale = Math.max(0, p.morale - 2);
            g.log(NK_BAD, s.person.name + " resigns as " + lcTitle(s.person.title()), null, 1.6);
        } else if (code == 3) { s.stage = Math.max(s.stage, 1); s.severity *= 0.9; g.log(NK_INFO, "An independent review of " + s.title.toLowerCase() + " begins", null, 0.9); }
    }

    static void leak(Game g, Person p) {
        Exec x = g.ex;
        x.leaks++;
        String[] about = {"shouting matches over " + PISSUE[Week.topIssue(g)].toLowerCase(), "the president's temper and long days", "infighting between the chief of staff and the cabinet",
                "second thoughts about the administration's agenda", "who's up and who's down in the West Wing"};
        String what = about[g.rng.nextInt(about.length)];
        if (g.stamina < 30) what = "how tired the president looks after long days";
        String src = p.real ? "unnamed aides" : p.trait == T_LEAKER || p.morale < 35 ? "aides close to " + p.name : "unnamed aides";
        Scandal s = add(g, SK_LEAK, "Leak: West Wing aides describe " + what, "The story quotes " + src + ".", 1 + g.rng.nextDouble() * 1.2, p.real ? null : p, false);
        if (s.severity < 3 && g.rng.nextDouble() < 0.5) {
            Decision d = Decisions.add(g, D_LEAK, "Another leak", "The Capital Ledger is running a story built on " + src + ". It describes " + what + ".",
                    new String[]{"Hunt for the leaker", "Ignore it", "Tell the staff: one more leak and people go"},
                    new String[]{"Counsel: \"A leak investigation sends a message.\"", "Chief of staff: \"Every White House leaks. Don't feed it.\"", "Press secretary: \"Say it once, in the staff meeting, and mean it.\""});
            d.person = p.real ? null : p;
        }
    }

    public static void leakDecision(Game g, Person p, int k) {
        Exec x = g.ex;
        if (k == 0) {
            for (Person o : Cabinet.everyone(g)) o.morale = Math.max(0, o.morale - 4);
            if (p != null && g.rng.nextDouble() < 0.45) { Cabinet.remove(g, p); x.departures++; g.log(NK_BAD, p.name + " is pushed out after a leak investigation", null, 1.4); }
            else g.log(NK_INFO, "A White House leak investigation turns up nothing", null, 0.7);
        } else if (k == 2) {
            for (Person o : Cabinet.everyone(g)) { o.morale = Math.max(0, o.morale - 2); o.loyalty = Math.min(100, o.loyalty + 2); }
        }
    }

    static void ethics(Game g, Person p) {
        String[] what = {"billed taxpayers for first-class travel and a $40,000 office renovation", "met with lobbyists for a former client while writing rules that affect it",
                "used government staff to run personal errands", "kept trading stocks in industries the department regulates", "steered a contract to a friend's company"};
        add(g, SK_ETHICS, p.title() + " " + p.name + " under fire", "Reporters say " + p.name + " " + what[g.rng.nextInt(what.length)] + ".", 2.5 + g.rng.nextDouble() * 2.5, p, false);
    }

    public static void week(Game g) {
        Exec x = g.ex;
        Random r = g.rng;
        double press = Cabinet.sq(g, S_PRESS);
        // scandals fade, or grow
        for (Scandal s : x.scandals) {
            if (s.over) continue;
            boolean probed = false;
            for (Probe p : x.probes) if (!p.over && p.scandal == s) probed = true;
            double decay = probed ? 0.995 : s.stage == 1 ? 0.965 : 0.975 - 0.02 * press;
            s.severity *= decay;
            if (r.nextDouble() < 0.012 * s.severity * (1.2 - Cabinet.sq(g, S_COUNSEL) * 0.5) && s.kind != SK_LEAK) {
                s.severity += 1 + r.nextDouble();
                g.log(NK_BAD, "New revelations in " + s.title.toLowerCase(), null, 1.2 + s.severity * 0.2);
            }
            g.scandal += 0.009 * s.severity;
            if (s.severity >= 7 && s.stage < 2 && s.impeachable && x.post[P_DOJ] != null && x.post[P_DOJ].loyalty < 85 && r.nextDouble() < 0.06) {
                s.stage = 2;
                g.log(NK_BIG, "ATTORNEY GENERAL APPOINTS A SPECIAL COUNSEL TO INVESTIGATE " + s.title.toUpperCase(), null, 4);
            }
            if (s.severity < 0.6) s.over = true;
        }
        // something from the president's own past
        Cand p = g.pres;
        if (p != null && r.nextDouble() < 0.0006 + p.a[A_SCA] / 60000.0) {
            String[] past = {"Old business deals with a foreign investor resurface", "A former employee sues over the president's past business", "Questions about the president's taxes return", "A book by a former aide paints an unflattering picture"};
            add(g, SK_PAST, past[r.nextInt(past.length)], "The White House calls it old news.", 2 + r.nextDouble() * 2.5, null, false);
        }
        // pardon requests
        if (g.week >= x.nextPardon) { pardon(g); x.nextPardon = g.week + 8 + r.nextInt(8); }
        // oversight
        boolean oppH = !Hill.majority(g, HOUSE, g.party), oppS = !Hill.majority(g, SENATE, g.party);
        if ((oppH || oppS) && g.week >= x.nextProbe) { probe(g, oppH ? HOUSE : SENATE); x.nextProbe = g.week + 10 + r.nextInt(9); }
        for (Probe pr : x.probes) if (!pr.over && g.week >= pr.until) endProbe(g, pr);
        impeachment(g);
        // the vice president and the delegates
        VicePres.week(g);
        if (x.deleg[DG_WHIP]) Decisions.autoWhip(g);
    }

    // ---- pardons ----
    static void pardon(Game g) {
        Random r = g.rng;
        int i = r.nextInt(PARDONS.length);
        if (g.tw() < 30 && i == 1) i = 0;
        String[] t = PARDONS[i].split("\\|");
        int kind = Integer.parseInt(t[2]);
        Decision d = Decisions.add(g, D_PARDON, "A request for clemency: " + t[0].toLowerCase(), t[1] + " The pardon attorney's file is on your desk.",
                new String[]{"Grant a full pardon", "Deny it", "Commute the sentence instead"},
                new String[]{kind == 1 || kind == 5 ? "Political director: \"Our people have been asking for this.\"" : "Counsel: \"The case for mercy is strong.\"",
                        "Counsel: \"There's no reason to take the risk.\"", "Chief of staff: \"Split the difference: let them out, don't wipe the slate.\""});
        d.a = kind; d.b = i;
        if (g.ex.deleg[DG_PARDONS]) { g.inbox.remove(d); pardonDecision(g, d, kind == 0 || kind == 6 ? 0 : kind == 3 ? 2 : 1); g.log(NK_INFO, "Your counsel handles a clemency request", null, 0.3); }
    }

    public static void pardonDecision(Game g, Decision d, int k) {
        if (k == 1) return;
        int kind = d.a;
        double m = k == 2 ? 0.5 : 1;
        Exec x = g.ex;
        x.pardons++;
        String who = PARDONS[d.b].split("\\|")[0].toLowerCase();
        g.log(kind == 1 || kind == 2 ? NK_BIG : NK_INFO, (k == 2 ? "President " + g.pres.last + " commutes the sentence of " : "President " + g.pres.last + " pardons ") + who, null, kind == 1 || kind == 2 ? 2.4 : 1.0);
        switch (kind) {
            case 0: groupBump(g, 10, 1.2 * m); g.perf += 0.1; break;
            case 1: for (Bloc b : g.cong.blocs) if (b.party == g.party) b.rel = Math.min(100, b.rel + 2); add(g, SK_PARDON, "The pardon of " + who, "Critics call it a reward for loyalty and a warning to investigators.", 4.5 * m, null, true); break;
            case 2: add(g, SK_PARDON, "The pardon of " + who, "Records show the donor's family gave to the president's allies after the request was filed.", 5.5 * m, null, true); g.issueAdj[PI_ETHICS] -= 0.3 * m; break;
            case 3: groupBump(g, 25, 1 * m); g.perf -= 0.2 * m; g.issueAdj[PI_SECURITY] += g.party == REP ? 0.05 : -0.1; break;
            case 4: groupBump(g, 0, 1 * m); g.issueAdj[PI_SECURITY] -= 0.2 * m; if (x.post[P_CIA] != null) x.post[P_CIA].morale -= 10; if (x.post[P_DNI] != null) x.post[P_DNI].morale -= 8; break;
            case 5: for (Bloc b : g.cong.blocs) if (b.party == g.party) b.rel = Math.min(100, b.rel + 1.5); add(g, SK_PARDON, "Pardons for the rioters", "Police unions and the other party condemn them.", 3 * m, null, false); g.issueAdj[PI_CRIME] -= 0.3 * m; break;
            default: groupBump(g, 10, 0.4 * m); g.perf += 0.05; break;
        }
    }

    // ---- oversight ----
    static void probe(Game g, int chamber) {
        Exec x = g.ex;
        Probe p = new Probe();
        p.chamber = chamber; p.started = g.week; p.until = g.week + 10;
        Scandal target = null;
        for (Scandal s : x.scandals) if (!s.over && s.kind != SK_LEAK && (target == null || s.severity > target.severity)) target = s;
        boolean taken = false;
        for (Probe o : x.probes) if (!o.over && o.scandal == target) taken = true;
        if (target != null && !taken) { p.scandal = target; p.topic = target.title.toLowerCase(); target.stage = Math.max(target.stage, 1); }
        else {
            Action hot = null;
            for (Action a : x.actions) if (a.status == A_LIVE && a.lean == g.party && (hot == null || a.support < hot.support)) hot = a;
            p.topic = hot != null ? hot.name.toLowerCase() : x.failures > 0 ? "problems at " + POST_NAME[worstPost(g)] : "the administration's spending";
            p.scandal = add(g, SK_POWER, "Congress investigates " + p.topic, "The committee wants documents and testimony from senior officials.", 1.6, null, false);
        }
        Member chair = null;
        for (Member m : g.cong.members) if (m.role == RO_CHAIR && m.chamber == chamber && m.party != g.party && (m.committee.contains("Oversight") || m.committee.contains("Judiciary") || m.committee.contains("Homeland") || m.committee.contains("Govern") || m.committee.contains("Expenditures"))) { chair = m; break; }
        p.chair = chair != null ? chair.name : "The committee chair";
        x.probes.add(p);
        g.log(NK_BAD, p.chair + " opens an investigation into " + p.topic, null, 1.6);
        Decision d = Decisions.add(g, D_SUBPOENA, "A subpoena from Congress", p.chair + " has subpoenaed White House documents and testimony from senior aides about " + p.topic + ".",
                new String[]{"Comply and hand them over", "Negotiate a narrower request", "Claim executive privilege", "Refuse to cooperate"},
                new String[]{"Counsel: \"There's nothing in there we can't explain.\"", "Legislative director: \"Give them enough to say they won.\"", "Counsel: \"Privilege protects the office, not just you. The courts will take months.\"",
                        "Political director: \"It's a fishing expedition. Our voters will back us.\""});
        d.probeRef = p;
    }

    static int worstPost(Game g) { int w = 0; for (int k = 1; k < NPOST; k++) if (postOn(k) && g.ex.ready[k] < g.ex.ready[w]) w = k; return w; }

    public static void subpoenaDecision(Game g, Probe p, int k) {
        Exec x = g.ex;
        Random r = g.rng;
        p.response = k;
        Scandal s = p.scandal;
        double cq = Cabinet.sq(g, S_COUNSEL);
        if (k == 0) { if (s != null) s.severity += r.nextDouble() * 2 - 0.6; p.until = g.week + 8; }
        else if (k == 1) {
            if (r.nextDouble() < 0.35 + 0.5 * cq) { if (s != null) s.severity += r.nextDouble() - 0.6; p.until = g.week + 7; g.log(NK_INFO, "The White House and " + p.chair + " reach a deal on documents", null, 0.8); }
            else { if (s != null) s.severity += 1; p.until = g.week + 10; g.log(NK_BAD, p.chair + " rejects the White House's offer and threatens contempt", null, 1.2); }
        } else if (k == 2) { p.privilegeCase = true; p.until = g.week + 14 + r.nextInt(10); g.log(NK_INFO, "The White House claims executive privilege; the fight goes to court", null, 1.4); }
        else {
            x.contempt++;
            if (s != null) { s.severity += 2; s.impeachable = true; }
            g.perf -= 0.5;
            for (Bloc b : g.cong.blocs) if (b.party == g.party) b.rel = Math.min(100, b.rel + 1);
            g.log(NK_BIG, (CHAMBER[p.chamber] + " votes to hold White House officials in contempt of Congress").toUpperCase(), null, 2.8);
            p.until = g.week + 12;
        }
    }

    static void endProbe(Game g, Probe p) {
        p.over = true;
        Scandal s = p.scandal;
        if (p.privilegeCase) {
            boolean win = g.rng.nextDouble() < 0.35 + 0.3 * Cabinet.sq(g, S_COUNSEL);
            if (win) { if (s != null) s.severity *= 0.6; g.log(NK_GOOD, "A judge upholds the White House's claim of executive privilege", null, 1.6); }
            else { if (s != null) s.severity += 1.5; g.log(NK_BAD, "Courts reject the White House's privilege claim; the documents go to Congress", null, 2.0); }
            return;
        }
        String verdict = s == null || s.severity < 2.5 ? "finds poor judgment but no wrongdoing" : s.severity < 5 ? "accuses the administration of misleading Congress" : "says senior officials may have broken the law";
        g.log(s != null && s.severity >= 5 ? NK_BIG : NK_BAD, "The " + CHAMBER[p.chamber] + " report on " + p.topic + " " + verdict, null, s != null && s.severity >= 5 ? 3 : 1.4);
        if (s != null && s.severity >= 5) s.impeachable = true;
    }

    // ---- impeachment ----
    static void impeachment(Game g) {
        Exec x = g.ex;
        Random r = g.rng;
        double pr = 2 * x.contempt + 3 * x.defiance;
        for (Scandal s : x.scandals) if (!s.over && s.impeachable) pr += s.severity;
        x.impPressure = pr;
        boolean oppH = !Hill.majority(g, HOUSE, g.party);
        if (x.impStage == 0 && oppH && pr >= 9 && r.nextDouble() < 0.06 + (pr - 9) * 0.01) {
            x.impStage = 1; x.impWeek = g.week + 8 + r.nextInt(6);
            g.log(NK_BIG, "THE HOUSE OPENS AN IMPEACHMENT INQUIRY", "The Judiciary Committee will decide whether to draft articles of impeachment.", 4.5);
            Decisions.add(g, D_IMPEACH, "An impeachment inquiry", "The House has opened an impeachment inquiry. Hearings start within weeks. How do you respond?",
                    new String[]{"Cooperate and make your case", "Stonewall: no witnesses, no documents", "Take it to the country: rallies and prime-time speeches"},
                    new String[]{"Counsel: \"If we have nothing to hide, show it.\"", "Political director: \"It's a partisan show. Don't dignify it.\"", "Communications director: \"Make it a referendum on them.\""});
            return;
        }
        if (x.impStage == 1 && g.week >= x.impWeek) {
            int yes = 0;
            for (Bloc b : g.cong.blocs) {
                if (b.chamber != HOUSE) continue;
                double p = b.party == g.party ? logistic(-4.5 + 0.18 * pr + (40 - g.approval) * 0.05) : logistic(-1.2 + 0.28 * pr - (b.name.contains("Blue Dogs") || b.name.contains("Problem") ? 0.8 : 0));
                for (int i = 0; i < b.seats; i++) if (r.nextDouble() < p) yes++;
            }
            if (yes >= 218) {
                x.impStage = 2; x.impeachments++; x.impWeek = g.week + 3 + r.nextInt(3);
                g.log(NK_BIG, "THE HOUSE IMPEACHES PRESIDENT " + g.pres.last.toUpperCase() + ", " + yes + "-" + (435 - yes), "The articles go to the Senate for trial.", 5);
                if (pr < 14) { g.perf += 0.8; for (Bloc b : g.cong.blocs) if (b.party == g.party) b.rel = Math.min(100, b.rel + 6); } else g.perf -= 2;
            } else { x.impStage = 0; x.impResult = "The House voted down impeachment, " + yes + "-" + (435 - yes) + "."; g.log(NK_BIG, "THE HOUSE REJECTS IMPEACHMENT, " + yes + "-" + (435 - yes), null, 4); g.perf += 1; clearImpeachable(g, 0.6); }
            return;
        }
        if (x.impStage == 2 && g.week >= x.impWeek) {
            int yes = 0;
            for (Bloc b : g.cong.blocs) {
                if (b.chamber != SENATE) continue;
                double p = b.party == g.party ? logistic(-4.2 + 0.16 * pr + (40 - g.approval) * 0.06) : logistic(0.2 + 0.2 * pr - (b.name.contains("oderate") ? 0.4 : 0));
                for (int i = 0; i < b.seats; i++) if (r.nextDouble() < p) yes++;
            }
            if (yes >= 67) {
                x.impStage = 4;
                x.impResult = "The Senate voted " + yes + "-" + (100 - yes) + " to convict. " + (g.vp != null ? g.vp.name() + " is sworn in as president." : "");
                g.log(NK_BIG, "THE SENATE CONVICTS PRESIDENT " + g.pres.last.toUpperCase() + ", " + yes + "-" + (100 - yes), x.impResult, 6);
                g.over = true; g.removed = true;
            } else {
                x.impStage = 3;
                x.impResult = "The Senate acquitted you, " + yes + " guilty to " + (100 - yes) + " not guilty; 67 votes were needed to convict.";
                g.log(NK_BIG, "SENATE ACQUITS PRESIDENT " + g.pres.last.toUpperCase(), x.impResult, 5);
                g.perf += pr < 14 ? 1.2 : 0;
                clearImpeachable(g, 0.5);
                Decisions.info(g, "Acquitted", x.impResult + " The trial is over, but the votes will be remembered.");
            }
        }
    }

    static void clearImpeachable(Game g, double k) { for (Scandal s : g.ex.scandals) if (!s.over && s.impeachable) s.severity *= k; g.ex.contempt = 0; g.ex.defiance = 0; }

    public static void impeachDecision(Game g, int k) {
        Exec x = g.ex;
        if (k == 0) { for (Scandal s : x.scandals) if (!s.over && s.impeachable) s.severity *= g.rng.nextDouble() < 0.5 ? 0.85 : 1.1; }
        else if (k == 1) { x.contempt++; for (Bloc b : g.cong.blocs) if (b.party == g.party) b.rel = Math.min(100, b.rel + 3); }
        else { g.stamina = Math.max(0, g.stamina - 10); g.perf += g.approval >= 44 ? 0.8 : -0.4; }
    }
}
