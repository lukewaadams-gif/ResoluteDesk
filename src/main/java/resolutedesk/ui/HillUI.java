package resolutedesk.ui;

import static resolutedesk.congress.BillData.*;
import static resolutedesk.congress.CongressRules.*;
import static resolutedesk.core.Dates.*;
import static resolutedesk.core.Fmt.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.data.StateData.*;
import static resolutedesk.executive.Posts.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.sim.Schedule.*;
import static resolutedesk.ui.Draw.*;
import static resolutedesk.ui.Fonts.*;
import static resolutedesk.ui.Theme.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import resolutedesk.ResoluteDesk;
import resolutedesk.congress.Bill;
import resolutedesk.congress.BillDef;
import resolutedesk.congress.Bloc;
import resolutedesk.congress.Budget;
import resolutedesk.congress.Congress;
import resolutedesk.congress.Favor;
import resolutedesk.congress.Hill;
import resolutedesk.congress.Legis;
import resolutedesk.congress.Member;
import resolutedesk.congress.Midterms;
import resolutedesk.congress.Relations;
import resolutedesk.executive.Cabinet;
import resolutedesk.model.Game;
import resolutedesk.opinion.Elect;
import resolutedesk.policy.Policies;

/** The Roosevelt Room: Congress, bills, whip counts, the budget and the midterms. */
public final class HillUI {
    public static int tab, chairCh, sel = -1, draftSel = -1;
    public static Bill draft;
    static String msg;
    public static final String[] TABS = {"The Hill", "Bills", "Whip count", "The budget", "Elections", "Favors"};

    static void draw(ResoluteDesk u, Graphics2D g, Game game) {
        Congress c = game.cong;
        u.segmented(g, 48, 116, 640, 30, TABS, tab, i -> tab = i, null);
        double hm = Relations.hoursMax(game), hl = Relations.hoursLeft(game);
        text(g, "Hill time", 712, 136, F_S, C_DIM);
        bar(g, 782, 127, 96, 10, hm > 0 ? hl / hm : 0, C_GOLD);
        text(g, Relations.hours(hl) + " of " + Relations.hours(hm) + " h", 886, 137, F_XSB, C_GOLD);
        u.hit(712, 120, 260, 24, null, "Calls and meetings take your time. You get two hours a week, and each Hill meeting on your calendar adds two and a half.");
        text(g, "Mandate", 990, 136, F_S, C_DIM);
        bar(g, 1050, 127, 70, 10, c.mandate / 15, C_ACCENT);
        text(g, String.valueOf(Math.round(c.mandate)), 1128, 137, F_XSB, C_ACCENT);
        u.hit(990, 120, 160, 24, null, Relations.mandateWord(c.mandate) + ". Your margin in the last election gives your bills a push in Congress. It fades over the first year of a term.");
        if (msg != null) textFit(g, msg, 1166, 137, DW - 1214, F_XS, C_GOLD);
        switch (tab) {
            case 5: favors(u, g, game); break;
            case 1: bills(u, g, game); break;
            case 2: whip(u, g, game); break;
            case 3: budget(u, g, game); break;
            case 4: elections(u, g, game); break;
            default: hill(u, g, game); break;
        }
    }

