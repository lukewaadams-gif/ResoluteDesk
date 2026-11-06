package resolutedesk.congress;

import static resolutedesk.congress.CongressRules.*;
import static resolutedesk.core.Dates.*;
import static resolutedesk.core.Fmt.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.model.NewsKinds.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.sim.DecisionKinds.*;

import java.util.Arrays;
import java.util.Random;
import resolutedesk.economy.Economy;
import resolutedesk.model.Econ;
import resolutedesk.model.Game;
import resolutedesk.sim.Decisions;

/**
 * Your budget request in February; twelve spending bills over the summer; a deadline on October 1;
 * continuing resolutions, shutdowns and, every so often, the debt limit.
 */
public final class Budget {
    static void setup(Game g) {
        Congress c = g.cong;
        double subj = debtSubject(g);
        c.debtLimit = g.era.year == 2025 ? 36.1 : Math.round((subj + 0.9) * 10) / 10.0;
        c.xWeek = g.era.year == 2025 ? 28 : -1;
        Arrays.fill(c.approps, 4);              // the fiscal year already under way runs on a CR or full-year bills
    }

    /** Debt subject to the limit, $ trillions (debt held by the public plus what the government owes its own trust funds). */
    public static double debtSubject(Game g) { return g.econ.debt / 100 * g.econ.nominalGdp * 1.25; }

    static boolean weekHas(Game g, int m, int d) { return Economy.weekHas(g, m, d); }

    public static void week(Game g) {
        Congress c = g.cong;
        int[] d0 = dateOf(g.day());
        int fy = Hill.fiscalYear(g);
        if (fy != c.fy) c.fy = fy;
        // a new budget cycle each year: the request, then twelve bills for the fiscal year that starts in October
        if (weekHas(g, 0, 2)) c.budgetSent = false;
        if (weekHas(g, 1, 1) && c.crUntil < 0 && !c.shutdown) Arrays.fill(c.approps, 0);
        boolean due = g.tw() < 20 ? weekHas(g, 3, 15) : weekHas(g, 1, 3);
        if (due && !c.budgetSent) {
            Decisions.add(g, D_BUDGET, "Your budget is due", (g.tw() < 20 ? "A new president's first budget goes to Congress in the spring." : "The law asks for the president's budget on the first Monday in February.")
                            + " Your budget director has a draft that follows your agenda. You can send it as is, or write your own in the Roosevelt Room (the budget tab).",
                    new String[]{"Send the draft", "I'll write it myself"}, new String[]{"Budget director: \"It funds your priorities and keeps the deficit about where it is.\"", "Chief of staff: \"Your call. It goes to Congress when you hit send.\""});
        }
        if ((g.tw() < 20 ? weekHas(g, 4, 13) : weekHas(g, 2, 3)) && !c.budgetSent) autoRequest(g);
        // appropriations: May through September
        boolean divided = !Hill.majority(g, HOUSE, g.party) || !Hill.majority(g, SENATE, g.party);
        int m = d0[1];
        if (m >= 4 && m <= 8 && !c.shutdown) {
            for (int i = 0; i < 12; i++) {
                if (c.approps[i] >= 3) continue;
                double p = (divided ? 0.05 : 0.09) * (c.approps[i] == 2 ? 1.3 : 1);
                if (g.rng.nextDouble() < p) {
                    c.approps[i]++;
                    if (c.approps[i] == 3) g.log(NK_INFO, "Congress passes the " + APPROPS[i] + " spending bill; you sign it", null, 0.3);
                }
            }
        }
        // the deadline
        if (weekHas(g, 8, 26) && !c.shutdown) deadline(g, divided);
        if (c.crUntil >= 0 && g.week >= c.crUntil && !c.shutdown) {
            c.crUntil = -1;
            if (!divided || g.rng.nextDouble() < 0.7) { omnibus(g); }
            else deadline(g, true);
        }
        if (c.shutdown) shutdownWeek(g);
        debtLimit(g);
    }

    static int unfinished(Congress c) { int n = 0; for (int a : c.approps) if (a < 3) n++; return n; }

