package resolutedesk.ui;

import static resolutedesk.core.Num.*;
import static resolutedesk.ui.Fonts.*;
import static resolutedesk.ui.Theme.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

/** Drawing helpers for text, labels, chips, bars and charts. */
public final class Draw {
    private Draw() {
    }

    /** Letter-spaced small caps label. */
    public static void label(Graphics2D g, String s, double x, double y, Color c) {
        g.setFont(F_TB);
        g.setColor(c);
        FontMetrics fm = g.getFontMetrics();
        double cx = x;
        String u = s.toUpperCase();
        for (int i = 0; i < u.length(); i++) {
            String ch = u.substring(i, i + 1);
            g.drawString(ch, (float) cx, (float) y);
            cx += fm.stringWidth(ch) + 1.2;
        }
    }

    public static double labelWidth(Graphics2D g, String s) {
        FontMetrics fm = g.getFontMetrics(F_TB);
        double w = 0;
        for (char c : s.toUpperCase().toCharArray()) w += fm.charWidth(c) + 1.2;
        return w;
    }

    /** A rounded chip; returns its width. */
    public static double chip(Graphics2D g, String s, double x, double y, Color c, boolean filled) {
        g.setFont(F_TB);
        FontMetrics fm = g.getFontMetrics();
        double w = fm.stringWidth(s) + 12;
        RoundRectangle2D rr = new RoundRectangle2D.Double(x, y, w, 16, 16, 16);
        g.setColor(filled ? c : alpha(c, 0.16));
        g.fill(rr);
        g.setColor(alpha(c, filled ? 1 : 0.7));
        g.draw(rr);
        g.setColor(filled ? Color.white : shade(c, 1.35));
        g.drawString(s, (float) (x + 6), (float) (y + 11.5));
        return w;
    }

    public static void text(Graphics2D g, String s, double x, double y, Font f, Color c) {
        g.setFont(f);
        g.setColor(c);
        g.drawString(s, (float) x, (float) y);
    }

    public static void textR(Graphics2D g, String s, double x, double y, Font f, Color c) {
        g.setFont(f);
        g.setColor(c);
        g.drawString(s, (float) (x - g.getFontMetrics().stringWidth(s)), (float) y);
    }

    public static void textC(Graphics2D g, String s, double x, double y, Font f, Color c) {
        g.setFont(f);
        g.setColor(c);
        g.drawString(s, (float) (x - g.getFontMetrics().stringWidth(s) / 2.0), (float) y);
    }

    /** Text that is shortened with an ellipsis to fit. */
    public static void textFit(Graphics2D g, String s, double x, double y, double w, Font f, Color c) {
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        String t = s;
        if (fm.stringWidth(t) > w) {
            while (t.length() > 1 && fm.stringWidth(t + "...") > w) t = t.substring(0, t.length() - 1);
            t = t + "...";
        }
        g.setColor(c);
        g.drawString(t, (float) x, (float) y);
    }

    public static double strW(Graphics2D g, String s, Font f) { return g.getFontMetrics(f).stringWidth(s); }

    /** Draws word-wrapped text; returns the height used. */
    public static double wrap(Graphics2D g, String s, double x, double y, double w, Font f, Color c) {
        g.setFont(f);
        g.setColor(c);
        FontMetrics fm = g.getFontMetrics();
        double lh = fm.getHeight();
        double cy = y;
        for (String para : s.split("\n")) {
            String[] words = para.split(" ");
            StringBuilder line = new StringBuilder();
            for (String wd : words) {
                String t = line.length() == 0 ? wd : line + " " + wd;
                if (fm.stringWidth(t) > w && line.length() > 0) {
                    g.drawString(line.toString(), (float) x, (float) cy);
                    cy += lh;
                    line = new StringBuilder(wd);
                } else line = new StringBuilder(t);
            }
            g.drawString(line.toString(), (float) x, (float) cy);
            cy += lh;
        }
        return cy - y;
    }

    /** Word-wrapped text cut off after a number of lines, the last ending in an ellipsis. */
    public static void wrapMax(Graphics2D g, String s, double x, double y, double w, Font f, Color c, int max, double lh) {
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        String[] words = s.split(" ");
        int i = 0;
        for (int ln = 0; ln < max && i < words.length; ln++) {
            StringBuilder line = new StringBuilder(words[i++]);
            while (i < words.length && fm.stringWidth(line + " " + words[i]) <= w) line.append(" ").append(words[i++]);
            if (ln == max - 1 && i < words.length) {
                String t = line.toString();
                while (t.length() > 1 && fm.stringWidth(t + "...") > w) t = t.substring(0, t.length() - 1);
                int sp = t.lastIndexOf(' ');
                if (sp > t.length() - 12 && sp > 0) t = t.substring(0, sp);
                line = new StringBuilder(t.replaceAll("[,;:.]+$", "") + "...");
            }
            g.setColor(c);
            g.drawString(line.toString(), (float) x, (float) (y + ln * lh));
        }
    }

