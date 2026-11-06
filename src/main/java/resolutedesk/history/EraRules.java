package resolutedesk.history;

import static resolutedesk.congress.CongressRules.*;
import static resolutedesk.core.Dates.*;
import static resolutedesk.core.Fmt.*;
import static resolutedesk.data.ElectoralMap.*;
import static resolutedesk.data.Names.*;
import static resolutedesk.data.StateData.*;
import static resolutedesk.executive.Posts.*;
import static resolutedesk.opinion.Issues.*;
import static resolutedesk.press.Outlets.*;

import resolutedesk.model.Era;
import resolutedesk.model.Game;
import resolutedesk.ui.Flag;
import resolutedesk.ui.PressRoom;

/** Applying an era: the calendar, electoral votes, prices and the issues of the day. */
public final class EraRules {
    private EraRules() {
    }

    /** The year in which the game's current term began. */
    public static int termYear(Game g) { return g.era.year + 4 * (g.term - 1); }

    /** The week the current term ends: noon on January 20 four years after it began. */
    public static int termEnd(Game g) { return (int) Math.floor(dayOfDate(termYear(g) + 4, 0, 20) / 7.0); }

    /** The calendar year now. */
    public static int yearNow(Game g) { return dateOf(g.day())[0]; }

    // ---- what changes with the era: the calendar, the Electoral College, the names of issues, the money ----------
    public static double COST_SCALE = 1;

    public static final String[] PISSUE0 = PISSUE.clone(), PISSUE_SHORT0 = PISSUE_SHORT.clone();

    public static void applyEra(Era e) {
        CAL_Y = e.year; CAL_M = e.inM; CAL_D = e.inD;
        if (STATE_EV24[0] == 0) for (int s = 0; s < NS; s++) STATE_EV24[s] = STATES[s].ev;
        setEv(e.year);
        for (int i = 0; i < NPI; i++) { PISSUE[i] = PISSUE0[i]; PISSUE_SHORT[i] = PISSUE_SHORT0[i]; }
        if (e.labels != null) for (String p : e.labels.split(" (?=\\d+=)")) {
            int k = p.indexOf('=');
            int i = Integer.parseInt(p.substring(0, k));
            PISSUE[i] = p.substring(k + 1);
            PISSUE_SHORT[i] = PISSUE[i].length() > 12 ? PISSUE[i].substring(0, PISSUE[i].indexOf(' ') > 0 ? PISSUE[i].indexOf(' ') : 12) : PISSUE[i];
        }
        COST_SCALE = e.year < 2021 ? e.nominalGdp / 29.7 : 1;
        GEN_YEAR = e.year;
        applyYear(e.year);
    }

    /** Sets the electoral votes in use for the election held in a year. */
    public static void setEv(int year) {
        if (STATE_EV24[0] == 0) for (int s = 0; s < NS; s++) STATE_EV24[s] = STATES[s].ev;
        int[] ev = evFor(year);
        for (int s = 0; s < NS; s++) STATES[s].ev = ev[s];
        EV_YEAR = year;
    }

    /** Party money ($ millions in today's terms) in the era's own dollars, in words. */
    public static String partyMoney(double m) {
        double a = Math.abs(m) * 1e6 * COST_SCALE;
        String s = a >= 9.95e6 ? "$" + Math.round(a / 1e6) + " million" : a >= 1e6 ? "$" + fmt1(a / 1e6) + " million" : a >= 1e4 ? "$" + thousands(Math.round(a / 1000) * 1000) : "$" + thousands(Math.round(a / 100) * 100);
        return (m < 0 ? "-" : "") + s;
    }

    /** The calendar year the game last changed over to (applyYear). */
    public static int CAL_YEAR = 2025;

    /** What changes with the calendar year: the press corps and the name of the president's megaphone. */
    public static void applyYear(int year) {
        eraPosts(year);
        eraChairs(year);
        CAL_YEAR = year; Flag.year = year; Flag.roc = year < 1950;
        String[] t = year < 1953 ? PRESS_1933 : year < 1981 ? PRESS_1953 : year < 1997 ? PRESS_1981 : year < 2009 ? PRESS_1997 : null;
        for (int i = 0; i < OUT_NAME.length; i++) {
            if (t == null) { OUT_NAME[i] = OUT_NAME0[i]; OUT_LEAN[i] = OUT_LEAN0[i]; OUT_AUD[i] = OUT_AUD0[i]; OUT_SIT[i] = OUT_SIT0[i]; continue; }
            String[] p = t[i].split("\\|");
            OUT_NAME[i] = p[0]; OUT_LEAN[i] = Double.parseDouble(p[1]); OUT_AUD[i] = Double.parseDouble(p[2]); OUT_SIT[i] = p[3].equals("y");
        }
        if (year >= 1971 && year < 1981) OUT_NAME[13] = "NPR";
        if (year >= 1968 && year < 1981) OUT_NAME[17] = "60 Minutes";
        if (year >= 1988 && year < 1997) OUT_NAME[12] = "Conservative talk radio";
        else if (year < 1988 && year >= 1981) { OUT_NAME[12] = "The Moral Majority Report"; OUT_AUD[12] = 1; }
        PressRoom.TABS[2] = year < 2009 ? "Statements" : "Social media";
    }
}
