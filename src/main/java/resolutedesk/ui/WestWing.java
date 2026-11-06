package resolutedesk.ui;

import static resolutedesk.congress.CongressRules.*;
import static resolutedesk.core.Dates.*;
import static resolutedesk.core.Fmt.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.data.StateData.*;
import static resolutedesk.executive.ActionData.*;
import static resolutedesk.executive.CourtData.*;
import static resolutedesk.executive.Delegation.*;
import static resolutedesk.executive.Posts.*;
import static resolutedesk.executive.Traits.*;
import static resolutedesk.executive.VicePresData.*;
import static resolutedesk.model.Attributes.*;
import static resolutedesk.model.NewsKinds.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.opinion.Demographics.*;
import static resolutedesk.opinion.Issues.*;
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
import resolutedesk.congress.Hill;
import resolutedesk.executive.Action;
import resolutedesk.executive.Actions;
import resolutedesk.executive.Cabinet;
import resolutedesk.executive.Case;
import resolutedesk.executive.Courts;
import resolutedesk.executive.Delegate;
import resolutedesk.executive.Exec;
import resolutedesk.executive.Justice;
import resolutedesk.executive.Person;
import resolutedesk.executive.Probe;
import resolutedesk.executive.Scandal;
import resolutedesk.executive.VicePres;
import resolutedesk.model.Cand;
import resolutedesk.model.Game;

/** The West Wing: the staff, the cabinet, orders and rules, the vice president and the courts. */
public final class WestWing {
    public static int tab, selStaff = -1, selPost = 0, lib = 0, selAct = -1, cArea = PI_ECON, cDir = -1, cBasis = 0;
    static double cScope = 0.8;
    static String msg;
    public static final String[] TABS = {"Staff", "Cabinet", "Vice president", "Orders and rules", "The courts", "Oversight"};

    static void draw(ResoluteDesk u, Graphics2D g, Game game) {
        if (game.ex == null) Cabinet.setup(game);
        if (cDir < 0) cDir = game.party;
        u.segmented(g, 48, 116, 780, 30, TABS, tab, i -> { tab = i; msg = null; }, null);
        if (msg != null) textFit(g, msg, 850, 137, DW - 900, F_XS, C_GOLD);
        switch (tab) {
            case 1: cabinet(u, g, game); break;
            case 2: vp(u, g, game); break;
            case 3: actions(u, g, game); break;
            case 4: courts(u, g, game); break;
            case 5: oversight(u, g, game); break;
            default: staff(u, g, game); break;
        }
    }

    static Color good(double v) { return v >= 65 ? C_GOOD : v >= 45 ? C_GOLD : C_BAD; }

    static void stats(Graphics2D g, Person p, double x, double y, double w) {
        String[] n = {"Skill", "Loyalty", "Morale"};
        double[] v = {p.skill, p.loyalty, p.morale};
        for (int i = 0; i < 3; i++) {
            text(g, n[i], x, y + i * 16, F_T, C_DIM);
            bar(g, x + 50, y - 7 + i * 16, w - 80, 6, v[i] / 100, good(v[i]));
            textR(g, String.valueOf(Math.round(v[i])), x + w, y + i * 16, F_TB, Color.white);
        }
    }

