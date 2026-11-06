package resolutedesk.ui;

import static resolutedesk.congress.CongressRules.*;
import static resolutedesk.core.Dates.*;
import static resolutedesk.core.Fmt.*;
import static resolutedesk.executive.Posts.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.home.PoliticsData.*;
import static resolutedesk.model.Attributes.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.opinion.Issues.*;
import static resolutedesk.press.Outlets.*;
import static resolutedesk.press.SpeechData.*;
import static resolutedesk.ui.Draw.*;
import static resolutedesk.ui.Fonts.*;
import static resolutedesk.ui.Theme.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import resolutedesk.ResoluteDesk;
import resolutedesk.congress.Hill;
import resolutedesk.congress.Member;
import resolutedesk.executive.Person;
import resolutedesk.home.Home;
import resolutedesk.home.HomeFront;
import resolutedesk.model.Cand;
import resolutedesk.model.Game;
import resolutedesk.press.Polls;
import resolutedesk.press.QA;
import resolutedesk.press.Social;
import resolutedesk.press.Speech;

/** The Press Briefing Room: press conferences, speeches, posts, polls, your party and the campaign. */
public final class PressRoom {
    public static int tab, outlet = 14, pIssue = PI_ECON, pTone = 0, aIssue = PI_ECON, pollIssue = PI_ECON;
    static String msg;
    public static final String[] TABS = {"The press", "Speeches", "Social media", "Your party", "The home front", "The campaign"};

    static void draw(ResoluteDesk u, Graphics2D g, Game game) {
        if (game.home == null) HomeFront.setup(game);
        u.segmented(g, 48, 116, 900, 30, TABS, tab, i -> { tab = i; msg = null; }, null);
        if (msg != null) textFit(g, msg, 968, 137, DW - 1016, F_XS, C_GOLD);
        switch (tab) {
            case 1: speeches(u, g, game); break;
            case 2: social(u, g, game); break;
            case 3: party(u, g, game); break;
            case 4: home(u, g, game); break;
            case 5: CampaignUI.draw(u, g, game); break;
            default: press(u, g, game); break;
        }
    }

    static String[] issues() { String[] s = new String[NPI]; for (int i = 0; i < NPI; i++) s[i] = PISSUE[i]; return s; }

