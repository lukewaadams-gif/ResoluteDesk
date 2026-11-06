package resolutedesk.history;

import static resolutedesk.congress.CongressRules.*;
import static resolutedesk.core.Dates.*;
import static resolutedesk.core.Fmt.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.data.Names.*;
import static resolutedesk.executive.Posts.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.history.HistoryData.*;
import static resolutedesk.model.NewsKinds.*;
import static resolutedesk.opinion.Issues.*;
import static resolutedesk.sim.DecisionKinds.*;
import static resolutedesk.world.WarData.*;
import static resolutedesk.world.WorldData.*;

import java.util.ArrayList;
import java.util.Random;
import resolutedesk.congress.Member;
import resolutedesk.executive.Cabinet;
import resolutedesk.home.Home;
import resolutedesk.home.HomeFront;
import resolutedesk.model.Game;
import resolutedesk.sim.Decision;
import resolutedesk.sim.Decisions;
import resolutedesk.ui.Flag;
import resolutedesk.world.Diplo;
import resolutedesk.world.War;
import resolutedesk.world.Wars;
import resolutedesk.world.World;

/** History as it unfolds: the turning points of each era, the wars of the day and the world changing on schedule. */
public final class History {
    public static void setup(Game g) {
        World w = g.world;
        w.wars = new ArrayList<War>();
        for (int i = 0; i < WAR_ID.length; i++) w.wars.add(newWar(i));
        w.hfired = new boolean[FLASH.length];
        int y = g.era.year;
        for (String s : WAR_START) {
            if (!s.startsWith(y + "|")) continue;
            for (String tok : s.substring(5).split(" ")) {
                String[] t = tok.split(":");
                War war = war(w, t[0]);
                war.stage = 2; war.front = Double.parseDouble(t[1]); war.troops = Double.parseDouble(t[2]); war.support = Double.parseDouble(t[3]); war.talks = Double.parseDouble(t[4]);
                war.started = -52;
                if (war.stakes >= 3) war.strategy = 2;
            }
        }
        if (g.set.scenario == 2 && y < 2021 && activeWars(w) == 0) {
            War war = war(w, y < 1960 ? "korea" : y < 1990 ? "vietnam" : y < 2008 ? "afghan" : "isis");
            war.stage = 2; war.troops = war.need * 0.6; war.front = 50; war.started = 0;
        }
        g.econ.war = warSpend(w); g.econ.war0 = g.econ.war;
        // the flashpoints of years already past are spent
        for (int i = 0; i < FLASH.length; i++) if (flashDay(i) < 0) w.hfired[i] = true;
        Home h = g.home;
        if (h != null && y < 2025) {
            int[] yy = {1933, 1941, 1953, 1961, 1965, 1969, 1973, 1977, 1981, 1985, 1989, 1993, 1997, 2001, 2005, 2009, 2013, 2017, 2021};
            double[] cr = {85, 60, 55, 45, 54, 89, 112, 128, 160, 150, 179, 200, 165, 136, 126, 116, 99, 103, 107};
            double[] od = {15, 15, 15, 15, 18, 22, 25, 25, 25, 28, 30, 30, 30, 32, 38, 45, 52, 78, 100};
            for (int i = 0; i < yy.length; i++) if (y >= yy[i]) { h.crime = cr[i]; h.od = od[i]; }
            h.grid = y < 1990 ? 30 : 40;
            if (y >= 1965 && y < 1973) h.protest = 55;
            if (y == 2021) { h.pand = 2; h.pandWeek = -45; h.deaths = 400000; }
            if (y < 2009) h.followers = 0;
        }
    }

    static War newWar(int i) {
        War war = new War();
        war.id = WAR_ID[i]; war.name = WAR_NAME[i];
        String[] t = WAR_SPEC[i].split("\\|");
        war.enemy = t[0].equals("-") ? -1 : ctIdx(t[0]); war.ally = t[1].equals("-") ? -1 : ctIdx(t[1]);
        war.need = Double.parseDouble(t[2]); war.drift = Double.parseDouble(t[3]); war.lethal = Double.parseDouble(t[4]); war.cost = Double.parseDouble(t[5]);
        war.support = Double.parseDouble(t[6]); war.stakes = Double.parseDouble(t[7]);
        return war;
    }

