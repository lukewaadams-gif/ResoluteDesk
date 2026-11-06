package resolutedesk.sim;

import static resolutedesk.congress.CongressRules.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.executive.ActionData.*;
import static resolutedesk.executive.Delegation.*;
import static resolutedesk.executive.Posts.*;
import static resolutedesk.executive.VicePresData.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.home.EventData.*;
import static resolutedesk.model.NewsKinds.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.sim.DecisionKinds.*;
import static resolutedesk.ui.Sound.*;
import static resolutedesk.ui.Theme.*;
import static resolutedesk.world.Countries.*;
import static resolutedesk.world.CrisisData.*;
import static resolutedesk.world.WorldData.*;

import java.util.ArrayList;
import java.util.List;
import resolutedesk.ResoluteDesk;
import resolutedesk.campaign.Reelection;
import resolutedesk.congress.Bill;
import resolutedesk.congress.BillDef;
import resolutedesk.congress.Budget;
import resolutedesk.congress.Legis;
import resolutedesk.congress.Member;
import resolutedesk.congress.Relations;
import resolutedesk.executive.Actions;
import resolutedesk.executive.Cabinet;
import resolutedesk.executive.Courts;
import resolutedesk.executive.Exec;
import resolutedesk.executive.Person;
import resolutedesk.executive.Trouble;
import resolutedesk.executive.VicePres;
import resolutedesk.history.History;
import resolutedesk.home.HomeFront;
import resolutedesk.model.Game;
import resolutedesk.press.QA;
import resolutedesk.press.Speech;
import resolutedesk.stories.Storylines;
import resolutedesk.ui.HillUI;
import resolutedesk.ui.Modal;
import resolutedesk.ui.Sfx;
import resolutedesk.world.Diplo;
import resolutedesk.world.Summit;
import resolutedesk.world.Wars;
import resolutedesk.world.World;

/** Making decisions: the options of each kind of decision, the advice, what each choice does, and how the hands-off president chooses. */
public final class Decisions {
    public static Decision add(Game g, int kind, String title, String body, String[] opts, String[] advice) {
        Decision d = new Decision();
        d.kind = kind; d.title = title; d.body = body; d.opts = opts; d.advice = advice;
        name(g, d);
        g.inbox.add(d);
        return d;
    }

    /** Puts a name to each fictional adviser ("Chief of staff Dana Reyes:"). Real people keep the plain title: the game never quotes them. */
    static void name(Game g, Decision d) {
        if (d.advice == null || g.ex == null) return;
        d.advisers = new Person[d.advice.length];
        for (int i = 0; i < d.advice.length; i++) {
            String s = d.advice[i];
            int c = s.indexOf(": ");
            if (c < 0) continue;
            String lab = s.substring(0, c);
            for (int j = 0; j < ADV_LABEL.length; j++) {
                if (!lab.equals(ADV_LABEL[j])) continue;
                int pk = ADV_WHO[j] >= 100 ? ep(ADV_WHO[j] - 100) : -1;
                Person p = pk >= 0 ? g.ex.post[pk] : g.ex.staff[ADV_WHO[j]];
                String nl = pk >= 0 && (pk != ADV_WHO[j] - 100 || !POST_TITLE[pk].equals(POST_TITLE0[pk])) ? POST_TITLE[pk] : lab;
                if (p == null) { if (!nl.equals(lab)) d.advice[i] = nl + s.substring(c); continue; }
                d.advisers[i] = p;
                if (!p.real) d.advice[i] = (j == 3 ? "White House counsel" : nl) + " " + p.name + s.substring(c);
                else if (!nl.equals(lab)) d.advice[i] = nl + s.substring(c);
            }
        }
    }

    /** The adviser whose option you took is pleased; the ones you overruled less so. */
    static void heard(Game g, Decision d, int k) {
        if (d.advisers == null) return;
        for (int i = 0; i < d.advisers.length; i++) {
            Person p = d.advisers[i];
            if (p == null) continue;
            p.morale = clamp(p.morale + (i == k ? 2.5 : -1.5), 0, 100);
        }
    }

    public static void info(Game g, String title, String body) { add(g, D_INFO, title, body, new String[]{"OK"}, null); }