    // ---- the Hill ----
    static void hill(ResoluteDesk u, Graphics2D g, Game game) {
        Congress c = game.cong;
        u.panel(g, 48, 160, 700, 652, "Congress");
        double y = 196;
        for (int ch = 0; ch < 2; ch++) {
            int total = ch == HOUSE ? 435 : 100;
            int dem = ch == HOUSE ? game.houseD : game.senateD;
            text(g, ch == HOUSE ? "House of Representatives" : "Senate", 66, y, F_H3, Color.white);
            textR(g, dem + " D - " + (total - dem) + " R" + (Hill.majority(game, ch, game.party) ? "   your party's majority" : "   the other party's majority"), 730, y, F_XS, Hill.majority(game, ch, game.party) ? C_GOOD : C_AMBER);
            double bx = 66, bw = 664;
            for (int i = 0; i < c.blocs.size(); i++) {
                Bloc b = c.blocs.get(i);
                if (b.chamber != ch) continue;
                double w = bw * b.seats / (double) total;
                g.setColor(BLOC_COL[i]);
                g.fill(new Rectangle2D.Double(bx, y + 10, Math.max(0, w - 1), 18));
                bx += w;
            }
            g.setColor(Color.white);
            g.fill(new Rectangle2D.Double(66 + bw * (ch == HOUSE ? 218 : 50.5) / total, y + 6, 2, 26));
            y += 46;
            for (int i = 0; i < c.blocs.size(); i++) {
                Bloc b = c.blocs.get(i);
                if (b.chamber != ch) continue;
                g.setColor(BLOC_COL[i]);
                g.fill(new RoundRectangle2D.Double(66, y - 10, 10, 10, 3, 3));
                text(g, b.name, 84, y, F_XS, C_TEXT);
                textR(g, b.seats + " seats", 360, y, F_XS, C_DIM);
                text(g, "relationship", 390, y, F_T, C_FAINT);
                bar(g, 460, y - 8, 200, 8, b.rel / 100, b.rel >= 55 ? C_GOOD : b.rel >= 40 ? C_GOLD : C_BAD);
                textR(g, String.valueOf(Math.round(b.rel)), 730, y, F_XSB, Color.white);
                y += 22;
            }
            y += 24;
        }
        wrap(g, "Hill meetings on your calendar improve your relationships with the leaders and every bloc. Wins, deals and vetoes move them too.", 66, y + 6, 660, F_XS, C_DIM);
        if (game.filibusterGone) text(g, "The legislative filibuster is gone: bills pass the Senate with a simple majority.", 66, 780, F_XS, C_GOLD);
        else if (Hill.majority(game, SENATE, game.party)) {
            double p = Legis.nukeChance(game);
            boolean ok = Relations.hoursLeft(game) >= Legis.NUKE_HOURS;
            u.button(g, 66, 756, 300, 36, "Ask the Senate to end the filibuster", ok ? B_NORM : B_OFF, ok ? () -> msg = Legis.nuke(game) : null,
                    "Takes " + Relations.hours(Legis.NUKE_HOURS) + " hours of calls. About a " + Math.round(p * 100) + "% chance your own senators go along. The other side won't forget.");
            text(g, "About " + Math.round(p * 100) + "% chance; " + Relations.hours(Legis.NUKE_HOURS) + " hours of calls", 380, 779, F_XS, C_DIM);
        }
        // leaders and chairs
        double px = 764, pw = DW - 48 - px;
        u.panel(g, px, 160, pw, 300, "The leaders");
        double lx = px + 18;
        int k = 0;
        for (Member m : c.members) {
            if (m.role != RO_LEADER) continue;
            double cx = lx + (k % 2) * (pw / 2 - 6), cy = 196 + (k / 2) * 128;
            Portrait.draw(g, m.face, cx, cy, 70, 84, u.tsec, 0, true, true);
            text(g, m.name, cx + 82, cy + 18, F_BB, Color.white);
            textFit(g, m.title, cx + 82, cy + 36, pw / 2 - 110, F_XS, partyLight(m.party));
            bar(g, cx + 82, cy + 48, 150, 8, m.rel / 100, m.rel >= 55 ? C_GOOD : m.rel >= 40 ? C_GOLD : C_BAD);
            text(g, Math.round(m.rel) + "", cx + 240, cy + 57, F_XSB, Color.white);
            final Member mm = m;
            boolean canMeet = Relations.hoursLeft(game) >= 2;
            u.button(g, cx + 82, cy + 64, 150, 24, "Meet (2 hours)", canMeet ? B_NORM : B_OFF, canMeet ? () -> msg = Relations.meet(game, mm) : null, "A private meeting. Improves your relationship. Takes two hours of your week.");
            if (Relations.owed(game, m) != null) text(g, "owes you", cx + 240, cy + 81, F_T, C_GOOD);
            k++;
        }
        u.panel(g, px, 470, pw, 342, "Committee chairs");
        u.segmented(g, px + pw - 220, 476, 200, 24, new String[]{"House", "Senate"}, chairCh, i -> chairCh = i, null);
        double yy = 518;
        for (Member m : c.members) {
            if (m.role != RO_CHAIR || m.chamber != chairCh) continue;
            textFit(g, m.committee, px + 18, yy, 270, F_XS, C_TEXT);
            text(g, m.name + " (" + PARTY_SHORT[m.party] + ")", px + 300, yy, F_XS, partyLight(m.party));
            bar(g, px + pw - 140, yy - 8, 100, 7, m.rel / 100, m.rel >= 55 ? C_GOOD : m.rel >= 40 ? C_GOLD : C_BAD);
            textR(g, String.valueOf(Math.round(m.rel)), px + pw - 18, yy, F_XSB, Color.white);
            yy += 23;
        }
    }

