package resolutedesk.congress;

import static resolutedesk.congress.BillData.*;
import static resolutedesk.congress.CongressRules.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.data.StateData.*;
import static resolutedesk.executive.Posts.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.model.Attributes.*;
import static resolutedesk.model.NewsKinds.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.sim.DecisionKinds.*;
import static resolutedesk.ui.Sound.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import resolutedesk.executive.Cabinet;
import resolutedesk.executive.Implement;
import resolutedesk.home.HomeFront;
import resolutedesk.model.Econ;
import resolutedesk.model.Game;
import resolutedesk.policy.Policies;
import resolutedesk.sim.Decision;
import resolutedesk.sim.Decisions;
import resolutedesk.ui.Sfx;

/**
 * A bill goes to committee, to the floor, to the other chamber and to conference, then to your desk.
 * Each bloc's vote turns on ideology, party, its relationship with you, your approval, the deficit and the deals made.
 */
public final class Legis {
    /** Votes needed to pass (or to override). */
    public static int need(Game g, Bill b, int chamber, boolean override) {
        if (chamber == HOUSE) return override ? 290 : 218;
        if (override) return 67;
        if (b.recon || g.filibusterGone) {
            int vpParty = g.party;
            return b.ours || vpParty == partyOf(b) ? 50 : 51;
        }
        int y = yearNow(g);
        // before the 1970s the filibuster was mostly kept for civil rights; other bills passed by majority
        if (y < 1971 && b.cult() > -0.45) return b.ours || g.party == partyOf(b) ? 50 : 51;
        return y < 1975 ? 67 : 60;
    }

    /** The party whose bill it is: yours, or the majority's that wrote it. */
    static int partyOf(Bill b) { return b.ours ? -1 : b.def().lean; }

    static int sponsorParty(Game g, Bill b) {
        if (b.ours) return g.party;
        int l = b.def().lean;
        return l >= 0 ? l : g.party;
    }

    /**
     * A spatial vote: a bloc likes a bill that moves the law (at 0,0) toward it. Party pulls hard, harder still
     * behind the president's own priorities; then the relationship, approval, the cost, public support and deals.
     */
    public static double blocLogit(Game g, Bill b, int bi, boolean override) {
        Bloc bl = g.cong.blocs.get(bi);
        double pol = g.era.polarization;
        double be = b.econ(), bc = b.cult();
        double spatial = 2 * (bl.econ * be + 0.8 * bl.cult * bc) - (be * be + 0.8 * bc * bc);
        double l = 0.3 + 2.2 * spatial;
        int sp = sponsorParty(g, b);
        boolean mine = bl.party == g.party;
        boolean bip = b.def().lean < 0;
        if (bip) l += 0.2;                                                 // bipartisan by design
        if (bl.party == sp) {
            double agree = clamp(0.5 + spatial, 0, 1);
            l += bl.loyalty * (b.ours && mine ? 0.4 + 2.4 * agree : 0.5 + 1.3 * agree);
        } else l -= 0.9 * pol * (bip ? 0.7 : 1);
        if (b.ours && !mine) l -= 0.35 + (Midterms.season(g) || g.tw() > 170 ? 0.3 : 0);   // why hand the president a win?
        if (b.ours || mine) l += (bl.rel - 50) / 35;
        if (b.ours) l += Relations.pull(g, bl);                             // the mandate and the honeymoon
        l += mine ? (g.approval - 45) / 18 : (g.approval - 50) / 30;
        double cost = b.netCost();
        if (cost > 0) l -= bl.hawk * cost / 180;
        boolean moderate = Relations.moderate(bl);
        l += (b.support() - 55) / 40 * (moderate ? 1.2 : 0.4);
        if (b.publicPush) l += moderate ? 0.25 : 0.1;
        if (b.defId.equals("stock_ban")) l -= 1.2;                          // members don't love this one
        if (override) l += mine ? -1.6 * bl.loyalty : 0.3;
        if (!b.ours && mine && b.vetoed) l -= 0.5;
        return 1.6 * l + 1.2 * b.sweet[bi];
    }

