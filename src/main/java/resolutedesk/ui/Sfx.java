package resolutedesk.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

/** Interface sounds, every one synthesised in code. */
public final class Sfx implements Runnable {
    static final int RATE = 22050;
    public static final int CLICK = 0, HOVER = 1, CHIME = 2, CHEER = 3, TURN = 4, CASH = 5, ALERT = 6, ERROR = 7, OPEN = 8,
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

    public Sfx() { build(); }

    public void start() {
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

    public void stop() {
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
