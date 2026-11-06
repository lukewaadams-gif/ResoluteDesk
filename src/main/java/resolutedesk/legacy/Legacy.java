package resolutedesk.legacy;

import static resolutedesk.campaign.CampaignData.*;
import static resolutedesk.congress.BillData.*;
import static resolutedesk.congress.CongressRules.*;
import static resolutedesk.core.Dates.*;
import static resolutedesk.core.Fmt.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.data.ElectoralMap.*;
import static resolutedesk.data.StateData.*;
import static resolutedesk.executive.Posts.*;
import static resolutedesk.executive.ScandalData.*;
import static resolutedesk.legacy.LegacyData.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.model.Tracking.*;
import static resolutedesk.opinion.Issues.*;
import static resolutedesk.ui.Draw.*;
import static resolutedesk.ui.Fonts.*;
import static resolutedesk.ui.ScreenIds.*;
import static resolutedesk.ui.Sound.*;
import static resolutedesk.ui.Theme.*;
import static resolutedesk.world.Countries.*;
import static resolutedesk.world.WorldData.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import resolutedesk.ResoluteDesk;
import resolutedesk.campaign.Reelect;
import resolutedesk.congress.Bill;
import resolutedesk.congress.Congress;
import resolutedesk.economy.Economy;
import resolutedesk.executive.Cabinet;
import resolutedesk.executive.Exec;
import resolutedesk.executive.Justice;
import resolutedesk.executive.Person;
import resolutedesk.executive.Scandal;
import resolutedesk.io.SaveGame;
import resolutedesk.model.Cand;
import resolutedesk.model.Econ;
import resolutedesk.model.Game;
import resolutedesk.sim.Decisions;
import resolutedesk.sim.Week;
import resolutedesk.ui.Sfx;
import resolutedesk.world.War;
import resolutedesk.world.World;

/** The verdict on a finished presidency: the historians' scores and ranking, the record, the epilogue, the library and achievements. */
public final class Legacy {
    /** Does the bill's name take "the" ("the Civil Rights Act", "the North American Free Trade Agreement")? */
    static boolean takesThe(String n) {
        return n.contains("Act") || n.contains("Agreement") || n.contains("Amendments") || n.contains("Appropriation") || n.contains("Treaty") || n.contains("Charter");
    }

    /** "The Civil Rights Act became law", "The Senate ratified the North Atlantic Treaty", "Congress approved the Apollo program". */
    static String passed(Bill b) {
        String n = b.name, id = b.def().id;
        String bare = n.startsWith("The ") ? n.substring(4) : n;
        if (id.equals("un_charter") || id.equals("nato_treaty") || id.equals("panama")) return "The Senate ratified the " + bare;
        if (n.startsWith("The ")) return "Congress approved the " + bare;
        if (n.endsWith("Authority") || n.endsWith("Corps")) return "Congress created the " + n;
        return (takesThe(n) ? "The " : "") + n + " became law";
    }

    /** "A pen used to sign the Civil Rights Act", "... to sign Medicare and Medicaid into law". */
    static String pen(Bill b) {
        String n = b.name;
        if (n.startsWith("The ")) return "A pen used to sign the " + n.substring(4);
        return takesThe(n) ? "A pen used to sign the " + n : "A pen used to sign " + n + " into law";
    }

    public static int tab;
    public static int[] lastScores;
    public static String rankLine, rankShort;
    public static int rankPos;

    static double clamp100(double v) { return clamp(v, 0, 100); }

    static double avgApproval(Game g) { double s = 0; for (double[] h : g.hist) s += h[H_APP]; return g.hist.isEmpty() ? g.approval : s / g.hist.size(); }

    static double first(Game g, int col) { return g.hist.isEmpty() ? 0 : g.hist.get(0)[col]; }

    static int lawsOurs(Game g) { int n = 0; for (Bill b : g.cong.bills) if (b.stage == ST_LAW && b.ours) n++; return n; }

    static int lawsAll(Game g) { int n = 0; for (Bill b : g.cong.bills) if (b.stage == ST_LAW) n++; return n; }

    static int wars(Game g, int stage) { int n = 0; for (War x : g.world.wars) if (x.stage == stage && x.started >= 0) n++; return n; }

    static int warsEnded(Game g, int stage) { int n = 0; for (War x : g.world.wars) if (x.stage == stage && x.ended >= 0) n++; return n; }