    /** {expected yes, standard deviation} in a chamber. */
    public static double[] count(Game g, Bill b, int chamber, boolean override) {
        double yes = 0, var = 0;
        for (int i = 0; i < g.cong.blocs.size(); i++) {
            Bloc bl = g.cong.blocs.get(i);
            if (bl.chamber != chamber || bl.seats <= 0) continue;
            double p = logistic(blocLogit(g, b, i, override));
            yes += bl.seats * p;
            var += bl.seats * p * (1 - p) + Math.pow(bl.seats * 0.06 * (1 - Math.abs(2 * p - 1)), 2);
        }
        return new double[]{yes, Math.sqrt(var) + (chamber == HOUSE ? 2 : 0.8)};
    }

    public static double chance(Game g, Bill b, int chamber, boolean override) {
        double[] c = count(g, b, chamber, override);
        return logistic(1.702 * (c[0] - need(g, b, chamber, override) + 0.5) / c[1]);
    }

    /** Would the committee chair let it out? Chairs from the other party bottle up bills they dislike. */
    public static double chairOk(Game g, Bill b, int chamber) {
        Member ch = Hill.chairFor(g, b, chamber);
        if (ch == null) return 1;
        int bi = ch.bloc;
        double l = blocLogit(g, b, bi, false) + (ch.rel - 50) / 30;
        if (ch.party == sponsorParty(g, b)) l += 1.5;
        if (b.support() > 65) l += 0.8;
        return logistic(l);
    }

    public static boolean reconOk(Game g, Bill b) {
        return b.def().budget && g.cong.reconFy != Hill.fiscalYear(g) && Hill.majority(g, HOUSE, g.party) && Hill.majority(g, SENATE, g.party);
    }

    // ---- your bills ----
    public static Bill draft(Game g, BillDef d) {
        Bill b = new Bill();
        b.defId = d.id; b.name = d.name; b.ours = true;
        b.first = d.origRevenue ? HOUSE : HOUSE;
        b.payFor = d.cost > 60 && g.party == DEM ? 1 : 0;
        return b;
    }

    public static void introduce(Game g, Bill b) {
        b.stage = ST_COMMITTEE;
        b.stageWeek = g.week;
        b.introduced = g.week;
        if (b.recon) g.cong.reconFy = Hill.fiscalYear(g);
        if (!g.cong.bills.contains(b)) g.cong.bills.add(b);
        g.log(NK_INFO, "The White House sends " + b.name + " to Congress", null, 1.0);
    }

    static int stageWeeks(Bill b) {
        switch (b.stage) {
            case ST_COMMITTEE: case ST_COMMITTEE2: return b.recon ? 2 : 3;
            case ST_CONFERENCE: return 2;
            default: return 1;
        }
    }

    public static void week(Game g) {
        Congress c = g.cong;
        c.floorUsed[0] = false; c.floorUsed[1] = false;
        for (Bill b : new ArrayList<Bill>(c.bills)) step(g, b);
        if (g.week % 7 == 3) congressBill(g);
    }

