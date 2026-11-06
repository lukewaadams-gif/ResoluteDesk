package resolutedesk.world;

import static resolutedesk.core.Dates.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.model.Attributes.*;
import static resolutedesk.model.NewsKinds.*;
import static resolutedesk.opinion.Issues.*;
import static resolutedesk.sim.DecisionKinds.*;
import static resolutedesk.world.Countries.*;
import static resolutedesk.world.WorldData.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import resolutedesk.model.Game;
import resolutedesk.sim.Decision;
import resolutedesk.sim.Decisions;

/** Summits: a bilateral one is played in three rounds; the big annual ones are a single choice. */
public final class Summit {
    static final String[][] ASKS = {
            {"A trade deal", "trade"}, {"A deal on fentanyl chemicals", "fent"}, {"Lower tensions over Taiwan", "taiwan"}, {"A ceasefire in Ukraine", "ukraine"},
            {"A new nuclear arms treaty", "arms"}, {"Limits on Iran's nuclear program", "iran"}, {"A freeze on North Korea's tests", "nk"}, {"More defense spending by allies", "spend"},
            {"Help at the border", "border"}, {"More oil on the market", "oil"}, {"An easing over Berlin", "berlin"}, {"A pledge of friendship and cooperation", "friend"}};

    static List<Integer> asks(Game g, int c) {
        int y = yearNow(g);
        List<Integer> l = new ArrayList<Integer>();
        if (tradeOf(g, c) > 20 * COST_SCALE) l.add(0);
        if (y >= 2016 && (c == CC_CN || c == CC_MX || c == CC_IN)) l.add(1);
        if (y >= 1972 && c == CC_CN) l.add(2);
        if (y >= 2014 && (c == CC_RU || c == CC_UA)) l.add(3);
        if (y >= 1955 && c == CC_RU || y >= 1990 && c == CC_CN) l.add(4);
        if (y >= 2003 && c == CC_IR) l.add(5);
        if (y >= 1994 && c == CC_KP) l.add(6);
        if (y >= 1950 && align(g, c) == AL_TREATY) l.add(7);
        if (y >= 1980 && (c == CC_MX || c == CC_PA || c == CC_VE || c == CC_CU)) l.add(8);
        if (y >= 1973 && (c == CC_SA || c == CC_AE || c == CC_QA || c == CC_CA)) l.add(9);
        if (y >= 1948 && y < 1972 && (c == CC_RU || c == CC_DE)) l.add(10);
        if (l.isEmpty()) l.add(11);
        return l;
    }

    public static void start(Game g) {
        World w = g.world;
        int c = w.summitCountry;
        w.summitRound = 1; w.summitMood = w.rel[c] / 2 + 10; w.summitLeverage = 0;
        g.stamina = Math.max(0, g.stamina - 6);
        Decision d = Decisions.add(g, D_SUMMIT, "Summit with " + w.leader[c] + ": the opening", "You sit down with " + w.leader[c] + " of " + cnameIn(g, c) + ". Relations stand at " + Math.round(w.rel[c]) + ". How do you open?",
                new String[]{"Warmly: talk about what you share", "Firmly: lay out your red lines", "Personally: flatter and charm"},
                new String[]{"Secretary of State: \"Build trust first; the asks come later.\"", "National security adviser: \"Show strength and they'll take you seriously.\"", "Chief of staff: \"You're good in a room. Use it.\""});
        d.a = c;
    }

    public static void round(Game g, int k) {
        World w = g.world;
        int c = w.summitCountry;
        Random r = g.rng;
        double cha = g.pres.a[A_CHA], fpc = g.pres.a[A_FPC];
        if (w.summitRound == 1) {
            if (k == 0) w.summitMood += 10 + (fpc - 60) / 6; else if (k == 1) { w.summitMood -= 8; w.summitLeverage += 15; } else w.summitMood += (cha - 55) / 2 + r.nextGaussian() * 6;
            w.summitRound = 2;
            List<Integer> l = asks(g, c);
            String[] lab = new String[l.size()];
            for (int i = 0; i < lab.length; i++) lab[i] = ASKS[l.get(i)][0];
            Decision d = Decisions.add(g, D_SUMMIT, "Summit with " + w.leader[c] + ": the ask", "The mood in the room is " + (w.summitMood > 35 ? "warm" : w.summitMood > 5 ? "businesslike" : "frosty") + ". What do you ask for?", lab, null);
            d.codes = new int[l.size()];
            for (int i = 0; i < l.size(); i++) d.codes[i] = l.get(i);
        } else if (w.summitRound == 2) {
            w.summitAsk = k;
            w.summitRound = 3;
            Decision d = Decisions.add(g, D_SUMMIT, "Summit with " + w.leader[c] + ": the give", "They want something in return. What do you put on the table?",
                    new String[]{"Lift some tariffs or sanctions", "More aid or investment", "Nothing: hold firm"},
                    new String[]{"Treasury secretary: \"A little relief buys a lot of goodwill.\"", "Secretary of State: \"Money talks.\"", "National security adviser: \"Don't pay for what they should do anyway.\""});
        } else {
            w.summitRound = 0;
            int ask = w.summitAsk;
            double give = k == 0 ? 20 : k == 1 ? 15 : -5;
            double chance = logistic((w.summitMood + w.summitLeverage * 0.5 + give - 20) / 18 + (fpc - 60) / 40);
            if (k == 0) { if (w.tariff[c] > 0) Diplo.setTariff(g, c, w.tariff[c] - 1); else if (w.sanction[c] > 0) Diplo.setSanction(g, c, w.sanction[c] - 1); }
            if (k == 1) g.econ.fiscal += 0.01;
            boolean ok = r.nextDouble() < chance;
            finish(g, c, ask, ok);
            w.summitCountry = -1; w.summitWeek = -1;
        }
    }

