package resolutedesk.diagnostics;

import static resolutedesk.campaign.CampaignData.*;
import static resolutedesk.congress.BillData.*;
import static resolutedesk.congress.CongressRules.*;
import static resolutedesk.core.Dates.*;
import static resolutedesk.core.Fmt.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.data.Candidates.*;
import static resolutedesk.data.ElectoralMap.*;
import static resolutedesk.data.Eras.*;
import static resolutedesk.data.HistoricalFigures.*;
import static resolutedesk.data.StateData.*;
import static resolutedesk.executive.ActionData.*;
import static resolutedesk.executive.CourtHistory.*;
import static resolutedesk.executive.Delegation.*;
import static resolutedesk.executive.Posts.*;
import static resolutedesk.executive.ScandalData.*;
import static resolutedesk.executive.Traits.*;
import static resolutedesk.executive.VicePresData.*;
import static resolutedesk.history.HistoryData.*;
import static resolutedesk.home.DisasterData.*;
import static resolutedesk.home.EventData.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.opinion.Issues.*;
import static resolutedesk.press.Outlets.*;
import static resolutedesk.sim.DecisionKinds.*;
import static resolutedesk.sim.Rosters.*;
import static resolutedesk.sim.Schedule.*;
import static resolutedesk.ui.RoomIds.*;
import static resolutedesk.ui.ScreenIds.*;
import static resolutedesk.ui.Theme.*;
import static resolutedesk.world.CrisisData.*;
import static resolutedesk.world.WarData.*;
import static resolutedesk.world.WorldData.*;
import static resolutedesk.world.WorldTables.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import resolutedesk.ResoluteDesk;
import resolutedesk.campaign.Reelection;
import resolutedesk.congress.Bill;
import resolutedesk.congress.BillDef;
import resolutedesk.congress.Budget;
import resolutedesk.congress.Congress;
import resolutedesk.congress.Legis;
import resolutedesk.executive.Action;
import resolutedesk.executive.Actions;
import resolutedesk.executive.Cabinet;
import resolutedesk.executive.Courts;
import resolutedesk.executive.Exec;
import resolutedesk.executive.Justice;
import resolutedesk.executive.Person;
import resolutedesk.executive.Scandal;
import resolutedesk.executive.Trouble;
import resolutedesk.history.History;
import resolutedesk.home.Home;
import resolutedesk.home.HomeFront;
import resolutedesk.legacy.HallStore;
import resolutedesk.legacy.Legacy;
import resolutedesk.model.Cand;
import resolutedesk.model.Econ;
import resolutedesk.model.Era;
import resolutedesk.model.Game;
import resolutedesk.model.Settings;
import resolutedesk.opinion.Elect;
import resolutedesk.opinion.Groups;
import resolutedesk.press.Polls;
import resolutedesk.press.QA;
import resolutedesk.press.Speech;
import resolutedesk.sim.Auto;
import resolutedesk.sim.Decision;
import resolutedesk.sim.Decisions;
import resolutedesk.sim.Setup;
import resolutedesk.sim.Week;
import resolutedesk.ui.Creator;
import resolutedesk.ui.HillUI;
import resolutedesk.ui.Night;
import resolutedesk.ui.PressRoom;
import resolutedesk.ui.Rooms;
import resolutedesk.ui.Screens;
import resolutedesk.ui.SitRoom;
import resolutedesk.ui.USMap;
import resolutedesk.ui.WestWing;
import resolutedesk.world.Diplo;
import resolutedesk.world.Summit;
import resolutedesk.world.War;
import resolutedesk.world.World;

/** Command-line checks of the game's data, simulations of whole presidencies, and screenshots of every screen. Run with "test check", "test sim ..." or "test ui <dir>". */
public final class Diagnostics {
    public static void run(String[] a) {
        long t0 = System.currentTimeMillis();
        Elect.init();
        Groups.init();
        USMap.load();
        System.out.println("init " + (System.currentTimeMillis() - t0) + " ms");
        String what = a.length > 1 ? a[1] : "sim";
        HallStore.off = true;
        for (String x : a) if (x.equals("trace")) TRACE = true;
        if (what.equals("sim")) {
            int runs = a.length > 2 ? Integer.parseInt(a[2]) : 1;
            int year = a.length > 3 ? Integer.parseInt(a[3]) : 2029;
            String pres = a.length > 4 ? a[4] : null;
            int scen = a.length > 5 ? Integer.parseInt(a[5]) : 0;
            int weeks = a.length > 6 ? Integer.parseInt(a[6]) : 208;
            for (int r = 0; r < runs; r++) sim(1000 + r, year, pres, scen, weeks);
        }
        if (what.equals("ui")) ui(a.length > 2 ? a[2] : "shots");
        if (what.equals("check")) check();
        if (what.equals("impeach")) for (int r = 0; r < (a.length > 2 ? Integer.parseInt(a[2]) : 5); r++) impeach(2000 + r, a.length > 3 ? Double.parseDouble(a[3]) : 9);
        if (what.equals("whip")) whip(a.length > 2 ? Integer.parseInt(a[2]) : 2025, a.length > 3 ? a[3] : null, a.length > 4 ? Integer.parseInt(a[4]) : 0);
    }

