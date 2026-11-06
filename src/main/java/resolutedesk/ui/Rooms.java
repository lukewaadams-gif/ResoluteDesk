package resolutedesk.ui;

import static resolutedesk.core.Dates.*;
import static resolutedesk.core.Fmt.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.data.StateData.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.model.Tracking.*;
import static resolutedesk.opinion.Demographics.*;
import static resolutedesk.opinion.Issues.*;
import static resolutedesk.sim.Schedule.*;
import static resolutedesk.ui.Draw.*;
import static resolutedesk.ui.Fonts.*;
import static resolutedesk.ui.RoomIds.*;
import static resolutedesk.ui.ScreenIds.*;
import static resolutedesk.ui.Sound.*;
import static resolutedesk.ui.Theme.*;
import static resolutedesk.world.Countries.*;
import static resolutedesk.world.WorldData.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import resolutedesk.ResoluteDesk;
import resolutedesk.campaign.Reelection;
import resolutedesk.congress.Midterms;
import resolutedesk.economy.Economy;
import resolutedesk.model.Econ;
import resolutedesk.model.Era;
import resolutedesk.model.Game;
import resolutedesk.model.StateDef;
import resolutedesk.sim.Week;

/** The rooms behind the Oval Office doors: the desk calendar, the country, the economy and the residence. */
public final class Rooms {
    public static int room = R_DESK, pendingSlot = -1, pendingAct = AC_TRIP, mapMode = 0, selState = -1, chartSel = 0;
    static final String[] MAPMODE = {"Approval", "Unemployment", "Gas prices"};

    public static void open(ResoluteDesk u, int r) {
        if (r < 0 || r >= NROOM) return;
        room = r;
        pendingSlot = -1;
        u.go(SC_ROOM);
    }

