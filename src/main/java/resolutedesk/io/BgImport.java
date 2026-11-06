package resolutedesk.io;

import static resolutedesk.core.Num.*;
import static resolutedesk.data.Candidates.*;
import static resolutedesk.data.Eras.*;
import static resolutedesk.data.StateData.*;
import static resolutedesk.model.Attributes.*;
import static resolutedesk.model.Factions.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.sim.Rosters.*;
import static resolutedesk.ui.Draw.*;
import static resolutedesk.ui.Fonts.*;
import static resolutedesk.ui.ScreenIds.*;
import static resolutedesk.ui.Theme.*;

import java.awt.Graphics2D;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import resolutedesk.ResoluteDesk;
import resolutedesk.model.Cand;
import resolutedesk.model.Look;
import resolutedesk.model.Settings;
import resolutedesk.ui.Creator;
import resolutedesk.ui.Portrait;
import resolutedesk.ui.Screens;

/** Finds Battleground 2028 saves and turns their winner into a president for a new game. */
public final class BgImport {
    static List<BgPres> found = new ArrayList<BgPres>();
    public static boolean scanned, busy;
    public static int sel;
    static String note;

    /** Looks for Battleground 2028 saves; calls done when finished (at once on the desktop). */
    static void scan(Runnable done) {
        found.clear();
        note = null;
        String[] slots = {"autosave", "save1"};
        File dir = new File(System.getProperty("user.home"), ".battleground2028");
        for (String s : slots) {
            File f = new File(dir, s + ".sav");
            if (!f.exists()) continue;
            try {
                InputStream in = new java.util.zip.GZIPInputStream(new BufferedInputStream(new FileInputStream(f)));
                Object root = new JSer(in).top();
                in.close();
                BgPres p = fromGame(root, s);
                if (p != null) found.add(p);
            } catch (Throwable t) { note = "A Battleground 2028 save couldn't be read (" + t.getClass().getSimpleName() + ")."; }
        }
        scanned = true;
        if (done != null) done.run();
    }

    static Object get(Object o, String k) { return o instanceof JObj ? ((JObj) o).f.get(k) : null; }

    static int gi(Object o, String k, int def) { Object v = get(o, k); return v instanceof Integer ? (Integer) v : v instanceof Double ? (int) Math.round((Double) v) : def; }

    static double gd(Object o, String k, double def) { Object v = get(o, k); return v instanceof Double ? (Double) v : v instanceof Integer ? (Integer) v : def; }

    static boolean gb(Object o, String k) { Object v = get(o, k); return v instanceof Boolean && (Boolean) v; }

    static String gs(Object o, String k) { Object v = get(o, k); return v instanceof String ? (String) v : null; }

    static List<Object> items(Object v) {
        List<Object> l = new ArrayList<Object>();
        if (v instanceof JObj) l.addAll(((JObj) v).extra);
        else if (v instanceof Object[]) for (Object x : (Object[]) v) l.add(x);
        return l;
    }

