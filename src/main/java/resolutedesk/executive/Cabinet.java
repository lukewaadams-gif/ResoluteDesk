package resolutedesk.executive;

import static resolutedesk.congress.CongressRules.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.data.Candidates.*;
import static resolutedesk.data.Faces.*;
import static resolutedesk.data.Names.*;
import static resolutedesk.data.RealCabinets.*;
import static resolutedesk.executive.CourtHistory.*;
import static resolutedesk.executive.Delegation.*;
import static resolutedesk.executive.Posts.*;
import static resolutedesk.executive.Traits.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.model.Attributes.*;
import static resolutedesk.model.NewsKinds.*;
import static resolutedesk.model.Parties.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import resolutedesk.congress.Bloc;
import resolutedesk.congress.Hill;
import resolutedesk.congress.Relations;
import resolutedesk.core.Seeds;
import resolutedesk.model.Cand;
import resolutedesk.model.Game;
import resolutedesk.model.Look;
import resolutedesk.sim.Decisions;

/**
 * Nominees go to the Senate for a vote; staff serve at your pleasure. Everyone has skill, loyalty and morale.
 * Morale falls when you're unpopular, when the place is tense and when you overrule them; low morale means leaks and resignations.
 */
public final class Cabinet {
    /** A staff member's effectiveness (0-1), or a stand-in's if the job is empty. */
    public static double sq(Game g, int role) { if (g.ex == null) return 0.5; Person p = g.ex.staff[role]; return p == null ? 0.4 : p.q(); }

    /** A cabinet officer's effectiveness (0-1); acting officials are career people of middling skill. */
    public static double pq(Game g, int post) { if (g.ex == null) return 0.5; Person p = g.ex.post[ep(post)]; return p == null ? 0.45 : p.q(); }

    /** Readiness of the department that does a post's work this year. */
    public static double rd(Game g, int post) { return g.ex.ready[ep(post)]; }

    public static void setup(Game g) {
        Exec x = new Exec();
        g.ex = x;
        Random r = new Random(g.set.seed ^ 0xCAB1L);
        for (int i = 0; i < NPOST; i++) { x.voteAt[i] = -1; x.wf[i] = POST_WF[i]; x.ready[i] = 62 + r.nextGaussian() * 5; }
        List<Person> real = realSlate(g, r);
        if (!real.isEmpty()) {
            for (Person p : real) { if (p.role >= 0) { p.since = 0; x.staff[p.role] = p; } else x.nominee[p.post] = p; }
        } else {
            for (int s = 0; s < NSTAFF; s++) { Person p = staffOptions(g, s, 0).get(0); p.since = 0; x.staff[s] = p; }
            for (int k = 0; k < NPOST; k++) if (postOn(k)) x.nominee[k] = options(g, k, 0).get(0);
        }
        schedule(g);
        // the Supreme Court
        for (String row : courtIn(g.era)) {
            String[] t = row.split("\\|");
            Justice j = new Justice();
            j.name = t[0]; j.born = Integer.parseInt(t[1]); j.party = t[2].equals("D") ? DEM : REP; j.ideo = Double.parseDouble(t[3]);
            j.face = face(j.name, r, j.party);
            if (j.face.look != null && realLook(j.name) == null) {
                boolean f = false;
                for (String fj : FEMALE_JUSTICES) if (fj.equals(j.name)) f = true;
                femaleLook(j.face.look, f, r);
                int age = g.era.year - j.born;
                j.face.look.age = clamp(0.3 + (age - 40) * 0.016, 0.3, 0.95); j.face.look.gray = clamp((age - 45) * 0.025, 0, 0.95);
                if (j.face.look.skin > 2 && !j.name.equals("Thurgood Marshall") && !j.name.equals("Clarence Thomas")) j.face.look.skin = r.nextInt(2);
                if (j.name.equals("Thurgood Marshall")) j.face.look.skin = 5;
            }
            x.court.add(j);
        }
        if (g.era.year < 2025) { double[] bn = benchIn(g.era.year); x.dDistrict = bn[0]; x.dCircuit = bn[1]; }
        if (g.era.year >= 2029) { x.dDistrict = 0.46; x.dCircuit = 0.42; }
        for (int d = 0; d < NDG; d++) x.deleg[d] = false;
    }

