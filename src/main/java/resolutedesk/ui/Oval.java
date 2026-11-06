package resolutedesk.ui;

import static resolutedesk.congress.CongressRules.*;
import static resolutedesk.core.Dates.*;
import static resolutedesk.core.Fmt.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.model.Tracking.*;
import static resolutedesk.sim.Schedule.*;
import static resolutedesk.ui.Draw.*;
import static resolutedesk.ui.Fonts.*;
import static resolutedesk.ui.RoomIds.*;
import static resolutedesk.ui.ScreenIds.*;
import static resolutedesk.ui.Sound.*;
import static resolutedesk.ui.Theme.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import resolutedesk.ResoluteDesk;
import resolutedesk.campaign.Reelect;
import resolutedesk.campaign.Reelection;
import resolutedesk.congress.Bill;
import resolutedesk.congress.Congress;
import resolutedesk.io.SaveGame;
import resolutedesk.legacy.Legacy;
import resolutedesk.model.Cand;
import resolutedesk.model.Game;
import resolutedesk.sim.Decisions;
import resolutedesk.sim.Week;

/** The Oval Office: the home screen of a presidency, with the desk, the windows, the doors and the top bar. */
public final class Oval {
    static int hot = -1;
    static double tick;

    static void begin(ResoluteDesk u) {
        Game g = u.game;
        u.go(SC_OVAL);
        snd(Sfx.CHEER);
        String vp = g.vp != null ? " Your vice president is " + g.vp.name() + "." : "";
        u.modal("The Oath of Office", "\"I do solemnly swear that I will faithfully execute the Office of President of the United States, and will to the best of my ability, preserve, protect and defend the Constitution of the United States.\"\n\n"
                        + "It is noon on " + dateLong(0) + ". You are " + g.pres.name() + ", the president of the United States." + vp + "\n\n"
                        + "Plan your week at the Resolute Desk, check the country through the windows and the economy in the briefing book, then end the week. Congress is through the Roosevelt Room door, your staff and cabinet in the West Wing, the world at the globe and the press through the door on the left."
                        + (g.tut >= 0 ? " A short tour will show you around; you can skip it at any time." : ""),
                new String[]{"Get to work"}, new Runnable[]{null}, C_GOLD);
        SaveGame.save(u, SaveGame.AUTO);
    }

    public static void update(ResoluteDesk u, double dt) { tick += dt * 60; if (u.game != null) { Watch.update(u, u.game, dt); if (!u.game.watch) Decisions.show(u, u.game); } }

