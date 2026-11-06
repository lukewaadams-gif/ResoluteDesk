package resolutedesk.ui;

import static resolutedesk.ui.Theme.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * The painter's toolbox: light and shadow, textures and a cache. Rooms are painted in code from these, once, into an
 * image at the screen's resolution, so the detail costs nothing frame to frame.
 */
public final class Art {
    private Art() {
    }

    private static final Map<String, BufferedImage> CACHE = new HashMap<String, BufferedImage>();

    /** Device pixels per design unit, from the graphics' transform. */
    static double scaleOf(Graphics2D g) {
        AffineTransform t = g.getTransform();
        double s = Math.sqrt(t.getScaleX() * t.getScaleX() + t.getShearY() * t.getShearY());
        return s <= 0 ? 1 : Math.min(3, s);
    }

    /**
     * Draws a painted layer, cached as an image at the screen's resolution. The key names what's in it; the layer is
     * repainted only when the key or the resolution changes. The rectangle is in whole design units.
     */
    public static void layer(Graphics2D g, String key, int x, int y, int w, int h, Painter p) {
        double s = Math.ceil(scaleOf(g) * 2) / 2;
        String k = key + "@" + s;
        BufferedImage img = CACHE.get(k);
        if (img == null) {
            if (CACHE.size() > 40) CACHE.clear();
            img = new BufferedImage((int) Math.ceil(w * s), (int) Math.ceil(h * s), BufferedImage.TYPE_INT_ARGB);
            Graphics2D b = img.createGraphics();
            smooth(b);
            b.scale(s, s);
            b.translate(-x, -y);
            p.paint(b, x, y, w, h);
            b.dispose();
            CACHE.put(k, img);
        }
        g.drawImage(img, x, y, w, h, null);
    }

    static void smooth(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
    }

    // ---- gradients ----

    /** A linear gradient with any number of stops. */
    public static LinearGradientPaint lin(double x1, double y1, double x2, double y2, float[] stops, Color[] colors) {
        return new LinearGradientPaint((float) x1, (float) y1, (float) x2, (float) y2, stops, colors);
    }

    /** A vertical gradient across a rectangle, filled. */
    public static void vfill(Graphics2D g, double x, double y, double w, double h, Color top, Color bottom) {
        g.setPaint(lin(x, y, x, y + h, new float[]{0f, 1f}, new Color[]{top, bottom}));
        g.fill(new Rectangle2D.Double(x, y, w, h));
    }

    /** A soft pool of light (or shade): an ellipse that fades from the color at its center to nothing at its edge. */
    public static void glow(Graphics2D g, double cx, double cy, double rx, double ry, Color c, double strength) {
        if (rx <= 0 || ry <= 0) return;
        AffineTransform at = g.getTransform();
        g.translate(cx, cy);
        g.scale(1, ry / rx);
        g.setPaint(new RadialGradientPaint(new Point2D.Double(0, 0), (float) rx, new float[]{0f, 0.45f, 1f},
                new Color[]{alpha(c, strength), alpha(c, strength * 0.45), alpha(c, 0)}));
        g.fill(new Ellipse2D.Double(-rx, -rx, rx * 2, rx * 2));
        g.setTransform(at);
    }