    /** The ten measures, 0 to 100 each. */
    public static int[] scores(Game g) {
        Econ e = g.econ;
        Exec x = g.ex;
        World w = g.world;
        double[] s = new double[10];
        double avg = avgApproval(g);
        double yrs = Math.max(1, g.week / 52.0);
        s[0] = clamp100(46 + (avg - 50) * 1.8 + Math.min(6, g.home.speeches * 0.5));
        int nCr = g.okCount + g.failCount;
        s[1] = clamp100(28 + (nCr > 0 ? 45.0 * g.okCount / nCr : 22) + Math.min(8, nCr * 0.3) + 12 * warsEnded(g, 3) + 4 * warsEnded(g, 5) - 18 * warsEnded(g, 4));
        double u0 = first(g, H_UNEMP), pi0 = first(g, H_INFL), sp0 = first(g, H_STOCKS);
        double gsum = 0; for (double[] h : g.hist) gsum += h[H_GDP];
        double gavg = g.hist.isEmpty() ? e.gdp : gsum / g.hist.size();
        s[2] = clamp100(52 + 3.2 * (u0 - e.unemp) + 4 * (gavg - e.potential) - 2.5 * Math.max(0, Economy.piEff(e.infl) - Math.max(3, Economy.piEff(pi0))) + 2.5 * Math.max(0, Economy.piEff(pi0) - Math.max(3, Economy.piEff(e.infl)))
                + 6 * Math.log(Math.max(0.2, e.stocks / Math.max(0.01, sp0))) - 0.15 * Math.max(0, e.debt - first(g, H_DEBT)));
        int sc = 0; double sev = 0;
        for (Scandal k : x.scandals) if (k.kind != SK_LEAK) { sc++; sev += k.severity; }
        s[3] = clamp100(72 - sev * 1.5 - x.impeachments * 15 - (g.removed ? 45 : 0) - x.pardons * 0.5 + g.justice * 4 + (g.ending == END_RETIRED && g.term == 1 ? 4 : 0));
        double allies = 0; int na = 0;
        for (int c = 0; c < NC; c++) if (!gone(g, c) && align(g, c) == AL_TREATY) { allies += w.rel[c]; na++; }
        allies = na > 0 ? allies / na : 50;
        s[4] = clamp100(36 + (allies - 50) * 0.5 + Math.min(18, w.deals * 2.5) + 8 * warsEnded(g, 3) + 5 * warsEnded(g, 5) - 14 * warsEnded(g, 4) + Math.min(6, sumVisits(w) * 0.5));
        double mor = 0; for (Person p : Cabinet.everyone(g)) mor += p.morale; mor /= Math.max(1, Cabinet.everyone(g).size());
        s[5] = clamp100(52 + (mor - 55) * 0.6 - x.departures / yrs * 3.5 - x.failures * 3 + (Cabinet.sq(g, S_COS) - 0.6) * 40);
        Congress c = g.cong;
        s[6] = clamp100(34 + Math.min(34, lawsAll(g) / yrs * 7) - c.overrides * 5 - c.shutdowns * 6 + (g.hill - 50) * 0.4 + (c.defaulted ? -25 : 0));
        double big = 0;
        for (Bill b : c.bills) if (b.stage == ST_LAW && (b.ours || b.def().lean == g.party)) big += 3 + Math.max(0, b.def().support - 50) * 0.08 + Math.abs(b.def().cost) * 0.004;
        s[7] = clamp100(24 + Math.min(62, big * 1.5));
        double civil = 0;
        for (Bill b : c.bills) if (b.stage == ST_LAW && b.def().area == PI_RIGHTS && b.cult() < -0.3) civil += 1;
        s[8] = clamp100(38 + civil * 10 + g.justice * 8 + (g.issueApp[PI_RIGHTS] - 50) * 0.25);
        double hard = Math.max(0, u0 - 6) * 1.5 + Math.max(0, Economy.piEff(pi0) - 5) * 2 + wars(g, 2) * 6 + (g.era.year < 1946 && g.era.year > 1936 ? 6 : 0);
        s[9] = clamp100(45 + hard + (avg - 50) * 0.8 + (g.term >= 2 ? 6 : 0) - (g.ending == END_LOST ? 8 : 0) - (g.removed ? 25 : 0));
        if (g.set.difficulty == 2) for (int i = 0; i < 10; i++) s[i] = clamp100(s[i] + 3);
        if (g.set.difficulty == 0) for (int i = 0; i < 10; i++) s[i] = clamp100(s[i] - 3);
        int[] out = new int[10];
        for (int i = 0; i < 10; i++) out[i] = (int) Math.round(s[i]);
        return out;
    }

    static double sumVisits(World w) { double v = 0; for (int c = 0; c < NC; c++) v += w.visits[c]; return v; }

    public static int total(int[] s) { int t = 0; for (int v : s) t += v; return t; }

    /** Where the president lands among the real presidents; the real one, if you are playing them, is set aside. */
    public static void rank(Game g, int score) {
        List<String> names = new ArrayList<String>();
        List<Integer> sc = new ArrayList<Integer>();
        String me = g.pres.name();
        for (int i = 0; i < RANKED.length; i++) {
            if (sameName(RANKED[i], g.pres)) continue;
            names.add(RANKED[i]); sc.add(RANK_SCORE[i]);
        }
        int pos = 0;
        while (pos < sc.size() && sc.get(pos) > score) pos++;
        rankPos = pos + 1;
        int n = names.size() + 1;
        String above = pos > 0 ? names.get(pos - 1) : null, below = pos < names.size() ? names.get(pos) : null;
        rankShort = ordinal(rankPos) + " of " + n;
        rankLine = "Historians rank you " + ordinal(rankPos) + " of " + n + " presidents" + (above != null && below != null ? ", between " + above + " and " + below : above == null ? ", ahead of " + below : ", behind " + above) + ".";
        for (int i = 0; i < RANKED.length; i++) if (sameName(RANKED[i], g.pres)) rankLine += " The real " + RANKED[i] + " ranked " + ordinal(i + 1) + " in the 2021 C-SPAN survey of historians.";
    }

    static boolean sameName(String a, Cand c) {
        if (c == null || c.last == null) return false;
        if (!a.endsWith(" " + c.last)) return false;
        String f = c.first.replace(".", "");
        String a2 = a.replace(".", "");
        return a2.startsWith(f) || a2.startsWith(f.split(" ")[0]) && (c.first.length() < 3 || a2.contains(f));
    }

