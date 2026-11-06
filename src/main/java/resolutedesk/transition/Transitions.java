package resolutedesk.transition;

import static resolutedesk.congress.CongressRules.*;
import static resolutedesk.core.Dates.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.data.StateData.*;
import static resolutedesk.executive.Posts.*;
import static resolutedesk.executive.Traits.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.transition.TransitionData.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import resolutedesk.congress.Bloc;
import resolutedesk.congress.Hill;
import resolutedesk.core.Seeds;
import resolutedesk.executive.Cabinet;
import resolutedesk.executive.Exec;
import resolutedesk.executive.Person;
import resolutedesk.model.Game;

/**
 * The transition: from the day after the election to the inauguration, the president-elect chooses a government.
 *
 * <p>Each job has a shortlist of three to five prospects. The team has eight hours a week to interview them, order
 * FBI background checks, and meet senators about the nominees; floating a name tests the Senate's mood and
 * announcing it locks the choice in. Unannounced front-runners leak, and an unchecked problem can surface at the
 * confirmation hearing. Whatever is still open on Inauguration Day the transition team fills on its own.
 */
public final class Transitions {
    private Transitions() {
    }

    // ---- starting ----

    /** Election Day of the election that came before this inauguration. */
    public static int electionDay() {
        int y = CAL_Y - 1;
        for (int d = 2; d <= 8; d++) { int day = dayOfDate(y, 10, d); if (dowOf(day) == 1) return day; }
        return dayOfDate(y, 10, 3);
    }

    /** Starts the transition in a game already set up for its inauguration: the jobs are emptied and shortlists drawn. */
    public static void begin(Game g) {
        Transition t = new Transition();
        g.trans = t;
        t.rng = new Random(Seeds.mix(g.set.seed, 0x7A45L));
        t.startDay = electionDay() + 1;
        t.weeks = Math.max(1, (int) Math.ceil(-t.startDay / 7.0));
        Exec x = g.ex;
        for (int s = 0; s < NSTAFF; s++) x.staff[s] = null;
        for (int k = 0; k < NPOST; k++) { x.nominee[k] = null; x.voteAt[k] = -1; }
        Set<String> taken = Cabinet.takenSurnames(g);
        List<Person> real = Cabinet.realSlate(g, new Random(Seeds.mix(g.set.seed, 0x7A46L)));
        for (int job = 0; job < NJOB; job++) {
            if (job < JOB_STAFF && !postOn(job)) continue;
            Person r = null;
            for (Person p : real) if (job < JOB_STAFF ? p.post == job : p.role == job - JOB_STAFF) r = p;
            shortlist(g, t, job, r, taken);
        }
        t.news.add(dateStr(t.startDay) + ": " + g.pres.name() + " wins the presidency. The transition begins.");
    }

    /** Three to five prospects for one job: always an expert and a loyalist, then a mix of the others. */
    static void shortlist(Game g, Transition t, int job, Person real, Set<String> taken) {
        Random r = t.rng;
        int[] more = {PK_POL, PK_OUTSIDER, PK_CROSS};
        for (int i = more.length - 1; i > 0; i--) { int j = r.nextInt(i + 1); int tmp = more[i]; more[i] = more[j]; more[j] = tmp; }
        int extra = 1 + r.nextInt(3);
        if (real != null) { extra = Math.max(0, extra - 1); t.prospects.add(realProspect(g, job, real)); }
        t.prospects.add(prospect(g, t, job, PK_EXPERT, taken));
        t.prospects.add(prospect(g, t, job, PK_LOYAL, taken));
        for (int i = 0; i < extra; i++) t.prospects.add(prospect(g, t, job, more[i], taken));
    }

    static Prospect realProspect(Game g, int job, Person p) {
        Prospect q = new Prospect();
        q.person = p; q.job = job; q.kind = PK_REAL;
        q.profile = p.background;
        q.skillSeen = p.skill; q.loyaltySeen = p.loyalty;
        q.interviewed = true;
        if (p.loyalty >= 80) q.strengths.add(S_LOYAL);
        if (p.trait == T_EXPERT) q.strengths.add(S_EXPERT);
        if (p.trait == T_STEADY) q.strengths.add(S_STEADY);
        if (p.trait == T_WORKHORSE) q.strengths.add(S_WORK);
        if (p.trait == T_DEALMAKER) q.strengths.add(S_DEAL);
        if (p.trait == T_MEDIA) q.strengths.add(S_MEDIA);
        if (p.background.contains("Senator")) q.strengths.add(S_SENATE);
        return q;
    }

