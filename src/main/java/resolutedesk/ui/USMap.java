package resolutedesk.ui;

import static resolutedesk.data.StateData.*;
import static resolutedesk.ui.Draw.*;
import static resolutedesk.ui.Fonts.*;
import static resolutedesk.ui.MapData.*;
import static resolutedesk.ui.Theme.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import resolutedesk.ResoluteDesk;
import resolutedesk.model.Cand;

/** The map of the states, coloured by approval, margins or other measures. */
public final class USMap {
    static Path2D[] shape = new Path2D[NS];
    static Rectangle2D[] bounds = new Rectangle2D[NS];
    static volatile boolean ready;
    static Color[] cur = new Color[NS];
    static double[] hoverAmt = new double[NS];
    static int hoverState = -1;
    static final double AW = 975, AH = 610;

    static int dec(char c) { int v = c; if (v > 92) v--; return v - 35; }

    static int num(String s, int i, int n) { int v = 0; for (int k = 0; k < n; k++) v = v * 90 + dec(s.charAt(i + k)); return v; }

    public static synchronized void load() {
        if (ready) return;
        for (String[] row : SHAPES) {
            int s = stateIdx(row[0]);
            if (s < 0) continue;
            Path2D.Double p = new Path2D.Double(Path2D.WIND_EVEN_ODD);
            for (String ring : row[1].split("~")) {
                if (ring.length() < 6) continue;
                int x = num(ring, 0, 3), y = num(ring, 3, 3);
                p.moveTo((x - 400) / 4.0, (y - 200) / 4.0);
                int i = 6;
                while (i < ring.length()) {
                    if (ring.charAt(i) == '!') { x = num(ring, i + 1, 3); y = num(ring, i + 4, 3); i += 7; }
                    else { x += dec(ring.charAt(i)) - 44; y += dec(ring.charAt(i + 1)) - 44; i += 2; }
                    p.lineTo((x - 400) / 4.0, (y - 200) / 4.0);
                }
                p.closePath();
            }
            shape[s] = p;
            bounds[s] = p.getBounds2D();
        }
        for (int s = 0; s < NS; s++) cur[s] = C_STATE;
        ready = true;
    }

    public static void animate(double dt) {
        for (int s = 0; s < NS; s++) hoverAmt[s] += ((s == hoverState ? 1 : 0) - hoverAmt[s]) * Math.min(1, dt * 12);
    }

    /** Map geometry for a target rectangle: {scale, offsetX, offsetY}. Leaves room on the right for the callout boxes. */
    static double[] fit(double x, double y, double w, double h) {
        double s = Math.min((w - 84) / AW, h / AH);
        return new double[]{s, x + (w - 84 - AW * s) / 2, y + (h - AH * s) / 2};
    }

    static int stateAt(double[] f, double px, double py) {
        if (!ready) return -1;
        double ax = (px - f[1]) / f[0], ay = (py - f[2]) / f[0];
        for (int s = 0; s < NS; s++) if (shape[s] != null && bounds[s].contains(ax, ay) && shape[s].contains(ax, ay)) return s;
        for (int i = 0; i < CALLOUTS.length; i++) {
            double[] b = calloutBox(f, i);
            if (px >= b[0] && py >= b[1] && px < b[0] + b[2] && py < b[1] + b[3]) return stateIdx(CALLOUTS[i]);
        }
        return -1;
    }

    static double[] calloutBox(double[] f, int i) {
        double bx = f[1] + AW * f[0] + 8, by = f[2] + 95 * f[0] + i * 22;
        return new double[]{bx, by, 76, 19};
    }

    /** Per-state fill colour and label for a map mode, from the player's point of view. */
    /** What the map shows: colours and labels the screen sets before drawing. */
    static Color[] paint = new Color[NS];
    static String[] tags = new String[NS];
    static int markState = -1;           // where the president is
    static Cand markCand;

