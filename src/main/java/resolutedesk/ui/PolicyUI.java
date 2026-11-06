package resolutedesk.ui;

import static resolutedesk.congress.CongressRules.*;
import static resolutedesk.core.Dates.*;
import static resolutedesk.core.Fmt.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.executive.Posts.*;
import static resolutedesk.opinion.Demographics.*;
import static resolutedesk.opinion.Issues.*;
import static resolutedesk.policy.Outcomes.*;
import static resolutedesk.policy.PolicyData.*;
import static resolutedesk.ui.Draw.*;
import static resolutedesk.ui.Fonts.*;
import static resolutedesk.ui.RoomIds.*;
import static resolutedesk.ui.Theme.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import resolutedesk.ResoluteDesk;
import resolutedesk.congress.Bill;
import resolutedesk.congress.Hill;
import resolutedesk.congress.Legis;
import resolutedesk.executive.Actions;
import resolutedesk.executive.Cabinet;
import resolutedesk.model.Game;
import resolutedesk.policy.Policies;
import resolutedesk.policy.PolicyDef;
import resolutedesk.policy.PolicyState;

/** The Cabinet Room: every policy, what changing it would do, the outcomes it produces, and a map of how it all connects. */
public final class PolicyUI {
    public static int tab, area, sel = -1, target = -1;
    static String msg;
    public static final String[] TABS = {"Policies", "Outcomes", "System map"};
    static final String[] AREA_SHORT = {"Taxes", "Safety net", "Jobs", "Health", "Housing", "Energy", "Immigration", "Crime", "Education", "Rights", "Tech", "Defense"};
    /** A typical effect on each outcome, for drawing the strength of a link. */
    static final double[] FX_SCALE = {0.15, 0.08, 0.04, 1, 0.08, 1.5, 0.5, 2, 5, 12, 3, 0.5};

    private PolicyUI() {
    }

    static void draw(ResoluteDesk u, Graphics2D g, Game game) {
        if (game.pol == null) return;
        u.segmented(g, 48, 116, 420, 30, TABS, tab, i -> tab = i, null);
        text(g, game.pol.changes + (game.pol.changes == 1 ? " change" : " changes") + " so far", 490, 136, F_S, C_DIM);
        if (msg != null) textFit(g, msg, 640, 137, DW - 690, F_XS, C_GOLD);
        if (sel < 0 || !Policies.available(game, sel)) { int[] l = Policies.inArea(game, area); sel = l.length > 0 ? l[0] : 0; target = -1; }
        switch (tab) {
            case 1: outcomes(u, g, game); break;
            case 2: map(u, g, game); break;
            default: policies(u, g, game); break;
        }
    }

    /** Opens the Cabinet Room on one policy, showing the impacts of moving it to a level. */
    public static void show(ResoluteDesk u, int k, int to) {
        tab = 0;
        area = Policies.def(k).area;
        sel = k;
        target = to;
        msg = null;
        Rooms.open(u, R_POLICY);
    }

    static void select(int k) {
        sel = k;
        target = -1;
        msg = null;
    }

