package resolutedesk.ui;

import static resolutedesk.core.Fmt.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.sim.DecisionKinds.*;
import static resolutedesk.ui.Draw.*;
import static resolutedesk.ui.Fonts.*;
import static resolutedesk.ui.ScreenIds.*;
import static resolutedesk.ui.Theme.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.List;
import resolutedesk.ResoluteDesk;
import resolutedesk.congress.BillDef;
import resolutedesk.model.Game;
import resolutedesk.sim.Auto;
import resolutedesk.sim.Decision;
import resolutedesk.sim.Decisions;
import resolutedesk.sim.WatchPlan;

/**
 * Watch mode: the game governs on its own while the player looks on. Before it starts, the player sets the plan:
 * a preset or six priorities, the bills to send and in what order, and the decisions to stop for.
 */
public final class Watch {
    private Watch() {
    }

    static final double[] SECONDS = {1.6, 0.8, 0.3};
    static final String[] SPEED = {"Slow", "Normal", "Fast"};
    static double timer;

    static void update(ResoluteDesk u, Game g, double dt) {
        if (!g.watch || g.over) return;
        if (u.screen == SC_PAPER) u.go(SC_OVAL);
        if (u.modal != null || u.screen == SC_WATCH) return;
        if (!g.inbox.isEmpty()) {
            Decision d = g.inbox.get(0);
            if (Auto.plan(g).stops(d.kind)) {          // the plan wants the player to make this one
                Decisions.show(u, g);
                u.toast("Watch mode is waiting for your decision.", C_GOLD);
                return;
            }
            int k = Decisions.auto(g, d);
            g.inbox.remove(0);
            // the big calls get a toast; the small print (speech lines, press questions, summit rounds) doesn't
            boolean big = WatchPlan.stopFor(d.kind) >= 0 || d.kind == D_DISASTER;
            if (big && d.opts != null && d.opts.length > 0) u.toast(d.title + ": " + d.opts[Math.max(0, Math.min(k, d.opts.length - 1))], C_GOLD);
            Decisions.apply(g, d, k);
            return;
        }
        timer += dt;
        if (timer < SECONDS[clampi(g.watchSpeed, 0, 2)]) return;
        timer = 0;
        Auto.week(g);
        Oval.endWeek(u);
    }

    /** Starts watching with the plan as set. */
    static void start(ResoluteDesk u, Game g) {
        g.watch = true;
        g.tut = -1;
        timer = 0;
        u.go(SC_OVAL);
        u.toast("Watching. Click 'Watching...' to take back the controls.", C_GOLD);
    }

    // ---- the plan ----

    public static void draw(ResoluteDesk u, Graphics2D g) {
        Game game = u.game;
        if (game == null) { u.go(SC_HOME); return; }
        WatchPlan pl = Auto.plan(game);
        Screens.header(u, g, "Watch Mode", "Choose how the computer governs, then watch it play. Take back the controls at any time.");
        priorities(u, g, pl);
        agenda(u, g, game, pl);
        stops(u, g, game, pl);
        u.button(g, 48, 832, 180, 44, "Back", B_GHOST, () -> u.go(SC_OVAL), "Esc");
        u.button(g, DW - 48 - 300, 832, 300, 44, "Start watching", B_GOLD, () -> start(u, game), "Enter");
    }

    static void priorities(ResoluteDesk u, Graphics2D g, WatchPlan pl) {
        double x = 48, w = 520;
        u.panel(g, x, 124, w, 690, "How the computer governs");
        double y = 160;
        String[] top = {WatchPlan.PRESET[0], WatchPlan.PRESET[1], WatchPlan.PRESET[2]}, bottom = {WatchPlan.PRESET[3], WatchPlan.PRESET[4], WatchPlan.PRESET[5]};
        u.segmented(g, x + 18, y, w - 36, 30, top, pl.preset < 3 ? pl.preset : -1, i -> pl.use(i), new String[]{WatchPlan.PRESET_DESC[0], WatchPlan.PRESET_DESC[1], WatchPlan.PRESET_DESC[2]});
        u.segmented(g, x + 18, y + 36, w - 36, 30, bottom, pl.preset >= 3 && pl.preset < WatchPlan.CUSTOM ? pl.preset - 3 : -1, i -> pl.use(i + 3),
                new String[]{WatchPlan.PRESET_DESC[3], WatchPlan.PRESET_DESC[4], WatchPlan.PRESET_DESC[5]});
        wrap(g, WatchPlan.PRESET[pl.preset] + ": " + WatchPlan.PRESET_DESC[pl.preset], x + 18, y + 90, w - 36, F_XS, pl.preset == WatchPlan.CUSTOM ? C_GOLD : C_DIM);
        y += 128;
        for (int i = 0; i < WatchPlan.NPRIORITY; i++) {
            final int k = i;
            text(g, WatchPlan.PRIORITY[i], x + 18, y, F_SB, Color.white);
            u.slider(g, x + 18, y + 18, w - 36, pl.p[i], C_GOLD, v -> pl.set(k, Math.round(v * 20) / 20.0), WatchPlan.PRIORITY_DESC[i]);
            text(g, WatchPlan.PRIORITY_ENDS[i][0], x + 18, y + 44, F_T, C_DIM);
            textR(g, WatchPlan.PRIORITY_ENDS[i][1], x + w - 18, y + 44, F_T, C_DIM);
            y += 76;
        }
    }

