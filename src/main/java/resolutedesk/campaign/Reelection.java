package resolutedesk.campaign;

import static resolutedesk.campaign.CampaignData.*;
import static resolutedesk.congress.CongressHistory.*;
import static resolutedesk.congress.CongressRules.*;
import static resolutedesk.core.Dates.*;
import static resolutedesk.core.Fmt.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.data.Candidates.*;
import static resolutedesk.data.ElectoralMap.*;
import static resolutedesk.data.Eras.*;
import static resolutedesk.data.HistoricalFigures.*;
import static resolutedesk.data.Names.*;
import static resolutedesk.data.StateData.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.model.Attributes.*;
import static resolutedesk.model.Factions.*;
import static resolutedesk.model.NewsKinds.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.opinion.Issues.*;
import static resolutedesk.sim.DecisionKinds.*;
import static resolutedesk.sim.Rosters.*;

import java.util.Random;
import resolutedesk.congress.Bill;
import resolutedesk.congress.Hill;
import resolutedesk.congress.Leaders;
import resolutedesk.congress.Relations;
import resolutedesk.executive.Cabinet;
import resolutedesk.model.Cand;
import resolutedesk.model.Econ;
import resolutedesk.model.Era;
import resolutedesk.model.Game;
import resolutedesk.sim.Decision;
import resolutedesk.sim.Decisions;
import resolutedesk.ui.Creator;
import resolutedesk.world.War;

/** The re-election campaign: deciding to run, the conventions, the debates, the fall campaign, the polls by state and election night. */
public final class Reelection {
    /** The election year of the current term. */
    public static int year(Game g) { return termYear(g) + 3; }

    public static int electionDay(Game g) {
        int y = year(g);
        for (int d = 2; d <= 8; d++) { int day = dayOfDate(y, 10, d); if (dowOf(day) == 1) return day; }
        return dayOfDate(y, 10, 3);
    }

    public static int weeksTo(Game g) { return (int) Math.floor((electionDay(g) - g.day()) / 7.0); }

    /** The fall campaign: stops on the calendar count. */
    public static boolean season(Game g) { return g.re != null && !g.re.done && (g.re.stage == 2 || g.re.stage == 3) && weeksTo(g) <= 10 && weeksTo(g) >= 0; }

    public static boolean debatesHeld(int y) { return y == 1960 || y >= 1976; }