    static void step(Game g, Bill b) {
        if (b.stage >= ST_DESK) return;
        if (g.week - b.stageWeek < stageWeeks(b)) return;
        Random r = g.rng;
        int ch = b.chamberNow();
        switch (b.stage) {
            case ST_COMMITTEE: case ST_COMMITTEE2: {
                double ok = chairOk(g, b, ch);
                if (r.nextDouble() < ok) {
                    Member chair = Hill.chairFor(g, b, ch);
                    if (chair != null && chair.party != sponsorParty(g, b) && b.size > 0.8) { b.size = Math.max(0.6, b.size - 0.15); b.note = "The committee trimmed it."; }
                    b.stage++; b.stageWeek = g.week; b.bottled = false;
                    g.log(NK_INFO, CHAMBER[ch] + " committee approves " + b.name, null, 0.6);
                } else {
                    if (!b.bottled) {
                        Member chair = Hill.chairFor(g, b, ch);
                        g.log(NK_BAD, (chair != null ? chair.name + " (" + PARTY_SHORT[chair.party] + ")" : "The committee chair") + " bottles up " + b.name + " in committee", null, 1.0);
                    }
                    b.bottled = true;
                    b.stageWeek = g.week;
                    if (g.week - b.introduced > 30) kill(g, b, "dies in committee");
                }
                break;
            }
            case ST_FLOOR: case ST_FLOOR2: {
                if (g.cong.floorUsed[ch]) return;
                if (hold(g, b, ch)) return;
                g.cong.floorUsed[ch] = true;
                if (vote(g, b, ch, false)) { b.stage++; b.stageWeek = g.week; }
                else kill(g, b, ch == SENATE && !b.recon && !g.filibusterGone && lastYes(b, ch) >= 50 ? "is filibustered" : "fails");
                break;
            }
            case ST_CONFERENCE: {
                b.stage = ST_DESK;
                b.stageWeek = g.week;
                toDesk(g, b);
                break;
            }
            default: break;
        }
    }

    /**
     * Leaders don't call votes they expect to lose. Your own party's leaders keep whipping for a while;
     * the other party's won't bring your bill up unless most of their members want it.
     */
    static boolean hold(Game g, Bill b, int ch) {
        Member lead = Hill.leaderOf(g, ch, true);
        int sp = sponsorParty(g, b);
        int waited = g.week - b.stageWeek;
        double p = chance(g, b, ch, false);
        boolean friendly = lead == null || lead.party == sp;
        if (friendly) {
            if (p >= 0.75 || waited >= 12) { b.held = false; return false; }
            for (int i = 0; i < g.cong.blocs.size(); i++) { Bloc bl = g.cong.blocs.get(i); if (bl.chamber == ch && bl.party == sp) b.sweet[i] += 0.05; }
        } else {
            double maj = 0, seats = 0;
            for (int i = 0; i < g.cong.blocs.size(); i++) { Bloc bl = g.cong.blocs.get(i); if (bl.chamber == ch && bl.party == lead.party) { maj += bl.seats * logistic(blocLogit(g, b, i, false)); seats += bl.seats; } }
            if (maj / Math.max(1, seats) >= 0.5 && p >= 0.4) { b.held = false; return false; }
            if (waited >= 14) { kill(g, b, "never gets a vote: " + lead.name + " won't bring it up"); return true; }
        }
        if (!b.held) g.log(NK_INFO, (friendly ? "Leaders put off the " + CHAMBER[ch] + " vote on " + b.name + " while they hunt for votes" : lead.name + " refuses to schedule a vote on " + b.name), null, b.ours ? 1.0 : 0.5);
        b.held = true;
        return true;
    }

    static int lastYes(Bill b, int ch) { return ch == HOUSE ? b.houseYes : b.senYes; }

    static boolean vote(Game g, Bill b, int ch, boolean override) {
        double[] c = count(g, b, ch, override);
        int total = ch == HOUSE ? 435 : 100;
        int yes = (int) Math.round(clamp(c[0] + g.rng.nextGaussian() * c[1], 0, total));
        int no = total - yes;
        if (ch == HOUSE) { b.houseYes = yes; b.houseNo = no; } else { b.senYes = yes; b.senNo = no; }
        boolean pass = yes >= need(g, b, ch, override);
        String what = override ? "the veto override" : ch == SENATE && !b.recon && !g.filibusterGone ? "cloture on " + b.name : b.name;
        g.log(pass ? NK_GOOD : NK_BAD, CHAMBER[ch] + (pass ? " passes " : " rejects ") + what + ", " + yes + "-" + no, null, b.ours ? 1.6 : 0.9);
        return pass;
    }

