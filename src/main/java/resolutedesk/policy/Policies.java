package resolutedesk.policy;

import static resolutedesk.congress.CongressRules.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.executive.ActionData.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.model.NewsKinds.*;
import static resolutedesk.opinion.Issues.*;
import static resolutedesk.policy.Outcomes.*;
import static resolutedesk.policy.PolicyData.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import resolutedesk.congress.Bill;
import resolutedesk.congress.BillData;
import resolutedesk.congress.BillDef;
import resolutedesk.congress.Hill;
import resolutedesk.congress.Legis;
import resolutedesk.executive.Action;
import resolutedesk.executive.Actions;
import resolutedesk.home.HomeFront;
import resolutedesk.model.Econ;
import resolutedesk.model.Game;
import resolutedesk.opinion.Approval;

/**
 * Policies and what they do. Every policy has a level; a change made by law, order, rule or budget moves it, and
 * the change works through over the policy's lag into the economy and the measurable outcomes. Voters react
 * twice: when a change is made (the announcement), and as its results show up.
 */
public final class Policies {
    private static List<PolicyDef> defs;
    private static final Map<String, BillDef> BILL_DEFS = new HashMap<String, BillDef>();

    private Policies() {
    }

    // ---- the table ----

    public static List<PolicyDef> all() {
        if (defs == null) {
            List<PolicyDef> l = new ArrayList<PolicyDef>();
            for (String row : PolicyTable.ROWS) l.add(new PolicyDef(row));
            defs = l;
        }
        return defs;
    }

    public static int count() { return all().size(); }

    public static PolicyDef def(int k) { return all().get(k); }

    public static int index(String id) {
        List<PolicyDef> l = all();
        for (int k = 0; k < l.size(); k++) if (l.get(k).id.equals(id)) return k;
        return -1;
    }

    /** Whether a policy is on the table yet in this game's year. */
    public static boolean available(Game g, int k) { return yearNow(g) >= def(k).from; }

    /** The policies of an area that are on the table this year. */
    public static int[] inArea(Game g, int area) {
        int n = 0;
        for (int k = 0; k < count(); k++) if (def(k).area == area && available(g, k)) n++;
        int[] l = new int[n];
        n = 0;
        for (int k = 0; k < count(); k++) if (def(k).area == area && available(g, k)) l[n++] = k;
        return l;
    }

    // ---- the start of a game ----

    public static void setup(Game g) {
        PolicyState s = new PolicyState();
        g.pol = s;
        grow(s, count(), g.era.year);
        Econ e = g.econ;
        s.out0 = new double[NOUT];
        for (int o = O_UNINS; o < NOUT; o++) s.out0[o] = outHistorical(o, g.era.year);
        s.out0[O_UNEMP] = e.unemp; s.out0[O_INFL] = e.infl; s.out0[O_GROWTH] = e.gdp; s.out0[O_DEFICIT] = e.deficit; s.out0[O_GAS] = e.gas;
        s.out = s.out0.clone();
        s.unemp0 = e.unemp;
        if (g.home != null) { s.homeCrime0 = Math.max(1, g.home.crime); s.homeOd0 = Math.max(1, g.home.od); }
        s.result = new double[NPI];
        s.fy = Hill.fiscalYear(g);
        record(g);
    }

    /** Makes room for every policy in the table, at its level in the given year (older saves know fewer policies). */
    static void grow(PolicyState s, int n, int year) {
        int had = s.level == null ? 0 : s.level.length;
        if (had >= n) return;
        s.level = growI(s.level, n); s.base = growI(s.base, n); s.levelEcon = growI(s.levelEcon, n); s.changed = growI(s.changed, n); s.via = growI(s.via, n);
        s.eff = growD(s.eff, n); s.effEcon = growD(s.effEcon, n); s.strength = growD(s.strength, n);
        s.aGdp = growD(s.aGdp, n); s.aUnemp = growD(s.aUnemp, n); s.aInfl = growD(s.aInfl, n); s.aOil = growD(s.aOil, n); s.aFiscal = growD(s.aFiscal, n);
        for (int k = had; k < n; k++) {
            int l = def(k).levelIn(year);
            s.level[k] = l; s.base[k] = l; s.levelEcon[k] = l; s.eff[k] = l; s.effEcon[k] = l; s.strength[k] = 1; s.changed[k] = -999; s.via[k] = -1;
        }
    }