    public static War war(World w, String id) { if (w.wars == null) return null; for (War x : w.wars) if (x.id.equals(id)) return x; return null; }

    static int activeWars(World w) { int n = 0; if (w.wars != null) for (War x : w.wars) if (x.stage == 2) n++; return n; }

    static double warSpend(World w) { double s = 0; if (w.wars != null) for (War x : w.wars) if (x.stage == 2) s += x.troops * x.cost; return s; }

    /** The game day of a flashpoint's date. */
    public static int dayOf(String f) {
        String d = f.substring(0, 10);
        return dayOfDate(Integer.parseInt(d.substring(0, 4)), Integer.parseInt(d.substring(5, 7)) - 1, Integer.parseInt(d.substring(8, 10)));
    }

    static int[] flashDays;
    static int flashKey = -1;

    /** Flashpoint days for the current calendar, cached. */
    static int flashDay(int i) {
        int key = CAL_Y * 10000 + CAL_M * 100 + CAL_D;
        if (flashDays == null || flashKey != key) {
            flashDays = new int[FLASH.length];
            for (int k = 0; k < FLASH.length; k++) flashDays[k] = dayOf(FLASH[k]);
            flashKey = key;
        }
        return flashDays[i];
    }

    /** The world of an inauguration year: names, leaders, alignments, relations, power, the bomb, trade. */
    public static void world(Game g, int year, boolean fresh) {
        World w = g.world;
        if (w.name == null) {
            w.name = new String[NC]; w.align = new int[NC]; w.base = new double[NC]; w.power = new double[NC]; w.nuke = new boolean[NC]; w.gone = new boolean[NC];
            for (int c = 0; c < NC; c++) { w.name[c] = CT_NAME[c]; w.align[c] = CT_ALIGN[c]; w.base[c] = CT_REL[c]; w.power[c] = CT_POWER[c]; w.nuke[c] = CT_NUKE[c]; }
        }
        w.modern = year >= 2009;
        w.tradeK = year >= 2021 ? 1 : g.era.nominalGdp / 29.7 * (year < 1946 ? 0.5 : year < 1971 ? 0.65 : year < 1991 ? 0.85 : 1);
        String spec = null;
        for (String s : WORLD_ERA) if (s.startsWith(year + "|")) spec = s;
        if (spec == null) return;
        for (int c = 0; c < NC; c++) {
            if (fresh) { w.name[c] = CT_NAME[c]; w.leader[c] = LEADER25[c].split("\\|")[1] + " of " + CT_NAME[c]; w.ltitle[c] = LEADER25[c].split("\\|")[1]; }
            w.gone[c] = false;
        }
        for (String b : BORN.split(" ")) { String[] p = b.split(":"); int c = ctIdx(p[0]); if (c >= 0 && year < Integer.parseInt(p[1])) w.gone[c] = true; }
        for (String b : NUKES.split(" ")) { String[] p = b.split(":"); int c = ctIdx(p[0]); if (c >= 0) w.nuke[c] = year >= Integer.parseInt(p[1]); }
        if (year < 1947) for (int c = 0; c < NC; c++) if (w.align[c] == AL_TREATY) w.align[c] = AL_PARTNER;
        for (String tok : spec.substring(5).split("\\^")) {
            int eq = tok.indexOf('=');
            int c = ctIdx(tok.substring(0, eq));
            if (c < 0) continue;
            String v = tok.substring(eq + 1);
            if (v.equals("-")) { w.gone[c] = true; continue; }
            w.gone[c] = false;
            String[] p = v.split(";", -1);
            if (!p[0].isEmpty()) w.name[c] = p[0]; else if (fresh) w.name[c] = CT_NAME[c];
            if (!p[1].isEmpty()) { w.leader[c] = p[1]; w.ltitle[c] = p[2]; }
            if (!p[3].isEmpty()) { double nb = Double.parseDouble(p[3]); if (fresh) w.rel[c] = nb; else w.rel[c] = clamp(w.rel[c] + (nb - w.base[c]) * 0.5, -100, 100); w.base[c] = nb; }
            if (!p[4].isEmpty()) w.align[c] = Integer.parseInt(p[4]);
            if (p.length > 5 && !p[5].isEmpty()) w.power[c] = Double.parseDouble(p[5]);
        }
        if (!w.modern) {
            w.twStage = 0; w.twt = 10; w.uaStage = 0; w.ukr = 50; w.talks = 0; w.gaza = 10; w.iran = 5; w.redsea = 5; w.proxy = 10; w.nk = year >= 1953 ? 40 : 0;
            w.terror = year >= 1993 ? 35 : 10; w.cyber = year >= 1995 ? 25 : 0; w.migr = year >= 1975 ? 40 : 20; w.cartel = year >= 1980 ? 40 : 10;
            for (int c = 0; c < NC; c++) { w.sanction[c] = 0; w.aid[c] = 0; }
            if (year >= 1960 && year < 2015) w.sanction[CC_CU] = 3;
            if (year >= 1950) w.sanction[CC_KP] = 3;
            if (year >= 1950 && year < 1972) w.sanction[CC_CN] = 3;
            if (year >= 1979) w.sanction[CC_IR] = 2;
            if (year >= 1990 && year < 2003) w.sanction[CC_IQ] = 3;
            if (year >= 1948 && year < 1991) w.sanction[CC_RU] = 1;
            if (year >= 1945) w.aid[CC_IL] = year >= 1973 ? 2 : 0;
            if (year >= 1979) w.aid[CC_EG] = 1;
            if (year >= 1955 && year < 1975) w.aid[CC_VN] = 2;
            w.nato = year >= 1949 ? 60 : 30; w.pacific = year >= 1952 ? 60 : 30; w.allySpend = year < 1991 ? 3.5 : 1.8;
        } else if (year < 2025) {
            w.twStage = year >= 2021 ? 1 : 0; w.twt = year >= 2021 ? 40 : 30;
            w.uaStage = year >= 2017 ? 1 : 0; w.ukr = 50; w.talks = year >= 2017 ? 30 : 0;
            w.gaza = 25; w.iran = year >= 2017 ? 45 : 60; w.redsea = 20; w.proxy = year >= 2013 ? 45 : 35; w.nk = year >= 2017 ? 70 : 55; w.terror = 45;
            w.aid[CC_UA] = year >= 2017 ? 1 : 0;
        }
        for (int c = 0; c < NC; c++) if (w.gone[c]) { w.tariff[c] = 0; w.sanction[c] = 0; w.aid[c] = 0; }
    }

