package resolutedesk.ui;

import static resolutedesk.core.Num.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.ui.Theme.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import resolutedesk.model.Cand;
import resolutedesk.model.Look;

/** Draws a person's portrait from their look. */
public final class Portrait {
    static final Color[] SKIN = {rgb(0xf4d7c3), rgb(0xebc3a3), rgb(0xd9a882), rgb(0xc58c63), rgb(0xa76d4a), rgb(0x7e4f34), rgb(0x5f3a26), rgb(0x45281a)};
    static final Color[] HAIR = {rgb(0x17120f), rgb(0x3a281d), rgb(0x5b3d29), rgb(0x7b5a3b), rgb(0xa58358), rgb(0xd8b46c), rgb(0x9c9a98), rgb(0xe8e6e2)};
    static final Color[] SUIT = {rgb(0x1d2a4a), rgb(0x34383f), rgb(0x16171b), rgb(0x9cbbe0), rgb(0x4a4038), rgb(0x2b3b5c)};
    static final Color[] TIE = {rgb(0x2f6fe0), rgb(0xc8323a), rgb(0x203a72), rgb(0xd4a52a), rgb(0x6a3fa0), rgb(0x2f8f5a), rgb(0x8a1f2a), rgb(0x5aa0d8), rgb(0x8a8f99), null};
    static final Color[] BLOUSE = {rgb(0xf2ece2), rgb(0xc8323a), rgb(0x2f6fe0), rgb(0x1f7a6a), rgb(0x6a3fa0), rgb(0xe7b9a3), rgb(0x16171b), rgb(0xd4a52a), rgb(0x9cbbe0), rgb(0xf2ece2)};
    static final Color[] IRIS = {rgb(0x4a2f1e), rgb(0x3b6ea8), rgb(0x5e7a3d), rgb(0x2a1d14)};
    static final Map<String, BufferedImage> cache = new HashMap<String, BufferedImage>();

    static Color partyBg(int p) {
        switch (p) {
            case DEM: return rgb(0x1f4fae);
            case REP: return rgb(0xb12a33);
            case GRN: return rgb(0x2a8c4a);
            case LBT: return rgb(0xb88a1d);
            case FWD: return rgb(0x6a3fbf);
            case IND: return rgb(0x9a5a0b);
            default: return rgb(0x14899a);
        }
    }

