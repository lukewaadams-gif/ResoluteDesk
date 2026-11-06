package resolutedesk.ui;

import static resolutedesk.core.Dates.*;
import static resolutedesk.executive.Posts.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.transition.TransitionData.*;
import static resolutedesk.ui.Draw.*;
import static resolutedesk.ui.Fonts.*;
import static resolutedesk.ui.ScreenIds.*;
import static resolutedesk.ui.Sound.*;
import static resolutedesk.ui.Theme.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.List;
import resolutedesk.ResoluteDesk;
import resolutedesk.executive.Person;
import resolutedesk.io.SaveGame;
import resolutedesk.model.Game;
import resolutedesk.sim.Setup;
import resolutedesk.transition.Prospect;
import resolutedesk.transition.Transition;
import resolutedesk.transition.Transitions;

/** The transition headquarters: the jobs to fill, the shortlist for each, and the news of the weeks before the inauguration. */
public final class TransitionUI {
    public static int sel = JOB_STAFF;
    static String msg;

    private TransitionUI() {
    }

    public static void draw(ResoluteDesk u, Graphics2D g) {
        Game game = u.game;
        if (game == null || game.trans == null) { u.go(SC_HOME); return; }
        Transition t = game.trans;
        u.backdrop(g);
        header(u, g, game, t);
        jobs(u, g, game, t);
        shortlist(u, g, game, t);
        news(u, g, game, t);
    }

    static void header(ResoluteDesk u, Graphics2D g, Game game, Transition t) {
        label(g, "The transition", 48, 40, C_GOLD);
        text(g, "President-elect " + game.pres.name(), 48, 84, F_DISPLAY, Color.white);
        int day = Transitions.dayNow(game);
        int left = t.weeks - t.week;
        textFit(g, "Week " + (t.week + 1) + " of " + t.weeks + "   " + dateStr(day) + "   " + (left <= 1 ? "The inauguration is this week" : left + " weeks to the inauguration"),
                48, 108, 700, F_S, C_DIM);
        double hx = 820;
        text(g, "Team hours", hx, 64, F_S, C_DIM);
        bar(g, hx + 84, 55, 120, 10, Transitions.hoursLeft(game) / TR_HOURS, C_GOLD);
        text(g, Math.round(Transitions.hoursLeft(game)) + " of " + Math.round(TR_HOURS), hx + 212, 65, F_XSB, C_GOLD);
        text(g, "FBI checks", hx, 88, F_S, C_DIM);
        bar(g, hx + 84, 79, 120, 10, Transitions.vetsRunning(game) / (double) TR_VET_SLOTS, C_ACCENT);
        text(g, Transitions.vetsRunning(game) + " of " + TR_VET_SLOTS + " running", hx + 212, 89, F_XSB, C_ACCENT);
        boolean last = Transitions.lastWeek(game);
        u.button(g, DW - 48 - 230, 44, 230, 40, last ? "Take the oath  >" : "End the week  >", B_GOLD, () -> endWeek(u), last ? "Noon on Inauguration Day. Jobs still open are filled by your team." : "The team's hours renew; checks finish; the press keeps digging. (Space)");
        u.button(g, DW - 48 - 230, 90, 230, 26, "Let the team finish", B_GHOST, () -> inaugurate(u), "Your transition team fills every job still open, and you take the oath.");
        if (msg != null) textFit(g, msg, 48, 132, DW - 96, F_XS, C_GOLD);
    }

    static void endWeek(ResoluteDesk u) {
        Game game = u.game;
        if (Transitions.lastWeek(game)) { inaugurate(u); return; }
        Transitions.week(game);
        msg = null;
        SaveGame.save(u, SaveGame.AUTO);
        snd(Sfx.CLICK);
    }

    static void inaugurate(ResoluteDesk u) {
        Setup.inaugurate(u.game);
        msg = null;
        Oval.begin(u);
    }

    static void snd(int s) { Sound.snd(s); }

    // ---- the jobs ----
    static void jobs(ResoluteDesk u, Graphics2D g, Game game, Transition t) {
        u.panel(g, 48, 144, 330, 732, "Jobs to fill");
        double y = 172;
        int named = 0, total = 0;
        for (int job = 0; job < NJOB; job++) if (!Transitions.prospects(game, job).isEmpty()) { total++; if (t.pick[job] != null) named++; }
        textR(g, named + " of " + total + " named", 368, 166, F_XS, C_DIM);
        y = group(u, g, game, t, "White House staff", JOB_STAFF, NJOB, y);
        group(u, g, game, t, "The cabinet", 0, JOB_STAFF, y + 4);
    }

