package resolutedesk.ui;

import static resolutedesk.core.Num.*;
import static resolutedesk.sim.DecisionKinds.*;
import static resolutedesk.ui.ScreenIds.*;
import static resolutedesk.ui.Theme.*;

import resolutedesk.ResoluteDesk;
import resolutedesk.model.Game;
import resolutedesk.sim.Auto;
import resolutedesk.sim.Decision;
import resolutedesk.sim.Decisions;

/** Watch mode: the game governs on its own while the player looks on. */
public final class Watch {
    static final double[] SECONDS = {1.6, 0.8, 0.3};
    static final String[] SPEED = {"Slow", "Normal", "Fast"};
    static double timer;

    static void update(ResoluteDesk u, Game g, double dt) {
        if (!g.watch || g.over) return;
        if (u.screen == SC_PAPER) u.go(SC_OVAL);
        if (u.modal != null) return;
        if (!g.inbox.isEmpty()) {
            Decision d = g.inbox.get(0);
            int k = Decisions.auto(g, d);
            g.inbox.remove(0);
            // the big calls get a toast; the small print (speech lines, press questions, summit rounds) doesn't
            boolean big = d.kind == D_SIGN || d.kind == D_WORLD || d.kind == D_HIST || d.kind == D_CAMPAIGN || d.kind == D_IMPEACH || d.kind == D_SHUTDOWN || d.kind == D_DEBT || d.kind == D_DISASTER || d.kind == D_COURT;
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
}
