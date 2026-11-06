package resolutedesk.ui;

import static resolutedesk.ui.Art.*;
import static resolutedesk.ui.Theme.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

/**
 * The title screen's view: the North Portico of the White House at dusk, floodlit, with lit windows, the fountain,
 * lamps along the drive and the fence in the foreground. The building is painted once; the stars, the flag, the
 * windows' flicker and the fountain move.
 */
public final class HomeArt {
    private HomeArt() {
    }

    static final Color STONE = rgb(0xf3ecdc), STONE_LO = rgb(0xb7ad98), WARM = rgb(0xffd98a);

    public static void paint(Graphics2D g, double t) {
        layer(g, "home-scene", 0, 0, 1600, 900, (b, x, y, w, h) -> scene(b));
        stars(g, t);
        windowsFlicker(g, t);
        flag(g, 1120, 380, t);
        fountain(g, t);
    }

    static void scene(Graphics2D g) {
        // dusk: deep blue above, a last warm band at the horizon
        g.setPaint(lin(0, 0, 0, 700, new float[]{0f, 0.55f, 0.85f, 1f}, new Color[]{rgb(0x040817), rgb(0x0f1d42), rgb(0x2b3567), rgb(0x5a4a6a)}));
        g.fill(new Rectangle2D.Double(0, 0, 1600, 700));
        glow(g, 1120, 700, 900, 160, rgb(0xff9f6a), 0.18);
        // the moon
        glow(g, 1430, 140, 90, 90, rgb(0xdfe8ff), 0.25);
        g.setColor(rgb(0xf4f1e6));
        g.fill(new Ellipse2D.Double(1414, 124, 32, 32));
        g.setColor(alpha(rgb(0xc9c3b0), 0.5));
        g.fill(new Ellipse2D.Double(1422, 132, 7, 6));
        g.fill(new Ellipse2D.Double(1432, 142, 5, 5));
        // trees on the far side of the lawn
        Random r = new Random(3);
        g.setColor(rgb(0x0b1a26));
        for (int i = 0; i < 40; i++) { double tx = 560 + i * 26 + r.nextDouble() * 10, ts = 30 + r.nextDouble() * 34; g.fill(new Ellipse2D.Double(tx - ts / 2, 640 - ts * 0.6 - r.nextDouble() * 20, ts, ts)); }
        building(g);
        grounds(g);
    }

    static void building(Graphics2D g) {
        double x = 700, w = 840, top = 506, base = 700;
        // the floodlights wash the facade from below
        glow(g, 1120, 690, 520, 220, rgb(0xfff2cf), 0.28);
        g.setPaint(lin(0, top, 0, base, new float[]{0f, 1f}, new Color[]{rgb(0xd9d1bf), STONE}));
        g.fill(new Rectangle2D.Double(x, top, w, base - top));
        g.setPaint(lin(x, 0, x + w, 0, new float[]{0f, 0.5f, 1f}, new Color[]{alpha(Color.black, 0.28), alpha(Color.black, 0), alpha(Color.black, 0.28)}));
        g.fill(new Rectangle2D.Double(x, top, w, base - top));
        speckle(g, x, top, w, base - top, 9, 1800, alpha(Color.white, 0.06), alpha(Color.black, 0.04));
        // the roof balustrade and cornice
        g.setPaint(lin(0, top - 22, 0, top, new float[]{0f, 1f}, new Color[]{rgb(0xcfc6b2), rgb(0xa79e8a)}));
        g.fill(new Rectangle2D.Double(x - 8, top - 10, w + 16, 12));
        g.setColor(rgb(0xc8bfaa));
        g.fill(new Rectangle2D.Double(x - 4, top - 30, w + 8, 6));
        for (double bx = x; bx < x + w; bx += 9) g.fill(new Rectangle2D.Double(bx, top - 24, 4, 14));
        g.setColor(alpha(Color.black, 0.25));
        g.fill(new Rectangle2D.Double(x - 8, top + 2, w + 16, 3));
        // a string course between the floors
        g.setColor(rgb(0xe8e0cc));
        g.fill(new Rectangle2D.Double(x, 598, w, 5));
        g.setColor(alpha(Color.black, 0.15));
        g.fill(new Rectangle2D.Double(x, 603, w, 2));
        // windows: lit from inside, each with its pediment
        for (int row = 0; row < 2; row++) for (int i = 0; i < 11; i++) {
            double wx = x + 30 + i * 74, wy = row == 0 ? 528 : 618;
            if (wx + 34 > 960 && wx < 1280) continue;
            window(g, wx, wy, 34, 54, row == 0 ? (i % 2 == 0 ? 0 : 1) : 2);
        }
        portico(g);
        // the ground the building stands on
        g.setColor(rgb(0x1a2230));
        g.fill(new Rectangle2D.Double(x - 30, base, w + 60, 8));
    }