    static int[] growI(int[] a, int n) { int[] b = new int[n]; if (a != null) System.arraycopy(a, 0, b, 0, a.length); return b; }

    static double[] growD(double[] a, int n) { double[] b = new double[n]; if (a != null) System.arraycopy(a, 0, b, 0, a.length); return b; }

    /** For a save from before policies existed, or from a version with fewer of them. */
    public static void upgrade(Game g) {
        if (g.pol == null) setup(g);
        else grow(g.pol, count(), g.era.year);
    }

    // ---- the week ----

    public static void week(Game g) {
        PolicyState s = g.pol;
        if (s == null) return;
        Econ e = g.econ;
        double[] sum = new double[NOUT];
        for (int k = 0; k < count(); k++) {
            PolicyDef d = def(k);
            double rate = Math.min(1, 2.3 / Math.max(1, d.lag));
            s.eff[k] += (s.level[k] - s.eff[k]) * rate;
            s.effEcon[k] += (s.levelEcon[k] - s.effEcon[k]) * rate;
            // the economy: growth, jobs, prices, oil and the budget
            double dk = (s.effEcon[k] - s.base[k]) * s.strength[k];
            double gdp = dk * d.fx[O_GROWTH], un = dk * d.fx[O_UNEMP], inf = dk * d.fx[O_INFL], oil = dk * d.fx[O_GAS] * 42 * e.priceLevel;
            double fis = (s.effEcon[k] - s.base[k]) * s.strength[k] * d.cost * COST_SCALE / (e.nominalGdp * 10);
            e.polGdp += gdp - s.aGdp[k]; e.polUnemp += un - s.aUnemp[k]; e.polInfl += inf - s.aInfl[k]; e.oilBase += oil - s.aOil[k]; e.fiscal += fis - s.aFiscal[k];
            s.aGdp[k] = gdp; s.aUnemp[k] = un; s.aInfl[k] = inf; s.aOil[k] = oil; s.aFiscal[k] = fis;
            // everything else
            double dl = (s.eff[k] - s.base[k]) * s.strength[k];
            if (dl != 0) for (int o = O_UNINS; o < NOUT; o++) sum[o] += dl * d.fx[o];
        }
        outcomes(g, sum);
        results(g, sum);
        budget(g);
        if (g.week % 4 == 0) record(g);
    }

    static void outcomes(Game g, double[] sum) {
        PolicyState s = g.pol;
        Econ e = g.econ;
        double[] o0 = s.out0, out = s.out;
        s.growthIdx *= 1 + e.gdp / 100 / 52;
        double du = e.unemp - s.unemp0;
        out[O_UNEMP] = e.unemp; out[O_INFL] = e.infl; out[O_GROWTH] = e.gdp; out[O_DEFICIT] = e.deficit; out[O_GAS] = e.gas;
        out[O_UNINS] = clamp(o0[O_UNINS] + sum[O_UNINS] + 0.35 * du, 1, 95);
        out[O_POV] = clamp(o0[O_POV] + sum[O_POV] + 0.45 * du, 2, 60);
        double hc = g.home != null ? g.home.crime / s.homeCrime0 : 1, ho = g.home != null ? g.home.od / s.homeOd0 : 1;
        out[O_CRIME] = Math.max(10, o0[O_CRIME] * (1 + sum[O_CRIME] / 100) * hc);
        out[O_OD] = Math.max(0.5, o0[O_OD] * (1 + sum[O_OD] / 100) * ho);
        out[O_BORDER] = Math.max(0.3, o0[O_BORDER] * (1 + sum[O_BORDER] / 100) * (1 - 0.04 * du));
        out[O_EMIS] = Math.max(0.2, o0[O_EMIS] * (1 + sum[O_EMIS] / 100) * Math.sqrt(s.growthIdx));
        out[O_SCORES] = clamp(o0[O_SCORES] + sum[O_SCORES], 60, 130);
    }

