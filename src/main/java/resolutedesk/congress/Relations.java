package resolutedesk.congress;

import static resolutedesk.congress.CongressRules.*;
import static resolutedesk.core.Dates.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.data.StateData.*;
import static resolutedesk.executive.Posts.*;
import static resolutedesk.model.Attributes.*;
import static resolutedesk.model.NewsKinds.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.sim.DecisionKinds.*;
import static resolutedesk.sim.Schedule.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import resolutedesk.campaign.Reelection;
import resolutedesk.executive.Cabinet;
import resolutedesk.model.Game;
import resolutedesk.sim.Decision;
import resolutedesk.sim.Decisions;

/**
 * The president's relationships with Congress. There is no currency to spend: what moves votes is what you have built.
 *
 * <ul>
 *   <li>Goodwill with each bloc, leader and committee chair ({@link Bloc#rel}, {@link Member#rel}), raised by meetings,
 *       help back home, wins and keeping your word, and lowered by threats, broken promises and vetoes.</li>
 *   <li>Favors members owe you, earned by doing things for them and called in on a hard vote.</li>
 *   <li>The mandate of the last election: strongest on the first day of a term and mostly gone within a year. Congress
 *       also gives a new president a honeymoon of about a hundred days.</li>
 *   <li>Time. Every call and meeting takes an hour of the president's week, and the hours come from the Hill meetings
 *       on the calendar.</li>
 * </ul>
 */
public final class Relations {
    /** Hours a week for Congress with no Hill meetings scheduled, and the hours each Hill meeting slot adds. */
    public static final double HOURS_BASE = 2, HOURS_PER_MEETING = 2.5;
    /** The most appointments (ambassadorships, boards, commissions) you can have in hand; one opens up every four weeks. */
    public static final int MAX_APPOINTMENTS = 6;
    /** What members ask the president for. */
    public static final int ASK_VISIT = 0, ASK_PROJECT = 1, ASK_APPOINT = 2;

    private Relations() {
    }

    // ---- the mandate ----

    /** The mandate an election margin gives: about 2 for a squeaker, 8 for a solid win and 15 for a landslide. */
    public static double mandateFor(double margin) { return clamp(2 + 0.55 * margin, 0.5, 15); }

    /** Sets the mandate from the popular-vote margin of the election just won. */
    public static void elected(Game g, double margin) {
        Congress c = g.cong;
        c.margin = margin;
        c.mandate = c.mandate0 = mandateFor(margin);
    }

    public static String mandateWord(double m) {
        return m >= 10 ? "A sweeping mandate" : m >= 6 ? "A clear mandate" : m >= 3 ? "A modest mandate" : m >= 1.2 ? "A thin mandate" : "No mandate left";
    }

    /** Congress's honeymoon: 1 on the first day of a term, gone after 15 weeks. */
    public static double honeymoon(Game g) { return clamp(1 - g.tw() / 15.0, 0, 1); }

    /** Blocs in the middle, who weigh the national mood and the president's standing more than party. */
    public static boolean moderate(Bloc b) {
        String n = b.name;
        return n.contains("oderate") || n.contains("Blue Dogs") || n.contains("Problem Solvers") || n.contains("Gypsy Moths") || n.contains("Boll Weevils")
                || n.contains("Liberal Republicans") || n.contains("Southern Democrats");
    }

    /** The pull of the mandate and the honeymoon on a bloc's vote for one of your bills (logit points). */
    public static double pull(Game g, Bloc b) {
        double m = g.cong.mandate / 15, h = honeymoon(g);
        if (b.party == g.party) return 0.9 * m + 0.25 * h;
        return moderate(b) ? 0.7 * m + 0.35 * h : 0.2 * m + 0.1 * h;
    }

    // ---- time ----

    /** The president's hours for Congress this week: a little phone time, plus the Hill meetings on the calendar. */
    public static double hoursMax(Game g) {
        int slots = 0;
        for (int i = 0; i < NSLOT; i++) if (g.act[i] == AC_CONGRESS) slots++;
        return HOURS_BASE + HOURS_PER_MEETING * slots + (Cabinet.sq(g, S_LEG) >= 0.7 ? 1 : 0);
    }

    public static double hoursLeft(Game g) { return Math.max(0, hoursMax(g) - g.cong.hoursUsed); }

    /** Spends hours of the president's week on Congress, if there are enough left. */
    public static boolean spend(Game g, double hours) {
        if (hoursLeft(g) + 1e-9 < hours) return false;
        g.cong.hoursUsed += hours;
        return true;
    }

    /** A private meeting with a leader: two hours. */
    public static String meet(Game g, Member m) {
        if (!spend(g, 2)) return "You need two free hours for a meeting. Hill meetings on your calendar add time.";
        m.rel = Math.min(100, m.rel + 5 + (g.pres != null ? g.pres.a[A_EST] : 60) / 30.0);
        return "You meet privately with " + m.name + ".";
    }

    // ---- wins and setbacks ----

    /** A win makes your own side keener to stick with you. */
    public static void win(Game g, double points) {
        for (Bloc b : g.cong.blocs) if (b.party == g.party) b.rel = Math.min(100, b.rel + points);
    }