    static double group(ResoluteDesk u, Graphics2D g, Game game, Transition t, String title, int from, int to, double y) {
        label(g, title, 66, y + 14, C_FAINT);
        y += 20;
        for (int job = from; job < to; job++) {
            if (Transitions.prospects(game, job).isEmpty()) continue;
            final int k = job;
            Prospect p = t.pick[job];
            u.card(g, 58, y, 310, 21, sel == job, () -> { sel = k; msg = null; }, null);
            textFit(g, from == JOB_STAFF ? STAFF_TITLE[job - JOB_STAFF] : POST_SHORT[job], 66, y + 15, 150, F_XS, Color.white);
            if (p != null) textFit(g, p.person.name, 222, y + 15, 140, F_XSB, C_GOOD);
            else {
                boolean leaked = false;
                for (Prospect q : Transitions.prospects(game, job)) if (q.leaked) leaked = true;
                text(g, leaked ? "Open: a name leaked" : "Open", 222, y + 15, F_XS, leaked ? C_AMBER : C_FAINT);
            }
            y += 22;
        }
        return y;
    }

    // ---- the shortlist ----
    static void shortlist(ResoluteDesk u, Graphics2D g, Game game, Transition t) {
        double px = 392, pw = 820;
        String title = Transitions.jobTitle(sel);
        u.panel(g, px, 144, pw, 732, title);
        String does = sel >= JOB_STAFF ? STAFF_DOES[sel - JOB_STAFF] + " No Senate vote needed." : POST_DOES[sel] + " Confirmed by the Senate.";
        wrap(g, does, px + 18, 184, pw - 36, F_XS, C_DIM);
        List<Prospect> l = Transitions.prospects(game, sel);
        double y = 204, h = Math.min(132, (860 - y) / Math.max(1, l.size()) - 6);
        for (Prospect q : l) { card(u, g, game, t, q, px + 12, y, pw - 24, h); y += h + 6; }
    }

    static String ideology(Game game, Person p) {
        if (p.party != game.party) return "From the " + PARTY[p.party] + " side";
        double a = Math.abs(p.ideo);
        return a > 0.7 ? (p.party == REP ? "Staunch conservative" : "Staunch progressive") : a > 0.4 ? "Mainstream " + PARTY[p.party] : a > 0.18 ? "Moderate" : "Centrist";
    }

    static void card(ResoluteDesk u, Graphics2D g, Game game, Transition t, Prospect q, double x, double y, double w, double h) {
        Person p = q.person;
        boolean chosen = t.pick[q.job] == q, filled = t.pick[q.job] != null;
        u.card(g, x, y, w, h, chosen, null, null);
        Portrait.draw(g, p.face, x + 10, y + 10, 62, 74, u.tsec, 0, true, true);
        double tx = x + 84, left = x + 460;
        // who they are
        text(g, p.name, tx, y + 24, F_BB, Color.white);
        double cx = tx + strW(g, p.name, F_BB) + 10;
        cx += chip(g, PK_NAME[q.kind], cx, y + 12, q.kind == PK_REAL ? C_GOLD : C_ACCENT, false) + 6;
        if (chosen) chip(g, "Your choice", cx, y + 12, C_GOOD, true);
        else if (q.leaked && !filled) chip(g, "Name leaked", cx, y + 12, C_AMBER, false);
        textFit(g, q.profile, tx, y + 42, left - tx, F_XS, C_DIM);
        String ideo = ideology(game, p);
        text(g, ideo, tx, y + 58, F_T, partyLight(p.party));
        String bg;
        Color bc;
        if (q.kind == PK_REAL) { bg = "Cleared by the FBI"; bc = C_GOOD; }
        else if (q.vetDue < 0 && p.baggageKnown && p.baggage != null) { bg = "In the papers: " + p.baggage; bc = C_BAD; }
        else if (q.vetDue < 0) { bg = "No background check yet"; bc = C_FAINT; }
        else if (Transitions.vetting(game, q)) { bg = "FBI check: results " + dateStr(t.startDay + 7 * q.vetDue); bc = C_ACCENT; }
        else if (p.baggageWeight > 0) { bg = "The FBI found " + p.baggage; bc = C_BAD; }
        else { bg = "Cleared by the FBI"; bc = C_GOOD; }
        double bgx = tx + strW(g, ideo, F_T) + 16;
        textFit(g, bg, bgx, y + 58, left - bgx, F_T, bc);
        // what the team knows of them
        stat(g, "Skill", q.skillSeen, q.interviewed, tx, y + 68);
        stat(g, "Loyalty", q.loyaltySeen, q.interviewed, tx + 180, y + 68);
        // strengths, weaknesses and the Senate
        double lx = x + 476, lw = x + w - 12 - lx, ly = y + 22;
        for (String st : q.strengths) { textFit(g, "+ " + st, lx, ly, lw, F_XS, C_GOOD); ly += 15; }
        for (String st : q.weaknesses) { textFit(g, "- " + st, lx, ly, lw, F_XS, C_AMBER); ly += 15; }
        if (Transitions.confirmed(q.job)) {
            String sen;
            Color sc;
            if (q.floated || q.kind == PK_REAL && filled) {
                double[] wv = Transitions.whip(game, q);
                sen = "Senate: about " + Math.round(wv[0]) + " votes, " + Math.round(wv[1] * 100) + "% to be confirmed";
                sc = wv[1] > 0.7 ? C_GOOD : wv[1] > 0.4 ? C_GOLD : C_BAD;
            } else {
                int[] gr = Transitions.guess(game, q);
                sen = "Early whip count: " + gr[0] + " to " + gr[1] + " votes";
                sc = C_DIM;
            }
            textFit(g, sen, lx, y + h - 12, lw, F_T, sc);
        }
        // what the team can do
        int[] acts = {Transitions.TA_INTERVIEW, Transitions.TA_VET, Transitions.TA_FLOAT, Transitions.TA_SENATE, Transitions.TA_ANNOUNCE};
        String[] lab = {"Interview", "Check", "Float", "Senators", "Announce"};
        double bw = (left - tx - 4 * 4) / 5, bx = tx, by = y + h - 30;
        for (int i = 0; i < acts.length; i++) {
            final int a = acts[i];
            String no = Transitions.cannot(game, q, a);
            if (chosen && a == Transitions.TA_ANNOUNCE) no = "Named.";
            u.button(g, bx, by, bw, 22, lab[i], no == null ? (a == Transitions.TA_ANNOUNCE ? B_GOLD : B_NORM) : B_OFF, no == null ? () -> msg = Transitions.act(game, q, a) : null,
                    Transitions.TA_NAME[a] + ". " + (no != null ? no : Transitions.TA_DESC[a]));
            bx += bw + 4;
        }
    }