    // ---- bills ----
    static void bills(ResoluteDesk u, Graphics2D g, Game game) {
        Congress c = game.cong;
        u.panel(g, 48, 160, 600, 652, "Before Congress");
        double y = 196;
        int shown = 0;
        label(g, "Your bills", 66, y, C_GOLD);
        y += 18;
        for (int i = c.bills.size() - 1; i >= 0; i--) {
            Bill b = c.bills.get(i);
            if (!b.ours) continue;
            if (b.stage == ST_DEAD && game.week - b.stageWeek > 26) continue;
            y = billRow(u, g, game, b, i, y);
            if (++shown >= 6) break;
        }
        if (shown == 0) { text(g, "None yet. Draft one on the right.", 66, y + 14, F_XS, C_DIM); y += 30; }
        y += 10;
        label(g, "Congress's bills", 66, y, C_GOLD);
        y += 18;
        shown = 0;
        for (int i = c.bills.size() - 1; i >= 0; i--) {
            Bill b = c.bills.get(i);
            if (b.ours) continue;
            if (b.stage >= ST_LAW && game.week - b.stageWeek > 16) continue;
            y = billRow(u, g, game, b, i, y);
            if (++shown >= 4 || y > 760) break;
        }
        if (shown == 0) text(g, "Nothing moving right now.", 66, y + 14, F_XS, C_DIM);
        // drafting
        double px = 664, pw = DW - 48 - px;
        u.panel(g, px, 160, pw, 652, "Draft a bill");
        List<BillDef> defs = agenda(game);
        textR(g, "public support", px + 330, 178, F_T, C_DIM);
        double ly = 194;
        double top = ly, h = 600;
        double off = u.beginScroll(g, "drafts", px + 10, top, 330, h);
        int h0 = u.hits.size();
        for (int i = 0; i < defs.size(); i++) {
            BillDef d = defs.get(i);
            final int k = i;
            double ry = top - off + i * 40;
            boolean busy = inPlay(game, d);
            u.card(g, px + 14, ry, 316, 36, draftSel == i, () -> { draftSel = k; draft = Legis.draft(game, defs.get(k)); }, d.desc);
            g.setColor(d.lean == DEM ? C_DEM : d.lean == REP ? C_REP : C_GOLD);
            g.fill(new RoundRectangle2D.Double(px + 20, ry + 10, 4, 16, 3, 3));
            textFit(g, d.name, px + 32, ry + 23, 230, F_S, busy ? C_DIM : Color.white);
            textR(g, busy ? "in play" : Math.round(d.support) + "%", px + 322, ry + 23, F_XS, busy ? C_DIM : C_TEXT);
        }
        u.clipHits(h0, top, top + h);
        u.endScroll(g, "drafts", defs.size() * 40);
        double dx = px + 350, dw = pw - 368;
        if (draft == null || draftSel < 0 || draftSel >= defs.size()) { wrap(g, "Pick a bill. Blue bills are Democratic priorities, red Republican, gold bipartisan.", dx, 200, dw, F_S, C_DIM); return; }
        BillDef d = draft.def();
        text(g, d.name, dx, 206, F_H2, Color.white);
        double yy = 216 + wrap(g, d.desc, dx, 226, dw, F_XS, C_TEXT);
        text(g, "Public support " + Math.round(draft.support()) + "%", dx, yy + 18, F_XSB, draft.support() >= 55 ? C_GOOD : draft.support() < 48 ? C_BAD : C_TEXT);
        double nc = draft.netCost();
        textR(g, nc > 1 ? "adds $" + Math.round(nc) + "B a year to the deficit" : nc < -1 ? "cuts the deficit by $" + Math.round(-nc) + "B a year" : "about deficit-neutral", dx + dw, yy + 18, F_XS, nc > 50 ? C_AMBER : C_DIM);
        yy += 44;
        text(g, "Size", dx, yy + 12, F_S, C_TEXT);
        u.slider(g, dx + 90, yy + 8, dw - 160, (draft.size - 0.5), C_ACCENT, v -> draft.size = 0.5 + v, "Bigger bills do more and cost more; smaller ones are easier to pass.");
        textR(g, Math.round(draft.size * 100) + "%", dx + dw, yy + 12, F_XSB, Color.white);
        yy += 34;
        text(g, "Scope", dx, yy + 12, F_S, C_TEXT);
        u.slider(g, dx + 90, yy + 8, dw - 160, draft.scope, C_ACCENT, v -> draft.scope = v, "Narrow bills draw fewer objections; broad ones go further and lose votes in the middle.");
        textR(g, draft.scope < 0.33 ? "narrow" : draft.scope > 0.66 ? "broad" : "middle", dx + dw, yy + 12, F_XSB, Color.white);
        yy += 34;
        if (d.cost > 0) {
            text(g, "Paid for by", dx, yy + 18, F_S, C_TEXT);
            u.segmented(g, dx + 90, yy, dw - 90, 28, PAYFOR, draft.payFor, i -> draft.payFor = i, new String[]{"Add it to the deficit. Fiscal hawks object.", "Moves the bill left. Popular, but business groups fight it.", "Moves the bill right. Hard for your own progressives (or easy, for a Republican)."});
            yy += 40;
        }
        boolean rc = d.budget && Legis.reconOk(game, draft);
        u.toggle(g, dx, yy, draft.recon && rc, rc ? () -> draft.recon = !draft.recon : null, d.budget ? (rc ? "A simple majority in the Senate, once a fiscal year. The Byrd rule strikes anything that isn't about money." : "Needs your party in control of both chambers, and one use a fiscal year.") : "Not a budget bill: the Byrd rule rules it out.");
        text(g, "Budget reconciliation", dx + 46, yy + 16, F_S, rc ? C_TEXT : C_DIM);
        if (!d.origRevenue) { u.segmented(g, dx + dw - 180, yy, 180, 26, new String[]{"House first", "Senate first"}, draft.first, i -> draft.first = i, null); }
        else text(g, "Revenue bills start in the House", dx + dw - 190, yy + 16, F_T, C_DIM);
        yy += 44;
        if (!rc) draft.recon = false;
        double pH = Legis.chance(game, draft, HOUSE, false), pS = Legis.chance(game, draft, SENATE, false);
        double cH = Legis.chairOk(game, draft, HOUSE), cS = Legis.chairOk(game, draft, SENATE);
        label(g, "The whip's first read", dx, yy, C_GOLD);
        yy += 22;
        double[] hc = Legis.count(game, draft, HOUSE, false), sc = Legis.count(game, draft, SENATE, false);
        text(g, "House: about " + Math.round(hc[0]) + " votes of " + Legis.need(game, draft, HOUSE, false) + " needed", dx, yy, F_XS, C_TEXT);
        textR(g, Math.round(pH * 100) + "%", dx + dw, yy, F_XSB, pH >= 0.6 ? C_GOOD : pH >= 0.3 ? C_GOLD : C_BAD); yy += 20;
        text(g, "Senate: about " + Math.round(sc[0]) + " of " + Legis.need(game, draft, SENATE, false) + (draft.recon || game.filibusterGone ? "" : " (to end a filibuster)"), dx, yy, F_XS, C_TEXT);
        textR(g, Math.round(pS * 100) + "%", dx + dw, yy, F_XSB, pS >= 0.6 ? C_GOOD : pS >= 0.3 ? C_GOLD : C_BAD); yy += 20;
        text(g, "Committee chairs let it out", dx, yy, F_XS, C_TEXT);
        textR(g, Math.round(cH * 100) + "% / " + Math.round(cS * 100) + "%", dx + dw, yy, F_XSB, Math.min(cH, cS) >= 0.6 ? C_GOOD : C_GOLD);
        yy += 28;
        int active = 0;
        for (Bill b : game.cong.bills) if (b.ours && b.stage > ST_DRAFT && b.stage < ST_LAW) active++;
        boolean can = !inPlay(game, d) && active < 3;
        u.button(g, dx, Math.min(yy, 760), dw, 40, inPlay(game, d) ? "Already before Congress" : active >= 3 ? "Congress can only handle three of your bills at once" : "Send to Congress", can ? B_GOLD : B_OFF, can ? () -> {
            if (d.origRevenue) draft.first = HOUSE;
            Legis.introduce(game, draft);
            sel = game.cong.bills.indexOf(draft);
            draft = null; draftSel = -1; tab = 2; msg = "Sent to Congress.";
        } : null);
    }

