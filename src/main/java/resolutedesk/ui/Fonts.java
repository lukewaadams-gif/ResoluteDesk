package resolutedesk.ui;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

/** Typefaces and the type scale. */
public final class Fonts {
    private Fonts() {
    }

    public static final String SANS = pickFont(new String[]{"Inter", "Segoe UI", "Helvetica Neue", "SF Pro Text", "Roboto", "Noto Sans",
            "Open Sans", "Lato", "DejaVu Sans", "Liberation Sans", "Arial"}, "SansSerif");

    public static final String SERIF = pickFont(new String[]{"Georgia", "Playfair Display", "Merriweather", "Cambria", "DejaVu Serif",
            "Liberation Serif", "Times New Roman"}, "Serif");

    public static final String MONO = pickFont(new String[]{"JetBrains Mono", "Cascadia Mono", "Consolas", "Menlo", "SF Mono",
            "DejaVu Sans Mono", "Liberation Mono", "Courier New"}, "Monospaced");

    public static String pickFont(String[] want, String fallback) {
        try {
            String[] have = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
            for (String w : want) for (String h : have) if (h.equalsIgnoreCase(w)) return h;
        } catch (Throwable t) { /* headless or no fonts */ }
        return fallback;
    }

    public static final Font F_HERO = new Font(SERIF, Font.BOLD, 78);

    public static final Font F_DISPLAY = new Font(SERIF, Font.BOLD, 40);

    public static final Font F_SERIF = new Font(SERIF, Font.BOLD, 24);

    public static final Font F_SERIF_S = new Font(SERIF, Font.ITALIC, 15);

    public static final Font F_H1 = new Font(SANS, Font.BOLD, 24);

    public static final Font F_H2 = new Font(SANS, Font.BOLD, 17);

    public static final Font F_H3 = new Font(SANS, Font.BOLD, 13);

    public static final Font F_B = new Font(SANS, Font.PLAIN, 13);

    public static final Font F_BB = new Font(SANS, Font.BOLD, 13);

    public static final Font F_S = new Font(SANS, Font.PLAIN, 12);

    public static final Font F_SB = new Font(SANS, Font.BOLD, 12);

    public static final Font F_XS = new Font(SANS, Font.PLAIN, 11);

    public static final Font F_XSB = new Font(SANS, Font.BOLD, 11);

    public static final Font F_T = new Font(SANS, Font.PLAIN, 10);

    public static final Font F_TB = new Font(SANS, Font.BOLD, 10);

    public static final Font F_M = new Font(MONO, Font.PLAIN, 12);

    public static final Font F_MB = new Font(MONO, Font.BOLD, 12);

    public static final Font F_MS = new Font(MONO, Font.PLAIN, 10);

    public static final Font F_NUM = new Font(SANS, Font.BOLD, 21);

    public static final Font F_NUMXL = new Font(SANS, Font.BOLD, 34);
}
