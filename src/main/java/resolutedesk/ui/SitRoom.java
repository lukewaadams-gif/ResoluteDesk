package resolutedesk.ui;

import static resolutedesk.core.Dates.*;
import static resolutedesk.core.Fmt.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.executive.Posts.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.ui.Draw.*;
import static resolutedesk.ui.Fonts.*;
import static resolutedesk.ui.Theme.*;
import static resolutedesk.world.Countries.*;
import static resolutedesk.world.CrisisData.*;
import static resolutedesk.world.WarData.*;
import static resolutedesk.world.WorldData.*;
import static resolutedesk.world.WorldTables.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import resolutedesk.ResoluteDesk;
import resolutedesk.executive.Cabinet;
import resolutedesk.executive.Person;
import resolutedesk.history.History;
import resolutedesk.home.Home;
import resolutedesk.model.Game;
import resolutedesk.world.Diplo;
import resolutedesk.world.War;
import resolutedesk.world.Wars;
import resolutedesk.world.World;

/** The Situation Room: the world map, the daily brief, hot spots, operations, trade, alliances and wars. */
public final class SitRoom {
    public static int tab, sel = CC_CN, selOp = 0;
    static String msg;
    public static final String[] TABS = {"The world", "Daily brief", "Hot spots", "Operations", "Trade", "Alliances", "Wars", "Statecraft"};

    static void draw(ResoluteDesk u, Graphics2D g, Game game) {
        if (game.world == null) Diplo.setup(game);
        u.segmented(g, 48, 116, 860, 30, TABS, tab, i -> { tab = i; msg = null; }, null);
        World w = game.world;
        text(g, "DEFCON " + w.defcon, 924, 137, F_BB, w.defcon <= 3 ? C_BAD : w.defcon == 4 ? C_AMBER : C_GOOD);
        if (msg != null) textFit(g, msg, 1024, 137, DW - 1072, F_XS, C_GOLD);
        switch (tab) {
            case 1: brief(u, g, game); break;
            case 2: hot(u, g, game); break;
            case 3: ops(u, g, game); break;
            case 4: trade(u, g, game); break;
            case 5: alliances(u, g, game); break;
            case 6: wars(u, g, game); break;
            case 7: StatecraftUI.draw(u, g, game); break;
            default: world(u, g, game); break;
        }
    }

    static Color tone(double v, boolean highBad) { double t = highBad ? v : 100 - v; return t >= 70 ? C_BAD : t >= 45 ? C_AMBER : C_GOOD; }

    static void gauge(Graphics2D g, String name, double v, boolean highBad, double x, double y, double w) { gauge(g, name, v, highBad, x, y, w, String.valueOf(Math.round(v))); }

    static void gauge(Graphics2D g, String name, double v, boolean highBad, double x, double y, double w, String shown) {
        text(g, name, x, y, F_XS, C_TEXT);
        bar(g, x, y + 6, w - 50, 8, clamp(v, 0, 100) / 100, tone(v, highBad));
        textR(g, shown, x + w, y + 14, F_XSB, Color.white);
    }

