package resolutedesk.executive;

import static resolutedesk.congress.CongressRules.*;
import static resolutedesk.core.Dates.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.data.Faces.*;
import static resolutedesk.data.Names.*;
import static resolutedesk.executive.ActionData.*;
import static resolutedesk.executive.CourtData.*;
import static resolutedesk.executive.ScandalData.*;
import static resolutedesk.model.NewsKinds.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.sim.DecisionKinds.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import resolutedesk.congress.Bloc;
import resolutedesk.congress.Hill;
import resolutedesk.congress.Midterms;
import resolutedesk.model.Game;
import resolutedesk.sim.Decision;
import resolutedesk.sim.Decisions;

/** Lawsuits, rulings and judicial appointments, from district courts to the Supreme Court. */
public final class Courts {
    static double oppShare(Game g, int level) {
        double d = level == CS_DISTRICT ? g.ex.dDistrict : g.ex.dCircuit;
        return g.party == DEM ? 1 - d : d;
    }

    public static void sue(Game g, Action a) {
        Case c = new Case();
        c.action = g.ex.actions.indexOf(a);
        c.stage = CS_DISTRICT;
        c.decideWeek = g.week + 3 + g.rng.nextInt(5);
        c.plaintiff = PLAINTIFFS[g.rng.nextInt(PLAINTIFFS.length)];
        a.sued = true;
        g.ex.cases.add(c);
        g.log(NK_INFO, c.plaintiff + " sues to stop " + a.name.toLowerCase(), null, 0.9);
    }

    public static Action act(Game g, Case c) { return c.action >= 0 && c.action < g.ex.actions.size() ? g.ex.actions.get(c.action) : null; }

    /** The chance a court at this level rules against you. */
    static double pBlock(Game g, Case c) {
        Action a = act(g, c);
        double opp = oppShare(g, c.stage);
        if (c.stage == CS_DISTRICT) opp = 0.5 + 0.5 * opp;                    // plaintiffs pick a friendly courthouse
        return clamp(Actions.legal(g, a.legal) * (0.5 + 0.9 * opp), 0.02, 0.97);
    }

    static double justiceP(Game g, Justice j, Action a) {
        double side = a.lean == REP ? 1 : a.lean == DEM ? -1 : 0;
        return logistic(2.0 * j.ideo * side + 3.0 * (0.5 - Actions.legal(g, a.legal)) + 0.2);
    }

    public static void week(Game g) {
        Exec x = g.ex;
        Random r = g.rng;
        // the lower courts: vacancies open, the Senate fills them
        x.vacDistrict += 1.15; x.vacCircuit += 0.22;
        boolean senate = Hill.majority(g, SENATE, g.party);
        double rate = (senate ? 1.15 : 0.2) * (x.judgeStrategy == 0 ? 0.8 : x.judgeStrategy == 2 ? 1.1 : 1);
        double dc = Math.min(x.vacDistrict, rate * 0.82), cc = Math.min(x.vacCircuit, rate * 0.18);
        double tilt = x.judgeStrategy == 2 ? 0.5 : 1;
        double mineD = g.party == DEM ? 1 : 0;
        x.dDistrict = clamp(x.dDistrict + dc * (mineD - x.dDistrict) * tilt / 677, 0, 1);
        x.dCircuit = clamp(x.dCircuit + cc * (mineD - x.dCircuit) * tilt / 179, 0, 1);
        x.vacDistrict -= dc; x.vacCircuit -= cc;
        x.judgeFrac += dc + cc;
        while (x.judgeFrac >= 1) { x.judgeFrac -= 1; x.judges++; }
        // the Supreme Court: a retirement or a death
        int yr = dateOf(g.day())[0];
        for (Justice j : new ArrayList<Justice>(x.court)) {
            int age = yr - j.born;
            double hz = 0.00012 * Math.exp((age - 60) / 7.5);
            if (j.party == g.party && senate && age >= 70 && g.tw() < 150) hz += 0.0016;
            if (j.party != g.party) hz *= 0.6;
            if (r.nextDouble() < hz) { vacancy(g, j, age); break; }
        }
        if (x.nomineeJ != null && x.scotusVoteAt >= 0 && g.week >= x.scotusVoteAt) scotusVote(g);
        // cases
        for (Case c : new ArrayList<Case>(x.cases)) if (!c.done && g.week >= c.decideWeek) decide(g, c);
    }

    static void vacancy(Game g, Justice j, int age) {
        Exec x = g.ex;
        x.court.remove(j);
        x.scotusVacancies++;
        boolean died = !j.real && age > 80 && g.rng.nextDouble() < 0.5;      // real justices only ever retire
        g.log(NK_BIG, (died ? "JUSTICE " + j.name.toUpperCase() + " DIES AT " + age : "JUSTICE " + j.name.toUpperCase() + " RETIRES FROM THE SUPREME COURT"), "The Court is down to eight. The choice of a successor is yours.", 4);
        Decisions.info(g, "A Supreme Court vacancy", "Justice " + j.name + (died ? " has died at " + age + "." : " is retiring at " + age + ".")
                + " The Court now has " + count(g, REP) + " justices appointed by Republicans and " + count(g, DEM) + " by Democrats. Choose a nominee in the West Wing (the Courts tab).");
    }