    /** Shows the next decision as a modal, if nothing else is on screen. */
    public static void show(ResoluteDesk u, Game g) {
        if (g.inbox.isEmpty() || u.modal != null || u.popup != null) return;
        Decision d = g.inbox.remove(0);
        if (d.kind == D_BUDGET && g.ex != null && g.ex.deleg[DG_BUDGET]) { apply(g, d, 0); u.toast("Your budget director sent your budget to Congress.", C_ACCENT); return; }
        Runnable[] acts = new Runnable[d.opts.length];
        for (int i = 0; i < d.opts.length; i++) { final int k = i; acts[i] = () -> { apply(g, d, k); g.dirty = true; }; }
        Modal m = new Modal();
        m.title = d.title; m.body = d.body; m.buttons = d.opts; m.actions = acts; m.descs = d.advice;
        m.stacked = d.opts.length > 2 || d.advice != null;
        m.col = d.kind == D_INFO ? C_ACCENT : C_GOLD;
        if (d.bill != null) m.w = 720;
        u.modal = m;
        snd(Sfx.OPEN);
    }

    /** What a cautious president would do: used by the test sim and, later, by watch mode. */
    public static int auto(Game g, Decision d) {
        switch (d.kind) {
            case D_SIGN: {
                BillDef b = d.bill.def();
                boolean mine = d.bill.ours || b.lean == g.party || b.lean < 0;
                if (mine) return 0;
                return d.bill.support() >= 60 ? 0 : 1;
            }
            case D_MUSTPASS: return 0;
            case D_CR: return g.rng.nextDouble() < 0.6 ? 0 : 1;
            case D_SHUTDOWN: return g.cong.shutdownWeeks >= 2 || g.cong.blame > 0.5 ? 1 : 0;
            case D_DEBT: return 0;
            case D_COURT: return Courts.act(g, d.caseRef) != null && Courts.act(g, d.caseRef).legal > 0.6 ? 1 : 0;
            case D_PARDON: return d.a == 0 || d.a == 6 ? 0 : d.a == 3 ? 2 : 1;
            case D_SUBPOENA: return 1;
            case D_SCANDAL: { for (int i = 0; i < d.codes.length; i++) if (d.codes[i] == 2 && d.scandalRef.severity >= 4) return i; return 0; }
            case D_LEAK: return 1;
            case D_CRA: case D_EMERG_END: return 0;
            case D_IMPEACH: return 0;
            case D_WORLD: {
                WatchPlan pl = Auto.plan(g);
                String[] o = CR_OPTS[d.a].split(";;");
                int best = 0; double bv = -9;
                for (int i = 0; i < o.length; i++) { String[] t = o[i].split("\\|"); double q = Diplo.p(g, t[2], Double.parseDouble(t[3])); double v = q - (t[2].equals("m") ? pl.forcePenalty() : 0) - (q >= 1 ? pl.surePenalty() : 0); if (v > bv) { bv = v; best = i; } }
                return best;
            }
            case D_HOME: {
                WatchPlan pl = Auto.plan(g);
                String[] o = EV_OPTS[d.a].split(";;");
                int best = 0; double bv = -9;
                for (int i = 0; i < o.length; i++) { String[] t = o[i].split("\\|"); double q = Diplo.p(g, t[2], Double.parseDouble(t[3])); double v = q - (q >= 1 ? pl.surePenalty() : 0); if (v > bv) { bv = v; best = i; } }
                return best;
            }
            case D_HIST: return History.auto(g, d.a);
            case D_CAMPAIGN: return Reelection.auto(g, d);
            case D_ASK: return Relations.auto(g, d);
            case D_WARPOWERS: return Wars.auto(g, d);
            case D_STORY: return Storylines.auto(g, d);
            case D_SPEECH: return d.codes != null ? (g.week % d.codes.length) : d.a == 0 ? 0 : 0;
            case D_SUMMIT: case D_GSUMMIT: case D_DISASTER: case D_QA: return 0;
            default: return 0;
        }
    }

    public static void autoAll(Game g) {
        int guard = 0;
        while (!g.inbox.isEmpty() && guard++ < 20) { Decision d = g.inbox.remove(0); apply(g, d, auto(g, d)); }
    }