    // ---- the world ----
    static void world(ResoluteDesk u, Graphics2D g, Game game) {
        World w = game.world;
        u.panel(g, 48, 160, 1010, 652, "The world");
        int hv = WorldMap.draw(u, g, game, 60, 186, 986, 500, sel);
        if (hv >= 0 && gone(game, hv)) hv = -1;
        if (hv >= 0) u.tip = cname(game, hv) + ": " + w.leader[hv] + ". Relations " + Math.round(w.rel[hv]) + ".";
        u.hit(60, 186, 986, 500, () -> { int h = WorldMap.at(u.mx, u.my); if (h >= 0 && !gone(game, h)) sel = h; }, null);
        if (gone(game, sel)) for (int k = 0; k < NC; k++) if (!gone(game, k)) { sel = k; break; }
        double ly = 712;
        g.setColor(WorldMap.relColor(-80)); g.fill(new Rectangle2D.Double(66, ly - 9, 14, 10)); text(g, "Hostile", 86, ly, F_T, C_DIM);
        g.setColor(WorldMap.relColor(0)); g.fill(new Rectangle2D.Double(146, ly - 9, 14, 10)); text(g, "Cool", 166, ly, F_T, C_DIM);
        g.setColor(WorldMap.relColor(80)); g.fill(new Rectangle2D.Double(206, ly - 9, 14, 10)); text(g, "Friendly", 226, ly, F_T, C_DIM);
        g.setColor(rgb(0xff5040)); g.fill(new Ellipse2D.Double(296, ly - 8, 7, 7)); text(g, "Flashpoint", 310, ly, F_T, C_DIM);
        text(g, "Click a country. Trips abroad go on your calendar (the Desk); calls and invitations are here.", 66, ly + 22, F_XS, C_DIM);
        if (!w.record.isEmpty()) textFit(g, "Your record: " + w.record.get(w.record.size() - 1), 66, ly + 44, 960, F_XS, C_GOLD);
        // the selected country
        double px = 1072, pw = DW - 48 - px;
        int c = sel;
        u.panel(g, px, 160, pw, 652, cname(game, c));
        Flag.draw(g, c, px + 18, 192, 72, 48);
        text(g, w.leader[c], px + 104, 208, F_H3, Color.white);
        text(g, w.ltitle[c], px + 104, 226, F_XS, C_DIM);
        int al = align(game, c);
        text(g, ALIGN[al] + (nuke(game, c) ? ", nuclear-armed" : "") + ", " + REGION[CT_REGION[c]].toLowerCase(), px + 104, 242, F_T, al == AL_ADVERSARY ? C_REP_L : al == AL_TREATY ? C_GOOD : C_TEXT);
        text(g, "Relations", px + 18, 272, F_XS, C_TEXT);
        g.setColor(new Color(255, 255, 255, 25)); g.fill(new Rectangle2D.Double(px + 18, 280, pw - 76, 10));
        double mid = px + 18 + (pw - 76) / 2;
        g.setColor(WorldMap.relColor(w.rel[c]));
        g.fill(new Rectangle2D.Double(Math.min(mid, mid + w.rel[c] / 100 * (pw - 76) / 2), 280, Math.abs(w.rel[c] / 100 * (pw - 76) / 2), 10));
        textR(g, (w.rel[c] > 0 ? "+" : "") + Math.round(w.rel[c]), px + pw - 18, 290, F_SB, Color.white);
        double y = 314;
        double tr = tradeOf(game, c), tb = tradeBal(game, c);
        if (tr <= 0) text(g, atWar(game, c) ? "No trade with the US: the two countries are at war." : "Next to no trade with the US.", px + 18, y, F_XS, C_TEXT);
        else text(g, "Trade with the US: " + money(tr) + " a year" + (tb < -tr * 0.05 ? ", a " + money(-tb) + " deficit" : tb > tr * 0.05 ? ", a " + money(tb) + " surplus" : ""), px + 18, y, F_XS, C_TEXT);
        y += 18;
        text(g, "Your tariff " + TARIFF_OPT[w.tariff[c]] + ", sanctions " + LEVEL[w.sanction[c]].toLowerCase() + ", aid " + LEVEL[w.aid[c]].toLowerCase(), px + 18, y, F_XS, C_TEXT); y += 18;
        if (w.visits[c] > 0) { text(g, "Visits: " + w.visits[c], px + 18, y, F_XS, C_DIM); y += 18; }
        if (w.visitAt[c] >= 0) { text(g, "Coming to the White House around " + dateStr(w.visitAt[c] * 7), px + 18, y, F_XS, C_GOLD); y += 18; }
        if (w.summitCountry == c) { text(g, "Summit planned around " + dateStr(w.summitWeek * 7), px + 18, y, F_XS, C_GOLD); y += 18; }
        y += 6;
        final int cc = c;
        int calls = w.callWeek == game.week ? 3 - w.callsThisWeek : 3;
        boolean war = atWar(game, c);
        String no = "The United States is at war with " + cnameIn(game, c) + ".";
        u.button(g, px + 18, y, pw - 36, 30, "Call " + w.leader[c] + " (" + calls + " calls left this week)", calls > 0 && !war ? B_NORM : B_OFF, calls > 0 && !war ? () -> msg = Diplo.call(game, cc) : null, war ? no : "A phone call warms relations a little. Calls in quick succession do less.");
        y += 36;
        boolean inv = w.visitAt[c] < 0 && !war;
        u.button(g, px + 18, y, pw - 36, 30, "Invite to the White House", inv ? B_NORM : B_OFF, inv ? () -> msg = Diplo.invite(game, cc) : null, war ? no : "A state visit in a few weeks. A bigger boost than a call.");
        y += 36;
        boolean sum = w.summitCountry < 0 && !war;
        u.button(g, px + 18, y + 36, pw - 36, 30, "Objectives, aid, arms, troops and force", B_GOLD, () -> { tab = 7; msg = null; }, "The Statecraft tab: what you want from " + cnameIn(game, c) + ", and every tool for getting it.");
        u.button(g, px + 18, y, pw - 36, 30, "Propose a summit", sum ? B_PRIMARY : B_OFF, sum ? () -> msg = Diplo.proposeSummit(game, cc) : null, war ? no + " Peace comes through the Wars tab and the turning points." : "A face-to-face negotiation in three rounds: the opening, the ask and the give.");
        y += 82;
        // every country, by region
        label(g, "All countries", px + 18, y, C_GOLD);
        double top = y + 10, hh = 800 - top;
        double off = u.beginScroll(g, "ctlist", px + 10, top, pw - 20, hh);
        int h0 = u.hits.size();
        int row = 0;
        for (int rg = 0; rg < REGION.length; rg++) for (int k = 0; k < NC; k++) {
            if (CT_REGION[k] != rg || gone(game, k)) continue;
            double ry = top - off + row * 24;
            final int kk = k;
            u.card(g, px + 14, ry, pw - 28, 22, k == sel, () -> sel = kk, w.leader[k]);
            Flag.draw(g, k, px + 20, ry + 4, 21, 14);
            text(g, cname(game, k), px + 48, ry + 16, F_XS, Color.white);
            textR(g, (w.rel[k] > 0 ? "+" : "") + Math.round(w.rel[k]), px + pw - 22, ry + 16, F_XSB, WorldMap.relColor(w.rel[k]).brighter());
            row++;
        }
        u.clipHits(h0, top, top + hh);
        u.endScroll(g, "ctlist", row * 24);
    }

