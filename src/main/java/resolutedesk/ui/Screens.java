package resolutedesk.ui;

import static resolutedesk.core.Dates.*;
import static resolutedesk.core.Fmt.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.data.Candidates.*;
import static resolutedesk.data.Eras.*;
import static resolutedesk.executive.CourtHistory.*;
import static resolutedesk.model.Attributes.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.sim.Rosters.*;
import static resolutedesk.ui.Draw.*;
import static resolutedesk.ui.Fonts.*;
import static resolutedesk.ui.ScreenIds.*;
import static resolutedesk.ui.Theme.*;
import static resolutedesk.world.Countries.*;
import static resolutedesk.world.WarData.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import resolutedesk.ResoluteDesk;
import resolutedesk.io.BgImport;
import resolutedesk.io.SaveGame;
import resolutedesk.model.Cand;
import resolutedesk.model.Era;
import resolutedesk.model.Game;
import resolutedesk.model.Settings;
import resolutedesk.sim.Setup;

/** The screens outside a presidency: the title screen, setup, choosing a president and a running mate, and the oath. */
public final class Screens {
    public static int pickParty = DEM, sel = -1, vpSel = -1;
    static String status;

    public static void header(ResoluteDesk u, Graphics2D g, String title, String sub) {
        u.backdrop(g);
        text(g, title, 48, 62, F_DISPLAY, Color.white);
        if (sub != null) text(g, sub, 50, 88, F_B, C_DIM);
        g.setColor(new Color(255, 255, 255, 20));
        g.draw(new Line2D.Double(48, 104, DW - 48, 104));
    }

    public static void back(ResoluteDesk u) {
        switch (u.screen) {
            case SC_SETUP: u.go(SC_HOME); break;
            case SC_PICK: u.go(SC_SETUP); break;
            case SC_CREATE: u.go(SC_PICK); break;
            case SC_VP: u.go(SC_PICK); break;
            default: u.go(SC_HOME);
        }
    }

    /** The North Portico at night. */
    static void portico(Graphics2D g, double cx, double base, double sc, double t) {
        double w = 620 * sc, h = 150 * sc, x = cx - w / 2, y = base - h;
        g.setColor(new Color(10, 16, 30));
        g.fill(new Rectangle2D.Double(x - 60 * sc, base, w + 120 * sc, 14 * sc));
        g.setPaint(new GradientPaint((float) x, (float) y, rgb(0xe9e1cf), (float) x, (float) base, rgb(0xb9b2a2)));
        g.fill(new Rectangle2D.Double(x, y, w, h));
        // windows, lit
        for (int row = 0; row < 2; row++) for (int i = 0; i < 9; i++) {
            double wx = x + 24 * sc + i * 66 * sc, wy = y + 22 * sc + row * 64 * sc;
            if (wx + 26 * sc > cx - 140 * sc && wx < cx + 140 * sc) continue;
            double fl = 0.75 + 0.25 * Math.sin(t * 0.7 + i * 1.3 + row);
            g.setColor(new Color(255, (int) (196 + 30 * fl), (int) (110 + 40 * fl)));
            g.fill(new Rectangle2D.Double(wx, wy, 26 * sc, 38 * sc));
            g.setColor(new Color(120, 110, 90));
            g.draw(new Line2D.Double(wx + 13 * sc, wy, wx + 13 * sc, wy + 38 * sc));
        }
        // the portico: columns and a pediment
        double px = cx - 120 * sc, pw = 240 * sc, py = y - 34 * sc;
        g.setColor(rgb(0xefe8d8));
        Path2D ped = new Path2D.Double();
        ped.moveTo(px - 14 * sc, py + 26 * sc); ped.lineTo(cx, py - 34 * sc); ped.lineTo(px + pw + 14 * sc, py + 26 * sc); ped.closePath();
        g.fill(ped);
        g.fill(new Rectangle2D.Double(px - 14 * sc, py + 26 * sc, pw + 28 * sc, 16 * sc));
        g.setColor(rgb(0xfff6d6));
        g.fill(new Rectangle2D.Double(px, py + 42 * sc, pw, h + 34 * sc - 42 * sc));
        for (int i = 0; i < 4; i++) {
            double colx = px + 12 * sc + i * (pw - 36 * sc) / 3;
            g.setPaint(new GradientPaint((float) colx, 0, rgb(0xd8d0bd), (float) (colx + 12 * sc), 0, rgb(0xffffff)));
            g.fill(new Rectangle2D.Double(colx, py + 42 * sc, 13 * sc, base - py - 42 * sc));
        }
        g.setColor(rgb(0xffd27a));
        g.fill(new Rectangle2D.Double(cx - 18 * sc, base - 60 * sc, 36 * sc, 60 * sc));
        // the flag on the roof
        double fx = cx, fy = py - 34 * sc;
        g.setColor(rgb(0xcfcfcf));
        g.fill(new Rectangle2D.Double(fx - 1.2 * sc, fy - 70 * sc, 2.4 * sc, 70 * sc));
        for (int k = 0; k < 7; k++) {
            g.setColor(k % 2 == 0 ? rgb(0xb22234) : Color.white);
            Path2D st = new Path2D.Double();
            double wave = Math.sin(t * 2 + k * 0.3) * 2 * sc;
            st.moveTo(fx, fy - 70 * sc + k * 4 * sc);
            st.lineTo(fx + 48 * sc, fy - 70 * sc + k * 4 * sc + wave);
            st.lineTo(fx + 48 * sc, fy - 66 * sc + k * 4 * sc + wave);
            st.lineTo(fx, fy - 66 * sc + k * 4 * sc);
            st.closePath();
            g.fill(st);
        }
        g.setColor(rgb(0x3c3b6e));
        g.fill(new Rectangle2D.Double(fx, fy - 70 * sc, 20 * sc, 16 * sc));
    }