    /** A defeat, or a slight to your own side, costs their goodwill. */
    public static void setback(Game g, double points) {
        for (Bloc b : g.cong.blocs) if (b.party == g.party) b.rel = Math.max(0, b.rel - points);
    }

    // ---- favors and promises ----

    /** The favor a member owes you, if any. */
    public static Favor owed(Game g, Member m) {
        for (Favor f : g.cong.favors) if (!f.settled && f.theyOwe && f.member == m) return f;
        return null;
    }

    /** A promise you have made a member and not yet kept, if any. */
    public static Favor promised(Game g, Member m) {
        for (Favor f : g.cong.favors) if (!f.settled && !f.theyOwe && f.member == m) return f;
        return null;
    }

    public static List<Favor> open(Game g, boolean theyOwe) {
        List<Favor> l = new ArrayList<Favor>();
        for (Favor f : g.cong.favors) if (!f.settled && f.theyOwe == theyOwe) l.add(f);
        return l;
    }

    /** A member now owes you a favor (one at a time; a second good turn just warms them up). */
    public static void earn(Game g, Member m, String why) {
        m.rel = Math.min(100, m.rel + 4);
        if (owed(g, m) != null) return;
        Favor f = new Favor();
        f.member = m; f.theyOwe = true; f.why = why; f.week = g.week;
        g.cong.favors.add(f);
    }

    /** You promise to be seen in a member's state by a deadline. */
    public static Favor promise(Game g, Member m, String why, int state, int due) {
        Favor f = new Favor();
        f.member = m; f.theyOwe = false; f.why = why; f.week = g.week; f.state = state; f.due = due;
        g.cong.favors.add(f);
        return f;
    }

    /** Calls in a favor a member owes: it's used up. */
    public static boolean callIn(Game g, Member m) {
        Favor f = owed(g, m);
        if (f == null) return false;
        f.settled = true;
        return true;
    }

    /** The week of the next general election (the midterms, or the presidential election). */
    public static int nextElectionWeek(Game g) {
        int mid = Midterms.electionDay(g);
        if (!g.cong.midDone && mid >= g.day()) return mid / 7;
        return Reelection.electionDay(g) / 7;
    }

    /** The state you've promised to visit soonest, or -1: the hands-off president schedules a trip there. */
    public static int promisedState(Game g) {
        if (g.cong == null) return -1;
        Favor best = null;
        for (Favor f : g.cong.favors) if (!f.settled && !f.theyOwe && f.state >= 0 && (best == null || f.due < best.due)) best = f;
        return best == null ? -1 : best.state;
    }

    // ---- the week ----

    /** After the week's calendar has played out: the mandate fades, trips keep promises, members ask for things. */
    public static void week(Game g) {
        Congress c = g.cong;
        c.hoursUsed = 0;
        c.mandate *= 0.965;
        if (g.week % 4 == 3 && c.appointments < MAX_APPOINTMENTS) c.appointments++;
        visits(g);
        promises(g);
        if (g.week >= c.nextAsk) ask(g);
        prune(g);
    }

    /** Trips this week keep promises, and campaigning for your party's members earns their gratitude. */
    static void visits(Game g) {
        Congress c = g.cong;
        boolean campaign = Midterms.season(g) || Reelection.season(g);
        boolean[] seen = new boolean[NS];
        for (int i = 0; i < NSLOT; i++) if ((g.act[i] == AC_TRIP || g.act[i] == AC_CAMPAIGN) && g.arg[i] >= 0 && g.arg[i] < NS) seen[g.arg[i]] = true;
        for (Favor f : new ArrayList<Favor>(c.favors)) {
            if (f.settled || f.theyOwe || f.state < 0 || !seen[f.state]) continue;
            f.settled = true;
            c.kept++;
            f.member.rel = Math.min(100, f.member.rel + 6);
            earn(g, f.member, "You kept your word and came to " + STATES[f.state].name);
            g.log(NK_INFO, g.pres.last + " keeps a promise to " + f.member.name + " with a stop in " + STATES[f.state].name, null, 0.5);
        }
        for (Member m : c.members) {
            if (m.party != g.party || m.role == RO_LEADER || m.state < 0 || !seen[m.state]) continue;
            m.rel = Math.min(100, m.rel + 2);
            if (campaign) earn(g, m, "You campaigned for them in " + STATES[m.state].name);
        }
    }

    /** Promises past their deadline are broken, and the member and their bloc remember. */
    static void promises(Game g) {
        Congress c = g.cong;
        for (Favor f : c.favors) {
            if (f.settled || f.theyOwe || f.due < 0 || g.week <= f.due) continue;
            f.settled = true;
            c.broken++;
            f.member.rel = Math.max(0, f.member.rel - 20);
            if (f.member.bloc >= 0 && f.member.bloc < c.blocs.size()) { Bloc b = c.blocs.get(f.member.bloc); b.rel = Math.max(0, b.rel - 4); }
            g.log(NK_BAD, f.member.name + " says the president broke a promise", "The White House had promised a visit to " + STATES[f.state].name + ". It never came.", 1.2);
        }
    }