    static void window(Graphics2D g, double x, double y, double w, double h, int pediment) {
        glow(g, x + w / 2, y + h / 2, w * 1.4, h, WARM, 0.25);
        g.setColor(rgb(0x8f8574));
        g.fill(new Rectangle2D.Double(x - 4, y - 4, w + 8, h + 8));
        g.setPaint(lin(0, y, 0, y + h, new float[]{0f, 1f}, new Color[]{rgb(0xffe6a8), rgb(0xf0b35c)}));
        g.fill(new Rectangle2D.Double(x, y, w, h));
        g.setColor(alpha(rgb(0x6b4a2a), 0.7));
        g.setStroke(new BasicStroke(1.6f));
        g.draw(new Line2D.Double(x + w / 2, y, x + w / 2, y + h));
        for (int k = 1; k < 3; k++) g.draw(new Line2D.Double(x, y + h * k / 3, x + w, y + h * k / 3));
        g.setStroke(new BasicStroke(1f));
        // curtains drawn back inside
        g.setColor(alpha(rgb(0xb5482c), 0.45));
        g.fill(new Rectangle2D.Double(x, y, 6, h));
        g.fill(new Rectangle2D.Double(x + w - 6, y, 6, h));
        // pediment: triangle, curve, or a flat cap on the lower floor
        g.setColor(rgb(0xe9e1cc));
        if (pediment == 0) { Path2D p = new Path2D.Double(); p.moveTo(x - 7, y - 6); p.lineTo(x + w / 2, y - 20); p.lineTo(x + w + 7, y - 6); p.closePath(); g.fill(p); }
        else if (pediment == 1) { Path2D p = new Path2D.Double(); p.moveTo(x - 7, y - 6); p.curveTo(x, y - 22, x + w, y - 22, x + w + 7, y - 6); p.closePath(); g.fill(p); }
        else g.fill(new Rectangle2D.Double(x - 6, y - 9, w + 12, 5));
        g.setColor(rgb(0xd5ccb6));
        g.fill(new Rectangle2D.Double(x - 6, y + h + 3, w + 12, 4));
    }

    /** The North Portico: four columns across the front, a pediment, the lantern and the door. */
    static void portico(Graphics2D g) {
        double cx = 1120, l = 970, r = 1270, base = 700, cap = 528;
        // the wall behind the columns, in shadow, with the door and lantern
        g.setPaint(lin(0, cap, 0, base, new float[]{0f, 1f}, new Color[]{rgb(0xbdb39e), rgb(0xe3dac6)}));
        g.fill(new Rectangle2D.Double(l + 10, cap, r - l - 20, base - cap));
        glow(g, cx, 648, 70, 60, WARM, 0.55);
        g.setPaint(lin(0, 610, 0, 700, new float[]{0f, 1f}, new Color[]{rgb(0xffe3a0), rgb(0xd99a4a)}));
        g.fill(new Rectangle2D.Double(cx - 20, 616, 40, 84));
        Path2D fan = new Path2D.Double();
        fan.moveTo(cx - 24, 616); fan.curveTo(cx - 24, 594, cx + 24, 594, cx + 24, 616); fan.closePath();
        g.fill(fan);
        g.setColor(rgb(0x5a4026));
        g.fill(new Rectangle2D.Double(cx - 1, 616, 2, 84));
        // the lantern hanging in the portico
        g.setColor(rgb(0x2a2a2a));
        g.fill(new Rectangle2D.Double(cx - 1, 540, 2, 30));
        g.setColor(WARM);
        g.fill(new Rectangle2D.Double(cx - 7, 568, 14, 18));
        glow(g, cx, 578, 40, 34, WARM, 0.7);
        // the steps
        for (int k = 0; k < 4; k++) {
            g.setColor(shade(STONE, 0.9 - k * 0.04));
            g.fill(new Rectangle2D.Double(l - 6 - k * 10, base - 4 + k * 3, r - l + 12 + k * 20, 4));
        }
        // the columns: round, so light falls across them
        for (int i = 0; i < 4; i++) {
            double colx = l + 16 + i * (r - l - 56) / 3;
            g.setPaint(lin(colx, 0, colx + 24, 0, new float[]{0f, 0.35f, 0.7f, 1f}, new Color[]{rgb(0xb0a690), rgb(0xffffff), rgb(0xe8e0cc), rgb(0xa59b86)}));
            g.fill(new Rectangle2D.Double(colx, cap + 14, 24, base - cap - 18));
            g.setColor(alpha(Color.black, 0.06));
            for (int k = 1; k < 5; k++) g.fill(new Rectangle2D.Double(colx + k * 5, cap + 18, 1, base - cap - 26));
            g.setColor(rgb(0xece4d0));
            g.fill(new Rectangle2D.Double(colx - 4, cap + 6, 32, 9));
            g.fill(new Rectangle2D.Double(colx - 4, base - 8, 32, 6));
            g.setColor(rgb(0xcfc5ae));
            g.fill(new Ellipse2D.Double(colx - 6, cap + 4, 9, 9));
            g.fill(new Ellipse2D.Double(colx + 21, cap + 4, 9, 9));
        }
        // the entablature and the pediment
        g.setPaint(lin(0, cap - 26, 0, cap + 6, new float[]{0f, 1f}, new Color[]{rgb(0xf6f0e2), rgb(0xcdc3ad)}));
        g.fill(new Rectangle2D.Double(l - 14, cap - 22, r - l + 28, 28));
        g.setColor(alpha(Color.black, 0.2));
        g.fill(new Rectangle2D.Double(l - 14, cap + 4, r - l + 28, 3));
        Path2D ped = new Path2D.Double();
        ped.moveTo(l - 22, cap - 22); ped.lineTo(cx, cap - 92); ped.lineTo(r + 22, cap - 22); ped.closePath();
        g.setPaint(lin(0, cap - 92, 0, cap - 22, new float[]{0f, 1f}, new Color[]{rgb(0xfaf5ea), rgb(0xd8cfba)}));
        g.fill(ped);
        Path2D tymp = new Path2D.Double();
        tymp.moveTo(l - 2, cap - 28); tymp.lineTo(cx, cap - 80); tymp.lineTo(r + 2, cap - 28); tymp.closePath();
        g.setColor(rgb(0xe4dbc6));
        g.fill(tymp);
        g.setColor(alpha(Color.black, 0.18));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(tymp);
        g.setStroke(new BasicStroke(1f));
        // the oval window in the pediment
        g.setColor(rgb(0xbfb49c));
        g.fill(new Ellipse2D.Double(cx - 16, cap - 62, 32, 20));
        g.setColor(rgb(0x3b4458));
        g.fill(new Ellipse2D.Double(cx - 12, cap - 59, 24, 14));
    }