    public static boolean inPlay(Game g, BillDef d) { for (Bill b : g.cong.bills) if (b.defId.equals(d.id) && b.stage > ST_DRAFT && b.stage < ST_LAW && b.stage != ST_DEAD) return true; for (Bill b : g.cong.bills) if (b.defId.equals(d.id) && b.stage == ST_LAW) return true; return false; }

    public static List<BillDef> agenda(Game g) {
        List<BillDef> l = new ArrayList<BillDef>();
        for (BillDef d : BILLS) if (d.lean == g.party && billOk(g, d)) l.add(d);
        for (BillDef d : BILLS) if (d.lean < 0 && billOk(g, d)) l.add(d);
        for (BillDef d : BILLS) if (d.lean >= 0 && d.lean != g.party && billOk(g, d)) l.add(d);
        return l;
    }

    static double billRow(ResoluteDesk u, Graphics2D g, Game game, Bill b, int idx, double y) {
        final int k = idx;
        u.card(g, 62, y, 572, 54, sel == idx, () -> { sel = k; tab = 2; }, "Open the whip count");
        text(g, b.name, 76, y + 22, F_BB, Color.white);
        Color sc = b.stage == ST_LAW ? C_GOOD : b.stage == ST_DEAD ? C_BAD : b.bottled ? C_AMBER : C_ACCENT;
        textR(g, b.stage == ST_COMMITTEE || b.stage == ST_COMMITTEE2 ? STAGE[b.stage].replace("committee", CHAMBER[b.chamberNow()] + " committee") + (b.bottled ? ", stuck" : "") : STAGE[b.stage], 620, y + 22, F_XSB, sc);
        for (int s = 1; s <= 6; s++) {
            g.setColor(b.stage >= s && b.stage != ST_DEAD ? C_GOLD : b.stage == ST_DEAD && s <= 2 ? C_BAD : new Color(255, 255, 255, 30));
            g.fill(new RoundRectangle2D.Double(76 + (s - 1) * 60, y + 34, 54, 6, 3, 3));
        }
        if (b.stage >= ST_COMMITTEE && b.stage <= ST_FLOOR2) {
            double p = Legis.chance(game, b, b.chamberNow(), false);
            textR(g, "chance of the next vote " + Math.round(p * 100) + "%", 620, y + 42, F_T, C_DIM);
        }
        return y + 60;
    }