    static Game newGame(long seed, int year, String pres, int scen) {
        Settings s = new Settings();
        s.year = year; s.seed = seed; s.scenario = scen;
        Era e = era(year);
        s.presId = pres != null ? pres : eraRoster(e).get(0).id;
        s.vpId = defaultVp(e, candById(s.presId));
        return Setup.newGame(s);
    }

    static void sim(long seed, int year, String pres, int scen, int weeks) {
        Game g = newGame(seed, year, pres, scen);
        StringBuilder b = new StringBuilder("=== seed " + seed + " " + year + " " + g.pres.name() + " (" + PARTY_SHORT[g.party] + ") " + SCEN[scen]
                + "  House D " + g.houseD + "  Senate D " + g.senateD + "  approval " + fmt1(g.approval) + "\n");
        for (int w = 0; w < weeks && !g.over && !g.removed; w++) {
            Auto.week(g);
            Week.resolve(g);
            Decisions.autoAll(g);
            if (g.week % 26 == 0) {
                Econ e = g.econ;
                b.append("  wk").append(g.week).append(" ").append(dateStr(g.day())).append(": approval ").append(fmt1(g.approval)).append(" gdp ").append(fmt1(e.gdp)).append(" u ").append(fmt1(e.unemp))
                        .append(" infl ").append(fmt1(e.infl)).append(" fed ").append(fmt2(e.fed)).append(" 10y ").append(fmt2(e.tenYr)).append(" sp ").append(Math.round(e.stocks))
                        .append(" gas ").append(fmt2(e.gas)).append(" def ").append(fmt1(e.deficit)).append(" debt ").append(fmt1(e.debt)).append(" sent ").append(fmt0(e.sentiment))
                        .append(" stamina ").append(fmt0(g.stamina)).append(" stress ").append(fmt0(g.stress)).append(" PA ").append(fmt0(g.stateApp[stateIdx("PA")]))
                        .append(" Black ").append(fmt0(g.groupApp[10])).append(" WhiteNoDeg ").append(fmt0(g.groupApp[8])).append("\n");
            }
        }
        Congress c = g.cong;
        b.append("  congress: laws ").append(c.laws).append(" vetoes ").append(c.vetoes).append(" overrides ").append(c.overrides).append(" shutdowns ").append(c.shutdowns).append(" (").append(c.shutdownTotal).append(" wks) default ").append(c.defaulted)
                .append(" capital ").append(fmt0(g.capital)).append(" House D ").append(g.houseD).append(" Senate D ").append(g.senateD).append("\n");
        for (Bill bl : c.bills) b.append("    ").append(bl.ours ? "* " : "  ").append(bl.name).append(": ").append(STAGE[bl.stage]).append(" wk").append(bl.introduced).append("-").append(bl.stageWeek).append(bl.recon ? " recon" : "").append(" H ").append(bl.houseYes).append("-").append(bl.houseNo).append(" S ").append(bl.senYes).append("-").append(bl.senNo).append("\n");
        if (c.midSummary != null) b.append("  midterms: ").append(c.midSummary).append("\n");
        Exec x = g.ex;
        int conf = 0, live = 0, blocked = 0, struck = 0, scs = 0;
        for (Person p : x.post) if (p != null) conf++;
        for (Action a : x.actions) { if (a.status == A_LIVE) live++; if (a.status == A_BLOCKED) blocked++; if (a.status == A_STRUCK) struck++; }
        for (Scandal s : x.scandals) if (s.severity >= 3 || s.kind != SK_LEAK) scs++;
        double mor = 0; for (Person p : Cabinet.everyone(g)) mor += p.morale; mor /= Math.max(1, Cabinet.everyone(g).size());
        double rd = 0; for (double v : x.ready) rd += v; rd /= NPOST;
        int on = 0; for (int k = 0; k < NPOST; k++) if (postOn(k)) on++;
        b.append("  exec: posts filled ").append(conf).append("/").append(on).append(" departures ").append(x.departures).append(" leaks ").append(x.leaks).append(" morale ").append(fmt0(mor))
                .append(" readiness ").append(fmt0(rd)).append(" failures ").append(x.failures).append(" actions ").append(x.actions.size()).append(" live ").append(live).append(" blocked ").append(blocked).append(" struck ").append(struck)
                .append(" cases ").append(x.cases.size()).append(" judges ").append(x.judges).append(" justices ").append(x.justices).append(" court R ").append(Courts.count(g, REP)).append("-D ").append(Courts.count(g, DEM))
                .append(" scandals ").append(scs).append(" pardons ").append(x.pardons).append(" probes ").append(x.probes.size()).append(" impeach ").append(x.impeachments).append(" removed ").append(g.removed).append("\n");
        World wd = g.world;
        Home hm = g.home;
        b.append("  home: crime ").append(fmt0(hm.crime)).append(" od ").append(fmt0(hm.od)).append(" protest ").append(fmt0(hm.protest)).append(" grid ").append(fmt0(hm.grid)).append(" pandemic ").append(hm.pand).append(" deaths ").append(hm.deaths)
                .append(" disasters ").append(hm.disasters.size()).append(" speeches ").append(hm.speeches).append(" factions ").append(fmt0(hm.fac[0])).append("/").append(fmt0(hm.fac[1])).append(" opp ").append(hm.opp).append(" press ").append(fmt0(hm.press)).append("\n");
        b.append("  world: taiwan ").append(TW_STAGE[wd.twStage]).append(" (t").append(fmt0(wd.twt)).append(" d").append(fmt0(wd.deter)).append(") ukraine ").append(UA_STAGE[wd.uaStage]).append(" (s").append(fmt0(wd.ukr)).append(" talks ").append(fmt0(wd.talks))
                .append(") gaza ").append(fmt0(wd.gaza)).append(" iran ").append(fmt0(wd.iran)).append(" redsea ").append(fmt0(wd.redsea)).append(" nk ").append(fmt0(wd.nk)).append(" terror ").append(fmt0(wd.terror))
                .append(" crises ").append(wd.crises).append(" deals ").append(wd.deals).append(" kia ").append(wd.kia).append(" nato ").append(fmt0(wd.nato)).append(" pacific ").append(fmt0(wd.pacific))
                .append(" relCN ").append(fmt0(wd.rel[CC_CN])).append(" relRU ").append(fmt0(wd.rel[CC_RU])).append(" oil ").append(fmt0(g.econ.oil)).append(" ").append(wd.leader[CC_GB]).append("\n");
        for (War wr : wd.wars) if (wr.stage != 0) b.append("  war: ").append(wr.name).append(" ").append(WAR_STAGE[wr.stage]).append(" front ").append(fmt0(wr.front)).append(" troops ").append(fmt0(wr.troops)).append("k kia ").append(Math.round(wr.kia)).append(" support ").append(fmt0(wr.support)).append(" talks ").append(fmt0(wr.talks)).append(" weeks ").append(fmt0(wr.weeks)).append("\n");
        for (String m : g.milestones) b.append("  * ").append(m).append("\n");
        b.append("  ending: ").append(g.over ? END_NAME[g.ending] : g.removed ? "removed" : "still in office").append(" after ").append(g.term).append(g.term == 1 ? " term" : " terms").append(", week ").append(g.week).append(" ").append(dateStr(g.day())).append("\n");
        if (g.re != null && g.re.done) b.append("  election ").append(g.re.year).append(": ").append((g.re.nominee != null ? g.re.nominee : g.pres).name()).append(" vs ").append(g.re.opp.name()).append(" ").append(g.re.evMe).append("-").append(g.re.evOpp).append(" pop ").append(fmt1(g.re.popMe)).append("\n");
        int[] lsc = Legacy.scores(g);
        Legacy.rank(g, Legacy.total(lsc));
        b.append("  verdict: ").append(Legacy.total(lsc)).append(" ").append(Arrays.toString(lsc)).append(" ").append(Legacy.rankShort).append(" ach ").append(String.join(",", Legacy.earned(g, Legacy.rankPos))).append("\n");
        b.append("  news ").append(g.news.size()).append(", last: ").append(g.news.isEmpty() ? "" : g.news.get(g.news.size() - 1).head);
        System.out.println(b);
    }

