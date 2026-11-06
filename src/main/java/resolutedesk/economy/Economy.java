package resolutedesk.economy;

import static resolutedesk.core.Dates.*;
import static resolutedesk.core.Fmt.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.data.StateData.*;
import static resolutedesk.economy.StateEconomy.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.model.NewsKinds.*;

import java.util.Random;
import resolutedesk.model.Econ;
import resolutedesk.model.Era;
import resolutedesk.model.Game;
import resolutedesk.model.Settings;

/** The weekly economic model: output, jobs, prices, the Fed, markets, energy, the budget and the debt, with state-by-state effects. */
public final class Economy {
    static double[] uOff, gasOff;
    static boolean[] mfg, farm, energy, tech, tour;
    /** FOMC decision days: {month0, day}. */
    static final int[][] FOMC = {{0, 29}, {2, 19}, {4, 7}, {5, 18}, {6, 30}, {8, 17}, {10, 5}, {11, 10}};

    static void tables() {
        if (uOff != null) return;
        uOff = stateTable(ST_UNEMP); gasOff = stateTable(ST_GAS);
        mfg = stateSet(ST_MFG); farm = stateSet(ST_FARM); energy = stateSet(ST_ENERGY); tech = stateSet(ST_TECH); tour = stateSet(ST_TOUR);
    }

    public static void init(Game g) {
        tables();
        Era er = g.era;
        Econ e = g.econ;
        Settings s = g.set;
        e.potential = er.potential; e.nairu = er.nairu; e.priceLevel = er.priceLevel;
        e.unemp = er.unemp; e.infl = er.infl; e.gdp = er.gdp; e.fed = er.fed; e.fedTarget = er.fed; e.tenYr = er.tenYr;
        e.stocks = er.stocks; e.oil = er.oil; e.oilBase = er.oil; e.debt = er.debt; e.deficit = er.deficit; e.nominalGdp = er.nominalGdp;
        e.tariff = er.tariff; e.fedChair = er.fedChair;
        e.fedCred = er.fedCred; e.slump = er.slump;
        switch (s.scenario) {
            case 1: e.unemp += 2; e.gdp -= 2.8; e.stocks *= 0.8; e.demandShock = -2.5; e.deficit += 1.5; break;
            case 2: e.oil *= 1.3; e.deficit += 0.8; break;
            case 5: e.infl += 3; e.oil *= 1.25; e.fed += 1; e.supplyShock = 2.0; break;
            default: break;
        }
        if (!Double.isNaN(s.cUnemp)) e.unemp = s.cUnemp;
        if (!Double.isNaN(s.cInfl)) e.infl = s.cInfl;
        if (!Double.isNaN(s.cGdp)) e.gdp = s.cGdp;
        if (!Double.isNaN(s.cDeficit)) e.deficit = s.cDeficit;
        e.og = -(e.unemp - e.nairu) / 0.5;
        double old = e.og - (e.gdp - e.potential);
        for (int i = 0; i < 53; i++) e.ogHist[i] = old + (e.og - old) * i / 52.0;
        e.ogPos = 0;
        e.ogHist[52] = e.og;
        e.expect = 0.7 * 2.0 + 0.3 * e.infl;
        e.core = e.infl;
        e.rNeutral = 0.75;
        if (er.year < 2021) e.gasAdj = (er.gas - gasPrice(e)) / e.priceLevel;
        e.gas = gasPrice(e); e.gas0 = e.gas;
        e.gasRef = er.year < 2021 ? e.gas / e.priceLevel : 3.1;
        e.uRef0 = e.unemp; e.piRef0 = e.infl;
        refs(g);
        e.stocks0 = e.stocks;
        e.mortgage = e.tenYr + 1.8;
        e.interest = e.debt * 0.032;
        e.groceries = e.infl + 0.3; e.rent = e.infl + 1.4; e.insurance = e.infl + 3.5;
        e.housing = 1.40; e.manufacturing = 49; e.aiInvest = 100; e.farm = 100; e.energyIdx = 100; e.tourism = 100;
        e.sentiment = sentimentOf(e);
        e.jobsLast = 150; e.gdpReported = e.gdp; e.cpiReported = e.infl;
        states(e);
    }

    static double gasPrice(Econ e) { return (1.25 + e.gasAdj) * e.priceLevel + 0.0245 * e.oil; }

    /** Deflation hurts too: a falling price level counts like inflation of the same size. */
    public static double piEff(double infl) { return infl >= 0 ? infl : -1.2 * infl; }