    static Prospect prospect(Game g, Transition t, int job, int kind, Set<String> taken) {
        Random r = t.rng;
        boolean staff = job >= JOB_STAFF;
        int party = kind == PK_CROSS && !staff ? Hill.other(g.party) : g.party;
        Person p = Cabinet.make(g, r, party, kind == PK_LOYAL ? 0 : kind == PK_EXPERT ? 1 : 2, taken);
        if (staff) { p.role = job - JOB_STAFF; p.party = g.party; } else p.post = job;
        Prospect q = new Prospect();
        q.person = p; q.job = job; q.kind = kind;
        // what each kind brings
        if (kind == PK_POL) { p.skill = clamp(p.skill + 2, 0, 100); p.loyalty = clamp(p.loyalty + 10, 0, 100); p.ideo = (g.party == REP ? 1 : -1) * (0.3 + r.nextDouble() * 0.35); p.senate = 0.35; }
        if (kind == PK_OUTSIDER) { p.skill = 55 + r.nextInt(35); p.loyalty = 55 + r.nextInt(25); }
        if (kind == PK_CROSS && staff) { p.loyalty = 45 + r.nextInt(20); p.ideo *= 0.3; }
        if (kind == PK_LOYAL && p.trait != T_LOYAL && r.nextDouble() < 0.5) p.trait = T_LOYAL;
        int dom = domain(job);
        String[] opts = PROFILE[kind][dom];
        int st = r.nextInt(NS);
        while (STATES[st].ab.equals("DC")) st = r.nextInt(NS);
        q.profile = opts[r.nextInt(opts.length)].replace("{S}", STATES[st].name).replace("{P}", PARTY[g.party]).replace("{O}", PARTY[Hill.other(g.party)]);
        p.background = q.profile;
        // the team's first read is rough: within about ten points
        q.skillSeen = clamp(p.skill + r.nextGaussian() * 6, 30, 99);
        q.loyaltySeen = clamp(p.loyalty + r.nextGaussian() * 6, 20, 99);
        q.guessOff = r.nextGaussian() * 3;
        describe(g, q);
        // what a background check would find
        double risk = (p.trait == T_RISK ? 0.6 : 0.18) + (kind == PK_OUTSIDER ? 0.1 : 0) + (kind == PK_LOYAL ? 0.06 : 0);
        if (r.nextDouble() < risk) {
            int b = r.nextInt(BAG_WHAT.length);
            if (yearNow(g) < BAG_FROM[b]) b = 0;
            p.baggage = BAG_WHAT[b];
            p.baggageWeight = BAG_WEIGHT[b];
        }
        return q;
    }

    /** Strengths and weaknesses that follow from who the prospect really is; some weaknesses show only in an interview. */
    static void describe(Game g, Prospect q) {
        Person p = q.person;
        List<String> s = q.strengths, w = q.weaknesses, h = q.hidden;
        if (p.skill >= 80) s.add(S_SKILL);
        if (p.trait == T_EXPERT) s.add(S_EXPERT);
        if (p.loyalty >= 85) s.add(S_LOYAL);
        if (p.trait == T_STEADY) s.add(S_STEADY);
        if (p.trait == T_WORKHORSE) s.add(S_WORK);
        if (p.trait == T_DEALMAKER) s.add(S_DEAL);
        if (p.trait == T_MEDIA) s.add(S_MEDIA);
        if (q.kind == PK_POL) { s.add(S_SENATE); w.add(W_VOTES); }
        if (q.kind == PK_OUTSIDER) { if (p.skill >= 70) s.add(S_ORG); else s.add(S_FRESH); w.add(W_NEVER); if (q.job >= JOB_STAFF && q.job - JOB_STAFF == S_LEG) w.add(W_CONGRESS); }
        if (q.kind == PK_CROSS) { s.add(q.job < JOB_STAFF ? S_EASY : S_UNITY); w.add(W_BASE); }
        if (p.skill < 62) w.add(W_THIN);
        if (Math.abs(p.ideo) > 0.7) w.add(W_PARTISAN);
        if (p.loyalty < 55) w.add(W_INDEP);
        if (p.trait == T_AMBITIOUS) w.add(W_AMBITION);
        if (p.trait == T_LEAKER) h.add(W_LEAKS);
        if (p.trait == T_ABRASIVE) h.add(W_ABRASIVE);
        if (p.trait == T_RISK) h.add(W_CORNERS);
        while (s.size() > 3) s.remove(s.size() - 1);
        while (w.size() > 3) w.remove(w.size() - 1);
    }