    // ---- staff ----
    static void staff(ResoluteDesk u, Graphics2D g, Game game) {
        Exec x = game.ex;
        u.panel(g, 48, 160, 870, 652, "The White House staff");
        double cw = 278, ch = 200;
        for (int s = 0; s < NSTAFF; s++) {
            double cx = 62 + (s % 3) * (cw + 8), cy = 194 + (s / 3) * (ch + 8);
            Person p = x.staff[s];
            final int k = s;
            u.card(g, cx, cy, cw, ch, selStaff == s, () -> selStaff = k, STAFF_DOES[s]);
            label(g, STAFF_TITLE[s].toUpperCase(), cx + 12, cy + 20, C_GOLD);
            if (p == null) { text(g, "Vacant", cx + 12, cy + 60, F_H2, C_BAD); text(g, "Click to choose someone", cx + 12, cy + 82, F_XS, C_DIM); continue; }
            Portrait.draw(g, p.face, cx + 12, cy + 30, 64, 76, u.tsec, 0, true, true);
            textFit(g, p.name, cx + 86, cy + 46, cw - 96, F_BB, Color.white);
            textFit(g, p.background, cx + 86, cy + 63, cw - 96, F_T, C_DIM);
            chip(g, TRAIT[p.trait], cx + 86, cy + 80, p.trait >= T_LEAKER ? C_AMBER : C_ACCENT, false);
            text(g, Math.max(0, game.week - p.since) + " weeks in the job", cx + 86, cy + 112, F_T, C_FAINT);
            stats(g, p, cx + 12, cy + 126, cw - 24);
            final Person pp = p;
            u.button(g, cx + cw - 82, cy + ch - 28, 70, 20, "Fire", B_GHOST, () -> { Cabinet.fire(game, pp); selStaff = k; msg = pp.name + " is out."; }, "Let them go. Everyone else notices.");
        }
        double px = 932, pw = DW - 48 - px;
        if (selStaff >= 0) {
            u.panel(g, px, 160, pw, 652, "Choose a " + STAFF_TITLE[selStaff].toLowerCase());
            wrap(g, STAFF_DOES[selStaff], px + 18, 200, pw - 36, F_XS, C_TEXT);
            List<Person> o = Cabinet.staffOptions(game, selStaff, x.departures);
            double y = 240;
            for (Person p : o) {
                u.card(g, px + 14, y, pw - 28, 160, false, null, TRAIT_DESC[p.trait]);
                Portrait.draw(g, p.face, px + 26, y + 12, 60, 72, u.tsec, 0, true, true);
                text(g, p.name, px + 96, y + 28, F_BB, Color.white);
                textFit(g, p.background, px + 96, y + 45, pw - 140, F_T, C_DIM);
                chip(g, TRAIT[p.trait], px + 96, y + 62, p.trait >= T_LEAKER ? C_AMBER : C_ACCENT, false);
                stats(g, p, px + 26, y + 104, pw - 200);
                final Person pp = p;
                final int role = selStaff;
                u.button(g, px + pw - 140, y + 112, 112, 30, "Hire", B_GOLD, () -> {
                    Person old = game.ex.staff[role];
                    if (old != null) { Cabinet.remove(game, old); game.ex.departures++; game.log(NK_INFO, old.name + " leaves the White House", null, 0.6); }
                    Cabinet.appoint(game, role, pp); selStaff = -1; msg = pp.name + " starts Monday.";
                });
                y += 170;
            }
            u.button(g, px + 14, 770, 140, 30, "Cancel", B_GHOST, () -> selStaff = -1);
            return;
        }
        u.panel(g, px, 160, pw, 652, "Delegate");
        double y = 196;
        wrap(g, "Hand routine work to your team. How well it's done depends on the person doing it.", px + 18, y, pw - 36, F_XS, C_DIM);
        y += 34;
        for (int d = 0; d < NDG; d++) {
            final int k = d;
            u.toggle(g, px + 18, y, x.deleg[d], () -> { x.deleg[k] = !x.deleg[k]; if (k == DG_SCHEDULE && x.deleg[k]) Delegate.plan(game); }, DG_DESC[d]);
            textFit(g, DG_NAME[d], px + 66, y + 13, pw - 90, F_SB, Color.white);
            y += 18 + wrap(g, DG_DESC[d], px + 66, y + 30, pw - 90, F_T, C_DIM);
            Person who = DG_ROLE[d] >= 0 ? x.staff[DG_ROLE[d]] : x.post[P_OMB];
            text(g, who != null ? who.name + ", skill " + Math.round(who.skill) : "Nobody in the job", px + 66, y + 22, F_T, who != null ? C_FAINT : C_BAD);
            y += 40;
        }
        y += 6;
        label(g, "The mood in the West Wing", px + 18, y, C_GOLD);
        double mor = 0; int n = 0;
        for (Person p : Cabinet.everyone(game)) { mor += p.morale; n++; }
        mor /= Math.max(1, n);
        bar(g, px + 18, y + 12, pw - 90, 8, mor / 100, good(mor));
        textR(g, String.valueOf(Math.round(mor)), px + pw - 18, y + 21, F_SB, Color.white);
        text(g, x.leaks + " leaks, " + x.departures + " departures so far", px + 18, y + 42, F_XS, C_DIM);
    }