    public static void home(ResoluteDesk u, Graphics2D g) {
        double t = u.tsec;
        g.setPaint(new GradientPaint(0, 0, rgb(0x050a18), 0, DH, rgb(0x16254a)));
        g.fill(new Rectangle2D.Double(0, 0, DW, DH));
        Random st = new Random(7);
        for (int i = 0; i < 160; i++) {
            double sx = st.nextDouble() * DW, sy = st.nextDouble() * 520, tw = 0.5 + 0.5 * Math.sin(t * 1.5 + i);
            g.setColor(new Color(255, 255, 255, (int) (40 + 90 * tw * st.nextDouble())));
            g.fill(new Ellipse2D.Double(sx, sy, 1.6, 1.6));
        }
        g.setPaint(new GradientPaint(0, 640, new Color(0, 0, 0, 0), 0, DH, new Color(0, 0, 0, 160)));
        g.fill(new Rectangle2D.Double(0, 520, DW, DH - 520));
        portico(g, 1120, 700, 1.25, t);
        g.setColor(rgb(0x0c1424));
        g.fill(new Rectangle2D.Double(0, 700, DW, DH - 700));
        double in = easeOut(Math.min(1, u.screenT * 1.4));
        double tx = 80 - (1 - in) * 30;
        label(g, "A PRESIDENCY SIMULATOR", tx + 4, 170, C_GOLD);
        Font big = new Font(SERIF, Font.BOLD, 96);
        g.setFont(big);
        g.setColor(C_IVORY);
        g.drawString("RESOLUTE", (float) tx, 262f);
        g.setPaint(new GradientPaint((float) tx, 280, rgb(0xf2c14e), (float) (tx + 400), 380, rgb(0xd49a2a)));
        g.drawString("DESK", (float) tx, 360f);
        text(g, "Four years, or eight, in the Oval Office. Govern, negotiate, survive the crises, and answer to history.", tx + 4, 400, F_SERIF_S, C_DIM);
        double by = 460;
        u.button(g, tx, by, 340, 54, "New presidency", B_GOLD, () -> { u.setup = new Settings(); showEra(u); u.go(SC_SETUP); });
        by += 66;
        boolean has = SaveGame.exists(SaveGame.AUTO);
        u.button(g, tx, by, 340, 46, has ? "Continue: " + SaveGame.describe(SaveGame.AUTO) : "Continue", has ? B_PRIMARY : B_OFF, has ? () -> SaveGame.load(u, SaveGame.AUTO) : null);
        by += 58;
        boolean slot = SaveGame.exists(SaveGame.SLOT);
        u.button(g, tx, by, 340, 46, "Load saved game", slot ? B_NORM : B_OFF, slot ? () -> SaveGame.load(u, SaveGame.SLOT) : null, slot ? SaveGame.describe(SaveGame.SLOT) : "No saved game yet.");
        by += 58;
        u.button(g, tx, by, 340, 46, "From Battleground 2028", B_NORM, () -> { BgImport.scanned = false; BgImport.sel = 0; u.go(SC_IMPORT); }, "Take office as the winner of a Battleground 2028 game.");
        by += 58;
        u.button(g, tx, by, 166, 46, "Watch history", B_NORM, () -> watchRandom(u), "Pick a random era and let the real president govern while you watch.");
        u.button(g, tx + 174, by, 166, 46, "Hall of Fame", B_NORM, () -> u.go(SC_HALL), "Your finished presidencies and achievements.");
        by += 58;
        if (u.frame != null) u.button(g, tx, by, 340, 40, "Quit", B_GHOST, () -> u.frame.dispose());
        text(g, "Everything that happens in a game is fiction. Real people appear as stylised characters; the game never quotes them.", 80, DH - 30, F_XS, C_FAINT);
        if (status != null) text(g, status, 80, DH - 52, F_XS, C_AMBER);
    }

