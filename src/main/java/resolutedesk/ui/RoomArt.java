package resolutedesk.ui;

import static resolutedesk.ui.Art.*;
import static resolutedesk.ui.RoomIds.*;
import static resolutedesk.ui.Theme.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Random;

/**
 * Behind each room's panels, the room itself, dimmed so the work stays readable: walnut by lamplight at the desk,
 * the Yellow Oval Room in the Residence, the red Roosevelt Room, the glowing screens of the Situation Room, the
 * blue curtain of the briefing room and the coral drapes of the Cabinet Room.
 */
public final class RoomArt {
    private RoomArt() {
    }

    public static void backdrop(Graphics2D g, int room) {
        layer(g, "room-" + room, 0, 56, 1600, 844, (b, x, y, w, h) -> paint(b, room));
    }

    static void paint(Graphics2D g, int room) {
        switch (room) {
            case R_DESK: walnut(g); break;
            case R_COUNTRY: nightWindow(g); break;
            case R_ECON: study(g); break;
            case R_RESIDENCE: yellowOval(g); break;
            case R_WESTWING: corridor(g); break;
            case R_CONGRESS: roosevelt(g); break;
            case R_SITROOM: sitRoom(g); break;
            case R_PRESS: briefing(g); break;
            default: cabinetRoom(g); break;   // the policy room works at the Cabinet table
        }
        // dim it all, more at the top where the titles sit, and close in the edges
        g.setPaint(lin(0, 56, 0, 900, new float[]{0f, 0.25f, 1f}, new Color[]{alpha(rgb(0x050a14), 0.74), alpha(rgb(0x050a14), 0.5), alpha(rgb(0x050a14), 0.62)}));
        g.fill(new Rectangle2D.Double(0, 56, 1600, 844));
        vignette(g, 0, 56, 1600, 844, 0.6);
    }

    // ---- shared pieces ----

    /** A wall of wood panels: stiles and rails with raised fields, and grain. */
    static void panels(Graphics2D g, double y0, double y1, Color wood, long seed) {
        vfill(g, 0, y0, 1600, y1 - y0, shade(wood, 1.1), shade(wood, 0.75));
        grain(g, 0, y0, 1600, y1 - y0, seed, 380, alpha(shade(wood, 0.5), 0.35), alpha(shade(wood, 1.4), 0.2));
        double pw = 200;
        for (double x = 20; x < 1600; x += pw) {
            inset(g, x, y0 + 30, pw - 40, (y1 - y0) * 0.55, 0.15, 0.4);
            bevel(g, x + 10, y0 + 40, pw - 60, (y1 - y0) * 0.55 - 20, 0.12, 0.3);
            inset(g, x, y0 + 60 + (y1 - y0) * 0.55, pw - 40, (y1 - y0) * 0.3, 0.15, 0.4);
        }
    }

    /** Folds of heavy cloth, as drapes or a curtain. */
    static void cloth(Graphics2D g, double x, double y, double w, double h, Color c, double foldW) {
        int n = (int) Math.ceil(w / foldW);
        for (int i = 0; i < n; i++) {
            double fx = x + i * foldW;
            g.setPaint(lin(fx, 0, fx + foldW, 0, new float[]{0f, 0.3f, 0.6f, 1f}, new Color[]{shade(c, 0.6), shade(c, 1.1), shade(c, 0.85), shade(c, 0.6)}));
            g.fill(new Rectangle2D.Double(fx, y, foldW + 1, h));
        }
        g.setPaint(lin(0, y, 0, y + h, new float[]{0f, 0.6f, 1f}, new Color[]{alpha(Color.white, 0.08), alpha(Color.white, 0), alpha(Color.black, 0.3)}));
        g.fill(new Rectangle2D.Double(x, y, w, h));
    }