    // ---- the shortlist ----

    public static List<Prospect> prospects(Game g, int job) {
        List<Prospect> l = new ArrayList<Prospect>();
        for (Prospect q : g.trans.prospects) if (q.job == job) l.add(q);
        return l;
    }

    public static String jobTitle(int job) { return job < JOB_STAFF ? POST_TITLE[job] : STAFF_TITLE[job - JOB_STAFF]; }

    public static boolean confirmed(int job) { return job < JOB_STAFF; }

    /** The Senate's likely vote {yes, chance}: an exact count once the name has been floated, the team's rough guess before. */
    public static double[] whip(Game g, Prospect q) {
        double[] c = Cabinet.count(g, q.person);
        double ch = Cabinet.chance(g, q.person);
        return new double[]{c[0], ch};
    }

    /** The team's guess of the Senate vote before a name is floated: a range about twelve votes wide. */
    public static int[] guess(Game g, Prospect q) {
        double y = Cabinet.count(g, q.person)[0];
        int mid = (int) Math.round(y + q.guessOff);
        return new int[]{Math.max(0, mid - 6), Math.min(100, mid + 6)};
    }

    public static boolean vetting(Game g, Prospect q) { return q.vetDue >= 0 && g.trans.week < q.vetDue; }

    public static boolean vetted(Game g, Prospect q) { return q.kind == PK_REAL || q.vetDue >= 0 && g.trans.week >= q.vetDue; }

    public static int vetsRunning(Game g) {
        int n = 0;
        for (Prospect q : g.trans.prospects) if (vetting(g, q)) n++;
        return n;
    }

    public static double hoursLeft(Game g) { return Math.max(0, TR_HOURS - g.trans.hoursUsed); }

    static boolean spend(Game g, double h) {
        if (hoursLeft(g) + 1e-9 < h) return false;
        g.trans.hoursUsed += h;
        return true;
    }

    public static boolean open(Game g, int job) { return g.trans.pick[job] == null; }

    // ---- what the team can do ----

    /** Why an action can't be taken now, or null. */
    public static String cannot(Game g, Prospect q, int action) {
        Transition t = g.trans;
        if (t.done) return "The transition is over.";
        boolean openJob = open(g, q.job);
        switch (action) {
            case TA_INTERVIEW:
                if (q.interviewed) return "Already interviewed.";
                return hoursLeft(g) < 1 ? "No hours left this week." : null;
            case TA_VET:
                if (q.kind == PK_REAL) return "The FBI has already cleared this nominee.";
                if (q.vetDue >= 0) return vetting(g, q) ? "The FBI is still checking." : "Already checked.";
                if (vetsRunning(g) >= TR_VET_SLOTS) return "The FBI is running " + TR_VET_SLOTS + " checks already.";
                return hoursLeft(g) < 1 ? "No hours left this week." : null;
            case TA_FLOAT:
                if (!confirmed(q.job)) return "Staff jobs don't need the Senate.";
                if (q.floated) return "Already floated.";
                return openJob ? null : "This job is filled.";
            case TA_SENATE:
                if (!confirmed(q.job)) return "Staff jobs don't need the Senate.";
                if (q.senate >= 0.85) return "Senators have heard all they need.";
                return hoursLeft(g) < 1 ? "No hours left this week." : null;
            default:
                return openJob ? null : "This job is filled.";
        }
    }

    public static final int TA_INTERVIEW = 0, TA_VET = 1, TA_FLOAT = 2, TA_SENATE = 3, TA_ANNOUNCE = 4;
    public static final String[] TA_NAME = {"Interview", "Background check", "Float the name", "Meet senators", "Announce"};
    public static final String[] TA_DESC = {"One hour. You learn how good and how loyal they really are, and the weaknesses a resume hides.",
            "One hour to order; the FBI takes two weeks and runs four checks at a time. Anything it finds stays private, and you can still choose them.",
            "Let reporters know you're considering them. You learn exactly where the Senate stands, but the press will dig too.",
            "One hour. Calls and coffees with senators about this nominee make the confirmation vote easier, and warm up the Senate.",
            "Name them. The choice is made; nominees go to the Senate after the inauguration."};