    /** Turns a Battleground game into a president-elect. */
    static BgPres fromGame(Object g, String slot) {
        List<Object> camps = items(get(g, "camps"));
        if (camps.isEmpty()) return null;
        int player = gi(g, "player", -1);
        Object result = get(g, "result");
        int evD = gi(g, "evD", 0), evR = gi(g, "evR", 0), evI = gi(g, "evI", 0);
        int winParty = -1;
        if (result != null) winParty = evD >= 270 ? DEM : evR >= 270 ? REP : evI >= 270 ? IND : gi(g, "contingent", -1);
        Object camp = null;
        boolean mine;
        if (winParty == DEM || winParty == REP) {
            int idx = gi(g, winParty == DEM ? "dNom" : "rNom", -1);
            for (Object c : camps) if (gi(c, "idx", -2) == idx) camp = c;
            if (camp == null) for (Object c : camps) if (gi(c, "party", -1) == winParty && gb(c, "nominee")) camp = c;
        } else if (winParty < 0 && player >= 0 && player < camps.size()) camp = camps.get(player);
        BgPres p = new BgPres();
        p.slot = slot;
        if (winParty == IND || winParty >= 2) { p.independent = true; return p; }
        if (camp == null) return null;
        mine = gi(camp, "idx", -1) == player;
        Cand c = cand(get(camp, "c"));
        if (c == null) return null;
        int party = gi(camp, "party", c.party);
        if (party != DEM && party != REP) { p.independent = true; return p; }
        c.party = party;
        p.pres = c;
        p.won = result != null;
        p.mine = mine;
        Cand vp = cand(get(g, party == DEM ? "vpD" : "vpR"));
        if (vp != null) { Cand known = candById(vp.id); p.vp = known != null && known != c ? known : vp; }
        int hs = gi(g, "houseSeatsD", -1);
        if (hs > 0) p.house = party == DEM ? hs : 435 - hs;
        Object set = get(g, "set");
        int mid = gi(set, "midSenateD", -1);
        if (mid > 0) {
            int up = 0, won = 0;
            for (Object s : items(get(g, "senate"))) { if (gi(s, "party", -1) == DEM) up++; if (gd(s, "margin", 0) > 0) won++; }
            int sd = mid - up + won;
            if (result != null) p.senate = party == DEM ? sd : 100 - sd;
        }
        if (result == null) { p.house = -1; p.senate = -1; }
        p.unemp = gd(g, "unemp", Double.NaN); p.infl = gd(g, "inflation", Double.NaN); p.gdp = gd(g, "gdp", Double.NaN);
        int ev = party == DEM ? evD : evR, oev = party == DEM ? evR : evD;
        p.desc = c.name() + " (" + PARTY_SHORT[party] + ")" + (p.won ? ", won " + ev + "-" + oev + " in 2028" : ", race not finished: takes office as if elected") + (mine ? "  (your campaign)" : "");
        return p;
    }

    static Cand cand(Object o) {
        if (!(o instanceof JObj)) return null;
        Cand c = new Cand();
        c.id = gs(o, "id") != null ? gs(o, "id") : "bg";
        c.first = gs(o, "first") != null ? gs(o, "first") : "";
        c.last = gs(o, "last") != null ? gs(o, "last") : "";
        c.party = gi(o, "party", DEM);
        c.role = gs(o, "role") != null ? gs(o, "role") : "President-elect";
        c.homeAb = gs(o, "homeAb");
        c.home = c.homeAb != null ? stateIdx(c.homeAb) : -1;
        c.born = gi(o, "born", 1965);
        c.bio = gs(o, "bio"); c.pros = gs(o, "pros"); c.cons = gs(o, "cons");
        c.faction = clampi(gi(o, "faction", c.party == DEM ? F_LIB : F_GOV), 0, NF - 1);
        c.econ = gd(o, "econ", FACTIONS[c.faction].econ); c.cult = gd(o, "cult", FACTIONS[c.faction].cult);
        c.plat = FACTIONS[c.faction].pos.clone();
        Object a = get(o, "a");
        if (a instanceof int[]) { int[] v = (int[]) a; for (int i = 0; i < NA && i < v.length; i++) c.a[i] = v[i]; }
        else if (a instanceof Object[]) { Object[] v = (Object[]) a; for (int i = 0; i < NA && i < v.length; i++) c.a[i] = v[i] instanceof Integer ? (Integer) v[i] : 50; }
        Object lk = get(o, "look");
        Look l = new Look();
        if (lk instanceof JObj) {
            l.female = gb(lk, "female"); l.skin = gi(lk, "skin", 1); l.hair = gi(lk, "hair", 1); l.style = gi(lk, "style", 1); l.gray = gd(lk, "gray", 0.2);
            l.facial = gi(lk, "facial", 0); l.glasses = gi(lk, "glasses", 0); l.faceW = gd(lk, "faceW", 1); l.jaw = gd(lk, "jaw", 0.5); l.suit = gi(lk, "suit", 0); l.tie = gi(lk, "tie", 1);
            l.age = gd(lk, "age", 0.5); l.smile = gd(lk, "smile", 0.5); l.earrings = gb(lk, "earrings"); l.lipstick = gb(lk, "lipstick");
        } else l = Creator.randomLook(new Random(c.last.hashCode()));
        c.look = l;
        c.custom = true;
        c.fictional = gb(o, "fictional");
        return c;
    }