    /**
     * Works the votes for a bill that's stuck short of a majority: calls in favors members owe, then spends up to
     * two of the week's Hill hours on the swing votes, each with the deal that member wants.
     */
    public static void autoWhip(Game g) {
        int made = 0;
        for (Bill b : g.cong.bills) {
            if (!b.ours || b.stage < ST_COMMITTEE || b.stage > ST_FLOOR2) continue;
            int ch = b.chamberNow();
            double p = Legis.chance(g, b, ch, false);
            if (p >= 0.6) continue;
            for (Member m : g.cong.members) {
                if (m.chamber != ch || m.role == RO_LEADER) continue;
                if (p < 0.45 && Legis.cannot(g, b, m, Legis.DL_FAVOR) == null) { Legis.deal(g, b, m, Legis.DL_FAVOR); continue; }
                if (made >= 2 || m.role != RO_SWING) continue;
                int kind = m.price == PRICE_PROJECT ? Legis.DL_PROJECT : m.price == PRICE_APPOINT ? Legis.DL_APPOINT : m.price == PRICE_ENDORSE && m.party == g.party ? Legis.DL_ENDORSE : Legis.DL_CALL;
                if (Legis.cannot(g, b, m, kind) != null) kind = Legis.DL_CALL;
                if (Legis.cannot(g, b, m, kind) != null) continue;
                Legis.deal(g, b, m, kind);
                made++;
            }
            if (!b.publicPush && b.support() > 55) Legis.deal(g, b, null, Legis.DL_PUBLIC);
        }
    }

    /** The executive side of a hands-off president: fill jobs, pick justices, and now and then sign an order. */
    public static void autoExec(Game g) {
        Exec x = g.ex;
        x.deleg[DG_PEOPLE] = true;
        Cabinet.fill(g);
        if (x.court.size() < 9 && x.nomineeJ == null) Courts.nominateJustice(g, Courts.shortlist(g).get(1));
        if (g.week % Auto.plan(g).orderWeeks() == 4 % Auto.plan(g).orderWeeks()) {
            int best = -1;
            for (int i = 0; i < AD_ID.length; i++) {
                if (AD_KIND[i] == K_EMERG || !Actions.available(g, i) || AD_LEAN[i] >= 0 && AD_LEAN[i] != g.party) continue;
                if (best < 0 || AD_SUP[i] - AD_LEGAL[i] * 20 > AD_SUP[best] - AD_LEGAL[best] * 20) best = i;
            }
            if (best >= 0) Actions.issue(g, best);
        }
        if (g.week == 30 && g.party == REP && !x.emergency[EM_BORDER]) Actions.issue(g, EM_BORDER);
        if (g.week == 31 && x.emergency[EM_BORDER] && Actions.available(g, actionIdx("eo_troops"))) Actions.issue(g, actionIdx("eo_troops"));
        if (g.week == 12 && Actions.available(g, actionIdx(g.party == REP ? "eo_freeze" : "eo_ai_safety"))) Actions.issue(g, actionIdx(g.party == REP ? "eo_freeze" : "eo_ai_safety"));
        if (g.world != null) {
            World w = g.world;
            int[] friends = {CC_GB, CC_JP, CC_DE, CC_FR, CC_CA, CC_MX, CC_KR, CC_IN};
            int fr = friends[(g.week / 5) % friends.length];
            if (g.week % 5 == 2 && !gone(g, fr) && align(g, fr) != AL_ADVERSARY) Diplo.call(g, fr);
            if (g.week % 7 == 3 && w.modern) Diplo.call(g, g.week % 14 == 3 ? CC_CN : CC_RU);
            if (g.week % 40 == 20 && w.summitCountry < 0 && w.modern) Diplo.proposeSummit(g, w.uaStage == 2 ? CC_RU : CC_CN);
            if (g.week % 40 == 20 && w.summitCountry < 0 && !w.modern && yearNow(g) >= 1955 && !gone(g, CC_RU)) Diplo.proposeSummit(g, CC_RU);
            int inv = new int[]{CC_GB, CC_JP, CC_IN, CC_DE, CC_FR, CC_CA, CC_AU, CC_KR}[(g.week / 13) % 8];
            if (g.week % 13 == 6 && w.visitAt[inv] < 0 && !gone(g, inv) && align(g, inv) != AL_ADVERSARY) Diplo.invite(g, inv);
        }
        if (g.week % 26 == 10 && g.vp != null) VicePres.start(g, g.week % 52 == 10 ? VM_TOUR : VM_TASK, Week.topIssue(g));
        History.autoWar(g);
    }

