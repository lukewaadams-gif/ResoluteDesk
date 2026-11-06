package resolutedesk.core;

/** Formatting numbers and money for display. */
public final class Fmt {
    private Fmt() {
    }

    public static String fmt1(double v) { return String.format("%.1f", v); }

    public static String fmt0(double v) { return String.valueOf(Math.round(v)); }

    public static String pct0(double v) { return Math.round(v * 100) + "%"; }

    public static String pct1(double v) { return String.format("%.1f%%", v * 100); }

    public static String signed1(double v) { if (Math.abs(v) < 0.05) return "0.0"; return (v >= 0 ? "+" : "") + String.format("%.1f", v); }

    /** A two-party margin in points, written D+3.2 / R+1.5. */
    public static String margin(double dMinusR) {
        if (Math.abs(dMinusR) < 0.05) return "TIED";
        return (dMinusR > 0 ? "D+" : "R+") + String.format("%.1f", Math.abs(dMinusR));
    }

    public static String thousands(long v) {
        String s = String.valueOf(Math.abs(v));
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (i > 0 && (s.length() - i) % 3 == 0) b.append(',');
            b.append(s.charAt(i));
        }
        return (v < 0 ? "-" : "") + b;
    }

    /** Money in dollars, compact: $950K, $12.4M, $1.21B. */
    public static String money(double d) {
        double a = Math.abs(d);
        String s;
        if (a >= 1e9) s = String.format("$%.2fB", a / 1e9);
        else if (a >= 1e7) s = String.format("$%.1fM", a / 1e6);
        else if (a >= 1e6) s = String.format("$%.2fM", a / 1e6);
        else if (a >= 1e3) s = String.format("$%.0fK", a / 1e3);
        else s = String.format("$%.0f", a);
        return (d < 0 ? "-" : "") + s;
    }

    public static String compact(double v) {
        double a = Math.abs(v);
        if (a >= 1e6) return String.format("%.1fM", v / 1e6);
        if (a >= 1e4) return String.format("%.0fK", v / 1e3);
        if (a >= 1e3) return String.format("%.1fK", v / 1e3);
        return String.valueOf(Math.round(v));
    }

    public static String fmt2(double v) { return String.format("%.2f", v); }

    public static String commas(long v) {
        String s = String.valueOf(Math.abs(v));
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) { if (i > 0 && (s.length() - i) % 3 == 0) b.append(','); b.append(s.charAt(i)); }
        return (v < 0 ? "-" : "") + b;
    }
}