    static String ordinal(int n) { int m = n % 100; String suf = m >= 11 && m <= 13 ? "th" : n % 10 == 1 ? "st" : n % 10 == 2 ? "nd" : n % 10 == 3 ? "rd" : "th"; return n + suf; }

    /** Achievements earned in this presidency. */
    public static List<String> earned(Game g, int rank) {
        List<String> l = new ArrayList<String>();
        Econ e = g.econ;
        int wonElections = g.term - 1;
        if (g.term >= 2 && g.ending != END_REMOVED) l.add("twoterms");
        if (g.term + g.prior >= 3) l.add("thirdterm");
        if (g.term + g.prior >= 4) l.add("fourth");
        if (lastWinEv(g) >= 400) l.add("landslide");
        if (lastWinEv(g) > 0 && lastWinMargin(g) <= 15) l.add("squeaker");
        if (wonElections > 0 && (g.set.custom != null || g.era.winnerId == null || !g.pres.id.equals(g.era.winnerId))) l.add("whatif");
        if (lawsAll(g) >= 10) l.add("lawmaker");
        if (lawsAll(g) >= 20) l.add("newdeal");
        for (Bill b : g.cong.bills) if (b.stage == ST_LAW && b.def().area == PI_RIGHTS && b.cult() < -0.3) { l.add("civilrights"); break; }
        if (g.midGain) l.add("midterm");
        if (warsEnded(g, 3) > 0) l.add("victory");
        if (warsEnded(g, 5) > 0) l.add("peace");
        if (g.warsStarted == 0 && g.week >= 200) l.add("dove");
        if (first(g, H_UNEMP) - e.unemp >= 5) l.add("recovery");
        if (e.unemp < 4) l.add("fullemp");
        if (first(g, H_INFL) - e.infl >= 5) l.add("slayer");
        if (e.deficit < 0) l.add("surplus");
        if (first(g, H_STOCKS) > 0 && e.stocks >= 2 * first(g, H_STOCKS)) l.add("bull");
        if (g.approval >= 60 && !g.removed) l.add("beloved");
        if (wonElections > 0 && g.minApproval < 40) l.add("comeback");
        if (g.ex.impeachments > 0 && !g.removed) l.add("survivor");
        if (g.ex.justices >= 3) l.add("court3");
        if (g.world.deals >= 5) l.add("summits");
        if (g.okCount >= 10) l.add("crises");
        if (rank <= 10) l.add("top10");
        if (rank == 1) l.add("lincoln");
        if (g.set.difficulty == 2 && g.week >= 200) l.add("hard");
        if (g.ending == END_RETIRED) l.add("stepdown");
        if (g.set.bgLine != null) l.add("import");
        for (String s : g.histOk) if (s.startsWith("Going to the Moon")) { l.add("moon"); break; }
        if (sumVisits(g.world) >= 10) l.add("tour");
        if (erasPlayed(g) >= 5) l.add("eras");
        return l;
    }

    static int lastWinEv(Game g) { return g.lastEvWon; }

    static int lastWinMargin(Game g) { return g.lastEvWon - (g.lastEvTotal / 2 + 1); }

    static int erasPlayed(Game g) {
        List<String> eras = new ArrayList<String>();
        for (String[] e : entries()) if (e.length > 3 && !eras.contains(e[3])) eras.add(e[3]);
        String mine = String.valueOf(g.era.year);
        if (!eras.contains(mine)) eras.add(mine);
        return eras.size();
    }

    static String achName(String id) { for (String[] a : ACH) if (a[0].equals(id)) return a[1]; return id; }

    static String achDesc(String id) { for (String[] a : ACH) if (a[0].equals(id)) return a[2]; return ""; }

    // ---- the Hall of Fame: one line per finished presidency ----
    static String readAll() { return HallStore.read(); }

    static List<String[]> entries() {
        List<String[]> l = new ArrayList<String[]>();
        for (String line : readAll().split("\n")) { String[] t = line.split("\\|", -1); if (t.length >= 10) l.add(t); }
        Collections.sort(l, (a, b) -> Integer.compare(parseIntSafe(b[6]), parseIntSafe(a[6])));
        return l;
    }

    static int parseIntSafe(String s) { try { return Integer.parseInt(s.trim()); } catch (Throwable t) { return 0; } }

    static List<String> unlocked() {
        List<String> u = new ArrayList<String>();
        for (String[] e : entries()) for (String id : e[8].split(",")) if (id.length() > 0 && !u.contains(id)) u.add(id);
        return u;
    }

    /** Closes the books: scores, rank, achievements, a Hall of Fame line. */
    public static void record(Game g) {
        int[] s = scores(g);
        lastScores = s;
        int tot = total(s);
        rank(g, tot);
        if (g.recorded) return;
        g.recorded = true;
        List<String> a = earned(g, rankPos);
        StringBuilder ids = new StringBuilder();
        for (String x : a) ids.append(ids.length() > 0 ? "," : "").append(x);
        String line = dateStr(g.day()) + "|" + g.pres.name().replace("|", "/") + "|" + PARTY_SHORT[g.party] + "|" + g.era.year + "|" + g.term + "|" + (g.removed ? END_NAME[END_REMOVED] : END_NAME[Math.max(1, g.ending)]) + "|"
                + tot + "|" + rankShort + "|" + ids + "|" + (g.set.difficulty == 0 ? "Easy" : g.set.difficulty == 2 ? "Hard" : "Normal");
        if (!HallStore.off) HallStore.write(readAll() + line + "\n");
        g.legacyScore = tot;
        g.legacyRank = rankPos;
        g.achievements = a;
    }