    // ---- the cabinet ----
    static void cabinet(ResoluteDesk u, Graphics2D g, Game game) {
        Exec x = game.ex;
        u.panel(g, 48, 160, 960, 652, "The cabinet");
        double y = 196;
        text(g, "Post", 66, y, F_TB, C_FAINT); text(g, "Who", 250, y, F_TB, C_FAINT); text(g, "Skill", 560, y, F_TB, C_FAINT); text(g, "Morale", 660, y, F_TB, C_FAINT);
        text(g, "Readiness", 760, y, F_TB, C_FAINT); textR(g, "Staff", 990, y, F_TB, C_FAINT);
        y += 10;
        int off = 0;
        for (int k = 0; k < NPOST; k++) {
            if (!postOn(k)) { off++; continue; }
            final int kk = k;
            Person p = x.post[k], nom = x.nominee[k];
            u.card(g, 58, y, 940, 26, selPost == k, () -> selPost = kk, POST_NAME[k]);
            text(g, POST_SHORT[k], 66, y + 17, F_SB, Color.white);
            if (p != null) {
                textFit(g, p.name, 250, y + 17, 300, F_S, Color.white);
                bar(g, 560, y + 10, 80, 6, p.skill / 100, good(p.skill));
                bar(g, 660, y + 10, 80, 6, p.morale / 100, good(p.morale));
            } else if (nom != null) {
                textFit(g, nom.name + ": Senate vote " + (x.voteAt[k] <= game.week ? "this week" : "in " + (x.voteAt[k] - game.week) + " wk"), 250, y + 17, 380, F_S, C_GOLD);
                textR(g, Math.round(Cabinet.chance(game, nom) * 100) + "% to pass", 740, y + 17, F_XS, Cabinet.chance(game, nom) > 0.6 ? C_GOOD : C_AMBER);
            } else text(g, "Vacant: an acting official is in charge", 250, y + 17, F_S, C_BAD);
            bar(g, 760, y + 10, 140, 6, x.ready[k] / 100, good(x.ready[k]));
            textR(g, x.wf[k] >= 10 ? Math.round(x.wf[k]) + "k" : fmt1(x.wf[k]) + "k", 990, y + 17, F_XS, C_DIM);
            y += 28;
        }
        double tot = 0; for (int k = 0; k < NPOST; k++) if (postOn(k)) tot += x.wf[k];
        if (off > 0) text(g, off + " of today's posts don't exist yet; their work is done by older departments, and Congress creates them as the years go by.", 66, 784, F_XS, C_FAINT);
        text(g, "Federal civilian workforce: about " + fmt1(tot / 1000) + " million. Schedule a cabinet meeting from your calendar to lift morale and readiness.", 66, 802, F_XS, C_DIM);
        // the selected post
        double px = 1022, pw = DW - 48 - px;
        if (!postOn(selPost)) selPost = 0;
        int k = selPost;
        Person p = x.post[k], nom = x.nominee[k];
        u.panel(g, px, 160, pw, 652, POST_NAME[k]);
        text(g, "Readiness " + Math.round(x.ready[k]) + " of 100", px + 18, 198, F_SB, good(x.ready[k]));
        wrap(g, "Readiness decides whether the department carries out your orders, rules and laws well. It rises with a strong, happy leader and falls with staff cuts and shutdowns.", px + 18, 216, pw - 36, F_T, C_DIM);
        double y2 = 262;
        if (p != null) {
            Portrait.draw(g, p.face, px + 18, y2, 80, 96, u.tsec, 0, true, true);
            text(g, p.name, px + 110, y2 + 20, F_H3, Color.white);
            textFit(g, p.title(), px + 110, y2 + 38, pw - 130, F_XS, C_TEXT);
            textFit(g, p.background, px + 110, y2 + 54, pw - 130, F_T, C_DIM);
            chip(g, TRAIT[p.trait], px + 110, y2 + 70, p.trait >= T_LEAKER ? C_AMBER : C_ACCENT, false);
            if (p.lastVote >= 0) text(g, "Confirmed " + p.voteYes + "-" + p.voteNo, px + 110, y2 + 96, F_T, C_FAINT);
            stats(g, p, px + 18, y2 + 124, pw - 36);
            final Person pp = p;
            u.button(g, px + 18, y2 + 180, 160, 30, "Fire", B_GHOST, () -> { Cabinet.fire(game, pp); msg = pp.name + " is out."; }, "A career official takes over until you nominate someone.");
            return;
        }
        if (nom != null) {
            Portrait.draw(g, nom.face, px + 18, y2, 80, 96, u.tsec, 0, true, true);
            text(g, nom.name, px + 110, y2 + 20, F_H3, Color.white);
            text(g, "Your nominee", px + 110, y2 + 38, F_XS, C_GOLD);
            textFit(g, nom.background, px + 110, y2 + 54, pw - 130, F_T, C_DIM);
            chip(g, TRAIT[nom.trait], px + 110, y2 + 70, nom.trait >= T_LEAKER ? C_AMBER : C_ACCENT, false);
            stats(g, nom, px + 18, y2 + 124, pw - 36);
            double[] c = Cabinet.count(game, nom);
            text(g, "Whip count: about " + Math.round(c[0]) + " votes; 50 plus the vice president confirms.", px + 18, y2 + 190, F_XS, C_TEXT);
            text(g, Math.round(Cabinet.chance(game, nom) * 100) + "% chance", px + 18, y2 + 208, F_SB, Cabinet.chance(game, nom) > 0.6 ? C_GOOD : C_AMBER);
            final int kk = k;
            u.button(g, px + 18, y2 + 226, 200, 30, "Withdraw the nomination", B_GHOST, () -> { Cabinet.withdraw(game, kk); msg = "Nomination withdrawn."; });
            return;
        }
        label(g, "Nominate someone", px + 18, y2, C_GOLD);
        List<Person> o = Cabinet.options(game, k, x.departures);
        double yy = y2 + 14;
        for (Person q : o) {
            u.card(g, px + 14, yy, pw - 28, 116, false, null, TRAIT_DESC[q.trait]);
            Portrait.draw(g, q.face, px + 24, yy + 10, 50, 60, u.tsec, 0, true, true);
            textFit(g, q.name, px + 84, yy + 24, pw - 120, F_BB, Color.white);
            textFit(g, q.background + (q.party != game.party ? " (" + PARTY_SHORT[q.party] + ")" : ""), px + 84, yy + 40, pw - 120, F_T, C_DIM);
            chip(g, TRAIT[q.trait], px + 84, yy + 56, q.trait >= T_LEAKER ? C_AMBER : C_ACCENT, false);
            text(g, "Skill " + Math.round(q.skill) + ", loyalty " + Math.round(q.loyalty), px + 24, yy + 90, F_T, C_TEXT);
            double ch = Cabinet.chance(game, q);
            text(g, Math.round(ch * 100) + "% to be confirmed", px + 24, yy + 106, F_TB, ch > 0.6 ? C_GOOD : C_AMBER);
            final Person qq = q; final int kk = k;
            u.button(g, px + pw - 128, yy + 80, 100, 26, "Nominate", B_GOLD, () -> { Cabinet.nominate(game, kk, qq); msg = qq.name + " goes to the Senate."; });
            yy += 124;
        }
    }

