package resolutedesk.executive;

import static resolutedesk.core.Dates.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.data.StateData.*;
import static resolutedesk.executive.ActionData.*;
import static resolutedesk.executive.Posts.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.model.NewsKinds.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.opinion.Issues.*;

import java.util.ArrayList;
import resolutedesk.congress.Bloc;
import resolutedesk.home.HomeFront;
import resolutedesk.model.Econ;
import resolutedesk.model.Game;

/**
 * Orders take effect at once and can be undone as easily. Rules take months of comment but last.
 * Emergencies unlock powers Congress can try to take back. Anything aggressive ends up in court.
 */
public final class Actions {
    public static boolean live(Game g, int def) { for (Action a : g.ex.actions) if (a.def == def && (a.status == A_LIVE || a.status == A_COMMENT || a.status == A_BLOCKED)) return true; return false; }

    public static boolean available(Game g, int def) {
        if (live(g, def)) return false;
        if (yearNow(g) < AD_MIN[def]) return false;
        int need = AD_NEEDS[def];
        if (need >= 0 && !g.ex.emergency[need]) return false;
        if (AD_KIND[def] == K_EMERG && g.ex.emergency[def]) return false;
        // the opposite of something in force
        if (AD_ID[def].equals("eo_paris_in") && live(g, actionIdx("eo_paris_out")) || AD_ID[def].equals("eo_paris_out") && live(g, actionIdx("eo_paris_in"))) return false;
        return true;
    }

    /** Legal risk after your counsel's work. */
    public static double legal(Game g, double base) { return clamp(base * (1.15 - 0.3 * Cabinet.sq(g, S_COUNSEL)), 0, 0.98); }

    public static Action issue(Game g, int def) {
        Action a = new Action();
        a.def = def; a.kind = AD_KIND[def]; a.name = AD_NAME[def]; a.area = AD_AREA[def]; a.lean = AD_LEAN[def];
        a.support = AD_SUP[def]; a.legal = AD_LEGAL[def]; a.issued = g.week; a.statusWeek = g.week;
        g.ex.actions.add(a);
        if (a.kind == K_RULE) {
            a.status = A_COMMENT;
            a.finalWeek = g.week + (int) Math.round(14 - 6 * Cabinet.pq(g, P_OMB));
            g.log(NK_INFO, "The administration proposes a rule: " + a.name.toLowerCase(), "Public comments are open until " + dateStr(a.finalWeek * 7) + ".", 1.0);
            react(g, a, 0.4);
            return a;
        }
        if (a.kind == K_EMERG) {
            g.ex.emergency[def] = true;
            g.log(NK_BIG, ("President " + g.pres.last + " declares a national emergency over " + EMERG_NAME[def]).toUpperCase(), AD_DESC[def], 2.8);
            react(g, a, 1);
            a.status = A_LIVE;
            Congressional.emergencyVote(g, a);
            return a;
        }
        a.status = A_LIVE;
        g.log(NK_INFO, "President " + g.pres.last + " signs an executive order: " + a.name.toLowerCase(), AD_DESC[def], 1.4);
        react(g, a, 1);
        takeEffect(g, a);
        return a;
    }

    /** A custom order: issue, direction, how far it reaches and on what legal footing. */
    public static Action custom(Game g, int area, int dir, double scope, int basis) {
        Action a = new Action();
        a.def = -1; a.kind = K_ORDER; a.custom = true; a.area = area; a.lean = dir; a.scope = scope;
        a.name = (scope > 1.1 ? "A sweeping order on " : scope < 0.7 ? "A narrow order on " : "An order on ") + PISSUE[area].toLowerCase();
        double fit = dir == g.party ? 1 : -1;
        a.support = clamp(50 + (g.issueApp[area] - 45) * 0.2 - 7 * (scope - 0.6) + g.rng.nextGaussian() * 3 - (fit < 0 ? 2 : 0), 25, 75);
        a.legal = clamp(0.08 + 0.3 * scope + (basis == 1 ? 0.3 : basis == 2 ? 0.12 : 0), 0, 0.95);
        a.issued = g.week; a.statusWeek = g.week; a.status = A_LIVE;
        g.ex.actions.add(a);
        g.log(NK_INFO, "President " + g.pres.last + " signs " + a.name.toLowerCase(), basis == 2 ? "The order relies on emergency powers." : basis == 1 ? "Lawyers call its legal basis a stretch." : null, 1.2);
        react(g, a, 1);
        takeEffect(g, a);
        return a;
    }