    public static void week(Game g) {
        World w = g.world;
        if (w.wars == null) return;
        // a new era's leaders arrive on inauguration day, four years on
        int y = yearNow(g);
        int[] d = dateOf(g.day());
        if (d[1] == 0 && d[2] >= 14 && d[2] <= 27 && y % 4 == 1 && y != g.era.year && y < 2025 && w.worldYear != y) { w.worldYear = y; world(g, y, false); }
        if (d[1] == 0 && d[2] <= 7 && w.pressYear != y) {
            boolean[] was = new boolean[NPOST];
            for (int k = 0; k < NPOST; k++) was[k] = POST_OFF[k];
            w.pressYear = y; applyYear(y); GEN_YEAR = y;
            for (int k = 0; k < NPOST; k++) if (was[k] && !POST_OFF[k]) Cabinet.created(g, k);
            // committees renamed by Congress keep their chairs
            for (Member m : g.cong.members) if (m.role == RO_CHAIR) {
                String[] names = m.chamber == HOUSE ? CHAIR_H : CHAIR_S;
                for (int k = 0; k < names.length; k++) if (sameCommittee(m.chamber, k, m.committee)) { m.committee = names[k]; m.title = "Chair, " + (m.chamber == HOUSE ? "House " : "Senate ") + names[k]; break; }
            }
        }
        wars(g);
        flash(g);
    }