    // ---- the President's Daily Brief ----
    static void brief(ResoluteDesk u, Graphics2D g, Game game) {
        Home h = game.home;
        double x = 360, w = DW - 720;
        g.setColor(rgb(0xf3ecd8));
        g.fill(new RoundRectangle2D.Double(x, 164, w, 640, 6, 6));
        g.setColor(rgb(0x7a2a24));
        g.fill(new Rectangle2D.Double(x, 164, w, 10));
        textC(g, "TOP SECRET", x + w / 2, 196, F_TB, rgb(0x7a2a24));
        textC(g, "THE PRESIDENT'S DAILY BRIEF", x + w / 2, 226, F_SERIF, rgb(0x1a1a1a));
        textC(g, dateLong(game.day()), x + w / 2, 248, F_SERIF_S, rgb(0x3a3a3a));
        double y = 288;
        if (h == null || h.brief == null || h.brief.isEmpty()) { text(g, "Your briefer will have the first brief ready next week.", x + 40, y, F_S, rgb(0x333333)); return; }
        for (String item : h.brief) {
            int pr = Integer.parseInt(item.substring(0, 1));
            String t = item.substring(2);
            g.setColor(pr >= 3 ? rgb(0xb3261e) : pr == 2 ? rgb(0xc77d0a) : rgb(0x5a6a7a));
            g.fill(new Rectangle2D.Double(x + 40, y - 12, 6, 16));
            y += wrap(g, t, x + 58, y, w - 100, F_B, rgb(0x1a1a1a)) + 14;
            if (y > 760) break;
        }
        Person dni = game.ex.post[P_DNI];
        int yr = yearNow(game);
        if (!postOn(P_DNI)) dni = game.ex.post[ep(P_DNI)];
        String by = yr >= 2005 ? "the Office of the Director of National Intelligence" : yr >= 1947 ? "the Central Intelligence Agency" : yr >= 1942 ? "the Office of Strategic Services and the service intelligence staffs" : "the State, War and Navy departments";
        text(g, "Prepared by " + by + (dni != null && !dni.real && yr >= 1947 ? " (" + dni.name + ")" : ""), x + 40, 790, F_T, rgb(0x555555));
    }