    // ---- the press ----
    static void press(ResoluteDesk u, Graphics2D g, Game game) {
        Home h = game.home;
        u.panel(g, 48, 160, 760, 652, "The press corps");
        text(g, "Outlet", 66, 196, F_TB, C_FAINT); text(g, "Audience", 330, 196, F_TB, C_FAINT); text(g, "Coverage of you", 430, 196, F_TB, C_FAINT);
        double y = 212;
        for (int o = 0; o < OUT_NAME.length; o++) {
            text(g, OUT_NAME[o], 66, y + 14, F_XS, Color.white);
            text(g, OUT_AUD[o] < 1 ? Math.round(OUT_AUD[o] * 1000) + "K" : Math.round(OUT_AUD[o]) + "M", 330, y + 14, F_XS, C_DIM);
            bar(g, 430, y + 6, 260, 8, h.tone[o] / 100, h.tone[o] >= 60 ? C_GOOD : h.tone[o] >= 40 ? C_GOLD : C_BAD);
            textR(g, h.tone[o] >= 60 ? "friendly" : h.tone[o] >= 40 ? "mixed" : "hostile", 790, y + 14, F_T, C_DIM);
            y += 30;
        }
        text(g, "Relations with the press corps: " + Math.round(h.press) + " of 100", 66, 800, F_XS, C_TEXT);
        double px = 822, pw = DW - 48 - px;
        u.panel(g, px, 160, pw, 300, "Face the press");
        Person ps = game.ex.staff[S_PRESS];
        wrap(g, (ps != null ? "Your press secretary, " + ps.name + "," : "Your press office") + " briefs the press every day. A press conference or an interview is you, on the record, answering three questions.", px + 18, 196, pw - 36, F_XS, C_DIM);
        boolean pc = game.week - h.lastPresser >= 2, iv = game.week - h.lastInterview >= 2;
        u.button(g, px + 18, 246, pw - 36, 36, pc ? "Hold a press conference" : "Next press conference in a week", pc ? B_GOLD : B_OFF, pc ? () -> { QA.start(game, 0, 0); msg = null; } : null, "Three questions from the White House press corps. Costs some stamina.");
        text(g, "Sit-down interview with", px + 18, 312, F_S, C_TEXT);
        u.button(g, px + 200, 294, pw - 218, 28, OUT_NAME[outlet], B_NORM, () -> {
            List<Integer> l = new ArrayList<Integer>();
            for (int o = 0; o < OUT_NAME.length; o++) if (OUT_SIT[o]) l.add(o);
            String[] n = new String[l.size()];
            for (int i = 0; i < n.length; i++) n[i] = OUT_NAME[l.get(i)] + "  (" + Math.round(OUT_AUD[l.get(i)]) + "M, " + (h.tone[l.get(i)] >= 60 ? "friendly" : h.tone[l.get(i)] >= 40 ? "mixed" : "hostile") + ")";
            u.openPopup(Math.min(u.mx, DW - 360), 200, 340, "Which outlet?", n, j -> outlet = l.get(j));
        });
        u.button(g, px + 18, 334, pw - 36, 36, iv ? "Do the interview" : "Next interview in a week", iv ? B_PRIMARY : B_OFF, iv ? () -> QA.start(game, 1, outlet) : null, "Hostile outlets are harder, but a good showing reaches people who don't usually like you.");
        text(g, "Press conferences: " + h.pressers + ".  Interviews: " + h.interviews + ".", px + 18, 400, F_XS, C_DIM);
        u.panel(g, px, 470, pw, 342, "Commission a poll");
        text(g, "Issue", px + 18, 516, F_S, C_TEXT);
        u.button(g, px + 90, 498, pw - 108, 28, PISSUE[pollIssue], B_NORM, () -> u.openPopup(Math.min(u.mx, DW - 320), 300, 300, "Poll on which issue?", issues(), j -> pollIssue = j));
        u.button(g, px + 18, 538, pw - 36, 32, "Run the poll (" + partyMoney(0.5) + " from the party)", game.funds >= 0.5 ? B_NORM : B_OFF, game.funds >= 0.5 ? () -> { Polls.commission(game, pollIssue); msg = "The poll results are in."; } : null);
        if (h.lastPoll != null) wrap(g, h.lastPoll, px + 18, 594, pw - 36, F_XS, C_TEXT);
        else wrap(g, "Your pollster breaks down how a group sees you on one issue, before you act on it.", px + 18, 594, pw - 36, F_XS, C_DIM);
    }

    // ---- speeches ----
    static void speeches(ResoluteDesk u, Graphics2D g, Game game) {
        Home h = game.home;
        u.panel(g, 48, 160, 760, 652, "An address from the Oval Office");
        wrap(g, "Ask the networks for fifteen minutes of prime time. You build the speech line by line: an opening, three passages and a close. Your speaking skill, a consistent tone and the issues you pick decide how it lands.", 66, 196, 720, F_S, C_TEXT);
        text(g, "Topic for the first passage", 66, 270, F_S, C_TEXT);
        u.button(g, 300, 252, 488, 28, PISSUE[aIssue], B_NORM, () -> u.openPopup(Math.min(u.mx, DW - 320), 260, 300, "Address the nation on...", issues(), j -> aIssue = j));
        boolean can = game.week - h.lastAddress >= 8 && h.spType < 0;
        u.button(g, 66, 300, 720, 40, can ? "Request airtime for tonight" : h.spType >= 0 ? "A speech is under way" : "The networks will give you time again in " + (8 - (game.week - h.lastAddress)) + " weeks", can ? B_GOLD : B_OFF,
                can ? () -> { game.issueSal[aIssue] = Math.min(2, game.issueSal[aIssue] + 0.15); Speech.start(game, 0); } : null);
        text(g, "Speeches given: " + h.speeches, 66, 370, F_XS, C_DIM);
        label(g, "Your speaking", 66, 420, C_GOLD);
        Cand p = game.pres;
        String[] n = {"Speaking", "Charisma", "Discipline", "Debate"};
        int[] a = {A_SPE, A_CHA, A_DIS, A_DEB};
        double y = 446;
        for (int i = 0; i < 4; i++) { text(g, n[i], 66, y, F_XS, C_TEXT); bar(g, 200, y - 8, 400, 7, p.a[a[i]] / 100.0, WestWing.good(p.a[a[i]])); textR(g, String.valueOf(p.a[a[i]]), 640, y, F_XSB, Color.white); y += 22; }
        double px = 822, pw = DW - 48 - px;
        u.panel(g, px, 160, pw, 652, "The State of the Union");
        int yr = dateOf(game.day())[0];
        wrap(g, (h.sotuYear == yr ? "You gave this year's address." : "Each winter you address a joint session of Congress, live in prime time, to about 30 million people. It comes up on its own in late January or early February.")
                + " It works like an Oval Office address, with a bigger audience and the other party sitting in front of you.", px + 18, 196, pw - 36, F_S, C_TEXT);
    }

