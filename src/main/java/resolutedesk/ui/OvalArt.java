package resolutedesk.ui;

import static resolutedesk.model.Parties.*;
import static resolutedesk.ui.Art.*;
import static resolutedesk.ui.Draw.*;
import static resolutedesk.ui.Fonts.*;
import static resolutedesk.ui.Theme.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Random;
import resolutedesk.ResoluteDesk;
import resolutedesk.model.Game;

/**
 * The Oval Office, painted in code: a curved cream wall with pilasters and mouldings, three south windows full of
 * daylight and gold drapes, doors, oil portraits under picture lights, a plank floor, the oval rug with its seal,
 * and the Resolute Desk. Sunlight falls from the windows across the room. Everything that doesn't move is painted
 * once into a cached layer; the flags, the globe, the president and the dust in the sunbeams are drawn each frame.
 */
public final class OvalArt {
    private OvalArt() {
    }

    static final Color WALL = rgb(0xf1e6cc), WALL_LO = rgb(0xd6c39b), TRIM = rgb(0xf7f0dd), TRIM_LO = rgb(0xc9b387), GOLD = rgb(0xd4a945), GOLD_LO = rgb(0x8f6a1f),
            WOOD = rgb(0x5b3a22), WOOD_LO = rgb(0x2f1c10), WOOD_HI = rgb(0x8a5a35);

    public static void paint(ResoluteDesk u, Graphics2D g, Game game, int hot) {
        double t = u.tsec;
        boolean rep = game.party == REP;
        layer(g, "oval-room-" + (rep ? "r" : "d"), 0, 56, 1600, 800, (b, x, y, w, h) -> room(b, rep));
        flag(g, 520, 180, t, true);
        flag(g, 1080, 180, t, false);
        globe(g, t);
        Portrait.draw(g, game.pres, 770, 360, 60, 72, u.tsec, 0, true, false);
        dust(g, t);
        if (hot >= 0) doorLight(g, hot);
    }

    // ---- the room, painted once ----
    static void room(Graphics2D g, boolean rep) {
        wall(g);
        windows(g);
        doors(g);
        landscape(g, 330, 150, 120, 150);
        portrait(g, 1290, 160, 110, 140, 1);
        floor(g);
        sunlight(g);
        rug(g, rep);
        sofa(g, 330, 610, 250, true);
        sofa(g, 1020, 610, 250, false);
        coffeeTable(g);
        globeStand(g);
        sideTable(g);
        deskBody(g);
        deskFront(g);
        // the room's light: warm from above, darker at the edges
        glow(g, 800, 300, 900, 420, rgb(0xfff1cf), 0.18);
        vignette(g, 0, 56, 1600, 800, 0.5);
    }

    static void wall(Graphics2D g) {
        // the curved wall: lit in the middle, falling away toward the edges
        vfill(g, 0, 56, 1600, 480, WALL, WALL_LO);
        g.setPaint(lin(0, 0, 1600, 0, new float[]{0f, 0.3f, 0.7f, 1f}, new Color[]{alpha(Color.black, 0.22), alpha(Color.black, 0), alpha(Color.black, 0), alpha(Color.black, 0.22)}));
        g.fill(new Rectangle2D.Double(0, 56, 1600, 480));
        stripes(g, 0, 80, 1600, 360, 26, alpha(Color.white, 0.05));
        speckle(g, 0, 80, 1600, 440, 11, 2600, alpha(Color.white, 0.05), alpha(Color.black, 0.035));
        // crown moulding with dentils
        vfill(g, 0, 56, 1600, 26, TRIM, TRIM_LO);
        g.setColor(alpha(Color.black, 0.18));
        g.fill(new Rectangle2D.Double(0, 82, 1600, 3));
        g.setColor(alpha(TRIM, 0.9));
        for (int x = 0; x < 1600; x += 14) g.fill(new Rectangle2D.Double(x, 70, 8, 8));
        g.setColor(alpha(Color.black, 0.12));
        for (int x = 0; x < 1600; x += 14) g.fill(new Rectangle2D.Double(x + 8, 70, 2, 8));
        // the dado rail and the panels below it
        vfill(g, 0, 436, 1600, 10, TRIM, TRIM_LO);
        g.setColor(alpha(Color.black, 0.16));
        g.fill(new Rectangle2D.Double(0, 446, 1600, 2));
        double[][] panels = {{6, 160}, {300, 150}, {470, 80}, {1100, 70}, {1272, 160}, {1560, 40}};
        for (double[] p : panels) {
            inset(g, p[0] + 8, 458, p[1] - 16, 52, 0.4, 0.18);
            inset(g, p[0] + 14, 464, p[1] - 28, 40, 0.25, 0.1);
        }
        // the baseboard
        vfill(g, 0, 516, 1600, 18, rgb(0xe9dcbc), rgb(0xb59f75));
        g.setColor(alpha(Color.black, 0.3));
        g.fill(new Rectangle2D.Double(0, 532, 1600, 2));
        // fluted pilasters between the doors and the windows
        for (double px : new double[]{172, 478, 1132, 1576, 4}) pilaster(g, px, 86, 22, 430);
    }

    static void pilaster(Graphics2D g, double x, double y, double w, double h) {
        vfill(g, x, y, w, h, TRIM, rgb(0xe2d3ae));
        g.setColor(alpha(Color.black, 0.1));
        for (int k = 1; k < 4; k++) g.fill(new Rectangle2D.Double(x + w * k / 4 - 1, y + 18, 2, h - 36));
        g.setColor(alpha(Color.white, 0.35));
        for (int k = 1; k < 4; k++) g.fill(new Rectangle2D.Double(x + w * k / 4 + 1, y + 18, 1, h - 36));
        vfill(g, x - 4, y, w + 8, 14, TRIM, TRIM_LO);
        vfill(g, x - 4, y + h - 12, w + 8, 12, TRIM, TRIM_LO);
        bevel(g, x, y, w, h, 0.5, 0.2);
        g.setColor(alpha(Color.black, 0.08));
        g.fill(new Rectangle2D.Double(x + w, y, 4, h));
    }