    static void wars(Game g) {
        World w = g.world;
        Random r = g.rng;
        double dq = Cabinet.pq(g, P_DEF);
        for (War x : new ArrayList<War>(w.wars)) {
            if (x.stage != 2) continue;
            x.weeks++;
            if (x.kind >= 0 && Wars.step(g, x)) continue;
            double eff = x.troops <= 0 ? -1.2 : clamp(Math.log(x.troops / x.need) * 0.9, -1.5, 1.2);
            double strat = x.strategy == 0 ? -0.15 : x.strategy == 2 ? 0.45 : x.strategy == 3 ? -0.2 : 0;
            double allyK = x.ally >= 0 ? (w.rel[x.ally] - 40) * 0.003 : 0;
            double sk = Math.sqrt(Math.max(0.5, x.stakes));
            x.front = clamp(x.front + (x.drift + eff * 0.35 + strat + allyK + (dq - 0.6) * 0.4 + r.nextGaussian() * 0.9) / sk, 0, 100);
            double kiaRate = x.lethal * (x.strategy == 0 ? 0.6 : x.strategy == 2 ? 1.6 : x.strategy == 3 ? 0.7 : 1) * (1.2 - dq * 0.4);
            double dead = x.troops * kiaRate * (0.6 + 0.8 * r.nextDouble());
            x.kia += dead;
            w.kia += (int) Math.round(dead);
            if (dead > 0) g.perf -= x.kind >= 0 ? Math.min(0.3, dead / 250.0) : Math.min(0.4, dead / (x.stakes * x.stakes) / 3000.0);
            double supT = x.kind >= 0 ? Wars.supportTarget(x) : 35 + 18 * Math.min(3, x.stakes);
            x.support = clamp(x.support + (supT - x.support) * (x.kind >= 0 ? 0.03 : 0.01) - dead / x.stakes / 2000.0 - (x.weeks > 104 ? 0.03 : 0) + (x.front - 50) * 0.003, 5, 98);
            x.talks = clamp(x.talks + (x.strategy == 3 ? 1.5 : 0.1) + (x.front > 65 ? 0.4 : 0) - (x.front < 35 ? 0.3 : 0) + r.nextGaussian() * 0.6, 0, 100);
            g.issueAdj[PI_SECURITY] = clamp(g.issueAdj[PI_SECURITY] + (x.support - 50) * 0.0004 + (x.front - 50) * 0.0002, -1.5, 1.5);
            g.issueSal[PI_SECURITY] = clamp(g.issueSal[PI_SECURITY] + 0.004 * x.stakes, 0.2, 2.2);
            if (dead > 50 && r.nextDouble() < 0.06) g.log(NK_WORLD, x.name + ": " + thousands(Math.round(x.kia)) + " Americans killed so far", null, 1.2);
            if (x.front >= 100) { if (x.kind < 0) end(g, x, 3); else Wars.victory(g, x); }
            else if (x.front <= 0) end(g, x, 4);
            else if (x.talks >= 100 && x.front >= 25) end(g, x, 5);
            else if (x.troops <= 0.5 && x.weeks > 4) end(g, x, x.front >= 60 ? 5 : 4);
        }
        g.econ.war = warSpend(w);
    }

    /** Words for how a war ended, after its name: " won", " lost", and so on. */
    static final String[] ENDED = {"", "", "", " won", " lost", " ended by agreement", " ended on a withdrawal timetable", " handed over", " ended when the president declared victory"};