    // ---- the policies ----
    static void policies(ResoluteDesk u, Graphics2D g, Game game) {
        PolicyState s = game.pol;
        u.panel(g, 48, 160, 260, 652, "Areas");
        double y = 190;
        for (int a = 0; a < NAREA; a++) {
            final int aa = a;
            int[] l = Policies.inArea(game, a);
            int moved = 0;
            for (int k : l) if (s.level[k] != s.base[k]) moved++;
            u.card(g, 58, y, 240, 46, area == a, () -> { area = aa; int[] ll = Policies.inArea(game, aa); if (ll.length > 0) select(ll[0]); }, null);
            text(g, AREA_NAME[a], 70, y + 20, F_SB, Color.white);
            text(g, l.length + " policies" + (moved > 0 ? ", " + moved + " changed" : ""), 70, y + 37, F_T, moved > 0 ? C_GOLD : C_DIM);
            y += 50;
        }
        // the area's policies
        int[] l = Policies.inArea(game, area);
        u.panel(g, 320, 160, 520, 652, AREA_NAME[area]);
        y = 188;
        double h = Math.min(46, 612.0 / Math.max(1, l.length));
        for (int k : l) {
            final int kk = k;
            PolicyDef d = Policies.def(k);
            u.card(g, 330, y, 500, h - 4, sel == k, () -> select(kk), null);
            textFit(g, d.name, 342, y + 17, 300, F_SB, Color.white);
            textFit(g, d.levels[s.level[k]], 342, y + 33, 300, F_T, s.level[k] != s.base[k] ? C_GOLD : C_DIM);
            pips(g, d, s.level[k], s.base[k], -1, 660, y + 14);
            String st = Policies.inMotion(game, k) != null ? "pending" : s.level[k] != s.base[k] ? viaName(s.via[k]) : null;
            if (st != null) textR(g, st, 820, y + 33, F_T, C_ACCENT);
            if (d.signature) textR(g, "signature", 820, y + 17, F_T, C_FAINT);
            y += h;
        }
        detail(u, g, game);
    }

    static String viaName(int ch) { return ch == CH_BILL ? "by law" : ch == CH_ORDER ? "by order" : ch == CH_RULE ? "by rule" : ch == CH_BUDGET ? "by budget" : "changed"; }

    /** One dot per level: filled for the law now, a ring for the law on day one, gold for the level you're looking at. */
    static void pips(Graphics2D g, PolicyDef d, int now, int base, int tgt, double x, double y) {
        int n = d.levels.length;
        for (int i = 0; i < n; i++) {
            double cx = x + i * 16;
            g.setColor(i == now ? C_ACCENT : i == tgt ? C_GOLD : new Color(255, 255, 255, 30));
            g.fill(new Ellipse2D.Double(cx, y - 5, 10, 10));
            if (i == base && i != now) { g.setColor(C_DIM); g.setStroke(new BasicStroke(1.4f)); g.draw(new Ellipse2D.Double(cx - 2, y - 7, 14, 14)); }
        }
    }