    static void frame(Graphics2D g, double x, double y, double w, double h, Color inside) {
        g.setPaint(lin(x, y, x + w, y + h, new float[]{0f, 1f}, new Color[]{rgb(0xe2bd62), rgb(0x7a5718)}));
        g.fill(new Rectangle2D.Double(x - 8, y - 8, w + 16, h + 16));
        g.setPaint(lin(x, y, x, y + h, new float[]{0f, 1f}, new Color[]{shade(inside, 1.3), shade(inside, 0.7)}));
        g.fill(new Rectangle2D.Double(x, y, w, h));
        glow(g, x + w * 0.4, y + h * 0.4, w * 0.5, h * 0.4, rgb(0xe9c890), 0.25);
    }

    static void floor(Graphics2D g, double y, Color c) {
        vfill(g, 0, y, 1600, 900 - y, shade(c, 0.9), shade(c, 0.5));
        g.setColor(alpha(Color.black, 0.35));
        g.fill(new Rectangle2D.Double(0, y, 1600, 4));
    }

    // ---- the rooms ----

    static void walnut(Graphics2D g) {
        panels(g, 56, 900, rgb(0x5a3a22), 1);
        glow(g, 1300, 220, 420, 300, rgb(0xffd890), 0.35);
        glow(g, 300, 700, 500, 260, rgb(0xffd890), 0.12);
    }

    static void nightWindow(Graphics2D g) {
        vfill(g, 0, 56, 1600, 844, rgb(0x1d2740), rgb(0x0b111f));
        // tall windows onto the city at night
        for (int i = 0; i < 4; i++) {
            double x = 140 + i * 360, y = 140, w = 240, h = 520;
            vfill(g, x, y, w, h * 0.7, rgb(0x0c1a3a), rgb(0x23335a));
            Random r = new Random(i + 5);
            for (int k = 0; k < 60; k++) { g.setColor(alpha(rgb(0xffe2a0), 0.3 + r.nextDouble() * 0.6)); g.fill(new Rectangle2D.Double(x + r.nextDouble() * w, y + h * 0.5 + r.nextDouble() * h * 0.2, 2, 2)); }
            g.setColor(rgb(0x0a1020));
            g.fill(new Rectangle2D.Double(x, y + h * 0.7, w, h * 0.3));
            g.setColor(rgb(0x3a4560));
            g.setStroke(new BasicStroke(6f));
            g.draw(new Rectangle2D.Double(x, y, w, h));
            g.setStroke(new BasicStroke(3f));
            g.draw(new Line2D.Double(x + w / 2, y, x + w / 2, y + h));
            for (int k = 1; k < 4; k++) g.draw(new Line2D.Double(x, y + h * k / 4, x + w, y + h * k / 4));
            g.setStroke(new BasicStroke(1f));
            cloth(g, x - 40, y - 30, 40, h + 60, rgb(0x3a2a4a), 13);
            cloth(g, x + w, y - 30, 40, h + 60, rgb(0x3a2a4a), 13);
        }
        floor(g, 760, rgb(0x2a1c14));
    }

    static final Color[] SPINES = {rgb(0x6b1e1e), rgb(0x1e3a6b), rgb(0x2e5a2e), rgb(0x6b5a1e), rgb(0x3a2a1a)};

