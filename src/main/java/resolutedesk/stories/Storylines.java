package resolutedesk.stories;

import static resolutedesk.core.Dates.*;
import static resolutedesk.core.Num.*;
import static resolutedesk.data.StateData.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.model.NewsKinds.*;
import static resolutedesk.sim.DecisionKinds.*;
import static resolutedesk.stories.StoryData.*;

import java.util.ArrayList;
import java.util.List;
import resolutedesk.data.Names;
import resolutedesk.model.Game;
import resolutedesk.sim.Decision;
import resolutedesk.sim.Decisions;
import resolutedesk.world.Diplo;

/**
 * Storylines that grow: a bank run, a strike wave, a protest movement, a third-party insurgency and a dozen more.
 * Each starts small and comes back to the president at every stage. A storyline has heat: what the president
 * does cools it or feeds it, and the state of the country (jobs, prices, approval, a war) keeps feeding it.
 * At the end of each stage a hot story grows into the next, a cool one fades, and one that reaches the end of
 * its last stage still hot takes over the presidency.
 */
public final class Storylines {
    private static List<StorylineDef> defs;

    private Storylines() {
    }

    public static List<StorylineDef> defs() {
        if (defs == null) {
            List<StorylineDef> l = new ArrayList<StorylineDef>();
            for (String[] rows : StorylineTable.LINES) l.add(new StorylineDef(rows));
            defs = l;
        }
        return defs;
    }

    public static StorylineDef def(Storyline l) { return defs().get(l.def); }

    /** Storylines still under way. */
    public static List<Storyline> active(Game g) {
        List<Storyline> l = new ArrayList<Storyline>();
        if (g.stories != null) for (Storyline x : g.stories.lines) if (x.status == SL_ACTIVE) l.add(x);
        return l;
    }

    public static void week(Game g) {
        StoryState s = g.stories;
        for (int i = 0; i < s.cool.length; i++) if (s.cool[i] > 0) s.cool[i]--;
        for (Storyline l : active(g)) step(g, l);
        if (active(g).size() >= SL_MAX) return;
        int y = yearNow(g);
        for (int i = 0; i < defs().size(); i++) {
            StorylineDef d = defs().get(i);
            if (s.cool[i] > 0 || y < d.from || y > d.to || running(g, i)) continue;
            if (triggered(g, d)) { start(g, i); return; }
        }
    }

    /** A storyline's trigger is one or more sets of conditions, separated by spaces: any one set starts it. */
    static boolean triggered(Game g, StorylineDef d) {
        for (String alt : d.trigger.split(" ")) if (!alt.isEmpty() && Stories.holds(g, alt)) return true;
        return false;
    }

    static boolean running(Game g, int i) {
        for (Storyline l : active(g)) if (l.def == i) return true;
        return false;
    }

    /** Begins a storyline at its first stage, with a made-up figure at its center and a state where it starts. */
    public static Storyline start(Game g, int i) {
        Storyline l = new Storyline();
        l.def = i;
        l.heat = clamp(SL_START + g.rng.nextGaussian() * 4, 30, 70);
        l.status = SL_ACTIVE;
        l.stageWeek = g.week;
        l.started = g.week;
        l.who = Names.genFirst(g.rng, Names.genFemale(g.rng, 0.8)) + " " + Names.MORE_LASTS[g.rng.nextInt(Names.MORE_LASTS.length)];
        l.state = Stories.someState(g);
        g.stories.lines.add(l);
        announce(g, l);
        return l;
    }

    /** A week of a storyline: the country feeds it or lets it cool; at the end of a stage it grows, fades or lingers. */
    static void step(Game g, Storyline l) {
        StorylineDef d = def(l);
        l.heat = clamp(l.heat + fuel(g, d) - SL_COOL + g.rng.nextGaussian() * 0.8, 0, 100);
        Stories.fx(g, fill(d.weekFx[l.stage], l));
        if (g.week - l.stageWeek < d.weeks[l.stage]) return;
        if (l.heat >= SL_GROW) { if (l.stage + 1 < d.stages()) grow(g, l); else runaway(g, l); }
        else if (l.heat <= SL_FADE || l.lingers >= 2) fade(g, l);
        else { l.lingers++; l.stageWeek = g.week - d.weeks[l.stage] + 3; }
    }

    /** What keeps a storyline going this week: "unemp>7:0.6 app<40:0.4" adds 0.6 while unemployment is over 7 and 0.4 while approval is under 40. */
    static double fuel(Game g, StorylineDef d) {
        double f = 0;
        for (String tok : d.fuel.split(" ")) {
            int c = tok.lastIndexOf(':');
            if (c <= 0) continue;
            if (Stories.holds(g, tok.substring(0, c))) f += Double.parseDouble(tok.substring(c + 1));
        }
        return f;
    }