    // ---- one policy and the impacts of changing it ----
    static void detail(ResoluteDesk u, Graphics2D g, Game game) {
        PolicyState s = game.pol;
        int k = sel;
        PolicyDef d = Policies.def(k);
        int now = s.level[k];
        if (target < 0 || target == now || target >= d.levels.length) target = now + 1 < d.levels.length ? now + 1 : now - 1;
        int steps = target - now;
        double px = 852, pw = DW - 48 - px, x0 = px + 18;
        u.panel(g, px, 160, pw, 652, null);
        text(g, d.name, x0, 194, F_H2, Color.white);
        double cx = x0 + strW(g, d.name, F_H2) + 12;
        if (d.signature) cx += chip(g, "Signature issue", cx, 180, C_GOLD, false) + 6;
        chip(g, "Evidence: " + EV_NAME[d.evidence].toLowerCase(), cx, 180, d.evidence == 0 ? C_GOOD : d.evidence == 3 ? C_AMBER : C_ACCENT, false);
        String since = s.level[k] != s.base[k] ? "  (changed " + viaName(s.via[k]) + ", " + monthYear(s.changed[k] * 7) + ")" : "";
        textFit(g, "Now: " + d.levels[now] + since, x0, 216, pw - 36, F_XS, C_TEXT);
        // the levels
        int n = d.levels.length;
        double bw = (pw - 36 - (n - 1) * 6) / n, by = 228;
        for (int i = 0; i < n; i++) {
            final int ii = i;
            int style = i == now ? B_TABON : i == target ? B_GOLD : B_NORM;
            u.button(g, x0 + i * (bw + 6), by, bw, 34, "", style, i == now ? null : () -> { target = ii; msg = null; }, d.levels[i]);
            wrapCenter(g, d.levels[i], x0 + i * (bw + 6) + bw / 2, by + 14, bw - 10, i == target ? new Color(29, 20, 5) : i == now ? Color.white : C_TEXT);
        }
        if (Math.abs(steps) > 2) text(g, "One bill can move a policy two steps at most; pick a nearer level.", x0, by + 50, F_XS, C_AMBER);
        label(g, "Real-world impacts of moving to: " + Policies.def(k).levels[target].toLowerCase(), x0, 290, C_GOLD);
        // left column: who wins and loses, the precedent, the evidence
        double lw = 300, y = 312;
        y = section(g, "Who gains", d.winners, x0, y, lw, C_GOOD);
        y = section(g, "Who pays", d.losers, x0, y, lw, C_AMBER);
        y = section(g, "Precedent", d.precedent, x0, y, lw, C_DIM);
        section(g, "How sure is this?", EV_NAME[d.evidence] + ". " + EV_DESC[d.evidence] + " The ranges show it.", x0, y, lw, C_DIM);
        // right column: what changes, and who reacts
        double rx = x0 + lw + 30, rw = px + pw - 18 - rx;
        y = 312;
        label(g, "Once it has worked through (" + lagText(d.lag) + ")", rx, y, C_FAINT);
        y += 18;
        int lines = 0;
        for (int o = 0; o < NOUT; o++) {
            if (d.fx[o] == 0 || o == O_DEFICIT) continue;
            double[] r = Policies.range(d, steps, o, 1);
            boolean good = r[1] * OC_GOOD[o] > 0;
            arrow(g, rx + 4, y - 4, r[1] > 0, good ? C_GOOD : C_BAD, 1);
            textFit(g, OC_NAME[o] + ": " + rangeText(o, r), rx + 18, y, rw - 18, F_XS, C_TEXT);
            y += 16;
            lines++;
        }
        if (d.cost != 0) {
            double[] c = Policies.costRange(d, steps, 1);
            boolean adds = c[1] > 0;
            arrow(g, rx + 4, y - 4, adds, adds ? C_BAD : C_GOOD, 1);
            textFit(g, "Deficit: " + (adds ? "adds " : "cuts it by ") + "$" + Math.round(Math.abs(c[0])) + " to $" + Math.round(Math.abs(c[2])) + " billion a year", rx + 18, y, rw - 18, F_XS, C_TEXT);
            y += 16;
            lines++;
        }
        if (lines == 0) { text(g, "Little that shows up in the numbers: this one is about values.", rx, y, F_XS, C_DIM); y += 16; }
        y += 10;
        label(g, "How voters react when it happens", rx, y, C_FAINT);
        y += 18;
        double nat = Policies.national(d, steps, 1);
        arrow(g, rx + 4, y - 4, nat >= 0, nat >= 0 ? C_GOOD : C_BAD, Math.abs(nat) >= 3 ? 2 : 1);
        text(g, "The country: " + signed1(nat) + " points at first, fading over months", rx + 18 + (Math.abs(nat) >= 3 ? 10 : 0), y, F_XS, Color.white);
        y += 16;
        int[] order = topGroups(d, steps);
        for (int i = 0; i < order.length && i < 7; i++) {
            int gr = order[i];
            double v = Policies.groupEffect(d, gr, steps, 1);
            int arrows = Math.abs(v) >= 6 ? 3 : Math.abs(v) >= 3 ? 2 : 1;
            arrow(g, rx + 4, y - 4, v > 0, v > 0 ? C_GOOD : C_BAD, arrows);
            text(g, GROUP[gr] + ": " + signed1(v), rx + 18 + (arrows - 1) * 10, y, F_XS, C_TEXT);
            y += 16;
        }
        String party = d.leanFor(steps) < 0 ? "Neither party owns this one." : d.leanFor(steps) == game.party ? "Your party wants this." : "This is the other party's idea; your base won't like it.";
        text(g, party, rx, y + 4, F_T, d.leanFor(steps) == game.party || d.leanFor(steps) < 0 ? C_DIM : C_AMBER);
        channels(u, g, game, k, target, x0, pw - 36);
    }