    /** In 2025 with the real president: the real cabinet nominees and senior staff, or an empty list in any other game. */
    public static List<Person> realSlate(Game g, Random r) {
        List<Person> l = new ArrayList<Person>();
        if (g.era.year != 2025 || g.pres == null || !"trump".equals(g.pres.id)) return l;
        for (String row : SLATE25) {
            String[] t = row.split("\\|");
            int k = Integer.parseInt(t[0]);
            Person p = new Person();
            p.name = t[1]; p.background = t[2]; p.party = REP; p.real = true;
            p.skill = Double.parseDouble(t[4]); p.loyalty = Double.parseDouble(t[5]); p.ideo = Double.parseDouble(t[6]); p.trait = Integer.parseInt(t[7]);
            p.rosterId = t.length > 8 && t[8].length() > 0 ? t[8] : null;
            Cand c = p.rosterId != null ? candById(p.rosterId) : null;
            p.face = c != null ? c : face(p.name, r, REP);
            if (c == null && p.face.look != null && realLook(p.name) == null) femaleLook(p.face.look, t[3].equals("F"), r);
            if (k >= 100) p.role = k - 100; else p.post = k;
            l.add(p);
        }
        return l;
    }

    /** The Senate takes the nominees a few at a time, the top jobs first. */
    public static void schedule(Game g) {
        Exec x = g.ex;
        int[] order = {P_STATE, P_TREAS, P_DEF, P_DHS, P_DOJ, P_CIA, P_DNI, P_OMB, P_HHS, P_COM, P_INT, P_DOE, P_AG, P_VA, P_DOT, P_HUD, P_LAB, P_ED, P_EPA, P_USTR, P_UN};
        int n = 0;
        for (int i = 0; i < order.length; i++) { if (x.nominee[order[i]] == null) continue; x.voteAt[order[i]] = n == 0 ? 0 : 1 + n / 3; x.nominee[order[i]].nominated = 0; n++; }
    }

    public static void femaleLook(Look l, boolean f, Random r) {
        l.female = f;
        if (f && l.style < 6) l.style = 6 + r.nextInt(4);
        if (!f && l.style >= 6) l.style = r.nextInt(5);
        if (f) l.facial = 0;
    }

    /** The surname of a person's full name. */
    public static String surname(String name) { return name.substring(name.lastIndexOf(' ') + 1); }

    /** Surnames already taken in this administration: the president, the vice president, the staff and the cabinet. */
    public static Set<String> takenSurnames(Game g) {
        Set<String> s = new HashSet<String>();
        if (g.pres != null) s.add(g.pres.last);
        if (g.vp != null) s.add(g.vp.last);
        if (g.ex != null) {
            for (Person p : g.ex.staff) if (p != null) s.add(surname(p.name));
            for (Person p : g.ex.post) if (p != null) s.add(surname(p.name));
            for (Person p : g.ex.nominee) if (p != null) s.add(surname(p.name));
        }
        return s;
    }

    /** A generated person for a job, with a surname no one else in the administration has. kind: 0 loyalist, 1 expert, 2 bipartisan or outsider. */
    public static Person make(Game g, Random r, int party, int kind, Set<String> taken) {
        Person p = new Person();
        boolean f = genFemale(r, 0.42);
        String last = genLast(r);
        for (int tries = 0; tries < 40 && taken.contains(last); tries++) last = genLast(r);
        taken.add(last);
        p.name = genFirst(r, f) + " " + last;
        p.party = party;
        double side = party == REP ? 1 : -1;
        if (kind == 0) { p.skill = 52 + r.nextInt(20); p.loyalty = 80 + r.nextInt(16); p.ideo = side * (0.55 + r.nextDouble() * 0.35); p.trait = r.nextDouble() < 0.5 ? T_LOYAL : pickTrait(r); }
        else if (kind == 1) { p.skill = 70 + r.nextInt(22); p.loyalty = 52 + r.nextInt(25); p.ideo = side * (0.2 + r.nextDouble() * 0.3); p.trait = r.nextDouble() < 0.45 ? T_EXPERT : pickTrait(r); }
        else { p.skill = 60 + r.nextInt(22); p.loyalty = 40 + r.nextInt(25); p.ideo = side * (r.nextDouble() * 0.25); p.trait = pickTrait(r); }
        p.background = BACKGROUNDS[r.nextInt(BACKGROUNDS.length)];
        p.face = face(p.name, r, party);
        if (p.face.look != null) femaleLook(p.face.look, f, r);
        return p;
    }