    public static void end(Game g, War x, int how) {
        if (x.stage != 2) return;
        x.stage = how; x.ended = g.week;
        String up = x.name.toUpperCase();
        String title = how == 3 ? up + " ENDS IN VICTORY" : how == 4 ? up + " ENDS IN DEFEAT" : how == 6 ? up + " ENDS AS THE LAST TROOPS COME HOME"
                : how == 7 ? up + ": AMERICAN FORCES HAND OVER AND COME HOME" : how == 8 ? "PRESIDENT DECLARES VICTORY: " + up + " IS OVER" : up + " ENDS IN AN AGREEMENT";
        String body = thousands(Math.round(x.kia)) + " Americans died over " + Math.round(x.weeks / 52.0 * 10) / 10.0 + " years.";
        g.log(NK_BIG, title, body, 5);
        String nm = x.name.startsWith("The ") ? "the " + x.name.substring(4) : x.name;
        String when = dateStr(g.day()) + ": ";
        g.world.record.add(when + nm + ENDED[how]);
        if (how == 3 && x.kind >= 0) { g.perf += 1.5 + x.stakes; g.rally += 2 + x.stakes; g.milestones.add(when + "won " + nm); }
        else if (how == 3) { g.perf += 3 + x.stakes; g.rally += 6 + x.stakes * 2; g.issueAdj[PI_SECURITY] = clamp(g.issueAdj[PI_SECURITY] + 0.5, -1.5, 1.5); g.milestones.add(when + "won " + nm); }
        else if (how == 4) { g.perf -= 3; g.issueAdj[PI_FOREIGN] = clamp(g.issueAdj[PI_FOREIGN] - 0.5, -1.5, 1.5); g.milestones.add(when + "lost " + nm); }
        else if (how == 5) { g.perf += 1.5; g.milestones.add(when + "ended " + nm + " by agreement"); }
        else g.milestones.add(when + nm + ENDED[how]);
        if (x.id.equals("ww2e") && how == 3 && x.enemy >= 0) { g.world.rel[x.enemy] = 0; g.world.align[x.enemy] = 0; g.world.leader[x.enemy] = "the Allied occupation"; g.world.ltitle[x.enemy] = "Military government"; }
        if (x.id.equals("ww2p") && how == 3 && x.enemy >= 0) { g.world.rel[x.enemy] = 10; g.world.align[x.enemy] = 0; g.world.leader[x.enemy] = "the American occupation"; g.world.ltitle[x.enemy] = "Military government"; }
        if (how == 3 && x.stakes >= 3) g.econ.demandShock += 0.45 * x.troops * x.cost * 0.8;
        x.troops = 0;
        Decisions.info(g, how == 3 ? "Victory" : how == 4 ? "Defeat" : "The War Is Over", x.name + (how == 3 ? " ends in victory. " : how == 4 ? " ends in defeat. " : how == 5 ? " ends in an agreement. " : ENDED[how].replace(" ended", " ends") + ". ") + body);
    }

    static void flash(Game g) {
        World w = g.world;
        int today = g.day();
        for (int i = 0; i < FLASH.length; i++) {
            if (w.hfired[i]) continue;
            String[] t = FLASH[i].split("\\|", 7);
            int day = flashDay(i), win = Integer.parseInt(t[1]);
            if (today + 7 <= day) continue;
            if (today > day + win * 7) { w.hfired[i] = true; continue; }
            if (!cond(g, t[2])) continue;
            w.hfired[i] = true;
            if (!t[5].isEmpty()) Diplo.fx(g, t[5]);
            g.log(NK_BIG, t[3].toUpperCase(), null, 3.5);
            if (t[6].isEmpty()) { Decisions.info(g, t[3], t[4]); return; }
            String[] opts = t[6].split(";;");
            String[] lab = new String[opts.length], adv = new String[opts.length];
            for (int k = 0; k < opts.length; k++) {
                String[] o = opts[k].split("\\|");
                lab[k] = o[0];
                adv[k] = o[1] + ": \"" + Diplo.odds(g, o[2], Double.parseDouble(o[3])) + "\"";
            }
            Decision dd = Decisions.add(g, D_HIST, t[3], t[4] + " Your advisers each have a view; the odds are their best guess.", lab, adv);
            dd.a = i;
            g.stress = Math.min(100, g.stress + 4);
            return;
        }
    }