    static void kill(Game g, Bill b, String how) {
        b.stage = ST_DEAD;
        b.stageWeek = g.week;
        if (b.recon && g.cong.reconFy == Hill.fiscalYear(g)) g.cong.reconFy = -1;      // the reconciliation slot is free again
        g.log(NK_BAD, b.name + " " + how, null, b.ours ? 1.4 : 0.6);
        if (b.ours) { Relations.setback(g, 1.5); g.issueAdj[b.def().area] -= 0.2; g.perf -= 0.3; }
    }

    public static void toDesk(Game g, Bill b) {
        BillDef d = b.def();
        String who = b.ours ? "your" : "the " + PARTIES[sponsorParty(g, b)] + "'";
        String body = "Congress has passed " + who + " bill: " + d.desc + " House " + b.houseYes + "-" + b.houseNo + ", Senate " + b.senYes + "-" + b.senNo + ". "
                + "About " + Math.round(b.support()) + "% of the public supports it." + (b.netCost() > 0 ? " It adds about $" + Math.round(b.netCost()) + " billion a year to the deficit." : b.netCost() < 0 ? " It cuts the deficit by about $" + Math.round(-b.netCost()) + " billion a year." : "");
        String[] adv;
        boolean mine = b.ours || d.lean == g.party || d.lean < 0;
        if (mine) adv = new String[]{"Legislative director: \"This is the win we fought for. Sign it in the Rose Garden.\"",
                "Chief of staff: \"Only if you want to throw away months of work. Vetoing your own priority would stun everyone.\"",
                "Counsel: \"It becomes law in ten days anyway, but you'd get none of the credit.\""};
        else adv = new String[]{"Chief of staff: \"Signing it takes the issue off the table and looks bipartisan, but your base won't like it.\"",
                "Political director: \"Veto it. Your voters hate this bill, and they can't override you.\"",
                "Counsel: \"Let it become law without your signature: you neither own it nor fight it.\""};
        Decision dd = Decisions.add(g, D_SIGN, b.name + " is on your desk", body, new String[]{"Sign it", "Veto it", "Let it become law without signing"}, adv);
        dd.bill = b;
    }

    /** The president's call on a bill: 0 sign, 1 veto, 2 no signature. */
    public static void decide(Game g, Bill b, int k) {
        if (k == 1) {
            b.vetoed = true;
            g.cong.vetoes++;
            g.log(NK_BIG, "PRESIDENT " + g.pres.last.toUpperCase() + " VETOES " + b.name.toUpperCase(), null, 2.5);
            boolean mine = b.ours || b.def().lean == g.party;
            if (mine) { g.perf -= 1.5; Relations.setback(g, 6); }
            else { g.issueAdj[b.def().area] += 0.1; for (Bloc bl : g.cong.blocs) if (bl.party != g.party) bl.rel = Math.max(0, bl.rel - 4); }
            // an override vote if Congress has the numbers to try
            double ch = chance(g, b, HOUSE, true) * chance(g, b, SENATE, true);
            if (ch > 0.05 || b.houseYes >= 280 && b.senYes >= 64) {
                b.stage = ST_OVERRIDE;
                boolean h = vote(g, b, HOUSE, true), s = h && vote(g, b, SENATE, true);
                if (h && s) { g.cong.overrides++; g.log(NK_BIG, "CONGRESS OVERRIDES THE VETO OF " + b.name.toUpperCase(), null, 2.6); enact(g, b, false); Relations.setback(g, 3); g.perf -= 1; }
                else { b.stage = ST_DEAD; g.log(NK_GOOD, "The veto of " + b.name + " stands", null, 1.2); }
            } else b.stage = ST_DEAD;
            return;
        }
        enact(g, b, k == 0);
    }