    /** The ways to make the change, each with its odds, timing and strength. */
    static void channels(ResoluteDesk u, Graphics2D g, Game game, int k, int to, double x0, double w) {
        PolicyDef d = Policies.def(k);
        double y = 716;
        label(g, "Make the change", x0, y, C_GOLD);
        y += 10;
        double bw = (w - 3 * 8) / 4;
        String[] lab = {"Send Congress a bill", "Sign an executive order", "Propose an agency rule", "Put it in the budget"};
        for (int ch = 0; ch < NCH; ch++) {
            final int c = ch;
            double bx = x0 + ch * (bw + 8);
            String no = Policies.cannot(game, k, to, ch);
            u.button(g, bx, y, bw, 32, lab[ch], no == null ? (ch == CH_BILL ? B_PRIMARY : B_NORM) : B_OFF, no == null ? () -> { msg = Policies.propose(game, k, to, c); target = -1; } : null,
                    no != null ? no : CH_NAME[ch] + ".");
            String fact = !d.can(ch) ? "Not possible for this policy" : fact(game, k, to, ch);
            wrap(g, fact, bx + 2, y + 48, bw - 4, F_T, d.can(ch) ? C_DIM : C_FAINT);
        }
    }

    static String fact(Game game, int k, int to, int ch) {
        PolicyDef d = Policies.def(k);
        switch (ch) {
            case CH_BILL: {
                if (Math.abs(to - game.pol.level[k]) > 2) return "A bill moves it two steps at most.";
                Bill b = Policies.bill(game, k, to);
                b.recon = b.def().budget && Legis.reconOk(game, b);
                return "House about " + Math.round(Legis.chance(game, b, HOUSE, false) * 100) + "%, Senate " + Math.round(Legis.chance(game, b, SENATE, false) * 100) + "%"
                        + (b.recon ? " (by reconciliation)" : "") + ". Full strength; hard to undo.";
            }
            case CH_ORDER: return "At once, at 60% strength. Court risk about " + Math.round(Actions.legal(game, d.legal) * 100) + "%; a successor can undo it.";
            case CH_RULE: return "After about " + Math.round(14 - 6 * Cabinet.pq(game, P_OMB)) + " weeks of comment, at 85%. Court risk " + Math.round(Actions.legal(game, d.legal) * 100) + "%.";
            default: {
                int[] dt = dateOf(game.day());
                int y = dt[1] >= 9 ? dt[0] + 1 : dt[0];
                return "On October 1, " + y + ", if Congress funds it: " + (Hill.majority(game, HOUSE, game.party) ? "likely" : "a fight") + ".";
            }
        }
    }

    static double section(Graphics2D g, String title, String body, double x, double y, double w, Color c) {
        label(g, title, x, y, c);
        return y + 14 + wrap(g, body, x, y + 14, w, F_XS, C_TEXT) + 10;
    }

    static String lagText(int weeks) {
        return weeks <= 13 ? "within months" : weeks <= 30 ? "about six months" : weeks <= 60 ? "about a year" : weeks <= 120 ? "about two years" : weeks <= 210 ? "three or four years" : "five years or more";
    }

    /** An estimate's range in the outcome's units: "-1.9 to -3.1 points", "-8% to -16%". */
    static String rangeText(int o, double[] r) {
        double a = r[0], b = r[2];
        if (Math.abs(a) > Math.abs(b)) { double t = a; a = b; b = t; }
        if (OC_PCT[o]) return pct(a) + " to " + pct(b);
        if (o == O_GAS) return money2(a) + " to " + money2(b) + " a gallon";
        return signed(a) + " to " + signed(b) + " points";
    }