    // ---- the vice president ----
    static void vp(ResoluteDesk u, Graphics2D g, Game game) {
        Exec x = game.ex;
        Cand v = game.vp;
        u.panel(g, 48, 160, 520, 652, "The vice president");
        if (v == null) { text(g, "The office is vacant.", 70, 210, F_H2, C_BAD); return; }
        Portrait.draw(g, v, 70, 196, 150, 180, u.tsec, 0, false, true);
        text(g, v.name(), 236, 220, F_H2, Color.white);
        textFit(g, v.role, 236, 242, 310, F_XS, C_DIM);
        text(g, "Loyalty to you", 236, 280, F_XS, C_TEXT);
        bar(g, 236, 288, 230, 8, x.vpLoyalty / 100, good(x.vpLoyalty));
        textR(g, String.valueOf(Math.round(x.vpLoyalty)), 546, 296, F_SB, Color.white);
        text(g, "Now: " + VM_NAME[x.vpMission] + (x.vpMission == VM_TASK && x.vpArg >= 0 ? " (" + PISSUE[x.vpArg].toLowerCase() + ")" : "") + (x.vpUntil >= 0 ? ", until " + dateStr(x.vpUntil * 7) : ""), 236, 326, F_S, C_GOLD);
        double y = 400;
        String[] sk = {"Charisma", "Establishment ties", "Discipline", "Fundraising"};
        int[] at = {A_CHA, A_EST, A_DIS, A_BIG};
        for (int i = 0; i < 4; i++) { text(g, sk[i], 70, y, F_XS, C_TEXT); bar(g, 220, y - 8, 260, 7, v.a[at[i]] / 100.0, good(v.a[at[i]])); textR(g, String.valueOf(v.a[at[i]]), 546, y, F_XSB, Color.white); y += 22; }
        wrap(g, "A vice president with nothing to do grows restless. Missions use their strengths; a gaffe-prone one can make news on the road. In the Senate, the vice president breaks 50-50 ties.", 70, y + 16, 470, F_XS, C_DIM);
        double px = 584, pw = DW - 48 - px;
        u.panel(g, px, 160, pw, 652, "Missions");
        double yy = 196;
        for (int m = 0; m < NVM; m++) {
            final int mm = m;
            boolean on = x.vpMission == m;
            u.card(g, px + 14, yy, pw - 28, 92, on, null, VM_DESC[m]);
            text(g, VM_NAME[m], px + 30, yy + 28, F_H3, on ? C_GOLD : Color.white);
            wrap(g, VM_DESC[m], px + 30, yy + 48, pw - 240, F_XS, C_TEXT);
            if (VM_WEEKS[m] > 0) text(g, VM_WEEKS[m] + " weeks", px + 30, yy + 80, F_T, C_DIM);
            if (!on) u.button(g, px + pw - 180, yy + 30, 150, 32, m == VM_NONE ? "Bring them home" : "Send", m == VM_NONE ? B_GHOST : B_PRIMARY, () -> {
                if (mm == VM_TASK) {
                    String[] iss = new String[NPI];
                    for (int j = 0; j < NPI; j++) iss[j] = PISSUE[j];
                    u.openPopup(Math.min(u.mx, DW - 300), 160, 280, "A task force on which issue?", iss, j -> { VicePres.start(game, VM_TASK, j); msg = "The vice president takes on " + PISSUE[j].toLowerCase() + "."; });
                } else { VicePres.start(game, mm, -1); msg = VM_NAME[mm] + "."; }
            });
            yy += 100;
        }
    }

