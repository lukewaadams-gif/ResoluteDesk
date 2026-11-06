package resolutedesk.ui;

import static resolutedesk.core.Dates.*;
import static resolutedesk.core.Fmt.*;
import static resolutedesk.data.StateData.*;
import static resolutedesk.model.Tracking.*;
import static resolutedesk.opinion.Issues.*;
import static resolutedesk.sim.Schedule.*;
import static resolutedesk.stories.StoryData.*;
import static resolutedesk.ui.Draw.*;
import static resolutedesk.ui.Fonts.*;
import static resolutedesk.ui.ScreenIds.*;
import static resolutedesk.ui.Theme.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import resolutedesk.ResoluteDesk;
import resolutedesk.model.Econ;
import resolutedesk.model.Game;
import resolutedesk.model.News;
import resolutedesk.sim.Week;
import resolutedesk.stories.Storyline;
import resolutedesk.stories.StorylineDef;
import resolutedesk.stories.Storylines;

/** The morning paper: the front page that sums up each week. */
public final class Paper {
    /** This week's stories, biggest first, with the week's biggest Washington story leading if there is one. */
    static List<News> stories(Game g) {
        List<News> l = new ArrayList<News>();
        int from = g.day() - 7;
        for (News n : g.news) if (n.day >= from) l.add(n);
        Collections.sort(l, (a, b) -> Double.compare(b.weight, a.weight));
        for (int i = 0; i < l.size(); i++) if (l.get(i).section == null) { if (i > 0) l.add(0, l.remove(i)); break; }
        return l;
    }