    static String pct(double v) { return (v > 0 ? "+" : v < 0 ? "-" : "") + Math.round(Math.abs(v)) + "%"; }

    static String signed(double v) { return (v > 0 ? "+" : v < 0 ? "-" : "") + (Math.abs(v) < 0.1 ? fmt2(Math.abs(v)) : fmt1(Math.abs(v))); }

    static String money2(double v) { return (v > 0 ? "+$" : "-$") + fmt2(Math.abs(v)); }

    /** The voter groups a change moves most. */
    static int[] topGroups(PolicyDef d, int steps) {
        int n = 0;
        for (int gr = 0; gr < NG; gr++) if (d.groups[gr] != 0) n++;
        int[] l = new int[n];
        n = 0;
        for (int gr = 0; gr < NG; gr++) if (d.groups[gr] != 0) l[n++] = gr;
        for (int i = 1; i < n; i++) for (int j = i; j > 0 && Math.abs(d.groups[l[j]]) > Math.abs(d.groups[l[j - 1]]); j--) { int t = l[j]; l[j] = l[j - 1]; l[j - 1] = t; }
        return l;
    }

    /** One to three small arrows, up or down. */
    static void arrow(Graphics2D g, double x, double y, boolean up, Color c, int n) {
        g.setColor(c);
        for (int i = 0; i < n; i++) {
            double ax = x + i * 10;
            Path2D p = new Path2D.Double();
            if (up) { p.moveTo(ax, y + 4); p.lineTo(ax + 4, y - 3); p.lineTo(ax + 8, y + 4); }
            else { p.moveTo(ax, y - 3); p.lineTo(ax + 4, y + 4); p.lineTo(ax + 8, y - 3); }
            p.closePath();
            g.fill(p);
        }
    }

    static void wrapCenter(Graphics2D g, String s, double cx, double y, double w, Color c) {
        g.setFont(F_T);
        String[] words = s.split(" ");
        String l1 = "", l2 = "";
        for (String wd : words) {
            String t = l1.isEmpty() ? wd : l1 + " " + wd;
            if (l2.isEmpty() && strW(g, t, F_T) <= w) l1 = t;
            else l2 = l2.isEmpty() ? wd : l2 + " " + wd;
        }
        if (l2.isEmpty()) textC(g, l1, cx, y + 5, F_T, c);
        else { textC(g, l1, cx, y, F_T, c); textC(g, fitTo(g, l2, w), cx, y + 12, F_T, c); }
    }

    static String fitTo(Graphics2D g, String s, double w) {
        if (strW(g, s, F_T) <= w) return s;
        while (s.length() > 1 && strW(g, s + "...", F_T) > w) s = s.substring(0, s.length() - 1);
        return s + "...";
    }