    static void grounds(Graphics2D g) {
        // the north lawn, falling away into the dark
        g.setPaint(lin(0, 700, 0, 900, new float[]{0f, 1f}, new Color[]{rgb(0x1f3326), rgb(0x070c12)}));
        g.fill(new Rectangle2D.Double(0, 700, 1600, 200));
        glow(g, 1120, 720, 560, 70, rgb(0xfff2cf), 0.12);
        // the drive: two curves toward the portico
        g.setColor(rgb(0x3a3b3f));
        Path2D drive = new Path2D.Double();
        drive.moveTo(700, 900); drive.curveTo(820, 790, 980, 730, 1060, 708); drive.lineTo(1180, 708); drive.curveTo(1260, 730, 1420, 790, 1540, 900); drive.lineTo(1480, 900);
        drive.curveTo(1380, 800, 1250, 742, 1170, 722); drive.lineTo(1070, 722); drive.curveTo(990, 742, 860, 800, 760, 900); drive.closePath();
        g.fill(drive);
        g.setColor(alpha(rgb(0xfff2cf), 0.08));
        g.fill(drive);
        // lamps along the drive
        for (double[] lp : new double[][]{{860, 790, 1.0}, {1380, 790, 1.0}, {960, 744, 0.75}, {1280, 744, 0.75}}) lamp(g, lp[0], lp[1], lp[2]);
        // the fountain's basin
        g.setColor(rgb(0x2a3a3a));
        g.fill(new Ellipse2D.Double(1050, 770, 140, 30));
        g.setColor(rgb(0x4a6a78));
        g.fill(new Ellipse2D.Double(1058, 773, 124, 22));
        glow(g, 1120, 784, 70, 16, rgb(0xbfe3ff), 0.3);
        // the fence in front: iron bars with spear tips
        g.setColor(rgb(0x05080c));
        g.fill(new Rectangle2D.Double(700, 852, 900, 5));
        g.fill(new Rectangle2D.Double(700, 886, 900, 4));
        g.setPaint(lin(0, 0, 760, 0, new float[]{0f, 1f}, new Color[]{rgb(0x05080c), alpha(rgb(0x05080c), 0)}));
        g.fill(new Rectangle2D.Double(0, 700, 760, 200));
        g.setColor(rgb(0x05080c));
        for (double fx = 704; fx < 1600; fx += 14) {
            g.fill(new Rectangle2D.Double(fx, 838, 3, 62));
            Path2D tip = new Path2D.Double();
            tip.moveTo(fx - 2, 840); tip.lineTo(fx + 1.5, 830); tip.lineTo(fx + 5, 840); tip.closePath();
            g.fill(tip);
        }
        for (double fx = 820; fx < 1600; fx += 280) { g.fill(new Rectangle2D.Double(fx - 6, 822, 15, 78)); g.fill(new Ellipse2D.Double(fx - 8, 812, 19, 14)); }
        // great old trees at either end of the lawn
        tree(g, 1590, 700, 1.1);
        tree(g, 668, 704, 0.72);
        vignette(g, 0, 0, 1600, 900, 0.45);
    }