    static int pickTrait(Random r) { double x = r.nextDouble(); return x < 0.12 ? T_LEAKER : x < 0.22 ? T_ABRASIVE : x < 0.34 ? T_AMBITIOUS : x < 0.42 ? T_RISK : r.nextInt(6); }

    /** Three choices for a cabinet post: a loyalist, an expert, and a big name or a bipartisan pick. Same choices all month. */
    public static List<Person> options(Game g, int post, int salt) {
        Random r = new Random(Seeds.mix(g.set.seed, 0xCAB1L, post, g.week / 4, salt));
        Set<String> taken = takenSurnames(g);
        List<Person> l = new ArrayList<Person>();
        l.add(make(g, r, g.party, 1, taken));
        l.add(make(g, r, g.party, 0, taken));
        Cand big = bigName(g, post);
        if (big != null && (yearNow(g) >= 2021 || yearNow(g) >= 2013 && big.born > 0 && yearNow(g) - big.born >= 45)) {
            Person p = new Person();
            p.name = big.name(); p.party = big.party; p.real = true; p.rosterId = big.id; p.face = big;
            p.background = big.role != null ? big.role : "Politician";
            p.skill = clamp(45 + big.a[A_EXE] * 0.25 + big.a[A_INT] * 0.2, 50, 90);
            p.loyalty = 60 + r.nextInt(15); p.ideo = clamp((big.econ + big.cult) / 2, -1, 1); p.trait = r.nextDouble() < 0.5 ? T_MEDIA : T_STEADY;
            l.add(p);
        } else {
            Person p = make(g, r, r.nextDouble() < 0.5 ? Hill.other(g.party) : g.party, 2, taken);
            if (p.party != g.party) p.background = "A " + PARTY[p.party] + " " + p.background.toLowerCase();
            l.add(p);
        }
        for (Person p : l) p.post = post;
        for (Person p : l) if (p.real && p.trait >= T_LEAKER) p.trait = T_STEADY;
        return l;
    }

    static Cand bigName(Game g, int post) {
        String[] map = g.party == DEM ? BIGNAMES_D : BIGNAMES_R;
        for (String m : map) {
            String[] kv = m.split(":");
            if (Integer.parseInt(kv[0]) != post) continue;
            for (String id : kv[1].split(",")) {
                Cand c = candById(id);
                if (c == null || c == g.pres || c == g.vp) continue;
                if (inGovernment(g, c.name())) continue;
                return c;
            }
        }
        return null;
    }

    static boolean inGovernment(Game g, String name) {
        if (g.ex == null) return false;
        for (Person p : g.ex.post) if (p != null && p.name.equals(name)) return true;
        for (Person p : g.ex.nominee) if (p != null && p.name.equals(name)) return true;
        for (Person p : g.ex.staff) if (p != null && p.name.equals(name)) return true;
        return false;
    }

    public static List<Person> staffOptions(Game g, int role, int salt) {
        Random r = new Random(Seeds.mix(g.set.seed, 0x57AFL, role, g.week / 4, salt));
        Set<String> taken = takenSurnames(g);
        List<Person> l = new ArrayList<Person>();
        l.add(make(g, r, g.party, 1, taken));
        l.add(make(g, r, g.party, 0, taken));
        l.add(make(g, r, g.party, 2, taken));
        for (Person p : l) { p.role = role; p.party = g.party; }
        l.get(0).background = role == S_COUNSEL ? "Former federal prosecutor" : role == S_NEC ? "Economist" : role == S_LEG ? "Hill veteran" : role == S_PRESS || role == S_COMMS ? "Former network correspondent" : "Veteran of two administrations";
        l.get(1).background = "Campaign veteran";
        return l;
    }