    public static void decide(Game g, int i, int k) {
        String[] t = FLASH[i].split("\\|", 7);
        String[] o = t[6].split(";;")[k].split("\\|");
        boolean ok = g.rng.nextDouble() < Diplo.p(g, o[2], Double.parseDouble(o[3]));
        Diplo.fx(g, ok ? o[4] : o[5]);
        if (ok) { g.okCount++; g.histOk.add(t[3] + ": " + o[0]); } else g.failCount++;
        String story = ok ? o[6] : o[7];
        g.log(ok ? NK_GOOD : NK_BAD, story, null, 2.2);
        g.milestones.add(dateStr(g.day()) + ": " + t[3] + " - " + o[0].toLowerCase());
        Decisions.info(g, ok ? "It worked" : "It didn't go as hoped", story);
    }

    public static int auto(Game g, int i) {
        String[] o = FLASH[i].split("\\|", 7)[6].split(";;");
        int best = 0;
        double bv = -9;
        for (int k = 0; k < o.length; k++) {
            String[] t = o[k].split("\\|");
            double q = Diplo.p(g, t[2], Double.parseDouble(t[3]));
            double v = q + (t[4].contains("perf=-") ? -0.15 : 0) + (t[4].contains("rights=-") ? -0.3 : 0) - (q >= 1 ? 0.25 : 0);
            if (v > bv) { bv = v; best = k; }
        }
        return best;
    }

    public static double var(Game g, String k) {
        World w = g.world;
        if (k.equals("year")) return yearNow(g);
        if (k.startsWith("war_")) { War x = war(w, k.substring(4)); return x == null ? 0 : x.stage; }
        if (k.startsWith("front_")) { War x = war(w, k.substring(6)); return x == null ? 0 : x.front; }
        if (k.startsWith("troops_")) { War x = war(w, k.substring(7)); return x == null ? 0 : x.troops; }
        if (k.startsWith("rel_")) { int c = ctIdx(k.substring(4)); return c < 0 ? 0 : w.rel[c]; }
        return HomeFront.var(g, k);
    }

    static boolean cond(Game g, String c) {
        if (c.isEmpty()) return true;
        for (String t : c.split(",")) {
            if (t.startsWith("p=")) { if (g.rng.nextDouble() >= Double.parseDouble(t.substring(2))) return false; continue; }
            int i = t.indexOf('>'), j = t.indexOf('<');
            if (i > 0) { if (!(var(g, t.substring(0, i)) > Double.parseDouble(t.substring(i + 1)))) return false; }
            else if (j > 0) { if (!(var(g, t.substring(0, j)) < Double.parseDouble(t.substring(j + 1)))) return false; }
        }
        return true;
    }

