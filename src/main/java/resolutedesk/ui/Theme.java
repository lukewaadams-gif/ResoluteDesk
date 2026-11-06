package resolutedesk.ui;

import static resolutedesk.core.Num.*;
import static resolutedesk.model.Parties.*;

import java.awt.Color;

/** The design resolution, the colour palette and button styles. */
public final class Theme {
    private Theme() {
    }

    public static final int DW = 1600, DH = 900;      // design resolution; everything is scaled to the window

    public static final Color C_IND = new Color(0xf5, 0x9e, 0x0b);

    public static Color mix(Color a, Color b, double t) {
        t = clamp(t, 0, 1);
        return new Color((int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t),
                (int) (a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t));
    }

    public static Color alpha(Color c, double a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) clamp(a * 255, 0, 255));
    }

    public static Color shade(Color c, double f) {
        if (f < 1) return new Color((int) (c.getRed() * f), (int) (c.getGreen() * f), (int) (c.getBlue() * f), c.getAlpha());
        return mix(c, Color.white, f - 1);
    }

    public static Color rgb(int hex) { return new Color((hex >> 16) & 255, (hex >> 8) & 255, hex & 255); }

    // ---- palette: "war room" ---------------------------------------------------------------------------
    public static final Color C_BG = rgb(0x060a13);

    public static final Color C_BG2 = rgb(0x0b1220);

    public static final Color C_PANEL = new Color(13, 21, 36, 236);

    public static final Color C_PANEL2 = new Color(19, 30, 50, 240);

    public static final Color C_CARD = new Color(22, 34, 56, 245);

    public static final Color C_LINE = new Color(46, 66, 98, 200);

    public static final Color C_LINE2 = new Color(80, 110, 160, 220);

    public static final Color C_TEXT = rgb(0xe7edf7);

    public static final Color C_DIM = rgb(0x93a4bf);

    public static final Color C_FAINT = rgb(0x56688a);

    public static final Color C_GOLD = rgb(0xf2c14e);

    public static final Color C_ACCENT = rgb(0x7cc4ff);

    public static final Color C_DEM = rgb(0x2f7bf6);

    public static final Color C_DEM_L = rgb(0x79acff);

    public static final Color C_DEM_D = rgb(0x163d86);

    public static final Color C_REP = rgb(0xe5484d);

    public static final Color C_REP_L = rgb(0xff8a8d);

    public static final Color C_REP_D = rgb(0x8a1f26);

    public static final Color C_GREEN = rgb(0x2bd47d);

    public static final Color C_MONEY = rgb(0x1fbf6a);

    public static final Color C_THIRD = rgb(0xa78bfa);

    public static final Color C_AMBER = rgb(0xffb347);

    public static final Color C_BAD = rgb(0xff5d5d);

    public static final Color C_GOOD = rgb(0x45d483);

    public static final Color C_TEAL = rgb(0x2dd4bf);

    public static final Color C_STATE = rgb(0x2a3650);

    public static final Color C_STATE_EDGE = rgb(0x0a1020);

    public static Color partyColor(int p) {
        switch (p) {
            case DEM: return C_DEM;
            case REP: return C_REP;
            case GRN: return rgb(0x3fbf5f);
            case LBT: return rgb(0xf5c542);
            case FWD: return rgb(0x8b5cf6);
            case IND: return C_IND;
            case CUS: return rgb(0xec4899);
            default: return rgb(0x22c3d6);
        }
    }

    public static Color partyLight(int p) { return p == DEM ? C_DEM_L : p == REP ? C_REP_L : shade(partyColor(p), 1.3); }

    /** Diverging map colour for a D-minus-R margin in points (positive = Democratic). */
    public static Color marginColor(double m, double confidence) {
        double a = clamp(Math.abs(m) / 20.0, 0, 1);
        Color base = m >= 0 ? C_DEM : C_REP;
        Color lite = m >= 0 ? rgb(0x9cc2ff) : rgb(0xffb0b2);
        Color dark = m >= 0 ? rgb(0x0f3a8f) : rgb(0x8f1d22);
        Color c = a < 0.5 ? mix(lite, base, a * 2) : mix(base, dark, (a - 0.5) * 2);
        if (Math.abs(m) < 1.0) c = mix(rgb(0xb9a6d9), c, Math.abs(m));
        return mix(C_STATE, c, clamp(confidence, 0.15, 1));
    }

    public static final int B_NORM = 0, B_PRIMARY = 1, B_DANGER = 2, B_ON = 3, B_GHOST = 4, B_TAB = 5, B_TABON = 6, B_OFF = 7, B_GOLD = 8,
            B_DEM = 9, B_REP = 10, B_SUBTLE = 11;

    public static final int IC_UP = 0, IC_DOWN = 1, IC_CLOSE = 2, IC_LEFT = 3, IC_RIGHT = 4;

    public static final Color C_IVORY = rgb(0xf3ead7), C_WOOD = rgb(0x5a3520), C_NAVY = rgb(0x14254a);

    public static final Color[] AC_COL = {rgb(0x34415a), rgb(0x2f8f5a), rgb(0x2f8f5a), rgb(0x2f8f5a), rgb(0x2f6fe0), rgb(0x7c4dff), rgb(0xd4a52a), rgb(0x1fbf6a), rgb(0x5aa0d8), rgb(0xc8323a), rgb(0x2f8f5a), rgb(0x8a6a1a), rgb(0x55607a), rgb(0xb8452e), rgb(0x3d6b9e), rgb(0x1f7a8c)};

    //  Each week opens with the front page of The Capital Ledger: the biggest story, the rest, and the numbers.
    public static final Color C_PAPER = rgb(0xf4efe4), C_INK = rgb(0x1c1a17), C_INK2 = rgb(0x5a554c);

    public static final Color[] BLOC_COL = {rgb(0x1e4fb8), rgb(0x2f7bf6), rgb(0x79acff), rgb(0x8a1f26), rgb(0xe5484d), rgb(0xff8a8d),
            rgb(0x1e4fb8), rgb(0x2f7bf6), rgb(0x79acff), rgb(0x8a1f26), rgb(0xe5484d), rgb(0xff8a8d)};
}