    /** A president in deep trouble with the other party running the House. */
    static void impeach(long seed, double sev) {
        Game g = newGame(seed, 2029, null, 3);
        for (int w = 0; w < 12; w++) { Week.routine(g); Decisions.autoExec(g); Week.resolve(g); Decisions.autoAll(g); }
        Trouble.add(g, SK_PARDON, "The pardon of a donor", "Diagnostics.", sev, null, true);
        g.ex.contempt = 1;
        g.inbox.clear();
        StringBuilder b = new StringBuilder("impeach seed " + seed + " sev " + sev + ":");
        int last = -1;
        for (int w = 0; w < 60 && !g.over; w++) {
            Week.routine(g); Decisions.autoExec(g); Week.resolve(g); Decisions.autoAll(g);
            if (g.ex.impStage != last) { b.append(" wk").append(g.week).append("=").append(g.ex.impStage).append("(p").append(fmt1(g.ex.impPressure)).append(",a").append(fmt0(g.approval)).append(")"); last = g.ex.impStage; }
        }
        b.append(" -> ").append(g.ex.impResult).append(g.removed ? " REMOVED" : "");
        System.out.println(b);
    }

    static final String[] FX_HIST = {"warstart", "strat", "war", "troops", "talks", "support", "warend", "leader", "align", "name", "nuke", "gone", "power", "aid", "pandnow", "slump", "farm", "ethics"};
    static final String[] FX_KEYS = {"twt", "deter", "twstage", "ua", "defcon", "ukr", "talks", "gaza", "iran", "redsea", "proxy", "nk", "terror", "cyber", "migr", "cartel", "nato", "pacific",
            "perf", "rally", "for", "sec", "imm", "crime", "oil", "fin", "def", "stress", "tcn", "kia", "econ", "prices", "jobs", "health", "rights", "tech", "energy", "edu",
            "base", "opp", "scandal", "infl", "demand", "supply", "grid", "protest", "od", "crimeidx", "pandslow", "pandfast", "pandsev", "pandend"};