    // ---- orders, rules and emergencies ----
    static void actions(ResoluteDesk u, Graphics2D g, Game game) {
        Exec x = game.ex;
        u.panel(g, 48, 160, 470, 652, "The library");
        u.segmented(g, 62, 192, 442, 26, new String[]{"Orders", "Rules", "Emergencies", "Custom"}, lib, i -> { lib = i; selAct = -1; }, null);
        List<Integer> items = new ArrayList<Integer>();
        int kind = lib == 0 ? K_ORDER : lib == 1 ? K_RULE : K_EMERG;
        if (lib < 3) {
            for (int i = 0; i < AD_ID.length; i++) if (AD_KIND[i] == kind && (AD_LEAN[i] == game.party || AD_LEAN[i] < 0)) items.add(i);
            for (int i = 0; i < AD_ID.length; i++) if (AD_KIND[i] == kind && AD_LEAN[i] >= 0 && AD_LEAN[i] != game.party) items.add(i);
        }
        double top = 228, h = 572;
        if (lib < 3) {
            double off = u.beginScroll(g, "lib", 58, top, 450, h);
            int h0 = u.hits.size();
            for (int n = 0; n < items.size(); n++) {
                int i = items.get(n);
                final int ii = i;
                double ry = top - off + n * 38;
                boolean av = Actions.available(game, i);
                u.card(g, 62, ry, 436, 34, selAct == i, () -> selAct = ii, AD_DESC[i]);
                g.setColor(AD_LEAN[i] == DEM ? C_DEM : AD_LEAN[i] == REP ? C_REP : C_GOLD);
                g.fill(new RoundRectangle2D.Double(68, ry + 9, 4, 16, 3, 3));
                textFit(g, AD_NAME[i], 80, ry + 22, 330, F_S, av ? Color.white : C_DIM);
                textR(g, Actions.live(game, i) ? "in force" : AD_NEEDS[i] >= 0 && !x.emergency[AD_NEEDS[i]] ? "needs emergency" : Math.round(AD_SUP[i]) + "%", 490, ry + 22, F_T, av ? C_TEXT : C_DIM);
            }
            u.clipHits(h0, top, top + h);
            u.endScroll(g, "lib", items.size() * 38);
        } else {
            double y = 250;
            text(g, "Write your own order", 66, y, F_H3, Color.white);
            y += 26;
            text(g, "Issue", 66, y + 16, F_S, C_TEXT);
            u.button(g, 160, y, 330, 26, PISSUE[cArea], B_NORM, () -> { String[] iss = new String[NPI]; for (int j = 0; j < NPI; j++) iss[j] = PISSUE[j]; u.openPopup(160, 260, 300, "Which issue?", iss, j -> cArea = j); });
            y += 40;
            text(g, "Direction", 66, y + 16, F_S, C_TEXT);
            u.segmented(g, 160, y, 330, 26, new String[]{"Progressive", "Conservative"}, cDir == DEM ? 0 : 1, i -> cDir = i == 0 ? DEM : REP, null);
            y += 40;
            text(g, "Reach", 66, y + 14, F_S, C_TEXT);
            u.slider(g, 160, y + 10, 270, (cScope - 0.3) / 1.2, C_ACCENT, v -> cScope = 0.3 + v * 1.2, "Broader orders do more, draw more lawsuits and less support.");
            textR(g, cScope < 0.7 ? "narrow" : cScope > 1.1 ? "sweeping" : "middle", 490, y + 14, F_XSB, Color.white);
            y += 40;
            boolean anyEm = false; for (boolean b : x.emergency) anyEm |= b;
            text(g, "Legal basis", 66, y + 16, F_S, C_TEXT);
            u.segmented(g, 160, y, 330, 26, anyEm ? new String[]{"Clear statute", "A stretch", "Emergency"} : new String[]{"Clear statute", "A stretch"}, Math.min(cBasis, anyEm ? 2 : 1), i -> cBasis = i,
                    new String[]{"Small effects, few lawsuits.", "Bigger effects, a good chance a judge blocks it.", "Uses a national emergency you've declared."});
            y += 50;
            double legal = Actions.legal(game, clamp(0.08 + 0.3 * cScope + (cBasis == 1 ? 0.3 : cBasis == 2 ? 0.12 : 0), 0, 0.95));
            text(g, "Chance a court blocks it: about " + Math.round(legal * 100) + "%", 66, y, F_XS, legal > 0.5 ? C_AMBER : C_TEXT);
            y += 30;
            u.button(g, 66, y, 424, 38, "Sign the order", B_GOLD, () -> { Actions.custom(game, cArea, cDir, cScope, cBasis); msg = "Signed."; });
            wrap(g, "A custom order shifts how people see you on the issue and can move a few groups. It won't move the economy much; for that, use the library or Congress.", 66, y + 70, 424, F_XS, C_DIM);
        }
        // detail
        double dx = 532, dw = 520;
        u.panel(g, dx, 160, dw, 652, lib == 3 ? "How it works" : "Details");
        if (lib == 3) {
            wrap(g, "Executive orders take effect the day you sign them, and the next president can undo them with a pen. Rules take months of public comment, but last longer and carry more weight. "
                    + "Emergencies unlock powers such as tariffs and border deployments; Congress can vote to end them, and you can veto.", dx + 18, 200, dw - 36, F_S, C_TEXT);
            wrap(g, "Aggressive actions end up in court. A judge can block one within weeks; you can appeal, rewrite it or let it go. Your counsel's skill and the judges your party has appointed shape the odds.", dx + 18, 320, dw - 36, F_S, C_TEXT);
        } else if (selAct >= 0) {
            int i = selAct;
            text(g, AD_NAME[i], dx + 18, 202, F_H2, Color.white);
            double y = 214 + wrap(g, AD_DESC[i], dx + 18, 226, dw - 36, F_S, C_TEXT);
            text(g, ACT_KIND[AD_KIND[i]] + " on " + PISSUE[AD_AREA[i]].toLowerCase() + (AD_LEAN[i] >= 0 ? ", a " + PARTY[AD_LEAN[i]] + " idea" : ", popular in both parties"), dx + 18, y + 16, F_XS, C_DIM);
            y += 40;
            text(g, "Public support", dx + 18, y, F_S, C_TEXT); textR(g, Math.round(AD_SUP[i]) + "%", dx + dw - 18, y, F_SB, AD_SUP[i] >= 55 ? C_GOOD : AD_SUP[i] < 45 ? C_BAD : C_TEXT); y += 22;
            double lg = Actions.legal(game, AD_LEGAL[i]);
            text(g, "Chance a court blocks it", dx + 18, y, F_S, C_TEXT); textR(g, Math.round(lg * 100) + "%", dx + dw - 18, y, F_SB, lg > 0.5 ? C_AMBER : C_TEXT); y += 22;
            if (AD_KIND[i] == K_RULE) { text(g, "Comment period", dx + 18, y, F_S, C_TEXT); textR(g, "about " + Math.round(14 - 6 * Cabinet.pq(game, P_OMB)) + " weeks", dx + dw - 18, y, F_SB, C_TEXT); y += 22; }
            int post = ep(AREA_POST[AD_AREA[i]]);
            text(g, "Carried out by " + POST_NAME[post], dx + 18, y, F_S, C_TEXT); textR(g, "readiness " + Math.round(x.ready[post]), dx + dw - 18, y, F_SB, good(x.ready[post])); y += 22;
            String fx = effects(AD_FX[i]);
            if (fx.length() > 0) { y += 8; y += wrap(g, "Effects: " + fx, dx + 18, y, dw - 36, F_XS, C_DIM) + 8; }
            if (AD_NEEDS[i] >= 0 && !x.emergency[AD_NEEDS[i]]) wrap(g, "Needs a national emergency over " + EMERG_NAME[AD_NEEDS[i]] + " first.", dx + 18, y + 14, dw - 36, F_XS, C_AMBER);
            boolean av = Actions.available(game, i);
            final int ii = i;
            u.button(g, dx + 18, 760, dw - 36, 38, Actions.live(game, i) ? "Already in force" : !av ? "Not available yet" : AD_KIND[i] == K_RULE ? "Propose the rule" : AD_KIND[i] == K_EMERG ? "Declare the emergency" : "Sign the order",
                    av ? B_GOLD : B_OFF, av ? () -> { Actions.issue(game, ii); msg = AD_NAME[ii] + ": done."; } : null);
        } else wrap(g, "Pick an action from the library. Blue ideas are Democratic, red Republican, gold popular with both.", dx + 18, 200, dw - 36, F_S, C_DIM);
        // in force
        double fx0 = 1066, fw = DW - 48 - fx0;
        u.panel(g, fx0, 160, fw, 652, "What you've done");
        double y = 196;
        int shown = 0;
        for (int n = x.actions.size() - 1; n >= 0 && y < 790; n--) {
            Action a = x.actions.get(n);
            if (a.status == A_REVOKED && game.week - a.statusWeek > 8) continue;
            textFit(g, a.name, fx0 + 16, y, fw - 120, F_XS, Color.white);
            Color sc = a.status == A_LIVE ? C_GOOD : a.status == A_COMMENT ? C_ACCENT : a.status == A_BLOCKED ? C_AMBER : C_BAD;
            text(g, A_STATUS[a.status] + (a.sued && a.status == A_LIVE ? ", in court" : "") + (a.failed ? ", rollout trouble" : ""), fx0 + 16, y + 15, F_T, sc);
            final Action aa = a;
            if (a.status == A_LIVE || a.status == A_COMMENT || a.status == A_BLOCKED) u.button(g, fx0 + fw - 96, y - 11, 80, 22, a.status == A_COMMENT ? "Withdraw" : "Rescind", B_GHOST, () -> { Actions.revoke(game, aa); msg = aa.name + ": rescinded."; });
            y += 36; shown++;
        }
        if (shown == 0) text(g, "Nothing yet.", fx0 + 16, y, F_XS, C_DIM);
    }