    /** What the public measures the economy against: normal times, or partly what you inherited if you inherited a disaster. */
    static void refs(Game g) {
        Econ e = g.econ;
        double w = Math.max(0.25, 0.65 - 0.25 * g.week / 208.0);
        e.uRef = 4.5 + Math.max(0, e.uRef0 - 5.5) * w;
        e.piRef = 2.5 + Math.max(0, piEff(e.piRef0) - 3.5) * w;
    }

    static double sentimentOf(Econ e) {
        return clamp(96 - 4.5 * (e.unemp - e.uRef + 0.5) - 5.5 * (piEff(e.infl) - e.piRef + 0.5) - 6 * (e.gas / e.priceLevel - e.gasRef + 0.1) + 1.5 * (e.gdp - 2) + 12 * Math.log(e.stocks / e.stocks0), 45, 112);
    }

    static void states(Econ e) {
        tables();
        double mfgDev = (e.manufacturing - 50) / 10, farmDev = (e.farm - 100) / 100, enDev = (e.energyIdx - 100) / 100 + (e.oil - e.oilBase) / 200,
                techDev = (e.aiInvest - 100) / 400, tourDev = (e.tourism - 100) / 100;
        for (int s = 0; s < NS; s++) {
            double u = e.unemp + uOff[s] * (e.unemp / 4.0);
            if (mfg[s]) u -= 0.8 * mfgDev;
            if (farm[s]) u -= 0.9 * farmDev;
            if (energy[s]) u -= 0.8 * enDev;
            if (tech[s]) u -= 0.5 * techDev;
            if (tour[s]) u -= 1.0 * tourDev;
            e.stUnemp[s] = clamp(u, 1.5, 20);
            e.stGas[s] = e.gas + gasOff[s] * e.priceLevel;
        }
    }

    public static boolean weekHas(Game g, int month0, int day) {
        for (int d = 0; d < 7; d++) { int[] t = dateOf(g.day() + d); if (t[1] == month0 && t[2] == day) return true; }
        return false;
    }

    /** The first Friday of a month falls in this week. */
    static int jobsDay(Game g) {
        for (int d = 0; d < 7; d++) { int day = g.day() + d; int[] t = dateOf(day); if (dowOf(day) == 4 && t[2] <= 7) return day; }
        return -1;
    }