    public static void begin(ResoluteDesk u) {
        Game g = u.game;
        if (g == null) return;
        if (g.removed) g.ending = END_REMOVED;
        if (g.ending == END_NONE) g.ending = END_SERVED;
        g.over = true;
        g.watch = false;
        record(g);
        epilogue(g);
        SaveGame.save(u, SaveGame.AUTO);
        tab = 0;
        u.go(SC_LEGACY);
        snd(Sfx.CHEER);
    }

    /** After a lost or last election: play out the lame-duck weeks on autopilot. */
    public static void finishTerm(ResoluteDesk u) {
        Game g = u.game;
        int guard = 0;
        while (!g.over && !g.removed && guard++ < 20) {
            Decisions.autoAll(g);
            Week.resolve(g);
        }
        Decisions.autoAll(g);
        begin(u);
    }

    // ---- the epilogue ----
    public static List<String[]> epi;

    public static void epilogue(Game g) {
        List<String[]> l = new ArrayList<String[]>();
        Econ e = g.econ;
        int y0 = g.era.year, y1 = dateOf(g.day())[0];
        int years = Math.max(1, Math.round((g.week) / 52f));
        Reelect re = g.re;
        String how;
        switch (g.ending) {
            case END_REMOVED: how = "The Senate removed you from office on " + dateLong(g.day()) + ". " + (g.vp != null ? g.vp.name() + " was sworn in within the hour." : "Your vice president was sworn in within the hour."); break;
            case END_LOST: how = "You lost your bid for another term" + (re != null && re.opp != null ? " to " + re.opp.name() + ", " + re.evOpp + " electoral votes to " + re.evMe : "") + ", and handed over the office on January 20, " + y1 + "."; break;
            case END_DENIED: how = "Your own party turned you out" + (re != null && re.challenger != null ? ": " + re.challenger + " beat you in the primaries" : "") + ". You left office on January 20, " + y1 + "."; break;
            case END_RETIRED: how = "You chose not to run again, and left office on January 20, " + y1 + ", after " + years + " years."; break;
            default: how = "You served " + (g.term == 1 ? "a full term" : g.term + " terms") + " and left office on January 20, " + y1 + ", after " + years + " years in the White House."; break;
        }
        l.add(new String[]{"How it ended", how});
        if (re != null && re.done && re.opp != null) {
            Cand me = re.nominee != null ? re.nominee : g.pres;
            Cand winner = re.won ? me : re.opp;
            if (winner != g.pres) l.add(new String[]{"Your successor", winner.name() + " won the " + re.year + " election, " + Math.max(re.evMe, re.evOpp) + " electoral votes to " + Math.min(re.evMe, re.evOpp)
                    + (winner.party == g.party ? ", keeping the White House in your party's hands." : ", and the White House changed parties.")});
        }
        double u0 = first(g, H_UNEMP), pi0 = first(g, H_INFL), sp0 = first(g, H_STOCKS), d0 = first(g, H_DEBT);
        l.add(new String[]{"The economy", "You inherited unemployment of " + fmt1(u0) + "% and left it at " + fmt1(e.unemp) + "%. Inflation went from " + fmt1(pi0) + "% to " + fmt1(e.infl) + "%, and the public debt from "
                + Math.round(d0) + "% to " + Math.round(e.debt) + "% of the economy. Stocks " + (e.stocks >= sp0 ? "rose " : "fell ") + Math.round(Math.abs(e.stocks / Math.max(0.01, sp0) - 1) * 100) + "% while you were in office."});
        StringBuilder laws = new StringBuilder();
        int n = 0;
        for (Bill b : g.cong.bills) {
            if (b.stage != ST_LAW || n >= 6) continue;
            String imp = impactOf(b);
            laws.append(n > 0 ? " " : "").append(passed(b)).append(imp != null ? ": " + imp : ".");
            n++;
        }
        l.add(new String[]{"Your laws", n == 0 ? "No major legislation became law on your watch." : laws.toString() + (lawsAll(g) > n ? " And " + (lawsAll(g) - n) + " more." : "")});
        StringBuilder world = new StringBuilder();
        for (War x : g.world.wars) if (x.stage >= 3 && (x.started >= 0 || x.ended >= 0)) world.append(x.name).append(x.stage == 3 ? " was won" : x.stage == 4 ? " was lost" : " ended in an agreement").append(", at a cost of ").append(thousands(Math.round(x.kia))).append(" American lives. ");
        for (War x : g.world.wars) if (x.stage == 2) world.append(x.name).append(" was still going on, with ").append(thousands(Math.round(x.troops * 1000))).append(" Americans deployed. ");
        world.append(g.world.deals == 0 ? "You reached no agreements with foreign leaders." : "You reached " + g.world.deals + (g.world.deals == 1 ? " agreement" : " agreements") + " with foreign leaders.");
        if (!g.world.record.isEmpty()) {
            String lw = g.world.record.get(g.world.record.size() - 1).replaceAll("^[A-Z][a-z]+ \\d+, \\d+: ", "").replaceAll("\\.$", "");
            boolean warEnd = false;
            for (War x : g.world.wars) if (x.stage >= 3 && lw.toLowerCase().startsWith(x.name.toLowerCase())) warEnd = true;
            if (!warEnd) world.append(" The last word from abroad: ").append(lw.substring(0, 1).toUpperCase()).append(lw.substring(1)).append(".");
        }
        l.add(new String[]{"The world", world.toString()});
        List<String> js = new ArrayList<String>();
        for (Justice j : g.ex.court) if (!j.real) js.add(j.name.replace("Judge ", ""));
        if (!js.isEmpty()) l.add(new String[]{"The Court", "Your appointees to the Supreme Court, " + String.join(", ", js) + ", would shape the law for a generation."});
        int sc = 0; for (Scandal k : g.ex.scandals) if (k.kind != SK_LEAK && k.severity >= 4) sc++;
        if (sc > 0 || g.ex.impeachments > 0) l.add(new String[]{"The scandals", (g.ex.impeachments > 0 ? "You were impeached" + (g.removed ? " and removed. " : " but not removed. ") : "") + (sc > 0 ? sc + " serious " + (sc == 1 ? "scandal" : "scandals") + " dogged your administration." : "")});
        Random r = new Random(g.set.seed ^ 0x1EADL);
        String[] after = {"You wrote two volumes of memoirs; the first was a bestseller.", "You spent your retirement building homes for families who needed them.", "You returned home and took up painting, badly and happily.",
                "You became a roving ambassador for peace, monitoring elections around the world.", "You raised money for your library and gave speeches for large fees.", "You went back to teaching law and wrote a well-reviewed history of the presidency.",
                "You ran a foundation fighting disease in the poorest countries.", "You stayed out of politics, and out of the papers, for the rest of your life."};
        l.add(new String[]{"After the White House", after[r.nextInt(after.length)] + " Your presidential library opened in " + cityOf(g.pres.home) + " in " + (y1 + 4 + r.nextInt(4)) + "."});
        l.add(new String[]{"The verdict", rankLine});
        epi = l;
        g.epilogue = new ArrayList<String>();
        for (String[] p : l) g.epilogue.add(p[0] + "|" + p[1]);
    }