    static String effects(String fx) {
        StringBuilder b = new StringBuilder();
        for (String t : fx.split(" ")) {
            if (t.isEmpty()) continue;
            String[] kv = t.split("=");
            double v = Double.parseDouble(kv[1]);
            String s = null;
            if (kv[0].equals("gdp")) s = (v > 0 ? "faster" : "slower") + " growth";
            else if (kv[0].equals("infl")) s = (v > 0 ? "higher" : "lower") + " prices";
            else if (kv[0].equals("tariff")) s = (v > 0 ? "tariffs up " : "tariffs down ") + Math.round(Math.abs(v)) + " points";
            else if (kv[0].equals("oil")) s = (v < 0 ? "cheaper" : "dearer") + " oil";
            else if (kv[0].equals("def")) s = (v > 0 ? "a bigger" : "a smaller") + " deficit";
            else if (kv[0].equals("wf")) s = "a smaller federal workforce";
            else if (kv[0].equals("ready")) s = "weaker agencies";
            else if (kv[0].startsWith("g") && Character.isDigit(kv[0].charAt(1))) { int gi = Integer.parseInt(kv[0].substring(1)); s = (v > 0 ? "popular with " : "unpopular with ") + GROUP[gi].toLowerCase(); }
            else if (kv[0].startsWith("st:")) s = "unpopular in " + STATES[stateIdx(kv[0].substring(3))].name;
            if (s != null) b.append(b.length() > 0 ? "; " : "").append(s);
        }
        return b.toString();
    }