    public static void week(Game g) {
        if (g.over || g.removed) return;
        int wt = weeksTo(g);
        if (g.re == null) {
            if (wt <= 50 && wt > 0) begin(g);
            return;
        }
        Reelect re = g.re;
        if (re.done) return;
        Random r = g.rng;
        if (re.stage == 1) return;           // waiting for your answer
        // the primaries: a challenger chips away if you're weak
        if (re.stage == 2 && re.challenger != null && wt <= 44 && wt >= 20 && wt % 2 == 0) {
            double share = clamp(re.chal - (g.approval - 40) * 0.9 + r.nextGaussian() * 4, 5, 75);
            re.chalShare = re.chalShare <= 0 ? share : re.chalShare * 0.7 + share * 0.3;
            String st = PRIMARY_STATES[(44 - wt) / 2 % PRIMARY_STATES.length];
            boolean lost = share > 50;
            g.log(lost ? NK_BAD : NK_INFO, (lost ? re.challenger + " beats the president in the " : "The president wins the ") + st + " primary, " + Math.round(lost ? share : 100 - share) + "-" + Math.round(lost ? 100 - share : share), null, lost ? 2.2 : 1.0);
            if (lost) { g.perf -= 0.6; g.home.fac[0] = Math.max(0, g.home.fac[0] - 2); }
            if (wt == 20) {
                if (re.chalShare > 50) {
                    re.denied = true; re.running = false; re.stage = 3;
                    re.nominee = successor(g);
                    re.nomVp = null;
                    g.log(NK_BIG, ("Your party denies President " + g.pres.last + " a second nomination").toUpperCase(), re.challenger + " won the primaries; " + re.nominee.name() + " will be the nominee.", 5);
                    Decisions.info(g, "Denied Renomination", re.challenger + " has beaten you in the primaries. " + re.nominee.name() + " will carry the party in November. You are a lame duck for the rest of your term.");
                    g.perf -= 2;
                } else {
                    g.log(NK_GOOD, "The president clinches renomination over " + re.challenger, null, 2);
                    g.perf += 0.5;
                }
            }
        }
        // the other party picks its nominee
        if (re.opp == null && wt <= 24) pickOpp(g);
        // conventions
        if (wt == 15 && re.opp != null) {
            re.oppConv = 2 + (re.opp.a[A_SPE] - 60) / 20.0 + r.nextGaussian();
            g.log(NK_INFO, "The " + PARTIES[other(g.party)] + " nominate " + re.opp.name() + (re.oppVp != null ? ", with " + re.oppVp.name() + " as running mate" : ""), null, 2.2);
        }
        if (wt == 11 && (re.stage == 2 || re.stage == 3)) {
            if (re.stage == 2) speechDecision(g);
            else { re.conv = 2 + (re.nominee.a[A_SPE] - 60) / 20.0 + r.nextGaussian(); g.log(NK_INFO, "Your party nominates " + re.nominee.name() + " for president", null, 2); }
        }
        // debates
        if (debatesHeld(year(g)) && re.opp != null) {
            int n = year(g) == 1960 ? 2 : 3;
            int[] at = n == 2 ? new int[]{7, 4} : new int[]{6, 4, 2};
            for (int i = 0; i < n; i++) if (wt == at[i] && re.debatesHeld == i) {
                re.debatesHeld++;
                if (re.stage == 2) debateDecision(g, i);
                else { Cand a = re.nominee; double s = (a.a[A_DEB] - re.opp.a[A_DEB]) / 25.0 + r.nextGaussian() * 0.8; re.debate[i] = clamp(s, -2.5, 2.5); g.log(NK_INFO, (s >= 0 ? a.name() + " wins" : re.opp.name() + " wins") + " the debate, polls say", null, 1.6); }
            }
        }
        // money: fundraising adds to the war chest
        re.money = clamp((g.funds - 40) / 40.0 + (re.adsMix - 55) / 100.0, -2, 2.5);
        poll(g);
        re.conv *= 0.93; re.oppConv *= 0.93;
        if (wt <= 0) election(g);
    }

    static final String[] PRIMARY_STATES = {"New Hampshire", "Florida", "Illinois", "Wisconsin", "Pennsylvania", "Texas", "Ohio", "Indiana", "Oregon", "California", "New Jersey", "New York", "Georgia"};

    static void begin(Game g) {
        Reelect re = new Reelect();
        g.re = re;
        re.year = year(g);
        setEv(re.year);
        re.eligible = Terms.eligible(g);
        re.houseD0 = g.houseD; re.senateD0 = g.senateD;
        re.mood = g.rng.nextGaussian() * 1.8;
        if (!re.eligible) {
            re.stage = 3; re.running = false;
            re.nominee = successor(g);
            re.nomVp = null;
            Decisions.info(g, "The Race to Succeed You", "The Constitution allows two terms, and you have served them. Your party's race to succeed you begins in earnest; " + re.nominee.name()
                    + " is the favorite. You can still help in the fall: your approval will rub off on the nominee, and your campaign stops count.");
            return;
        }
        re.stage = 1;
        String age = g.presAge() >= 75 ? " You would be " + (g.presAge() + 4) + " at the end of a second term." : "";
        Decision d = Decisions.add(g, D_CAMPAIGN, "Another Four Years?", "The " + re.year + " election is a year away. Your party wants to know whether you will run again. Your approval is " + Math.round(g.approval) + "%."
                + (g.approval < 42 ? " At this level, expect a challenge from inside your own party." : "") + age,
                new String[]{"Announce that you're running", "Step aside: let the party choose a successor"},
                new String[]{"Political director: \"" + (g.approval >= 48 ? "We're in good shape. Let's go." : g.approval >= 42 ? "It'll be close, but incumbents usually win." : "It will be hard. But the alternative is worse.") + "\"",
                        "Chief of staff: \"" + (g.approval < 40 ? "There's no shame in it. Some of the greats did it." : "You'd be walking away from a winnable race.") + "\""});
        d.a = 0;
    }