    // ---- the Senate's vote ----
    static double blocP(Game g, int bi, Person p) {
        Bloc b = g.cong.blocs.get(bi);
        double bIdeo = (b.econ + b.cult) / 2;
        double dist = Math.abs(p.ideo - bIdeo);
        double l = 0.6 + (p.skill - 62) / 22 + (b.rel - 50) / 40 + (p.trait == T_EXPERT ? 0.4 : 0);
        if (b.party == g.party) l += 3.0 * b.loyalty - 1.0 * dist;                   // senators defer to their own president's picks
        else l += 0.5 - 2.2 * dist - 1.2 * g.era.polarization;
        if (p.party != g.party) l += b.party == p.party ? 1.6 : -0.4;
        if (p.real && p.rosterId != null && b.party != g.party) l += 0.3;      // a senator or governor colleagues know
        l += p.senate;                                                         // the calls the transition made
        if (p.baggageKnown) l -= p.baggageWeight;                              // what the hearings dwell on
        return logistic(1.5 * l);
    }

    public static double[] count(Game g, Person p) {
        double yes = 0, var = 0;
        for (int i = 0; i < g.cong.blocs.size(); i++) {
            Bloc b = g.cong.blocs.get(i);
            if (b.chamber != SENATE) continue;
            double q = blocP(g, i, p);
            yes += b.seats * q; var += b.seats * q * (1 - q);
        }
        return new double[]{yes, Math.sqrt(var) + 0.8};
    }

    public static double chance(Game g, Person p) {
        double[] c = count(g, p);
        return logistic(1.702 * (c[0] - 50 + 0.5) / c[1]);
    }

    public static void nominate(Game g, int post, Person p) {
        Exec x = g.ex;
        x.nominee[post] = p;
        p.post = post; p.nominated = g.week;
        int busy = 0;
        for (int k = 0; k < NPOST; k++) if (x.voteAt[k] >= g.week) busy++;
        x.voteAt[post] = g.week + 3 + busy / 3;
        g.log(NK_INFO, g.pres.last + " nominates " + p.name + " as " + lcTitle(POST_TITLE[post]), null, 0.9);
    }

    public static void withdraw(Game g, int post) {
        Exec x = g.ex;
        Person p = x.nominee[post];
        if (p == null) return;
        x.nominee[post] = null; x.voteAt[post] = -1;
        g.log(NK_BAD, "The White House withdraws the nomination of " + p.name, null, 1.0);
        g.perf -= 0.2;
    }

    static void vote(Game g, int post) {
        Exec x = g.ex;
        Person p = x.nominee[post];
        if (p == null) return;
        if (p.baggageWeight > 0 && !p.baggageKnown && g.rng.nextDouble() < 0.55) {
            // what no one checked comes out at the hearing
            p.baggageKnown = true;
            g.perf -= 0.6;
            g.log(NK_BAD, "Confirmation hearing: " + p.name + " is pressed over " + p.baggage, "Senators say the White House should have known. The nomination is in trouble.", 1.8);
        }
        double[] c = count(g, p);
        int yes = (int) Math.round(clamp(c[0] + g.rng.nextGaussian() * c[1], 0, 100)), no = 100 - yes;
        boolean tie = yes == 50 && Hill.majority(g, SENATE, g.party);
        p.voteYes = yes; p.voteNo = no; p.lastVote = g.week;
        x.nominee[post] = null; x.voteAt[post] = -1;
        if (yes > 50 || tie) {
            p.confirmed = true; p.acting = false; p.since = g.week; p.morale = 75;
            x.post[post] = p;
            g.log(post <= P_DHS || post == P_OMB ? NK_GOOD : NK_INFO, "Senate confirms " + p.name + " as " + lcTitle(POST_TITLE[post]) + ", " + yes + "-" + no + (tie ? ", with the vice president breaking the tie" : ""), null, post <= P_DEF || tie ? 1.4 : 0.8);
        } else {
            g.log(NK_BIG, "SENATE REJECTS " + p.name.toUpperCase() + " FOR " + POST_TITLE[post].toUpperCase(), "The vote was " + yes + "-" + no + ". A career official stays in charge until you name someone else.", 2.6);
            g.perf -= 1.0; Relations.setback(g, 1);
            Decisions.info(g, "The Senate rejects your nominee", p.name + " lost the vote to be " + lcTitle(POST_TITLE[post]) + ", " + yes + "-" + no + ". Choose someone else in the West Wing (the Cabinet tab).");
        }
    }