    static void checkFx(String fx) {
        for (String tok : fx.split(" ")) {
            if (tok.isEmpty()) continue;
            String[] kv = tok.split("=");
            if (kv.length != 2) { System.out.println("BAD FX " + tok); continue; }
            int c2 = kv[0].indexOf(':');
            String hk = c2 > 0 ? kv[0].substring(0, c2) : kv[0];
            boolean hist = false;
            for (String k : FX_HIST) if (k.equals(hk) && (c2 > 0 || hk.equals("pandnow") || hk.equals("slump") || hk.equals("farm") || hk.equals("ethics"))) hist = true;
            if (hist) {
                String arg = c2 > 0 ? kv[0].substring(c2 + 1) : "";
                if (hk.startsWith("war") || hk.equals("strat") || hk.equals("troops") || hk.equals("talks") || hk.equals("support")) { boolean found = false; for (String w : WAR_ID) if (w.equals(arg)) found = true; if (!found) System.out.println("BAD WAR " + tok); }
                else if (c2 > 0 && ctIdx(arg) < 0) System.out.println("BAD COUNTRY " + tok);
                if (!hk.equals("leader") && !hk.equals("name")) Double.parseDouble(kv[1]);
                continue;
            }
            Double.parseDouble(kv[1]);
            boolean ok = kv[0].startsWith("rel:") && ctIdx(kv[0].substring(4)) >= 0 || kv[0].startsWith("st:") && stateIdx(kv[0].substring(3)) >= 0
                    || kv[0].length() > 1 && kv[0].charAt(0) == 'g' && Character.isDigit(kv[0].charAt(1));
            for (String k : FX_KEYS) if (k.equals(kv[0])) ok = true;
            if (!ok) System.out.println("UNKNOWN FX KEY " + kv[0]);
        }
    }