    /** Voters judge results: what your policies have done, by now, to coverage, crime, the border, emissions, schools and poverty. */
    static void results(Game g, double[] sum) {
        double[] r = g.pol.result;
        r[PI_HEALTH] = clamp(-0.12 * sum[O_UNINS], -1.2, 1.2);
        r[PI_CRIME] = clamp(-0.015 * sum[O_CRIME] - 0.004 * sum[O_OD], -1.2, 1.2);
        r[PI_IMMIG] = clamp(-0.005 * sum[O_BORDER], -1.2, 1.2);
        r[PI_ENERGY] = clamp(-0.008 * sum[O_EMIS], -0.8, 0.8);
        r[PI_EDU] = clamp(0.06 * sum[O_SCORES], -1.2, 1.2);
        r[PI_PRICES] = clamp(-0.05 * sum[O_POV], -0.8, 0.8);
    }

    /** The approval effect of policy results on an issue, in the same units as an issue's adjustment. */
    public static double result(Game g, int issue) { return g.pol == null || g.pol.result == null ? 0 : g.pol.result[issue]; }

    static void record(Game g) {
        PolicyState s = g.pol;
        double[] row = new double[NOUT + 1];
        row[0] = g.week;
        System.arraycopy(s.out, 0, row, 1, NOUT);
        s.hist.add(row);
        if (s.hist.size() > 200) s.hist.remove(0);
    }

    // ---- making changes ----

    /** Something already under way for a policy, or null. */
    public static String inMotion(Game g, int k) {
        String id = def(k).id;
        if (g.cong != null) for (Bill b : g.cong.bills) if (b.defId.startsWith("pol:" + id + ":") && b.stage < ST_DESK && b.stage != ST_DEAD) return "A bill on this is already in Congress.";
        if (g.ex != null) for (Action a : g.ex.actions) if (a.policy == k && a.status == A_COMMENT) return "A rule on this is open for public comment.";
        for (int[] b : g.pol.budget) if (b[0] == k) return "It's already in your budget request.";
        return null;
    }

    /** Why a change can't be made this way now, or null if it can. */
    public static String cannot(Game g, int k, int to, int ch) {
        PolicyDef d = def(k);
        int from = g.pol.level[k];
        if (!available(g, k)) return "This isn't on the table until " + d.from + ".";
        if (to < 0 || to >= d.levels.length || to == from) return "Choose a different level.";
        if (!d.can(ch)) return "This can't be done by " + CH_NAME[ch].toLowerCase().replace("a bill in congress", "a bill") + ".";
        int steps = Math.abs(to - from);
        if (ch != CH_BILL && steps > 1) return "Orders, rules and budgets move a policy one step at a time.";
        if (steps > 2) return "One bill can move a policy two steps at most.";
        return inMotion(g, k);
    }

    /** Starts a change through the chosen channel, and says what happens next. */
    public static String propose(Game g, int k, int to, int ch) {
        String no = cannot(g, k, to, ch);
        if (no != null) return no;
        PolicyDef d = def(k);
        int from = g.pol.level[k];
        String what = d.name + ": " + d.levels[to].toLowerCase();
        switch (ch) {
            case CH_BILL: {
                Bill b = bill(g, k, to);
                Legis.introduce(g, b);
                return "You send Congress a bill. Follow it in the Roosevelt Room.";
            }
            case CH_ORDER: case CH_RULE: {
                Action a = new Action();
                a.policy = k; a.polFrom = from; a.polTo = to;
                a.name = what; a.area = AREA_ISSUE[d.area]; a.lean = d.leanFor(to - from);
                a.support = 50;                                  // the policy model handles the public's reaction
                a.legal = Actions.legal(g, d.legal);
                Actions.issuePolicy(g, a, ch == CH_RULE);
                return ch == CH_RULE ? "The agency proposes the rule. It takes effect after public comment." : "You sign the order. It takes effect today.";
            }
            default:
                g.pol.budget.add(new int[]{k, to});
                g.log(NK_INFO, "The president's budget will ask for a change: " + what, null, 0.6);
                return "It goes in your budget request, and takes effect when the fiscal year begins on October 1 if Congress goes along.";
        }
    }