    public static void appoint(Game g, int role, Person p) {
        Exec x = g.ex;
        p.role = role; p.since = g.week; p.morale = 75;
        x.staff[role] = p;
        g.log(NK_INFO, g.pres.last + " names " + p.name + " " + STAFF_TITLE[role].toLowerCase(), null, role == S_COS ? 1.4 : 0.7);
    }

    /** You fire someone. */
    public static void fire(Game g, Person p) {
        Exec x = g.ex;
        boolean big = p.role == S_COS || p.post >= 0 && p.post <= P_DEF || p.post == P_DOJ;
        remove(g, p);
        g.log(big ? NK_BIG : NK_BAD, (big ? (g.pres.last + " fires " + p.name).toUpperCase() : g.pres.last + " fires " + p.name), "The " + lcTitle(p.title()) + " is out after " + Math.max(1, g.week - p.since) + " weeks.", big ? 2.4 : 1.2);
        g.perf -= big ? 0.8 : 0.3;
        for (Person o : everyone(g)) o.morale = Math.max(0, o.morale - 2);
        x.departures++;
        if (p.post == P_DOJ) for (Scandal s : x.scandals) if (!s.over && s.stage == 2) { s.severity += 3; s.impeachable = true; g.log(NK_BIG, "FIRING OF THE ATTORNEY GENERAL RAISES QUESTIONS ABOUT THE SPECIAL COUNSEL", null, 3); }
    }

    public static void remove(Game g, Person p) {
        Exec x = g.ex;
        if (p.role >= 0 && x.staff[p.role] == p) x.staff[p.role] = null;
        if (p.post >= 0 && x.post[p.post] == p) x.post[p.post] = null;
    }

    public static List<Person> everyone(Game g) {
        List<Person> l = new ArrayList<Person>();
        for (Person p : g.ex.staff) if (p != null) l.add(p);
        for (Person p : g.ex.post) if (p != null) l.add(p);
        return l;
    }

    /** A cabinet meeting on the calendar. */
    public static void meeting(Game g) {
        if (g.ex == null) return;
        for (Person p : g.ex.post) if (p != null) p.morale = Math.min(100, p.morale + 3);
        for (int k = 0; k < NPOST; k++) g.ex.ready[k] = Math.min(100, g.ex.ready[k] + 0.6);
        if (g.week - g.ex.lastCabinet > 6) g.log(NK_INFO, "The president holds a cabinet meeting", null, 0.4);
        g.ex.lastCabinet = g.week;
    }

    public static void week(Game g) {
        Exec x = g.ex;
        Random r = g.rng;
        // confirmation votes
        for (int k = 0; k < NPOST; k++) if (x.nominee[k] != null && x.voteAt[k] >= 0 && g.week >= x.voteAt[k]) {
            if (!Hill.majority(g, SENATE, g.party) && chance(g, x.nominee[k]) < 0.2 && g.week - x.nominee[k].nominated < 20) { x.voteAt[k] = g.week + 2; continue; }   // the majority sits on it
            vote(g, k);
        }
        // morale
        int abrasive = 0;
        for (Person p : everyone(g)) if (p.trait == T_ABRASIVE) abrasive++;
        for (Person p : everyone(g)) {
            double target = 58 + (g.approval - 45) * 0.7 + (p.loyalty - 60) * 0.35 - Math.max(0, g.stress - 45) * 0.25 - (abrasive - (p.trait == T_ABRASIVE ? 1 : 0)) * 2.5
                    + (g.cong.shutdown ? -6 : 0) + (p.trait == T_STEADY ? 4 : 0);
            p.morale = clamp(p.morale + (target - p.morale) * 0.05 + r.nextGaussian() * 0.8, 0, 100);
        }
        // the chief of staff keeps the temperature down
        g.stress = clamp(g.stress - 2.5 * (sq(g, S_COS) - 0.4), 0, 100);
        // departures
        for (Person p : everyone(g)) {
            if (g.week - p.since < 12) continue;
            double hz = 0.0006 + Math.max(0, 38 - p.morale) / 700 + (p.trait == T_AMBITIOUS && g.tw() > 70 ? 0.003 : 0) - (p.trait == T_LOYAL ? 0.0004 : 0);
            if (r.nextDouble() < hz) { resign(g, p); break; }
        }
        // readiness of each department
        double fed = 0;
        for (int k = 0; k < NPOST; k++) {
            double q = pq(g, k);
            double cut = Math.max(0, (POST_WF[k] - x.wf[k]) / POST_WF[k]);
            double target = 40 + 42 * q - cut * 70 + (g.week - x.lastCabinet < 8 ? 3 : 0) - (g.cong.shutdown ? 12 : 0)
                    + (x.post[k] != null && x.post[k].trait == T_WORKHORSE ? 5 : 0) + (x.post[k] != null && x.post[k].trait == T_EXPERT ? 3 : 0);
            x.ready[k] = clamp(x.ready[k] + (target - x.ready[k]) * 0.04, 5, 100);
            fed += x.wf[k];
        }
        // leaks
        double discipline = 1.3 - 0.7 * sq(g, S_COS);
        for (Person p : everyone(g)) {
            double pl = (0.0007 + (p.trait == T_LEAKER ? 0.006 : 0) + Math.max(0, 50 - p.morale) / 9000 + (p.loyalty < 50 ? 0.0015 : 0)) * discipline;
            if (r.nextDouble() < pl) { Trouble.leak(g, p); break; }
        }
        // ethics
        for (Person p : everyone(g)) {
            if (p.real) continue;
            double pe = p.trait == T_RISK ? 0.0035 : 0.00025;
            if (r.nextDouble() < pe) { Trouble.ethics(g, p); break; }
        }
        // the delegate fills empty jobs
        if (x.deleg[DG_PEOPLE]) fill(g);
    }

