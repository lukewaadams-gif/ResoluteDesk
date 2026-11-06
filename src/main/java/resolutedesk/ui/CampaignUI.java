package resolutedesk.ui;

import static resolutedesk.campaign.CampaignData.*;
import static resolutedesk.core.Fmt.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.data.ElectoralMap.*;
import static resolutedesk.data.StateData.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.ui.Draw.*;
import static resolutedesk.ui.Fonts.*;
import static resolutedesk.ui.Theme.*;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import resolutedesk.ResoluteDesk;
import resolutedesk.campaign.Reelect;
import resolutedesk.campaign.Reelection;
import resolutedesk.campaign.Terms;
import resolutedesk.model.Cand;
import resolutedesk.model.Game;

/** The campaign tab of the Press Briefing Room: tickets, polls, the message, money and the calendar. */
public final class CampaignUI {
    static void draw(ResoluteDesk u, Graphics2D g, Game game) {
        Reelect re = game.re;
        if (re == null) {
            u.panel(g, 48, 160, DW - 96, 652, "The next election");
            int y = Reelection.year(game);
            boolean can = Terms.eligible(game);
            wrap(g, "The " + y + " presidential election is " + Math.max(0, Reelection.weeksTo(game)) + " weeks away. " + (can ? "About a year before it, you'll decide whether to run for another term." : "You can't run again: the Constitution limits a president to two terms. You can still help your party's nominee in the fall.")
                    + " Until then, everything you do here is the campaign: the economy, your approval, your record in Congress and abroad.", 66, 200, 900, F_S, C_TEXT);
            return;
        }
        Cand me = re.nominee != null ? re.nominee : game.pres;
        double lx = 48, lw = 700;
        u.panel(g, lx, 160, lw, 652, re.year + ": " + (re.running ? "your campaign" : me.name() + "'s campaign"));
        Portrait.draw(g, me, lx + 18, 196, 96, 116, u.tsec, 0, true, true);
        text(g, me.name(), lx + 126, 218, F_H3, partyColor(game.party));
        text(g, re.running ? "The incumbent" : re.denied ? "Nominee after your defeat in the primaries" : "Your party's nominee", lx + 126, 238, F_XS, C_DIM);
        if (re.opp != null) {
            Portrait.draw(g, re.opp, lx + lw - 114, 196, 96, 116, u.tsec, 0, true, true);
            textR(g, re.opp.name(), lx + lw - 126, 218, F_H3, partyColor(re.opp.party));
            textR(g, re.opp.role != null ? re.opp.role : "", lx + lw - 126, 238, F_XS, C_DIM);
        } else textR(g, "The other party hasn't chosen yet", lx + lw - 126, 218, F_S, C_DIM);
        double y = 340;
        if (re.stage == 1) { wrap(g, "You haven't said yet whether you're running. Your decision is in the inbox.", lx + 18, y, lw - 36, F_S, C_AMBER); return; }
        double m = re.nat;
        text(g, "National polls", lx + 18, y, F_SB, C_TEXT);
        textR(g, (m >= 0 ? me.last + " +" : (re.opp != null ? re.opp.last : "Opponent") + " +") + fmt1(Math.abs(m)), lx + lw - 18, y, F_H3, m >= 0 ? C_GOOD : C_BAD);
        y += 16;
        double share = clamp(50 + m / 2, 5, 95);
        g.setColor(partyColor(game.party)); g.fill(new Rectangle2D.Double(lx + 18, y, (lw - 36) * share / 100, 12));
        g.setColor(partyColor(other(game.party))); g.fill(new Rectangle2D.Double(lx + 18 + (lw - 36) * share / 100, y, (lw - 36) * (1 - share / 100), 12));
        y += 34;
        int fev = Reelection.forecastEv(game);
        text(g, "Electoral votes if the election were today: " + fev + " of " + evTotal(evFor(re.year)) + " (" + (evTotal(evFor(re.year)) / 2 + 1) + " to win)", lx + 18, y, F_S, fev * 2 > evTotal(evFor(re.year)) ? C_GOOD : C_AMBER);
        y += 30;
        if (re.challenger != null) {
            text(g, "Primary challenge: " + re.challenger + (re.chalShare > 0 ? ", " + Math.round(re.chalShare) + "% of the vote so far" : ""), lx + 18, y, F_S, re.chalShare > 45 ? C_BAD : C_AMBER);
            y += 26;
        }
        label(g, "Message", lx + 18, y, C_GOLD); y += 10;
        u.segmented(g, lx + 18, y, lw - 36, 30, THEME, re.theme, i -> re.theme = i, THEME_DESC);
        y += 44;
        label(g, "Money", lx + 18, y, C_GOLD); y += 12;
        text(g, "Ground game", lx + 18, y + 14, F_XS, C_TEXT);
        u.slider(g, lx + 120, y + 8, lw - 260, re.adsMix / 100.0, C_GOLD, v -> re.adsMix = (int) Math.round(v * 100), "How your money is split between television ads and organizers.");
        textR(g, "Television " + re.adsMix + "%", lx + lw - 18, y + 14, F_XS, C_TEXT);
        y += 40;
        text(g, "Party war chest: " + partyMoney(game.funds) + ". Fundraisers on your calendar add to it.", lx + 18, y, F_XS, C_DIM);
        y += 30;
        label(g, "The calendar", lx + 18, y, C_GOLD); y += 18;
        int wt = Reelection.weeksTo(game);
        String[] steps = {"Primaries", "The other party's convention", "Your convention", "Labor Day: the fall campaign", "Debates", "Election Day"};
        int[] at = {44, 15, 11, 9, Reelection.debatesHeld(re.year) ? 6 : -1, 0};
        for (int i = 0; i < steps.length; i++) {
            if (at[i] < 0) continue;
            boolean past = wt < at[i] || i == 5 && re.done;
            text(g, (past ? "Done: " : "In " + Math.max(0, wt - at[i]) + " weeks: ") + steps[i], lx + 18, y, F_XS, past ? C_FAINT : C_TEXT);
            y += 18;
        }
        if (Reelection.season(game)) wrap(g, "It's the fall campaign: schedule campaign stops in swing states from your calendar (the Desk). Each one moves that state.", lx + 18, y + 6, lw - 36, F_XS, C_GOLD);
        // the map
        double mx = lx + lw + 16, mw = DW - 48 - mx;
        u.panel(g, mx, 160, mw, 652, "The map, by the polls");
        for (int s = 0; s < NS; s++) {
            if (!votes(s, re.year)) { USMap.paint[s] = rgb(0x2a3346); USMap.tags[s] = ""; continue; }
            double sm = game.party == DEM ? re.st[s] : -re.st[s];
            USMap.paint[s] = marginColor(sm, 1);
            USMap.tags[s] = String.valueOf(STATES[s].ev);
        }
        USMap.markState = -1; USMap.markCand = null;
        double[] f = USMap.draw(u, g, mx + 10, 196, mw - 20, 420, -1, true);
        int hs = USMap.stateAt(f, u.mx, u.my);
        if (hs >= 0 && votes(hs, re.year)) u.tip = STATES[hs].name + " (" + STATES[hs].ev + "): " + (re.st[hs] >= 0 ? me.last : re.opp != null ? re.opp.last : "the other side") + " +" + fmt1(Math.abs(re.st[hs])) + (re.effort[hs] > 0 ? ". Your stops: " + fmt1(re.effort[hs]) : "");
        USMap.scale(g, mx + 18, 640, C_REP, C_STATE, C_DEM, "R+20", "D+20", "margin in the polls");
        // the closest states
        Integer[] ord = new Integer[NS];
        for (int i = 0; i < NS; i++) ord[i] = i;
        Arrays.sort(ord, (a, b) -> Double.compare(Math.abs(re.st[a]), Math.abs(re.st[b])));
        text(g, "Closest states:", mx + 18, 690, F_XSB, C_TEXT);
        double xx = mx + 130;
        for (int n = 0, k = 0; n < NS && k < 6; n++) {
            int s = ord[n];
            if (!votes(s, re.year)) continue;
            String t = STATES[s].ab + " " + (re.st[s] >= 0 ? "+" : "") + fmt1(re.st[s]);
            text(g, t, xx, 690, F_XS, re.st[s] >= 0 ? C_GOOD : C_BAD);
            xx += strW(g, t, F_XS) + 16;
            k++;
        }
        if (re.done) u.button(g, mx + 18, 720, 260, 34, "Election night results", B_GOLD, () -> Night.begin(u));
    }
}