    // ---- the screens ----
    public static void draw(ResoluteDesk u, Graphics2D g) {
        Game game = u.game;
        if (game == null) { u.go(SC_HOME); return; }
        if (lastScores == null) { record(game); }
        if (epi == null) epilogue(game);
        u.backdrop(g);
        label(g, "THE VERDICT OF HISTORY", 48, 46, C_GOLD);
        text(g, game.pres.name() + ", " + game.era.year + "-" + dateOf(game.day())[0], 48, 84, F_DISPLAY, Color.white);
        u.segmented(g, 48, 104, 760, 30, new String[]{"The verdict", "The record", "The library", "Epilogue", "Achievements"}, tab, i -> tab = i, null);
        switch (tab) {
            case 1: recordTab(u, g, game); break;
            case 2: library(u, g, game); break;
            case 3: epilogueTab(u, g, game); break;
            case 4: achievementsTab(u, g, game); break;
            default: verdict(u, g, game); break;
        }
        u.button(g, 48, 832, 200, 44, "Main menu", B_GHOST, () -> u.go(SC_HOME));
        u.button(g, DW - 48 - 240, 832, 240, 44, "Hall of Fame", B_GOLD, () -> u.go(SC_HALL));
    }

    static void verdict(ResoluteDesk u, Graphics2D g, Game game) {
        int[] s = lastScores;
        u.panel(g, 48, 150, 760, 664, "The historians' ten measures");
        double y = 196;
        for (int i = 0; i < 10; i++) {
            text(g, MEASURE[i], 66, y + 12, F_S, C_TEXT);
            bar(g, 300, y + 3, 400, 12, s[i] / 100.0, s[i] >= 70 ? C_GOOD : s[i] >= 45 ? C_GOLD : C_BAD);
            textR(g, String.valueOf(s[i]), 790, y + 14, F_BB, Color.white);
            y += 34;
        }
        y += 10;
        text(g, "Total: " + total(s) + " of 1,000", 66, y + 20, F_H2, C_GOLD);
        wrap(g, rankLine, 66, y + 50, 720, F_B, Color.white);
        wrap(g, "The measures follow the C-SPAN Presidential Historians Survey. Real presidents are placed by their 2021 ranks; where you'd land is this game's estimate, not the historians'.", 66, y + 110, 720, F_XS, C_DIM);
        double px = 824, pw = DW - 48 - px;
        u.panel(g, px, 150, pw, 664, "Among the presidents");
        List<String> names = new ArrayList<String>();
        for (String nm : RANKED) if (!sameName(nm, game.pres)) names.add(nm);
        int pos = rankPos - 1;
        int lo = Math.max(0, pos - 9), hi = Math.min(names.size(), lo + 18);
        double yy = 192;
        for (int i = lo; i <= hi && yy < 790; i++) {
            if (i == pos) {
                g.setColor(new Color(242, 193, 78, 40));
                g.fill(new RoundRectangle2D.Double(px + 12, yy - 16, pw - 24, 26, 8, 8));
                text(g, ordinal(i + 1), px + 24, yy, F_BB, C_GOLD);
                text(g, game.pres.name() + " (you)", px + 84, yy, F_BB, C_GOLD);
                yy += 30;
            }
            if (i < names.size()) {
                text(g, ordinal(i + (i >= pos ? 2 : 1)), px + 24, yy, F_S, C_DIM);
                text(g, names.get(i), px + 84, yy, F_S, C_TEXT);
                yy += 28;
            }
        }
    }