    static void windows(Graphics2D g) {
        // the recess the windows sit in
        g.setColor(alpha(Color.black, 0.12));
        g.fill(new RoundRectangle2D.Double(548, 96, 504, 316, 10, 10));
        vfill(g, 556, 102, 488, 300, rgb(0xe8dab9), rgb(0xcbb68b));
        for (int i = 0; i < 3; i++) {
            double wx = 600 + i * 140, wy = 130, ww = 110, wh = 240;
            // sky, clouds, trees and lawn
            g.setPaint(lin(wx, wy, wx, wy + wh * 0.72, new float[]{0f, 0.6f, 1f}, new Color[]{rgb(0x7fb2e6), rgb(0xb9d9f5), rgb(0xe8f3fb)}));
            g.fill(new Rectangle2D.Double(wx, wy, ww, wh * 0.72));
            Random r = new Random(40 + i);
            for (int k = 0; k < 4; k++) glow(g, wx + r.nextDouble() * ww, wy + 30 + r.nextDouble() * 70, 26 + r.nextDouble() * 20, 9, Color.white, 0.55);
            g.setColor(rgb(0x5f8a52));
            for (int k = 0; k < 9; k++) g.fill(new Ellipse2D.Double(wx - 8 + k * 14, wy + wh * 0.66 - r.nextDouble() * 10, 22, 22));
            g.setPaint(lin(wx, wy + wh * 0.72, wx, wy + wh, new float[]{0f, 1f}, new Color[]{rgb(0x79a85a), rgb(0x56873e)}));
            g.fill(new Rectangle2D.Double(wx, wy + wh * 0.72, ww, wh * 0.28));
            if (i == 1) monument(g, wx + ww / 2, wy + 30, wy + wh * 0.72);
            // the glass catches the light
            gloss(g, wx, wy, ww, wh, 0.12);
            // sash and mullions
            g.setColor(rgb(0xfbf7ec));
            g.setStroke(new BasicStroke(6f));
            g.draw(new Rectangle2D.Double(wx, wy, ww, wh));
            g.setStroke(new BasicStroke(2.6f));
            for (int k = 1; k < 3; k++) g.draw(new Line2D.Double(wx + ww * k / 3, wy, wx + ww * k / 3, wy + wh));
            for (int k = 1; k < 5; k++) g.draw(new Line2D.Double(wx, wy + wh * k / 5, wx + ww, wy + wh * k / 5));
            g.setColor(alpha(Color.black, 0.12));
            g.setStroke(new BasicStroke(1f));
            for (int k = 1; k < 3; k++) g.draw(new Line2D.Double(wx + ww * k / 3 + 1.5, wy, wx + ww * k / 3 + 1.5, wy + wh));
            g.draw(new Rectangle2D.Double(wx + 3, wy + 3, ww - 6, wh - 6));
            // the sill
            vfill(g, wx - 10, wy + wh + 2, ww + 20, 10, TRIM, TRIM_LO);
            drapes(g, wx, wy, ww, wh);
        }
        valance(g, 560, 104, 480);
    }

    static void monument(Graphics2D g, double cx, double top, double base) {
        Path2D lit = new Path2D.Double(), dark = new Path2D.Double();
        lit.moveTo(cx - 6, base); lit.lineTo(cx - 4, top + 10); lit.lineTo(cx, top); lit.lineTo(cx, base); lit.closePath();
        dark.moveTo(cx, base); dark.lineTo(cx, top); dark.lineTo(cx + 4, top + 10); dark.lineTo(cx + 6, base); dark.closePath();
        g.setColor(rgb(0xfbf8f0)); g.fill(lit);
        g.setColor(rgb(0xd9d3c4)); g.fill(dark);
    }

    static void drapes(Graphics2D g, double wx, double wy, double ww, double wh) {
        for (int side = 0; side < 2; side++) {
            double dx = side == 0 ? wx - 28 : wx + ww - 6;
            Path2D d = new Path2D.Double();
            d.moveTo(dx, wy - 18); d.lineTo(dx + 34, wy - 18);
            d.curveTo(dx + 30, wy + wh * 0.5, dx + 38, wy + wh * 0.8, dx + 36, wy + wh + 34);
            d.lineTo(dx - 4, wy + wh + 34);
            d.curveTo(dx + 4, wy + wh * 0.7, dx - 2, wy + wh * 0.4, dx, wy - 18);
            d.closePath();
            // folds: light and dark bands across the width of the drape
            g.setPaint(lin(dx, 0, dx + 36, 0, new float[]{0f, 0.18f, 0.36f, 0.55f, 0.75f, 1f},
                    new Color[]{rgb(0x9c7022), rgb(0xf3cf6c), rgb(0xb5862c), rgb(0xf0c75e), rgb(0xa57826), rgb(0xd9ad48)}));
            g.fill(d);
            g.setPaint(lin(0, wy, 0, wy + wh + 34, new float[]{0f, 0.5f, 1f}, new Color[]{alpha(Color.white, 0.12), alpha(Color.white, 0), alpha(Color.black, 0.25)}));
            g.fill(d);
            // the tie-back
            double ty = wy + wh * 0.62;
            g.setColor(GOLD_LO);
            g.fill(new RoundRectangle2D.Double(dx + 2, ty, 32, 7, 6, 6));
            g.setColor(alpha(rgb(0xfff0b3), 0.6));
            g.fill(new RoundRectangle2D.Double(dx + 4, ty + 1, 28, 2, 2, 2));
        }
    }