    static void lamp(Graphics2D g, double x, double y, double sc) {
        glow(g, x, y + 40 * sc, 60 * sc, 18 * sc, WARM, 0.25);
        g.setColor(rgb(0x0d0f12));
        g.fill(new Rectangle2D.Double(x - 1.5 * sc, y - 40 * sc, 3 * sc, 80 * sc));
        g.setColor(WARM);
        g.fill(new Rectangle2D.Double(x - 5 * sc, y - 52 * sc, 10 * sc, 13 * sc));
        glow(g, x, y - 46 * sc, 28 * sc, 28 * sc, WARM, 0.6);
    }

    static void tree(Graphics2D g, double x, double base, double sc) {
        g.setColor(rgb(0x0a120e));
        g.fill(new Rectangle2D.Double(x - 7 * sc, base - 120 * sc, 14 * sc, 120 * sc));
        Random r = new Random((long) (x * 7));
        for (int i = 0; i < 16; i++) {
            double cx = x + (r.nextDouble() - 0.5) * 170 * sc, cy = base - 150 * sc - r.nextDouble() * 120 * sc, s = (50 + r.nextDouble() * 60) * sc;
            g.setColor(i % 3 == 0 ? rgb(0x13241c) : rgb(0x0c1712));
            g.fill(new Ellipse2D.Double(cx - s / 2, cy - s / 2, s, s));
        }
        glow(g, x + 50 * sc, base - 160 * sc, 70 * sc, 60 * sc, rgb(0xfff2cf), 0.05);
    }

    // ---- what moves ----

    static void stars(Graphics2D g, double t) {
        Random st = new Random(7);
        for (int i = 0; i < 160; i++) {
            double sx = st.nextDouble() * 1600, sy = st.nextDouble() * 500, tw = 0.5 + 0.5 * Math.sin(t * 1.5 + i);
            g.setColor(new Color(255, 255, 255, (int) (40 + 90 * tw * st.nextDouble())));
            g.fill(new Ellipse2D.Double(sx, sy, 1.6, 1.6));
        }
    }

    static void windowsFlicker(Graphics2D g, double t) {
        for (int i = 0; i < 4; i++) {
            double fl = 0.5 + 0.5 * Math.sin(t * 0.7 + i * 1.9);
            double wx = 730 + i * 222, wy = i % 2 == 0 ? 528 : 618;
            g.setColor(alpha(rgb(0xfff2c8), 0.08 * fl));
            g.fill(new Rectangle2D.Double(wx, wy, 34, 54));
        }
    }

    static void flag(Graphics2D g, double x, double y, double t) {
        g.setColor(rgb(0xd8d8d8));
        g.fill(new Rectangle2D.Double(x - 1.5, y - 64, 3, 72));
        for (int k = 0; k < 7; k++) {
            Path2D st = new Path2D.Double();
            double w0 = Math.sin(t * 2 + k * 0.3) * 2.5, w1 = Math.sin(t * 2 + k * 0.3 + 1.2) * 2.5;
            st.moveTo(x, y - 64 + k * 5);
            st.curveTo(x + 20, y - 64 + k * 5 + w0, x + 40, y - 64 + k * 5 + w1, x + 60, y - 64 + k * 5 + w0);
            st.lineTo(x + 60, y - 59 + k * 5 + w0);
            st.curveTo(x + 40, y - 59 + k * 5 + w1, x + 20, y - 59 + k * 5 + w0, x, y - 59 + k * 5);
            st.closePath();
            g.setColor(k % 2 == 0 ? rgb(0xb22234) : rgb(0xf5f3ee));
            g.fill(st);
        }
        g.setColor(rgb(0x3c3b6e));
        g.fill(new Rectangle2D.Double(x, y - 64, 24, 20));
        glow(g, x + 30, y - 46, 50, 30, rgb(0xfff2cf), 0.12);
    }

    static void fountain(Graphics2D g, double t) {
        Random r = new Random(11);
        for (int i = 0; i < 40; i++) {
            double ph = r.nextDouble(), sp = 0.6 + r.nextDouble() * 0.6, a = (t * sp + ph) % 1.0;
            double ang = (r.nextDouble() - 0.5) * 1.2, px = 1120 + Math.sin(ang) * a * 46, py = 782 - Math.sin(a * Math.PI) * 44 * (1 - Math.abs(ang) * 0.4);
            g.setColor(alpha(rgb(0xdff2ff), 0.5 * (1 - a)));
            g.fill(new Ellipse2D.Double(px - 1.2, py - 1.2, 2.4, 2.4));
        }
    }
}