    static void enact(Game g, Bill b, boolean signed) {
        BillDef d = b.def();
        b.stage = ST_LAW;
        b.lawWeek = g.week;
        b.strength = b.size * (0.7 + 0.6 * b.scope) * (b.recon && b.scope > 0.6 ? 0.85 : 1);
        if (Implement.roll(g, b.name, d.area, 1)) b.strength *= 0.75;
        g.cong.laws++;
        if (Policies.isPolicyBill(b.defId)) { enactPolicy(g, b, signed); return; }
        if (signed) HomeFront.act(g, b.econ(), b.cult(), 1.0 * Math.min(1.5, b.strength));
        Econ e = g.econ;
        double pctGdp = b.netCost() / (e.nominalGdp * 10);
        e.fiscal += pctGdp;
        e.polGdp += d.gdp * b.strength * 0.3; e.polUnemp += d.unemp * b.strength * 0.5; e.polInfl += d.infl * b.strength * 0.6;
        boolean mine = b.ours || d.lean == g.party || d.lean < 0;
        if (signed) {
            double sup = b.support() - 50;
            g.issueAdj[d.area] = clamp(g.issueAdj[d.area] + sup * 0.02 * Math.min(1.5, b.strength) * (mine ? 1 : 0.4), -1.5, 1.5);
            g.perf += mine ? 0.6 + sup * 0.02 : 0.2;
            if (b.ours) Relations.win(g, 2);
            else { Member ch = Hill.chairFor(g, b, b.first); if (ch != null) Relations.earn(g, ch, "You signed their committee's bill, " + b.name); }
            if (!mine) for (Bloc bl : g.cong.blocs) if (bl.party == g.party) bl.rel = Math.max(0, bl.rel - 3);
            g.log(NK_BIG, "PRESIDENT " + g.pres.last.toUpperCase() + " SIGNS " + b.name.toUpperCase(), d.desc + (b.recon && b.scope > 0.6 ? " The parliamentarian struck several provisions under the Byrd rule." : ""), 3.0);
            snd(Sfx.CHEER);
        } else g.log(NK_INFO, b.name + " becomes law without the president's signature", null, 1.4);
        Policies.law(g, b, signed);
    }

    /** A bill that moves one policy: the policy model applies its effects, on the economy and on voters. */
    static void enactPolicy(Game g, Bill b, boolean signed) {
        Policies.law(g, b, signed);
        if (signed) {
            if (b.ours) Relations.win(g, 2);
            g.log(NK_BIG, "PRESIDENT " + g.pres.last.toUpperCase() + " SIGNS A LAW ON THE " + b.def().name.toUpperCase(), b.def().desc, 2.6);
            snd(Sfx.CHEER);
        } else g.log(NK_INFO, b.name + " becomes law without the president's signature", null, 1.4);
    }

    /** The majority in Congress moves bills of its own. */
    static void congressBill(Game g) {
        Random r = g.rng;
        boolean oppHouse = !Hill.majority(g, HOUSE, g.party), oppSen = !Hill.majority(g, SENATE, g.party);
        int active = 0;
        for (Bill b : g.cong.bills) if (!b.ours && b.stage < ST_DESK) active++;
        if (active >= 2) return;
        int sp = oppHouse ? Hill.other(g.party) : g.party;
        List<BillDef> l = new ArrayList<BillDef>();
        for (BillDef d : BILLS) {
            if (d.lean != sp && d.lean >= 0 || !billOk(g, d)) continue;
            boolean used = false;
            for (Bill b : g.cong.bills) if (b.defId.equals(d.id) && (b.stage != ST_DEAD || g.week - b.stageWeek < 52)) used = true;
            if (!used) l.add(d);
        }
        if (l.isEmpty() || (!oppHouse && !oppSen && r.nextDouble() < 0.6)) return;
        BillDef d = l.get(r.nextInt(l.size()));
        Bill b = new Bill();
        b.defId = d.id; b.name = d.name; b.ours = false; b.first = oppHouse || !oppSen ? HOUSE : SENATE;
        b.stage = ST_COMMITTEE; b.stageWeek = g.week; b.introduced = g.week;
        g.cong.bills.add(b);
        g.log(NK_INFO, (b.first == HOUSE ? "House " : "Senate ") + PARTIES[sp] + " move " + d.name, null, 0.7);
    }