    static int yr(Game g) { return dateOf(g.day())[0]; }

    public static int count(Game g, int party) { int n = 0; for (Justice j : g.ex.court) if (j.party == party) n++; return n; }

    public static List<Justice> shortlist(Game g) {
        Random r = new Random(g.set.seed * 7 + g.ex.scotusVacancies * 1013);
        List<Justice> l = new ArrayList<Justice>();
        double side = g.party == REP ? 1 : -1;
        double[] ideo = {0.85, 0.5, 0.2};
        int[] age = {46, 53, 60};
        for (int i = 0; i < 3; i++) {
            Justice j = new Justice();
            boolean f = genFemale(r, 0.45);
            j.name = "Judge " + genFirst(r, f) + " " + genLast(r);
            j.ideo = side * (ideo[i] + r.nextGaussian() * 0.05);
            j.born = yr(g) - age[i] - r.nextInt(4);
            j.party = g.party; j.real = false;
            j.face = face(j.name.substring(6), r, g.party);
            if (j.face.look != null) Cabinet.femaleLook(j.face.look, f, r);
            l.add(j);
        }
        return l;
    }

    public static final String[] SHORTLIST_WHAT = {"A young appeals judge with a strong record for your side", "A respected appeals judge in the mainstream of your party", "A moderate with friends in both parties"};

    static double scotusBloc(Game g, int bi, Justice j) {
        Bloc b = g.cong.blocs.get(bi);
        double bIdeo = (b.econ + b.cult) / 2;
        double l = 0.6 - 2.4 * Math.abs(j.ideo - bIdeo);
        if (b.party == g.party) l += 2.4 * b.loyalty; else l -= 1.5 * g.era.polarization;
        return logistic(1.5 * l);
    }

    static double[] scotusCount(Game g, Justice j) {
        double yes = 0, var = 0;
        for (int i = 0; i < g.cong.blocs.size(); i++) { Bloc b = g.cong.blocs.get(i); if (b.chamber != SENATE) continue; double q = scotusBloc(g, i, j); yes += b.seats * q; var += b.seats * q * (1 - q); }
        return new double[]{yes, Math.sqrt(var) + 0.8};
    }

    public static double scotusChance(Game g, Justice j) { double[] c = scotusCount(g, j); return logistic(1.702 * (c[0] - 50 + 0.5) / c[1]); }

    public static void nominateJustice(Game g, Justice j) {
        Exec x = g.ex;
        x.nomineeJ = j;
        boolean opp = !Hill.majority(g, SENATE, g.party);
        x.scotusVoteAt = g.week + 7 + g.rng.nextInt(4);
        g.log(NK_BIG, ("President " + g.pres.last + " nominates " + j.name + " to the Supreme Court").toUpperCase(), null, 3.2);
        if (opp && (Midterms.season(g) || g.tw() > 150)) {
            x.scotusVoteAt = g.week + 40;
            g.log(NK_BAD, "The Senate majority says it won't consider a Supreme Court nominee in an election year", null, 2.4);
        }
    }

    static void scotusVote(Game g) {
        Exec x = g.ex;
        Justice j = x.nomineeJ;
        double[] c = scotusCount(g, j);
        int yes = (int) Math.round(clamp(c[0] + g.rng.nextGaussian() * c[1], 0, 100)), no = 100 - yes;
        boolean tie = yes == 50 && Hill.majority(g, SENATE, g.party);
        x.nomineeJ = null; x.scotusVoteAt = -1;
        if (yes > 50 || tie) {
            x.court.add(j); x.justices++;
            g.log(NK_BIG, "SENATE CONFIRMS " + j.name.toUpperCase() + " TO THE SUPREME COURT, " + yes + "-" + no, null, 4);
            g.perf += 0.8; g.capital = Math.min(100, g.capital + 5);
            for (Bloc b : g.cong.blocs) if (b.party == g.party) b.rel = Math.min(100, b.rel + 3);
        } else {
            g.log(NK_BIG, "SENATE REJECTS " + j.name.toUpperCase() + " FOR THE SUPREME COURT, " + yes + "-" + no, null, 4);
            g.perf -= 1.5; g.capital = Math.max(0, g.capital - 8);
            Decisions.info(g, "Your Supreme Court nominee is rejected", j.name + " lost, " + yes + "-" + no + ". The seat is still open. Choose another nominee in the West Wing (the Courts tab).");
        }
    }