    static void valance(Graphics2D g, double x, double y, double w) {
        vfill(g, x, y, w, 14, rgb(0xe2b552), GOLD_LO);
        int swags = 6;
        for (int i = 0; i < swags; i++) {
            double sx = x + w * i / swags, sw = w / swags;
            Path2D s = new Path2D.Double();
            s.moveTo(sx, y + 12); s.curveTo(sx + sw * 0.3, y + 40, sx + sw * 0.7, y + 40, sx + sw, y + 12); s.lineTo(sx + sw, y + 18);
            s.curveTo(sx + sw * 0.7, y + 46, sx + sw * 0.3, y + 46, sx, y + 18); s.closePath();
            g.setPaint(lin(0, y + 12, 0, y + 46, new float[]{0f, 0.6f, 1f}, new Color[]{rgb(0xf2cb63), rgb(0xc4932f), rgb(0x8c6420)}));
            g.fill(s);
            g.setColor(rgb(0xf6dc8a));
            for (int k = 0; k < 6; k++) { double fx = sx + sw * (k + 0.5) / 6; g.fill(new Rectangle2D.Double(fx - 0.6, y + 40 + Math.sin(k / 5.0 * Math.PI) * 4, 1.2, 6)); }
        }
        g.setColor(alpha(Color.white, 0.4));
        g.fill(new Rectangle2D.Double(x, y + 1, w, 2));
    }

    static void doors(Graphics2D g) {
        door(g, 60, 210, 90, 290);
        door(g, 215, 160, 70, 260);
        door(g, 1190, 170, 70, 240);
        door(g, 1450, 210, 90, 290);
    }

    static void door(Graphics2D g, double x, double y, double w, double h) {
        // the casing and pediment
        g.setColor(alpha(Color.black, 0.12));
        g.fill(new Rectangle2D.Double(x - 8, y - 20, w + 24, h + 20));
        vfill(g, x - 12, y - 22, w + 24, h + 22, TRIM, rgb(0xddcca5));
        Path2D ped = new Path2D.Double();
        ped.moveTo(x - 20, y - 22); ped.lineTo(x + w / 2, y - 54); ped.lineTo(x + w + 20, y - 22); ped.closePath();
        g.setPaint(lin(0, y - 54, 0, y - 22, new float[]{0f, 1f}, new Color[]{TRIM, TRIM_LO}));
        g.fill(ped);
        g.setColor(alpha(Color.black, 0.18));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(new Line2D.Double(x - 20, y - 22, x + w + 20, y - 22));
        g.setStroke(new BasicStroke(1f));
        bevel(g, x - 12, y - 22, w + 24, h + 22, 0.5, 0.2);
        // the door itself: raised panels in white paint
        g.setPaint(lin(x, y, x + w, y + h, new float[]{0f, 1f}, new Color[]{rgb(0xfbf5e5), rgb(0xe0d2b0)}));
        g.fill(new Rectangle2D.Double(x, y, w, h));
        double pw = w - 18;
        double[][] ps = {{y + 10, h * 0.38}, {y + h * 0.46, h * 0.48}};
        for (double[] p : ps) {
            inset(g, x + 9, p[0], pw, p[1], 0.6, 0.22);
            bevel(g, x + 15, p[0] + 6, pw - 12, p[1] - 12, 0.5, 0.15);
        }
        g.setColor(alpha(Color.black, 0.25));
        g.fill(new Rectangle2D.Double(x + w - 2, y, 2, h));
        // the brass knob
        double kx = x + w - 16, ky = y + h * 0.5;
        g.setColor(GOLD_LO); g.fill(new Ellipse2D.Double(kx - 1, ky - 1, 10, 10));
        g.setColor(GOLD); g.fill(new Ellipse2D.Double(kx, ky, 8, 8));
        g.setColor(alpha(Color.white, 0.7)); g.fill(new Ellipse2D.Double(kx + 1.5, ky + 1.5, 3, 3));
    }

    /** A gilt frame with its shadow on the wall, under a brass picture light. */
    static void giltFrame(Graphics2D g, double x, double y, double w, double h) {
        shadow(g, new Rectangle2D.Double(x - 6, y - 4, w + 12, h + 16), 8, 0.25);
        for (int k = 0; k < 3; k++) {
            double m = 12 - k * 4;
            g.setPaint(lin(x - m, y - m, x + w + m, y + h + m, new float[]{0f, 0.5f, 1f}, new Color[]{rgb(0xf1d17a), k == 1 ? GOLD_LO : GOLD, rgb(0x7a5718)}));
            g.fill(new Rectangle2D.Double(x - m, y - m, w + m * 2, h + m * 2));
        }
    }

    static void pictureLight(Graphics2D g, double x, double y, double w, double h) {
        double cx = x + w / 2;
        g.setColor(GOLD_LO);
        g.fill(new Rectangle2D.Double(cx - 2, y - 22, 4, 12));
        g.setPaint(lin(0, y - 14, 0, y - 6, new float[]{0f, 1f}, new Color[]{rgb(0xf1d17a), GOLD_LO}));
        g.fill(new RoundRectangle2D.Double(cx - w * 0.3, y - 14, w * 0.6, 9, 6, 6));
        glow(g, cx, y + h * 0.15, w * 0.7, h * 0.35, rgb(0xfff3d6), 0.22);
    }