    static void speechDecision(Game g) {
        Decision d = Decisions.add(g, D_CAMPAIGN, "Your Acceptance Speech", "The convention hall is packed and fifty million are watching. What is the heart of your case?", THEME,
                new String[]{"Political director: \"" + (g.approval >= 50 ? "The record is strong. Own it.\"" : "The record is a hard sell right now.\""),
                        "Communications director: \"" + (g.re.opp != null ? "Make it about " + g.re.opp.last + ".\"" : "Make it about them.\""),
                        "Chief of staff: \"Give them a reason to want four more years.\""});
        d.a = 1;
    }

    static void debateDecision(Game g, int i) {
        Reelect re = g.re;
        Decision d = Decisions.add(g, D_CAMPAIGN, (i == 0 ? "The First Debate" : i == 1 ? "The Second Debate" : "The Final Debate") + " with " + re.opp.name(),
                "Seventy million are watching. " + re.opp.last + "'s debating is rated " + re.opp.a[A_DEB] + ", yours " + g.pres.a[A_DEB] + ". How do you play it?", DEBATE_APP,
                new String[]{"Policy staff: \"Your record is your shield.\"", "Political director: \"Make them defend their plan.\"", "Communications director: \"Talk past them, to the country.\"", "Media adviser: \"One great line is all anyone remembers. A bad one too.\""});
        d.a = 2; d.b = i;
    }

    /** Who carries the party when you don't. */
    static Cand successor(Game g) {
        int y = year(g);
        String id = nominee(y, g.party);
        Cand c = id != null ? candById(id) : null;
        if (c == null || c == g.pres) c = g.vp;
        if (c == null || c == g.pres) for (Cand x : eraRoster(g.era)) if (x.party == g.party && x != g.pres) { c = x; break; }
        if (c == null) c = makeCand(g, g.party);
        return c;
    }

    /** The other party's nominee: the real one when history offers a match, otherwise its strongest figure. */
    static void pickOpp(Game g) {
        Reelect re = g.re;
        int y = year(g), op = other(g.party);
        String id = nominee(y, op);
        Cand c = id != null ? candById(id) : null;
        Cand mine = re.nominee != null ? re.nominee : g.pres;
        if (c == g.pres || c == g.vp || c == mine) c = null;
        if (c == null) {
            Era next = era(y + 1);
            if (next.year == y + 1) for (Cand x : eraRoster(next)) if (x.party == op && x != g.pres) { c = x; break; }
        }
        if (c == null) for (Cand x : eraRoster(g.era)) if (x.party == op && x != g.pres && x != g.vp) { c = x; break; }
        if (c == null) c = makeCand(g, op);
        re.opp = c;
        String vid = null;
        for (String s : NOMINEES) if (s.startsWith(y + ":")) { String t = s.substring(5).split(",")[op == DEM ? 0 : 1]; if (t.startsWith(c.id + ">")) vid = t.substring(c.id.length() + 1); }
        re.oppVp = vid != null && !vid.isEmpty() ? candById(vid) : null;
        if (re.oppVp == g.pres || re.oppVp == g.vp) re.oppVp = null;
        g.log(NK_BIG, (c.name() + " clinches the " + PARTY[op] + " nomination").toUpperCase(), null, 3);
    }

    /** A made-up nominee when history runs out. */
    static Cand makeCand(Game g, int party) {
        Random r = new Random(g.set.seed ^ (party * 977L + g.week));
        Cand c = new Cand();
        boolean f = genFemale(r, 0.45);
        c.id = "gen_" + party + "_" + g.week; c.first = genFirst(r, f); c.last = genLast(r); c.party = party; c.fictional = true;
        c.role = "Governor of " + STATES[Hill.swingState(r)].name; c.born = yearNow(g) - 52 - r.nextInt(14);
        for (int i = 0; i < NA; i++) c.a[i] = 50 + r.nextInt(25);
        c.a[A_SCA] = 20 + r.nextInt(30);
        c.home = Hill.swingState(r); c.homeAb = STATES[c.home].ab;
        c.faction = party == DEM ? F_LIB : F_GOV; c.econ = party == DEM ? -0.4 : 0.5; c.cult = party == DEM ? -0.4 : 0.5;
        c.look = Creator.randomLook(r); c.look.female = f; Cabinet.femaleLook(c.look, f, r); genSkin(c.look, r);
        c.bio = "A " + (f ? "woman" : "man") + " the party trusts to take on the White House. Fictional.";
        return c;
    }

