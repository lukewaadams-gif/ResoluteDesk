package resolutedesk.model;

import java.io.Serializable;

/**
 * Each era is the country on an inauguration day: Congress, the economy, the leaders and the mood.
 * Every inauguration from 1933 on, plus the projected world after Battleground 2028.
 */
public final class Era implements Serializable {
    private static final long serialVersionUID = 1L;
    public int year, inM = 0, inD = 20;        // inauguration day (March 4 until 1937)
    public String name, blurb, predecessor, winnerId;
    public int termOf = 1;                     // which term the real winner is starting
    public boolean fictional;                  // a projected future rather than a real date
    public int houseD = -1, senateD = -1;      // Democratic seats on day one (-1: set by who won)
    public double approvalBase = 45, polarization = 0.85;
    public double unemp, infl, gdp, fed, tenYr, stocks, oil, gas, debt, deficit, nominalGdp, potential = 1.9, nairu = 4.3, priceLevel = 1, tariff = 2.5;
    public double fedCred = 0.8, slump;        // the Fed's credibility; a slow-healing shortfall in demand (the Depression)
    public String fedChair;
    public String speakerD, speakerR, leaderD, leaderR, houseLeaderD, houseLeaderR;   // who leads each party in each chamber
    public String issues;                      // salience weights: "index=weight ..."
    public String labels;                      // what some issues were called then: "index=name ..."
    public boolean ai = true;
    public String midSeats;                    // Senate seats up at the midterm, "AB:D" for the party holding each
}