    /** Scrolls the list of inaugurations so the chosen one is in view. */
    public static void showEra(ResoluteDesk u) {
        int i = 0;
        for (int k = 0; k < ERAS.size(); k++) if (ERAS.get(k).year == u.setup.year) i = k;
        u.scrollTo("eras", i * 104 - 646 + 124);
    }

    /** Watch mode from the title screen: a random era, the real winner, the game playing itself. */
    static void watchRandom(ResoluteDesk u) {
        Random r = new Random(freshSeed());
        Era e = ERAS.get(r.nextInt(ERAS.size() - 1));
        Settings s = new Settings();
        s.year = e.year;
        Cand w = eraRoster(e).get(0);
        s.presId = w.id; s.vpId = defaultVp(e, w); s.tutorial = false;
        u.setup = s;
        start(u);
        u.game.watch = true;
        u.game.tut = -1;
        u.modal = null;
        u.toast("Watching " + w.name() + " from " + e.year + ". Click 'Watching...' to take over.", C_GOLD);
    }

    // ---- a new presidency --------------------------------------------------------------------------------
    public static void setup(ResoluteDesk u, Graphics2D g) {
        Settings s = u.setup;
        header(u, g, "A New Presidency", "Choose when you take office and how the country looks on day one.");
        Era cur = era(s.year);
        double ax = 48, aw = 480;
        u.panel(g, ax, 124, aw, 690, "When you take office: " + ERAS.size() + " inaugurations");
        double top = 160, hh = 646;
        double off = u.beginScroll(g, "eras", ax + 10, top, aw - 20, hh);
        int h0 = u.hits.size();
        double y = top - off;
        for (Era e : ERAS) {
            final Era ee = e;
            boolean on = e.year == s.year;
            u.card(g, ax + 18, y, aw - 36, 96, on, () -> { s.year = ee.year; s.presId = null; s.vpId = null; s.custom = null; }, null);
            text(g, String.valueOf(e.year), ax + 32, y + 36, new Font(SERIF, Font.BOLD, 28), on ? C_GOLD : Color.white);
            text(g, e.name, ax + 124, y + 26, F_BB, Color.white);
            Cand w = e.winnerId != null ? candById(e.winnerId) : null;
            text(g, e.fictional ? "A projected future" : (w != null ? w.name() + ", " : "") + MONTH_FULL[e.inM] + " " + e.inD, ax + 124, y + 44, F_XS, e.fictional ? C_AMBER : C_DIM);
            String bl = e.blurb.indexOf(". ") > 0 && e.blurb.indexOf(". ") < 22 ? e.blurb.substring(e.blurb.indexOf(". ") + 2) : e.blurb;
            wrapMax(g, bl, ax + 32, y + 66, aw - 72, F_XS, C_TEXT, 2, 16);
            y += 104;
        }
        u.clipHits(h0, top, top + hh);
        u.endScroll(g, "eras", ERAS.size() * 104);
        // conditions
        double bx = ax + aw + 16, bw = 560;
        u.panel(g, bx, 124, bw, 690, "Starting conditions");
        y = 168;
        text(g, "Scenario", bx + 18, y, F_H3, C_TEXT);
        u.segmented(g, bx + 18, y + 10, bw - 36, 30, new String[]{SCEN[0], SCEN[1], SCEN[2]}, s.scenario < 3 ? s.scenario : -1, i -> s.scenario = i, new String[]{SCEN_DESC[0], SCEN_DESC[1], SCEN_DESC[2]});
        u.segmented(g, bx + 18, y + 46, bw - 36, 30, new String[]{SCEN[3], SCEN[4], SCEN[5]}, s.scenario >= 3 ? s.scenario - 3 : -1, i -> s.scenario = i + 3, new String[]{SCEN_DESC[3], SCEN_DESC[4], SCEN_DESC[5]});
        wrap(g, SCEN_DESC[s.scenario], bx + 18, y + 96, bw - 36, F_XS, C_DIM);
        y += 130;
        text(g, "Difficulty", bx + 18, y + 20, F_H3, C_TEXT);
        u.segmented(g, bx + 120, y, bw - 138, 30, new String[]{"Easy", "Normal", "Hard"}, s.difficulty, i -> s.difficulty = i,
                new String[]{"A friendlier public and a forgiving Congress.", "The job as it is.", "Less patience from voters, the press and Congress."});
        y += 60;
        label(g, "Custom conditions (optional)", bx + 18, y, C_GOLD);
        y += 18;
        y = condition(u, g, bx, bw, y, "Approval", s.cApproval, cur.approvalBase + 5, 25, 75, "%", v -> s.cApproval = v, () -> s.cApproval = Double.NaN);
        y = condition(u, g, bx, bw, y, "Unemployment", s.cUnemp, cur.unemp, 2.5, 14, "%", v -> s.cUnemp = v, () -> s.cUnemp = Double.NaN);
        y = condition(u, g, bx, bw, y, "Inflation", s.cInfl, cur.infl, -1, 12, "%", v -> s.cInfl = v, () -> s.cInfl = Double.NaN);
        y = condition(u, g, bx, bw, y, "Growth", s.cGdp, cur.gdp, -4, 6, "%", v -> s.cGdp = v, () -> s.cGdp = Double.NaN);
        y += 8;
        text(g, "Your party's seats", bx + 18, y + 16, F_S, C_TEXT);
        int ph = s.cHouse, ps = s.cSenate;
        u.stepper(g, bx + 200, y, ph < 0 ? "House: era" : "House: " + ph, () -> s.cHouse = Math.max(120, (s.cHouse < 0 ? 218 : s.cHouse) - 5), () -> s.cHouse = Math.min(330, (s.cHouse < 0 ? 218 : s.cHouse) + 5), "Your party's seats in the House (218 is a majority).", 110);
        u.stepper(g, bx + 380, y, ps < 0 ? "Senate: era" : "Senate: " + ps, () -> s.cSenate = Math.max(25, (s.cSenate < 0 ? 50 : s.cSenate) - 1), () -> s.cSenate = Math.min(75, (s.cSenate < 0 ? 50 : s.cSenate) + 1), "Your party's senators (50 plus the vice president is a majority).", 110);
        y += 44;
        u.button(g, bx + 18, y, 200, 30, "Reset to the era", B_GHOST, () -> { s.cApproval = Double.NaN; s.cUnemp = Double.NaN; s.cInfl = Double.NaN; s.cGdp = Double.NaN; s.cHouse = -1; s.cSenate = -1; });
        y += 50;
        u.button(g, bx + 18, y, bw - 36, 30, (s.tutorial ? "[x] " : "[  ] ") + "Show me around the Oval Office (a one-minute tour)", B_GHOST, () -> s.tutorial = !s.tutorial);
        y += 44;
        wrap(g, cur.blurb, bx + 18, y, bw - 36, F_XS, C_DIM);
        // the era at a glance
        double cx = bx + bw + 16, cw = DW - 48 - cx;
        u.panel(g, cx, 124, cw, 690, cur.year + " at a glance");
        y = 170;
        String[][] rows = {{"Unemployment", fmt1(cur.unemp) + "%"}, {"Inflation", fmt1(cur.infl) + "%"}, {"Growth", fmt1(cur.gdp) + "%"}, {"Fed funds rate", fmt1(cur.fed) + "%"},
                {"10-year Treasury", fmt1(cur.tenYr) + "%"}, {"Stocks (S&P index)", thousands(Math.round(cur.stocks))}, {"Gas", "$" + fmt2(cur.gas)},
                {"Debt held by the public", Math.round(cur.debt) + "% of GDP"}, {"Deficit", fmt1(cur.deficit) + "% of GDP"}, {"Fed chair", cur.fedChair}, {"Outgoing president", cur.predecessor}};
        for (String[] r : rows) { text(g, r[0], cx + 18, y, F_S, C_TEXT); textR(g, r[1], cx + cw - 18, y, F_BB, Color.white); y += 26; }
        y += 10;
        label(g, "Congress on day one", cx + 18, y, C_GOLD);
        y += 24;
        if (cur.houseD >= 0) {
            text(g, "House: " + cur.houseD + " D - " + (435 - cur.houseD) + " R", cx + 18, y, F_S, C_TEXT); y += 22;
            text(g, "Senate: " + cur.senateD + " D - " + (100 - cur.senateD) + " R", cx + 18, y, F_S, C_TEXT); y += 22;
            text(g, "Speaker " + (cur.houseD >= 218 ? cur.speakerD : cur.speakerR) + "; Senate leader " + (cur.senateD >= 51 ? cur.leaderD : cur.leaderR), cx + 18, y, F_XS, C_DIM);
            y += 22;
            if (cur.year < 2025) {
                List<String> ct = courtIn(cur);
                int nr = 0; for (String j : ct) if (j.split("\\|")[2].equals("R")) nr++;
                text(g, "Supreme Court: " + nr + " appointed by Republicans, " + (ct.size() - nr) + " by Democrats", cx + 18, y, F_XS, C_DIM);
                y += 22;
                for (String wsp : WAR_START) if (wsp.startsWith(cur.year + "|")) for (String tok : wsp.substring(5).split(" ")) { text(g, "At war: " + WAR_NAME[warIndex(tok.split(":")[0])].replace("The ", "the "), cx + 18, y, F_XS, C_AMBER); y += 20; }
            }
        } else wrap(g, "Depends on who won: your party starts with about 232 House seats and 51 senators as a Democrat, or 229 and 52 as a Republican.", cx + 18, y, cw - 36, F_XS, C_TEXT);
        u.button(g, 48, 832, 180, 44, "Back", B_GHOST, () -> u.go(SC_HOME));
        u.button(g, DW - 48 - 300, 832, 300, 44, "Choose your president", B_GOLD, () -> { sel = -1; Cand w = era(s.year).winnerId != null ? candById(era(s.year).winnerId) : null; pickParty = w != null ? w.party : DEM; u.go(SC_PICK); });
    }