    /** A Hudson River landscape: mountains, a lake and a stormy sky clearing. */
    static void landscape(Graphics2D g, double x, double y, double w, double h) {
        giltFrame(g, x, y, w, h);
        g.setPaint(lin(x, y, x, y + h * 0.6, new float[]{0f, 0.55f, 1f}, new Color[]{rgb(0x56606a), rgb(0xc9b688), rgb(0xe8d6a6)}));
        g.fill(new Rectangle2D.Double(x, y, w, h * 0.6));
        glow(g, x + w * 0.62, y + h * 0.42, w * 0.4, h * 0.2, rgb(0xfff0c0), 0.6);
        Path2D far = new Path2D.Double();
        far.moveTo(x, y + h * 0.58); far.lineTo(x + w * 0.2, y + h * 0.42); far.lineTo(x + w * 0.38, y + h * 0.5); far.lineTo(x + w * 0.55, y + h * 0.36);
        far.lineTo(x + w * 0.75, y + h * 0.52); far.lineTo(x + w, y + h * 0.46); far.lineTo(x + w, y + h * 0.62); far.lineTo(x, y + h * 0.62); far.closePath();
        g.setColor(rgb(0x7c8478)); g.fill(far);
        g.setPaint(lin(x, y + h * 0.6, x, y + h * 0.78, new float[]{0f, 1f}, new Color[]{rgb(0xd8c79c), rgb(0x8f8a70)}));
        g.fill(new Rectangle2D.Double(x, y + h * 0.6, w, h * 0.18));
        Path2D near = new Path2D.Double();
        near.moveTo(x, y + h * 0.7); near.curveTo(x + w * 0.2, y + h * 0.62, x + w * 0.35, y + h * 0.8, x + w * 0.5, y + h * 0.78);
        near.curveTo(x + w * 0.7, y + h * 0.76, x + w * 0.85, y + h * 0.86, x + w, y + h * 0.82); near.lineTo(x + w, y + h); near.lineTo(x, y + h); near.closePath();
        g.setColor(rgb(0x3e3a24)); g.fill(near);
        g.setColor(rgb(0x2c2a18));
        g.fill(new Rectangle2D.Double(x + w * 0.14, y + h * 0.36, 3, h * 0.4));
        g.fill(new Ellipse2D.Double(x + w * 0.04, y + h * 0.28, w * 0.24, h * 0.16));
        speckle(g, x, y, w, h, 71, 520, alpha(Color.white, 0.05), alpha(Color.black, 0.07));
        g.setPaint(lin(x, y, x, y + h, new float[]{0f, 1f}, new Color[]{alpha(rgb(0xc9a35a), 0.12), alpha(rgb(0x3a2a10), 0.22)}));
        g.fill(new Rectangle2D.Double(x, y, w, h));
        pictureLight(g, x, y, w, h);
    }

    /** An oil portrait of a statesman of long ago, lit from the left against a dark ground. */
    static void portrait(Graphics2D g, double x, double y, double w, double h, int who) {
        giltFrame(g, x, y, w, h);
        // the painting: a sitter lit from the left against a dark ground
        Color ground = who == 0 ? rgb(0x2c3442) : rgb(0x3d2f26);
        g.setPaint(lin(x, y, x + w, y + h, new float[]{0f, 1f}, new Color[]{shade(ground, 1.35), shade(ground, 0.65)}));
        g.fill(new Rectangle2D.Double(x, y, w, h));
        glow(g, x + w * 0.38, y + h * 0.35, w * 0.6, h * 0.5, rgb(0xd9b27a), 0.25);
        double cx = x + w * 0.5, cy = y + h * 0.4;
        // shoulders and coat
        Path2D coat = new Path2D.Double();
        coat.moveTo(x + w * 0.1, y + h); coat.curveTo(x + w * 0.15, y + h * 0.66, x + w * 0.3, y + h * 0.6, cx, y + h * 0.62);
        coat.curveTo(x + w * 0.7, y + h * 0.6, x + w * 0.85, y + h * 0.66, x + w * 0.9, y + h); coat.closePath();
        g.setPaint(lin(x, 0, x + w, 0, new float[]{0f, 1f}, new Color[]{rgb(0x2a2a30), rgb(0x101014)}));
        g.fill(coat);
        g.setColor(rgb(0xe9e2d0));
        Path2D collar = new Path2D.Double();
        collar.moveTo(cx - w * 0.08, y + h * 0.62); collar.lineTo(cx, y + h * 0.78); collar.lineTo(cx + w * 0.08, y + h * 0.62); collar.closePath();
        g.fill(collar);
        // the head, lit on one side
        g.setPaint(lin(cx - w * 0.16, 0, cx + w * 0.16, 0, new float[]{0f, 1f}, new Color[]{rgb(0xe3b892), rgb(0x8c6248)}));
        g.fill(new Ellipse2D.Double(cx - w * 0.15, cy - h * 0.16, w * 0.3, h * 0.36));
        // brow, eyes, nose and mouth, in soft shadow as a painter would leave them
        Color sh = alpha(rgb(0x4a2c1c), 0.55);
        g.setColor(sh);
        g.fill(new Ellipse2D.Double(cx - w * 0.09, cy - h * 0.01, w * 0.06, h * 0.025));
        g.fill(new Ellipse2D.Double(cx + w * 0.035, cy - h * 0.01, w * 0.06, h * 0.025));
        g.setColor(alpha(rgb(0x4a2c1c), 0.35));
        Path2D nose = new Path2D.Double();
        nose.moveTo(cx + w * 0.005, cy); nose.lineTo(cx + w * 0.03, cy + h * 0.08); nose.lineTo(cx - w * 0.005, cy + h * 0.085); nose.closePath();
        g.fill(nose);
        g.setColor(alpha(rgb(0x5a2e22), 0.6));
        g.fill(new RoundRectangle2D.Double(cx - w * 0.045, cy + h * 0.115, w * 0.09, h * 0.012, 2, 2));
        g.setColor(who == 0 ? rgb(0xcfc8bc) : rgb(0x4a3526));
        g.fill(new Arc2D.Double(cx - w * 0.16, cy - h * 0.18, w * 0.32, h * 0.22, 0, 180, Arc2D.CHORD));
        // canvas weave and old varnish
        speckle(g, x, y, w, h, 70 + who, 520, alpha(Color.white, 0.05), alpha(Color.black, 0.07));
        g.setPaint(lin(x, y, x, y + h, new float[]{0f, 1f}, new Color[]{alpha(rgb(0xc9a35a), 0.1), alpha(rgb(0x3a2a10), 0.2)}));
        g.fill(new Rectangle2D.Double(x, y, w, h));
        pictureLight(g, x, y, w, h);
    }