    // ---- deals to win votes ----
    public static final int DL_CALL = 0, DL_PROJECT = 1, DL_APPOINT = 2, DL_ENDORSE = 3, DL_THREAT = 4, DL_PUBLIC = 5, DL_FAVOR = 6;
    public static final String[] DEAL = {"Call", "Project back home", "Appointment", "Endorsement", "Threaten", "Go public", "Call in a favor"};
    /** Hours of the president's week each kind of deal takes. */
    public static final double[] DEAL_HOURS = {1, 1, 1, 1, 1, 0, 0};
    /** What each kind of deal costs besides time. */
    public static final String[] DEAL_COST = {"An hour on the phone.", "An hour, a little money, and the deficit hawks' patience.", "An hour and one of your appointments.",
            "An hour, and a promise to campaign in their state before the election.", "An hour, and their goodwill and their bloc's.", "Your stamina and a news cycle. The other side's leaders resent the pressure.",
            "The favor they owe you."};

    /** Why a deal can't be made right now, or null if it can. */
    public static String cannot(Game g, Bill b, Member m, int kind) {
        Congress c = g.cong;
        if (kind == DL_PUBLIC) return b.publicPush ? "You've already gone public on this bill." : null;
        if (m.lastDeal == g.week) return m.name + " has already heard from you this week.";
        if (kind == DL_FAVOR) return Relations.owed(g, m) == null ? m.name + " doesn't owe you a favor." : null;
        if (Relations.hoursLeft(g) + 1e-9 < DEAL_HOURS[kind]) return "No time left for Congress this week. Hill meetings on your calendar add hours.";
        if (kind == DL_APPOINT && c.appointments <= 0) return "You have no appointments left to offer; one opens up every month.";
        if (kind == DL_ENDORSE && Relations.promised(g, m) != null) return "You've already made " + m.name + " a promise.";
        return null;
    }

    public static String deal(Game g, Bill b, Member m, int kind) {
        String no = cannot(g, b, m, kind);
        if (no != null) return no;
        Random r = g.rng;
        Congress c = g.cong;
        Relations.spend(g, DEAL_HOURS[kind]);
        int ch = b.chamberNow();
        if (kind == DL_PUBLIC) {
            b.publicPush = true;
            g.issueSal[b.def().area] = Math.min(2, g.issueSal[b.def().area] + 0.1);
            g.stamina = Math.max(0, g.stamina - 3);
            for (Member o : c.members) if (o.role == RO_LEADER && o.party != g.party && o.chamber == ch) o.rel = Math.max(0, o.rel - 2);
            return "You make the case for " + b.name + " to the country. Moderates feel the pressure.";
        }
        m.lastDeal = g.week;
        double boost;
        String msg;
        switch (kind) {
            case DL_PROJECT:
                boost = m.price == PRICE_PROJECT ? 1.4 : 0.7;
                g.econ.fiscal += 0.01;
                for (Bloc bl : c.blocs) if (bl.chamber == ch && bl.hawk > 0.6) bl.rel = Math.max(0, bl.rel - 1.5);
                msg = "You promise " + m.name + " a project back home.";
                if (r.nextDouble() < 0.15) g.log(NK_BAD, "\"Pork for votes\": reporters dig into the White House's deal with " + m.name, null, 1.0);
                break;
            case DL_APPOINT:
                boost = m.price == PRICE_APPOINT ? 1.5 : 0.8;
                c.appointments--;
                msg = "You give " + m.name + " a say in an appointment.";
                break;
            case DL_ENDORSE:
                boost = m.party == g.party ? (m.price == PRICE_ENDORSE ? 1.5 : 0.9) : 0.2;
                if (m.party == g.party && m.state >= 0) {
                    Relations.promise(g, m, "Campaign for them in " + STATES[m.state].name, m.state, Relations.nextElectionWeek(g));
                    msg = "You promise to campaign for " + m.name + " in " + STATES[m.state].name + " before the election. Keep it, or they'll remember.";
                } else msg = m.name + " is polite, but your endorsement won't help a member of the other party.";
                break;
            case DL_THREAT:
                boost = m.party == g.party ? 0.8 : -0.3;
                m.rel = Math.max(0, m.rel - 15);
                if (m.bloc >= 0 && m.bloc < c.blocs.size()) c.blocs.get(m.bloc).rel = Math.max(0, c.blocs.get(m.bloc).rel - 3);
                msg = m.party == g.party ? "You make it clear you'd back a primary challenger. " + m.name + " is furious, but listening." : "The threat backfires: " + m.name + " goes on TV to complain.";
                if (r.nextDouble() < 0.3) g.log(NK_BAD, m.name + " says the White House threatened them over " + b.name, null, 1.2);
                break;
            case DL_FAVOR:
                Relations.callIn(g, m);
                boost = 1.8;
                msg = "You call in the favor " + m.name + " owes you.";
                break;
            default:
                boost = 0.35 * (g.pres.a[A_CHA] + g.pres.a[A_EST]) / 140;
                m.rel = Math.min(100, m.rel + 4);
                msg = "You call " + m.name + ".";
                break;
        }
        boost *= 0.8 + 0.4 * Cabinet.sq(g, S_LEG);
        if (m.role == RO_SWING || m.role == RO_CHAIR) {
            // a named member moves their bloc a little and votes with you
            b.sweet[m.bloc] += boost * (m.role == RO_CHAIR ? 0.35 : 0.22);
            if (m.role == RO_CHAIR && kind != DL_THREAT) m.rel = Math.min(100, m.rel + 6 * boost);
        } else {
            b.sweet[m.bloc] += boost * 0.3;
            if (kind != DL_THREAT) m.rel = Math.min(100, m.rel + 5 * boost);
        }
        b.deals.add(DEAL[kind] + ": " + m.name);
        return msg;
    }

