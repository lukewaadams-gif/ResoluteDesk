package resolutedesk.ui;

import static resolutedesk.core.Fmt.*;
import static resolutedesk.data.ElectoralMap.*;
import static resolutedesk.data.StateData.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.ui.Draw.*;
import static resolutedesk.ui.Fonts.*;
import static resolutedesk.ui.ScreenIds.*;
import static resolutedesk.ui.Sound.*;
import static resolutedesk.ui.Theme.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import resolutedesk.ResoluteDesk;
import resolutedesk.campaign.Reelect;
import resolutedesk.legacy.Legacy;
import resolutedesk.model.Cand;
import resolutedesk.model.Game;

/** Election night: the states called one by one, then the result. */
public final class Night {
    static double t0;
    static int[] order;
    public static boolean all;

    public static void begin(ResoluteDesk u) {
        Game g = u.game;
        if (g == null || g.re == null) return;
        g.night = false;
        Integer[] o = new Integer[NS];
        for (int i = 0; i < NS; i++) o[i] = i;
        Arrays.sort(o, (a, b) -> Double.compare(STATES[a].cx + Math.abs(g.re.margin[a]) * 2, STATES[b].cx + Math.abs(g.re.margin[b]) * 2));
        order = new int[NS];
        for (int i = 0; i < NS; i++) order[i] = o[i];
        t0 = u.tsec;
        all = false;
        u.go(SC_NIGHT);
        snd(Sfx.OPEN);
    }

    public static void draw(ResoluteDesk u, Graphics2D g) {
        Game game = u.game;
        if (game == null || game.re == null) { u.go(SC_OVAL); return; }
        Reelect re = game.re;
        Cand me = re.nominee != null ? re.nominee : game.pres;
        g.setPaint(new GradientPaint(0, 0, rgb(0x070b16), 0, DH, rgb(0x15213d)));
        g.fill(new Rectangle2D.Double(0, 0, DW, DH));
        label(g, "ELECTION NIGHT " + re.year, 48, 50, C_GOLD);
        text(g, me.name() + " vs. " + re.opp.name(), 48, 84, F_DISPLAY, Color.white);
        double el = (u.tsec - t0) * 4.5;
        int shown = all ? NS : (int) Math.min(NS, el);
        int a = 0, b = 0;
        boolean[] in = new boolean[NS];
        for (int i = 0; i < shown; i++) in[order[i]] = true;
        for (int s = 0; s < NS; s++) {
            if (!votes(s, re.year)) { USMap.paint[s] = rgb(0x1d2434); USMap.tags[s] = ""; continue; }
            if (in[s]) {
                double sm = game.party == DEM ? re.margin[s] : -re.margin[s];
                USMap.paint[s] = marginColor(sm, 1);
                USMap.tags[s] = String.valueOf(STATES[s].ev);
                if (re.margin[s] > 0) a += STATES[s].ev; else b += STATES[s].ev;
            } else { USMap.paint[s] = rgb(0x3a4256); USMap.tags[s] = ""; }
        }
        USMap.markState = -1; USMap.markCand = null;
        double[] f = USMap.draw(u, g, 300, 120, 1000, 600, -1, true);
        int hs = USMap.stateAt(f, u.mx, u.my);
        if (hs >= 0 && in[hs] && votes(hs, re.year)) u.tip = STATES[hs].name + ": " + (re.margin[hs] > 0 ? me.last : re.opp.last) + " wins by " + fmt1(Math.abs(re.margin[hs])) + " points";
        int need = re.evTotal / 2 + 1;
        // the scoreboard
        tally(g, 48, 150, me, a, need, partyColor(game.party));
        tally(g, DW - 288, 150, re.opp, b, need, partyColor(re.opp.party));
        double p = (double) shown / NS;
        text(g, Math.round(p * 100) + "% of states reporting", 48, 760, F_S, C_DIM);
        boolean finished = shown >= NS;
        if (finished) {
            String head = re.won ? me.name().toUpperCase() + " WINS" : re.opp.name().toUpperCase() + " WINS";
            g.setColor(new Color(8, 13, 26, 230));
            g.fill(new RoundRectangle2D.Double(DW / 2 - 420, 726, 840, 70, 14, 14));
            textC(g, head, DW / 2, 770, F_H1, re.won ? C_GOLD : C_BAD);
            text(g, "Popular vote: " + me.last + " " + fmt1(re.popMe) + "%, " + re.opp.last + " " + fmt1(re.popOpp) + "%", 48, 790, F_S, C_TEXT);
            boolean cont = re.won && re.running;
            u.button(g, DW - 48 - 300, 832, 300, 44, cont ? "Four more years" : "Back to the Oval Office", B_GOLD, () -> { u.go(SC_OVAL); if (cont) u.toast("You'll be sworn in again on January 20.", C_GOLD); });
            if (!cont) u.button(g, DW - 48 - 620, 832, 300, 44, "Skip to the end of the term", B_NORM, () -> Legacy.finishTerm(u));
            if (!re.nightSeen) { re.nightSeen = true; snd(re.won ? Sfx.CHEER : Sfx.OPEN); }
            if (game.watch && u.tsec - t0 > NS / 4.5 + 3) u.go(SC_OVAL);
        } else u.button(g, DW - 48 - 300, 832, 300, 44, "Skip to the result", B_NORM, () -> all = true);
    }

    static void tally(Graphics2D g, double x, double y, Cand c, int ev, int need, Color col) {
        Portrait.draw(g, c, x, y, 120, 144, 0, 0, true, true);
        text(g, c.name(), x, y + 170, F_H3, col);
        text(g, String.valueOf(ev), x, y + 230, new Font(SERIF, Font.BOLD, 56), ev >= need ? C_GOLD : Color.white);
        text(g, "electoral votes", x, y + 254, F_XS, C_DIM);
        bar(g, x, y + 266, 200, 10, Math.min(1, ev / (double) need), col);
        text(g, need + " to win", x, y + 294, F_XS, C_FAINT);
    }
}
