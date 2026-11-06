package resolutedesk.model;

import static resolutedesk.data.StateData.*;

import java.io.Serializable;

/** The state of the economy in a game: growth, jobs, prices, rates, markets, the budget and the debt. */
public final class Econ implements Serializable {
    private static final long serialVersionUID = 1L;
    public double og, potential = 1.9, nairu = 4.3, structural;     // output gap, trend growth, natural unemployment
    public double gdp, unemp, infl, core, expect;                    // growth (yoy %), unemployment %, inflation %, expectations
    public double fed, fedTarget, rNeutral = 0.75, tenYr, mortgage;
    public double stocks, stocks0, oil, oilBase, gas, gas0;
    public double groceries, rent, insurance, sentiment;
    public double debt, deficit, interest, nominalGdp;              // % of GDP, % of GDP, % of GDP, $ trillions
    public double fiscal, tariff, demandShock, supplyShock, finShock, aiShock, finShockPrev, ogPrev;
    public int calm;
    public double housing, manufacturing, aiInvest, farm, energyIdx, tourism;
    public double priceLevel = 1;                                    // era prices relative to 2025
    public String fedChair = "";
    public double fedHawk = 0.5, fedCred = 0.8;
    int nextFomc;
    public double[] ogHist = new double[53];
    public int ogPos;
    public double jobsLast, gdpReported, cpiReported;
    public double[] stUnemp = new double[NS], stGas = new double[NS];
    public double polGdp, polUnemp, polInfl;                         // what the laws you sign do to trend growth, jobs and prices
    public double gasRef = 3.1, gasAdj, uRef0 = 4.5, piRef0 = 2.5, uRef = 4.5, piRef = 2.5;   // what the public measures you against
    public double slump, war, war0;                                  // the Depression's slow-healing demand gap; war spending (% of GDP) now and at the start
}
