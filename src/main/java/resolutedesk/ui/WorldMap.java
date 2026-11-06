package resolutedesk.ui;

import static resolutedesk.ui.Art.*;
import static resolutedesk.ui.Draw.*;
import static resolutedesk.ui.Fonts.*;
import static resolutedesk.ui.Theme.*;
import static resolutedesk.world.Countries.*;
import static resolutedesk.world.WorldData.*;
import static resolutedesk.world.WorldShapes.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import resolutedesk.ResoluteDesk;
import resolutedesk.history.History;
import resolutedesk.model.Game;
import resolutedesk.world.War;
import resolutedesk.world.World;

/** The world map of the Situation Room. */
public final class WorldMap {
    static Path2D[] shape;
    static Rectangle2D[] bounds;
    static String[] code;
    static int us = -1;
    static final double AW = 1000, AH = 447;

    static synchronized void load() {
        if (shape != null) return;
        int n = WORLD.length;
        Path2D[] sh = new Path2D[n];
        Rectangle2D[] bd = new Rectangle2D[n];
        String[] cd = new String[n];
        for (int k = 0; k < n; k++) {
            String[] row = WORLD[k];
            cd[k] = row[0];
            Path2D.Double p = new Path2D.Double(Path2D.WIND_EVEN_ODD);
            for (String ring : row[1].split("~")) {
                if (ring.length() < 6) continue;
                int x = USMap.num(ring, 0, 3), y = USMap.num(ring, 3, 3);
                p.moveTo(x / 2.0, y / 2.0);
                int i = 6;
                while (i < ring.length()) {
                    if (ring.charAt(i) == '!') { x = USMap.num(ring, i + 1, 3); y = USMap.num(ring, i + 4, 3); i += 7; }
                    else { x += USMap.dec(ring.charAt(i)) - 44; y += USMap.dec(ring.charAt(i + 1)) - 44; i += 2; }
                    p.lineTo(x / 2.0, y / 2.0);
                }
                p.closePath();
            }
            sh[k] = p; bd[k] = p.getBounds2D();
            if (row[0].equals("US")) us = k;
        }
        bounds = bd; code = cd; shape = sh;
    }

    static double[] last;

    static double[] fit(double x, double y, double w, double h) {
        double s = Math.min(w / AW, h / AH);
        return new double[]{s, x + (w - AW * s) / 2, y + (h - AH * s) / 2};
    }

    /** The country (index into CT_ID) at a screen point on the last map drawn, or -1. */
    static int at(double px, double py) {
        if (last == null || shape == null) return -1;
        double ax = (px - last[1]) / last[0], ay = (py - last[2]) / last[0];
        for (int k = 0; k < shape.length; k++) { int c = ctIdx(code[k]); if (c >= 0 && bounds[k].contains(ax, ay) && shape[k].contains(ax, ay)) return c; }
        return -1;
    }

    static Color relColor(double r) {
        return r >= 0 ? mix(rgb(0x2b3a55), rgb(0x2f9e6a), Math.min(1, r / 80)) : mix(rgb(0x2b3a55), rgb(0xc0392b), Math.min(1, -r / 80));
    }