    // ---- the hot spots ----
    static void hot(ResoluteDesk u, Graphics2D g, Game game) {
        World w = game.world;
        if (!w.modern) { oldHot(u, g, game); return; }
        double cw = (DW - 96 - 24) / 3.0, ch = 316;
        String[] title = {"Taiwan and China", "Russia and Ukraine", "The Middle East", "The Korean Peninsula", "Terrorism and cyber", "The Western Hemisphere"};
        for (int i = 0; i < 6; i++) {
            double x = 48 + (i % 3) * (cw + 12), y = 160 + (i / 3) * (ch + 12);
            u.panel(g, x, y, cw, ch, title[i]);
            double gx = x + 18, gy = y + 66, gw = cw - 36;
            switch (i) {
                case 0:
                    text(g, TW_STAGE[w.twStage], gx, y + 46, F_H3, w.twStage >= 3 ? C_BAD : w.twStage == 2 ? C_AMBER : C_GOOD);
                    gauge(g, "Tension", w.twt, true, gx, gy, gw); gauge(g, "Deterrence", w.deter, false, gx, gy + 34, gw);
                    wrap(g, "Deterrence rises with forces in the Pacific, arms for Taiwan and strong alliances. Tension rises when deterrence slips and relations with Beijing sour.", gx, gy + 84, gw, F_T, C_DIM);
                    break;
                case 1:
                    text(g, UA_STAGE[w.uaStage], gx, y + 46, F_H3, w.uaStage == 2 ? C_AMBER : C_GOOD);
                    gauge(g, "Ukraine's strength", w.ukr, false, gx, gy, gw); gauge(g, "Russian gains", w.ruGain, true, gx, gy + 34, gw); gauge(g, "Progress toward talks", w.talks, false, gx, gy + 68, gw);
                    wrap(g, "Aid keeps Ukraine in the fight; summits and calls move the talks. A ceasefire comes when the talks are far enough along.", gx, gy + 116, gw, F_T, C_DIM);
                    break;
                case 2:
                    gauge(g, "Gaza fighting", w.gaza, true, gx, y + 46, gw); gauge(g, "Iran's nuclear program", w.iran, true, gx, y + 80, gw);
                    gauge(g, "Red Sea attacks", w.redsea, true, gx, y + 114, gw); gauge(g, "Attacks on US troops", w.proxy, true, gx, y + 148, gw);
                    wrap(g, "At 100, Iran has a bomb. Red Sea attacks raise oil prices.", gx, y + 196, gw, F_T, C_DIM);
                    break;
                case 3:
                    gauge(g, "North Korean provocations", w.nk, true, gx, y + 46, gw);
                    wrap(g, "Missile tests, and the risk of a nuclear test. Joint drills with Seoul and Tokyo deter; talks can freeze testing.", gx, y + 96, gw, F_T, C_DIM);
                    break;
                case 4:
                    gauge(g, "Terror threat", w.terror, true, gx, y + 46, gw); gauge(g, "Cyber threat", w.cyber, true, gx, y + 80, gw);
                    wrap(g, "Strong intelligence chiefs and a ready Homeland Security department keep plots from becoming attacks.", gx, y + 128, gw, F_T, C_DIM);
                    break;
                default:
                    gauge(g, "Migration pressure", w.migr, true, gx, y + 46, gw); gauge(g, "Cartel violence", w.cartel, true, gx, y + 80, gw);
                    wrap(g, "Relations with Mexico matter most at the border.", gx, y + 128, gw, F_T, C_DIM);
                    break;
            }
        }
    }

    /** Before the modern theaters: the great powers, the wars and the threats. */
    static void oldHot(ResoluteDesk u, Graphics2D g, Game game) {
        World w = game.world;
        double cw = (DW - 96 - 24) / 3.0;
        u.panel(g, 48, 160, cw, 652, "The great powers");
        Integer[] ord = new Integer[NC];
        for (int i = 0; i < NC; i++) ord[i] = i;
        Arrays.sort(ord, (a, b) -> Double.compare(power(game, b), power(game, a)));
        double y = 200;
        for (int n = 0, shown = 0; n < NC && shown < 14; n++) {
            int c = ord[n];
            if (gone(game, c)) continue;
            Flag.draw(g, c, 66, y - 12, 24, 16);
            text(g, cname(game, c), 98, y, F_XS, Color.white);
            text(g, ALIGN[align(game, c)] + (nuke(game, c) ? ", nuclear" : ""), 98, y + 16, F_T, align(game, c) == AL_ADVERSARY ? C_REP_L : C_DIM);
            textR(g, (w.rel[c] > 0 ? "+" : "") + Math.round(w.rel[c]), 48 + cw - 18, y, F_XSB, WorldMap.relColor(w.rel[c]).brighter());
            y += 42; shown++;
        }
        double x2 = 48 + cw + 12;
        u.panel(g, x2, 160, cw, 652, "America's wars");
        y = 204;
        boolean any = false;
        for (War x : w.wars) {
            if (x.stage == 0) continue;
            any = true;
            text(g, x.name, x2 + 18, y, F_H3, Color.white);
            text(g, x.stage == 2 ? thousands(Math.round(x.troops * 1000)) + " troops, " + thousands(Math.round(x.kia)) + " killed" : WAR_STAGE[x.stage] + ", " + thousands(Math.round(x.kia)) + " killed", x2 + 18, y + 20, F_XS, x.stage == 2 ? C_AMBER : x.stage == 3 ? C_GOOD : C_DIM);
            if (x.stage == 2) { gauge(g, "The front (100 is victory)", x.front, false, x2 + 18, y + 42, cw - 36); gauge(g, "Support at home", x.support, false, x2 + 18, y + 76, cw - 36); y += 120; }
            else y += 50;
        }
        if (!any) wrap(g, "America is at peace. History may not leave it that way: the turning points of the era arrive on their real dates, and how you answer them is up to you.", x2 + 18, y, cw - 36, F_XS, C_DIM);
        u.button(g, x2 + 18, 770, cw - 36, 30, "Run the wars", B_NORM, () -> tab = 6);
        double x3 = x2 + cw + 12;
        u.panel(g, x3, 160, cw, 652, "Threats and alliances");
        int yr = yearNow(game);
        y = 210;
        gauge(g, yr >= 1993 ? "Terror threat" : "Sabotage and subversion", w.terror, true, x3 + 18, y, cw - 36); y += 40;
        gauge(g, "Migration pressure", w.migr, true, x3 + 18, y, cw - 36); y += 40;
        if (yr >= 1949) { gauge(g, "NATO cohesion", w.nato, false, x3 + 18, y, cw - 36); y += 40; }
        if (yr >= 1951) { gauge(g, "Pacific alliances", w.pacific, false, x3 + 18, y, cw - 36); y += 40; }
        y += 10;
        wrap(g, yr < 1941 ? "There is no alliance system yet: America has no treaty allies, and Congress wants to keep it that way." : yr < 1947 ? "A wartime coalition, not yet a standing alliance." : yr < 1991 ? "The Cold War: a contest with the Soviet bloc on every continent, under the shadow of nuclear weapons." : "The Cold War is over. America is the only superpower.", x3 + 18, y, cw - 36, F_XS, C_DIM);
    }