    /** Darkens the edges of a rectangle, as a lens does. */
    public static void vignette(Graphics2D g, double x, double y, double w, double h, double strength) {
        AffineTransform at = g.getTransform();
        double cx = x + w / 2, cy = y + h / 2, r = w * 0.62;
        g.translate(cx, cy);
        g.scale(1, h / w * 1.15);
        g.setPaint(new RadialGradientPaint(new Point2D.Double(0, 0), (float) r, new float[]{0f, 0.62f, 1f},
                new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), alpha(Color.black, strength)}));
        g.fill(new Rectangle2D.Double(-w, -h * 2, w * 2, h * 4));
        g.setTransform(at);
    }

    // ---- shadows ----

    /** A soft shadow around and under a shape: strokes of falling strength approximate a blur. */
    public static void shadow(Graphics2D g, Shape s, double spread, double strength) {
        int n = 7;
        for (int i = n; i >= 1; i--) {
            g.setStroke(new BasicStroke((float) (spread * 2 * i / n), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(alpha(Color.black, strength / n));
            g.draw(s);
        }
        g.setColor(alpha(Color.black, strength / n * 2));
        g.fill(s);
        g.setStroke(new BasicStroke(1f));
    }

    /** A shadow cast on the floor by an object: a flattened, offset, soft ellipse. */
    public static void floorShadow(Graphics2D g, double cx, double cy, double w, double h, double strength) {
        glow(g, cx, cy, w / 2, h / 2, Color.black, strength);
    }

    // ---- edges and mouldings ----

    /** Light along the top and left of a rectangle and shade along the bottom and right: a raised edge. */
    public static void bevel(Graphics2D g, double x, double y, double w, double h, double hi, double lo) {
        g.setStroke(new BasicStroke(1.2f));
        g.setColor(alpha(Color.white, hi));
        Path2D p = new Path2D.Double();
        p.moveTo(x, y + h); p.lineTo(x, y); p.lineTo(x + w, y);
        g.draw(p);
        g.setColor(alpha(Color.black, lo));
        Path2D q = new Path2D.Double();
        q.moveTo(x + w, y); q.lineTo(x + w, y + h); q.lineTo(x, y + h);
        g.draw(q);
        g.setStroke(new BasicStroke(1f));
    }

    /** The same edge turned inward: a recessed panel. */
    public static void inset(Graphics2D g, double x, double y, double w, double h, double hi, double lo) {
        g.setStroke(new BasicStroke(1.2f));
        g.setColor(alpha(Color.black, lo));
        Path2D p = new Path2D.Double();
        p.moveTo(x, y + h); p.lineTo(x, y); p.lineTo(x + w, y);
        g.draw(p);
        g.setColor(alpha(Color.white, hi));
        Path2D q = new Path2D.Double();
        q.moveTo(x + w, y); q.lineTo(x + w, y + h); q.lineTo(x, y + h);
        g.draw(q);
        g.setStroke(new BasicStroke(1f));
    }

    // ---- textures ----

    /** A fine speckle over a rectangle: plaster, paper, rug pile, canvas. */
    public static void speckle(Graphics2D g, double x, double y, double w, double h, long seed, int count, Color light, Color dark) {
        Random r = new Random(seed);
        for (int i = 0; i < count; i++) {
            double px = x + r.nextDouble() * w, py = y + r.nextDouble() * h, sz = 0.6 + r.nextDouble() * 1.4;
            g.setColor(r.nextBoolean() ? light : dark);
            g.fill(new Rectangle2D.Double(px, py, sz, sz));
        }
    }

    /** Wood grain: long wavering lines along a board. Horizontal if wide, vertical if tall. */
    public static void grain(Graphics2D g, double x, double y, double w, double h, long seed, int lines, Color dark, Color light) {
        Random r = new Random(seed);
        boolean across = w >= h;
        double len = across ? w : h, depth = across ? h : w;
        g.setStroke(new BasicStroke(0.8f));
        for (int i = 0; i < lines; i++) {
            double off = r.nextDouble() * depth, amp = 0.6 + r.nextDouble() * 2.2, freq = 0.006 + r.nextDouble() * 0.02, ph = r.nextDouble() * 6.3;
            Path2D p = new Path2D.Double();
            for (int k = 0; k <= 24; k++) {
                double a = len * k / 24.0, b = off + Math.sin(a * freq + ph) * amp + Math.sin(a * freq * 3.1 + ph * 2) * amp * 0.3;
                b = Math.max(0, Math.min(depth, b));
                double px = across ? x + a : x + b, py = across ? y + b : y + a;
                if (k == 0) p.moveTo(px, py); else p.lineTo(px, py);
            }
            g.setColor(r.nextDouble() < 0.7 ? dark : light);
            g.draw(p);
        }
        g.setStroke(new BasicStroke(1f));
    }

    /** Thin stripes, as on wallpaper or silk. */
    public static void stripes(Graphics2D g, double x, double y, double w, double h, double gap, Color c) {
        g.setColor(c);
        for (double sx = x; sx < x + w; sx += gap) g.fill(new Rectangle2D.Double(sx, y, gap * 0.35, h));
    }

    /** A gloss: a soft white streak across a surface, for polish and glass. */
    public static void gloss(Graphics2D g, double x, double y, double w, double h, double strength) {
        g.setPaint(lin(x, y, x + w, y + h, new float[]{0f, 0.45f, 0.55f, 1f},
                new Color[]{alpha(Color.white, 0), alpha(Color.white, strength), alpha(Color.white, strength * 0.6), alpha(Color.white, 0)}));
        g.fill(new Rectangle2D.Double(x, y, w, h));
    }
}