    static void recordTab(ResoluteDesk u, Graphics2D g, Game game) {
        u.panel(g, 48, 150, 900, 664, "Approval, week by week");
        double cx = 70, cy = 190, cw = 856, ch = 260;
        g.setColor(new Color(255, 255, 255, 18));
        for (int k = 20; k <= 80; k += 20) { double yy = cy + ch - (k - 10) / 80.0 * ch; g.draw(new Line2D.Double(cx, yy, cx + cw, yy)); text(g, k + "%", cx - 4 - 22, yy + 4, F_T, C_FAINT); }
        int n = game.hist.size();
        Path2D p = new Path2D.Double();
        for (int i = 0; i < n; i++) {
            double v = game.hist.get(i)[H_APP];
            double xx = cx + cw * i / Math.max(1, n - 1), yy = cy + ch - (v - 10) / 80.0 * ch;
            if (i == 0) p.moveTo(xx, yy); else p.lineTo(xx, yy);
        }
        g.setColor(C_GOLD);
        g.setStroke(new BasicStroke(2f));
        g.draw(p);
        g.setStroke(new BasicStroke(1f));
        double y = 490;
        Econ e = game.econ;
        String[][] rows = {{"Average approval", Math.round(avgApproval(game)) + "%"}, {"Highest / lowest", Math.round(game.maxApproval) + "% / " + Math.round(game.minApproval) + "%"},
                {"Unemployment", fmt1(first(game, H_UNEMP)) + "% to " + fmt1(e.unemp) + "%"}, {"Inflation", fmt1(first(game, H_INFL)) + "% to " + fmt1(e.infl) + "%"},
                {"Public debt", Math.round(first(game, H_DEBT)) + "% to " + Math.round(e.debt) + "% of GDP"}, {"Laws signed", String.valueOf(lawsAll(game))}, {"Vetoes (overridden)", game.cong.vetoes + " (" + game.cong.overrides + ")"},
                {"Shutdowns", String.valueOf(game.cong.shutdowns)}, {"Justices appointed", String.valueOf(game.ex.justices)}, {"Crises that went your way / badly", game.okCount + " / " + game.failCount}};
        for (int i = 0; i < rows.length; i++) {
            double xx = i < 5 ? 70 : 510, yy = y + (i % 5) * 28;
            text(g, rows[i][0], xx, yy, F_S, C_TEXT);
            textR(g, rows[i][1], xx + 410, yy, F_BB, Color.white);
        }
        double px = 964, pw = DW - 48 - px;
        u.panel(g, px, 150, pw, 664, "Milestones");
        double yy = 190;
        for (int i = 0; i < game.milestones.size() && yy < 790; i++) yy += wrap(g, game.milestones.get(i), px + 18, yy, pw - 36, F_XS, C_TEXT) + 6;
    }

    static void epilogueTab(ResoluteDesk u, Graphics2D g, Game game) {
        u.panel(g, 48, 150, DW - 96, 664, "What came after");
        double y = 192;
        for (String[] p : epi) {
            text(g, p[0], 66, y, F_H3, C_GOLD);
            y += 6 + wrap(g, p[1], 250, y, DW - 96 - 230, F_B, C_TEXT) + 12;
            if (y > 790) break;
        }
    }

    static void achievementsTab(ResoluteDesk u, Graphics2D g, Game game) {
        List<String> mine = game.achievements != null ? game.achievements : new ArrayList<String>();
        List<String> all = unlocked();
        u.panel(g, 48, 150, DW - 96, 664, "Achievements: " + mine.size() + " this presidency, " + all.size() + " of " + ACH.length + " unlocked in all");
        double colw = (DW - 96 - 48) / 3.0;
        for (int i = 0; i < ACH.length; i++) {
            double x = 66 + (i % 3) * colw, y = 186 + (i / 3) * 56;
            boolean now = mine.contains(ACH[i][0]), ever = all.contains(ACH[i][0]);
            g.setColor(now ? new Color(242, 193, 78, 45) : new Color(255, 255, 255, ever ? 14 : 6));
            g.fill(new RoundRectangle2D.Double(x, y, colw - 14, 48, 10, 10));
            text(g, ACH[i][1], x + 12, y + 20, F_BB, now ? C_GOLD : ever ? Color.white : C_FAINT);
            textFit(g, ACH[i][2], x + 12, y + 38, colw - 40, F_XS, ever ? C_DIM : C_FAINT);
        }
    }

