package resolutedesk.core;

/** Small numeric helpers: clamping, easing, interpolation and the logistic curve. */
public final class Num {
    private Num() {
    }

    public static double clamp(double v, double lo, double hi) { return v < lo ? lo : v > hi ? hi : v; }

    public static int clampi(int v, int lo, int hi) { return v < lo ? lo : v > hi ? hi : v; }

    public static double ease(double t) { t = clamp(t, 0, 1); return t * t * (3 - 2 * t); }

    public static double easeOut(double t) { t = clamp(t, 0, 1); return 1 - (1 - t) * (1 - t) * (1 - t); }

    public static double lerp(double a, double b, double t) { return a + (b - a) * t; }

    public static double logistic(double x) { return 1 / (1 + Math.exp(-x)); }

    public static double logit(double p) { p = clamp(p, 1e-6, 1 - 1e-6); return Math.log(p / (1 - p)); }
}