    static double condition(ResoluteDesk u, Graphics2D g, double bx, double bw, double y, String name, double v, double def, double lo, double hi, String unit, DoubleAction set, Runnable reset) {
        boolean custom = !Double.isNaN(v);
        double val = custom ? v : def;
        text(g, name, bx + 18, y + 16, F_S, C_TEXT);
        u.slider(g, bx + 150, y + 12, bw - 290, (val - lo) / (hi - lo), custom ? C_GOLD : C_FAINT, x -> set.on(Math.round((lo + x * (hi - lo)) * 10) / 10.0), "Drag to set; the era's value until you do.");
        textR(g, custom ? fmt1(val) + unit : "era (" + fmt1(def) + unit + ")", bx + bw - 18, y + 16, F_XSB, custom ? C_GOLD : C_DIM);
        return y + 34;
    }

    // ---- choosing a president ------------------------------------------------------------------------------
    public static void pick(ResoluteDesk u, Graphics2D g) {
        Settings s = u.setup;
        Era e = era(s.year);
        Cand win = e.winnerId != null ? candById(e.winnerId) : null;
        header(u, g, "Choose Your President", e.year == 2029 ? "Who won the 2028 election? Pick them, or create your own president." : (win != null ? win.name() : "The winner") + " won in " + (e.year - 1) + ". Play them, the runner-up, or anyone else as a what-if.");
        List<Cand> all = eraRoster(e);
        List<Cand> l = new ArrayList<Cand>();
        for (Cand c : all) if (c.party == pickParty) l.add(c);
        u.tabs(g, 48, 120, 400, 34, new String[]{"Democrats", "Republicans"}, pickParty, i -> { pickParty = i; sel = -1; });
        double lx = 48, lw = 1000, top = 166, h = 646;
        double sy = u.beginScroll(g, "pick", lx, top, lw, h);
        int h0 = u.hits.size();
        int cols = 4;
        double cw = (lw - 20 - (cols - 1) * 10) / cols, ch = 104;
        for (int i = 0; i <= l.size(); i++) {
            double x = lx + (i % cols) * (cw + 10), yy = top - sy + (i / cols) * (ch + 10);
            if (i == 0) {
                u.card(g, x, yy, cw, ch, false, () -> { Creator.begin(u, pickParty); }, "Design your own president.");
                textC(g, "+", x + 46, yy + 64, new Font(SANS, Font.BOLD, 40), C_GOLD);
                text(g, "Create your own", x + 92, yy + 46, F_BB, Color.white);
                text(g, "Name, record and strengths", x + 92, yy + 66, F_XS, C_DIM);
                continue;
            }
            Cand c = l.get(i - 1);
            final int k = i - 1;
            boolean on = sel == k;
            u.card(g, x, yy, cw, ch, on, () -> sel = k, null);
            Portrait.draw(g, c, x + 10, yy + 10, 70, 84, u.tsec, 0, true, true);
            text(g, c.name(), x + 92, yy + 30, F_BB, on ? C_GOLD : Color.white);
            wrap(g, c.role, x + 92, yy + 50, cw - 100, F_XS, C_DIM);
            if (e.winnerId != null && c.id.equals(e.winnerId)) chip(g, "WON THE ELECTION", x + 92, yy + 80, C_GOLD, true);
        }
        u.clipHits(h0, top, top + h);
        u.endScroll(g, "pick", ((l.size() + cols) / cols) * (ch + 10));
        double px = lx + lw + 16, pw = DW - 48 - px;
        u.panel(g, px, 124, pw, 690, "The president");
        Cand c = sel >= 0 && sel < l.size() ? l.get(sel) : null;
        if (c == null) wrap(g, "Pick a president on the left, or create your own.", px + 20, 170, pw - 40, F_S, C_DIM);
        else candidatePanel(u, g, c, px, pw);
        u.button(g, 48, 832, 180, 44, "Back", B_GHOST, () -> u.go(SC_SETUP));
        u.button(g, DW - 48 - 300, 832, 300, 44, "Next: your running mate", c != null ? B_GOLD : B_OFF, c != null ? () -> { s.presId = c.id; s.custom = null; s.vpId = defaultVp(e, c); vpSel = -1; u.go(SC_VP); } : null);
    }