    public static void deadline(Game g, boolean divided) {
        Congress c = g.cong;
        int left = unfinished(c);
        if (left == 0) { g.log(NK_GOOD, "All twelve spending bills are law before the deadline, the first time in decades", null, 1.6); g.perf += 0.5; return; }
        int minority = g.party == DEM ? 100 - g.senateD : g.senateD;
        if (!divided && minority >= 41 && !g.filibusterGone && g.rng.nextDouble() < 0.12 + 0.2 * g.era.polarization + Math.max(0, 48 - g.approval) * 0.015) {
            Member ml = Hill.leaderOf(g, SENATE, false);
            c.crMinority = true;
            Decisions.add(g, D_CR, "The Senate minority blocks the stopgap", left + " of the 12 spending bills aren't done, and a stopgap needs 60 votes in the Senate. " + (ml != null ? ml.name : "The minority leader")
                            + " says " + PARTIES[Hill.other(g.party)] + " won't supply them unless you extend " + (g.party == REP ? "the health insurance subsidies that expire this year" : "the border and policing money they want") + ".",
                    new String[]{"Give them what they want and sign the stopgap", "Demand a clean stopgap", "Refuse: let them own the shutdown"},
                    new String[]{"Budget director: \"It's a few billion dollars. A shutdown costs more.\"",
                            "Legislative director: \"A few of their moderates will break. Make them choose.\"",
                            "Political director: \"Our voters want us to stand firm. It's a gamble with everyone else.\""});
            return;
        }
        if (!divided) {
            c.crUntil = g.week + 11;
            for (int i = 0; i < 12; i++) if (c.approps[i] < 3) c.approps[i] = 4;
            g.log(NK_INFO, "Congress passes a stopgap to keep the government open until December", left + " of 12 spending bills are unfinished.", 1.0);
            return;
        }
        Member opp = Hill.leaderOf(g, HOUSE, !Hill.majority(g, HOUSE, g.party));
        String who = opp != null ? opp.name : "The other party's leaders";
        Decisions.add(g, D_CR, "The government runs out of money on October 1", left + " of the 12 spending bills aren't done. " + who + " will pass a stopgap to December only with spending cuts attached.",
                new String[]{"Accept the cuts and sign the stopgap", "Demand a clean stopgap", "Refuse: no deal without full-year funding"},
                new String[]{"Budget director: \"The cuts are small. A shutdown costs more than they do.\"",
                        "Legislative director: \"They don't want a shutdown either. Call their bluff, but have an exit.\"",
                        "Political director: \"A shutdown fight rallies our side. It's a gamble with everyone else.\""});
    }

    public static void crDecision(Game g, int k) {
        Congress c = g.cong;
        Random r = g.rng;
        boolean minority = c.crMinority;
        c.crMinority = false;
        if (k == 0) {
            c.crUntil = g.week + 11;
            for (int i = 0; i < 12; i++) if (c.approps[i] < 3) c.approps[i] = 4;
            g.econ.fiscal += minority ? 0.04 : -0.05;
            for (Bloc b : c.blocs) if (b.party == g.party && (b.econ < -0.3 == (g.party == DEM) || minority && b.hawk > 0.6)) b.rel = Math.max(0, b.rel - 3);
            g.log(NK_INFO, minority ? "You give the minority its price and sign the stopgap; your hardliners fume" : "You sign a stopgap with spending cuts; your party's base grumbles", null, 1.2);
        } else if (k == 1 && r.nextDouble() < (minority ? 0.45 : 0.55) + (g.approval - 45) / 40) {
            c.crUntil = g.week + 11;
            for (int i = 0; i < 12; i++) if (c.approps[i] < 3) c.approps[i] = 4;
            g.perf += 0.6;
            g.capital = Math.min(100, g.capital + 4);
            g.log(NK_GOOD, "The other side blinks: Congress passes a clean stopgap", null, 1.4);
        } else {
            startShutdown(g, minority ? (k == 2 ? 0.5 : 0.42) : (k == 2 ? 0.6 : 0.5));
        }
    }

    static void startShutdown(Game g, double blame) {
        Congress c = g.cong;
        c.shutdown = true; c.shutdowns++;
        c.shutdownWeeks = 0;
        c.blame = blame;
        g.log(NK_BIG, "THE GOVERNMENT SHUTS DOWN", "Hundreds of thousands of federal workers are furloughed; national parks close; air travel slows.", 3.5);
    }

    static void shutdownWeek(Game g) {
        Congress c = g.cong;
        c.shutdownWeeks++; c.shutdownTotal++;
        g.econ.demandShock -= 0.08;
        g.perf -= 0.5 * c.blame;
        g.stress = Math.min(100, g.stress + 4);
        if (c.shutdownWeeks % 2 == 1) g.log(NK_BAD, "The shutdown enters week " + c.shutdownWeeks + "; polls show " + Math.round(c.blame * 100) + "% blame the White House", null, 1.6);
        Decisions.add(g, D_SHUTDOWN, "Week " + c.shutdownWeeks + " of the shutdown", "Federal workers are missing paychecks and airports are backing up. About " + Math.round(c.blame * 100) + "% of Americans blame you, the rest blame Congress.",
                new String[]{"Hold firm", "Cut a deal and reopen the government"},
                new String[]{"Political director: \"Every week they look worse. A few more days.\"", "Chief of staff: \"End it. Nobody wins a long shutdown.\""});
    }