    /** The national margin for the president's party (points), before the day's noise. */
    static double national(Game g) {
        Reelect re = g.re;
        Econ e = g.econ;
        Cand me = re.nominee != null ? re.nominee : g.pres, op = re.opp;
        double appK = re.running ? 0.85 : 0.6;
        double pol = g.era.polarization;
        double m = appK * (g.approval - 50) * (1.25 - 0.35 * pol) + (re.running ? 1.2 : -0.5) + 0.5 * (e.gdp - 2) - 0.3 * (e.unemp - e.uRef);
        if (op != null) m += 0.06 * (me.a[A_CHA] - op.a[A_CHA]) + 0.05 * (me.a[A_XOV] - op.a[A_XOV]) - 0.03 * (me.a[A_SCA] - op.a[A_SCA]);
        m += re.conv - re.oppConv;
        for (double d : re.debate) m += d;
        m += re.money;
        m += themeFit(g) * (re.running ? 1 : 0.5);
        if (re.chalShare > 30) m -= (re.chalShare - 30) * 0.06;
        int held = g.term + g.prior;
        if (!re.running && held >= 2) m -= 2 + 1.2 * (held - 2);                // time for a change
        for (War x : g.world.wars) if (x.stage == 2 && x.support < 40) m -= (40 - x.support) * 0.05;
        m -= Math.min(4, g.scandal * 0.08);
        if (g.set.difficulty == 0) m += 1; if (g.set.difficulty == 2) m -= 1;
        m += re.mood;
        // landslides have a ceiling: past a dozen points, every extra point is harder to win
        double a = Math.abs(m);
        if (a > 12) m = (m > 0 ? 1 : -1) * (12 + (a - 12) * 0.6);
        return m;
    }

    static double themeFit(Game g) {
        Reelect re = g.re;
        switch (re.theme) {
            case 0: return (g.approval - 48) * 0.08;
            case 1: return re.opp != null ? (g.pres.a[A_CHA] - re.opp.a[A_CHA]) * 0.02 + (g.approval < 47 ? 0.8 : 0.2) : 0.3;
            default: return (g.presAge() < 60 ? 0.5 : 0) + (g.approval < 45 ? 0.4 : 0.1) + (g.pres.a[A_SPE] - 60) * 0.01;
        }
    }

    static double stateMargin(Game g, int s, double nat) {
        Reelect re = g.re;
        double sign = g.party == DEM ? 1 : -1;
        double m = sign * (leanIn(s, re.year) - backlash(g, s, re.year)) + nat;
        Cand me = re.nominee != null ? re.nominee : g.pres;
        double hk = re.year < 1990 ? 5 : 3;
        if (me.home == s) m += hk;
        if (re.opp != null && re.opp.home == s) m -= hk;
        Cand myVp = re.running ? g.vp : re.nomVp;
        if (myVp != null && myVp.home == s) m += hk * 0.4;
        if (re.oppVp != null && re.oppVp.home == s) m -= hk * 0.4;
        m += Math.min(4, re.effort[s] * 0.9) + g.stateBump[s] * 0.15;
        return m;
    }

    static final String[] SOUTH = {"MS:60", "AL:55", "GA:45", "LA:45", "SC:40", "AR:15", "FL:12", "NC:12", "TN:8", "VA:8", "TX:8"};

    /** The South breaks with the Democrats once a Democratic president has signed civil rights into law (1964 to 1972;
     *  from 1976 the election results the leans are built on carry it). Points off the Democratic margin. */
    static double backlash(Game g, int s, int year) {
        if (year < 1964 || year > 1975) return 0;
        boolean signed = g.era.year >= 1965;
        if (!signed && g.party == DEM) for (Bill b : g.cong.bills) if (b.stage == ST_LAW && b.def().area == PI_RIGHTS && b.cult() < -0.3) { signed = true; break; }
        if (!signed) return 0;
        for (String t : SOUTH) if (t.startsWith(STATES[s].ab + ":")) return Double.parseDouble(t.substring(3));
        return 0;
    }