    public static void week(Game g) {
        Econ e = g.econ;
        Random r = g.rng;
        shocks(g);
        double realRate = e.fed - e.expect;
        refs(g);
        e.slump *= 0.9965;
        double ogTarget = -0.6 * (realRate - e.rNeutral) + 0.55 * e.fiscal + e.demandShock + e.finShock - 0.07 * (e.tariff - 2.5) + 0.03 * (e.sentiment - 80) - e.aiShock * 0.3
                + e.slump + 0.45 * (e.war - e.war0);
        e.og += (ogTarget - e.og) * 0.022 + r.nextGaussian() * 0.05;
        e.demandShock *= 0.985; e.supplyShock *= 0.965; e.finShock *= 0.975; e.aiShock *= 0.99;
        e.ogHist[e.ogPos] = e.og;
        e.ogPos = (e.ogPos + 1) % 53;
        double old = e.ogHist[e.ogPos];
        e.gdp = e.potential + e.polGdp + (e.og - old) + (g.era.ai ? 0.15 * (e.aiInvest - 100) / 100 : 0);
        // unemployment follows the gap, plus any structural damage (AI disruption, a lost decade)
        e.structural = Math.max(0, e.structural * 0.995 + e.aiShock * 0.004);
        double uTarget = e.nairu + e.polUnemp + e.structural - 0.5 * e.og;
        e.unemp += (uTarget - e.unemp) * 0.035 + r.nextGaussian() * 0.012;
        e.unemp = clamp(e.unemp, 1.0, 30);
        // inflation: anchored by the Fed's credibility, pushed by the gap, oil and tariffs
        double anchor = e.fedCred * 2.0 + (1 - e.fedCred) * e.infl;
        e.expect += (anchor - e.expect) * 0.02;
        double ogP = e.og < -5 ? -5 + 0.04 * (e.og + 5) : e.og;
        double piT = e.expect + e.polInfl + 0.28 * ogP + e.supplyShock + 0.012 * (e.oil - e.oilBase) + 0.05 * (e.tariff - g.era.tariff);
        e.infl += (piT - e.infl) * 0.03 + r.nextGaussian() * 0.025;
        e.infl = clamp(e.infl, -12, 25);
        e.core += (e.infl - 0.2 - e.core) * 0.04;
        // the Fed meets eight times a year
        double taylor = e.rNeutral + e.infl + 0.5 * (e.infl - 2) + 0.5 * e.og + (e.fedHawk - 0.5);
        e.fedTarget = clamp(taylor, 0.125, 20);
        if (yearNow(g) < 1951) e.fedTarget = Math.min(e.fedTarget, 1.0);   // the Fed pegged rates low to finance the war, until the 1951 accord
        for (int[] f : FOMC) if (weekHas(g, f[0], f[1])) fomc(g);
        // markets
        double prev10 = e.tenYr;
        double ten = 0.55 * e.fed + 0.45 * (e.rNeutral + 1.0 + e.expect) + 0.015 * Math.max(0, e.debt - 90) + 0.1;
        e.tenYr += (ten - e.tenYr) * 0.06 + r.nextGaussian() * 0.04;
        e.mortgage = e.tenYr + 1.8;
        double ret = 0.0010 + 0.0012 * (e.gdp - e.potential) - 0.035 * (e.tenYr - prev10) + 0.004 * (e.finShock - e.finShockPrev) + 0.002 * (e.og - e.ogPrev) * 10
                + r.nextGaussian() * 0.019;
        e.finShockPrev = e.finShock; e.ogPrev = e.og;
        e.stocks *= Math.exp(ret);
        // oil and gas
        e.oil += (e.oilBase - e.oil) * 0.015 + r.nextGaussian() * 1.4 * e.oilBase / 75;
        double pl = e.priceLevel < 0.95 ? e.priceLevel : 1;
        e.oil = clamp(e.oil, 15 * pl, 250 * pl);
        e.gas += (gasPrice(e) - e.gas) * 0.25;
        // the prices people feel
        e.groceries += (e.infl + 0.3 + 0.06 * (e.tariff - 2.5) - 0.01 * (e.farm - 100) - e.groceries) * 0.05 + r.nextGaussian() * 0.05;
        e.rent += (e.infl + 1.3 - 0.8 * (e.housing - 1.4) - e.rent) * 0.03 + r.nextGaussian() * 0.03;
        e.insurance += (e.infl + 3.0 - e.insurance) * 0.03 + r.nextGaussian() * 0.06;
        // industries
        e.housing += (1.45 - 0.12 * (e.mortgage - 6.5) + 0.03 * e.og - e.housing) * 0.05 + r.nextGaussian() * 0.01;
        e.manufacturing += (50 + 1.4 * e.og - 0.12 * (e.tariff - g.era.tariff) - e.manufacturing) * 0.08 + r.nextGaussian() * 0.4;
        if (g.era.ai) e.aiInvest *= 1 + 0.0015 + r.nextGaussian() * 0.004;
        e.farm += (100 - 0.8 * Math.max(0, e.tariff - g.era.tariff) - e.farm) * 0.03 + r.nextGaussian() * 0.5;
        e.energyIdx += (100 + 0.2 * (e.oil - e.oilBase) - e.energyIdx) * 0.03 + r.nextGaussian() * 0.3;
        e.tourism += (100 + 2 * e.og - e.tourism) * 0.05 + r.nextGaussian() * 0.4;
        e.sentiment += (sentimentOf(e) - e.sentiment) * 0.2 + r.nextGaussian() * 0.6;
        // the budget
        double cyc = -0.35 * e.og;
        double avgRate = e.interest / Math.max(1, e.debt) * 100;
        avgRate += (e.tenYr - avgRate) * 0.004;
        e.interest = e.debt * avgRate / 100;
        double primary = g.era.deficit - e.debt * 0.032 + e.fiscal + (e.war - e.war0);
        double target = primary + cyc + e.interest;
        e.deficit += (target - e.deficit) * 0.05;
        double nom = (e.gdp + e.infl) / 100;
        e.debt += (e.deficit - e.debt * nom) / 52;
        e.nominalGdp *= 1 + nom / 52;
        states(e);
        reports(g);
    }

