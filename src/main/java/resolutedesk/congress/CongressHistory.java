package resolutedesk.congress;

import static resolutedesk.data.ElectoralMap.*;
import static resolutedesk.data.StateData.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Senate classes and the party coalitions of each era. */
public final class CongressHistory {
    private CongressHistory() {
    }

    // ---- the Senate's three classes -------------------------------------------------------------------------
    public static final String[] SEN_CLASS = {
            "AZ CA CT DE FL HI IN ME MD MA MI MN MS MO MT NE NV NJ NM NY ND OH PA RI TN TX UT VT VA WA WV WI WY",
            "AL AK AR CO DE GA ID IL IA KS KY LA ME MA MI MN MS MT NE NH NJ NM NC OK OR RI SC SD TN TX VA WV WY",
            "AL AK AZ AR CA CO CT FL GA HI ID IL IN IA KS KY LA MD MO NV NH NY NC ND OH OK OR PA SC SD UT VT WA WI"};

    /** The Senate seats up in an election year: "AB:D" for each, the party holding it drawn from the chamber's balance and the state's lean. */
    public static String senateUp(int year, int senateD) {
        int k = Math.floorMod(year - 2024, 6) / 2;
        List<Integer> st = new ArrayList<Integer>();
        for (String ab : SEN_CLASS[k].split(" ")) { int s = stateIdx(ab); if (s >= 0 && (year >= 1959 || !ab.equals("AK") && !ab.equals("HI"))) st.add(s); }
        final int yy = year;
        Collections.sort(st, (a, b) -> Double.compare(leanIn(b, yy), leanIn(a, yy)));
        int dHeld = (int) Math.round(st.size() * senateD / 100.0);
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < st.size(); i++) b.append(b.length() > 0 ? " " : "").append(STATES[st.get(i)].ab).append(i < dHeld ? ":D" : ":R");
        return b.toString();
    }

    // ---- the parties in Congress before today's sorting: the conservative coalition and the liberal Republicans ----
    /** name|share|econ|cult|loyalty|deficit hawk, for the 12 bloc slots (House D x3, House R x3, Senate D x3, Senate R x3). */
    public static final String[] TIER_NEWDEAL = {"New Dealers|0.42|-0.6|-0.2|0.85|0.2", "Northern regulars|0.28|-0.4|0|0.85|0.4", "Southern Democrats|0.30|-0.2|0.75|0.7|0.5",
            "Old Guard Republicans|0.45|0.65|0.35|0.8|0.8", "Mainstream Republicans|0.35|0.4|0.15|0.85|0.6", "Liberal Republicans|0.20|-0.1|-0.25|0.65|0.4",
            "New Deal senators|0.40|-0.6|-0.2|0.85|0.2", "Mainstream Democrats|0.28|-0.4|0|0.85|0.4", "Southern Democrats|0.32|-0.2|0.75|0.7|0.5",
            "Old Guard senators|0.45|0.65|0.35|0.8|0.8", "Mainstream Republicans|0.35|0.4|0.15|0.85|0.6", "Liberal Republicans|0.20|-0.1|-0.25|0.65|0.4"};

    public static final String[] TIER_REALIGN = {"Liberal Democrats|0.45|-0.6|-0.5|0.8|0.2", "Mainstream Democrats|0.32|-0.35|-0.2|0.85|0.4", "Boll Weevils|0.23|0.2|0.5|0.55|0.6",
            "Conservatives|0.42|0.7|0.6|0.75|0.8", "Mainstream Republicans|0.40|0.45|0.3|0.85|0.6", "Gypsy Moths|0.18|0.1|-0.1|0.65|0.4",
            "Liberal senators|0.42|-0.6|-0.5|0.8|0.2", "Mainstream Democrats|0.35|-0.35|-0.2|0.85|0.4", "Southern conservatives|0.23|0.2|0.5|0.6|0.6",
            "Conservatives|0.42|0.7|0.6|0.75|0.8", "Mainstream Republicans|0.40|0.45|0.3|0.85|0.6", "Moderate Republicans|0.18|0.1|-0.1|0.65|0.4"};

    public static final String[] TIER_SORTED = {"Progressive Caucus|0.35|-0.6|-0.6|0.8|0.15", "Mainstream Democrats|0.42|-0.4|-0.45|0.9|0.3", "Blue Dogs|0.23|-0.1|0|0.7|0.65",
            "Conservatives|0.30|0.75|0.7|0.65|0.85", "Mainstream Republicans|0.55|0.55|0.5|0.85|0.5", "Moderate Republicans|0.15|0.25|0.1|0.75|0.4",
            "Liberal senators|0.25|-0.65|-0.6|0.85|0.15", "Mainstream Democrats|0.58|-0.4|-0.4|0.9|0.3", "Moderate Democrats|0.17|-0.1|-0.1|0.7|0.5",
            "Hardliners|0.20|0.8|0.7|0.6|0.85", "Mainstream Republicans|0.65|0.55|0.5|0.85|0.5", "Moderate Republicans|0.15|0.2|0.05|0.7|0.4"};

    /** The bloc templates for a year: null means today's. */
    public static String[] tierFor(int year) { return year < 1965 ? TIER_NEWDEAL : year < 1995 ? TIER_REALIGN : year < 2015 ? TIER_SORTED : null; }
}