    /** Sets up a 2029 presidency for the imported winner. */
    static void take(ResoluteDesk u, BgPres p) {
        Settings s = new Settings();
        s.year = 2029;
        s.custom = p.pres;
        s.presId = null;
        s.vpId = p.vp != null && candById(p.vp.id) == p.vp ? p.vp.id : defaultVp(era(2029), p.pres);
        if (p.house > 0) s.cHouse = p.house;
        if (p.senate > 0) s.cSenate = p.senate;
        if (!Double.isNaN(p.unemp) && p.unemp > 0) s.cUnemp = Math.round(p.unemp * 10) / 10.0;
        if (!Double.isNaN(p.infl)) s.cInfl = Math.round(p.infl * 10) / 10.0;
        if (!Double.isNaN(p.gdp)) s.cGdp = Math.round(p.gdp * 10) / 10.0;
        s.bgLine = p.desc;
        u.setup = s;
        Screens.vpSel = -1;
        u.go(SC_VP);
    }

    public static void draw(ResoluteDesk u, Graphics2D g) {
        Screens.header(u, g, "From Battleground 2028", "Bring in the winner of a Battleground 2028 game and govern from January 20, 2029.");
        if (!scanned && !busy) { busy = true; scan(() -> busy = false); }
        u.panel(g, 48, 124, 760, 690, "Saved races found");
        double y = 168;
        if (busy) text(g, "Looking for Battleground 2028 saves...", 66, y + 10, F_S, C_DIM);
        else if (found.isEmpty()) wrap(g, "No Battleground 2028 games were found. " + (note != null ? note + " " : "")
                + "Play Battleground 2028 to election night (or anywhere in the race) and save; this screen reads its saves from the same computer or, in the browser, the same browser and site.", 66, y, 720, F_S, C_TEXT);
        for (int i = 0; i < found.size(); i++) {
            BgPres p = found.get(i);
            final int k = i;
            u.card(g, 62, y, 732, 110, sel == i, () -> sel = k, null);
            if (p.independent) { text(g, "An independent won this race; Resolute Desk's presidents are Democrats or Republicans.", 80, y + 50, F_S, C_AMBER); y += 120; continue; }
            Portrait.draw(g, p.pres, 76, y + 10, 76, 90, u.tsec, 0, true, true);
            text(g, p.pres.name(), 170, y + 34, F_H3, partyColor(p.pres.party));
            text(g, p.desc, 170, y + 56, F_XS, C_TEXT);
            text(g, "From the " + (p.slot.equals("autosave") ? "autosave" : "saved game"), 170, y + 76, F_XS, C_DIM);
            if (p.vp != null) text(g, "Running mate: " + p.vp.name(), 170, y + 94, F_XS, C_DIM);
            y += 120;
        }
        double px = 824, pw = DW - 48 - px;
        u.panel(g, px, 124, pw, 690, "What comes with them");
        BgPres p = sel >= 0 && sel < found.size() ? found.get(sel) : null;
        if (p != null && !p.independent) {
            Screens.candidatePanel(u, g, p.pres, px, pw);
        } else wrap(g, "The president-elect's face, ratings and record come across, with their running mate, the House and Senate the election produced and the economy they inherit. You can still change the running mate on the next screen.", px + 20, 170, pw - 40, F_S, C_DIM);
        u.button(g, 48, 832, 180, 44, "Back", B_GHOST, () -> { scanned = false; u.go(SC_HOME); });
        boolean ok = p != null && !p.independent;
        u.button(g, DW - 48 - 300, 832, 300, 44, "Take office", ok ? B_GOLD : B_OFF, ok ? () -> take(u, p) : null);
    }
}