    public static void key(ResoluteDesk u, KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_ESCAPE) { if (pendingSlot >= 0) pendingSlot = -1; else u.go(SC_OVAL); }
        if (k == KeyEvent.VK_SPACE) Oval.endWeek(u);
        if (k >= KeyEvent.VK_1 && k <= KeyEvent.VK_9) open(u, k - KeyEvent.VK_1);
    }

    public static void press(ResoluteDesk u, double mx, double my) { }

    public static void rightClick(ResoluteDesk u, double mx, double my) { }

    public static void draw(ResoluteDesk u, Graphics2D g) {
        Game game = u.game;
        if (game == null) { u.go(SC_HOME); return; }
        u.backdrop(g);
        Oval.topBar(u, g, game);
        u.button(g, 24, 70, 200, 32, "<  Oval Office", B_GHOST, () -> u.go(SC_OVAL), "Back to the Oval Office (Esc)");
        text(g, ROOM_NAME[room], 248, 96, F_DISPLAY, Color.white);
        double tabsW = 0;
        for (int r = 0; r < NROOM; r++) tabsW += strW(g, ROOM_TAB[r], F_XSB) + 24;
        double subX = 252 + strW(g, ROOM_NAME[room], F_DISPLAY) + 16;
        textFit(g, ROOM_SUB[room], subX, 94, DW - 48 - tabsW - subX - 16, F_B, C_DIM);
        // room tabs
        double tx = DW - 48;
        for (int r = NROOM - 1; r >= 0; r--) {
            final int k = r;
            String nm = ROOM_TAB[r];
            double w = strW(g, nm, F_XSB) + 20;
            tx -= w + 4;
            u.button(g, tx, 74, w, 26, nm, r == room ? B_TABON : ROOM_OPEN[r] ? B_TAB : B_OFF, ROOM_OPEN[r] ? () -> open(u, k) : null, ROOM_OPEN[r] ? null : "Coming in a later update.");
        }
        switch (room) {
            case R_DESK: desk(u, g, game); break;
            case R_COUNTRY: country(u, g, game); break;
            case R_ECON: econ(u, g, game); break;
            case R_RESIDENCE: residence(u, g, game); break;
            case R_CONGRESS: HillUI.draw(u, g, game); break;
            case R_WESTWING: WestWing.draw(u, g, game); break;
            case R_SITROOM: SitRoom.draw(u, g, game); break;
            case R_PRESS: PressRoom.draw(u, g, game); break;
            case R_POLICY: PolicyUI.draw(u, g, game); break;
            default: later(u, g, game); break;
        }
    }

    static final String[] ROOM_TAB = {"Desk", "Country", "Economy", "Residence", "West Wing", "Congress", "Situation Room", "Press", "Policy"};

    // ---- the desk: the week's calendar ----
    static void desk(ResoluteDesk u, Graphics2D g, Game game) {
        double gx = 48, gy = 128, cw = 139, ch = 132;
        u.panel(g, gx - 12, gy - 12, 7 * (cw + 6) + 18, 3 * (ch + 6) + 70, "Week of " + dateStr(game.day()));
        for (int d = 0; d < 7; d++) {
            int day = game.day() + d;
            int[] dt = dateOf(day);
            textC(g, DOW3[dowOf(day)] + " " + MONTHS[dt[1]] + " " + dt[2], gx + d * (cw + 6) + cw / 2, gy + 26, F_SB, dowOf(day) >= 5 ? C_GOLD : Color.white);
            for (int sl = 0; sl < 3; sl++) {
                int i = d * 3 + sl;
                double x = gx + d * (cw + 6), y = gy + 38 + sl * (ch + 6);
                slot(u, g, game, i, x, y, cw, ch);
            }
        }
        for (int sl = 0; sl < 3; sl++) { g.setColor(C_FAINT); }
        double by = gy + 38 + 3 * (ch + 6) + 8;
        u.button(g, gx, by, 170, 30, "Routine week", B_NORM, () -> Week.routine(game), "Policy in the mornings, remarks, a trip and rest on the weekend.");
        u.button(g, gx + 180, by, 170, 30, "Road week", B_NORM, () -> roadWeek(game), "Trips to four swing states. Tiring.");
        u.button(g, gx + 360, by, 170, 30, "Rest week", B_NORM, () -> restWeek(game), "A light schedule and Camp David at the weekend.");
        u.button(g, gx + 540, by, 170, 30, "Clear the week", B_GHOST, () -> { Arrays.fill(game.act, AC_FREE); Arrays.fill(game.arg, -1); });
        text(g, "Click a slot to change it.", gx + 730, by + 20, F_XS, C_DIM);
        // the week ahead
        double px = 1100, pw = DW - 48 - px;
        u.panel(g, px, 116, pw, 356, "The week ahead");
        double ps = Week.projectedStamina(game);
        text(g, "Stamina now", px + 18, 160, F_S, C_TEXT);
        bar(g, px + 150, 151, pw - 230, 10, game.stamina / 100, C_ACCENT);
        textR(g, String.valueOf(Math.round(game.stamina)), px + pw - 18, 160, F_BB, Color.white);
        text(g, "After this week", px + 18, 186, F_S, C_TEXT);
        bar(g, px + 150, 177, pw - 230, 10, ps / 100, ps >= 50 ? C_GOOD : ps >= 25 ? C_AMBER : C_BAD);
        textR(g, String.valueOf(Math.round(ps)), px + pw - 18, 186, F_BB, Color.white);
        text(g, "Sleep", px + 18, 212, F_S, C_TEXT);
        textR(g, fmt1(Week.sleepOf(game)) + " hours a night", px + pw - 18, 212, F_BB, Week.sleepOf(game) < 5.8 ? C_AMBER : Color.white);
        text(g, "Age " + game.presAge() + (game.presAge() > 70 ? ": long days cost you more" : ""), px + 18, 238, F_XS, C_DIM);
        // Air Force One
        label(g, "Air Force One", px + 18, 266, C_GOLD);
        double ty = 286;
        int n = 0;
        for (int i = 0; i < NSLOT; i++) if (isTrip(game.act[i]) && game.arg[i] >= 0) {
            int s = game.arg[i];
            text(g, DOW3[dowOf(game.day() + i / 3)] + " " + SLOT[i % 3].toLowerCase() + ": " + STATES[s].name + (game.act[i] == AC_CAMPAIGN ? " (campaign)" : ""), px + 18, ty, F_XS, C_TEXT);
            textR(g, fmt1(Week.flight(s)) + " hours each way", px + pw - 18, ty, F_XS, Week.flight(s) > 3.5 ? C_AMBER : C_DIM);
            ty += 18;
            if (++n >= 8) break;
        }
        for (int i = 0; i < NSLOT && n < 8; i++) if (game.act[i] == AC_ABROAD && game.arg[i] >= 0 && (i == 0 || game.act[i - 1] != AC_ABROAD)) {
            text(g, DOW3[dowOf(game.day() + i / 3)] + ": " + cname(game, game.arg[i]), px + 18, ty, F_XS, C_TEXT);
            textR(g, fmt1(flightAbroad(game.arg[i])) + " hours each way", px + pw - 18, ty, F_XS, C_AMBER);
            ty += 18; n++;
        }
        if (n == 0) text(g, "No trips this week.", px + 18, ty, F_XS, C_DIM);
        // the map, for picking trip destinations
        u.panel(g, px, 482, pw, 330, pendingSlot >= 0 ? "Click a state for the trip" : "Where you're going");
        for (int s = 0; s < NS; s++) { USMap.paint[s] = null; USMap.tags[s] = null; }
        for (int i = 0; i < NSLOT; i++) if (isTrip(game.act[i]) && game.arg[i] >= 0) USMap.paint[game.arg[i]] = rgb(0xd4a52a);
        if (pendingSlot >= 0) for (int s = 0; s < NS; s++) if (USMap.paint[s] == null) USMap.paint[s] = mix(C_STATE, marginColor(game.stateApp[s] - 50, 1), 0.5);
        USMap.markState = Week.dc(); USMap.markCand = game.pres;
        double[] f = USMap.draw(u, g, px + 10, 510, pw - 20, 290, -1, true);
        int hs = USMap.stateAt(f, u.mx, u.my);
        if (hs >= 0 && u.my > 510) {
            u.tip = STATES[hs].name + ": approval " + Math.round(game.stateApp[hs]) + "%, " + STATES[hs].ev + " electoral votes, " + fmt1(Week.flight(hs)) + " hours from Washington";
            final int ss = hs;
            if (pendingSlot >= 0) u.hit(px + 10, 510, pw - 20, 290, () -> { game.act[pendingSlot] = pendingAct; game.arg[pendingSlot] = ss; pendingSlot = -1; snd(Sfx.CHIME, 0.5, 1); }, null);
        }
    }

    static void slot(ResoluteDesk u, Graphics2D g, Game game, int i, double x, double y, double w, double h) {
        int a = game.act[i], arg = game.arg[i];
        boolean pend = pendingSlot == i;
        u.card(g, x, y, w, h, pend, () -> choose(u, game, i), AC_DESC[a]);
        g.setColor(AC_COL[a]);
        g.fill(new RoundRectangle2D.Double(x + 8, y + 10, 5, h - 20, 4, 4));
        label(g, SLOT[i % 3].toUpperCase(), x + 20, y + 22, C_DIM);
        text(g, AC_NAME[a], x + 20, y + 46, F_BB, a == AC_FREE ? C_DIM : Color.white);
        String sub = arg >= 0 && a == AC_ABROAD ? cname(game, arg) : arg >= 0 && AC_ISSUE[a] ? PISSUE[arg] : arg >= 0 && AC_STATE[a] ? STATES[arg].name : pend ? "pick a state on the map" : null;
        if (sub != null) wrap(g, sub, x + 20, y + 64, w - 28, F_XS, pend ? C_GOLD : C_TEXT);
        double c = AC_STAMINA[a];
        if (isTrip(a) && arg >= 0) c -= Math.min(6, Week.flight(arg) * 0.9);
        if (a == AC_ABROAD && arg >= 0 && (i == 0 || game.act[i - 1] != AC_ABROAD)) c -= Math.min(12, flightAbroad(arg) * 0.8);
        text(g, (c >= 0 ? "+" : "") + fmt1(c) + " stamina", x + 20, y + h - 14, F_T, c >= 0 ? C_GOOD : C_DIM);
    }

    static void choose(ResoluteDesk u, Game game, int i) {
        boolean weekend = dowOf(game.day() + i / 3) >= 5;
        List<Integer> opts = new ArrayList<Integer>();
        for (int a = 0; a < NAC; a++) {
            if (a == AC_TRAVEL) continue;
            if (a == AC_CAMPDAVID && !weekend) continue;
            if (a == AC_CONGRESS && weekend) continue;
            if (a == AC_WORKLATE && i % 3 != 2) continue;
            if (a == AC_CAMPAIGN && !Midterms.season(game) && !Reelection.season(game)) continue;
            if (a == AC_CABINET && (weekend || i % 3 == 2)) continue;
            opts.add(a);
        }
        String[] names = new String[opts.size()];
        for (int k = 0; k < names.length; k++) names[k] = AC_NAME[opts.get(k)];
        u.openPopup(Math.min(u.mx, DW - 300), Math.min(u.my, DH - 420), 280, SLOT[i % 3] + ", " + DOW[dowOf(game.day() + i / 3)], names, k -> {
            int a = opts.get(k);
            if (AC_ISSUE[a]) {
                String[] iss = new String[NPI];
                for (int j = 0; j < NPI; j++) iss[j] = PISSUE[j] + "  (" + Math.round(game.issueApp[j]) + "% approve)";
                u.openPopup(Math.min(u.mx, DW - 340), 140, 320, AC_NAME[a] + ": which issue?", iss, j -> { game.act[i] = a; game.arg[i] = j; });
            } else if (a == AC_ABROAD) {
                Integer[] ord = new Integer[NC];
                for (int j = 0; j < NC; j++) ord[j] = j;
                Arrays.sort(ord, (x1, x2) -> Double.compare(flightAbroad(x1), flightAbroad(x2)));
                String[] cs = new String[NC];
                for (int j = 0; j < NC; j++) cs[j] = cname(game, ord[j]) + "  (" + fmt1(flightAbroad(ord[j])) + " hours)";
                u.openPopup(Math.min(u.mx, DW - 340), 120, 320, "Where to?", cs, j -> { int c0 = ord[j]; for (int s2 = i; s2 < Math.min(NSLOT, i + 3); s2++) { game.act[s2] = AC_ABROAD; game.arg[s2] = c0; } });
            } else if (AC_STATE[a]) {
                pendingSlot = i; pendingAct = a;
            } else { game.act[i] = a; game.arg[i] = -1; }
        });
    }

    public static void roadWeek(Game game) {
        Week.routine(game);
        List<Integer> sw = new ArrayList<Integer>();
        for (int s = 0; s < NS; s++) sw.add(s);
        final Game gg = game;
        Collections.sort(sw, (a, b) -> Double.compare(Math.abs(gg.stateApp[a] - 48) - STATES[a].ev * 0.15, Math.abs(gg.stateApp[b] - 48) - STATES[b].ev * 0.15));
        int[] slots = {1, 4, 7, 10};
        for (int k = 0; k < 4; k++) { game.act[slots[k]] = AC_TRIP; game.arg[slots[k]] = sw.get(k); }
    }

    public static void restWeek(Game game) {
        Week.routine(game);
        for (int d = 0; d < 5; d++) { game.act[d * 3 + 1] = d % 2 == 0 ? AC_POLICY : AC_FREE; game.arg[d * 3 + 1] = Week.topIssue(game); game.act[d * 3 + 2] = AC_REST; }
        game.act[15] = AC_CAMPDAVID; game.act[16] = AC_CAMPDAVID; game.act[17] = AC_CAMPDAVID; game.act[18] = AC_CAMPDAVID; game.act[19] = AC_CAMPDAVID; game.act[20] = AC_REST;
    }

    // ---- the country: approval ----
    static void country(ResoluteDesk u, Graphics2D g, Game game) {
        double lx = 48, lw = 470;
        u.panel(g, lx, 116, lw, 300, "Approval");
        text(g, Math.round(game.approval) + "%", lx + 18, 186, F_NUMXL, Color.white);
        text(g, "approve of the job you're doing", lx + 18, 206, F_XS, C_DIM);
        textR(g, "Week 1: " + Math.round(game.approval0) + "%", lx + lw - 18, 186, F_S, C_DIM);
        List<double[]> ser = new ArrayList<double[]>();
        double[] a = new double[game.hist.size()];
        for (int i = 0; i < a.length; i++) a[i] = game.hist.get(i)[H_APP];
        ser.add(a);
        Draw.chart(g, lx + 40, 224, lw - 60, 170, ser, new Color[]{C_GOLD}, new String[]{"Approval"}, 25, 75, "%.0f%%", Math.max(53, a.length));
        u.panel(g, lx, 426, lw, 386, "By issue (how much it matters)");
        Integer[] idx = new Integer[NPI];
        for (int i = 0; i < NPI; i++) idx[i] = i;
        final Game gg = game;
        Arrays.sort(idx, (p, q) -> Double.compare(gg.issueSal[q], gg.issueSal[p]));
        double y = 464;
        for (int k = 0; k < NPI; k++) {
            int i = idx[k];
            if (game.issueSal[i] < 0.08) continue;
            text(g, PISSUE[i], lx + 18, y, F_XS, C_TEXT);
            bar(g, lx + 170, y - 9, 200, 10, game.issueApp[i] / 100, game.issueApp[i] >= 50 ? C_GOOD : game.issueApp[i] >= 40 ? C_GOLD : C_BAD);
            textR(g, Math.round(game.issueApp[i]) + "%", lx + 410, y, F_XSB, Color.white);
            for (int d = 0; d < Math.min(5, (int) Math.round(game.issueSal[i] * 3)); d++) { g.setColor(C_AMBER); g.fill(new Ellipse2D.Double(lx + 420 + d * 7, y - 7, 5, 5)); }
            y += 24;
            if (y > 800) break;
        }
        // the map
        double mx = lx + lw + 16, mw = 640;
        u.panel(g, mx, 116, mw, 696, "By state");
        u.segmented(g, mx + 18, 150, 360, 28, MAPMODE, mapMode, i -> mapMode = i, null);
        Econ e = game.econ;
        for (int s = 0; s < NS; s++) {
            if (mapMode == 0) { USMap.paint[s] = marginColor((game.stateApp[s] - 50) * (game.party == REP ? -1 : 1), 1); USMap.tags[s] = Math.round(game.stateApp[s]) + "%"; }
            else if (mapMode == 1) { USMap.paint[s] = mix(rgb(0x1f6f4a), rgb(0xc8323a), clamp((e.stUnemp[s] - 2.5) / 6, 0, 1)); USMap.tags[s] = fmt1(e.stUnemp[s]); }
            else { USMap.paint[s] = mix(rgb(0x1f6f4a), rgb(0xc8323a), clamp((e.stGas[s] - e.gas + 0.6) / 2.0, 0, 1)); USMap.tags[s] = "$" + fmt2(e.stGas[s]); }
        }
        USMap.markState = game.location; USMap.markCand = game.pres;
        double[] f = USMap.draw(u, g, mx + 10, 190, mw - 20, 440, selState, true);
        int hs = USMap.stateAt(f, u.mx, u.my);
        if (hs >= 0) { final int ss = hs; u.hit(mx + 10, 190, mw - 20, 440, () -> selState = ss, null); }
        if (mapMode == 0) USMap.scale(g, mx + 18, 644, game.party == REP ? C_DEM : C_REP, C_STATE, game.party == REP ? C_REP : C_DEM, "35%", "65%", "approve of you");
        else USMap.scale(g, mx + 18, 644, rgb(0x1f6f4a), mix(rgb(0x1f6f4a), rgb(0xc8323a), 0.5), rgb(0xc8323a), "low", "high", mapMode == 1 ? "unemployment rate" : "gas price vs. the nation");
        int s = selState >= 0 ? selState : hs;
        if (s >= 0) {
            StateDef d = STATES[s];
            double m24 = (d.d24 - d.r24) / Math.max(1, d.t24) * 100;
            text(g, d.name, mx + 18, 696, F_H2, Color.white);
            text(g, "Approval " + Math.round(game.stateApp[s]) + "%   Unemployment " + fmt1(e.stUnemp[s]) + "%   Gas $" + fmt2(e.stGas[s]) + "   " + d.ev + " electoral votes   2024: " + margin(m24), mx + 18, 720, F_XS, C_TEXT);
            if (game.stateBump[s] > 0.3) text(g, "Your recent visit is still helping here.", mx + 18, 740, F_XS, C_GOOD);
        } else text(g, "Click a state for details.", mx + 18, 700, F_XS, C_DIM);
        // groups
        double gx = mx + mw + 16, gw = DW - 48 - gx;
        u.panel(g, gx, 116, gw, 696, "By group");
        y = 156;
        for (int gr : SHOW_GROUPS) {
            textFit(g, GROUP[gr], gx + 18, y, 150, F_XS, C_TEXT);
            bar(g, gx + 176, y - 9, gw - 240, 10, game.groupApp[gr] / 100, game.groupApp[gr] >= 50 ? C_GOOD : game.groupApp[gr] >= 35 ? C_GOLD : C_BAD);
            textR(g, Math.round(game.groupApp[gr]) + "%", gx + gw - 18, y, F_XSB, Color.white);
            y += 34;
        }
    }

    // ---- the economy ----
    static void econ(ResoluteDesk u, Graphics2D g, Game game) {
        Econ e = game.econ;
        String[][] tiles = {{"GROWTH", fmt1(e.gdp) + "%", "over the past year"}, {"UNEMPLOYMENT", fmt1(e.unemp) + "%", (e.jobsLast >= 0 ? "+" : "") + Math.round(e.jobsLast) + "K jobs last month"},
                {"INFLATION", fmt1(e.infl) + "%", "core " + fmt1(e.core) + "%"}, {"FED FUNDS", fmt2(e.fed) + "%", "10-year " + fmt2(e.tenYr) + "%"},
                {"S&P 500", thousands(Math.round(e.stocks)), (e.stocks >= e.stocks0 ? "+" : "") + fmt1((e.stocks / e.stocks0 - 1) * 100) + "% since you took office"},
                {"GAS", "$" + fmt2(e.gas), "a gallon, national average"}, {"CONSUMER MOOD", fmt0(e.sentiment), "sentiment index"},
                {"DEFICIT", fmt1(e.deficit) + "%", "of GDP, $" + fmt1(e.deficit * e.nominalGdp * 10) + "B"}, {"DEBT", Math.round(e.debt) + "%", "of GDP, held by the public"}};
        double tw = (DW - 96 - 8 * 10) / 9.0;
        for (int i = 0; i < tiles.length; i++) {
            double x = 48 + i * (tw + 10);
            g.setColor(new Color(255, 255, 255, 12));
            g.fill(new RoundRectangle2D.Double(x, 116, tw, 84, 10, 10));
            label(g, tiles[i][0], x + 12, 136, C_DIM);
            text(g, tiles[i][1], x + 12, 168, F_NUM, Color.white);
            textFit(g, tiles[i][2], x + 12, 188, tw - 20, F_T, C_DIM);
        }
        // charts
        String[] cn = {"Jobs and prices", "Growth", "Stocks", "Rates", "Gas", "The budget"};
        u.segmented(g, 48, 214, 700, 28, cn, chartSel, i -> chartSel = i, null);
        u.panel(g, 48, 252, 900, 560, cn[chartSel]);
        List<double[]> ser = new ArrayList<double[]>();
        Color[] cols;
        String[] names;
        double lo, hi;
        String unit = "%.1f%%";
        int n = game.hist.size();
        switch (chartSel) {
            case 1: ser.add(col(game, H_GDP)); cols = new Color[]{C_GOOD}; names = new String[]{"Growth, past year"}; lo = -4; hi = 6; break;
            case 2: ser.add(col(game, H_STOCKS)); cols = new Color[]{C_ACCENT}; names = new String[]{"S&P 500"}; lo = minOf(ser.get(0)) * 0.95; hi = maxOf(ser.get(0)) * 1.05; unit = "%.0f"; break;
            case 3: ser.add(col(game, H_FED)); ser.add(col(game, H_TEN)); cols = new Color[]{C_GOLD, C_ACCENT}; names = new String[]{"Fed funds", "10-year Treasury"}; lo = 0; hi = Math.max(6, maxOf(ser.get(1)) + 1); break;
            case 4: ser.add(col(game, H_GAS)); cols = new Color[]{C_AMBER}; names = new String[]{"Gas, $ a gallon"}; lo = Math.max(0, minOf(ser.get(0)) - 0.5); hi = maxOf(ser.get(0)) + 0.5; unit = "$%.2f"; break;
            case 5: ser.add(col(game, H_DEF)); cols = new Color[]{C_BAD}; names = new String[]{"Deficit, % of GDP"}; lo = 0; hi = Math.max(10, maxOf(ser.get(0)) + 2); break;
            default: ser.add(col(game, H_UNEMP)); ser.add(col(game, H_INFL)); cols = new Color[]{C_ACCENT, C_AMBER}; names = new String[]{"Unemployment", "Inflation"}; lo = 0; hi = Math.max(8, Math.max(maxOf(ser.get(0)), maxOf(ser.get(1))) + 1); break;
        }
        Draw.chart(g, 96, 292, 820, 480, ser, cols, names, lo, hi, unit, Math.max(27, n));
        if (n < 3) textC(g, "The chart fills in as the weeks go by.", 500, 540, F_S, C_DIM);
        // right column
        double px = 964, pw = DW - 48 - px;
        u.panel(g, px, 214, pw, 220, "The prices people feel (over the past year)");
        String[][] pr = {{"Gas", "$" + fmt2(e.gas) + " a gallon", fmt1((e.gas / e.gas0 - 1) * 100) + "% since you took office"}, {"Groceries", fmt1(e.groceries) + "%", null},
                {"Rent", fmt1(e.rent) + "%", null}, {"Car and home insurance", fmt1(e.insurance) + "%", null}, {"Mortgage rate", fmt2(e.mortgage) + "%", "30-year fixed"}};
        double y = 254;
        for (String[] r : pr) {
            text(g, r[0], px + 18, y, F_S, C_TEXT);
            textR(g, r[1], px + pw - 18, y, F_BB, Color.white);
            if (r[2] != null) textR(g, r[2], px + pw - 18, y + 14, F_T, C_DIM);
            y += r[2] != null ? 36 : 28;
        }
        u.panel(g, px, 444, pw, 200, "Industries");
        String[][] ind = {{"Housing starts", fmt2(e.housing) + " million a year"}, {"Manufacturing (PMI)", fmt1(e.manufacturing) + (e.manufacturing >= 50 ? " (growing)" : " (shrinking)")},
                {"AI investment", game.era.ai ? fmt0(e.aiInvest) + " (100 = 2025)" : "n/a"}, {"Farm income", fmt0(e.farm) + " (100 = normal)"},
                {"Energy production", fmt0(e.energyIdx)}, {"Oil", "$" + fmt0(e.oil) + " a barrel"}};
        y = 482;
        for (String[] r : ind) { text(g, r[0], px + 18, y, F_S, C_TEXT); textR(g, r[1], px + pw - 18, y, F_SB, Color.white); y += 25; }
        u.panel(g, px, 654, pw, 158, "The Federal Reserve");
        text(g, "Chair: " + e.fedChair, px + 18, 692, F_S, Color.white);
        text(g, "Target rate " + Economy.rate(e.fed), px + 18, 714, F_S, C_TEXT);
        double gap = e.fedTarget - e.fed;
        text(g, Math.abs(gap) < 0.15 ? "Markets expect the Fed to hold." : gap > 0 ? "Markets expect the Fed to raise rates." : "Markets expect the Fed to cut.", px + 18, 736, F_XS, C_DIM);
        wrap(g, "You'll be able to nominate Fed governors and lean on the chair in a later update, at a cost to the Fed's credibility.", px + 18, 760, pw - 36, F_T, C_FAINT);
    }

    static double[] col(Game game, int c) {
        double[] v = new double[game.hist.size()];
        for (int i = 0; i < v.length; i++) v[i] = game.hist.get(i)[c];
        return v;
    }

    static double minOf(double[] v) { double m = Double.MAX_VALUE; for (double x : v) m = Math.min(m, x); return v.length == 0 ? 0 : m; }

    static double maxOf(double[] v) { double m = -Double.MAX_VALUE; for (double x : v) m = Math.max(m, x); return v.length == 0 ? 1 : m; }

    // ---- the residence ----
    static void residence(ResoluteDesk u, Graphics2D g, Game game) {
        u.panel(g, 48, 116, 560, 696, "How you're holding up");
        Portrait.draw(g, game.pres, 68, 156, 150, 180, u.tsec, 0, false, true);
        text(g, game.pres.name(), 236, 186, F_H2, Color.white);
        text(g, "Age " + game.presAge(), 236, 208, F_S, C_DIM);
        if (game.pres.spouse != null) text(g, "Spouse: " + game.pres.spouse, 236, 230, F_S, C_DIM);
        String[][] rows = {{"Stamina", String.valueOf(Math.round(game.stamina))}, {"Sleep", fmt1(game.sleep) + " hours a night"}, {"Stress", String.valueOf(Math.round(game.stress))},
                {"Fitness", String.valueOf(Math.round(game.fitness))}, {"Health", String.valueOf(Math.round(game.health))}};
        double y = 370;
        for (String[] r : rows) { text(g, r[0], 68, y, F_S, C_TEXT); textR(g, r[1], 588, y, F_BB, Color.white); y += 30; }
        String advice = game.sick > 0 ? "You're unwell. Keep the schedule light until you recover." : game.stamina < 30 ? "The physician wants you to rest: exhaustion makes gaffes and illness more likely."
                : game.stress > 70 ? "Your stress is high. Family time and exercise help; so does a weekend at Camp David." : game.sleep < 5.8 ? "You aren't sleeping enough. Fewer late nights and evening events." : "The White House physician says you're in good shape for the job.";
        wrap(g, advice, 68, y + 20, 520, F_S, game.stamina < 30 || game.stress > 70 || game.sick > 0 ? C_AMBER : C_GOOD);
        wrap(g, "Long days cost more as presidents age. Exercise builds fitness slowly; sleep and rest restore stamina week to week.", 68, y + 70, 520, F_XS, C_DIM);
        u.panel(g, 624, 116, DW - 48 - 624, 696, "Stamina and stress");
        List<double[]> ser = new ArrayList<double[]>();
        ser.add(col(game, H_STAMINA)); ser.add(col(game, H_STRESS));
        Draw.chart(g, 672, 160, DW - 48 - 624 - 80, 600, ser, new Color[]{C_GOOD, C_AMBER}, new String[]{"Stamina", "Stress"}, 0, 100, "%.0f", Math.max(27, game.hist.size()));
    }

    // ---- the rooms still to come ----
    static void later(ResoluteDesk u, Graphics2D g, Game game) {
        u.panel(g, 48, 116, DW - 96, 696, ROOM_NAME[room]);
        text(g, "Opens in a later update", 80, 190, F_H1, C_GOLD);
        wrap(g, ROOM_LATER[room], 80, 226, 900, F_B, C_TEXT);
        double y = 330;
        if (room == R_CONGRESS) {
            Era e = game.era;
            label(g, "Congress today", 80, y, C_GOLD);
            y += 28;
            text(g, "House: " + game.houseD + " Democrats, " + (435 - game.houseD) + " Republicans. Speaker " + (game.houseD >= 218 ? e.speakerD : e.speakerR) + ".", 80, y, F_S, C_TEXT); y += 24;
            text(g, "Senate: " + game.senateD + " Democrats, " + (100 - game.senateD) + " Republicans. Majority leader " + (game.senateD > 50 || game.senateD == 50 && game.party == DEM ? e.leaderD : e.leaderR) + ".", 80, y, F_S, C_TEXT); y += 24;
            text(g, "Your relationship with the Hill: " + Math.round(game.hill) + " of 100. Hill meetings on your calendar improve it.", 80, y, F_S, C_TEXT);
        }
        if (room == R_WESTWING && game.vp != null) {
            Portrait.draw(g, game.vp, 80, y, 120, 144, u.tsec, 0, false, true);
            text(g, "Vice President " + game.vp.name(), 220, y + 30, F_H2, Color.white);
            wrap(g, game.vp.role, 220, y + 54, 600, F_S, C_DIM);
        }
    }
}
