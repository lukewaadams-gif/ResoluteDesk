package resolutedesk.congress;

import static resolutedesk.congress.CongressHistory.*;
import static resolutedesk.congress.CongressRules.*;
import static resolutedesk.core.Dates.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.data.Faces.*;
import static resolutedesk.data.Names.*;
import static resolutedesk.data.StateData.*;
import static resolutedesk.executive.Posts.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.model.Attributes.*;
import static resolutedesk.model.Parties.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import resolutedesk.executive.Cabinet;
import resolutedesk.model.Era;
import resolutedesk.model.Game;

/** Setting up Congress for an era: seats, blocs, leaders and committee chairs. */
public final class Hill {
    /** Bloc templates: chamber, party, name, share of the party's seats, econ, cult, loyalty, deficit hawkishness. */
    static final int[] TB_CH = {HOUSE, HOUSE, HOUSE, HOUSE, HOUSE, HOUSE, SENATE, SENATE, SENATE, SENATE, SENATE, SENATE};
    static final int[] TB_PARTY = {DEM, DEM, DEM, REP, REP, REP, DEM, DEM, DEM, REP, REP, REP};
    static final String[] TB_NAME = {"Progressive Caucus", "Mainstream Democrats", "New Dems and Blue Dogs", "Freedom Caucus", "Mainstream Republicans", "Problem Solvers Republicans",
            "Progressive senators", "Mainstream Democrats", "Moderate Democrats", "Hardliners", "Mainstream Republicans", "Moderate Republicans"};
    static final double[] TB_SHARE = {0.45, 0.38, 0.17, 0.17, 0.70, 0.13, 0.25, 0.60, 0.15, 0.20, 0.68, 0.12};
    static final double[] TB_ECON = {-0.65, -0.4, -0.1, 0.8, 0.55, 0.25, -0.7, -0.4, -0.1, 0.8, 0.55, 0.2};
    static final double[] TB_CULT = {-0.7, -0.5, -0.25, 0.8, 0.55, 0.2, -0.7, -0.45, -0.2, 0.75, 0.5, 0.1};
    static final double[] TB_LOY = {0.8, 0.9, 0.75, 0.55, 0.85, 0.75, 0.85, 0.9, 0.7, 0.6, 0.85, 0.7};
    static final double[] TB_HAWK = {0.1, 0.3, 0.6, 0.9, 0.5, 0.4, 0.1, 0.3, 0.5, 0.85, 0.5, 0.4};

    public static void setup(Game g) {
        Congress c = new Congress();
        g.cong = c;
        Random r = new Random(g.set.seed ^ 0x5eedL);
        seats(g);
        // the leaders
        Era e = g.era;
        boolean dHouse = g.houseD >= 218, dSen = g.senateD > 50 || g.senateD == 50 && g.party == DEM;
        leader(g, r, HOUSE, dHouse ? DEM : REP, "Speaker of the House", dHouse ? e.speakerD : e.speakerR);
        leader(g, r, HOUSE, dHouse ? REP : DEM, "House " + (dHouse ? "Republican" : "Democratic") + " leader", dHouse ? e.houseLeaderR : e.houseLeaderD);
        leader(g, r, SENATE, dSen ? DEM : REP, "Senate Majority Leader", dSen ? e.leaderD : e.leaderR);
        leader(g, r, SENATE, dSen ? REP : DEM, "Senate " + (dSen ? "Republican" : "Democratic") + " leader", dSen ? e.leaderR : e.leaderD);
        // committee chairs from the majority
        for (int ch = 0; ch < 2; ch++) {
            int maj = ch == HOUSE ? (dHouse ? DEM : REP) : (dSen ? DEM : REP);
            String[] names = ch == HOUSE ? CHAIR_H : CHAIR_S;
            for (int k = 0; k < names.length; k++) {
                Member m = person(c, r, maj, ch);
                m.role = RO_CHAIR; m.committee = names[k];
                m.title = "Chair, " + (ch == HOUSE ? "House " : "Senate ") + names[k];
                m.bloc = blocOf(c, ch, maj, r.nextDouble() < 0.7 ? 1 : r.nextBoolean() ? 0 : 2);
                ideo(c, m, r);
                c.members.add(m);
            }
        }
        // swing votes: moderates of both parties, and a hardliner or two
        int[][] swing = {{HOUSE, g.party, 2}, {HOUSE, g.party, 2}, {HOUSE, other(g.party), 2}, {HOUSE, other(g.party), 2}, {HOUSE, g.party, 0},
                {SENATE, g.party, 2}, {SENATE, g.party, 2}, {SENATE, other(g.party), 2}, {SENATE, other(g.party), 2}, {SENATE, g.party, 0}};
        for (int[] sw : swing) {
            Member m = person(c, r, sw[1], sw[0]);
            m.role = RO_SWING;
            m.bloc = blocOf(c, sw[0], sw[1], sw[2]);
            m.price = r.nextInt(4);
            m.state = swingState(r);
            for (int t = 0; t < 20 && stateTaken(c, m); t++) m.state = swingState(r);
            m.title = (sw[0] == HOUSE ? "Rep. " : "Sen. ") + m.name.split(" ")[1] + " (" + PARTY_SHORT[sw[1]] + "-" + STATES[m.state].ab + ")";
            ideo(c, m, r);
            m.rel = 45 + r.nextDouble() * 15;
            c.members.add(m);
        }
        for (Bloc b : c.blocs) b.rel = b.party == g.party ? 58 : 38;
        c.fy = fiscalYear(g);
        Budget.setup(g);
    }

