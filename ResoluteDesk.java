/*
 * ============================================================================
 *  ResoluteDesk.java  -  "RESOLUTE DESK: A Presidency Simulator"
 *  A game about the job of president, for BlueJ (single file, Java 8 or newer).
 * ============================================================================
 *
 *  HOW TO RUN IN BLUEJ
 *    1. Project > New Project (any name).
 *    2. Edit > Add Class from File...  and choose this file  (or create a class
 *       named  ResoluteDesk  and paste the whole file into it).
 *    3. Compile, then right-click ResoluteDesk and choose void main(String[] args).
 *
 *  WHAT IT IS
 *    Take the oath on January 20 and govern. Each turn is one week: plan the
 *    president's days in three slots each (policy time, remarks, trips on Air
 *    Force One, Hill meetings, fundraisers, rest, family, Camp David), watch
 *    your stamina, sleep and stress, and read the morning paper. A weekly
 *    economic model moves growth, jobs, inflation, the Fed, markets, gas and
 *    the prices people feel, the deficit and the debt; approval follows, by
 *    issue, by group and in every state (96 voter cells per state).
 *
 *    Real people appear as stylised characters with ratings that are the
 *    game's estimates; the game never quotes them. Everything that happens in
 *    a game is fiction.
 *
 *  CONTROLS
 *    Click the desk, the windows, the briefing book, the doors, the globe or
 *    the papers in the Oval Office.  Space ... end the week   1-8 ... rooms
 *    Esc ... back to the Oval Office   P ... the paper
 *
 *  SAVES
 *    The game autosaves every week to a folder named .resolutedesk in your
 *    home directory; "Continue" on the title screen picks up where you left off.
 * ============================================================================
 */

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ResoluteDesk extends JPanel implements ActionListener, MouseListener, MouseMotionListener, KeyListener, MouseWheelListener {
    private static final long serialVersionUID = 1L;

    // =====================================================================
    //  1.  CONSTANTS AND SMALL HELPERS
    // =====================================================================
    static final int DW = 1600, DH = 900;      // design resolution; everything is scaled to the window

    // parties
    static final int DEM = 0, REP = 1, GRN = 2, LBT = 3, FWD = 4, AMP = 5, IND = 6;
    static final String[] PARTY = {"Democratic", "Republican", "Green", "Libertarian", "Forward", "America Party", "Independent", "Your party"};
    static final String[] PARTY_SHORT = {"D", "R", "G", "L", "F", "A", "I", "U"};
    static final Color C_IND = new Color(0xf5, 0x9e, 0x0b);

    static double clamp(double v, double lo, double hi) { return v < lo ? lo : v > hi ? hi : v; }

    static int clampi(int v, int lo, int hi) { return v < lo ? lo : v > hi ? hi : v; }

    static double ease(double t) { t = clamp(t, 0, 1); return t * t * (3 - 2 * t); }

    static double easeOut(double t) { t = clamp(t, 0, 1); return 1 - (1 - t) * (1 - t) * (1 - t); }

    static double lerp(double a, double b, double t) { return a + (b - a) * t; }

    static double logistic(double x) { return 1 / (1 + Math.exp(-x)); }

    static double logit(double p) { p = clamp(p, 1e-6, 1 - 1e-6); return Math.log(p / (1 - p)); }

    static Color mix(Color a, Color b, double t) {
        t = clamp(t, 0, 1);
        return new Color((int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t),
                (int) (a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t));
    }

    static Color alpha(Color c, double a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) clamp(a * 255, 0, 255));
    }

    static Color shade(Color c, double f) {
        if (f < 1) return new Color((int) (c.getRed() * f), (int) (c.getGreen() * f), (int) (c.getBlue() * f), c.getAlpha());
        return mix(c, Color.white, f - 1);
    }

    static Color rgb(int hex) { return new Color((hex >> 16) & 255, (hex >> 8) & 255, hex & 255); }

    static String fmt1(double v) { return String.format("%.1f", v); }

    static String fmt0(double v) { return String.valueOf(Math.round(v)); }

    static String pct0(double v) { return Math.round(v * 100) + "%"; }

    static String pct1(double v) { return String.format("%.1f%%", v * 100); }

    static String signed1(double v) { if (Math.abs(v) < 0.05) return "0.0"; return (v >= 0 ? "+" : "") + String.format("%.1f", v); }

    /** A two-party margin in points, written D+3.2 / R+1.5. */
    static String margin(double dMinusR) {
        if (Math.abs(dMinusR) < 0.05) return "TIED";
        return (dMinusR > 0 ? "D+" : "R+") + String.format("%.1f", Math.abs(dMinusR));
    }

    static String thousands(long v) {
        String s = String.valueOf(Math.abs(v));
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (i > 0 && (s.length() - i) % 3 == 0) b.append(',');
            b.append(s.charAt(i));
        }
        return (v < 0 ? "-" : "") + b;
    }

    /** Money in dollars, compact: $950K, $12.4M, $1.21B. */
    static String money(double d) {
        double a = Math.abs(d);
        String s;
        if (a >= 1e9) s = String.format("$%.2fB", a / 1e9);
        else if (a >= 1e7) s = String.format("$%.1fM", a / 1e6);
        else if (a >= 1e6) s = String.format("$%.2fM", a / 1e6);
        else if (a >= 1e3) s = String.format("$%.0fK", a / 1e3);
        else s = String.format("$%.0f", a);
        return (d < 0 ? "-" : "") + s;
    }

    static String compact(double v) {
        double a = Math.abs(v);
        if (a >= 1e6) return String.format("%.1fM", v / 1e6);
        if (a >= 1e4) return String.format("%.0fK", v / 1e3);
        if (a >= 1e3) return String.format("%.1fK", v / 1e3);
        return String.valueOf(Math.round(v));
    }

    // ---- palette: "war room" ---------------------------------------------------------------------------
    static final Color C_BG = rgb(0x060a13);
    static final Color C_BG2 = rgb(0x0b1220);
    static final Color C_PANEL = new Color(13, 21, 36, 236);
    static final Color C_PANEL2 = new Color(19, 30, 50, 240);
    static final Color C_CARD = new Color(22, 34, 56, 245);
    static final Color C_LINE = new Color(46, 66, 98, 200);
    static final Color C_LINE2 = new Color(80, 110, 160, 220);
    static final Color C_TEXT = rgb(0xe7edf7);
    static final Color C_DIM = rgb(0x93a4bf);
    static final Color C_FAINT = rgb(0x56688a);
    static final Color C_GOLD = rgb(0xf2c14e);
    static final Color C_ACCENT = rgb(0x7cc4ff);
    static final Color C_DEM = rgb(0x2f7bf6);
    static final Color C_DEM_L = rgb(0x79acff);
    static final Color C_DEM_D = rgb(0x163d86);
    static final Color C_REP = rgb(0xe5484d);
    static final Color C_REP_L = rgb(0xff8a8d);
    static final Color C_REP_D = rgb(0x8a1f26);
    static final Color C_GREEN = rgb(0x2bd47d);
    static final Color C_MONEY = rgb(0x1fbf6a);
    static final Color C_THIRD = rgb(0xa78bfa);
    static final Color C_AMBER = rgb(0xffb347);
    static final Color C_BAD = rgb(0xff5d5d);
    static final Color C_GOOD = rgb(0x45d483);
    static final Color C_TEAL = rgb(0x2dd4bf);
    static final Color C_STATE = rgb(0x2a3650);
    static final Color C_STATE_EDGE = rgb(0x0a1020);

    static Color partyColor(int p) {
        switch (p) {
            case DEM: return C_DEM;
            case REP: return C_REP;
            case GRN: return rgb(0x3fbf5f);
            case LBT: return rgb(0xf5c542);
            case FWD: return rgb(0x8b5cf6);
            case IND: return C_IND;
            case CUS: return rgb(0xec4899);
            default: return rgb(0x22c3d6);
        }
    }

    static Color partyLight(int p) { return p == DEM ? C_DEM_L : p == REP ? C_REP_L : shade(partyColor(p), 1.3); }

    /** Diverging map colour for a D-minus-R margin in points (positive = Democratic). */
    static Color marginColor(double m, double confidence) {
        double a = clamp(Math.abs(m) / 20.0, 0, 1);
        Color base = m >= 0 ? C_DEM : C_REP;
        Color lite = m >= 0 ? rgb(0x9cc2ff) : rgb(0xffb0b2);
        Color dark = m >= 0 ? rgb(0x0f3a8f) : rgb(0x8f1d22);
        Color c = a < 0.5 ? mix(lite, base, a * 2) : mix(base, dark, (a - 0.5) * 2);
        if (Math.abs(m) < 1.0) c = mix(rgb(0xb9a6d9), c, Math.abs(m));
        return mix(C_STATE, c, clamp(confidence, 0.15, 1));
    }

    // =====================================================================
    //  SOUND  -  every effect is synthesised in code
    // =====================================================================
    static final class Sfx implements Runnable {
        static final int RATE = 22050;
        static final int CLICK = 0, HOVER = 1, CHIME = 2, CHEER = 3, TURN = 4, CASH = 5, ALERT = 6, ERROR = 7, OPEN = 8,
                CALL = 9, GAVEL = 10, WHOOSH = 11, BELL = 12, STING = 13, COUNT = 14;

        static final class Voice {
            float[] d;
            double pos, rate;
            float vol;
        }

        final float[][] lib = new float[COUNT][];
        final ArrayList<Voice> voices = new ArrayList<Voice>();
        final Random rnd = new Random(11);
        volatile boolean running, enabled = true;
        volatile float volume = 0.6f;
        SourceDataLine line;

        Sfx() { build(); }

        void start() {
            try {
                AudioFormat fmt = new AudioFormat(RATE, 16, 1, true, false);
                SourceDataLine l = AudioSystem.getSourceDataLine(fmt);
                l.open(fmt, 4096);
                l.start();
                line = l;
                running = true;
                Thread t = new Thread(this, "bg2028-audio");
                t.setDaemon(true);
                t.start();
            } catch (Throwable t) {
                line = null;          // no audio device: the game simply runs silently
            }
        }

        void stop() {
            running = false;
            try { if (line != null) { line.stop(); line.close(); } } catch (Throwable t) { /* ignore */ }
        }

        void play(int id, double vol, double pitch) {
            if (!enabled || line == null || id < 0 || id >= COUNT || lib[id] == null) return;
            Voice v = new Voice();
            v.d = lib[id];
            v.rate = pitch;
            v.vol = (float) vol;
            synchronized (voices) {
                if (voices.size() > 16) voices.remove(0);
                voices.add(v);
            }
        }

        public void run() {
            final int n = 256;
            float[] mixBuf = new float[n];
            byte[] out = new byte[n * 2];
            while (running) {
                Arrays.fill(mixBuf, 0f);
                synchronized (voices) {
                    for (int vi = voices.size() - 1; vi >= 0; vi--) {
                        Voice v = voices.get(vi);
                        float[] d = v.d;
                        for (int i = 0; i < n; i++) {
                            int p = (int) v.pos;
                            if (p >= d.length - 1) break;
                            double fr = v.pos - p;
                            mixBuf[i] += (float) ((d[p] * (1 - fr) + d[p + 1] * fr) * v.vol);
                            v.pos += v.rate;
                        }
                        if ((int) v.pos >= d.length - 1) voices.remove(vi);
                    }
                }
                float vol = enabled ? volume : 0f;
                for (int i = 0; i < n; i++) {
                    double s = mixBuf[i] * vol;
                    s = s / (1 + Math.abs(s) * 0.55);
                    int v = (int) (s * 30000);
                    out[2 * i] = (byte) v;
                    out[2 * i + 1] = (byte) (v >> 8);
                }
                try { line.write(out, 0, out.length); } catch (Throwable t) { running = false; }
            }
        }

        static int len(double s) { return Math.max(1, (int) (s * RATE)); }

        float[] noise(double secs, double fc0, double fc1, double decay, double attack) {
            float[] o = new float[len(secs)];
            double y1 = 0, y2 = 0;
            for (int i = 0; i < o.length; i++) {
                double t = i / (double) RATE, u = t / secs;
                double fc = fc0 * Math.pow(fc1 / fc0, u);
                double a = 1 - Math.exp(-2 * Math.PI * fc / RATE);
                y1 += a * (rnd.nextDouble() * 2 - 1 - y1);
                y2 += a * (y1 - y2);
                double env = Math.min(1, t / attack) * Math.exp(-t * decay);
                o[i] = (float) (y2 * env);
            }
            return o;
        }

        static float[] tone(double secs, double f0, double f1, int wave, double decay, double attack) {
            float[] o = new float[len(secs)];
            double ph = 0;
            for (int i = 0; i < o.length; i++) {
                double t = i / (double) RATE, u = t / secs;
                double f = f0 * Math.pow(f1 / f0, u);
                ph += 2 * Math.PI * f / RATE;
                double s;
                if (wave == 0) s = Math.sin(ph);
                else if (wave == 1) s = 2 / Math.PI * Math.asin(Math.sin(ph));
                else if (wave == 2) s = Math.tanh(3 * Math.sin(ph)) * 0.8;
                else s = (ph % (2 * Math.PI)) < Math.PI ? 0.6 : -0.6;
                double env = Math.min(1, t / attack) * Math.exp(-t * decay);
                o[i] = (float) (s * env);
            }
            return o;
        }

        static float[] mixAt(float[] a, float[] b, double gb, double atSecs) {
            int off = len(atSecs);
            float[] o = new float[Math.max(a.length, b.length + off)];
            for (int i = 0; i < a.length; i++) o[i] = a[i];
            for (int i = 0; i < b.length; i++) o[i + off] += (float) (b[i] * gb);
            return o;
        }

        static float[] norm(float[] a, double peak) {
            double m = 1e-9;
            for (float v : a) m = Math.max(m, Math.abs(v));
            for (int i = 0; i < a.length; i++) a[i] = (float) (a[i] / m * peak);
            return a;
        }

        void build() {
            lib[CLICK] = norm(mixAt(tone(0.04, 1900, 1500, 1, 90, 0.0008), noise(0.02, 6000, 3000, 160, 0.0005), 0.25, 0), 0.3);
            lib[HOVER] = norm(tone(0.03, 2600, 2400, 0, 120, 0.002), 0.06);
            float[] ch = mixAt(tone(0.9, 1046, 1046, 0, 4, 0.003), tone(0.9, 1568, 1568, 0, 5, 0.003), 0.6, 0.09);
            lib[CHIME] = norm(ch, 0.3);
            float[] cheer = noise(2.2, 900, 2500, 1.4, 0.25);
            for (int i = 0; i < 14; i++) cheer = mixAt(cheer, noise(0.06, 3000, 5000, 40, 0.004), 0.5, 0.2 + rnd.nextDouble() * 1.6);
            lib[CHEER] = norm(cheer, 0.38);
            lib[TURN] = norm(mixAt(tone(0.16, 659, 659, 0, 9, 0.004), tone(0.32, 988, 988, 0, 6, 0.004), 0.8, 0.11), 0.26);
            float[] cash = new float[1];
            for (int i = 0; i < 3; i++) cash = mixAt(cash, tone(0.09, 2637 + i * 300, 2637 + i * 300, 1, 25, 0.002), 0.8, i * 0.06);
            lib[CASH] = norm(cash, 0.25);
            float[] al = new float[1];
            for (int i = 0; i < 2; i++) al = mixAt(al, tone(0.14, 880, 880, 1, 10, 0.003), 1, i * 0.18);
            lib[ALERT] = norm(al, 0.26);
            lib[ERROR] = norm(tone(0.22, 220, 190, 2, 9, 0.005), 0.28);
            lib[OPEN] = norm(mixAt(noise(0.25, 900, 4000, 9, 0.01), tone(0.2, 520, 780, 1, 12, 0.004), 0.4, 0), 0.2);
            float[] call = mixAt(tone(0.5, 523, 523, 1, 4, 0.004), tone(0.5, 659, 659, 1, 4, 0.004), 0.8, 0.12);
            call = mixAt(call, tone(0.8, 784, 784, 1, 3, 0.004), 0.8, 0.24);
            lib[CALL] = norm(call, 0.32);
            lib[GAVEL] = norm(mixAt(noise(0.3, 2500, 300, 18, 0.001), tone(0.25, 180, 120, 0, 14, 0.001), 0.8, 0), 0.4);
            lib[WHOOSH] = norm(noise(0.5, 400, 3000, 5, 0.15), 0.18);
            lib[BELL] = norm(mixAt(tone(1.4, 784, 784, 0, 2.5, 0.002), tone(1.4, 1976, 1976, 0, 3.5, 0.002), 0.35, 0), 0.3);
            float[] st = mixAt(tone(0.25, 392, 392, 2, 4, 0.01), tone(0.25, 494, 494, 2, 4, 0.01), 0.7, 0);
            st = mixAt(st, tone(0.7, 587, 587, 2, 2.5, 0.01), 0.9, 0.22);
            lib[STING] = norm(st, 0.3);
        }
    }

    static Sfx sfx;

    static void snd(int id) { if (sfx != null) sfx.play(id, 1, 1); }

    static void snd(int id, double vol, double pitch) { if (sfx != null) sfx.play(id, vol, pitch); }

    // =====================================================================
    //  2.  MAP AND STATE DATA  (generated from public data; see BRIEFING > SOURCES)
    // =====================================================================
    /** State outlines in Albers USA projection (us-atlas / Census cartographic boundaries), quarter-pixel encoded. */
    static final String[][] SHAPES = {
            {"AL", "#D9#;-_KPMSRLQ>P~#Cz#6=!#F*#6,!#F`#85TWRZ]cMXXTD]QZJ`Q]UUQYNdNQPWUVSX!#Dm#:=M[XYYUQSKZVVKQNUKRRP@U8SPLSQYLOLCELHQKNHLKJMKSKnKOOMILEQ!#C}#9K!#D(#6FJLKI"},
            {"AK", "#+p#>UQLRQOKXFNLTMMXQMSSQMNUKMPRINNRJRGM~#+X#>lRJVJSPQSNFRLVPMLURLLVPNQVLRUMNOUTNLVHLRSEVTPIQNRMLPKIZGSTHMORHGYKKOIJKOM~#*N#?YSGSPGV~#*D#?LRLVQNSLKNVMLMRNI~#*K#>>PKULKVLO~#):#?dSMRTHOOL~#)H#=UWQSKXNORSOPSPZHOKR@BNJ~#(o#?wYNWGSQOMIKRLWNNRRPRLQRHUWLOQFRJTHNESKL~#)c#;^r@bJWPL[PT]PSQSNSQSIVPNKJMJPPJGCSMRTNSTWSNIFPJSLLMFP@HNBB>@ATNQKQEYQ]OXJSJT@^AUPUM]DUMTRYLYC]SPRJSOSROVESSPUSQRKVNZRMVVPNSWPQMUPYKXQRS"
                    + "QMUSdNZTaGlZ!#-}#=SWPOLWRSIXLOXXSRSc[TYWESNNERNMK[EXURWTOYYWQYUNQomNSUOPUTOQUSMgTQRTMSUQYUXM[KXLLMQHDQKIALNTVP[DNJKNCO]JNMLO>LUQQKSLJKONLPJSNLKSOGKNLEIEMKCLNOLWQEKGIMHNRGPA?JEJLNQUT]jINMKGRMHKIMRJMMLNQKNOQ]STWHTMRJKLPFK@C3FPKROLBMVHUEPFLQKJQ;N:VJVQJTKKLENRMKNPKITGKLPRJITPRFSPH[JMKKSPKIRKMWKIQLJPJKUMJMUMQMLKTIPQFLPMYSOLXRKRSMVTTLTLOMINUDOKSPKMJOVJNPYJKQRHXNOMJNTMOKVJONMISKLQJUMWEGTIKU@QDMJ_DUTSLYQ@"
                    + "GXELNJXEMISMTJPKTPSFVPSHVQRLVHOSPNTDSOSIQNZQMTPTRQRMSEWPSKV@UORKPPRIMMWJNOSLNITSNOTKUIPIULKJWNMKRPRGNLTUSISNRLKOTLMNQDQNUKRRGJNBYJMGTJNSGJOI[LNPSLMMSKNMFMRQROTJPKHLQRSIRRQQSJKJSFMISJQKMNJTMRI]KVPRUPGWO]@XJbLLUQRPKXSPMIJSGk>TQOITG]FR=RNMKQHVIOJ<VIIQHJSMUQUKRMNJDJILSIIOL6^LKRKPILEUIG8MPNSFREPHMOIC@RLIOVIOJMKROKINQLOOIKLOLRMMMKPPKTOLJVONJQK_?TCRLTNTQTVSN]CXRVNUGN@KILNQLTPSIJHLSKNHREYOIKJMQSSGKDNFQ@HM"
                    + "IPKKFRPSKBGHIOL~#(U#@%UJPLSLTQMLRKVNSQMSFRJTCR~#(|#<8RHPSXUVLSSOSUUZTLSHMIUNJCALNIQLLOI~#(O#=7RLNRXXHKKJ~#'K#?}dVFOEKNL~#'2#?yeOOLSMRSJQPRHP>J~#&j#?zUKPJTPMSTPNSLNGPLN~#&]#?sZOPLRPLTKONMHN~#&U#?jTPQSJTMKROLI~#$s#>YSNWTRVLMHNMHLN~#03#>rNGRPRUJP~#/h#>RQKUOOSLQJM~#/e#>rOKTMRQJWOKLO~#/`#>bQHMNOJVNWWXSVWINORRMUROTSLOYQLRYFPQLIMNILQPSLNMJKOWYQLSTLNRSKOHIKGKQNLUJ@GQMOILTMM~#/L#>JQKSNRSMJMMQL]N^YKSIPOQKOL"
                    + "RMLO_MSIEOIMOJG~#/5#=xUUXLWYQTGIVVRVH]MPMIPIHGE;~#/3#>@QHVQSOYcSbBBGKQNOJLMPJLPLL~#/1#>EPKSRPVKPPKLL~#.z#>3NJRLMKSRUIVRWPQRLQSQTXJQFKQUMSLO?B~#,m#=tZ;RSHZPQGR~#,l#=fPGSLM]LM"},
            {"AZ", "#-t#7?_OPKTLQIMCDLMIT@LLRJNKWNYEPDRNOARMNIWHRH]LZEC?LMPIL=JIKDU>LLUMPLP@L?SEMKP@TJKHPB^KTPOQXPRWTTUPYA!#/;#30!#1B#3Q!#36#3l!#2H#9P!#0c#99!#/*#8?!#-i#7RRDWG"},
            {"AR", "#>b#5$!#@T#4{!#B;#4sPVTPQSNWEXEb!#B^#5CVXFQORTRIQMSEQOTRRSMPSHSRSORJKKPOSQSMUMILOKSOSROPLRQMWKPPRSPQRLRTSLQLNMPMXHNNSTSOSCVMKLOR[NQLONILUPRUOPQISNJLQS[LTPVJROTMOOKIPQTHSOTJLMRSRTNNTILLRPQURMTHPQRLQNLJQUTKTQTSQDQOSQQSNQSKPJKJRVTOQHRSUKUTMOKRQOSJQRRSNPIRQIWOSRURJPQLSPVHPMRUSMT!#?7#8T!#?6#8#KMMQJLLRJMJRNLIMOKMP!#>q#6/!#>b#5$"},
            {"CA", "#*s#5q_ZRVMRFJOFJMMJ~#*i#6>QOT[VZIPKIK=~#*&#5qSPUUGMMJ~#)z#5#[TSTTPQMTQLRBOEKPJLJ~#)a#5$`PNRSQNSDPHD~#)R#4xVMSUDK~#(t#,A!#+v#-4!#+0#0/!#.H#5(MZSZTURbNURQ[`DYBRLVGVPULQO^LPNZEYGPPSLTRRJ_QUZRQ[MUJRNS@O!#+r#7*PFLIKPO@RKO8F9!#+C#5dIJNSIODIOLSJJ<>JJQMK<EJIMD><GMLPCKGHFK6KNHGGUFNISGLIUBPHBEMJTBNJHLH=GIOEHCODKLI=IIKFP@RHMMSKQSUKUCMBKJLPELJHKDMNLGSAOCKJV8WOMdUSX]RPLIO@KIPLJKRJOIKMMJ^KNIIJHPLWRSJSRTKMOUKOH"
                    + "ELOF@KMLRMMWBNHQLMKLON@EAJ?B8PLTGO1W@Q5E9OHKKG@QJOCo*SGMLQGUHW>P2KJUGQE"},
            {"CO", "#3h#/i!#6:#0)!#9$#0=!#8i#4D!#6H#43!#4w#4%!#36#3l!#3h#/i"},
            {"CT", "#N2#-G|EOSRNOK!#OF#-.XpTeLQQTNRIOCULNNSDTJOJSMNHPNRKNESNKC`LNJUHROSIREWHF^AII!#N2#-G"},
            {"DE", "#L}#0+VEULZPK[JSOTSQNW^]TXR]VTPUZWSQRMYn!#MA#1A!#L}#0+"},
            {"DC", "#L1#1+SHZWHZNIHI"},
            {"FL", "#KA#?p^FQQCWKN~#Je#@*SFg>[RQSSQCVNMGQAXEROM~#JS#@/ZKMSGQNM~#J##>&XaWRQMSPISFJG=~#GM#;8WJMSJRNM~#G&#:(UYS[SR!#IN#:3NQV`YMO=KGOCSOPKcS^PSMTQQ`!#JI#:o!#Jj#;J!#K6#;xTZKTOXUcdm!#Kt#=HWcRd!#L(#>hNJLNKRKaOUSVKVPRTKV;NYFi:k[@QAFO?WJMJPMSASILLJOEQN<2KPKH9GKTB;F6BGNKKPPRKPKBKLSLRRPBNILNPIGTSZGRNSLF!#IL#=;RRSLZ9RMRHLIIORWHNPLLGDKPTLRWQTWLSOUIQQVLPNGD@OCJ=QOR^RONHMMU5O@MFKMPLKGRJHGLILOOH@NKTKDHMJDHNNLGLLBEKA?"
                    + "3BESFNOQHVSVNRHOMMIQ5dPKLNIVEPTUTQg?JV;[@HIPKQHAQJPWSWRLNHFDHMEH8C3FDM1Q!#Dy#;#SMLNSLPISMHHSDMKEIFEQC!#G&#:("},
            {"GA", "#F*#6,!#H,#5oOTGVNUKRNUQSWTTOVVVSSMUPSXVWRWZ[SVaWWTUZ[T[XMWSRSPPTSRRNPTfZOUWWP]RSNQSSVPWVNRX[MTSSPVSPSNVSVPNTLRLNNSROKUINOSROMTKTJORSRNPULVIPOQTNQPI_P_HWOYJTUPPMPUM`QQ9O<KNSKOO[SWObEQH?PM!#G4#:AKLKCKKI@IHNGPMP;MEIIMBT?MDZBFJQFB<LDJG!#F*#6,"},
            {"HI", "#5%#?5OLWHRNRITKOIIENJQKRMXS]XZQaW`_NYWOQX^YOSA[BVAP=[KTKYITJJBILGQ<LBMNMDHC~#4G#>%RGVMSQUY[JXQXXYRPUJV3VJKM@KNLQGKIF~#4G#>HZHRQNUDPMM~#4)#>&UMWPVYMSDQKEKJ~#3n#=oTINKgRRLQS^NUQGWHQ=J<O~#2v#=C]NXETNZaNUUSPKRONTVYJRKMJQIK<OE>OKKK~#1O#<tTF_GSQQMVOTPSVLWOWEWBMIIGLMK~#1(#=)[BRONXHRLWKKOL"},
            {"ID", "#/%#)iNFSINCKJ!#/Y#&n!#09#&z!#0&#'zXaOUTUNRQUJRTYHOYZPSZVRYWZPWRORUMTRTTSOVTLSSNQPSXRVMQQJ[LOPUKSLYMOOXJNNSSRLSOTSTMPPTMRJNIURTOTJQOVTPUZTLQKVO[DYZLVSPMUQUMUV]OUSQNQSSNXKOPWUTOSVLUURXOUMPRWNUXZQMNKTJSN]UQNTSRHULYU`NQTXL[SQNMJSJOKSPQKSRTYNSTTPSSR!#2J#.S!#0B#.4!#.(#-c!#.M#+vY=MIVJNKLNPLJNLJKPLKRLMIRGTLY?XLXCPGYFV?c8LIOHHKNKKNI="},
            {"IL", "#A)#0}RNOELKSH_IPBWDP@CBR?]K^MSJWNWHPBWHQ=MD?FLKPK?BOL!#C]#.`NaRWY]UeUY!#D-#1oLPNVQSKTPTTSOVTQMXSVNWIUM[JSOQH`MMMRNMKWTUJURMORJRQULTSQIOUXF]RWTTNSARJTKMKSMYV[MVIP2BKPD]NUSSKOIHLORSNRHKOKHEOJJLQJSNPKGBNKRMLLOJDIOMAEKRLNMLRKJOHK<>MIPEY;PKLHTGPKEIAKIXLPIJKCQLK@8?KGKNLJBDOIIHQJIENGOETB"},
            {"IN", "#Cu#/JSRTNNSZPXLcB!#Ew#/3!#F7#1qJUTTNWUPMSPTDQBYEKFQNST]JWGRMZJUHOKZO[HSNRJKJOHKOHIKMRSOITKORQNSJPQYIQOTKPOJJQMMKIKOLSFTMYKQLJ?GJOKSIIMSRSNTJPNHESIKMRRVMSIMIFUOKMRJMITLOLLQTIJISGPQQLQQV?OMTKQCUIPGKHQFJMOHJKNJSJMKPHRN!#Cu#/J"},
            {"IA", "#=9#-u!#?3#-s!#A(#-mP[VTSTH[RdRQS]WT^RSUQX[WSTNSRS`XQZMbGVN]GVGPKTAQBSL`[]N_GZN]@UKVRSOYLPHLNKMOJGKOLI!#?&#0q!#=h#0qPMLKIHRKNGRMNGLLNARMJONERPOLKMPDJMPLLLMRMKMBREIFQIIMOKLNOIJJNJQNIDRFNLHKPLKFIJOJUGOHRLNGSNPJLEJONHSNOHJKOIXO"},
            {"KS", "#8y#1>!#;[#1I!#>-#1L[ZUPQLUPNQTSKRPRRMNTJMNTKQMWLPOR[^RNOXRRQTUSSMPRTO!#>a#4P!#;p#4O!#8i#4D!#8y#1>"},
            {"KY", "#C(#4AKKPIZBSNYRcYUNQHHCQESKSQTJ^LPKJJLGXBUQQKLHQLUSYKPVTNPJLKQKUUSKTO`WRTSMQEXJRKSOSUQQTMOTSNOJUMMETNPKLMSOUJKOQLUSOVVSTOTSPLVKOCSDVOTIQDWLTGJBPKXMYS]EZMNJQKINPGJJYEZTWJRPPSXSXaYQUMXQVWWQRIULZRUTRLVNVEXKU_WPXVTbMRPUMPZ^SPMTURPSUSTYSPYWYO8m7aPUEWOWBTLZ;XHQDX!#E}#4J!#E3#4OKQMM!#D,#4YOM?OS[NS!#Bq#4oRDRNTURNSFLJQKRNKLS@MN~#Bm#4pKOLJQMSOPV"},
            {"LA", "#Cj#;AUYNYMUQCNIKG~#CE#;CZEMUSSKSNLHONN~#A)#;zVKUOZUKRORIP@HPM~#?7#8T!#A^#8MKVTRPGSPPVJTNXWPHWRTTMQKQQIVVTOPGLNSVSQMQRMPPTVQMRKONWMMDSPUWOOJSQHYMMLPVSGTOSIVJOMWWNEUOWMPILNRRRSNQQFSS_LNMJLRUWNRFPORSTMSTXIS!#B|#:JIeNYQQQVYXOSTUNTQTSSSOHQJWLPPRGOLRQUTORMOSSSVMPEUJPLXRJROQTQPSSNRHSQOSKOLSUNNRERUSRMMTHLLTOWMOMILONVEPTRPQHLRSLPSUUORRQVYMTRPLUVRKX^TLQRIQNRTOMSLMLUPTHMLJC^WBPELMMVKOHHELLL?M2gNLLMPLKGKLKVP"
                    + "IKKLTFNPTG[MNISNTJNPKFHEOLMCLJKSNQJTTQMOTSPNGHKOKLOITOKMPRJLLGSLILPMEGPQEFODRQHPPOKGPLNORDUMLJQRSTPMQQTUMLRQPFPBT8K!#@'#;m8P:RGSIDTKTCUIMLRJMIOGKLOJTJKFTJOKSJOKTGNIRJLJSML@KPMFJLPJLHLMPKHKMKRGMGHDEF!#?7#8T"},
            {"ME", "#Q/#*FOHVTMSJM~#Po#*RUFUXNSKQLNNILP~#Pj#*FPCROO[KO~#Ou#+uEKNGCGKHNC!#O'#)jSLNMVPQURPPGLGSKRRTNMJIJV?WFLKVDOJJNPLLMQCKLQAVEK6!#Od#'AZOT`YRXGSNRI[KNKQMZN`WVVUP!#Q,#(QRQLSTSMRRZTNONUSYOQLTWJRVVPTMUXXTQPHXO]aSPRXGcJRKHJTSRMSLONTJPTTLTHIKTLKM]KOLTMNNVKNNLKQQTMURQHRIOIHTILLNRMLHUQVSSLZORSSOTLQLDJMMKQJHKLQLGQKLJLWLMKSTULcPVSNLRSRNSHQNULPPTLPOHMNLTOIKQPSLWLLOSMRKNPTLLMWJTLIKPLVLOMTLOLTRUMRLNJSNSRSLUQQHSNd"
                    + "LW"},
            {"MD", "#M6#1tLPNHSOOU~#M,#1fNIRMTZHL~#J6#0]!#L}#0+!#MA#1A!#Mo#18O[Jm5XMSKNKPJUMONESLJNQIJPMRMHVLIKQFKQNXKKQLLKMVQQNSIQGIDATMLIOKRMQQQLPRYOJKGMIJOQKNPTMNNGRGTRPMLGMNLSLOPSLQQ=QTUOQLLKQJKPPVMNNIKKS=RJWNLNRILOPEGRNSTSFZKGP]LHKOSVKSSPIVDKTUWSPVSRJSQUMONTQQLVRRSb[ZNUKOQRRMRPNRVYQXGJMKMPOSEG@PIERXIMFBCZLMKGPHUFNLUINIVDDGKVLMHNNLHLGPJLQDFJBONGEHNKRMNMCQGIJOJWINMTLOPRMSHPIOFKQMKNORG`ENEbCZ!#J6#0]"},
            {"MA", "#Pa#-RYNSLPIUVLTIPCL~#P:#-XSNRGTIYUQKPV?TKTJK~#N1#,a!#OW#,DQJSNMKRKTOPJUKVNV_UQQKSQMWMNDWRQH[SSKQQTROPLRNRRUOSRWXOVLPTSWORYUS_O]DPMJEMRKDKNLQKNRMYPVVUZSYOVMTOGLNAWIPITMUFRGZEU`>LCQMHMPTLOPTLNORIQOYDTKAJNIIJNLELPLD4WON!#Nb#-<OSLPOK#YLL!#N1#,a"},
            {"MI", "#ES#+&ROQR_RLTIODD~#Ds#+ER@SOQ]KSJM~#D[#+sPMTPPVILNL~#DI#/A[BV9VBT:P9M=H<F?C2T?H9SIX:P:LHPJXKN@UNQITQSNX8TMPRJTSUJWRMPUMXQTQORDMNQHQMO]KXROV>M0XF[MVLNLGOLKLGY@JL]OQKeYWMVQVTPT]PWTTOWSVNWVMQ[cFLLSQVSSTPT_NZQgIVJOMTO`IQOSFQJVN_SVSQQLQRWSVCTLMLRDPSTIJNUNRI]JPLVL[TX^ZrScYfMTP_N^EYOJLMTHJMITNRRTLOMSPVMXFSLWP^LQOUD^LWQSMP!#Ew#/9OI!#DI#/A~#B3#)ASIfBWG^IAb<ZTPCUKOKL~#AL#*bhD^AgKYIWFWNTFc@WC^FhLTQPR@RPS?^O"
                    + "TKQKYKTM]QU[?UKH]UHaMaVY^VTQVaOTKXVSOQLRNRSMERMQRNSSQVFcBRP]KiM]GcKKVQ^MQRSTMXRYJRUWLQIYNQLUNSkHPNTWNVRLSUTWQPLSROJLJZNSPSUSQMTLQIMHQHM@PBSLQHKLQLIINMRPTLRRWMQ=A9JCPF]GNJQNSFLASMRMZLONQJQNSLPORMPPTILNLRMQGROQHNLMNKVFMPTLWIRLNNFMMLQPYHSHf?mQRJNJJS?LKKSJOLMSCMJQOPKKKRMMLHKLOLLIPLMSJMJELLPJLMQIKHPORGLMP:E!#Aa#*vG@JNNLKQML"},
            {"MN", "#<h#(D!#>0#(DO-UQUNWUXuNYRRVR`ORTgPRYdLOKVLWMQQ[N`VTOOSLONRRQXNYdULLIRL[NSROTRR_QNRUUSNOUcJf@ROT[ZMQQjLUQSUUQVKZQTM3c3Y=Z6i=f*pTWJKIRNUJN!#@3#+9KUJOMSLNCXK[JSN]VOWYOUIYP`LRR]Lab`]OXZYRXTR[d_YR]cN_SY!#?3#-s!#=9#-u!#=:#,*MJCJD=PK[GSHQ7L9KKKEK>Q;LH!#=##)j!#<l#)5QEM6SCG3"},
            {"MS", "#A^#8MQJIKQLVNNHRKNMLTLIOKUGLMNUKPLLTMOKLMOSJQSIKIVLOMHJTLTSSNMKKPMMOKZMKMMJSJIJTMPRRMMLVNQJILNMRLURPJJPKLQLTROJVKMJUNOSQOOJTLNHRJKCRMPTUKNMIONLRIPUROPMLCROQS[HOKJK!#Cz#6=SUTR!#C}#9K!#D3#:|GTLMLQMLDQILOQGM2YPKLMKQRSITMVEPIFPJJIOKEFMHMMPEU:!#AA#:RUKJFQKKJOLXNPLIGRLQTRPK@XKMMKPLLPLURQNOGYIGPQGTOUHOKWJHKRNQQVEKMOTGONIZKQQPGSOQLHMNJQNMLMQHKPKWRONHJUHMMMSJQLJVGGNPFTJNHKNNWJLSH"},
            {"MO", "#=h#0q!#?&#0q!#@n#0hRUSOPSUSPSVRJ]OYPWUYMTUVOU]ZRTSPSWg`S_MRS[UTRNUF^SYUNSJWRVNSEdNYQUcaVSTOLSQRRPSL^YOQZUOTRRLQPSW]NSKPMTTROTVYOSVSPLLKROUVUPK_SRLPMSRTKXLPJILPJ_MLNHKOMQSVNSQTNSIOPRTRLRGNNQVTQSITNV!#B.#5GY=YFPGMKJNNH!#@T#4{!#>b#5$!#>`#2?JONLKQIKMJLLOFLPCAOLRNQGSMPJTQPJLQNLSLJKPMINMRIN@AKOOFJJPKEGIAOLQNOJMSJOOI"},
            {"MT", "#09#&z!#1k#';!#4+#'_!#5x#'v!#84#(0!#7q#,0!#5c#+w!#2u#+S!#2n#,)KLNKJJPKJEKLMSKNOSKTQTMPCKFRMJ?PEIIRLVJKMPBIKPJTPSMQFDPILGQNOILFIIHROKIJNGSOPFKKPMKMOIHBQIMIQIKNRHEDCZHOMSJRIDJNOHTMOJLJUITPQLNJQNKJOJRKKLPKTPOFQORESKNIROTCMMHQFLNKPMKKJROHJKLJQJLILONGGDLEDHNKEDVOJETLMIPLJIOIF>!#09#&z"},
            {"NE", "#7a#.1!#9F#.=!#;g#.FQSlbUNTGRNZRhMTQPSTRYPPSSNVQORVRNRVYTNQRVOPRLXUZMPPTTTOURPOSUQMUUXLYQ]QSQLRRNRTQNZSQORLNPYTOLQP^RRPWLQPWLSXZNWTOQKOTMPORU^YWNSTTOXVR!#;[#1I!#8y#1>!#9$#0=!#7Q#03!#7a#.1"},
            {"NV", "#+v#-4!#.S#-m!#08#.1!#/-#3zE^INJJLGFNOMJNASN]SVJTN_QSKYR`N_NRIQRRKU!#+0#0/!#+v#-4"},
            {"NH", "#No#*/MJRIPHKMSNSGTSUNQI!#OX#+MP[SV[WPWYSLbHPISNTJOLSQSKPMT!#Nf#,THJMHNHTILKPIMMO@J7RLNFTGNGREMLSEH@PJ]HOMUJUELKQMNIHFS?KIQL"},
            {"NJ", "#M4#00U>TIZJOHg6=CLJIOLCLNIOLEOGROREHEWDNNTGQDVG!#N-#.QLnNRGSM]PRTPSMYQOKR]!#N:#/iGmJ[C]HjLTGQQ:ENNNIPNRKJGM@DJNNDKK"},
            {"NM", "#36#3l!#5L#4*!#8##4>!#7z#4mMO!#7V#9D!#4U#9+LXRVTS!#34#90Jy!#2H#9P!#36#3l"},
            {"NY", "#N)#.sQSKWGTQBWK~#IU#..c=TI[FSBZDGAJMNHJNPIKEt>qI_O`VXIlJYId7WORLQKI;IMQJRNPQSKNLJLJQOLHMP@SMSKOK^DTHPGk)^AXJUO!#MU#*LRWNWRQQdUVRUNURWLWO^W^OTRSObQPOJTMTW!#N.#,[RT!#N/#-DRR!#N>#.1UUA^VXJWPULPRSRGSOPLWKOLUPOL[QTKOLqE_<VLKVTRTOPMTSWHOMUMOQ1i(m/`AQEVOLIPKFR1!#MK#.=KIDQMLKOFFQMKCLNOKHKJQLIIK!#K$#./!#IY#.FK7"},
            {"NC", "#Mq#3qTOTXKOOKIJ~#Mm#4iPJn:L1H@X_Qq?X?^~#I%#4)!#J.#3x!#Jg#3o!#Lk#3K!#MZ#3;]kjt@?LOJBEBMFJIIPNLLQVVRNPT^hILIELOQTSQGODGLOZ[GSGKWTMQ<JVTUOHSIWJPLMJOJGPCNNLVPUYbYLWISS[GWNSQNYTaQNM:RJMNVLTQUZS`LTGRJYNULPORKT6QJLJGWMOKERMRUZEMLP:KUUTNbT[NSTIdB[@IQTaSWGXLPIROSVTNLJUPQUKVKQJbJNNILNPVKP[RUBc/RP;nHdLJEL6WCW8hF^JZKmKONMCO@SDV!#Jw#5Y!#J$#5fNDABIUMF!#Hh#5SGQNMLTLO;]NM8Z!#G.#5{O:TJPQVNTJNEXCVJeLYERORHSNPLULUO"
                    + "UFNHRNRQQHYGRRNUSPVIRGVIROQMTOQRVMU=WIUPLIQINJPI"},
            {"ND", "#84#(0!#:Z#(>!#<h#(DWlK[QiMY!#=##)j!#=$#*GRVMdSaSYSSRfN^!#:_#+D!#7x#+4!#84#(0"},
            {"OH", "#Ew#/9!#Fw#/*PQTPRNVS[RXUTKPKSSSNPRWVXRn>TP]Nk2!#I(#.Q!#I;#/qHRMSVXQ^IgP_LPPSLZRQMXKQ?hGTKOKJHVPVHOJYQVKQSVPVKNKVLMPKLKJLJULVPUJSU`NQHRN[BUJNMKFHGNI@FSHYHPLRIJDLIRLUGMHGFMIQEMF>FKNKLNGTDJJS!#Ew#/9"},
            {"OK", "#8##4>!#9?#4F!#;0#4M!#=3#4Q!#>a#4P!#>b#5$!#>q#6/!#>p#7wLQJIFNJIIQJKKHIOIHHMKW;NNKCSLNPSHPHKNQLOMQIOIROSLQJMJVIGJOMKGLRLIMMTJQMKJQMMMHIOOUHVNVINMIRLLHKSIOLSKOMMOJEMOLHLA[GJRGDMMHQKMMKRAKKVJPHGJNGR>FJQEMMDCCNWAJNSGNA?LMKP!#:D#4z!#7z#4m!#8##4>"},
            {"OR", "#*Y#(BSOQTNSRS`PY]R[JkSTaXRR_PdITR]OVUSOVTOUSORM[RbKTPQTUP]PRM^LcQVKTR_OWRYK!#/%#)iUbSPPSVSOVRU<gH`EXNWF[FRE`JRLWQULRRSSNRTTPNRRPPSHTQUEb!#.(#-c!#,p#-I!#+2#,}!#(t#,AKHMIP:RFUDPGMHMMOFZC[8NLTL]?Y;!#)k#)kS@b)T=VAPHNLRIPETKP;WWOLVOQR[L"},
            {"PA", "#I(#.Qf>PJSQb@Sh!#K$#./!#Ln#-iUSRUTMVSOSRPS[MQXXSOQRZMSUHWMZJWPPGZVYLYLOOWRYUORPR[UORTb[8iOVDTJUDNIRHY!#Kf#0C!#II#0j!#I(#.Q"},
            {"RI", "#Ov#-BTPS^KSJAMPS[KPORKOQ?SI~#On#-YNFPOQUMR~#Og#-xPHRVKO~#OR#-nPLMJRM!#OF#-/kGRZRNRYWTPRLSMIGJQXKORQRULQSXMY8Z"},
            {"SC", "#H,#5ogDPQdBRORJPQWM!#Ih#5JQXUI^]PZ!#Jw#5Y!#L$#6FCVAaDjOcFYNVFRLLKRMURPPQ=aKMMSSPOSHXDSMSMNGXLMLRMMLPNRUUMVCVNLIMLSWTLUFVLTPPLOHKKPKNNHKKQJFCPLGHHNKKPMLKNBGGOI9DNJLPKLNJKNKLQGCFCJIDGJ>GKHDCLGHGKFINKQHKHHJOGJMKPISLPIWHOJ"},
            {"SD", "#7p#,0!#7x#+4!#:_#+D!#=3#+KNYKVCWNSZb[TQT!#=9#-uFOOUTSOVKPPVTORYNTKPPWLROVIWOTUTSXNRRRLPMLJPHEPLHLOLHMKPNKENJLNKJM7QDLLPJWIP3=MK!#9F#.=!#7a#.1!#7p#,0"},
            {"TN", "#B^#5CQOPHUJMKHJPMWPRLJLNLUOPKMJPKNMSOQRQKXN!#Cz#4hPKKC`OOQ!#E-#4OQQSM!#Fg#4D!#GI#4>PM!#Hn#4+OMdLLXPTMURUINGUIbHQMLJOMQLOHULWHUKNPILLEWMVLMLPPVIXIOIRNRKPLVLOEY:RHTF[PYJTHPNMJTOe!#E)#64!#B8#6HPKVPQFQNRPRMJKRLMLKNNLSNQGLMNRLOOKSKROQUQIMKOKSNTSOLLKVKNKKQLLOJYMQKUMJLOLXMHF"},
            {"TX", "#7z#4m!#:D#4z!#:=#6wSNRQ^`WPPK^TPG[[QZYQTMaXWLTPVWTNSH^SSLQQMSQVZQLWWT^CSORRORYQOTUQRKUOSKRVLRQUUPPHVHOIUOQVQQTMQSTMQJUQLRWRQSTOUWTHTQRMOKULUOQMROPMVSVNNKRP[KPSdPSGVQUVUOSVTSUMTUXPTUTLOSUQPRTLTQRLTRQMSQ!#?9#9WYXVZQWLWQSVSNSRQRVNTTRQXSNR_KQRTLTPUJWOSKTOSJTSXJTOTSROWQULTQRIUJ[JSU]CODR(bGWLPWDUNQKUPKJ<SUGPHOJJMJQKWKPLKKVQQMSRSTQNSRPO[JPDZJNNVQQZF^DPJVQ7a3jKP!#=?#=4DZ2b>bDbBjJaM`Qh[|RdE%HNTMH3O;S9TA_5"
                    + "NLSHTLZ?TMRKSOZF]KNHAXJOMGJOLYQRNRDXHMNKAXURXJOUAdLOMJ<NMRRRWMNTQSXSBrGSLNSMPKLNFZVPYMIxRbQ[ROWcLSQZVQPRRNPXDPGSOUCJDCFNLL<NNRLLIQGLHIOMKOIILPHKHQCBIPCJLPLLQKIIL>MMMFFGOKLKMOP:GIRMRBKNPIHGLQMLLOJHGHK?JMHBFJHFJ=QMKNOIKLOLKML@MOMHMOK<IIMH?CKGHMNKHMPLNIMUMGKNLFJMKPNLLQEODK:OJJCLKXGMKQCQJZMONSLQK_LOLXRPKTIPMSHUMUBOKKHLEDHNILKH@KCFFBINFFJINFG@NCQDKCHGL;KNMHKNKJHIKOEHOLDELFCFIDJLNJ=DD5JLLPGGLHRF!#7V#9D!"
                    + "#7x#4mQO"},
            {"UT", "#08#.1!#2J#.S!#2;#/T!#3h#/i!#36#3l!#1B#3Q!#/;#30!#08#.1"},
            {"VT", "#MU#*L!#No#*/MRSUK`VXPUMQRSIYITOQBVNTV_KYQRLYPWJWPXLRThO_QQNURSJUPVQVVT!#N1#,aLJ!#M|#+uJGJQOTMNO=LKOJGAOARGLGPILIIHM;LMPGLG"},
            {"VA", "#MM#1oQKjFJ_NRJNMSLYM[QSKYQPLZLTOXLVJQNRNIIGO>Q=SANMTLQJMKLPTI~#H^#30SQJSTRPTVVTOXUWNXGRIZVfERKLLPKVS^CRNRTZEPMLONLVHIJSD[<RDNMW@MLVBMLQDWQUW[QUFX,YUSCSJORUGPPRIMNXAMMS@LDyhT<YOXTMZTRWNVRPRVPVSRSQ[IUPRKRLMLbTYWLVIRQOUUSORYRTNSQPMUNXYfWOTKRRZMSSPJRGLLQHEFLBDKMOQ_^XRXYUNQQXPKSRQRMRTP]DIMRXVESQRTMPUSMSRRSNWLNKUJJCIOKJJHSFKJSSQVMTSQOPJS[_VPTVMQMRQMISN]QULap!#KS#3b!#J.#3x!#I%#4)QL;ROQ!#GJ#4<YHVMdFRD]JO"
                    + "GYGNIh>g2"},
            {"WA", "#+X#'NVCPTMTSQIRKOOL~#+`#&YZBROOTRULPMMFTTPQSM_TITVOWLSLMOHJOPMLKQDLNKG~#+O#&2QMSPQUSQMHYJXVJROTJ[KNMJHMKKME~#/Y#&n!#/##)HSTP[KUPX!#-Z#)JESGL@OJLHS<MARLQBNINMJJN=SCLLQKOOIHJKOHIBOJL;U@NLL>FKJT4LCEB?NLKPKMJDLLKERHFJRY/QTJbRPQDQRSKNEYJLKGQKJP@RQNQ`MGHOLJLMPNVKPU9PEKGS+MMPJIGNGRDNHWBMISPTSWYWT]]`SUTXQQRWRULUXVPOXQOQLMKPLWOOQKQPSTKOWKOQYQQJONSKPHYSEKPJW=_IWWQWNRMCPKL^AXLUNVFWKPHRSN_KMPSMZKTQQF_PRJTKHR"
                    + "HKQMTOSSSMOKUJASMPJJTOULRROPUTRVJVDSURKVNN9UNKITJPHSLSGUNPJIEPELNLRS]HGQDSORQRMNKJJNGHPNJQKSOSUSPOFSPQMLIOIQMMLFPRLLMOIMKRLKMTJ!#-`#&C!#/Y#&n"},
            {"WV", "#H&#2@TP]IPCVLPMI?TKNIRHTITRRSNSRQSHSPNHKHSMMHTEVONHVHSTSOWJ`7SMQFLMRDNKRNN@U8MAHFQKVL!#II#0j!#J6#0]!#J>#11[DY=YPW?OLSPMQXS]NQKNLROQJUPTGTOWU[MPQLQPSVRS[ROJc&7RZK_QQF^QPLUNNIWOLKTK[EIFsIXCMIGGMMZQRH]QRG_PQLZCcKZUTHVPRRONQDYLJLPA[HKNSRRLS9YDHLUFWGPFIJOHHNJJLTKAMEGKNJEIKNKILQJKNDAQNNIQLLA"},
            {"WI", "#C|#+ePIVOKWKM~#A<#*BVJNTJPNN~#A4#*9VLRSRLPSGTIH~#A4#*OYFRQIRPRGSOL~#@B#*ZTQVNcHcDQQWJSJRPTLUUG]QTJXRPXIPKYWYQQRSMPRTPW_!#BH#+(eYQNWROLVNUSQMTRRNYRQTKTRQUNRRROVSQRLQSSNSMOQTK[RQTOSKRSK`TTTPMZCROUF`OYMSQRTOQJXHV=ZJQQOKV>PHWLOIRKTNOWLNO[KSKWQRLSPQIVGhLeR]GVKeRbKYInT`NVRTQXTTL_R[!#AS#.lICALGJKBLML;VCKJHJM<LLP@LIEBEL;@LCFJELFDBO=?R>LBRLN?UEOIGEHOPBTKSC[FRPQKTOSI!#@1#*_TPPIULTS"},
            {"WY", "#7p#,0!#7Q#03!#6:#0)!#4n#/u!#2;#/T!#2u#+S!#5c#+w!#7p#,0"},
    };

    /** Per-state data. Results are D/R/total votes; demographics are population shares (ACS 2012-16, county-aggregated)
     *  plus hand-assembled religion, union, veteran, gun and LGBTQ estimates (PRRI, BLS, VA, RAND, Gallup). */
    static final class StateDef {
        final String ab, name, region;
        int ev;                                     // electoral votes (the 2030 what-if map changes them)
        final double d24, r24, t24, d20, r20, t20, d16, r16, t16, m22;
        final double white, black, hisp, other, asian, nativeAm, u29, o65, nocol, nocolW, rural, income, cvap;
        final double wev, wca, jew, lds, arab, cuban, pr, union, vet, gun, lgbt, urban, close;
        final int countSpeed;
        final double cx, cy;

        StateDef(String ab, String name, String region, int ev, double[] r, double[] d, double[] h, double close, int spd, double cx, double cy) {
            this.ab = ab; this.name = name; this.region = region; this.ev = ev;
            d24 = r[0]; r24 = r[1]; t24 = r[2]; d20 = r[3]; r20 = r[4]; t20 = r[5]; d16 = r[6]; r16 = r[7]; t16 = r[8]; m22 = r[9];
            white = d[0]; black = d[1]; hisp = d[2]; other = d[3]; u29 = d[4]; o65 = d[5]; nocol = d[6]; nocolW = d[7]; rural = d[8]; income = d[9]; cvap = d[10];
            wev = h[0]; wca = h[1]; jew = h[2]; lds = h[3]; arab = h[4]; nativeAm = h[5]; asian = h[6]; cuban = h[7]; pr = h[8];
            union = h[9]; vet = h[10]; gun = h[11]; lgbt = h[12]; urban = h[13];
            this.close = close; countSpeed = spd; this.cx = cx; this.cy = cy;
        }
    }

    static final StateDef[] STATES = {
            new StateDef("AL", "Alabama", "S", 9, new double[]{772412, 1462616, 2256352, 849648, 1441168, 2323304, 718084, 1306925, 2078165, -33}, new double[]{66.19, 26.35, 4.00, 3.46, 39.22, 15.34, 75.86, 72.12, 40.68, 45708.31, 3639505.00}, new double[]{32, 3, 0.2, 0.7, 0.3, 0.7, 1.7, 0.05, 0.15, 13, 9, 55, 6, 15}, 20.0, 1, 671.6, 432.1),
            new StateDef("AK", "Alaska", "W", 3, new double[]{139690, 184407, 324097, 153405, 189892, 391346, 116454, 163387, 318608, -10}, new double[]{61.20, 3.30, 7.00, 28.50, 42.50, 10.40, 71.00, 65.00, 34.00, 74000.00, 530000.00}, new double[]{18, 8, 0.6, 4.5, 0.3, 15, 7.5, 0.03, 0.12, 30, 11, 64, 7, 20}, 25.0, 3, 101.8, 520.9),
            new StateDef("AZ", "Arizona", "W", 11, new double[]{1582860, 1770242, 3389319, 1672143, 1661686, 3387326, 936250, 1021154, 2062810, 3}, new double[]{56.15, 4.01, 30.54, 9.30, 40.79, 15.90, 71.96, 65.64, 9.95, 51693.66, 4613575.00}, new double[]{13, 10, 1.6, 5, 0.6, 4.5, 4, 0.02, 0.04, 9, 9, 46, 8, 30}, 21.0, 3, 208.0, 382.3),
            new StateDef("AR", "Arkansas", "S", 6, new double[]{396905, 759241, 1182676, 423932, 760647, 1219069, 378729, 677904, 1108615, -30}, new double[]{73.37, 15.42, 6.97, 4.23, 40.06, 15.66, 78.28, 76.01, 43.31, 43195.05, 2175330.00}, new double[]{33, 3, 0.2, 0.8, 0.3, 1, 2, 0.03, 0.06, 7, 9, 57, 6, 12}, 20.5, 1, 563.6, 390.9),
            new StateDef("CA", "California", "W", 54, new double[]{9276179, 6081697, 15862536, 11109764, 6005961, 17495906, 7362490, 3916209, 11954317, 18}, new double[]{38.38, 5.58, 38.56, 17.47, 41.44, 12.88, 68.42, 57.97, 4.97, 65366.51, 24582600.00}, new double[]{7, 9, 3, 1.9, 1.2, 0.9, 16.5, 0.02, 0.03, 25, 5, 28, 9, 40}, 23.0, 3, 82.1, 287.9),
            new StateDef("CO", "Colorado", "W", 10, new double[]{1728159, 1377441, 3190873, 1804352, 1364607, 3256953, 1212209, 1137455, 2564185, 17}, new double[]{68.97, 3.87, 21.13, 6.03, 40.47, 12.65, 61.35, 55.49, 13.45, 63927.38, 3824445.00}, new double[]{13, 12, 1.8, 2.1, 0.4, 1.1, 4, 0.02, 0.04, 12, 9, 45, 9, 30}, 21.0, 2, 331.6, 289.8),
            new StateDef("CT", "Connecticut", "NE", 7, new double[]{992053, 736918, 1758429, 1080680, 715291, 1824280, 884432, 668266, 1623542, 15}, new double[]{68.67, 9.69, 14.98, 6.65, 37.42, 15.47, 61.90, 57.01, 11.88, 72706.81, 2582880.00}, new double[]{5, 23, 3, 0.4, 0.6, 0.3, 5, 0.03, 0.55, 26, 6, 23, 8, 25}, 20.0, 2, 885.9, 190.5),
            new StateDef("DE", "Delaware", "NE", 3, new double[]{289758, 214351, 511697, 296268, 200603, 504010, 235581, 185103, 441535, 14}, new double[]{63.50, 21.14, 8.84, 6.52, 38.26, 16.46, 69.44, 66.97, 17.00, 61520.37, 689655.00}, new double[]{11, 14, 1.6, 0.4, 0.5, 0.4, 4.5, 0.04, 0.3, 16, 9, 30, 7, 25}, 20.0, 1, 853.3, 259.9),
            new StateDef("DC", "District of Columbia", "NE", 3, new double[]{294185, 21076, 325869, 317323, 18586, 344356, 260223, 11553, 280272, 75}, new double[]{35.77, 47.43, 10.49, 6.32, 41.60, 11.41, 44.64, 9.19, 0.00, 72935.00, 494090.00}, new double[]{3, 7, 4.5, 0.5, 1, 0.3, 5, 0.05, 0.15, 16, 5, 10, 14, 100}, 20.0, 1, 827.8, 267.3),
            new StateDef("FL", "Florida", "S", 30, new double[]{4683038, 6110125, 10893547, 5297045, 5668731, 11067456, 4485745, 4605515, 9386750, -19}, new double[]{55.58, 15.45, 24.11, 4.86, 35.82, 19.05, 72.03, 66.02, 8.67, 49050.78, 14195900.00}, new double[]{15, 12, 3.4, 0.6, 0.6, 0.3, 3.2, 0.45, 0.3, 10, 9, 38, 8, 25}, 20.0, 1, 767.3, 515.4),
            new StateDef("GA", "Georgia", "S", 16, new double[]{2548017, 2663117, 5250047, 2473633, 2461854, 4997716, 1837300, 2068623, 4029564, -1}, new double[]{54.12, 30.75, 9.18, 5.95, 41.63, 12.34, 70.58, 64.23, 24.25, 52480.77, 7063795.00}, new double[]{23, 5, 1.3, 0.7, 0.6, 0.3, 4.8, 0.06, 0.15, 8, 9, 50, 7, 25}, 19.0, 1, 736.0, 427.9),
            new StateDef("HI", "Hawaii", "W", 4, new double[]{313044, 193661, 516701, 366130, 196864, 574469, 266827, 128815, 428825, 27}, new double[]{22.37, 1.68, 10.03, 65.91, 38.51, 16.10, 68.60, 54.43, 8.12, 72569.94, 1006640.00}, new double[]{5, 6, 0.6, 5, 0.3, 0.3, 48, 0.02, 0.3, 40, 9, 9, 8, 35}, 24.0, 2, 317.0, 587.7),
            new StateDef("ID", "Idaho", "W", 4, new double[]{274972, 605246, 904812, 287021, 554118, 868105, 189677, 407199, 688235, -40}, new double[]{82.90, 0.60, 11.96, 4.54, 42.49, 14.27, 73.72, 72.14, 28.75, 49496.79, 1148900.00}, new double[]{18, 7, 0.3, 23, 0.2, 1.3, 2, 0.02, 0.03, 9, 9, 60, 6, 15}, 23.0, 1, 198.7, 146.5),
            new StateDef("IL", "Illinois", "MW", 19, new double[]{3062863, 2449079, 5592368, 3471915, 2446891, 6038850, 2977498, 2118179, 5374280, 13}, new double[]{62.22, 14.09, 16.62, 7.06, 39.88, 13.88, 66.96, 60.43, 11.33, 60574.05, 9003485.00}, new double[]{10, 17, 2.3, 0.4, 1.2, 0.3, 6.3, 0.02, 0.12, 22, 6, 28, 8, 35}, 21.0, 2, 615.0, 270.1),
            new StateDef("IN", "Indiana", "MW", 11, new double[]{1163603, 1720347, 2933700, 1242495, 1729852, 3033198, 1031953, 1556220, 2722029, -20}, new double[]{80.20, 9.10, 6.52, 4.18, 40.58, 14.27, 75.15, 73.71, 27.21, 51379.55, 4824565.00}, new double[]{23, 10, 0.5, 0.6, 0.5, 0.3, 2.8, 0.02, 0.05, 15, 8, 45, 7, 20}, 19.0, 1, 665.3, 269.1),
            new StateDef("IA", "Iowa", "MW", 6, new double[]{707278, 927019, 1656849, 759061, 897672, 1690871, 650790, 798923, 1542880, -18}, new double[]{86.96, 3.24, 5.56, 4.24, 39.91, 15.80, 72.39, 71.52, 35.33, 55214.44, 2295045.00}, new double[]{17, 18, 0.2, 0.7, 0.4, 0.5, 2.8, 0.02, 0.04, 13, 9, 45, 7, 15}, 22.0, 1, 539.9, 227.2),
            new StateDef("KS", "Kansas", "MW", 6, new double[]{544853, 758802, 1327591, 558669, 758100, 1349567, 414788, 656009, 1147143, -3}, new double[]{76.84, 5.63, 11.31, 6.22, 41.78, 14.34, 68.31, 65.96, 25.24, 55357.44, 2061765.00}, new double[]{21, 13, 0.6, 1, 0.5, 1.2, 3.4, 0.02, 0.04, 12, 9, 46, 7, 18}, 21.0, 1, 456.2, 309.1),
            new StateDef("KY", "Kentucky", "S", 8, new double[]{704043, 1337494, 2073309, 772474, 1326646, 2136768, 628834, 1202942, 1922218, -22}, new double[]{85.37, 7.84, 3.33, 3.47, 39.10, 14.80, 77.14, 75.96, 41.04, 45959.05, 3313780.00}, new double[]{33, 7, 0.3, 0.6, 0.4, 0.3, 1.8, 0.04, 0.06, 14, 9, 54, 6, 15}, 19.0, 1, 688.3, 321.0),
            new StateDef("LA", "Louisiana", "S", 8, new double[]{766870, 1208505, 2006975, 856034, 1255776, 2148062, 779535, 1178004, 2027731, -30}, new double[]{59.29, 31.94, 4.81, 3.95, 41.31, 13.64, 77.02, 70.95, 26.39, 46248.26, 3432025.00}, new double[]{19, 16, 0.3, 0.6, 0.4, 0.6, 1.9, 0.08, 0.2, 8, 8, 52, 7, 20}, 21.0, 1, 576.2, 478.3),
            new StateDef("ME", "Maine", "NE", 4, new double[]{435652, 377977, 831375, 430473, 359899, 813740, 354873, 334838, 741550, 13}, new double[]{93.74, 1.16, 1.49, 3.61, 33.84, 18.22, 70.68, 70.50, 61.11, 51456.53, 1051865.00}, new double[]{9, 18, 0.8, 0.7, 0.4, 0.6, 1.4, 0.02, 0.25, 17, 10, 45, 8, 10}, 20.0, 2, 919.0, 93.5),
            new StateDef("MD", "Maryland", "NE", 10, new double[]{1902577, 1035550, 3015650, 1985023, 976414, 3037030, 1497951, 873646, 2474543, 29}, new double[]{52.49, 29.17, 9.23, 9.11, 38.96, 13.76, 61.62, 53.88, 12.68, 78895.89, 4212250.00}, new double[]{10, 13, 4, 0.7, 1, 0.4, 7.2, 0.05, 0.2, 17, 8, 30, 8, 30}, 20.0, 2, 831.4, 263.3),
            new StateDef("MA", "Massachusetts", "NE", 11, new double[]{2126545, 1251308, 3453437, 2382202, 1167202, 3631402, 1964768, 1083069, 3231531, 29}, new double[]{73.72, 6.57, 10.85, 8.86, 38.14, 15.08, 58.84, 55.11, 7.92, 72391.21, 4887320.00}, new double[]{4, 26, 4, 0.4, 0.8, 0.3, 7.6, 0.04, 0.4, 21, 6, 15, 10, 30}, 20.0, 1, 896.9, 172.3),
            new StateDef("MI", "Michigan", "MW", 15, new double[]{2736533, 2816636, 5662504, 2804040, 2649852, 5539302, 2268193, 2279805, 4790917, 11}, new double[]{75.72, 13.74, 4.79, 5.75, 38.79, 15.42, 72.42, 70.76, 25.31, 51729.19, 7408635.00}, new double[]{16, 15, 1, 0.4, 3, 0.7, 3.8, 0.03, 0.08, 23, 8, 40, 7, 25}, 21.0, 2, 684.1, 184.8),
            new StateDef("MN", "Minnesota", "MW", 10, new double[]{1656979, 1519032, 3240916, 1717077, 1484065, 3277171, 1366676, 1322891, 2916404, 6}, new double[]{81.32, 5.60, 5.06, 8.02, 39.52, 14.30, 65.74, 63.37, 26.19, 64828.63, 3980480.00}, new double[]{12, 19, 1, 0.4, 1.5, 1.2, 5.6, 0.02, 0.05, 23, 8, 40, 8, 25}, 21.0, 2, 524.7, 131.9),
            new StateDef("MS", "Mississippi", "S", 6, new double[]{466668, 747744, 1228008, 539398, 756764, 1315178, 462001, 678457, 1162987, -26}, new double[]{57.21, 37.38, 2.90, 2.51, 41.34, 14.27, 78.80, 74.00, 50.20, 41085.15, 2217760.00}, new double[]{30, 4, 0.1, 0.4, 0.3, 0.6, 1.1, 0.04, 0.1, 9, 9, 55, 6, 10}, 20.0, 1, 618.1, 437.5),
            new StateDef("MO", "Missouri", "MW", 10, new double[]{1200599, 1751986, 2993596, 1253014, 1718736, 3025962, 1054889, 1585753, 2775098, -14}, new double[]{80.05, 11.50, 3.92, 4.54, 39.42, 15.35, 72.27, 69.71, 29.28, 50748.27, 4547870.00}, new double[]{23, 13, 0.8, 1, 0.6, 0.5, 2.3, 0.02, 0.06, 15, 9, 50, 7, 20}, 20.0, 1, 560.4, 311.6),
            new StateDef("MT", "Montana", "W", 4, new double[]{231906, 352079, 602963, 244786, 343602, 605750, 174521, 274120, 483574, -15}, new double[]{86.83, 0.39, 3.40, 9.38, 37.98, 16.70, 69.91, 68.72, 43.83, 48641.95, 789490.00}, new double[]{18, 13, 0.3, 4, 0.2, 6.5, 1, 0.02, 0.05, 17, 11, 66, 6, 10}, 22.0, 2, 286.9, 99.6),
            new StateDef("NE", "Nebraska", "MW", 5, new double[]{369995, 564816, 947159, 374583, 556846, 956379, 273858, 485819, 805638, -24}, new double[]{80.34, 4.57, 10.20, 4.90, 41.56, 14.43, 69.78, 67.38, 26.02, 55183.66, 1341015.00}, new double[]{16, 20, 0.4, 1.3, 0.6, 1, 2.8, 0.02, 0.04, 10, 9, 45, 6, 20}, 21.0, 1, 434.0, 238.7),
            new StateDef("NV", "Nevada", "W", 6, new double[]{705197, 751205, 1484840, 703486, 669890, 1405376, 537753, 511319, 1122990, 0}, new double[]{51.27, 8.22, 27.84, 12.67, 39.53, 14.12, 76.81, 72.22, 5.62, 53275.58, 1902290.00}, new double[]{9, 10, 2.5, 6, 0.6, 1.7, 10, 0.06, 0.08, 20, 9, 37, 9, 45}, 22.0, 3, 142.2, 251.8),
            new StateDef("NH", "New Hampshire", "NE", 4, new double[]{418496, 395578, 822153, 424921, 365654, 804430, 348521, 345789, 731553, 3}, new double[]{91.24, 1.17, 3.28, 4.31, 35.69, 15.85, 64.49, 64.50, 39.53, 69517.38, 1027635.00}, new double[]{7, 22, 0.8, 0.7, 0.4, 0.3, 3, 0.02, 0.2, 12, 9, 40, 9, 10}, 19.5, 1, 892.7, 140.2),
            new StateDef("NJ", "New Jersey", "NE", 14, new double[]{2220713, 1968215, 4272725, 2608335, 1883274, 4564234, 2021756, 1535513, 3674893, 3}, new double[]{56.69, 12.72, 19.29, 11.30, 37.83, 14.72, 62.62, 57.40, 5.21, 74686.16, 6087275.00}, new double[]{5, 21, 6, 0.3, 1.5, 0.3, 10.5, 0.07, 0.3, 26, 5, 15, 8, 30}, 20.0, 2, 861.6, 230.2),
            new StateDef("NM", "New Mexico", "W", 5, new double[]{478802, 423391, 923403, 501614, 401894, 923965, 380724, 315875, 783127, 12}, new double[]{38.68, 1.79, 47.82, 11.71, 40.76, 15.28, 73.66, 60.87, 22.30, 46241.30, 1464245.00}, new double[]{9, 9, 0.6, 2, 0.4, 10, 2, 0.01, 0.02, 9, 10, 46, 8, 25}, 21.0, 2, 310.5, 393.2),
            new StateDef("NY", "New York", "NE", 28, new double[]{4619195, 3578899, 8198094, 5244006, 3250230, 8616205, 4143874, 2640570, 7046175, 6}, new double[]{56.41, 14.37, 18.59, 10.63, 38.89, 14.69, 65.54, 56.88, 11.83, 64021.34, 13605855.00}, new double[]{5, 18, 9, 0.3, 1.5, 0.4, 9.5, 0.05, 0.45, 33, 5, 20, 8, 45}, 21.0, 2, 833.9, 171.7),
            new StateDef("NC", "North Carolina", "S", 16, new double[]{2715375, 2898423, 5679647, 2684292, 2758773, 5524801, 2162074, 2339603, 4629471, -3}, new double[]{63.99, 21.17, 8.90, 5.94, 39.34, 14.68, 70.80, 66.03, 33.10, 49484.63, 7203800.00}, new double[]{25, 6, 0.5, 0.8, 0.6, 1.3, 3.5, 0.05, 0.2, 5, 9, 45, 7, 25}, 19.5, 1, 800.9, 350.6),
            new StateDef("ND", "North Dakota", "MW", 3, new double[]{112327, 246505, 365059, 114902, 235595, 361819, 93526, 216133, 336968, -40}, new double[]{86.37, 1.97, 3.12, 8.54, 42.80, 14.23, 71.58, 70.70, 39.45, 59707.09, 555685.00}, new double[]{15, 22, 0.1, 0.6, 0.3, 5.5, 1.7, 0.02, 0.04, 9, 10, 55, 6, 10}, 21.0, 1, 429.1, 104.0),
            new StateDef("OH", "Ohio", "MW", 17, new double[]{2533699, 3180116, 5765017, 2679165, 3154834, 5922202, 2317001, 2771984, 5325395, -13}, new double[]{79.96, 12.10, 3.46, 4.47, 38.74, 15.50, 73.22, 71.27, 21.92, 51449.18, 8734140.00}, new double[]{18, 15, 1.3, 0.4, 0.8, 0.3, 2.7, 0.03, 0.12, 20, 9, 40, 7, 25}, 19.5, 1, 723.8, 252.9),
            new StateDef("OK", "Oklahoma", "S", 7, new double[]{499599, 1036213, 1566173, 503890, 1020280, 1560699, 419788, 947934, 1451056, -20}, new double[]{66.93, 7.14, 9.84, 16.09, 41.52, 14.50, 75.37, 72.68, 33.18, 48501.17, 2787660.00}, new double[]{32, 6, 0.2, 0.8, 0.4, 8, 2.6, 0.02, 0.05, 9, 9, 52, 6, 20}, 20.0, 1, 470.8, 376.1),
            new StateDef("OR", "Oregon", "W", 8, new double[]{1228410, 910702, 2207592, 1340383, 958448, 2374321, 934631, 742506, 1808575, 5}, new double[]{77.00, 1.77, 12.43, 8.80, 37.63, 15.93, 68.54, 66.44, 18.70, 54625.58, 2911325.00}, new double[]{12, 9, 1, 3, 0.4, 1.8, 5.2, 0.02, 0.04, 21, 9, 42, 10, 25}, 23.0, 2, 102.6, 134.0),
            new StateDef("PA", "Pennsylvania", "NE", 19, new double[]{3423042, 3543308, 7034206, 3459923, 3378263, 6925255, 2844705, 2912941, 5970107, 14}, new double[]{77.71, 10.59, 6.60, 5.10, 37.39, 16.69, 70.62, 68.16, 21.14, 56751.76, 9725830.00}, new double[]{13, 21, 2.3, 0.4, 0.8, 0.3, 4, 0.04, 0.45, 20, 8, 40, 7, 25}, 20.0, 3, 805.8, 225.6),
            new StateDef("RI", "Rhode Island", "NE", 4, new double[]{285156, 214406, 510659, 306210, 199837, 516383, 249902, 179421, 450121, 15}, new double[]{74.00, 5.44, 14.07, 6.50, 38.12, 15.79, 67.62, 65.23, 9.24, 58483.28, 780250.00}, new double[]{4, 26, 1.8, 0.4, 0.6, 0.7, 3.8, 0.03, 0.4, 25, 7, 14, 9, 25}, 20.0, 1, 903.9, 184.1),
            new StateDef("SC", "South Carolina", "S", 9, new double[]{1028452, 1483747, 2548140, 1091541, 1385103, 2513329, 849469, 1143611, 2084444, -20}, new double[]{63.86, 27.12, 5.34, 3.68, 39.02, 15.75, 73.49, 68.71, 33.03, 47359.63, 3620835.00}, new double[]{26, 5, 0.5, 0.6, 0.4, 0.5, 2, 0.05, 0.15, 4, 10, 50, 6, 15}, 19.0, 1, 779.3, 392.4),
            new StateDef("SD", "South Dakota", "MW", 3, new double[]{146859, 272081, 428922, 150471, 261043, 422609, 117442, 227701, 370047, -32}, new double[]{84.23, 1.63, 3.43, 10.71, 40.89, 15.33, 72.15, 70.66, 41.98, 52915.99, 618215.00}, new double[]{17, 18, 0.1, 0.6, 0.2, 9, 1.6, 0.02, 0.04, 5, 10, 55, 6, 10}, 21.0, 1, 429.7, 172.2),
            new StateDef("TN", "Tennessee", "S", 11, new double[]{1056265, 1966865, 3063942, 1143711, 1852475, 3053851, 867110, 1517402, 2484691, -33}, new double[]{74.52, 16.68, 5.00, 3.80, 39.04, 15.03, 74.42, 70.91, 33.15, 47969.45, 4876600.00}, new double[]{34, 4, 0.4, 0.7, 0.5, 0.4, 2.1, 0.03, 0.08, 9, 9, 51, 6, 20}, 20.0, 1, 673.6, 361.1),
            new StateDef("TX", "Texas", "S", 40, new double[]{4835250, 6393597, 11380105, 5259126, 5890347, 11317911, 3867816, 4681590, 8903237, -11}, new double[]{43.42, 11.63, 38.63, 6.32, 43.92, 11.49, 71.97, 61.67, 14.89, 55916.85, 17177620.00}, new double[]{16, 7, 0.6, 0.8, 0.8, 0.5, 5.6, 0.03, 0.04, 7, 8, 45, 7, 35}, 20.0, 2, 434.0, 469.2),
            new StateDef("UT", "Utah", "W", 6, new double[]{562566, 883818, 1487951, 560282, 865140, 1488289, 274188, 452086, 984957, -18}, new double[]{79.34, 1.03, 13.50, 6.14, 49.45, 10.02, 68.09, 65.48, 9.29, 63068.94, 1903500.00}, new double[]{5, 4, 0.2, 55, 0.3, 1.1, 3.4, 0.02, 0.04, 9, 7, 47, 7, 20}, 22.0, 2, 226.8, 268.7),
            new StateDef("VT", "Vermont", "NE", 3, new double[]{235791, 119395, 366389, 242820, 112704, 367428, 178179, 95053, 291413, 40}, new double[]{93.44, 1.13, 1.74, 3.70, 35.91, 16.96, 63.53, 63.40, 60.89, 56866.50, 493535.00}, new double[]{6, 18, 1, 0.7, 0.3, 0.4, 1.9, 0.02, 0.15, 18, 8, 50, 10, 10}, 19.0, 1, 873.3, 136.0),
            new StateDef("VA", "Virginia", "S", 13, new double[]{2335395, 2075085, 4482576, 2413568, 1962430, 4460524, 1916845, 1731156, 3844787, 4}, new double[]{63.12, 18.87, 8.73, 9.29, 39.35, 13.78, 62.99, 57.57, 23.96, 72743.83, 5998900.00}, new double[]{17, 8, 1.3, 0.8, 1.2, 0.4, 7.5, 0.05, 0.2, 8, 11, 36, 8, 25}, 19.0, 2, 801.4, 304.7),
            new StateDef("WA", "Washington", "W", 12, new double[]{2245849, 1530923, 3898835, 2369612, 1584651, 4087631, 1610524, 1129120, 2957942, 15}, new double[]{70.38, 3.49, 12.08, 14.05, 39.35, 14.04, 66.72, 64.41, 15.66, 64487.90, 5008030.00}, new double[]{11, 10, 1, 3, 0.6, 1.9, 11, 0.02, 0.04, 28, 9, 42, 10, 30}, 23.0, 3, 124.4, 59.0),
            new StateDef("WV", "West Virginia", "S", 4, new double[]{214309, 533556, 762390, 235984, 545382, 794652, 187457, 486198, 708226, -36}, new double[]{92.42, 3.38, 1.42, 2.78, 35.62, 17.77, 80.21, 80.33, 50.98, 43318.28, 1453210.00}, new double[]{31, 5, 0.1, 0.5, 0.3, 0.2, 0.8, 0.03, 0.08, 16, 10, 58, 6, 10}, 19.5, 1, 766.7, 284.7),
            new StateDef("WI", "Wisconsin", "MW", 10, new double[]{1668229, 1697626, 3415213, 1630673, 1610065, 3297352, 1380823, 1403694, 2937326, 2}, new double[]{82.15, 6.18, 6.45, 5.22, 38.61, 15.21, 71.46, 69.34, 29.61, 55672.93, 4313285.00}, new double[]{13, 22, 0.6, 0.4, 0.6, 1, 3.2, 0.02, 0.12, 13, 8, 44, 7, 20}, 21.0, 2, 594.7, 166.7),
            new StateDef("WY", "Wyoming", "W", 3, new double[]{69527, 192633, 266353, 73491, 193559, 276765, 55949, 174248, 248742, -45}, new double[]{84.36, 1.06, 9.67, 4.91, 40.59, 13.84, 73.91, 72.29, 34.93, 59863.70, 432375.00}, new double[]{17, 12, 0.2, 9, 0.2, 2.6, 1, 0.02, 0.04, 9, 10, 66, 6, 10}, 21.0, 1, 307.8, 195.1),
    };

    /** Maine and Nebraska award electoral votes by congressional district. {parent, ev, 2024 D margin, 2020 D margin, vote share of state} */
    static final String[] CD_NAMES = {"ME-1", "ME-2", "NE-1", "NE-2", "NE-3"};
    static final double[][] CD_DATA = {
            {19, 1, 24.0, 23.1, 0.52},
            {19, 1, -9.1, -7.4, 0.48},
            {27, 1, -12.6, -11.2, 0.33},
            {27, 1, 4.6, 6.5, 0.34},
            {27, 1, -51.0, -53.2, 0.33},
    };

    /**
     * 2026 midterm results by state: the average Democratic margin (points) in the state's Senate and governor races,
     * counting only Democrat-vs-Republican contests, as counted on 5 November 2026 (Fox News Decision Desk; some counts
     * incomplete). States with no comparable statewide race are left out.
     */
    static final String M26_DATA = "AK 0.3 AL -6.8 AR -13.6 AZ 12.8 CA 28.7 CO 25.2 CT 18.7 DE 27.1 FL -2.1 GA 13.4 HI 45.3 IA 7.5 ID -24.7 IL 23.0 KS 2.8 KY -12.1 LA -1.8 MA 29.5 MD 29.6 ME 11.9 MI 8.9 MN 11.5 MS 0.2 NC 19.6 NE -8.6 NH 5.1 NJ 27.0 NM 20.6 NV 7.1 NY 19.4 OH 6.4 OK -12.0 OR 11.6 PA 22.1 RI 37.5 SC 0.0 SD -27.0 TN -7.3 TX 9.6 VA 22.6 WI 8.0 WV -20.4 WY -30.5";

    static double[] m26;

    /** The 2026 statewide margin for a state, or NaN if it had no comparable race. */
    static double m26(int s) {
        if (m26 == null) {
            double[] v = new double[NS];
            Arrays.fill(v, Double.NaN);
            String[] t = M26_DATA.split(" ");
            for (int i = 0; i + 1 < t.length; i += 2) { int k = stateIdx(t[i]); if (k >= 0) v[k] = Double.parseDouble(t[i + 1]); }
            m26 = v;
        }
        return m26[s];
    }

    /** U.S. Senate seats up in 2028 (class III). {state, incumbent, party, 2022 D margin, note} */
    static final String[][] SENATE_2028 = {
            {"AL", "Katie Britt", "R", "-36", ""},
            {"AK", "Lisa Murkowski", "R", "-10", "independent streak"},
            {"AZ", "Mark Kelly", "D", "5", ""},
            {"AR", "John Boozman", "R", "-35", ""},
            {"CA", "Alex Padilla", "D", "22", ""},
            {"CO", "Michael Bennet", "D", "15", "lost the 2026 primary for governor"},
            {"CT", "Richard Blumenthal", "D", "15", ""},
            {"FL", "Ashley Moody", "R", "-3", "won the 2026 special election by 3"},
            {"GA", "Raphael Warnock", "D", "3", ""},
            {"HI", "Brian Schatz", "D", "46", ""},
            {"ID", "Mike Crapo", "R", "-32", ""},
            {"IL", "Tammy Duckworth", "D", "15", ""},
            {"IN", "Todd Young", "R", "-21", ""},
            {"IA", "Chuck Grassley", "R", "-12", "would be 95"},
            {"KS", "Jerry Moran", "R", "-23", ""},
            {"KY", "Rand Paul", "R", "-24", ""},
            {"LA", "John Kennedy", "R", "-43", ""},
            {"MD", "Chris Van Hollen", "D", "32", ""},
            {"MO", "Eric Schmitt", "R", "-13", ""},
            {"NV", "Catherine Cortez Masto", "D", "1", ""},
            {"NH", "Maggie Hassan", "D", "9", ""},
            {"NY", "Chuck Schumer", "D", "14", ""},
            {"NC", "Ted Budd", "R", "-3", ""},
            {"ND", "John Hoeven", "R", "-38", ""},
            {"OH", "Sherrod Brown", "D", "5", "won the 2026 special election by 5"},
            {"OK", "James Lankford", "R", "-32", ""},
            {"OR", "Ron Wyden", "D", "15", ""},
            {"PA", "John Fetterman", "D", "5", ""},
            {"SC", "Tim Scott", "R", "-25", ""},
            {"SD", "John Thune", "R", "-44", ""},
            {"UT", "Mike Lee", "R", "-11", ""},
            {"VT", "Peter Welch", "D", "40", ""},
            {"WA", "Patty Murray", "D", "15", ""},
            {"WI", "Ron Johnson", "R", "-1", "pledged two terms"},
    };

    /** Nielsen designated market areas (TV households in thousands, 2024-25, approximate) and the share of each market's
     *  households in each state. States not fully covered get a synthetic "rest of state" market at load. */
    static final String[] DMA_NAMES = {
            "New York",
            "Los Angeles",
            "Chicago",
            "Dallas-Ft. Worth",
            "Philadelphia",
            "Houston",
            "Atlanta",
            "Washington, DC",
            "Boston (Manchester NH)",
            "San Francisco Bay Area",
            "Phoenix",
            "Seattle-Tacoma",
            "Tampa-St. Petersburg",
            "Detroit",
            "Minneapolis-St. Paul",
            "Denver",
            "Orlando-Daytona Beach",
            "Miami-Ft. Lauderdale",
            "Cleveland-Akron",
            "Sacramento-Stockton",
            "Charlotte",
            "Raleigh-Durham",
            "Portland, OR",
            "St. Louis",
            "Indianapolis",
            "Nashville",
            "Pittsburgh",
            "Salt Lake City",
            "Baltimore",
            "San Diego",
            "San Antonio",
            "Columbus, OH",
            "Kansas City",
            "Hartford-New Haven",
            "Austin",
            "Cincinnati",
            "Greenville-Spartanburg-Asheville",
            "Milwaukee",
            "West Palm Beach",
            "Las Vegas",
            "Jacksonville",
            "Harrisburg-Lancaster-York",
            "Grand Rapids-Kalamazoo",
            "Norfolk-Portsmouth",
            "Birmingham",
            "Greensboro-Winston-Salem",
            "Oklahoma City",
            "Albuquerque-Santa Fe",
            "Louisville",
            "New Orleans",
            "Memphis",
            "Providence-New Bedford",
            "Ft. Myers-Naples",
            "Buffalo",
            "Fresno-Visalia",
            "Richmond-Petersburg",
            "Mobile-Pensacola",
            "Little Rock",
            "Wilkes-Barre-Scranton",
            "Knoxville",
            "Tulsa",
            "Albany-Schenectady",
            "Lexington",
            "Dayton",
            "Tucson",
            "Spokane",
            "Des Moines-Ames",
            "Green Bay-Appleton",
            "Honolulu",
            "Roanoke-Lynchburg",
            "Wichita-Hutchinson",
            "Flint-Saginaw-Bay City",
            "Omaha",
            "Charleston-Huntington, WV",
            "Columbia, SC",
            "Madison",
            "Portland-Auburn, ME",
            "Toledo",
            "Huntsville-Decatur",
            "Chattanooga",
            "Savannah",
            "Charleston, SC",
            "Jackson, MS",
            "Reno",
            "Boise",
            "Cedar Rapids-Waterloo-Dubuque",
            "Davenport-Rock Island-Moline",
            "Burlington-Plattsburgh",
            "Myrtle Beach-Florence",
            "Greenville-New Bern",
            "Youngstown",
            "Lansing",
            "Traverse City-Cadillac",
            "Augusta-Aiken",
            "Macon",
            "Johnstown-Altoona-State College",
            "Wilmington, NC",
            "Erie",
            "Bangor",
            "Anchorage",
            "Sioux Falls",
            "Fargo-Grand Forks",
            "Montana markets",
    };
    static final double[] DMA_HH = {7452, 5764, 3473, 3137, 3036, 2743, 2646, 2571, 2534, 2507, 2130, 2049, 2027, 1879, 1837, 1817, 1758, 1712, 1497, 1494, 1333, 1317, 1296, 1227, 1149, 1138, 1132, 1128, 1110, 1092, 1036, 1001, 996, 978, 953, 934, 917, 908, 862, 847, 757, 752, 734, 716, 711, 707, 702, 698, 683, 621, 614, 608, 601, 588, 586, 582, 568, 546, 545, 541, 525, 524, 506, 488, 482, 469, 435, 444, 431, 445, 428, 427, 433, 418, 416, 412, 409, 406, 404, 382, 370, 358, 330, 309, 330, 335, 297, 300, 300, 300, 260, 255, 250, 260, 235, 270, 210, 150, 135, 160, 270, 245, 420};
    static final String[] DMA_STATES = {
            "NY:0.72 NJ:0.25 CT:0.03",
            "CA:1.00",
            "IL:0.88 IN:0.12",
            "TX:1.00",
            "PA:0.66 NJ:0.27 DE:0.07",
            "TX:1.00",
            "GA:0.94 AL:0.06",
            "VA:0.48 MD:0.34 DC:0.13 WV:0.05",
            "MA:0.77 NH:0.21 VT:0.02",
            "CA:1.00",
            "AZ:1.00",
            "WA:1.00",
            "FL:1.00",
            "MI:1.00",
            "MN:0.88 WI:0.12",
            "CO:0.94 WY:0.03 NE:0.03",
            "FL:1.00",
            "FL:1.00",
            "OH:1.00",
            "CA:1.00",
            "NC:0.78 SC:0.22",
            "NC:1.00",
            "OR:0.85 WA:0.15",
            "MO:0.80 IL:0.20",
            "IN:1.00",
            "TN:0.95 KY:0.05",
            "PA:0.90 WV:0.07 OH:0.03",
            "UT:0.82 ID:0.08 NV:0.05 WY:0.05",
            "MD:1.00",
            "CA:1.00",
            "TX:1.00",
            "OH:1.00",
            "MO:0.62 KS:0.38",
            "CT:1.00",
            "TX:1.00",
            "OH:0.75 KY:0.20 IN:0.05",
            "SC:0.60 NC:0.37 GA:0.03",
            "WI:1.00",
            "FL:1.00",
            "NV:1.00",
            "FL:0.87 GA:0.13",
            "PA:1.00",
            "MI:1.00",
            "VA:0.90 NC:0.10",
            "AL:1.00",
            "NC:0.93 VA:0.07",
            "OK:1.00",
            "NM:0.90 CO:0.10",
            "KY:0.85 IN:0.15",
            "LA:1.00",
            "TN:0.60 MS:0.25 AR:0.15",
            "RI:0.55 MA:0.45",
            "FL:1.00",
            "NY:1.00",
            "CA:1.00",
            "VA:1.00",
            "AL:0.55 FL:0.45",
            "AR:1.00",
            "PA:1.00",
            "TN:1.00",
            "OK:1.00",
            "NY:0.95 MA:0.05",
            "KY:1.00",
            "OH:1.00",
            "AZ:1.00",
            "WA:0.65 ID:0.35",
            "IA:1.00",
            "WI:0.92 MI:0.08",
            "HI:1.00",
            "VA:0.97 WV:0.03",
            "KS:1.00",
            "MI:1.00",
            "NE:0.70 IA:0.30",
            "WV:0.70 KY:0.20 OH:0.10",
            "SC:1.00",
            "WI:1.00",
            "ME:0.88 NH:0.12",
            "OH:0.85 MI:0.15",
            "AL:0.90 TN:0.10",
            "TN:0.65 GA:0.30 AL:0.05",
            "GA:0.75 SC:0.25",
            "SC:1.00",
            "MS:1.00",
            "NV:0.85 CA:0.15",
            "ID:0.95 OR:0.05",
            "IA:1.00",
            "IA:0.50 IL:0.50",
            "VT:0.55 NY:0.45",
            "SC:0.80 NC:0.20",
            "NC:1.00",
            "OH:0.90 PA:0.10",
            "MI:1.00",
            "MI:1.00",
            "GA:0.60 SC:0.40",
            "GA:1.00",
            "PA:1.00",
            "NC:1.00",
            "PA:1.00",
            "ME:1.00",
            "AK:1.00",
            "SD:0.80 MN:0.10 IA:0.10",
            "ND:0.70 MN:0.30",
            "MT:1.00",
    };
    static final double[] STATE_TVHH = {2000, 260, 2850, 1200, 13600, 2350, 1420, 400, 330, 8800, 4000, 470, 720, 4900, 2650, 1300, 1150, 1800, 1780, 580, 2350, 2750, 4000, 2300, 1110, 2500, 450, 780, 1200, 550, 3500, 830, 7600, 4200, 330, 4800, 1550, 1700, 5200, 420, 2000, 360, 2800, 10700, 1100, 270, 3300, 3000, 720, 2450, 240};

    // =====================================================================
    //  3.  ATTRIBUTES, ISSUES, VOTER CELLS AND GROUPS
    // =====================================================================
    // ---- candidate attributes (0-100) -------------------------------------------------------------------
    static final int A_CHA = 0, A_POD = 1, A_SPE = 2, A_DEB = 3, A_INT = 4, A_DIS = 5, A_RET = 6, A_SMD = 7, A_BIG = 8,
            A_SELF = 9, A_VOL = 10, A_VIR = 11, A_AUT = 12, A_STA = 13, A_SCA = 14, A_EXE = 15, A_FPC = 16, A_XOV = 17,
            A_EST = 18, A_HOM = 19, A_BAS = 20, NA = 21;
    static final String[] ATTR = {"Charisma", "Long-form / podcasts", "Speeches", "Debating", "Intellect & policy depth",
            "Message discipline", "Retail politics", "Small-dollar fundraising", "Big-donor network", "Self-funding",
            "Volunteer energy", "Media virality", "Authenticity", "Stamina", "Scandal baggage", "Executive record",
            "Foreign-policy credibility", "Crossover appeal", "Establishment ties", "Home-state strength", "Base fire"};
    static final String[] ATTR_SHORT = {"CHA", "POD", "SPE", "DEB", "INT", "DIS", "RET", "SMD", "BIG", "SELF", "VOL", "VIR",
            "AUT", "STA", "SCA", "EXE", "FPC", "XOV", "EST", "HOM", "BASE"};
    static final String[] ATTR_DESC = {
            "Presence and likeability on stage and on screen.",
            "How well the candidate does in long, unscripted interviews and podcasts - where young men now get their news.",
            "Set-piece oratory: rallies, conventions, big moments.",
            "Head-to-head debating under pressure.",
            "Command of policy and quickness of mind.",
            "Staying on message; low gaffe risk.",
            "Handshakes, diners and town halls - the Iowa and New Hampshire arts.",
            "Ability to raise from millions of small online donors.",
            "Access to bundlers and high-dollar donors.",
            "Personal fortune that can be spent on the race.",
            "Ability to inspire volunteers to knock doors and make calls.",
            "Ability to go viral and dominate the attention economy.",
            "How genuine the candidate seems. High authenticity blunts flip-flop attacks.",
            "Ability to keep a punishing schedule without mistakes.",
            "Hidden risk of damaging revelations (higher is worse).",
            "Governing record: running a state, a department or a business.",
            "Credibility as a potential commander-in-chief.",
            "Appeal to the other party's voters and independents.",
            "Ties to party leaders, donors and endorsers.",
            "Strength at home - matters most if the home state is competitive.",
            "How strongly the party's activist base sees the candidate as a fighter."};

    // ---- the 18 issues; positions run from -1 (left pole) to +1 (right pole) -----------------------------
    static final int I_CHINA = 0, I_AI = 1, I_COL = 2, I_JOBS = 3, I_TRADE = 4, I_ISRAEL = 5, I_HOUSING = 6, I_IMMIG = 7,
            I_ABORT = 8, I_HEALTH = 9, I_SOCSEC = 10, I_CRIME = 11, I_CLIMATE = 12, I_DEMOC = 13, I_GUNS = 14, I_EDU = 15,
            I_UKRAINE = 16, I_TRANS = 17, NI = 18;
    static final String[] ISSUE = {"China", "AI & data centers", "Cost of living", "Jobs & AI disruption", "Tariffs & trade",
            "Israel, Gaza & Iran", "Housing", "Immigration", "Abortion", "Health care", "Social Security & Medicare",
            "Crime & policing", "Climate & energy", "Democracy & corruption", "Guns", "Education & student debt",
            "Ukraine & Russia", "Transgender issues"};
    static final String[][] ISSUE_POLES = {
            {"De-escalate and trade", "Confront and decouple"},
            {"Regulate AI, pause data centers", "Accelerate AI, build data centers"},
            {"Government action on prices", "Cut spending and deregulate"},
            {"Safety net, worker protections", "Markets and retraining only"},
            {"Free trade", "Tariffs and protection"},
            {"Condition or cut aid to Israel", "Unconditional support for Israel"},
            {"Rent limits and public housing", "Build, build, build: deregulate"},
            {"Path to citizenship", "Restrict and deport"},
            {"Abortion rights", "Restrict abortion"},
            {"Single-payer / public option", "Market-based, repeal mandates"},
            {"Expand benefits", "Trim and reform benefits"},
            {"Reform policing", "Tough on crime"},
            {"Clean-energy transition", "Drill and burn"},
            {"Limit presidential power, ethics reform", "Strong presidency, loyalty"},
            {"Gun safety laws", "Gun rights"},
            {"Debt relief, free college", "School choice, no bailouts"},
            {"Back Ukraine to win", "Make a deal with Russia"},
            {"Protect trans rights", "Restrict (sports, youth care)"}};
    /** Base national salience of each issue (share of the vote decision driven by it). */
    static final double[] ISSUE_SAL = {0.035, 0.045, 0.20, 0.07, 0.045, 0.025, 0.055, 0.09, 0.06, 0.08, 0.06, 0.045,
            0.04, 0.07, 0.03, 0.03, 0.02, 0.025};
    /** Where the typical Democratic and Republican voter stands (2026 polling). */
    static final double[] IDEAL_D = {0.10, -0.40, -0.70, -0.55, -0.30, -0.45, -0.30, -0.45, -0.80, -0.65, -0.70, -0.25, -0.70, -0.85, -0.70, -0.55, -0.60, -0.40};
    static final double[] IDEAL_R = {0.60, 0.05, 0.30, 0.25, 0.45, 0.60, 0.40, 0.75, 0.45, 0.40, -0.35, 0.75, 0.60, 0.35, 0.75, 0.60, 0.30, 0.80};

    // ---- voter blocs (16) x age (3) x sex (2) = 96 cells per state ---------------------------------------
    static final int B_WEV_NC = 0, B_WEV_C = 1, B_WCA_NC = 2, B_WCA_C = 3, B_WOT_NC = 4, B_WOT_C = 5, B_JEW = 6, B_LDS = 7,
            B_BLK = 8, B_HMX = 9, B_HCU = 10, B_HPR = 11, B_ASN = 12, B_NAT = 13, B_ARB = 14, B_OTH = 15, NB = 16;
    static final String[] BLOC = {"White evangelicals (no degree)", "White evangelicals (degree)", "White Catholics (no degree)",
            "White Catholics (degree)", "Other whites (no degree)", "Other whites (degree)", "Jewish", "Latter-day Saints",
            "Black", "Hispanic (Mexican & other)", "Cuban & Venezuelan", "Puerto Rican & Dominican", "Asian American",
            "Native American", "Arab & Muslim American", "Multiracial & other"};
    static final int AGE_YOUNG = 0, AGE_MID = 1, AGE_OLD = 2, NAGE = 3;
    static final String[] AGE = {"18-29", "30-64", "65+"};
    static final int NSEX = 2;                       // 0 men, 1 women
    static final int NCELL = NB * NAGE * NSEX;

    static int cell(int b, int a, int s) { return (b * NAGE + a) * NSEX + s; }

    static int cellBloc(int c) { return c / (NAGE * NSEX); }

    static int cellAge(int c) { return (c / NSEX) % NAGE; }

    static int cellSex(int c) { return c % NSEX; }

    static boolean white(int b) { return b <= B_WOT_C; }

    static boolean hispanic(int b) { return b == B_HMX || b == B_HCU || b == B_HPR; }

    static boolean college(int b) { return b == B_WEV_C || b == B_WCA_C || b == B_WOT_C; }

    /** National two-party Democratic share of each bloc in 2024 (Pew validated voters, AP VoteCast, exit polls). */
    static final double[] BLOC_D24 = {0.15, 0.25, 0.34, 0.47, 0.41, 0.63, 0.69, 0.32, 0.86, 0.55, 0.32, 0.63, 0.59, 0.57, 0.52, 0.57};
    /** Weight of the state's own partisan offset for each bloc (regional variation is largest among whites). */
    static final double[] BLOC_STATE_W = {1, 1, 1, 1, 1, 1, 0.45, 0.8, 0.3, 0.6, 0.6, 0.6, 0.6, 0.6, 0.5, 0.8};
    /** How far each bloc moves with the national environment and campaigns (persuadability). */
    static final double[] BLOC_ELAST = {0.55, 0.75, 1.0, 1.0, 1.0, 1.0, 0.8, 0.85, 0.65, 1.25, 0.95, 1.15, 1.1, 0.9, 1.05, 1.0};
    /** Base turnout of eligible citizens. */
    static final double[] BLOC_TURN = {0.70, 0.84, 0.70, 0.83, 0.60, 0.80, 0.82, 0.74, 0.60, 0.48, 0.60, 0.50, 0.58, 0.50, 0.55, 0.55};
    /** Gender gap multiplier (2024: very large among Black and Hispanic voters). */
    static final double[] BLOC_GENDER = {0.6, 0.6, 0.8, 0.8, 0.85, 0.85, 0.6, 0.6, 2.0, 1.5, 1.0, 1.4, 0.9, 0.9, 0.7, 1.0};
    /** Age profile of each bloc's eligible voters {18-29, 30-64, 65+}. */
    static final double[][] BLOC_AGE = {
            {0.13, 0.54, 0.33}, {0.12, 0.56, 0.32}, {0.15, 0.54, 0.31}, {0.14, 0.56, 0.30}, {0.18, 0.54, 0.28}, {0.20, 0.57, 0.23},
            {0.17, 0.50, 0.33}, {0.25, 0.55, 0.20}, {0.24, 0.58, 0.18}, {0.31, 0.56, 0.13}, {0.20, 0.55, 0.25}, {0.29, 0.56, 0.15},
            {0.23, 0.59, 0.18}, {0.26, 0.58, 0.16}, {0.31, 0.57, 0.12}, {0.38, 0.52, 0.10}};
    /** Age x sex logit adjustments (2024: young men swung right, young women stayed left). */
    static final double[][] AGESEX_ADJ = {{-0.12, 0.50}, {-0.22, 0.16}, {-0.28, 0.10}};
    static final double[] AGE_ELAST = {1.30, 1.0, 0.85};
    static final double[] AGE_TURN = {0.70, 1.0, 1.15};

    // ---- overlays: fractions of each cell with a trait ----------------------------------------------------
    static final int O_RURAL = 0, O_SUBURB = 1, O_URBAN = 2, O_UNION = 3, O_VET = 4, O_GUN = 5, O_LGBT = 6, O_STUDENT = 7,
            O_PARENT = 8, NO = 9;
    static final String[] OVERLAY = {"Rural", "Suburban", "Urban", "Union household", "Veteran household", "Gun owner",
            "LGBTQ", "College student", "Parent of minor"};
    /** Partisan lean of each overlay relative to its cell (logit). */
    static final double[] OV_LEAN = {-0.55, 0.05, 0.45, 0.35, -0.35, -0.55, 1.2, 0.45, -0.10};

    // ---- the 30 voter groups shown to the player ----------------------------------------------------------
    static final int NG = 30;
    static final String[] GROUP = {"Gen Z (18-29)", "Seniors (65+)", "Young men", "Young women", "Women", "Men",
            "White evangelicals", "White Catholics", "White, no degree", "White, college degree", "Black voters", "Black men",
            "Hispanic voters", "Hispanic men", "Cuban & Venezuelan", "Puerto Rican & Dominican", "Asian Americans",
            "Jewish voters", "Arab & Muslim Americans", "Latter-day Saints", "Native Americans", "Suburban voters",
            "Rural voters", "Urban voters", "Union households", "Veterans & families", "Gun owners", "LGBTQ voters",
            "College students", "Parents"};
    /** For overlay-defined groups, which overlay; otherwise -1. */
    static final int[] GROUP_OV = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            O_SUBURB, O_RURAL, O_URBAN, O_UNION, O_VET, O_GUN, O_LGBT, O_STUDENT, O_PARENT};

    /** Membership weight of cell c in group g (for non-overlay groups). */
    static double inGroup(int g, int c) {
        int b = cellBloc(c), a = cellAge(c), s = cellSex(c);
        switch (g) {
            case 0: return a == AGE_YOUNG ? 1 : 0;
            case 1: return a == AGE_OLD ? 1 : 0;
            case 2: return a == AGE_YOUNG && s == 0 ? 1 : 0;
            case 3: return a == AGE_YOUNG && s == 1 ? 1 : 0;
            case 4: return s == 1 ? 1 : 0;
            case 5: return s == 0 ? 1 : 0;
            case 6: return b == B_WEV_NC || b == B_WEV_C ? 1 : 0;
            case 7: return b == B_WCA_NC || b == B_WCA_C ? 1 : 0;
            case 8: return b == B_WEV_NC || b == B_WCA_NC || b == B_WOT_NC ? 1 : 0;
            case 9: return b == B_WEV_C || b == B_WCA_C || b == B_WOT_C ? 1 : 0;
            case 10: return b == B_BLK ? 1 : 0;
            case 11: return b == B_BLK && s == 0 ? 1 : 0;
            case 12: return hispanic(b) ? 1 : 0;
            case 13: return hispanic(b) && s == 0 ? 1 : 0;
            case 14: return b == B_HCU ? 1 : 0;
            case 15: return b == B_HPR ? 1 : 0;
            case 16: return b == B_ASN ? 1 : 0;
            case 17: return b == B_JEW ? 1 : 0;
            case 18: return b == B_ARB ? 1 : 0;
            case 19: return b == B_LDS ? 1 : 0;
            case 20: return b == B_NAT ? 1 : 0;
            default: return 0;
        }
    }

    // ---- issue opinion by bloc, age, sex and overlay ---------------------------------------------------------
    /** Bloc-specific shifts of the ideal point (added to the partisan blend). */
    static double blocIdealShift(int b, int i) {
        switch (b) {
            case B_WEV_NC: case B_WEV_C:
                return i == I_ISRAEL ? 0.40 : i == I_ABORT ? 0.40 : i == I_TRANS ? 0.30 : i == I_GUNS ? 0.20 : 0;
            case B_WCA_NC: case B_WCA_C:
                return i == I_ABORT ? 0.15 : 0;
            case B_JEW:
                return i == I_ISRAEL ? 0.70 : i == I_DEMOC ? -0.10 : i == I_GUNS ? -0.15 : 0;
            case B_LDS:
                return i == I_IMMIG ? -0.25 : i == I_DEMOC ? -0.30 : i == I_ABORT ? 0.20 : i == I_UKRAINE ? -0.20 : 0;
            case B_BLK:
                return i == I_ISRAEL ? -0.20 : i == I_TRANS ? 0.40 : i == I_ABORT ? 0.15 : i == I_IMMIG ? 0.20 : i == I_CRIME ? 0.05 : 0;
            case B_HMX:
                return i == I_IMMIG ? -0.15 : i == I_ABORT ? 0.15 : i == I_CRIME ? 0.10 : i == I_TRANS ? 0.15 : 0;
            case B_HCU:
                return i == I_CHINA ? 0.30 : i == I_ISRAEL ? 0.20 : i == I_HEALTH ? -0.10 : 0;
            case B_HPR:
                return i == I_HEALTH ? -0.10 : i == I_TRANS ? 0.10 : 0;
            case B_ASN:
                return i == I_CHINA ? -0.10 : i == I_EDU ? 0.15 : i == I_GUNS ? -0.20 : i == I_CRIME ? 0.15 : 0;
            case B_NAT:
                return i == I_CLIMATE ? -0.20 : i == I_GUNS ? 0.20 : 0;
            case B_ARB:
                return i == I_ISRAEL ? -1.10 : i == I_TRANS ? 0.50 : i == I_ABORT ? 0.30 : 0;
            default:
                return 0;
        }
    }

    static double blocSalMul(int b, int i) {
        switch (b) {
            case B_WEV_NC: case B_WEV_C: return i == I_ABORT ? 1.7 : i == I_TRANS ? 1.9 : i == I_ISRAEL ? 1.4 : 1;
            case B_WOT_C: case B_WCA_C: return i == I_DEMOC ? 1.5 : i == I_CLIMATE ? 1.3 : 1;
            case B_JEW: return i == I_ISRAEL ? 3.2 : i == I_DEMOC ? 1.3 : 1;
            case B_ARB: return i == I_ISRAEL ? 6.5 : 1;
            case B_BLK: return i == I_CRIME ? 1.2 : i == I_COL ? 1.1 : i == I_HEALTH ? 1.2 : 1;
            case B_HMX: case B_HPR: return i == I_IMMIG ? 1.3 : i == I_COL ? 1.2 : 1;
            case B_HCU: return i == I_CHINA ? 1.8 : i == I_IMMIG ? 1.2 : 1;
            case B_ASN: return i == I_CHINA ? 1.5 : i == I_EDU ? 1.3 : 1;
            case B_LDS: return i == I_DEMOC ? 1.3 : 1;
            default: return 1;
        }
    }

    static final double[][] AGE_IDEAL = {
            {-0.15, -0.10, -0.05, -0.05, -0.05, -0.45, -0.10, -0.10, -0.05, -0.05, 0.05, -0.10, -0.20, 0, -0.10, -0.30, 0.10, -0.25},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0.10, 0.05, 0.05, 0, 0, 0.25, 0.05, 0.05, 0, 0, -0.15, 0.10, 0.05, 0, 0, 0.05, -0.15, 0.10}};
    static final double[][] AGE_SAL = {
            {0.8, 1.3, 1.0, 1.3, 0.8, 2.5, 2.0, 0.9, 1.1, 0.8, 0.35, 0.8, 1.5, 1.0, 1.1, 2.0, 0.7, 1.2},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1.2, 0.9, 1.0, 0.7, 1.0, 1.1, 0.5, 1.1, 0.9, 1.4, 2.4, 1.2, 0.8, 1.2, 0.9, 0.4, 1.3, 0.9}};
    static final double[][] SEX_IDEAL = {
            {0.05, 0.10, 0.03, 0.03, 0.03, 0.05, 0.03, 0.05, 0.10, 0.05, 0, 0.05, 0.05, 0.05, 0.12, 0.03, 0.05, 0.08},
            {-0.05, -0.10, -0.03, -0.03, -0.03, -0.05, -0.03, -0.05, -0.12, -0.05, 0, -0.05, -0.05, -0.05, -0.12, -0.03, -0.05, -0.08}};
    static final double[][] SEX_SAL = {
            {1.2, 1.1, 0.95, 1.05, 1.1, 1.0, 0.95, 1.05, 0.7, 0.9, 0.95, 1.1, 0.9, 1.0, 1.2, 0.9, 1.1, 1.0},
            {0.85, 0.9, 1.05, 0.95, 0.9, 1.0, 1.05, 0.95, 1.4, 1.15, 1.05, 0.95, 1.1, 1.0, 0.85, 1.1, 0.9, 1.0}};
    /** Overlay ideal shifts and salience multipliers (indexed [overlay][issue]). */
    static final double[][] OV_IDEAL = new double[NO][NI];
    static final double[][] OV_SAL = new double[NO][NI];

    static {
        for (int o = 0; o < NO; o++) Arrays.fill(OV_SAL[o], 1);
        OV_IDEAL[O_RURAL][I_AI] = -0.25; OV_SAL[O_RURAL][I_AI] = 1.4; OV_SAL[O_RURAL][I_GUNS] = 1.4; OV_IDEAL[O_RURAL][I_CLIMATE] = 0.2;
        OV_SAL[O_URBAN][I_CRIME] = 1.3; OV_SAL[O_URBAN][I_HOUSING] = 1.5; OV_IDEAL[O_URBAN][I_HOUSING] = -0.1;
        OV_SAL[O_SUBURB][I_COL] = 1.1; OV_SAL[O_SUBURB][I_ABORT] = 1.1; OV_SAL[O_SUBURB][I_EDU] = 1.1;
        OV_SAL[O_UNION][I_JOBS] = 1.6; OV_SAL[O_UNION][I_TRADE] = 1.8; OV_IDEAL[O_UNION][I_TRADE] = 0.3; OV_IDEAL[O_UNION][I_JOBS] = -0.3; OV_IDEAL[O_UNION][I_AI] = -0.2;
        OV_SAL[O_VET][I_CHINA] = 1.5; OV_SAL[O_VET][I_UKRAINE] = 1.3; OV_IDEAL[O_VET][I_CHINA] = 0.15;
        OV_SAL[O_GUN][I_GUNS] = 3.0; OV_IDEAL[O_GUN][I_GUNS] = 0.40;
        OV_SAL[O_LGBT][I_TRANS] = 4.0; OV_IDEAL[O_LGBT][I_TRANS] = -0.60; OV_SAL[O_LGBT][I_DEMOC] = 1.3;
        OV_SAL[O_STUDENT][I_EDU] = 3.0; OV_SAL[O_STUDENT][I_ISRAEL] = 1.6; OV_IDEAL[O_STUDENT][I_ISRAEL] = -0.3; OV_SAL[O_STUDENT][I_HOUSING] = 1.5;
        OV_SAL[O_PARENT][I_EDU] = 1.4; OV_SAL[O_PARENT][I_COL] = 1.2; OV_SAL[O_PARENT][I_TRANS] = 1.3;
    }

    // =====================================================================
    //  4.  IDEOLOGIES (FACTIONS)
    // =====================================================================
    static final int F_DSOC = 0, F_PROG = 1, F_LIB = 2, F_ABUN = 3, F_MOD = 4, F_OUTD = 5, F_MAGA = 6, F_NATCON = 7,
            F_TECHR = 8, F_HAWK = 9, F_GOV = 10, F_RESTR = 11, F_MAHA = 12, F_GREEN = 13, F_LIBT = 14, F_FWD = 15, F_AMP = 16, NF = 17;

    static final class Faction {
        final String name, desc, impact;
        final int party;
        final double econ, cult;
        final double[] pos;

        Faction(String name, int party, double econ, double cult, double[] pos, String desc, String impact) {
            this.name = name; this.party = party; this.econ = econ; this.cult = cult; this.pos = pos; this.desc = desc; this.impact = impact;
        }
    }

    static final Faction[] FACTIONS = {
            new Faction("Democratic Socialist", DEM, -0.90, -0.85,
                    new double[]{-0.30, -0.70, -0.95, -0.90, 0.10, -0.80, -0.60, -0.70, -1.0, -1.0, -0.90, -0.70, -0.95, -0.90, -0.80, -0.95, -0.30, -0.95},
                    "Medicare for All, taxing billionaires, a Green New Deal and confronting 'the oligarchy'. Heir to the Sanders movement.",
                    "Electrifies Gen Z, renters and progressive activists; huge small-dollar and volunteer engine. Costs ground with older moderates, suburban independents and some Hispanic men."),
            new Faction("Progressive Populist", DEM, -0.65, -0.60,
                    new double[]{0.00, -0.60, -0.80, -0.75, 0.15, -0.50, -0.30, -0.50, -0.95, -0.75, -0.80, -0.35, -0.80, -0.90, -0.75, -0.75, -0.60, -0.75},
                    "Economic fighter: anti-corporate, pro-union, tough on monopolies and corruption, eager to take the fight to Trump.",
                    "Strong with union households, young voters and the activist base; plays well with non-college voters on economics, less so on culture."),
            new Faction("Mainstream Liberal", DEM, -0.50, -0.60,
                    new double[]{0.10, -0.30, -0.65, -0.55, -0.20, -0.10, -0.10, -0.45, -0.95, -0.60, -0.65, -0.20, -0.75, -0.85, -0.75, -0.60, -0.80, -0.65},
                    "The Democratic center of gravity since 2020: the Biden-Harris agenda, abortion rights, democracy and the ACA.",
                    "Solid with Black voters, college-educated women and older Democrats. Struggles to win back non-college and young men."),
            new Faction("Abundance Liberal", DEM, -0.30, -0.55,
                    new double[]{0.25, 0.15, -0.45, -0.30, -0.45, -0.10, 0.60, -0.35, -0.90, -0.45, -0.50, 0.00, -0.65, -0.80, -0.60, -0.40, -0.80, -0.45},
                    "Build more housing, energy and infrastructure; cut red tape; competent, pro-growth liberalism.",
                    "Appeals to college graduates, young renters and suburban independents. Housing and permitting messages travel; union voters are wary of the tech tilt."),
            new Faction("Pragmatic Moderate", DEM, -0.25, -0.20,
                    new double[]{0.35, 0.00, -0.50, -0.40, -0.05, 0.25, 0.25, -0.10, -0.75, -0.45, -0.60, 0.20, -0.40, -0.70, -0.40, -0.30, -0.70, -0.20},
                    "Win-the-middle Democrat: kitchen-table economics, faith and patriotism, distance from the left on crime, borders and culture.",
                    "Best with independents, rural and exurban whites, Catholics and swing-state suburbs. Activists and Gen Z are cooler; base turnout can sag."),
            new Faction("Outsider Centrist", DEM, -0.15, -0.15,
                    new double[]{0.30, 0.20, -0.40, -0.20, 0.00, 0.00, 0.30, 0.00, -0.70, -0.50, -0.50, 0.25, -0.30, -0.60, -0.30, -0.30, -0.50, 0.00},
                    "Business or celebrity outsider: anti-politician, plain-spoken, ideologically flexible.",
                    "Draws low-propensity and disaffected voters, especially young men; lacks the institutional support that wins primaries."),
            new Faction("MAGA Populist", REP, 0.30, 0.90,
                    new double[]{0.80, 0.40, 0.10, 0.20, 0.90, 0.60, 0.40, 1.0, 0.50, 0.50, -0.30, 0.90, 0.90, 0.80, 0.90, 0.80, 0.70, 0.95},
                    "Trump's movement in pure form: tariffs, mass deportation, America First, war on the 'deep state'.",
                    "Commands the Republican primary electorate and non-college whites; mobilises low-propensity men. Repels college suburbs and many independents."),
            new Faction("National Conservative", REP, 0.10, 0.85,
                    new double[]{0.85, 0.20, 0.00, 0.00, 0.85, 0.50, 0.20, 0.95, 0.75, 0.30, -0.40, 0.85, 0.75, 0.70, 0.85, 0.70, 0.60, 0.95},
                    "The 'New Right': pro-worker, anti-Big Tech and anti-China, socially traditional - MAGA with a policy agenda.",
                    "Strong with the MAGA base and working-class whites, can reach union households; weak with college women and suburban moderates."),
            new Faction("Tech Right", REP, 0.75, 0.60,
                    new double[]{0.70, 0.95, 0.60, 0.60, 0.50, 0.60, 0.80, 0.70, 0.40, 0.70, 0.30, 0.80, 0.80, 0.60, 0.80, 0.90, 0.50, 0.80},
                    "Accelerationist: AI and energy dominance, deregulation, merit, school choice - Silicon Valley's turn right.",
                    "Excites young men, entrepreneurs and podcast audiences; data-center and AI-job anxieties make rural voters and seniors uneasy."),
            new Faction("Reagan Conservative", REP, 0.70, 0.70,
                    new double[]{0.95, 0.50, 0.60, 0.50, 0.20, 0.95, 0.60, 0.70, 0.80, 0.60, 0.20, 0.80, 0.70, 0.30, 0.85, 0.85, -0.60, 0.85},
                    "Peace through strength, free markets, the traditional conservative coalition; hawkish on China, Russia and Iran.",
                    "Strong with older Republicans, evangelicals, veterans, Cuban Americans and donors; MAGA activists distrust 'neocons'."),
            new Faction("Governing Conservative", REP, 0.60, 0.50,
                    new double[]{0.70, 0.50, 0.50, 0.40, 0.30, 0.70, 0.60, 0.70, 0.60, 0.50, 0.00, 0.80, 0.60, 0.20, 0.80, 0.85, -0.20, 0.80},
                    "Results-first Republican governor: tax cuts, schools, order and competence, with less drama.",
                    "Wins back college suburbs and independents; the base finds it bloodless."),
            new Faction("Restrainer / Paleo", REP, 0.15, 0.95,
                    new double[]{0.40, -0.30, 0.00, 0.00, 0.90, -0.20, 0.20, 1.0, 0.80, 0.30, -0.50, 0.85, 0.80, 0.50, 0.95, 0.80, 0.95, 0.95},
                    "No more foreign wars, skeptical of aid to Israel and Ukraine, nationalist on trade and immigration, anti-establishment to the core.",
                    "Fires up online populists and young men; alarms hawks, Jewish and evangelical voters and party donors."),
            new Faction("MAHA Populist", REP, -0.05, 0.35,
                    new double[]{0.30, -0.30, -0.20, -0.10, 0.40, 0.60, 0.00, 0.60, -0.10, 0.00, -0.50, 0.40, 0.20, 0.20, 0.30, 0.30, 0.80, 0.60},
                    "Make America Healthy Again: anti-pharma, anti-processed food, distrust of institutions; crosses party lines.",
                    "Draws 'crunchy' independents, health-conscious women and conspiracy-curious young voters; scientists and doctors recoil."),
            new Faction("Green", GRN, -0.95, -0.90,
                    new double[]{-0.60, -0.80, -1.0, -1.0, -0.10, -1.0, -0.80, -0.90, -1.0, -1.0, -1.0, -0.90, -1.0, -0.80, -0.70, -1.0, 0.40, -1.0},
                    "Eco-socialist and anti-war: end arms to Israel, Medicare for All, a rapid climate transition.",
                    "Pulls protest votes from young progressives and Arab and Muslim Americans; a spoiler in close states."),
            new Faction("Libertarian", LBT, 0.90, -0.40,
                    new double[]{0.00, 0.90, 1.0, 0.90, -0.90, -0.30, 1.0, -0.30, 0.00, 0.90, 0.70, -0.40, 0.50, -0.50, 1.0, 0.90, 0.70, -0.20},
                    "Minimal government at home and abroad: free trade, free markets, gun rights and civil liberties.",
                    "Draws disaffected Republicans and young men; usually 1-2% but decisive in close races."),
            new Faction("Forward / Reform", FWD, -0.20, -0.30,
                    new double[]{0.20, -0.20, -0.40, -0.70, -0.20, 0.00, 0.40, -0.10, -0.50, -0.40, -0.40, 0.00, -0.30, -0.70, -0.30, -0.20, -0.40, -0.20},
                    "Fix the system: ranked-choice voting, open primaries, a universal basic income for the AI age.",
                    "Appeals to tech-minded independents, young voters and the politically homeless."),
            new Faction("America Party Centrist", AMP, 0.35, 0.10,
                    new double[]{0.50, 0.70, 0.40, 0.30, -0.20, 0.30, 0.80, 0.40, -0.30, 0.20, 0.00, 0.50, 0.00, -0.40, 0.30, 0.40, 0.20, 0.30},
                    "Billionaire-backed revolt against both parties: cut the deficit, build, AI dominance, end the uniparty.",
                    "Pulls from both sides - independents, young men and moderate suburbanites; scrambles the map."),
    };
    static final double[] GENERIC_D = FACTIONS[F_LIB].pos;
    static final double[] GENERIC_R = FACTIONS[F_MAGA].pos;

    // =====================================================================
    //  5.  CANDIDATES
    //  Ratings are the game's estimates from public records (elections won, money raised, debate and interview
    //  reviews, approval ratings) as of autumn 2026. They are judgements for play, not facts.
    // =====================================================================
    /** Appearance for the stylised portrait. */
    static final class Look implements Serializable {
        private static final long serialVersionUID = 1L;
        boolean female;
        int skin, hair, style, facial, glasses, suit, tie;
        double gray, faceW = 1, jaw = 0.5, age = 0.5, brow = 0.5, smile = 0.5;
        boolean earrings, lipstick;

        /** "F|M skin hair style gray facial glasses faceW jaw suit tie age smile [E][L]" */
        static Look parse(String s) {
            String[] t = s.trim().split("\\s+");
            Look l = new Look();
            l.female = t[0].equals("F");
            l.skin = Integer.parseInt(t[1]); l.hair = Integer.parseInt(t[2]); l.style = Integer.parseInt(t[3]);
            l.gray = Double.parseDouble(t[4]); l.facial = Integer.parseInt(t[5]); l.glasses = Integer.parseInt(t[6]);
            l.faceW = Double.parseDouble(t[7]); l.jaw = Double.parseDouble(t[8]); l.suit = Integer.parseInt(t[9]); l.tie = Integer.parseInt(t[10]);
            l.age = Double.parseDouble(t[11]); l.smile = Double.parseDouble(t[12]);
            for (int i = 13; i < t.length; i++) { if (t[i].contains("E")) l.earrings = true; if (t[i].contains("L")) l.lipstick = true; }
            return l;
        }
    }

    static final class Cand implements Serializable {
        private static final long serialVersionUID = 1L;
        String id, first, last, role, homeAb, bio, pros, cons, spouse;
        int party, born, faction, home;
        double econ, cult;
        int[] a = new int[NA];
        double nameId, poll, cash, list, selfMax;
        double[] plat = new double[NI];
        String[] record = new String[0];
        int[] recIssue = new int[0];
        double[] recCred = new double[0];
        int[] apG = new int[0];
        double[] apV = new double[0];
        Look look;
        boolean custom, fictional;

        String name() { return first + " " + last; }

        int age2028() { return 2028 - born - 1 + (born > 0 ? 1 : 0); }
    }

    static final List<Cand> ROSTER = new ArrayList<Cand>();

    static int stateIdx(String ab) {
        for (int i = 0; i < STATES.length; i++) if (STATES[i].ab.equals(ab)) return i;
        return -1;
    }

    /** Compact roster entry. attrs: CHA POD SPE DEB INT DIS RET SMD BIG SELF VOL VIR AUT STA SCA EXE FPC XOV EST HOM BASE */
    static Cand cand(String id, String first, String last, int party, String role, String home, int born, int faction,
                     String attrs, double nameId, double poll, double cash, double list, double selfMax, String look) {
        Cand c = new Cand();
        c.id = id; c.first = first; c.last = last; c.party = party; c.role = role; c.homeAb = home; c.born = born; c.faction = faction;
        String[] t = attrs.trim().split("\\s+");
        for (int i = 0; i < NA; i++) c.a[i] = Integer.parseInt(t[i]);
        c.nameId = nameId; c.poll = poll; c.cash = cash * 1e6; c.list = list * 1e6; c.selfMax = selfMax * 1e6;
        c.econ = FACTIONS[faction].econ; c.cult = FACTIONS[faction].cult;
        c.plat = FACTIONS[faction].pos.clone();
        c.look = Look.parse(look);
        ROSTER.add(c);
        return c;
    }

    static void bio(Cand c, String bio, String pros, String cons) { c.bio = bio; c.pros = pros; c.cons = cons; }

    /** Record items: "issue:credibility:text" with issue -1 for general. */
    static void rec(Cand c, String... items) {
        c.record = new String[items.length]; c.recIssue = new int[items.length]; c.recCred = new double[items.length];
        for (int i = 0; i < items.length; i++) {
            String[] t = items[i].split(":", 3);
            c.recIssue[i] = Integer.parseInt(t[0]); c.recCred[i] = Double.parseDouble(t[1]); c.record[i] = t[2];
        }
    }

    /** Group appeal: "group:value" pairs (logit shifts among that group). */
    static void appeal(Cand c, String s) {
        String[] t = s.trim().split("\\s+");
        c.apG = new int[t.length]; c.apV = new double[t.length];
        for (int i = 0; i < t.length; i++) { String[] p = t[i].split(":"); c.apG[i] = Integer.parseInt(p[0]); c.apV[i] = Double.parseDouble(p[1]); }
    }

    static void pos(Cand c, double econ, double cult, String issues) {
        c.econ = econ; c.cult = cult;
        if (issues == null) return;
        for (String p : issues.trim().split("\\s+")) { String[] kv = p.split("="); c.plat[Integer.parseInt(kv[0])] = Double.parseDouble(kv[1]); }
    }

    // groups: 0 GenZ 1 Seniors 2 YoungMen 3 YoungWomen 4 Women 5 Men 6 WhiteEvang 7 WhiteCath 8 WhiteNoDeg 9 WhiteDeg 10 Black
    // 11 BlackMen 12 Hispanic 13 HispMen 14 Cuban 15 PR 16 Asian 17 Jewish 18 ArabMuslim 19 LDS 20 Native 21 Suburban 22 Rural
    // 23 Urban 24 Union 25 Veterans 26 GunOwners 27 LGBTQ 28 Students 29 Parents
    static {
        Cand c;
        // ---------------------------------------------------------------- DEMOCRATS
        c = cand("harris", "Kamala", "Harris", DEM, "Former Vice President", "CA", 1964, F_LIB,
                "70 55 72 80 75 64 58 92 90 10 85 74 50 70 22 62 68 40 88 60 65", 0.97, 22, 8, 9.0, 0, "F 4 1 7 0.10 0 0 0.98 0.45 0 0 0.6 0.75 E");
        bio(c, "Vice President 2021-25, senator and attorney general of California. Lost the 2024 election to Donald Trump 49.8-48.3 after replacing Joe Biden at the top of the ticket and raising more than $1 billion in fifteen weeks.",
                "Near-universal name recognition, a proven national fundraising machine, deep loyalty among Black voters and older Democrats.",
                "A third presidential run after a loss; tied to the Biden years; critics see her as scripted in long-form settings.");
        rec(c, "8:0.6:Led the administration's abortion-rights campaign after Dobbs", "2:0.3:As California AG, won $18bn for homeowners in the foreclosure settlement",
                "12:0.4:Cast tie-breaking votes for the Inflation Reduction Act", "-1:0:Raised $1 billion in 15 weeks in 2024");
        appeal(c, "10:0.14 4:0.04 3:0.05 11:-0.04 2:-0.05 8:-0.04");
        c.spouse = "Doug Emhoff";

        c = cand("newsom", "Gavin", "Newsom", DEM, "Governor of California (to Jan 2027)", "CA", 1967, F_ABUN,
                "82 80 80 84 80 62 64 88 92 15 75 92 45 85 35 82 55 35 80 55 85", 0.85, 18, 10, 6.0, 0, "M 1 3 3 0.45 0 0 1.0 0.7 1 1 0.55 0.7");
        pos(c, -0.42, -0.58, "6=0.45 1=0.25");
        bio(c, "Two-term governor of California and former mayor of San Francisco. Became the loudest Democratic voice against Trump in 2025, winning the Prop 50 redistricting fight and trolling the White House online.",
                "Polished, combative and everywhere: a top debater with his own podcast, a giant email list and California's donor base.",
                "California's cost of living, homelessness and taxes are a ready-made attack; 'slick coastal elite' image; the French Laundry dinner.");
        rec(c, "13:0.5:Won the Prop 50 redistricting fight against Trump (2025)", "3:0.3:Signed a $20 minimum wage for fast-food workers",
                "12:0.4:Phase-out of new gas-car sales and a cap-and-trade extension", "6:-0.3:Presided over a worsening housing and homelessness crisis");
        appeal(c, "9:0.05 27:0.06 8:-0.06 22:-0.06");
        c.spouse = "Jennifer Siebel Newsom";

        c = cand("buttigieg", "Pete", "Buttigieg", DEM, "Former Secretary of Transportation", "MI", 1982, F_ABUN,
                "75 92 85 90 92 88 72 80 80 5 72 80 60 80 15 55 55 55 65 45 70", 0.80, 11, 5, 3.0, 0, "M 1 1 2 0.05 0 0 0.95 0.5 0 0 0.35 0.7");
        bio(c, "Transportation Secretary 2021-25 and mayor of South Bend, Indiana; Navy Reserve veteran of Afghanistan. Won the 2020 Iowa caucuses. Moved to Michigan.",
                "Perhaps the party's best communicator in hostile settings - Fox News, manosphere podcasts, debates. Disciplined and policy-fluent.",
                "Struggled with Black voters in 2020; thin executive record; the East Palestine derailment and air-traffic control troubles.");
        rec(c, "6:0.3:Oversaw rollout of the bipartisan infrastructure law", "3:0.3:Infrastructure jobs in all 50 states", "16:0.2:Navy Reserve intelligence officer in Afghanistan");
        appeal(c, "9:0.10 27:0.10 10:-0.12 2:0.04 1:0.03");
        c.spouse = "Chasten Buttigieg";

        c = cand("aoc", "Alexandria", "Ocasio-Cortez", DEM, "U.S. Representative, New York", "NY", 1989, F_DSOC,
                "92 78 88 78 78 74 80 98 30 0 95 95 85 85 15 20 35 25 30 60 95", 0.80, 12, 15, 5.0, 0, "F 2 1 8 0 0 0 0.92 0.4 0 0 0.25 0.7 EL");
        bio(c, "Democratic socialist congresswoman from the Bronx and Queens since 2019; author of the Green New Deal; co-leader with Bernie Sanders of the 'Fighting Oligarchy' tour.",
                "The most magnetic figure on the left: unmatched small-dollar fundraising, huge crowds and a volunteer army.",
                "Republicans' favourite foil; no executive experience; worries about electability among older and moderate voters.");
        rec(c, "12:0.5:Author of the Green New Deal resolution", "2:0.4:Led the 'Fighting Oligarchy' tour with Bernie Sanders", "-1:0:Record small-dollar fundraising for a House member");
        appeal(c, "0:0.22 15:0.14 12:0.05 3:0.08 1:-0.08 8:-0.06 6:-0.05 17:-0.06");

        c = cand("shapiro", "Josh", "Shapiro", DEM, "Governor of Pennsylvania", "PA", 1973, F_MOD,
                "78 62 88 78 82 85 75 72 88 5 70 60 60 80 25 85 50 82 75 90 55", 0.50, 4.5, 5, 1.5, 0, "M 1 1 4 0.30 0 0 1.02 0.55 0 0 0.45 0.8");
        pos(c, -0.28, -0.32, "15=0.0 5=0.35");
        bio(c, "Governor of Pennsylvania since 2023; re-elected in November 2026 by 22 points (60-38 over Stacy Garrity) after first winning by nearly 15. Previously state attorney general.",
                "High approval in the ultimate swing state, a 'get stuff done' record and real crossover appeal with independents and Republicans.",
                "Pro-Israel record is a liability with young progressives and Arab Americans; support for school vouchers angers teachers' unions.");
        rec(c, "-1:0:Reopened I-95 in Philadelphia 12 days after its collapse", "6:0.3:Cut permitting and licensing backlogs", "11:0.3:As AG, exposed clergy abuse cover-ups",
                "13:0.2:Defended Pennsylvania's 2020 election in court");
        appeal(c, "17:0.15 21:0.05 24:0.05 7:0.04 18:-0.25 0:-0.05");
        c.spouse = "Lori Shapiro";

        c = cand("pritzker", "JB", "Pritzker", DEM, "Governor of Illinois", "IL", 1965, F_PROG,
                "62 55 78 65 75 70 55 55 80 100 60 70 55 65 30 80 40 40 75 70 85", 0.50, 3, 3, 0.8, 2500, "M 1 2 4 0.25 0 0 1.30 0.35 1 0 0.5 0.6");
        pos(c, -0.58, -0.62, null);
        bio(c, "Billionaire heir to the Hyatt fortune, governor of Illinois since 2019; won a third term in November 2026 (60-37 over Darren Bailey). Became a national figure fighting the 2025 National Guard and immigration deployments in Chicago.",
                "Can self-fund a whole campaign; combative anti-Trump record; a progressive governing record with balanced budgets.",
                "Easy to paint as an out-of-touch billionaire; average charisma on camera.");
        rec(c, "3:0.4:Signed a $15 minimum wage and paid leave for all workers", "13:0.4:Fought the federal troop deployment to Chicago", "8:0.4:Made Illinois an abortion-access haven");
        appeal(c, "17:0.08 24:0.05 22:-0.04");

        c = cand("whitmer", "Gretchen", "Whitmer", DEM, "Governor of Michigan (to Jan 2027)", "MI", 1971, F_MOD,
                "72 60 75 72 72 70 78 70 78 5 70 55 65 75 15 82 40 65 75 80 55", 0.60, 2, 4, 1.5, 0, "F 1 2 7 0.05 0 0 0.98 0.45 0 0 0.4 0.75 E");
        pos(c, -0.35, -0.45, "8=-0.9");
        bio(c, "Two-term governor of Michigan, term-limited in January 2027. 'Fix the damn roads.' Repealed right-to-work and won an abortion-rights amendment.",
                "Midwestern swing-state appeal, strong with suburban women and union households.",
                "Cosy appearances with Trump in 2025 annoyed the base; has hinted she may not run.");
        rec(c, "3:0.5:Repealed Michigan's right-to-work law", "8:0.5:Championed the Prop 3 abortion-rights amendment", "-1:0:Rebuilt thousands of miles of Michigan roads");
        appeal(c, "4:0.05 21:0.05 24:0.06");

        c = cand("beshear", "Andy", "Beshear", DEM, "Governor of Kentucky (to Dec 2027)", "KY", 1977, F_MOD,
                "70 65 72 70 75 88 82 55 65 5 55 45 80 80 10 82 35 90 65 90 45", 0.35, 2, 2, 0.8, 0, "M 1 2 2 0.05 0 0 1.0 0.5 0 0 0.35 0.75");
        pos(c, -0.25, -0.18, "17=-0.4");
        bio(c, "Twice elected governor of deep-red Kentucky (2019, 2023); chair of the Democratic Governors Association. A former attorney general and Sunday-school teacher who talks easily about faith.",
                "Proven to win Trump voters; scandal-free, disciplined, comfortable with rural and religious voters.",
                "Little known outside Kentucky; may be too mild for a base that wants a fighter; Kentucky's electoral votes are out of reach anyway.");
        rec(c, "3:0.4:Record economic development and job growth in Kentucky", "-1:0:Led recovery from the 2021-22 tornadoes and floods", "17:0.2:Vetoed an anti-trans bill");
        appeal(c, "22:0.12 6:0.08 8:0.08 7:0.04 0:-0.04");
        c.spouse = "Britainy Beshear";

        c = cand("moore", "Wes", "Moore", DEM, "Governor of Maryland", "MD", 1978, F_LIB,
                "88 70 90 72 78 72 75 70 82 10 75 65 60 80 30 65 55 60 75 80 60", 0.35, 1.5, 3, 1.0, 0, "M 5 0 1 0.05 0 0 1.0 0.6 0 0 0.35 0.8");
        pos(c, -0.42, -0.40, null);
        bio(c, "Governor of Maryland since 2023, Army combat veteran of Afghanistan, Rhodes Scholar, author and former CEO of an anti-poverty foundation. Re-elected in November 2026 (63-33 over Dan Cox). Says he is not running.",
                "Charismatic orator with a compelling biography; appeals across racial lines and to veterans.",
                "Short record; questions over a Bronze Star claim; Maryland budget troubles.");
        rec(c, "2:0.3:Signed the ENOUGH Act on child poverty", "16:0.3:Army paratrooper and captain in Afghanistan", "-1:0:Rebuilding the Key Bridge");
        appeal(c, "10:0.12 25:0.08 11:0.05");
        c.spouse = "Dawn Flythe Moore";

        c = cand("booker", "Cory", "Booker", DEM, "U.S. Senator, New Jersey", "NJ", 1969, F_LIB,
                "80 65 90 75 80 70 75 75 75 5 70 70 55 98 10 55 50 40 70 65 70", 0.65, 3.5, 6, 2.5, 0, "M 5 0 0 0 0 0 1.0 0.55 0 1 0.45 0.8");
        bio(c, "Senator from New Jersey since 2013 and former mayor of Newark. Set the Senate record with a 25-hour speech against the Trump administration in 2025; re-elected in November 2026 (62-35). Ran for president in 2020.",
                "A tireless, inspiring speaker with deep ties to Black communities and the Senate.",
                "Sometimes seen as more performance than substance; 2020 campaign never caught fire.");
        rec(c, "13:0.3:Record 25-hour Senate speech against the administration", "11:0.3:Co-architect of the First Step Act on criminal justice", "-1:0:Mayor of Newark 2006-13");
        appeal(c, "10:0.10 11:0.04");

        c = cand("khanna", "Ro", "Khanna", DEM, "U.S. Representative, California", "CA", 1976, F_PROG,
                "60 80 65 72 85 70 65 70 60 15 60 65 65 75 10 15 45 40 25 40 70", 0.30, 1, 10, 1.2, 0, "M 3 0 2 0.10 0 0 0.95 0.45 0 0 0.4 0.7");
        pos(c, -0.62, -0.50, "4=0.35 1=0.0 0=0.45");
        bio(c, "Silicon Valley congressman since 2017 and co-chair of Bernie Sanders' 2020 campaign. Co-led the bipartisan push with Thomas Massie to release the Epstein files. Says he will decide now that the midterms are over.",
                "A progressive who speaks tech; tireless media presence on every platform, including right-leaning podcasts.",
                "Low name recognition; no executive record; a House member has rarely won the nomination.");
        rec(c, "0:0.4:Co-author of the Endless Frontier bill that became the CHIPS Act", "13:0.4:Forced release of the Epstein files with Rep. Massie", "4:0.3:'Economic patriotism' plan to rebuild manufacturing");
        appeal(c, "16:0.10 0:0.08 28:0.06");

        c = cand("kelly", "Mark", "Kelly", DEM, "U.S. Senator, Arizona", "AZ", 1964, F_MOD,
                "65 60 62 62 82 85 70 85 75 10 65 60 80 75 10 40 85 75 70 80 60", 0.55, 3, 22, 3.0, 0, "M 1 6 0 0.8 4 0 1.0 0.6 1 1 0.75 0.6");
        pos(c, -0.25, -0.30, "14=-0.6 7=0.05");
        bio(c, "Senator from Arizona since 2020, retired Navy combat pilot and NASA astronaut, husband of former Rep. Gabby Giffords. Threatened with a court-martial review in 2025 over a video urging troops to refuse illegal orders.",
                "Unimpeachable biography, swing-state wins, the largest war chest of any potential candidate.",
                "Low-key on stage; may be too moderate and too cautious for primary voters.");
        rec(c, "16:0.7:Navy combat pilot in the Gulf War and four-time Space Shuttle astronaut", "14:0.4:Gun-safety advocate with Gabby Giffords", "-1:0:Won Arizona statewide twice");
        appeal(c, "25:0.12 21:0.04 22:0.03");
        c.spouse = "Gabby Giffords";

        c = cand("gallego", "Ruben", "Gallego", DEM, "U.S. Senator, Arizona", "AZ", 1979, F_PROG,
                "72 72 68 68 70 70 75 70 60 0 65 65 80 80 25 25 60 70 50 70 65", 0.30, 1, 3, 1.2, 0, "M 3 0 2 0 2 0 1.02 0.6 0 0 0.35 0.65");
        pos(c, -0.48, -0.25, "7=-0.05 11=0.2");
        bio(c, "Senator from Arizona since 2025, Marine Corps combat veteran of Iraq and son of immigrants; won Arizona by 2 points in 2024 while Trump carried it by 5.",
                "A working-class Latino Democrat who wins Hispanic men - exactly the voters the party lost.",
                "Short Senate tenure; personal life and past votes will be scrutinised; little national profile.");
        rec(c, "16:0.4:Marine combat veteran of Iraq", "-1:0:Outran Harris by 7 points in Arizona in 2024", "7:0.3:Voted for the Laken Riley Act on immigration enforcement");
        appeal(c, "13:0.18 12:0.10 25:0.06 2:0.04");

        c = cand("murphy", "Chris", "Murphy", DEM, "U.S. Senator, Connecticut", "CT", 1973, F_PROG,
                "68 75 75 75 82 75 60 82 60 0 65 78 70 75 10 15 75 35 55 65 85", 0.35, 1, 8, 2.5, 0, "M 1 2 2 0.10 0 0 0.95 0.5 0 0 0.4 0.6");
        bio(c, "Senator from Connecticut since 2013 after the Sandy Hook massacre shaped his career. One of the most aggressive anti-Trump voices online since 2025.",
                "Sharp, combative and policy-serious; a foreign-policy voice; strong small-dollar engine.",
                "A blue-state senator with little crossover appeal; never run nationally.");
        rec(c, "14:0.6:Negotiated the Bipartisan Safer Communities Act", "16:0.3:Foreign Relations Committee leader on Ukraine", "13:0.4:Anti-corruption crusade against the administration");
        appeal(c, "0:0.04 27:0.03");

        c = cand("ossoff", "Jon", "Ossoff", DEM, "U.S. Senator, Georgia", "GA", 1987, F_LIB,
                "78 65 82 75 78 80 70 85 75 5 75 70 65 85 10 20 45 55 60 75 70", 0.40, 1.5, 6, 3.0, 0, "M 1 1 5 0 0 0 0.95 0.45 0 0 0.25 0.6");
        bio(c, "Senator from Georgia since 2021 and the youngest member of the Senate; re-elected in November 2026 by 18 points over Rep. Mike Collins, the best Democratic showing in Georgia in decades. A former investigative documentary producer.",
                "Young, eloquent, battle-tested in Georgia's brutal races; a prodigious fundraiser.",
                "Thin legislative record; just began a new six-year term.");
        rec(c, "13:0.5:Wrote the bill to ban stock trading by members of Congress", "11:0.3:Exposed abuse in federal prisons", "-1:0:Won Georgia twice");
        appeal(c, "0:0.08 10:0.06");

        c = cand("warnock", "Raphael", "Warnock", DEM, "U.S. Senator, Georgia", "GA", 1969, F_LIB,
                "82 60 95 75 80 85 80 80 65 0 75 55 80 75 25 15 35 55 60 75 65", 0.45, 1, 5, 2.5, 0, "M 5 0 1 0.35 4 0 1.0 0.5 0 1 0.5 0.75");
        bio(c, "Senator from Georgia since 2021 and senior pastor of Ebenezer Baptist Church, Martin Luther King Jr.'s congregation.",
                "A preacher's gift for oratory, moral authority and strong appeal to Black and religious voters.",
                "Little executive experience; a messy divorce dispute resurfaced in campaigns.");
        rec(c, "9:0.5:Capped insulin at $35 for seniors", "-1:0:Pastor of Ebenezer Baptist Church", "-1:0:Won Georgia twice, including a 2022 runoff");
        appeal(c, "10:0.15 6:0.04 1:0.03");

        c = cand("walz", "Tim", "Walz", DEM, "Governor of Minnesota", "MN", 1964, F_PROG,
                "72 55 68 55 65 50 85 70 55 0 70 60 85 70 45 70 50 55 60 50 70", 0.75, 2, 1, 1.5, 0, "M 1 7 2 0.9 0 1 1.10 0.5 3 0 0.8 0.75");
        bio(c, "Governor of Minnesota since 2019, former congressman, teacher, football coach and 24-year Army National Guard veteran. Kamala Harris's 2024 running mate. Ended his bid for a third term in January 2026; Amy Klobuchar succeeds him in January 2027.",
                "Folksy, authentic Midwestern populist with rural roots and a progressive record.",
                "Weak 2024 debate performance; a sprawling Minnesota fraud scandal on his watch; gaffe-prone.");
        rec(c, "2:0.4:Free school meals and paid family leave in Minnesota", "16:0.2:24 years in the Army National Guard", "13:-0.3:Minnesota social-service fraud scandal");
        appeal(c, "22:0.06 8:0.05 25:0.04 26:0.03");
        c.spouse = "Gwen Walz";

        c = cand("emanuel", "Rahm", "Emanuel", DEM, "Former Ambassador to Japan", "IL", 1959, F_MOD,
                "60 75 62 70 85 55 55 30 90 15 30 55 60 70 40 70 75 50 85 40 30", 0.45, 1, 1, 0.2, 0, "M 1 6 4 0.6 0 0 0.92 0.4 1 1 0.75 0.5");
        pos(c, -0.15, -0.12, "0=0.6 11=0.45 17=0.2");
        bio(c, "Obama's first White House chief of staff, two-term mayor of Chicago and Biden's ambassador to Japan. Arguing the party must reclaim the centre.",
                "Formidable strategist and fundraiser; deep foreign-policy and governing experience.",
                "Abrasive; the left distrusts him; the Laquan McDonald case still follows him.");
        rec(c, "0:0.5:Ambassador to Japan; built anti-China alliances", "15:0.3:Lengthened Chicago's school day", "9:0.2:Helped pass the Affordable Care Act as chief of staff");
        appeal(c, "17:0.08 0:-0.10 10:-0.06");

        c = cand("raimondo", "Gina", "Raimondo", DEM, "Former Secretary of Commerce", "RI", 1971, F_ABUN,
                "55 60 60 62 88 80 55 35 85 10 35 40 55 70 15 80 70 50 75 50 30", 0.25, 0.5, 1, 0.2, 0, "F 1 1 6 0.05 0 0 0.98 0.5 1 0 0.45 0.6 E");
        pos(c, -0.12, -0.40, "0=0.6 1=0.3");
        bio(c, "Commerce Secretary 2021-25 who ran the CHIPS Act and export controls on China; governor of Rhode Island and a former venture capitalist.",
                "Technocratic competence on chips, AI and China; well connected to business.",
                "Little grassroots following; progressive suspicion of her business ties.");
        rec(c, "0:0.6:Ran the CHIPS Act and chip export controls on China", "1:0.4:Built the U.S. AI Safety Institute", "10:-0.2:Cut Rhode Island pensions as treasurer");
        appeal(c, "7:0.05 9:0.04");

        // ---- new faces from the 2026 midterms
        c = cand("talarico", "James", "Talarico", DEM, "U.S. Senator-elect, Texas", "TX", 1989, F_PROG,
                "84 88 85 75 80 80 82 82 55 0 80 88 88 85 10 15 25 70 45 75 75", 0.25, 3, 4, 2.5, 0, "M 1 2 5 0 0 0 0.95 0.45 0 1 0.2 0.75");
        bio(c, "A former San Antonio middle-school teacher and Presbyterian seminarian who served four terms in the Texas House. Won Texas's 2026 Senate race over Attorney General Ken Paxton with about 55% of the vote, ending the Republicans' 32-year run of statewide wins in Texas. Became a national figure after viral appearances on The Joe Rogan Experience and The Late Show.",
                "A generational talent on camera and in long-form interviews; a Texas win proves crossover appeal; electrifies young and religious voters.",
                "Only 39 on Election Day; no executive record; would be running before the end of his second year in the Senate.");
        rec(c, "15:0.5:Fought school vouchers and the Ten Commandments classroom law as a teacher-legislator", "-1:0:First Democrat to win statewide in Texas since 1994");
        appeal(c, "0:0.06 12:0.04 6:0.03");

        c = cand("cooper", "Roy", "Cooper", DEM, "U.S. Senator-elect, North Carolina", "NC", 1957, F_MOD,
                "70 55 66 68 75 82 72 60 78 5 55 40 72 70 10 85 45 82 75 85 45", 0.40, 1.5, 3, 1.0, 0, "M 1 6 0 0.9 0 0 1.0 0.55 1 1 0.85 0.6");
        bio(c, "Governor of North Carolina for two terms (2017-2025) and attorney general for four. Beat former RNC chair Michael Whatley in North Carolina's 2026 Senate race, the first Democratic Senate win in the state since 2008.",
                "Has won North Carolina statewide six times; calm, moderate and trusted by independents.",
                "Would be 71 on Election Day; low-wattage on the stump; little pull with the activist base.");
        rec(c, "-1:0:Vetoed more than 100 bills from a Republican legislature as governor", "9:0.5:Expanded Medicaid to 600,000 North Carolinians");
        appeal(c, "21:0.04 8:0.03");

        c = cand("brown", "Sherrod", "Brown", DEM, "U.S. Senator-elect, Ohio", "OH", 1952, F_PROG,
                "72 70 75 70 75 75 85 70 55 0 70 55 92 65 10 20 40 75 70 80 80", 0.45, 1.5, 3, 1.5, 0, "M 1 6 4 0.95 0 0 1.05 0.5 1 1 0.95 0.55");
        bio(c, "Senator from Ohio for three terms (2007-2025). Lost narrowly in 2024, then came back to win the 2026 special election for Vance's old seat by 5 points over Sen. Jon Husted. The Senate's best-known pro-worker populist and trade hawk.",
                "Union households and working-class voters trust him; has won Ohio again and again in a Republican-leaning state.",
                "Would be 76 on Election Day; his special-election seat is up again in 2028.");
        rec(c, "4:0.5:Led the fight against NAFTA-style trade deals for 30 years", "3:0.4:Wrote the law protecting Ohio's union pensions");
        appeal(c, "24:0.06 8:0.03");

        c = cand("klobuchar", "Amy", "Klobuchar", DEM, "Governor-elect of Minnesota", "MN", 1960, F_MOD,
                "64 60 62 72 80 80 82 65 70 5 60 40 70 75 25 45 50 70 75 85 45", 0.60, 2, 3, 1.5, 0, "F 1 3 3 0.15 0 0 1.0 0.5 0 0 0.6 0.65 E");
        bio(c, "Senator from Minnesota since 2007 and a 2020 presidential candidate. Elected governor in November 2026 with 55.5% to Lisa Demuth's 42.7% after Tim Walz ended his bid for a third term; she will be the state's first woman governor.",
                "Wins rural and suburban Minnesota big; a pragmatic dealmaker with a long Senate record.",
                "Her 2020 run never caught fire; past reports of harsh treatment of staff; would be barely two years into the governorship.");
        rec(c, "13:0.4:Led Senate work on election security and the 2021 inauguration", "-1:0:Has passed more bills than almost any other senator of her era");
        appeal(c, "22:0.04 21:0.03");

        c = cand("cuban", "Mark", "Cuban", DEM, "Businessman", "TX", 1958, F_OUTD,
                "78 90 60 72 78 45 70 60 60 100 55 90 85 70 40 70 25 70 15 65 55", 0.75, 2.5, 0, 0.5, 4000, "M 1 1 10 0.3 0 0 1.0 0.55 4 9 0.7 0.7");
        bio(c, "Billionaire entrepreneur, longtime owner of the Dallas Mavericks, 'Shark Tank' investor and founder of the Cost Plus Drugs pharmacy. Campaigned for Harris in 2024.",
                "Can self-fund; plain-spoken outsider with real crossover appeal and a health-care business that cut drug prices.",
                "No political experience; says he does not want the job; prone to off-the-cuff remarks.");
        rec(c, "9:0.5:Cost Plus Drugs: cut prices on hundreds of prescriptions", "-1:0:Built and sold Broadcast.com; owned the Dallas Mavericks");
        appeal(c, "2:0.12 5:0.05 0:0.04");

        c = cand("smith", "Stephen A.", "Smith", DEM, "ESPN commentator", "NY", 1967, F_OUTD,
                "85 88 75 70 55 35 65 55 40 40 50 95 80 75 45 10 10 60 5 50 50", 0.70, 2, 0, 0.5, 60, "M 5 0 0 0.2 5 0 1.0 0.6 2 2 0.6 0.7");
        pos(c, -0.20, -0.05, "7=0.15 11=0.4 17=0.3");
        bio(c, "Host of ESPN's 'First Take' and one of the most-watched sports commentators in America. Has openly mused about a 2028 run.",
                "Enormous reach with Black men, young men and sports fans; unscripted and electric on air.",
                "No political or governing experience; a long archive of hot takes.");
        rec(c, "-1:0:Built one of the biggest audiences in sports media");
        appeal(c, "11:0.25 2:0.12 5:0.06 10:0.06");

        // ---------------------------------------------------------------- REPUBLICANS
        c = cand("vance", "JD", "Vance", REP, "Vice President", "OH", 1984, F_NATCON,
                "62 85 70 88 88 70 55 85 92 15 85 80 45 80 25 40 60 30 90 70 90", 0.92, 42, 10, 6.0, 0, "M 1 1 2 0 1 0 1.05 0.55 0 1 0.3 0.6");
        bio(c, "Vice President since 2025, previously senator from Ohio, Marine veteran of Iraq and author of 'Hillbilly Elegy'. Finance chair of the RNC and widely seen as Trump's heir.",
                "Trump's blessing, MAGA donors, a strong debater and podcast guest; leads every primary poll.",
                "Net-negative favourability; tied to the administration's record; struggles with women and college-educated voters.");
        rec(c, "4:0.4:Leading voice for tariffs and reshoring", "0:0.3:Hawk on China trade", "16:0.2:Marine Corps veteran of Iraq", "2:0.1:Cast the tie-breaking vote on the One Big Beautiful Bill");
        appeal(c, "8:0.08 2:0.06 4:-0.06 9:-0.06");
        c.spouse = "Usha Vance";

        c = cand("rubio", "Marco", "Rubio", REP, "Secretary of State", "FL", 1971, F_HAWK,
                "72 65 82 78 85 75 60 70 88 5 60 55 55 75 15 55 95 60 85 80 70", 0.85, 18, 4, 1.5, 0, "M 2 0 2 0.10 0 0 0.95 0.5 0 2 0.4 0.65");
        pos(c, 0.55, 0.62, "4=0.6 16=0.2");
        bio(c, "Secretary of State and acting National Security Adviser since 2025; senator from Florida 2011-25; 2016 presidential candidate. The face of the 2026 Venezuela operation.",
                "Commanding foreign-policy credentials, polished speaker, appeal to Hispanic voters and donors; rising in primary polls.",
                "MAGA activists remember him as a 'neocon'; the 2016 debate collapse; owns the administration's foreign policy.");
        rec(c, "0:0.6:Architect of the administration's confrontation with China", "-1:0:Secretary of State and National Security Adviser", "3:0.2:Co-wrote the Paycheck Protection Program");
        appeal(c, "14:0.25 12:0.10 6:0.03 25:0.05 0:-0.03");
        c.spouse = "Jeanette Rubio";

        c = cand("trumpjr", "Donald", "Trump Jr.", REP, "Businessman", "FL", 1977, F_MAGA,
                "65 85 60 55 60 45 55 90 80 40 85 92 70 70 55 30 25 15 70 60 98", 0.88, 11, 5, 5.0, 50, "M 1 1 3 0.05 1 0 1.05 0.6 0 0 0.3 0.5");
        bio(c, "The president's eldest son, executive vice president of the Trump Organization, podcast host and MAGA's most effective surrogate and fundraiser.",
                "The Trump name and the MAGA list; owns the online right; unshakeable base loyalty.",
                "No government experience; polarising to independents and women; questions about the family's business deals.");
        rec(c, "-1:0:MAGA's top surrogate and fundraiser", "-1:0:Built a conservative media and venture business");
        appeal(c, "8:0.08 2:0.06 26:0.05 4:-0.10 9:-0.10 21:-0.06");

        c = cand("desantis", "Ron", "DeSantis", REP, "Governor of Florida (to Jan 2027)", "FL", 1978, F_MAGA,
                "45 50 62 68 82 75 40 60 75 0 60 50 50 75 25 88 45 45 55 75 70", 0.85, 6, 4, 2.0, 0, "M 1 1 2 0 0 0 1.05 0.7 0 2 0.35 0.4");
        pos(c, 0.62, 0.90, "4=0.4 16=0.3");
        bio(c, "Two-term governor of Florida, term-limited in January 2027; Navy JAG veteran; ran second to Trump in the 2024 primaries before dropping out.",
                "A big governing record and a 19-point re-election; disciplined and policy-driven; a 'non-Vance' lane for donors.",
                "Awkward retail politician; the 2024 campaign flopped; MAGA loyalists have not forgotten he challenged Trump.");
        rec(c, "15:0.4:Parental-rights and universal school-choice laws", "-1:0:Won re-election by 19 points in 2022", "11:0.3:Hurricane Ian recovery");
        appeal(c, "7:0.04 14:0.10 4:-0.04 0:-0.05");
        c.spouse = "Casey DeSantis";

        c = cand("youngkin", "Glenn", "Youngkin", REP, "Former Governor of Virginia", "VA", 1966, F_GOV,
                "70 60 68 65 75 82 75 50 90 70 55 45 60 75 15 75 40 70 75 55 50", 0.50, 1.5, 5, 0.8, 300, "M 1 6 2 0.75 0 0 0.95 0.55 5 0 0.6 0.8");
        bio(c, "Governor of Virginia 2022-26 and former co-CEO of the Carlyle Group. Won a blue-leaning state in 2021 on parental rights; Republicans lost Virginia badly in 2025.",
                "Suburban-friendly, scandal-free, wealthy and well connected to donors.",
                "No national base; MAGA sees him as a country-club Republican; Virginia's 2025 rout dented his brand.");
        rec(c, "15:0.4:Raised teacher pay and championed parental rights", "2:0.3:Cut $4bn in Virginia taxes", "-1:0:Former co-CEO of the Carlyle Group");
        appeal(c, "21:0.08 9:0.08 29:0.06");

        c = cand("haley", "Nikki", "Haley", REP, "Former UN Ambassador", "SC", 1972, F_HAWK,
                "68 55 72 80 78 78 72 55 85 5 50 45 50 80 15 70 85 75 60 60 25", 0.80, 3, 3, 1.5, 0, "F 3 1 7 0.05 0 0 0.95 0.5 0 0 0.45 0.75 E");
        pos(c, 0.55, 0.50, "7=0.6 16=-0.8");
        bio(c, "Two-term governor of South Carolina and UN ambassador in Trump's first term; Trump's last challenger in the 2024 primaries.",
                "Strong with independents, college-educated suburbanites and hawks; a sharp debater.",
                "The MAGA base sees her as disloyal; small primary lane; voters she wins often are not Republicans.");
        rec(c, "16:0.6:UN Ambassador who took a hard line on Russia, Iran and China", "-1:0:Removed the Confederate flag from South Carolina's statehouse", "-1:0:Ran second in the 2024 primaries");
        appeal(c, "9:0.12 21:0.10 4:0.05 16:0.05 8:-0.05");

        c = cand("cruz", "Ted", "Cruz", REP, "U.S. Senator, Texas", "TX", 1970, F_HAWK,
                "55 80 75 85 88 65 50 75 75 0 60 60 40 80 30 20 65 25 55 55 70", 0.85, 2, 5, 2.5, 0, "M 2 1 3 0.40 2 0 1.0 0.55 0 1 0.55 0.5");
        pos(c, 0.75, 0.80, "16=-0.2");
        bio(c, "Senator from Texas since 2013, former Texas solicitor general who argued before the Supreme Court, runner-up for the 2016 nomination; hosts the 'Verdict' podcast.",
                "Formidable debater and constitutional lawyer; long-form media fluency; a grassroots list from 2016.",
                "Widely disliked by colleagues; the Cancun episode; a hawk out of step with the restrainer right.");
        rec(c, "-1:0:Argued nine cases before the Supreme Court", "-1:0:Runner-up for the 2016 nomination", "5:0.4:Leading voice against antisemitism on the right");
        appeal(c, "6:0.05 12:0.03 4:-0.06");

        c = cand("hawley", "Josh", "Hawley", REP, "U.S. Senator, Missouri", "MO", 1979, F_NATCON,
                "58 65 68 72 85 75 55 70 55 0 55 55 60 80 20 30 45 35 40 65 75", 0.55, 1, 5, 1.5, 0, "M 1 2 2 0 0 0 0.95 0.5 0 0 0.3 0.5");
        pos(c, 0.05, 0.85, "1=-0.5 3=-0.3 13=0.2");
        bio(c, "Senator from Missouri since 2019 and former state attorney general. A populist who breaks with the White House on AI regulation, stock trading and labour.",
                "Policy entrepreneur of the working-class right; Yale-trained litigator.",
                "The January 6 fist-pump; little crossover appeal; clashes with Trump.");
        rec(c, "13:0.5:Wrote a ban on stock trading by members of Congress and the president", "1:0.5:Leading Republican voice for AI regulation and child online safety", "3:0.3:Backed labour-law reforms with the Teamsters");
        appeal(c, "24:0.06 8:0.05 6:0.05");

        c = cand("kemp", "Brian", "Kemp", REP, "Governor of Georgia (to Jan 2027)", "GA", 1963, F_GOV,
                "60 50 62 65 72 85 72 50 80 15 65 35 75 70 10 85 30 70 70 85 40", 0.55, 1, 5, 0.8, 0, "M 1 6 2 0.85 0 0 1.05 0.7 0 2 0.7 0.6");
        bio(c, "Two-term governor of Georgia, term-limited in January 2027. Beat Stacey Abrams twice and refused Trump's pressure to overturn Georgia's 2020 result.",
                "Swing-state winner with a strong record on jobs and a formidable Georgia machine; appeals to suburbs.",
                "MAGA grudges over 2020; low national profile; little charisma on the national stage.");
        rec(c, "3:0.5:Landed Hyundai's and Rivian's EV plants in Georgia", "-1:0:Beat Stacey Abrams twice", "13:0.3:Certified Georgia's 2020 result despite pressure");
        appeal(c, "21:0.06 22:0.05 10:0.03");

        c = cand("sanders", "Sarah", "Huckabee Sanders", REP, "Governor of Arkansas", "AR", 1982, F_MAGA,
                "62 55 65 65 70 78 65 65 70 0 60 55 55 75 30 65 30 35 80 75 80", 0.60, 1, 3, 1.2, 0, "F 1 1 7 0 0 0 1.0 0.5 0 0 0.3 0.6 E");
        pos(c, 0.60, 0.85, null);
        bio(c, "Governor of Arkansas since 2023 and Trump's White House press secretary 2017-19; daughter of former governor Mike Huckabee. Re-elected in November 2026 (54-39) as Democrats swept much of the South.",
                "Trump-world credentials with a governing record; strong with evangelicals.",
                "Small-state profile; the lectern spending controversy; untested nationally.");
        rec(c, "15:0.5:Signed Arkansas LEARNS, universal school choice", "-1:0:Trump's White House press secretary");
        appeal(c, "6:0.08 29:0.03");

        c = cand("ramaswamy", "Vivek", "Ramaswamy", REP, "Entrepreneur; 2026 Ohio governor nominee", "OH", 1985, F_TECHR,
                "70 88 75 82 85 60 70 70 75 80 60 82 45 90 25 35 30 40 50 60 80", 0.65, 2, 5, 1.5, 500, "M 3 0 2 0 0 0 0.92 0.45 0 0 0.25 0.7");
        bio(c, "Biotech entrepreneur, 2024 presidential candidate and briefly co-chair of DOGE; lost the November 2026 race for Ohio governor to Democrat Amy Acton by 8 points (52-44) in a state Trump carried by 11.",
                "Relentless energy, podcast-native, can self-fund; popular with young men and the tech right.",
                "Seen as glib by older voters; no governing record, and an 8-point loss in Ohio in 2026.");
        rec(c, "-1:0:Founded Roivant Sciences", "2:0.2:Co-chaired DOGE at its launch", "15:0.3:Champion of merit-based education");
        appeal(c, "16:0.08 2:0.08 1:-0.06");

        c = cand("carlson", "Tucker", "Carlson", REP, "Media personality", "FL", 1969, F_RESTR,
                "78 95 70 70 75 55 50 80 55 25 70 90 70 70 55 10 30 25 25 30 90", 0.80, 3, 2, 3.0, 40, "M 1 4 11 0.45 0 0 1.0 0.5 3 0 0.6 0.4");
        bio(c, "Former Fox News prime-time host turned independent media giant; interviewed Putin in 2024 and drew fire from fellow conservatives over a 2025 interview with Nick Fuentes.",
                "One of the most influential voices on the right, with a vast audience and podcast fluency.",
                "Feuds with hawks and Jewish conservatives; no governing experience; a trail of controversies.");
        rec(c, "16:0.3:Leading voice against aid to Ukraine", "-1:0:Built the largest independent conservative media platform");
        appeal(c, "2:0.08 8:0.04 17:-0.25 9:-0.08 25:-0.03");

        c = cand("greene", "Marjorie", "Taylor Greene", REP, "Former U.S. Representative, Georgia", "GA", 1974, F_RESTR,
                "60 75 60 55 50 30 60 80 20 10 65 90 85 75 60 10 15 15 5 45 70", 0.75, 2, 3, 2.5, 0, "F 1 5 7 0 0 0 1.0 0.5 0 0 0.4 0.6 E");
        pos(c, 0.15, 0.85, "9=0.0 13=-0.2");
        bio(c, "Congresswoman from Georgia 2021-26 who broke with Trump in 2025 over the Epstein files, health-care costs and foreign wars, then resigned from Congress.",
                "Fierce populist following, viral reach and a reputation for saying what she thinks.",
                "Toxic with most general-election voters; now feuding with Trump and the MAGA establishment.");
        rec(c, "13:0.3:Pushed release of the Epstein files", "9:0.2:Broke with her party over health-care costs");
        appeal(c, "22:0.04 4:-0.08 9:-0.15");

        c = cand("rfk", "Robert F.", "Kennedy Jr.", REP, "Secretary of Health and Human Services", "CA", 1954, F_MAHA,
                "65 85 62 55 70 40 60 70 60 20 65 75 70 70 70 50 20 50 40 30 55", 0.90, 4, 2, 2.5, 10, "M 1 2 11 0.5 0 0 0.92 0.55 0 1 0.85 0.55");
        bio(c, "Health and Human Services Secretary since 2025, environmental lawyer and nephew of President John F. Kennedy; ran as an independent in 2024 before endorsing Trump.",
                "MAHA movement, a famous name, real crossover appeal to independents and health-conscious parents.",
                "Vaccine controversies; would be 74; doctors, scientists and much of the GOP establishment distrust him.");
        rec(c, "9:0.3:HHS Secretary: the 'Make America Healthy Again' agenda", "12:0.3:Riverkeeper environmental lawyer", "-1:0:2024 independent candidate");
        appeal(c, "29:0.06 4:0.05 0:0.03 1:-0.04 9:-0.03");

        // ---------------------------------------------------------------- THIRD PARTIES
        c = cand("stein", "Jill", "Stein", GRN, "Physician and activist", "MA", 1950, F_GREEN,
                "45 50 50 50 65 55 45 40 5 5 35 45 70 50 30 5 10 10 0 10 60", 0.45, 0, 0.5, 0.5, 0, "F 1 6 6 0.9 0 1 0.95 0.4 1 0 0.85 0.5");
        bio(c, "Physician and the Green Party's presidential nominee in 2012, 2016 and 2024.", "Owns the anti-war protest vote over Gaza.", "Tiny organisation; accused of spoiling for Republicans.");
        appeal(c, "18:0.6 0:0.15 28:0.1");
        c = cand("yang", "Andrew", "Yang", FWD, "Forward Party co-founder", "NY", 1975, F_FWD,
                "70 85 62 68 82 65 70 55 30 10 50 70 75 75 15 30 20 60 5 5 50", 0.70, 0, 1, 1.0, 0, "M 2 0 2 0.15 0 0 0.95 0.5 0 0 0.4 0.75");
        bio(c, "Entrepreneur, 2020 Democratic candidate who popularised universal basic income, and co-founder of the Forward Party.", "AI-age message for the politically homeless.", "Thin support beyond the online; ballot access is patchy.");
        appeal(c, "16:0.10 0:0.08 2:0.06");
        c = cand("libert", "Dana", "Whitfield", LBT, "Libertarian nominee (fictional)", "NH", 1979, F_LIBT,
                "55 65 55 55 70 60 55 35 20 5 40 45 65 70 15 20 15 35 5 10 50", 0.10, 0, 0.3, 0.2, 0, "F 1 3 6 0 0 0 1.0 0.5 3 0 0.4 0.6");
        c.fictional = true;
        bio(c, "A fictional Libertarian Party nominee: a former state legislator and small-business owner.", "The Libertarian ballot line in all 50 states.", "Little money, little attention.");
        appeal(c, "2:0.06 26:0.04");
        c = cand("amp", "Reid", "Callahan", AMP, "America Party nominee (fictional)", "TX", 1980, F_AMP,
                "72 85 68 65 82 65 60 60 90 100 55 85 60 80 35 75 30 75 5 30 60", 0.35, 0, 80, 1.0, 1500, "M 1 3 5 0.1 2 0 1.0 0.55 4 9 0.4 0.6");
        c.fictional = true;
        bio(c, "A fictional tech founder running on the America Party line with billionaire backing.", "Unlimited money and a post-partisan pitch to the exhausted middle.", "No party infrastructure; ballot access fights; attacked by both parties.");
        appeal(c, "2:0.08 21:0.05");

        // primary polls: RealClearPolling averages, late August to November 1, 2026 (others estimated from their support in individual polls)
        String polls = "harris:22 buttigieg:14 newsom:13.5 aoc:12.5 ossoff:11 kelly:6.5 shapiro:5 beshear:3.3 pritzker:3 booker:2.5 whitmer:1.3 "
                + "moore:1.5 khanna:1 gallego:1 murphy:1 warnock:1 walz:0.5 emanuel:1 raimondo:0.5 cuban:2 talarico:3 cooper:1.5 brown:1.5 klobuchar:2 "
                + "vance:38 rubio:26 desantis:8 rfk:5 haley:5 cruz:2.8 carlson:1.7 hawley:1 youngkin:0.7 trumpjr:4 kemp:1 sanders:1 ramaswamy:1 greene:1";
        for (String t : polls.split(" ")) { String[] p = t.split(":"); for (Cand x : ROSTER) if (x.id.equals(p[0])) x.poll = Double.parseDouble(p[1]); }
        for (Cand x : ROSTER) x.home = stateIdx(x.homeAb);
    }

    static Cand candById(String id) { for (Cand c : ROSTER) if (c.id.equals(id)) return c; return null; }

    static List<Cand> roster(int party) {
        List<Cand> l = new ArrayList<Cand>();
        for (Cand c : ROSTER) if (c.party == party) l.add(c);
        return l;
    }

    // ---- sandbox archetypes: base ratings and a point pool ----------------------------------------------------
    static final String[] ARCHETYPE = {"Governor", "Senator", "House member", "Big-city mayor", "Cabinet secretary", "General / admiral",
            "Billionaire CEO", "Celebrity", "Activist / organiser", "Podcaster / media star", "Faith leader"};
    static final String[] ARCH_BASE = {
            "60 55 62 60 68 70 65 55 65 5 55 50 60 70 25 80 40 55 65 75 55",
            "60 60 65 65 72 68 58 65 62 5 55 55 55 70 25 30 60 50 65 65 60",
            "58 62 60 60 66 62 62 62 45 5 55 60 60 72 25 15 35 40 45 50 65",
            "62 58 62 58 66 62 70 50 60 5 60 55 60 70 30 70 30 45 55 50 55",
            "52 55 55 58 78 75 50 35 70 5 40 40 55 70 15 70 65 50 70 40 40",
            "60 55 60 55 72 85 55 40 55 5 55 45 75 75 15 70 95 70 50 40 50",
            "62 70 55 58 75 55 55 45 75 95 45 65 60 70 40 75 30 55 30 40 45",
            "82 75 70 55 50 45 65 65 50 50 60 92 70 70 45 10 10 60 10 40 55",
            "72 70 72 62 65 60 70 75 30 0 85 70 80 75 25 10 15 30 25 40 85",
            "72 92 62 65 66 50 55 70 40 20 60 90 75 75 40 5 15 55 10 30 70",
            "75 60 85 62 70 80 70 55 45 5 70 50 75 70 20 20 20 55 40 45 60"};
    static final int SANDBOX_POINTS = 120;
    // =====================================================================
    //  5.  THE PRESIDENCY: calendar, settings and the state of a game
    // =====================================================================
    static final int NS = 51;                             // states incl. DC
    static final int CUS = 7;                             // a party of the player's own design (unused here, kept for shared code)
    static int CAL_Y = 2029, CAL_M = 0, CAL_D = 20;      // day 0 of the game: inauguration day

    static final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    static final String[] MONTH_FULL = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    static final String[] DOW = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    static final String[] DOW3 = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    /** {year, month0, day} of a game day. */
    static int[] dateOf(int day) {
        java.time.LocalDate d = java.time.LocalDate.of(CAL_Y, CAL_M + 1, CAL_D).plusDays(day);
        return new int[]{d.getYear(), d.getMonthValue() - 1, d.getDayOfMonth()};
    }

    /** Day of the week of a game day, 0 = Monday. */
    static int dowOf(int day) {
        return java.time.LocalDate.of(CAL_Y, CAL_M + 1, CAL_D).plusDays(day).getDayOfWeek().getValue() - 1;
    }

    static String dateStr(int day) { int[] d = dateOf(day); return MONTHS[d[1]] + " " + d[2] + ", " + d[0]; }

    static String dateLong(int day) { int[] d = dateOf(day); return DOW[dowOf(day)] + ", " + MONTH_FULL[d[1]] + " " + d[2] + ", " + d[0]; }

    static String monthYear(int day) { int[] d = dateOf(day); return MONTH_FULL[d[1]] + " " + d[0]; }

    /** The game day of a calendar date (may be negative). */
    static int dayOfDate(int y, int m0, int d) {
        int lo = -4000, hi = 4000;
        while (lo < hi) { int mid = (lo + hi) / 2; int[] t = dateOf(mid); long k = t[0] * 10000L + t[1] * 100 + t[2], want = y * 10000L + m0 * 100 + d; if (k < want) lo = mid + 1; else hi = mid; }
        return lo;
    }

    // ---- the president's week: seven days of three slots -----------------------------------------------------
    static final int NSLOT = 21;
    static final String[] SLOT = {"Morning", "Afternoon", "Evening"};
    static final int AC_FREE = 0, AC_REST = 1, AC_EXERCISE = 2, AC_FAMILY = 3, AC_POLICY = 4, AC_REMARKS = 5, AC_TRIP = 6, AC_FUNDRAISER = 7,
            AC_CEREMONY = 8, AC_CONGRESS = 9, AC_CAMPDAVID = 10, AC_WORKLATE = 11, AC_TRAVEL = 12, AC_CAMPAIGN = 13, NAC = 14;
    static final String[] AC_NAME = {"Open time", "Rest", "Exercise", "Family time", "Policy time", "White House remarks", "Trip", "Fundraiser",
            "Ceremony", "Hill meetings", "Camp David", "Work late", "Travel", "Campaign stop"};
    static final String[] AC_DESC = {
            "Nothing scheduled. Staff fill it with calls and paperwork.",
            "Sleep in or nap. Restores stamina and lowers stress.",
            "A workout. Restores a little stamina, lowers stress and helps your health over time.",
            "Time with your family. Lowers stress a lot; the public likes seeing it.",
            "Meetings with your policy staff on one issue. Builds your command of it and your standing on it.",
            "A speech at the White House on one issue. Moves the news and your standing on that issue.",
            "Fly to a state for an event. Lifts your standing there and nationally, but long flights are tiring.",
            "Raise money for your party. Fills the war chest; too many look bad.",
            "Bill signings, medals, championship teams. A small, safe lift.",
            "Meet members of Congress. Improves your relationships on the Hill.",
            "A weekend at Camp David. Big stamina boost, but bad optics in a crisis.",
            "Stay at your desk into the night. Gets work done but costs sleep.",
            "In the air.",
            "Rally for your party's candidates in a state. Only in the weeks before the midterms."};
    static final double[] AC_STAMINA = {0, 6, 2.5, 3, -2.5, -3, -5, -3.5, -1.5, -3, 8, -4.5, -2, -5};
    static final double[] AC_STRESS = {0, -1.2, -1.2, -1.8, 0.6, 0.6, 0.8, 0.6, -0.3, 0.8, -2.5, 1.5, 0.5, 0.8};
    static boolean TRACE;                   // the test harness prints every headline

    static final String[] PARTIES = {"Democrats", "Republicans", "Greens", "Libertarians", "Forward", "America Party", "Independents", "Your party"};

    static boolean isTrip(int a) { return a == AC_TRIP || a == AC_CAMPAIGN; }

    static final boolean[] AC_ISSUE = {false, false, false, false, true, true, false, false, false, false, false, false, false, false};
    static final boolean[] AC_STATE = {false, false, false, false, false, false, true, false, false, false, false, false, false, true};

    // ---- the issues voters judge a president on -----------------------------------------------------------
    static final int PI_ECON = 0, PI_PRICES = 1, PI_JOBS = 2, PI_HEALTH = 3, PI_IMMIG = 4, PI_CRIME = 5, PI_FOREIGN = 6, PI_SECURITY = 7,
            PI_TRADE = 8, PI_ENERGY = 9, PI_RIGHTS = 10, PI_TECH = 11, PI_EDU = 12, PI_ETHICS = 13, NPI = 14;
    static final String[] PISSUE = {"The economy", "Prices", "Jobs", "Health care", "Immigration", "Crime", "Foreign policy", "National security",
            "Trade", "Energy & climate", "Civil rights", "Technology & AI", "Education", "Ethics & government"};
    static final String[] PISSUE_SHORT = {"Economy", "Prices", "Jobs", "Health", "Immigration", "Crime", "Foreign", "Security", "Trade", "Energy", "Rights", "Tech & AI", "Education", "Ethics"};

    static final int NK_INFO = 0, NK_GOOD = 1, NK_BAD = 2, NK_BIG = 3, NK_ECON = 4, NK_WORLD = 5;

    static final class News implements Serializable {
        private static final long serialVersionUID = 1L;
        int day, kind;
        String head, body;
        double weight;                      // how big a story it is
    }

    static final class Settings implements Serializable {
        private static final long serialVersionUID = 1L;
        int year = 2029;                    // inauguration year
        int scenario;
        int difficulty = 1;                 // 0 easy, 1 normal, 2 hard
        String presId, vpId;
        Cand custom;                        // a president you created
        boolean tutorial = true;
        double cApproval = Double.NaN, cUnemp = Double.NaN, cInfl = Double.NaN, cGdp = Double.NaN;
        int cHouse = -1, cSenate = -1;      // the president's party's seats, or -1 for the era's
        long seed;
        String bgLine;                      // a Battleground 2028 Hall of Fame line the president came from
    }

    static final class Econ implements Serializable {
        private static final long serialVersionUID = 1L;
        double og, potential = 1.9, nairu = 4.3, structural;     // output gap, trend growth, natural unemployment
        double gdp, unemp, infl, core, expect;                    // growth (yoy %), unemployment %, inflation %, expectations
        double fed, fedTarget, rNeutral = 0.75, tenYr, mortgage;
        double stocks, stocks0, oil, oilBase, gas, gas0;
        double groceries, rent, insurance, sentiment;
        double debt, deficit, interest, nominalGdp;              // % of GDP, % of GDP, % of GDP, $ trillions
        double fiscal, tariff, demandShock, supplyShock, finShock, aiShock, finShockPrev, ogPrev;
        int calm;
        double housing, manufacturing, aiInvest, farm, energyIdx, tourism;
        double priceLevel = 1;                                    // era prices relative to 2025
        String fedChair = "";
        double fedHawk = 0.5, fedCred = 0.8;
        int nextFomc;
        double[] ogHist = new double[53];
        int ogPos;
        double jobsLast, gdpReported, cpiReported;
        double[] stUnemp = new double[NS], stGas = new double[NS];
        double polGdp, polUnemp, polInfl;                         // what the laws you sign do to trend growth, jobs and prices
    }

    static final class Game implements Serializable {
        private static final long serialVersionUID = 1L;
        static final int VERSION = 1;
        int version = VERSION;
        Settings set;
        Random rng;
        Era era;
        int week, termWeeks = 208, term = 1;
        Cand pres, vp;
        int party;
        int age;
        // the calendar
        int[] act = new int[NSLOT], arg = new int[NSLOT];
        int location;                       // state the president is in (DC by default)
        // health
        double stamina = 85, sleep = 6.5, stress = 25, health = 92, fitness = 50;
        int sick;                           // weeks of illness left
        // standing with the public
        double approval, approval0, honeymoon, rally, perf, scandal;
        double[] issueApp = new double[NPI], issueAdj = new double[NPI], issueSal = new double[NPI], command = new double[NPI];
        double[] stateApp = new double[NS], stateBump = new double[NS];
        double[] groupApp = new double[NG], groupAdj = new double[NG];
        double offset;                      // the cell model's national shift
        double warChest, funds;             // party money ($M)
        int fundraisers;
        double hill = 50;                   // relations with Congress (0-100), deepened in a later version
        int houseD, senateD;                // seats held by Democrats (and the independents who caucus with them)
        Econ econ = new Econ();
        Congress cong;
        double capital = 30;                // political capital: spent on deals, earned by approval and wins
        List<Decision> inbox = new ArrayList<Decision>();
        boolean filibusterGone;
        // history: one row a week
        List<double[]> hist = new ArrayList<double[]>();
        // the news
        List<News> news = new ArrayList<News>();
        int paperWeek = -1;
        List<String> ticker = new ArrayList<String>();
        boolean over;
        transient boolean dirty = true;

        int day() { return week * 7; }

        int presAge() { return pres == null || pres.born <= 0 ? 60 : dateOf(day())[0] - pres.born; }

        boolean dem() { return party == DEM; }

        int myHouse() { return party == REP ? 435 - houseD : houseD; }

        int mySenate() { return party == REP ? 100 - senateD : senateD; }

        void log(int kind, String head, String body, double weight) {
            News n = new News();
            n.day = day(); n.kind = kind; n.head = head; n.body = body; n.weight = weight;
            news.add(n);
            if (TRACE) System.out.println("    [" + dateStr(n.day) + "] " + head);
            if (news.size() > 400) news.remove(0);
            ticker.add(head);
            if (ticker.size() > 40) ticker.remove(0);
        }

        void log(int kind, String head) { log(kind, head, null, 1); }
    }
    // =====================================================================
    //  6.  THE ECONOMY
    //  A small macro model run weekly: an output gap pushed by fiscal policy, interest rates, tariffs, confidence
    //  and shocks; unemployment by Okun's law; inflation by an expectations-anchored Phillips curve; a Fed that
    //  follows a Taylor rule at its eight meetings a year; markets, gas and the prices people feel; the budget.
    // =====================================================================
    /** State unemployment relative to the nation (2024 averages, rounded). */
    static final String ST_UNEMP = "AL:-1.0 AK:0.6 AZ:-0.4 AR:-0.6 CA:1.3 CO:0.2 CT:0.0 DE:0.0 DC:1.5 FL:-0.7 GA:-0.5 HI:-1.0 ID:-0.5 IL:0.9 IN:0.2 IA:-1.0 KS:-0.6 KY:0.9 LA:0.3 ME:-0.9 MD:-0.9 MA:-0.2 MI:0.7 MN:-0.5 MS:0.2 MO:-0.3 MT:-1.0 NE:-1.3 NV:1.5 NH:-1.4 NJ:0.6 NM:0.0 NY:0.4 NC:-0.3 ND:-1.8 OH:0.4 OK:-0.8 OR:0.1 PA:-0.6 RI:0.4 SC:0.5 SD:-2.0 TN:-0.6 TX:0.1 UT:-0.6 VT:-1.7 VA:-1.1 WA:0.4 WV:0.0 WI:-1.0 WY:-1.0";
    /** State gas prices relative to the national average (2024, dollars). */
    static final String ST_GAS = "AL:-0.35 AK:0.60 AZ:0.20 AR:-0.40 CA:1.40 CO:0.00 CT:0.10 DE:-0.05 DC:0.30 FL:0.00 GA:-0.30 HI:1.30 ID:0.20 IL:0.40 IN:0.00 IA:-0.20 KS:-0.30 KY:-0.20 LA:-0.40 ME:0.10 MD:0.05 MA:0.10 MI:0.10 MN:-0.10 MS:-0.50 MO:-0.35 MT:0.10 NE:-0.20 NV:0.70 NH:-0.05 NJ:0.00 NM:-0.10 NY:0.20 NC:-0.20 ND:-0.20 OH:-0.10 OK:-0.45 OR:0.70 PA:0.25 RI:0.05 SC:-0.35 SD:-0.15 TN:-0.35 TX:-0.40 UT:0.10 VT:0.20 VA:-0.10 WA:0.90 WV:0.00 WI:-0.15 WY:-0.05";
    /** Which states lean on which industries: manufacturing, farming, energy, tech, tourism. */
    static final String ST_MFG = "MI IN OH WI IA KY AL SC TN NC AR MS", ST_FARM = "IA NE KS SD ND MN MT ID AR", ST_ENERGY = "TX ND WY OK NM AK WV LA CO PA",
            ST_TECH = "CA WA MA NY VA CO UT TX NC OR MD", ST_TOUR = "NV HI FL";

    static double[] stateTable(String s) {
        double[] v = new double[NS];
        for (String t : s.split(" ")) { String[] p = t.split(":"); int k = stateIdx(p[0]); if (k >= 0) v[k] = Double.parseDouble(p[1]); }
        return v;
    }

    static boolean[] stateSet(String s) {
        boolean[] v = new boolean[NS];
        for (String t : s.split(" ")) { int k = stateIdx(t); if (k >= 0) v[k] = true; }
        return v;
    }

    static final class Economy {
        static double[] uOff, gasOff;
        static boolean[] mfg, farm, energy, tech, tour;
        /** FOMC decision days: {month0, day}. */
        static final int[][] FOMC = {{0, 29}, {2, 19}, {4, 7}, {5, 18}, {6, 30}, {8, 17}, {10, 5}, {11, 10}};

        static void tables() {
            if (uOff != null) return;
            uOff = stateTable(ST_UNEMP); gasOff = stateTable(ST_GAS);
            mfg = stateSet(ST_MFG); farm = stateSet(ST_FARM); energy = stateSet(ST_ENERGY); tech = stateSet(ST_TECH); tour = stateSet(ST_TOUR);
        }

        static void init(Game g) {
            tables();
            Era er = g.era;
            Econ e = g.econ;
            Settings s = g.set;
            e.potential = er.potential; e.nairu = er.nairu; e.priceLevel = er.priceLevel;
            e.unemp = er.unemp; e.infl = er.infl; e.gdp = er.gdp; e.fed = er.fed; e.fedTarget = er.fed; e.tenYr = er.tenYr;
            e.stocks = er.stocks; e.oil = er.oil; e.oilBase = er.oil; e.debt = er.debt; e.deficit = er.deficit; e.nominalGdp = er.nominalGdp;
            e.tariff = er.tariff; e.fedChair = er.fedChair;
            switch (s.scenario) {
                case 1: e.unemp += 2; e.gdp -= 2.8; e.stocks *= 0.8; e.demandShock = -2.5; e.deficit += 1.5; break;
                case 2: e.oil *= 1.3; e.deficit += 0.8; break;
                case 5: e.infl += 3; e.oil *= 1.25; e.fed += 1; e.supplyShock = 2.0; break;
                default: break;
            }
            if (!Double.isNaN(s.cUnemp)) e.unemp = s.cUnemp;
            if (!Double.isNaN(s.cInfl)) e.infl = s.cInfl;
            if (!Double.isNaN(s.cGdp)) e.gdp = s.cGdp;
            e.og = -(e.unemp - e.nairu) / 0.5;
            double old = e.og - (e.gdp - e.potential);
            for (int i = 0; i < 53; i++) e.ogHist[i] = old + (e.og - old) * i / 52.0;
            e.ogPos = 0;
            e.ogHist[52] = e.og;
            e.expect = 0.7 * 2.0 + 0.3 * e.infl;
            e.core = e.infl;
            e.rNeutral = 0.75;
            e.gas = gasPrice(e); e.gas0 = e.gas;
            e.stocks0 = e.stocks;
            e.mortgage = e.tenYr + 1.8;
            e.interest = e.debt * 0.032;
            e.groceries = e.infl + 0.3; e.rent = e.infl + 1.4; e.insurance = e.infl + 3.5;
            e.housing = 1.40; e.manufacturing = 49; e.aiInvest = 100; e.farm = 100; e.energyIdx = 100; e.tourism = 100;
            e.sentiment = sentimentOf(e);
            e.jobsLast = 150; e.gdpReported = e.gdp; e.cpiReported = e.infl;
            states(e);
        }

        static double gasPrice(Econ e) { return 1.25 * e.priceLevel + 0.0245 * e.oil; }

        static double sentimentOf(Econ e) {
            return clamp(96 - 4.5 * (e.unemp - 4) - 5.5 * (e.infl - 2) - 6 * (e.gas / e.priceLevel - 3.0) + 1.5 * (e.gdp - 2) + 12 * Math.log(e.stocks / e.stocks0), 45, 112);
        }

        static void states(Econ e) {
            tables();
            double mfgDev = (e.manufacturing - 50) / 10, farmDev = (e.farm - 100) / 100, enDev = (e.energyIdx - 100) / 100 + (e.oil - e.oilBase) / 200,
                    techDev = (e.aiInvest - 100) / 400, tourDev = (e.tourism - 100) / 100;
            for (int s = 0; s < NS; s++) {
                double u = e.unemp + uOff[s] * (e.unemp / 4.0);
                if (mfg[s]) u -= 0.8 * mfgDev;
                if (farm[s]) u -= 0.9 * farmDev;
                if (energy[s]) u -= 0.8 * enDev;
                if (tech[s]) u -= 0.5 * techDev;
                if (tour[s]) u -= 1.0 * tourDev;
                e.stUnemp[s] = clamp(u, 1.5, 20);
                e.stGas[s] = e.gas + gasOff[s] * e.priceLevel;
            }
        }

        static boolean weekHas(Game g, int month0, int day) {
            for (int d = 0; d < 7; d++) { int[] t = dateOf(g.day() + d); if (t[1] == month0 && t[2] == day) return true; }
            return false;
        }

        /** The first Friday of a month falls in this week. */
        static int jobsDay(Game g) {
            for (int d = 0; d < 7; d++) { int day = g.day() + d; int[] t = dateOf(day); if (dowOf(day) == 4 && t[2] <= 7) return day; }
            return -1;
        }

        static void week(Game g) {
            Econ e = g.econ;
            Random r = g.rng;
            shocks(g);
            double realRate = e.fed - e.expect;
            double ogTarget = -0.6 * (realRate - e.rNeutral) + 0.55 * e.fiscal + e.demandShock + e.finShock - 0.07 * (e.tariff - 2.5) + 0.03 * (e.sentiment - 80) - e.aiShock * 0.3;
            e.og += (ogTarget - e.og) * 0.022 + r.nextGaussian() * 0.05;
            e.demandShock *= 0.985; e.supplyShock *= 0.965; e.finShock *= 0.975; e.aiShock *= 0.99;
            e.ogHist[e.ogPos] = e.og;
            e.ogPos = (e.ogPos + 1) % 53;
            double old = e.ogHist[e.ogPos];
            e.gdp = e.potential + e.polGdp + (e.og - old) + (g.era.ai ? 0.15 * (e.aiInvest - 100) / 100 : 0);
            // unemployment follows the gap, plus any structural damage (AI disruption, a lost decade)
            e.structural = Math.max(0, e.structural * 0.995 + e.aiShock * 0.004);
            double uTarget = e.nairu + e.polUnemp + e.structural - 0.5 * e.og;
            e.unemp += (uTarget - e.unemp) * 0.035 + r.nextGaussian() * 0.012;
            e.unemp = clamp(e.unemp, 2.5, 25);
            // inflation: anchored by the Fed's credibility, pushed by the gap, oil and tariffs
            double anchor = e.fedCred * 2.0 + (1 - e.fedCred) * e.infl;
            e.expect += (anchor - e.expect) * 0.02;
            double piT = e.expect + e.polInfl + 0.28 * e.og + e.supplyShock + 0.012 * (e.oil - e.oilBase) + 0.05 * (e.tariff - g.era.tariff);
            e.infl += (piT - e.infl) * 0.03 + r.nextGaussian() * 0.025;
            e.infl = clamp(e.infl, -3, 20);
            e.core += (e.infl - 0.2 - e.core) * 0.04;
            // the Fed meets eight times a year
            double taylor = e.rNeutral + e.infl + 0.5 * (e.infl - 2) + 0.5 * e.og + (e.fedHawk - 0.5);
            e.fedTarget = clamp(taylor, 0.125, 20);
            for (int[] f : FOMC) if (weekHas(g, f[0], f[1])) fomc(g);
            // markets
            double prev10 = e.tenYr;
            double ten = 0.55 * e.fed + 0.45 * (e.rNeutral + 1.0 + e.expect) + 0.015 * Math.max(0, e.debt - 90) + 0.1;
            e.tenYr += (ten - e.tenYr) * 0.06 + r.nextGaussian() * 0.04;
            e.mortgage = e.tenYr + 1.8;
            double ret = 0.0010 + 0.0012 * (e.gdp - e.potential) - 0.035 * (e.tenYr - prev10) + 0.004 * (e.finShock - e.finShockPrev) + 0.002 * (e.og - e.ogPrev) * 10
                    + r.nextGaussian() * 0.019;
            e.finShockPrev = e.finShock; e.ogPrev = e.og;
            e.stocks *= Math.exp(ret);
            // oil and gas
            e.oil += (e.oilBase - e.oil) * 0.015 + r.nextGaussian() * 1.4 * e.oilBase / 75;
            e.oil = clamp(e.oil, 15, 250);
            e.gas += (gasPrice(e) - e.gas) * 0.25;
            // the prices people feel
            e.groceries += (e.infl + 0.3 + 0.06 * (e.tariff - 2.5) - 0.01 * (e.farm - 100) - e.groceries) * 0.05 + r.nextGaussian() * 0.05;
            e.rent += (e.infl + 1.3 - 0.8 * (e.housing - 1.4) - e.rent) * 0.03 + r.nextGaussian() * 0.03;
            e.insurance += (e.infl + 3.0 - e.insurance) * 0.03 + r.nextGaussian() * 0.06;
            // industries
            e.housing += (1.45 - 0.12 * (e.mortgage - 6.5) + 0.03 * e.og - e.housing) * 0.05 + r.nextGaussian() * 0.01;
            e.manufacturing += (50 + 1.4 * e.og - 0.12 * (e.tariff - g.era.tariff) - e.manufacturing) * 0.08 + r.nextGaussian() * 0.4;
            if (g.era.ai) e.aiInvest *= 1 + 0.0015 + r.nextGaussian() * 0.004;
            e.farm += (100 - 0.8 * Math.max(0, e.tariff - g.era.tariff) - e.farm) * 0.03 + r.nextGaussian() * 0.5;
            e.energyIdx += (100 + 0.2 * (e.oil - e.oilBase) - e.energyIdx) * 0.03 + r.nextGaussian() * 0.3;
            e.tourism += (100 + 2 * e.og - e.tourism) * 0.05 + r.nextGaussian() * 0.4;
            e.sentiment += (sentimentOf(e) - e.sentiment) * 0.2 + r.nextGaussian() * 0.6;
            // the budget
            double cyc = -0.35 * e.og;
            double avgRate = e.interest / Math.max(1, e.debt) * 100;
            avgRate += (e.tenYr - avgRate) * 0.004;
            e.interest = e.debt * avgRate / 100;
            double primary = g.era.deficit - e.debt * 0.032 + e.fiscal;
            double target = primary + cyc + e.interest;
            e.deficit += (target - e.deficit) * 0.05;
            double nom = (e.gdp + e.infl) / 100;
            e.debt += (e.deficit - e.debt * nom) / 52;
            e.nominalGdp *= 1 + nom / 52;
            states(e);
            reports(g);
        }

        /** The business cycle's surprises: about one recession every six years, oil spikes, the odd boom. */
        static void shocks(Game g) {
            Econ e = g.econ;
            Random r = g.rng;
            if (e.calm > 0) { e.calm--; return; }
            double x = r.nextDouble();
            if (x < 0.0035) {
                e.demandShock -= 1.5 + r.nextDouble() * 2; e.calm = 40;
                g.log(NK_ECON, "Layoffs spread as consumers and businesses pull back", "Economists warn the economy may be heading into recession.", 2.0);
            } else if (x < 0.0045) {
                e.finShock -= 2 + r.nextDouble() * 2; e.calm = 52;
                g.log(NK_ECON, "Markets plunge as a big lender fails", "Credit is tightening and stocks fell sharply.", 2.6);
            } else if (x < 0.0075) {
                e.oil *= 1.25 + r.nextDouble() * 0.2; e.oilBase *= 1.08; e.calm = 20;
                g.log(NK_ECON, "Oil jumps on supply fears; gas prices climb", null, 1.6);
            } else if (x < 0.0095) {
                e.demandShock += 0.6 + r.nextDouble() * 0.8; e.calm = 26;
                g.log(NK_ECON, "Business investment surges; economists raise their growth forecasts", null, 1.0);
            }
        }

        static void fomc(Game g) {
            Econ e = g.econ;
            double gap = e.fedTarget - e.fed;
            double step = Math.abs(gap) < 0.15 ? 0 : Math.abs(gap) < 0.6 ? 0.25 : 0.5;
            if (e.finShock < -2 && gap < 0) step = Math.min(Math.abs(gap), 1.0);
            double old = e.fed;
            e.fed = clamp(e.fed + Math.signum(gap) * step, 0.125, 20);
            String chair = e.fedChair == null || e.fedChair.isEmpty() ? "The Fed" : "The Fed under " + e.fedChair;
            if (e.fed > old) g.log(NK_ECON, chair + " raises rates to " + rate(e.fed), "The Federal Reserve lifted its target by " + bp(e.fed - old) + ", citing inflation of " + fmt1(e.infl) + "%.", 1.2);
            else if (e.fed < old) g.log(NK_ECON, chair + " cuts rates to " + rate(e.fed), "The Federal Reserve cut its target by " + bp(old - e.fed) + " as unemployment reached " + fmt1(e.unemp) + "%.", 1.2);
            else g.log(NK_ECON, "Fed holds rates at " + rate(e.fed), null, 0.4);
        }

        static String rate(double r) { double lo = Math.floor(r * 4 + 0.001) / 4; return fmt2(lo - 0.125 < 0 ? 0 : lo - 0.125) + "-" + fmt2(lo + 0.125) + "%"; }

        static String bp(double d) { return Math.round(d * 100) + " basis points"; }

        static void reports(Game g) {
            Econ e = g.econ;
            Random r = g.rng;
            int jd = jobsDay(g);
            if (jd >= 0) {
                double prevU = g.hist.size() >= 4 ? g.hist.get(g.hist.size() - 4)[2] : e.unemp;
                double jobs = 110 + (prevU - e.unemp) * 1100 + 25 * e.og + r.nextGaussian() * 45;
                e.jobsLast = jobs;
                int[] t = dateOf(jd - 7);
                String m = MONTH_FULL[t[1]];
                if (jobs < 0) g.log(NK_ECON, "Economy sheds " + thousands(Math.round(-jobs * 1000)) + " jobs in " + m, "Unemployment is " + fmt1(e.unemp) + "%.", 1.6);
                else g.log(NK_ECON, "Employers add " + thousands(Math.round(jobs * 1000)) + " jobs in " + m + "; unemployment " + fmt1(e.unemp) + "%", null, jobs > 250 ? 1.4 : 0.9);
            }
            if (weekHas(g, dateOf(g.day())[1], 12) || weekHas(g, dateOf(g.day() + 6)[1], 12)) {
                e.cpiReported = e.infl;
                g.log(NK_ECON, "Prices up " + fmt1(e.infl) + "% over the past year", "Groceries are up " + fmt1(e.groceries) + "%, rent " + fmt1(e.rent) + "% and car insurance " + fmt1(e.insurance) + "%.", e.infl > 4 ? 1.6 : 0.7);
            }
            for (int m : new int[]{0, 3, 6, 9}) if (weekHas(g, m, 28)) {
                double q = e.gdp + r.nextGaussian() * 0.8;
                e.gdpReported = q;
                g.log(NK_ECON, q < 0 ? "The economy shrank at a " + fmt1(-q) + "% rate last quarter" : "The economy grew at a " + fmt1(q) + "% rate last quarter", null, q < 0 ? 2 : 0.8);
            }
        }
    }

    static String fmt2(double v) { return String.format("%.2f", v); }

    // =====================================================================
    //  8.  THE ELECTORATE  -  96 voter cells in every state, calibrated to 2020-2024
    // =====================================================================
    static final class Elect {
        static final double NAT24 = 0.4924, NAT20 = 0.5226, NAT22 = 0.4864;   // national two-party Democratic share
        static double[][] cvap = new double[NS][NCELL];       // eligible voters, 2028 composition
        static double[][] cvap24 = new double[NS][NCELL];     // eligible voters, 2024 composition
        static double[][] baseL = new double[NS][NCELL];      // calibrated Democratic logit (2024 environment)
        static double[][] turnB = new double[NS][NCELL];      // base turnout
        static double[][][] ov = new double[NS][NCELL][NO];   // overlay fractions
        static double[][][] ideal = new double[NS][NCELL][NI];
        static double[][][] sal = new double[NS][NCELL][NI];
        static double[][] elast = new double[NS][NCELL];
        static double[] offset = new double[NS], turnScale = new double[NS], target = new double[NS];
        static double[] blocK = new double[NB];
        static double[][] ageSexK = new double[NAGE][NSEX];
        /** 2024 two-party Democratic share by age and sex (AP VoteCast / Pew validated voters). */
        static final double[][] AGESEX_D24 = {{0.42, 0.59}, {0.425, 0.525}, {0.44, 0.52}};
        static double[] stateVotes24 = new double[NS];
        static double[] lean = new double[NS];                // blended D-minus-R lean relative to the nation (points)
        static double[][] dmaShare;                           // [market][state] share of the state's TV households
        static String[] marketName;
        static double[] marketHH;
        static int[][] marketStates;
        static int NM;
        static boolean ready;

        /**
         * Group swings since 2024 that outlast the 2026 midterms (logit), beyond the national mood: Hispanic and young voters
         * swung furthest back toward the Democrats in 2026 (about +28 and +32 points against +15 nationally in early
         * estimates); only part of a midterm swing carries into a presidential year.
         */
        static double swing2026(int c) {
            int b = cellBloc(c), a = cellAge(c);
            double v = 0;
            if (hispanic(b)) v += 0.15;
            if (b == B_BLK) v += 0.04;
            if (b == B_ASN) v += 0.06;
            if (a == AGE_YOUNG) v += 0.11;
            if (b == B_WOT_C || b == B_WCA_C) v += 0.03;
            return v;
        }

        static synchronized void init() {
            if (ready) return;
            for (int s = 0; s < NS; s++) compose(s);
            calibrate();
            markets();
            ready = true;
        }

        static double[] blocShares(StateDef d, int years) {
            double W = d.white, B = d.black, H = d.hisp;
            double A = d.asian, N = d.nativeAm, O = Math.max(0.6, d.other - A - N);
            if (d.ab.equals("HI")) { A = Math.min(A, d.other - 1); O = Math.max(2, d.other - A - N); }
            W *= Math.pow(1 - 0.0075, years); H *= Math.pow(1.02, years); A *= Math.pow(1.022, years); O *= Math.pow(1.03, years); B *= Math.pow(1.002, years);
            // share of each group that is an adult citizen
            double wc = W * 0.80, bc = B * 0.70, hc = H * 0.43, ac = A * 0.55, nc = N * 0.66, oc = O * 0.50;
            double tot = wc + bc + hc + ac + nc + oc;
            wc /= tot; bc /= tot; hc /= tot; ac /= tot; nc /= tot; oc /= tot;
            double adj = 1.05;
            double wev = Math.min(0.55 * wc, d.wev / 100 * adj), wca = Math.min(0.5 * wc, d.wca / 100 * adj);
            double jew = Math.min(0.2 * wc, d.jew / 100 * 1.1), lds = Math.min(0.75 * wc, d.lds / 100 * 0.9 * adj), arb = Math.min(0.1 * wc, d.arab / 100);
            double wot = wc - wev - wca - jew - lds - arb;
            if (wot < 0.05 * wc) {
                double f = (wc * 0.95) / (wev + wca + jew + lds + arb);
                wev *= f; wca *= f; jew *= f; lds *= f; arb *= f; wot = 0.05 * wc;
            }
            double cw = clamp(1 - d.nocolW / 100 + 0.012 * years, 0.12, 0.85);
            double fot = Math.min(0.85, cw + cw * 0.4 * wev / Math.max(0.01, wot));
            double[] sh = new double[NB];
            sh[B_WEV_NC] = wev * (1 - 0.6 * cw); sh[B_WEV_C] = wev * 0.6 * cw;
            sh[B_WCA_NC] = wca * (1 - cw); sh[B_WCA_C] = wca * cw;
            sh[B_WOT_NC] = wot * (1 - fot); sh[B_WOT_C] = wot * fot;
            sh[B_JEW] = jew; sh[B_LDS] = lds; sh[B_ARB] = arb;
            sh[B_BLK] = bc;
            sh[B_HCU] = hc * d.cuban; sh[B_HPR] = hc * d.pr; sh[B_HMX] = hc * (1 - d.cuban - d.pr);
            sh[B_ASN] = ac; sh[B_NAT] = nc; sh[B_OTH] = oc;
            double t = 0;
            for (double v : sh) t += v;
            for (int b = 0; b < NB; b++) sh[b] /= t;
            return sh;
        }

        static void compose(int s) {
            StateDef d = STATES[s];
            double grow = d.region.equals("S") || d.region.equals("W") ? 1.25 : 0.7;
            double cv24 = d.cvap * (1 + 0.007 * 8 * grow), cv28 = d.cvap * (1 + 0.007 * 12 * grow);
            double[] sh24 = blocShares(d, 10), sh28 = blocShares(d, 14);
            double fy = clamp(d.u29 / 39.9, 0.7, 1.4), fo = clamp(d.o65 / 14.9, 0.6, 1.5);
            for (int b = 0; b < NB; b++) {
                double[] ag = {BLOC_AGE[b][0] * fy, BLOC_AGE[b][1], BLOC_AGE[b][2] * fo};
                double at = ag[0] + ag[1] + ag[2];
                for (int a = 0; a < NAGE; a++) {
                    double men = b == B_BLK ? 0.45 : b == B_ARB ? 0.52 : 0.48;
                    for (int x = 0; x < NSEX; x++) {
                        int c = cell(b, a, x);
                        double f = ag[a] / at * (x == 0 ? men : 1 - men);
                        cvap24[s][c] = cv24 * sh24[b] * f;
                        cvap[s][c] = cv28 * sh28[b] * f;
                        overlays(d, s, c, b, a, x);
                        opinions(s, c, b, a, x);
                        elast[s][c] = BLOC_ELAST[b] * AGE_ELAST[a];
                    }
                }
            }
        }

        static void overlays(StateDef d, int s, int c, int b, int a, int x) {
            double[] o = ov[s][c];
            double rb, ub;
            switch (b) {
                case B_WEV_NC: case B_WEV_C: rb = 1.5; ub = 0.4; break;
                case B_WCA_NC: case B_WCA_C: rb = 1.0; ub = 0.7; break;
                case B_WOT_NC: rb = 1.3; ub = 0.6; break;
                case B_WOT_C: rb = 0.75; ub = 1.25; break;
                case B_BLK: rb = 0.55; ub = 1.8; break;
                case B_HMX: rb = 0.45; ub = 1.5; break;
                case B_HCU: rb = 0.2; ub = 1.3; break;
                case B_HPR: rb = 0.3; ub = 1.8; break;
                case B_ASN: rb = 0.2; ub = 1.6; break;
                case B_NAT: rb = 2.0; ub = 0.5; break;
                case B_JEW: rb = 0.15; ub = 1.6; break;
                case B_LDS: rb = 1.0; ub = 0.6; break;
                case B_ARB: rb = 0.2; ub = 1.6; break;
                default: rb = 0.6; ub = 1.3; break;
            }
            double rural = clamp(d.rural / 100 * rb * (a == 0 ? 0.85 : a == 2 ? 1.15 : 1), 0, 0.92);
            double urban = clamp(d.urban / 100 * ub * (a == 0 ? 1.3 : a == 2 ? 0.8 : 1), 0, 0.97 - rural);
            o[O_RURAL] = rural; o[O_URBAN] = urban; o[O_SUBURB] = Math.max(0, 1 - rural - urban);
            boolean col = college(b);
            o[O_UNION] = clamp(d.union / 100 * (col ? 0.8 : 1.15) * (b == B_BLK ? 1.3 : hispanic(b) ? 0.9 : 1) * (a == 0 ? 0.6 : a == 1 ? 1.1 : 1.0), 0, 0.7);
            o[O_VET] = clamp(d.vet / 100 * 1.4 * (x == 0 ? 1.8 : 0.35) * (a == 0 ? 0.4 : a == 1 ? 1.0 : 1.8), 0, 0.6);
            double gb = b <= B_WEV_C ? 1.3 : b == B_WOT_NC ? 1.15 : b == B_WOT_C ? 0.75 : b == B_WCA_NC || b == B_WCA_C ? 1.0 : b == B_LDS ? 1.1 : b == B_NAT ? 1.0
                    : b == B_JEW || b == B_ARB ? 0.3 : b == B_ASN ? 0.4 : 0.6;
            o[O_GUN] = clamp(d.gun / 100 * gb * (x == 0 ? 1.25 : 0.8) * (0.8 + rural * 0.6), 0, 0.92);
            o[O_LGBT] = clamp(d.lgbt / 100 * (a == 0 ? 2.6 : a == 1 ? 0.75 : 0.3) * (b <= B_WEV_C ? 0.3 : b == B_LDS ? 0.4 : 1), 0, 0.4);
            o[O_STUDENT] = a == 0 ? (col ? 0.55 : 0.28) : 0;
            o[O_PARENT] = a == 1 ? 0.48 : a == 0 ? 0.15 : 0.02;
        }

        static void opinions(int s, int c, int b, int a, int x) {
            double p = BLOC_D24[b];
            double[] id = ideal[s][c], sl = sal[s][c];
            double tot = 0;
            for (int i = 0; i < NI; i++) {
                double v = IDEAL_R[i] + (IDEAL_D[i] - IDEAL_R[i]) * p + blocIdealShift(b, i) + AGE_IDEAL[a][i] + SEX_IDEAL[x][i];
                double m = ISSUE_SAL[i] * blocSalMul(b, i) * AGE_SAL[a][i] * SEX_SAL[x][i];
                for (int o = 0; o < NO; o++) {
                    double f = ov[s][c][o];
                    v += f * OV_IDEAL[o][i];
                    m *= 1 + f * (OV_SAL[o][i] - 1);
                }
                id[i] = clamp(v, -1, 1);
                sl[i] = m;
                tot += m;
            }
            for (int i = 0; i < NI; i++) sl[i] /= tot;
        }

        static double cellLogitRaw(int s, int c) {
            int b = cellBloc(c), a = cellAge(c), x = cellSex(c);
            double mean = (ageSexK[a][0] + ageSexK[a][1]) / 2;
            double adj = mean + (ageSexK[a][x] - mean) * BLOC_GENDER[b];
            double l = blocK[b] + adj;
            for (int o = 0; o < NO; o++) l += ov[s][c][o] * OV_LEAN[o];
            return l;
        }

        static void calibrate() {
            double[] nat = {0, 0, 0};
            // 2026: each state's statewide margin relative to the states that voted, centred so the 2026 term moves
            // states relative to one another without shifting the group as a whole
            double w26 = 0, sum26 = 0, sum24 = 0;
            for (int s = 0; s < NS; s++) {
                if (Double.isNaN(m26(s))) continue;
                StateDef d = STATES[s];
                w26 += d.t24; sum26 += d.t24 * m26(s); sum24 += d.t24 * (d.d24 / (d.d24 + d.r24) - NAT24) * 200;
            }
            double nat26 = w26 > 0 ? (sum26 - sum24) / w26 : 0;
            for (int s = 0; s < NS; s++) {
                StateDef d = STATES[s];
                double sh24 = d.d24 / (d.d24 + d.r24), sh20 = d.d20 / (d.d20 + d.r20), sh22 = clamp(0.5 + d.m22 / 200, 0.05, 0.95);
                double rel = 0.65 * (sh24 - NAT24) + 0.25 * (sh20 - NAT20) + 0.10 * (sh22 - NAT22);
                if (!Double.isNaN(m26(s))) rel = 0.60 * (sh24 - NAT24) + 0.22 * (sh20 - NAT20) + 0.08 * (sh22 - NAT22) + 0.10 * (m26(s) - nat26) / 200;
                target[s] = clamp(NAT24 + rel, 0.03, 0.97);
                lean[s] = rel * 200;
                stateVotes24[s] = d.t24;
            }
            for (int b = 0; b < NB; b++) blocK[b] = logit(BLOC_D24[b]);
            for (int a = 0; a < NAGE; a++) for (int x = 0; x < NSEX; x++) ageSexK[a][x] = AGESEX_ADJ[a][x];
            // targets: the national result must equal 2024 exactly (the blend shifts relative positions only)
            double tw0 = 0, tv0 = 0;
            for (int s = 0; s < NS; s++) { tw0 += stateVotes24[s] * target[s]; tv0 += stateVotes24[s]; }
            double shift = NAT24 - tw0 / tv0;
            for (int s = 0; s < NS; s++) { target[s] = clamp(target[s] + shift, 0.03, 0.97); lean[s] = (target[s] - NAT24) * 200; }
            // turnout scale per state to reproduce 2024 total votes
            for (int s = 0; s < NS; s++) {
                double lo = 0.3, hi = 2.0;
                for (int it = 0; it < 40; it++) {
                    double mid = (lo + hi) / 2, v = 0;
                    for (int c = 0; c < NCELL; c++) v += cvap24[s][c] * Math.min(0.97, baseTurn(c) * mid);
                    if (v > stateVotes24[s]) hi = mid; else lo = mid;
                }
                turnScale[s] = (lo + hi) / 2;
                for (int c = 0; c < NCELL; c++) turnB[s][c] = Math.min(0.97, baseTurn(c) * turnScale[s]);
            }
            // raking: state offsets reproduce the blended state results, bloc intercepts reproduce national bloc votes
            for (int pass = 0; pass < 30; pass++) {
                for (int s = 0; s < NS; s++) {
                    double lo = -4, hi = 4;
                    for (int it = 0; it < 40; it++) {
                        double mid = (lo + hi) / 2, dv = 0, tv = 0;
                        for (int c = 0; c < NCELL; c++) {
                            double w = cvap24[s][c] * turnB[s][c];
                            dv += w * logistic(cellLogitRaw(s, c) + mid * BLOC_STATE_W[cellBloc(c)]);
                            tv += w;
                        }
                        if (dv / tv > target[s]) hi = mid; else lo = mid;
                    }
                    offset[s] = (lo + hi) / 2;
                }
                double[] dv = new double[NB], tv = new double[NB];
                for (int s = 0; s < NS; s++)
                    for (int c = 0; c < NCELL; c++) {
                        int b = cellBloc(c);
                        double w = cvap24[s][c] * turnB[s][c];
                        dv[b] += w * logistic(cellLogitRaw(s, c) + offset[s] * BLOC_STATE_W[b]);
                        tv[b] += w;
                    }
                for (int b = 0; b < NB; b++) blocK[b] += 0.7 * (logit(BLOC_D24[b]) - logit(dv[b] / tv[b]));
                // age and sex
                double[][] ad = new double[NAGE][NSEX], at = new double[NAGE][NSEX];
                for (int s = 0; s < NS; s++)
                    for (int c = 0; c < NCELL; c++) {
                        double w = cvap24[s][c] * turnB[s][c];
                        ad[cellAge(c)][cellSex(c)] += w * logistic(cellLogitRaw(s, c) + offset[s] * BLOC_STATE_W[cellBloc(c)]);
                        at[cellAge(c)][cellSex(c)] += w;
                    }
                for (int a = 0; a < NAGE; a++) for (int x = 0; x < NSEX; x++) ageSexK[a][x] += 0.7 * (logit(AGESEX_D24[a][x]) - logit(ad[a][x] / at[a][x]));
            }
            for (int s = 0; s < NS; s++)
                for (int c = 0; c < NCELL; c++) baseL[s][c] = cellLogitRaw(s, c) + offset[s] * BLOC_STATE_W[cellBloc(c)];
        }

        static double baseTurn(int c) {
            int b = cellBloc(c), a = cellAge(c), x = cellSex(c);
            return BLOC_TURN[b] * AGE_TURN[a] * (x == 1 ? 1.03 : 0.97);
        }

        /** Builds the TV market table: real DMAs plus a 'rest of state' market where coverage is incomplete. */
        static void markets() {
            List<String> names = new ArrayList<String>();
            List<Double> hh = new ArrayList<Double>();
            List<double[]> shares = new ArrayList<double[]>();
            double[] cov = new double[NS];
            for (int m = 0; m < DMA_NAMES.length; m++) {
                double[] sh = new double[NS];
                for (String p : DMA_STATES[m].split(" ")) {
                    String[] kv = p.split(":");
                    int s = stateIdx(kv[0]);
                    double h = DMA_HH[m] * Double.parseDouble(kv[1]);
                    sh[s] = h / STATE_TVHH[s];
                    cov[s] += sh[s];
                }
                names.add(DMA_NAMES[m]); hh.add(DMA_HH[m]); shares.add(sh);
            }
            for (int s = 0; s < NS; s++) {
                if (cov[s] > 1) for (double[] sh : shares) sh[s] /= cov[s];
                else if (cov[s] < 0.93) {
                    double[] sh = new double[NS];
                    sh[s] = 1 - cov[s];
                    names.add("Rest of " + STATES[s].name);
                    hh.add(STATE_TVHH[s] * (1 - cov[s]));
                    shares.add(sh);
                }
            }
            NM = names.size();
            marketName = names.toArray(new String[0]);
            marketHH = new double[NM];
            dmaShare = new double[NM][];
            marketStates = new int[NM][];
            for (int m = 0; m < NM; m++) {
                marketHH[m] = hh.get(m);
                dmaShare[m] = shares.get(m);
                int k = 0;
                for (int s = 0; s < NS; s++) if (dmaShare[m][s] > 0.001) k++;
                marketStates[m] = new int[k];
                k = 0;
                for (int s = 0; s < NS; s++) if (dmaShare[m][s] > 0.001) marketStates[m][k++] = s;
            }
        }

        /** Markets covering a state, largest first. */
        static List<Integer> marketsIn(int s) {
            List<Integer> l = new ArrayList<Integer>();
            for (int m = 0; m < NM; m++) if (dmaShare[m][s] > 0.001) l.add(m);
            final int ss = s;
            Collections.sort(l, new Comparator<Integer>() { public int compare(Integer a, Integer b) { return Double.compare(dmaShare[b][ss], dmaShare[a][ss]); } });
            return l;
        }
    }
    // =====================================================================
    //  8.  APPROVAL
    //  National approval follows the economy, prices, events and the honeymoon, squeezed by partisanship.
    //  Below it, the electorate's 96 voter cells in every state give approval by state and by group.
    // =====================================================================
    static final class Groups {
        static float[][][] mem;            // [group][state][cell]
        static volatile boolean ready;

        static synchronized void init() {
            if (ready) return;
            Elect.init();
            mem = new float[NG][NS][NCELL];
            for (int gr = 0; gr < NG; gr++) {
                int o = GROUP_OV[gr];
                for (int s = 0; s < NS; s++) for (int c = 0; c < NCELL; c++) mem[gr][s][c] = (float) (o >= 0 ? Elect.ov[s][c][o] : inGroup(gr, c));
            }
            ready = true;
        }
    }

    /** Groups shown in the approval tables. */
    static final int[] SHOW_GROUPS = {0, 1, 4, 5, 8, 9, 10, 12, 16, 6, 7, 21, 22, 23, 24, 25, 29};

    static final class Approval {
        static double squash(Game g, double t) {
            double pol = g.era.polarization;
            double lo = 50 - (22 + 18 * (1 - pol)), hi = 50 + (12 + 30 * (1 - pol));
            double c = (lo + hi) / 2, s = (hi - lo) / 4;
            return lo + (hi - lo) * logistic((t - c) / s);
        }

        static double issueWeighted(Game g) {
            double v = 0, w = 0;
            for (int i = 0; i < NPI; i++) { v += g.issueSal[i] * g.issueAdj[i]; w += g.issueSal[i]; }
            return w > 0 ? v / w : 0;
        }

        /** Where approval is heading, before partisanship squeezes it. */
        static double target(Game g) {
            Econ e = g.econ;
            double t = g.era.approvalBase + 0.8 * (e.gdp - 2) - 1.6 * (e.unemp - 4.5) - 1.5 * (e.infl - 2.5) - 3.0 * (e.gas / e.priceLevel - 3.1)
                    + 0.06 * (e.sentiment - 80) + g.perf + g.honeymoon + g.rally - g.scandal + 1.5 * issueWeighted(g) + personal(g);
            if (g.set.difficulty == 0) t += 2;
            if (g.set.difficulty == 2) t -= 2;
            return t;
        }

        /** What the president is like: crossover appeal and charm help; scandal baggage hurts. */
        static double personal(Game g) {
            Cand p = g.pres;
            if (p == null) return 0;
            return (p.a[A_XOV] - 60) * 0.08 + (p.a[A_CHA] - 60) * 0.03 - (p.a[A_SCA] - 40) * 0.03;
        }

        static void init(Game g) {
            Era er = g.era;
            for (int i = 0; i < NPI; i++) g.issueSal[i] = 0.5;
            if (er.issues != null) for (String p : er.issues.split(" ")) { String[] kv = p.split("="); g.issueSal[Integer.parseInt(kv[0])] = Double.parseDouble(kv[1]); }
            if (!er.ai) g.issueSal[PI_TECH] = 0.05;
            g.honeymoon = 5 + (g.set.scenario == 4 ? 3 : 0);
            if (g.set.scenario == 2) g.rally = 6;
            double t = target(g);
            g.approval = squash(g, t);
            if (!Double.isNaN(g.set.cApproval)) { g.approval = g.set.cApproval; g.perf = g.approval - squash(g, t); }
            g.approval0 = g.approval;
            for (int i = 0; i < NPI; i++) g.command[i] = 30 + (g.pres != null ? g.pres.a[A_INT] * 0.2 : 10);
            issues(g);
            cells(g);
        }

        static void week(Game g) {
            Random r = g.rng;
            double t = target(g);
            g.approval += (squash(g, t) - g.approval) * 0.07 + r.nextGaussian() * 0.35;
            g.approval = clamp(g.approval, 8, 92);
            g.honeymoon *= 0.975;
            g.rally *= 0.95;
            g.perf *= 0.96;
            g.scandal *= 0.985;
            for (int i = 0; i < NPI; i++) { g.issueAdj[i] *= 0.97; g.issueSal[i] += (base(g, i) - g.issueSal[i]) * 0.02; }
            for (int s = 0; s < NS; s++) g.stateBump[s] *= 0.9;
            for (int gr = 0; gr < NG; gr++) g.groupAdj[gr] *= 0.99;
            issues(g);
            cells(g);
        }

        static double base(Game g, int i) {
            Era er = g.era;
            if (er.issues != null) for (String p : er.issues.split(" ")) { String[] kv = p.split("="); if (Integer.parseInt(kv[0]) == i) return Double.parseDouble(kv[1]); }
            return 0.5;
        }

        static void issues(Game g) {
            Econ e = g.econ;
            double a = g.approval;
            for (int i = 0; i < NPI; i++) {
                double v = a + 4 * g.issueAdj[i] + (g.command[i] - 50) * 0.04;
                if (i == PI_ECON) v += 1.6 * (e.gdp - 2) - 1.5 * (e.unemp - 4.5) + 0.08 * (e.sentiment - 80);
                if (i == PI_PRICES) v += -2.5 * (e.infl - 2.5) - 5 * (e.gas / e.priceLevel - 3.1) - 0.6 * (e.groceries - 2.5) - 0.4 * (e.rent - 3.5) - 4;
                if (i == PI_JOBS) v += -2.2 * (e.unemp - 4.5) + 0.01 * (e.jobsLast - 150);
                g.issueApp[i] = clamp(v, 10, 88);
            }
        }

        /** Approval in each voter cell, shifted so the nation matches the headline number. */
        static void cells(Game g) {
            if (!Groups.ready) return;
            double sign = g.party == REP ? -1 : 1;
            double k = (g.party == DEM || g.party == REP ? 0.8 + 0.6 * g.era.polarization : 0.35);
            Econ e = g.econ;
            double[] local = new double[NS];
            for (int s = 0; s < NS; s++) local[s] = -0.07 * (e.stUnemp[s] - e.unemp) - 0.10 * (e.stGas[s] - e.gas) + 0.06 * g.stateBump[s];
            double lo = -6, hi = 6, off = g.offset;
            for (int it = 0; it < 30; it++) {
                double v = national(g, sign, k, off, local);
                if (Math.abs(v - g.approval / 100) < 0.0005) break;
                if (v < g.approval / 100) lo = off; else hi = off;
                off = (lo + hi) / 2;
            }
            g.offset = off;
            double[] gs = new double[NG], gw = new double[NG];
            for (int s = 0; s < NS; s++) {
                double num = 0, den = 0;
                for (int c = 0; c < NCELL; c++) {
                    double w = Elect.cvap24[s][c];
                    if (w <= 0) continue;
                    double l = k * sign * Elect.baseL[s][c] + off + local[s];
                    for (int gr = 0; gr < NG; gr++) if (g.groupAdj[gr] != 0) l += g.groupAdj[gr] * Groups.mem[gr][s][c];
                    double p = logistic(l);
                    num += w * p; den += w;
                    for (int gr = 0; gr < NG; gr++) { double m = Groups.mem[gr][s][c]; if (m > 0) { gs[gr] += w * m * p; gw[gr] += w * m; } }
                }
                g.stateApp[s] = den > 0 ? num / den * 100 : g.approval;
            }
            for (int gr = 0; gr < NG; gr++) g.groupApp[gr] = gw[gr] > 0 ? gs[gr] / gw[gr] * 100 : g.approval;
        }

        static double national(Game g, double sign, double k, double off, double[] local) {
            double num = 0, den = 0;
            for (int s = 0; s < NS; s++) for (int c = 0; c < NCELL; c++) {
                double w = Elect.cvap24[s][c];
                if (w <= 0) continue;
                double l = k * sign * Elect.baseL[s][c] + off + local[s];
                for (int gr = 0; gr < NG; gr++) if (g.groupAdj[gr] != 0) l += g.groupAdj[gr] * Groups.mem[gr][s][c];
                num += w * logistic(l); den += w;
            }
            return num / den;
        }
    }
    // =====================================================================
    //  9.  THE WEEK
    //  The president's calendar plays out slot by slot; then the economy, opinion and the news move.
    // =====================================================================
    static final class Week {
        static int dc() { return stateIdx("DC"); }

        /** Flight time from Washington, in hours. */
        static double flight(int s) {
            if (s < 0) return 0;
            String ab = STATES[s].ab;
            if (ab.equals("HI")) return 10.5;
            if (ab.equals("AK")) return 8.5;
            int d = dc();
            double dx = STATES[s].cx - STATES[d].cx, dy = STATES[s].cy - STATES[d].cy;
            double miles = Math.sqrt(dx * dx + dy * dy) * 2.9;
            return miles < 60 ? 0.3 : 0.6 + miles / 480;
        }

        static double ageFactor(Game g) { return 1 + Math.max(0, g.presAge() - 60) * 0.012; }

        /** A routine week: policy, remarks, a trip, rest on the weekend. */
        static void routine(Game g) {
            Arrays.fill(g.act, AC_FREE);
            Arrays.fill(g.arg, -1);
            int top = topIssue(g);
            int[] pri = priorities(g);
            for (int d = 0; d < 5; d++) {
                g.act[d * 3] = AC_POLICY; g.arg[d * 3] = pri[d % pri.length];
                g.act[d * 3 + 1] = d == 1 ? AC_REMARKS : d == 2 ? AC_CONGRESS : d == 3 ? AC_TRIP : AC_CEREMONY;
                if (d == 1) g.arg[d * 3 + 1] = top;
                if (d == 3) g.arg[d * 3 + 1] = swingState(g);
                g.act[d * 3 + 2] = d == 4 ? AC_FAMILY : AC_FREE;
            }
            g.act[15] = AC_EXERCISE; g.act[16] = AC_FAMILY; g.act[17] = AC_REST;
            g.act[18] = AC_REST; g.act[19] = AC_FAMILY; g.act[20] = AC_FREE;
        }

        /** Issues in order of how much attention they need: salience times the room to improve. */
        static int[] priorities(Game g) {
            Integer[] idx = new Integer[NPI];
            for (int i = 0; i < NPI; i++) idx[i] = i;
            final Game gg = g;
            Arrays.sort(idx, (a, b) -> Double.compare(gg.issueSal[b] * (62 - gg.issueApp[b]), gg.issueSal[a] * (62 - gg.issueApp[a])));
            int[] out = new int[4];
            for (int i = 0; i < 4; i++) out[i] = idx[i];
            return out;
        }

        static int topIssue(Game g) {
            int best = 0;
            for (int i = 1; i < NPI; i++) if (g.issueSal[i] * (60 - g.issueApp[i]) > g.issueSal[best] * (60 - g.issueApp[best])) best = i;
            return best;
        }

        static int swingState(Game g) {
            int best = stateIdx("PA");
            double bv = -1;
            for (int s = 0; s < NS; s++) {
                double v = STATES[s].ev * (1 - Math.abs(g.stateApp[s] - 47) / 25) - g.stateBump[s] * 3;
                if (v > bv) { bv = v; best = s; }
            }
            return best;
        }

        /** Hours of sleep the evening slots allow. */
        static double sleepOf(Game g) {
            double h = 0;
            for (int d = 0; d < 7; d++) {
                int a = g.act[d * 3 + 2];
                double v = a == AC_REST || a == AC_FAMILY || a == AC_CAMPDAVID ? 7.4 : a == AC_FREE || a == AC_EXERCISE ? 6.8 : a == AC_WORKLATE ? 4.8 : a == AC_TRIP || a == AC_FUNDRAISER ? 5.6 : 6.2;
                int m = g.act[d * 3];
                if (isTrip(m) && g.arg[d * 3] >= 0 && flight(g.arg[d * 3]) > 3.5) v -= 1;
                h += v;
            }
            return h / 7;
        }

        /** Stamina the week will cost, before sleep. */
        static double cost(Game g) {
            double c = 0;
            for (int i = 0; i < NSLOT; i++) {
                double v = AC_STAMINA[g.act[i]];
                if (isTrip(g.act[i]) && g.arg[i] >= 0) v -= Math.min(6, flight(g.arg[i]) * 0.9);
                c += v < 0 ? v * ageFactor(g) : v;
            }
            return c;
        }

        static double projectedStamina(Game g) {
            return clamp(g.stamina + cost(g) + 4 + (sleepOf(g) - 6.5) * 3 - g.stress / 25 + (g.fitness - 50) / 25, 0, 100);
        }

        static void resolve(Game g) {
            Random r = g.rng;
            Cand p = g.pres;
            int trips = 0, funds = 0, campDavid = 0;
            double speech = p != null ? p.a[A_SPE] : 60, cha = p != null ? p.a[A_CHA] : 60;
            for (int i = 0; i < NSLOT; i++) {
                int a = g.act[i], arg = g.arg[i];
                g.stress = clamp(g.stress + AC_STRESS[a], 0, 100);
                switch (a) {
                    case AC_POLICY:
                        if (arg >= 0) { g.command[arg] = Math.min(100, g.command[arg] + 5 * (1 - g.command[arg] / 110)); g.issueAdj[arg] = Math.min(1.5, g.issueAdj[arg] + 0.02 * (1 - g.issueAdj[arg] / 1.5)); }
                        break;
                    case AC_REMARKS:
                        if (arg >= 0) {
                            g.issueSal[arg] = Math.min(2, g.issueSal[arg] + 0.08);
                            g.issueAdj[arg] = clamp(g.issueAdj[arg] + (0.04 * speech / 70) * (1 - Math.abs(g.issueAdj[arg]) / 1.5) + r.nextGaussian() * 0.02, -1.5, 1.5);
                            g.perf += 0.012;
                        }
                        break;
                    case AC_TRIP:
                        if (arg >= 0) {
                            trips++;
                            g.stateBump[arg] += 2.2 * cha / 70;
                            g.perf += 0.015;
                            g.location = arg;
                            if (r.nextDouble() < 0.5) g.log(NK_INFO, g.pres.last + " visits " + STATES[arg].name, null, 0.4);
                        }
                        break;
                    case AC_FUNDRAISER:
                        funds++;
                        double raised = (1.5 + r.nextDouble() * 2) * (p != null ? p.a[A_BIG] : 60) / 70;
                        g.funds += raised;
                        break;
                    case AC_CEREMONY: g.perf += 0.012; break;
                    case AC_CONGRESS: g.hill = Math.min(100, g.hill + 1.2 * (p != null ? p.a[A_EST] : 60) / 70); Hill.meeting(g); break;
                    case AC_FAMILY: g.perf += 0.004; break;
                    case AC_EXERCISE: g.fitness = Math.min(100, g.fitness + 1.2); break;
                    case AC_CAMPDAVID: campDavid++; break;
                    case AC_CAMPAIGN: if (arg >= 0 && g.cong != null) { g.cong.campaign[arg] += cha / 70; g.stateBump[arg] += 0.8; g.location = arg; trips++; } break;
                    case AC_WORKLATE: { int t = topIssue(g); g.command[t] = Math.min(100, g.command[t] + 1.5); break; }
                    default: break;
                }
            }
            if (funds > 3) { g.perf -= 0.06 * (funds - 3); if (funds >= 5) g.log(NK_BAD, "Critics count " + funds + " fundraisers in a week for " + g.pres.last, null, 0.7); }
            if (trips == 0) g.location = Week.dc();
            g.fitness = Math.max(0, g.fitness - 0.4);
            // sleep and stamina
            double sl = sleepOf(g);
            g.sleep = sl;
            g.stamina = clamp(g.stamina + cost(g) + 4 + (sl - 6.5) * 3 - g.stress / 25 + (g.fitness - 50) / 25, 0, 100);
            if (sl < 5.5) g.stress = clamp(g.stress + 3, 0, 100);
            g.stress += 9.5 + (50 - g.approval) * 0.08 + (g.stamina < 30 ? 3 : 0);       // the job itself
            g.stress = clamp(g.stress * 0.85, 0, 100);
            health(g);
            Economy.week(g);
            Approval.week(g);
            if (g.cong != null) {
                Hill.drift(g);
                Legis.week(g);
                Budget.week(g);
                Midterms.week(g);
                g.capital = clamp(g.capital + 0.6 + (g.approval - 45) * 0.05 + (g.honeymoon > 1 ? 0.5 : 0), 0, 100);
            }
            record(g);
            g.week++;
            if (g.week >= g.termWeeks) g.over = true;
            g.dirty = true;
        }

        static void health(Game g) {
            Random r = g.rng;
            int age = g.presAge();
            g.health = clamp(g.health + (g.stamina - 50) * 0.01 - (g.stress - 40) * 0.01 + (g.fitness - 50) * 0.005 - Math.max(0, age - 70) * 0.01, 20, 100);
            if (g.sick > 0) { g.sick--; g.stamina = Math.min(g.stamina, 55); return; }
            double risk = 0.004 + 0.0007 * Math.max(0, age - 60) + 0.0006 * Math.max(0, g.stress - 60) + (g.stamina < 20 ? 0.02 : 0) + (100 - g.health) * 0.0004;
            if (r.nextDouble() < risk) {
                if (r.nextDouble() < 0.8) {
                    g.sick = 1 + r.nextInt(2);
                    g.stamina = Math.max(10, g.stamina - 15);
                    g.log(NK_INFO, "White House: the president has a cold and is working from the residence", null, 0.6);
                } else {
                    g.sick = 2;
                    g.stamina = Math.max(10, g.stamina - 30);
                    g.perf -= 1.0;
                    g.log(NK_BAD, "President " + g.pres.last + " spends a night at Walter Reed for tests", "The White House physician says the president is doing well and will be back at work within days.", 2.2);
                }
            }
        }

        static void record(Game g) {
            Econ e = g.econ;
            g.hist.add(new double[]{g.week, g.approval, e.unemp, e.infl, e.gdp, e.stocks, e.gas, e.deficit, e.debt, e.fed, e.sentiment, g.stamina, g.stress, e.tenYr, g.issueApp[PI_ECON], g.issueApp[PI_PRICES]});
        }
    }

    // columns in Game.hist
    static final int H_WEEK = 0, H_APP = 1, H_UNEMP = 2, H_INFL = 3, H_GDP = 4, H_STOCKS = 5, H_GAS = 6, H_DEF = 7, H_DEBT = 8, H_FED = 9, H_SENT = 10,
            H_STAMINA = 11, H_STRESS = 12, H_TEN = 13, H_APP_ECON = 14, H_APP_PRICES = 15;
    // =====================================================================
    //  10.  ERAS AND SCENARIOS
    //  Each era is the country on an inauguration day: Congress, the economy, the leaders and the mood.
    // =====================================================================
    static final class Era implements Serializable {
        private static final long serialVersionUID = 1L;
        int year;
        String name, blurb, predecessor, winnerId;
        boolean fictional;                  // a projected future rather than a real date
        int houseD = -1, senateD = -1;      // Democratic seats on day one (-1: set by who won)
        double approvalBase = 45, polarization = 0.85;
        double unemp, infl, gdp, fed, tenYr, stocks, oil, gas, debt, deficit, nominalGdp, potential = 1.9, nairu = 4.3, priceLevel = 1, tariff = 2.5;
        String fedChair;
        String speakerD, speakerR, leaderD, leaderR, houseLeaderD, houseLeaderR;   // who leads each party in each chamber
        String issues;                      // salience weights: "index=weight ..."
        boolean ai = true;
        String midSeats;                    // Senate seats up at the midterm, "AB:D" for the party holding each
    }

    static final List<Era> ERAS = new ArrayList<Era>();

    static Era era(int year) { for (Era e : ERAS) if (e.year == year) return e; return ERAS.get(ERAS.size() - 1); }

    static {
        Era e = new Era();
        e.year = 2025; e.name = "The real present";
        e.blurb = "January 20, 2025. Inflation has cooled but prices are still high, Republicans hold both chambers by a few seats, wars grind on in Ukraine and Gaza, and AI is reshaping the economy.";
        e.predecessor = "Joe Biden"; e.winnerId = "trump";
        e.houseD = 215; e.senateD = 47;
        e.unemp = 4.0; e.infl = 3.0; e.gdp = 2.5; e.fed = 4.33; e.tenYr = 4.6; e.stocks = 5950; e.oil = 76; e.gas = 3.10; e.debt = 98; e.deficit = 6.2; e.nominalGdp = 29.7;
        e.potential = 1.9; e.nairu = 4.2; e.tariff = 2.4;
        e.fedChair = "Jerome Powell";
        e.speakerD = "Hakeem Jeffries"; e.speakerR = "Mike Johnson"; e.leaderD = "Chuck Schumer"; e.leaderR = "John Thune"; e.houseLeaderD = "Hakeem Jeffries"; e.houseLeaderR = "Mike Johnson";
        e.midSeats = "CO:D DE:D GA:D IL:D MA:D MI:D MN:D NH:D NJ:D NM:D OR:D RI:D VA:D AL:R AK:R AR:R ID:R IA:R KS:R KY:R LA:R ME:R MS:R MT:R NE:R NC:R OK:R SC:R SD:R TN:R TX:R WV:R WY:R OH:R FL:R";
        e.issues = "0=1.0 1=1.4 2=0.7 3=0.7 4=1.1 5=0.5 6=0.6 7=0.4 8=0.7 9=0.4 10=0.3 11=0.5 12=0.3 13=0.5";
        ERAS.add(e);

        e = new Era();
        e.year = 2029; e.name = "After Battleground 2028"; e.fictional = true;
        e.blurb = "January 20, 2029, in the world of Battleground 2028. Democrats won the 2026 midterms in a landslide; now a new president takes office after the 2028 election. A projection, not a forecast.";
        e.predecessor = "Donald Trump";
        e.unemp = 4.5; e.infl = 2.7; e.gdp = 1.8; e.fed = 3.4; e.tenYr = 4.3; e.stocks = 6900; e.oil = 72; e.gas = 3.20; e.debt = 104; e.deficit = 6.5; e.nominalGdp = 34.5;
        e.potential = 1.9; e.nairu = 4.3; e.tariff = 11; e.priceLevel = 1.1;
        e.fedChair = "Kevin Warsh";
        e.speakerD = "Hakeem Jeffries"; e.speakerR = "Mike Johnson"; e.leaderD = "Chuck Schumer"; e.leaderR = "John Thune"; e.houseLeaderD = "Hakeem Jeffries"; e.houseLeaderR = "Mike Johnson";
        e.midSeats = "AZ:D CA:D CT:D DE:D HI:D ME:D MD:D MA:D MI:D MN:D NV:D NJ:D NM:D NY:D RI:D VT:D VA:D WA:D WI:D FL:R IN:R MS:R MO:R MT:R NE:R ND:R OH:R PA:R TN:R TX:R UT:R WV:R WY:R";
        e.issues = "0=1.0 1=1.3 2=0.8 3=0.7 4=0.9 5=0.5 6=0.6 7=0.5 8=0.8 9=0.5 10=0.3 11=0.8 12=0.3 13=0.6";
        ERAS.add(e);
    }

    /** Scenarios: a twist on the era's starting conditions. */
    static final String[] SCEN = {"As it was", "Recession", "A war abroad", "Divided government", "Landslide mandate", "Inflation shock"};
    static final String[] SCEN_DESC = {
            "The country as it stood on inauguration day.",
            "The economy is shrinking: unemployment is up two points and markets are down a fifth.",
            "American forces are fighting abroad. The country has rallied around you, for now; oil is up and the deficit with it.",
            "The other party controls both the House and the Senate.",
            "You won big: higher approval and a bigger majority in both chambers.",
            "Prices are surging: inflation is three points higher, gas is up a quarter and the Fed is tightening."};

    /** Starting seats for the president's party when the era leaves it to the election. */
    static int[] seatsFor(Era e, int party) {
        if (e.houseD >= 0) return new int[]{e.houseD, e.senateD};
        return party == DEM ? new int[]{232, 51} : party == REP ? new int[]{206, 48} : new int[]{218, 50};
    }
    // =====================================================================
    //  11.  PRESIDENTS, RUNNING MATES AND A NEW GAME
    // =====================================================================
    static {
        Cand c = cand("trump", "Donald", "Trump", REP, "45th President, elected again in 2024", "FL", 1946, F_MAGA,
                "88 85 70 72 50 30 92 90 92 80 85 98 90 75 95 70 60 50 70 60 98", 1.0, 0, 0, 0, 0, "M 1 5 3 0.3 0 0 1.1 0.62 0 1 0.85 0.45");
        bio(c, "Businessman and television host; president 2017-21, impeached twice, convicted on 34 felony counts in New York in 2024, then elected again over Kamala Harris with 312 electoral votes and a narrow popular-vote win.",
                "Total command of his party, a direct line to his voters, and a gift for setting the day's agenda.",
                "Deeply polarising; low discipline; legal and ethical controversies follow him.");
        c.home = stateIdx(c.homeAb);
        c.spouse = "Melania Trump";
    }

    /** The presidents you can pick for an era: the real winner first, then what-ifs. */
    static List<Cand> eraRoster(Era e) {
        List<Cand> l = new ArrayList<Cand>();
        Cand w = e.winnerId != null ? candById(e.winnerId) : null;
        if (w != null) l.add(w);
        for (Cand c : ROSTER) {
            if (c == w || c.party > REP) continue;
            if (e.year == 2029 && c.id.equals("trump")) continue;
            l.add(c);
        }
        return l;
    }

    /** The running mate the real winner had, or a sensible default. */
    static String defaultVp(Era e, Cand p) {
        if (p == null) return null;
        if (p.id.equals("trump")) return "vance";
        if (p.id.equals("harris")) return "walz";
        String[] d = {"shapiro", "kelly", "beshear", "whitmer", "moore", "warnock"};
        String[] r = {"rubio", "youngkin", "haley", "kemp", "sanders", "cruz"};
        for (String id : p.party == DEM ? d : r) if (!id.equals(p.id)) return id;
        return null;
    }

    static final class Setup {
        static Game newGame(Settings s) {
            Game g = new Game();
            g.set = s;
            g.era = era(s.year);
            CAL_Y = g.era.year; CAL_M = 0; CAL_D = 20;
            g.rng = new Random(s.seed);
            g.pres = s.custom != null ? s.custom : candById(s.presId);
            if (g.pres == null) g.pres = eraRoster(g.era).get(0);
            g.party = g.pres.party;
            g.vp = s.vpId != null ? candById(s.vpId) : candById(defaultVp(g.era, g.pres));
            if (g.vp == g.pres) g.vp = null;
            int[] seats = seatsFor(g.era, g.party);
            int house = g.party == REP ? 435 - seats[0] : seats[0], senate = g.party == REP ? 100 - seats[1] : seats[1];
            if (s.scenario == 3) { house = Math.min(house, 205); senate = Math.min(senate, 46); }
            if (s.scenario == 4) { house += 20; senate += 4; }
            if (s.cHouse >= 0) house = s.cHouse;
            if (s.cSenate >= 0) senate = s.cSenate;
            house = clampi(house, 100, 335); senate = clampi(senate, 20, 80);
            g.houseD = g.party == REP ? 435 - house : house;
            g.senateD = g.party == REP ? 100 - senate : senate;
            g.location = Week.dc();
            g.hill = 45 + (g.pres.a[A_EST] - 60) * 0.3;
            g.stamina = 80 + (g.pres.a[A_STA] - 60) * 0.25;
            g.fitness = 40 + (g.pres.a[A_STA] - 60) * 0.5;
            Economy.init(g);
            Hill.setup(g);
            Approval.init(g);
            Week.routine(g);
            Week.record(g);
            String vp = g.vp != null ? " " + g.vp.name() + " is sworn in as vice president." : "";
            g.log(NK_BIG, g.pres.name().toUpperCase() + " IS SWORN IN AS PRESIDENT",
                    g.pres.name() + " took the oath of office at noon on " + dateLong(0) + "." + vp + " " + PARTIES[g.party] + " hold " + g.myHouse() + " House seats and " + g.mySenate() + " Senate seats.", 5);
            g.log(NK_ECON, "The new president inherits unemployment of " + fmt1(g.econ.unemp) + "% and inflation of " + fmt1(g.econ.infl) + "%", null, 1.5);
            return g;
        }
    }
    // =====================================================================
    //  12.  CONGRESS: blocs, members, committees and the bills on offer
    //  The leaders are real; committee chairs, swing votes and the rank and file are fictional.
    // =====================================================================
    static final int HOUSE = 0, SENATE = 1;
    static final String[] CHAMBER = {"House", "Senate"};

    static final class Bloc implements Serializable {
        private static final long serialVersionUID = 1L;
        String name;
        int party, chamber, seats;
        double econ, cult, loyalty, hawk, rel = 50;
    }

    static final int RO_RANK = 0, RO_CHAIR = 1, RO_LEADER = 2, RO_SWING = 3;
    static final int PRICE_PROJECT = 0, PRICE_APPOINT = 1, PRICE_ENDORSE = 2, PRICE_POLICY = 3;
    static final String[] PRICE = {"a project back home", "a say in an appointment", "your endorsement", "a change to the bill"};

    static final class Member implements Serializable {
        private static final long serialVersionUID = 1L;
        String name, title, committee;
        int party, chamber, bloc, state, role, price;
        double rel = 50, econ, cult;
        Cand face;
        int lastDeal = -99;
        boolean promised;
    }

    static final class BillDef {
        final String id, name, desc, impact;
        final int area, lean;                       // lean: the party whose agenda it is, or -1 for bipartisan
        final double econ, cult, cost, support, gdp, unemp, infl, base;
        final boolean budget, origRevenue;          // reconciliation-eligible; a revenue bill must start in the House
        final int fmt;

        BillDef(String id, String name, String desc, int area, int lean, double econ, double cult, double cost, boolean budget, double support,
                double gdp, double unemp, double infl, String impact, double base, int fmt) {
            this.id = id; this.name = name; this.desc = desc; this.area = area; this.lean = lean; this.econ = econ; this.cult = cult; this.cost = cost; this.budget = budget;
            this.support = support; this.gdp = gdp; this.unemp = unemp; this.infl = infl; this.impact = impact; this.base = base; this.fmt = fmt;
            this.origRevenue = cost < 0 || id.contains("tax") || id.equals("tariffs");
        }
    }

    /** The modern agenda. Cost is $ billions a year at full size (negative raises revenue). */
    static final BillDef[] BILLS = {
            new BillDef("lower_costs", "Lower Costs Act", "Caps insulin at $35 a month and child care at 7% of income, and bans junk fees.", PI_PRICES, DEM, -0.4, -0.1, 80, true, 66, 0.05, 0, -0.15, "Families with young children save about $# a year on child care.", 3100, 0),
            new BillDef("public_option", "A public option", "A government health plan on the ACA marketplaces in every state.", PI_HEALTH, DEM, -0.6, -0.1, 60, true, 58, 0, 0, -0.05, "# million more Americans have health insurance.", 4.6, 1),
            new BillDef("restore_roe", "Restore Roe", "The Women's Health Protection Act: abortion rights written into federal law.", PI_RIGHTS, DEM, 0, -0.8, 0, false, 60, 0, 0, 0, "Abortion is legal through viability in every state; # states had banned it.", 13, 2),
            new BillDef("imm_reform", "Immigration reform", "Border security money plus a path to citizenship for Dreamers and long-time workers.", PI_IMMIG, DEM, -0.1, -0.3, 10, false, 61, 0.1, 0, 0, "# million Dreamers and long-time residents are on a path to citizenship.", 2.4, 1),
            new BillDef("billionaire_tax", "Billionaire minimum tax", "A 25% minimum tax on households worth more than $100 million.", PI_ECON, DEM, -0.7, -0.1, -50, true, 63, -0.03, 0, -0.05, "The minimum tax raises about $# billion a year.", 50, 0),
            new BillDef("housing", "Build 3 million homes", "Zoning incentives, a builders' tax credit and federal land for housing.", PI_PRICES, DEM, -0.2, 0, 40, true, 66, 0.12, -0.05, -0.08, "# million new homes are under construction.", 1.1, 1),
            new BillDef("ai_safety", "AI safety standards", "Safety testing for the most powerful AI models, and data centers pay their own power bills.", PI_TECH, DEM, -0.3, -0.1, 2, false, 63, -0.02, 0, 0, "# frontier AI labs submit their models for federal safety testing.", 9, 0),
            new BillDef("ss_protect", "Protect Social Security", "Lift the payroll-tax cap and raise benefits for the oldest retirees.", PI_ECON, DEM, -0.5, -0.1, -15, false, 68, 0, 0, 0, "Social Security's trust fund is solvent for # more years.", 30, 0),
            new BillDef("gun_safety", "Gun safety", "Universal background checks, red-flag laws and a minimum age of 21 for assault weapons.", PI_CRIME, DEM, -0.1, -0.6, 1, false, 58, 0, 0, 0, "Background checks cover # million more gun sales a year.", 6, 1),
            new BillDef("clean_energy", "Clean energy buildout", "New transmission lines, permitting reform and clean-power tax credits.", PI_ENERGY, DEM, -0.4, -0.2, 50, true, 55, 0.15, -0.05, 0.02, "# gigawatts of new clean power are on the grid.", 120, 0),
            new BillDef("voting_rights", "Voting rights", "The Freedom to Vote Act: national standards for early voting and mail ballots.", PI_RIGHTS, DEM, 0, -0.6, 2, false, 60, 0, 0, 0, "# states have had to expand early voting.", 19, 2),
            new BillDef("paid_leave", "Paid leave", "Twelve weeks of paid family and medical leave for every worker.", PI_JOBS, DEM, -0.6, -0.1, 70, true, 70, 0.02, 0, 0.03, "# million workers took paid leave in its first year.", 11, 1),
            new BillDef("child_credit", "Expanded child tax credit", "A $3,600 credit per young child, paid monthly.", PI_PRICES, DEM, -0.5, -0.1, 100, true, 62, 0.08, 0, 0.05, "Child poverty is down # percent.", 40, 0),
            new BillDef("min_wage", "A $17 minimum wage", "Raise the federal minimum wage to $17 over five years.", PI_JOBS, DEM, -0.6, 0, 0, false, 62, -0.02, 0.05, 0.05, "# million workers got a raise.", 22, 1),
            new BillDef("tax_cuts", "Make the tax cuts bigger", "Extend the 2025 tax cuts and cut the corporate rate to 15%.", PI_ECON, REP, 0.7, 0.2, 150, true, 45, 0.18, -0.05, 0.12, "The typical family pays about $# less in federal income tax.", 1500, 0),
            new BillDef("border_wall", "Finish the wall", "Money for the border wall, more ICE agents and faster deportations.", PI_IMMIG, REP, 0.2, 0.7, 25, true, 48, -0.05, 0, 0.05, "Border crossings are down #% from when you took office.", 40, 0),
            new BillDef("health_freedom", "Health freedom act", "Replace ACA subsidies with health savings accounts and state block grants.", PI_HEALTH, REP, 0.7, 0.2, -40, true, 39, 0.03, 0, 0, "# million fewer Americans have health insurance.", 5.2, 1),
            new BillDef("abortion_limit", "A 15-week abortion limit", "A national limit on abortion after 15 weeks, with exceptions.", PI_RIGHTS, REP, 0.1, 0.8, 0, false, 41, 0, 0, 0, "Abortion is limited after 15 weeks nationwide, including in # states that had allowed it.", 24, 2),
            new BillDef("tariffs", "Reciprocal tariffs", "Match every country's tariffs, with a 60% floor on Chinese goods.", PI_TRADE, REP, 0.3, 0.3, -150, true, 42, -0.15, 0.08, 0.35, "Tariffs bring in about $# billion a year, and prices on imported goods are up.", 150, 0),
            new BillDef("energy_dom", "Energy dominance", "Open federal lands and waters to drilling and speed up permits for pipelines.", PI_ENERGY, REP, 0.6, 0.3, -5, true, 53, 0.1, -0.03, -0.12, "US oil and gas output is up #%.", 9, 0),
            new BillDef("balance_budget", "Balance the budget", "Cap spending and cut federal agencies by a tenth.", PI_ECON, REP, 0.8, 0.2, -250, true, 51, -0.12, 0.08, -0.15, "Federal spending is $# billion a year lower than planned.", 250, 0),
            new BillDef("school_choice", "School choice", "A federal tax credit for private-school and home-school scholarships.", PI_EDU, REP, 0.5, 0.5, 10, true, 52, 0, 0, 0, "# million children use the new scholarships.", 1.8, 1),
            new BillDef("police", "Back the blue", "Money for 100,000 police officers and tougher sentences for fentanyl dealers.", PI_CRIME, REP, 0.3, 0.5, 8, false, 57, 0, 0, 0, "# thousand new police officers are on the street.", 60, 0),
            new BillDef("save_act", "The SAVE Act", "Proof of citizenship to register and photo ID to vote.", PI_RIGHTS, REP, 0.1, 0.7, 1, false, 63, 0, 0, 0, "Every state now requires photo ID to vote; # did not before.", 15, 2),
            new BillDef("no_tax_tips", "No tax on tips and overtime", "Make the tax break on tips and overtime permanent and wider.", PI_JOBS, REP, 0.3, 0.1, 40, true, 66, 0.05, 0, 0.05, "Workers keep about $# billion a year in tips and overtime pay.", 40, 0),
            new BillDef("infrastructure", "Infrastructure renewal", "Roads, bridges, water pipes, broadband and the power grid.", PI_JOBS, -1, -0.1, 0, 60, false, 70, 0.15, -0.08, 0.04, "# thousand bridges and roads are being rebuilt.", 22, 0),
            new BillDef("chips2", "Chips and China Act", "Chips, shipbuilding and export controls to compete with China.", PI_TRADE, -1, 0, 0.1, 15, false, 64, 0.1, -0.03, 0.03, "# new chip and shipbuilding plants are under way.", 14, 0),
            new BillDef("permitting", "Permitting reform", "Faster environmental reviews for power lines, pipelines and mines.", PI_ENERGY, -1, 0.2, 0, 0, false, 58, 0.06, -0.02, -0.02, "Permits for big projects now take # years less.", 2, 1),
            new BillDef("kids_online", "Kids Online Safety Act", "Platforms must protect minors from harmful content and addictive design.", PI_TECH, -1, 0, 0.1, 0, false, 72, 0, 0, 0, "# million teenagers are on safer default settings.", 18, 0),
            new BillDef("farm_bill", "The farm bill", "Five years of crop insurance, conservation and food assistance.", PI_ECON, -1, 0.1, 0.1, 5, false, 60, 0.02, 0, 0, "# million farms are covered by crop insurance.", 1.2, 1),
            new BillDef("stock_ban", "Ban stock trading by Congress", "Members of Congress and their spouses can't trade individual stocks.", PI_ETHICS, -1, 0, 0, 0, false, 80, 0, 0, 0, "# members of Congress have sold their individual stocks.", 535, 0),
            new BillDef("crypto", "Crypto market rules", "Clear rules for stablecoins and digital-asset exchanges.", PI_TECH, -1, 0.3, 0, 0, false, 50, 0.02, 0, 0, "Americans hold about $# billion in regulated stablecoins.", 400, 0),
            new BillDef("ss_fix", "A bipartisan Social Security fix", "A commission's mix of a higher payroll-tax cap and a slower rise in benefits for the well-off.", PI_ECON, -1, 0, 0, -20, false, 55, 0, 0, 0, "Social Security's trust fund is solvent for # more years.", 40, 0)};

    static BillDef billDef(String id) { for (BillDef b : BILLS) if (b.id.equals(id)) return b; return null; }

    static final int ST_DRAFT = 0, ST_COMMITTEE = 1, ST_FLOOR = 2, ST_COMMITTEE2 = 3, ST_FLOOR2 = 4, ST_CONFERENCE = 5, ST_DESK = 6, ST_LAW = 7, ST_DEAD = 8, ST_OVERRIDE = 9;
    static final String[] STAGE = {"Draft", "In committee", "Floor vote", "In committee (second chamber)", "Floor vote (second chamber)", "Conference", "On your desk", "Law", "Dead", "Veto override vote"};
    static final String[] PAYFOR = {"Borrow it", "Tax the wealthy and corporations", "Cut spending elsewhere"};

    static final class Bill implements Serializable {
        private static final long serialVersionUID = 1L;
        String defId, name;
        int stage, first = HOUSE, stageWeek, introduced = -1, lawWeek = -1, payFor;
        double size = 1, scope = 0.5, strength = 1;
        boolean recon, ours = true, vetoed, bottled, publicPush, held;
        int houseYes, houseNo, senYes, senNo;
        String note = "";
        List<String> deals = new ArrayList<String>();
        double[] sweet = new double[12];                // extra support per bloc from deals

        BillDef def() { return billDef(defId); }

        int second() { return first == HOUSE ? SENATE : HOUSE; }

        int chamberNow() { return stage <= ST_FLOOR ? first : second(); }

        double netCost() {
            BillDef d = def();
            double c = d.cost * size * (0.7 + 0.6 * scope);
            if (c > 0 && payFor != 0) c *= 0.05;
            return c;
        }

        /** Where the bill sits on the left-right scales, after its provisions. */
        double econ() {
            BillDef d = def();
            double dir = d.lean == REP ? 1 : d.lean == DEM ? -1 : (d.econ >= 0 ? 0.5 : -0.5);
            return clamp(d.econ + dir * 0.15 * (size - 1) + dir * 0.18 * (scope - 0.5) + (payFor == 1 ? -0.15 : payFor == 2 ? 0.15 : 0), -1, 1);
        }

        double cult() {
            BillDef d = def();
            double dir = d.lean == REP ? 1 : d.lean == DEM ? -1 : 0;
            return clamp(d.cult + dir * 0.18 * (scope - 0.5), -1, 1);
        }

        double support() { return def().support - 4 * (scope - 0.5) - (payFor == 0 && def().cost > 30 ? 2 : 0) + (payFor == 1 ? 2 : 0); }
    }

    static final String[] CHAIR_H = {"Ways and Means", "Appropriations", "Energy and Commerce", "Judiciary", "Armed Services", "Financial Services", "Education and Workforce",
            "Agriculture", "Transportation and Infrastructure", "Homeland Security", "Foreign Affairs", "Budget"};
    static final String[] CHAIR_S = {"Finance", "Appropriations", "Health, Education, Labor and Pensions", "Judiciary", "Armed Services", "Banking", "Commerce",
            "Agriculture", "Environment and Public Works", "Homeland Security", "Foreign Relations", "Budget"};

    /** Which committee (index into the chair lists) takes up a bill on an issue. */
    static int committeeFor(BillDef d, int chamber) {
        switch (d.area) {
            case PI_ECON: return d.cost < 0 || d.id.contains("tax") ? 0 : d.id.equals("farm_bill") ? 7 : 11;
            case PI_PRICES: return d.id.equals("housing") ? 5 : 0;
            case PI_JOBS: return d.id.equals("infrastructure") ? 8 : chamber == HOUSE ? 6 : 2;
            case PI_HEALTH: return chamber == HOUSE ? 2 : 0;
            case PI_IMMIG: case PI_CRIME: case PI_RIGHTS: case PI_ETHICS: return 3;
            case PI_ENERGY: return chamber == HOUSE ? 2 : 8;
            case PI_TECH: return chamber == HOUSE ? 2 : 6;
            case PI_TRADE: return 0;
            case PI_EDU: return chamber == HOUSE ? 6 : 2;
            case PI_SECURITY: return 4;
            case PI_FOREIGN: return 10;
            default: return 11;
        }
    }

    static final String[] FIRST_M = {"James", "Robert", "Michael", "David", "William", "Thomas", "Daniel", "Mark", "Steven", "Brian", "Kevin", "Jason", "Eric", "Greg", "Carlos", "Andre", "Luis", "Raj", "Tom", "Ben", "Sam", "Nick", "Paul", "Doug", "Wes", "Cole", "Ray", "Tim"};
    static final String[] FIRST_F = {"Mary", "Patricia", "Jennifer", "Linda", "Elizabeth", "Susan", "Karen", "Lisa", "Nancy", "Sandra", "Ashley", "Michelle", "Maria", "Rosa", "Diane", "Angela", "Priya", "Grace", "Hannah", "Claire", "Tasha", "Joy", "Erin", "Lauren"};
    static final String[] LASTS = {"Whitaker", "Calloway", "Brennan", "Okafor", "Delgado", "Hargrove", "Lindqvist", "Marsh", "Pruitt", "Vasquez", "Thornton", "Kowalski", "Ashby", "Mercer", "Haldane",
            "Ruiz", "Sutherland", "Pennington", "Castellano", "Whitfield", "Abernathy", "Fairbanks", "Nakamura", "Oyelaran", "Quinlan", "Rourke", "Stanton", "Talbot", "Underwood", "Vance-Hill",
            "Waller", "Yates", "Ellison", "Garrity", "Holloway", "Iverson", "Jardine", "Kilgore", "Langford", "Montague", "Norwood", "Ostrander", "Pemberton", "Radcliffe", "Sandoval", "Tillman", "Voss", "Winslow"};

    /** Real leaders: faces for the portraits. */
    static final String[][] LEADER_LOOKS = {
            {"Mike Johnson", "M 1 2 2 0.1 0 1 0.95 0.5 0 1 0.45 0.6"}, {"Hakeem Jeffries", "M 6 0 0 0 0 1 1.0 0.55 0 0 0.45 0.5"},
            {"John Thune", "M 1 6 2 0.8 0 0 0.95 0.6 0 2 0.65 0.55"}, {"Chuck Schumer", "M 1 6 1 0.9 0 1 1.05 0.5 0 0 0.8 0.4"}};

    static Cand face(String name, Random r, int party) {
        Cand c = new Cand();
        String[] p = name.split(" ", 2);
        c.first = p[0]; c.last = p.length > 1 ? p[1] : ""; c.party = party; c.id = "m_" + name.replace(' ', '_');
        Look l = null;
        for (String[] lk : LEADER_LOOKS) if (lk[0].equals(name)) l = Look.parse(lk[1]);
        if (l == null) l = Creator.randomLook(r);
        c.look = l;
        return c;
    }

    static final class Congress implements Serializable {
        private static final long serialVersionUID = 1L;
        List<Bloc> blocs = new ArrayList<Bloc>();
        List<Member> members = new ArrayList<Member>();
        List<Bill> bills = new ArrayList<Bill>();
        int reconFy = -1, fy;                           // the fiscal year whose reconciliation bill has been used
        boolean[] floorUsed = new boolean[2];
        // the budget
        boolean budgetSent;
        double reqDefense, reqNondefense, reqTax, reqMandatory;
        int[] approps = new int[12];                    // 0 none, 1 one chamber, 2 both, 3 enacted, 4 on a CR
        int crUntil = -1, xWeek = -1;
        boolean shutdown;
        int shutdownWeeks;
        double blame;                                   // share of the public blaming you for a shutdown (0-1)
        double debtLimit;                               // $ trillions
        boolean xWarned, defaulted, crMinority;
        // midterms
        boolean midDone;
        double[] campaign = new double[NS];             // your campaigning per state in the midterm season
        double midMargin;
        String midSummary;
        int overrides, vetoes, laws, shutdowns, shutdownTotal;
    }

    static final String[] APPROPS = {"Agriculture", "Commerce, Justice, Science", "Defense", "Energy and Water", "Financial Services", "Homeland Security", "Interior and Environment",
            "Labor, Health and Human Services, Education", "Legislative Branch", "Military Construction and Veterans Affairs", "State and Foreign Operations", "Transportation and Housing"};

    static final class Hill {
        /** Bloc templates: chamber, party, name, share of the party's seats, econ, cult, loyalty, deficit hawkishness. */
        static final int[] TB_CH = {HOUSE, HOUSE, HOUSE, HOUSE, HOUSE, HOUSE, SENATE, SENATE, SENATE, SENATE, SENATE, SENATE};
        static final int[] TB_PARTY = {DEM, DEM, DEM, REP, REP, REP, DEM, DEM, DEM, REP, REP, REP};
        static final String[] TB_NAME = {"Progressive Caucus", "Mainstream Democrats", "New Dems and Blue Dogs", "Freedom Caucus", "Mainstream Republicans", "Problem Solvers Republicans",
                "Progressive senators", "Mainstream Democrats", "Moderate Democrats", "Hardliners", "Mainstream Republicans", "Moderate Republicans"};
        static final double[] TB_SHARE = {0.45, 0.38, 0.17, 0.17, 0.70, 0.13, 0.25, 0.60, 0.15, 0.20, 0.68, 0.12};
        static final double[] TB_ECON = {-0.65, -0.4, -0.1, 0.8, 0.55, 0.25, -0.7, -0.4, -0.1, 0.8, 0.55, 0.2};
        static final double[] TB_CULT = {-0.7, -0.5, -0.25, 0.8, 0.55, 0.2, -0.7, -0.45, -0.2, 0.75, 0.5, 0.1};
        static final double[] TB_LOY = {0.8, 0.9, 0.75, 0.55, 0.85, 0.75, 0.85, 0.9, 0.7, 0.6, 0.85, 0.7};
        static final double[] TB_HAWK = {0.1, 0.3, 0.6, 0.9, 0.5, 0.4, 0.1, 0.3, 0.5, 0.85, 0.5, 0.4};

        static void setup(Game g) {
            Congress c = new Congress();
            g.cong = c;
            Random r = new Random(g.set.seed ^ 0x5eedL);
            seats(g);
            // the leaders
            Era e = g.era;
            boolean dHouse = g.houseD >= 218, dSen = g.senateD > 50 || g.senateD == 50 && g.party == DEM;
            leader(g, r, HOUSE, dHouse ? DEM : REP, "Speaker of the House", dHouse ? e.speakerD : e.speakerR);
            leader(g, r, HOUSE, dHouse ? REP : DEM, "House " + (dHouse ? "Republican" : "Democratic") + " leader", dHouse ? e.houseLeaderR : e.houseLeaderD);
            leader(g, r, SENATE, dSen ? DEM : REP, "Senate Majority Leader", dSen ? e.leaderD : e.leaderR);
            leader(g, r, SENATE, dSen ? REP : DEM, "Senate " + (dSen ? "Republican" : "Democratic") + " leader", dSen ? e.leaderR : e.leaderD);
            // committee chairs from the majority
            for (int ch = 0; ch < 2; ch++) {
                int maj = ch == HOUSE ? (dHouse ? DEM : REP) : (dSen ? DEM : REP);
                String[] names = ch == HOUSE ? CHAIR_H : CHAIR_S;
                for (int k = 0; k < names.length; k++) {
                    Member m = person(c, r, maj, ch);
                    m.role = RO_CHAIR; m.committee = names[k];
                    m.title = "Chair, " + (ch == HOUSE ? "House " : "Senate ") + names[k];
                    m.bloc = blocOf(c, ch, maj, r.nextDouble() < 0.7 ? 1 : r.nextBoolean() ? 0 : 2);
                    ideo(c, m, r);
                    c.members.add(m);
                }
            }
            // swing votes: moderates of both parties, and a hardliner or two
            int[][] swing = {{HOUSE, g.party, 2}, {HOUSE, g.party, 2}, {HOUSE, other(g.party), 2}, {HOUSE, other(g.party), 2}, {HOUSE, g.party, 0},
                    {SENATE, g.party, 2}, {SENATE, g.party, 2}, {SENATE, other(g.party), 2}, {SENATE, other(g.party), 2}, {SENATE, g.party, 0}};
            for (int[] sw : swing) {
                Member m = person(c, r, sw[1], sw[0]);
                m.role = RO_SWING;
                m.bloc = blocOf(c, sw[0], sw[1], sw[2]);
                m.price = r.nextInt(4);
                m.state = swingState(r);
                for (int t = 0; t < 20 && stateTaken(c, m); t++) m.state = swingState(r);
                m.title = (sw[0] == HOUSE ? "Rep. " : "Sen. ") + m.name.split(" ")[1] + " (" + PARTY_SHORT[sw[1]] + "-" + STATES[m.state].ab + ")";
                ideo(c, m, r);
                m.rel = 45 + r.nextDouble() * 15;
                c.members.add(m);
            }
            for (Bloc b : c.blocs) b.rel = b.party == g.party ? 58 : 38;
            c.fy = fiscalYear(g);
            Budget.setup(g);
        }

        static int other(int p) { return p == REP ? DEM : REP; }

        /** One slot of Hill meetings: the leaders and the blocs warm to you, your own side more. */
        static void meeting(Game g) {
            if (g.cong == null) return;
            double k = (g.pres != null ? g.pres.a[A_EST] : 60) / 70.0;
            for (Member m : g.cong.members) {
                if (m.role == RO_LEADER) m.rel = Math.min(100, m.rel + (m.party == g.party ? 1.6 : 0.9) * k);
                else if (m.role == RO_CHAIR || m.role == RO_SWING) m.rel = Math.min(100, m.rel + 0.4 * k);
            }
            for (Bloc b : g.cong.blocs) b.rel = Math.min(100, b.rel + (b.party == g.party ? 0.7 : 0.35) * k);
        }

        /** Relationships drift back toward where party and approval put them. */
        static void drift(Game g) {
            double pull = (g.approval - 45) * 0.3;
            for (Bloc b : g.cong.blocs) { double base = (b.party == g.party ? 55 : 35) + pull * (b.party == g.party ? 1 : 0.5); b.rel += (base - b.rel) * 0.02; }
            for (Member m : g.cong.members) { double base = (m.party == g.party ? 55 : 35) + pull * 0.5; m.rel += (base - m.rel) * 0.015; }
        }

        static int swingState(Random r) {
            String[] sw = {"PA", "MI", "WI", "AZ", "NV", "GA", "NC", "NH", "ME", "VA", "MN", "OH", "IA", "TX", "FL", "CO", "NM"};
            return stateIdx(sw[r.nextInt(sw.length)]);
        }

        static Member person(Congress c, Random r, int party, int chamber) {
            Member m = new Member();
            boolean f = r.nextDouble() < 0.33;
            String last = LASTS[r.nextInt(LASTS.length)];
            for (int t = 0; t < 30 && lastUsed(c, last); t++) last = LASTS[r.nextInt(LASTS.length)];
            m.name = (f ? FIRST_F[r.nextInt(FIRST_F.length)] : FIRST_M[r.nextInt(FIRST_M.length)]) + " " + last;
            m.party = party; m.chamber = chamber; m.state = r.nextInt(NS);
            m.face = face(m.name, r, party);
            if (m.face.look != null) { m.face.look.female = f; if (f && m.face.look.style < 6) m.face.look.style = 6 + r.nextInt(4); if (!f && m.face.look.style >= 6) m.face.look.style = r.nextInt(5); }
            return m;
        }

        static boolean stateTaken(Congress c, Member m) {
            for (Member o : c.members) if (o.role == RO_SWING && o.chamber == m.chamber && o.state == m.state) return true;
            return false;
        }

        static boolean lastUsed(Congress c, String last) {
            for (Member m : c.members) if (m.name.endsWith(" " + last)) return true;
            return false;
        }

        static void leader(Game g, Random r, int chamber, int party, String title, String name) {
            Member m = new Member();
            m.name = name; m.party = party; m.chamber = chamber; m.role = RO_LEADER; m.title = title;
            m.face = face(name, r, party);
            m.bloc = blocOf(g.cong, chamber, party, 1);
            ideo(g.cong, m, r);
            m.rel = party == g.party ? 62 : 35;
            g.cong.members.add(m);
        }

        static void ideo(Congress c, Member m, Random r) {
            Bloc b = c.blocs.get(m.bloc);
            m.econ = clamp(b.econ + r.nextGaussian() * 0.08, -1, 1);
            m.cult = clamp(b.cult + r.nextGaussian() * 0.08, -1, 1);
        }

        static int blocOf(Congress c, int chamber, int party, int k) {
            int n = 0;
            for (int i = 0; i < c.blocs.size(); i++) { Bloc b = c.blocs.get(i); if (b.chamber == chamber && b.party == party) { if (n == k) return i; n++; } }
            return 0;
        }

        /** Rebuild the blocs from the seat counts (after an election). */
        static void seats(Game g) {
            Congress c = g.cong;
            List<Bloc> old = c.blocs;
            c.blocs = new ArrayList<Bloc>();
            for (int t = 0; t < TB_CH.length; t++) {
                Bloc b = new Bloc();
                b.chamber = TB_CH[t]; b.party = TB_PARTY[t]; b.name = TB_NAME[t];
                int total = b.chamber == HOUSE ? (b.party == DEM ? g.houseD : 435 - g.houseD) : (b.party == DEM ? g.senateD : 100 - g.senateD);
                b.seats = (int) Math.round(total * TB_SHARE[t]);
                b.econ = TB_ECON[t]; b.cult = TB_CULT[t]; b.loyalty = TB_LOY[t]; b.hawk = TB_HAWK[t];
                for (Bloc o : old) if (o.name.equals(b.name) && o.chamber == b.chamber) b.rel = o.rel;
                c.blocs.add(b);
            }
            // rounding: make each party's blocs add up
            for (int ch = 0; ch < 2; ch++) for (int p = DEM; p <= REP; p++) {
                int total = ch == HOUSE ? (p == DEM ? g.houseD : 435 - g.houseD) : (p == DEM ? g.senateD : 100 - g.senateD), sum = 0;
                Bloc big = null;
                for (Bloc b : c.blocs) if (b.chamber == ch && b.party == p) { sum += b.seats; if (big == null || b.seats > big.seats) big = b; }
                if (big != null) big.seats += total - sum;
            }
        }

        static int fiscalYear(Game g) { int[] d = dateOf(g.day()); return d[1] >= 9 ? d[0] + 1 : d[0]; }

        static boolean majority(Game g, int chamber, int party) {
            if (chamber == HOUSE) return party == DEM ? g.houseD >= 218 : 435 - g.houseD >= 218;
            int s = party == DEM ? g.senateD : 100 - g.senateD;
            return s > 50 || s == 50 && g.party == party;
        }

        static Member leaderOf(Game g, int chamber, boolean majority) {
            for (Member m : g.cong.members) if (m.role == RO_LEADER && m.chamber == chamber && majority(g, chamber, m.party) == majority) return m;
            return null;
        }

        static Member chairFor(Game g, Bill b, int chamber) {
            int k = committeeFor(b.def(), chamber);
            String[] names = chamber == HOUSE ? CHAIR_H : CHAIR_S;
            for (Member m : g.cong.members) if (m.role == RO_CHAIR && m.chamber == chamber && m.committee.equals(names[k])) return m;
            return null;
        }
    }
    // =====================================================================
    //  13.  MAKING LAWS
    //  A bill goes to committee, to the floor, to the other chamber and to conference, then to your desk.
    //  Each bloc's vote turns on ideology, party, its relationship with you, your approval, the deficit and the deals made.
    // =====================================================================
    static final class Legis {
        /** Votes needed to pass (or to override). */
        static int need(Game g, Bill b, int chamber, boolean override) {
            if (chamber == HOUSE) return override ? 290 : 218;
            if (override) return 67;
            if (b.recon || g.filibusterGone) {
                int vpParty = g.party;
                return b.ours || vpParty == partyOf(b) ? 50 : 51;
            }
            return g.era.year < 1975 ? 67 : 60;
        }

        /** The party whose bill it is: yours, or the majority's that wrote it. */
        static int partyOf(Bill b) { return b.ours ? -1 : b.def().lean; }

        static int sponsorParty(Game g, Bill b) {
            if (b.ours) return g.party;
            int l = b.def().lean;
            return l >= 0 ? l : g.party;
        }

        /**
         * A spatial vote: a bloc likes a bill that moves the law (at 0,0) toward it. Party pulls hard, harder still
         * behind the president's own priorities; then the relationship, approval, the cost, public support and deals.
         */
        static double blocLogit(Game g, Bill b, int bi, boolean override) {
            Bloc bl = g.cong.blocs.get(bi);
            double pol = g.era.polarization;
            double be = b.econ(), bc = b.cult();
            double spatial = 2 * (bl.econ * be + 0.8 * bl.cult * bc) - (be * be + 0.8 * bc * bc);
            double l = 0.3 + 2.2 * spatial;
            int sp = sponsorParty(g, b);
            boolean mine = bl.party == g.party;
            boolean bip = b.def().lean < 0;
            if (bip) l += 0.2;                                                 // bipartisan by design
            if (bl.party == sp) {
                double agree = clamp(0.5 + spatial, 0, 1);
                l += bl.loyalty * (b.ours && mine ? 0.4 + 2.4 * agree : 0.5 + 1.3 * agree);
            } else l -= 0.9 * pol * (bip ? 0.7 : 1);
            if (b.ours && !mine) l -= 0.35 + (Midterms.season(g) || g.week > 170 ? 0.3 : 0);   // why hand the president a win?
            if (b.ours || mine) l += (bl.rel - 50) / 35;
            l += mine ? (g.approval - 45) / 18 : (g.approval - 50) / 30;
            double cost = b.netCost();
            if (cost > 0) l -= bl.hawk * cost / 180;
            boolean moderate = bl.name.contains("oderate") || bl.name.contains("Blue Dogs") || bl.name.contains("Problem Solvers");
            l += (b.support() - 55) / 40 * (moderate ? 1.2 : 0.4);
            if (b.publicPush) l += moderate ? 0.25 : 0.1;
            if (b.defId.equals("stock_ban")) l -= 1.2;                          // members don't love this one
            if (override) l += mine ? -1.6 * bl.loyalty : 0.3;
            if (!b.ours && mine && b.vetoed) l -= 0.5;
            return 1.6 * l + 1.2 * b.sweet[bi];
        }

        /** {expected yes, standard deviation} in a chamber. */
        static double[] count(Game g, Bill b, int chamber, boolean override) {
            double yes = 0, var = 0;
            for (int i = 0; i < g.cong.blocs.size(); i++) {
                Bloc bl = g.cong.blocs.get(i);
                if (bl.chamber != chamber || bl.seats <= 0) continue;
                double p = logistic(blocLogit(g, b, i, override));
                yes += bl.seats * p;
                var += bl.seats * p * (1 - p) + Math.pow(bl.seats * 0.06 * (1 - Math.abs(2 * p - 1)), 2);
            }
            return new double[]{yes, Math.sqrt(var) + (chamber == HOUSE ? 2 : 0.8)};
        }

        static double chance(Game g, Bill b, int chamber, boolean override) {
            double[] c = count(g, b, chamber, override);
            return logistic(1.702 * (c[0] - need(g, b, chamber, override) + 0.5) / c[1]);
        }

        /** Would the committee chair let it out? Chairs from the other party bottle up bills they dislike. */
        static double chairOk(Game g, Bill b, int chamber) {
            Member ch = Hill.chairFor(g, b, chamber);
            if (ch == null) return 1;
            int bi = ch.bloc;
            double l = blocLogit(g, b, bi, false) + (ch.rel - 50) / 30;
            if (ch.party == sponsorParty(g, b)) l += 1.5;
            if (b.support() > 65) l += 0.8;
            return logistic(l);
        }

        static boolean reconOk(Game g, Bill b) {
            return b.def().budget && g.cong.reconFy != Hill.fiscalYear(g) && Hill.majority(g, HOUSE, g.party) && Hill.majority(g, SENATE, g.party);
        }

        // ---- your bills ----
        static Bill draft(Game g, BillDef d) {
            Bill b = new Bill();
            b.defId = d.id; b.name = d.name; b.ours = true;
            b.first = d.origRevenue ? HOUSE : HOUSE;
            b.payFor = d.cost > 60 && g.party == DEM ? 1 : 0;
            return b;
        }

        static void introduce(Game g, Bill b) {
            b.stage = ST_COMMITTEE;
            b.stageWeek = g.week;
            b.introduced = g.week;
            if (b.recon) g.cong.reconFy = Hill.fiscalYear(g);
            if (!g.cong.bills.contains(b)) g.cong.bills.add(b);
            g.log(NK_INFO, "The White House sends " + b.name + " to Congress", null, 1.0);
        }

        static int stageWeeks(Bill b) {
            switch (b.stage) {
                case ST_COMMITTEE: case ST_COMMITTEE2: return b.recon ? 2 : 3;
                case ST_CONFERENCE: return 2;
                default: return 1;
            }
        }

        static void week(Game g) {
            Congress c = g.cong;
            c.floorUsed[0] = false; c.floorUsed[1] = false;
            for (Bill b : new ArrayList<Bill>(c.bills)) step(g, b);
            if (g.week % 7 == 3) congressBill(g);
        }

        static void step(Game g, Bill b) {
            if (b.stage >= ST_DESK) return;
            if (g.week - b.stageWeek < stageWeeks(b)) return;
            Random r = g.rng;
            int ch = b.chamberNow();
            switch (b.stage) {
                case ST_COMMITTEE: case ST_COMMITTEE2: {
                    double ok = chairOk(g, b, ch);
                    if (r.nextDouble() < ok) {
                        Member chair = Hill.chairFor(g, b, ch);
                        if (chair != null && chair.party != sponsorParty(g, b) && b.size > 0.8) { b.size = Math.max(0.6, b.size - 0.15); b.note = "The committee trimmed it."; }
                        b.stage++; b.stageWeek = g.week; b.bottled = false;
                        g.log(NK_INFO, CHAMBER[ch] + " committee approves " + b.name, null, 0.6);
                    } else {
                        if (!b.bottled) {
                            Member chair = Hill.chairFor(g, b, ch);
                            g.log(NK_BAD, (chair != null ? chair.name + " (" + PARTY_SHORT[chair.party] + ")" : "The committee chair") + " bottles up " + b.name + " in committee", null, 1.0);
                        }
                        b.bottled = true;
                        b.stageWeek = g.week;
                        if (g.week - b.introduced > 30) kill(g, b, "dies in committee");
                    }
                    break;
                }
                case ST_FLOOR: case ST_FLOOR2: {
                    if (g.cong.floorUsed[ch]) return;
                    if (hold(g, b, ch)) return;
                    g.cong.floorUsed[ch] = true;
                    if (vote(g, b, ch, false)) { b.stage++; b.stageWeek = g.week; }
                    else kill(g, b, ch == SENATE && !b.recon && !g.filibusterGone && lastYes(b, ch) >= 50 ? "is filibustered" : "fails");
                    break;
                }
                case ST_CONFERENCE: {
                    b.stage = ST_DESK;
                    b.stageWeek = g.week;
                    toDesk(g, b);
                    break;
                }
                default: break;
            }
        }

        /**
         * Leaders don't call votes they expect to lose. Your own party's leaders keep whipping for a while;
         * the other party's won't bring your bill up unless most of their members want it.
         */
        static boolean hold(Game g, Bill b, int ch) {
            Member lead = Hill.leaderOf(g, ch, true);
            int sp = sponsorParty(g, b);
            int waited = g.week - b.stageWeek;
            double p = chance(g, b, ch, false);
            boolean friendly = lead == null || lead.party == sp;
            if (friendly) {
                if (p >= 0.75 || waited >= 12) { b.held = false; return false; }
                for (int i = 0; i < g.cong.blocs.size(); i++) { Bloc bl = g.cong.blocs.get(i); if (bl.chamber == ch && bl.party == sp) b.sweet[i] += 0.05; }
            } else {
                double maj = 0, seats = 0;
                for (int i = 0; i < g.cong.blocs.size(); i++) { Bloc bl = g.cong.blocs.get(i); if (bl.chamber == ch && bl.party == lead.party) { maj += bl.seats * logistic(blocLogit(g, b, i, false)); seats += bl.seats; } }
                if (maj / Math.max(1, seats) >= 0.5 && p >= 0.4) { b.held = false; return false; }
                if (waited >= 14) { kill(g, b, "never gets a vote: " + lead.name + " won't bring it up"); return true; }
            }
            if (!b.held) g.log(NK_INFO, (friendly ? "Leaders put off the " + CHAMBER[ch] + " vote on " + b.name + " while they hunt for votes" : lead.name + " refuses to schedule a vote on " + b.name), null, b.ours ? 1.0 : 0.5);
            b.held = true;
            return true;
        }

        static int lastYes(Bill b, int ch) { return ch == HOUSE ? b.houseYes : b.senYes; }

        static boolean vote(Game g, Bill b, int ch, boolean override) {
            double[] c = count(g, b, ch, override);
            int total = ch == HOUSE ? 435 : 100;
            int yes = (int) Math.round(clamp(c[0] + g.rng.nextGaussian() * c[1], 0, total));
            int no = total - yes;
            if (ch == HOUSE) { b.houseYes = yes; b.houseNo = no; } else { b.senYes = yes; b.senNo = no; }
            boolean pass = yes >= need(g, b, ch, override);
            String what = override ? "the veto override" : ch == SENATE && !b.recon && !g.filibusterGone ? "cloture on " + b.name : b.name;
            g.log(pass ? NK_GOOD : NK_BAD, CHAMBER[ch] + (pass ? " passes " : " rejects ") + what + ", " + yes + "-" + no, null, b.ours ? 1.6 : 0.9);
            return pass;
        }

        static void kill(Game g, Bill b, String how) {
            b.stage = ST_DEAD;
            b.stageWeek = g.week;
            if (b.recon && g.cong.reconFy == Hill.fiscalYear(g)) g.cong.reconFy = -1;      // the reconciliation slot is free again
            g.log(NK_BAD, b.name + " " + how, null, b.ours ? 1.4 : 0.6);
            if (b.ours) { g.capital = Math.max(0, g.capital - 5); g.issueAdj[b.def().area] -= 0.2; g.perf -= 0.3; }
        }

        static void toDesk(Game g, Bill b) {
            BillDef d = b.def();
            String who = b.ours ? "your" : "the " + PARTIES[sponsorParty(g, b)] + "'";
            String body = "Congress has passed " + who + " bill: " + d.desc + " House " + b.houseYes + "-" + b.houseNo + ", Senate " + b.senYes + "-" + b.senNo + ". "
                    + "About " + Math.round(b.support()) + "% of the public supports it." + (b.netCost() > 0 ? " It adds about $" + Math.round(b.netCost()) + " billion a year to the deficit." : b.netCost() < 0 ? " It cuts the deficit by about $" + Math.round(-b.netCost()) + " billion a year." : "");
            String[] adv;
            boolean mine = b.ours || d.lean == g.party || d.lean < 0;
            if (mine) adv = new String[]{"Legislative director: \"This is the win we fought for. Sign it in the Rose Garden.\"",
                    "Chief of staff: \"Only if you want to throw away months of work. Vetoing your own priority would stun everyone.\"",
                    "Counsel: \"It becomes law in ten days anyway, but you'd get none of the credit.\""};
            else adv = new String[]{"Chief of staff: \"Signing it takes the issue off the table and looks bipartisan, but your base won't like it.\"",
                    "Political director: \"Veto it. Your voters hate this bill, and they can't override you.\"",
                    "Counsel: \"Let it become law without your signature: you neither own it nor fight it.\""};
            Decision dd = Decisions.add(g, D_SIGN, b.name + " is on your desk", body, new String[]{"Sign it", "Veto it", "Let it become law without signing"}, adv);
            dd.bill = b;
        }

        /** The president's call on a bill: 0 sign, 1 veto, 2 no signature. */
        static void decide(Game g, Bill b, int k) {
            if (k == 1) {
                b.vetoed = true;
                g.cong.vetoes++;
                g.log(NK_BIG, "PRESIDENT " + g.pres.last.toUpperCase() + " VETOES " + b.name.toUpperCase(), null, 2.5);
                boolean mine = b.ours || b.def().lean == g.party;
                if (mine) { g.perf -= 1.5; g.capital = Math.max(0, g.capital - 10); }
                else { g.issueAdj[b.def().area] += 0.1; for (Bloc bl : g.cong.blocs) if (bl.party != g.party) bl.rel = Math.max(0, bl.rel - 4); }
                // an override vote if Congress has the numbers to try
                double ch = chance(g, b, HOUSE, true) * chance(g, b, SENATE, true);
                if (ch > 0.05 || b.houseYes >= 280 && b.senYes >= 64) {
                    b.stage = ST_OVERRIDE;
                    boolean h = vote(g, b, HOUSE, true), s = h && vote(g, b, SENATE, true);
                    if (h && s) { g.cong.overrides++; g.log(NK_BIG, "CONGRESS OVERRIDES THE VETO OF " + b.name.toUpperCase(), null, 2.6); enact(g, b, false); g.capital = Math.max(0, g.capital - 10); g.perf -= 1; }
                    else { b.stage = ST_DEAD; g.log(NK_GOOD, "The veto of " + b.name + " stands", null, 1.2); }
                } else b.stage = ST_DEAD;
                return;
            }
            enact(g, b, k == 0);
        }

        static void enact(Game g, Bill b, boolean signed) {
            BillDef d = b.def();
            b.stage = ST_LAW;
            b.lawWeek = g.week;
            b.strength = b.size * (0.7 + 0.6 * b.scope) * (b.recon && b.scope > 0.6 ? 0.85 : 1);
            g.cong.laws++;
            Econ e = g.econ;
            double pctGdp = b.netCost() / (e.nominalGdp * 10);
            e.fiscal += pctGdp;
            e.polGdp += d.gdp * b.strength * 0.3; e.polUnemp += d.unemp * b.strength * 0.5; e.polInfl += d.infl * b.strength * 0.6;
            boolean mine = b.ours || d.lean == g.party || d.lean < 0;
            if (signed) {
                double sup = b.support() - 50;
                g.issueAdj[d.area] = clamp(g.issueAdj[d.area] + sup * 0.02 * Math.min(1.5, b.strength) * (mine ? 1 : 0.4), -1.5, 1.5);
                g.perf += mine ? 0.6 + sup * 0.02 : 0.2;
                if (b.ours) g.capital = Math.min(100, g.capital + 6);
                if (!mine) for (Bloc bl : g.cong.blocs) if (bl.party == g.party) bl.rel = Math.max(0, bl.rel - 3);
                g.log(NK_BIG, "PRESIDENT " + g.pres.last.toUpperCase() + " SIGNS " + b.name.toUpperCase(), d.desc + (b.recon && b.scope > 0.6 ? " The parliamentarian struck several provisions under the Byrd rule." : ""), 3.0);
                snd(Sfx.CHEER);
            } else g.log(NK_INFO, b.name + " becomes law without the president's signature", null, 1.4);
        }

        /** The majority in Congress moves bills of its own. */
        static void congressBill(Game g) {
            Random r = g.rng;
            boolean oppHouse = !Hill.majority(g, HOUSE, g.party), oppSen = !Hill.majority(g, SENATE, g.party);
            int active = 0;
            for (Bill b : g.cong.bills) if (!b.ours && b.stage < ST_DESK) active++;
            if (active >= 2) return;
            int sp = oppHouse ? Hill.other(g.party) : g.party;
            List<BillDef> l = new ArrayList<BillDef>();
            for (BillDef d : BILLS) {
                if (d.lean != sp && d.lean >= 0) continue;
                boolean used = false;
                for (Bill b : g.cong.bills) if (b.defId.equals(d.id) && (b.stage != ST_DEAD || g.week - b.stageWeek < 52)) used = true;
                if (!used) l.add(d);
            }
            if (l.isEmpty() || (!oppHouse && !oppSen && r.nextDouble() < 0.6)) return;
            BillDef d = l.get(r.nextInt(l.size()));
            Bill b = new Bill();
            b.defId = d.id; b.name = d.name; b.ours = false; b.first = oppHouse || !oppSen ? HOUSE : SENATE;
            b.stage = ST_COMMITTEE; b.stageWeek = g.week; b.introduced = g.week;
            g.cong.bills.add(b);
            g.log(NK_INFO, (b.first == HOUSE ? "House " : "Senate ") + PARTIES[sp] + " move " + d.name, null, 0.7);
        }

        // ---- deals to win votes ----
        static final int DL_CALL = 0, DL_PROJECT = 1, DL_APPOINT = 2, DL_ENDORSE = 3, DL_THREAT = 4, DL_PUBLIC = 5;
        static final String[] DEAL = {"Call", "Project back home", "Appointment", "Endorsement", "Threaten", "Go public"};
        static final int[] DEAL_COST = {2, 5, 6, 4, 4, 3};

        static String deal(Game g, Bill b, Member m, int kind) {
            if (g.capital < DEAL_COST[kind]) return "You don't have enough political capital.";
            if (m != null && m.lastDeal == g.week) return m.name + " has already heard from you this week.";
            Random r = g.rng;
            g.capital -= DEAL_COST[kind];
            if (kind == DL_PUBLIC) {
                b.publicPush = true;
                g.issueSal[b.def().area] = Math.min(2, g.issueSal[b.def().area] + 0.1);
                return "You make the case for " + b.name + " to the country. Moderates feel the pressure.";
            }
            m.lastDeal = g.week;
            double boost;
            String msg;
            switch (kind) {
                case DL_PROJECT:
                    boost = m.price == PRICE_PROJECT ? 1.4 : 0.7;
                    g.econ.fiscal += 0.01;
                    msg = "You promise " + m.name + " a project back home.";
                    if (r.nextDouble() < 0.15) g.log(NK_BAD, "\"Pork for votes\": reporters dig into the White House's deal with " + m.name, null, 1.0);
                    break;
                case DL_APPOINT: boost = m.price == PRICE_APPOINT ? 1.5 : 0.8; msg = "You give " + m.name + " a say in an appointment."; break;
                case DL_ENDORSE:
                    boost = m.party == g.party ? (m.price == PRICE_ENDORSE ? 1.5 : 0.9) : 0.2;
                    msg = m.party == g.party ? "You promise to campaign for " + m.name + "." : m.name + " is polite, but your endorsement won't help a member of the other party.";
                    break;
                case DL_THREAT:
                    boost = m.party == g.party ? 0.8 : -0.3;
                    m.rel = Math.max(0, m.rel - 15);
                    msg = m.party == g.party ? "You make it clear you'd back a primary challenger. " + m.name + " is furious, but listening." : "The threat backfires: " + m.name + " goes on TV to complain.";
                    if (r.nextDouble() < 0.3) g.log(NK_BAD, m.name + " says the White House threatened them over " + b.name, null, 1.2);
                    break;
                default:
                    boost = 0.35 * (g.pres.a[A_CHA] + g.pres.a[A_EST]) / 140;
                    m.rel = Math.min(100, m.rel + 4);
                    msg = "You call " + m.name + ".";
                    break;
            }
            if (m.role == RO_SWING || m.role == RO_CHAIR) {
                // a named member moves their bloc a little and votes with you
                b.sweet[m.bloc] += boost * (m.role == RO_CHAIR ? 0.35 : 0.22);
                b.deals.add(DEAL[kind] + ": " + m.name);
                if (m.role == RO_CHAIR) m.rel = Math.min(100, m.rel + 6 * boost);
            } else {
                b.sweet[m.bloc] += boost * 0.3;
                m.rel = Math.min(100, m.rel + 5 * boost);
                b.deals.add(DEAL[kind] + ": " + m.name);
            }
            return msg;
        }

        /** The nuclear option: a simple majority changes the Senate's rules. Your own moderates usually balk. */
        static double nukeChance(Game g) {
            if (g.filibusterGone || !Hill.majority(g, SENATE, g.party)) return 0;
            double rel = 0, n = 0;
            for (Bloc b : g.cong.blocs) if (b.chamber == SENATE && b.party == g.party) { rel += b.rel * b.seats; n += b.seats; }
            rel /= Math.max(1, n);
            return clamp(0.06 + 0.06 * (g.mySenate() - 50) + (rel - 55) / 120 + (g.approval - 50) / 100, 0.02, 0.6);
        }

        static String nuke(Game g) {
            if (g.capital < 15) return "You need 15 political capital.";
            g.capital -= 15;
            if (g.rng.nextDouble() < nukeChance(g)) {
                g.filibusterGone = true;
                for (Bloc b : g.cong.blocs) if (b.chamber == SENATE && b.party != g.party) b.rel = Math.max(0, b.rel - 15);
                g.log(NK_BIG, "SENATE ENDS THE FILIBUSTER FOR LEGISLATION", "A simple majority can now pass bills. The minority vows to remember.", 4);
                return "The Senate changes its rules: bills now pass with a simple majority.";
            }
            for (Bloc b : g.cong.blocs) if (b.chamber == SENATE && b.party == g.party && b.name.contains("oderate")) b.rel = Math.max(0, b.rel - 8);
            g.perf -= 0.5;
            g.log(NK_BAD, "Senate " + PARTIES[g.party] + " lack the votes to end the filibuster", "Several of your own senators say they won't change the rules.", 2.2);
            return "Not enough of your own senators would go along.";
        }

        /** The whip's read of where a bloc stands. */
        static String lean(double p) { return p > 0.8 ? "Yes" : p > 0.6 ? "Leans yes" : p > 0.4 ? "Undecided" : p > 0.2 ? "Leans no" : "No"; }
    }
    // =====================================================================
    //  14.  THE BUDGET
    //  Your budget request in February; twelve spending bills over the summer; a deadline on October 1;
    //  continuing resolutions, shutdowns and, every so often, the debt limit.
    // =====================================================================
    static final class Budget {
        static void setup(Game g) {
            Congress c = g.cong;
            double subj = debtSubject(g);
            c.debtLimit = g.era.year == 2025 ? 36.1 : Math.round((subj + 0.9) * 10) / 10.0;
            c.xWeek = g.era.year == 2025 ? 28 : -1;
            Arrays.fill(c.approps, 4);              // the fiscal year already under way runs on a CR or full-year bills
        }

        /** Debt subject to the limit, $ trillions (debt held by the public plus what the government owes its own trust funds). */
        static double debtSubject(Game g) { return g.econ.debt / 100 * g.econ.nominalGdp * 1.25; }

        static boolean weekHas(Game g, int m, int d) { return Economy.weekHas(g, m, d); }

        static void week(Game g) {
            Congress c = g.cong;
            int[] d0 = dateOf(g.day());
            int fy = Hill.fiscalYear(g);
            if (fy != c.fy) c.fy = fy;
            // a new budget cycle each year: the request, then twelve bills for the fiscal year that starts in October
            if (weekHas(g, 0, 2)) c.budgetSent = false;
            if (weekHas(g, 1, 1) && c.crUntil < 0 && !c.shutdown) Arrays.fill(c.approps, 0);
            boolean due = g.week < 20 ? weekHas(g, 3, 15) : weekHas(g, 1, 3);
            if (due && !c.budgetSent) {
                Decisions.add(g, D_BUDGET, "Your budget is due", (g.week < 20 ? "A new president's first budget goes to Congress in the spring." : "The law asks for the president's budget on the first Monday in February.")
                                + " Your budget director has a draft that follows your agenda. You can send it as is, or write your own in the Roosevelt Room (the budget tab).",
                        new String[]{"Send the draft", "I'll write it myself"}, new String[]{"Budget director: \"It funds your priorities and keeps the deficit about where it is.\"", "Chief of staff: \"Your call. It goes to Congress when you hit send.\""});
            }
            if ((g.week < 20 ? weekHas(g, 4, 13) : weekHas(g, 2, 3)) && !c.budgetSent) autoRequest(g);
            // appropriations: May through September
            boolean divided = !Hill.majority(g, HOUSE, g.party) || !Hill.majority(g, SENATE, g.party);
            int m = d0[1];
            if (m >= 4 && m <= 8 && !c.shutdown) {
                for (int i = 0; i < 12; i++) {
                    if (c.approps[i] >= 3) continue;
                    double p = (divided ? 0.05 : 0.09) * (c.approps[i] == 2 ? 1.3 : 1);
                    if (g.rng.nextDouble() < p) {
                        c.approps[i]++;
                        if (c.approps[i] == 3) g.log(NK_INFO, "Congress passes the " + APPROPS[i] + " spending bill; you sign it", null, 0.3);
                    }
                }
            }
            // the deadline
            if (weekHas(g, 8, 26) && !c.shutdown) deadline(g, divided);
            if (c.crUntil >= 0 && g.week >= c.crUntil && !c.shutdown) {
                c.crUntil = -1;
                if (!divided || g.rng.nextDouble() < 0.7) { omnibus(g); }
                else deadline(g, true);
            }
            if (c.shutdown) shutdownWeek(g);
            debtLimit(g);
        }

        static int unfinished(Congress c) { int n = 0; for (int a : c.approps) if (a < 3) n++; return n; }

        static void deadline(Game g, boolean divided) {
            Congress c = g.cong;
            int left = unfinished(c);
            if (left == 0) { g.log(NK_GOOD, "All twelve spending bills are law before the deadline, the first time in decades", null, 1.6); g.perf += 0.5; return; }
            int minority = g.party == DEM ? 100 - g.senateD : g.senateD;
            if (!divided && minority >= 41 && !g.filibusterGone && g.rng.nextDouble() < 0.12 + 0.2 * g.era.polarization + Math.max(0, 48 - g.approval) * 0.015) {
                Member ml = Hill.leaderOf(g, SENATE, false);
                c.crMinority = true;
                Decisions.add(g, D_CR, "The Senate minority blocks the stopgap", left + " of the 12 spending bills aren't done, and a stopgap needs 60 votes in the Senate. " + (ml != null ? ml.name : "The minority leader")
                                + " says " + PARTIES[Hill.other(g.party)] + " won't supply them unless you extend " + (g.party == REP ? "the health insurance subsidies that expire this year" : "the border and policing money they want") + ".",
                        new String[]{"Give them what they want and sign the stopgap", "Demand a clean stopgap", "Refuse: let them own the shutdown"},
                        new String[]{"Budget director: \"It's a few billion dollars. A shutdown costs more.\"",
                                "Legislative director: \"A few of their moderates will break. Make them choose.\"",
                                "Political director: \"Our voters want us to stand firm. It's a gamble with everyone else.\""});
                return;
            }
            if (!divided) {
                c.crUntil = g.week + 11;
                for (int i = 0; i < 12; i++) if (c.approps[i] < 3) c.approps[i] = 4;
                g.log(NK_INFO, "Congress passes a stopgap to keep the government open until December", left + " of 12 spending bills are unfinished.", 1.0);
                return;
            }
            Member opp = Hill.leaderOf(g, HOUSE, !Hill.majority(g, HOUSE, g.party));
            String who = opp != null ? opp.name : "The other party's leaders";
            Decisions.add(g, D_CR, "The government runs out of money on October 1", left + " of the 12 spending bills aren't done. " + who + " will pass a stopgap to December only with spending cuts attached.",
                    new String[]{"Accept the cuts and sign the stopgap", "Demand a clean stopgap", "Refuse: no deal without full-year funding"},
                    new String[]{"Budget director: \"The cuts are small. A shutdown costs more than they do.\"",
                            "Legislative director: \"They don't want a shutdown either. Call their bluff, but have an exit.\"",
                            "Political director: \"A shutdown fight rallies our side. It's a gamble with everyone else.\""});
        }

        static void crDecision(Game g, int k) {
            Congress c = g.cong;
            Random r = g.rng;
            boolean minority = c.crMinority;
            c.crMinority = false;
            if (k == 0) {
                c.crUntil = g.week + 11;
                for (int i = 0; i < 12; i++) if (c.approps[i] < 3) c.approps[i] = 4;
                g.econ.fiscal += minority ? 0.04 : -0.05;
                for (Bloc b : c.blocs) if (b.party == g.party && (b.econ < -0.3 == (g.party == DEM) || minority && b.hawk > 0.6)) b.rel = Math.max(0, b.rel - 3);
                g.log(NK_INFO, minority ? "You give the minority its price and sign the stopgap; your hardliners fume" : "You sign a stopgap with spending cuts; your party's base grumbles", null, 1.2);
            } else if (k == 1 && r.nextDouble() < (minority ? 0.45 : 0.55) + (g.approval - 45) / 40) {
                c.crUntil = g.week + 11;
                for (int i = 0; i < 12; i++) if (c.approps[i] < 3) c.approps[i] = 4;
                g.perf += 0.6;
                g.capital = Math.min(100, g.capital + 4);
                g.log(NK_GOOD, "The other side blinks: Congress passes a clean stopgap", null, 1.4);
            } else {
                startShutdown(g, minority ? (k == 2 ? 0.5 : 0.42) : (k == 2 ? 0.6 : 0.5));
            }
        }

        static void startShutdown(Game g, double blame) {
            Congress c = g.cong;
            c.shutdown = true; c.shutdowns++;
            c.shutdownWeeks = 0;
            c.blame = blame;
            g.log(NK_BIG, "THE GOVERNMENT SHUTS DOWN", "Hundreds of thousands of federal workers are furloughed; national parks close; air travel slows.", 3.5);
        }

        static void shutdownWeek(Game g) {
            Congress c = g.cong;
            c.shutdownWeeks++; c.shutdownTotal++;
            g.econ.demandShock -= 0.08;
            g.perf -= 0.5 * c.blame;
            g.stress = Math.min(100, g.stress + 4);
            if (c.shutdownWeeks % 2 == 1) g.log(NK_BAD, "The shutdown enters week " + c.shutdownWeeks + "; polls show " + Math.round(c.blame * 100) + "% blame the White House", null, 1.6);
            Decisions.add(g, D_SHUTDOWN, "Week " + c.shutdownWeeks + " of the shutdown", "Federal workers are missing paychecks and airports are backing up. About " + Math.round(c.blame * 100) + "% of Americans blame you, the rest blame Congress.",
                    new String[]{"Hold firm", "Cut a deal and reopen the government"},
                    new String[]{"Political director: \"Every week they look worse. A few more days.\"", "Chief of staff: \"End it. Nobody wins a long shutdown.\""});
        }

        static void shutdownDecision(Game g, int k) {
            Congress c = g.cong;
            if (k == 1) { endShutdown(g, false); return; }
            double cave = 0.12 + c.shutdownWeeks * 0.06 + (0.5 - c.blame) * 0.6;
            if (g.rng.nextDouble() < cave) endShutdown(g, true);
            else c.blame = clamp(c.blame + (g.approval < 45 ? 0.03 : -0.02), 0.2, 0.8);
        }

        static void endShutdown(Game g, boolean won) {
            Congress c = g.cong;
            c.shutdown = false;
            c.crUntil = g.week + 10;
            for (int i = 0; i < 12; i++) if (c.approps[i] < 3) c.approps[i] = 4;
            if (won) { g.perf += 1.2; g.capital = Math.min(100, g.capital + 8); g.log(NK_BIG, "SHUTDOWN ENDS ON THE PRESIDENT'S TERMS AFTER " + c.shutdownWeeks + " WEEKS", null, 3.0); }
            else { g.capital = Math.max(0, g.capital - 8); g.econ.fiscal -= 0.05; g.log(NK_BIG, "THE SHUTDOWN ENDS AFTER " + c.shutdownWeeks + " WEEKS WITH A DEAL", "The White House accepted spending cuts to reopen the government.", 2.6); }
        }

        static void omnibus(Game g) {
            Congress c = g.cong;
            for (int i = 0; i < 12; i++) if (c.approps[i] >= 4 || c.approps[i] < 3) c.approps[i] = 3;
            boolean unified = Hill.majority(g, HOUSE, g.party) && Hill.majority(g, SENATE, g.party);
            double share = unified ? 0.5 : 0.2;
            if (c.budgetSent) g.econ.fiscal += share * (c.reqDefense * 0.03 + c.reqNondefense * 0.033);
            g.log(NK_INFO, "Congress passes a year-long spending package and you sign it", null, 1.0);
        }

        // ---- your budget request ----
        static void autoRequest(Game g) {
            Congress c = g.cong;
            c.reqDefense = g.party == REP ? 4 : 1;
            c.reqNondefense = g.party == DEM ? 4 : -6;
            c.reqTax = g.party == DEM ? 0.3 : -0.3;
            c.reqMandatory = 0;
            send(g);
        }

        static void send(Game g) {
            Congress c = g.cong;
            c.budgetSent = true;
            double change = deficitChange(c);
            g.log(NK_INFO, "The White House sends its budget to Congress", "It would " + (change > 0.05 ? "add about " + fmt1(change) + "% of GDP to the deficit" : change < -0.05 ? "cut the deficit by about " + fmt1(-change) + "% of GDP" : "leave the deficit about where it is")
                    + ". Defense " + signed1(c.reqDefense) + "%, other agencies " + signed1(c.reqNondefense) + "%.", 1.2);
            for (Bloc b : c.blocs) {
                if (b.hawk > 0.6 && change > 0.3) b.rel = Math.max(0, b.rel - 3);
                if (b.hawk > 0.6 && change < -0.3) b.rel = Math.min(100, b.rel + 2);
            }
        }

        /** The request's effect on the deficit, % of GDP. */
        static double deficitChange(Congress c) { return c.reqDefense * 0.03 + c.reqNondefense * 0.033 - c.reqTax + c.reqMandatory; }

        /** Ten years of deficits and debt under the request: {deficit[], debt[]} as % of GDP. */
        static double[][] projection(Game g) {
            Econ e = g.econ;
            double[] def = new double[11], debt = new double[11];
            double d = e.debt, rate = e.interest / Math.max(1, e.debt) * 100;
            double primary = e.deficit - e.interest + deficitChange(g.cong);
            double growth = (e.potential + e.polGdp + 2.2) / 100;
            for (int y = 0; y <= 10; y++) {
                rate += (e.tenYr - rate) * 0.25;
                double interest = d * rate / 100;
                double dd = primary + 0.08 * y + interest;
                def[y] = dd;
                debt[y] = d;
                d = d + dd - d * growth;
            }
            return new double[][]{def, debt};
        }

        // ---- the debt limit ----
        static void debtLimit(Game g) {
            Congress c = g.cong;
            double subj = debtSubject(g);
            if (c.xWeek < 0 && subj >= c.debtLimit) {
                c.xWeek = g.week + 22;
                g.log(NK_ECON, "Treasury hits the debt limit and starts \"extraordinary measures\"", "The Treasury secretary says the money runs out around " + dateStr(c.xWeek * 7) + ".", 1.6);
            }
            if (c.xWeek < 0) return;
            int left = c.xWeek - g.week;
            if (left == 8 || left == 3) {
                boolean unified = Hill.majority(g, HOUSE, g.party) && Hill.majority(g, SENATE, g.party);
                Decisions.add(g, D_DEBT, "The debt limit: " + left + " weeks to go", "Unless Congress raises the $" + fmt1(c.debtLimit) + " trillion limit by " + dateStr(c.xWeek * 7) + ", the United States can't pay all its bills."
                                + (unified ? " Your party controls Congress, but a handful of fiscal hawks want something in return." : " The other side wants spending cuts in exchange."),
                        new String[]{"Ask for a clean increase", "Trade spending cuts for an increase", "Go to the brink: no negotiating over the full faith and credit"},
                        new String[]{"Treasury secretary: \"Markets are calm now. They won't be in a month.\"",
                                "Budget director: \"Cuts we can live with, and it's over.\"",
                                "Political director: \"They'll fold before they take the blame for a default. Probably.\""});
            }
            if (left == 1) {
                Decisions.add(g, D_DEBT, "The Treasury runs out of room next week", "Talks have failed. Unless the limit goes up within days, the United States will miss payments to bondholders, retirees and soldiers. "
                                + "The other side's terms are still on the table.",
                        new String[]{"Take their deal", "Invoke the 14th Amendment and keep paying", "Refuse: let it happen"},
                        new String[]{"Treasury secretary: \"Take it. A default would be a catastrophe for decades.\"",
                                "Counsel: \"The Constitution says the public debt shall not be questioned. The courts may agree. Markets won't wait to find out.\"",
                                "Chief of staff: \"Nobody who has sat in that chair has ever let this happen.\""}).a = 1;
            }
            if (left <= 0 && !c.defaulted) {
                c.defaulted = true;
                g.econ.finShock -= 4.5;
                g.perf -= 6;
                g.log(NK_BIG, "THE UNITED STATES MISSES PAYMENTS AS THE DEBT LIMIT BINDS", "Stocks plunge and Treasury yields spike. Congress rushes to raise the limit.", 5);
                raise(g, 1.0);
            }
        }

        static void debtDecision(Game g, int k, boolean lastChance) {
            Congress c = g.cong;
            Random r = g.rng;
            boolean unified = Hill.majority(g, HOUSE, g.party) && Hill.majority(g, SENATE, g.party);
            if (c.xWeek < 0) return;
            if (lastChance) {
                if (k == 0) { raise(g, 1.0); g.econ.fiscal -= 0.2; g.capital = Math.max(0, g.capital - 12); g.perf -= 0.8; for (Bloc b : c.blocs) if (b.party == g.party) b.rel = Math.max(0, b.rel - 4); g.log(NK_BIG, "A LAST-MINUTE DEAL RAISES THE DEBT LIMIT", "The White House gave up more than it wanted to.", 2.6); }
                else if (k == 1) { raise(g, 1.5); g.econ.finShock -= 1.6; g.perf -= 1.5; g.scandal += 4; g.log(NK_BIG, "PRESIDENT INVOKES THE 14TH AMENDMENT TO KEEP PAYING THE NATION'S BILLS", "Treasury keeps borrowing past the limit. Lawsuits are filed within hours; bond markets lurch.", 3.6); }
                return;
            }
            if (k == 1) { raise(g, 1.5); g.econ.fiscal -= 0.15; g.capital = Math.max(0, g.capital - 5); g.log(NK_INFO, "A deal: spending cuts for a higher debt limit", null, 1.6); return; }
            double p = k == 0 ? (unified ? 0.85 : 0.35) : (unified ? 0.7 : 0.45);
            if (r.nextDouble() < p) {
                raise(g, 2.0);
                if (k == 2) { g.perf += 1; g.capital = Math.min(100, g.capital + 6); }
                g.log(NK_GOOD, "Congress raises the debt limit" + (k == 0 ? " with no strings attached" : " after a tense standoff"), null, 1.8);
            } else {
                g.econ.finShock -= k == 2 ? 1.2 : 0.6;
                g.log(NK_BAD, "Debt-limit talks collapse; markets slide", null, 1.8);
            }
        }

        /** Raises the limit by enough for this many years of borrowing. */
        static void raise(Game g, double years) {
            Congress c = g.cong;
            double perYear = Math.max(0.8, g.econ.deficit / 100 * g.econ.nominalGdp * 1.25);
            c.debtLimit = Math.round((debtSubject(g) + years * perYear) * 10) / 10.0;
            c.xWeek = -1;
            c.defaulted = false;
        }
    }
    // =====================================================================
    //  15.  THE MIDTERMS
    //  They run themselves, mostly on your approval and the economy. From August you can campaign and raise money
    //  for your party's candidates, and the Senate seats up that year are fought state by state.
    // =====================================================================
    static final class Midterms {
        /** Election day: the Tuesday after the first Monday in November, two years in. */
        static int electionDay(Game g) {
            int y = g.era.year + 1;
            for (int d = 2; d <= 8; d++) { int day = dayOfDate(y, 10, d); if (dowOf(day) == 1) return day; }
            return dayOfDate(y, 10, 3);
        }

        static boolean season(Game g) {
            int ed = electionDay(g);
            return !g.cong.midDone && g.day() >= ed - 100 && g.day() <= ed;
        }

        /** The Senate seats up at the midterm: "AB:D" or "AB:R" for the party holding each. */
        static String seatsUp(Game g) {
            if (g.era.midSeats != null) return g.era.midSeats;
            return "";
        }

        static double nationalMargin(Game g, double noise) {
            Econ e = g.econ;
            double camp = 0;
            for (double v : g.cong.campaign) camp += v;
            double mP = 0.5 * (g.approval - 50) - 2.5 + 0.4 * (e.gdp - 2) - 0.3 * (e.unemp - 4.5) + Math.min(1.5, g.funds / 40) + Math.min(1.0, camp * 0.05) + noise;
            return mP;
        }

        static void week(Game g) {
            if (g.cong.midDone) return;
            int ed = electionDay(g);
            if (g.day() + 7 > ed && g.day() <= ed) run(g);
            else if (g.day() + 7 > ed - 63 && g.day() <= ed - 63) g.log(NK_INFO, "Campaign season: the midterms are nine weeks away", "You can now schedule campaign stops for your party's candidates from your calendar.", 1.2);
        }

        static void run(Game g) {
            Congress c = g.cong;
            Random r = g.rng;
            c.midDone = true;
            double mP = nationalMargin(g, r.nextGaussian() * 1.8);
            c.midMargin = mP;
            double natD = g.party == DEM ? mP : -mP;
            int hB = g.myHouse(), sB = g.mySenate();
            boolean houseBefore = Hill.majority(g, HOUSE, g.party), senBefore = Hill.majority(g, SENATE, g.party);
            g.houseD = clampi((int) Math.round(222 + 3.8 * natD + r.nextGaussian() * 5), 120, 330);
            int dUp = 0, dWon = 0;
            StringBuilder flips = new StringBuilder();
            String up = seatsUp(g);
            if (!up.isEmpty()) for (String tok : up.split(" ")) {
                String[] p = tok.split(":");
                int s = stateIdx(p[0]);
                if (s < 0) continue;
                boolean dHeld = p[1].equals("D");
                if (dHeld) dUp++;
                double m = Elect.lean[s] + natD + (dHeld ? 3 : -3) + c.campaign[s] * 1.2 * (g.party == DEM ? 1 : -1) + r.nextGaussian() * 3.5;
                if (m > 0) dWon++;
                if (dHeld != m > 0) flips.append(flips.length() > 0 ? ", " : "").append(STATES[s].name).append(m > 0 ? " (D gain)" : " (R gain)");
            }
            g.senateD = clampi(g.senateD - dUp + dWon, 0, 100);
            Hill.seats(g);
            boolean houseAfter = Hill.majority(g, HOUSE, g.party), senAfter = Hill.majority(g, SENATE, g.party);
            Leaders.reshuffle(g, houseBefore != houseAfter, senBefore != senAfter);
            String head = (mP >= 0 ? "Your party gains" : "Your party loses") + " in the midterms";
            String body = "The national House vote went " + (g.party == DEM ? margin(natD) : margin(natD)) + ". " + PARTIES[g.party] + " now hold " + g.myHouse() + " House seats (" + signedInt(g.myHouse() - hB) + ") and "
                    + g.mySenate() + " Senate seats (" + signedInt(g.mySenate() - sB) + ")." + (flips.length() > 0 ? " Senate seats that changed hands: " + flips + "." : "")
                    + (houseBefore && !houseAfter ? " You have lost the House." : !houseBefore && houseAfter ? " You have won back the House." : "")
                    + (senBefore && !senAfter ? " You have lost the Senate." : !senBefore && senAfter ? " You have won the Senate." : "");
            c.midSummary = body;
            g.log(NK_BIG, head.toUpperCase(), body, 4.5);
            Decisions.info(g, "The Midterm Elections", body + "\n\n" + (houseAfter && senAfter ? "You keep control of Congress for the second half of your term." : !houseAfter && !senAfter ? "Divided government: the other party controls Congress. Expect investigations and hard bargaining." : "A split Congress: one chamber is yours, the other isn't."));
            if (mP < -3) g.perf -= 1;
            Arrays.fill(c.campaign, 0);
            g.funds *= 0.3;
        }

        static String signedInt(int v) { return (v >= 0 ? "+" : "") + v; }
    }

    /** New leaders after a change of control. */
    static final class Leaders {
        static void reshuffle(Game g, boolean house, boolean senate) {
            Congress c = g.cong;
            Random r = new Random(g.set.seed ^ g.week);
            Era e = g.era;
            if (house || senate) {
                for (int i = c.members.size() - 1; i >= 0; i--) {
                    Member m = c.members.get(i);
                    if ((m.role == RO_LEADER || m.role == RO_CHAIR) && (m.chamber == HOUSE && house || m.chamber == SENATE && senate)) c.members.remove(i);
                }
            }
            boolean dHouse = g.houseD >= 218, dSen = g.senateD > 50 || g.senateD == 50 && g.party == DEM;
            if (house) {
                Hill.leader(g, r, HOUSE, dHouse ? DEM : REP, "Speaker of the House", dHouse ? e.speakerD : e.speakerR);
                Hill.leader(g, r, HOUSE, dHouse ? REP : DEM, "House " + (dHouse ? "Republican" : "Democratic") + " leader", dHouse ? e.houseLeaderR : e.houseLeaderD);
                chairs(g, r, HOUSE, dHouse ? DEM : REP);
            }
            if (senate) {
                Hill.leader(g, r, SENATE, dSen ? DEM : REP, "Senate Majority Leader", dSen ? e.leaderD : e.leaderR);
                Hill.leader(g, r, SENATE, dSen ? REP : DEM, "Senate " + (dSen ? "Republican" : "Democratic") + " leader", dSen ? e.leaderR : e.leaderD);
                chairs(g, r, SENATE, dSen ? DEM : REP);
            }
            for (Member m : c.members) if (m.bloc >= c.blocs.size()) m.bloc = 0;
        }

        static void chairs(Game g, Random r, int ch, int maj) {
            String[] names = ch == HOUSE ? CHAIR_H : CHAIR_S;
            for (int k = 0; k < names.length; k++) {
                Member m = Hill.person(g.cong, r, maj, ch);
                m.role = RO_CHAIR; m.committee = names[k];
                m.title = "Chair, " + (ch == HOUSE ? "House " : "Senate ") + names[k];
                m.bloc = Hill.blocOf(g.cong, ch, maj, r.nextDouble() < 0.7 ? 1 : r.nextBoolean() ? 0 : 2);
                Hill.ideo(g.cong, m, r);
                m.rel = maj == g.party ? 55 : 35;
                g.cong.members.add(m);
            }
        }
    }
    // =====================================================================
    //  16.  DECISIONS
    //  Things only the president can decide land in the inbox. Each choice comes with what your advisers say.
    // =====================================================================
    static final int D_SIGN = 1, D_CR = 2, D_SHUTDOWN = 3, D_DEBT = 4, D_BUDGET = 5, D_INFO = 6, D_MUSTPASS = 7;

    static final class Decision implements Serializable {
        private static final long serialVersionUID = 1L;
        int kind, a, b;
        String title, body;
        String[] opts, advice;
        Bill bill;
    }

    static final class Decisions {
        static Decision add(Game g, int kind, String title, String body, String[] opts, String[] advice) {
            Decision d = new Decision();
            d.kind = kind; d.title = title; d.body = body; d.opts = opts; d.advice = advice;
            g.inbox.add(d);
            return d;
        }

        static void info(Game g, String title, String body) { add(g, D_INFO, title, body, new String[]{"OK"}, null); }

        /** Shows the next decision as a modal, if nothing else is on screen. */
        static void show(ResoluteDesk u, Game g) {
            if (g.inbox.isEmpty() || u.modal != null || u.popup != null) return;
            Decision d = g.inbox.remove(0);
            Runnable[] acts = new Runnable[d.opts.length];
            for (int i = 0; i < d.opts.length; i++) { final int k = i; acts[i] = () -> { apply(g, d, k); g.dirty = true; }; }
            Modal m = new Modal();
            m.title = d.title; m.body = d.body; m.buttons = d.opts; m.actions = acts; m.descs = d.advice;
            m.stacked = d.opts.length > 2 || d.advice != null;
            m.col = d.kind == D_INFO ? C_ACCENT : C_GOLD;
            if (d.bill != null) m.w = 720;
            u.modal = m;
            snd(Sfx.OPEN);
        }

        /** What a cautious president would do: used by the test sim and, later, by watch mode. */
        static int auto(Game g, Decision d) {
            switch (d.kind) {
                case D_SIGN: {
                    BillDef b = d.bill.def();
                    boolean mine = d.bill.ours || b.lean == g.party || b.lean < 0;
                    if (mine) return 0;
                    return d.bill.support() >= 60 ? 0 : 1;
                }
                case D_MUSTPASS: return 0;
                case D_CR: return g.rng.nextDouble() < 0.6 ? 0 : 1;
                case D_SHUTDOWN: return g.cong.shutdownWeeks >= 2 || g.cong.blame > 0.5 ? 1 : 0;
                case D_DEBT: return 0;
                default: return 0;
            }
        }

        static void autoAll(Game g) {
            int guard = 0;
            while (!g.inbox.isEmpty() && guard++ < 20) { Decision d = g.inbox.remove(0); apply(g, d, auto(g, d)); }
        }

        /** Spends capital on the swing votes for a bill that's stuck short of a majority. */
        static void autoWhip(Game g) {
            int made = 0;
            for (Bill b : g.cong.bills) {
                if (!b.ours || b.stage < ST_COMMITTEE || b.stage > ST_FLOOR2 || made >= 2) continue;
                int ch = b.chamberNow();
                if (Legis.chance(g, b, ch, false) >= 0.6) continue;
                for (Member m : g.cong.members) {
                    if (made >= 2 || g.capital < 12) break;
                    if (m.role != RO_SWING || m.chamber != ch || m.lastDeal == g.week) continue;
                    int kind = m.price == PRICE_PROJECT ? Legis.DL_PROJECT : m.price == PRICE_APPOINT ? Legis.DL_APPOINT : m.price == PRICE_ENDORSE && m.party == g.party ? Legis.DL_ENDORSE : Legis.DL_CALL;
                    Legis.deal(g, b, m, kind);
                    made++;
                }
                if (!b.publicPush && g.capital > 20 && b.support() > 55) Legis.deal(g, b, null, Legis.DL_PUBLIC);
            }
        }

        /** Sends the president's next agenda bill to Congress when fewer than two are moving. */
        static void autoAgenda(Game g) {
            int active = 0;
            for (Bill b : g.cong.bills) if (b.ours && b.stage > ST_DRAFT && b.stage < ST_LAW && b.stage != ST_DEAD) active++;
            if (active >= 2) return;
            for (BillDef d : HillUI.agenda(g)) {
                if (d.lean >= 0 && d.lean != g.party) continue;
                if (HillUI.inPlay(g, d)) continue;
                boolean tried = false;
                for (Bill b : g.cong.bills) if (b.ours && b.defId.equals(d.id)) tried = true;
                if (tried) continue;
                Bill b = Legis.draft(g, d);
                b.recon = d.budget && Legis.reconOk(g, b);
                if (Legis.chance(g, b, HOUSE, false) * Legis.chance(g, b, SENATE, false) < 0.05) { b.size = 0.6; b.scope = 0.2; }
                if (Legis.chance(g, b, HOUSE, false) * Legis.chance(g, b, SENATE, false) < 0.03) continue;
                Legis.introduce(g, b);
                return;
            }
        }

        static void apply(Game g, Decision d, int k) {
            switch (d.kind) {
                case D_SIGN: Legis.decide(g, d.bill, k); break;
                case D_MUSTPASS: Legis.decide(g, d.bill, k == 0 ? 0 : 1); break;
                case D_CR: Budget.crDecision(g, k); break;
                case D_SHUTDOWN: Budget.shutdownDecision(g, k); break;
                case D_DEBT: Budget.debtDecision(g, k, d.a == 1); break;
                case D_BUDGET: if (k == 0) Budget.autoRequest(g); break;
                default: break;
            }
        }
    }

    // =====================================================================
    //  20.  USER INTERFACE: window, input, widgets
    // =====================================================================
    static final int SC_HOME = 0, SC_SETUP = 1, SC_PICK = 2, SC_CREATE = 3, SC_OVAL = 4, SC_ROOM = 5, SC_PAPER = 6, SC_VP = 7,
            SC_HALL = 8, SC_END = 9;

    static final String SANS = pickFont(new String[]{"Inter", "Segoe UI", "Helvetica Neue", "SF Pro Text", "Roboto", "Noto Sans",
            "Open Sans", "Lato", "DejaVu Sans", "Liberation Sans", "Arial"}, "SansSerif");
    static final String SERIF = pickFont(new String[]{"Georgia", "Playfair Display", "Merriweather", "Cambria", "DejaVu Serif",
            "Liberation Serif", "Times New Roman"}, "Serif");
    static final String MONO = pickFont(new String[]{"JetBrains Mono", "Cascadia Mono", "Consolas", "Menlo", "SF Mono",
            "DejaVu Sans Mono", "Liberation Mono", "Courier New"}, "Monospaced");

    static String pickFont(String[] want, String fallback) {
        try {
            String[] have = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
            for (String w : want) for (String h : have) if (h.equalsIgnoreCase(w)) return h;
        } catch (Throwable t) { /* headless or no fonts */ }
        return fallback;
    }

    static final Font F_HERO = new Font(SERIF, Font.BOLD, 78);
    static final Font F_DISPLAY = new Font(SERIF, Font.BOLD, 40);
    static final Font F_SERIF = new Font(SERIF, Font.BOLD, 24);
    static final Font F_SERIF_S = new Font(SERIF, Font.ITALIC, 15);
    static final Font F_H1 = new Font(SANS, Font.BOLD, 24);
    static final Font F_H2 = new Font(SANS, Font.BOLD, 17);
    static final Font F_H3 = new Font(SANS, Font.BOLD, 13);
    static final Font F_B = new Font(SANS, Font.PLAIN, 13);
    static final Font F_BB = new Font(SANS, Font.BOLD, 13);
    static final Font F_S = new Font(SANS, Font.PLAIN, 12);
    static final Font F_SB = new Font(SANS, Font.BOLD, 12);
    static final Font F_XS = new Font(SANS, Font.PLAIN, 11);
    static final Font F_XSB = new Font(SANS, Font.BOLD, 11);
    static final Font F_T = new Font(SANS, Font.PLAIN, 10);
    static final Font F_TB = new Font(SANS, Font.BOLD, 10);
    static final Font F_M = new Font(MONO, Font.PLAIN, 12);
    static final Font F_MB = new Font(MONO, Font.BOLD, 12);
    static final Font F_MS = new Font(MONO, Font.PLAIN, 10);
    static final Font F_NUM = new Font(SANS, Font.BOLD, 21);
    static final Font F_NUMXL = new Font(SANS, Font.BOLD, 34);

    interface DFn { void on(double v); }

    interface IFn { void on(int i); }

    interface SFn { void on(String s); }

    interface Painter { void paint(Graphics2D g, double x, double y, double w, double h); }

    static ResoluteDesk APP;
    JFrame frame;
    javax.swing.Timer timer;
    int screen = SC_HOME, prevScreen = SC_HOME;
    double screenT;                         // seconds since the screen changed (for intro animations)
    Game game;
    Settings setup = new Settings();
    double sc = 1, ox, oy;
    int winW = DW, winH = DH;
    double mx = -1, my = -1, pressX, pressY;
    boolean pressed;
    double tsec;
    long lastNanos;

    static final class Hit {
        double x, y, w, h;
        Runnable act;
        DFn dragX;
        double dragX0, dragW;
        String tip, lab;
        int layer;

        boolean in(double px, double py) { return px >= x && py >= y && px < x + w && py < y + h; }
    }

    final List<Hit> hits = new ArrayList<Hit>();
    final Map<String, double[]> scrolls = new HashMap<String, double[]>();   // id -> {x,y,w,h,offset,max,target}
    String tip;
    Hit dragging;
    int layer;                              // 0 normal, 1 popup, 2 modal: only the top layer takes clicks
    int topLayer;

    // text editing
    String editId;
    StringBuilder editBuf = new StringBuilder();
    SFn editSet;

    // toasts
    static final class Toast { String text; Color col; double t0; }

    final List<Toast> toasts = new ArrayList<Toast>();

    void toast(String s, Color c) {
        for (Toast o : toasts) if (o.text.equals(s)) { o.t0 = tsec; return; }
        Toast t = new Toast();
        t.text = s; t.col = c == null ? C_ACCENT : c; t.t0 = tsec;
        toasts.add(t);
        while (toasts.size() > 5) toasts.remove(0);
    }

    // modal
    static final class Modal {
        String title, body;
        String[] buttons, descs;
        Runnable[] actions;
        Color col = C_ACCENT;
        Painter painter;
        double w = 660, extraH;
        boolean stacked;                     // options as full-width stacked buttons with descriptions
        Cand face;                           // portrait shown at the left
    }

    Modal modal;
    final List<Modal> modalQueue = new ArrayList<Modal>();

    // popup menu
    static final class Popup {
        double x, y, w;
        String title;
        String[] items, tips, right;
        Color[] cols;
        boolean[] enabled;
        IFn pick;
        int hover = -1;
    }

    Popup popup;

    ResoluteDesk() {
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

    void go(int s) {
        if (s != screen) { prevScreen = screen; screen = s; screenT = 0; popup = null; editId = null; }
    }

    void update(double dt) {
        if (game != null && (screen == SC_OVAL || screen == SC_ROOM)) Oval.update(this, dt);
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
            case SC_PAPER: if (k == KeyEvent.VK_ESCAPE || k == KeyEvent.VK_SPACE) go(SC_OVAL); break;
            case SC_SETUP: case SC_PICK: case SC_CREATE: case SC_VP:
                if (k == KeyEvent.VK_ESCAPE) Screens.back(this);
                break;
            default: break;
        }
    }

    void commitEdit() { editId = null; }

    // ---- widgets ----------------------------------------------------------------------------------------
    boolean hover(double x, double y, double w, double h) {
        return dragging == null && layer == topLayer && mx >= x && my >= y && mx < x + w && my < y + h;
    }

    Hit hit(double x, double y, double w, double h, Runnable a, String t) {
        Hit hh = new Hit();
        hh.x = x; hh.y = y; hh.w = w; hh.h = h; hh.act = a; hh.tip = t; hh.layer = layer;
        hits.add(hh);
        if (t != null && hover(x, y, w, h)) tip = t;
        return hh;
    }

    /** A horizontal drag region (sliders). */
    void dragRegion(double x, double y, double w, double h, double x0, double ww, DFn f, String t) {
        Hit hh = hit(x, y, w, h, null, t);
        hh.dragX = f; hh.dragX0 = x0; hh.dragW = ww;
    }

    static final int B_NORM = 0, B_PRIMARY = 1, B_DANGER = 2, B_ON = 3, B_GHOST = 4, B_TAB = 5, B_TABON = 6, B_OFF = 7, B_GOLD = 8,
            B_DEM = 9, B_REP = 10, B_SUBTLE = 11;

    boolean button(Graphics2D g, double x, double y, double w, double h, String label, int style, Runnable act) {
        return button(g, x, y, w, h, label, style, act, null);
    }

    boolean button(Graphics2D g, double x, double y, double w, double h, String label, int style, Runnable act, String tipText) {
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

    static final int IC_UP = 0, IC_DOWN = 1, IC_CLOSE = 2, IC_LEFT = 3, IC_RIGHT = 4;

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
    void panel(Graphics2D g, double x, double y, double w, double h, String title) {
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
    boolean card(Graphics2D g, double x, double y, double w, double h, boolean selected, Runnable act, String t) {
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

    /** Letter-spaced small caps label. */
    static void label(Graphics2D g, String s, double x, double y, Color c) {
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

    static double labelWidth(Graphics2D g, String s) {
        FontMetrics fm = g.getFontMetrics(F_TB);
        double w = 0;
        for (char c : s.toUpperCase().toCharArray()) w += fm.charWidth(c) + 1.2;
        return w;
    }

    /** A rounded chip; returns its width. */
    static double chip(Graphics2D g, String s, double x, double y, Color c, boolean filled) {
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

    static void text(Graphics2D g, String s, double x, double y, Font f, Color c) {
        g.setFont(f);
        g.setColor(c);
        g.drawString(s, (float) x, (float) y);
    }

    static void textR(Graphics2D g, String s, double x, double y, Font f, Color c) {
        g.setFont(f);
        g.setColor(c);
        g.drawString(s, (float) (x - g.getFontMetrics().stringWidth(s)), (float) y);
    }

    static void textC(Graphics2D g, String s, double x, double y, Font f, Color c) {
        g.setFont(f);
        g.setColor(c);
        g.drawString(s, (float) (x - g.getFontMetrics().stringWidth(s) / 2.0), (float) y);
    }

    /** Text that is shortened with an ellipsis to fit. */
    static void textFit(Graphics2D g, String s, double x, double y, double w, Font f, Color c) {
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

    static double strW(Graphics2D g, String s, Font f) { return g.getFontMetrics(f).stringWidth(s); }

    /** Draws word-wrapped text; returns the height used. */
    static double wrap(Graphics2D g, String s, double x, double y, double w, Font f, Color c) {
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

    static double wrapHeight(Graphics2D g, String s, double w, Font f) {
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

    static void bar(Graphics2D g, double x, double y, double w, double h, double frac, Color c) {
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
    static void sbar(Graphics2D g, double x, double y, double w, double h, double v, Color neg, Color pos) {
        g.setColor(new Color(255, 255, 255, 14));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, h, h));
        double cx = x + w / 2;
        double len = w / 2 * clamp(Math.abs(v), 0, 1);
        g.setColor(v < 0 ? neg : pos);
        g.fill(new Rectangle2D.Double(v < 0 ? cx - len : cx, y, len, h));
        g.setColor(new Color(255, 255, 255, 90));
        g.draw(new Line2D.Double(cx, y - 2, cx, y + h + 2));
    }

    /** A horizontal slider; value in [0,1]. */
    void slider(Graphics2D g, double x, double y, double w, double v, Color c, DFn set, String t) { slider(g, x, y, w, v, c, set, t, true); }

    void slider(Graphics2D g, double x, double y, double w, double v, Color c, DFn set, String t, boolean fill) {
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
    double stepper(Graphics2D g, double x, double y, String value, Runnable minus, Runnable plus, String t, double vw) {
        button(g, x, y, 24, 24, "-", B_NORM, minus, t);
        g.setColor(new Color(0, 0, 0, 70));
        g.fill(new RoundRectangle2D.Double(x + 27, y, vw, 24, 6, 6));
        textC(g, value, x + 27 + vw / 2, y + 16.5, F_SB, C_TEXT);
        button(g, x + 30 + vw, y, 24, 24, "+", B_NORM, plus, t);
        return 54 + vw;
    }

    /** A pill toggle. */
    void toggle(Graphics2D g, double x, double y, boolean on, Runnable act, String t) {
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
    void tabs(Graphics2D g, double x, double y, double w, double h, String[] names, int cur, IFn pick) {
        double tw = w / names.length;
        for (int i = 0; i < names.length; i++) {
            final int k = i;
            button(g, x + i * tw, y, tw - 2, h, names[i], i == cur ? B_TABON : B_TAB, () -> pick.on(k));
        }
        g.setColor(new Color(255, 255, 255, 20));
        g.draw(new Line2D.Double(x, y + h, x + w, y + h));
    }

    /** A segmented control. */
    void segmented(Graphics2D g, double x, double y, double w, double h, String[] names, int cur, IFn pick, String[] tips) {
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
    void textField(Graphics2D g, String id, double x, double y, double w, double h, String value, SFn set) {
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

    /** Simple multi-series line chart. */
    static void chart(Graphics2D g, double x, double y, double w, double h, List<double[]> series, Color[] cols, String[] names, double lo, double hi, String fmtY, int n) {
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

    /** Begins a scrollable region; returns the current scroll offset. */
    double beginScroll(Graphics2D g, String id, double x, double y, double w, double h) {
        double[] s = scrolls.get(id);
        if (s == null) { s = new double[]{x, y, w, h, 0, 0, 0}; scrolls.put(id, s); }
        s[0] = x; s[1] = y; s[2] = w; s[3] = h;
        g.setClip(new Rectangle2D.Double(x, y, w, h));
        return Math.round(s[4]);
    }

    void endScroll(Graphics2D g, String id, double contentH) {
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

    void resetScroll(String id) { double[] s = scrolls.get(id); if (s != null) { s[4] = 0; s[6] = 0; } }

    /** Hits outside the visible part of a scroll region are dropped (call after drawing the list). */
    void clipHits(int from, double y0, double y1) {
        for (int i = hits.size() - 1; i >= from; i--) {
            Hit h = hits.get(i);
            if (h.y + h.h < y0 || h.y > y1) hits.remove(i);
            else if (h.y < y0) { h.h -= y0 - h.y; h.y = y0; }
            else if (h.y + h.h > y1) h.h = y1 - h.y;
        }
    }

    // ---- modal ------------------------------------------------------------------------------------------
    void modal(String title, String body, String[] buttons, Runnable[] actions, Color col) {
        Modal m = new Modal();
        m.title = title; m.body = body; m.buttons = buttons; m.actions = actions; m.col = col == null ? C_ACCENT : col;
        if (modal == null) modal = m; else modalQueue.add(m);
        snd(Sfx.OPEN);
    }

    void info(String title, String body) { modal(title, body, new String[]{"OK"}, new Runnable[]{null}, null); }

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
    Popup openPopup(double x, double y, double w, String title, String[] items, IFn pick) {
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

    // ---- backgrounds ------------------------------------------------------------------------------------------------
    static BufferedImage bgCache;

    /** Deep navy war-room backdrop with a faint grid and vignette (cached). */
    void backdrop(Graphics2D g) {
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
    BufferedImage snapshot(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        winW = w; winH = h;
        render(g, w, h);
        g.dispose();
        return img;
    }

    // =====================================================================
    //  21.  PORTRAITS  -  stylised, animated caricatures drawn from each candidate's Look
    // =====================================================================
    static final class Portrait {
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
        static void draw(Graphics2D g, Cand c, double x, double y, double w, double h, double t, double talk, boolean still, boolean framed) {
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

    // =====================================================================
    //  22.  THE MAP OF THE UNITED STATES
    // =====================================================================
    static final int MM_BLANK = 0, MM_MONEY = 1, MM_INTERNAL = 2, MM_PUBLIC = 3, MM_FIELD = 4, MM_CAL = 5, MM_BALLOT = 6;
    static final String[] CALLOUTS = {"VT", "NH", "MA", "RI", "CT", "NJ", "DE", "MD", "DC"};

    static final class USMap {
        static Path2D[] shape = new Path2D[NS];
        static Rectangle2D[] bounds = new Rectangle2D[NS];
        static volatile boolean ready;
        static Color[] cur = new Color[NS];
        static double[] hoverAmt = new double[NS];
        static int hoverState = -1;
        static final double AW = 975, AH = 610;

        static int dec(char c) { int v = c; if (v > 92) v--; return v - 35; }

        static int num(String s, int i, int n) { int v = 0; for (int k = 0; k < n; k++) v = v * 90 + dec(s.charAt(i + k)); return v; }

        static synchronized void load() {
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

        static void animate(double dt) {
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
    // =====================================================================
    //  23.  MENUS: the title screen, a new presidency, choosing a president and a running mate
    // =====================================================================
    static final Color C_IVORY = rgb(0xf3ead7), C_WOOD = rgb(0x5a3520), C_NAVY = rgb(0x14254a);

    static final class Screens {
        static int pickParty = DEM, sel = -1, vpSel = -1;
        static String status;

        static void header(ResoluteDesk u, Graphics2D g, String title, String sub) {
            u.backdrop(g);
            text(g, title, 48, 62, F_DISPLAY, Color.white);
            if (sub != null) text(g, sub, 50, 88, F_B, C_DIM);
            g.setColor(new Color(255, 255, 255, 20));
            g.draw(new Line2D.Double(48, 104, DW - 48, 104));
        }

        static void back(ResoluteDesk u) {
            switch (u.screen) {
                case SC_SETUP: u.go(SC_HOME); break;
                case SC_PICK: u.go(SC_SETUP); break;
                case SC_CREATE: u.go(SC_PICK); break;
                case SC_VP: u.go(SC_PICK); break;
                default: u.go(SC_HOME);
            }
        }

        /** The North Portico at night. */
        static void portico(Graphics2D g, double cx, double base, double sc, double t) {
            double w = 620 * sc, h = 150 * sc, x = cx - w / 2, y = base - h;
            g.setColor(new Color(10, 16, 30));
            g.fill(new Rectangle2D.Double(x - 60 * sc, base, w + 120 * sc, 14 * sc));
            g.setPaint(new GradientPaint((float) x, (float) y, rgb(0xe9e1cf), (float) x, (float) base, rgb(0xb9b2a2)));
            g.fill(new Rectangle2D.Double(x, y, w, h));
            // windows, lit
            for (int row = 0; row < 2; row++) for (int i = 0; i < 9; i++) {
                double wx = x + 24 * sc + i * 66 * sc, wy = y + 22 * sc + row * 64 * sc;
                if (wx + 26 * sc > cx - 140 * sc && wx < cx + 140 * sc) continue;
                double fl = 0.75 + 0.25 * Math.sin(t * 0.7 + i * 1.3 + row);
                g.setColor(new Color(255, (int) (196 + 30 * fl), (int) (110 + 40 * fl)));
                g.fill(new Rectangle2D.Double(wx, wy, 26 * sc, 38 * sc));
                g.setColor(new Color(120, 110, 90));
                g.draw(new Line2D.Double(wx + 13 * sc, wy, wx + 13 * sc, wy + 38 * sc));
            }
            // the portico: columns and a pediment
            double px = cx - 120 * sc, pw = 240 * sc, py = y - 34 * sc;
            g.setColor(rgb(0xefe8d8));
            Path2D ped = new Path2D.Double();
            ped.moveTo(px - 14 * sc, py + 26 * sc); ped.lineTo(cx, py - 34 * sc); ped.lineTo(px + pw + 14 * sc, py + 26 * sc); ped.closePath();
            g.fill(ped);
            g.fill(new Rectangle2D.Double(px - 14 * sc, py + 26 * sc, pw + 28 * sc, 16 * sc));
            g.setColor(rgb(0xfff6d6));
            g.fill(new Rectangle2D.Double(px, py + 42 * sc, pw, h + 34 * sc - 42 * sc));
            for (int i = 0; i < 4; i++) {
                double colx = px + 12 * sc + i * (pw - 36 * sc) / 3;
                g.setPaint(new GradientPaint((float) colx, 0, rgb(0xd8d0bd), (float) (colx + 12 * sc), 0, rgb(0xffffff)));
                g.fill(new Rectangle2D.Double(colx, py + 42 * sc, 13 * sc, base - py - 42 * sc));
            }
            g.setColor(rgb(0xffd27a));
            g.fill(new Rectangle2D.Double(cx - 18 * sc, base - 60 * sc, 36 * sc, 60 * sc));
            // the flag on the roof
            double fx = cx, fy = py - 34 * sc;
            g.setColor(rgb(0xcfcfcf));
            g.fill(new Rectangle2D.Double(fx - 1.2 * sc, fy - 70 * sc, 2.4 * sc, 70 * sc));
            for (int k = 0; k < 7; k++) {
                g.setColor(k % 2 == 0 ? rgb(0xb22234) : Color.white);
                Path2D st = new Path2D.Double();
                double wave = Math.sin(t * 2 + k * 0.3) * 2 * sc;
                st.moveTo(fx, fy - 70 * sc + k * 4 * sc);
                st.lineTo(fx + 48 * sc, fy - 70 * sc + k * 4 * sc + wave);
                st.lineTo(fx + 48 * sc, fy - 66 * sc + k * 4 * sc + wave);
                st.lineTo(fx, fy - 66 * sc + k * 4 * sc);
                st.closePath();
                g.fill(st);
            }
            g.setColor(rgb(0x3c3b6e));
            g.fill(new Rectangle2D.Double(fx, fy - 70 * sc, 20 * sc, 16 * sc));
        }

        static void home(ResoluteDesk u, Graphics2D g) {
            double t = u.tsec;
            g.setPaint(new GradientPaint(0, 0, rgb(0x050a18), 0, DH, rgb(0x16254a)));
            g.fill(new Rectangle2D.Double(0, 0, DW, DH));
            Random st = new Random(7);
            for (int i = 0; i < 160; i++) {
                double sx = st.nextDouble() * DW, sy = st.nextDouble() * 520, tw = 0.5 + 0.5 * Math.sin(t * 1.5 + i);
                g.setColor(new Color(255, 255, 255, (int) (40 + 90 * tw * st.nextDouble())));
                g.fill(new Ellipse2D.Double(sx, sy, 1.6, 1.6));
            }
            g.setPaint(new GradientPaint(0, 640, new Color(0, 0, 0, 0), 0, DH, new Color(0, 0, 0, 160)));
            g.fill(new Rectangle2D.Double(0, 520, DW, DH - 520));
            portico(g, 1120, 700, 1.25, t);
            g.setColor(rgb(0x0c1424));
            g.fill(new Rectangle2D.Double(0, 700, DW, DH - 700));
            double in = easeOut(Math.min(1, u.screenT * 1.4));
            double tx = 80 - (1 - in) * 30;
            label(g, "A PRESIDENCY SIMULATOR", tx + 4, 170, C_GOLD);
            Font big = new Font(SERIF, Font.BOLD, 96);
            g.setFont(big);
            g.setColor(C_IVORY);
            g.drawString("RESOLUTE", (float) tx, 262f);
            g.setPaint(new GradientPaint((float) tx, 280, rgb(0xf2c14e), (float) (tx + 400), 380, rgb(0xd49a2a)));
            g.drawString("DESK", (float) tx, 360f);
            text(g, "Four years, or eight, in the Oval Office. Govern, negotiate, survive the crises, and answer to history.", tx + 4, 400, F_SERIF_S, C_DIM);
            double by = 460;
            u.button(g, tx, by, 340, 54, "New presidency", B_GOLD, () -> { u.setup = new Settings(); u.go(SC_SETUP); });
            by += 66;
            boolean has = SaveGame.exists(SaveGame.AUTO);
            u.button(g, tx, by, 340, 46, has ? "Continue: " + SaveGame.describe(SaveGame.AUTO) : "Continue", has ? B_PRIMARY : B_OFF, has ? () -> SaveGame.load(u, SaveGame.AUTO) : null);
            by += 58;
            boolean slot = SaveGame.exists(SaveGame.SLOT);
            u.button(g, tx, by, 340, 46, "Load saved game", slot ? B_NORM : B_OFF, slot ? () -> SaveGame.load(u, SaveGame.SLOT) : null, slot ? SaveGame.describe(SaveGame.SLOT) : "No saved game yet.");
            by += 58;
            u.button(g, tx, by, 340, 46, "Quit", B_GHOST, () -> { if (u.frame != null) u.frame.dispose(); });
            text(g, "Everything that happens in a game is fiction. Real people appear as stylised characters; the game never quotes them.", 80, DH - 30, F_XS, C_FAINT);
            if (status != null) text(g, status, 80, DH - 52, F_XS, C_AMBER);
        }

        // ---- a new presidency --------------------------------------------------------------------------------
        static void setup(ResoluteDesk u, Graphics2D g) {
            Settings s = u.setup;
            header(u, g, "A New Presidency", "Choose when you take office and how the country looks on day one.");
            Era cur = era(s.year);
            double ax = 48, aw = 480;
            u.panel(g, ax, 124, aw, 690, "When you take office");
            double y = 168;
            for (Era e : ERAS) {
                final Era ee = e;
                boolean on = e.year == s.year;
                u.card(g, ax + 18, y, aw - 36, 150, on, () -> { s.year = ee.year; s.presId = null; s.vpId = null; s.custom = null; }, null);
                text(g, String.valueOf(e.year), ax + 36, y + 42, new Font(SERIF, Font.BOLD, 34), on ? C_GOLD : Color.white);
                text(g, e.name, ax + 140, y + 30, F_H2, Color.white);
                text(g, e.fictional ? "A projected future" : "Inauguration day, January 20", ax + 140, y + 50, F_XS, e.fictional ? C_AMBER : C_DIM);
                wrap(g, e.blurb, ax + 36, y + 76, aw - 72, F_XS, C_TEXT);
                y += 162;
            }
            wrap(g, "Earlier start years, every inauguration from 1933 on with that era's Congress, economy, leaders and issues, arrive in a later update.", ax + 18, y + 10, aw - 36, F_XS, C_FAINT);
            // conditions
            double bx = ax + aw + 16, bw = 560;
            u.panel(g, bx, 124, bw, 690, "Starting conditions");
            y = 168;
            text(g, "Scenario", bx + 18, y, F_H3, C_TEXT);
            u.segmented(g, bx + 18, y + 10, bw - 36, 30, new String[]{SCEN[0], SCEN[1], SCEN[2]}, s.scenario < 3 ? s.scenario : -1, i -> s.scenario = i, new String[]{SCEN_DESC[0], SCEN_DESC[1], SCEN_DESC[2]});
            u.segmented(g, bx + 18, y + 46, bw - 36, 30, new String[]{SCEN[3], SCEN[4], SCEN[5]}, s.scenario >= 3 ? s.scenario - 3 : -1, i -> s.scenario = i + 3, new String[]{SCEN_DESC[3], SCEN_DESC[4], SCEN_DESC[5]});
            wrap(g, SCEN_DESC[s.scenario], bx + 18, y + 96, bw - 36, F_XS, C_DIM);
            y += 130;
            text(g, "Difficulty", bx + 18, y + 20, F_H3, C_TEXT);
            u.segmented(g, bx + 120, y, bw - 138, 30, new String[]{"Easy", "Normal", "Hard"}, s.difficulty, i -> s.difficulty = i,
                    new String[]{"A friendlier public and a forgiving Congress.", "The job as it is.", "Less patience from voters, the press and Congress."});
            y += 60;
            label(g, "Custom conditions (optional)", bx + 18, y, C_GOLD);
            y += 18;
            y = condition(u, g, bx, bw, y, "Approval", s.cApproval, cur.approvalBase + 5, 25, 75, "%", v -> s.cApproval = v, () -> s.cApproval = Double.NaN);
            y = condition(u, g, bx, bw, y, "Unemployment", s.cUnemp, cur.unemp, 2.5, 14, "%", v -> s.cUnemp = v, () -> s.cUnemp = Double.NaN);
            y = condition(u, g, bx, bw, y, "Inflation", s.cInfl, cur.infl, -1, 12, "%", v -> s.cInfl = v, () -> s.cInfl = Double.NaN);
            y = condition(u, g, bx, bw, y, "Growth", s.cGdp, cur.gdp, -4, 6, "%", v -> s.cGdp = v, () -> s.cGdp = Double.NaN);
            y += 8;
            text(g, "Your party's seats", bx + 18, y + 16, F_S, C_TEXT);
            int ph = s.cHouse, ps = s.cSenate;
            u.stepper(g, bx + 200, y, ph < 0 ? "House: era" : "House: " + ph, () -> s.cHouse = Math.max(120, (s.cHouse < 0 ? 218 : s.cHouse) - 5), () -> s.cHouse = Math.min(330, (s.cHouse < 0 ? 218 : s.cHouse) + 5), "Your party's seats in the House (218 is a majority).", 110);
            u.stepper(g, bx + 380, y, ps < 0 ? "Senate: era" : "Senate: " + ps, () -> s.cSenate = Math.max(25, (s.cSenate < 0 ? 50 : s.cSenate) - 1), () -> s.cSenate = Math.min(75, (s.cSenate < 0 ? 50 : s.cSenate) + 1), "Your party's senators (50 plus the vice president is a majority).", 110);
            y += 44;
            u.button(g, bx + 18, y, 200, 30, "Reset to the era", B_GHOST, () -> { s.cApproval = Double.NaN; s.cUnemp = Double.NaN; s.cInfl = Double.NaN; s.cGdp = Double.NaN; s.cHouse = -1; s.cSenate = -1; });
            // the era at a glance
            double cx = bx + bw + 16, cw = DW - 48 - cx;
            u.panel(g, cx, 124, cw, 690, cur.year + " at a glance");
            y = 170;
            String[][] rows = {{"Unemployment", fmt1(cur.unemp) + "%"}, {"Inflation", fmt1(cur.infl) + "%"}, {"Growth", fmt1(cur.gdp) + "%"}, {"Fed funds rate", fmt1(cur.fed) + "%"},
                    {"10-year Treasury", fmt1(cur.tenYr) + "%"}, {"Stocks (S&P 500)", thousands(Math.round(cur.stocks))}, {"Gas", "$" + fmt2(cur.gas)},
                    {"Debt held by the public", Math.round(cur.debt) + "% of GDP"}, {"Deficit", fmt1(cur.deficit) + "% of GDP"}, {"Fed chair", cur.fedChair}, {"Outgoing president", cur.predecessor}};
            for (String[] r : rows) { text(g, r[0], cx + 18, y, F_S, C_TEXT); textR(g, r[1], cx + cw - 18, y, F_BB, Color.white); y += 26; }
            y += 10;
            label(g, "Congress on day one", cx + 18, y, C_GOLD);
            y += 24;
            if (cur.houseD >= 0) {
                text(g, "House: " + cur.houseD + " D - " + (435 - cur.houseD) + " R", cx + 18, y, F_S, C_TEXT); y += 22;
                text(g, "Senate: " + cur.senateD + " D - " + (100 - cur.senateD) + " R", cx + 18, y, F_S, C_TEXT); y += 22;
                text(g, "Speaker " + (cur.houseD >= 218 ? cur.speakerD : cur.speakerR) + "; Senate leader " + (cur.senateD >= 51 ? cur.leaderD : cur.leaderR), cx + 18, y, F_XS, C_DIM);
            } else wrap(g, "Depends on who won: your party starts with about 232 House seats and 51 senators as a Democrat, or 229 and 52 as a Republican.", cx + 18, y, cw - 36, F_XS, C_TEXT);
            u.button(g, 48, 832, 180, 44, "Back", B_GHOST, () -> u.go(SC_HOME));
            u.button(g, DW - 48 - 300, 832, 300, 44, "Choose your president", B_GOLD, () -> { sel = -1; Cand w = era(s.year).winnerId != null ? candById(era(s.year).winnerId) : null; pickParty = w != null ? w.party : DEM; u.go(SC_PICK); });
        }

        static double condition(ResoluteDesk u, Graphics2D g, double bx, double bw, double y, String name, double v, double def, double lo, double hi, String unit, DFn set, Runnable reset) {
            boolean custom = !Double.isNaN(v);
            double val = custom ? v : def;
            text(g, name, bx + 18, y + 16, F_S, C_TEXT);
            u.slider(g, bx + 150, y + 12, bw - 290, (val - lo) / (hi - lo), custom ? C_GOLD : C_FAINT, x -> set.on(Math.round((lo + x * (hi - lo)) * 10) / 10.0), "Drag to set; the era's value until you do.");
            textR(g, custom ? fmt1(val) + unit : "era (" + fmt1(def) + unit + ")", bx + bw - 18, y + 16, F_XSB, custom ? C_GOLD : C_DIM);
            return y + 34;
        }

        // ---- choosing a president ------------------------------------------------------------------------------
        static void pick(ResoluteDesk u, Graphics2D g) {
            Settings s = u.setup;
            Era e = era(s.year);
            header(u, g, "Choose Your President", e.year == 2025 ? "Donald Trump won in 2024. Play him, or anyone else as a what-if." : "Who won the 2028 election? Pick them, or create your own president.");
            List<Cand> all = eraRoster(e);
            List<Cand> l = new ArrayList<Cand>();
            for (Cand c : all) if (c.party == pickParty) l.add(c);
            u.tabs(g, 48, 120, 400, 34, new String[]{"Democrats", "Republicans"}, pickParty, i -> { pickParty = i; sel = -1; });
            double lx = 48, lw = 1000, top = 166, h = 646;
            double sy = u.beginScroll(g, "pick", lx, top, lw, h);
            int h0 = u.hits.size();
            int cols = 4;
            double cw = (lw - 20 - (cols - 1) * 10) / cols, ch = 104;
            for (int i = 0; i <= l.size(); i++) {
                double x = lx + (i % cols) * (cw + 10), yy = top - sy + (i / cols) * (ch + 10);
                if (i == 0) {
                    u.card(g, x, yy, cw, ch, false, () -> { Creator.begin(u, pickParty); }, "Design your own president.");
                    textC(g, "+", x + 46, yy + 64, new Font(SANS, Font.BOLD, 40), C_GOLD);
                    text(g, "Create your own", x + 92, yy + 46, F_BB, Color.white);
                    text(g, "Name, record and strengths", x + 92, yy + 66, F_XS, C_DIM);
                    continue;
                }
                Cand c = l.get(i - 1);
                final int k = i - 1;
                boolean on = sel == k;
                u.card(g, x, yy, cw, ch, on, () -> sel = k, null);
                Portrait.draw(g, c, x + 10, yy + 10, 70, 84, u.tsec, 0, true, true);
                text(g, c.name(), x + 92, yy + 30, F_BB, on ? C_GOLD : Color.white);
                wrap(g, c.role, x + 92, yy + 50, cw - 100, F_XS, C_DIM);
                if (e.winnerId != null && c.id.equals(e.winnerId)) chip(g, "WON THE ELECTION", x + 92, yy + 80, C_GOLD, true);
            }
            u.clipHits(h0, top, top + h);
            u.endScroll(g, "pick", ((l.size() + cols) / cols) * (ch + 10));
            double px = lx + lw + 16, pw = DW - 48 - px;
            u.panel(g, px, 124, pw, 690, "The president");
            Cand c = sel >= 0 && sel < l.size() ? l.get(sel) : null;
            if (c == null) wrap(g, "Pick a president on the left, or create your own.", px + 20, 170, pw - 40, F_S, C_DIM);
            else candidatePanel(u, g, c, px, pw);
            u.button(g, 48, 832, 180, 44, "Back", B_GHOST, () -> u.go(SC_SETUP));
            u.button(g, DW - 48 - 300, 832, 300, 44, "Next: your running mate", c != null ? B_GOLD : B_OFF, c != null ? () -> { s.presId = c.id; s.custom = null; s.vpId = defaultVp(e, c); vpSel = -1; u.go(SC_VP); } : null);
        }

        static final int[] SHOW_ATTR = {A_CHA, A_SPE, A_DEB, A_INT, A_DIS, A_EXE, A_FPC, A_EST, A_XOV, A_STA, A_SCA};
        static final String[] SHOW_ATTR_NAME = {"Charisma", "Speeches", "Debating", "Policy depth", "Discipline", "Executive experience", "Foreign policy", "Ties in Washington", "Crossover appeal", "Stamina", "Scandal risk"};

        static void candidatePanel(ResoluteDesk u, Graphics2D g, Cand c, double px, double pw) {
            Portrait.draw(g, c, px + 20, 160, 130, 156, u.tsec, 0, false, true);
            text(g, c.name(), px + 166, 186, F_H2, Color.white);
            chip(g, PARTY[c.party].toUpperCase(), px + 166, 198, partyColor(c.party), true);
            wrap(g, c.role, px + 166, 236, pw - 186, F_XS, C_DIM);
            if (c.born > 0) text(g, "Born " + c.born + (c.homeAb != null ? ", " + c.homeAb : ""), px + 166, 290, F_XS, C_DIM);
            double y = 336;
            if (c.bio != null) y += 10 + wrap(g, c.bio, px + 20, y, pw - 40, F_XS, C_TEXT);
            for (int i = 0; i < SHOW_ATTR.length; i++) {
                int v = c.a[SHOW_ATTR[i]];
                text(g, SHOW_ATTR_NAME[i], px + 20, y + 12, F_XS, C_TEXT);
                bar(g, px + 170, y + 4, pw - 230, 9, v / 100.0, SHOW_ATTR[i] == A_SCA ? C_AMBER : C_ACCENT);
                textR(g, String.valueOf(v), px + pw - 20, y + 12, F_XSB, Color.white);
                y += 19;
            }
            if (c.pros != null && y < 720) { y += 8; y += 4 + wrap(g, "+ " + c.pros, px + 20, y, pw - 40, F_XS, C_GOOD); }
            if (c.cons != null && y < 760) wrap(g, "- " + c.cons, px + 20, y, pw - 40, F_XS, C_AMBER);
        }

        // ---- the running mate -------------------------------------------------------------------------------
        static void vp(ResoluteDesk u, Graphics2D g) {
            Settings s = u.setup;
            Era e = era(s.year);
            Cand p = s.custom != null ? s.custom : candById(s.presId);
            header(u, g, "Your Vice President", p == null ? "" : "Who serves with " + p.name() + "? You can give them missions and portfolios once in office.");
            List<Cand> l = new ArrayList<Cand>();
            for (Cand c : eraRoster(e)) if (p != null && c.party == p.party && c != p) l.add(c);
            if (vpSel < 0 && s.vpId != null) for (int i = 0; i < l.size(); i++) if (l.get(i).id.equals(s.vpId)) vpSel = i;
            double lx = 48, lw = 1000, top = 124, h = 690;
            double sy = u.beginScroll(g, "vp", lx, top, lw, h);
            int h0 = u.hits.size();
            int cols = 4;
            double cw = (lw - 20 - (cols - 1) * 10) / cols, ch = 104;
            for (int i = 0; i < l.size(); i++) {
                Cand c = l.get(i);
                final int k = i;
                double x = lx + (i % cols) * (cw + 10), yy = top - sy + (i / cols) * (ch + 10);
                boolean on = vpSel == k;
                u.card(g, x, yy, cw, ch, on, () -> { vpSel = k; s.vpId = l.get(k).id; }, null);
                Portrait.draw(g, c, x + 10, yy + 10, 70, 84, u.tsec, 0, true, true);
                text(g, c.name(), x + 92, yy + 30, F_BB, on ? C_GOLD : Color.white);
                wrap(g, c.role, x + 92, yy + 50, cw - 100, F_XS, C_DIM);
            }
            u.clipHits(h0, top, top + h);
            u.endScroll(g, "vp", ((l.size() + cols - 1) / cols) * (ch + 10));
            double px = lx + lw + 16, pw = DW - 48 - px;
            u.panel(g, px, 124, pw, 690, "Vice president");
            Cand c = vpSel >= 0 && vpSel < l.size() ? l.get(vpSel) : null;
            if (c != null) candidatePanel(u, g, c, px, pw);
            else wrap(g, "Pick a running mate.", px + 20, 170, pw - 40, F_S, C_DIM);
            u.button(g, 48, 832, 180, 44, "Back", B_GHOST, () -> u.go(s.custom != null ? SC_CREATE : SC_PICK));
            u.button(g, DW - 48 - 300, 832, 300, 44, "Take the oath", c != null ? B_GOLD : B_OFF, c != null ? () -> start(u) : null);
        }

        static void start(ResoluteDesk u) {
            Settings s = u.setup;
            s.seed = System.nanoTime();
            Game g = Setup.newGame(s);
            u.game = g;
            Oval.begin(u);
        }
    }

    // ---- create your own president -------------------------------------------------------------------------
    static final class Creator {
        static Cand c;
        static int arch;
        static int[] spent = new int[Screens.SHOW_ATTR.length];
        static final int POINTS = 40;

        static void begin(ResoluteDesk u, int party) {
            c = new Cand();
            c.id = "custom"; c.custom = true; c.first = "Alex"; c.last = "Morgan"; c.party = party; c.role = "Governor"; c.homeAb = "PA"; c.home = stateIdx("PA"); c.born = u.setup.year - 58;
            c.faction = party == DEM ? F_LIB : F_GOV;
            c.econ = FACTIONS[c.faction].econ; c.cult = FACTIONS[c.faction].cult; c.plat = FACTIONS[c.faction].pos.clone();
            arch = 0;
            Arrays.fill(spent, 0);
            apply();
            c.look = randomLook(new Random(System.nanoTime()));
            u.go(SC_CREATE);
        }

        static void apply() {
            String[] t = ARCH_BASE[arch].trim().split("\\s+");
            for (int i = 0; i < NA; i++) c.a[i] = Integer.parseInt(t[i]);
            for (int i = 0; i < spent.length; i++) c.a[Screens.SHOW_ATTR[i]] = clampi(c.a[Screens.SHOW_ATTR[i]] + spent[i] * 3 * (Screens.SHOW_ATTR[i] == A_SCA ? -1 : 1), 5, 99);
            c.role = ARCHETYPE[arch];
        }

        static Look randomLook(Random r) {
            Look l = new Look();
            l.female = r.nextBoolean();
            l.skin = r.nextInt(8); l.hair = r.nextInt(8); l.style = l.female ? 6 + r.nextInt(4) : r.nextInt(5);
            l.gray = r.nextDouble() * 0.6; l.facial = l.female ? 0 : r.nextInt(3) == 0 ? 1 : 0; l.glasses = r.nextInt(4) == 0 ? 1 : 0;
            l.faceW = 0.92 + r.nextDouble() * 0.16; l.jaw = 0.3 + r.nextDouble() * 0.5; l.suit = r.nextInt(6); l.tie = r.nextInt(9); l.age = 0.4 + r.nextDouble() * 0.4; l.smile = 0.4 + r.nextDouble() * 0.5;
            l.earrings = l.female && r.nextBoolean(); l.lipstick = l.female && r.nextBoolean();
            return l;
        }

        static int left() { int n = 0; for (int v : spent) n += Math.abs(v); return POINTS - n; }

        static void draw(ResoluteDesk u, Graphics2D g) {
            Screens.header(u, g, "Create Your President", "Choose a background, then spend " + POINTS + " points on strengths. Lowering a rating gives a point back.");
            u.panel(g, 48, 124, 520, 690, "Who they are");
            double y = 170;
            text(g, "First name", 68, y + 18, F_S, C_TEXT);
            u.textField(g, "cfirst", 180, y, 360, 28, c.first, v -> c.first = v);
            y += 40;
            text(g, "Last name", 68, y + 18, F_S, C_TEXT);
            u.textField(g, "clast", 180, y, 360, 28, c.last, v -> c.last = v);
            y += 44;
            text(g, "Party", 68, y + 18, F_S, C_TEXT);
            u.segmented(g, 180, y, 360, 30, new String[]{"Democrat", "Republican"}, c.party, i -> { c.party = i; c.faction = i == DEM ? F_LIB : F_GOV; c.econ = FACTIONS[c.faction].econ; c.cult = FACTIONS[c.faction].cult; }, null);
            y += 44;
            text(g, "Home state", 68, y + 18, F_S, C_TEXT);
            u.button(g, 180, y, 360, 30, STATES[c.home].name, B_NORM, () -> {
                String[] names = new String[NS];
                for (int i = 0; i < NS; i++) names[i] = STATES[i].name;
                u.openPopup(180, 260, 360, "Home state", names, i -> { c.home = i; c.homeAb = STATES[i].ab; });
            });
            y += 44;
            text(g, "Born", 68, y + 18, F_S, C_TEXT);
            u.stepper(g, 180, y, String.valueOf(c.born), () -> c.born = Math.max(u.setup.year - 85, c.born - 1), () -> c.born = Math.min(u.setup.year - 35, c.born + 1), "Presidents must be at least 35.", 90);
            textR(g, "age " + (u.setup.year - c.born) + " on inauguration day", 540, y + 18, F_XS, C_DIM);
            y += 48;
            text(g, "Background", 68, y + 18, F_S, C_TEXT);
            u.button(g, 180, y, 360, 30, ARCHETYPE[arch], B_NORM, () -> u.openPopup(180, y0(), 360, "Background", ARCHETYPE, i -> { arch = i; apply(); }));
            y += 52;
            Portrait.draw(g, c, 68, y, 150, 180, u.tsec, 0, false, true);
            u.button(g, 236, y + 10, 200, 30, "New look", B_NORM, () -> c.look = randomLook(new Random(System.nanoTime())));
            u.button(g, 236, y + 50, 200, 30, c.look.female ? "Woman" : "Man", B_GHOST, () -> { c.look.female = !c.look.female; c.look.style = c.look.female ? 6 + c.look.style % 4 : c.look.style % 5; });
            // ratings
            u.panel(g, 584, 124, DW - 48 - 584, 690, "Strengths (" + left() + " points left)");
            double ry = 176;
            for (int i = 0; i < Screens.SHOW_ATTR.length; i++) {
                final int k = i;
                int v = c.a[Screens.SHOW_ATTR[i]];
                text(g, Screens.SHOW_ATTR_NAME[i], 604, ry + 18, F_S, C_TEXT);
                bar(g, 800, ry + 10, 420, 10, v / 100.0, Screens.SHOW_ATTR[i] == A_SCA ? C_AMBER : C_ACCENT);
                text(g, String.valueOf(v), 1232, ry + 19, F_BB, Color.white);
                u.button(g, 1290, ry, 40, 28, "-", B_NORM, () -> { if (spent[k] > -4 && (spent[k] > 0 || left() > 0)) { spent[k]--; apply(); } });
                u.button(g, 1338, ry, 40, 28, "+", B_NORM, () -> { if (spent[k] < 8 && (spent[k] < 0 || left() > 0)) { spent[k]++; apply(); } });
                ry += 44;
            }
            wrap(g, "Scandal risk works the other way: spending points lowers it.", 604, ry + 14, 700, F_XS, C_DIM);
            u.button(g, 48, 832, 180, 44, "Back", B_GHOST, () -> u.go(SC_PICK));
            u.button(g, DW - 48 - 300, 832, 300, 44, "Next: your running mate", B_GOLD, () -> {
                u.setup.custom = c; u.setup.presId = null; u.setup.vpId = defaultVp(era(u.setup.year), c); Screens.vpSel = -1; u.go(SC_VP);
            });
        }

        static double y0() { return 330; }
    }
    // =====================================================================
    //  24.  THE OVAL OFFICE
    //  The home screen: click the desk, the windows, the doors, the globe or the papers to go where the work is.
    // =====================================================================
    static final int R_DESK = 0, R_COUNTRY = 1, R_ECON = 2, R_RESIDENCE = 3, R_WESTWING = 4, R_CONGRESS = 5, R_SITROOM = 6, R_PRESS = 7, NROOM = 8;
    static final String[] ROOM_NAME = {"The Resolute Desk", "The Country", "The Economy", "The Residence", "The West Wing", "The Roosevelt Room", "The Situation Room", "The Press Briefing Room"};
    static final String[] ROOM_SUB = {"Your week, slot by slot", "Approval, by issue, group and state", "Jobs, prices, markets and the budget", "Your health, your sleep and your family",
            "Your staff and your cabinet", "Congress: the leaders, the votes, the budget", "The world: allies, rivals and crises", "The press corps and the media"};
    static final boolean[] ROOM_OPEN = {true, true, true, true, false, true, false, false};
    static final String[] ROOM_LATER = {"", "", "", "",
            "Your chief of staff, national security adviser, press secretary and counsel, each with skills, traits and loyalty; all 15 departments and their secretaries; cabinet meetings; leaks and resignations; and your vice president's missions.",
            "Named leaders, committee chairs and swing votes; bills you negotiate provision by provision; the filibuster, reconciliation and the Byrd rule; your budget, the 12 spending bills, shutdowns, the debt ceiling and vetoes.",
            "About forty countries and their leaders; calls, summits, state visits, treaties, aid and sanctions; operations planned with the Pentagon; covert action; alliances; and the crises that start overseas.",
            "Daily briefings by your press secretary, press conferences with named reporters, interviews, your own social media, the State of the Union and addresses to the nation."};

    static final class Oval {
        static int hot = -1;
        static double tick;

        static void begin(ResoluteDesk u) {
            Game g = u.game;
            u.go(SC_OVAL);
            snd(Sfx.CHEER);
            String vp = g.vp != null ? " Your vice president is " + g.vp.name() + "." : "";
            u.modal("The Oath of Office", "\"I do solemnly swear that I will faithfully execute the Office of President of the United States, and will to the best of my ability, preserve, protect and defend the Constitution of the United States.\"\n\n"
                            + "It is noon on " + dateLong(0) + ". You are " + g.pres.name() + ", the president of the United States." + vp + "\n\n"
                            + "Plan your week at the Resolute Desk, check the country through the windows and the economy in the briefing book, then end the week. This version covers your schedule, your health, the economy and public opinion; Congress, the cabinet, the world and the press arrive in later updates.",
                    new String[]{"Get to work"}, new Runnable[]{null}, C_GOLD);
            SaveGame.save(u, SaveGame.AUTO);
        }

        static void update(ResoluteDesk u, double dt) { tick += dt * 60; if (u.game != null) Decisions.show(u, u.game); }

        static void key(ResoluteDesk u, KeyEvent e) {
            int k = e.getKeyCode();
            if (k == KeyEvent.VK_SPACE) endWeek(u);
            if (k >= KeyEvent.VK_1 && k <= KeyEvent.VK_8) Rooms.open(u, k - KeyEvent.VK_1);
            if (k == KeyEvent.VK_P) u.go(SC_PAPER);
        }

        static void endWeek(ResoluteDesk u) {
            Game g = u.game;
            if (g == null || g.over || u.modal != null) return;
            if (!g.inbox.isEmpty()) { Decisions.show(u, g); return; }
            Week.resolve(g);
            snd(Sfx.TURN);
            SaveGame.save(u, SaveGame.AUTO);
            if (g.over) {
                u.modal("The End of the Term", "Your four years are up. Re-election, the second term and the verdict of history arrive in a later update. For now, thank you for your service, Mr. or Madam President.",
                        new String[]{"Main menu"}, new Runnable[]{() -> u.go(SC_HOME)}, C_GOLD);
                return;
            }
            if (g.stamina < 25) u.toast("You're exhausted. Schedule rest this week.", C_BAD);
            u.go(SC_PAPER);
        }

        // ---- the top bar, shared by every room ----
        static void topBar(ResoluteDesk u, Graphics2D g, Game game) {
            g.setColor(new Color(8, 13, 26, 245));
            g.fill(new Rectangle2D.Double(0, 0, DW, 56));
            g.setColor(new Color(242, 193, 78, 60));
            g.draw(new Line2D.Double(0, 56, DW, 56));
            seal(g, 30, 28, 20);
            text(g, "THE WHITE HOUSE", 60, 24, F_TB, C_GOLD);
            text(g, dateLong(game.day()), 60, 44, F_SB, Color.white);
            double x = 360;
            double last = game.hist.size() >= 2 ? game.hist.get(game.hist.size() - 2)[H_APP] : game.approval;
            stat(g, x, "APPROVAL", Math.round(game.approval) + "%", (game.approval >= last ? "+" : "") + fmt1(game.approval - last), game.approval >= 50 ? C_GOOD : game.approval >= 42 ? Color.white : C_BAD);
            x += 150;
            label(g, "STAMINA", x, 22, C_DIM);
            bar(g, x, 30, 110, 8, game.stamina / 100, game.stamina >= 50 ? C_GOOD : game.stamina >= 25 ? C_AMBER : C_BAD);
            textR(g, String.valueOf(Math.round(game.stamina)), x + 140, 40, F_SB, Color.white);
            x += 170;
            label(g, "STRESS", x, 22, C_DIM);
            bar(g, x, 30, 110, 8, game.stress / 100, game.stress < 50 ? C_ACCENT : game.stress < 75 ? C_AMBER : C_BAD);
            textR(g, String.valueOf(Math.round(game.stress)), x + 140, 40, F_SB, Color.white);
            x += 170;
            stat(g, x, "UNEMPLOYMENT", fmt1(game.econ.unemp) + "%", null, Color.white);
            x += 130;
            stat(g, x, "INFLATION", fmt1(game.econ.infl) + "%", null, game.econ.infl > 4 ? C_AMBER : Color.white);
            x += 110;
            stat(g, x, "GAS", "$" + fmt2(game.econ.gas), null, Color.white);
            x += 90;
            int wk = game.week + 1;
            stat(g, x, "TERM " + game.term, "Week " + wk, "of " + game.termWeeks, C_TEXT);
            u.button(g, DW - 190, 10, 170, 36, game.over ? "Term over" : "End week  >", game.over ? B_OFF : B_GOLD, game.over ? null : () -> endWeek(u), "Play out this week. (Space)");
            u.button(g, DW - 252, 10, 54, 36, "Save", B_GHOST, () -> SaveGame.save(u, SaveGame.SLOT), "Save the game");
            u.button(g, DW - 314, 10, 54, 36, "Menu", B_GHOST, () -> u.modal("Leave the Oval Office?", "Your game is saved automatically every week.", new String[]{"Stay", "Main menu"},
                    new Runnable[]{null, () -> { SaveGame.save(u, SaveGame.AUTO); u.go(SC_HOME); }}, null));
        }

        static void stat(Graphics2D g, double x, String lab, String v, String sub, Color c) {
            label(g, lab, x, 22, C_DIM);
            text(g, v, x, 44, F_H2, c);
            if (sub != null) text(g, sub, x + strW(g, v, F_H2) + 6, 44, F_XS, sub.startsWith("-") ? C_BAD : sub.startsWith("+") ? C_GOOD : C_DIM);
        }

        /** A small presidential seal. */
        static void seal(Graphics2D g, double cx, double cy, double r) {
            g.setColor(rgb(0x1a2f63));
            g.fill(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
            g.setColor(C_GOLD);
            g.setStroke(new BasicStroke((float) Math.max(1, r * 0.08)));
            g.draw(new Ellipse2D.Double(cx - r * 0.92, cy - r * 0.92, r * 1.84, r * 1.84));
            for (int i = 0; i < 13; i++) {
                double a = -Math.PI / 2 + (i - 6) * 0.22;
                double sx = cx + Math.cos(a) * r * 0.7, sy = cy + Math.sin(a) * r * 0.7;
                g.fill(new Ellipse2D.Double(sx - r * 0.05, sy - r * 0.05, r * 0.1, r * 0.1));
            }
            // an eagle, much simplified: body, spread wings and a shield
            g.setColor(rgb(0xe8d9a8));
            Path2D w = new Path2D.Double();
            w.moveTo(cx - r * 0.62, cy - r * 0.05);
            w.curveTo(cx - r * 0.4, cy - r * 0.35, cx - r * 0.15, cy - r * 0.2, cx, cy - r * 0.05);
            w.curveTo(cx + r * 0.15, cy - r * 0.2, cx + r * 0.4, cy - r * 0.35, cx + r * 0.62, cy - r * 0.05);
            w.curveTo(cx + r * 0.35, cy + r * 0.05, cx + r * 0.2, cy + r * 0.1, cx, cy + r * 0.1);
            w.curveTo(cx - r * 0.2, cy + r * 0.1, cx - r * 0.35, cy + r * 0.05, cx - r * 0.62, cy - r * 0.05);
            g.fill(w);
            g.setColor(rgb(0xb22234));
            g.fill(new RoundRectangle2D.Double(cx - r * 0.16, cy - r * 0.08, r * 0.32, r * 0.42, r * 0.1, r * 0.1));
            g.setColor(rgb(0x3c3b6e));
            g.fill(new Rectangle2D.Double(cx - r * 0.16, cy - r * 0.08, r * 0.32, r * 0.12));
            g.setStroke(new BasicStroke(1f));
        }

        // ---- the room itself ----
        static final double[][] SPOT = {
                {650, 430, 300, 170},     // desk
                {585, 120, 430, 255},     // windows
                {1290, 455, 120, 110},    // briefing book (economy)
                {1440, 200, 110, 300},    // residence door
                {50, 200, 110, 300},      // West Wing door
                {1180, 160, 90, 250},     // Roosevelt Room door, beside the windows
                {300, 420, 110, 150},     // globe
                {205, 150, 90, 270},      // press door
                {690, 752, 220, 70}};     // papers on the coffee table
        static final int[] SPOT_ROOM = {R_DESK, R_COUNTRY, R_ECON, R_RESIDENCE, R_WESTWING, R_CONGRESS, R_SITROOM, R_PRESS, -1};
        static final String[] SPOT_LABEL = {"The Resolute Desk: plan your week", "The windows: the country", "The briefing book: the economy", "To the Residence",
                "To the West Wing: staff and cabinet", "To the Roosevelt Room: Congress", "The globe: the Situation Room", "To the Press Briefing Room", "Today's paper"};

        static void draw(ResoluteDesk u, Graphics2D g) {
            Game game = u.game;
            if (game == null) { u.go(SC_HOME); return; }
            scene(u, g, game);
            hot = -1;
            for (int i = 0; i < SPOT.length; i++) {
                double[] s = SPOT[i];
                final int k = i;
                boolean over = u.modal == null && u.hover(s[0], s[1], s[2], s[3]);
                if (over) hot = i;
                u.hit(s[0], s[1], s[2], s[3], () -> { if (SPOT_ROOM[k] >= 0) Rooms.open(u, SPOT_ROOM[k]); else u.go(SC_PAPER); }, null);
            }
            if (hot >= 0) {
                double[] s = SPOT[hot];
                g.setColor(new Color(242, 193, 78, 40));
                g.fill(new RoundRectangle2D.Double(s[0] - 4, s[1] - 4, s[2] + 8, s[3] + 8, 14, 14));
                g.setColor(new Color(242, 193, 78, 170));
                g.setStroke(new BasicStroke(2f));
                g.draw(new RoundRectangle2D.Double(s[0] - 4, s[1] - 4, s[2] + 8, s[3] + 8, 14, 14));
                g.setStroke(new BasicStroke(1f));
                String lab = SPOT_LABEL[hot] + (SPOT_ROOM[hot] >= 0 && !ROOM_OPEN[SPOT_ROOM[hot]] ? "  (coming in a later update)" : "");
                double w = strW(g, lab, F_SB) + 28, lx = clamp(s[0] + s[2] / 2 - w / 2, 10, DW - w - 10), ly = s[1] + s[3] + 12 > 790 ? s[1] - 40 : s[1] + s[3] + 10;
                g.setColor(new Color(8, 13, 26, 235));
                g.fill(new RoundRectangle2D.Double(lx, ly, w, 30, 10, 10));
                text(g, lab, lx + 14, ly + 20, F_SB, C_GOLD);
            }
            topBar(u, g, game);
            weekCard(u, g, game);
            hillCard(u, g, game);
            ticker(u, g, game);
        }

        static void weekCard(ResoluteDesk u, Graphics2D g, Game game) {
            double x = 24, y = 640, w = 300, h = 150;
            g.setColor(new Color(8, 13, 26, 220));
            g.fill(new RoundRectangle2D.Double(x, y, w, h, 12, 12));
            g.setColor(new Color(242, 193, 78, 70));
            g.draw(new RoundRectangle2D.Double(x, y, w, h, 12, 12));
            label(g, "THIS WEEK", x + 14, y + 22, C_GOLD);
            int trips = 0, policy = 0, rest = 0;
            for (int i = 0; i < NSLOT; i++) { int a = game.act[i]; if (a == AC_TRIP) trips++; if (a == AC_POLICY || a == AC_REMARKS) policy++; if (a == AC_REST || a == AC_FAMILY || a == AC_CAMPDAVID) rest++; }
            text(g, policy + " policy slots, " + trips + (trips == 1 ? " trip, " : " trips, ") + rest + " to recharge", x + 14, y + 44, F_S, C_TEXT);
            double ps = Week.projectedStamina(game);
            text(g, "Stamina after the week", x + 14, y + 68, F_XS, C_DIM);
            bar(g, x + 14, y + 74, w - 80, 8, ps / 100, ps >= 50 ? C_GOOD : ps >= 25 ? C_AMBER : C_BAD);
            textR(g, String.valueOf(Math.round(ps)), x + w - 14, y + 83, F_SB, Color.white);
            text(g, "Sleep about " + fmt1(Week.sleepOf(game)) + " hours a night", x + 14, y + 104, F_XS, C_DIM);
            u.button(g, x + 14, y + 114, w - 28, 26, "Plan the week", B_NORM, () -> Rooms.open(u, R_DESK));
        }

        /** What's moving on Capitol Hill, bottom right of the office. */
        static void hillCard(ResoluteDesk u, Graphics2D g, Game game) {
            if (game.cong == null) return;
            Congress c = game.cong;
            double w = 300, h = 150, x = DW - 24 - w, y = 640;
            g.setColor(new Color(8, 13, 26, 220));
            g.fill(new RoundRectangle2D.Double(x, y, w, h, 12, 12));
            g.setColor(new Color(242, 193, 78, 70));
            g.draw(new RoundRectangle2D.Double(x, y, w, h, 12, 12));
            label(g, "ON THE HILL", x + 14, y + 22, C_GOLD);
            double yy = y + 44;
            int n = 0;
            for (int i = c.bills.size() - 1; i >= 0 && n < 3; i--) {
                Bill b = c.bills.get(i);
                if (!b.ours || b.stage < ST_COMMITTEE || b.stage > ST_DESK) continue;
                textFit(g, b.name, x + 14, yy, 160, F_XS, Color.white);
                textR(g, b.held ? "votes short" : STAGE[b.stage], x + w - 14, yy, F_XS, b.held || b.bottled ? C_AMBER : C_ACCENT);
                yy += 18; n++;
            }
            if (n == 0) { text(g, "None of your bills is moving.", x + 14, yy, F_XS, C_DIM); yy += 18; }
            String status = c.shutdown ? "The government is shut down" : c.xWeek >= 0 ? "Debt limit: " + Math.max(0, c.xWeek - game.week) + " weeks of room" : c.crUntil >= 0 ? "Funded by a stopgap until " + dateStr(c.crUntil * 7) : "Government funded";
            text(g, status, x + 14, Math.max(yy, y + 100), F_XS, c.shutdown || c.xWeek >= 0 ? C_AMBER : C_DIM);
            u.button(g, x + 14, y + 114, w - 28, 26, "The Roosevelt Room", B_NORM, () -> Rooms.open(u, R_CONGRESS));
        }

        static void ticker(ResoluteDesk u, Graphics2D g, Game game) {
            double y = DH - 44;
            g.setColor(new Color(6, 10, 20, 250));
            g.fill(new Rectangle2D.Double(0, y, DW, 44));
            g.setColor(rgb(0xb22234));
            g.fill(new Rectangle2D.Double(0, y, 130, 44));
            text(g, "THE LEDGER", 16, y + 27, F_SB, Color.white);
            Shape clip = g.getClip();
            g.clip(new Rectangle2D.Double(140, y, DW - 140, 44));
            StringBuilder b = new StringBuilder();
            for (int i = game.ticker.size() - 1; i >= Math.max(0, game.ticker.size() - 12); i--) b.append(game.ticker.get(i)).append("      |      ");
            String s = b.toString();
            double w = Math.max(400, strW(g, s, F_S));
            double off = (tick * 1.0) % w;
            text(g, s, 150 - off, y + 27, F_S, C_TEXT);
            text(g, s, 150 - off + w, y + 27, F_S, C_TEXT);
            g.setClip(clip);
        }

        static void scene(ResoluteDesk u, Graphics2D g, Game game) {
            double t = u.tsec;
            // the curved back wall
            g.setPaint(new GradientPaint(0, 56, rgb(0xefe3c8), 0, 560, rgb(0xd8c7a3)));
            g.fill(new Rectangle2D.Double(0, 56, DW, 560));
            g.setColor(new Color(255, 255, 255, 30));
            for (int i = 0; i < 9; i++) { double px = 40 + i * 190; g.draw(new Line2D.Double(px, 80, px, 520)); }
            // crown moulding and the oval ceiling's edge
            g.setColor(rgb(0xcdb98e));
            g.fill(new Rectangle2D.Double(0, 56, DW, 18));
            g.setColor(rgb(0xb9a37a));
            g.fill(new Rectangle2D.Double(0, 520, DW, 14));
            // three windows behind the desk, onto the South Lawn
            for (int i = 0; i < 3; i++) {
                double wx = 600 + i * 140, wy = 130, ww = 110, wh = 240;
                g.setPaint(new GradientPaint((float) wx, (float) wy, rgb(0x9cc8ef), (float) wx, (float) (wy + wh), rgb(0xdcefff)));
                g.fill(new Rectangle2D.Double(wx, wy, ww, wh));
                g.setColor(rgb(0x6f9f4f));
                g.fill(new Rectangle2D.Double(wx, wy + wh * 0.72, ww, wh * 0.28));
                if (i == 1) {   // the Washington Monument in the distance
                    g.setColor(rgb(0xf4f1ea));
                    Path2D ob = new Path2D.Double();
                    ob.moveTo(wx + ww / 2 - 6, wy + wh * 0.72); ob.lineTo(wx + ww / 2 - 4, wy + 40); ob.lineTo(wx + ww / 2, wy + 30); ob.lineTo(wx + ww / 2 + 4, wy + 40); ob.lineTo(wx + ww / 2 + 6, wy + wh * 0.72); ob.closePath();
                    g.fill(ob);
                }
                g.setColor(rgb(0xf7f2e6));
                g.setStroke(new BasicStroke(5f));
                g.draw(new Rectangle2D.Double(wx, wy, ww, wh));
                g.setStroke(new BasicStroke(2.5f));
                for (int k = 1; k < 3; k++) g.draw(new Line2D.Double(wx + ww * k / 3, wy, wx + ww * k / 3, wy + wh));
                for (int k = 1; k < 5; k++) g.draw(new Line2D.Double(wx, wy + wh * k / 5, wx + ww, wy + wh * k / 5));
                g.setStroke(new BasicStroke(1f));
                // gold drapes
                for (int side = 0; side < 2; side++) {
                    double dx = side == 0 ? wx - 26 : wx + ww - 4;
                    g.setPaint(new GradientPaint((float) dx, 0, rgb(0xc9962e), (float) (dx + 30), 0, rgb(0xf0c862)));
                    Path2D d = new Path2D.Double();
                    d.moveTo(dx, wy - 16); d.lineTo(dx + 30, wy - 16); d.curveTo(dx + 26, wy + wh * 0.5, dx + 34, wy + wh * 0.8, dx + 32, wy + wh + 30); d.lineTo(dx - 4, wy + wh + 30); d.curveTo(dx + 4, wy + wh * 0.7, dx - 2, wy + wh * 0.4, dx, wy - 16); d.closePath();
                    g.fill(d);
                }
            }
            g.setColor(rgb(0xb4862a));
            g.fill(new Rectangle2D.Double(560, 108, 480, 16));
            // flags either side of the windows
            flag(g, 520, 180, t, true);
            flag(g, 1080, 180, t, false);
            // doors: West Wing (far left), press office, Roosevelt Room, residence (far right)
            door(g, 60, 210, 90, 290, hot == 4);
            door(g, 215, 160, 70, 260, hot == 7);
            door(g, 1190, 170, 70, 240, hot == 5);
            door(g, 1450, 210, 90, 290, hot == 3);
            // portraits
            frame(g, 330, 150, 120, 150, rgb(0x3f4a5e));
            frame(g, 1290, 160, 110, 140, rgb(0x5e4a3f));
            // the floor
            g.setPaint(new GradientPaint(0, 534, rgb(0x7a5230), 0, DH, rgb(0x4c321d)));
            g.fill(new Rectangle2D.Double(0, 534, DW, DH - 534));
            g.setColor(new Color(0, 0, 0, 40));
            for (int i = 0; i < 40; i++) g.draw(new Line2D.Double(i * 44 - 200, 534, i * 44 + 120, DH));
            // the oval rug with the seal
            Color rug = game.party == REP ? rgb(0xb98a2e) : rgb(0x1f3a7a);
            g.setColor(shade(rug, 0.7));
            g.fill(new Ellipse2D.Double(220, 560, 1160, 300));
            g.setColor(rug);
            g.fill(new Ellipse2D.Double(240, 568, 1120, 284));
            g.setColor(new Color(242, 193, 78, 160));
            g.setStroke(new BasicStroke(3f));
            g.draw(new Ellipse2D.Double(270, 580, 1060, 260));
            for (int i = 0; i < 24; i++) {
                double a = i * Math.PI * 2 / 24;
                g.draw(new Line2D.Double(800 + Math.cos(a) * 150, 710 + Math.sin(a) * 40, 800 + Math.cos(a) * 470, 710 + Math.sin(a) * 118));
            }
            g.setStroke(new BasicStroke(1f));
            AffineTransform at = g.getTransform();
            g.translate(800, 710);
            g.scale(1, 0.3);
            seal(g, 0, 0, 120);
            g.setTransform(at);
            // sofas and the coffee table
            sofa(g, 330, 610, 250, true);
            sofa(g, 1020, 610, 250, false);
            g.setColor(rgb(0x3b2414));
            g.fill(new RoundRectangle2D.Double(690, 778, 220, 40, 8, 8));
            g.setColor(rgb(0x5a3a22));
            g.fill(new RoundRectangle2D.Double(690, 770, 220, 18, 8, 8));
            g.setColor(rgb(0xf4f0e6));
            g.fill(new Rectangle2D.Double(730, 760, 70, 14));
            g.fill(new Rectangle2D.Double(812, 762, 64, 12));
            g.setColor(rgb(0x333333));
            g.fill(new Rectangle2D.Double(736, 763, 56, 2));
            // the globe on its stand
            g.setColor(rgb(0x4a2e18));
            g.fill(new Rectangle2D.Double(350, 520, 8, 46));
            g.fill(new Rectangle2D.Double(330, 562, 50, 6));
            g.setPaint(new GradientPaint(320, 440, rgb(0x6fa8c9), 380, 510, rgb(0x2e5f80)));
            g.fill(new Ellipse2D.Double(318, 438, 76, 76));
            g.setColor(rgb(0xa9c77b));
            double spin = (t * 6) % 76;
            for (int k = 0; k < 3; k++) { double cx = 318 + ((spin + k * 26) % 76); g.fill(new Ellipse2D.Double(cx - 8, 456 + k * 14, 16 + k * 3, 10)); }
            g.setColor(rgb(0xb4862a));
            g.setStroke(new BasicStroke(2f));
            g.draw(new Arc2D.Double(312, 432, 88, 88, 200, 320, Arc2D.OPEN));
            g.setStroke(new BasicStroke(1f));
            // the side table and the briefing book
            g.setColor(rgb(0x3b2414));
            g.fill(new Rectangle2D.Double(1296, 500, 108, 64));
            g.setColor(rgb(0x5a3a22));
            g.fill(new Rectangle2D.Double(1290, 492, 120, 12));
            g.setColor(rgb(0x1a2f63));
            g.fill(new Rectangle2D.Double(1312, 470, 76, 22));
            g.setColor(C_GOLD);
            text(g, "CEA", 1334, 486, F_TB, C_GOLD);
            // the Resolute Desk and the chair
            g.setColor(rgb(0x2b1b12));
            g.fill(new RoundRectangle2D.Double(762, 400, 76, 80, 20, 20));
            g.setPaint(new GradientPaint(650, 450, rgb(0x6a4026), 650, 600, rgb(0x3e2414)));
            g.fill(new Rectangle2D.Double(660, 460, 280, 130));
            g.setColor(rgb(0x7a4b2d));
            g.fill(new Rectangle2D.Double(648, 448, 304, 18));
            g.setColor(rgb(0x4a2c18));
            g.fill(new Rectangle2D.Double(752, 480, 96, 106));
            g.setColor(new Color(242, 193, 78, 120));
            g.draw(new Rectangle2D.Double(756, 484, 88, 98));
            seal(g, 800, 528, 26);
            g.setColor(rgb(0x3e2414));
            for (int k = 0; k < 2; k++) g.draw(new Rectangle2D.Double(672 + k * 186, 478, 70, 100));
            g.setColor(rgb(0xf4f0e6));
            g.fill(new Rectangle2D.Double(700, 440, 60, 10));
            g.setColor(rgb(0x222222));
            g.fill(new RoundRectangle2D.Double(866, 438, 36, 12, 4, 4));
            // the president, at the desk
            Portrait.draw(g, game.pres, 770, 360, 60, 72, u.tsec, 0, true, false);
        }

        static void flag(Graphics2D g, double x, double y, double t, boolean us) {
            g.setColor(rgb(0xc9a24a));
            g.fill(new Rectangle2D.Double(x - 2, y - 40, 4, 360));
            g.fill(new Ellipse2D.Double(x - 6, y - 50, 12, 12));
            for (int k = 0; k < 7; k++) {
                g.setColor(us ? (k % 2 == 0 ? rgb(0xb22234) : rgb(0xf5f5f0)) : (k < 4 ? rgb(0x1a2f63) : rgb(0x223c78)));
                Path2D p = new Path2D.Double();
                double sway = Math.sin(t * 0.8 + k * 0.4) * 2;
                p.moveTo(x + 2, y - 30 + k * 26);
                p.curveTo(x + 18, y - 26 + k * 26, x + 10 + sway, y - 10 + k * 26, x + 26 + sway, y - 4 + k * 26);
                p.lineTo(x + 26 + sway, y + 22 + k * 26);
                p.curveTo(x + 10 + sway, y + 16 + k * 26, x + 18, y + 2 + k * 26, x + 2, y - 4 + k * 26);
                p.closePath();
                g.fill(p);
            }
            if (us) { g.setColor(rgb(0x3c3b6e)); g.fill(new Rectangle2D.Double(x + 2, y - 30, 14, 60)); }
            else { g.setColor(C_GOLD); g.fill(new Ellipse2D.Double(x + 6, y + 30, 14, 14)); }
        }

        static void door(Graphics2D g, double x, double y, double w, double h, boolean lit) {
            g.setColor(rgb(0xcdb98e));
            g.fill(new Rectangle2D.Double(x - 10, y - 22, w + 20, h + 22));
            Path2D ped = new Path2D.Double();
            ped.moveTo(x - 16, y - 22); ped.lineTo(x + w / 2, y - 50); ped.lineTo(x + w + 16, y - 22); ped.closePath();
            g.fill(ped);
            g.setPaint(new GradientPaint((float) x, (float) y, lit ? rgb(0xfaf3e1) : rgb(0xf2e9d3), (float) (x + w), (float) (y + h), rgb(0xd9ccad)));
            g.fill(new Rectangle2D.Double(x, y, w, h));
            g.setColor(new Color(0, 0, 0, 40));
            g.draw(new Rectangle2D.Double(x + 8, y + 10, w - 16, h * 0.42));
            g.draw(new Rectangle2D.Double(x + 8, y + h * 0.5, w - 16, h * 0.46));
            g.setColor(C_GOLD);
            g.fill(new Ellipse2D.Double(x + w - 18, y + h * 0.5, 7, 7));
        }

        static void frame(Graphics2D g, double x, double y, double w, double h, Color c) {
            g.setColor(rgb(0xb4862a));
            g.fill(new Rectangle2D.Double(x - 8, y - 8, w + 16, h + 16));
            g.setPaint(new GradientPaint((float) x, (float) y, shade(c, 1.4), (float) x, (float) (y + h), shade(c, 0.6)));
            g.fill(new Rectangle2D.Double(x, y, w, h));
            g.setColor(new Color(255, 230, 190, 60));
            g.fill(new Ellipse2D.Double(x + w * 0.3, y + h * 0.18, w * 0.4, h * 0.42));
        }

        static void sofa(Graphics2D g, double x, double y, double w, boolean left) {
            Color c = rgb(0xe8dcc0);
            g.setColor(shade(c, 0.7));
            g.fill(new RoundRectangle2D.Double(x, y + 30, w, 60, 18, 18));
            g.setColor(c);
            g.fill(new RoundRectangle2D.Double(x, y, w, 48, 20, 20));
            g.setColor(shade(c, 0.88));
            g.fill(new RoundRectangle2D.Double(x + 10, y + 36, w - 20, 34, 14, 14));
            g.setColor(shade(c, 0.8));
            g.fill(new RoundRectangle2D.Double(left ? x - 8 : x + w - 22, y + 18, 30, 68, 12, 12));
            g.fill(new RoundRectangle2D.Double(left ? x + w - 22 : x - 8, y + 18, 30, 68, 12, 12));
        }
    }
    // =====================================================================
    //  25.  THE ROOMS
    // =====================================================================
    static final Color[] AC_COL = {rgb(0x34415a), rgb(0x2f8f5a), rgb(0x2f8f5a), rgb(0x2f8f5a), rgb(0x2f6fe0), rgb(0x7c4dff), rgb(0xd4a52a), rgb(0x1fbf6a), rgb(0x5aa0d8), rgb(0xc8323a), rgb(0x2f8f5a), rgb(0x8a6a1a), rgb(0x55607a)};

    static final class Rooms {
        static int room = R_DESK, pendingSlot = -1, pendingAct = AC_TRIP, mapMode = 0, selState = -1, chartSel = 0;
        static final String[] MAPMODE = {"Approval", "Unemployment", "Gas prices"};

        static void open(ResoluteDesk u, int r) {
            if (r < 0 || r >= NROOM) return;
            room = r;
            pendingSlot = -1;
            u.go(SC_ROOM);
        }

        static void key(ResoluteDesk u, KeyEvent e) {
            int k = e.getKeyCode();
            if (k == KeyEvent.VK_ESCAPE) { if (pendingSlot >= 0) pendingSlot = -1; else u.go(SC_OVAL); }
            if (k == KeyEvent.VK_SPACE) Oval.endWeek(u);
            if (k >= KeyEvent.VK_1 && k <= KeyEvent.VK_8) open(u, k - KeyEvent.VK_1);
        }

        static void press(ResoluteDesk u, double mx, double my) { }

        static void rightClick(ResoluteDesk u, double mx, double my) { }

        static void draw(ResoluteDesk u, Graphics2D g) {
            Game game = u.game;
            if (game == null) { u.go(SC_HOME); return; }
            u.backdrop(g);
            Oval.topBar(u, g, game);
            u.button(g, 24, 70, 200, 32, "<  Oval Office", B_GHOST, () -> u.go(SC_OVAL), "Back to the Oval Office (Esc)");
            text(g, ROOM_NAME[room], 248, 96, F_DISPLAY, Color.white);
            double tabsW = 0;
            for (int r = 0; r < NROOM; r++) tabsW += strW(g, ROOM_TAB[r], F_XSB) + 24;
            double subX = 252 + strW(g, ROOM_NAME[room], F_DISPLAY) + 16;
            textFit(g, ROOM_SUB[room], subX, 94, DW - 48 - tabsW - subX - 16, F_B, C_DIM);
            // room tabs
            double tx = DW - 48;
            for (int r = NROOM - 1; r >= 0; r--) {
                final int k = r;
                String nm = ROOM_TAB[r];
                double w = strW(g, nm, F_XSB) + 20;
                tx -= w + 4;
                u.button(g, tx, 74, w, 26, nm, r == room ? B_TABON : ROOM_OPEN[r] ? B_TAB : B_OFF, ROOM_OPEN[r] ? () -> open(u, k) : null, ROOM_OPEN[r] ? null : "Coming in a later update.");
            }
            switch (room) {
                case R_DESK: desk(u, g, game); break;
                case R_COUNTRY: country(u, g, game); break;
                case R_ECON: econ(u, g, game); break;
                case R_RESIDENCE: residence(u, g, game); break;
                case R_CONGRESS: HillUI.draw(u, g, game); break;
                default: later(u, g, game); break;
            }
        }

        static final String[] ROOM_TAB = {"Desk", "Country", "Economy", "Residence", "West Wing", "Congress", "Situation Room", "Press"};

        // ---- the desk: the week's calendar ----
        static void desk(ResoluteDesk u, Graphics2D g, Game game) {
            double gx = 48, gy = 128, cw = 139, ch = 132;
            u.panel(g, gx - 12, gy - 12, 7 * (cw + 6) + 18, 3 * (ch + 6) + 70, "Week of " + dateStr(game.day()));
            for (int d = 0; d < 7; d++) {
                int day = game.day() + d;
                int[] dt = dateOf(day);
                textC(g, DOW3[dowOf(day)] + " " + MONTHS[dt[1]] + " " + dt[2], gx + d * (cw + 6) + cw / 2, gy + 26, F_SB, dowOf(day) >= 5 ? C_GOLD : Color.white);
                for (int sl = 0; sl < 3; sl++) {
                    int i = d * 3 + sl;
                    double x = gx + d * (cw + 6), y = gy + 38 + sl * (ch + 6);
                    slot(u, g, game, i, x, y, cw, ch);
                }
            }
            for (int sl = 0; sl < 3; sl++) { g.setColor(C_FAINT); }
            double by = gy + 38 + 3 * (ch + 6) + 8;
            u.button(g, gx, by, 170, 30, "Routine week", B_NORM, () -> Week.routine(game), "Policy in the mornings, remarks, a trip and rest on the weekend.");
            u.button(g, gx + 180, by, 170, 30, "Road week", B_NORM, () -> roadWeek(game), "Trips to four swing states. Tiring.");
            u.button(g, gx + 360, by, 170, 30, "Rest week", B_NORM, () -> restWeek(game), "A light schedule and Camp David at the weekend.");
            u.button(g, gx + 540, by, 170, 30, "Clear the week", B_GHOST, () -> { Arrays.fill(game.act, AC_FREE); Arrays.fill(game.arg, -1); });
            text(g, "Click a slot to change it.", gx + 730, by + 20, F_XS, C_DIM);
            // the week ahead
            double px = 1100, pw = DW - 48 - px;
            u.panel(g, px, 116, pw, 356, "The week ahead");
            double ps = Week.projectedStamina(game);
            text(g, "Stamina now", px + 18, 160, F_S, C_TEXT);
            bar(g, px + 150, 151, pw - 230, 10, game.stamina / 100, C_ACCENT);
            textR(g, String.valueOf(Math.round(game.stamina)), px + pw - 18, 160, F_BB, Color.white);
            text(g, "After this week", px + 18, 186, F_S, C_TEXT);
            bar(g, px + 150, 177, pw - 230, 10, ps / 100, ps >= 50 ? C_GOOD : ps >= 25 ? C_AMBER : C_BAD);
            textR(g, String.valueOf(Math.round(ps)), px + pw - 18, 186, F_BB, Color.white);
            text(g, "Sleep", px + 18, 212, F_S, C_TEXT);
            textR(g, fmt1(Week.sleepOf(game)) + " hours a night", px + pw - 18, 212, F_BB, Week.sleepOf(game) < 5.8 ? C_AMBER : Color.white);
            text(g, "Age " + game.presAge() + (game.presAge() > 70 ? ": long days cost you more" : ""), px + 18, 238, F_XS, C_DIM);
            // Air Force One
            label(g, "Air Force One", px + 18, 266, C_GOLD);
            double ty = 286;
            int n = 0;
            for (int i = 0; i < NSLOT; i++) if (isTrip(game.act[i]) && game.arg[i] >= 0) {
                int s = game.arg[i];
                text(g, DOW3[dowOf(game.day() + i / 3)] + " " + SLOT[i % 3].toLowerCase() + ": " + STATES[s].name + (game.act[i] == AC_CAMPAIGN ? " (campaign)" : ""), px + 18, ty, F_XS, C_TEXT);
                textR(g, fmt1(Week.flight(s)) + " hours each way", px + pw - 18, ty, F_XS, Week.flight(s) > 3.5 ? C_AMBER : C_DIM);
                ty += 18;
                if (++n >= 8) break;
            }
            if (n == 0) text(g, "No trips this week.", px + 18, ty, F_XS, C_DIM);
            // the map, for picking trip destinations
            u.panel(g, px, 482, pw, 330, pendingSlot >= 0 ? "Click a state for the trip" : "Where you're going");
            for (int s = 0; s < NS; s++) { USMap.paint[s] = null; USMap.tags[s] = null; }
            for (int i = 0; i < NSLOT; i++) if (isTrip(game.act[i]) && game.arg[i] >= 0) USMap.paint[game.arg[i]] = rgb(0xd4a52a);
            if (pendingSlot >= 0) for (int s = 0; s < NS; s++) if (USMap.paint[s] == null) USMap.paint[s] = mix(C_STATE, marginColor(game.stateApp[s] - 50, 1), 0.5);
            USMap.markState = Week.dc(); USMap.markCand = game.pres;
            double[] f = USMap.draw(u, g, px + 10, 510, pw - 20, 290, -1, true);
            int hs = USMap.stateAt(f, u.mx, u.my);
            if (hs >= 0 && u.my > 510) {
                u.tip = STATES[hs].name + ": approval " + Math.round(game.stateApp[hs]) + "%, " + STATES[hs].ev + " electoral votes, " + fmt1(Week.flight(hs)) + " hours from Washington";
                final int ss = hs;
                if (pendingSlot >= 0) u.hit(px + 10, 510, pw - 20, 290, () -> { game.act[pendingSlot] = pendingAct; game.arg[pendingSlot] = ss; pendingSlot = -1; snd(Sfx.CHIME, 0.5, 1); }, null);
            }
        }

        static void slot(ResoluteDesk u, Graphics2D g, Game game, int i, double x, double y, double w, double h) {
            int a = game.act[i], arg = game.arg[i];
            boolean pend = pendingSlot == i;
            u.card(g, x, y, w, h, pend, () -> choose(u, game, i), AC_DESC[a]);
            g.setColor(AC_COL[a]);
            g.fill(new RoundRectangle2D.Double(x + 8, y + 10, 5, h - 20, 4, 4));
            label(g, SLOT[i % 3].toUpperCase(), x + 20, y + 22, C_DIM);
            text(g, AC_NAME[a], x + 20, y + 46, F_BB, a == AC_FREE ? C_DIM : Color.white);
            String sub = arg >= 0 && AC_ISSUE[a] ? PISSUE[arg] : arg >= 0 && AC_STATE[a] ? STATES[arg].name : pend ? "pick a state on the map" : null;
            if (sub != null) wrap(g, sub, x + 20, y + 64, w - 28, F_XS, pend ? C_GOLD : C_TEXT);
            double c = AC_STAMINA[a];
            if (isTrip(a) && arg >= 0) c -= Math.min(6, Week.flight(arg) * 0.9);
            text(g, (c >= 0 ? "+" : "") + fmt1(c) + " stamina", x + 20, y + h - 14, F_T, c >= 0 ? C_GOOD : C_DIM);
        }

        static void choose(ResoluteDesk u, Game game, int i) {
            boolean weekend = dowOf(game.day() + i / 3) >= 5;
            List<Integer> opts = new ArrayList<Integer>();
            for (int a = 0; a < NAC; a++) {
                if (a == AC_TRAVEL) continue;
                if (a == AC_CAMPDAVID && !weekend) continue;
                if (a == AC_CONGRESS && weekend) continue;
                if (a == AC_WORKLATE && i % 3 != 2) continue;
                if (a == AC_CAMPAIGN && !Midterms.season(game)) continue;
                opts.add(a);
            }
            String[] names = new String[opts.size()];
            for (int k = 0; k < names.length; k++) names[k] = AC_NAME[opts.get(k)];
            u.openPopup(Math.min(u.mx, DW - 300), Math.min(u.my, DH - 420), 280, SLOT[i % 3] + ", " + DOW[dowOf(game.day() + i / 3)], names, k -> {
                int a = opts.get(k);
                if (AC_ISSUE[a]) {
                    String[] iss = new String[NPI];
                    for (int j = 0; j < NPI; j++) iss[j] = PISSUE[j] + "  (" + Math.round(game.issueApp[j]) + "% approve)";
                    u.openPopup(Math.min(u.mx, DW - 340), 140, 320, AC_NAME[a] + ": which issue?", iss, j -> { game.act[i] = a; game.arg[i] = j; });
                } else if (AC_STATE[a]) {
                    pendingSlot = i; pendingAct = a;
                } else { game.act[i] = a; game.arg[i] = -1; }
            });
        }

        static void roadWeek(Game game) {
            Week.routine(game);
            List<Integer> sw = new ArrayList<Integer>();
            for (int s = 0; s < NS; s++) sw.add(s);
            final Game gg = game;
            Collections.sort(sw, (a, b) -> Double.compare(Math.abs(gg.stateApp[a] - 48) - STATES[a].ev * 0.15, Math.abs(gg.stateApp[b] - 48) - STATES[b].ev * 0.15));
            int[] slots = {1, 4, 7, 10};
            for (int k = 0; k < 4; k++) { game.act[slots[k]] = AC_TRIP; game.arg[slots[k]] = sw.get(k); }
        }

        static void restWeek(Game game) {
            Week.routine(game);
            for (int d = 0; d < 5; d++) { game.act[d * 3 + 1] = d % 2 == 0 ? AC_POLICY : AC_FREE; game.arg[d * 3 + 1] = Week.topIssue(game); game.act[d * 3 + 2] = AC_REST; }
            game.act[15] = AC_CAMPDAVID; game.act[16] = AC_CAMPDAVID; game.act[17] = AC_CAMPDAVID; game.act[18] = AC_CAMPDAVID; game.act[19] = AC_CAMPDAVID; game.act[20] = AC_REST;
        }

        // ---- the country: approval ----
        static void country(ResoluteDesk u, Graphics2D g, Game game) {
            double lx = 48, lw = 470;
            u.panel(g, lx, 116, lw, 300, "Approval");
            text(g, Math.round(game.approval) + "%", lx + 18, 186, F_NUMXL, Color.white);
            text(g, "approve of the job you're doing", lx + 18, 206, F_XS, C_DIM);
            textR(g, "Week 1: " + Math.round(game.approval0) + "%", lx + lw - 18, 186, F_S, C_DIM);
            List<double[]> ser = new ArrayList<double[]>();
            double[] a = new double[game.hist.size()];
            for (int i = 0; i < a.length; i++) a[i] = game.hist.get(i)[H_APP];
            ser.add(a);
            ResoluteDesk.chart(g, lx + 40, 224, lw - 60, 170, ser, new Color[]{C_GOLD}, new String[]{"Approval"}, 25, 75, "%.0f%%", Math.max(53, a.length));
            u.panel(g, lx, 426, lw, 386, "By issue (how much it matters)");
            Integer[] idx = new Integer[NPI];
            for (int i = 0; i < NPI; i++) idx[i] = i;
            final Game gg = game;
            Arrays.sort(idx, (p, q) -> Double.compare(gg.issueSal[q], gg.issueSal[p]));
            double y = 464;
            for (int k = 0; k < NPI; k++) {
                int i = idx[k];
                if (game.issueSal[i] < 0.08) continue;
                text(g, PISSUE[i], lx + 18, y, F_XS, C_TEXT);
                bar(g, lx + 170, y - 9, 200, 10, game.issueApp[i] / 100, game.issueApp[i] >= 50 ? C_GOOD : game.issueApp[i] >= 40 ? C_GOLD : C_BAD);
                textR(g, Math.round(game.issueApp[i]) + "%", lx + 410, y, F_XSB, Color.white);
                for (int d = 0; d < Math.min(5, (int) Math.round(game.issueSal[i] * 3)); d++) { g.setColor(C_AMBER); g.fill(new Ellipse2D.Double(lx + 420 + d * 7, y - 7, 5, 5)); }
                y += 24;
                if (y > 800) break;
            }
            // the map
            double mx = lx + lw + 16, mw = 640;
            u.panel(g, mx, 116, mw, 696, "By state");
            u.segmented(g, mx + 18, 150, 360, 28, MAPMODE, mapMode, i -> mapMode = i, null);
            Econ e = game.econ;
            for (int s = 0; s < NS; s++) {
                if (mapMode == 0) { USMap.paint[s] = marginColor((game.stateApp[s] - 50) * (game.party == REP ? -1 : 1), 1); USMap.tags[s] = Math.round(game.stateApp[s]) + "%"; }
                else if (mapMode == 1) { USMap.paint[s] = mix(rgb(0x1f6f4a), rgb(0xc8323a), clamp((e.stUnemp[s] - 2.5) / 6, 0, 1)); USMap.tags[s] = fmt1(e.stUnemp[s]); }
                else { USMap.paint[s] = mix(rgb(0x1f6f4a), rgb(0xc8323a), clamp((e.stGas[s] - e.gas + 0.6) / 2.0, 0, 1)); USMap.tags[s] = "$" + fmt2(e.stGas[s]); }
            }
            USMap.markState = game.location; USMap.markCand = game.pres;
            double[] f = USMap.draw(u, g, mx + 10, 190, mw - 20, 440, selState, true);
            int hs = USMap.stateAt(f, u.mx, u.my);
            if (hs >= 0) { final int ss = hs; u.hit(mx + 10, 190, mw - 20, 440, () -> selState = ss, null); }
            if (mapMode == 0) USMap.scale(g, mx + 18, 644, game.party == REP ? C_DEM : C_REP, C_STATE, game.party == REP ? C_REP : C_DEM, "35%", "65%", "approve of you");
            else USMap.scale(g, mx + 18, 644, rgb(0x1f6f4a), mix(rgb(0x1f6f4a), rgb(0xc8323a), 0.5), rgb(0xc8323a), "low", "high", mapMode == 1 ? "unemployment rate" : "gas price vs. the nation");
            int s = selState >= 0 ? selState : hs;
            if (s >= 0) {
                StateDef d = STATES[s];
                double m24 = (d.d24 - d.r24) / Math.max(1, d.t24) * 100;
                text(g, d.name, mx + 18, 696, F_H2, Color.white);
                text(g, "Approval " + Math.round(game.stateApp[s]) + "%   Unemployment " + fmt1(e.stUnemp[s]) + "%   Gas $" + fmt2(e.stGas[s]) + "   " + d.ev + " electoral votes   2024: " + margin(m24), mx + 18, 720, F_XS, C_TEXT);
                if (game.stateBump[s] > 0.3) text(g, "Your recent visit is still helping here.", mx + 18, 740, F_XS, C_GOOD);
            } else text(g, "Click a state for details.", mx + 18, 700, F_XS, C_DIM);
            // groups
            double gx = mx + mw + 16, gw = DW - 48 - gx;
            u.panel(g, gx, 116, gw, 696, "By group");
            y = 156;
            for (int gr : SHOW_GROUPS) {
                textFit(g, GROUP[gr], gx + 18, y, 150, F_XS, C_TEXT);
                bar(g, gx + 176, y - 9, gw - 240, 10, game.groupApp[gr] / 100, game.groupApp[gr] >= 50 ? C_GOOD : game.groupApp[gr] >= 35 ? C_GOLD : C_BAD);
                textR(g, Math.round(game.groupApp[gr]) + "%", gx + gw - 18, y, F_XSB, Color.white);
                y += 34;
            }
        }

        // ---- the economy ----
        static void econ(ResoluteDesk u, Graphics2D g, Game game) {
            Econ e = game.econ;
            String[][] tiles = {{"GROWTH", fmt1(e.gdp) + "%", "over the past year"}, {"UNEMPLOYMENT", fmt1(e.unemp) + "%", (e.jobsLast >= 0 ? "+" : "") + Math.round(e.jobsLast) + "K jobs last month"},
                    {"INFLATION", fmt1(e.infl) + "%", "core " + fmt1(e.core) + "%"}, {"FED FUNDS", fmt2(e.fed) + "%", "10-year " + fmt2(e.tenYr) + "%"},
                    {"S&P 500", thousands(Math.round(e.stocks)), (e.stocks >= e.stocks0 ? "+" : "") + fmt1((e.stocks / e.stocks0 - 1) * 100) + "% since you took office"},
                    {"GAS", "$" + fmt2(e.gas), "a gallon, national average"}, {"CONSUMER MOOD", fmt0(e.sentiment), "sentiment index"},
                    {"DEFICIT", fmt1(e.deficit) + "%", "of GDP, $" + fmt1(e.deficit * e.nominalGdp * 10) + "B"}, {"DEBT", Math.round(e.debt) + "%", "of GDP, held by the public"}};
            double tw = (DW - 96 - 8 * 10) / 9.0;
            for (int i = 0; i < tiles.length; i++) {
                double x = 48 + i * (tw + 10);
                g.setColor(new Color(255, 255, 255, 12));
                g.fill(new RoundRectangle2D.Double(x, 116, tw, 84, 10, 10));
                label(g, tiles[i][0], x + 12, 136, C_DIM);
                text(g, tiles[i][1], x + 12, 168, F_NUM, Color.white);
                textFit(g, tiles[i][2], x + 12, 188, tw - 20, F_T, C_DIM);
            }
            // charts
            String[] cn = {"Jobs and prices", "Growth", "Stocks", "Rates", "Gas", "The budget"};
            u.segmented(g, 48, 214, 700, 28, cn, chartSel, i -> chartSel = i, null);
            u.panel(g, 48, 252, 900, 560, cn[chartSel]);
            List<double[]> ser = new ArrayList<double[]>();
            Color[] cols;
            String[] names;
            double lo, hi;
            String unit = "%.1f%%";
            int n = game.hist.size();
            switch (chartSel) {
                case 1: ser.add(col(game, H_GDP)); cols = new Color[]{C_GOOD}; names = new String[]{"Growth, past year"}; lo = -4; hi = 6; break;
                case 2: ser.add(col(game, H_STOCKS)); cols = new Color[]{C_ACCENT}; names = new String[]{"S&P 500"}; lo = minOf(ser.get(0)) * 0.95; hi = maxOf(ser.get(0)) * 1.05; unit = "%.0f"; break;
                case 3: ser.add(col(game, H_FED)); ser.add(col(game, H_TEN)); cols = new Color[]{C_GOLD, C_ACCENT}; names = new String[]{"Fed funds", "10-year Treasury"}; lo = 0; hi = Math.max(6, maxOf(ser.get(1)) + 1); break;
                case 4: ser.add(col(game, H_GAS)); cols = new Color[]{C_AMBER}; names = new String[]{"Gas, $ a gallon"}; lo = Math.max(0, minOf(ser.get(0)) - 0.5); hi = maxOf(ser.get(0)) + 0.5; unit = "$%.2f"; break;
                case 5: ser.add(col(game, H_DEF)); cols = new Color[]{C_BAD}; names = new String[]{"Deficit, % of GDP"}; lo = 0; hi = Math.max(10, maxOf(ser.get(0)) + 2); break;
                default: ser.add(col(game, H_UNEMP)); ser.add(col(game, H_INFL)); cols = new Color[]{C_ACCENT, C_AMBER}; names = new String[]{"Unemployment", "Inflation"}; lo = 0; hi = Math.max(8, Math.max(maxOf(ser.get(0)), maxOf(ser.get(1))) + 1); break;
            }
            ResoluteDesk.chart(g, 96, 292, 820, 480, ser, cols, names, lo, hi, unit, Math.max(27, n));
            if (n < 3) textC(g, "The chart fills in as the weeks go by.", 500, 540, F_S, C_DIM);
            // right column
            double px = 964, pw = DW - 48 - px;
            u.panel(g, px, 214, pw, 220, "The prices people feel (over the past year)");
            String[][] pr = {{"Gas", "$" + fmt2(e.gas) + " a gallon", fmt1((e.gas / e.gas0 - 1) * 100) + "% since you took office"}, {"Groceries", fmt1(e.groceries) + "%", null},
                    {"Rent", fmt1(e.rent) + "%", null}, {"Car and home insurance", fmt1(e.insurance) + "%", null}, {"Mortgage rate", fmt2(e.mortgage) + "%", "30-year fixed"}};
            double y = 254;
            for (String[] r : pr) {
                text(g, r[0], px + 18, y, F_S, C_TEXT);
                textR(g, r[1], px + pw - 18, y, F_BB, Color.white);
                if (r[2] != null) textR(g, r[2], px + pw - 18, y + 14, F_T, C_DIM);
                y += r[2] != null ? 36 : 28;
            }
            u.panel(g, px, 444, pw, 200, "Industries");
            String[][] ind = {{"Housing starts", fmt2(e.housing) + " million a year"}, {"Manufacturing (PMI)", fmt1(e.manufacturing) + (e.manufacturing >= 50 ? " (growing)" : " (shrinking)")},
                    {"AI investment", game.era.ai ? fmt0(e.aiInvest) + " (100 = 2025)" : "n/a"}, {"Farm income", fmt0(e.farm) + " (100 = normal)"},
                    {"Energy production", fmt0(e.energyIdx)}, {"Oil", "$" + fmt0(e.oil) + " a barrel"}};
            y = 482;
            for (String[] r : ind) { text(g, r[0], px + 18, y, F_S, C_TEXT); textR(g, r[1], px + pw - 18, y, F_SB, Color.white); y += 25; }
            u.panel(g, px, 654, pw, 158, "The Federal Reserve");
            text(g, "Chair: " + e.fedChair, px + 18, 692, F_S, Color.white);
            text(g, "Target rate " + Economy.rate(e.fed), px + 18, 714, F_S, C_TEXT);
            double gap = e.fedTarget - e.fed;
            text(g, Math.abs(gap) < 0.15 ? "Markets expect the Fed to hold." : gap > 0 ? "Markets expect the Fed to raise rates." : "Markets expect the Fed to cut.", px + 18, 736, F_XS, C_DIM);
            wrap(g, "You'll be able to nominate Fed governors and lean on the chair in a later update, at a cost to the Fed's credibility.", px + 18, 760, pw - 36, F_T, C_FAINT);
        }

        static double[] col(Game game, int c) {
            double[] v = new double[game.hist.size()];
            for (int i = 0; i < v.length; i++) v[i] = game.hist.get(i)[c];
            return v;
        }

        static double minOf(double[] v) { double m = Double.MAX_VALUE; for (double x : v) m = Math.min(m, x); return v.length == 0 ? 0 : m; }

        static double maxOf(double[] v) { double m = -Double.MAX_VALUE; for (double x : v) m = Math.max(m, x); return v.length == 0 ? 1 : m; }

        // ---- the residence ----
        static void residence(ResoluteDesk u, Graphics2D g, Game game) {
            u.panel(g, 48, 116, 560, 696, "How you're holding up");
            Portrait.draw(g, game.pres, 68, 156, 150, 180, u.tsec, 0, false, true);
            text(g, game.pres.name(), 236, 186, F_H2, Color.white);
            text(g, "Age " + game.presAge(), 236, 208, F_S, C_DIM);
            if (game.pres.spouse != null) text(g, "Spouse: " + game.pres.spouse, 236, 230, F_S, C_DIM);
            String[][] rows = {{"Stamina", String.valueOf(Math.round(game.stamina))}, {"Sleep", fmt1(game.sleep) + " hours a night"}, {"Stress", String.valueOf(Math.round(game.stress))},
                    {"Fitness", String.valueOf(Math.round(game.fitness))}, {"Health", String.valueOf(Math.round(game.health))}};
            double y = 370;
            for (String[] r : rows) { text(g, r[0], 68, y, F_S, C_TEXT); textR(g, r[1], 588, y, F_BB, Color.white); y += 30; }
            String advice = game.sick > 0 ? "You're unwell. Keep the schedule light until you recover." : game.stamina < 30 ? "The physician wants you to rest: exhaustion makes gaffes and illness more likely."
                    : game.stress > 70 ? "Your stress is high. Family time and exercise help; so does a weekend at Camp David." : game.sleep < 5.8 ? "You aren't sleeping enough. Fewer late nights and evening events." : "The White House physician says you're in good shape for the job.";
            wrap(g, advice, 68, y + 20, 520, F_S, game.stamina < 30 || game.stress > 70 || game.sick > 0 ? C_AMBER : C_GOOD);
            wrap(g, "Long days cost more as presidents age. Exercise builds fitness slowly; sleep and rest restore stamina week to week.", 68, y + 70, 520, F_XS, C_DIM);
            u.panel(g, 624, 116, DW - 48 - 624, 696, "Stamina and stress");
            List<double[]> ser = new ArrayList<double[]>();
            ser.add(col(game, H_STAMINA)); ser.add(col(game, H_STRESS));
            ResoluteDesk.chart(g, 672, 160, DW - 48 - 624 - 80, 600, ser, new Color[]{C_GOOD, C_AMBER}, new String[]{"Stamina", "Stress"}, 0, 100, "%.0f", Math.max(27, game.hist.size()));
        }

        // ---- the rooms still to come ----
        static void later(ResoluteDesk u, Graphics2D g, Game game) {
            u.panel(g, 48, 116, DW - 96, 696, ROOM_NAME[room]);
            text(g, "Opens in a later update", 80, 190, F_H1, C_GOLD);
            wrap(g, ROOM_LATER[room], 80, 226, 900, F_B, C_TEXT);
            double y = 330;
            if (room == R_CONGRESS) {
                Era e = game.era;
                label(g, "Congress today", 80, y, C_GOLD);
                y += 28;
                text(g, "House: " + game.houseD + " Democrats, " + (435 - game.houseD) + " Republicans. Speaker " + (game.houseD >= 218 ? e.speakerD : e.speakerR) + ".", 80, y, F_S, C_TEXT); y += 24;
                text(g, "Senate: " + game.senateD + " Democrats, " + (100 - game.senateD) + " Republicans. Majority leader " + (game.senateD > 50 || game.senateD == 50 && game.party == DEM ? e.leaderD : e.leaderR) + ".", 80, y, F_S, C_TEXT); y += 24;
                text(g, "Your relationship with the Hill: " + Math.round(game.hill) + " of 100. Hill meetings on your calendar improve it.", 80, y, F_S, C_TEXT);
            }
            if (room == R_WESTWING && game.vp != null) {
                Portrait.draw(g, game.vp, 80, y, 120, 144, u.tsec, 0, false, true);
                text(g, "Vice President " + game.vp.name(), 220, y + 30, F_H2, Color.white);
                wrap(g, game.vp.role, 220, y + 54, 600, F_S, C_DIM);
            }
        }
    }
    // =====================================================================
    //  26.  THE MORNING PAPER
    //  Each week opens with the front page of The Capital Ledger: the biggest story, the rest, and the numbers.
    // =====================================================================
    static final Color C_PAPER = rgb(0xf4efe4), C_INK = rgb(0x1c1a17), C_INK2 = rgb(0x5a554c);

    static final class Paper {
        static List<News> stories(Game g) {
            List<News> l = new ArrayList<News>();
            int from = g.day() - 7;
            for (News n : g.news) if (n.day >= from) l.add(n);
            Collections.sort(l, (a, b) -> Double.compare(b.weight, a.weight));
            return l;
        }

        static void draw(ResoluteDesk u, Graphics2D g) {
            Game game = u.game;
            if (game == null) { u.go(SC_HOME); return; }
            g.setColor(rgb(0x20262f));
            g.fill(new Rectangle2D.Double(0, 0, DW, DH));
            double px = 260, pw = DW - 520, py = 30, ph = DH - 100;
            g.setColor(new Color(0, 0, 0, 90));
            g.fill(new Rectangle2D.Double(px + 8, py + 10, pw, ph));
            g.setColor(C_PAPER);
            g.fill(new Rectangle2D.Double(px, py, pw, ph));
            // masthead
            Font mast = new Font(SERIF, Font.BOLD, 54);
            textC(g, "The Capital Ledger", px + pw / 2, py + 72, mast, C_INK);
            g.setColor(C_INK);
            g.fill(new Rectangle2D.Double(px + 30, py + 88, pw - 60, 2));
            g.fill(new Rectangle2D.Double(px + 30, py + 116, pw - 60, 1));
            text(g, dateLong(game.day()).toUpperCase(), px + 34, py + 108, F_TB, C_INK2);
            textR(g, "WASHINGTON EDITION", px + pw - 34, py + 108, F_TB, C_INK2);
            textC(g, "Week " + (game.week + 1) + " of the " + game.pres.last + " administration", px + pw / 2, py + 108, F_TB, C_INK2);
            List<News> st = stories(game);
            News lead = st.isEmpty() ? null : st.get(0);
            double cy = py + 130;
            Font head = new Font(SERIF, Font.BOLD, 40);
            String h = lead == null ? "A quiet week in Washington" : lead.head;
            double hh = wrap(g, cap(h), px + 34, cy + 38, pw - 68, head, C_INK);
            cy += hh + 20;
            // lead story: a photo of the president and the body
            double photoW = 230, photoH = 270;
            g.setColor(rgb(0xd8d2c4));
            g.fill(new Rectangle2D.Double(px + 34, cy, photoW, photoH));
            Portrait.draw(g, game.pres, px + 44, cy + 10, photoW - 20, photoH - 50, u.tsec, 0, true, false);
            text(g, "President " + game.pres.last + (game.location != Week.dc() ? " in " + STATES[game.location].name : " at the White House"), px + 40, cy + photoH - 14, F_XS, C_INK2);
            String body = (lead != null && lead.body != null ? lead.body + "\n\n" : "") + leadBody(game, lead);
            wrap(g, body, px + 34 + photoW + 20, cy + 16, pw - 68 - photoW - 20, new Font(SERIF, Font.PLAIN, 15), C_INK);
            cy += photoH + 24;
            g.setColor(C_INK);
            g.fill(new Rectangle2D.Double(px + 30, cy - 10, pw - 60, 1));
            // other stories in three columns
            double colW = (pw - 68 - 40) / 3;
            for (int i = 1; i < Math.min(st.size(), 7); i++) {
                int c = (i - 1) % 3;
                double x = px + 34 + c * (colW + 20), y = cy + ((i - 1) / 3) * 92;
                News n = st.get(i);
                double yy = y + 4 + wrap(g, cap(n.head), x, y + 16, colW, new Font(SERIF, Font.BOLD, 16), C_INK);
                if (n.body != null) wrap(g, n.body, x, yy + 6, colW, new Font(SERIF, Font.PLAIN, 12), C_INK2);
            }
            int used = Math.min(st.size(), 7) - 1;
            String[][] feat = features(game);
            for (int k = 0; used + k < 6 && k < feat.length; k++) {
                int i = used + k;
                int c = i % 3;
                double x = px + 34 + c * (colW + 20), y = cy + (i / 3) * 92;
                double yy = y + 4 + wrap(g, feat[k][0], x, y + 16, colW, new Font(SERIF, Font.BOLD, 16), C_INK);
                wrap(g, feat[k][1], x, yy + 6, colW, new Font(SERIF, Font.PLAIN, 12), C_INK2);
            }
            // the side panel: numbers
            double sx = px + pw + 24, sw = DW - sx - 24;
            g.setColor(new Color(255, 255, 255, 12));
            g.fill(new RoundRectangle2D.Double(sx, py, sw, 330, 10, 10));
            label(g, "BY THE NUMBERS", sx + 14, py + 26, C_GOLD);
            Econ e = game.econ;
            String[][] rows = {{"Approval", Math.round(game.approval) + "%"}, {"Unemployment", fmt1(e.unemp) + "%"}, {"Inflation", fmt1(e.infl) + "%"}, {"S&P 500", thousands(Math.round(e.stocks))},
                    {"Gas", "$" + fmt2(e.gas)}, {"Fed funds", fmt2(e.fed) + "%"}, {"Your stamina", String.valueOf(Math.round(game.stamina))}};
            double y = py + 56;
            for (String[] r : rows) { text(g, r[0], sx + 14, y, F_XS, C_TEXT); textR(g, r[1], sx + sw - 14, y, F_SB, Color.white); y += 28; }
            u.button(g, DW / 2 - 160, DH - 58, 320, 44, "To the Oval Office", B_GOLD, () -> u.go(SC_OVAL), "Space");
        }

        /** Regular features that fill the page: the week ahead, poll watch, markets and an opinion column. */
        static String[][] features(Game g) {
            Econ e = g.econ;
            StringBuilder trips = new StringBuilder();
            int nt = 0;
            for (int i = 0; i < NSLOT; i++) if (isTrip(g.act[i]) && g.arg[i] >= 0) { trips.append(nt++ > 0 ? ", " : "").append(STATES[g.arg[i]].name); }
            int top = Week.topIssue(g);
            String ahead = (nt > 0 ? "The president travels to " + trips + ". " : "No travel is planned. ") + "Policy meetings focus on " + PISSUE[top].toLowerCase() + ".";
            double prev = g.hist.size() >= 5 ? g.hist.get(g.hist.size() - 5)[H_APP] : g.approval0;
            String trend = g.approval > prev + 1.5 ? "rising" : g.approval < prev - 1.5 ? "slipping" : "steady";
            String polls = "Approval is " + trend + " at " + Math.round(g.approval) + "%. Among the issues voters care about most, the president scores best on " + PISSUE[bestIssue(g)].toLowerCase()
                    + " and worst on " + PISSUE[worstIssue(g)].toLowerCase() + ".";
            double wk = g.hist.size() >= 2 ? (e.stocks / g.hist.get(g.hist.size() - 2)[H_STOCKS] - 1) * 100 : 0;
            String markets = "Stocks " + (wk >= 0 ? "rose " : "fell ") + fmt1(Math.abs(wk)) + "% last week. The 10-year Treasury yields " + fmt2(e.tenYr) + "% and oil trades at $" + fmt0(e.oil) + ".";
            String opTitle = g.approval > prev + 1.5 ? "Opinion: The president finds a groove" : g.approval < prev - 1.5 ? "Opinion: Is the honeymoon over?" : g.econ.infl > 4 ? "Opinion: Prices are the whole story" : "Opinion: The long game in Washington";
            String op = g.approval < 42 ? "Allies on the Hill are getting nervous, and it shows." : g.approval > 52 ? "For now the public is giving the White House the benefit of the doubt." : "Neither a crisis nor a triumph: the country is still making up its mind.";
            return new String[][]{{"The week ahead", ahead}, {"Poll watch", polls}, {"Markets", markets}, {opTitle, op}};
        }

        static int bestIssue(Game g) { int b = 0; for (int i = 1; i < NPI; i++) if (g.issueSal[i] > 0.3 && g.issueApp[i] > g.issueApp[b]) b = i; return b; }

        static int worstIssue(Game g) { int b = 0; for (int i = 1; i < NPI; i++) if (g.issueSal[i] > 0.3 && g.issueApp[i] < g.issueApp[b]) b = i; return b; }

        static String cap(String s) { return s == null || s.isEmpty() ? "" : Character.toUpperCase(s.charAt(0)) + s.substring(1); }

        static String leadBody(Game g, News lead) {
            Econ e = g.econ;
            return "President " + g.pres.last + " begins the week with " + Math.round(g.approval) + " percent of Americans approving of the job, according to the latest polling average. "
                    + "Unemployment stands at " + fmt1(e.unemp) + " percent and prices are up " + fmt1(e.infl) + " percent from a year ago. "
                    + (g.stamina < 35 ? "Aides privately worry about the president's punishing schedule." : "Aides say the president's schedule this week focuses on " + PISSUE[Week.topIssue(g)].toLowerCase() + ".");
        }
    }
    // =====================================================================
    //  27.  THE ROOSEVELT ROOM: Congress, your bills, the whip count, the budget and the midterms
    // =====================================================================
    static final Color[] BLOC_COL = {rgb(0x1e4fb8), rgb(0x2f7bf6), rgb(0x79acff), rgb(0x8a1f26), rgb(0xe5484d), rgb(0xff8a8d),
            rgb(0x1e4fb8), rgb(0x2f7bf6), rgb(0x79acff), rgb(0x8a1f26), rgb(0xe5484d), rgb(0xff8a8d)};

    static final class HillUI {
        static int tab, chairCh, sel = -1, draftSel = -1;
        static Bill draft;
        static String msg;
        static final String[] TABS = {"The Hill", "Bills", "Whip count", "The budget", "Elections"};

        static void draw(ResoluteDesk u, Graphics2D g, Game game) {
            Congress c = game.cong;
            u.segmented(g, 48, 116, 640, 30, TABS, tab, i -> tab = i, null);
            text(g, "Political capital", 720, 136, F_S, C_DIM);
            bar(g, 840, 127, 160, 10, game.capital / 100, C_GOLD);
            text(g, String.valueOf(Math.round(game.capital)), 1010, 137, F_BB, C_GOLD);
            if (msg != null) textFit(g, msg, 1050, 137, DW - 1100, F_XS, C_GOLD);
            switch (tab) {
                case 1: bills(u, g, game); break;
                case 2: whip(u, g, game); break;
                case 3: budget(u, g, game); break;
                case 4: elections(u, g, game); break;
                default: hill(u, g, game); break;
            }
        }

        // ---- the Hill ----
        static void hill(ResoluteDesk u, Graphics2D g, Game game) {
            Congress c = game.cong;
            u.panel(g, 48, 160, 700, 652, "Congress");
            double y = 196;
            for (int ch = 0; ch < 2; ch++) {
                int total = ch == HOUSE ? 435 : 100;
                int dem = ch == HOUSE ? game.houseD : game.senateD;
                text(g, ch == HOUSE ? "House of Representatives" : "Senate", 66, y, F_H3, Color.white);
                textR(g, dem + " D - " + (total - dem) + " R" + (Hill.majority(game, ch, game.party) ? "   your party's majority" : "   the other party's majority"), 730, y, F_XS, Hill.majority(game, ch, game.party) ? C_GOOD : C_AMBER);
                double bx = 66, bw = 664;
                for (int i = 0; i < c.blocs.size(); i++) {
                    Bloc b = c.blocs.get(i);
                    if (b.chamber != ch) continue;
                    double w = bw * b.seats / (double) total;
                    g.setColor(BLOC_COL[i]);
                    g.fill(new Rectangle2D.Double(bx, y + 10, Math.max(0, w - 1), 18));
                    bx += w;
                }
                g.setColor(Color.white);
                g.fill(new Rectangle2D.Double(66 + bw * (ch == HOUSE ? 218 : 50.5) / total, y + 6, 2, 26));
                y += 46;
                for (int i = 0; i < c.blocs.size(); i++) {
                    Bloc b = c.blocs.get(i);
                    if (b.chamber != ch) continue;
                    g.setColor(BLOC_COL[i]);
                    g.fill(new RoundRectangle2D.Double(66, y - 10, 10, 10, 3, 3));
                    text(g, b.name, 84, y, F_XS, C_TEXT);
                    textR(g, b.seats + " seats", 360, y, F_XS, C_DIM);
                    text(g, "relationship", 390, y, F_T, C_FAINT);
                    bar(g, 460, y - 8, 200, 8, b.rel / 100, b.rel >= 55 ? C_GOOD : b.rel >= 40 ? C_GOLD : C_BAD);
                    textR(g, String.valueOf(Math.round(b.rel)), 730, y, F_XSB, Color.white);
                    y += 22;
                }
                y += 24;
            }
            wrap(g, "Hill meetings on your calendar improve your relationships with the leaders and every bloc. Wins, deals and vetoes move them too.", 66, y + 6, 660, F_XS, C_DIM);
            if (game.filibusterGone) text(g, "The legislative filibuster is gone: bills pass the Senate with a simple majority.", 66, 780, F_XS, C_GOLD);
            else if (Hill.majority(game, SENATE, game.party)) {
                double p = Legis.nukeChance(game);
                u.button(g, 66, 756, 300, 36, "Ask the Senate to end the filibuster", game.capital >= 15 ? B_NORM : B_OFF, game.capital >= 15 ? () -> msg = Legis.nuke(game) : null,
                        "Costs 15 capital. About a " + Math.round(p * 100) + "% chance your own senators go along. The other side won't forget.");
                text(g, "About " + Math.round(p * 100) + "% chance; 15 capital", 380, 779, F_XS, C_DIM);
            }
            // leaders and chairs
            double px = 764, pw = DW - 48 - px;
            u.panel(g, px, 160, pw, 300, "The leaders");
            double lx = px + 18;
            int k = 0;
            for (Member m : c.members) {
                if (m.role != RO_LEADER) continue;
                double cx = lx + (k % 2) * (pw / 2 - 6), cy = 196 + (k / 2) * 128;
                Portrait.draw(g, m.face, cx, cy, 70, 84, u.tsec, 0, true, true);
                text(g, m.name, cx + 82, cy + 18, F_BB, Color.white);
                textFit(g, m.title, cx + 82, cy + 36, pw / 2 - 110, F_XS, partyLight(m.party));
                bar(g, cx + 82, cy + 48, 150, 8, m.rel / 100, m.rel >= 55 ? C_GOOD : m.rel >= 40 ? C_GOLD : C_BAD);
                text(g, Math.round(m.rel) + "", cx + 240, cy + 57, F_XSB, Color.white);
                final Member mm = m;
                u.button(g, cx + 82, cy + 64, 150, 24, "Meet (2 capital)", game.capital >= 2 ? B_NORM : B_OFF, game.capital >= 2 ? () -> { game.capital -= 2; mm.rel = Math.min(100, mm.rel + 5 + game.pres.a[A_EST] / 30.0); msg = "You meet with " + mm.name + "."; } : null, "A private meeting. Improves your relationship.");
                k++;
            }
            u.panel(g, px, 470, pw, 342, "Committee chairs");
            u.segmented(g, px + pw - 220, 476, 200, 24, new String[]{"House", "Senate"}, chairCh, i -> chairCh = i, null);
            double yy = 518;
            for (Member m : c.members) {
                if (m.role != RO_CHAIR || m.chamber != chairCh) continue;
                textFit(g, m.committee, px + 18, yy, 270, F_XS, C_TEXT);
                text(g, m.name + " (" + PARTY_SHORT[m.party] + ")", px + 300, yy, F_XS, partyLight(m.party));
                bar(g, px + pw - 140, yy - 8, 100, 7, m.rel / 100, m.rel >= 55 ? C_GOOD : m.rel >= 40 ? C_GOLD : C_BAD);
                textR(g, String.valueOf(Math.round(m.rel)), px + pw - 18, yy, F_XSB, Color.white);
                yy += 23;
            }
        }

        // ---- bills ----
        static void bills(ResoluteDesk u, Graphics2D g, Game game) {
            Congress c = game.cong;
            u.panel(g, 48, 160, 600, 652, "Before Congress");
            double y = 196;
            int shown = 0;
            label(g, "Your bills", 66, y, C_GOLD);
            y += 18;
            for (int i = c.bills.size() - 1; i >= 0; i--) {
                Bill b = c.bills.get(i);
                if (!b.ours) continue;
                if (b.stage == ST_DEAD && game.week - b.stageWeek > 26) continue;
                y = billRow(u, g, game, b, i, y);
                if (++shown >= 6) break;
            }
            if (shown == 0) { text(g, "None yet. Draft one on the right.", 66, y + 14, F_XS, C_DIM); y += 30; }
            y += 10;
            label(g, "Congress's bills", 66, y, C_GOLD);
            y += 18;
            shown = 0;
            for (int i = c.bills.size() - 1; i >= 0; i--) {
                Bill b = c.bills.get(i);
                if (b.ours) continue;
                if (b.stage >= ST_LAW && game.week - b.stageWeek > 16) continue;
                y = billRow(u, g, game, b, i, y);
                if (++shown >= 4 || y > 760) break;
            }
            if (shown == 0) text(g, "Nothing moving right now.", 66, y + 14, F_XS, C_DIM);
            // drafting
            double px = 664, pw = DW - 48 - px;
            u.panel(g, px, 160, pw, 652, "Draft a bill");
            List<BillDef> defs = agenda(game);
            textR(g, "public support", px + 330, 178, F_T, C_DIM);
            double ly = 194;
            double top = ly, h = 600;
            double off = u.beginScroll(g, "drafts", px + 10, top, 330, h);
            int h0 = u.hits.size();
            for (int i = 0; i < defs.size(); i++) {
                BillDef d = defs.get(i);
                final int k = i;
                double ry = top - off + i * 40;
                boolean busy = inPlay(game, d);
                u.card(g, px + 14, ry, 316, 36, draftSel == i, () -> { draftSel = k; draft = Legis.draft(game, defs.get(k)); }, d.desc);
                g.setColor(d.lean == DEM ? C_DEM : d.lean == REP ? C_REP : C_GOLD);
                g.fill(new RoundRectangle2D.Double(px + 20, ry + 10, 4, 16, 3, 3));
                textFit(g, d.name, px + 32, ry + 23, 230, F_S, busy ? C_DIM : Color.white);
                textR(g, busy ? "in play" : Math.round(d.support) + "%", px + 322, ry + 23, F_XS, busy ? C_DIM : C_TEXT);
            }
            u.clipHits(h0, top, top + h);
            u.endScroll(g, "drafts", defs.size() * 40);
            double dx = px + 350, dw = pw - 368;
            if (draft == null || draftSel < 0 || draftSel >= defs.size()) { wrap(g, "Pick a bill. Blue bills are Democratic priorities, red Republican, gold bipartisan.", dx, 200, dw, F_S, C_DIM); return; }
            BillDef d = draft.def();
            text(g, d.name, dx, 206, F_H2, Color.white);
            double yy = 216 + wrap(g, d.desc, dx, 226, dw, F_XS, C_TEXT);
            text(g, "Public support " + Math.round(draft.support()) + "%", dx, yy + 18, F_XSB, draft.support() >= 55 ? C_GOOD : draft.support() < 48 ? C_BAD : C_TEXT);
            double nc = draft.netCost();
            textR(g, nc > 1 ? "adds $" + Math.round(nc) + "B a year to the deficit" : nc < -1 ? "cuts the deficit by $" + Math.round(-nc) + "B a year" : "about deficit-neutral", dx + dw, yy + 18, F_XS, nc > 50 ? C_AMBER : C_DIM);
            yy += 44;
            text(g, "Size", dx, yy + 12, F_S, C_TEXT);
            u.slider(g, dx + 90, yy + 8, dw - 160, (draft.size - 0.5), C_ACCENT, v -> draft.size = 0.5 + v, "Bigger bills do more and cost more; smaller ones are easier to pass.");
            textR(g, Math.round(draft.size * 100) + "%", dx + dw, yy + 12, F_XSB, Color.white);
            yy += 34;
            text(g, "Scope", dx, yy + 12, F_S, C_TEXT);
            u.slider(g, dx + 90, yy + 8, dw - 160, draft.scope, C_ACCENT, v -> draft.scope = v, "Narrow bills draw fewer objections; broad ones go further and lose votes in the middle.");
            textR(g, draft.scope < 0.33 ? "narrow" : draft.scope > 0.66 ? "broad" : "middle", dx + dw, yy + 12, F_XSB, Color.white);
            yy += 34;
            if (d.cost > 0) {
                text(g, "Paid for by", dx, yy + 18, F_S, C_TEXT);
                u.segmented(g, dx + 90, yy, dw - 90, 28, PAYFOR, draft.payFor, i -> draft.payFor = i, new String[]{"Add it to the deficit. Fiscal hawks object.", "Moves the bill left. Popular, but business groups fight it.", "Moves the bill right. Hard for your own progressives (or easy, for a Republican)."});
                yy += 40;
            }
            boolean rc = d.budget && Legis.reconOk(game, draft);
            u.toggle(g, dx, yy, draft.recon && rc, rc ? () -> draft.recon = !draft.recon : null, d.budget ? (rc ? "A simple majority in the Senate, once a fiscal year. The Byrd rule strikes anything that isn't about money." : "Needs your party in control of both chambers, and one use a fiscal year.") : "Not a budget bill: the Byrd rule rules it out.");
            text(g, "Budget reconciliation", dx + 46, yy + 16, F_S, rc ? C_TEXT : C_DIM);
            if (!d.origRevenue) { u.segmented(g, dx + dw - 180, yy, 180, 26, new String[]{"House first", "Senate first"}, draft.first, i -> draft.first = i, null); }
            else text(g, "Revenue bills start in the House", dx + dw - 190, yy + 16, F_T, C_DIM);
            yy += 44;
            if (!rc) draft.recon = false;
            double pH = Legis.chance(game, draft, HOUSE, false), pS = Legis.chance(game, draft, SENATE, false);
            double cH = Legis.chairOk(game, draft, HOUSE), cS = Legis.chairOk(game, draft, SENATE);
            label(g, "The whip's first read", dx, yy, C_GOLD);
            yy += 22;
            double[] hc = Legis.count(game, draft, HOUSE, false), sc = Legis.count(game, draft, SENATE, false);
            text(g, "House: about " + Math.round(hc[0]) + " votes of " + Legis.need(game, draft, HOUSE, false) + " needed", dx, yy, F_XS, C_TEXT);
            textR(g, Math.round(pH * 100) + "%", dx + dw, yy, F_XSB, pH >= 0.6 ? C_GOOD : pH >= 0.3 ? C_GOLD : C_BAD); yy += 20;
            text(g, "Senate: about " + Math.round(sc[0]) + " of " + Legis.need(game, draft, SENATE, false) + (draft.recon || game.filibusterGone ? "" : " (to end a filibuster)"), dx, yy, F_XS, C_TEXT);
            textR(g, Math.round(pS * 100) + "%", dx + dw, yy, F_XSB, pS >= 0.6 ? C_GOOD : pS >= 0.3 ? C_GOLD : C_BAD); yy += 20;
            text(g, "Committee chairs let it out", dx, yy, F_XS, C_TEXT);
            textR(g, Math.round(cH * 100) + "% / " + Math.round(cS * 100) + "%", dx + dw, yy, F_XSB, Math.min(cH, cS) >= 0.6 ? C_GOOD : C_GOLD);
            yy += 28;
            int active = 0;
            for (Bill b : game.cong.bills) if (b.ours && b.stage > ST_DRAFT && b.stage < ST_LAW) active++;
            boolean can = !inPlay(game, d) && active < 3;
            u.button(g, dx, Math.min(yy, 760), dw, 40, inPlay(game, d) ? "Already before Congress" : active >= 3 ? "Congress can only handle three of your bills at once" : "Send to Congress", can ? B_GOLD : B_OFF, can ? () -> {
                if (d.origRevenue) draft.first = HOUSE;
                Legis.introduce(game, draft);
                sel = game.cong.bills.indexOf(draft);
                draft = null; draftSel = -1; tab = 2; msg = "Sent to Congress.";
            } : null);
        }

        static boolean inPlay(Game g, BillDef d) { for (Bill b : g.cong.bills) if (b.defId.equals(d.id) && b.stage > ST_DRAFT && b.stage < ST_LAW && b.stage != ST_DEAD) return true; for (Bill b : g.cong.bills) if (b.defId.equals(d.id) && b.stage == ST_LAW) return true; return false; }

        static List<BillDef> agenda(Game g) {
            List<BillDef> l = new ArrayList<BillDef>();
            for (BillDef d : BILLS) if (d.lean == g.party) l.add(d);
            for (BillDef d : BILLS) if (d.lean < 0) l.add(d);
            for (BillDef d : BILLS) if (d.lean >= 0 && d.lean != g.party) l.add(d);
            return l;
        }

        static double billRow(ResoluteDesk u, Graphics2D g, Game game, Bill b, int idx, double y) {
            final int k = idx;
            u.card(g, 62, y, 572, 54, sel == idx, () -> { sel = k; tab = 2; }, "Open the whip count");
            text(g, b.name, 76, y + 22, F_BB, Color.white);
            Color sc = b.stage == ST_LAW ? C_GOOD : b.stage == ST_DEAD ? C_BAD : b.bottled ? C_AMBER : C_ACCENT;
            textR(g, b.stage == ST_COMMITTEE || b.stage == ST_COMMITTEE2 ? STAGE[b.stage].replace("committee", CHAMBER[b.chamberNow()] + " committee") + (b.bottled ? ", stuck" : "") : STAGE[b.stage], 620, y + 22, F_XSB, sc);
            for (int s = 1; s <= 6; s++) {
                g.setColor(b.stage >= s && b.stage != ST_DEAD ? C_GOLD : b.stage == ST_DEAD && s <= 2 ? C_BAD : new Color(255, 255, 255, 30));
                g.fill(new RoundRectangle2D.Double(76 + (s - 1) * 60, y + 34, 54, 6, 3, 3));
            }
            if (b.stage >= ST_COMMITTEE && b.stage <= ST_FLOOR2) {
                double p = Legis.chance(game, b, b.chamberNow(), false);
                textR(g, "chance of the next vote " + Math.round(p * 100) + "%", 620, y + 42, F_T, C_DIM);
            }
            return y + 60;
        }

        // ---- the whip count ----
        static void whip(ResoluteDesk u, Graphics2D g, Game game) {
            Congress c = game.cong;
            Bill b = sel >= 0 && sel < c.bills.size() ? c.bills.get(sel) : null;
            if (b == null) {
                for (int i = c.bills.size() - 1; i >= 0; i--) if (c.bills.get(i).stage < ST_DESK && c.bills.get(i).stage > ST_DRAFT) { sel = i; b = c.bills.get(i); break; }
            }
            if (b == null) { u.panel(g, 48, 160, DW - 96, 652, "Whip count"); wrap(g, "Nothing to count yet. Send a bill to Congress from the Bills tab.", 70, 200, 800, F_S, C_DIM); return; }
            final Bill bb = b;
            int ch = b.stage >= ST_DESK ? b.second() : b.chamberNow();
            u.panel(g, 48, 160, 760, 652, b.name + ": " + STAGE[b.stage]);
            double y = 196;
            for (int cc = 0; cc < 2; cc++) {
                int chamber = cc == 0 ? ch : (ch == HOUSE ? SENATE : HOUSE);
                double[] cnt = Legis.count(game, b, chamber, false);
                int need = Legis.need(game, b, chamber, false);
                text(g, CHAMBER[chamber] + (cc == 0 ? " (next)" : ""), 66, y, F_H3, Color.white);
                textR(g, "about " + Math.round(cnt[0]) + " yes, " + need + " needed  |  " + Math.round(Legis.chance(game, b, chamber, false) * 100) + "% to pass", 790, y, F_XSB, cnt[0] >= need ? C_GOOD : C_AMBER);
                y += 22;
                for (int i = 0; i < c.blocs.size(); i++) {
                    Bloc bl = c.blocs.get(i);
                    if (bl.chamber != chamber) continue;
                    double p = logistic(Legis.blocLogit(game, b, i, false));
                    g.setColor(BLOC_COL[i]);
                    g.fill(new RoundRectangle2D.Double(66, y - 10, 10, 10, 3, 3));
                    text(g, bl.name, 84, y, F_XS, C_TEXT);
                    text(g, Legis.lean(p), 330, y, F_XSB, p > 0.6 ? C_GOOD : p > 0.4 ? C_GOLD : C_BAD);
                    bar(g, 440, y - 8, 240, 8, p, p > 0.6 ? C_GOOD : p > 0.4 ? C_GOLD : C_BAD);
                    textR(g, Math.round(p * bl.seats) + " of " + bl.seats, 790, y, F_XS, C_DIM);
                    y += 22;
                }
                y += 18;
            }
            if (b.stage <= ST_FLOOR2 && b.stage >= ST_COMMITTEE) {
                label(g, "Change the bill", 66, y, C_GOLD);
                y += 16;
                text(g, "Size", 66, y + 12, F_XS, C_TEXT);
                u.slider(g, 120, y + 8, 260, bb.size - 0.5, C_ACCENT, v -> bb.size = 0.5 + v, "Shrinking a bill wins over the middle.");
                text(g, "Scope", 420, y + 12, F_XS, C_TEXT);
                u.slider(g, 470, y + 8, 260, bb.scope, C_ACCENT, v -> bb.scope = v, "Narrower is easier to pass.");
                y += 30;
                if (b.def().cost > 0) u.segmented(g, 66, y, 724, 26, PAYFOR, bb.payFor, i -> bb.payFor = i, null);
                y += 36;
            }
            double cx = 66;
            for (int i = c.bills.size() - 1; i >= 0 && cx < 700; i--) {
                Bill o = c.bills.get(i);
                if (o.stage < ST_COMMITTEE || o.stage > ST_FLOOR2) continue;
                final int k = i;
                double w = Math.min(230, strW(g, o.name, F_XSB) + 22);
                u.button(g, cx, 770, w, 28, o.name, o == b ? B_TABON : B_TAB, () -> sel = k, (o.ours ? "Your bill" : "Congress's bill") + ": " + STAGE[o.stage]);
                cx += w + 6;
            }
            if (!b.deals.isEmpty()) {
                StringBuilder s = new StringBuilder("Deals so far: ");
                for (int i = 0; i < b.deals.size(); i++) s.append(i > 0 ? "; " : "").append(b.deals.get(i));
                wrap(g, s.toString(), 66, Math.min(y, 730), 720, F_XS, C_DIM);
            }
            // the people
            double px = 824, pw = DW - 48 - px;
            u.panel(g, px, 160, pw, 652, "Working the votes");
            double yy = 196;
            List<Member> people = new ArrayList<Member>();
            for (Member m : c.members) if (m.role == RO_LEADER && m.chamber == ch) people.add(m);
            Member chair = Hill.chairFor(game, b, ch);
            if (chair != null) people.add(chair);
            for (Member m : c.members) if (m.role == RO_SWING && m.chamber == ch) people.add(m);
            for (Member m : people) {
                double p = logistic(Legis.blocLogit(game, b, m.bloc, false) + (m.rel - 50) / 30);
                Portrait.draw(g, m.face, px + 14, yy - 4, 34, 40, u.tsec, 0, true, true);
                text(g, m.title != null && m.role == RO_SWING ? m.title : m.name, px + 56, yy + 10, F_SB, Color.white);
                textFit(g, m.role == RO_SWING ? "wants " + PRICE[m.price] : m.title, px + 56, yy + 26, 250, F_T, C_DIM);
                text(g, Legis.lean(p), px + 300, yy + 10, F_XSB, p > 0.6 ? C_GOOD : p > 0.4 ? C_GOLD : C_BAD);
                if (b.stage >= ST_COMMITTEE && b.stage <= ST_FLOOR2) {
                    double bx = px + 300;
                    int[] kinds = {Legis.DL_CALL, Legis.DL_PROJECT, Legis.DL_APPOINT, Legis.DL_ENDORSE, Legis.DL_THREAT};
                    String[] ab = {"Call", "Project", "Appoint", "Endorse", "Threat"};
                    for (int j = 0; j < kinds.length; j++) {
                        final int kind = kinds[j];
                        final Member mm = m;
                        boolean ok = game.capital >= Legis.DEAL_COST[kind] && mm.lastDeal != game.week;
                        u.button(g, bx + j * 62, yy + 16, 58, 20, ab[j], ok ? B_NORM : B_OFF, ok ? () -> msg = Legis.deal(game, bb, mm, kind) : null, Legis.DEAL[kind] + " (" + Legis.DEAL_COST[kind] + " capital)");
                    }
                }
                yy += 46;
                if (yy > 700) break;
            }
            if (b.stage >= ST_COMMITTEE && b.stage <= ST_FLOOR2) {
                u.button(g, px + 14, 760, 220, 36, b.publicPush ? "You've gone public" : "Go public (3 capital)", !b.publicPush && game.capital >= 3 ? B_PRIMARY : B_OFF, !b.publicPush && game.capital >= 3 ? () -> msg = Legis.deal(game, bb, null, Legis.DL_PUBLIC) : null, "Make the case to the country. Moderates feel the pressure.");
                if (b.ours) u.button(g, px + pw - 180, 760, 166, 36, "Withdraw the bill", B_GHOST, () -> { bb.stage = ST_DEAD; bb.stageWeek = game.week; if (bb.recon && game.cong.reconFy == Hill.fiscalYear(game)) game.cong.reconFy = -1; msg = "You pulled " + bb.name + "."; }, "Pull it before it fails.");
            }
        }

        // ---- the budget ----
        static void budget(ResoluteDesk u, Graphics2D g, Game game) {
            Congress c = game.cong;
            u.panel(g, 48, 160, 760, 652, "Your budget request" + (c.budgetSent ? " (sent to Congress)" : ""));
            double y = 200;
            String[] names = {"Defense", "Other agencies", "Taxes (revenue)", "Entitlements"};
            String[] tips = {"Change in defense spending, percent.", "Change in non-defense discretionary spending, percent.", "Revenue change, % of GDP. Positive raises taxes.", "Social Security, Medicare and other mandatory spending, % of GDP."};
            double[] lo = {-15, -25, -1.5, -1}, hi = {15, 25, 1.5, 1};
            double[] val = {c.reqDefense, c.reqNondefense, c.reqTax, c.reqMandatory};
            for (int i = 0; i < 4; i++) {
                final int k = i;
                text(g, names[i], 66, y + 12, F_S, C_TEXT);
                if (!c.budgetSent) u.slider(g, 220, y + 8, 420, (val[i] - lo[i]) / (hi[i] - lo[i]), C_GOLD, v -> setReq(c, k, lo[k] + v * (hi[k] - lo[k])), tips[i], false);
                else bar(g, 220, y + 4, 420, 8, (val[i] - lo[i]) / (hi[i] - lo[i]), C_FAINT);
                textR(g, (i < 2 ? signed1(val[i]) + "%" : signed1(val[i]) + "% of GDP"), 790, y + 12, F_XSB, Color.white);
                y += 38;
            }
            double ch = Budget.deficitChange(c);
            text(g, "Your plan " + (ch > 0.05 ? "adds " + fmt1(ch) : ch < -0.05 ? "cuts " + fmt1(-ch) : "changes about 0") + " % of GDP " + (ch < 0 ? "from" : "to") + " the yearly deficit", 66, y + 10, F_S, ch > 0.3 ? C_AMBER : C_TEXT);
            y += 30;
            double[][] pr = Budget.projection(game);
            List<double[]> ser = new ArrayList<double[]>();
            ser.add(pr[0]);
            ResoluteDesk.chart(g, 110, y, 660, 160, ser, new Color[]{C_BAD}, new String[]{"Deficit, % of GDP, next ten years"}, 0, Math.max(10, Rooms.maxOf(pr[0]) + 1), "%.0f%%", 11);
            y += 176;
            List<double[]> ser2 = new ArrayList<double[]>();
            ser2.add(pr[1]);
            ResoluteDesk.chart(g, 110, y, 660, 140, ser2, new Color[]{C_AMBER}, new String[]{"Debt held by the public, % of GDP"}, Math.floor(Rooms.minOf(pr[1]) / 10) * 10, Math.ceil(Rooms.maxOf(pr[1]) / 10) * 10 + 10, "%.0f%%", 11);
            y += 156;
            if (!c.budgetSent) u.button(g, 66, Math.min(y, 760), 300, 38, "Send the budget to Congress", B_GOLD, () -> { Budget.send(game); msg = "Budget sent."; });
            else text(g, "Congress writes the actual spending bills; your request sets the terms of the fight.", 66, Math.min(y + 20, 780), F_XS, C_DIM);
            // appropriations, the shutdown clock and the debt limit
            double px = 824, pw = DW - 48 - px;
            u.panel(g, px, 160, pw, 420, "Fiscal year " + (c.fy + (dateOf(game.day())[1] >= 4 ? 1 : 0)) + " spending bills");
            String[] stn = {"not started", "passed one chamber", "passed both chambers", "law", "on a stopgap"};
            Color[] stc = {C_FAINT, C_GOLD, C_ACCENT, C_GOOD, C_AMBER};
            double yy = 198;
            for (int i = 0; i < 12; i++) {
                textFit(g, APPROPS[i], px + 18, yy, 330, F_XS, C_TEXT);
                textR(g, stn[c.approps[i]], px + pw - 18, yy, F_XSB, stc[c.approps[i]]);
                yy += 23;
            }
            yy += 10;
            if (c.shutdown) text(g, "THE GOVERNMENT IS SHUT DOWN: week " + c.shutdownWeeks, px + 18, yy, F_BB, C_BAD);
            else if (c.crUntil >= 0) text(g, "Running on a stopgap until " + dateStr(c.crUntil * 7), px + 18, yy, F_S, C_AMBER);
            else text(g, "Deadline: September 30", px + 18, yy, F_S, C_DIM);
            u.panel(g, px, 590, pw, 222, "The debt limit");
            double subj = Budget.debtSubject(game);
            text(g, "$" + fmt1(subj) + " trillion owed, limit $" + fmt1(c.debtLimit) + " trillion", px + 18, 630, F_S, C_TEXT);
            bar(g, px + 18, 642, pw - 36, 12, subj / c.debtLimit, subj >= c.debtLimit ? C_BAD : subj > c.debtLimit * 0.97 ? C_AMBER : C_ACCENT);
            if (c.xWeek >= 0) text(g, "The Treasury runs out of room around " + dateStr(c.xWeek * 7) + ".", px + 18, 680, F_SB, C_AMBER);
            else text(g, "Room to borrow for about " + Math.max(0, Math.round((c.debtLimit - subj) / Math.max(0.01, subj * (game.econ.deficit / Math.max(1, game.econ.debt))) * 52)) + " more weeks.", px + 18, 680, F_S, C_DIM);
            wrap(g, "When the debt reaches the limit, the Treasury uses accounting moves for a few months. Then Congress has to raise it, or the government starts missing payments.", px + 18, 704, pw - 36, F_XS, C_DIM);
        }

        static void setReq(Congress c, int k, double v) {
            v = Math.round(v * 10) / 10.0;
            if (k == 0) c.reqDefense = v; else if (k == 1) c.reqNondefense = v; else if (k == 2) c.reqTax = v; else c.reqMandatory = v;
        }

        // ---- the midterms ----
        static void elections(ResoluteDesk u, Graphics2D g, Game game) {
            Congress c = game.cong;
            u.panel(g, 48, 160, 600, 652, "The midterms, " + monthYear(Midterms.electionDay(game)));
            double y = 200;
            if (c.midDone) {
                wrap(g, c.midSummary == null ? "The midterms are over." : c.midSummary, 66, y, 560, F_S, C_TEXT);
                return;
            }
            double mP = Midterms.nationalMargin(game, 0);
            double natD = game.party == DEM ? mP : -mP;
            int house = (int) Math.round(222 + 3.8 * natD);
            int myHouse = game.party == DEM ? house : 435 - house;
            text(g, "If the election were held today", 66, y, F_H3, Color.white);
            y += 30;
            text(g, "Your party " + (mP >= 0 ? "wins" : "loses") + " the national House vote by about " + fmt1(Math.abs(mP)) + " points", 66, y, F_S, mP >= 0 ? C_GOOD : C_BAD);
            y += 24;
            text(g, "That's about " + myHouse + " House seats for " + PARTIES[game.party] + " (" + Midterms.signedInt(myHouse - game.myHouse()) + "); 218 is a majority.", 66, y, F_S, C_TEXT);
            y += 36;
            wrap(g, "The president's party usually loses ground in a midterm. Your approval and the economy matter most; campaigning and the money you raise for candidates can add a point or two. "
                    + (Midterms.season(game) ? "It's campaign season: schedule \"Campaign\" stops from your calendar." : "Campaign season opens in late summer of " + (game.era.year + 1) + "."), 66, y, 560, F_XS, C_DIM);
            y += 70;
            text(g, "Raised for your party: $" + fmt1(game.funds) + " million", 66, y, F_S, C_TEXT);
            // senate seats up
            double px = 664, pw = DW - 48 - px;
            u.panel(g, px, 160, pw, 652, "Senate seats up");
            String up = Midterms.seatsUp(game);
            double yy = 198;
            int col = 0;
            if (!up.isEmpty()) for (String tok : up.split(" ")) {
                String[] p = tok.split(":");
                int s = stateIdx(p[0]);
                if (s < 0) continue;
                boolean dHeld = p[1].equals("D");
                double m = Elect.lean[s] + natD + (dHeld ? 3 : -3) + c.campaign[s] * 1.2 * (game.party == DEM ? 1 : -1);
                double x = px + 18 + col * (pw / 3 - 6);
                text(g, STATES[s].name, x, yy, F_XS, C_TEXT);
                text(g, p[1], x + 140, yy, F_XSB, dHeld ? C_DEM_L : C_REP_L);
                textR(g, margin(m), x + pw / 3 - 30, yy, F_XSB, m > 0 ? C_DEM_L : C_REP_L);
                col++;
                if (col == 3) { col = 0; yy += 22; }
            }
            wrap(g, "Holder, then the projected margin. Campaign stops in a state help your party's candidate there.", px + 18, Math.max(yy + 40, 700), pw - 36, F_XS, C_DIM);
        }
    }
    // =====================================================================
    //  30.  SAVING
    // =====================================================================
    static final class SaveGame {
        static final String AUTO = "autosave", SLOT = "slot1";

        static File dir() {
            File d = new File(System.getProperty("user.home"), ".resolutedesk");
            if (!d.exists()) d.mkdirs();
            return d;
        }

        static boolean exists(String name) { return new File(dir(), name + ".sav").exists(); }

        static String describe(String name) {
            try {
                File f = new File(dir(), name + ".txt");
                if (!f.exists()) return "";
                BufferedReader r = new BufferedReader(new FileReader(f));
                String s = r.readLine();
                r.close();
                return s == null ? "" : s;
            } catch (Throwable t) { return ""; }
        }

        static void save(ResoluteDesk u, String name) {
            Game g = u.game;
            if (g == null) return;
            try {
                ObjectOutputStream o = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(new File(dir(), name + ".sav"))));
                o.writeObject(g);
                o.close();
                FileWriter w = new FileWriter(new File(dir(), name + ".txt"));
                w.write(g.pres.last + " (" + PARTY_SHORT[g.party] + "), " + monthYear(g.day()));
                w.close();
                if (name.equals(SLOT)) u.toast("Game saved.", C_GOOD);
            } catch (Throwable t) {
                if (name.equals(SLOT)) u.toast("Could not save: " + t.getMessage(), C_BAD);
            }
        }

        static void load(ResoluteDesk u, String name) {
            try {
                ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File(dir(), name + ".sav"))));
                Game g = (Game) in.readObject();
                in.close();
                resume(u, g);
            } catch (Throwable t) {
                u.info("Could not load", "The saved game could not be read (" + t.getClass().getSimpleName() + ").");
            }
        }

        /** Fills in what a save from an earlier update doesn't have. */
        static void upgrade(Game g) {
            if (g.inbox == null) g.inbox = new ArrayList<Decision>();
            if (g.era.midSeats == null) g.era.midSeats = era(g.era.year).midSeats;
            if (g.cong == null) { Hill.setup(g); g.capital = 30; }
        }

        static void resume(ResoluteDesk u, Game g) {
            if (g.version < Game.VERSION) { u.info("An older save", "This game was saved by an earlier version and can't be continued in this one."); return; }
            CAL_Y = g.era.year; CAL_M = 0; CAL_D = 20;
            Elect.init();
            Groups.init();
            upgrade(g);
            g.dirty = true;
            u.game = g;
            u.setup = g.set;
            u.go(SC_OVAL);
            u.toast("Welcome back. " + dateLong(g.day()) + ".", C_ACCENT);
        }
    }
    // =====================================================================
    //  99.  START-UP AND THE TEST HARNESS
    // =====================================================================
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("test")) { Test.run(args); return; }
        SwingUtilities.invokeLater(new Runnable() { public void run() { launch(); } });
    }

    static final class Test {
        static void run(String[] a) {
            long t0 = System.currentTimeMillis();
            Elect.init();
            Groups.init();
            USMap.load();
            System.out.println("init " + (System.currentTimeMillis() - t0) + " ms");
            String what = a.length > 1 ? a[1] : "sim";
            for (String x : a) if (x.equals("trace")) TRACE = true;
            if (what.equals("sim")) {
                int runs = a.length > 2 ? Integer.parseInt(a[2]) : 1;
                int year = a.length > 3 ? Integer.parseInt(a[3]) : 2029;
                String pres = a.length > 4 ? a[4] : null;
                int scen = a.length > 5 ? Integer.parseInt(a[5]) : 0;
                int weeks = a.length > 6 ? Integer.parseInt(a[6]) : 208;
                for (int r = 0; r < runs; r++) sim(1000 + r, year, pres, scen, weeks);
            }
            if (what.equals("ui")) ui(a.length > 2 ? a[2] : "shots");
            if (what.equals("whip")) whip(a.length > 2 ? Integer.parseInt(a[2]) : 2025, a.length > 3 ? a[3] : null, a.length > 4 ? Integer.parseInt(a[4]) : 0);
        }

        static Game newGame(long seed, int year, String pres, int scen) {
            Settings s = new Settings();
            s.year = year; s.seed = seed; s.scenario = scen;
            Era e = era(year);
            s.presId = pres != null ? pres : eraRoster(e).get(0).id;
            s.vpId = defaultVp(e, candById(s.presId));
            return Setup.newGame(s);
        }

        static void sim(long seed, int year, String pres, int scen, int weeks) {
            Game g = newGame(seed, year, pres, scen);
            StringBuilder b = new StringBuilder("=== seed " + seed + " " + year + " " + g.pres.name() + " (" + PARTY_SHORT[g.party] + ") " + SCEN[scen]
                    + "  House D " + g.houseD + "  Senate D " + g.senateD + "  approval " + fmt1(g.approval) + "\n");
            for (int w = 0; w < weeks && !g.over; w++) {
                if (w % 4 == 0) Week.routine(g);
                if (w % 8 == 3) Rooms.roadWeek(g);
                if (g.stamina < 35) Rooms.restWeek(g);
                if (w % 6 == 1) Decisions.autoAgenda(g);
                Decisions.autoWhip(g);
                Week.resolve(g);
                Decisions.autoAll(g);
                if (g.week % 26 == 0) {
                    Econ e = g.econ;
                    b.append("  wk").append(g.week).append(" ").append(dateStr(g.day())).append(": approval ").append(fmt1(g.approval)).append(" gdp ").append(fmt1(e.gdp)).append(" u ").append(fmt1(e.unemp))
                            .append(" infl ").append(fmt1(e.infl)).append(" fed ").append(fmt2(e.fed)).append(" 10y ").append(fmt2(e.tenYr)).append(" sp ").append(Math.round(e.stocks))
                            .append(" gas ").append(fmt2(e.gas)).append(" def ").append(fmt1(e.deficit)).append(" debt ").append(fmt1(e.debt)).append(" sent ").append(fmt0(e.sentiment))
                            .append(" stamina ").append(fmt0(g.stamina)).append(" stress ").append(fmt0(g.stress)).append(" PA ").append(fmt0(g.stateApp[stateIdx("PA")]))
                            .append(" Black ").append(fmt0(g.groupApp[10])).append(" WhiteNoDeg ").append(fmt0(g.groupApp[8])).append("\n");
                }
            }
            Congress c = g.cong;
            b.append("  congress: laws ").append(c.laws).append(" vetoes ").append(c.vetoes).append(" overrides ").append(c.overrides).append(" shutdowns ").append(c.shutdowns).append(" (").append(c.shutdownTotal).append(" wks) default ").append(c.defaulted)
                    .append(" capital ").append(fmt0(g.capital)).append(" House D ").append(g.houseD).append(" Senate D ").append(g.senateD).append("\n");
            for (Bill bl : c.bills) b.append("    ").append(bl.ours ? "* " : "  ").append(bl.name).append(": ").append(STAGE[bl.stage]).append(" wk").append(bl.introduced).append("-").append(bl.stageWeek).append(bl.recon ? " recon" : "").append(" H ").append(bl.houseYes).append("-").append(bl.houseNo).append(" S ").append(bl.senYes).append("-").append(bl.senNo).append("\n");
            if (c.midSummary != null) b.append("  midterms: ").append(c.midSummary).append("\n");
            b.append("  news ").append(g.news.size()).append(", last: ").append(g.news.isEmpty() ? "" : g.news.get(g.news.size() - 1).head);
            System.out.println(b);
        }

        static void whip(int year, String pres, int scen) {
            Game g = newGame(7, year, pres, scen);
            StringBuilder s = new StringBuilder("=== whip " + g.pres.name() + " House D " + g.houseD + " Senate D " + g.senateD + " approval " + fmt1(g.approval) + "\n");
            for (BillDef d : HillUI.agenda(g)) {
                Bill b = Legis.draft(g, d);
                b.recon = d.budget && Legis.reconOk(g, b);
                s.append(String.format("%-34s econ %5.2f cult %5.2f sup %3.0f cost %5.0f %s  H %.0f (%.2f)  S %.0f (%.2f)  chair %.2f/%.2f%n", d.name, b.econ(), b.cult(), b.support(), b.netCost(), b.recon ? "R" : " ",
                        Legis.count(g, b, HOUSE, false)[0], Legis.chance(g, b, HOUSE, false), Legis.count(g, b, SENATE, false)[0], Legis.chance(g, b, SENATE, false), Legis.chairOk(g, b, HOUSE), Legis.chairOk(g, b, SENATE)));
                StringBuilder bl = new StringBuilder("      ");
                for (int i = 0; i < g.cong.blocs.size(); i++) bl.append(String.format("%.2f ", logistic(Legis.blocLogit(g, b, i, false))));
                s.append(bl).append("\n");
            }
            System.out.print(s);
        }

        static void shot(ResoluteDesk u, String dir, String name) {
            BufferedImage img = u.snapshot(1600, 900);
            try { javax.imageio.ImageIO.write(img, "png", new File(dir, name + ".png")); } catch (IOException e) { e.printStackTrace(); }
            System.out.println("shot " + name);
        }

        static void ui(String dir) {
            new File(dir).mkdirs();
            ResoluteDesk u = new ResoluteDesk();
            u.go(SC_HOME); shot(u, dir, "home");
            u.setup = new Settings();
            u.go(SC_SETUP); shot(u, dir, "setup");
            u.setup.year = 2025;
            u.go(SC_PICK); Screens.pickParty = REP; Screens.sel = 0; shot(u, dir, "pick");
            Creator.begin(u, DEM); shot(u, dir, "create");
            u.setup.presId = "trump"; u.setup.vpId = "vance"; u.go(SC_VP); shot(u, dir, "vp");
            u.game = newGame(5, 2025, "trump", 0);
            u.go(SC_OVAL); shot(u, dir, "oval");
            for (int w = 0; w < 30; w++) { if (w % 8 == 3) Rooms.roadWeek(u.game); else Week.routine(u.game); if (u.game.stamina < 40) Rooms.restWeek(u.game); if (w % 6 == 1) Decisions.autoAgenda(u.game); Week.resolve(u.game); Decisions.autoAll(u.game); }
            u.go(SC_PAPER); shot(u, dir, "paper");
            u.go(SC_OVAL); shot(u, dir, "oval_later");
            for (int r = 0; r < NROOM; r++) { Rooms.open(u, r); if (r == R_DESK) Rooms.roadWeek(u.game); shot(u, dir, "room_" + r); }
            Rooms.chartSel = 2; Rooms.open(u, R_ECON); shot(u, dir, "room_econ_stocks");
            Rooms.mapMode = 1; Rooms.selState = stateIdx("MI"); Rooms.open(u, R_COUNTRY); shot(u, dir, "room_country_unemp");
            Rooms.open(u, R_CONGRESS);
            for (int t = 0; t < HillUI.TABS.length; t++) { HillUI.tab = t; HillUI.sel = -1; shot(u, dir, "hill_" + t); }
            HillUI.tab = 1; HillUI.draftSel = 1; HillUI.draft = Legis.draft(u.game, HillUI.agenda(u.game).get(1)); shot(u, dir, "hill_draft");
            HillUI.tab = 0; HillUI.chairCh = 1; shot(u, dir, "hill_senate_chairs");
            for (Bill b : u.game.cong.bills) if (b.stage == ST_LAW) { Legis.toDesk(u.game, b); break; }
            Decisions.show(u, u.game); shot(u, dir, "decision_sign");
            u.modal = null; u.game.inbox.clear();
            u.game.cong.approps[0] = 1;
            Budget.deadline(u.game, true); Decisions.show(u, u.game); shot(u, dir, "decision_cr");
        }
    }
}
