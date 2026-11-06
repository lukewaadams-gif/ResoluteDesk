package resolutedesk;

import static resolutedesk.core.Num.*;
import static resolutedesk.ui.Draw.*;
import static resolutedesk.ui.Fonts.*;
import static resolutedesk.ui.ScreenIds.*;
import static resolutedesk.ui.Sound.*;
import static resolutedesk.ui.Theme.*;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import resolutedesk.diagnostics.Diagnostics;
import resolutedesk.io.BgImport;
import resolutedesk.legacy.Legacy;
import resolutedesk.model.Game;
import resolutedesk.model.Settings;
import resolutedesk.opinion.Elect;
import resolutedesk.opinion.Groups;
import resolutedesk.ui.Creator;
import resolutedesk.ui.DoubleAction;
import resolutedesk.ui.Hit;
import resolutedesk.ui.IntAction;
import resolutedesk.ui.Modal;
import resolutedesk.ui.Night;
import resolutedesk.ui.Oval;
import resolutedesk.ui.Paper;
import resolutedesk.ui.Popup;
import resolutedesk.ui.Portrait;
import resolutedesk.ui.Rooms;
import resolutedesk.ui.Screens;
import resolutedesk.ui.Sfx;
import resolutedesk.ui.TextAction;
import resolutedesk.ui.Toast;
import resolutedesk.ui.TransitionUI;
import resolutedesk.ui.USMap;
import resolutedesk.ui.Watch;

/** The game window: input, screens, widgets and the main loop. */
public class ResoluteDesk extends JPanel implements ActionListener, MouseListener, MouseMotionListener, KeyListener, MouseWheelListener {
    private static final long serialVersionUID = 1L;

    static ResoluteDesk APP;

    public JFrame frame;

    javax.swing.Timer timer;

    public int screen = SC_HOME, prevScreen = SC_HOME;

    public double screenT;                         // seconds since the screen changed (for intro animations)

    public Game game;

    public Settings setup = new Settings();

    double sc = 1, ox, oy;

    int winW = DW, winH = DH;

    public double mx = -1, my = -1, pressX, pressY;

    boolean pressed;

    public double tsec;

    long lastNanos;

    public final List<Hit> hits = new ArrayList<Hit>();

    final Map<String, double[]> scrolls = new HashMap<String, double[]>();   // id -> {x,y,w,h,offset,max,target}

    public String tip;

    Hit dragging;

    int layer;                              // 0 normal, 1 popup, 2 modal: only the top layer takes clicks

    int topLayer;

    // text editing
    String editId;

    StringBuilder editBuf = new StringBuilder();

    TextAction editSet;

    final List<Toast> toasts = new ArrayList<Toast>();

    public void toast(String s, Color c) {
        for (Toast o : toasts) if (o.text.equals(s)) { o.t0 = tsec; return; }
        Toast t = new Toast();
        t.text = s; t.col = c == null ? C_ACCENT : c; t.t0 = tsec;
        toasts.add(t);
        while (toasts.size() > 5) toasts.remove(0);
    }

    public Modal modal;

    final List<Modal> modalQueue = new ArrayList<Modal>();

    public Popup popup;

    public ResoluteDesk() {
        setPreferredSize(new Dimension(1440, 810));
        setBackground(C_BG);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addKeyListener(this);
        timer = new javax.swing.Timer(16, this);
        lastNanos = System.nanoTime();
    }