    /** Drops settled favors after half a year, and any from members who have left Congress. */
    static void prune(Game g) {
        Congress c = g.cong;
        for (int i = c.favors.size() - 1; i >= 0; i--) {
            Favor f = c.favors.get(i);
            if (!c.members.contains(f.member) || f.settled && g.week - f.week > 26) c.favors.remove(i);
        }
    }

    /** Now and then a member asks the president for something. Saying yes earns a favor; saying no costs a little goodwill. */
    static void ask(Game g) {
        Congress c = g.cong;
        Random r = g.rng;
        c.nextAsk = g.week + 5 + r.nextInt(5);
        List<Member> l = new ArrayList<Member>();
        for (Member m : c.members) if ((m.role == RO_SWING || m.role == RO_CHAIR) && owed(g, m) == null && promised(g, m) == null) l.add(m);
        if (l.isEmpty()) return;
        Member m = l.get(r.nextInt(l.size()));
        int kind = r.nextInt(3);
        if (kind == ASK_APPOINT && c.appointments <= 0) kind = ASK_PROJECT;
        if (kind == ASK_VISIT && m.state < 0) kind = ASK_PROJECT;
        String who = m.title != null && m.role == RO_SWING ? m.title : m.name + " (" + PARTY_SHORT[m.party] + "), " + lcTitle(m.title);
        String st = m.state >= 0 ? STATES[m.state].name : "their state";
        String title, body;
        String[] advice;
        if (kind == ASK_VISIT) {
            title = m.name + " asks for a visit";
            body = who + " wants you in " + st + " within eight weeks: a plant opening, a town hall, anything with the president in it. Say yes and you'll need a trip there on your calendar by " + dateStr((g.week + 8) * 7) + ".";
            advice = new String[]{"Legislative director: \"Easy to give and they'll remember it. Just make sure we actually go.\"", "Chief of staff: \"Your calendar is full enough. Promises we can't keep are worse than no.\""};
        } else if (kind == ASK_PROJECT) {
            title = m.name + " asks for a project back home";
            body = who + " wants federal money for a project in " + st + ": a bridge, a lab, a port. It would add a little to the deficit, and deficit hawks will grumble.";
            advice = new String[]{"Legislative director: \"It's how this town works. We'll have their vote when we need it.\"", "Budget director: \"Every project is a precedent. The hawks are already counting.\""};
        } else {
            title = m.name + " asks for an appointment";
            body = who + " would like an ally named to a federal board or commission. You have " + c.appointments + " appointment" + (c.appointments == 1 ? "" : "s") + " to give; one more opens up each month.";
            advice = new String[]{"Legislative director: \"A seat on a commission is cheap for a vote we'll need.\"", "Chief of staff: \"We have few enough of these. Keep them for the votes that count.\""};
        }
        Decision d = Decisions.add(g, D_ASK, title, body, new String[]{"Say yes", "Not now"}, advice);
        d.member = m; d.a = kind;
    }

    /** Your answer to a member's request. */
    public static void answer(Game g, Decision d, int k) {
        Member m = d.member;
        Congress c = g.cong;
        if (m == null || !c.members.contains(m)) return;
        if (k != 0) { m.rel = Math.max(0, m.rel - 6); return; }
        if (d.a == ASK_VISIT) {
            promise(g, m, "A visit to " + STATES[m.state].name, m.state, g.week + 8);
            m.rel = Math.min(100, m.rel + 3);
        } else if (d.a == ASK_PROJECT) {
            g.econ.fiscal += 0.005;
            for (Bloc b : c.blocs) if (b.chamber == m.chamber && b.hawk > 0.6) b.rel = Math.max(0, b.rel - 1);
            earn(g, m, "Funding for a project back home");
        } else {
            if (c.appointments <= 0) { m.rel = Math.max(0, m.rel - 3); return; }
            c.appointments--;
            earn(g, m, "An appointment for an ally");
        }
    }

    /** What the hands-off president says to a request. */
    public static int auto(Game g, Decision d) {
        if (d.a == ASK_APPOINT) return g.cong.appointments >= 2 ? 0 : 1;
        if (d.a == ASK_PROJECT) return g.econ.deficit < 7 ? 0 : 1;
        return promisedState(g) < 0 ? 0 : 1;
    }

    /** One line on where the president stands on the Hill, for the header of the Roosevelt Room. */
    public static String summary(Game g) {
        Congress c = g.cong;
        int owed = open(g, true).size(), prom = open(g, false).size();
        return hours(hoursLeft(g)) + " of " + hours(hoursMax(g)) + " hours left this week  -  " + owed + (owed == 1 ? " favor" : " favors") + " owed to you"
                + (prom > 0 ? ", " + prom + (prom == 1 ? " promise" : " promises") + " to keep" : "");
    }

    public static String hours(double v) { return Math.abs(v - Math.round(v)) < 0.05 ? String.valueOf(Math.round(v)) : String.valueOf(Math.round(v * 10) / 10.0); }
}