    public static double wrapHeight(Graphics2D g, String s, double w, Font f) {
        FontMetrics fm = g.getFontMetrics(f);
        double lh = fm.getHeight();
        int lines = 0;
        for (String para : s.split("\n")) {
            String[] words = para.split(" ");
            StringBuilder line = new StringBuilder();
            lines++;
            for (String wd : words) {
                String t = line.length() == 0 ? wd : line + " " + wd;
                if (fm.stringWidth(t) > w && line.length() > 0) { lines++; line = new StringBuilder(wd); } else line = new StringBuilder(t);
            }
        }
        return lines * lh;
    }

    public static void bar(Graphics2D g, double x, double y, double w, double h, double frac, Color c) {
        RoundRectangle2D bg = new RoundRectangle2D.Double(x, y, w, h, h, h);
        g.setColor(new Color(255, 255, 255, 16));
        g.fill(bg);
        double f = clamp(frac, 0, 1);
        if (f > 0) {
            g.setPaint(new GradientPaint((float) x, (float) y, shade(c, 1.15), (float) (x + w), (float) y, c));
            g.fill(new RoundRectangle2D.Double(x, y, Math.max(h, w * f), h, h, h));
        }
    }

    /** Signed bar around a centre line. */
    public static void sbar(Graphics2D g, double x, double y, double w, double h, double v, Color neg, Color pos) {
        g.setColor(new Color(255, 255, 255, 14));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, h, h));
        double cx = x + w / 2;
        double len = w / 2 * clamp(Math.abs(v), 0, 1);
        g.setColor(v < 0 ? neg : pos);
        g.fill(new Rectangle2D.Double(v < 0 ? cx - len : cx, y, len, h));
        g.setColor(new Color(255, 255, 255, 90));
        g.draw(new Line2D.Double(cx, y - 2, cx, y + h + 2));
    }

    /** Simple multi-series line chart. */
    public static void chart(Graphics2D g, double x, double y, double w, double h, List<double[]> series, Color[] cols, String[] names, double lo, double hi, String fmtY, int n) {
        g.setColor(new Color(0, 0, 0, 70));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 8, 8));
        g.setStroke(new BasicStroke(1f));
        for (int i = 0; i <= 4; i++) {
            double yy = y + 6 + (h - 12) * i / 4.0;
            g.setColor(new Color(255, 255, 255, i == 2 ? 26 : 12));
            g.draw(new Line2D.Double(x + 4, yy, x + w - 4, yy));
            double v = hi - (hi - lo) * i / 4.0;
            if (fmtY != null) textR(g, String.format(fmtY, v), x - 4, yy + 4, F_MS, C_FAINT);
        }
        for (int s = 0; s < series.size(); s++) {
            double[] d = series.get(s);
            int len = Math.min(d.length, n);
            if (len < 2) continue;
            Path2D.Double p = new Path2D.Double();
            for (int i = 0; i < len; i++) {
                double xx = x + 4 + (w - 8) * i / (double) (Math.max(2, n) - 1);
                double yy = y + 6 + (h - 12) * (1 - (d[i] - lo) / (hi - lo == 0 ? 1 : hi - lo));
                yy = clamp(yy, y + 2, y + h - 2);
                if (i == 0) p.moveTo(xx, yy); else p.lineTo(xx, yy);
            }
            g.setColor(cols[s % cols.length]);
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(p);
        }
        g.setStroke(new BasicStroke(1f));
        if (names != null) {
            double lx = x + 8;
            for (int s = 0; s < names.length; s++) {
                g.setColor(cols[s % cols.length]);
                g.fill(new RoundRectangle2D.Double(lx, y + 7, 10, 4, 4, 4));
                text(g, names[s], lx + 14, y + 13, F_T, C_DIM);
                lx += 22 + g.getFontMetrics(F_T).stringWidth(names[s]);
            }
        }
    }

    // ---- backgrounds ------------------------------------------------------------------------------------------------
    public static BufferedImage bgCache;
}
