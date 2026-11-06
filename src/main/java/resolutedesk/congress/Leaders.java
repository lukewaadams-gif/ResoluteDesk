package resolutedesk.congress;

import static resolutedesk.congress.CongressRules.*;
import static resolutedesk.model.Parties.*;

import java.util.Random;
import resolutedesk.model.Era;
import resolutedesk.model.Game;

/** New leaders after a change of control. */
public final class Leaders {
    public static void reshuffle(Game g, boolean house, boolean senate) {
        Congress c = g.cong;
        Random r = new Random(g.set.seed ^ g.week);
        Era e = g.era;
        if (house || senate) {
            for (int i = c.members.size() - 1; i >= 0; i--) {
                Member m = c.members.get(i);
                if ((m.role == RO_LEADER || m.role == RO_CHAIR) && (m.chamber == HOUSE && house || m.chamber == SENATE && senate)) c.members.remove(i);
            }
        }
        boolean dHouse = g.houseD >= 218, dSen = g.senateD > 50 || g.senateD == 50 && g.party == DEM;
        if (house) {
            Hill.leader(g, r, HOUSE, dHouse ? DEM : REP, "Speaker of the House", dHouse ? e.speakerD : e.speakerR);
            Hill.leader(g, r, HOUSE, dHouse ? REP : DEM, "House " + (dHouse ? "Republican" : "Democratic") + " leader", dHouse ? e.houseLeaderR : e.houseLeaderD);
            chairs(g, r, HOUSE, dHouse ? DEM : REP);
        }
        if (senate) {
            Hill.leader(g, r, SENATE, dSen ? DEM : REP, "Senate Majority Leader", dSen ? e.leaderD : e.leaderR);
            Hill.leader(g, r, SENATE, dSen ? REP : DEM, "Senate " + (dSen ? "Republican" : "Democratic") + " leader", dSen ? e.leaderR : e.leaderD);
            chairs(g, r, SENATE, dSen ? DEM : REP);
        }
        for (Member m : c.members) if (m.bloc >= c.blocs.size()) m.bloc = 0;
    }

    static void chairs(Game g, Random r, int ch, int maj) {
        String[] names = ch == HOUSE ? CHAIR_H : CHAIR_S;
        for (int k = 0; k < names.length; k++) {
            Member m = Hill.person(g.cong, r, maj, ch);
            m.role = RO_CHAIR; m.committee = names[k];
            m.title = "Chair, " + (ch == HOUSE ? "House " : "Senate ") + names[k];
            m.bloc = Hill.blocOf(g.cong, ch, maj, r.nextDouble() < 0.7 ? 1 : r.nextBoolean() ? 0 : 2);
            Hill.ideo(g.cong, m, r);
            m.rel = maj == g.party ? 55 : 35;
            g.cong.members.add(m);
        }
    }
}
