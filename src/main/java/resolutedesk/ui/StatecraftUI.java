package resolutedesk.ui;

import static resolutedesk.core.Dates.*;
import static resolutedesk.core.Fmt.*;
import static resolutedesk.ui.Draw.*;
import static resolutedesk.ui.Fonts.*;
import static resolutedesk.ui.Theme.*;
import static resolutedesk.world.Countries.*;
import static resolutedesk.world.StatecraftData.*;
import static resolutedesk.world.WarData.*;
import static resolutedesk.world.WorldData.*;
import static resolutedesk.world.WorldTables.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.List;
import resolutedesk.ResoluteDesk;
import resolutedesk.model.Game;
import resolutedesk.world.Objective;
import resolutedesk.world.Objectives;
import resolutedesk.world.Statecraft;
import resolutedesk.world.Wars;
import resolutedesk.world.World;

/** The Situation Room's statecraft tab: one country's objectives, and every tool for dealing with it, up to war. */
public final class StatecraftUI {
    private StatecraftUI() {
    }

    static void draw(ResoluteDesk u, Graphics2D g, Game game) {
        World w = game.world;
        if (gone(game, SitRoom.sel)) for (int k = 0; k < NC; k++) if (!gone(game, k)) { SitRoom.sel = k; break; }
        countries(u, g, game, w);
        objectives(u, g, game, w, SitRoom.sel);
        tools(u, g, game, w, SitRoom.sel);
    }

    static void say(String m) { SitRoom.msg = m; }

    // ---- every country ----
    static void countries(ResoluteDesk u, Graphics2D g, Game game, World w) {
        u.panel(g, 48, 160, 200, 652, "Countries");
        double y = 188;
        int n = 0;
        for (int c = 0; c < NC; c++) if (!gone(game, c)) n++;
        double h = Math.min(16, 616.0 / Math.max(1, n));
        for (int rg = 0; rg < REGION.length; rg++) for (int c = 0; c < NC; c++) {
            if (CT_REGION[c] != rg || gone(game, c)) continue;
            final int cc = c;
            u.card(g, 54, y, 188, h - 1, c == SitRoom.sel, () -> { SitRoom.sel = cc; say(null); }, null);
            textFit(g, cname(game, c), 62, y + h - 4, 130, F_T, atWar(game, c) ? C_BAD : Color.white);
            textR(g, (w.rel[c] > 0 ? "+" : "") + Math.round(w.rel[c]), 236, y + h - 4, F_TB, WorldMap.relColor(w.rel[c]).brighter());
            y += h;
        }
    }

    // ---- what we want from them ----
    static void objectives(ResoluteDesk u, Graphics2D g, Game game, World w, int c) {
        double px = 260, pw = 500;
        u.panel(g, px, 160, pw, 652, null);
        Flag.draw(g, c, px + 18, 176, 54, 36);
        text(g, cname(game, c), px + 84, 192, F_H2, Color.white);
        int al = align(game, c);
        textFit(g, w.leader[c] + ", " + w.ltitle[c].toLowerCase() + ".  " + ALIGN[al] + (nuke(game, c) ? ", nuclear-armed" : ""), px + 84, 210, pw - 100, F_XS, al == AL_ADVERSARY ? C_REP_L : al == AL_TREATY ? C_GOOD : C_TEXT);
        String state = atWar(game, c) ? "At war with the United States" : "Relations " + (w.rel[c] > 0 ? "+" : "") + Math.round(w.rel[c]) + ";  ties " + TIES[w.ties[c]].toLowerCase()
                + (w.unrest[c] >= 40 ? ";  unrest " + Math.round(w.unrest[c]) : "") + (w.toppled[c] ? ";  government toppled" : "");
        textFit(g, state, px + 18, 234, pw - 36, F_XS, atWar(game, c) ? C_BAD : C_DIM);
        label(g, "Objectives", px + 18, 262, C_GOLD);
        List<Objective> l = Objectives.of(game, c);
        double y = 274, h = l.isEmpty() ? 0 : Math.min(132, (800 - y) / l.size() - 8);
        if (l.isEmpty()) wrap(g, "Nothing on the agenda with " + cnameIn(game, c) + " in this era.", px + 18, y + 16, pw - 36, F_XS, C_DIM);
        for (Objective o : l) { objective(u, g, game, o, px + 12, y, pw - 24, h); y += h + 8; }
    }