    public static int other(int p) { return p == REP ? DEM : REP; }

    /** One slot of Hill meetings: the leaders and the blocs warm to you, your own side more. */
    public static void meeting(Game g) {
        if (g.cong == null) return;
        double k = (g.pres != null ? g.pres.a[A_EST] : 60) / 70.0;
        for (Member m : g.cong.members) {
            if (m.role == RO_LEADER) m.rel = Math.min(100, m.rel + (m.party == g.party ? 1.6 : 0.9) * k);
            else if (m.role == RO_CHAIR || m.role == RO_SWING) m.rel = Math.min(100, m.rel + 0.4 * k);
        }
        for (Bloc b : g.cong.blocs) b.rel = Math.min(100, b.rel + (b.party == g.party ? 0.7 : 0.35) * k);
    }

    /** Relationships drift back toward where party and approval put them. */
    public static void drift(Game g) {
        double pull = (g.approval - 45) * 0.3 + (Cabinet.sq(g, S_LEG) - 0.55) * 12;
        for (Bloc b : g.cong.blocs) { double base = (b.party == g.party ? 55 : 35) + pull * (b.party == g.party ? 1 : 0.5); b.rel += (base - b.rel) * 0.02; }
        for (Member m : g.cong.members) { double base = (m.party == g.party ? 55 : 35) + pull * 0.5; m.rel += (base - m.rel) * 0.015; }
    }

    public static int swingState(Random r) {
        String[] sw = {"PA", "MI", "WI", "AZ", "NV", "GA", "NC", "NH", "ME", "VA", "MN", "OH", "IA", "TX", "FL", "CO", "NM"};
        return stateIdx(sw[r.nextInt(sw.length)]);
    }

    static Member person(Congress c, Random r, int party, int chamber) {
        Member m = new Member();
        boolean f = genFemale(r, 0.33);
        String last = genLast(r);
        for (int t = 0; t < 30 && lastUsed(c, last); t++) last = genLast(r);
        m.name = genFirst(r, f) + " " + last;
        m.party = party; m.chamber = chamber; m.state = r.nextInt(NS);
        m.face = face(m.name, r, party);
        if (m.face.look != null) { m.face.look.female = f; if (f && m.face.look.style < 6) m.face.look.style = 6 + r.nextInt(4); if (!f && m.face.look.style >= 6) m.face.look.style = r.nextInt(5); }
        return m;
    }

    static boolean stateTaken(Congress c, Member m) {
        for (Member o : c.members) if (o.role == RO_SWING && o.chamber == m.chamber && o.state == m.state) return true;
        return false;
    }

    static boolean lastUsed(Congress c, String last) {
        for (Member m : c.members) if (m.name.endsWith(" " + last)) return true;
        return false;
    }