    // ---- the whip count ----
    static void whip(ResoluteDesk u, Graphics2D g, Game game) {
        Congress c = game.cong;
        Bill b = sel >= 0 && sel < c.bills.size() ? c.bills.get(sel) : null;
        if (b == null) {
            for (int i = c.bills.size() - 1; i >= 0; i--) if (c.bills.get(i).stage < ST_DESK && c.bills.get(i).stage > ST_DRAFT) { sel = i; b = c.bills.get(i); break; }
        }
        if (b == null) { u.panel(g, 48, 160, DW - 96, 652, "Whip count"); wrap(g, "Nothing to count yet. Send a bill to Congress from the Bills tab.", 70, 200, 800, F_S, C_DIM); return; }
        final Bill bb = b;
        int ch = b.stage >= ST_DESK ? b.second() : b.chamberNow();
        u.panel(g, 48, 160, 760, 652, b.name + ": " + STAGE[b.stage]);
        double y = 196;
        for (int cc = 0; cc < 2; cc++) {
            int chamber = cc == 0 ? ch : (ch == HOUSE ? SENATE : HOUSE);
            double[] cnt = Legis.count(game, b, chamber, false);
            int need = Legis.need(game, b, chamber, false);
            text(g, CHAMBER[chamber] + (cc == 0 ? " (next)" : ""), 66, y, F_H3, Color.white);
            textR(g, "about " + Math.round(cnt[0]) + " yes, " + need + " needed  |  " + Math.round(Legis.chance(game, b, chamber, false) * 100) + "% to pass", 790, y, F_XSB, cnt[0] >= need ? C_GOOD : C_AMBER);
            y += 22;
            for (int i = 0; i < c.blocs.size(); i++) {
                Bloc bl = c.blocs.get(i);
                if (bl.chamber != chamber) continue;
                double p = logistic(Legis.blocLogit(game, b, i, false));
                g.setColor(BLOC_COL[i]);
                g.fill(new RoundRectangle2D.Double(66, y - 10, 10, 10, 3, 3));
                text(g, bl.name, 84, y, F_XS, C_TEXT);
                text(g, Legis.lean(p), 330, y, F_XSB, p > 0.6 ? C_GOOD : p > 0.4 ? C_GOLD : C_BAD);
                bar(g, 440, y - 8, 240, 8, p, p > 0.6 ? C_GOOD : p > 0.4 ? C_GOLD : C_BAD);
                textR(g, Math.round(p * bl.seats) + " of " + bl.seats, 790, y, F_XS, C_DIM);
                y += 22;
            }
            y += 18;
        }
        if (b.stage <= ST_FLOOR2 && b.stage >= ST_COMMITTEE) {
            label(g, "Change the bill", 66, y, C_GOLD);
            y += 16;
            text(g, "Size", 66, y + 12, F_XS, C_TEXT);
            u.slider(g, 120, y + 8, 260, bb.size - 0.5, C_ACCENT, v -> bb.size = 0.5 + v, "Shrinking a bill wins over the middle.");
            text(g, "Scope", 420, y + 12, F_XS, C_TEXT);
            u.slider(g, 470, y + 8, 260, bb.scope, C_ACCENT, v -> bb.scope = v, "Narrower is easier to pass.");
            y += 30;
            if (b.def().cost > 0) u.segmented(g, 66, y, 724, 26, PAYFOR, bb.payFor, i -> bb.payFor = i, null);
            y += 36;
        }
        double cx = 66;
        for (int i = c.bills.size() - 1; i >= 0 && cx < 700; i--) {
            Bill o = c.bills.get(i);
            if (o.stage < ST_COMMITTEE || o.stage > ST_FLOOR2) continue;
            final int k = i;
            double w = Math.min(230, strW(g, o.name, F_XSB) + 22);
            u.button(g, cx, 770, w, 28, o.name, o == b ? B_TABON : B_TAB, () -> sel = k, (o.ours ? "Your bill" : "Congress's bill") + ": " + STAGE[o.stage]);
            cx += w + 6;
        }
        if (!b.deals.isEmpty()) {
            StringBuilder s = new StringBuilder("Deals so far: ");
            for (int i = 0; i < b.deals.size(); i++) s.append(i > 0 ? "; " : "").append(b.deals.get(i));
            wrap(g, s.toString(), 66, Math.min(y, 730), 720, F_XS, C_DIM);
        }
        // the people
        double px = 824, pw = DW - 48 - px;
        u.panel(g, px, 160, pw, 652, "Working the votes");
        double yy = 196;
        List<Member> people = new ArrayList<Member>();
        for (Member m : c.members) if (m.role == RO_LEADER && m.chamber == ch) people.add(m);
        Member chair = Hill.chairFor(game, b, ch);
        if (chair != null) people.add(chair);
        for (Member m : c.members) if (m.role == RO_SWING && m.chamber == ch) people.add(m);
        for (Member m : people) {
            double p = logistic(Legis.blocLogit(game, b, m.bloc, false) + (m.rel - 50) / 30);
            Portrait.draw(g, m.face, px + 14, yy - 4, 34, 40, u.tsec, 0, true, true);
            text(g, m.title != null && m.role == RO_SWING ? m.title : m.name, px + 56, yy + 10, F_SB, Color.white);
            textFit(g, m.role == RO_SWING ? "wants " + PRICE[m.price] : m.title, px + 56, yy + 26, 250, F_T, C_DIM);
            text(g, Legis.lean(p), px + 300, yy + 10, F_XSB, p > 0.6 ? C_GOOD : p > 0.4 ? C_GOLD : C_BAD);
            if (b.stage >= ST_COMMITTEE && b.stage <= ST_FLOOR2) {
                double bx = px + 300;
                int[] kinds = {Legis.DL_CALL, Legis.DL_PROJECT, Legis.DL_APPOINT, Legis.DL_ENDORSE, Legis.DL_THREAT, Legis.DL_FAVOR};
                String[] ab = {"Call", "Project", "Appoint", "Endorse", "Threat", "Favor"};
                for (int j = 0; j < kinds.length; j++) {
                    final int kind = kinds[j];
                    final Member mm = m;
                    if (kind == Legis.DL_FAVOR && Relations.owed(game, m) == null) continue;
                    String no = Legis.cannot(game, bb, mm, kind);
                    u.button(g, bx + j * 62, yy + 16, 58, 20, ab[j], no == null ? (kind == Legis.DL_FAVOR ? B_ON : B_NORM) : B_OFF, no == null ? () -> msg = Legis.deal(game, bb, mm, kind) : null,
                            Legis.DEAL[kind] + ". " + (no != null ? no : Legis.DEAL_COST[kind]));
                }
            }
            yy += 46;
            if (yy > 700) break;
        }
        if (b.stage >= ST_COMMITTEE && b.stage <= ST_FLOOR2) {
            u.button(g, px + 14, 760, 220, 36, b.publicPush ? "You've gone public" : "Go public", !b.publicPush ? B_PRIMARY : B_OFF, !b.publicPush ? () -> msg = Legis.deal(game, bb, null, Legis.DL_PUBLIC) : null, "Make the case to the country. Moderates feel the pressure. " + Legis.DEAL_COST[Legis.DL_PUBLIC]);
            int[] pt = Policies.target(b.defId);
            if (pt != null) u.button(g, px + 244, 760, 190, 36, "Real-world impacts", B_NORM, () -> PolicyUI.show(u, pt[0], pt[1]), "What this bill would do, who wins and loses, and how sure anyone can be.");
            if (b.ours) u.button(g, px + pw - 180, 760, 166, 36, "Withdraw the bill", B_GHOST, () -> { bb.stage = ST_DEAD; bb.stageWeek = game.week; if (bb.recon && game.cong.reconFy == Hill.fiscalYear(game)) game.cong.reconFy = -1; msg = "You pulled " + bb.name + "."; }, "Pull it before it fails.");
        }
    }