    static void objective(ResoluteDesk u, Graphics2D g, Game game, Objective o, double x, double y, double w, double h) {
        u.card(g, x, y, w, h, o.status == 1, null, null);
        String[][] steps = Objectives.def(o).steps;
        textFit(g, Objectives.name(game, o), x + 12, y + 20, w - 24, F_SB, o.status == 1 ? C_GOOD : Color.white);
        wrapMax(g, Objectives.why(game, o), x + 12, y + 36, w - 24, F_T, C_DIM, 2, 12);
        // the steps, as dots on a line
        double sx = x + 16, sy = y + 72, gap = (w - 32) / Math.max(1, steps.length - 1);
        g.setColor(new Color(255, 255, 255, 30));
        g.fillRect((int) sx, (int) sy - 1, (int) (w - 32), 2);
        for (int i = 0; i < steps.length; i++) {
            double cx = steps.length == 1 ? x + w / 2 : sx + i * gap;
            boolean done = i < o.step;
            g.setColor(done ? C_GOOD : i == o.step && o.status == 0 ? C_GOLD : new Color(255, 255, 255, 50));
            g.fill(new Ellipse2D.Double(cx - 5, sy - 5, 10, 10));
        }
        if (o.status == 1) { text(g, "Achieved", x + 12, y + h - 14, F_XSB, C_GOOD); return; }
        String[] s = Objectives.step(o);
        String no = Objectives.cannot(game, o);
        textFit(g, "Next: " + s[0] + (s[4].equals("-") ? "" : "  (costs something)"), x + 12, y + 96, w - 140, F_XS, C_TEXT);
        double odds = Objectives.odds(game, o);
        textFit(g, no != null ? no : odds >= 1 ? "Certain to work: everything it needs is in place" : Paper.cap(chance(odds)) + " to work", x + 12, y + 112, w - 140, F_T, no != null ? C_AMBER : odds >= 0.6 ? C_GOOD : C_GOLD);
        u.button(g, x + w - 120, y + h - 40, 108, 28, "Try it", no == null ? B_GOLD : B_OFF, no == null ? () -> say(Objectives.attempt(game, o)) : null, s[0] + ". Failure costs a month, and three in a row stall it for half a year.");
    }

    // ---- the tools ----
    static void tools(ResoluteDesk u, Graphics2D g, Game game, World w, int c) {
        double px = 772, pw = DW - 48 - px, x0 = px + 18, cw = pw - 36;
        u.panel(g, px, 160, pw, 652, "The tools");
        boolean war = atWar(game, c);
        double y = 208;
        // support
        label(g, "Military aid", x0, y, C_FAINT);
        u.segmented(g, x0 + 130, y - 14, cw - 130, 22, MIL_AID, w.milAid[c], i -> say(Statecraft.setMilAid(game, c, i)), null);
        y += 28;
        label(g, "Arms sales", x0, y, C_FAINT);
        u.segmented(g, x0 + 130, y - 14, cw - 130, 22, ARMS, w.arms[c], i -> say(Statecraft.setArms(game, c, i)), null);
        y += 28;
        label(g, "American forces", x0, y, C_FAINT);
        u.segmented(g, x0 + 130, y - 14, cw - 130, 22, TROOP_NAME, Statecraft.troopStep(w, c), i -> say(Statecraft.setTroops(game, c, i)), null);
        y += 28;
        label(g, "Diplomatic ties", x0, y, C_FAINT);
        u.segmented(g, x0 + 130, y - 14, (cw - 130) * 0.6, 22, TIES, w.ties[c], i -> say(Statecraft.setTies(game, c, i)), null);
        boolean pk = w.peace[c] > 0;
        u.button(g, x0 + 130 + (cw - 130) * 0.6 + 8, y - 14, (cw - 130) * 0.4 - 8, 22, pk ? "Peacekeepers: in" : "Send peacekeepers", pk ? B_ON : B_NORM, () -> say(Statecraft.setPeace(game, c, !pk)),
                "Join a UN or allied mission where there's a conflict to keep apart.");
        y += 22;
        if (c == CC_TW) { text(g, "Full ties with Taiwan means recognizing it as a country. Beijing would treat it as an act of hostility.", x0, y + 6, F_T, C_AMBER); y += 12; }
        // agreements
        y += 18;
        label(g, "Agreements", x0, y, C_GOLD);
        y += 10;
        double tw = (cw - 10) / 2;
        boolean hasPact = w.pact[c] > 0, ally = align(game, c) == AL_TREATY;
        u.button(g, x0, y, tw, 28, hasPact ? "Trade agreement in force" : "Send Congress a trade agreement", hasPact || war ? B_OFF : B_NORM, hasPact || war ? null : () -> say(Statecraft.tradeDeal(game, c)),
                "Needs a majority in both chambers: " + chance(Statecraft.tradeOdds(game, c)) + " to pass.");
        u.button(g, x0 + tw + 10, y, tw, 28, ally ? "A treaty ally" : "Sign a defense treaty", ally || war ? B_OFF : B_NORM, ally || war ? null : () -> say(Statecraft.defensePact(game, c)),
                "Needs two-thirds of the Senate: " + chance(Statecraft.treatyOdds(game, c)) + " to be ratified.");
        y += 34;
        text(g, "A trade deal: " + chance(Statecraft.tradeOdds(game, c)) + " in Congress.   A treaty: " + (w.rel[c] < 40 && !ally ? "they aren't interested yet" : chance(Statecraft.treatyOdds(game, c)) + " in the Senate") + ".", x0, y + 8, F_T, C_DIM);
        // covert action
        y += 30;
        label(g, "Covert action", x0, y, C_GOLD);
        y += 8;
        if (w.cvProg[c] > 0) {
            text(g, CV2_NAME[w.cvProg[c] - 1] + " is under way. Results by " + dateStr(w.cvDue[c] * 7) + ".", x0, y + 16, F_XS, C_ACCENT);
            y += 26;
        } else {
            double bw = (cw - 3 * 8) / 4;
            for (int k = 0; k < CV2_NAME.length; k++) {
                final int kk = k;
                double bx = x0 + k * (bw + 8);
                u.button(g, bx, y, bw, 28, CV2_NAME[k], war ? B_OFF : B_NORM, war ? null : () -> say(Statecraft.startCovert(game, c, kk)), CV2_DESC[k] + " A presidential finding; if it leaks, it's a scandal.");
                text(g, Math.round(Statecraft.covertOdds(game, c, k) * 100) + "% to work, " + Math.round(Statecraft.leakOdds(game, k) * 100) + "% to leak", bx + 2, y + 42, F_T, C_DIM);
            }
            y += 50;
        }
        // force
        y += 16;
        label(g, "Force", x0, y, C_BAD);
        y += 8;
        boolean auth = w.authorized[c] > 0 && game.week - w.authorized[c] < 52;
        u.button(g, x0, y, cw, 28, auth ? "Congress has authorized force against " + cnameIn(game, c) : "Ask Congress to authorize the use of force", auth ? B_OFF : B_NORM, auth ? null : () -> say(Wars.authorize(game, c)),
                "A vote in both chambers: " + chance(Wars.authorizeOdds(game, c)) + ". With it, the war starts with more support, and Congress can't order the troops home.");
        y += 36;
        int[] kinds = {WK_STRIKES, WK_BLOCKADE, WK_NOFLY, WK_INVASION};
        double bw = (cw - 3 * 8) / 4;
        for (int i = 0; i < kinds.length; i++) {
            final int kind = kinds[i];
            double bx = x0 + i * (bw + 8);
            String no = Wars.cannot(game, c, kind);
            u.button(g, bx, y, bw, 32, WK_NAME[kind], no == null ? B_DANGER : B_OFF, no == null ? () -> confirm(u, game, c, kind) : null, no != null ? no : WK_DESC[kind]);
            wrapMax(g, no == null ? "About " + thousands(Math.round(Wars.need(game, c, kind) * 1000)) + " troops" : "Not possible", bx + 2, y + 46, bw - 4, F_T, no == null ? C_DIM : C_FAINT, 1, 12);
        }
        y += 58;
        wrapMax(g, "What to expect: " + Wars.expect(game, c, WK_STRIKES), x0, y + 6, cw, F_T, C_DIM, 3, 12);
        standing(g, game, w, c, x0, 712, cw);
    }