    // ---- the wars ----
    public static int selWar;

    static void wars(ResoluteDesk u, Graphics2D g, Game game) {
        World w = game.world;
        List<War> l = new ArrayList<War>();
        for (War x : w.wars) if (x.stage != 0) l.add(x);
        u.panel(g, 48, 160, 420, 652, "Wars");
        if (l.isEmpty()) { wrap(g, "No wars. America is at peace, for now.", 66, 200, 384, F_S, C_DIM); return; }
        if (selWar >= l.size()) selWar = 0;
        double y = 194;
        for (int i = 0; i < l.size(); i++) {
            War x = l.get(i);
            final int k = i;
            u.card(g, 62, y, 392, 64, selWar == i, () -> selWar = k, null);
            text(g, x.name, 76, y + 24, F_SB, Color.white);
            text(g, x.stage == 2 ? "Fighting: " + thousands(Math.round(x.troops * 1000)) + " troops" : WAR_STAGE[x.stage], 76, y + 46, F_XS, x.stage == 2 ? C_AMBER : x.stage == 3 ? C_GOOD : C_DIM);
            y += 72;
        }
        War x = l.get(selWar);
        double px = 482, pw = DW - 48 - px;
        u.panel(g, px, 160, pw, 652, x.name);
        y = 204;
        String foe = x.enemy >= 0 ? cname(game, x.enemy) : x.id.equals("vietnam") ? "North Vietnam and the Viet Cong" : x.id.equals("afghan") ? "The Taliban and al-Qaeda" : x.id.equals("iraq") ? "The insurgency" : "The Islamic State";
        text(g, "Against: " + foe + (x.ally >= 0 ? ".  Fighting alongside: " + cname(game, x.ally) + "." : "."), px + 18, y, F_S, C_TEXT); y += 26;
        text(g, "Americans killed: " + thousands(Math.round(x.kia)) + "    Weeks of war: " + Math.round(x.weeks) + "    Cost: about " + fmt1(x.troops * x.cost) + "% of GDP a year", px + 18, y, F_S, C_TEXT); y += 22;
        if (x.kind >= 0 && x.enemy >= 0) {
            World wd = game.world;
            text(g, WK_NAME[x.kind] + ".   Unrest inside " + cnameIn(game, x.enemy) + ": " + Math.round(wd.unrest[x.enemy]) + "    Refugees: " + Math.round(wd.refugees) + "    Terror threat: " + Math.round(wd.terror) + (game.home != null ? "    Protest at home: " + Math.round(game.home.protest) : ""),
                    px + 18, y, F_XS, C_DIM);
        }
        y += 22;
        if (x.stage != 2) { text(g, WAR_STAGE[x.stage] + (x.ended >= 0 ? " on " + dateStr(x.ended * 7) : "") + ".", px + 18, y, F_H3, x.stage == 3 ? C_GOOD : C_DIM); return; }
        gauge(g, "The front (0 is defeat, 100 is victory)", x.front, false, px + 18, y, pw - 36); y += 38;
        gauge(g, "Support at home", x.support, false, px + 18, y, pw - 36); y += 38;
        gauge(g, "Progress toward a settlement", x.talks, false, px + 18, y, pw - 36); y += 50;
        label(g, "Troops", px + 18, y, C_GOLD); y += 14;
        text(g, thousands(Math.round(x.troops * 1000)) + " deployed; your generals say about " + thousands(Math.round(x.need * 1000)) + " can hold the line.", px + 18, y + 14, F_S, Color.white);
        y += 26;
        final War xx = x;
        String[] steps = {"Bring home a quarter", "Bring home a tenth", "Send a tenth more", "Send a quarter more"};
        double[] mul = {-0.25, -0.1, 0.1, 0.25};
        double bw = (pw - 36 - 30) / 4;
        for (int i = 0; i < 4; i++) {
            final double m = mul[i];
            u.button(g, px + 18 + i * (bw + 10), y, bw, 30, steps[i], i >= 2 ? B_NORM : B_GHOST, () -> { History.setTroops(game, xx, xx.troops + Math.max(Math.abs(m) * xx.troops, xx.need * 0.05) * Math.signum(m)); game.dirty = true; });
        }
        y += 48;
        label(g, "Strategy", px + 18, y, C_GOLD); y += 10;
        u.segmented(g, px + 18, y, pw - 36, 30, STRATEGY, x.strategy, i -> History.setStrategy(game, xx, i),
                new String[]{"Fewer casualties, slow losses of ground.", "Steady pressure.", "Gains on the front, at a much higher cost in lives.", "Fewer casualties and faster talks, but the enemy gains ground."});
        y += 50;
        wrap(g, "Victory comes when the front reaches 100; defeat at 0. A settlement comes when the talks are complete and the front still holds. Casualties wear down support at home, and support is part of how the public judges your handling of security.", px + 18, y, pw - 36, F_XS, C_DIM);
        y += 50;
        // ways out, and in
        label(g, "Ending it, or not", px + 18, y, C_GOLD); y += 10;
        double bw2 = (pw - 36 - 5 * 8) / 6;
        String[] lab = {"Withdraw now", "Declare victory", "Timetable: 6 months", "Timetable: a year", "Hand over", "Escalate"};
        String hand = Wars.cannotHand(xx);
        String[] tips = {"Bring everyone home now. " + (xx.front >= 60 ? "On terms we can live with." : "History will call it a defeat."),
                "End it now and call it a win. Believable if the front is 70 or better; otherwise allies and enemies take note.",
                "Troops come home gradually over six months. The enemy can count the days.", "A slower drawdown over a year.",
                hand != null ? hand : "Hand the fight to " + Wars.successor(xx) + " over two months.", "More troops and a harder offensive: faster gains, more casualties, less support."};
        Runnable[] acts = {() -> u.modal("End the war?", "Bringing everyone home now ends " + xx.name.toLowerCase() + (xx.front >= 60 ? " on terms we can live with." : " in what history will call a defeat."),
                new String[]{"Keep fighting", "Withdraw"}, new Runnable[]{null, () -> History.withdraw(game, xx)}, C_BAD),
                () -> msg = Wars.declareVictory(game, xx), () -> msg = Wars.timetable(game, xx, 26), () -> msg = Wars.timetable(game, xx, 52), () -> msg = Wars.handoff(game, xx), () -> msg = Wars.escalate(game, xx)};
        for (int i = 0; i < lab.length; i++) {
            boolean ok = (i != 4 || hand == null) && (i < 2 || i > 3 || xx.timetable < 0);
            u.button(g, px + 18 + i * (bw2 + 8), y, bw2, 30, lab[i], ok ? (i == 5 ? B_DANGER : i == 0 ? B_GHOST : B_NORM) : B_OFF, ok ? acts[i] : null, tips[i]);
        }
        y += 44;
        if (xx.kind >= 0) {
            String auth = xx.authorized ? "Congress authorized this war." : xx.wpVoted ? "Congress never authorized this war." : "Congress has not authorized this war; after about sixty days it can vote to end it.";
            text(g, auth, px + 18, y + 18, F_XS, xx.authorized ? C_DIM : C_AMBER);
            if (!xx.authorized && xx.enemy >= 0) u.button(g, px + pw - 18 - 250, y, 250, 26, "Ask Congress to authorize it", B_NORM, () -> msg = Wars.authorize(game, xx.enemy), "A vote in both chambers: " + chance(Wars.authorizeOdds(game, xx.enemy)) + " to pass.");
            y += 30;
        }
        if (xx.timetable >= 0) text(g, "Withdrawal under way: the last troops leave by " + dateStr(xx.timetable * 7) + ".", px + 18, y + 18, F_XS, C_GOLD);
        if (xx.handoff >= 0) text(g, "Handover under way: complete by " + dateStr(xx.handoff * 7) + ".", px + 18, y + 18, F_XS, C_GOLD);
    }