    /** A bill to move a policy, ready to introduce. */
    public static Bill bill(Game g, int k, int to) {
        Bill b = Legis.draft(g, BillData.billDef(billId(k, g.pol.level[k], to)));
        b.payFor = 0;
        return b;
    }

    static String billId(int k, int from, int to) { return "pol:" + def(k).id + ":" + from + ":" + to; }

    public static boolean isPolicyBill(String defId) { return defId.startsWith("pol:"); }

    /** For a policy bill: {policy, level it would set}; otherwise null. */
    public static int[] target(String defId) {
        if (!isPolicyBill(defId)) return null;
        String[] t = defId.split(":");
        int k = index(t[1]);
        return k < 0 ? null : new int[]{k, Integer.parseInt(t[3])};
    }

    /** The bill a policy change becomes: what Congress votes on. */
    public static BillDef billDef(String id) {
        BillDef b = BILL_DEFS.get(id);
        if (b != null) return b;
        String[] t = id.split(":");
        int k = index(t[1]);
        if (k < 0) return null;
        PolicyDef d = def(k);
        int from = Integer.parseInt(t[2]), to = Integer.parseInt(t[3]), steps = to - from;
        String desc = "Changes the " + lower(d.name) + " from " + lower(d.levels[from]) + " to " + lower(d.levels[to]) + ".";
        b = BillData.policyBill(id, d.name + ": " + lower(d.levels[to]), desc, AREA_ISSUE[d.area], d.leanFor(steps), clamp(d.econ * steps, -1, 1), clamp(d.cult * steps, -1, 1),
                d.cost * steps, d.can(CH_BUDGET), d.supportFor(steps), d.area == A_TAX);
        BILL_DEFS.put(id, b);
        return b;
    }

    static String lower(String s) { return s.isEmpty() ? s : Character.toLowerCase(s.charAt(0)) + s.substring(1); }

    /** A bill becomes law: a policy bill moves its policy; a named bill moves the policies it changed in history. */
    public static void law(Game g, Bill b, boolean signed) {
        if (g.pol == null) return;
        if (isPolicyBill(b.defId)) {
            String[] t = b.defId.split(":");
            int k = index(t[1]);
            if (k >= 0) set(g, k, Integer.parseInt(t[3]), CH_BILL, clamp(b.strength, 0.6, 1.2), true, signed ? 2 : 1);
            return;
        }
        for (String row : PolicyTable.BILL_POLICY) {
            String[] t = row.split(" ");
            if (!t[0].equals(b.defId)) continue;
            for (int i = 1; i < t.length; i++) {
                String[] kv = t[i].split("=");
                int k = index(kv[0]);
                if (k >= 0) set(g, k, Integer.parseInt(kv[1]), CH_BILL, 1, false, 1);
            }
        }
    }

    /** An order or rule takes effect, or is struck down, revoked or overturned. */
    public static void fromAction(Game g, Action a, boolean on) {
        if (g.pol == null || a.policy < 0) return;
        int ch = a.kind == K_RULE ? CH_RULE : CH_ORDER;
        if (on) {
            set(g, a.policy, a.polTo, ch, CH_STRENGTH[ch] * Math.min(1, a.scope), true, a.reacted ? 0 : 2);
            a.reacted = true;
        } else if (g.pol.level[a.policy] == a.polTo) set(g, a.policy, a.polFrom, ch, g.pol.strength[a.policy], true, 0);
    }