    static void poll(Game g) {
        Reelect re = g.re;
        double nat = national(g) + g.rng.nextGaussian() * 1.2;
        re.nat = re.nat == 0 ? nat : re.nat * 0.6 + nat * 0.4;
        for (int s = 0; s < NS; s++) re.st[s] = stateMargin(g, s, re.nat);
    }

    /** Electoral votes the polls point to. */
    public static int forecastEv(Game g) {
        int ev = 0;
        for (int s = 0; s < NS; s++) if (votes(s, g.re.year) && g.re.st[s] > 0) ev += STATES[s].ev;
        return ev;
    }

    public static void decide(Game g, Decision d, int k) {
        Reelect re = g.re;
        if (re == null) return;
        if (d.a == 0) {
            if (k == 0) {
                re.running = true; re.stage = 2; re.nominee = g.pres; re.nomVp = g.vp;
                g.log(NK_BIG, ("President " + g.pres.last + " announces a campaign for a " + (g.term == 1 ? "second" : g.term == 2 ? "third" : "fourth") + " term").toUpperCase(), null, 3);
                if (g.approval < 42 && g.rng.nextDouble() < 0.4 + (42 - g.approval) * 0.04) {
                    Random r = g.rng;
                    boolean f = genFemale(r, 0.4);
                    re.challenger = (r.nextBoolean() ? "Senator " : "Governor ") + genFirst(r, f) + " " + genLast(r);
                    re.chal = 38 + (42 - g.approval) * 1.2;
                    g.log(NK_BAD, (re.challenger + " launches a primary challenge to the president").toUpperCase(), "The challenger says the party can't win with " + g.pres.last + " at the top of the ticket.", 3);
                }
            } else {
                re.running = false; re.stage = 3;
                re.nominee = successor(g); re.nomVp = null;
                g.log(NK_BIG, ("President " + g.pres.last + " will not seek another term").toUpperCase(), re.nominee.name() + " is the early favorite for the nomination.", 4.5);
                g.perf += 1;
                Relations.setback(g, 5);                       // a lame duck
                g.cong.mandate = 0;
            }
        } else if (d.a == 1) {
            re.theme = k;
            double fit = themeFit(g);
            re.conv = 2 + (g.pres.a[A_SPE] - 60) / 15.0 + fit * 2 + g.rng.nextGaussian() * 0.8;
            g.log(re.conv > 2.5 ? NK_GOOD : NK_INFO, "The president's acceptance speech " + (re.conv > 3.5 ? "brings the hall to its feet" : re.conv > 2 ? "is well received" : "falls a little flat"), null, 2);
        } else if (d.a == 2) {
            int i = d.b;
            Cand op = re.opp;
            double fit = k == 0 ? (g.approval - 48) * 0.05 : k == 1 ? (op != null ? (55 - op.a[A_INT]) * 0.03 : 0) + 0.2 : k == 2 ? 0.25 + (g.pres.a[A_SPE] - 60) * 0.01 : 0;
            double s = (g.pres.a[A_DEB] - (op != null ? op.a[A_DEB] : 60)) / 25.0 + fit + g.rng.nextGaussian() * (k == 3 ? 1.5 : 0.8);
            re.debate[i] = clamp(s, -3, 3);
            String who = s >= 0 ? "the president" : op.name();
            g.log(s >= 0 ? NK_GOOD : NK_BAD, "Snap polls say " + who + " won the " + (i == 0 ? "first" : i == 1 ? "second" : "final") + " debate" + (k == 3 && Math.abs(s) > 1.5 ? (s > 0 ? "; your line is everywhere" : "; your attempt at a zinger misfires") : ""), null, 2);
        }
    }

    public static int auto(Game g, Decision d) {
        if (d.a == 0) return g.approval >= 36 ? 0 : 1;
        if (d.a == 1) return g.approval >= 50 ? 0 : g.approval < 44 ? 1 : 2;
        return g.approval >= 50 ? 0 : 1;
    }