    static String mediumOf(int yr) { return yr < 1950 ? "radio" : yr < 1995 ? "television" : yr < 2009 ? "cable news and the internet" : "social media"; }

    // ---- social media ----
    static void social(ResoluteDesk u, Graphics2D g, Game game) {
        Home h = game.home;
        int yr = yearNow(game);
        String medium = mediumOf(yr);
        u.panel(g, 48, 160, 760, 652, yr < 2009 ? "A statement by " + medium : "Post");
        text(g, "About", 66, 212, F_S, C_TEXT);
        u.button(g, 160, 194, 628, 28, PISSUE[pIssue], B_NORM, () -> u.openPopup(Math.min(u.mx, DW - 320), 200, 300, "Post about...", issues(), j -> pIssue = j));
        text(g, "Tone", 66, 256, F_S, C_TEXT);
        u.segmented(g, 160, 238, 628, 28, POST_TONE, pTone, i -> pTone = i, new String[]{"Lifts your standing a little.", "Helps on the issue.", "Fires up your base; the other side digs in. Riskier.", "Humanizes you.", "Can land well with young voters, or badly."});
        int left = h.postWeek == game.week ? 5 - h.postsThisWeek : 5;
        u.button(g, 66, 290, 722, 40, left > 0 ? (yr < 2009 ? "Make the statement (" : "Post it (") + left + " left this week)" : "No more this week", left > 0 ? B_GOLD : B_OFF, left > 0 ? () -> msg = Social.post(game, pIssue, pTone) : null);
        wrap(g, yr < 2009 ? "Before social media, a president reached the country through " + medium + ". Your reach depends on your gifts as a speaker and how often you go to the well: the fifth statement of the week gets less attention than the first."
                : "Your reach depends on your feel for social media and how often you post: the fifth post of the week gets less attention than the first. Undisciplined presidents set off backlashes.", 66, 360, 720, F_XS, C_DIM);
        double px = 822, pw = DW - 48 - px;
        u.panel(g, px, 160, pw, 652, yr < 2009 ? "Your audience" : "Your accounts");
        if (yr >= 2009) text(g, fmt1(h.followers) + " million followers", px + 18, 210, F_H2, Color.white);
        else text(g, "About " + Math.round(yr < 1950 ? 50 : yr < 1990 ? 70 : 60) + " million listening", px + 18, 210, F_H2, Color.white);
        text(g, h.posts + (yr < 2009 ? " statements so far" : " posts so far"), px + 18, 236, F_S, C_DIM);
        text(g, yr < 2009 ? "Speaking gifts: " + (game.pres.a[A_SPE] + game.pres.a[A_CHA]) / 2 : "Feel for social media: " + game.pres.a[A_SMD], px + 18, 266, F_S, C_TEXT);
    }

