package resolutedesk.data;

import static resolutedesk.data.Names.*;

import java.util.Random;
import resolutedesk.model.Cand;
import resolutedesk.model.Look;
import resolutedesk.ui.Creator;

/** Drawn looks for real leaders, and faces for everyone else. */
public final class Faces {
    private Faces() {
    }

    /** Real leaders: faces for the portraits. */
    public static final String[][] LEADER_LOOKS = {
            {"Mike Johnson", "M 1 2 2 0.1 0 1 0.95 0.5 0 1 0.45 0.6"}, {"Hakeem Jeffries", "M 6 0 0 0 0 1 1.0 0.55 0 0 0.45 0.5"},
            {"John Thune", "M 1 6 2 0.8 0 0 0.95 0.6 0 2 0.65 0.55"}, {"Chuck Schumer", "M 1 6 1 0.9 0 1 1.05 0.5 0 0 0.8 0.4"}};

    /** Deliberate stylised looks for the real people in the 2025 government and on the Supreme Court. */
    public static final String[] REAL_LOOKS = {
            "John Roberts|M 1 6 2 0.7 0 0 1.0 0.5 2 9 0.7 0.5", "Clarence Thomas|M 6 6 0 0.9 0 0 1.05 0.6 2 9 0.8 0.45", "Samuel Alito|M 1 6 1 0.8 0 1 1.0 0.5 2 9 0.75 0.4",
            "Sonia Sotomayor|F 2 2 6 0.1 0 0 1.02 0.5 2 9 0.72 0.65 E", "Elena Kagan|F 1 6 6 0.5 0 0 1.05 0.5 2 9 0.65 0.6", "Neil Gorsuch|M 0 6 2 0.85 0 0 1.0 0.5 2 9 0.6 0.55",
            "Brett Kavanaugh|M 1 2 2 0.3 0 0 1.0 0.5 2 9 0.6 0.55", "Amy Coney Barrett|F 0 1 7 0 0 0 0.98 0.45 2 9 0.5 0.6 E", "Ketanji Brown Jackson|F 6 0 7 0 0 0 1.0 0.5 2 9 0.55 0.7 E",
            "Scott Bessent|M 0 7 2 0.95 0 0 1.0 0.5 0 2 0.6 0.5", "Pete Hegseth|M 1 3 2 0 0 0 1.0 0.55 0 1 0.4 0.6", "Pam Bondi|F 0 5 7 0 0 0 1.0 0.45 0 0 0.5 0.65 E L",
            "Doug Burgum|M 1 6 4 0.8 0 0 1.0 0.5 0 0 0.65 0.5", "Brooke Rollins|F 0 3 7 0 0 0 0.98 0.45 0 0 0.45 0.7 E", "Howard Lutnick|M 1 6 1 0.7 0 1 1.0 0.5 2 2 0.6 0.55",
            "Lori Chavez-DeRemer|F 1 1 7 0 0 0 1.0 0.45 0 1 0.5 0.7 E", "Scott Turner|M 6 0 0 0.1 0 0 1.0 0.55 0 1 0.5 0.65", "Sean Duffy|M 1 2 1 0 0 0 1.0 0.55 0 1 0.45 0.6",
            "Chris Wright|M 1 6 2 0.6 0 0 1.0 0.5 0 0 0.6 0.55", "Linda McMahon|F 0 5 6 0.2 0 0 1.0 0.45 0 0 0.75 0.6 E", "Doug Collins|M 1 6 2 0.7 0 0 1.0 0.55 0 1 0.55 0.55",
            "Kristi Noem|F 0 1 8 0 0 0 0.98 0.45 0 0 0.5 0.6 E L", "Lee Zeldin|M 1 1 2 0 0 0 1.0 0.5 0 0 0.45 0.6", "Russ Vought|M 1 2 2 0.2 0 1 1.0 0.5 0 2 0.5 0.5",
            "Jamieson Greer|M 0 2 1 0 0 0 1.0 0.5 0 2 0.4 0.55", "Elise Stefanik|F 0 3 7 0 0 0 1.0 0.45 0 1 0.35 0.65 E", "Tulsi Gabbard|F 3 6 8 0.9 0 0 1.0 0.45 0 0 0.4 0.6 E",
            "John Ratcliffe|M 1 1 2 0.3 0 0 1.0 0.5 0 2 0.55 0.55", "Susie Wiles|F 0 7 6 0.95 0 0 1.0 0.45 0 0 0.75 0.55 E", "Mike Waltz|M 1 6 2 0.5 0 0 1.0 0.5 0 1 0.5 0.55",
            "Karoline Leavitt|F 0 5 7 0 0 0 0.96 0.42 0 1 0.25 0.7 E L", "David Warrington|M 1 2 2 0.2 0 1 1.0 0.5 0 2 0.5 0.5", "Kevin Hassett|M 1 6 4 0.6 0 1 1.05 0.5 0 0 0.6 0.55",
            "Vince Haley|M 1 2 2 0.3 0 0 1.0 0.5 0 2 0.5 0.5", "James Braid|M 1 2 1 0.2 0 0 1.0 0.5 0 0 0.45 0.55", "James Blair|M 1 1 1 0 0 0 1.0 0.5 0 1 0.35 0.55",
            "Steven Cheung|M 2 0 1 0 0 0 1.0 0.5 0 1 0.4 0.55"};

    public static String realLook(String name) {
        for (String s : REAL_LOOKS) { int i = s.indexOf('|'); if (s.substring(0, i).equals(name)) return s.substring(i + 1); }
        return null;
    }

    public static Cand face(String name, Random r, int party) {
        Cand c = new Cand();
        String[] p = name.split(" ", 2);
        c.first = p[0]; c.last = p.length > 1 ? p[1] : ""; c.party = party; c.id = "m_" + name.replace(' ', '_');
        Look l = null;
        for (String[] lk : LEADER_LOOKS) if (lk[0].equals(name)) l = Look.parse(lk[1]);
        if (l == null && realLook(name) != null) l = Look.parse(realLook(name));
        if (l == null) { l = Creator.randomLook(r); genSkin(l, r); }
        c.look = l;
        return c;
    }
}