    /** The relationship at a glance: trade, tariffs and sanctions, aid, American forces there, and Congress's view. */
    static void standing(Graphics2D g, Game game, World w, int c, double x0, double y, double cw) {
        label(g, "Where things stand", x0, y, C_FAINT);
        String[][] cells = {
                {"Trade", gone(game, c) ? "-" : "$" + Math.round(tradeOf(game, c)) + " bn a year"},
                {"Tariff / sanctions", TARIFF_OPT[Math.max(0, Math.min(TARIFF_OPT.length - 1, w.tariff[c]))] + " / " + LEVEL[Math.max(0, Math.min(LEVEL.length - 1, w.sanction[c]))].toLowerCase()},
                {"Aid", LEVEL[Math.max(0, Math.min(LEVEL.length - 1, w.aid[c]))]},
                {"US forces there", w.troopsIn[c] < 0.05 ? "None" : thousands(Math.round(w.troopsIn[c] * 1000))},
                {"Force authorized", Wars.authorized(game, c) ? "In force" : "None"}};
        double cwid = cw / cells.length;
        for (int i = 0; i < cells.length; i++) {
            text(g, cells[i][0], x0 + i * cwid, y + 22, F_T, C_DIM);
            textFit(g, cells[i][1], x0 + i * cwid, y + 40, cwid - 10, F_SB, Color.white);
        }
        wrapMax(g, "Odds shift with relations, your standing in Congress, and the era. Each objective's next step names what it needs.", x0, y + 64, cw, F_T, C_FAINT, 2, 12);
    }

    static void confirm(ResoluteDesk u, Game game, int c, int kind) {
        String body = WK_DESC[kind] + "\n\n" + Wars.expect(game, c, kind) + "\n\nThis can't be taken back once the first shots are fired.";
        u.modal(Wars.warName(game, c, kind) + "?", body, new String[]{"Stand down", "Give the order"}, new Runnable[]{null, () -> { say(Wars.start(game, c, kind)); SitRoom.tab = 6; }}, C_BAD);
    }
}