    /** Sends the president's next agenda bill to Congress when fewer than the plan allows are moving. */
    public static void autoAgenda(Game g) {
        WatchPlan pl = Auto.plan(g);
        int active = 0;
        for (Bill b : g.cong.bills) if (b.ours && b.stage > ST_DRAFT && b.stage < ST_LAW && b.stage != ST_DEAD) active++;
        if (active >= pl.activeBills()) return;
        for (BillDef d : agendaFor(g, pl)) {
            if (d.lean >= 0 && d.lean != g.party) continue;
            if (HillUI.inPlay(g, d)) continue;
            boolean tried = false;
            for (Bill b : g.cong.bills) if (b.ours && b.defId.equals(d.id)) tried = true;
            if (tried) continue;
            Bill b = Legis.draft(g, d);
            b.recon = d.budget && Legis.reconOk(g, b);
            if (pl.p[WatchPlan.P_BIPARTISAN] > 0.75) { b.size = Math.min(b.size, 0.75); b.scope = Math.min(b.scope, 0.35); }   // trimmed to win votes across the aisle
            if (Legis.chance(g, b, HOUSE, false) * Legis.chance(g, b, SENATE, false) < 0.05) { b.size = 0.6; b.scope = 0.2; }
            if (Legis.chance(g, b, HOUSE, false) * Legis.chance(g, b, SENATE, false) < 0.03) continue;
            Legis.introduce(g, b);
            return;
        }
    }

    /**
     * The bills the computer will try, in order: the plan's own list if the player made one; otherwise the agenda,
     * with bipartisan bills first for a plan that wants them, or the party's own first (the usual order).
     */
    static List<BillDef> agendaFor(Game g, WatchPlan pl) {
        List<BillDef> all = HillUI.agenda(g);
        List<BillDef> l = new ArrayList<BillDef>();
        if (!pl.bills.isEmpty()) {
            for (String id : pl.bills) for (BillDef d : all) if (d.id.equals(id)) l.add(d);
            return l;
        }
        if (pl.p[WatchPlan.P_BIPARTISAN] <= 0.75) return all;
        for (BillDef d : all) if (d.lean < 0) l.add(d);
        for (BillDef d : all) if (d.lean >= 0) l.add(d);
        return l;
    }

    public static void apply(Game g, Decision d, int k) {
        heard(g, d, k);
        switch (d.kind) {
            case D_COURT: Courts.courtDecision(g, d.caseRef, k); break;
            case D_WORLD: Diplo.crisisDecision(g, d.a, k); break;
            case D_HIST: History.decide(g, d.a, k); break;
            case D_CAMPAIGN: Reelection.decide(g, d, k); break;
            case D_ASK: Relations.answer(g, d, k); break;
            case D_WARPOWERS: Wars.warPowers(g, d, k); break;
            case D_STORY: Storylines.decide(g, d, k); break;
            case D_HOME: HomeFront.eventDecision(g, d.a, k); break;
            case D_DISASTER: HomeFront.disasterDecision(g, d, k); break;
            case D_SPEECH: Speech.step(g, d.codes != null ? d.codes[k] : k); break;
            case D_QA: QA.answer(g, d, k); break;
            case D_SUMMIT: Summit.round(g, d.codes != null ? d.codes[k] : k); break;
            case D_GSUMMIT: Summit.multiDecision(g, d, k); break;
            case D_PARDON: Trouble.pardonDecision(g, d, k); break;
            case D_SUBPOENA: Trouble.subpoenaDecision(g, d.probeRef, k); break;
            case D_SCANDAL: Trouble.scandalDecision(g, d.scandalRef, d.codes[k]); break;
            case D_LEAK: Trouble.leakDecision(g, d.person, k); break;
            case D_IMPEACH: Trouble.impeachDecision(g, k); break;
            case D_CRA:
                if (k == 1 || g.rng.nextDouble() < 0.05) { Actions.apply(g, d.action, false); d.action.status = A_UNDONE; d.action.statusWeek = g.week; g.log(NK_BAD, "Congress overturns " + d.action.name.toLowerCase(), null, 1.6); }
                else { g.cong.vetoes++; g.log(NK_INFO, "You veto Congress's attempt to overturn " + d.action.name.toLowerCase() + "; the veto stands", null, 1.2); }
                break;
            case D_EMERG_END:
                if (k == 1) Actions.revoke(g, d.action);
                else { g.cong.vetoes++; g.perf -= 0.2; g.log(NK_INFO, "You veto the resolution ending the emergency over " + EMERG_NAME[d.action.def] + "; it stays in force", null, 1.3); }
                break;
            case D_SIGN: Legis.decide(g, d.bill, k); break;
            case D_MUSTPASS: Legis.decide(g, d.bill, k == 0 ? 0 : 1); break;
            case D_CR: Budget.crDecision(g, k); break;
            case D_SHUTDOWN: Budget.shutdownDecision(g, k); break;
            case D_DEBT: Budget.debtDecision(g, k, d.a == 1); break;
            case D_BUDGET: if (k == 0) Budget.autoRequest(g); break;
            default: break;
        }
    }
}