    // ---- the outcomes ----
    static void outcomes(ResoluteDesk u, Graphics2D g, Game game) {
        PolicyState s = game.pol;
        double cw = (DW - 96 - 3 * 12) / 4.0, ch = (652 - 2 * 12) / 3.0;
        for (int o = 0; o < NOUT; o++) {
            double x = 48 + (o % 4) * (cw + 12), y = 160 + (o / 4) * (ch + 12);
            u.panel(g, x, y, cw, ch, OC_NAME[o]);
            double v = s.out[o], v0 = s.out0[o], dv = v - v0;
            text(g, outShow(o, v), x + 14, y + 62, F_NUMXL, Color.white);
            text(g, OC_UNIT[o].startsWith("%") || OC_UNIT[o].equals("index") ? "" : OC_UNIT[o], x + 18 + strW(g, outShow(o, v), F_NUMXL), y + 62, F_XS, C_DIM);
            boolean better = dv * OC_GOOD[o] > 0;
            double rel = OC_PCT[o] && v0 != 0 ? dv / v0 * 100 : dv;
            String since = OC_PCT[o] ? Math.round(Math.abs(rel)) + "%" : o == O_GAS ? "$" + fmt2(Math.abs(dv)) : fmt1(Math.abs(dv)) + " points";
            if (Math.abs(rel) >= (OC_PCT[o] ? 0.5 : 0.05)) {
                arrow(g, x + 14, y + 82, dv > 0, better ? C_GOOD : C_BAD, 1);
                text(g, (dv > 0 ? "Up " : "Down ") + since + " since you took office", x + 30, y + 86, F_XS, better ? C_GOOD : C_BAD);
            } else text(g, "About where it was when you took office", x + 14, y + 86, F_XS, C_DIM);
            spark(g, s.hist, o + 1, x + 14, y + 98, cw - 28, 46, o == O_GROWTH ? C_ACCENT : better || Math.abs(rel) < 0.5 ? C_ACCENT : C_AMBER);
            // what moved it
            int a = -1, b = -1;
            double av = 0, bv = 0;
            for (int k = 0; k < Policies.count(); k++) {
                double c = Math.abs(Policies.contribution(game, k, o));
                if (c > av) { b = a; bv = av; a = k; av = c; } else if (c > bv) { b = k; bv = c; }
            }
            double ly = y + 162;
            if (a < 0 || av < FX_SCALE[o] * 0.05) wrap(g, outFromEconomy(o) ? OC_DESC[o] + " Set by the economy; policies push it at the edges." : OC_DESC[o] + " No policy change has moved it yet.", x + 14, ly, cw - 28, F_T, C_DIM);
            else {
                text(g, "Moved most by", x + 14, ly, F_T, C_FAINT);
                textFit(g, Policies.def(a).name, x + 14, ly + 14, cw - 28, F_XS, C_TEXT);
                if (b >= 0 && bv >= FX_SCALE[o] * 0.05) textFit(g, Policies.def(b).name, x + 14, ly + 28, cw - 28, F_XS, C_TEXT);
            }
        }
    }