    static String money(double b) { return b >= 10 ? "$" + Math.round(b) + " billion" : b >= 1 ? "$" + fmt1(b) + " billion" : "$" + Math.round(b * 1000) + " million"; }

    // ---- operations and covert action ----
    static void ops(ResoluteDesk u, Graphics2D g, Game game) {
        World w = game.world;
        if (!w.modern) {
            u.panel(g, 48, 160, DW - 96, 652, "Military operations");
            wrap(g, "In this era there are no standing strike plans on your desk: the big military choices come as the crises of the day arrive, and the wars are run from the Wars tab.", 66, 200, 900, F_S, C_DIM);
            u.button(g, 66, 250, 220, 32, "The Wars tab", B_NORM, () -> tab = 6);
            return;
        }
        u.panel(g, 48, 160, 700, 652, "Military operations");
        double y = 194;
        for (int i = 0; i < OP_NAME.length; i++) {
            final int k = i;
            boolean ok = Diplo.opReady(game, i);
            u.card(g, 62, y, 672, 52, selOp == i, () -> selOp = k, OP_DESC[i]);
            text(g, OP_NAME[i], 76, y + 22, F_SB, ok ? Color.white : C_DIM);
            textFit(g, OP_DESC[i], 76, y + 40, 470, F_T, C_DIM);
            textR(g, ok ? Math.round(Diplo.opOdds(game, i) * 100) + "% to succeed" : w.opCool[i] > 0 ? "forces resetting" : "no target", 650, y + 22, F_XS, ok ? C_TEXT : C_DIM);
            final boolean go = ok;
            u.button(g, 664, y + 12, 60, 28, "Order", go ? B_GOLD : B_OFF, go ? () -> msg = Diplo.order(game, k) : null, "Defense readiness " + Math.round(game.ex.ready[P_DEF]) + ". Failure can cost American lives.");
            y += 58;
        }
        text(g, "Operations so far: " + w.ops + ".  Americans killed in action: " + w.kia + ".", 66, 800, F_XS, C_DIM);
        double px = 762, pw = DW - 48 - px;
        u.panel(g, px, 160, pw, 652, "Covert action");
        wrap(g, "Covert programs need a presidential finding and are briefed to eight members of Congress. If one leaks, it becomes a scandal and an incident.", px + 18, 196, pw - 36, F_XS, C_DIM);
        double yy = 232;
        for (int i = 0; i < CV_NAME.length; i++) {
            final int k = i;
            boolean ok = w.cvCool[i] == 0;
            u.card(g, px + 14, yy, pw - 28, 72, false, null, CV_DESC[i]);
            text(g, CV_NAME[i], px + 28, yy + 22, F_SB, ok ? Color.white : C_DIM);
            textFit(g, CV_DESC[i], px + 28, yy + 40, pw - 200, F_T, C_DIM);
            text(g, Math.round(Diplo.cvOdds(game, i) * 100) + "% to work, " + Math.round(CV_EXPOSE[i] * (1.3 - Cabinet.pq(game, P_CIA) * 0.6) * 100) + "% to leak", px + 28, yy + 58, F_T, C_TEXT);
            u.button(g, px + pw - 160, yy + 22, 130, 28, ok ? "Sign the finding" : "Under way", ok ? B_NORM : B_OFF, ok ? () -> msg = Diplo.covert(game, k) : null);
            yy += 80;
        }
        text(g, "Programs: " + w.covert + ", exposed: " + w.exposed, px + 18, 800, F_XS, C_DIM);
    }