    // ---- favors, promises and the mandate ----
    static void favors(ResoluteDesk u, Graphics2D g, Game game) {
        Congress c = game.cong;
        u.panel(g, 48, 160, 700, 652, "What you've built");
        double y = 204;
        text(g, Relations.mandateWord(c.mandate), 66, y, F_H3, Color.white);
        bar(g, 66, y + 12, 664, 10, c.mandate / 15, C_ACCENT);
        y += 44;
        wrap(g, "You won the last election by " + (c.margin >= 0 ? fmt1(c.margin) + " points" : "losing the popular vote by " + fmt1(-c.margin) + " points")
                + ". A mandate gives your bills a push with your own party and the moderates of the other. It was " + Math.round(c.mandate0) + " on the first day of the term and fades over the first year.", 66, y, 664, F_XS, C_DIM);
        y += 52;
        double h = Relations.honeymoon(game);
        text(g, h > 0 ? "Congress's honeymoon: about " + Math.round(h * 15) + " more weeks" : "Congress's honeymoon is over", 66, y, F_SB, h > 0 ? C_GOOD : C_DIM);
        y += 18;
        wrap(g, "In the first hundred days or so, even some of the other side give a new president the benefit of the doubt.", 66, y, 664, F_XS, C_DIM);
        y += 40;
        int slots = 0;
        for (int i = 0; i < game.act.length; i++) if (game.act[i] == AC_CONGRESS) slots++;
        text(g, "Your time this week: " + Relations.hours(Relations.hoursLeft(game)) + " of " + Relations.hours(Relations.hoursMax(game)) + " hours left", 66, y, F_SB, C_GOLD);
        y += 18;
        wrap(g, "Two hours of phone time, plus two and a half for each Hill meeting on your calendar (you have " + slots + ")" + (Cabinet.sq(game, S_LEG) >= 0.7 ? ", plus one from your legislative director" : "")
                + ". Calls and deals take an hour each, a private meeting two.", 66, y, 664, F_XS, C_DIM);
        y += 40;
        text(g, "Appointments to offer: " + c.appointments + " of " + Relations.MAX_APPOINTMENTS, 66, y, F_SB, Color.white);
        y += 18;
        wrap(g, "Ambassadorships, boards and commissions. One opens up every four weeks.", 66, y, 664, F_XS, C_DIM);
        y += 34;
        text(g, "Promises kept: " + c.kept + "   broken: " + c.broken, 66, y, F_SB, c.broken > c.kept ? C_BAD : Color.white);
        y += 40;
        label(g, "There is no political capital to spend", 66, y, C_GOLD);
        y += 18;
        wrap(g, "Votes come from goodwill, favors owed and the mandate. Members owe you when you do things for them: say yes when they ask, campaign in their states in an election year, "
                + "sign their committees' bills, and keep your promises. Call a favor in on a hard vote from the whip count. Threats and broken promises cost goodwill that takes months to rebuild.", 66, y, 664, F_XS, C_TEXT);
        // the ledger
        double px = 764, pw = DW - 48 - px;
        u.panel(g, px, 160, pw, 330, "Favors owed to you");
        double yy = 200;
        List<Favor> owed = Relations.open(game, true);
        if (owed.isEmpty()) wrap(g, "No one owes you anything yet.", px + 18, yy, pw - 36, F_XS, C_DIM);
        for (Favor f : owed) {
            if (yy > 470) break;
            Portrait.draw(g, f.member.face, px + 14, yy - 6, 30, 36, u.tsec, 0, true, true);
            text(g, f.member.title != null && f.member.role == RO_SWING ? f.member.title : f.member.name + " (" + PARTY_SHORT[f.member.party] + ")", px + 52, yy + 8, F_SB, Color.white);
            textFit(g, f.why + ", " + dateStr(f.week * 7), px + 52, yy + 24, pw - 80, F_XS, C_DIM);
            yy += 42;
        }
        u.panel(g, px, 500, pw, 312, "Your promises");
        yy = 540;
        List<Favor> prom = Relations.open(game, false);
        if (prom.isEmpty()) wrap(g, "You haven't promised anyone anything.", px + 18, yy, pw - 36, F_XS, C_DIM);
        for (Favor f : prom) {
            if (yy > 790) break;
            int left = f.due - game.week;
            Portrait.draw(g, f.member.face, px + 14, yy - 6, 30, 36, u.tsec, 0, true, true);
            text(g, f.member.name + " (" + PARTY_SHORT[f.member.party] + ")", px + 52, yy + 8, F_SB, Color.white);
            textFit(g, f.why + " by " + dateStr(f.due * 7), px + 52, yy + 24, pw - 200, F_XS, C_DIM);
            textR(g, left <= 0 ? "this week" : left + (left == 1 ? " week left" : " weeks left"), px + pw - 18, yy + 8, F_XSB, left <= 2 ? C_BAD : left <= 5 ? C_AMBER : C_GOOD);
            yy += 42;
        }
    }