    /**
     * Moves a policy. econ: whether this model applies its economic effects (a named bill applies its own).
     * react: 0 quietly (a court's ruling), 1 the voter groups only, 2 the groups and the country.
     */
    static void set(Game g, int k, int to, int ch, double str, boolean econ, int react) {
        PolicyState s = g.pol;
        int from = s.level[k];
        if (to == from || to < 0 || to >= def(k).levels.length) return;
        s.level[k] = to;
        if (econ) s.levelEcon[k] += to - from;
        s.strength[k] = str;
        s.changed[k] = g.week;
        s.via[k] = ch;
        s.changes++;
        if (react > 0) react(g, def(k), to - from, str, react == 2);
    }

    /** Approval points across the country when a change is made: up to five for a signature policy the public wants. */
    public static double national(PolicyDef d, int steps, double str) {
        double sup = d.supportFor(steps);
        return clamp((sup - 50) * (d.signature ? 0.2 : 0.06) * Math.min(2, Math.abs(steps)) * str, -5, 5);
    }

    /** Points of approval in a voter group when a change is made. */
    public static double groupEffect(PolicyDef d, int gr, int steps, double str) {
        return d.groups[gr] * steps * str * (d.signature ? 1.5 : 1);
    }

    static void react(Game g, PolicyDef d, int steps, double str, boolean country) {
        if (country) {
            double n = national(d, steps, str);
            g.perf += n / Approval.PERF_WEIGHT;
            int issue = AREA_ISSUE[d.area];
            g.issueAdj[issue] = clamp(g.issueAdj[issue] + n * 0.03, -1.5, 1.5);
            g.issueSal[issue] = Math.min(2, g.issueSal[issue] + 0.1);
        }
        for (int gr = 0; gr < d.groups.length; gr++) if (d.groups[gr] != 0) groupBump(g, gr, groupEffect(d, gr, steps, str));
        HomeFront.act(g, d.econ * steps, d.cult * steps, 0.5 * Math.abs(steps));
    }

    /** At the start of each fiscal year, the changes in your budget take effect if Congress funded them. */
    static void budget(Game g) {
        PolicyState s = g.pol;
        int fy = Hill.fiscalYear(g);
        if (fy == s.fy) return;
        s.fy = fy;
        if (s.budget.isEmpty() || g.cong == null) return;
        double p = g.cong.shutdown ? 0 : g.cong.crUntil >= g.week ? 0.5 : Hill.majority(g, HOUSE, g.party) ? 0.9 : 0.55;
        for (int[] b : s.budget) {
            PolicyDef d = def(b[0]);
            String what = d.name + ": " + lower(d.levels[b[1]]);
            if (g.rng.nextDouble() < p) {
                set(g, b[0], b[1], CH_BUDGET, CH_STRENGTH[CH_BUDGET], true, 2);
                g.log(NK_GOOD, "The new budget takes effect: " + what, null, 1.0);
            } else g.log(NK_BAD, "Congress leaves out of the budget: " + what, null, 0.9);
        }
        s.budget.clear();
    }

    // ---- what a change would do ----

    /** The estimated effect of a move on an outcome once it has worked through: {low, middle, high}. */
    public static double[] range(PolicyDef d, int steps, int o, double str) {
        double mid = d.fx[o] * steps * str;
        double sp = EV_SPREAD[d.evidence] * Math.abs(mid);
        return new double[]{mid - sp, mid, mid + sp};
    }

    /** The change in the deficit, $ billions a year in the era's dollars: {low, middle, high}. */
    public static double[] costRange(PolicyDef d, int steps, double str) {
        double mid = d.cost * steps * str * COST_SCALE;
        double sp = 0.25 * Math.abs(mid);
        return new double[]{mid - sp, mid, mid + sp};
    }

    /** What a policy adds to an outcome right now (for the system map and the outcome cards). */
    public static double contribution(Game g, int k, int o) {
        PolicyState s = g.pol;
        if (outFromEconomy(o)) return (s.effEcon[k] - s.base[k]) * s.strength[k] * def(k).fx[o];
        return (s.eff[k] - s.base[k]) * s.strength[k] * def(k).fx[o];
    }
}
