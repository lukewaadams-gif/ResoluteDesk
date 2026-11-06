package resolutedesk.model;

import java.io.Serializable;

//  Ratings are the game's estimates from public records (elections won, money raised, debate and interview
//  reviews, approval ratings) as of autumn 2026. They are judgements for play, not facts.
/** Appearance for the stylised portrait. */
public final class Look implements Serializable {
    private static final long serialVersionUID = 1L;
    public boolean female;
    public int skin, hair, style, facial, glasses, suit, tie;
    public double gray, faceW = 1, jaw = 0.5, age = 0.5, brow = 0.5, smile = 0.5;
    public boolean earrings, lipstick;

    /** "F|M skin hair style gray facial glasses faceW jaw suit tie age smile [E][L]" */
    public static Look parse(String s) {
        String[] t = s.trim().split("\\s+");
        Look l = new Look();
        l.female = t[0].equals("F");
        l.skin = Integer.parseInt(t[1]); l.hair = Integer.parseInt(t[2]); l.style = Integer.parseInt(t[3]);
        l.gray = Double.parseDouble(t[4]); l.facial = Integer.parseInt(t[5]); l.glasses = Integer.parseInt(t[6]);
        l.faceW = Double.parseDouble(t[7]); l.jaw = Double.parseDouble(t[8]); l.suit = Integer.parseInt(t[9]); l.tie = Integer.parseInt(t[10]);
        l.age = Double.parseDouble(t[11]); l.smile = Double.parseDouble(t[12]);
        for (int i = 13; i < t.length; i++) { if (t[i].contains("E")) l.earrings = true; if (t[i].contains("L")) l.lipstick = true; }
        return l;
    }
}
