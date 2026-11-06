package resolutedesk.data;

import java.util.Random;
import resolutedesk.model.Look;

/** Made-up names for officials, reporters and members of Congress, fitted to the era. */
public final class Names {
    private Names() {
    }

    // ---- people the game makes up should look like their time ------------------------------------------------
    public static int GEN_YEAR = 2025;

    public static final String[] FIRST_M_OLD = {"John", "William", "James", "Charles", "George", "Frank", "Joseph", "Edward", "Henry", "Robert", "Harold", "Walter", "Arthur", "Clarence",
            "Raymond", "Paul", "Fred", "Herbert", "Lloyd", "Earl", "Howard", "Ralph", "Louis", "Carl", "Thomas", "Richard", "Donald", "Kenneth"};

    public static final String[] FIRST_F_OLD = {"Mary", "Dorothy", "Helen", "Margaret", "Ruth", "Frances", "Florence", "Edith", "Mildred", "Alice", "Eleanor", "Marian", "Barbara", "Betty", "Jean", "Martha"};

    /** A woman, with the chance scaled to how often women held such jobs in the year. */
    public static boolean genFemale(Random r, double modern) {
        int y = GEN_YEAR;
        double k = y < 1950 ? 0.06 : y < 1970 ? 0.12 : y < 1990 ? 0.35 : y < 2010 ? 0.7 : 1;
        return r.nextDouble() < modern * k;
    }

    public static String genFirst(Random r, boolean f) {
        if (GEN_YEAR < 1975) return f ? FIRST_F_OLD[r.nextInt(FIRST_F_OLD.length)] : FIRST_M_OLD[r.nextInt(FIRST_M_OLD.length)];
        return f ? FIRST_F[r.nextInt(FIRST_F.length)] : FIRST_M[r.nextInt(FIRST_M.length)];
    }

    public static final String[] MODERN_LASTS = {"Okafor", "Delgado", "Nakamura", "Oyelaran", "Vasquez", "Ruiz", "Castellano", "Sandoval", "Vance-Hill"};

    public static final String[] OLD_LASTS = {"Callahan", "Dunmore", "Hollister", "Whitcomb", "Ainsworth", "Rutledge", "Carrington", "Sherwood", "Vance"};

    public static final String[] MID_LASTS = {"Callahan", "Delgado", "Hollister", "Whitcomb", "Vasquez", "Rutledge", "Carrington", "Sandoval", "Vance"};

    /** A made-up surname: today's mix, or for earlier decades one that fits the Washington of the day. */
    public static String genLast(Random r) {
        String s = LASTS[r.nextInt(LASTS.length)];
        if (GEN_YEAR >= 1990) return s;
        for (int i = 0; i < MODERN_LASTS.length; i++) if (MODERN_LASTS[i].equals(s)) return (GEN_YEAR < 1965 ? OLD_LASTS : MID_LASTS)[i];
        return s;
    }

    /** Officials of earlier decades were overwhelmingly white; a made-up face follows the year. */
    public static void genSkin(Look l, Random r) {
        int y = GEN_YEAR;
        if (y >= 2010) return;
        double keep = y < 1965 ? 0.04 : y < 1990 ? 0.15 : y < 2010 ? 0.6 : 1;
        if (l.skin > 2 && r.nextDouble() > keep) l.skin = r.nextInt(3);
        if (y < 1975 && r.nextDouble() < 0.6) { l.glasses = r.nextInt(3) == 0 ? 1 : 0; if (!l.female) l.facial = r.nextInt(6) == 0 ? 4 : 0; }
    }

    public static final String[] FIRST_M = {"James", "Robert", "Michael", "David", "William", "Thomas", "Daniel", "Mark", "Steven", "Brian", "Kevin", "Jason", "Eric", "Greg", "Carlos", "Andre", "Luis", "Raj", "Tom", "Ben", "Sam", "Nick", "Paul", "Doug", "Wes", "Cole", "Ray", "Tim"};

    public static final String[] FIRST_F = {"Mary", "Patricia", "Jennifer", "Linda", "Elizabeth", "Susan", "Karen", "Lisa", "Nancy", "Sandra", "Ashley", "Michelle", "Maria", "Rosa", "Diane", "Angela", "Priya", "Grace", "Hannah", "Claire", "Tasha", "Joy", "Erin", "Lauren"};

    public static final String[] LASTS = {"Whitaker", "Calloway", "Brennan", "Okafor", "Delgado", "Hargrove", "Lindqvist", "Marsh", "Pruitt", "Vasquez", "Thornton", "Kowalski", "Ashby", "Mercer", "Haldane",
            "Ruiz", "Sutherland", "Pennington", "Castellano", "Whitfield", "Abernathy", "Fairbanks", "Nakamura", "Oyelaran", "Quinlan", "Rourke", "Stanton", "Talbot", "Underwood", "Vance-Hill",
            "Waller", "Yates", "Ellison", "Garrity", "Holloway", "Iverson", "Jardine", "Kilgore", "Langford", "Montague", "Norwood", "Ostrander", "Pemberton", "Radcliffe", "Sandoval", "Tillman", "Voss", "Winslow"};
}