    /** The nuclear option: a simple majority changes the Senate's rules. Your own moderates usually balk. */
    public static double nukeChance(Game g) {
        if (g.filibusterGone || !Hill.majority(g, SENATE, g.party)) return 0;
        double rel = 0, n = 0;
        for (Bloc b : g.cong.blocs) if (b.chamber == SENATE && b.party == g.party) { rel += b.rel * b.seats; n += b.seats; }
        rel /= Math.max(1, n);
        return clamp(0.06 + 0.06 * (g.mySenate() - 50) + (rel - 55) / 120 + (g.approval - 50) / 100, 0.02, 0.6);
    }

    /** Hours of calls it takes to whip the Senate on the rules. */
    public static final double NUKE_HOURS = 3;

    public static String nuke(Game g) {
        if (!Relations.spend(g, NUKE_HOURS)) return "Whipping the Senate on its rules takes " + Relations.hours(NUKE_HOURS) + " hours of your week. Schedule Hill meetings first.";
        if (g.rng.nextDouble() < nukeChance(g)) {
            g.filibusterGone = true;
            for (Bloc b : g.cong.blocs) if (b.chamber == SENATE && b.party != g.party) b.rel = Math.max(0, b.rel - 15);
            g.log(NK_BIG, "SENATE ENDS THE FILIBUSTER FOR LEGISLATION", "A simple majority can now pass bills. The minority vows to remember.", 4);
            return "The Senate changes its rules: bills now pass with a simple majority.";
        }
        for (Bloc b : g.cong.blocs) if (b.chamber == SENATE && b.party == g.party && Relations.moderate(b)) b.rel = Math.max(0, b.rel - 8);
        g.perf -= 0.5;
        g.log(NK_BAD, "Senate " + PARTIES[g.party] + " lack the votes to end the filibuster", "Several of your own senators say they won't change the rules.", 2.2);
        return "Not enough of your own senators would go along.";
    }

    /** The whip's read of where a bloc stands. */
    public static String lean(double p) { return p > 0.8 ? "Yes" : p > 0.6 ? "Leans yes" : p > 0.4 ? "Undecided" : p > 0.2 ? "Leans no" : "No"; }
}