    // ---- the courts ----
    static void courts(ResoluteDesk u, Graphics2D g, Game game) {
        Exec x = game.ex;
        u.panel(g, 48, 160, 640, 652, "The Supreme Court");
        int yr = dateOf(game.day())[0];
        double y = 196;
        for (int i = 0; i < 9; i++) {
            double cx = 62 + (i % 3) * 206, cy = y + (i / 3) * 112;
            if (i < x.court.size()) {
                Justice j = x.court.get(i);
                Portrait.draw(g, j.face, cx, cy, 52, 62, u.tsec, 0, true, true);
                textFit(g, j.name, cx + 60, cy + 18, 140, F_XSB, Color.white);
                text(g, "Age " + (yr - j.born), cx + 60, cy + 34, F_T, C_DIM);
                text(g, "Appointed by a " + PARTY_SHORT[j.party], cx + 60, cy + 50, F_T, partyLight(j.party));
            } else {
                g.setColor(new Color(255, 255, 255, 18));
                g.fill(new RoundRectangle2D.Double(cx, cy, 196, 62, 8, 8));
                text(g, x.nomineeJ != null ? "Nominee pending" : "Vacant", cx + 14, cy + 36, F_SB, x.nomineeJ != null ? C_GOLD : C_BAD);
            }
        }
        y += 340;
        if (x.nomineeJ != null) {
            Justice j = x.nomineeJ;
            text(g, "Your nominee: " + j.name + ", " + (yr - j.born), 66, y, F_H3, Color.white);
            double ch = Courts.scotusChance(game, j);
            text(g, "Senate vote " + (x.scotusVoteAt - game.week <= 0 ? "this week" : "in about " + (x.scotusVoteAt - game.week) + " weeks") + ". About " + Math.round(ch * 100) + "% to be confirmed.", 66, y + 22, F_S, ch > 0.6 ? C_GOOD : C_AMBER);
        } else if (x.court.size() < 9) {
            label(g, "Your shortlist", 66, y, C_GOLD);
            List<Justice> sl = Courts.shortlist(game);
            for (int i = 0; i < sl.size(); i++) {
                Justice j = sl.get(i);
                double cx = 62 + i * 206, cy = y + 14;
                u.card(g, cx, cy, 198, 196, false, null, Courts.SHORTLIST_WHAT[i]);
                Portrait.draw(g, j.face, cx + 10, cy + 10, 48, 58, u.tsec, 0, true, true);
                textFit(g, j.name, cx + 64, cy + 26, 128, F_XSB, Color.white);
                text(g, "Age " + (yr - j.born), cx + 64, cy + 44, F_T, C_DIM);
                wrap(g, Courts.SHORTLIST_WHAT[i], cx + 10, cy + 88, 180, F_T, C_TEXT);
                double ch = Courts.scotusChance(game, j);
                text(g, Math.round(ch * 100) + "% to be confirmed", cx + 10, cy + 150, F_TB, ch > 0.6 ? C_GOOD : C_AMBER);
                final Justice jj = j;
                u.button(g, cx + 10, cy + 160, 178, 26, "Nominate", B_GOLD, () -> { Courts.nominateJustice(game, jj); msg = jj.name + " goes to the Senate."; });
            }
        } else wrap(g, "No vacancy. Justices retire or die on their own schedule; the older ones are likelier to leave, and a justice tends to retire when a president of the same party can pick the successor.", 66, y, 600, F_XS, C_DIM);
        // the lower courts
        double px = 702, pw = 420;
        u.panel(g, px, 160, pw, 652, "The lower courts");
        double yy = 200;
        String[] lv = {"District courts (677 judges)", "Courts of appeals (179 judges)"};
        double[] sh = {x.dDistrict, x.dCircuit};
        for (int i = 0; i < 2; i++) {
            text(g, lv[i], px + 18, yy, F_SB, Color.white);
            g.setColor(C_DEM); g.fill(new Rectangle2D.Double(px + 18, yy + 10, (pw - 36) * sh[i], 14));
            g.setColor(C_REP); g.fill(new Rectangle2D.Double(px + 18 + (pw - 36) * sh[i], yy + 10, (pw - 36) * (1 - sh[i]), 14));
            text(g, Math.round(sh[i] * 100) + "% appointed by Democrats", px + 18, yy + 42, F_XS, C_TEXT);
            yy += 64;
        }
        text(g, "Vacancies: about " + Math.round(x.vacDistrict + x.vacCircuit), px + 18, yy, F_S, C_TEXT);
        text(g, "Judges you've appointed: " + x.judges, px + 18, yy + 20, F_S, C_TEXT);
        yy += 50;
        label(g, "Who you nominate", px + 18, yy, C_GOLD);
        u.segmented(g, px + 18, yy + 10, pw - 36, 28, new String[]{"Ideological", "Mainstream", "Consensus"}, x.judgeStrategy, i -> x.judgeStrategy = i,
                new String[]{"The biggest shift on the bench, but senators from the other party use blue slips to block district nominees in their states.", "Solid picks the way most presidents make them.", "Picks the other party's senators sign off on: more confirmed, a smaller shift."});
        yy += 52;
        wrap(g, "Your nominees go to the Senate in a steady stream. With your party in control, the Senate confirms over a judge a week; without it, a trickle. Plaintiffs file where the judges suit them, so the mix on the bench decides how often your orders get blocked.", px + 18, yy, pw - 36, F_XS, C_DIM);
        // cases
        double cx0 = 1136, cw0 = DW - 48 - cx0;
        u.panel(g, cx0, 160, cw0, 652, "In court");
        double cy = 196;
        int n = 0;
        for (int i = x.cases.size() - 1; i >= 0 && cy < 780; i--) {
            Case c = x.cases.get(i);
            Action a = Courts.act(game, c);
            if (a == null || c.done && game.week - c.decideWeek > 20) continue;
            textFit(g, a.name, cx0 + 16, cy, cw0 - 32, F_XSB, Color.white);
            textFit(g, c.plaintiff + " v. the United States", cx0 + 16, cy + 15, cw0 - 32, F_T, C_DIM);
            String st = c.done ? (c.stage == CS_DONE && c.votesFor + c.votesAgainst > 0 ? "Supreme Court: " + (c.won ? "upheld " : "struck ") + Math.max(c.votesFor, c.votesAgainst) + "-" + Math.min(c.votesFor, c.votesAgainst) : c.won ? "You won" : "Over") : CS_STAGE[c.stage] + (c.blocked ? ", blocked" : "") + ", ruling around " + dateStr(c.decideWeek * 7);
            text(g, st, cx0 + 16, cy + 30, F_T, c.done ? (c.won ? C_GOOD : C_BAD) : c.blocked ? C_AMBER : C_ACCENT);
            cy += 48; n++;
        }
        if (n == 0) text(g, "No lawsuits against you yet.", cx0 + 16, cy, F_XS, C_DIM);
    }