    /** The effects history adds to the interpreter: wars, leaders, alignments. Returns true if it handled the key. */
    public static boolean fxKey(Game g, String k, String v) {
        World w = g.world;
        int c2 = k.indexOf(':');
        if (c2 < 0) {
            if (k.equals("pandnow")) { if (g.home != null && g.home.pand == 0) { g.home.pand = 2; g.home.pandWeek = g.week; } return true; }
            if (k.equals("slump")) { g.econ.slump += Double.parseDouble(v); return true; }
            return false;
        }
        String key = k.substring(0, c2), arg = k.substring(c2 + 1);
        War x = war(w, arg);
        if (key.equals("warstart")) { if (x != null && x.stage != 2) { x.stage = 2; g.warsStarted++; x.troops = Double.parseDouble(v); x.front = 50; x.started = g.week; x.weeks = 0; g.rally += 4 + x.stakes; g.log(NK_BIG, ("America goes to war: " + x.name).toUpperCase(), null, 5); g.econ.war = warSpend(w); } return true; }
        if (key.equals("war")) { if (x != null && x.stage == 2) x.front = clamp(x.front + Double.parseDouble(v), 0, 100); return true; }
        if (key.equals("troops")) { if (x != null && x.stage == 2) x.troops = Math.max(0, x.troops + Double.parseDouble(v)); g.econ.war = warSpend(w); return true; }
        if (key.equals("strat")) { if (x != null && x.stage == 2) x.strategy = (int) Double.parseDouble(v); return true; }
        if (key.equals("talks")) { if (x != null && x.stage == 2) x.talks = clamp(x.talks + Double.parseDouble(v), 0, 100); return true; }
        if (key.equals("support")) { if (x != null && x.stage == 2) x.support = clamp(x.support + Double.parseDouble(v), 0, 100); return true; }
        if (key.equals("warend")) { if (x != null && x.stage == 2) end(g, x, (int) Double.parseDouble(v)); return true; }
        int c = ctIdx(arg);
        if (key.equals("leader")) { if (c == CC_CN) Flag.roc = false; if (c >= 0) { String[] p = v.split(";"); w.leader[c] = p[0].replace('_', ' '); w.ltitle[c] = p.length > 1 ? p[1].replace('_', ' ') : w.ltitle[c]; } return true; }
        if (key.equals("align")) { if (c >= 0 && w.align != null) w.align[c] = (int) Double.parseDouble(v); return true; }
        if (key.equals("name")) { if (c >= 0 && w.name != null) w.name[c] = v.replace('_', ' '); return true; }
        if (key.equals("nuke")) { if (c >= 0 && w.nuke != null) w.nuke[c] = Double.parseDouble(v) > 0; return true; }
        if (key.equals("gone")) { if (c >= 0 && w.gone != null) w.gone[c] = Double.parseDouble(v) > 0; return true; }
        if (key.equals("power")) { if (c >= 0 && w.power != null) w.power[c] = Double.parseDouble(v); return true; }
        if (key.equals("aid")) { if (c >= 0) w.aid[c] = (int) Double.parseDouble(v); return true; }
        return false;
    }

    // ---- the president's levers in a war ----
    public static void setTroops(Game g, War x, double t) {
        if (x.stage != 2) return;
        double old = x.troops;
        x.troops = clamp(t, 0, x.need * 1.6);
        g.econ.war = warSpend(g.world);
        if (x.troops > old) { x.support = clamp(x.support - 2 / x.stakes, 0, 100); g.log(NK_WORLD, "The president orders " + thousands(Math.round((x.troops - old) * 1000)) + " more troops to " + x.name.toLowerCase().replace("the ", ""), null, 1.6); }
        else if (x.troops < old) g.log(NK_WORLD, thousands(Math.round((old - x.troops) * 1000)) + " troops are coming home from " + x.name.toLowerCase().replace("the ", ""), null, 1.6);
    }

    public static void setStrategy(Game g, War x, int s) {
        if (x.stage != 2 || x.strategy == s) return;
        x.strategy = s;
        g.log(NK_WORLD, x.name + ": the president orders a new strategy, " + STRATEGY[s].toLowerCase(), null, 1.0);
    }

    public static void withdraw(Game g, War x) {
        if (x.stage != 2) return;
        end(g, x, x.front >= 60 ? 5 : 4);
    }

    /** What a hands-off president does with a war: send what the generals ask while the public backs it, look for a way out when it doesn't. */
    public static void autoWar(Game g) {
        if (g.world == null || g.world.wars == null || g.week % 4 != 0) return;
        for (War x : g.world.wars) {
            if (x.stage != 2) continue;
            double target = x.support < 30 ? x.troops * 0.85 : x.front < 85 ? x.need * 1.25 : x.need;
            if (x.stakes >= 3) target = x.need * 1.4;
            setTroops(g, x, x.troops + (target - x.troops) * 0.25);
            int s = x.support < 35 && x.stakes < 2 ? 3 : x.front < 40 && x.support > 50 ? 2 : 1;
            if (x.stakes >= 3 && x.front < 90) s = 2;
            setStrategy(g, x, s);
        }
    }

    public static String countryName(Game g, int c) { return g.world != null && g.world.name != null ? g.world.name[c] : CT_NAME[c]; }
}
