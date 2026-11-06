package resolutedesk.sim;

import static resolutedesk.sim.DecisionKinds.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * How the computer governs in watch mode: six priorities from 0 to 1, the bills it sends to Congress and in what
 * order, and the kinds of decision it stops for so the player can make them. The middle of every priority is the
 * steady hand the game has always used, so a new plan plays exactly as watch mode did before.
 */
public final class WatchPlan implements Serializable {
    private static final long serialVersionUID = 1L;

    /** The priorities: how many bills at once, how bipartisan, how hawkish, how many executive orders, how much travel, how cautious. */
    public static final int P_AMBITION = 0, P_BIPARTISAN = 1, P_HAWK = 2, P_ORDERS = 3, P_TRAVEL = 4, P_CAUTION = 5, NPRIORITY = 6;
    public static final String[] PRIORITY = {"Legislative ambition", "Bipartisanship", "Foreign policy", "Executive orders", "Travel and campaigning", "Caution"};
    public static final String[][] PRIORITY_ENDS = {{"One bill at a time", "Four bills at once"}, {"Party-line bills", "Bills across the aisle"}, {"Dove", "Hawk"},
            {"Rarely", "Every few weeks"}, {"Stay in Washington", "On the road"}, {"Takes risks", "Plays it safe"}};
    public static final String[] PRIORITY_DESC = {"How many of your bills the computer keeps moving in Congress at once.",
            "Whether it leads with your party's bills, sized to pass on party lines, or with bipartisan ones, trimmed to win votes across the aisle.",
            "How readily it reaches for force in a crisis, and how long it keeps fighting a war that's going badly.",
            "How often it signs an executive order from your party's library.",
            "How often it spends a week on the road around the country.",
            "Whether it takes the sure thing or the gamble with the bigger payoff."};

    /** Ready-made plans. The last is whatever the player has set by hand. */
    public static final String[] PRESET = {"Steady hand", "Big agenda", "Deal-maker", "Hawk", "Dove", "On the road", "Your own"};
    public static final String[] PRESET_DESC = {"The cautious middle on everything: the way the game has always played itself.",
            "Four bills at once, sized for your own party, and plenty of executive orders.",
            "Bipartisan bills trimmed to pass, few orders, and safe choices.",
            "Force is on the table in every crisis, and wars are fought to the end.",
            "Diplomacy first; wars that go badly end early.",
            "A week on the road every few weeks, and the campaign never stops.",
            "Your own settings."};
    static final double[][] PRESET_VALUES = {{0.5, 0.5, 0.5, 0.5, 0.5, 0.5}, {1, 0.2, 0.5, 0.85, 0.5, 0.4}, {0.5, 1, 0.4, 0.2, 0.5, 0.8},
            {0.5, 0.4, 1, 0.6, 0.5, 0.4}, {0.5, 0.6, 0, 0.5, 0.5, 0.6}, {0.5, 0.5, 0.5, 0.5, 1, 0.5}};
    public static final int CUSTOM = PRESET.length - 1;

    /** The decisions watch mode can stop for: bills to sign, the budget, crises and war, the courts, scandal, storylines and the campaign. */
    public static final int S_SIGN = 0, S_BUDGET = 1, S_WORLD = 2, S_COURT = 3, S_SCANDAL = 4, S_STORY = 5, S_CAMPAIGN = 6, NSTOP = 7;
    public static final String[] STOP = {"Bills to sign or veto", "Shutdowns and the debt limit", "Crises, war and turning points", "Court rulings", "Scandals and impeachment",
            "Storylines", "The campaign"};

    public int preset;
    public double[] p = {0.5, 0.5, 0.5, 0.5, 0.5, 0.5};
    public List<String> bills = new ArrayList<String>();   // bill ids to send, in order; empty means the computer's own order
    public boolean[] stop = new boolean[NSTOP];

    /** Applies a ready-made plan. */
    public void use(int k) {
        preset = k;
        if (k < CUSTOM) for (int i = 0; i < NPRIORITY; i++) p[i] = PRESET_VALUES[k][i];
    }

    /** Sets one priority by hand, which makes the plan the player's own. */
    public void set(int i, double v) {
        p[i] = Math.max(0, Math.min(1, v));
        preset = CUSTOM;
    }

    /** Which kind of stop a decision falls under, or -1 if watch mode always decides it. */
    public static int stopFor(int kind) {
        switch (kind) {
            case D_SIGN: return S_SIGN;
            case D_SHUTDOWN: case D_DEBT: case D_CR: return S_BUDGET;
            case D_WORLD: case D_HIST: case D_WARPOWERS: return S_WORLD;
            case D_COURT: return S_COURT;
            case D_SCANDAL: case D_IMPEACH: case D_SUBPOENA: case D_PARDON: return S_SCANDAL;
            case D_STORY: return S_STORY;
            case D_CAMPAIGN: return S_CAMPAIGN;
            default: return -1;
        }
    }

    /** Whether watch mode stops for this decision. */
    public boolean stops(int kind) { int s = stopFor(kind); return s >= 0 && stop[s]; }

    /** Bills kept moving at once: two in the middle, one to four at the ends. */
    public int activeBills() { return 1 + (int) Math.round(p[P_AMBITION] * 3 - 0.5 + 1e-9); }

    /** Weeks between executive orders: nine in the middle, every three weeks at most. */
    public int orderWeeks() { return Math.max(3, (int) Math.round(17 - 16 * p[P_ORDERS])); }

    /** Weeks between road trips: eight in the middle, every second week at most. */
    public int roadWeeks() { return Math.max(2, (int) Math.round(14 - 12 * p[P_TRAVEL])); }

    /** How much a military option is held back in a crisis: 0.12 in the middle, less for a hawk, more for a dove. */
    public double forcePenalty() { return 0.12 - (p[P_HAWK] - 0.5) * 0.5; }

    /** The front below which a war Congress wants ended is ended: 55 in the middle. */
    public double giveUpFront() { return 55 - (p[P_HAWK] - 0.5) * 60; }

    /** How much a sure but passive option is held back: 0.45 in the middle, less for a cautious plan. */
    public double surePenalty() { return 0.45 - (p[P_CAUTION] - 0.5) * 0.6; }
}