    /** The business cycle's surprises: about one recession every six years, oil spikes, the odd boom. */
    static void shocks(Game g) {
        Econ e = g.econ;
        Random r = g.rng;
        if (e.calm > 0) { e.calm--; return; }
        double x = r.nextDouble();
        if (x < 0.0035) {
            e.demandShock -= 1.5 + r.nextDouble() * 2; e.calm = 40;
            g.log(NK_ECON, "Layoffs spread as consumers and businesses pull back", "Economists warn the economy may be heading into recession.", 2.0);
        } else if (x < 0.0045) {
            e.finShock -= 2 + r.nextDouble() * 2; e.calm = 52;
            g.log(NK_ECON, "Markets plunge as a big lender fails", "Credit is tightening and stocks fell sharply.", 2.6);
        } else if (x < 0.0075) {
            e.oil *= 1.25 + r.nextDouble() * 0.2; e.oilBase *= 1.08; e.calm = 20;
            g.log(NK_ECON, "Oil jumps on supply fears; gas prices climb", null, 1.6);
        } else if (x < 0.0095) {
            e.demandShock += 0.6 + r.nextDouble() * 0.8; e.calm = 26;
            g.log(NK_ECON, "Business investment surges; economists raise their growth forecasts", null, 1.0);
        }
    }

    static void fomc(Game g) {
        Econ e = g.econ;
        double gap = e.fedTarget - e.fed;
        double step = Math.abs(gap) < 0.15 ? 0 : Math.abs(gap) < 0.6 ? 0.25 : 0.5;
        if (e.finShock < -2 && gap < 0) step = Math.min(Math.abs(gap), 1.0);
        double old = e.fed;
        e.fed = clamp(e.fed + Math.signum(gap) * step, 0.125, 20);
        String chair = e.fedChair == null || e.fedChair.isEmpty() ? "The Fed" : "The Fed under " + e.fedChair;
        if (e.fed > old) g.log(NK_ECON, chair + " raises rates to " + rate(e.fed), "The Federal Reserve lifted its target by " + bp(e.fed - old) + ", citing inflation of " + fmt1(e.infl) + "%.", 1.2);
        else if (e.fed < old) g.log(NK_ECON, chair + " cuts rates to " + rate(e.fed), "The Federal Reserve cut its target by " + bp(old - e.fed) + " as unemployment reached " + fmt1(e.unemp) + "%.", 1.2);
        else g.log(NK_ECON, "Fed holds rates at " + rate(e.fed), null, 0.4);
    }

    /** The Fed's target as it was announced: a quarter-point range since December 2008, a single rate before. */
    public static String rate(double r) {
        if (CAL_YEAR < 2009) return fmt2(Math.round(r * 4) / 4.0) + "%";
        double lo = Math.floor(r * 4 + 0.001) / 4;
        return fmt2(lo) + "-" + fmt2(lo + 0.25) + "%";
    }

    static String bp(double d) { return Math.round(d * 100) + " basis points"; }

    static void reports(Game g) {
        Econ e = g.econ;
        Random r = g.rng;
        int jd = jobsDay(g);
        if (jd >= 0) {
            double prevU = g.hist.size() >= 4 ? g.hist.get(g.hist.size() - 4)[2] : e.unemp;
            double jobs = 110 + (prevU - e.unemp) * 1100 + 25 * e.og + r.nextGaussian() * 45;
            e.jobsLast = jobs;
            int[] t = dateOf(jd - 7);
            String m = MONTH_FULL[t[1]];
            if (jobs < 0) g.log(NK_ECON, "Economy sheds " + thousands(Math.round(-jobs * 1000)) + " jobs in " + m, "Unemployment is " + fmt1(e.unemp) + "%.", 1.6);
            else g.log(NK_ECON, "Employers add " + thousands(Math.round(jobs * 1000)) + " jobs in " + m + "; unemployment " + fmt1(e.unemp) + "%", null, jobs > 250 ? 1.4 : 0.9);
        }
        if (weekHas(g, dateOf(g.day())[1], 12) || weekHas(g, dateOf(g.day() + 6)[1], 12)) {
            e.cpiReported = e.infl;
            g.log(NK_ECON, "Prices up " + fmt1(e.infl) + "% over the past year", "Groceries are up " + fmt1(e.groceries) + "%, rent " + fmt1(e.rent) + "% and car insurance " + fmt1(e.insurance) + "%.", e.infl > 4 ? 1.6 : 0.7);
        }
        for (int m : new int[]{0, 3, 6, 9}) if (weekHas(g, m, 28)) {
            double q = e.gdp + r.nextGaussian() * 0.8;
            e.gdpReported = q;
            g.log(NK_ECON, q < 0 ? "The economy shrank at a " + fmt1(-q) + "% rate last quarter" : "The economy grew at a " + fmt1(q) + "% rate last quarter", null, q < 0 ? 2 : 0.8);
        }
    }
}