    /** Every parallel data table must line up. */
    static void check() {
        int n = AD_ID.length;
        int[] lens = {AD_KIND.length, AD_NAME.length, AD_DESC.length, AD_AREA.length, AD_LEAN.length, AD_SUP.length, AD_LEGAL.length, AD_NEEDS.length, AD_FX.length};
        for (int i = 0; i < lens.length; i++) if (lens[i] != n) System.out.println("ACTION TABLE " + i + " has " + lens[i] + ", expected " + n);
        int[] pl = {POST_SHORT.length, POST_TITLE.length, POST_WF.length, POST_AREA.length};
        for (int i = 0; i < pl.length; i++) if (pl[i] != NPOST) System.out.println("POST TABLE " + i + " has " + pl[i]);
        if (AC_NAME.length != NAC || AC_DESC.length != NAC || AC_STAMINA.length != NAC || AC_STRESS.length != NAC || AC_ISSUE.length != NAC || AC_STATE.length != NAC || AC_COL.length != NAC) System.out.println("ACTIVITY TABLES DON'T MATCH");
        if (STAFF_TITLE.length != NSTAFF || STAFF_DOES.length != NSTAFF) System.out.println("STAFF TABLES DON'T MATCH");
        if (TRAIT.length != NTRAIT || TRAIT_DESC.length != NTRAIT) System.out.println("TRAIT TABLES DON'T MATCH");
        if (VM_NAME.length != NVM || VM_DESC.length != NVM || VM_WEEKS.length != NVM) System.out.println("VP TABLES DON'T MATCH");
        if (DG_NAME.length != NDG || DG_DESC.length != NDG || DG_ROLE.length != NDG) System.out.println("DELEGATION TABLES DON'T MATCH");
        if (ADV_LABEL.length != ADV_WHO.length) System.out.println("ADVISER TABLES DON'T MATCH");
        for (String fx : AD_FX) for (String t : fx.split(" ")) if (!t.isEmpty() && t.split("=").length != 2) System.out.println("BAD EFFECT " + t);
        double[][] ct = {CT_REL, CT_POWER, CT_TRADE, CT_BAL, CT_LX, CT_LY};
        for (int i = 0; i < ct.length; i++) if (ct[i].length != NC) System.out.println("COUNTRY TABLE " + i + " has " + ct[i].length);
        if (CT_NAME.length != NC || CT_REGION.length != NC || LEADER25.length != NC || LEADER29.length != NC || CT_ALIGN.length != NC || CT_NUKE.length != NC || CT_FLAG.length != NC) System.out.println("COUNTRY TABLES DON'T MATCH");
        int nc = CR_ID.length;
        if (CR_COND.length != nc || CR_COOL.length != nc || CR_TITLE.length != nc || CR_BODY.length != nc || CR_OPTS.length != nc) System.out.println("CRISIS TABLES DON'T MATCH");
        for (int i = 0; i < nc; i++) for (String o : CR_OPTS[i].split(";;")) { String[] t = o.split("\\|"); if (t.length != 8) System.out.println("CRISIS " + CR_ID[i] + " option has " + t.length + " parts: " + o.substring(0, Math.min(40, o.length()))); else { checkFx(t[4]); checkFx(t[5]); Double.parseDouble(t[3]); } }
        if (OP_DESC.length != OP_NAME.length || OP_COND.length != OP_NAME.length || OP_FX.length != OP_NAME.length) System.out.println("OP TABLES DON'T MATCH");
        for (String o : OP_FX) { String[] t = o.split("\\|"); if (t.length != 3) System.out.println("BAD OP " + o); else { checkFx(t[1]); checkFx(t[2]); } }
        if (CV_DESC.length != CV_NAME.length || CV_ODDS.length != CV_NAME.length || CV_EXPOSE.length != CV_NAME.length || CV_FX.length != CV_NAME.length || CV_EXPFX.length != CV_NAME.length) System.out.println("COVERT TABLES DON'T MATCH");
        for (String f : CV_FX) checkFx(f); for (String f : CV_EXPFX) checkFx(f);
        for (String ch : CHANGES25) if (ctIdx(ch.split("\\|")[0]) < 0) System.out.println("BAD CHANGE " + ch);
        for (String f : CT_FLAG) for (String part : f.split("\\|")) for (String hx : part.substring(2).split(",")) if (hx.length() == 6) Integer.parseInt(hx, 16);
        int ne = EV_ID.length;
        if (EV_COND.length != ne || EV_COOL.length != ne || EV_TITLE.length != ne || EV_BODY.length != ne || EV_OPTS.length != ne) System.out.println("EVENT TABLES DON'T MATCH " + ne + " " + EV_COND.length + " " + EV_COOL.length + " " + EV_TITLE.length + " " + EV_BODY.length + " " + EV_OPTS.length);
        for (int i = 0; i < Math.min(ne, EV_OPTS.length); i++) for (String o : EV_OPTS[i].split(";;")) { String[] t = o.split("\\|"); if (t.length != 8) System.out.println("EVENT " + EV_ID[i] + " option has " + t.length + " parts"); else { checkFx(t[4]); checkFx(t[5]); Double.parseDouble(t[3]); } }
        if (OUT_LEAN.length != OUT_NAME.length || OUT_AUD.length != OUT_NAME.length || OUT_SIT.length != OUT_NAME.length) System.out.println("OUTLET TABLES DON'T MATCH");
        for (String t : DIS_STATES) for (String s : t.split(" ")) if (stateIdx(s) < 0) System.out.println("BAD DISASTER STATE " + s);
        if (EV_MIN.length != ne) System.out.println("EV_MIN has " + EV_MIN.length);
        if (AD_MIN.length != n) System.out.println("AD_MIN has " + AD_MIN.length);
        // history
        for (int k = 0; k < EV_FROM.length; k++) {
            int[] ev = evFor(EV_FROM[k]);
            int want = EV_FROM[k] < 1960 ? 531 : EV_FROM[k] == 1960 ? 537 : 538;
            if (evTotal(ev) != want) System.out.println("EV " + EV_FROM[k] + " totals " + evTotal(ev) + ", expected " + want);
        }
        for (String t : LEAN_TABLE) { int c = 0; for (String tok : t.split(" ")) { if (stateIdx(tok.split(":")[0]) < 0) System.out.println("BAD LEAN STATE " + tok); c++; } if (c != 51) System.out.println("LEAN TABLE has " + c); }
        for (String r : ERA_ROSTER) { String[] t = r.split("\\|", -1); for (int p = 1; p <= 2; p++) for (String x : t[p].split(" ")) for (String id : x.split(">")) if (!id.isEmpty() && candById(id) == null) System.out.println("BAD ROSTER ID " + id + " in " + t[0]);
            if (era(Integer.parseInt(t[0])).year != Integer.parseInt(t[0])) System.out.println("NO ERA FOR ROSTER " + t[0]);
            Cand w = candById(era(Integer.parseInt(t[0])).winnerId); if (w == null) System.out.println("BAD WINNER " + t[0]); }
        for (String s : NOMINEES) for (String x : s.substring(5).split(",")) for (String id : x.split(">")) if (!id.isEmpty() && candById(id) == null) System.out.println("BAD NOMINEE " + id);
        for (String s : WORLD_ERA) for (String tok : s.substring(5).split("\\^")) { int eq = tok.indexOf('='); if (ctIdx(tok.substring(0, eq)) < 0) System.out.println("BAD WORLD COUNTRY " + tok); String v = tok.substring(eq + 1); if (!v.equals("-")) { String[] p = v.split(";", -1); if (p.length != 6) System.out.println("BAD WORLD ENTRY " + s.substring(0, 4) + " " + tok); else { if (!p[3].isEmpty()) Double.parseDouble(p[3]); if (!p[4].isEmpty()) Integer.parseInt(p[4]); if (!p[5].isEmpty()) Double.parseDouble(p[5]); } } }
        for (String f : FLASH) {
            String[] t = f.split("\\|", 7);
            if (t.length < 7) { System.out.println("BAD FLASH " + f.substring(0, 40)); continue; }
            History.dayOf(f);
            Integer.parseInt(t[1]);
            checkFx(t[5]);
            if (t[6].isEmpty()) continue;
            for (String o : t[6].split(";;")) { String[] q = o.split("\\|"); if (q.length != 8) System.out.println("FLASH " + t[3] + " option has " + q.length + " parts: " + o.substring(0, Math.min(50, o.length()))); else { checkFx(q[4]); checkFx(q[5]); Double.parseDouble(q[3]); } }
        }
        for (String s : HBILL_SPEC) if (s.split("\\|").length != 17) System.out.println("BAD BILL " + s.substring(0, 30));
        for (String j : JUSTICES) if (j.split("\\|").length != 6) System.out.println("BAD JUSTICE " + j);
        for (Era e : ERAS) { int nj = courtIn(e).size(); if (nj < 8 || nj > 9) System.out.println("COURT " + e.year + " has " + nj); }
        for (String tok : ENACTED.split(" ")) {
            String id = tok.substring(0, tok.indexOf(':'));
            boolean found = false;
            for (BillDef d : BILLS) if (d.id.equals(id)) { found = true; if (enacted(id) < d.from || enacted(id) > d.to) System.out.println("BILL " + id + " ENACTED OUTSIDE ITS YEARS"); }
            if (!found) System.out.println("ENACTED: NO BILL " + id);
        }
        System.out.println("history: " + ERAS.size() + " eras, " + HIST.size() + " historical figures, " + FLASH.length + " turning points, " + HBILL_SPEC.length + " historical bills");
        System.out.println("check done: " + n + " actions, " + nc + " crises, " + ne + " events");
    }