    static void floor(Graphics2D g) {
        vfill(g, 0, 534, 1600, 322, rgb(0x6e4728), rgb(0x3e2716));
        // planks running toward the windows, each its own shade, with grain
        Random r = new Random(5);
        double vx = 800, vy = 300;
        for (int i = -56; i <= 56; i++) {
            double x0 = vx + i * 36 * (534 - vy) / (856 - vy) * 1.0, x1 = vx + i * 36 * 1.0;
            double x0b = vx + (i + 1) * 36 * (534 - vy) / (856 - vy), x1b = vx + (i + 1) * 36;
            Path2D p = new Path2D.Double();
            p.moveTo(x0, 534); p.lineTo(x0b, 534); p.lineTo(x1b * 1.0 + (x1b - vx) * 0.9, 856); p.lineTo(x1 + (x1 - vx) * 0.9, 856); p.closePath();
            g.setColor(alpha(r.nextBoolean() ? Color.white : Color.black, 0.02 + r.nextDouble() * 0.05));
            g.fill(p);
            g.setColor(alpha(Color.black, 0.3));
            g.draw(new Line2D.Double(x0, 534, x1 + (x1 - vx) * 0.9, 856));
        }
        grain(g, 0, 540, 1600, 316, 9, 260, alpha(rgb(0x1f120a), 0.2), alpha(rgb(0xb07a4c), 0.12));
        // the polish reflects the windows
        for (int i = 0; i < 3; i++) glow(g, 655 + i * 140, 600, 48, 70, rgb(0xfff2d8), 0.16);
        g.setColor(alpha(Color.black, 0.35));
        g.fill(new Rectangle2D.Double(0, 534, 1600, 4));
    }

    static void sunlight(Graphics2D g) {
        // beams in the air from the windows, and the patches they lay on the floor
        for (int i = 0; i < 3; i++) {
            double wx = 600 + i * 140;
            Path2D beam = new Path2D.Double();
            beam.moveTo(wx + 20 - i * 10, 536); beam.lineTo(wx + 120 - i * 10, 536); beam.lineTo(wx + 200 - i * 60, 780); beam.lineTo(wx - 20 - i * 60, 780); beam.closePath();
            g.setPaint(lin(0, 536, 0, 780, new float[]{0f, 1f}, new Color[]{alpha(rgb(0xfff4d6), 0.13), alpha(rgb(0xfff4d6), 0)}));
            g.fill(beam);
            glow(g, wx + 90 - i * 60, 700, 120, 40, rgb(0xffefc4), 0.14);
        }
    }

    static void rug(Graphics2D g, boolean rep) {
        Color field = rep ? rgb(0xb98a2e) : rgb(0x1f3a7a), deep = shade(field, 0.62), light = shade(field, 1.25);
        floorShadow(g, 800, 718, 1210, 330, 0.45);
        g.setColor(deep);
        g.fill(new Ellipse2D.Double(220, 558, 1160, 304));
        // the field, lit from the windows
        AffineTransform at = g.getTransform();
        g.translate(800, 710);
        g.scale(1, 0.255);
        g.setPaint(new RadialGradientPaint(new Point2D.Double(0, -180), 640f, new float[]{0f, 0.7f, 1f}, new Color[]{light, field, shade(field, 0.82)}));
        g.fill(new Ellipse2D.Double(-560, -557, 1120, 1114));
        g.setTransform(at);
        // the border bands and a ring of stars
        g.setStroke(new BasicStroke(3f));
        g.setColor(alpha(rgb(0xf2c14e), 0.75));
        g.draw(new Ellipse2D.Double(266, 578, 1068, 264));
        g.setStroke(new BasicStroke(1.2f));
        g.setColor(alpha(rgb(0xf2c14e), 0.45));
        g.draw(new Ellipse2D.Double(284, 584, 1032, 252));
        g.setColor(alpha(rgb(0xf6d77f), 0.8));
        for (int i = 0; i < 50; i++) {
            double a = i * Math.PI * 2 / 50, sx = 800 + Math.cos(a) * 524, sy = 710 + Math.sin(a) * 129;
            star(g, sx, sy, 4.2, 0.4);
        }
        // the sunburst
        for (int i = 0; i < 32; i++) {
            double a0 = i * Math.PI * 2 / 32, a1 = a0 + Math.PI / 32;
            Path2D ray = new Path2D.Double();
            ray.moveTo(800 + Math.cos(a0) * 150, 710 + Math.sin(a0) * 40);
            ray.lineTo(800 + Math.cos(a0) * 470, 710 + Math.sin(a0) * 116);
            ray.lineTo(800 + Math.cos(a1) * 470, 710 + Math.sin(a1) * 116);
            ray.lineTo(800 + Math.cos(a1) * 150, 710 + Math.sin(a1) * 40);
            ray.closePath();
            g.setColor(alpha(i % 2 == 0 ? rgb(0xf6d77f) : Color.black, i % 2 == 0 ? 0.16 : 0.06));
            g.fill(ray);
        }
        speckle(g, 240, 570, 1120, 280, 21, 5200, alpha(Color.white, 0.04), alpha(Color.black, 0.05));
        // the seal at the center, laid flat
        g.translate(800, 710);
        g.scale(1, 0.3);
        Oval.seal(g, 0, 0, 124);
        g.setTransform(at);
        // light on the rug from the windows
        for (int i = 0; i < 3; i++) glow(g, 690 + i * 120, 680, 120, 34, rgb(0xfff1c8), 0.12);
    }