    /** Draws the map; returns the index into CT_ID of the country under the mouse, or -1. */
    static int draw(ResoluteDesk u, Graphics2D g, Game game, double x, double y, double w, double h, int sel) {
        load();
        double[] f = fit(x, y, w, h);
        last = f;
        int hover = u.modal == null && u.mx >= x && u.mx < x + w && u.my >= y && u.my < y + h ? at(u.mx, u.my) : -1;
        AffineTransform at = g.getTransform();
        int bx0 = (int) Math.floor(f[1]), by0 = (int) Math.floor(f[2]), bw0 = (int) Math.ceil(AW * f[0]) + 1, bh0 = (int) Math.ceil(AH * f[0]) + 1;
        String box = bx0 + "," + by0 + "," + bw0 + "," + bh0;
        layer(g, "world-sea|" + box, bx0, by0, bw0, bh0, (b, lx, ly, lw, lh) -> sea(b, f, lx, ly, lw, lh));
        g.translate(f[1], f[2]);
        g.scale(f[0], f[0]);
        g.setStroke(new BasicStroke((float) (0.7 / f[0])));
        for (int k = 0; k < shape.length; k++) {
            int c = ctIdx(code[k]);
            if (c >= 0 && gone(game, c)) c = -1;
            Color col = k == us ? rgb(0x1f4fa8) : c >= 0 ? relColor(game.world.rel[c]) : rgb(0x253044);
            if (c >= 0 && (c == sel || c == hover)) col = shade(col, 1.35);
            g.setColor(col);
            g.fill(shape[k]);
            g.setColor(new Color(10, 16, 30, 200));
            g.draw(shape[k]);
        }
        g.setTransform(at);
        g.setStroke(new BasicStroke(1f));
        layer(g, "world-light|" + box, bx0, by0, bw0, bh0, (b, lx, ly, lw, lh) -> light(b, f));
        // labels for the bigger players, and the selected one
        for (int c = 0; c < NC; c++) {
            boolean big = c == CC_CN || c == CC_RU || c == CC_CA || c == CC_BR || c == CC_AU || c == CC_IN;
            if (!big && c != sel && c != hover || gone(game, c)) continue;
            double lx = f[1] + CT_LX[c] * f[0], ly = f[2] + CT_LY[c] * f[0];
            textC(g, cname(game, c), lx, ly + 4, F_T, c == sel ? C_GOLD : new Color(235, 240, 250, 210));
        }
        // flashpoints
        World wd = game.world;
        double[][] hot = {{828, 172, wd.modern && wd.twStage >= 2 ? 1 : 0}, {585, 96, wd.modern && wd.uaStage == 2 ? 1 : 0}, {590, 150, wd.modern && wd.gaza > 60 ? 1 : 0}, {640, 140, wd.modern && wd.iran > 80 ? 1 : 0}, {612, 192, wd.modern && wd.redsea > 60 ? 1 : 0}, {826, 117, wd.modern && wd.nk > 75 ? 1 : 0},
                {CT_LX[CC_DE], CT_LY[CC_DE], warOn(wd, "ww2e")}, {CT_LX[CC_JP] + 10, CT_LY[CC_JP] + 20, warOn(wd, "ww2p")}, {CT_LX[CC_KR], CT_LY[CC_KR], warOn(wd, "korea")}, {CT_LX[CC_VN], CT_LY[CC_VN], warOn(wd, "vietnam")},
                {618, 160, warOn(wd, "gulf")}, {668, 138, warOn(wd, "afghan")}, {CT_LX[CC_IQ], CT_LY[CC_IQ], warOn(wd, "iraq")}, {CT_LX[CC_SY], CT_LY[CC_SY], warOn(wd, "isis")}};
        for (double[] hp : hot) {
            if (hp[2] < 1) continue;
            double px = f[1] + hp[0] * f[0], py = f[2] + hp[1] * f[0];
            double pulse = 6 + 3 * Math.sin(u.tsec * 4);
            g.setColor(new Color(255, 80, 60, 90));
            g.fill(new Ellipse2D.Double(px - pulse, py - pulse, pulse * 2, pulse * 2));
            g.setColor(rgb(0xff5040));
            g.fill(new Ellipse2D.Double(px - 3, py - 3, 6, 6));
        }
        return hover;
    }

    /** The ocean: deep water with a faint graticule, the shallows along every coast, and the land's shadow. */
    static void sea(Graphics2D b, double[] f, double x, double y, double w, double h) {
        b.setPaint(lin(x, y, x, y + h, new float[]{0f, 1f}, new Color[]{rgb(0x0d2140), rgb(0x07122a)}));
        b.fill(new RoundRectangle2D.Double(x, y, w, h, 14, 14));
        glow(b, x + w * 0.45, y + h * 0.38, w * 0.6, h * 0.55, rgb(0x2a5a9a), 0.22);
        b.setColor(new Color(120, 170, 240, 22));
        b.setStroke(new BasicStroke(1f));
        for (int i = 1; i < 12; i++) b.draw(new Line2D.Double(x + w * i / 12, y + 4, x + w * i / 12, y + h - 4));
        for (int i = 1; i < 6; i++) b.draw(new Line2D.Double(x + 4, y + h * i / 6, x + w - 4, y + h * i / 6));
        AffineTransform at = b.getTransform();
        b.translate(f[1], f[2]);
        b.scale(f[0], f[0]);
        b.setStroke(new BasicStroke((float) (5 / f[0]), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        b.setColor(new Color(80, 150, 230, 34));
        for (Path2D s : shape) b.draw(s);
        b.translate(1.5 / f[0], 2.5 / f[0]);
        b.setColor(new Color(0, 0, 0, 110));
        for (Path2D s : shape) b.fill(s);
        b.setTransform(at);
    }

    /** Light from the upper left over the land, whatever color each country is. */
    static void light(Graphics2D b, double[] f) {
        b.translate(f[1], f[2]);
        b.scale(f[0], f[0]);
        b.setPaint(lin(0, 0, AW, AH, new float[]{0f, 0.5f, 1f}, new Color[]{new Color(255, 255, 255, 30), new Color(255, 255, 255, 0), new Color(0, 0, 20, 56)}));
        for (Path2D s : shape) b.fill(s);
    }

    static double warOn(World w, String id) { War x = History.war(w, id); return x != null && x.stage == 2 ? 1 : 0; }
}
