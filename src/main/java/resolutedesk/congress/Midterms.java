package resolutedesk.congress;

import static resolutedesk.campaign.CampaignData.*;
import static resolutedesk.congress.CongressHistory.*;
import static resolutedesk.congress.CongressRules.*;
import static resolutedesk.core.Dates.*;
import static resolutedesk.core.Fmt.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.data.ElectoralMap.*;
import static resolutedesk.data.StateData.*;
import static resolutedesk.executive.Posts.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.model.NewsKinds.*;
import static resolutedesk.model.Parties.*;

import java.util.Arrays;
import java.util.Random;
import resolutedesk.executive.Cabinet;
import resolutedesk.model.Econ;
import resolutedesk.model.Game;
import resolutedesk.sim.Decisions;

/**
 * They run themselves, mostly on your approval and the economy. From August you can campaign and raise money
 * for your party's candidates, and the Senate seats up that year are fought state by state.
 */
public final class Midterms {
    /** Election day: the Tuesday after the first Monday in November, two years in. */
    public static int electionDay(Game g) {
        int y = termYear(g) + 1;
        for (int d = 2; d <= 8; d++) { int day = dayOfDate(y, 10, d); if (dowOf(day) == 1) return day; }
        return dayOfDate(y, 10, 3);
    }

    public static boolean season(Game g) {
        int ed = electionDay(g);
        return !g.cong.midDone && g.day() >= ed - 100 && g.day() <= ed;
    }

    /** The Senate seats up at the midterm: "AB:D" or "AB:R" for the party holding each. */
    public static String seatsUp(Game g) {
        if (g.era.midSeats != null && g.term == 1) return g.era.midSeats;
        return senateUp(termYear(g) + 1, g.senateD);
    }

    public static double nationalMargin(Game g, double noise) {
        Econ e = g.econ;
        double camp = 0;
        for (double v : g.cong.campaign) camp += v;
        double mP = 0.5 * (g.approval - 50) - 2.5 + 0.4 * (e.gdp - 2) - 0.3 * (e.unemp - e.uRef) + Math.min(1.5, g.funds / 40) + Math.min(1.0, camp * 0.05) + (Cabinet.sq(g, S_POL) - 0.55) * 1.5 + (g.home != null ? ((g.home.fac[0] + g.home.fac[1]) / 2 - 55) * 0.04 : 0) + noise;
        return mP;
    }

    public static void week(Game g) {
        if (g.cong.midDone) return;
        int ed = electionDay(g);
        if (g.day() + 7 > ed && g.day() <= ed) run(g);
        else if (g.day() + 7 > ed - 63 && g.day() <= ed - 63) g.log(NK_INFO, "Campaign season: the midterms are nine weeks away", "You can now schedule campaign stops for your party's candidates from your calendar.", 1.2);
    }

    static void run(Game g) {
        Congress c = g.cong;
        Random r = g.rng;
        c.midDone = true;
        double mP = nationalMargin(g, r.nextGaussian() * 1.8);
        c.midMargin = mP;
        double natD = g.party == DEM ? mP : -mP;
        int hB = g.myHouse(), sB = g.mySenate();
        boolean houseBefore = Hill.majority(g, HOUSE, g.party), senBefore = Hill.majority(g, SENATE, g.party);
        int my = termYear(g) + 1;
        g.houseD = clampi((int) Math.round(houseBase(my) + houseSlope(my) * natD + r.nextGaussian() * 5), 120, 340);
        int dUp = 0, dWon = 0;
        StringBuilder flips = new StringBuilder();
        String up = seatsUp(g);
        if (!up.isEmpty()) for (String tok : up.split(" ")) {
            String[] p = tok.split(":");
            int s = stateIdx(p[0]);
            if (s < 0) continue;
            boolean dHeld = p[1].equals("D");
            if (dHeld) dUp++;
            int yy = yearNow(g);
            double m = leanIn(s, yy) + natD * (yy >= 1995 ? 1 : 0.6) + (dHeld ? 1 : -1) * (yy >= 1995 ? 3 : 6) + c.campaign[s] * 1.2 * (g.party == DEM ? 1 : -1) + r.nextGaussian() * 3.5;
            if (m > 0) dWon++;
            if (dHeld != m > 0) flips.append(flips.length() > 0 ? ", " : "").append(STATES[s].name).append(m > 0 ? " (D gain)" : " (R gain)");
        }
        g.senateD = clampi(g.senateD - dUp + dWon, 0, 100);
        Hill.seats(g);
        boolean houseAfter = Hill.majority(g, HOUSE, g.party), senAfter = Hill.majority(g, SENATE, g.party);
        Leaders.reshuffle(g, houseBefore != houseAfter, senBefore != senAfter);
        if (g.myHouse() > hB) g.midGain = true;
        String head = (mP >= 0 ? "Your party gains" : "Your party loses") + " in the midterms";
        String body = "The national House vote went " + (g.party == DEM ? margin(natD) : margin(natD)) + ". " + PARTIES[g.party] + " now hold " + g.myHouse() + " House seats (" + signedInt(g.myHouse() - hB) + ") and "
                + g.mySenate() + " Senate seats (" + signedInt(g.mySenate() - sB) + ")." + (flips.length() > 0 ? " Senate seats that changed hands: " + flips + "." : "")
                + (houseBefore && !houseAfter ? " You have lost the House." : !houseBefore && houseAfter ? " You have won back the House." : "")
                + (senBefore && !senAfter ? " You have lost the Senate." : !senBefore && senAfter ? " You have won the Senate." : "");
        c.midSummary = body;
        g.log(NK_BIG, head.toUpperCase(), body, 4.5);
        Decisions.info(g, "The Midterm Elections", body + "\n\n" + (houseAfter && senAfter ? "You keep control of Congress for the second half of your term." : !houseAfter && !senAfter ? "Divided government: the other party controls Congress. Expect investigations and hard bargaining." : "A split Congress: one chamber is yours, the other isn't."));
        if (mP < -3) g.perf -= 1;
        Arrays.fill(c.campaign, 0);
        g.funds *= 0.3;
    }

    public static String signedInt(int v) { return (v >= 0 ? "+" : "") + v; }
}