    // ---- trade and sanctions ----
    static void trade(ResoluteDesk u, Graphics2D g, Game game) {
        World w = game.world;
        u.panel(g, 48, 160, DW - 96, 652, "Trade, sanctions and aid");
        text(g, "Country", 66, 196, F_TB, C_FAINT); text(g, "Trade", 290, 196, F_TB, C_FAINT); text(g, "Balance", 370, 196, F_TB, C_FAINT);
        text(g, "Your tariff", 470, 196, F_TB, C_FAINT); text(g, "Sanctions", 830, 196, F_TB, C_FAINT); text(g, "Aid", 1190, 196, F_TB, C_FAINT);
        textR(g, "Average tariff on imports: " + fmt1(game.econ.tariff) + "%", DW - 66, 196, F_XSB, C_GOLD);
        Integer[] order = new Integer[NC];
        for (int i = 0; i < NC; i++) order[i] = i;
        Arrays.sort(order, (a, b) -> Double.compare(tradeOf(game, b), tradeOf(game, a)));
        double top = 206, hh = 594;
        double off = u.beginScroll(g, "trade", 58, top, DW - 116, hh);
        int h0 = u.hits.size();
        int rows = 0;
        for (int n = 0; n < NC; n++) {
            int c = order[n];
            final int cc = c;
            if (gone(game, c)) continue;
            double ry = top - off + rows * 34;
            rows++;
            Flag.draw(g, c, 66, ry + 8, 24, 16);
            text(g, cname(game, c), 98, ry + 21, F_XS, Color.white);
            double tr = tradeOf(game, c), tb = tradeBal(game, c);
            text(g, tr >= 1 ? "$" + Math.round(tr) + "B" : "$" + Math.round(tr * 1000) + "M", 290, ry + 21, F_XS, C_TEXT);
            text(g, tb >= 0 ? "+" + fmt1(tb) : fmt1(tb), 370, ry + 21, F_XS, tb < -tr * 0.2 ? C_AMBER : C_DIM);
            u.segmented(g, 470, ry + 4, 330, 24, TARIFF_OPT, w.tariff[c], i -> { if (i != w.tariff[cc]) Diplo.setTariff(game, cc, i); }, null);
            u.segmented(g, 830, ry + 4, 330, 24, LEVEL, w.sanction[c], i -> { if (i != w.sanction[cc]) Diplo.setSanction(game, cc, i); }, null);
            u.segmented(g, 1190, ry + 4, 330, 24, LEVEL, w.aid[c], i -> { if (i != w.aid[cc]) Diplo.setAid(game, cc, i); }, null);
        }
        u.clipHits(h0, top, top + hh);
        u.endScroll(g, "trade", rows * 34);
    }