    static void star(Graphics2D g, double cx, double cy, double r, double flat) {
        Path2D s = new Path2D.Double();
        for (int k = 0; k < 10; k++) {
            double a = -Math.PI / 2 + k * Math.PI / 5, rr = k % 2 == 0 ? r : r * 0.45;
            double px = cx + Math.cos(a) * rr, py = cy + Math.sin(a) * rr * (1 - flat);
            if (k == 0) s.moveTo(px, py); else s.lineTo(px, py);
        }
        s.closePath();
        g.fill(s);
    }

    static void sofa(Graphics2D g, double x, double y, double w, boolean left) {
        Color c = rgb(0xece0c4), lo = rgb(0xb5a27c);
        floorShadow(g, x + w / 2, y + 96, w + 60, 34, 0.45);
        // the back
        g.setPaint(lin(0, y, 0, y + 52, new float[]{0f, 1f}, new Color[]{shade(c, 1.06), shade(c, 0.86)}));
        g.fill(new RoundRectangle2D.Double(x, y, w, 52, 22, 22));
        g.setColor(alpha(Color.white, 0.35));
        g.fill(new RoundRectangle2D.Double(x + 12, y + 4, w - 24, 4, 4, 4));
        // the base and skirt
        g.setPaint(lin(0, y + 40, 0, y + 92, new float[]{0f, 1f}, new Color[]{shade(c, 0.88), lo}));
        g.fill(new RoundRectangle2D.Double(x - 2, y + 40, w + 4, 52, 16, 16));
        // seat cushions, with seams and piping
        double cw = (w - 28) / 3;
        for (int k = 0; k < 3; k++) {
            double cx = x + 14 + k * cw;
            g.setPaint(lin(0, y + 34, 0, y + 70, new float[]{0f, 1f}, new Color[]{shade(c, 1.04), shade(c, 0.84)}));
            g.fill(new RoundRectangle2D.Double(cx + 1, y + 34, cw - 2, 36, 14, 14));
            g.setColor(alpha(lo, 0.7));
            g.draw(new RoundRectangle2D.Double(cx + 1, y + 34, cw - 2, 36, 14, 14));
            g.setColor(alpha(Color.white, 0.25));
            g.fill(new RoundRectangle2D.Double(cx + 8, y + 37, cw - 16, 4, 4, 4));
        }
        // the arms
        for (int s = 0; s < 2; s++) {
            double ax = (s == 0) == left ? x - 10 : x + w - 22;
            g.setPaint(lin(ax, 0, ax + 32, 0, new float[]{0f, 0.5f, 1f}, new Color[]{shade(c, 0.82), shade(c, 1.04), shade(c, 0.8)}));
            g.fill(new RoundRectangle2D.Double(ax, y + 16, 32, 72, 14, 14));
        }
        // turned wooden feet
        g.setColor(WOOD_LO);
        for (double fx : new double[]{x + 8, x + w - 14}) g.fill(new RoundRectangle2D.Double(fx, y + 90, 7, 10, 3, 3));
        speckle(g, x, y, w, 92, (long) x, 700, alpha(Color.white, 0.05), alpha(Color.black, 0.04));
    }

    static void coffeeTable(Graphics2D g) {
        floorShadow(g, 800, 826, 260, 30, 0.5);
        g.setPaint(lin(0, 776, 0, 820, new float[]{0f, 1f}, new Color[]{WOOD, WOOD_LO}));
        g.fill(new RoundRectangle2D.Double(690, 780, 220, 40, 8, 8));
        g.setPaint(lin(690, 0, 910, 0, new float[]{0f, 0.5f, 1f}, new Color[]{WOOD, WOOD_HI, WOOD}));
        g.fill(new RoundRectangle2D.Double(684, 768, 232, 18, 8, 8));
        grain(g, 690, 770, 220, 14, 33, 30, alpha(WOOD_LO, 0.4), alpha(rgb(0xc08a5a), 0.25));
        gloss(g, 684, 768, 232, 18, 0.2);
        // today's papers
        g.setColor(alpha(Color.black, 0.25));
        g.fill(new Rectangle2D.Double(732, 763, 70, 14));
        g.setColor(rgb(0xf6f2e6));
        g.fill(new Rectangle2D.Double(730, 760, 70, 14));
        g.fill(new Rectangle2D.Double(812, 762, 64, 12));
        g.setColor(rgb(0x333333));
        g.fill(new Rectangle2D.Double(736, 763, 56, 2));
        g.setColor(alpha(Color.black, 0.35));
        for (int k = 0; k < 3; k++) g.fill(new Rectangle2D.Double(736, 767 + k * 2, 30 + k * 6, 1));
    }