    public static void key(ResoluteDesk u, KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_SPACE) endWeek(u);
        if (k >= KeyEvent.VK_1 && k <= KeyEvent.VK_9) Rooms.open(u, k - KeyEvent.VK_1);
        if (k == KeyEvent.VK_P) u.go(SC_PAPER);
    }

    static void endWeek(ResoluteDesk u) {
        Game g = u.game;
        if (g == null || g.over || u.modal != null) return;
        if (!g.inbox.isEmpty()) { Decisions.show(u, g); return; }
        Week.resolve(g);
        snd(Sfx.TURN);
        SaveGame.save(u, SaveGame.AUTO);
        if (g.removed) {
            g.watch = false;
            u.modal("Removed from Office", (g.ex != null && g.ex.impResult != null ? g.ex.impResult + " " : "") + "Your presidency ends here, on " + dateLong(g.day()) + ". Historians will argue about it for decades.",
                    new String[]{"The verdict of history"}, new Runnable[]{() -> Legacy.begin(u)}, C_BAD);
            return;
        }
        if (g.night) { Night.begin(u); return; }
        if (g.over) {
            g.watch = false;
            u.modal("The End of " + (g.term == 1 ? "the Term" : "Your Presidency"), "At noon on January 20 you hand the office to " + successorName(g) + ". Thank you for your service.",
                    new String[]{"The verdict of history"}, new Runnable[]{() -> Legacy.begin(u)}, C_GOLD);
            return;
        }
        if (g.stamina < 25 && !g.watch) u.toast("You're exhausted. Schedule rest this week.", C_BAD);
        if (!g.watch) u.go(SC_PAPER);
    }

    // ---- the top bar, shared by every room ----
    static void topBar(ResoluteDesk u, Graphics2D g, Game game) {
        g.setColor(new Color(8, 13, 26, 245));
        g.fill(new Rectangle2D.Double(0, 0, DW, 56));
        g.setColor(new Color(242, 193, 78, 60));
        g.draw(new Line2D.Double(0, 56, DW, 56));
        seal(g, 30, 28, 20);
        text(g, "THE WHITE HOUSE", 60, 24, F_TB, C_GOLD);
        text(g, dateLong(game.day()), 60, 44, F_SB, Color.white);
        double x = 320;
        double last = game.hist.size() >= 2 ? game.hist.get(game.hist.size() - 2)[H_APP] : game.approval;
        stat(g, x, "APPROVAL", Math.round(game.approval) + "%", (game.approval >= last ? "+" : "") + fmt1(game.approval - last), game.approval >= 50 ? C_GOOD : game.approval >= 42 ? Color.white : C_BAD);
        x += 135;
        label(g, "STAMINA", x, 22, C_DIM);
        bar(g, x, 30, 96, 8, game.stamina / 100, game.stamina >= 50 ? C_GOOD : game.stamina >= 25 ? C_AMBER : C_BAD);
        textR(g, String.valueOf(Math.round(game.stamina)), x + 126, 40, F_SB, Color.white);
        x += 150;
        label(g, "STRESS", x, 22, C_DIM);
        bar(g, x, 30, 96, 8, game.stress / 100, game.stress < 50 ? C_ACCENT : game.stress < 75 ? C_AMBER : C_BAD);
        textR(g, String.valueOf(Math.round(game.stress)), x + 126, 40, F_SB, Color.white);
        x += 150;
        stat(g, x, "UNEMPLOYMENT", fmt1(game.econ.unemp) + "%", null, Color.white);
        x += 120;
        stat(g, x, "INFLATION", fmt1(game.econ.infl) + "%", null, game.econ.infl > 4 ? C_AMBER : Color.white);
        x += 100;
        stat(g, x, "GAS", "$" + fmt2(game.econ.gas), null, Color.white);
        x += 80;
        int wk = game.week + 1;
        stat(g, x, "TERM " + game.term, "Week " + (game.tw() + 1), "of " + (game.termWeeks - game.termStart), C_TEXT);
        if (game.over) u.button(g, DW - 190, 10, 170, 36, "The verdict  >", B_GOLD, () -> Legacy.begin(u), "Your presidency is over. See how history judges it.");
        else u.button(g, DW - 190, 10, 170, 36, game.watch ? "Watching..." : "End week  >", game.watch ? B_NORM : B_GOLD, () -> { if (game.watch) game.watch = false; else endWeek(u); }, game.watch ? "Watch mode is on: click to take the controls back." : "Play out this week. (Space)");
        u.button(g, DW - 252, 10, 54, 36, "Save", B_GHOST, () -> SaveGame.save(u, SaveGame.SLOT), "Save the game");
        if (!game.over) u.button(g, DW - 378, 10, 58, 36, game.watch ? Watch.SPEED[clampi(game.watchSpeed, 0, 2)] : "Watch", game.watch ? B_PRIMARY : B_GHOST,
                () -> { if (game.watch) game.watchSpeed = (game.watchSpeed + 1) % 3; else u.go(SC_WATCH); },
                game.watch ? "Watch speed: click to change. Click 'Watching...' to stop." : "Watch mode: choose how the computer governs, then watch it play until you take back the controls.");
        u.button(g, DW - 314, 10, 54, 36, "Menu", B_GHOST, () -> u.modal("Leave the Oval Office?", "Your game is saved automatically every week.", new String[]{"Stay", "Main menu"},
                new Runnable[]{null, () -> { SaveGame.save(u, SaveGame.AUTO); u.go(SC_HOME); }}, null));
    }

    static String successorName(Game g) {
        Reelect re = g.re;
        if (re != null && re.done && re.opp != null) { Cand w = re.won ? (re.nominee != null ? re.nominee : g.pres) : re.opp; if (w != g.pres) return w.name(); }
        return "your successor";
    }

    static void stat(Graphics2D g, double x, String lab, String v, String sub, Color c) {
        label(g, lab, x, 22, C_DIM);
        text(g, v, x, 44, F_H2, c);
        if (sub != null) text(g, sub, x + strW(g, v, F_H2) + 6, 44, F_XS, sub.startsWith("-") ? C_BAD : sub.startsWith("+") ? C_GOOD : C_DIM);
    }

    /** A small presidential seal. */
    static void seal(Graphics2D g, double cx, double cy, double r) {
        g.setColor(rgb(0x1a2f63));
        g.fill(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
        g.setColor(C_GOLD);
        g.setStroke(new BasicStroke((float) Math.max(1, r * 0.08)));
        g.draw(new Ellipse2D.Double(cx - r * 0.92, cy - r * 0.92, r * 1.84, r * 1.84));
        for (int i = 0; i < 13; i++) {
            double a = -Math.PI / 2 + (i - 6) * 0.22;
            double sx = cx + Math.cos(a) * r * 0.7, sy = cy + Math.sin(a) * r * 0.7;
            g.fill(new Ellipse2D.Double(sx - r * 0.05, sy - r * 0.05, r * 0.1, r * 0.1));
        }
        // an eagle, much simplified: body, spread wings and a shield
        g.setColor(rgb(0xe8d9a8));
        Path2D w = new Path2D.Double();
        w.moveTo(cx - r * 0.62, cy - r * 0.05);
        w.curveTo(cx - r * 0.4, cy - r * 0.35, cx - r * 0.15, cy - r * 0.2, cx, cy - r * 0.05);
        w.curveTo(cx + r * 0.15, cy - r * 0.2, cx + r * 0.4, cy - r * 0.35, cx + r * 0.62, cy - r * 0.05);
        w.curveTo(cx + r * 0.35, cy + r * 0.05, cx + r * 0.2, cy + r * 0.1, cx, cy + r * 0.1);
        w.curveTo(cx - r * 0.2, cy + r * 0.1, cx - r * 0.35, cy + r * 0.05, cx - r * 0.62, cy - r * 0.05);
        g.fill(w);
        g.setColor(rgb(0xb22234));
        g.fill(new RoundRectangle2D.Double(cx - r * 0.16, cy - r * 0.08, r * 0.32, r * 0.42, r * 0.1, r * 0.1));
        g.setColor(rgb(0x3c3b6e));
        g.fill(new Rectangle2D.Double(cx - r * 0.16, cy - r * 0.08, r * 0.32, r * 0.12));
        g.setStroke(new BasicStroke(1f));
    }

    // ---- the room itself ----
    static final double[][] SPOT = {
            {650, 430, 300, 170},     // desk
            {585, 120, 430, 255},     // windows
            {1290, 455, 120, 110},    // briefing book (economy)
            {1440, 200, 110, 300},    // residence door
            {50, 200, 110, 300},      // West Wing door
            {1180, 160, 90, 250},     // Roosevelt Room door, beside the windows
            {300, 420, 110, 150},     // globe
            {205, 150, 90, 270},      // press door
            {690, 752, 220, 70}};     // papers on the coffee table
    static final int[] SPOT_ROOM = {R_DESK, R_COUNTRY, R_ECON, R_RESIDENCE, R_WESTWING, R_CONGRESS, R_SITROOM, R_PRESS, -1};
    static final String[] SPOT_LABEL = {"The Resolute Desk: plan your week", "The windows: the country", "The briefing book: the economy", "To the Residence",
            "To the West Wing: staff and cabinet", "To the Roosevelt Room: Congress", "The globe: the Situation Room", "To the Press Briefing Room", "Today's paper"};

    public static void draw(ResoluteDesk u, Graphics2D g) {
        Game game = u.game;
        if (game == null) { u.go(SC_HOME); return; }
        scene(u, g, game);
        hot = -1;
        for (int i = 0; i < SPOT.length; i++) {
            double[] s = SPOT[i];
            final int k = i;
            boolean over = u.modal == null && u.hover(s[0], s[1], s[2], s[3]);
            if (over) hot = i;
            u.hit(s[0], s[1], s[2], s[3], () -> { if (SPOT_ROOM[k] >= 0) Rooms.open(u, SPOT_ROOM[k]); else u.go(SC_PAPER); }, null);
        }
        if (hot >= 0) {
            double[] s = SPOT[hot];
            g.setColor(new Color(242, 193, 78, 40));
            g.fill(new RoundRectangle2D.Double(s[0] - 4, s[1] - 4, s[2] + 8, s[3] + 8, 14, 14));
            g.setColor(new Color(242, 193, 78, 170));
            g.setStroke(new BasicStroke(2f));
            g.draw(new RoundRectangle2D.Double(s[0] - 4, s[1] - 4, s[2] + 8, s[3] + 8, 14, 14));
            g.setStroke(new BasicStroke(1f));
            String lab = SPOT_LABEL[hot] + (SPOT_ROOM[hot] >= 0 && !ROOM_OPEN[SPOT_ROOM[hot]] ? "  (coming in a later update)" : "");
            double w = strW(g, lab, F_SB) + 28, lx = clamp(s[0] + s[2] / 2 - w / 2, 10, DW - w - 10), ly = s[1] + s[3] + 12 > 790 ? s[1] - 40 : s[1] + s[3] + 10;
            g.setColor(new Color(8, 13, 26, 235));
            g.fill(new RoundRectangle2D.Double(lx, ly, w, 30, 10, 10));
            text(g, lab, lx + 14, ly + 20, F_SB, C_GOLD);
        }
        topBar(u, g, game);
        weekCard(u, g, game);
        if (game.re != null && !game.re.done && game.re.stage >= 2) campaignCard(u, g, game); else hillCard(u, g, game);
        ticker(u, g, game);
        Tutorial.draw(u, g, game);
    }

    static void weekCard(ResoluteDesk u, Graphics2D g, Game game) {
        double x = 24, y = 640, w = 300, h = 150;
        g.setColor(new Color(8, 13, 26, 220));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 12, 12));
        g.setColor(new Color(242, 193, 78, 70));
        g.draw(new RoundRectangle2D.Double(x, y, w, h, 12, 12));
        label(g, "THIS WEEK", x + 14, y + 22, C_GOLD);
        int trips = 0, policy = 0, rest = 0;
        for (int i = 0; i < NSLOT; i++) { int a = game.act[i]; if (a == AC_TRIP) trips++; if (a == AC_POLICY || a == AC_REMARKS) policy++; if (a == AC_REST || a == AC_FAMILY || a == AC_CAMPDAVID) rest++; }
        text(g, policy + " policy slots, " + trips + (trips == 1 ? " trip, " : " trips, ") + rest + " to recharge", x + 14, y + 44, F_S, C_TEXT);
        double ps = Week.projectedStamina(game);
        text(g, "Stamina after the week", x + 14, y + 68, F_XS, C_DIM);
        bar(g, x + 14, y + 74, w - 80, 8, ps / 100, ps >= 50 ? C_GOOD : ps >= 25 ? C_AMBER : C_BAD);
        textR(g, String.valueOf(Math.round(ps)), x + w - 14, y + 83, F_SB, Color.white);
        text(g, "Sleep about " + fmt1(Week.sleepOf(game)) + " hours a night", x + 14, y + 104, F_XS, C_DIM);
        u.button(g, x + 14, y + 114, w - 28, 26, "Plan the week", B_NORM, () -> Rooms.open(u, R_DESK));
    }

    /** In an election year: the race at a glance. */
    static void campaignCard(ResoluteDesk u, Graphics2D g, Game game) {
        Reelect re = game.re;
        double w = 300, h = 150, x = DW - 24 - w, y = 640;
        g.setColor(new Color(8, 13, 26, 220));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 12, 12));
        g.setColor(new Color(242, 193, 78, 70));
        g.draw(new RoundRectangle2D.Double(x, y, w, h, 12, 12));
        Cand me = re.nominee != null ? re.nominee : game.pres;
        label(g, "THE " + re.year + " RACE", x + 14, y + 22, C_GOLD);
        text(g, me.last + (re.opp != null ? " vs. " + re.opp.last : ": the other side hasn't picked"), x + 14, y + 44, F_SB, Color.white);
        text(g, "Polls: " + (re.nat >= 0 ? me.last + " +" : (re.opp != null ? re.opp.last : "them") + " +") + fmt1(Math.abs(re.nat)), x + 14, y + 66, F_S, re.nat >= 0 ? C_GOOD : C_BAD);
        text(g, "Electoral votes today: " + Reelection.forecastEv(game), x + 14, y + 86, F_XS, C_DIM);
        text(g, Math.max(0, Reelection.weeksTo(game)) + " weeks to Election Day", x + 14, y + 104, F_XS, C_DIM);
        u.button(g, x + 14, y + 114, w - 28, 26, "Campaign headquarters", B_NORM, () -> { PressRoom.tab = 5; Rooms.open(u, R_PRESS); });
    }

    /** What's moving on Capitol Hill, bottom right of the office. */
    static void hillCard(ResoluteDesk u, Graphics2D g, Game game) {
        if (game.cong == null) return;
        Congress c = game.cong;
        double w = 300, h = 150, x = DW - 24 - w, y = 640;
        g.setColor(new Color(8, 13, 26, 220));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 12, 12));
        g.setColor(new Color(242, 193, 78, 70));
        g.draw(new RoundRectangle2D.Double(x, y, w, h, 12, 12));
        label(g, "ON THE HILL", x + 14, y + 22, C_GOLD);
        double yy = y + 44;
        int n = 0;
        for (int i = c.bills.size() - 1; i >= 0 && n < 3; i--) {
            Bill b = c.bills.get(i);
            if (!b.ours || b.stage < ST_COMMITTEE || b.stage > ST_DESK) continue;
            textFit(g, b.name, x + 14, yy, 160, F_XS, Color.white);
            textR(g, b.held ? "votes short" : STAGE[b.stage], x + w - 14, yy, F_XS, b.held || b.bottled ? C_AMBER : C_ACCENT);
            yy += 18; n++;
        }
        if (n == 0) { text(g, "None of your bills is moving.", x + 14, yy, F_XS, C_DIM); yy += 18; }
        String status = c.shutdown ? "The government is shut down" : c.xWeek >= 0 ? "Debt limit: " + Math.max(0, c.xWeek - game.week) + " weeks of room" : c.crUntil >= 0 ? "Funded by a stopgap until " + dateStr(c.crUntil * 7) : "Government funded";
        text(g, status, x + 14, Math.max(yy, y + 100), F_XS, c.shutdown || c.xWeek >= 0 ? C_AMBER : C_DIM);
        u.button(g, x + 14, y + 114, w - 28, 26, "The Roosevelt Room", B_NORM, () -> Rooms.open(u, R_CONGRESS));
    }

    static void ticker(ResoluteDesk u, Graphics2D g, Game game) {
        double y = DH - 44;
        g.setColor(new Color(6, 10, 20, 250));
        g.fill(new Rectangle2D.Double(0, y, DW, 44));
        g.setColor(rgb(0xb22234));
        g.fill(new Rectangle2D.Double(0, y, 130, 44));
        text(g, "THE LEDGER", 16, y + 27, F_SB, Color.white);
        Shape clip = g.getClip();
        g.clip(new Rectangle2D.Double(140, y, DW - 140, 44));
        StringBuilder b = new StringBuilder();
        for (int i = game.ticker.size() - 1; i >= Math.max(0, game.ticker.size() - 12); i--) b.append(game.ticker.get(i)).append("      |      ");
        String s = b.toString();
        double w = Math.max(400, strW(g, s, F_S));
        double off = (tick * 1.0) % w;
        text(g, s, 150 - off, y + 27, F_S, C_TEXT);
        text(g, s, 150 - off + w, y + 27, F_S, C_TEXT);
        g.setClip(clip);
    }

    static void scene(ResoluteDesk u, Graphics2D g, Game game) {
        double t = u.tsec;
        // the curved back wall
        g.setPaint(new GradientPaint(0, 56, rgb(0xefe3c8), 0, 560, rgb(0xd8c7a3)));
        g.fill(new Rectangle2D.Double(0, 56, DW, 560));
        g.setColor(new Color(255, 255, 255, 30));
        for (int i = 0; i < 9; i++) { double px = 40 + i * 190; g.draw(new Line2D.Double(px, 80, px, 520)); }
        // crown moulding and the oval ceiling's edge
        g.setColor(rgb(0xcdb98e));
        g.fill(new Rectangle2D.Double(0, 56, DW, 18));
        g.setColor(rgb(0xb9a37a));
        g.fill(new Rectangle2D.Double(0, 520, DW, 14));
        // three windows behind the desk, onto the South Lawn
        for (int i = 0; i < 3; i++) {
            double wx = 600 + i * 140, wy = 130, ww = 110, wh = 240;
            g.setPaint(new GradientPaint((float) wx, (float) wy, rgb(0x9cc8ef), (float) wx, (float) (wy + wh), rgb(0xdcefff)));
            g.fill(new Rectangle2D.Double(wx, wy, ww, wh));
            g.setColor(rgb(0x6f9f4f));
            g.fill(new Rectangle2D.Double(wx, wy + wh * 0.72, ww, wh * 0.28));
            if (i == 1) {   // the Washington Monument in the distance
                g.setColor(rgb(0xf4f1ea));
                Path2D ob = new Path2D.Double();
                ob.moveTo(wx + ww / 2 - 6, wy + wh * 0.72); ob.lineTo(wx + ww / 2 - 4, wy + 40); ob.lineTo(wx + ww / 2, wy + 30); ob.lineTo(wx + ww / 2 + 4, wy + 40); ob.lineTo(wx + ww / 2 + 6, wy + wh * 0.72); ob.closePath();
                g.fill(ob);
            }
            g.setColor(rgb(0xf7f2e6));
            g.setStroke(new BasicStroke(5f));
            g.draw(new Rectangle2D.Double(wx, wy, ww, wh));
            g.setStroke(new BasicStroke(2.5f));
            for (int k = 1; k < 3; k++) g.draw(new Line2D.Double(wx + ww * k / 3, wy, wx + ww * k / 3, wy + wh));
            for (int k = 1; k < 5; k++) g.draw(new Line2D.Double(wx, wy + wh * k / 5, wx + ww, wy + wh * k / 5));
            g.setStroke(new BasicStroke(1f));
            // gold drapes
            for (int side = 0; side < 2; side++) {
                double dx = side == 0 ? wx - 26 : wx + ww - 4;
                g.setPaint(new GradientPaint((float) dx, 0, rgb(0xc9962e), (float) (dx + 30), 0, rgb(0xf0c862)));
                Path2D d = new Path2D.Double();
                d.moveTo(dx, wy - 16); d.lineTo(dx + 30, wy - 16); d.curveTo(dx + 26, wy + wh * 0.5, dx + 34, wy + wh * 0.8, dx + 32, wy + wh + 30); d.lineTo(dx - 4, wy + wh + 30); d.curveTo(dx + 4, wy + wh * 0.7, dx - 2, wy + wh * 0.4, dx, wy - 16); d.closePath();
                g.fill(d);
            }
        }
        g.setColor(rgb(0xb4862a));
        g.fill(new Rectangle2D.Double(560, 108, 480, 16));
        // flags either side of the windows
        flag(g, 520, 180, t, true);
        flag(g, 1080, 180, t, false);
        // doors: West Wing (far left), press office, Roosevelt Room, residence (far right)
        door(g, 60, 210, 90, 290, hot == 4);
        door(g, 215, 160, 70, 260, hot == 7);
        door(g, 1190, 170, 70, 240, hot == 5);
        door(g, 1450, 210, 90, 290, hot == 3);
        // portraits
        frame(g, 330, 150, 120, 150, rgb(0x3f4a5e));
        frame(g, 1290, 160, 110, 140, rgb(0x5e4a3f));
        // the floor
        g.setPaint(new GradientPaint(0, 534, rgb(0x7a5230), 0, DH, rgb(0x4c321d)));
        g.fill(new Rectangle2D.Double(0, 534, DW, DH - 534));
        g.setColor(new Color(0, 0, 0, 40));
        for (int i = 0; i < 40; i++) g.draw(new Line2D.Double(i * 44 - 200, 534, i * 44 + 120, DH));
        // the oval rug with the seal
        Color rug = game.party == REP ? rgb(0xb98a2e) : rgb(0x1f3a7a);
        g.setColor(shade(rug, 0.7));
        g.fill(new Ellipse2D.Double(220, 560, 1160, 300));
        g.setColor(rug);
        g.fill(new Ellipse2D.Double(240, 568, 1120, 284));
        g.setColor(new Color(242, 193, 78, 160));
        g.setStroke(new BasicStroke(3f));
        g.draw(new Ellipse2D.Double(270, 580, 1060, 260));
        for (int i = 0; i < 24; i++) {
            double a = i * Math.PI * 2 / 24;
            g.draw(new Line2D.Double(800 + Math.cos(a) * 150, 710 + Math.sin(a) * 40, 800 + Math.cos(a) * 470, 710 + Math.sin(a) * 118));
        }
        g.setStroke(new BasicStroke(1f));
        AffineTransform at = g.getTransform();
        g.translate(800, 710);
        g.scale(1, 0.3);
        seal(g, 0, 0, 120);
        g.setTransform(at);
        // sofas and the coffee table
        sofa(g, 330, 610, 250, true);
        sofa(g, 1020, 610, 250, false);
        g.setColor(rgb(0x3b2414));
        g.fill(new RoundRectangle2D.Double(690, 778, 220, 40, 8, 8));
        g.setColor(rgb(0x5a3a22));
        g.fill(new RoundRectangle2D.Double(690, 770, 220, 18, 8, 8));
        g.setColor(rgb(0xf4f0e6));
        g.fill(new Rectangle2D.Double(730, 760, 70, 14));
        g.fill(new Rectangle2D.Double(812, 762, 64, 12));
        g.setColor(rgb(0x333333));
        g.fill(new Rectangle2D.Double(736, 763, 56, 2));
        // the globe on its stand
        g.setColor(rgb(0x4a2e18));
        g.fill(new Rectangle2D.Double(350, 520, 8, 46));
        g.fill(new Rectangle2D.Double(330, 562, 50, 6));
        g.setPaint(new GradientPaint(320, 440, rgb(0x6fa8c9), 380, 510, rgb(0x2e5f80)));
        g.fill(new Ellipse2D.Double(318, 438, 76, 76));
        g.setColor(rgb(0xa9c77b));
        double spin = (t * 6) % 76;
        for (int k = 0; k < 3; k++) { double cx = 318 + ((spin + k * 26) % 76); g.fill(new Ellipse2D.Double(cx - 8, 456 + k * 14, 16 + k * 3, 10)); }
        g.setColor(rgb(0xb4862a));
        g.setStroke(new BasicStroke(2f));
        g.draw(new Arc2D.Double(312, 432, 88, 88, 200, 320, Arc2D.OPEN));
        g.setStroke(new BasicStroke(1f));
        // the side table and the briefing book
        g.setColor(rgb(0x3b2414));
        g.fill(new Rectangle2D.Double(1296, 500, 108, 64));
        g.setColor(rgb(0x5a3a22));
        g.fill(new Rectangle2D.Double(1290, 492, 120, 12));
        g.setColor(rgb(0x1a2f63));
        g.fill(new Rectangle2D.Double(1312, 470, 76, 22));
        g.setColor(C_GOLD);
        text(g, "CEA", 1334, 486, F_TB, C_GOLD);
        // the Resolute Desk and the chair
        g.setColor(rgb(0x2b1b12));
        g.fill(new RoundRectangle2D.Double(762, 400, 76, 80, 20, 20));
        g.setPaint(new GradientPaint(650, 450, rgb(0x6a4026), 650, 600, rgb(0x3e2414)));
        g.fill(new Rectangle2D.Double(660, 460, 280, 130));
        g.setColor(rgb(0x7a4b2d));
        g.fill(new Rectangle2D.Double(648, 448, 304, 18));
        g.setColor(rgb(0x4a2c18));
        g.fill(new Rectangle2D.Double(752, 480, 96, 106));
        g.setColor(new Color(242, 193, 78, 120));
        g.draw(new Rectangle2D.Double(756, 484, 88, 98));
        seal(g, 800, 528, 26);
        g.setColor(rgb(0x3e2414));
        for (int k = 0; k < 2; k++) g.draw(new Rectangle2D.Double(672 + k * 186, 478, 70, 100));
        g.setColor(rgb(0xf4f0e6));
        g.fill(new Rectangle2D.Double(700, 440, 60, 10));
        g.setColor(rgb(0x222222));
        g.fill(new RoundRectangle2D.Double(866, 438, 36, 12, 4, 4));
        // the president, at the desk
        Portrait.draw(g, game.pres, 770, 360, 60, 72, u.tsec, 0, true, false);
    }

    static void flag(Graphics2D g, double x, double y, double t, boolean us) {
        g.setColor(rgb(0xc9a24a));
        g.fill(new Rectangle2D.Double(x - 2, y - 40, 4, 360));
        g.fill(new Ellipse2D.Double(x - 6, y - 50, 12, 12));
        for (int k = 0; k < 7; k++) {
            g.setColor(us ? (k % 2 == 0 ? rgb(0xb22234) : rgb(0xf5f5f0)) : (k < 4 ? rgb(0x1a2f63) : rgb(0x223c78)));
            Path2D p = new Path2D.Double();
            double sway = Math.sin(t * 0.8 + k * 0.4) * 2;
            p.moveTo(x + 2, y - 30 + k * 26);
            p.curveTo(x + 18, y - 26 + k * 26, x + 10 + sway, y - 10 + k * 26, x + 26 + sway, y - 4 + k * 26);
            p.lineTo(x + 26 + sway, y + 22 + k * 26);
            p.curveTo(x + 10 + sway, y + 16 + k * 26, x + 18, y + 2 + k * 26, x + 2, y - 4 + k * 26);
            p.closePath();
            g.fill(p);
        }
        if (us) { g.setColor(rgb(0x3c3b6e)); g.fill(new Rectangle2D.Double(x + 2, y - 30, 14, 60)); }
        else { g.setColor(C_GOLD); g.fill(new Ellipse2D.Double(x + 6, y + 30, 14, 14)); }
    }

    static void door(Graphics2D g, double x, double y, double w, double h, boolean lit) {
        g.setColor(rgb(0xcdb98e));
        g.fill(new Rectangle2D.Double(x - 10, y - 22, w + 20, h + 22));
        Path2D ped = new Path2D.Double();
        ped.moveTo(x - 16, y - 22); ped.lineTo(x + w / 2, y - 50); ped.lineTo(x + w + 16, y - 22); ped.closePath();
        g.fill(ped);
        g.setPaint(new GradientPaint((float) x, (float) y, lit ? rgb(0xfaf3e1) : rgb(0xf2e9d3), (float) (x + w), (float) (y + h), rgb(0xd9ccad)));
        g.fill(new Rectangle2D.Double(x, y, w, h));
        g.setColor(new Color(0, 0, 0, 40));
        g.draw(new Rectangle2D.Double(x + 8, y + 10, w - 16, h * 0.42));
        g.draw(new Rectangle2D.Double(x + 8, y + h * 0.5, w - 16, h * 0.46));
        g.setColor(C_GOLD);
        g.fill(new Ellipse2D.Double(x + w - 18, y + h * 0.5, 7, 7));
    }

    static void frame(Graphics2D g, double x, double y, double w, double h, Color c) {
        g.setColor(rgb(0xb4862a));
        g.fill(new Rectangle2D.Double(x - 8, y - 8, w + 16, h + 16));
        g.setPaint(new GradientPaint((float) x, (float) y, shade(c, 1.4), (float) x, (float) (y + h), shade(c, 0.6)));
        g.fill(new Rectangle2D.Double(x, y, w, h));
        g.setColor(new Color(255, 230, 190, 60));
        g.fill(new Ellipse2D.Double(x + w * 0.3, y + h * 0.18, w * 0.4, h * 0.42));
    }

    static void sofa(Graphics2D g, double x, double y, double w, boolean left) {
        Color c = rgb(0xe8dcc0);
        g.setColor(shade(c, 0.7));
        g.fill(new RoundRectangle2D.Double(x, y + 30, w, 60, 18, 18));
        g.setColor(c);
        g.fill(new RoundRectangle2D.Double(x, y, w, 48, 20, 20));
        g.setColor(shade(c, 0.88));
        g.fill(new RoundRectangle2D.Double(x + 10, y + 36, w - 20, 34, 14, 14));
        g.setColor(shade(c, 0.8));
        g.fill(new RoundRectangle2D.Double(left ? x - 8 : x + w - 22, y + 18, 30, 68, 12, 12));
        g.fill(new RoundRectangle2D.Double(left ? x + w - 22 : x - 8, y + 18, 30, 68, 12, 12));
    }
}