    static void spark(Graphics2D g, List<double[]> hist, int col, double x, double y, double w, double h, Color c) {
        if (hist.size() < 2) return;
        double lo = 1e9, hi = -1e9;
        for (double[] r : hist) { lo = Math.min(lo, r[col]); hi = Math.max(hi, r[col]); }
        if (hi - lo < 1e-6) { hi += 1; lo -= 1; }
        g.setColor(new Color(255, 255, 255, 14));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 6, 6));
        Path2D p = new Path2D.Double();
        for (int i = 0; i < hist.size(); i++) {
            double px = x + 4 + (w - 8) * i / (hist.size() - 1.0), py = y + h - 4 - (h - 8) * (hist.get(i)[col] - lo) / (hi - lo);
            if (i == 0) p.moveTo(px, py); else p.lineTo(px, py);
        }
        g.setColor(c);
        g.setStroke(new BasicStroke(1.6f));
        g.draw(p);
        g.setStroke(new BasicStroke(1f));
    }

    // ---- the system map ----
    static void map(ResoluteDesk u, Graphics2D g, Game game) {
        PolicyState s = game.pol;
        double ax = 48;
        for (int a = 0; a < NAREA; a++) {
            final int aa = a;
            double w = strW(g, AREA_SHORT[a], F_XSB) + 22;
            u.button(g, ax, 160, w, 26, AREA_SHORT[a], area == a ? B_TABON : B_TAB, () -> { area = aa; int[] l = Policies.inArea(game, aa); if (l.length > 0) select(l[0]); });
            ax += w + 4;
        }
        u.panel(g, 48, 196, DW - 96, 616, null);
        int[] l = Policies.inArea(game, area);
        double pxL = 70, pw = 250, outX = 700, outW = 200, grX = 1290;
        double py0 = 222, ph = Math.min(44, 570.0 / Math.max(1, l.length));
        double oy0 = 222, oh = 570.0 / NOUT;
        double gy0 = 218, gh = 576.0 / NG;
        label(g, "Policies", pxL, 214, C_FAINT);
        label(g, "Outcomes", outX, 214, C_FAINT);
        label(g, "Voter groups and their approval", grX, 214, C_FAINT);
        // which policy is in focus: the one under the mouse, or the selected one
        int focus = -1;
        for (int i = 0; i < l.length; i++) if (u.hover(pxL, py0 + i * ph, pw, ph - 6)) focus = l[i];
        int hoverO = -1;
        for (int o = 0; o < NOUT; o++) if (u.hover(outX, oy0 + o * oh, outW, oh - 6)) hoverO = o;
        if (focus < 0 && hoverO < 0) focus = sel;
        // links: policy to outcome
        for (int i = 0; i < l.length; i++) {
            PolicyDef d = Policies.def(l[i]);
            double sy = py0 + i * ph + (ph - 6) / 2;
            for (int o = 0; o < NOUT; o++) {
                if (d.fx[o] == 0 && !(o == O_DEFICIT && d.cost != 0)) continue;
                double mag = o == O_DEFICIT ? d.cost / 60 : d.fx[o] / FX_SCALE[o];
                boolean good = o == O_DEFICIT ? d.cost < 0 : d.fx[o] * OC_GOOD[o] > 0;
                boolean lit = focus == l[i] || hoverO == o;
                link(g, pxL + pw, sy, outX, oy0 + o * oh + (oh - 6) / 2, good, mag, lit, focus >= 0 || hoverO >= 0);
            }
        }
        // links: outcome to the issue voters judge it by
        double isX = 1010, isW = 200, iy0 = 222, ih = 570.0 / NPI;
        for (int[] e : OUTCOME_ISSUE) {
            boolean lit = hoverO == e[0] || focus >= 0 && touches(focus, e[0]);
            Color c = new Color(124, 196, 255, lit ? 170 : 40);
            g.setColor(c);
            g.setStroke(new BasicStroke(lit ? 2f : 1.2f));
            double y1 = oy0 + e[0] * oh + (oh - 6) / 2, y2 = iy0 + e[1] * ih + (ih - 6) / 2, dx = (isX - outX - outW) * 0.45;
            Path2D p = new Path2D.Double();
            p.moveTo(outX + outW, y1);
            p.curveTo(outX + outW + dx, y1, isX - dx, y2, isX, y2);
            g.draw(p);
            g.setStroke(new BasicStroke(1f));
        }
        label(g, "Issues, and your approval on each", isX, 214, C_FAINT);
        for (int i = 0; i < NPI; i++) {
            double y = iy0 + i * ih;
            g.setColor(new Color(22, 34, 56, 245));
            g.fill(new RoundRectangle2D.Double(isX, y, isW, ih - 6, 10, 10));
            textFit(g, PISSUE[i], isX + 10, y + 15, isW - 60, F_XS, C_TEXT);
            textR(g, Math.round(game.issueApp[i]) + "%", isX + isW - 10, y + 15, F_XSB, game.issueApp[i] >= 50 ? C_GOOD : C_AMBER);
            bar(g, isX + 10, y + 21, isW - 20, 4, game.issueApp[i] / 100, game.issueApp[i] >= 50 ? C_GOOD : C_AMBER);
        }
        // nodes
        for (int i = 0; i < l.length; i++) {
            final int k = l[i];
            PolicyDef d = Policies.def(k);
            double y = py0 + i * ph;
            u.card(g, pxL, y, pw, ph - 6, k == sel, () -> select(k), d.levels[s.level[k]]);
            textFit(g, d.name, pxL + 10, y + 16, pw - 20, F_XSB, k == focus ? Color.white : C_TEXT);
            double f = d.levels.length > 1 ? s.level[k] / (d.levels.length - 1.0) : 0;
            bar(g, pxL + 10, y + 24, pw - 20, 5, f, s.level[k] != s.base[k] ? C_GOLD : C_ACCENT);
        }
        for (int o = 0; o < NOUT; o++) {
            double y = oy0 + o * oh, dv = s.out[o] - s.out0[o];
            boolean better = dv * OC_GOOD[o] > 0, moved = Math.abs(OC_PCT[o] && s.out0[o] != 0 ? dv / s.out0[o] * 100 : dv) >= (OC_PCT[o] ? 0.5 : 0.05);
            g.setColor(new Color(22, 34, 56, 245));
            g.fill(new RoundRectangle2D.Double(outX, y, outW, oh - 6, 18, 18));
            g.setColor(o == hoverO ? C_ACCENT : new Color(255, 255, 255, 30));
            g.draw(new RoundRectangle2D.Double(outX, y, outW, oh - 6, 18, 18));
            text(g, OC_NAME[o], outX + 12, y + 17, F_XSB, Color.white);
            textR(g, outShow(o, s.out[o]), outX + outW - 12, y + 17, F_XS, C_TEXT);
            if (moved) arrow(g, outX + outW - 22, y + 30, dv > 0, better ? C_GOOD : C_BAD, 1);
            u.hit(outX, y, outW, oh - 6, null, OC_DESC[o]);
        }
        // the voter groups: today's approval, and how the policy in focus would move each of them
        PolicyDef fd = focus >= 0 ? Policies.def(focus) : null;
        for (int gr = 0; gr < NG; gr++) {
            double y = gy0 + gr * gh, v = fd != null ? Policies.groupEffect(fd, gr, 1, 1) : 0;
            boolean on = v != 0;
            if (on) arrow(g, grX - 34, y + gh / 2, v > 0, v > 0 ? C_GOOD : C_BAD, Math.abs(v) >= 6 ? 3 : Math.abs(v) >= 3 ? 2 : 1);
            text(g, GROUP[gr], grX, y + gh / 2 + 4, F_T, on ? Color.white : C_DIM);
            bar(g, grX + 170, y + gh / 2 - 2, 70, 5, game.groupApp[gr] / 100, game.groupApp[gr] >= 50 ? C_GOOD : C_AMBER);
        }
        text(g, "Lines: green when a step up makes that outcome better, red when worse; thicker is bigger. Arrows: how each group would react. Point at a policy or an outcome to trace it.", 70, 800, F_T, C_DIM);
    }

    /** Which issue each outcome is judged by: {outcome, issue}. */
    static final int[][] OUTCOME_ISSUE = {{O_UNEMP, PI_JOBS}, {O_UNEMP, PI_ECON}, {O_INFL, PI_PRICES}, {O_GROWTH, PI_ECON}, {O_DEFICIT, PI_ECON}, {O_GAS, PI_PRICES},
            {O_UNINS, PI_HEALTH}, {O_POV, PI_PRICES}, {O_CRIME, PI_CRIME}, {O_OD, PI_CRIME}, {O_BORDER, PI_IMMIG}, {O_EMIS, PI_ENERGY}, {O_SCORES, PI_EDU}};

    static boolean touches(int k, int o) { PolicyDef d = Policies.def(k); return o == O_DEFICIT ? d.cost != 0 : d.fx[o] != 0; }

    static void link(Graphics2D g, double x1, double y1, double x2, double y2, boolean good, double mag, boolean lit, boolean dimOthers) {
        double m = clamp(Math.abs(mag), 0.15, 3);
        Color c = good ? C_GOOD : C_BAD;
        int alpha = lit ? 200 : dimOthers ? 18 : 70;
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha));
        g.setStroke(new BasicStroke((float) (0.8 + 1.6 * m), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        double dx = (x2 - x1) * 0.45;
        Path2D p = new Path2D.Double();
        p.moveTo(x1, y1);
        p.curveTo(x1 + dx, y1, x2 - dx, y2, x2, y2);
        g.draw(p);
        g.setStroke(new BasicStroke(1f));
    }
}