    static void stat(Graphics2D g, String name, double v, boolean known, double x, double y) {
        text(g, name, x, y + 10, F_T, C_DIM);
        if (known) {
            bar(g, x + 44, y + 3, 70, 6, v / 100, WestWing.good(v));
            text(g, String.valueOf(Math.round(v)), x + 120, y + 10, F_TB, Color.white);
        } else {
            long lo = Math.round(v / 5) * 5 - 5, hi = Math.min(99, lo + 15);
            text(g, "about " + lo + "-" + hi, x + 44, y + 10, F_T, C_FAINT);
        }
    }

    // ---- the news ----
    static void news(ResoluteDesk u, Graphics2D g, Game game, Transition t) {
        double px = 1226, pw = DW - 48 - px;
        u.panel(g, px, 144, pw, 440, "Transition news");
        double y = 186;
        for (int i = t.news.size() - 1; i >= 0 && y < 560; i--) y += wrap(g, t.news.get(i), px + 18, y, pw - 36, F_XS, i == t.news.size() - 1 ? Color.white : C_DIM) + 6;
        u.panel(g, px, 594, pw, 282, "For the team only");
        double s = Transitions.score(game);
        text(g, "Grade so far: " + Transitions.grade(s), px + 18, 644, F_H2, s >= 72 ? C_GOOD : s >= 58 ? C_GOLD : C_BAD);
        y = 664;
        y += wrap(g, "Name your own people, check them, and keep the leaks down. A good transition lengthens your honeymoon and gets the departments off to a running start.", px + 18, y, pw - 36, F_T, C_DIM) + 4;
        text(g, "Leaks " + t.leaks + "   Surprises " + t.surprises, px + 18, y + 8, F_XS, t.leaks + t.surprises > 2 ? C_AMBER : C_DIM);
        y += 28;
        for (int i = t.notes.size() - 1; i >= 0 && y < 860; i--) y += wrap(g, t.notes.get(i), px + 18, y, pw - 36, F_T, C_TEXT) + 4;
    }

    public static void key(ResoluteDesk u, KeyEvent e) {
        int k = e.getKeyCode();
        Game game = u.game;
        if (game == null || game.trans == null) return;
        if (k == KeyEvent.VK_SPACE) endWeek(u);
        if (k == KeyEvent.VK_DOWN || k == KeyEvent.VK_UP) {
            int step = k == KeyEvent.VK_DOWN ? 1 : -1;
            // the list shows the staff first, then the cabinet
            int[] order = new int[NJOB];
            int n = 0;
            for (int j = JOB_STAFF; j < NJOB; j++) order[n++] = j;
            for (int j = 0; j < JOB_STAFF; j++) if (!Transitions.prospects(game, j).isEmpty()) order[n++] = j;
            int at = 0;
            for (int i = 0; i < n; i++) if (order[i] == sel) at = i;
            sel = order[Math.max(0, Math.min(n - 1, at + step))];
        }
    }

    /** Opens the transition for a new game. */
    public static void begin(ResoluteDesk u) {
        sel = JOB_STAFF;
        msg = null;
        u.go(SC_TRANS);
        SaveGame.save(u, SaveGame.AUTO);
    }
}
