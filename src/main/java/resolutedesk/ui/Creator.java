package resolutedesk.ui;

import static resolutedesk.core.Num.*;
import static resolutedesk.data.Candidates.*;
import static resolutedesk.data.Eras.*;
import static resolutedesk.data.StateData.*;
import static resolutedesk.model.Attributes.*;
import static resolutedesk.model.Factions.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.sim.Rosters.*;
import static resolutedesk.ui.Draw.*;
import static resolutedesk.ui.Fonts.*;
import static resolutedesk.ui.ScreenIds.*;
import static resolutedesk.ui.Theme.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Random;
import resolutedesk.ResoluteDesk;
import resolutedesk.model.Cand;
import resolutedesk.model.Look;

/** The screen for creating your own president. */
public final class Creator {
    static Cand c;
    static int arch;
    static int[] spent = new int[Screens.SHOW_ATTR.length];
    static final int POINTS = 40;

    public static void begin(ResoluteDesk u, int party) {
        c = new Cand();
        c.id = "custom"; c.custom = true; c.first = "Alex"; c.last = "Morgan"; c.party = party; c.role = "Governor"; c.homeAb = "PA"; c.home = stateIdx("PA"); c.born = u.setup.year - 58;
        c.faction = party == DEM ? F_LIB : F_GOV;
        c.econ = FACTIONS[c.faction].econ; c.cult = FACTIONS[c.faction].cult; c.plat = FACTIONS[c.faction].pos.clone();
        arch = 0;
        Arrays.fill(spent, 0);
        apply();
        c.look = randomLook(new Random(Screens.freshSeed()));
        u.go(SC_CREATE);
    }

    static void apply() {
        String[] t = ARCH_BASE[arch].trim().split("\\s+");
        for (int i = 0; i < NA; i++) c.a[i] = Integer.parseInt(t[i]);
        for (int i = 0; i < spent.length; i++) c.a[Screens.SHOW_ATTR[i]] = clampi(c.a[Screens.SHOW_ATTR[i]] + spent[i] * 3 * (Screens.SHOW_ATTR[i] == A_SCA ? -1 : 1), 5, 99);
        c.role = ARCHETYPE[arch];
    }

    public static Look randomLook(Random r) {
        Look l = new Look();
        l.female = r.nextBoolean();
        l.skin = r.nextInt(8); l.hair = r.nextInt(8); l.style = l.female ? 6 + r.nextInt(4) : r.nextInt(5);
        l.gray = r.nextDouble() * 0.6; l.facial = l.female ? 0 : r.nextInt(3) == 0 ? 1 : 0; l.glasses = r.nextInt(4) == 0 ? 1 : 0;
        l.faceW = 0.92 + r.nextDouble() * 0.16; l.jaw = 0.3 + r.nextDouble() * 0.5; l.suit = r.nextInt(6); l.tie = r.nextInt(9); l.age = 0.4 + r.nextDouble() * 0.4; l.smile = 0.4 + r.nextDouble() * 0.5;
        l.earrings = l.female && r.nextBoolean(); l.lipstick = l.female && r.nextBoolean();
        return l;
    }

    static int left() { int n = 0; for (int v : spent) n += Math.abs(v); return POINTS - n; }

    public static void draw(ResoluteDesk u, Graphics2D g) {
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
        u.button(g, 236, y + 10, 200, 30, "New look", B_NORM, () -> c.look = randomLook(new Random(Screens.freshSeed())));
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