    // ---- the presidential library ----
    static void library(ResoluteDesk u, Graphics2D g, Game game) {
        int y0 = game.era.year, y1 = dateOf(game.day())[0];
        String city = cityOf(game.pres.home);
        String st = game.pres.home >= 0 ? STATES[game.pres.home].name : "Washington, D.C.";
        u.panel(g, 48, 150, 900, 664, "The " + game.pres.name() + " Presidential Library and Museum");
        text(g, city + ", " + st, 66, 190, F_S, C_DIM);
        building(g, game, 66, 210, 864, 300, u.tsec);
        double y = 540;
        label(g, "Galleries", 66, y, C_GOLD);
        y += 22;
        List<String> gal = galleries(game);
        for (int i = 0; i < gal.size() && i < 8; i++) {
            double x = 66 + (i % 2) * 432, yy = y + (i / 2) * 58;
            g.setColor(new Color(255, 255, 255, 12));
            g.fill(new RoundRectangle2D.Double(x, yy, 418, 50, 8, 8));
            String[] t = gal.get(i).split("\\|");
            text(g, t[0], x + 12, yy + 20, F_SB, Color.white);
            textFit(g, t[1], x + 12, yy + 38, 396, F_XS, C_DIM);
        }
        double px = 964, pw = DW - 48 - px;
        u.panel(g, px, 150, pw, 664, "In the collection");
        double yy = 192;
        for (String a : artifacts(game)) { yy += wrap(g, "- " + a, px + 18, yy, pw - 36, F_XS, C_TEXT) + 8; if (yy > 760) break; }
        int visitors = 150 + (game.legacyScore - 400) / 2;
        text(g, "About " + Math.max(60, visitors) + ",000 visitors a year", px + 18, 796, F_XSB, C_GOLD);
    }

    static List<String> galleries(Game g) {
        List<String> l = new ArrayList<String>();
        l.add("The Oval Office|A full-size replica, as it looked on your last day: " + (g.era.year < 1950 ? "the Resolute Desk and a wall of ship paintings" : "the Resolute Desk, the family photos and the flags") + ".");
        l.add("The road to the White House|Your life before the presidency, from " + (g.pres.homeAb != null ? STATES[Math.max(0, g.pres.home)].name : "home") + " to the inaugural stand in " + g.era.year + ".");
        int n = 0;
        for (Bill b : g.cong.bills) if (b.stage == ST_LAW && b.ours && n < 2) { l.add(b.name + "|" + (impactOf(b) != null ? impactOf(b) : b.def().desc)); n++; }
        for (War x : g.world.wars) if (x.stage >= 2 && (x.started >= 0 || x.ended >= 0)) { l.add(x.name + "|" + (x.stage == 3 ? "From the first battles to victory" : x.stage == 5 ? "The fighting, and the long road to a settlement" : x.stage == 4 ? "A hard war and a bitter end" : "The war that outlasted your presidency") + "; " + thousands(Math.round(x.kia)) + " names on the memorial wall."); break; }
        if (!g.histOk.isEmpty()) l.add("Decisions|" + g.histOk.get(g.histOk.size() - 1).replace(":", ": you chose to") + ".");
        if (g.re != null && g.re.done) l.add("The " + g.re.year + " campaign|Buttons, posters and the election-night map: " + g.re.evMe + " to " + g.re.evOpp + ".");
        l.add("The first family|Life in the Residence: the dinners, the pets and the Christmas trees.");
        l.add("Your legacy|An interactive gallery where visitors vote on your hardest decisions.");
        return l;
    }

    static List<String> artifacts(Game g) {
        List<String> l = new ArrayList<String>();
        int y = g.era.year;
        for (Bill b : g.cong.bills) if (b.stage == ST_LAW && l.size() < 3) l.add(pen(b));
        if (y < 1950) l.add("The microphone from your radio addresses");
        else if (y < 1990) l.add("The teleprompter glass from your addresses to the nation");
        else if (y < 2009) l.add("Your first White House e-mail");
        else l.add("The phone you posted from, retired");
        for (String s : g.histOk) if (l.size() < 9) l.add("Documents from \"" + s.split(":")[0] + "\"");
        if (g.re != null && g.re.done && g.re.won) l.add("Your " + g.re.year + " victory speech, with your handwritten edits");
        l.add("Gifts from foreign leaders, " + Math.max(12, g.world.deals * 9 + (int) sumVisits(g.world) * 4) + " of them on display");
        l.add("The family Bible you took the oath on");
        return l;
    }