    static void agenda(ResoluteDesk u, Graphics2D g, Game game, WatchPlan pl) {
        double x = 584, w = 520;
        u.panel(g, x, 124, w, 690, "Your agenda");
        wrap(g, pl.bills.isEmpty() ? "The computer chooses which of your bills to send, and when. Check bills to choose them yourself; it sends them in the order you check them."
                : "The computer sends the checked bills, in this order, and no others. Uncheck them all to let it choose.", x + 18, 170, w - 36, F_XS, C_DIM);
        List<BillDef> all = HillUI.agenda(game);
        double top = 208, hh = 590;
        double off = u.beginScroll(g, "watchbills", x + 10, top, w - 20, hh);
        int h0 = u.hits.size();
        double y = top - off;
        int n = 0;
        for (BillDef d : all) {
            if (d.lean >= 0 && d.lean != game.party) continue;
            final String id = d.id;
            int at = pl.bills.indexOf(id);
            u.card(g, x + 18, y, w - 36, 50, at >= 0, () -> { if (pl.bills.contains(id)) pl.bills.remove(id); else pl.bills.add(id); }, d.desc);
            text(g, at >= 0 ? String.valueOf(at + 1) : "", x + 34, y + 31, F_H3, C_GOLD);
            textFit(g, d.name, x + 64, y + 22, w - 240, F_SB, Color.white);
            text(g, d.lean < 0 ? "Bipartisan" : "Your party's", x + 64, y + 40, F_T, d.lean < 0 ? C_ACCENT : C_DIM);
            if (at >= 0) {
                u.button(g, x + w - 112, y + 11, 36, 28, "Up", B_GHOST, at > 0 ? () -> { pl.bills.remove(id); pl.bills.add(at - 1, id); } : null, "Send it sooner.");
                u.button(g, x + w - 70, y + 11, 36, 28, "Dn", B_GHOST, at < pl.bills.size() - 1 ? () -> { pl.bills.remove(id); pl.bills.add(at + 1, id); } : null, "Send it later.");
            }
            y += 56;
            n++;
        }
        u.clipHits(h0, top, top + hh);
        u.endScroll(g, "watchbills", n * 56);
    }

    static void stops(ResoluteDesk u, Graphics2D g, Game game, WatchPlan pl) {
        double x = 1120, w = DW - 48 - x;
        u.panel(g, x, 124, w, 690, "Stop for these decisions");
        wrap(g, "When one of these reaches your desk, watch mode waits for you to decide. Everything else, the computer decides.", x + 18, 170, w - 36, F_XS, C_DIM);
        double y = 208;
        for (int i = 0; i < WatchPlan.NSTOP; i++) {
            final int k = i;
            u.button(g, x + 18, y, w - 36, 34, (pl.stop[i] ? "[x]  " : "[  ]  ") + WatchPlan.STOP[i], pl.stop[i] ? B_ON : B_GHOST, () -> pl.stop[k] = !pl.stop[k], null);
            y += 42;
        }
        y += 18;
        label(g, "Speed", x + 18, y, C_GOLD);
        u.segmented(g, x + 18, y + 12, w - 36, 30, SPEED, clampi(game.watchSpeed, 0, 2), i -> game.watchSpeed = i, new String[]{"A week every " + fmt1(SECONDS[0]) + " seconds.", "A week every " + fmt1(SECONDS[1]) + " seconds.", "A week every " + fmt1(SECONDS[2]) + " seconds."});
        y += 66;
        wrap(g, "In the middle, every priority is the steady hand watch mode has always used. The plan is saved with the game.", x + 18, y, w - 36, F_XS, C_FAINT);
    }

    public static void key(ResoluteDesk u, KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_ESCAPE) u.go(SC_OVAL);
        if (k == KeyEvent.VK_ENTER && u.game != null) start(u, u.game);
    }
}
