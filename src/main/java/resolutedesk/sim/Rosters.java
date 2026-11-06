package resolutedesk.sim;

import static resolutedesk.data.Candidates.*;
import static resolutedesk.data.HistoricalFigures.*;
import static resolutedesk.model.Parties.*;

import java.util.ArrayList;
import java.util.List;
import resolutedesk.model.Cand;
import resolutedesk.model.Era;
import resolutedesk.model.Game;

/** Who takes office in each era, and how many terms they have already served. */
public final class Rosters {
    private Rosters() {
    }

    public static final String TRUMP_ROLE25 = "45th President, elected again in 2024";

    /** The presidents you can pick for an era: the real winner first, then what-ifs. */
    public static List<Cand> eraRoster(Era e) {
        List<Cand> l = new ArrayList<Cand>();
        String row = rosterRow(e.year);
        Cand tr = candById("trump");
        if (tr != null) tr.role = TRUMP_ROLE25;
        if (row != null) {
            String[] t = row.split("\\|", -1);
            for (int p = 1; p <= 2; p++) for (String x : t[p].split(" ")) {
                if (x.isEmpty()) continue;
                Cand c = candById(x.split(">")[0]);
                if (c != null && !l.contains(c)) l.add(c);
            }
            if (t.length > 3 && !t[3].isEmpty()) for (String ov : t[3].split(";")) {
                int k = ov.indexOf('=');
                Cand c = candById(ov.substring(0, k));
                if (c != null) c.role = ov.substring(k + 1);
            }
            Cand w = candById(e.winnerId);
            if (w != null) { l.remove(w); l.add(0, w); }
            if (e.year == 2017 && tr != null) tr.role = "President-elect";
            return l;
        }
        Cand w = e.winnerId != null ? candById(e.winnerId) : null;
        if (w != null) l.add(w);
        for (Cand c : ROSTER) {
            if (c == w || c.party > REP) continue;
            if (e.year == 2029 && c.id.equals("trump")) continue;
            l.add(c);
        }
        return l;
    }

    /** The running mate the real winner had, or a sensible default. */
    public static String defaultVp(Era e, Cand p) {
        if (p == null) return null;
        String row = rosterRow(e.year);
        if (row != null) {
            String[] t = row.split("\\|", -1);
            for (int k = 1; k <= 2; k++) for (String x : t[k].split(" ")) if (x.startsWith(p.id + ">")) return x.substring(p.id.length() + 1);
            for (int k = 1; k <= 2; k++) for (String x : t[k].split(" ")) {
                String id = x.split(">")[0];
                Cand c = candById(id);
                if (c != null && c != p && c.party == p.party) return id;
            }
        }
        if (p.id.equals("trump")) return "vance";
        if (p.id.equals("harris")) return "walz";
        String[] d = {"shapiro", "kelly", "beshear", "whitmer", "moore", "warnock"};
        String[] r = {"rubio", "youngkin", "haley", "kemp", "sanders", "cruz"};
        for (String id : p.party == DEM ? d : r) if (!id.equals(p.id)) return id;
        return null;
    }

    /** How many terms the president has already won before this game began (the real winner's earlier terms). */
    public static int priorTerms(Game g) {
        if (g.set.custom != null || g.set.bgLine != null) return 0;
        if (g.pres != null && g.era.winnerId != null && g.pres.id.equals(g.era.winnerId)) return g.era.termOf - 1;
        return 0;
    }
}