    static void launch() {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception e) { /* default */ }
        final ResoluteDesk p = new ResoluteDesk();
        APP = p;
        JFrame f = new JFrame("Resolute Desk");
        p.frame = f;
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) { p.timer.stop(); if (sfx != null) sfx.stop(); }
        });
        f.add(p);
        f.pack();
        Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
        int w = Math.min(1600, scr.width - 60), h = Math.min(900, scr.height - 80);
        double ar = DW / (double) DH;
        if (w / ar > h) w = (int) (h * ar); else h = (int) (w / ar);
        f.setSize(Math.max(960, w), Math.max(560, h + 28));
        f.setLocationRelativeTo(null);
        f.setVisible(true);
        p.requestFocusInWindow();
        sfx = new Sfx();
        sfx.start();
        new Thread(new Runnable() { public void run() { Elect.init(); USMap.load(); Groups.init(); } }, "rd-loader").start();
        p.timer.start();
    }

    public void actionPerformed(ActionEvent e) {
        long now = System.nanoTime();
        double dt = Math.min(0.1, (now - lastNanos) / 1e9);
        lastNanos = now;
        tsec += dt;
        screenT += dt;
        update(dt);
        repaint();
    }

    public void go(int s) {
        if (s != screen) { prevScreen = screen; screen = s; screenT = 0; popup = null; editId = null; }
    }

    void update(double dt) {
        if (game != null && (screen == SC_OVAL || screen == SC_ROOM || screen == SC_PAPER)) Oval.update(this, dt);
        USMap.animate(dt);
        for (double[] s : scrolls.values()) if (s.length > 6) s[4] += (s[6] - s[4]) * Math.min(1, dt * 14);
    }

    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        winW = getWidth();
        winH = getHeight();
        render(g, winW, winH);
    }

    void render(Graphics2D g, int w, int h) {
        g.setColor(Color.black);
        g.fillRect(0, 0, w, h);
        sc = Math.min(w / (double) DW, h / (double) DH);
        ox = (w - DW * sc) / 2;
        oy = (h - DH * sc) / 2;
        AffineTransform base = g.getTransform();
        g.translate(ox, oy);
        g.scale(sc, sc);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setClip(0, 0, DW, DH);
        hits.clear();
        tip = null;
        layer = 0;
        topLayer = modal != null ? 2 : popup != null ? 1 : 0;
        try {
            switch (screen) {
                case SC_HOME: Screens.home(this, g); break;
                case SC_SETUP: Screens.setup(this, g); break;
                case SC_PICK: Screens.pick(this, g); break;
                case SC_CREATE: Creator.draw(this, g); break;
                case SC_VP: Screens.vp(this, g); break;
                case SC_OVAL: Oval.draw(this, g); break;
                case SC_ROOM: Rooms.draw(this, g); break;
                case SC_PAPER: Paper.draw(this, g); break;
                case SC_NIGHT: Night.draw(this, g); break;
                case SC_LEGACY: Legacy.draw(this, g); break;
                case SC_HALL: Legacy.hall(this, g); break;
                case SC_IMPORT: BgImport.draw(this, g); break;
                case SC_TRANS: TransitionUI.draw(this, g); break;
                case SC_WATCH: Watch.draw(this, g); break;
                default: break;
            }
        } catch (RuntimeException ex) {
            g.setClip(0, 0, DW, DH);
            text(g, "Display error: " + ex, 20, DH - 20, F_XS, C_BAD);
            if (System.getProperty("rd.debug") != null) ex.printStackTrace();
        }
        g.setClip(0, 0, DW, DH);
        drawToasts(g);
        if (popup != null) { layer = 1; drawPopup(g); layer = 0; }
        if (modal == null && !modalQueue.isEmpty()) modal = modalQueue.remove(0);
        if (modal != null) { layer = 2; drawModal(g); layer = 0; }
        if (tip != null && dragging == null) drawTip(g, tip);
        g.setTransform(base);
    }

    // ---- input ------------------------------------------------------------------------------------------
    double dX(MouseEvent e) { return (e.getX() - ox) / sc; }

    double dY(MouseEvent e) { return (e.getY() - oy) / sc; }

    public void mousePressed(MouseEvent e) {
        requestFocusInWindow();
        mx = dX(e); my = dY(e);
        pressX = mx; pressY = my;
        pressed = true;
        if (editId != null) commitEdit();
        if (SwingUtilities.isRightMouseButton(e)) {
            if (popup != null) { popup = null; return; }
            if (modal == null && screen == SC_ROOM) Rooms.rightClick(this, mx, my);
            return;
        }
        int top = modal != null ? 2 : popup != null ? 1 : 0;
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit h = hits.get(i);
            if (h.layer != top) continue;
            if (h.in(mx, my)) {
                if (h.dragX != null) { dragging = h; h.dragX.on(clamp((mx - h.dragX0) / h.dragW, 0, 1)); return; }
                if (h.act != null) { snd(Sfx.CLICK, 0.6, 1); h.act.run(); }
                return;
            }
        }
        if (top == 1) { popup = null; return; }
        if (top == 2) return;
        if (screen == SC_ROOM) Rooms.press(this, mx, my);
    }

    public void mouseReleased(MouseEvent e) {
        mx = dX(e); my = dY(e);
        pressed = false;
        dragging = null;
    }

    public void mouseDragged(MouseEvent e) {
        double nx = dX(e), ny = dY(e);
        if (dragging != null && dragging.dragX != null) dragging.dragX.on(clamp((nx - dragging.dragX0) / dragging.dragW, 0, 1));
        else {
            for (double[] s : scrolls.values()) {
                if (pressX >= s[0] + s[2] - 12 && pressX <= s[0] + s[2] + 2 && pressY >= s[1] && pressY <= s[1] + s[3] && s[5] > 0) {
                    s[6] = clamp(s[6] + (ny - my) * (s[5] + s[3]) / s[3], 0, s[5]);
                    s[4] = s[6];
                }
            }
        }
        mx = nx; my = ny;
    }

    public void mouseMoved(MouseEvent e) { mx = dX(e); my = dY(e); }

    public void mouseWheelMoved(MouseWheelEvent e) {
        double x = dX(e), y = dY(e);
        if (modal != null || popup != null) return;
        double[] best = null;
        double area = 1e18;
        for (double[] s : scrolls.values()) {
            if (x >= s[0] && y >= s[1] && x < s[0] + s[2] && y < s[1] + s[3] && s[2] * s[3] < area && s[5] > 0) { best = s; area = s[2] * s[3]; }
        }
        if (best != null) best[6] = clamp(best[6] + e.getPreciseWheelRotation() * 48, 0, Math.max(0, best[5]));
    }

    public void mouseClicked(MouseEvent e) {}

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) { mx = -1; my = -1; }

    public void keyTyped(KeyEvent e) {
        if (editId == null) return;
        char c = e.getKeyChar();
        if (c >= 32 && c < 127 && editBuf.length() < 28) { editBuf.append(c); if (editSet != null) editSet.on(editBuf.toString()); }
    }

    public void keyReleased(KeyEvent e) {}

    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (editId != null) {
            if (k == KeyEvent.VK_BACK_SPACE && editBuf.length() > 0) { editBuf.setLength(editBuf.length() - 1); if (editSet != null) editSet.on(editBuf.toString()); }
            if (k == KeyEvent.VK_ENTER || k == KeyEvent.VK_ESCAPE || k == KeyEvent.VK_TAB) commitEdit();
            return;
        }
        if (modal != null) {
            if (k == KeyEvent.VK_ESCAPE && modal.buttons != null && modal.buttons.length == 1) closeModal();
            if (k == KeyEvent.VK_ENTER && modal.actions != null && modal.actions.length == 1) { Runnable r = modal.actions[0]; closeModal(); if (r != null) r.run(); }
            return;
        }
        if (popup != null) { if (k == KeyEvent.VK_ESCAPE) popup = null; return; }
        switch (screen) {
            case SC_OVAL: Oval.key(this, e); break;
            case SC_ROOM: Rooms.key(this, e); break;
            case SC_TRANS: TransitionUI.key(this, e); break;
            case SC_WATCH: Watch.key(this, e); break;
            case SC_PAPER: if (k == KeyEvent.VK_ESCAPE || k == KeyEvent.VK_SPACE) go(SC_OVAL); break;
            case SC_SETUP: case SC_PICK: case SC_CREATE: case SC_VP:
                if (k == KeyEvent.VK_ESCAPE) Screens.back(this);
                break;
            default: break;
        }
    }

    void commitEdit() { editId = null; }

    // ---- widgets ----------------------------------------------------------------------------------------
    public boolean hover(double x, double y, double w, double h) {
        return dragging == null && layer == topLayer && mx >= x && my >= y && mx < x + w && my < y + h;
    }

    public Hit hit(double x, double y, double w, double h, Runnable a, String t) {
        Hit hh = new Hit();
        hh.x = x; hh.y = y; hh.w = w; hh.h = h; hh.act = a; hh.tip = t; hh.layer = layer;
        hits.add(hh);
        if (t != null && hover(x, y, w, h)) tip = t;
        return hh;
    }

    /** A horizontal drag region (sliders). */
    void dragRegion(double x, double y, double w, double h, double x0, double ww, DoubleAction f, String t) {
        Hit hh = hit(x, y, w, h, null, t);
        hh.dragX = f; hh.dragX0 = x0; hh.dragW = ww;
    }

    public boolean button(Graphics2D g, double x, double y, double w, double h, String label, int style, Runnable act) {
        return button(g, x, y, w, h, label, style, act, null);
    }

    public boolean button(Graphics2D g, double x, double y, double w, double h, String label, int style, Runnable act, String tipText) {
        boolean dis = act == null || style == B_OFF;
        boolean hov = !dis && hover(x, y, w, h);
        boolean down = hov && pressed;
        Color edge, top, bot, txt;
        switch (style) {
            case B_PRIMARY: edge = rgb(0x5aa9ff); top = rgb(0x2f6fe0); bot = rgb(0x1d4fb0); txt = Color.white; break;
            case B_GOLD: edge = rgb(0xffd77a); top = rgb(0xf2c14e); bot = rgb(0xd49a1f); txt = rgb(0x1d1405); break;
            case B_DANGER: edge = rgb(0xff7b7b); top = rgb(0xc93a3f); bot = rgb(0x8f2228); txt = Color.white; break;
            case B_ON: edge = rgb(0x5be39a); top = rgb(0x1f8f55); bot = rgb(0x136b3e); txt = Color.white; break;
            case B_DEM: edge = C_DEM_L; top = rgb(0x2f6fe0); bot = C_DEM_D; txt = Color.white; break;
            case B_REP: edge = C_REP_L; top = rgb(0xd9434a); bot = C_REP_D; txt = Color.white; break;
            case B_GHOST: edge = new Color(120, 150, 200, 90); top = new Color(255, 255, 255, 8); bot = new Color(255, 255, 255, 4); txt = C_TEXT; break;
            case B_SUBTLE: edge = new Color(255, 255, 255, 0); top = new Color(255, 255, 255, 10); bot = new Color(255, 255, 255, 6); txt = C_DIM; break;
            case B_TAB: edge = new Color(80, 110, 160, 0); top = new Color(255, 255, 255, 0); bot = new Color(255, 255, 255, 0); txt = C_DIM; break;
            case B_TABON: edge = new Color(124, 196, 255, 0); top = new Color(124, 196, 255, 34); bot = new Color(124, 196, 255, 18); txt = Color.white; break;
            case B_OFF: edge = new Color(70, 80, 100, 120); top = new Color(30, 36, 48, 200); bot = new Color(24, 28, 38, 200); txt = C_FAINT; break;
            default: edge = new Color(90, 120, 170, 160); top = rgb(0x1c2a44); bot = rgb(0x152036); txt = C_TEXT; break;
        }
        if (dis && style != B_TAB && style != B_TABON) { edge = new Color(70, 80, 100, 110); top = new Color(30, 36, 48, 190); bot = new Color(24, 28, 38, 190); txt = C_FAINT; }
        if (hov) { top = shade(top, style == B_GHOST || style == B_TAB || style == B_SUBTLE ? 1.0 : 1.18); bot = shade(bot, 1.15); edge = shade(edge, 1.2); if (style == B_TAB || style == B_GHOST || style == B_SUBTLE) { top = new Color(255, 255, 255, 22); bot = new Color(255, 255, 255, 14); txt = Color.white; } }
        if (down) { Color t = top; top = bot; bot = t; }
        RoundRectangle2D rr = new RoundRectangle2D.Double(x, y, w, h, 8, 8);
        g.setPaint(new GradientPaint((float) x, (float) y, top, (float) x, (float) (y + h), bot));
        g.fill(rr);
        if (style == B_PRIMARY || style == B_GOLD || style == B_DANGER || style == B_ON || style == B_DEM || style == B_REP) {
            g.setColor(new Color(255, 255, 255, 40));
            g.draw(new Line2D.Double(x + 6, y + 1.5, x + w - 6, y + 1.5));
        }
        if (edge.getAlpha() > 0) {
            g.setColor(edge);
            g.setStroke(new BasicStroke(hov ? 1.4f : 1f));
            g.draw(rr);
        }
        if (style == B_TABON) {
            g.setColor(C_ACCENT);
            g.fill(new RoundRectangle2D.Double(x + 8, y + h - 3, w - 16, 3, 3, 3));
        }
        boolean tabStyle = style == B_TAB || style == B_TABON;
        Font f = tabStyle ? F_TB : h >= 40 ? F_H2 : h >= 30 ? F_BB : h >= 22 ? F_SB : F_XSB;
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        String s = label;
        while (fm.stringWidth(s) > w - (tabStyle ? 4 : 10) && s.length() > 3) s = s.substring(0, s.length() - 2);
        g.setColor(txt);
        g.drawString(s, (float) (x + (w - fm.stringWidth(s)) / 2), (float) (y + h / 2 + fm.getAscent() / 2.0 - 2.5));
        Hit hh = hit(x, y, w, h, dis ? null : act, tipText);
        hh.lab = label;
        if (hov && hov != lastHoverState(label)) snd(Sfx.HOVER, 0.35, 1);
        return hov;
    }

    /** A small button with a drawn glyph. */
    boolean iconButton(Graphics2D g, double x, double y, double w, double h, int glyph, Runnable act, String tipText) {
        boolean dis = act == null;
        boolean hov = !dis && hover(x, y, w, h);
        RoundRectangle2D rr = new RoundRectangle2D.Double(x, y, w, h, 6, 6);
        g.setColor(hov ? new Color(255, 255, 255, 30) : new Color(255, 255, 255, dis ? 4 : 12));
        g.fill(rr);
        g.setColor(dis ? C_FAINT : hov ? Color.white : C_DIM);
        double cx = x + w / 2, cy = y + h / 2, r = Math.min(w, h) * 0.26;
        Path2D p = new Path2D.Double();
        switch (glyph) {
            case IC_UP: p.moveTo(cx - r, cy + r * 0.6); p.lineTo(cx, cy - r * 0.6); p.lineTo(cx + r, cy + r * 0.6); p.closePath(); g.fill(p); break;
            case IC_DOWN: p.moveTo(cx - r, cy - r * 0.6); p.lineTo(cx, cy + r * 0.6); p.lineTo(cx + r, cy - r * 0.6); p.closePath(); g.fill(p); break;
            case IC_LEFT: p.moveTo(cx + r * 0.6, cy - r); p.lineTo(cx - r * 0.6, cy); p.lineTo(cx + r * 0.6, cy + r); p.closePath(); g.fill(p); break;
            case IC_RIGHT: p.moveTo(cx - r * 0.6, cy - r); p.lineTo(cx + r * 0.6, cy); p.lineTo(cx - r * 0.6, cy + r); p.closePath(); g.fill(p); break;
            default:
                g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Line2D.Double(cx - r, cy - r, cx + r, cy + r));
                g.draw(new Line2D.Double(cx - r, cy + r, cx + r, cy - r));
                g.setStroke(new BasicStroke(1f));
        }
        Hit hh = hit(x, y, w, h, act, tipText);
        hh.lab = "icon" + glyph;
        return hov;
    }

    String lastHover;

    boolean lastHoverState(String label) {
        boolean same = label.equals(lastHover);
        lastHover = label;
        return same;
    }

    /** A glass panel with an optional small-caps title. */
    public void panel(Graphics2D g, double x, double y, double w, double h, String title) {
        RoundRectangle2D rr = new RoundRectangle2D.Double(x, y, w, h, 12, 12);
        g.setPaint(new GradientPaint((float) x, (float) y, new Color(18, 28, 48, 236), (float) x, (float) (y + h), new Color(10, 17, 30, 240)));
        g.fill(rr);
        g.setColor(new Color(110, 150, 220, 46));
        g.setStroke(new BasicStroke(1f));
        g.draw(rr);
        g.setColor(new Color(255, 255, 255, 18));
        g.draw(new Line2D.Double(x + 10, y + 1, x + w - 10, y + 1));
        if (title != null) {
            label(g, title, x + 14, y + 22, C_DIM);
        }
    }

    /** A flat card (for list items); returns hover. */
    public boolean card(Graphics2D g, double x, double y, double w, double h, boolean selected, Runnable act, String t) {
        boolean hov = act != null && hover(x, y, w, h);
        RoundRectangle2D rr = new RoundRectangle2D.Double(x, y, w, h, 10, 10);
        g.setColor(selected ? new Color(47, 123, 246, 46) : hov ? new Color(255, 255, 255, 16) : new Color(255, 255, 255, 7));
        g.fill(rr);
        g.setColor(selected ? new Color(124, 196, 255, 200) : hov ? new Color(124, 196, 255, 90) : new Color(255, 255, 255, 18));
        g.setStroke(new BasicStroke(selected ? 1.6f : 1f));
        g.draw(rr);
        if (act != null || t != null) hit(x, y, w, h, act, t);
        return hov;
    }

    /** A horizontal slider; value in [0,1]. */
    public void slider(Graphics2D g, double x, double y, double w, double v, Color c, DoubleAction set, String t) { slider(g, x, y, w, v, c, set, t, true); }

    public void slider(Graphics2D g, double x, double y, double w, double v, Color c, DoubleAction set, String t, boolean fill) {
        double h = 6;
        g.setColor(new Color(255, 255, 255, 22));
        g.fill(new RoundRectangle2D.Double(x, y - h / 2, w, h, h, h));
        g.setColor(c);
        if (fill) g.fill(new RoundRectangle2D.Double(x, y - h / 2, w * clamp(v, 0, 1), h, h, h));
        double kx = x + w * clamp(v, 0, 1);
        boolean hov = hover(x - 8, y - 10, w + 16, 20);
        g.setColor(Color.white);
        g.fill(new Ellipse2D.Double(kx - (hov ? 8 : 7), y - (hov ? 8 : 7), hov ? 16 : 14, hov ? 16 : 14));
        g.setColor(c);
        g.fill(new Ellipse2D.Double(kx - 4, y - 4, 8, 8));
        if (set != null) dragRegion(x - 8, y - 11, w + 16, 22, x, w, set, t);
    }

    /** A -/+ stepper; returns the width used. */
    public double stepper(Graphics2D g, double x, double y, String value, Runnable minus, Runnable plus, String t, double vw) {
        button(g, x, y, 24, 24, "-", B_NORM, minus, t);
        g.setColor(new Color(0, 0, 0, 70));
        g.fill(new RoundRectangle2D.Double(x + 27, y, vw, 24, 6, 6));
        textC(g, value, x + 27 + vw / 2, y + 16.5, F_SB, C_TEXT);
        button(g, x + 30 + vw, y, 24, 24, "+", B_NORM, plus, t);
        return 54 + vw;
    }

    /** A pill toggle. */
    public void toggle(Graphics2D g, double x, double y, boolean on, Runnable act, String t) {
        double w = 36, h = 20;
        boolean hov = hover(x, y, w, h);
        g.setColor(on ? rgb(0x2f9e5f) : new Color(255, 255, 255, hov ? 40 : 26));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, h, h));
        double kx = on ? x + w - h + 2 : x + 2;
        g.setColor(Color.white);
        g.fill(new Ellipse2D.Double(kx, y + 2, h - 4, h - 4));
        hit(x, y, w, h, act, t);
    }

    /** A row of tabs; returns nothing, calls pick on click. */
    public void tabs(Graphics2D g, double x, double y, double w, double h, String[] names, int cur, IntAction pick) {
        double tw = w / names.length;
        for (int i = 0; i < names.length; i++) {
            final int k = i;
            button(g, x + i * tw, y, tw - 2, h, names[i], i == cur ? B_TABON : B_TAB, () -> pick.on(k));
        }
        g.setColor(new Color(255, 255, 255, 20));
        g.draw(new Line2D.Double(x, y + h, x + w, y + h));
    }

    /** A segmented control. */
    public void segmented(Graphics2D g, double x, double y, double w, double h, String[] names, int cur, IntAction pick, String[] tips) {
        RoundRectangle2D rr = new RoundRectangle2D.Double(x, y, w, h, 10, 10);
        g.setColor(new Color(0, 0, 0, 80));
        g.fill(rr);
        g.setColor(new Color(255, 255, 255, 22));
        g.draw(rr);
        double tw = w / names.length;
        for (int i = 0; i < names.length; i++) {
            final int k = i;
            double bx = x + i * tw;
            boolean on = i == cur, hov = hover(bx, y, tw, h);
            if (on) {
                g.setPaint(new GradientPaint((float) bx, (float) y, rgb(0x2f6fe0), (float) bx, (float) (y + h), rgb(0x1d4fb0)));
                g.fill(new RoundRectangle2D.Double(bx + 2, y + 2, tw - 4, h - 4, 8, 8));
            } else if (hov) {
                g.setColor(new Color(255, 255, 255, 16));
                g.fill(new RoundRectangle2D.Double(bx + 2, y + 2, tw - 4, h - 4, 8, 8));
            }
            g.setFont(h >= 30 ? F_SB : F_XSB);
            FontMetrics fm = g.getFontMetrics();
            String s = names[i];
            while (fm.stringWidth(s) > tw - 8 && s.length() > 3) s = s.substring(0, s.length() - 2);
            g.setColor(on ? Color.white : hov ? C_TEXT : C_DIM);
            g.drawString(s, (float) (bx + (tw - fm.stringWidth(s)) / 2), (float) (y + h / 2 + fm.getAscent() / 2.0 - 2));
            Hit sh = hit(bx, y, tw, h, () -> pick.on(k), tips == null ? null : tips[i]);
            sh.lab = "seg:" + names[i];
        }
    }

    /** A text field; click to edit. */
    public void textField(Graphics2D g, String id, double x, double y, double w, double h, String value, TextAction set) {
        boolean ed = id.equals(editId);
        RoundRectangle2D rr = new RoundRectangle2D.Double(x, y, w, h, 8, 8);
        g.setColor(new Color(0, 0, 0, ed ? 120 : 80));
        g.fill(rr);
        g.setColor(ed ? C_ACCENT : new Color(255, 255, 255, 40));
        g.draw(rr);
        String v = ed ? editBuf.toString() : value;
        text(g, v, x + 10, y + h / 2 + 5, F_B, C_TEXT);
        if (ed && ((int) (tsec * 2)) % 2 == 0) {
            double cx = x + 10 + strW(g, v, F_B) + 1;
            g.setColor(C_ACCENT);
            g.fill(new Rectangle2D.Double(cx, y + 6, 1.6, h - 12));
        }
        hit(x, y, w, h, () -> { editId = id; editBuf.setLength(0); editBuf.append(value); editSet = set; }, "Click to type");
    }

    /** Begins a scrollable region; returns the current scroll offset. */
    public double beginScroll(Graphics2D g, String id, double x, double y, double w, double h) {
        double[] s = scrolls.get(id);
        if (s == null) { s = new double[]{x, y, w, h, 0, 0, 0}; scrolls.put(id, s); }
        s[0] = x; s[1] = y; s[2] = w; s[3] = h;
        g.setClip(new Rectangle2D.Double(x, y, w, h));
        return Math.round(s[4]);
    }

    public void endScroll(Graphics2D g, String id, double contentH) {
        double[] s = scrolls.get(id);
        g.setClip(0, 0, DW, DH);
        if (s == null) return;
        s[5] = Math.max(0, contentH - s[3]);
        s[6] = clamp(s[6], 0, s[5]);
        s[4] = clamp(s[4], 0, s[5]);
        if (s[5] > 0) {
            double th = Math.max(24, s[3] * s[3] / contentH);
            double ty = s[1] + (s[3] - th) * (s[4] / s[5]);
            g.setColor(new Color(255, 255, 255, 14));
            g.fill(new RoundRectangle2D.Double(s[0] + s[2] - 5, s[1] + 2, 4, s[3] - 4, 4, 4));
            g.setColor(new Color(124, 196, 255, 130));
            g.fill(new RoundRectangle2D.Double(s[0] + s[2] - 5, ty, 4, th, 4, 4));
        }
    }

    /** Jumps a scrollable region to an offset (clamped when it's next drawn). */
    public void scrollTo(String id, double off) {
        double[] s = scrolls.get(id);
        if (s == null) { s = new double[]{0, 0, 0, 0, 0, 0, 0}; scrolls.put(id, s); }
        s[4] = Math.max(0, off); s[6] = s[4];
    }

    void resetScroll(String id) { double[] s = scrolls.get(id); if (s != null) { s[4] = 0; s[6] = 0; } }

    /** Hits outside the visible part of a scroll region are dropped (call after drawing the list). */
    public void clipHits(int from, double y0, double y1) {
        for (int i = hits.size() - 1; i >= from; i--) {
            Hit h = hits.get(i);
            if (h.y + h.h < y0 || h.y > y1) hits.remove(i);
            else if (h.y < y0) { h.h -= y0 - h.y; h.y = y0; }
            else if (h.y + h.h > y1) h.h = y1 - h.y;
        }
    }

    // ---- modal ------------------------------------------------------------------------------------------
    public void modal(String title, String body, String[] buttons, Runnable[] actions, Color col) {
        Modal m = new Modal();
        m.title = title; m.body = body; m.buttons = buttons; m.actions = actions; m.col = col == null ? C_ACCENT : col;
        if (modal == null) modal = m; else modalQueue.add(m);
        snd(Sfx.OPEN);
    }

    public void info(String title, String body) { modal(title, body, new String[]{"OK"}, new Runnable[]{null}, null); }

    void closeModal() { modal = null; }

    void drawModal(Graphics2D g) {
        Modal m = modal;
        g.setColor(new Color(2, 6, 14, 175));
        g.fillRect(0, 0, DW, DH);
        double w = m.w;
        double faceW = m.face != null ? 150 : 0;
        double tw = w - 56 - faceW;
        double th = m.body == null ? 0 : wrapHeight(g, m.body, tw, F_B);
        double oh = 0;
        if (m.stacked && m.buttons != null) for (int i = 0; i < m.buttons.length; i++) oh += Math.max(46, 28 + (m.descs != null && m.descs[i] != null ? wrapHeight(g, m.descs[i], w - 76, F_XS) : 0)) + 8;
        // stacked options always start below the portrait so they never run into the candidate's face
        double faceBottom = m.face != null ? 26 + 150 + 14 : 0;
        double textBottom = 74 + th + m.extraH;
        double optsTop = m.stacked ? Math.max(textBottom + 8, faceBottom) : textBottom;
        double h = Math.min(DH - 60, Math.max(m.face != null ? 250 : 0, optsTop + (m.stacked ? oh + 16 : 64)));
        double x = (DW - w) / 2, y = (DH - h) / 2;
        double pop = easeOut(Math.min(1, (tsec - modalT(m)) * 5));
        y += (1 - pop) * 18;
        RoundRectangle2D rr = new RoundRectangle2D.Double(x, y, w, h, 16, 16);
        g.setColor(new Color(0, 0, 0, 120));
        g.fill(new RoundRectangle2D.Double(x + 4, y + 10, w, h, 16, 16));
        g.setPaint(new GradientPaint((float) x, (float) y, rgb(0x16233b), (float) x, (float) (y + h), rgb(0x0c1424)));
        g.fill(rr);
        g.setColor(alpha(m.col, 0.75));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(rr);
        g.setColor(m.col);
        g.fill(new RoundRectangle2D.Double(x + 24, y, 80, 4, 4, 4));
        double tx = x + 28 + faceW;
        if (m.face != null) Portrait.draw(g, m.face, x + 22, y + 26, 128, 150, tsec, 0, false, true);
        textFit(g, m.title, tx, y + 44, w - 56 - faceW, F_H1, Color.white);
        double cy = y + 74;
        if (m.body != null) cy += wrap(g, m.body, tx, cy, tw, F_B, C_TEXT);
        if (m.painter != null) { m.painter.paint(g, tx, cy, tw, m.extraH); cy += m.extraH; }
        if (m.buttons == null) return;
        if (m.stacked) {
            cy = Math.max(cy + 8, y + faceBottom);
            for (int i = 0; i < m.buttons.length; i++) {
                final int k = i;
                String d = m.descs != null ? m.descs[i] : null;
                double bh = Math.max(46, 28 + (d != null ? wrapHeight(g, d, w - 76, F_XS) : 0));
                boolean hov = card(g, x + 24, cy, w - 48, bh, false, () -> { Runnable r = m.actions[k]; closeModal(); if (r != null) r.run(); }, null);
                if (!hits.isEmpty()) hits.get(hits.size() - 1).lab = m.buttons[i];
                g.setColor(hov ? m.col : alpha(m.col, 0.6));
                g.fill(new RoundRectangle2D.Double(x + 24, cy, 4, bh, 4, 4));
                text(g, m.buttons[i], x + 40, cy + 20, F_BB, hov ? Color.white : C_TEXT);
                if (d != null) wrap(g, d, x + 40, cy + 37, w - 76, F_XS, C_DIM);
                cy += bh + 8;
            }
        } else {
            double bw = Math.max(130, Math.min(200, (w - 48 - 10 * (m.buttons.length - 1)) / m.buttons.length)), bx = x + w - 24 - bw * m.buttons.length - 10 * (m.buttons.length - 1);
            for (int i = 0; i < m.buttons.length; i++) {
                final int k = i;
                button(g, bx + i * (bw + 10), y + h - 52, bw, 34, m.buttons[i], i == m.buttons.length - 1 ? (m.col == C_BAD ? B_DANGER : B_PRIMARY) : B_NORM, () -> {
                    Runnable r = m.actions[k];
                    closeModal();
                    if (r != null) r.run();
                });
            }
        }
    }

    final Map<Modal, Double> modalTimes = new HashMap<Modal, Double>();

    double modalT(Modal m) {
        Double t = modalTimes.get(m);
        if (t == null) { t = tsec; modalTimes.clear(); modalTimes.put(m, t); }
        return t;
    }

    // ---- popup menus -----------------------------------------------------------------------------------------
    public Popup openPopup(double x, double y, double w, String title, String[] items, IntAction pick) {
        Popup p = new Popup();
        p.x = x; p.y = y; p.w = w; p.title = title; p.items = items; p.pick = pick;
        popup = p;
        snd(Sfx.OPEN, 0.5, 1.3);
        return p;
    }

    void drawPopup(Graphics2D g) {
        Popup p = popup;
        double rowH = 26, head = p.title != null ? 26 : 6;
        double h = head + p.items.length * rowH + 6;
        double x = Math.min(p.x, DW - p.w - 8), y = Math.min(p.y, DH - h - 8);
        y = Math.max(8, y);
        RoundRectangle2D rr = new RoundRectangle2D.Double(x, y, p.w, h, 12, 12);
        g.setColor(new Color(0, 0, 0, 130));
        g.fill(new RoundRectangle2D.Double(x + 3, y + 8, p.w, h, 12, 12));
        g.setColor(rgb(0x121d31));
        g.fill(rr);
        g.setColor(new Color(124, 196, 255, 110));
        g.draw(rr);
        if (p.title != null) label(g, p.title, x + 12, y + 18, C_DIM);
        for (int i = 0; i < p.items.length; i++) {
            final int k = i;
            double ry = y + head + i * rowH;
            boolean en = p.enabled == null || p.enabled[i];
            boolean hov = en && hover(x + 4, ry, p.w - 8, rowH);
            if (hov) {
                g.setColor(new Color(47, 123, 246, 70));
                g.fill(new RoundRectangle2D.Double(x + 4, ry + 1, p.w - 8, rowH - 2, 8, 8));
            }
            if (p.cols != null && p.cols[i] != null) {
                g.setColor(p.cols[i]);
                g.fill(new RoundRectangle2D.Double(x + 12, ry + 9, 8, 8, 3, 3));
            }
            text(g, p.items[i], x + (p.cols != null ? 28 : 12), ry + 17, F_S, en ? (hov ? Color.white : C_TEXT) : C_FAINT);
            if (p.right != null && p.right[i] != null) textR(g, p.right[i], x + p.w - 12, ry + 17, F_XS, en ? C_DIM : C_FAINT);
            Hit ph = hit(x + 4, ry, p.w - 8, rowH, en ? () -> { popup = null; p.pick.on(k); } : null, p.tips != null ? p.tips[i] : null);
            ph.lab = "popup:" + p.items[i];
        }
    }

    // ---- tooltips and toasts ---------------------------------------------------------------------------------------
    void drawTip(Graphics2D g, String t) {
        g.setFont(F_S);
        FontMetrics fm = g.getFontMetrics();
        double maxW = 340;
        List<String> wrapped = new ArrayList<String>();
        for (String l : t.split("\n")) {
            if (fm.stringWidth(l) <= maxW) { wrapped.add(l); continue; }
            StringBuilder cur = new StringBuilder();
            for (String wd : l.split(" ")) {
                if (fm.stringWidth(cur + " " + wd) > maxW && cur.length() > 0) { wrapped.add(cur.toString()); cur = new StringBuilder(wd); }
                else cur.append(cur.length() > 0 ? " " : "").append(wd);
            }
            wrapped.add(cur.toString());
        }
        double w = 0;
        for (String l : wrapped) w = Math.max(w, fm.stringWidth(l));
        double h = wrapped.size() * fm.getHeight() + 12;
        double x = mx + 16, y = my + 18;
        if (x + w + 20 > DW) x = mx - w - 24;
        if (y + h > DH) y = my - h - 8;
        RoundRectangle2D rr = new RoundRectangle2D.Double(x, y, w + 18, h, 10, 10);
        g.setColor(new Color(4, 9, 18, 242));
        g.fill(rr);
        g.setColor(new Color(124, 196, 255, 110));
        g.draw(rr);
        double cy = y + fm.getAscent() + 6;
        boolean first = true;
        for (String l : wrapped) { text(g, l, x + 9, cy, first && wrapped.size() > 1 && t.indexOf('\n') > 0 && l.equals(t.split("\n")[0]) ? F_SB : F_S, C_TEXT); cy += fm.getHeight(); first = false; }
    }

    void drawToasts(Graphics2D g) {
        boolean hq = false;
        double y = DH - 150;
        for (int i = toasts.size() - 1; i >= 0; i--) {
            Toast t = toasts.get(i);
            double age = tsec - t.t0;
            if (age > 5) { toasts.remove(i); continue; }
            double a = Math.min(1, Math.min(age * 5, (5 - age) * 2));
            double w = Math.min(hq ? 600 : 460, strW(g, t.text, F_S) + 36);
            double x = DW - w - 20 + (1 - easeOut(Math.min(1, age * 4))) * 40;
            if (hq) y -= 0;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) clamp(a, 0, 1)));
            RoundRectangle2D rr = new RoundRectangle2D.Double(x, y, w, 34, 10, 10);
            g.setColor(new Color(8, 14, 26, 235));
            g.fill(rr);
            g.setColor(alpha(t.col, 0.7));
            g.draw(rr);
            g.setColor(t.col);
            g.fill(new RoundRectangle2D.Double(x + 8, y + 10, 4, 14, 4, 4));
            textFit(g, t.text, x + 20, y + 22, w - 28, F_S, C_TEXT);
            g.setComposite(AlphaComposite.SrcOver);
            y -= 40;
        }
    }

    /** Deep navy war-room backdrop with a faint grid and vignette (cached). */
    public void backdrop(Graphics2D g) {
        if (bgCache == null) {
            BufferedImage img = new BufferedImage(DW, DH, BufferedImage.TYPE_INT_RGB);
            Graphics2D b = img.createGraphics();
            b.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            b.setPaint(new GradientPaint(0, 0, rgb(0x081120), 0, DH, rgb(0x04080f)));
            b.fillRect(0, 0, DW, DH);
            b.setPaint(new RadialGradientPaint(new Point2D.Double(DW * 0.5, DH * 0.42), DW * 0.6f, new float[]{0f, 1f},
                    new Color[]{new Color(40, 80, 160, 60), new Color(0, 0, 0, 0)}));
            b.fillRect(0, 0, DW, DH);
            b.setColor(new Color(120, 160, 230, 9));
            for (int x = 0; x < DW; x += 40) b.drawLine(x, 0, x, DH);
            for (int y = 0; y < DH; y += 40) b.drawLine(0, y, DW, y);
            b.setPaint(new RadialGradientPaint(new Point2D.Double(DW / 2.0, DH / 2.0), DW * 0.75f, new float[]{0.55f, 1f},
                    new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 150)}));
            b.fillRect(0, 0, DW, DH);
            b.dispose();
            bgCache = img;
        }
        g.drawImage(bgCache, 0, 0, null);
    }

    /** Renders the current screen into an image (used for testing). */
    public BufferedImage snapshot(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        winW = w; winH = h;
        render(g, w, h);
        g.dispose();
        return img;
    }

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("test")) { Diagnostics.run(args); return; }
        SwingUtilities.invokeLater(new Runnable() { public void run() { launch(); } });
    }
}