    static void leader(Game g, Random r, int chamber, int party, String title, String name) {
        Member m = new Member();
        m.name = name; m.party = party; m.chamber = chamber; m.role = RO_LEADER; m.title = title;
        m.face = face(name, r, party);
        // the leaders of the past were men, until Nancy Pelosi
        boolean drawn = realLook(name) != null;
        for (String[] lk : LEADER_LOOKS) if (lk[0].equals(name)) drawn = true;
        if (!drawn && m.face.look != null) Cabinet.femaleLook(m.face.look, name.equals("Nancy Pelosi"), r);
        m.bloc = blocOf(g.cong, chamber, party, 1);
        ideo(g.cong, m, r);
        m.rel = party == g.party ? 62 : 35;
        g.cong.members.add(m);
    }

    static void ideo(Congress c, Member m, Random r) {
        Bloc b = c.blocs.get(m.bloc);
        m.econ = clamp(b.econ + r.nextGaussian() * 0.08, -1, 1);
        m.cult = clamp(b.cult + r.nextGaussian() * 0.08, -1, 1);
    }

    static int blocOf(Congress c, int chamber, int party, int k) {
        int n = 0;
        for (int i = 0; i < c.blocs.size(); i++) { Bloc b = c.blocs.get(i); if (b.chamber == chamber && b.party == party) { if (n == k) return i; n++; } }
        return 0;
    }

    /** Rebuild the blocs from the seat counts (after an election). */
    public static void seats(Game g) {
        Congress c = g.cong;
        List<Bloc> old = c.blocs;
        c.blocs = new ArrayList<Bloc>();
        String[] tier = tierFor(yearNow(g));
        for (int t = 0; t < TB_CH.length; t++) {
            Bloc b = new Bloc();
            b.chamber = TB_CH[t]; b.party = TB_PARTY[t]; b.name = TB_NAME[t];
            int total = b.chamber == HOUSE ? (b.party == DEM ? g.houseD : 435 - g.houseD) : (b.party == DEM ? g.senateD : 100 - g.senateD);
            b.seats = (int) Math.round(total * TB_SHARE[t]);
            b.econ = TB_ECON[t]; b.cult = TB_CULT[t]; b.loyalty = TB_LOY[t]; b.hawk = TB_HAWK[t];
            if (tier != null) {
                String[] p = tier[t].split("\\|");
                b.name = p[0]; b.seats = (int) Math.round(total * Double.parseDouble(p[1]));
                b.econ = Double.parseDouble(p[2]); b.cult = Double.parseDouble(p[3]); b.loyalty = Double.parseDouble(p[4]); b.hawk = Double.parseDouble(p[5]);
            }
            for (Bloc o : old) if (o.name.equals(b.name) && o.chamber == b.chamber) b.rel = o.rel;
            c.blocs.add(b);
        }
        // rounding: make each party's blocs add up
        for (int ch = 0; ch < 2; ch++) for (int p = DEM; p <= REP; p++) {
            int total = ch == HOUSE ? (p == DEM ? g.houseD : 435 - g.houseD) : (p == DEM ? g.senateD : 100 - g.senateD), sum = 0;
            Bloc big = null;
            for (Bloc b : c.blocs) if (b.chamber == ch && b.party == p) { sum += b.seats; if (big == null || b.seats > big.seats) big = b; }
            if (big != null) big.seats += total - sum;
        }
    }

    public static int fiscalYear(Game g) { int[] d = dateOf(g.day()); return d[1] >= 9 ? d[0] + 1 : d[0]; }

    public static boolean majority(Game g, int chamber, int party) {
        if (chamber == HOUSE) return party == DEM ? g.houseD >= 218 : 435 - g.houseD >= 218;
        int s = party == DEM ? g.senateD : 100 - g.senateD;
        return s > 50 || s == 50 && g.party == party;
    }

    static Member leaderOf(Game g, int chamber, boolean majority) {
        for (Member m : g.cong.members) if (m.role == RO_LEADER && m.chamber == chamber && majority(g, chamber, m.party) == majority) return m;
        return null;
    }

    public static Member chairFor(Game g, Bill b, int chamber) {
        int k = committeeFor(b.def(), chamber);
        String[] names = chamber == HOUSE ? CHAIR_H : CHAIR_S;
        for (Member m : g.cong.members) if (m.role == RO_CHAIR && m.chamber == chamber && sameCommittee(chamber, k, m.committee)) return m;
        return null;
    }
}