    static final int[] SHOW_ATTR = {A_CHA, A_SPE, A_DEB, A_INT, A_DIS, A_EXE, A_FPC, A_EST, A_XOV, A_STA, A_SCA};
    static final String[] SHOW_ATTR_NAME = {"Charisma", "Speeches", "Debating", "Policy depth", "Discipline", "Executive experience", "Foreign policy", "Ties in Washington", "Crossover appeal", "Stamina", "Scandal risk"};

    public static void candidatePanel(ResoluteDesk u, Graphics2D g, Cand c, double px, double pw) {
        Portrait.draw(g, c, px + 20, 160, 130, 156, u.tsec, 0, false, true);
        text(g, c.name(), px + 166, 186, F_H2, Color.white);
        chip(g, PARTY[c.party].toUpperCase(), px + 166, 198, partyColor(c.party), true);
        wrap(g, c.role, px + 166, 236, pw - 186, F_XS, C_DIM);
        if (c.born > 0) text(g, "Born " + c.born + (c.homeAb != null ? ", " + c.homeAb : ""), px + 166, 290, F_XS, C_DIM);
        double y = 336;
        if (c.bio != null) y += 10 + wrap(g, c.bio, px + 20, y, pw - 40, F_XS, C_TEXT);
        for (int i = 0; i < SHOW_ATTR.length; i++) {
            int v = c.a[SHOW_ATTR[i]];
            text(g, SHOW_ATTR_NAME[i], px + 20, y + 12, F_XS, C_TEXT);
            bar(g, px + 170, y + 4, pw - 230, 9, v / 100.0, SHOW_ATTR[i] == A_SCA ? C_AMBER : C_ACCENT);
            textR(g, String.valueOf(v), px + pw - 20, y + 12, F_XSB, Color.white);
            y += 19;
        }
        if (c.pros != null && y < 720) { y += 8; y += 4 + wrap(g, "+ " + c.pros, px + 20, y, pw - 40, F_XS, C_GOOD); }
        if (c.cons != null && y < 760) wrap(g, "- " + c.cons, px + 20, y, pw - 40, F_XS, C_AMBER);
    }