    /** Takes one of the transition team's actions on a prospect, and says what happened. */
    public static String act(Game g, Prospect q, int action) {
        String no = cannot(g, q, action);
        if (no != null) return no;
        Transition t = g.trans;
        Person p = q.person;
        Random r = t.rng;
        String day = dateStr(dayNow(g));
        switch (action) {
            case TA_INTERVIEW:
                spend(g, 1);
                q.interviewed = true;
                q.skillSeen = p.skill; q.loyaltySeen = p.loyalty;
                for (String h : q.hidden) if (!q.weaknesses.contains(h)) q.weaknesses.add(h);
                return "You sit down with " + p.name + "." + (q.hidden.isEmpty() ? " No surprises." : " You come away with doubts: " + q.hidden.get(0).toLowerCase() + ".");
            case TA_VET:
                spend(g, 1);
                q.vetDue = t.week + TR_VET_WEEKS;
                return "The FBI opens a background check on " + p.name + ". Results in " + TR_VET_WEEKS + " weeks.";
            case TA_FLOAT: {
                q.floated = true;
                double[] w = whip(g, q);
                String head = p.name + " under consideration for " + lcTitle(jobTitle(q.job));
                t.news.add(day + ": " + head);
                if (p.baggageWeight > 0 && !p.baggageKnown && r.nextDouble() < 0.35) {
                    p.baggageKnown = true;
                    t.surprises++;
                    t.news.add(day + ": Reporters dig up " + p.baggage + " in " + p.name + "'s past");
                    return "The trial balloon backfires: reporters find " + p.baggage + ". The Senate count is now about " + Math.round(Cabinet.count(g, p)[0]) + " votes.";
                }
                return "Word gets out. Senators' reactions put " + p.name + " at about " + Math.round(w[0]) + " votes, a " + Math.round(w[1] * 100) + "% chance of confirmation.";
            }
            case TA_SENATE:
                spend(g, 1);
                q.senate = Math.min(0.9, q.senate + 0.3);
                p.senate = Math.max(p.senate, q.senate + (q.kind == PK_POL ? 0.35 : 0));
                for (Bloc b : g.cong.blocs) if (b.chamber == SENATE) b.rel = Math.min(100, b.rel + 0.5);
                return "You spend an hour with senators on behalf of " + p.name + ". The count is now about " + Math.round(Cabinet.count(g, p)[0]) + " votes.";
            default:
                announce(g, q);
                return "You name " + p.name + " " + lcTitle(jobTitle(q.job)) + ".";
        }
    }

    /** Names a prospect to their job. Passing over a front-runner whose name leaked makes a story of its own. */
    static void announce(Game g, Prospect q) {
        Transition t = g.trans;
        t.pick[q.job] = q;
        t.namedWeek[q.job] = t.week;
        String day = dateStr(dayNow(g));
        t.news.add(day + ": " + g.pres.last + " names " + q.person.name + " " + lcTitle(jobTitle(q.job)));
        if (vetted(g, q) && q.person.baggageWeight > 0) q.person.baggageKnown = true;      // nominating with eyes open: it will come out
        for (Prospect o : prospects(g, q.job)) if (o != q && o.leaked) {
            t.leaks++;
            t.news.add(day + ": Passed over after weeks as the front-runner, " + o.person.name + " lets friends know how they feel");
        }
    }

    // ---- the week ----

    /** The day the transition week begins. */
    public static int dayNow(Game g) { return g.trans.startDay + 7 * g.trans.week; }

    public static boolean lastWeek(Game g) { return g.trans.week >= g.trans.weeks - 1; }

    /** Ends a transition week: checks finish, names leak, and the team's hours renew. */
    public static void week(Game g) {
        Transition t = g.trans;
        Random r = t.rng;
        t.week++;
        t.hoursUsed = 0;
        String day = dateStr(dayNow(g));
        for (Prospect q : t.prospects) {
            if (q.vetDue == t.week) {
                Person p = q.person;
                if (p.baggageWeight > 0) t.notes.add(day + ": The FBI's check on " + p.name + " finds " + p.baggage + ".");
                else t.notes.add(day + ": The FBI clears " + p.name + ".");
            }
        }
        // the press is chasing front-runners for every open job
        double discipline = t.pick[JOB_STAFF + S_COS] != null ? 1.3 - t.pick[JOB_STAFF + S_COS].person.skill / 100 : 1.0;
        for (int job = 0; job < NJOB; job++) {
            if (t.pick[job] != null) continue;
            Prospect lead = null;
            for (Prospect q : prospects(g, job)) if ((q.interviewed && q.kind != PK_REAL || q.floated) && !q.leaked && (lead == null || q.skillSeen > lead.skillSeen)) lead = q;
            if (lead == null || r.nextDouble() > 0.12 * discipline) continue;
            lead.leaked = true;
            t.news.add(day + ": Sources: " + lead.person.name + " is the front-runner for " + lcTitle(jobTitle(job)));
        }
        event(g);
    }