    /** How the country and the Hill react. */
    static void react(Game g, Action a, double k) {
        if (k >= 1) HomeFront.act(g, a.lean == DEM ? -0.5 : a.lean == REP ? 0.5 : 0, a.lean == DEM ? -0.45 : a.lean == REP ? 0.5 : 0, 0.5);
        boolean mine = a.lean == g.party || a.lean < 0;
        double sup = a.support - 50;
        g.perf += k * sup * 0.012 * (mine ? 1 : 0.6) * Math.min(1.3, a.scope);
        g.issueAdj[a.area] = clamp(g.issueAdj[a.area] + k * sup * 0.006 * a.scope, -1.5, 1.5);
        g.issueSal[a.area] = Math.min(2, g.issueSal[a.area] + 0.08 * k);
        for (Bloc b : g.cong.blocs) {
            if (b.party == g.party) b.rel = clamp(b.rel + (mine ? 0.6 : -2) * k, 0, 100);
            else if (a.lean == g.party) b.rel = clamp(b.rel - 1.0 * k, 0, 100);
        }
    }

    /** Turns on its effects, and the lawsuits start. */
    static void takeEffect(Game g, Action a) {
        apply(g, a, true);
        Implement.roll(g, a.name, a.area, 0.7);
        double sue = a.legal * 1.3 + (a.support < 50 ? 0.1 : 0);
        if (a.legal > 0.04 && g.rng.nextDouble() < sue) Courts.sue(g, a);
    }

    public static void apply(Game g, Action a, boolean on) {
        if (on == a.applied) return;
        a.applied = on;
        Econ e = g.econ;
        double s = on ? a.scope : -a.scope;
        if (on) {
            a.aGdp = a.aUnemp = a.aInfl = a.aTariff = a.aOil = a.aDef = 0;
            String fx = a.def >= 0 ? AD_FX[a.def] : "";
            if (a.custom) fx = (a.lean == REP ? "gdp=0.01" : "def=0.01") + " g" + (a.area == PI_IMMIG ? 12 : a.area == PI_JOBS ? 24 : a.area == PI_EDU ? 28 : 4) + "=" + (a.lean == DEM ? 1 : -1);
            for (String t : fx.split(" ")) {
                if (t.isEmpty()) continue;
                String[] kv = t.split("=");
                String k = kv[0];
                double v = Double.parseDouble(kv[1]);
                if (k.equals("gdp")) a.aGdp = v; else if (k.equals("unemp")) a.aUnemp = v; else if (k.equals("infl")) a.aInfl = v;
                else if (k.equals("tariff")) a.aTariff = v; else if (k.equals("oil")) a.aOil = v; else if (k.equals("def")) a.aDef = v;
                else if (k.equals("wf")) { for (int i = 0; i < NPOST; i++) g.ex.wf[i] *= 1 + v * a.scope / 100; }
                else if (k.equals("ready")) { for (int i = 0; i < NPOST; i++) g.ex.ready[i] = clamp(g.ex.ready[i] + v * a.scope, 5, 100); }
                else if (k.startsWith("st:")) { int st = stateIdx(k.substring(3)); if (st >= 0) g.stateBump[st] += v * a.scope; }
                else if (k.startsWith("g") && k.length() > 1 && Character.isDigit(k.charAt(1))) { groupBump(g, Integer.parseInt(k.substring(1)), v * a.scope); }
            }
        }
        e.polGdp += a.aGdp * s; e.polUnemp += a.aUnemp * s; e.polInfl += a.aInfl * s;
        e.tariff += a.aTariff * s; e.oilBase += a.aOil * s; e.fiscal += a.aDef * s;
        if (!on && a.def >= 0) { String fx = AD_FX[a.def]; for (String t : fx.split(" ")) if (t.startsWith("g") && !t.startsWith("gdp")) { String[] kv = t.split("="); groupBump(g, Integer.parseInt(kv[0].substring(1)), -Double.parseDouble(kv[1]) * a.scope * 0.7); } }
    }

    public static void revoke(Game g, Action a) {
        apply(g, a, false);
        a.status = A_REVOKED; a.statusWeek = g.week;
        if (a.kind == K_EMERG && a.def >= 0) {
            g.ex.emergency[a.def] = false;
            for (Action o : g.ex.actions) if (o.def >= 0 && AD_NEEDS[o.def] == a.def && o.status == A_LIVE) { apply(g, o, false); o.status = A_REVOKED; o.statusWeek = g.week; }
        }
        g.log(NK_INFO, "The White House rescinds " + a.name.toLowerCase(), null, 0.9);
        g.perf -= 0.15;
    }

    public static void week(Game g) {
        for (Action a : new ArrayList<Action>(g.ex.actions)) {
            if (a.status == A_COMMENT && g.week >= a.finalWeek) {
                a.status = A_LIVE; a.statusWeek = g.week;
                g.log(NK_GOOD, "A new rule takes effect: " + a.name.toLowerCase(), null, 1.2);
                react(g, a, 0.6);
                takeEffect(g, a);
                Congressional.cra(g, a);
            }
        }
    }
}