    // ---- your party ----
    static void party(ResoluteDesk u, Graphics2D g, Game game) {
        Home h = game.home;
        u.panel(g, 48, 160, 760, 652, "Your party");
        String[] f = HomeFront.factions(game);
        double y = 210;
        for (int i = 0; i < 2; i++) {
            text(g, f[i], 66, y, F_H3, Color.white);
            bar(g, 66, y + 10, 600, 10, h.fac[i] / 100, WestWing.good(h.fac[i]));
            textR(g, Math.round(h.fac[i]) + " satisfied", 788, y + 20, F_SB, Color.white);
            y += 56;
        }
        wrap(g, "The two wings judge you by where your laws, orders and choices have landed, and by how popular you are. Unhappy wings cool on you in Congress, and an unhappy base turns out less for the midterms.", 66, y, 720, F_XS, C_DIM);
        y += 60;
        String side = h.recW < 0.5 ? "You haven't done enough yet to have a record." : "Your record so far sits " + (Math.abs(h.recE) < 0.15 ? "in the center" : h.recE < 0 ? "to the left" : "to the right") + " on the economy and " + (Math.abs(h.recC) < 0.15 ? "in the center" : h.recC < 0 ? "to the left" : "to the right") + " on social issues.";
        wrap(g, side, 66, y, 720, F_S, C_TEXT);
        double px = 822, pw = DW - 48 - px;
        u.panel(g, px, 160, pw, 652, "The opposition");
        text(g, PARTIES[Hill.other(game.party)], px + 18, 206, F_H2, partyLight(Hill.other(game.party)));
        text(g, "Strategy: " + OPP_STRAT[h.opp], px + 18, 232, F_S, h.opp == 2 ? C_BAD : h.opp == 0 ? C_GOOD : C_GOLD);
        wrap(g, "They decide each quarter how to deal with you, mostly on your approval: a popular president gets cooperation, an unpopular one gets obstruction, and everyone gets obstruction near an election.", px + 18, 256, pw - 36, F_XS, C_DIM);
        double yy = 320;
        for (Member m : game.cong.members) if (m.role == RO_LEADER && m.party != game.party) {
            Portrait.draw(g, m.face, px + 18, yy, 56, 66, u.tsec, 0, true, true);
            text(g, m.name, px + 86, yy + 22, F_SB, Color.white);
            text(g, m.title, px + 86, yy + 40, F_XS, C_DIM);
            yy += 80;
        }
    }

    // ---- the home front ----
    static void home(ResoluteDesk u, Graphics2D g, Game game) {
        Home h = game.home;
        u.panel(g, 48, 160, 760, 652, "Conditions at home");
        double y = 204;
        SitRoom.gauge(g, "Violent crime, an index (2025 = 100)", (h.crime - 60) * 1.2, true, 66, y, 720, String.valueOf(Math.round(h.crime))); y += 40;
        SitRoom.gauge(g, "Overdose deaths a year", h.od * 0.8, true, 66, y, 720, Math.round(h.od) + "k"); y += 40;
        SitRoom.gauge(g, "Protest in the streets", h.protest, true, 66, y, 720); y += 40;
        SitRoom.gauge(g, "Strain on the power grid", h.grid, true, 66, y, 720); y += 40;
        String ps = h.pand == 0 ? "No pandemic" : h.pand == 1 ? "A new virus is spreading abroad" : h.pand == 2 ? "Pandemic: about " + commas(h.deaths) + " Americans have died" : "The pandemic is over: " + commas(h.deaths) + " Americans died";
        text(g, ps, 66, y + 10, F_SB, h.pand == 2 ? C_BAD : h.pand == 1 ? C_AMBER : C_TEXT);
        y += 40;
        wrap(g, "Disasters, blackouts, strikes, shootings and fights with the states arrive as events. How well your departments respond depends on their readiness.", 66, y, 720, F_XS, C_DIM);
        double px = 822, pw = DW - 48 - px;
        u.panel(g, px, 160, pw, 652, "Disasters");
        double yy = 198;
        if (h.disasters.isEmpty()) text(g, "None so far.", px + 18, yy, F_XS, C_DIM);
        for (int i = h.disasters.size() - 1; i >= 0 && yy < 790; i--) { textFit(g, h.disasters.get(i), px + 18, yy, pw - 36, F_XS, C_TEXT); yy += 20; }
    }
}