    public static void shutdownDecision(Game g, int k) {
        Congress c = g.cong;
        if (k == 1) { endShutdown(g, false); return; }
        double cave = 0.12 + c.shutdownWeeks * 0.06 + (0.5 - c.blame) * 0.6;
        if (g.rng.nextDouble() < cave) endShutdown(g, true);
        else c.blame = clamp(c.blame + (g.approval < 45 ? 0.03 : -0.02), 0.2, 0.8);
    }

    static void endShutdown(Game g, boolean won) {
        Congress c = g.cong;
        c.shutdown = false;
        c.crUntil = g.week + 10;
        for (int i = 0; i < 12; i++) if (c.approps[i] < 3) c.approps[i] = 4;
        if (won) { g.perf += 1.2; g.capital = Math.min(100, g.capital + 8); g.log(NK_BIG, "SHUTDOWN ENDS ON THE PRESIDENT'S TERMS AFTER " + c.shutdownWeeks + " WEEKS", null, 3.0); }
        else { g.capital = Math.max(0, g.capital - 8); g.econ.fiscal -= 0.05; g.log(NK_BIG, "THE SHUTDOWN ENDS AFTER " + c.shutdownWeeks + " WEEKS WITH A DEAL", "The White House accepted spending cuts to reopen the government.", 2.6); }
    }

    static void omnibus(Game g) {
        Congress c = g.cong;
        for (int i = 0; i < 12; i++) if (c.approps[i] >= 4 || c.approps[i] < 3) c.approps[i] = 3;
        boolean unified = Hill.majority(g, HOUSE, g.party) && Hill.majority(g, SENATE, g.party);
        double share = unified ? 0.5 : 0.2;
        if (c.budgetSent) g.econ.fiscal += share * (c.reqDefense * 0.03 + c.reqNondefense * 0.033);
        g.log(NK_INFO, "Congress passes a year-long spending package and you sign it", null, 1.0);
    }

    // ---- your budget request ----
    public static void autoRequest(Game g) {
        Congress c = g.cong;
        c.reqDefense = g.party == REP ? 4 : 1;
        c.reqNondefense = g.party == DEM ? 4 : -6;
        c.reqTax = g.party == DEM ? 0.3 : -0.3;
        c.reqMandatory = 0;
        send(g);
    }

    public static void send(Game g) {
        Congress c = g.cong;
        c.budgetSent = true;
        double change = deficitChange(c);
        g.log(NK_INFO, "The White House sends its budget to Congress", "It would " + (change > 0.05 ? "add about " + fmt1(change) + "% of GDP to the deficit" : change < -0.05 ? "cut the deficit by about " + fmt1(-change) + "% of GDP" : "leave the deficit about where it is")
                + ". Defense " + signed1(c.reqDefense) + "%, other agencies " + signed1(c.reqNondefense) + "%.", 1.2);
        for (Bloc b : c.blocs) {
            if (b.hawk > 0.6 && change > 0.3) b.rel = Math.max(0, b.rel - 3);
            if (b.hawk > 0.6 && change < -0.3) b.rel = Math.min(100, b.rel + 2);
        }
    }

    /** The request's effect on the deficit, % of GDP. */
    public static double deficitChange(Congress c) { return c.reqDefense * 0.03 + c.reqNondefense * 0.033 - c.reqTax + c.reqMandatory; }

    /** Ten years of deficits and debt under the request: {deficit[], debt[]} as % of GDP. */
    public static double[][] projection(Game g) {
        Econ e = g.econ;
        double[] def = new double[11], debt = new double[11];
        double d = e.debt, rate = e.interest / Math.max(1, e.debt) * 100;
        double primary = e.deficit - e.interest + deficitChange(g.cong);
        double growth = (e.potential + e.polGdp + 2.2) / 100;
        for (int y = 0; y <= 10; y++) {
            rate += (e.tenYr - rate) * 0.25;
            double interest = d * rate / 100;
            double dd = primary + 0.08 * y + interest;
            def[y] = dd;
            debt[y] = d;
            d = d + dd - d * growth;
        }
        return new double[][]{def, debt};
    }