    /** Draws the map in a rectangle. Returns the fit used (for hit testing). */
    static double[] draw(ResoluteDesk u, Graphics2D g, double x, double y, double w, double h, int selected, boolean interactive) {
        double[] f = fit(x, y, w, h);
        if (!ready) { textC(g, "Loading map...", x + w / 2, y + h / 2, F_B, C_DIM); return f; }
        int hov = interactive && u.modal == null && u.popup == null ? stateAt(f, u.mx, u.my) : -1;
        if (interactive) hoverState = hov;
        String[] lab = new String[1];
        String[] labels = new String[NS];
        Color[] target = new Color[NS];
        for (int s = 0; s < NS; s++) {
            target[s] = paint[s] == null ? C_STATE : paint[s];
            labels[s] = tags[s];
            cur[s] = mix(cur[s] == null ? target[s] : cur[s], target[s], 0.18);
        }
        AffineTransform at = g.getTransform();
        // drop shadow
        g.translate(f[1], f[2]);
        g.scale(f[0], f[0]);
        g.translate(3, 5);
        g.setColor(new Color(0, 0, 0, 90));
        for (int s = 0; s < NS; s++) if (shape[s] != null) g.fill(shape[s]);
        g.translate(-3, -5);
        float edge = (float) (1.0 / f[0]);
        for (int s = 0; s < NS; s++) {
            if (shape[s] == null) continue;
            Color c = cur[s];
            if (hoverAmt[s] > 0.01) c = mix(c, Color.white, 0.18 * hoverAmt[s]);
            g.setColor(c);
            g.fill(shape[s]);
        }
        g.setColor(new Color(6, 10, 20, 230));
        g.setStroke(new BasicStroke(0.9f * edge, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int s = 0; s < NS; s++) if (shape[s] != null) g.draw(shape[s]);
        if (hov >= 0 && shape[hov] != null) {
            g.setColor(new Color(255, 255, 255, 200));
            g.setStroke(new BasicStroke(1.6f * edge, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(shape[hov]);
        }
        if (selected >= 0 && shape[selected] != null) {
            float pulse = (float) (0.75 + 0.25 * Math.sin(u.tsec * 4));
            g.setColor(new Color(242, 193, 78, (int) (255 * pulse)));
            g.setStroke(new BasicStroke(2.6f * edge, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(shape[selected]);
        }
        // candidate location marker
        g.setTransform(at);
        // labels
        for (int s = 0; s < NS; s++) {
            if (shape[s] == null || isCallout(s)) continue;
            double lx = f[1] + STATES[s].cx * f[0], ly = f[2] + STATES[s].cy * f[0];
            if (STATES[s].ab.equals("FL")) lx += 14 * f[0];
            if (STATES[s].ab.equals("MI")) { lx += 8 * f[0]; ly += 14 * f[0]; }
            if (STATES[s].ab.equals("LA")) lx -= 10 * f[0];
            if (STATES[s].ab.equals("HI")) { lx += 10 * f[0]; ly -= 4 * f[0]; }
            if (STATES[s].ab.equals("KY")) ly += 4 * f[0];
            Color bg = cur[s];
            Color tc = luminance(bg) > 0.55 ? rgb(0x0b1220) : Color.white;
            boolean big = bounds[s].getWidth() * f[0] > 40 && bounds[s].getHeight() * f[0] > 26;
            textC(g, STATES[s].ab, lx, ly + (labels[s] != null && big ? -1 : 4), F_TB, alpha(tc, 0.92));
            if (labels[s] != null && big) textC(g, labels[s], lx, ly + 10, F_T, alpha(tc, 0.85));
        }
        // callouts for the small north-eastern states
        for (int i = 0; i < CALLOUTS.length; i++) {
            int s = stateIdx(CALLOUTS[i]);
            double[] b = calloutBox(f, i);
            Color c = cur[s];
            if (hov == s) c = mix(c, Color.white, 0.18);
            RoundRectangle2D rr = new RoundRectangle2D.Double(b[0], b[1], b[2], b[3], 6, 6);
            g.setColor(c);
            g.fill(rr);
            g.setColor(s == selected ? C_GOLD : new Color(255, 255, 255, 40));
            g.setStroke(new BasicStroke(s == selected ? 2f : 1f));
            g.draw(rr);
            Color tc = luminance(c) > 0.55 ? rgb(0x0b1220) : Color.white;
            text(g, CALLOUTS[i], b[0] + 5, b[1] + 13.5, F_TB, tc);
            if (labels[s] != null) textR(g, labels[s].length() > 7 ? labels[s].substring(0, 7) : labels[s], b[0] + b[2] - 4, b[1] + 13.5, F_T, alpha(tc, 0.85));
            // leader line to the state
            double sx = f[1] + STATES[s].cx * f[0], sy = f[2] + STATES[s].cy * f[0];
            g.setColor(new Color(255, 255, 255, 40));
            g.setStroke(new BasicStroke(0.8f));
            g.draw(new Line2D.Double(b[0], b[1] + b[3] / 2, sx, sy));
            g.fill(new Ellipse2D.Double(sx - 1.5, sy - 1.5, 3, 3));
        }
        g.setStroke(new BasicStroke(1f));
        // the candidate's whereabouts
        if (markCand != null && markState >= 0 && markState < NS) {
            double lx = f[1] + STATES[markState].cx * f[0], ly = f[2] + STATES[markState].cy * f[0] - 18;
            double bob = Math.sin(u.tsec * 3) * 2;
            g.setColor(new Color(0, 0, 0, 120));
            g.fill(new Ellipse2D.Double(lx - 6, ly + 17, 12, 4));
            Portrait.avatar(g, markCand, lx, ly + bob, 10, C_GOLD);
        }
        return f;
    }

    static boolean isCallout(int s) { for (String c : CALLOUTS) if (STATES[s].ab.equals(c)) return true; return false; }

    static double luminance(Color c) { return (0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue()) / 255.0; }

    /** A colour scale with labels at both ends. */
    static void scale(Graphics2D g, double x, double y, Color lo, Color mid, Color hi, String a, String b, String note) {
        for (int i = 0; i < 9; i++) { double t = i / 8.0; g.setColor(t < 0.5 ? mix(lo, mid, t * 2) : mix(mid, hi, (t - 0.5) * 2)); g.fill(new Rectangle2D.Double(x + i * 20, y, 20, 10)); }
        text(g, a, x, y + 22, F_T, C_DIM); textR(g, b, x + 180, y + 22, F_T, C_DIM);
        if (note != null) text(g, note, x + 196, y + 10, F_T, C_FAINT);
    }

    static void gradientBar(Graphics2D g, double x, double y, double w, Color a, Color b) {
        g.setPaint(new GradientPaint((float) x, 0, a, (float) (x + w), 0, b));
        g.fill(new RoundRectangle2D.Double(x, y, w, 10, 5, 5));
    }
}