    static void whip(int year, String pres, int scen) {
        Game g = newGame(7, year, pres, scen);
        StringBuilder s = new StringBuilder("=== whip " + g.pres.name() + " House D " + g.houseD + " Senate D " + g.senateD + " approval " + fmt1(g.approval) + "\n");
        for (BillDef d : HillUI.agenda(g)) {
            Bill b = Legis.draft(g, d);
            b.recon = d.budget && Legis.reconOk(g, b);
            s.append(String.format("%-34s econ %5.2f cult %5.2f sup %3.0f cost %5.0f %s  H %.0f (%.2f)  S %.0f (%.2f)  chair %.2f/%.2f%n", d.name, b.econ(), b.cult(), b.support(), b.netCost(), b.recon ? "R" : " ",
                    Legis.count(g, b, HOUSE, false)[0], Legis.chance(g, b, HOUSE, false), Legis.count(g, b, SENATE, false)[0], Legis.chance(g, b, SENATE, false), Legis.chairOk(g, b, HOUSE), Legis.chairOk(g, b, SENATE)));
            StringBuilder bl = new StringBuilder("      ");
            for (int i = 0; i < g.cong.blocs.size(); i++) bl.append(String.format("%.2f ", logistic(Legis.blocLogit(g, b, i, false))));
            s.append(bl).append("\n");
        }
        System.out.print(s);
    }

    static void shot(ResoluteDesk u, String dir, String name) {
        BufferedImage img = u.snapshot(1600, 900);
        try { javax.imageio.ImageIO.write(img, "png", new File(dir, name + ".png")); } catch (IOException e) { e.printStackTrace(); }
        System.out.println("shot " + name);
    }