    static void grow(Game g, Storyline l) {
        l.stage++;
        l.stageWeek = g.week;
        l.lingers = 0;
        announce(g, l);
    }

    /** The news of a new stage, and the decision it brings to the president. */
    static void announce(Game g, Storyline l) {
        StorylineDef d = def(l);
        String[] st = d.stage[l.stage];
        String head = fill(st[0], l), body = fill(st[1], l);
        g.log(l.stage == 0 ? NK_BAD : NK_BIG, head, body, 1.6 + l.stage * 0.7);
        String[][] o = d.options[l.stage];
        String[] lab = new String[o.length], adv = new String[o.length];
        for (int k = 0; k < o.length; k++) {
            lab[k] = o[k][0];
            adv[k] = o[k][1] + ": \"" + Diplo.odds(g, o[k][2], Double.parseDouble(o[k][3])) + "\"";
        }
        String where = d.name + ", stage " + (l.stage + 1) + " of " + d.stages() + (l.stage + 1 < d.stages() ? ". Left alone, stories like this grow." : ". If it doesn't cool now, it will take over the presidency.");
        Decision dec = Decisions.add(g, D_STORY, head, body + "\n\n" + where, lab, adv);
        dec.story = l;
        if (l.stage > 0) g.stress = Math.min(100, g.stress + 2 + l.stage);
    }

    /** The president's answer at a stage: it cools the story or feeds it, with what else it does. */
    public static void decide(Game g, Decision dec, int k) {
        Storyline l = dec.story;
        if (l == null || l.status != SL_ACTIVE) return;
        String[] o = def(l).options[l.stage][k];
        double base = Double.parseDouble(o[3]);
        boolean ok = g.rng.nextDouble() < Diplo.p(g, o[2], base);
        if (base < 1) { if (ok) g.okCount++; else g.failCount++; }
        l.heat = clamp(l.heat + (ok ? Double.parseDouble(o[4]) : Double.parseDouble(o[5]) + (base < 1 ? SL_BACKFIRE : 0)), 0, 100);
        l.choices++;
        Stories.fx(g, fill(ok ? o[6] : o[7], l));
        g.log(ok ? NK_GOOD : NK_BAD, fill(ok ? o[8] : o[9], l), null, 1.3);
    }

    /** What the hands-off president does: whatever is most likely to cool the story. */
    public static int auto(Game g, Decision dec) {
        Storyline l = dec.story;
        if (l == null) return 0;
        String[][] o = def(l).options[l.stage];
        int best = 0;
        double bv = 1e9;
        for (int k = 0; k < o.length; k++) {
            double p = Diplo.p(g, o[k][2], Double.parseDouble(o[k][3]));
            double v = p * Double.parseDouble(o[k][4]) + (1 - p) * Double.parseDouble(o[k][5]);
            if (v < bv) { bv = v; best = k; }
        }
        return best;
    }

    static void fade(Game g, Storyline l) {
        StorylineDef d = def(l);
        l.status = SL_FADED;
        l.ended = g.week;
        g.stories.cool[l.def] = SL_REST;
        g.log(NK_INFO, fill(d.fade, l), null, 1.0);
    }

    /** The storyline takes over: its full effect, a lasting hold on what voters care about, and a place in the record. */
    static void runaway(Game g, Storyline l) {
        StorylineDef d = def(l);
        l.status = SL_RUNAWAY;
        l.ended = g.week;
        g.stories.cool[l.def] = SL_REST * 2;
        Stories.fx(g, fill(d.endFx, l));
        String head = fill(d.endHead, l), body = fill(d.endBody, l);
        g.log(NK_BIG, head.toUpperCase(), body, 4.5);
        g.milestones.add(dateStr(g.day()) + ": " + d.name.toLowerCase() + " took over the presidency");
        g.stress = Math.min(100, g.stress + 8);
        Decisions.info(g, head, body);
    }

    /** Ends a storyline at once, for tests and for history that overtakes it. */
    public static void end(Game g, Storyline l) {
        if (l.status != SL_ACTIVE) return;
        l.status = SL_ENDED;
        l.ended = g.week;
        g.stories.cool[l.def] = SL_REST;
    }

    static String fill(String s, Storyline l) { return Stories.fill(s, STATES[l.state].name, l); }

    /** A storyline's name, stage and heat in a line, for the paper and the daily brief. */
    public static String describe(Storyline l) {
        StorylineDef d = def(l);
        return d.name + ": stage " + (l.stage + 1) + " of " + d.stages() + (l.heat >= SL_GROW ? ", and growing" : l.heat <= SL_FADE + 5 ? ", and cooling" : "");
    }

    /** The current stage's headline, with its blanks filled in. */
    public static String stageHead(Storyline l) { return fill(def(l).stage[l.stage][0], l); }
}
