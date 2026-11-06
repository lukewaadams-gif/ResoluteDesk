package resolutedesk.ui;

import static resolutedesk.ui.Theme.*;
import static resolutedesk.world.WorldData.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/** Simplified flags of the countries, as they looked in the year of the game. */
public final class Flag {
    static Color hex(String s) { return rgb(Integer.parseInt(s.trim(), 16)); }

    /** The calendar year, for the flags of the day (set with the rest of the era). */
    public static int year = 2025;
    /** Nationalist China still holds the mainland (until Mao's victory in October 1949). */
    public static boolean roc;

    static String spec(int c) {
        if (c == CC_RU && year < 1992) return "s:cc0000,ffd700";                    // the Soviet Union
        if (c == CC_CN && roc) return CT_FLAG[CC_TW];                       // the Republic of China
        if (c == CC_DE && year >= 1933 && year < 1946) return "h:000000,ffffff,dd0000"; // the old imperial colors
        if (c == CC_CA && year < 1965) return "h:cc0000,cc0000|k:012169";           // the Red Ensign, simplified
        if (c == CC_VN && year < 1976) return "h:ffcd00,ffcd00,ffcd00,da251d,ffcd00,da251d,ffcd00,da251d,ffcd00,ffcd00,ffcd00"; // South Vietnam
        return CT_FLAG[c];
    }

    static void draw(Graphics2D g, int c, double x, double y, double w, double h) {
        String[] parts = spec(c).split("\\|");
        Shape clip = g.getClip();
        g.clip(new RoundRectangle2D.Double(x, y, w, h, 3, 3));
        String base = parts[0];
        String kind = base.substring(0, 1);
        String[] cols = base.substring(2).split(",");
        if (kind.equals("h") || kind.equals("v")) {
            int n = cols.length;
            for (int i = 0; i < n; i++) {
                g.setColor(hex(cols[i]));
                if (kind.equals("h")) g.fill(new Rectangle2D.Double(x, y + h * i / n, w, h / n + 0.5));
                else g.fill(new Rectangle2D.Double(x + w * i / n, y, w / n + 0.5, h));
            }
        } else {
            g.setColor(hex(cols[0]));
            g.fill(new Rectangle2D.Double(x, y, w, h));
            g.setColor(hex(cols[1]));
            if (kind.equals("d")) { double r = h * 0.3; g.fill(new Ellipse2D.Double(x + w / 2 - r, y + h / 2 - r, r * 2, r * 2)); }
            else if (kind.equals("x")) { g.fill(new Rectangle2D.Double(x + w * 0.3, y, h * 0.2, h)); g.fill(new Rectangle2D.Double(x, y + h * 0.4, w, h * 0.2)); }
            else if (kind.equals("s")) star(g, x + w * 0.2, y + h * 0.3, h * 0.18);
            else if (kind.equals("c")) star(g, x + w / 2, y + h / 2, h * 0.28);
        }
        for (int i = 1; i < parts.length; i++) {
            String p = parts[i];
            Color col = hex(p.substring(2));
            g.setColor(col);
            if (p.startsWith("t:")) { Path2D.Double t = new Path2D.Double(); t.moveTo(x, y); t.lineTo(x + w * 0.38, y + h / 2); t.lineTo(x, y + h); t.closePath(); g.fill(t); }
            else if (p.startsWith("k:")) g.fill(new Rectangle2D.Double(x, y, w * 0.42, h * 0.5));
            else if (p.startsWith("o:")) { double r = h * 0.16; g.fill(new Ellipse2D.Double(x + w / 2 - r, y + h / 2 - r, r * 2, r * 2)); }
        }
        g.setClip(clip);
        g.setColor(new Color(255, 255, 255, 60));
        g.draw(new RoundRectangle2D.Double(x, y, w, h, 3, 3));
    }

    static void star(Graphics2D g, double cx, double cy, double r) {
        Path2D.Double p = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double a = -Math.PI / 2 + i * Math.PI / 5, rr = i % 2 == 0 ? r : r * 0.42;
            if (i == 0) p.moveTo(cx + Math.cos(a) * rr, cy + Math.sin(a) * rr); else p.lineTo(cx + Math.cos(a) * rr, cy + Math.sin(a) * rr);
        }
        p.closePath();
        g.fill(p);
    }
}