    static void ui(String dir) {
        new File(dir).mkdirs();
        ResoluteDesk u = new ResoluteDesk();
        u.go(SC_HOME); shot(u, dir, "home");
        u.setup = new Settings();
        Screens.showEra(u); u.go(SC_SETUP); shot(u, dir, "setup");
        u.setup.year = 2025;
        u.go(SC_PICK); Screens.pickParty = REP; Screens.sel = 0; shot(u, dir, "pick");
        Creator.begin(u, DEM); shot(u, dir, "create");
        u.setup.presId = "trump"; u.setup.vpId = "vance"; u.go(SC_VP); shot(u, dir, "vp");
        u.game = newGame(5, 2025, "trump", 0);
        u.go(SC_OVAL); shot(u, dir, "oval");
        for (int w = 0; w < 30; w++) { if (w % 8 == 3) Rooms.roadWeek(u.game); else Week.routine(u.game); if (u.game.stamina < 40) Rooms.restWeek(u.game); if (w % 6 == 1) Decisions.autoAgenda(u.game); Week.resolve(u.game); Decisions.autoAll(u.game); }
        u.go(SC_PAPER); shot(u, dir, "paper");
        u.go(SC_OVAL); shot(u, dir, "oval_later");
        for (int r = 0; r < NROOM; r++) { Rooms.open(u, r); if (r == R_DESK) Rooms.roadWeek(u.game); shot(u, dir, "room_" + r); }
        Rooms.chartSel = 2; Rooms.open(u, R_ECON); shot(u, dir, "room_econ_stocks");
        Rooms.mapMode = 1; Rooms.selState = stateIdx("MI"); Rooms.open(u, R_COUNTRY); shot(u, dir, "room_country_unemp");
        Rooms.open(u, R_CONGRESS);
        for (int t = 0; t < HillUI.TABS.length; t++) { HillUI.tab = t; HillUI.sel = -1; shot(u, dir, "hill_" + t); }
        HillUI.tab = 1; HillUI.draftSel = 1; HillUI.draft = Legis.draft(u.game, HillUI.agenda(u.game).get(1)); shot(u, dir, "hill_draft");
        HillUI.tab = 0; HillUI.chairCh = 1; shot(u, dir, "hill_senate_chairs");
        for (Bill b : u.game.cong.bills) if (b.stage == ST_LAW) { Legis.toDesk(u.game, b); break; }
        Decisions.show(u, u.game); shot(u, dir, "decision_sign");
        u.modal = null; u.game.inbox.clear();
        u.game.cong.approps[0] = 1;
        Budget.deadline(u.game, true); Decisions.show(u, u.game); shot(u, dir, "decision_cr");
        u.modal = null; u.game.inbox.clear();
        // the West Wing
        Game g = u.game;
        Actions.issue(g, EM_BORDER); Actions.issue(g, actionIdx("eo_troops")); Actions.issue(g, actionIdx("rule_hospital")); Actions.issue(g, actionIdx("eo_birthright"));
        Courts.sue(g, g.ex.actions.get(g.ex.actions.size() - 1));
        u.game.inbox.clear();
        Rooms.open(u, R_WESTWING);
        for (int t = 0; t < WestWing.TABS.length; t++) { WestWing.tab = t; shot(u, dir, "ww_" + t); }
        WestWing.tab = 0; WestWing.selStaff = S_PRESS; shot(u, dir, "ww_staff_pick"); WestWing.selStaff = -1;
        g.ex.post[P_LAB] = null; WestWing.tab = 1; WestWing.selPost = P_LAB; shot(u, dir, "ww_cabinet_vacant");
        WestWing.selPost = P_DEF; shot(u, dir, "ww_cabinet_def");
        WestWing.tab = 3; WestWing.lib = 0; WestWing.selAct = actionIdx("eo_drill"); shot(u, dir, "ww_orders_pick");
        WestWing.lib = 3; shot(u, dir, "ww_custom");
        Justice gone = g.ex.court.get(1); g.ex.court.remove(gone); WestWing.tab = 4; shot(u, dir, "ww_courts_vacancy");
        Person fict = Cabinet.options(g, P_HUD, 99).get(0);
        g.ex.post[P_HUD] = fict; fict.since = g.week;
        Trouble.add(g, SK_ETHICS, "Housing secretary " + fict.name + " under fire", "Reporters say " + fict.name + " billed taxpayers for first-class travel and a $40,000 office renovation.", 4, fict, false);
        Decisions.show(u, g); shot(u, dir, "decision_scandal");
        u.modal = null; g.inbox.clear();
        // every kind of calendar slot draws
        for (int i = 0; i < NSLOT; i++) { g.act[i] = i % NAC; g.arg[i] = AC_ISSUE[i % NAC] ? 0 : AC_STATE[i % NAC] ? 5 : -1; }
        Rooms.open(u, R_DESK); shot(u, dir, "desk_all_kinds");
        // the Situation Room
        Week.routine(g);
        g.world.twStage = 2; g.world.redsea = 70;
        Rooms.open(u, R_SITROOM);
        for (int t = 0; t < SitRoom.TABS.length; t++) { SitRoom.tab = t; SitRoom.sel = t == 0 ? CC_CN : SitRoom.sel; shot(u, dir, "sit_" + t); }
        SitRoom.tab = 0; SitRoom.sel = CC_DK; shot(u, dir, "sit_denmark");
        g.world.cool[0] = 0; g.world.twt = 70; Diplo.crises(g); Decisions.show(u, g); shot(u, dir, "decision_crisis");
        u.modal = null; g.inbox.clear();
        g.world.summitCountry = CC_CN; g.world.summitWeek = g.week; Summit.start(g); Decisions.show(u, g); shot(u, dir, "decision_summit");
        u.modal = null; g.inbox.clear(); g.world.summitCountry = -1;
        SitRoom.tab = 1; HomeFront.brief(g); shot(u, dir, "sit_brief");
        // the Press Briefing Room
        g.home.lastPoll = null; Polls.commission(g, PI_PRICES);
        Rooms.open(u, R_PRESS);
        for (int t = 0; t < PressRoom.TABS.length; t++) { PressRoom.tab = t; shot(u, dir, "press_" + t); }
        Speech.start(g, 1); Decisions.show(u, g); shot(u, dir, "decision_sotu");
        u.modal = null; Decision sd = g.inbox.isEmpty() ? null : null; g.inbox.clear(); Speech.step(g, 0); Decisions.show(u, g); shot(u, dir, "decision_speechline");
        u.modal = null; g.inbox.clear(); g.home.spType = -1;
        QA.start(g, 0, 0); Decisions.show(u, g); shot(u, dir, "decision_presser");
        u.modal = null; g.inbox.clear(); g.home.qaType = -1;
        Arrays.fill(g.home.cool, 999); for (int i = 0; i < 400 && g.inbox.isEmpty(); i++) { g.home.cool[0] = 0; HomeFront.events(g); }
        Decisions.show(u, g); shot(u, dir, "decision_event");
        u.modal = null; g.inbox.clear();
        history(u, dir);
    }

