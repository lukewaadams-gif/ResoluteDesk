package resolutedesk.world;

import static resolutedesk.history.EraRules.*;
import static resolutedesk.world.WarData.*;
import static resolutedesk.world.WorldData.*;

import resolutedesk.history.History;
import resolutedesk.model.Game;

/** Questions about a country in the current game: its name, alignment, power and trade. */
public final class Countries {
    private Countries() {
    }

    public static String cname(Game g, int c) { return History.countryName(g, c); }

    /** The country's name as it reads in a sentence: "the United Kingdom", "the Soviet Union". */
    public static String cnameIn(Game g, int c) {
        String n = cname(g, c);
        return n.startsWith("United ") || n.startsWith("Soviet ") || n.equals("Philippines") || n.startsWith("Allied ") ? "the " + n : n;
    }

    public static int align(Game g, int c) { return g.world != null && g.world.align != null ? g.world.align[c] : CT_ALIGN[c]; }

    public static boolean gone(Game g, int c) { return g.world != null && g.world.gone != null && g.world.gone[c]; }

    public static double power(Game g, int c) { return g.world != null && g.world.power != null ? g.world.power[c] : CT_POWER[c]; }

    public static boolean nuke(Game g, int c) { return g.world != null && g.world.nuke != null ? g.world.nuke[c] : CT_NUKE[c]; }

    /** Two-way trade with a country in the era's dollars ($ billions a year). */
    public static double tradeOf(Game g, int c) {
        if (gone(g, c) || atWar(g, c)) return 0;
        World w = g.world;
        double k = w != null ? w.tradeK : 1;
        if (w != null && w.align != null && w.align[c] == AL_ADVERSARY && yearNow(g) < 1990) k *= 0.05;
        return CT_TRADE[c] * k;
    }

    /** Is the country fighting American troops right now? */
    public static boolean atWar(Game g, int c) {
        if (g.world == null || g.world.wars == null) return false;
        for (War x : g.world.wars) if (x.stage == 2 && x.enemy == c) return true;
        return false;
    }

    public static double tradeBal(Game g, int c) { double t = CT_TRADE[c]; return t <= 0 ? 0 : CT_BAL[c] * tradeOf(g, c) / t; }

    public static int warIndex(String id) { for (int i = 0; i < WAR_ID.length; i++) if (WAR_ID[i].equals(id)) return i; return 0; }
}