    static void finish(Game g, int c, int ask, boolean ok) {
        World w = g.world;
        String what = ASKS[ask][1];
        String story;
        if (ok) {
            w.deals++;
            w.rel[c] = clamp(w.rel[c] + 8, -100, 100);
            g.perf += 0.8; g.issueAdj[PI_FOREIGN] = clamp(g.issueAdj[PI_FOREIGN] + 0.15, -1.5, 1.5);
            if (what.equals("trade")) { if (w.tariff[c] > 0) Diplo.setTariff(g, c, 0); g.econ.finShock += 0.4; g.econ.farm = Math.min(140, g.econ.farm + 3); }
            else if (what.equals("fent")) { w.cartel -= 10; g.issueAdj[PI_CRIME] += 0.1; }
            else if (what.equals("taiwan")) { w.twt -= 15; if (w.twStage > 0) w.twStage--; }
            else if (what.equals("ukraine")) { w.talks = Math.min(100, w.talks + 18); }
            else if (what.equals("arms")) { g.issueAdj[PI_SECURITY] += 0.2; w.defcon = 5; }
            else if (what.equals("iran")) { w.iran -= 25; Diplo.oil(g, -2); }
            else if (what.equals("nk")) { w.nk -= 20; }
            else if (what.equals("spend")) { w.allySpend += 0.2; w.nato -= 2; }
            else if (what.equals("border")) { w.migr -= 15; g.issueAdj[PI_IMMIG] += 0.15; }
            else if (what.equals("oil")) Diplo.oil(g, -4);
            else if (what.equals("berlin")) { g.issueAdj[PI_SECURITY] += 0.1; w.defcon = Math.min(5, w.defcon + 1); }
            else if (what.equals("friend")) w.rel[c] = clamp(w.rel[c] + 4, -100, 100);
            story = "A deal with " + cnameIn(g, c) + ": " + ASKS[ask][0].toLowerCase() + ".";
            w.record.add(dateStr(g.day()) + ": " + story);
            g.log(NK_BIG, story.toUpperCase(), null, power(g, c) >= 6 ? 3.5 : 2);
        } else {
            w.rel[c] = clamp(w.rel[c] - 3, -100, 100);
            g.perf -= 0.3;
            story = "The summit with " + w.leader[c] + " ends without a deal.";
            g.log(NK_BAD, story, null, power(g, c) >= 6 ? 2.2 : 1);
        }
        Decisions.info(g, ok ? "A deal" : "No deal", story);
    }

    /** The G7, NATO, the UN and the G20: you go, and pick a theme. */
    static void multilateral(Game g, String name) {
        Decision d = Decisions.add(g, D_GSUMMIT, "The " + name + " summit", "World leaders gather for the " + name + " summit. It will take three days of your time. What's your message?",
                new String[]{"Reassure allies", "Press allies to do more", "Skip it and send the vice president"},
                new String[]{"Secretary of State: \"Allies are nervous. Calm them.\"", "Treasury secretary: \"Make them pay their share.\"", "Chief of staff: \"You need the rest more than they need a photo.\""});
        d.a = name.equals("NATO") ? 1 : 0;
    }

    public static void multiDecision(Game g, Decision d, int k) {
        World w = g.world;
        boolean nato = d.a == 1;
        if (k == 2) { w.nato -= 5; w.pacific -= 3; g.issueAdj[PI_FOREIGN] -= 0.05; g.log(NK_WORLD, "The president skips the summit; the vice president goes instead", null, 1.2); return; }
        g.stamina = Math.max(0, g.stamina - 7);
        if (k == 0) { w.nato += nato ? 4 : 1.5; w.pacific += 1.5; for (int c = 0; c < NC; c++) if (align(g, c) == AL_TREATY) w.rel[c] += 1; }
        else { w.allySpend += 0.15; w.nato -= nato ? 3 : 1; for (int c = 0; c < NC; c++) if (align(g, c) == AL_TREATY) w.rel[c] -= 1.5; g.issueAdj[PI_FOREIGN] += 0.05; }
        g.issueAdj[PI_FOREIGN] = clamp(g.issueAdj[PI_FOREIGN] + 0.05, -1.5, 1.5);
        g.log(NK_WORLD, "The president attends the summit", null, 1.0);
    }
}