    static void resign(Game g, Person p) {
        Exec x = g.ex;
        String why = p.trait == T_AMBITIOUS && g.tw() > 70 ? "to run for office" : p.morale < 30 ? "after months of friction" : "to return to private life";
        remove(g, p);
        x.departures++;
        boolean big = p.role == S_COS || p.role == S_NSA || p.post >= 0 && p.post <= P_DEF || p.post == P_DOJ;
        g.log(big ? NK_BIG : NK_BAD, (p.name + " resigns as " + lcTitle(p.title()) + " " + why), null, big ? 2.2 : 1.0);
        if (big) g.perf -= 0.4;
        if (!x.deleg[DG_PEOPLE]) Decisions.info(g, p.name + " resigns", "Your " + lcTitle(p.title()) + " is leaving " + why + ". " + (p.role >= 0 ? "Pick a replacement in the West Wing (the Staff tab)." : "A career official takes over until you nominate someone (the Cabinet tab)."));
    }

    /** Congress creates a department: the president puts a name forward at once. */
    public static void created(Game g, int k) {
        if (g.ex == null || g.ex.post[k] != null || g.ex.nominee[k] != null) return;
        g.ex.ready[k] = 50;
        String what = k == P_EPA ? "the Environmental Protection Agency" : k == P_UN ? "the post of ambassador to the new United Nations" : k == P_CIA ? "the Central Intelligence Agency"
                : k == P_DNI ? "the post of director of national intelligence" : k == P_USTR ? "the Special Trade Representative" : "the Department of " + POST_NAME[k];
        g.log(NK_GOOD, "Congress creates " + what, "The work moves over from " + POST_NAME[POST_PARENT[k]] + ".", 1.4);
        nominate(g, k, safest(g, options(g, k, g.week)));
    }

    /** Fill every empty job with the first choice. */
    public static void fill(Game g) {
        Exec x = g.ex;
        for (int s = 0; s < NSTAFF; s++) if (x.staff[s] == null) appoint(g, s, staffOptions(g, s, g.week).get(0));
        for (int k = 0; k < NPOST; k++) if (postOn(k) && x.post[k] == null && x.nominee[k] == null) {
            List<Person> o = options(g, k, g.week);
            Person best = o.get(0);
            for (Person p : o) if (chance(g, p) > chance(g, best) + 0.15) best = p;
            nominate(g, k, best);
        }
    }

    /** The pick of the three with the best chance in the Senate. */
    static Person safest(Game g, List<Person> o) {
        Person best = o.get(0);
        for (Person p : o) if (chance(g, p) > chance(g, best) + 0.1) best = p;
        return best;
    }
}