    static void globeStand(Graphics2D g) {
        floorShadow(g, 356, 568, 86, 16, 0.5);
        g.setPaint(lin(348, 0, 360, 0, new float[]{0f, 0.5f, 1f}, new Color[]{WOOD_LO, WOOD_HI, WOOD_LO}));
        g.fill(new Rectangle2D.Double(350, 512, 8, 52));
        g.setPaint(lin(0, 560, 0, 570, new float[]{0f, 1f}, new Color[]{WOOD_HI, WOOD_LO}));
        g.fill(new RoundRectangle2D.Double(326, 560, 58, 9, 4, 4));
    }

    static void sideTable(Graphics2D g) {
        floorShadow(g, 1350, 568, 150, 22, 0.5);
        g.setPaint(lin(0, 500, 0, 564, new float[]{0f, 1f}, new Color[]{WOOD, WOOD_LO}));
        g.fill(new Rectangle2D.Double(1296, 500, 108, 64));
        inset(g, 1304, 508, 92, 48, 0.12, 0.3);
        g.setPaint(lin(1290, 0, 1410, 0, new float[]{0f, 0.5f, 1f}, new Color[]{WOOD, WOOD_HI, WOOD}));
        g.fill(new Rectangle2D.Double(1288, 490, 124, 13));
        gloss(g, 1288, 490, 124, 13, 0.25);
        // a lamp
        g.setColor(GOLD_LO);
        g.fill(new Rectangle2D.Double(1392, 442, 4, 48));
        g.fill(new RoundRectangle2D.Double(1382, 486, 24, 5, 3, 3));
        Path2D shade = new Path2D.Double();
        shade.moveTo(1378, 444); shade.lineTo(1410, 444); shade.lineTo(1402, 418); shade.lineTo(1386, 418); shade.closePath();
        g.setPaint(lin(1378, 0, 1410, 0, new float[]{0f, 1f}, new Color[]{rgb(0xfbf1d8), rgb(0xd8c49a)}));
        g.fill(shade);
        glow(g, 1394, 450, 60, 40, rgb(0xfff0c4), 0.3);
        // the briefing book
        g.setColor(alpha(Color.black, 0.3));
        g.fill(new Rectangle2D.Double(1314, 474, 76, 18));
        g.setPaint(lin(0, 470, 0, 492, new float[]{0f, 1f}, new Color[]{rgb(0x24407e), rgb(0x142650)}));
        g.fill(new Rectangle2D.Double(1312, 470, 76, 22));
        g.setColor(GOLD);
        g.fill(new Rectangle2D.Double(1312, 470, 4, 22));
        text(g, "CEA", 1336, 486, F_TB, C_GOLD);
    }

    /** The Resolute Desk and the chair behind it. */
    static void deskBody(Graphics2D g) {
        // the chair
        g.setPaint(lin(762, 0, 838, 0, new float[]{0f, 0.45f, 1f}, new Color[]{rgb(0x1e130c), rgb(0x4a3020), rgb(0x1a100a)}));
        g.fill(new RoundRectangle2D.Double(758, 394, 84, 86, 24, 24));
        g.setColor(alpha(Color.white, 0.12));
        g.fill(new RoundRectangle2D.Double(772, 400, 18, 70, 10, 10));
        floorShadow(g, 800, 600, 360, 40, 0.6);
    }

    /** The front of the desk: carved panels, the kneehole door with the seal, and what's on the desk top. */
    static void deskFront(Graphics2D g) {
        // the body: oak timbers from HMS Resolute, carved panels, a kneehole door with the seal
        g.setPaint(lin(0, 460, 0, 592, new float[]{0f, 1f}, new Color[]{rgb(0x6a4026), rgb(0x34200f)}));
        g.fill(new Rectangle2D.Double(660, 460, 280, 132));
        grain(g, 660, 462, 280, 128, 3, 70, alpha(rgb(0x1c1008), 0.35), alpha(rgb(0xa8764a), 0.18));
        for (int k = 0; k < 2; k++) {
            double px = 672 + k * 186;
            inset(g, px, 478, 70, 102, 0.12, 0.45);
            bevel(g, px + 6, 484, 58, 90, 0.18, 0.4);
            g.setColor(alpha(GOLD, 0.35));
            g.draw(new Ellipse2D.Double(px + 22, 515, 26, 26));
        }
        g.setPaint(lin(0, 480, 0, 588, new float[]{0f, 1f}, new Color[]{rgb(0x553320), rgb(0x2c190c)}));
        g.fill(new Rectangle2D.Double(752, 480, 96, 108));
        g.setColor(alpha(GOLD, 0.55));
        g.draw(new Rectangle2D.Double(756, 484, 88, 100));
        Oval.seal(g, 800, 530, 27);
        // the top, with a polished edge
        g.setPaint(lin(648, 0, 952, 0, new float[]{0f, 0.5f, 1f}, new Color[]{rgb(0x6e442a), rgb(0x9a6640), rgb(0x6e442a)}));
        g.fill(new Rectangle2D.Double(648, 446, 304, 20));
        gloss(g, 648, 446, 304, 20, 0.22);
        g.setColor(alpha(Color.black, 0.45));
        g.fill(new Rectangle2D.Double(648, 466, 304, 3));
        // what's on it: papers, a telephone, a pen set
        g.setColor(rgb(0xf6f2e6));
        g.fill(new Rectangle2D.Double(700, 438, 60, 10));
        g.setColor(alpha(Color.black, 0.2));
        g.fill(new Rectangle2D.Double(704, 446, 56, 2));
        g.setColor(rgb(0x1e1e22));
        g.fill(new RoundRectangle2D.Double(866, 436, 38, 13, 4, 4));
        g.setColor(alpha(Color.white, 0.25));
        g.fill(new RoundRectangle2D.Double(870, 437, 30, 3, 2, 2));
        g.setColor(GOLD);
        g.fill(new RoundRectangle2D.Double(786, 440, 28, 7, 3, 3));
    }