    /** Now and then something happens in the transition. */
    static void event(Game g) {
        Transition t = g.trans;
        Random r = t.rng;
        if (r.nextDouble() > 0.35) return;
        String day = dateStr(dayNow(g));
        int k = r.nextInt(5);
        Prospect treas = t.pick[P_TREAS];
        if (k == 0) { t.news.add(day + ": The outgoing president welcomes " + g.pres.last + " to the White House for a handover meeting"); g.honeymoon += 0.4; }
        else if (k == 1 && treas != null) {
            boolean good = treas.person.skill >= 72;
            t.news.add(day + ": " + (good ? "Markets rally on the choice of " + treas.person.name + " for the Treasury" : "Markets slip; investors question the pick for the Treasury"));
            g.econ.sentiment += good ? 1.5 : -1.5;
        } else if (k == 2) { t.news.add(day + ": A feud inside the transition spills into the papers"); t.leaks++; }
        else if (k == 3) t.news.add(day + ": The president-elect receives the first full intelligence briefing");
        else t.news.add(day + ": Agencies brief the transition's review teams");
    }

    // ---- inauguration ----

    /** The team's own choice for a job still open: the strongest prospect who can be confirmed. */
    public static Prospect teamChoice(Game g, int job) {
        Prospect best = null;
        double bv = -9;
        for (Prospect q : prospects(g, job)) {
            double v = q.skillSeen / 100 + (confirmed(job) ? 0.6 * Cabinet.chance(g, q.person) : 0) + q.loyaltySeen / 400 + (q.kind == PK_REAL ? 1 : 0);
            if (v > bv) { bv = v; best = q; }
        }
        return best;
    }

    /**
     * How well the transition went, 0 to 100: how many jobs you filled yourself, how strong and well checked the picks
     * are, and how few leaks and surprises. A team left to fill everything on its own earns about a D.
     */
    public static double score(Game g) {
        Transition t = g.trans;
        int jobs = 0, named = 0, checked = 0;
        double skill = 0;
        for (int job = 0; job < NJOB; job++) {
            if (prospects(g, job).isEmpty()) continue;
            jobs++;
            Prospect q = t.pick[job] != null ? t.pick[job] : teamChoice(g, job);
            if (t.pick[job] != null) named++;
            if (vetted(g, q)) checked++;
            skill += q.person.skill;
        }
        if (jobs == 0) return 60;
        double s = 40 + 25.0 * named / jobs + 15.0 * checked / jobs + (skill / jobs - 60) * 1.2 - 3 * t.leaks - 6 * t.surprises;
        if (t.pick[JOB_STAFF + S_COS] != null && t.namedWeek[JOB_STAFF + S_COS] <= 2) s += 5;
        return clamp(s, 0, 100);
    }

    public static String grade(double s) { return s >= 85 ? "A" : s >= 72 ? "B" : s >= 58 ? "C" : s >= 45 ? "D" : "F"; }

    /** Inauguration Day: every job gets its person, nominees go to the Senate, and the transition's grade colors the start. */
    public static void finish(Game g) {
        Transition t = g.trans;
        Exec x = g.ex;
        double s = score(g);
        for (int job = 0; job < NJOB; job++) {
            if (prospects(g, job).isEmpty()) continue;
            Prospect q = t.pick[job] != null ? t.pick[job] : teamChoice(g, job);
            Person p = q.person;
            if (vetted(g, q) && p.baggageWeight > 0) p.baggageKnown = true;
            if (job >= JOB_STAFF) { p.role = job - JOB_STAFF; p.since = 0; p.morale = clamp(70 + (s - 60) / 4, 40, 95); x.staff[p.role] = p; }
            else {
                x.nominee[job] = p;
                if (t.pick[job] != null && vetted(g, q)) x.ready[job] = Math.min(100, x.ready[job] + 4);
            }
        }
        Cabinet.schedule(g);
        g.honeymoon = Math.max(0, g.honeymoon + (s - 60) / 10);
        t.done = true;
    }

    /** Lets the transition team make every remaining choice. */
    public static void skip(Game g) {
        finish(g);
    }
}