    public static void draw(ResoluteDesk u, Graphics2D g) {
        Game game = u.game;
        if (game == null) { u.go(SC_HOME); return; }
        g.setColor(rgb(0x20262f));
        g.fill(new Rectangle2D.Double(0, 0, DW, DH));
        double px = 260, pw = DW - 520, py = 30, ph = DH - 100;
        g.setColor(new Color(0, 0, 0, 90));
        g.fill(new Rectangle2D.Double(px + 8, py + 10, pw, ph));
        g.setColor(C_PAPER);
        g.fill(new Rectangle2D.Double(px, py, pw, ph));
        // masthead
        Font mast = new Font(SERIF, Font.BOLD, 54);
        textC(g, "The Capital Ledger", px + pw / 2, py + 72, mast, C_INK);
        g.setColor(C_INK);
        g.fill(new Rectangle2D.Double(px + 30, py + 88, pw - 60, 2));
        g.fill(new Rectangle2D.Double(px + 30, py + 116, pw - 60, 1));
        text(g, dateLong(game.day()).toUpperCase(), px + 34, py + 108, F_TB, C_INK2);
        textR(g, "WASHINGTON EDITION", px + pw - 34, py + 108, F_TB, C_INK2);
        textC(g, "Week " + (game.week + 1) + " of the " + game.pres.last + " administration", px + pw / 2, py + 108, F_TB, C_INK2);
        List<News> st = stories(game);
        News lead = st.isEmpty() ? null : st.get(0);
        double cy = py + 130;
        Font head = new Font(SERIF, Font.BOLD, 40);
        String h = lead == null ? "A quiet week in Washington" : lead.head;
        double hh = wrap(g, cap(h), px + 34, cy + 38, pw - 68, head, C_INK);
        cy += hh + 20;
        // lead story: a photo of the president and the body
        double photoW = 230, photoH = 270;
        g.setColor(rgb(0xd8d2c4));
        g.fill(new Rectangle2D.Double(px + 34, cy, photoW, photoH));
        Portrait.draw(g, game.pres, px + 44, cy + 10, photoW - 20, photoH - 50, u.tsec, 0, true, false);
        text(g, "President " + game.pres.last + (game.location != Week.dc() ? " in " + STATES[game.location].name : " at the White House"), px + 40, cy + photoH - 14, F_XS, C_INK2);
        String body = (lead != null && lead.body != null ? lead.body + "\n\n" : "") + leadBody(game, lead);
        wrap(g, body, px + 34 + photoW + 20, cy + 16, pw - 68 - photoW - 20, new Font(SERIF, Font.PLAIN, 15), C_INK);
        cy += photoH + 24;
        g.setColor(C_INK);
        g.fill(new Rectangle2D.Double(px + 30, cy - 10, pw - 60, 1));
        // other stories in three columns
        double colW = (pw - 68 - 40) / 3;
        for (int i = 1; i < Math.min(st.size(), 7); i++) {
            int c = (i - 1) % 3;
            double x = px + 34 + c * (colW + 20), y = cy + ((i - 1) / 3) * 92;
            News n = st.get(i);
            if (n.section != null) { text(g, n.section.toUpperCase(), x, y + 2, F_TB, C_INK2); y += 12; }
            double yy = y + 4 + wrap(g, cap(n.head), x, y + 16, colW, new Font(SERIF, Font.BOLD, 16), C_INK);
            if (n.body != null) wrap(g, n.body, x, yy + 6, colW, new Font(SERIF, Font.PLAIN, 12), C_INK2);
        }
        int used = Math.min(st.size(), 7) - 1;
        String[][] feat = features(game);
        for (int k = 0; used + k < 6 && k < feat.length; k++) {
            int i = used + k;
            int c = i % 3;
            double x = px + 34 + c * (colW + 20), y = cy + (i / 3) * 92;
            double yy = y + 4 + wrap(g, feat[k][0], x, y + 16, colW, new Font(SERIF, Font.BOLD, 16), C_INK);
            wrap(g, feat[k][1], x, yy + 6, colW, new Font(SERIF, Font.PLAIN, 12), C_INK2);
        }
        // everything else, in brief
        if (st.size() > 7) {
            double by = cy + 2 * 92 + 8;
            g.setColor(C_INK);
            g.fill(new Rectangle2D.Double(px + 30, by - 4, pw - 60, 1));
            text(g, "IN BRIEF", px + 34, by + 12, F_TB, C_INK2);
            Font brief = new Font(SERIF, Font.PLAIN, 13);
            for (int i = 7; i < Math.min(st.size(), 15); i++) {
                int k = i - 7;
                double bx = px + 34 + (k % 2) * ((pw - 68) / 2), yb = by + 30 + (k / 2) * 18;
                if (yb > py + ph - 12) break;
                News n = st.get(i);
                textFit(g, (n.section != null ? n.section + ": " : "") + cap(n.head), bx, yb, (pw - 68) / 2 - 16, brief, C_INK);
            }
        }
        // the side panel: numbers
        double sx = px + pw + 24, sw = DW - sx - 24;
        g.setColor(new Color(255, 255, 255, 12));
        g.fill(new RoundRectangle2D.Double(sx, py, sw, 330, 10, 10));
        label(g, "BY THE NUMBERS", sx + 14, py + 26, C_GOLD);
        Econ e = game.econ;
        String[][] rows = {{"Approval", Math.round(game.approval) + "%"}, {"Unemployment", fmt1(e.unemp) + "%"}, {"Inflation", fmt1(e.infl) + "%"}, {"S&P 500", thousands(Math.round(e.stocks))},
                {"Gas", "$" + fmt2(e.gas)}, {"Fed funds", fmt2(e.fed) + "%"}, {"Your stamina", String.valueOf(Math.round(game.stamina))}};
        double y = py + 56;
        for (String[] r : rows) { text(g, r[0], sx + 14, y, F_XS, C_TEXT); textR(g, r[1], sx + sw - 14, y, F_SB, Color.white); y += 28; }
        developing(g, game, 24, py, px - 48);
        u.button(g, DW / 2 - 160, DH - 58, 320, 44, "To the Oval Office", B_GOLD, () -> u.go(SC_OVAL), "Space");
    }

    /** The storylines under way, beside the paper: each one's stage and how hot it is, and how the last ones ended. */
    static void developing(Graphics2D g, Game game, double x, double y, double w) {
        if (game.stories == null) return;
        List<Storyline> act = Storylines.active(game);
        int ended = 0;
        for (Storyline l : game.stories.lines) if (l.status != SL_ACTIVE) ended++;
        g.setColor(new Color(255, 255, 255, 12));
        g.fill(new RoundRectangle2D.Double(x, y, w, 70 + (act.isEmpty() ? 52 : act.size() * 84) + (ended > 0 ? 30 + Math.min(4, ended) * 40 : 0), 10, 10));
        label(g, "DEVELOPING STORIES", x + 14, y + 26, C_GOLD);
        double yy = y + 52;
        if (act.isEmpty()) { wrap(g, "Nothing is building. Storylines start small, and they come back to you at every stage.", x + 14, yy, w - 28, F_XS, C_DIM); yy += 52; }
        for (Storyline l : act) {
            StorylineDef d = Storylines.def(l);
            yy += wrap(g, d.name, x + 14, yy, w - 28, F_SB, Color.white) + 2;
            for (int i = 0; i < d.stages(); i++) {
                g.setColor(i < l.stage ? C_BAD : i == l.stage ? C_GOLD : new Color(255, 255, 255, 50));
                g.fill(new RoundRectangle2D.Double(x + 14 + i * 22, yy, 16, 6, 3, 3));
            }
            text(g, "Stage " + (l.stage + 1) + " of " + d.stages(), x + 14 + d.stages() * 22 + 6, yy + 7, F_T, C_DIM);
            yy += 18;
            bar(g, x + 14, yy, w - 28, 8, l.heat / 100, l.heat >= SL_GROW ? C_BAD : l.heat >= 45 ? C_AMBER : C_GOOD);
            text(g, l.heat >= SL_GROW ? "Growing" : l.heat >= 45 ? "Holding" : "Cooling", x + 14, yy + 20, F_T, C_DIM);
            yy += 40;
        }
        int shown = 0;
        for (int i = game.stories.lines.size() - 1; i >= 0 && shown < 4; i--) {
            Storyline l = game.stories.lines.get(i);
            if (l.status == SL_ACTIVE) continue;
            if (shown++ == 0) { label(g, "LATELY", x + 14, yy + 8, C_FAINT); yy += 22; }
            yy += wrap(g, Storylines.def(l).name + ": " + SL_STATUS[l.status].toLowerCase(), x + 14, yy, w - 28, F_XS, l.status == SL_RUNAWAY ? C_BAD : C_DIM) + 6;
        }
    }