    // ---- election day ----
    static void election(Game g) {
        Reelect re = g.re;
        if (re.opp == null) pickOpp(g);
        Random r = g.rng;
        double nat = national(g) + r.nextGaussian() * 1.6;
        double[] reg = new double[6];
        for (int i = 0; i < reg.length; i++) reg[i] = r.nextGaussian() * 1.6;
        re.evMe = 0; re.evOpp = 0; re.evTotal = 0;
        double vMe = 0, vOpp = 0;
        for (int s = 0; s < NS; s++) {
            if (!votes(s, re.year)) { re.margin[s] = 0; continue; }
            double m = stateMargin(g, s, nat) + reg[regionOf(s)] + r.nextGaussian() * 2.4;
            re.margin[s] = m;
            int ev = STATES[s].ev;
            re.evTotal += ev;
            if (m > 0) re.evMe += ev; else re.evOpp += ev;
            double w = STATES[s].t24 > 0 ? STATES[s].t24 : 1;
            double share = clamp(50 + m / 2, 5, 95);
            vMe += w * share; vOpp += w * (100 - share);
        }
        re.popMe = vMe / (vMe + vOpp) * 100; re.popOpp = 100 - re.popMe;
        int need = re.evTotal / 2 + 1;
        boolean won = re.evMe >= need || re.evMe * 2 == re.evTotal && Hill.majority(g, HOUSE, g.party);
        re.won = won;
        re.done = true; re.stage = 4;
        congress(g, nat);
        Cand me = re.nominee != null ? re.nominee : g.pres;
        String head = won ? (re.running ? "PRESIDENT " + g.pres.last.toUpperCase() + " WINS RE-ELECTION" : me.name().toUpperCase() + " WINS THE PRESIDENCY") : re.opp.name().toUpperCase() + " IS ELECTED PRESIDENT";
        String body = me.last + " " + re.evMe + ", " + re.opp.last + " " + re.evOpp + " in the Electoral College; the popular vote " + fmt1(re.popMe) + "% to " + fmt1(re.popOpp) + "%.";
        g.log(NK_BIG, head, body, 6);
        g.milestones.add(dateStr(electionDay(g)) + ": " + (won ? (re.running ? "re-elected, " : me.name() + " elected, ") : (re.running ? "defeated, " : me.name() + " defeated, ")) + re.evMe + "-" + re.evOpp);
        if (re.running && !won) g.ending = END_LOST;
        if (re.running && won) { g.lastEvWon = re.evMe; g.lastEvTotal = re.evTotal; }
        if (won && re.running) g.perf += 2;
        g.night = true;
    }

    static int regionOf(int s) {
        String r = STATES[s].region;
        if (r == null) return 0;
        return Math.abs(r.hashCode()) % 6;
    }

    /** The House and the Senate on the same day: coattails, and in older eras plenty of ticket-splitting. */
    static void congress(Game g, double nat) {
        Reelect re = g.re;
        Random r = g.rng;
        double natD = (g.party == DEM ? nat : -nat);
        int y = re.year;
        boolean hB = Hill.majority(g, HOUSE, g.party), sB = Hill.majority(g, SENATE, g.party);
        g.houseD = clampi((int) Math.round(houseBase(y) + houseSlope(y) * natD * 0.8 + r.nextGaussian() * 6), 120, 340);
        int dUp = 0, dWon = 0;
        for (String tok : senateUp(y, g.senateD).split(" ")) {
            String[] p = tok.split(":");
            int s = stateIdx(p[0]);
            if (s < 0) continue;
            boolean dHeld = p[1].equals("D");
            if (dHeld) dUp++;
            double m = leanIn(s, y) + natD * (y >= 1995 ? 0.7 : 0.35) + (dHeld ? 1 : -1) * (y >= 1995 ? 3 : 6) + r.nextGaussian() * 4;
            if (m > 0) dWon++;
        }
        g.senateD = clampi(g.senateD - dUp + dWon, 20, 80);
        re.houseD1 = g.houseD; re.senateD1 = g.senateD;
        Hill.seats(g);
        boolean hA = Hill.majority(g, HOUSE, g.party), sA = Hill.majority(g, SENATE, g.party);
        Leaders.reshuffle(g, hB != hA, sB != sA);
        g.log(NK_INFO, PARTIES[g.party] + " will hold " + g.myHouse() + " House seats and " + g.mySenate() + " Senate seats in the new Congress", null, 2.4);
    }
}