    /** The final update: history, the campaign, the verdict. */
    static void history(ResoluteDesk u, String dir) {
        u.setup = new Settings(); u.setup.year = 1961;
        Screens.showEra(u); u.go(SC_SETUP); shot(u, dir, "h_setup_1961");
        u.go(SC_PICK); Screens.pickParty = DEM; Screens.sel = 0; shot(u, dir, "h_pick_1961");
        u.game = newGame(7, 1941, null, 0);
        Game g = u.game;
        g.tut = 2; u.go(SC_OVAL); shot(u, dir, "h_tutorial"); g.tut = -1;
        for (int w = 0; w < 60; w++) { Auto.week(g); Week.resolve(g); Decisions.autoAll(g); }
        Rooms.open(u, R_SITROOM);
        SitRoom.tab = 0; SitRoom.sel = CC_DE; shot(u, dir, "h_sit_world_1942");
        SitRoom.tab = 2; shot(u, dir, "h_sit_hot_1942");
        SitRoom.tab = 6; SitRoom.selWar = 0; shot(u, dir, "h_sit_wars_1942");
        SitRoom.tab = 1; HomeFront.brief(g); shot(u, dir, "h_sit_brief_1942");
        Rooms.open(u, R_CONGRESS); HillUI.tab = 0; shot(u, dir, "h_hill_1942");
        Rooms.open(u, R_PRESS); PressRoom.tab = 0; shot(u, dir, "h_press_1942");
        Rooms.open(u, R_WESTWING); WestWing.tab = 1; WestWing.selPost = P_DEF; shot(u, dir, "h_ww_cabinet_1942");
        // a turning point
        g = newGame(8, 1961, null, 0);
        u.game = g;
        g.inbox.clear();
        for (int w = 0; w < 95 && g.inbox.isEmpty(); w++) { Auto.week(g); Week.resolve(g); if (!g.inbox.isEmpty() && g.inbox.get(0).kind != D_HIST) g.inbox.clear(); }
        u.go(SC_OVAL); Decisions.show(u, g); shot(u, dir, "h_decision_flash");
        u.modal = null; Decisions.autoAll(g);
        // the campaign and election night
        for (int w = 0; w < 400 && !(g.re != null && g.re.opp != null && Reelection.weeksTo(g) <= 6); w++) { Auto.week(g); Week.resolve(g); Decisions.autoAll(g); }
        u.go(SC_OVAL); shot(u, dir, "h_oval_campaign");
        Rooms.open(u, R_PRESS); PressRoom.tab = 5; shot(u, dir, "h_campaign");
        for (int w = 0; w < 20 && !g.night; w++) { Auto.week(g); Week.resolve(g); Decisions.autoAll(g); }
        Night.begin(u); Night.all = true; shot(u, dir, "h_night");
        // the verdict
        for (int w = 0; w < 500 && !g.over; w++) { Auto.week(g); Week.resolve(g); Decisions.autoAll(g); }
        Legacy.lastScores = null; Legacy.epi = null;
        Legacy.record(g); Legacy.epilogue(g);
        u.go(SC_LEGACY);
        for (int t = 0; t < 5; t++) { Legacy.tab = t; shot(u, dir, "h_legacy_" + t); }
        u.go(SC_HALL); shot(u, dir, "h_hall");
        u.go(SC_IMPORT); shot(u, dir, "h_import");
    }
}