    /** A library in the style of its era: a classical temple, a modernist slab or a glass pavilion. */
    static void building(Graphics2D g, Game game, double x, double y, double w, double h, double t) {
        g.setPaint(new GradientPaint(0, (float) y, rgb(0x7fb4e6), 0, (float) (y + h), rgb(0xd9ecf8)));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 10, 10));
        g.setColor(rgb(0x6aa35a));
        g.fill(new Rectangle2D.Double(x, y + h * 0.8, w, h * 0.2));
        double cx = x + w / 2, base = y + h * 0.82;
        int era = game.era.year < 1960 ? 0 : game.era.year < 1995 ? 1 : 2;
        Random r = new Random(game.set.seed);
        if (era == 0) {
            double bw = w * 0.56, bh = h * 0.42;
            g.setColor(rgb(0xeee6d4)); g.fill(new Rectangle2D.Double(cx - bw / 2, base - bh, bw, bh));
            Path2D ped = new Path2D.Double(); ped.moveTo(cx - bw / 2 - 10, base - bh); ped.lineTo(cx, base - bh - h * 0.16); ped.lineTo(cx + bw / 2 + 10, base - bh); ped.closePath();
            g.setColor(rgb(0xf6f0e2)); g.fill(ped);
            g.setColor(rgb(0xd2c8b2));
            for (int i = 0; i < 8; i++) g.fill(new Rectangle2D.Double(cx - bw / 2 + 18 + i * (bw - 44) / 7, base - bh + 14, 12, bh - 14));
            g.setColor(rgb(0xc9bfa8)); g.fill(new Rectangle2D.Double(cx - bw / 2 - 20, base - 10, bw + 40, 10));
        } else if (era == 1) {
            double bw = w * 0.5, bh = h * 0.5;
            g.setColor(rgb(0xd8d3c8)); g.fill(new Rectangle2D.Double(cx - bw / 2, base - bh, bw, bh));
            g.setColor(rgb(0x3d4656));
            for (int i = 0; i < 6; i++) g.fill(new Rectangle2D.Double(cx - bw / 2 + 20 + i * (bw - 40) / 6, base - bh + 16, (bw - 40) / 6 - 10, bh * 0.25));
            g.setColor(rgb(0xbfb8aa)); g.fill(new Rectangle2D.Double(cx + bw / 2 - 10, base - bh * 0.6, w * 0.16, bh * 0.6));
            g.setColor(rgb(0x2e3542)); g.fill(new Rectangle2D.Double(cx - 30, base - bh * 0.35, 60, bh * 0.35));
        } else {
            double bw = w * 0.6, bh = h * 0.36;
            g.setColor(new Color(170, 205, 230, 220)); g.fill(new Rectangle2D.Double(cx - bw / 2, base - bh, bw, bh));
            g.setColor(new Color(255, 255, 255, 120));
            for (int i = 0; i <= 12; i++) g.draw(new Line2D.Double(cx - bw / 2 + i * bw / 12, base - bh, cx - bw / 2 + i * bw / 12, base));
            g.setColor(rgb(0xe9e6df)); g.fill(new Rectangle2D.Double(cx - bw / 2 - 30, base - bh - 18, bw + 60, 18));
        }
        // the flag
        double fx = x + w * 0.12, fy = base - h * 0.55;
        g.setColor(rgb(0xcfcfcf)); g.fill(new Rectangle2D.Double(fx - 1.5, fy, 3, base - fy));
        for (int k = 0; k < 7; k++) { g.setColor(k % 2 == 0 ? rgb(0xb22234) : Color.white); g.fill(new Rectangle2D.Double(fx, fy + k * 5 + Math.sin(t * 2 + k * 0.4) * 1.5, 54, 5)); }
        g.setColor(rgb(0x3c3b6e)); g.fill(new Rectangle2D.Double(fx, fy, 22, 20));
        // trees
        for (int i = 0; i < 9; i++) { double tx = x + 30 + r.nextDouble() * (w - 60); if (Math.abs(tx - cx) < w * 0.33) continue; g.setColor(rgb(0x3f7a3a)); g.fill(new Ellipse2D.Double(tx - 16, base - 40, 32, 36)); g.setColor(rgb(0x5a3a22)); g.fill(new Rectangle2D.Double(tx - 2, base - 8, 4, 10)); }
        textC(g, game.pres.last.toUpperCase() + " LIBRARY", cx, base + 24, F_SERIF, rgb(0x2a2a2a));
    }

    // ---- the Hall of Fame ----
    public static void hall(ResoluteDesk u, Graphics2D g) {
        u.backdrop(g);
        label(g, "THE HALL OF FAME", 48, 46, C_GOLD);
        text(g, "Your finished presidencies, best first", 48, 84, F_DISPLAY, Color.white);
        List<String[]> l = entries();
        u.panel(g, 48, 120, 1000, 694, l.size() + (l.size() == 1 ? " presidency" : " presidencies"));
        String[] head = {"President", "Era", "Terms", "How it ended", "Score", "Rank"};
        double[] cx = {66, 360, 450, 520, 760, 850};
        for (int i = 0; i < head.length; i++) text(g, head[i], cx[i], 156, F_TB, C_FAINT);
        double top = 166, hh = 640;
        double off = u.beginScroll(g, "hall", 58, top, 980, hh);
        int h0 = u.hits.size();
        for (int i = 0; i < l.size(); i++) {
            String[] e = l.get(i);
            double yy = top - off + i * 30 + 20;
            text(g, (i + 1) + ". " + e[1] + " (" + e[2] + ")", cx[0], yy, F_S, i == 0 ? C_GOLD : Color.white);
            text(g, e[3], cx[1], yy, F_S, C_TEXT);
            text(g, e[4], cx[2], yy, F_S, C_TEXT);
            textFit(g, e[5], cx[3], yy, 230, F_S, C_TEXT);
            text(g, e[6], cx[4], yy, F_SB, Color.white);
            text(g, e[7], cx[5], yy, F_S, C_DIM);
        }
        u.clipHits(h0, top, top + hh);
        u.endScroll(g, "hall", l.size() * 30 + 30);
        if (l.isEmpty()) wrap(g, "No finished presidencies yet. Serve out a term, or lose trying, and it lands here.", 66, 200, 900, F_S, C_DIM);
        double px = 1064, pw = DW - 48 - px;
        List<String> all = unlocked();
        u.panel(g, px, 120, pw, 694, "Achievements: " + all.size() + " of " + ACH.length);
        double yy = 160;
        for (String[] a : ACH) {
            boolean got = all.contains(a[0]);
            text(g, (got ? "* " : "  ") + a[1], px + 18, yy, F_XSB, got ? C_GOLD : C_FAINT);
            yy += 19;
            if (yy > 800) break;
        }
        u.button(g, 48, 832, 200, 44, "Back", B_GHOST, () -> u.go(u.game != null && u.game.over ? SC_LEGACY : SC_HOME));
    }
}