    // ---- the budget ----
    static void budget(ResoluteDesk u, Graphics2D g, Game game) {
        Congress c = game.cong;
        u.panel(g, 48, 160, 760, 652, "Your budget request" + (c.budgetSent ? " (sent to Congress)" : ""));
        double y = 200;
        String[] names = {"Defense", "Other agencies", "Taxes (revenue)", "Entitlements"};
        String[] tips = {"Change in defense spending, percent.", "Change in non-defense discretionary spending, percent.", "Revenue change, % of GDP. Positive raises taxes.", "Social Security, Medicare and other mandatory spending, % of GDP."};
        double[] lo = {-15, -25, -1.5, -1}, hi = {15, 25, 1.5, 1};
        double[] val = {c.reqDefense, c.reqNondefense, c.reqTax, c.reqMandatory};
        for (int i = 0; i < 4; i++) {
            final int k = i;
            text(g, names[i], 66, y + 12, F_S, C_TEXT);
            if (!c.budgetSent) u.slider(g, 220, y + 8, 420, (val[i] - lo[i]) / (hi[i] - lo[i]), C_GOLD, v -> setReq(c, k, lo[k] + v * (hi[k] - lo[k])), tips[i], false);
            else bar(g, 220, y + 4, 420, 8, (val[i] - lo[i]) / (hi[i] - lo[i]), C_FAINT);
            textR(g, (i < 2 ? signed1(val[i]) + "%" : signed1(val[i]) + "% of GDP"), 790, y + 12, F_XSB, Color.white);
            y += 38;
        }
        double ch = Budget.deficitChange(c);
        text(g, "Your plan " + (ch > 0.05 ? "adds " + fmt1(ch) : ch < -0.05 ? "cuts " + fmt1(-ch) : "changes about 0") + " % of GDP " + (ch < 0 ? "from" : "to") + " the yearly deficit", 66, y + 10, F_S, ch > 0.3 ? C_AMBER : C_TEXT);
        y += 30;
        double[][] pr = Budget.projection(game);
        List<double[]> ser = new ArrayList<double[]>();
        ser.add(pr[0]);
        Draw.chart(g, 110, y, 660, 160, ser, new Color[]{C_BAD}, new String[]{"Deficit, % of GDP, next ten years"}, 0, Math.max(10, Rooms.maxOf(pr[0]) + 1), "%.0f%%", 11);
        y += 176;
        List<double[]> ser2 = new ArrayList<double[]>();
        ser2.add(pr[1]);
        Draw.chart(g, 110, y, 660, 140, ser2, new Color[]{C_AMBER}, new String[]{"Debt held by the public, % of GDP"}, Math.floor(Rooms.minOf(pr[1]) / 10) * 10, Math.ceil(Rooms.maxOf(pr[1]) / 10) * 10 + 10, "%.0f%%", 11);
        y += 156;
        if (!c.budgetSent) u.button(g, 66, Math.min(y, 760), 300, 38, "Send the budget to Congress", B_GOLD, () -> { Budget.send(game); msg = "Budget sent."; });
        else text(g, "Congress writes the actual spending bills; your request sets the terms of the fight.", 66, Math.min(y + 20, 780), F_XS, C_DIM);
        // appropriations, the shutdown clock and the debt limit
        double px = 824, pw = DW - 48 - px;
        u.panel(g, px, 160, pw, 420, "Fiscal year " + (c.fy + (dateOf(game.day())[1] >= 4 ? 1 : 0)) + " spending bills");
        String[] stn = {"not started", "passed one chamber", "passed both chambers", "law", "on a stopgap"};
        Color[] stc = {C_FAINT, C_GOLD, C_ACCENT, C_GOOD, C_AMBER};
        double yy = 198;
        for (int i = 0; i < 12; i++) {
            textFit(g, APPROPS[i], px + 18, yy, 330, F_XS, C_TEXT);
            textR(g, stn[c.approps[i]], px + pw - 18, yy, F_XSB, stc[c.approps[i]]);
            yy += 23;
        }
        yy += 10;
        if (c.shutdown) text(g, "THE GOVERNMENT IS SHUT DOWN: week " + c.shutdownWeeks, px + 18, yy, F_BB, C_BAD);
        else if (c.crUntil >= 0) text(g, "Running on a stopgap until " + dateStr(c.crUntil * 7), px + 18, yy, F_S, C_AMBER);
        else text(g, "Deadline: September 30", px + 18, yy, F_S, C_DIM);
        u.panel(g, px, 590, pw, 222, "The debt limit");
        double subj = Budget.debtSubject(game);
        text(g, "$" + fmt1(subj) + " trillion owed, limit $" + fmt1(c.debtLimit) + " trillion", px + 18, 630, F_S, C_TEXT);
        bar(g, px + 18, 642, pw - 36, 12, subj / c.debtLimit, subj >= c.debtLimit ? C_BAD : subj > c.debtLimit * 0.97 ? C_AMBER : C_ACCENT);
        if (c.xWeek >= 0) text(g, "The Treasury runs out of room around " + dateStr(c.xWeek * 7) + ".", px + 18, 680, F_SB, C_AMBER);
        else text(g, "Room to borrow for about " + Math.max(0, Math.round((c.debtLimit - subj) / Math.max(0.01, subj * (game.econ.deficit / Math.max(1, game.econ.debt))) * 52)) + " more weeks.", px + 18, 680, F_S, C_DIM);
        wrap(g, "When the debt reaches the limit, the Treasury uses accounting moves for a few months. Then Congress has to raise it, or the government starts missing payments.", px + 18, 704, pw - 36, F_XS, C_DIM);
    }