    static void decide(Game g, Case c) {
        Action a = act(g, c);
        if (a == null || a.status == A_REVOKED) { c.done = true; return; }
        Random r = g.rng;
        if (c.stage == CS_SCOTUS) {
            int yes = 0;
            for (Justice j : g.ex.court) if (r.nextDouble() < justiceP(g, j, a)) yes++;
            int n = g.ex.court.size();
            c.votesFor = yes; c.votesAgainst = n - yes;
            c.done = true; c.stage = CS_DONE;
            boolean win = yes * 2 > n;
            c.won = win;
            if (win) {
                if (a.status == A_BLOCKED) { a.status = A_LIVE; a.statusWeek = g.week; Actions.apply(g, a, true); }
                g.log(NK_BIG, "SUPREME COURT UPHOLDS " + a.name.toUpperCase() + ", " + yes + "-" + (n - yes), null, 3.2);
                g.perf += 0.6; g.capital = Math.min(100, g.capital + 4);
            } else {
                Actions.apply(g, a, false); a.status = A_STRUCK; a.statusWeek = g.week;
                g.log(NK_BIG, "SUPREME COURT STRIKES DOWN " + a.name.toUpperCase() + ", " + (n - yes) + "-" + yes, null, 3.2);
                g.perf -= 0.8; g.capital = Math.max(0, g.capital - 4);
            }
            return;
        }
        boolean against = r.nextDouble() < pBlock(g, c);
        String court = c.stage == CS_DISTRICT ? "A federal judge" : "An appeals court";
        if (against) {
            if (a.status == A_LIVE) { Actions.apply(g, a, false); a.status = A_BLOCKED; a.statusWeek = g.week; }
            c.blocked = true;
            g.log(NK_BAD, court + " blocks " + a.name.toLowerCase(), null, 1.8);
            g.perf -= 0.3;
            Decision d = Decisions.add(g, D_COURT, (c.stage == CS_DISTRICT ? "A judge blocks " : "An appeals court rules against ") + a.name.toLowerCase(),
                    c.plaintiff + " won " + (c.stage == CS_DISTRICT ? "an injunction in federal district court" : "in the court of appeals") + ". The order is on hold. Your lawyers put the odds of winning on appeal at about "
                            + Math.round(100 * (1 - (c.stage == CS_DISTRICT ? pBlockAt(g, c, CS_CIRCUIT) : 0.5))) + "%.",
                    new String[]{"Appeal", "Rewrite it to fix the legal problems", "Let it go", "Slow-walk compliance while you appeal"},
                    new String[]{"Counsel: \"We have good arguments. Take it up.\"", "Attorney general: \"Narrow it and we win next time.\"", "Chief of staff: \"It's not worth the oxygen.\"",
                            "Domestic policy adviser: \"Comply on paper and keep going. They'll have to come after us.\""});
            d.caseRef = c;
        } else {
            g.log(NK_GOOD, court + " lets " + a.name.toLowerCase() + " stand", null, 1.2);
            if (r.nextDouble() < (c.stage == CS_DISTRICT ? 0.6 : 0.5)) advance(g, c);
            else { c.done = true; c.won = true; }
        }
    }

    static double pBlockAt(Game g, Case c, int stage) { int s = c.stage; c.stage = stage; double p = pBlock(g, c); c.stage = s; return p; }

    static void advance(Game g, Case c) {
        c.stage++;
        c.blocked = false;
        if (c.stage == CS_SCOTUS) {
            // most cases end in the courts of appeals; big ones are decided by the end of June
            if (g.rng.nextDouble() > 0.5) { c.done = true; c.won = !c.blocked; c.stage = CS_DONE; return; }
            int[] d = dateOf(g.day());
            int june = dayOfDate(d[1] >= 9 ? d[0] + 1 : d[0], 5, 28);
            c.decideWeek = Math.max(g.week + 10, june / 7);
            g.log(NK_INFO, "The Supreme Court agrees to hear the case over " + act(g, c).name.toLowerCase(), null, 1.4);
        } else c.decideWeek = g.week + 8 + g.rng.nextInt(8);
    }

    public static void courtDecision(Game g, Case c, int k) {
        Action a = act(g, c);
        if (a == null) return;
        if (k == 0) advance(g, c);
        else if (k == 1) {
            c.done = true;
            a.status = A_REVOKED; a.statusWeek = g.week;
            Action b = new Action();
            b.def = a.def; b.kind = a.kind; b.custom = a.custom; b.area = a.area; b.lean = a.lean; b.scope = a.scope * 0.7;
            b.name = a.name + " (revised)"; b.support = a.support + 2; b.legal = a.legal * 0.55; b.issued = g.week; b.statusWeek = g.week; b.status = A_LIVE;
            g.ex.actions.add(b);
            g.log(NK_INFO, "The White House issues a narrower version of " + a.name.toLowerCase(), null, 1.0);
            Actions.takeEffect(g, b);
        } else if (k == 2) {
            c.done = true; a.status = A_STRUCK; a.statusWeek = g.week;
        } else {
            advance(g, c);
            g.ex.defiance++;
            Trouble.add(g, SK_DEFY, "Defying the courts", "Agencies are carrying on with " + a.name.toLowerCase() + " despite a court order. Judges threaten contempt.", 4.5, null, true);
            if (a.status == A_BLOCKED) { a.status = A_LIVE; Actions.apply(g, a, true); }
        }
    }
}