    // ---- the debt limit ----
    static void debtLimit(Game g) {
        Congress c = g.cong;
        double subj = debtSubject(g);
        if (c.xWeek < 0 && subj >= c.debtLimit) {
            c.xWeek = g.week + 22;
            g.log(NK_ECON, "Treasury hits the debt limit and starts \"extraordinary measures\"", "The Treasury secretary says the money runs out around " + dateStr(c.xWeek * 7) + ".", 1.6);
        }
        if (c.xWeek < 0) return;
        int left = c.xWeek - g.week;
        if (left == 8 || left == 3) {
            boolean unified = Hill.majority(g, HOUSE, g.party) && Hill.majority(g, SENATE, g.party);
            Decisions.add(g, D_DEBT, "The debt limit: " + left + " weeks to go", "Unless Congress raises the $" + fmt1(c.debtLimit) + " trillion limit by " + dateStr(c.xWeek * 7) + ", the United States can't pay all its bills."
                            + (unified ? " Your party controls Congress, but a handful of fiscal hawks want something in return." : " The other side wants spending cuts in exchange."),
                    new String[]{"Ask for a clean increase", "Trade spending cuts for an increase", "Go to the brink: no negotiating over the full faith and credit"},
                    new String[]{"Treasury secretary: \"Markets are calm now. They won't be in a month.\"",
                            "Budget director: \"Cuts we can live with, and it's over.\"",
                            "Political director: \"They'll fold before they take the blame for a default. Probably.\""});
        }
        if (left == 1) {
            Decisions.add(g, D_DEBT, "The Treasury runs out of room next week", "Talks have failed. Unless the limit goes up within days, the United States will miss payments to bondholders, retirees and soldiers. "
                            + "The other side's terms are still on the table.",
                    new String[]{"Take their deal", "Invoke the 14th Amendment and keep paying", "Refuse: let it happen"},
                    new String[]{"Treasury secretary: \"Take it. A default would be a catastrophe for decades.\"",
                            "Counsel: \"The Constitution says the public debt shall not be questioned. The courts may agree. Markets won't wait to find out.\"",
                            "Chief of staff: \"Nobody who has sat in that chair has ever let this happen.\""}).a = 1;
        }
        if (left <= 0 && !c.defaulted) {
            c.defaulted = true;
            g.econ.finShock -= 4.5;
            g.perf -= 6;
            g.log(NK_BIG, "THE UNITED STATES MISSES PAYMENTS AS THE DEBT LIMIT BINDS", "Stocks plunge and Treasury yields spike. Congress rushes to raise the limit.", 5);
            raise(g, 1.0);
        }
    }

    public static void debtDecision(Game g, int k, boolean lastChance) {
        Congress c = g.cong;
        Random r = g.rng;
        boolean unified = Hill.majority(g, HOUSE, g.party) && Hill.majority(g, SENATE, g.party);
        if (c.xWeek < 0) return;
        if (lastChance) {
            if (k == 0) { raise(g, 1.0); g.econ.fiscal -= 0.2; g.capital = Math.max(0, g.capital - 12); g.perf -= 0.8; for (Bloc b : c.blocs) if (b.party == g.party) b.rel = Math.max(0, b.rel - 4); g.log(NK_BIG, "A LAST-MINUTE DEAL RAISES THE DEBT LIMIT", "The White House gave up more than it wanted to.", 2.6); }
            else if (k == 1) { raise(g, 1.5); g.econ.finShock -= 1.6; g.perf -= 1.5; g.scandal += 4; g.log(NK_BIG, "PRESIDENT INVOKES THE 14TH AMENDMENT TO KEEP PAYING THE NATION'S BILLS", "Treasury keeps borrowing past the limit. Lawsuits are filed within hours; bond markets lurch.", 3.6); }
            return;
        }
        if (k == 1) { raise(g, 1.5); g.econ.fiscal -= 0.15; g.capital = Math.max(0, g.capital - 5); g.log(NK_INFO, "A deal: spending cuts for a higher debt limit", null, 1.6); return; }
        double p = k == 0 ? (unified ? 0.85 : 0.35) : (unified ? 0.7 : 0.45);
        if (r.nextDouble() < p) {
            raise(g, 2.0);
            if (k == 2) { g.perf += 1; g.capital = Math.min(100, g.capital + 6); }
            g.log(NK_GOOD, "Congress raises the debt limit" + (k == 0 ? " with no strings attached" : " after a tense standoff"), null, 1.8);
        } else {
            g.econ.finShock -= k == 2 ? 1.2 : 0.6;
            g.log(NK_BAD, "Debt-limit talks collapse; markets slide", null, 1.8);
        }
    }

    /** Raises the limit by enough for this many years of borrowing. */
    static void raise(Game g, double years) {
        Congress c = g.cong;
        double perYear = Math.max(0.8, g.econ.deficit / 100 * g.econ.nominalGdp * 1.25);
        c.debtLimit = Math.round((debtSubject(g) + years * perYear) * 10) / 10.0;
        c.xWeek = -1;
        c.defaulted = false;
    }
}