    // ---- oversight ----
    static void oversight(ResoluteDesk u, Graphics2D g, Game game) {
        Exec x = game.ex;
        u.panel(g, 48, 160, 640, 652, "Scandals and stories");
        double y = 196;
        int n = 0;
        for (int i = x.scandals.size() - 1; i >= 0 && y < 780; i--) {
            Scandal s = x.scandals.get(i);
            if (s.over && game.week - s.started > 30) continue;
            textFit(g, s.title, 66, y, 420, F_XSB, s.over ? C_DIM : Color.white);
            bar(g, 500, y - 8, 120, 7, Math.min(1, s.severity / 10), s.severity >= 5 ? C_BAD : s.severity >= 2.5 ? C_AMBER : C_GOLD);
            textR(g, s.over ? "faded" : fmt1(s.severity), 668, y, F_XSB, s.over ? C_DIM : Color.white);
            text(g, (s.stage == 2 ? "Special counsel" : s.stage == 1 ? "Under investigation" : "In the news") + (s.impeachable ? ", could feed impeachment" : "") + ", since " + dateStr(s.started * 7), 66, y + 15, F_T, s.impeachable ? C_AMBER : C_DIM);
            y += 38; n++;
        }
        if (n == 0) text(g, "No scandals. Enjoy it.", 66, y, F_XS, C_DIM);
        double px = 702, pw = 440;
        u.panel(g, px, 160, pw, 652, "Congressional investigations");
        double yy = 196;
        int m = 0;
        for (int i = x.probes.size() - 1; i >= 0 && yy < 780; i--) {
            Probe p = x.probes.get(i);
            if (p.over && game.week - p.until > 26) continue;
            textFit(g, CHAMBER[p.chamber] + ": " + p.topic, px + 16, yy, pw - 32, F_XSB, p.over ? C_DIM : Color.white);
            String[] resp = {"you complied", "you negotiated", "you claimed privilege", "you refused"};
            text(g, p.chair + (p.response >= 0 ? "; " + resp[p.response] : "") + (p.over ? "; over" : "; report due " + dateStr(p.until * 7)), px + 16, yy + 15, F_T, C_DIM);
            yy += 38; m++;
        }
        if (m == 0) wrap(g, Hill.majority(game, HOUSE, game.party) && Hill.majority(game, SENATE, game.party) ? "Your party runs both committees of jurisdiction. That changes if you lose a chamber." : "None open right now.", px + 16, yy, pw - 32, F_XS, C_DIM);
        double ix = 1156, iw = DW - 48 - ix;
        u.panel(g, ix, 160, iw, 652, "Impeachment");
        String[] stage = {"No inquiry", "Inquiry under way", "Impeached: Senate trial", "Acquitted", "Removed"};
        text(g, stage[x.impStage], ix + 18, 200, F_H3, x.impStage == 0 ? C_GOOD : x.impStage == 3 ? C_GOLD : C_BAD);
        text(g, "Pressure", ix + 18, 228, F_XS, C_TEXT);
        bar(g, ix + 18, 236, iw - 36, 10, Math.min(1, x.impPressure / 20), x.impPressure >= 9 ? C_BAD : x.impPressure >= 5 ? C_AMBER : C_GOOD);
        wrap(g, "Impeachable scandals, defied subpoenas and defied courts build pressure. It takes the House (a simple majority) to impeach and two-thirds of the Senate to remove you.", ix + 18, 268, iw - 36, F_T, C_DIM);
        if (x.impResult != null) wrap(g, x.impResult, ix + 18, 340, iw - 36, F_XS, C_TEXT);
        double ly = 420;
        label(g, "The record", ix + 18, ly, C_GOLD);
        String[] rec = {"Pardons and commutations: " + x.pardons, "Contempt votes: " + x.contempt, "Court orders slow-walked: " + x.defiance, "Rollout failures: " + x.failures, "Leaks: " + x.leaks, "Departures: " + x.departures, "Impeachments: " + x.impeachments};
        for (String s : rec) { ly += 20; text(g, s, ix + 18, ly, F_XS, C_TEXT); }
    }
}