    /** Draws a portrait. still = cached, unanimated; framed = with party card background. talk 0..1 animates speech. */
    public static void draw(Graphics2D g, Cand c, double x, double y, double w, double h, double t, double talk, boolean still, boolean framed) {
        if (c == null || c.look == null) return;
        if (still) {
            int pw = (int) Math.max(8, Math.round(w * 2)), ph = (int) Math.max(8, Math.round(h * 2));
            String key = c.id + "|" + c.look.hashCode() + "|" + pw + "x" + ph + (framed ? "f" : "");
            BufferedImage img = cache.get(key);
            if (img == null) {
                if (cache.size() > 400) cache.clear();
                img = new BufferedImage(pw, ph, BufferedImage.TYPE_INT_ARGB);
                Graphics2D b = img.createGraphics();
                hints(b);
                paint(b, c, 0, 0, pw, ph, 0.7, 0, framed, true);
                b.dispose();
                cache.put(key, img);
            }
            Object old = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(img, (int) Math.round(x), (int) Math.round(y), (int) Math.round(w), (int) Math.round(h), null);
            if (old != null) g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, old);
            return;
        }
        paint(g, c, x, y, w, h, t, talk, framed, false);
    }

    static void hints(Graphics2D b) {
        b.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        b.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        b.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    /** A round head-and-shoulders avatar. */
    static void avatar(Graphics2D g, Cand c, double cx, double cy, double r, Color ring) {
        Shape old = g.getClip();
        Ellipse2D circ = new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r);
        g.setColor(c == null ? C_STATE : shade(partyBg(c.party), 0.55));
        g.fill(circ);
        if (c != null) {
            Area clip = new Area(circ);
            if (old != null) clip.intersect(new Area(old));
            g.setClip(clip);
            double w = r * 2.5, h = w * 1.2;
            draw(g, c, cx - w / 2, cy - h * 0.36, w, h, 0, 0, true, false);
            g.setClip(old);
        }
        if (ring != null) {
            g.setColor(ring);
            g.setStroke(new BasicStroke(2f));
            g.draw(circ);
            g.setStroke(new BasicStroke(1f));
        }
    }

    static void paint(Graphics2D g, Cand c, double x, double y, double w, double h, double t, double talk, boolean framed, boolean still) {
        AffineTransform saved = g.getTransform();
        Shape oldClip = g.getClip();
        Look L = c.look;
        double ph = (Math.abs(c.id.hashCode()) % 1000) / 159.0;           // per-candidate animation phase
        g.translate(x, y);
        g.scale(w / 200.0, h / 240.0);
        RoundRectangle2D card = new RoundRectangle2D.Double(0, 0, 200, 240, 22, 22);
        if (framed) {
            Color pc = partyBg(c.party);
            g.setPaint(new GradientPaint(0, 0, shade(pc, 0.95), 0, 240, shade(pc, 0.32)));
            g.fill(card);
            g.clip(card);
            // stripes and stars
            g.setColor(new Color(255, 255, 255, 14));
            for (int i = -4; i < 12; i++) g.fill(new Rectangle2D.Double(i * 28, 0, 9, 260));
            g.setColor(new Color(255, 255, 255, 30));
            for (int i = 0; i < 9; i++) star(g, 18 + (i % 3) * 22 + (i / 3 % 2) * 11, 18 + (i / 3) * 18, 3.2);
            g.setPaint(new RadialGradientPaint(new Point2D.Double(100, 92), 120f, new float[]{0f, 1f},
                    new Color[]{new Color(255, 255, 255, 70), new Color(255, 255, 255, 0)}));
            g.fill(new Rectangle2D.Double(0, 0, 200, 240));
        }
        // animation
        double breath = still ? 0 : Math.sin(t * 1.7 + ph) * 1.3;
        double tilt = still ? 0 : Math.sin(t * 0.45 + ph) * 0.022 + Math.sin(t * 1.3 + ph * 2) * 0.006 + talk * Math.sin(t * 3.1) * 0.02;
        double blinkPhase = (t + ph * 3) % (3.6 + (ph % 1.5));
        double open = still ? 1 : blinkPhase < 0.13 ? Math.abs(blinkPhase - 0.065) / 0.065 : 1;
        double gazeX = still ? 0 : Math.sin(t * 0.37 + ph) * 1.4 + Math.sin(t * 1.9 + ph) * 0.4;
        double gazeY = still ? 0 : Math.sin(t * 0.29 + ph * 1.7) * 0.6;
        double mouthOpen = 0;
        if (talk > 0 && !still) mouthOpen = talk * Math.max(0, 0.55 + 0.45 * Math.sin(t * 15.0 + ph)) * (0.55 + 0.45 * Math.abs(Math.sin(t * 6.3 + ph)));
        Color skin = SKIN[clampi(L.skin, 0, SKIN.length - 1)];
        Color hair = mix(HAIR[clampi(L.hair, 0, HAIR.length - 1)], rgb(0xc9c7c4), L.gray);
        double cx = 100, cy = 100 + breath * 0.5;
        double hw = 43 * L.faceW, hh = 55;
        // ---- back hair (long styles)
        AffineTransform preHair = g.getTransform();
        g.rotate(tilt, 100, 190);
        g.translate(100, 197);
        g.scale(1.13, 1.13);
        g.translate(-100, -192);
        if (L.style == 6 || L.style == 7 || L.style == 8 || L.style == 9) backHair(g, L, hair, cx, cy, hw, hh);
        g.setTransform(preHair);
        // ---- body
        body(g, c, L, breath);
        g.rotate(tilt, 100, 190);
        g.translate(100, 197);
        g.scale(1.13, 1.13);
        g.translate(-100, -192);
        // ---- neck
        Path2D neck = new Path2D.Double();
        neck.moveTo(cx - 19, cy + hh * 0.55);
        neck.lineTo(cx - 21, 192 + breath);
        neck.quadTo(cx, 200 + breath, cx + 21, 192 + breath);
        neck.lineTo(cx + 19, cy + hh * 0.55);
        neck.closePath();
        g.setColor(shade(skin, 0.86));
        g.fill(neck);
        g.setPaint(new GradientPaint((float) cx, (float) (cy + hh * 0.8), new Color(0, 0, 0, 70), (float) cx, (float) (cy + hh * 1.25), new Color(0, 0, 0, 0)));
        g.fill(neck);
        // ---- ears
        for (int sgn = -1; sgn <= 1; sgn += 2) {
            Ellipse2D ear = new Ellipse2D.Double(cx + sgn * (hw + 1) - 7, cy - 4, 14, 25);
            g.setColor(shade(skin, 0.93));
            g.fill(ear);
            g.setColor(shade(skin, 0.72));
            g.setStroke(new BasicStroke(1.3f));
            g.draw(new Arc2D.Double(cx + sgn * (hw + 1) - 4, cy + 1, 8, 15, sgn < 0 ? 90 : -90, 180, Arc2D.OPEN));
            if (L.earrings) {
                g.setColor(rgb(0xf1d27a));
                g.fill(new Ellipse2D.Double(cx + sgn * (hw + 1) - 2.5, cy + 19, 5, 5));
                g.setColor(new Color(255, 255, 255, 160));
                g.fill(new Ellipse2D.Double(cx + sgn * (hw + 1) - 1.5, cy + 19.5, 2, 2));
            }
        }
        // ---- face
        double jawW = hw * (0.52 + 0.34 * L.jaw), chinW = 9 + 11 * L.jaw;
        Path2D face = new Path2D.Double();
        face.moveTo(cx - hw, cy - 4);
        face.curveTo(cx - hw, cy - hh * 1.36, cx + hw, cy - hh * 1.36, cx + hw, cy - 4);
        face.curveTo(cx + hw * 1.01, cy + hh * 0.42, cx + jawW + 4, cy + hh * 0.84, cx + chinW, cy + hh * 0.98);
        face.curveTo(cx + chinW * 0.45, cy + hh * 1.05, cx - chinW * 0.45, cy + hh * 1.05, cx - chinW, cy + hh * 0.98);
        face.curveTo(cx - jawW - 4, cy + hh * 0.84, cx - hw * 1.01, cy + hh * 0.42, cx - hw, cy - 4);
        face.closePath();
        g.setPaint(new RadialGradientPaint(new Point2D.Double(cx - hw * 0.25, cy - hh * 0.25), (float) (hw * 1.9),
                new float[]{0f, 0.62f, 1f}, new Color[]{shade(skin, 1.07), skin, shade(skin, 0.80)}));
        g.fill(face);
        Shape faceClip = face;
        Shape prevClip = g.getClip();
        g.clip(faceClip);
        // cheek warmth and jaw shading
        Color blush = mix(skin, rgb(0xd0584e), 0.45);
        for (int sgn = -1; sgn <= 1; sgn += 2) {
            g.setPaint(new RadialGradientPaint(new Point2D.Double(cx + sgn * hw * 0.55, cy + 22), 18f, new float[]{0f, 1f},
                    new Color[]{alpha(blush, L.female ? 0.30 : 0.16), alpha(blush, 0)}));
            g.fill(new Rectangle2D.Double(cx - hw, cy, hw * 2, 50));
        }
        g.setPaint(new GradientPaint((float) cx, (float) (cy + hh * 0.5), new Color(0, 0, 0, 0), (float) cx, (float) (cy + hh), new Color(0, 0, 0, 34)));
        g.fill(new Rectangle2D.Double(cx - hw, cy + hh * 0.5, hw * 2, hh * 0.6));
        // stubble / shaved-head shadow inside the face
        if (L.facial == 3 || L.facial == 1 || L.facial == 2) {
            g.setColor(alpha(shade(hair, 0.7), L.facial == 3 ? 0.20 : 0.10));
            g.fill(new Ellipse2D.Double(cx - jawW - 6, cy + 18, (jawW + 6) * 2, hh));
        }
        g.setClip(prevClip);
        g.setColor(shade(skin, 0.66));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(face);
        // ---- age lines
        double age = L.age;
        if (age > 0.5) {
            double a = clamp((age - 0.5) * 1.3, 0, 0.55);
            g.setColor(alpha(shade(skin, 0.55), a * 0.55));
            g.setStroke(new BasicStroke(1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < (age > 0.7 ? 3 : 2); i++) g.draw(new Arc2D.Double(cx - 18 + i * 2, cy - 34 - i * 5, 36 - i * 4, 8, 20, 140, Arc2D.OPEN));
            for (int sgn = -1; sgn <= 1; sgn += 2) {
                double ex = cx + sgn * hw * 0.42;
                g.draw(new Line2D.Double(ex + sgn * 12, cy + 4, ex + sgn * 16, cy + 1));
                g.draw(new Line2D.Double(ex + sgn * 12, cy + 7, ex + sgn * 16, cy + 8));
                Path2D nl = new Path2D.Double();
                nl.moveTo(cx + sgn * 9, cy + 25);
                nl.quadTo(cx + sgn * 15, cy + 33, cx + sgn * 14, cy + 42);
                g.setColor(alpha(shade(skin, 0.55), a * 0.7));
                g.draw(nl);
            }
        }
        // ---- eyes
        double eyeY = cy + 3 + gazeY * 0.2, eyeDX = hw * 0.41, ew = 9.0 * Math.sqrt(L.faceW), eh = 4.8 * open;
        Color iris = IRIS[(L.skin <= 1 && (L.hair >= 3 && L.hair <= 5)) ? 1 : (L.skin <= 1 && (c.id.hashCode() & 3) == 1) ? 2 : (L.skin >= 4 ? 3 : 0)];
        for (int sgn = -1; sgn <= 1; sgn += 2) {
            double ex = cx + sgn * eyeDX;
            // socket shadow
            g.setPaint(new RadialGradientPaint(new Point2D.Double(ex, eyeY - 2), 15f, new float[]{0f, 1f},
                    new Color[]{alpha(shade(skin, 0.7), 0.35), alpha(shade(skin, 0.7), 0)}));
            g.fill(new Ellipse2D.Double(ex - 15, eyeY - 14, 30, 24));
            Path2D eye = new Path2D.Double();
            eye.moveTo(ex - ew, eyeY);
            eye.quadTo(ex - ew * 0.2, eyeY - eh * 1.55 - 0.2, ex + ew, eyeY - 0.5);
            eye.quadTo(ex + ew * 0.1, eyeY + eh * 1.15, ex - ew, eyeY);
            eye.closePath();
            if (eh > 0.6) {
                g.setColor(rgb(0xf7f2ec));
                g.fill(eye);
                Shape pc = g.getClip();
                g.clip(eye);
                double ix = ex + gazeX, iy = eyeY - 0.6;
                g.setColor(iris);
                g.fill(new Ellipse2D.Double(ix - 4.3, iy - 4.3, 8.6, 8.6));
                g.setColor(rgb(0x0d0907));
                g.fill(new Ellipse2D.Double(ix - 2.1, iy - 2.1, 4.2, 4.2));
                g.setColor(new Color(255, 255, 255, 220));
                g.fill(new Ellipse2D.Double(ix + 0.6, iy - 2.6, 1.9, 1.9));
                g.setColor(new Color(0, 0, 0, 40));
                g.fill(new Rectangle2D.Double(ex - ew, eyeY - 8, ew * 2, 3.2));
                g.setClip(pc);
            }
            g.setColor(rgb(0x2a1c16));
            g.setStroke(new BasicStroke(L.female ? 2.2f : 1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D lid = new Path2D.Double();
            lid.moveTo(ex - ew - 0.5, eyeY + 0.3);
            lid.quadTo(ex - ew * 0.2, eyeY - eh * 1.55 - 0.2, ex + ew + (L.female ? 1.5 : 0), eyeY - (L.female ? 1.6 : 0.5));
            g.draw(lid);
            g.setColor(alpha(shade(skin, 0.5), 0.6));
            g.setStroke(new BasicStroke(0.9f));
            g.draw(new Arc2D.Double(ex - ew * 0.8, eyeY - 5 - eh * 0.6, ew * 1.6, 8, 20, 140, Arc2D.OPEN));
            if (age > 0.5) {
                g.setColor(alpha(shade(skin, 0.6), (age - 0.5) * 0.9));
                g.draw(new Arc2D.Double(ex - ew * 0.7, eyeY + 1, ew * 1.4, 6, 200, 140, Arc2D.OPEN));
            }
        }
        // ---- brows
        Color browC = L.style == 0 && L.hair <= 1 ? rgb(0x241a14) : shade(hair, L.hair >= 6 ? 0.8 : 0.85);
        for (int sgn = -1; sgn <= 1; sgn += 2) {
            double ex = cx + sgn * eyeDX;
            double by = eyeY - 11 - L.brow * 3 + (talk > 0 && !still ? -Math.max(0, Math.sin(t * 2.3 + ph)) * 1.6 * talk : 0);
            Path2D b = new Path2D.Double();
            b.moveTo(ex - sgn * (ew - 1), by + 1.5);
            b.quadTo(ex + sgn * 1, by - 3.5, ex + sgn * (ew + 3), by + 1);
            g.setColor(browC);
            g.setStroke(new BasicStroke(L.female ? 2.4f : 3.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(b);
        }
        // ---- nose
        g.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(alpha(shade(skin, 0.62), 0.75));
        Path2D nose = new Path2D.Double();
        nose.moveTo(cx + 2.5, eyeY + 3);
        nose.quadTo(cx + 6, cy + 16, cx + 6.5, cy + 23);
        g.draw(nose);
        Path2D tip = new Path2D.Double();
        tip.moveTo(cx - 7, cy + 24);
        tip.quadTo(cx - 4, cy + 28.5, cx, cy + 27.5);
        tip.quadTo(cx + 4, cy + 28.5, cx + 7, cy + 24);
        g.setColor(alpha(shade(skin, 0.55), 0.85));
        g.draw(tip);
        g.setColor(alpha(shade(skin, 1.25), 0.6));
        g.fill(new Ellipse2D.Double(cx - 2.5, cy + 19, 4, 3));
        // ---- facial hair under the mouth
        Color fh = shade(hair, 0.9);
        if (L.facial == 1 || L.facial == 2) {
            Path2D beard = new Path2D.Double();
            double bt = L.facial == 1 ? 1.0 : 0.6;
            beard.moveTo(cx - hw + 2, cy + 6);
            beard.curveTo(cx - hw * 0.98, cy + hh * 0.5, cx - jawW - 6, cy + hh * (0.9 + 0.12 * bt), cx, cy + hh * (1.02 + 0.12 * bt));
            beard.curveTo(cx + jawW + 6, cy + hh * (0.9 + 0.12 * bt), cx + hw * 0.98, cy + hh * 0.5, cx + hw - 2, cy + 6);
            beard.lineTo(cx + hw - 6, cy + 10);
            beard.curveTo(cx + hw * 0.7, cy + 30, cx + 16, cy + 30, cx + 15, cy + 36);
            beard.quadTo(cx, cy + 32, cx - 15, cy + 36);
            beard.curveTo(cx - 16, cy + 30, cx - hw * 0.7, cy + 30, cx - hw + 6, cy + 10);
            beard.closePath();
            g.setColor(alpha(fh, 0.92));
            g.fill(beard);
            g.setColor(alpha(shade(fh, 1.3), 0.25));
            for (int i = 0; i < 18; i++) {
                double a = i / 17.0;
                double bx = cx - jawW + a * jawW * 2, by = cy + hh * 0.72 + Math.sin(a * Math.PI) * 14;
                g.draw(new Line2D.Double(bx, by, bx + 1, by + 4));
            }
        }
        if (L.facial == 5) {
            Path2D goatee = new Path2D.Double();
            goatee.moveTo(cx - 13, cy + 44);
            goatee.quadTo(cx, cy + hh * 1.12, cx + 13, cy + 44);
            goatee.quadTo(cx, cy + 50, cx - 13, cy + 44);
            g.setColor(alpha(fh, 0.92));
            g.fill(goatee);
        }
        // ---- mouth
        double my = cy + 38, mw = 13 + L.smile * 3 + (L.female ? 0 : 1);
        double up = (L.smile - 0.45) * 7;
        Color lipC = L.lipstick ? rgb(0xb8324a) : mix(shade(skin, 0.78), rgb(0xa8505a), 0.45);
        if (mouthOpen > 0.05 || L.smile > 0.72) {
            double oh = Math.max(mouthOpen * 9, L.smile > 0.72 ? 3.6 : 0);
            Path2D m = new Path2D.Double();
            m.moveTo(cx - mw, my - up);
            m.quadTo(cx, my + 2 + oh * 1.6 + up * 0.4, cx + mw, my - up);
            m.quadTo(cx, my + 1.0, cx - mw, my - up);
            m.closePath();
            g.setColor(rgb(0x4a1d1f));
            g.fill(m);
            Shape pc = g.getClip();
            g.clip(m);
            g.setColor(rgb(0xf4efe6));
            g.fill(new Rectangle2D.Double(cx - mw, my - up - 1, mw * 2, 3.2 + oh * 0.25));
            g.setClip(pc);
            g.setColor(lipC);
            g.setStroke(new BasicStroke(L.lipstick ? 2.4f : 1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(m);
        } else {
            Path2D m = new Path2D.Double();
            m.moveTo(cx - mw, my - up);
            m.quadTo(cx, my + 2.5 + up * 0.5, cx + mw, my - up);
            g.setColor(alpha(shade(lipC, 0.75), 0.95));
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(m);
            if (L.lipstick || L.female) {
                Path2D lower = new Path2D.Double();
                lower.moveTo(cx - mw * 0.75, my + 1);
                lower.quadTo(cx, my + 7, cx + mw * 0.75, my + 1);
                g.setColor(alpha(lipC, L.lipstick ? 0.85 : 0.45));
                g.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(lower);
            }
            g.setColor(alpha(shade(skin, 0.6), 0.4));
            g.setStroke(new BasicStroke(1f));
            g.draw(new Arc2D.Double(cx - 5, my + 7, 10, 4, 200, 140, Arc2D.OPEN));
        }
        if (L.facial == 4 || L.facial == 1 || L.facial == 2) {
            Path2D mus = new Path2D.Double();
            mus.moveTo(cx - mw - 2, my - up + 1);
            mus.quadTo(cx - mw * 0.6, my - 9, cx, my - 6.5);
            mus.quadTo(cx + mw * 0.6, my - 9, cx + mw + 2, my - up + 1);
            mus.quadTo(cx, my - 2.5, cx - mw - 2, my - up + 1);
            g.setColor(alpha(fh, 0.95));
            g.fill(mus);
        }
        // ---- glasses
        if (L.glasses > 0) {
            g.setColor(L.glasses == 2 ? rgb(0x161616) : rgb(0x4b4b52));
            g.setStroke(new BasicStroke(L.glasses == 2 ? 2.6f : 1.6f));
            for (int sgn = -1; sgn <= 1; sgn += 2) {
                double ex = cx + sgn * eyeDX;
                RoundRectangle2D lens = new RoundRectangle2D.Double(ex - ew - 4, eyeY - 8, (ew + 4) * 2, 15, 8, 8);
                g.setColor(new Color(200, 230, 255, 26));
                g.fill(lens);
                g.setColor(L.glasses == 2 ? rgb(0x161616) : rgb(0x4b4b52));
                g.draw(lens);
                g.draw(new Line2D.Double(ex + sgn * (ew + 4), eyeY - 3, cx + sgn * (hw + 1), eyeY - 5));
            }
            g.draw(new Arc2D.Double(cx - 5, eyeY - 5, 10, 6, 0, 180, Arc2D.OPEN));
        }
        // ---- front hair
        frontHair(g, L, hair, cx, cy, hw, hh, jawW);
        g.setTransform(saved);
        g.setClip(oldClip);
        if (framed) {
            g.setColor(new Color(255, 255, 255, 50));
            g.setStroke(new BasicStroke(1f));
            g.draw(new RoundRectangle2D.Double(x + 0.5, y + 0.5, w - 1, h - 1, 22 * w / 200, 22 * h / 240));
        }
    }

    static void star(Graphics2D g, double x, double y, double r) {
        Path2D p = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double a = -Math.PI / 2 + i * Math.PI / 5, rr = i % 2 == 0 ? r : r * 0.45;
            if (i == 0) p.moveTo(x + Math.cos(a) * rr, y + Math.sin(a) * rr); else p.lineTo(x + Math.cos(a) * rr, y + Math.sin(a) * rr);
        }
        p.closePath();
        g.fill(p);
    }

    /** Shoulders, jacket, shirt and tie (or blouse). */
    static void body(Graphics2D g, Cand c, Look L, double breath) {
        double by = breath;
        Color suit = SUIT[clampi(L.suit, 0, SUIT.length - 1)];
        boolean casual = L.suit == 3, tee = L.suit == 4, vest = L.suit == 5;
        Path2D sh = new Path2D.Double();
        sh.moveTo(-4, 244);
        sh.lineTo(2, 214 + by);
        sh.curveTo(10, 192 + by, 40, 184 + by, 72, 178 + by);
        sh.lineTo(128, 178 + by);
        sh.curveTo(160, 184 + by, 190, 192 + by, 198, 214 + by);
        sh.lineTo(204, 244);
        sh.closePath();
        Color jacket = vest ? rgb(0xe9eef6) : suit;
        g.setPaint(new GradientPaint(0, (float) (180 + by), shade(jacket, 1.12), 0, 244, shade(jacket, 0.7)));
        g.fill(sh);
        if (casual) {
            // open-collar shirt
            g.setColor(shade(suit, 0.8));
            g.setStroke(new BasicStroke(1.2f));
            g.draw(new Line2D.Double(100, 196 + by, 100, 244));
            for (int i = 0; i < 3; i++) g.fill(new Ellipse2D.Double(98.5, 206 + i * 13 + by, 3, 3));
            Path2D v = new Path2D.Double();
            v.moveTo(84, 180 + by); v.lineTo(100, 202 + by); v.lineTo(116, 180 + by); v.closePath();
            g.setColor(shade(Portrait.SKIN[clampi(L.skin, 0, 7)], 0.85));
            g.fill(v);
            collar(g, 100, 178 + by, shade(suit, 1.12), true);
            return;
        }
        if (vest) {
            // shirt sleeves + fleece vest
            Path2D vv = new Path2D.Double();
            vv.moveTo(36, 244); vv.lineTo(44, 196 + by); vv.curveTo(60, 186 + by, 74, 182 + by, 86, 182 + by);
            vv.lineTo(100, 210 + by); vv.lineTo(114, 182 + by);
            vv.curveTo(126, 182 + by, 140, 186 + by, 156, 196 + by); vv.lineTo(164, 244); vv.closePath();
            g.setPaint(new GradientPaint(0, (float) (186 + by), rgb(0x2a3a5a), 0, 244, rgb(0x172238)));
            g.fill(vv);
            g.setColor(new Color(220, 220, 230, 160));
            g.draw(new Line2D.Double(100, 210 + by, 100, 244));
            collar(g, 100, 178 + by, rgb(0xdfe6f2), true);
            return;
        }
        // V of shirt or blouse
        Color shirt = L.female ? BLOUSE[clampi(L.tie, 0, BLOUSE.length - 1)] : (tee ? rgb(0x22252b) : rgb(0xf2f4f8));
        Path2D v = new Path2D.Double();
        double vDepth = L.female ? 226 : tee ? 214 : 244;
        v.moveTo(78, 180 + by);
        v.lineTo(100, vDepth + by);
        v.lineTo(122, 180 + by);
        v.closePath();
        g.setColor(shirt);
        g.fill(v);
        if (L.female && clampi(L.tie, 0, 9) == 0) {
            // a necklace
            g.setColor(new Color(245, 240, 230, 220));
            for (int i = 0; i < 9; i++) {
                double a = Math.PI * (0.15 + 0.7 * i / 8.0);
                g.fill(new Ellipse2D.Double(100 + Math.cos(a) * 15 - 1.6, 182 + by + Math.sin(a) * 12 - 1.6, 3.2, 3.2));
            }
        }
        if (!L.female && !tee) {
            collar(g, 100, 178 + by, rgb(0xf7f8fb), false);
            Color tc = TIE[clampi(L.tie, 0, TIE.length - 1)];
            if (tc != null) {
                Path2D knot = new Path2D.Double();
                knot.moveTo(95, 186 + by); knot.lineTo(105, 186 + by); knot.lineTo(103, 194 + by); knot.lineTo(97, 194 + by); knot.closePath();
                Path2D blade = new Path2D.Double();
                blade.moveTo(97, 194 + by); blade.lineTo(103, 194 + by); blade.lineTo(108, 236 + by); blade.lineTo(100, 244 + by); blade.lineTo(92, 236 + by); blade.closePath();
                g.setPaint(new GradientPaint(92, 0, shade(tc, 1.15), 108, 0, shade(tc, 0.8)));
                g.fill(blade);
                g.setColor(shade(tc, 0.85));
                g.fill(knot);
                if (L.tie == 2) {
                    g.setColor(new Color(255, 255, 255, 70));
                    g.setStroke(new BasicStroke(1.6f));
                    for (int i = 0; i < 5; i++) g.draw(new Line2D.Double(92, 200 + i * 9 + by, 108, 194 + i * 9 + by));
                }
            }
        }
        // lapels
        for (int sgn = -1; sgn <= 1; sgn += 2) {
            Path2D lap = new Path2D.Double();
            lap.moveTo(100 + sgn * 22, 179 + by);
            lap.lineTo(100 + sgn * 4, (L.female ? 226 : 236) + by);
            lap.lineTo(100 + sgn * 16, 216 + by);
            lap.lineTo(100 + sgn * 30, 200 + by);
            lap.lineTo(100 + sgn * 26, 182 + by);
            lap.closePath();
            g.setColor(shade(suit, 0.82));
            g.fill(lap);
            g.setColor(shade(suit, 1.35));
            g.setStroke(new BasicStroke(0.9f));
            g.draw(lap);
        }
        // flag pin
        if (c.party <= REP && !tee) {
            double px = 136, py = 206 + by;
            g.setColor(rgb(0xb5243a)); g.fill(new Rectangle2D.Double(px, py, 9, 6));
            g.setColor(Color.white); g.fill(new Rectangle2D.Double(px, py + 1.5, 9, 1)); g.fill(new Rectangle2D.Double(px, py + 3.5, 9, 1));
            g.setColor(rgb(0x1d3a8a)); g.fill(new Rectangle2D.Double(px, py, 4, 3));
        }
    }

    static void collar(Graphics2D g, double cx, double y, Color col, boolean open) {
        for (int sgn = -1; sgn <= 1; sgn += 2) {
            Path2D p = new Path2D.Double();
            p.moveTo(cx + sgn * 17, y - 2);
            p.lineTo(cx + sgn * (open ? 22 : 2), y + (open ? 20 : 12));
            p.lineTo(cx + sgn * (open ? 10 : 15), y + (open ? 24 : 14));
            p.lineTo(cx + sgn * 20, y + 2);
            p.closePath();
            g.setColor(col);
            g.fill(p);
            g.setColor(new Color(0, 0, 0, 50));
            g.draw(p);
        }
    }

    static void backHair(Graphics2D g, Look L, Color hair, double cx, double cy, double hw, double hh) {
        double len = L.style == 6 ? 0.62 : L.style == 7 ? 1.3 : 1.65;
        double wid = L.style == 8 ? 1.32 : L.style == 6 ? 1.14 : 1.2;
        Path2D p = new Path2D.Double();
        p.moveTo(cx - hw * wid, cy + hh * len);
        p.curveTo(cx - hw * (wid + 0.12), cy - hh * 0.2, cx - hw * 1.2, cy - hh * 1.38, cx, cy - hh * 1.2);
        p.curveTo(cx + hw * 1.2, cy - hh * 1.38, cx + hw * (wid + 0.12), cy - hh * 0.2, cx + hw * wid, cy + hh * len);
        if (L.style == 8) {
            for (int i = 6; i >= 0; i--) {
                double xx = cx - hw * wid + (hw * wid * 2) * i / 6.0;
                p.lineTo(xx, cy + hh * len + (i % 2 == 0 ? 6 : -2));
            }
        } else p.quadTo(cx, cy + hh * (len + 0.08), cx - hw * wid, cy + hh * len);
        p.closePath();
        g.setPaint(new GradientPaint((float) cx, (float) (cy - hh), shade(hair, 1.05), (float) cx, (float) (cy + hh * len), shade(hair, 0.62)));
        g.fill(p);
    }

    static void frontHair(Graphics2D g, Look L, Color hair, double cx, double cy, double hw, double hh, double jawW) {
        int st = L.style;
        Color hi = shade(hair, L.hair >= 5 ? 1.12 : 1.45), dk = shade(hair, 0.7);
        g.setStroke(new BasicStroke(1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (st == 0) {
            if (L.hair >= 6 || L.gray > 0.5) {
                // horseshoe: hair only on the sides
                for (int sgn = -1; sgn <= 1; sgn += 2) {
                    Path2D p = new Path2D.Double();
                    p.moveTo(cx + sgn * (hw + 1), cy + 4);
                    p.curveTo(cx + sgn * (hw + 3), cy - 18, cx + sgn * (hw - 2), cy - 30, cx + sgn * (hw - 9), cy - 36);
                    p.lineTo(cx + sgn * (hw - 12), cy - 32);
                    p.curveTo(cx + sgn * (hw - 6), cy - 22, cx + sgn * (hw - 3), cy - 10, cx + sgn * (hw - 3), cy + 4);
                    p.closePath();
                    g.setColor(alpha(hair, 0.9));
                    g.fill(p);
                }
            } else {
                Path2D cap = new Path2D.Double();
                cap.moveTo(cx - hw, cy - 6);
                cap.curveTo(cx - hw, cy - hh * 1.36, cx + hw, cy - hh * 1.36, cx + hw, cy - 6);
                cap.curveTo(cx + hw * 0.8, cy - hh * 0.6, cx - hw * 0.8, cy - hh * 0.6, cx - hw, cy - 6);
                g.setColor(alpha(hair, 0.18));
                g.fill(cap);
            }
            // scalp highlight
            g.setColor(new Color(255, 255, 255, 34));
            g.fill(new Ellipse2D.Double(cx - 18, cy - hh * 0.98, 30, 12));
            return;
        }
        if (st == 6 || st == 7 || st == 8 || st == 9) {
            // women's front: a domed cap with a side-swept fringe, plus side curtains framing the face
            boolean center = st == 8;
            double part = center ? cx : cx - hw * 0.32;
            double low = st == 6 ? hh * 0.42 : st == 7 ? hh * 0.78 : hh * 1.0;
            Area front = new Area();
            Path2D cap = new Path2D.Double();
            cap.moveTo(cx - hw - 3, cy + 2);
            cap.curveTo(cx - hw - 4, cy - hh * 1.42, cx + hw + 4, cy - hh * 1.42, cx + hw + 3, cy + 2);
            cap.lineTo(cx + hw - 3, cy + 2);
            if (center) {
                cap.curveTo(cx + hw - 1, cy - hh * 0.45, cx + hw * 0.45, cy - hh * 0.7, part, cy - hh * 0.74);
                cap.curveTo(cx - hw * 0.45, cy - hh * 0.7, cx - hw + 1, cy - hh * 0.45, cx - hw + 3, cy + 2);
            } else {
                cap.curveTo(cx + hw - 1, cy - hh * 0.4, cx + hw * 0.5, cy - hh * 0.66, part + 3, cy - hh * 0.76);
                cap.curveTo(part - hw * 0.2, cy - hh * 0.55, cx - hw * 0.62, cy - hh * 0.42, cx - hw + 3, cy - hh * 0.12);
                cap.lineTo(cx - hw + 3, cy + 2);
            }
            cap.closePath();
            front.add(new Area(cap));
            for (int sgn = -1; sgn <= 1; sgn += 2) {
                Path2D cur = new Path2D.Double();
                cur.moveTo(cx + sgn * (hw + 3), cy - hh * 0.25);
                cur.curveTo(cx + sgn * (hw + 7), cy + low * 0.4, cx + sgn * (hw + 6), cy + low * 0.8, cx + sgn * (hw + 3), cy + low);
                cur.lineTo(cx + sgn * (hw - 6), cy + low * 0.92);
                cur.curveTo(cx + sgn * (hw - 2), cy + low * 0.6, cx + sgn * (hw - 1), cy + low * 0.2, cx + sgn * (hw - 3), cy - hh * 0.1);
                cur.closePath();
                front.add(new Area(cur));
            }
            g.setPaint(new GradientPaint((float) cx, (float) (cy - hh * 1.1), shade(hair, 1.12), (float) cx, (float) (cy + low), shade(hair, 0.82)));
            g.fill(front);
            Shape pc = g.getClip();
            g.clip(front);
            g.setColor(alpha(hi, 0.30));
            for (int i = 0; i < 9; i++) {
                Path2D sl = new Path2D.Double();
                double sx = part + (i - 4) * 8.5;
                sl.moveTo(part + (i - 4) * 1.5, cy - hh * 1.02);
                sl.quadTo(sx + (sx < part ? -6 : 6), cy - hh * 0.7, sx + (sx < part ? -14 : 14), cy - hh * 0.1);
                g.draw(sl);
            }
            for (int sgn = -1; sgn <= 1; sgn += 2) {
                g.draw(new Line2D.Double(cx + sgn * (hw + 2), cy - hh * 0.1, cx + sgn * (hw + 1), cy + low * 0.9));
                g.draw(new Line2D.Double(cx + sgn * (hw - 1), cy - hh * 0.05, cx + sgn * (hw - 3), cy + low * 0.85));
            }
            g.setClip(pc);
            return;
        }
        // men's short styles: a cap over the cranium with a hairline
        double top = st == 11 ? 1.48 : st == 5 || st == 10 ? 1.44 : st == 3 ? 1.40 : st == 1 ? 1.33 : 1.38;
        double line = st == 4 ? 0.86 : st == 3 || st == 11 ? 0.70 : st == 1 ? 0.66 : st == 5 || st == 10 ? 0.58 : 0.62;
        Path2D p = new Path2D.Double();
        p.moveTo(cx - hw - 1.5, cy + 2);
        p.curveTo(cx - hw - 3, cy - hh * top, cx + hw + 3, cy - hh * top, cx + hw + 1.5, cy + 2);
        p.lineTo(cx + hw - 3, cy + 2);
        if (st == 4) {
            // receding: temples pushed back, thin centre
            p.curveTo(cx + hw - 2, cy - hh * 0.5, cx + hw * 0.55, cy - hh * 0.95, cx + hw * 0.25, cy - hh * 0.8);
            p.quadTo(cx, cy - hh * 0.72, cx - hw * 0.25, cy - hh * 0.8);
            p.curveTo(cx - hw * 0.55, cy - hh * 0.95, cx - hw + 2, cy - hh * 0.5, cx - hw + 3, cy + 2);
        } else {
            double partX = st == 2 ? cx - hw * 0.38 : cx;
            p.curveTo(cx + hw - 2, cy - hh * 0.4, cx + hw * 0.7, cy - hh * line, partX + hw * 0.2, cy - hh * line);
            if (st == 2 || st == 5 || st == 10) p.quadTo(partX, cy - hh * (line - 0.06), partX - hw * 0.3, cy - hh * (line - 0.02));
            p.curveTo(cx - hw * 0.75, cy - hh * line, cx - hw + 2, cy - hh * 0.4, cx - hw + 3, cy + 2);
        }
        p.closePath();
        g.setPaint(new GradientPaint((float) cx, (float) (cy - hh * top), shade(hair, 1.1), (float) cx, (float) (cy - hh * 0.3), shade(hair, 0.85)));
        g.fill(p);
        double capTop = cy - hh * top * 0.75;
        // volume on top: soft waves (5, 10) or a swept-back pompadour (11)
        if (st == 5 || st == 10) {
            int n = st == 10 ? 6 : 5;
            for (int i = 0; i < n; i++) {
                double a = i / (double) (n - 1);
                double ex = cx - hw * 0.78 + a * hw * 1.56, ey = capTop + 6 + Math.abs(a - 0.5) * 14 + (st == 10 && i % 2 == 1 ? -3 : 0);
                double r = st == 10 ? 13 : 12;
                g.setColor(i % 2 == 0 ? shade(hair, 1.04) : shade(hair, 0.95));
                g.fill(new Ellipse2D.Double(ex - r, ey - r * 0.85, r * 2, r * 1.6));
            }
        } else if (st == 11) {
            Path2D v = new Path2D.Double();
            v.moveTo(cx - hw * 0.95, cy - hh * 0.55);
            v.curveTo(cx - hw * 1.05, capTop - 10, cx + hw * 0.2, capTop - 20, cx + hw * 0.85, capTop + 4);
            v.curveTo(cx + hw * 1.0, cy - hh * 0.75, cx + hw * 0.6, cy - hh * 0.75, cx - hw * 0.2, cy - hh * 0.7);
            v.closePath();
            g.setPaint(new GradientPaint((float) cx, (float) (capTop - 18), shade(hair, 1.18), (float) cx, (float) (cy - hh * 0.6), shade(hair, 0.9)));
            g.fill(v);
        }
        // texture strokes
        g.setColor(alpha(hi, 0.30));
        g.setStroke(new BasicStroke(1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 8; i++) {
            double a = i / 7.0;
            double sx = cx - hw * 0.7 + a * hw * 1.4;
            double ty = capTop + 4 + Math.abs(a - 0.5) * 10;
            Path2D s = new Path2D.Double();
            if (st == 3 || st == 11) {
                s.moveTo(sx, cy - hh * (line + 0.04));
                s.quadTo(sx * 0.9 + cx * 0.1, (ty + cy - hh * line) / 2, sx * 0.75 + cx * 0.25, ty);
            } else {
                s.moveTo(sx, ty);
                s.quadTo(sx + 5, (ty + cy - hh * line) / 2, sx + 2 + (a - 0.5) * 8, cy - hh * (line + 0.06));
            }
            g.draw(s);
        }
        if (st == 2) {
            g.setColor(alpha(dk, 0.8));
            g.setStroke(new BasicStroke(1.4f));
            g.draw(new Line2D.Double(cx - hw * 0.38, cy - hh * (line + 0.02), cx - hw * 0.33, cy - hh * (line + 0.22)));
        }
        // sideburns, inside the face outline
        for (int sgn = -1; sgn <= 1; sgn += 2) {
            Path2D sb = new Path2D.Double();
            sb.moveTo(cx + sgn * (hw + 0.5), cy - 10);
            sb.lineTo(cx + sgn * (hw + 0.2), cy + 10);
            sb.lineTo(cx + sgn * (hw - 4.5), cy + 9);
            sb.lineTo(cx + sgn * (hw - 5), cy - 10);
            sb.closePath();
            g.setColor(alpha(hair, 0.92));
            g.fill(sb);
        }
    }
}