    // ---- the running mate -------------------------------------------------------------------------------
    public static void vp(ResoluteDesk u, Graphics2D g) {
        Settings s = u.setup;
        Era e = era(s.year);
        Cand p = s.custom != null ? s.custom : candById(s.presId);
        header(u, g, "Your Vice President", p == null ? "" : "Who serves with " + p.name() + "? You can give them missions and portfolios once in office.");
        List<Cand> l = new ArrayList<Cand>();
        for (Cand c : eraRoster(e)) if (p != null && c.party == p.party && c != p) l.add(c);
        if (vpSel < 0 && s.vpId != null) for (int i = 0; i < l.size(); i++) if (l.get(i).id.equals(s.vpId)) vpSel = i;
        double lx = 48, lw = 1000, top = 124, h = 690;
        double sy = u.beginScroll(g, "vp", lx, top, lw, h);
        int h0 = u.hits.size();
        int cols = 4;
        double cw = (lw - 20 - (cols - 1) * 10) / cols, ch = 104;
        for (int i = 0; i < l.size(); i++) {
            Cand c = l.get(i);
            final int k = i;
            double x = lx + (i % cols) * (cw + 10), yy = top - sy + (i / cols) * (ch + 10);
            boolean on = vpSel == k;
            u.card(g, x, yy, cw, ch, on, () -> { vpSel = k; s.vpId = l.get(k).id; }, null);
            Portrait.draw(g, c, x + 10, yy + 10, 70, 84, u.tsec, 0, true, true);
            text(g, c.name(), x + 92, yy + 30, F_BB, on ? C_GOLD : Color.white);
            wrap(g, c.role, x + 92, yy + 50, cw - 100, F_XS, C_DIM);
        }
        u.clipHits(h0, top, top + h);
        u.endScroll(g, "vp", ((l.size() + cols - 1) / cols) * (ch + 10));
        double px = lx + lw + 16, pw = DW - 48 - px;
        u.panel(g, px, 124, pw, 690, "Vice president");
        Cand c = vpSel >= 0 && vpSel < l.size() ? l.get(vpSel) : null;
        if (c != null) candidatePanel(u, g, c, px, pw);
        else wrap(g, "Pick a running mate.", px + 20, 170, pw - 40, F_S, C_DIM);
        u.button(g, 48, 832, 180, 44, "Back", B_GHOST, () -> u.go(s.custom != null ? SC_CREATE : SC_PICK));
        u.button(g, DW - 48 - 300, 832, 300, 44, "Take the oath", c != null ? B_GOLD : B_OFF, c != null ? () -> start(u) : null);
    }

    /** A seed for a new game. Kept well under 2^31: game code multiplies and adds to it, and the browser
     *  build does that arithmetic in doubles, which lose integer precision past 2^53. */
    static long freshSeed() { return Math.abs(System.nanoTime() / 1000) % 2000000011L; }

    static void start(ResoluteDesk u) {
        Settings s = u.setup;
        s.seed = freshSeed();
        Game g = Setup.newGame(s);
        g.tut = s.tutorial ? 0 : -1;
        u.game = g;
        Oval.begin(u);
    }
}