    /** Regular features that fill the page: the week ahead, poll watch, markets and an opinion column. */
    static String[][] features(Game g) {
        Econ e = g.econ;
        StringBuilder trips = new StringBuilder();
        int nt = 0;
        for (int i = 0; i < NSLOT; i++) if (isTrip(g.act[i]) && g.arg[i] >= 0) { trips.append(nt++ > 0 ? ", " : "").append(STATES[g.arg[i]].name); }
        int top = Week.topIssue(g);
        String ahead = (nt > 0 ? "The president travels to " + trips + ". " : "No travel is planned. ") + "Policy meetings focus on " + PISSUE[top].toLowerCase() + ".";
        double prev = g.hist.size() >= 5 ? g.hist.get(g.hist.size() - 5)[H_APP] : g.approval0;
        String trend = g.approval > prev + 1.5 ? "rising" : g.approval < prev - 1.5 ? "slipping" : "steady";
        String polls = "Approval is " + trend + " at " + Math.round(g.approval) + "%. Among the issues voters care about most, the president scores best on " + PISSUE[bestIssue(g)].toLowerCase()
                + " and worst on " + PISSUE[worstIssue(g)].toLowerCase() + ".";
        double wk = g.hist.size() >= 2 ? (e.stocks / g.hist.get(g.hist.size() - 2)[H_STOCKS] - 1) * 100 : 0;
        String markets = "Stocks " + (wk >= 0 ? "rose " : "fell ") + fmt1(Math.abs(wk)) + "% last week. The 10-year Treasury yields " + fmt2(e.tenYr) + "% and oil trades at $" + fmt0(e.oil) + ".";
        String opTitle = g.approval > prev + 1.5 ? "Opinion: The president finds a groove" : g.approval < prev - 1.5 ? "Opinion: Is the honeymoon over?" : g.econ.infl > 4 ? "Opinion: Prices are the whole story" : "Opinion: The long game in Washington";
        String op = g.approval < 42 ? "Allies on the Hill are getting nervous, and it shows." : g.approval > 52 ? "For now the public is giving the White House the benefit of the doubt." : "Neither a crisis nor a triumph: the country is still making up its mind.";
        return new String[][]{{"The week ahead", ahead}, {"Poll watch", polls}, {"Markets", markets}, {opTitle, op}};
    }

    static int bestIssue(Game g) { int b = 0; for (int i = 1; i < NPI; i++) if (g.issueSal[i] > 0.3 && g.issueApp[i] > g.issueApp[b]) b = i; return b; }

    static int worstIssue(Game g) { int b = 0; for (int i = 1; i < NPI; i++) if (g.issueSal[i] > 0.3 && g.issueApp[i] < g.issueApp[b]) b = i; return b; }

    static String cap(String s) { return s == null || s.isEmpty() ? "" : Character.toUpperCase(s.charAt(0)) + s.substring(1); }

    static String leadBody(Game g, News lead) {
        Econ e = g.econ;
        return "President " + g.pres.last + " begins the week with " + Math.round(g.approval) + " percent of Americans approving of the job, according to the latest polling average. "
                + "Unemployment stands at " + fmt1(e.unemp) + " percent and prices are up " + fmt1(e.infl) + " percent from a year ago. "
                + (g.stamina < 35 ? "Aides privately worry about the president's punishing schedule." : "Aides say the president's schedule this week focuses on " + PISSUE[Week.topIssue(g)].toLowerCase() + ".");
    }
}