    static void setReq(Congress c, int k, double v) {
        v = Math.round(v * 10) / 10.0;
        if (k == 0) c.reqDefense = v; else if (k == 1) c.reqNondefense = v; else if (k == 2) c.reqTax = v; else c.reqMandatory = v;
    }

    // ---- the midterms ----
    static void elections(ResoluteDesk u, Graphics2D g, Game game) {
        Congress c = game.cong;
        u.panel(g, 48, 160, 600, 652, "The midterms, " + monthYear(Midterms.electionDay(game)));
        double y = 200;
        if (c.midDone) {
            wrap(g, c.midSummary == null ? "The midterms are over." : c.midSummary, 66, y, 560, F_S, C_TEXT);
            return;
        }
        double mP = Midterms.nationalMargin(game, 0);
        double natD = game.party == DEM ? mP : -mP;
        int house = (int) Math.round(222 + 3.8 * natD);
        int myHouse = game.party == DEM ? house : 435 - house;
        text(g, "If the election were held today", 66, y, F_H3, Color.white);
        y += 30;
        text(g, "Your party " + (mP >= 0 ? "wins" : "loses") + " the national House vote by about " + fmt1(Math.abs(mP)) + " points", 66, y, F_S, mP >= 0 ? C_GOOD : C_BAD);
        y += 24;
        text(g, "That's about " + myHouse + " House seats for " + PARTIES[game.party] + " (" + Midterms.signedInt(myHouse - game.myHouse()) + "); 218 is a majority.", 66, y, F_S, C_TEXT);
        y += 36;
        wrap(g, "The president's party usually loses ground in a midterm. Your approval and the economy matter most; campaigning and the money you raise for candidates can add a point or two. "
                + (Midterms.season(game) ? "It's campaign season: schedule \"Campaign\" stops from your calendar." : "Campaign season opens in late summer of " + (termYear(game) + 1) + "."), 66, y, 560, F_XS, C_DIM);
        y += 70;
        text(g, "Raised for your party: " + partyMoney(game.funds), 66, y, F_S, C_TEXT);
        // senate seats up
        double px = 664, pw = DW - 48 - px;
        u.panel(g, px, 160, pw, 652, "Senate seats up");
        String up = Midterms.seatsUp(game);
        double yy = 198;
        int col = 0;
        if (!up.isEmpty()) for (String tok : up.split(" ")) {
            String[] p = tok.split(":");
            int s = stateIdx(p[0]);
            if (s < 0) continue;
            boolean dHeld = p[1].equals("D");
            double m = Elect.lean[s] + natD + (dHeld ? 3 : -3) + c.campaign[s] * 1.2 * (game.party == DEM ? 1 : -1);
            double x = px + 18 + col * (pw / 3 - 6);
            text(g, STATES[s].name, x, yy, F_XS, C_TEXT);
            text(g, p[1], x + 140, yy, F_XSB, dHeld ? C_DEM_L : C_REP_L);
            textR(g, margin(m), x + pw / 3 - 30, yy, F_XSB, m > 0 ? C_DEM_L : C_REP_L);
            col++;
            if (col == 3) { col = 0; yy += 22; }
        }
        wrap(g, "Holder, then the projected margin. Campaign stops in a state help your party's candidate there.", px + 18, Math.max(yy + 40, 700), pw - 36, F_XS, C_DIM);
    }
}