    // ---- alliances and posture ----
    static void alliances(ResoluteDesk u, Graphics2D g, Game game) {
        World w = game.world;
        double cw = 480;
        u.panel(g, 48, 160, cw, 316, "NATO");
        gauge(g, "Cohesion", w.nato, false, 66, 206, cw - 36);
        text(g, "Allies spend about " + fmt1(w.allySpend) + "% of GDP on defense", 66, 252, F_XS, C_TEXT);
        boolean a5 = game.week - w.art5Week >= 13, pr = game.week - w.pressWeek >= 13, dr = game.week - w.drillWeek >= 13;
        u.button(g, 66, 276, cw - 36, 32, "Reaffirm Article 5", a5 ? B_NORM : B_OFF, a5 ? () -> { w.art5Week = game.week; w.nato = Math.min(100, w.nato + 6); for (int c = 0; c < NC; c++) if (align(game, c) == AL_TREATY && CT_REGION[c] == RG_EUROPE) w.rel[c] += 2; msg = "You reaffirm the commitment to defend every ally."; } : null);
        u.button(g, 66, 316, cw - 36, 32, "Press allies to spend more", pr ? B_NORM : B_OFF, pr ? () -> { w.pressWeek = game.week; w.allySpend += 0.1; w.nato = Math.max(0, w.nato - 4); msg = "Allies grumble, then pledge more."; } : null);
        text(g, "Each of these works once a quarter.", 66, 368, F_T, C_DIM);
        u.panel(g, 48, 486, cw, 326, "Asia-Pacific alliances");
        gauge(g, "Cohesion", w.pacific, false, 66, 532, cw - 36);
        gauge(g, "Deterrence in the Taiwan Strait", w.deter, false, 66, 570, cw - 36);
        u.button(g, 66, 610, cw - 36, 32, "Joint exercises with Japan, Korea and Australia", dr ? B_NORM : B_OFF, dr ? () -> { w.drillWeek = game.week; w.pacific = Math.min(100, w.pacific + 4); w.rel[CC_CN] -= 3; w.rel[CC_KP] -= 3; w.nk = Math.min(100, w.nk + 3); msg = "The exercises reassure allies and irritate Beijing and Pyongyang."; } : null);
        double px = 48 + cw + 14, pw = 520;
        u.panel(g, px, 160, pw, 652, "Force posture");
        double y = 204;
        for (int a = 0; a < 3; a++) {
            final int aa = a;
            text(g, POSTURE_AREA[a], px + 18, y, F_H3, Color.white);
            u.segmented(g, px + 18, y + 10, pw - 36, 28, POSTURE, w.posture[a], i -> { if (i != w.posture[aa]) Diplo.setPosture(game, aa, i); }, null);
            y += 70;
        }
        wrap(g, "More forces deter rivals and reassure allies, but they cost money, wear down the Pentagon's readiness and anger the countries they're aimed at.", px + 18, y, pw - 36, F_XS, C_DIM);
        y += 60;
        text(g, "Pentagon readiness: " + Math.round(game.ex.ready[P_DEF]), px + 18, y, F_S, WestWing.good(game.ex.ready[P_DEF]));
        double rx = px + pw + 14, rw = DW - 48 - rx;
        u.panel(g, rx, 160, rw, 652, "The record");
        double yy = 200;
        String[] rec = {"Agreements reached: " + w.deals, "Crises handled: " + w.crises, "Military operations: " + w.ops, "Covert programs: " + w.covert + " (" + w.exposed + " exposed)", "Americans killed in action: " + w.kia};
        for (String s : rec) { text(g, s, rx + 18, yy, F_S, C_TEXT); yy += 22; }
        yy += 10;
        for (int i = w.record.size() - 1; i >= 0 && yy < 790; i--) { yy += wrap(g, w.record.get(i), rx + 18, yy, rw - 36, F_XS, C_GOLD) + 6; }
    }
}