    static void study(Graphics2D g) {
        vfill(g, 0, 56, 1600, 600, rgb(0x1f3a2c), rgb(0x10241a));
        stripes(g, 0, 56, 1600, 600, 30, alpha(Color.white, 0.03));
        panels(g, 600, 900, rgb(0x4a2c18), 2);
        // bookshelves along the left
        Random r = new Random(9);
        for (int s = 0; s < 6; s++) {
            double sy = 120 + s * 78;
            g.setColor(rgb(0x2a180c));
            g.fill(new Rectangle2D.Double(40, sy + 62, 420, 8));
            for (double bx = 48; bx < 450; ) {
                double bw = 8 + r.nextDouble() * 12, bh = 40 + r.nextDouble() * 20;
                g.setColor(SPINES[r.nextInt(SPINES.length)]);
                g.fill(new Rectangle2D.Double(bx, sy + 62 - bh, bw, bh));
                g.setColor(alpha(rgb(0xd4a945), 0.5));
                g.fill(new Rectangle2D.Double(bx + 2, sy + 62 - bh + 8, bw - 4, 2));
                bx += bw + 1;
            }
        }
        // a banker's lamp
        g.setColor(rgb(0x1f6a3a));
        Path2D lamp = new Path2D.Double();
        lamp.moveTo(1160, 520); lamp.lineTo(1300, 520); lamp.curveTo(1290, 490, 1170, 490, 1160, 520); lamp.closePath();
        g.fill(lamp);
        g.setColor(rgb(0xb08a3a));
        g.fill(new Rectangle2D.Double(1226, 520, 6, 60));
        glow(g, 1230, 560, 260, 120, rgb(0xfff0c4), 0.45);
    }

    static void yellowOval(Graphics2D g) {
        vfill(g, 0, 56, 1600, 700, rgb(0xd9b85a), rgb(0x9c7a2e));
        stripes(g, 0, 56, 1600, 700, 22, alpha(Color.white, 0.06));
        speckle(g, 0, 56, 1600, 700, 4, 2400, alpha(Color.white, 0.05), alpha(Color.black, 0.05));
        // a fireplace with a fire in it
        g.setColor(rgb(0xf0e6d2));
        g.fill(new Rectangle2D.Double(120, 500, 360, 260));
        g.setColor(rgb(0x1a0f08));
        g.fill(new RoundRectangle2D.Double(190, 570, 220, 190, 30, 30));
        glow(g, 300, 720, 140, 70, rgb(0xff8a2a), 0.9);
        glow(g, 300, 640, 420, 260, rgb(0xffb05a), 0.25);
        g.setColor(rgb(0xe6dcc6));
        g.fill(new Rectangle2D.Double(100, 488, 400, 18));
        frame(g, 1100, 200, 220, 280, rgb(0x2f3a2a));
        floor(g, 756, rgb(0x5a3a22));
    }

    static void corridor(Graphics2D g) {
        vfill(g, 0, 56, 1600, 760, rgb(0xe8dcc0), rgb(0xb9a67e));
        for (double x = 40; x < 1600; x += 220) {
            vfill(g, x, 90, 28, 640, rgb(0xf6efdc), rgb(0xcab58a));
            bevel(g, x, 90, 28, 640, 0.4, 0.2);
            glow(g, x + 110, 140, 70, 40, rgb(0xfff0c4), 0.4);
            frame(g, x + 70, 250, 80, 110, rgb(0x2f2a3a));
        }
        floor(g, 740, rgb(0x6e2a2a));
        stripes(g, 0, 744, 1600, 156, 40, alpha(rgb(0xd4a945), 0.06));
    }

    static void roosevelt(Graphics2D g) {
        vfill(g, 0, 56, 1600, 520, rgb(0x7a2e26), rgb(0x4e1a16));
        speckle(g, 0, 56, 1600, 520, 6, 2000, alpha(Color.white, 0.04), alpha(Color.black, 0.06));
        panels(g, 576, 900, rgb(0x6a4a30), 3);
        frame(g, 300, 160, 180, 230, rgb(0x2a2420));
        frame(g, 1120, 160, 180, 230, rgb(0x2a2420));
        // the fireplace and its mantel
        g.setColor(rgb(0x2a1a10));
        g.fill(new Rectangle2D.Double(680, 430, 240, 146));
        glow(g, 800, 560, 120, 50, rgb(0xff8a2a), 0.7);
        g.setColor(rgb(0xd8ccb4));
        g.fill(new Rectangle2D.Double(650, 412, 300, 20));
        glow(g, 800, 300, 700, 260, rgb(0xffd890), 0.15);
    }