    // ---- what moves ----

    static void flag(Graphics2D g, double x, double y, double t, boolean us) {
        // the pole and its finial
        g.setPaint(lin(x - 3, 0, x + 3, 0, new float[]{0f, 0.5f, 1f}, new Color[]{GOLD_LO, rgb(0xf1d17a), GOLD_LO}));
        g.fill(new Rectangle2D.Double(x - 2.5, y - 40, 5, 362));
        g.setColor(GOLD);
        g.fill(new Ellipse2D.Double(x - 6, y - 52, 12, 12));
        g.setColor(alpha(Color.white, 0.6));
        g.fill(new Ellipse2D.Double(x - 3, y - 49, 4, 4));
        floorShadow(g, x, y + 324, 40, 8, 0.4);
        // the cloth in folds, swaying a little
        for (int k = 0; k < 7; k++) {
            Color c = us ? (k % 2 == 0 ? rgb(0xb22234) : rgb(0xf5f3ee)) : (k < 4 ? rgb(0x1a2f63) : rgb(0x223c78));
            Path2D p = new Path2D.Double();
            double sway = Math.sin(t * 0.8 + k * 0.4) * 2;
            p.moveTo(x + 2, y - 30 + k * 26);
            p.curveTo(x + 18, y - 26 + k * 26, x + 10 + sway, y - 10 + k * 26, x + 26 + sway, y - 4 + k * 26);
            p.lineTo(x + 26 + sway, y + 22 + k * 26);
            p.curveTo(x + 10 + sway, y + 16 + k * 26, x + 18, y + 2 + k * 26, x + 2, y - 4 + k * 26);
            p.closePath();
            g.setPaint(lin(x, 0, x + 28, 0, new float[]{0f, 0.4f, 0.7f, 1f}, new Color[]{shade(c, 0.7), shade(c, 1.08), shade(c, 0.82), shade(c, 0.95)}));
            g.fill(p);
        }
        if (us) {
            g.setColor(rgb(0x3c3b6e));
            g.fill(new Rectangle2D.Double(x + 2, y - 30, 14, 60));
            g.setColor(alpha(Color.white, 0.7));
            for (int k = 0; k < 6; k++) g.fill(new Ellipse2D.Double(x + 5 + (k % 2) * 5, y - 24 + k * 9, 2.2, 2.2));
        } else {
            g.setColor(C_GOLD);
            g.fill(new Ellipse2D.Double(x + 6, y + 30, 14, 14));
        }
        // gold fringe at the bottom
        g.setColor(alpha(GOLD, 0.85));
        g.fill(new Rectangle2D.Double(x + 2, y + 152, 24, 4));
    }

    static void globe(Graphics2D g, double t) {
        double cx = 356, cy = 476, r = 38;
        g.setPaint(new RadialGradientPaint(new Point2D.Double(cx - 12, cy - 14), (float) (r * 1.4), new float[]{0f, 1f}, new Color[]{rgb(0x8cc3e0), rgb(0x1f4a68)}));
        g.fill(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
        Shape clip = g.getClip();
        g.clip(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
        double spin = (t * 6) % 120;
        g.setColor(rgb(0xb7c98a));
        for (int k = 0; k < 4; k++) {
            double lx = cx - r - 20 + ((spin + k * 30) % 120);
            g.fill(new Ellipse2D.Double(lx - 9, cy - 22 + k * 13, 18 + k * 4, 10 + (k % 2) * 4));
        }
        g.setColor(alpha(Color.white, 0.18));
        for (int k = -2; k <= 2; k++) g.draw(new Ellipse2D.Double(cx - r * Math.abs(Math.cos(k * 0.5 + spin * 0.05)), cy - r, r * 2 * Math.abs(Math.cos(k * 0.5 + spin * 0.05)), r * 2));
        g.setClip(clip);
        glow(g, cx - 13, cy - 15, 16, 12, Color.white, 0.5);
        g.setColor(alpha(Color.black, 0.25));
        g.setStroke(new BasicStroke(2f));
        g.draw(new Arc2D.Double(cx - r, cy - r, r * 2, r * 2, 250, 140, Arc2D.OPEN));
        // the brass meridian
        g.setColor(GOLD);
        g.setStroke(new BasicStroke(2.4f));
        g.draw(new Arc2D.Double(cx - r - 6, cy - r - 6, (r + 6) * 2, (r + 6) * 2, 200, 320, Arc2D.OPEN));
        g.setStroke(new BasicStroke(1f));
    }

    /** Motes of dust drifting through the sunbeams. */
    static void dust(Graphics2D g, double t) {
        Random r = new Random(77);
        for (int i = 0; i < 26; i++) {
            double bx = 600 + r.nextDouble() * 380, by = 380 + r.nextDouble() * 300, sp = 0.2 + r.nextDouble() * 0.5, ph = r.nextDouble() * 6.28;
            double x = bx + Math.sin(t * sp + ph) * 14 + ((t * 3 * sp) % 30), y = by + Math.cos(t * sp * 0.7 + ph) * 10;
            double a = 0.12 + 0.12 * Math.sin(t * sp * 2 + ph);
            g.setColor(alpha(rgb(0xfff5dc), Math.max(0, a)));
            g.fill(new Ellipse2D.Double(x, y, 2, 2));
        }
    }

    /** Light spilling from a door, or a glow on an object, when the pointer is over it. */
    static void doorLight(Graphics2D g, int hot) {
        double[] s = Oval.SPOT[hot];
        glow(g, s[0] + s[2] / 2, s[1] + s[3] / 2, s[2] * 0.9, s[3] * 0.6, rgb(0xfff0c4), 0.18);
    }
}