    static void sitRoom(Graphics2D g) {
        panels(g, 56, 900, rgb(0x3a2818), 4);
        // the screens on the wall, glowing blue
        Random r = new Random(12);
        for (int i = 0; i < 5; i++) {
            double x = 90 + i * 300, y = 150, w = 260, h = 150;
            glow(g, x + w / 2, y + h / 2, w * 0.9, h * 0.9, rgb(0x3a7aff), 0.3);
            g.setColor(rgb(0x0a0f18));
            g.fill(new RoundRectangle2D.Double(x - 6, y - 6, w + 12, h + 12, 8, 8));
            vfill(g, x, y, w, h, rgb(0x0f2a5a), rgb(0x081530));
            g.setColor(alpha(rgb(0x7cc4ff), 0.4));
            for (int k = 0; k < 6; k++) g.fill(new Rectangle2D.Double(x + 14, y + 20 + k * 20, 40 + r.nextDouble() * (w - 70), 4));
            gloss(g, x, y, w, h, 0.06);
        }
        // the long table's edge
        g.setPaint(lin(0, 760, 0, 900, new float[]{0f, 1f}, new Color[]{rgb(0x2a1a10), rgb(0x0a0604)}));
        g.fill(new Rectangle2D.Double(0, 760, 1600, 140));
        gloss(g, 0, 760, 1600, 20, 0.08);
    }

    static void briefing(Graphics2D g) {
        cloth(g, 0, 56, 1600, 844, rgb(0x1b3a7a), 46);
        // the oval plaque above the lectern
        glow(g, 800, 300, 260, 160, rgb(0xbcd4ff), 0.25);
        g.setColor(rgb(0x0f2350));
        g.fill(new Ellipse2D.Double(640, 200, 320, 200));
        g.setColor(rgb(0xd4a945));
        g.setStroke(new BasicStroke(3f));
        g.draw(new Ellipse2D.Double(650, 210, 300, 180));
        g.setStroke(new BasicStroke(1f));
        Oval.seal(g, 800, 300, 70);
        // the lectern
        g.setPaint(lin(720, 0, 880, 0, new float[]{0f, 0.5f, 1f}, new Color[]{rgb(0x2a1a10), rgb(0x6a4026), rgb(0x2a1a10)}));
        g.fill(new Rectangle2D.Double(730, 560, 140, 340));
        g.setColor(rgb(0x0f2350));
        g.fill(new Ellipse2D.Double(770, 600, 60, 60));
    }

    static void cabinetRoom(Graphics2D g) {
        vfill(g, 0, 56, 1600, 700, rgb(0xefe4cc), rgb(0xc9b48a));
        for (int i = 0; i < 4; i++) {
            double x = 160 + i * 340, y = 120, w = 180, h = 480;
            vfill(g, x, y, w, h, rgb(0x9cc0e6), rgb(0xd9ebf8));
            g.setColor(rgb(0xfbf7ec));
            g.setStroke(new BasicStroke(5f));
            g.draw(new Rectangle2D.Double(x, y, w, h));
            g.setStroke(new BasicStroke(2f));
            for (int k = 1; k < 3; k++) g.draw(new Line2D.Double(x + w * k / 3, y, x + w * k / 3, y + h));
            for (int k = 1; k < 6; k++) g.draw(new Line2D.Double(x, y + h * k / 6, x + w, y + h * k / 6));
            g.setStroke(new BasicStroke(1f));
            cloth(g, x - 50, y - 40, 50, h + 80, rgb(0xc24a3a), 12);
            cloth(g, x + w, y - 40, 50, h + 80, rgb(0xc24a3a), 12);
        }
        // the table
        g.setPaint(lin(0, 700, 0, 900, new float[]{0f, 1f}, new Color[]{rgb(0x6a4026), rgb(0x2a1a10)}));
        g.fill(new Rectangle2D.Double(0, 700, 1600, 200));
        gloss(g, 0, 700, 1600, 30, 0.12);
    }
}
