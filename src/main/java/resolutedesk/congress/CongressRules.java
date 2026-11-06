package resolutedesk.congress;

import static resolutedesk.opinion.Issues.*;

/** Chambers, roles, bill stages and committees. */
public final class CongressRules {
    private CongressRules() {
    }

    //  The leaders are real; committee chairs, swing votes and the rank and file are fictional.
    public static final int HOUSE = 0, SENATE = 1;

    public static final String[] CHAMBER = {"House", "Senate"};

    public static final int RO_RANK = 0, RO_CHAIR = 1, RO_LEADER = 2, RO_SWING = 3;

    public static final int PRICE_PROJECT = 0, PRICE_APPOINT = 1, PRICE_ENDORSE = 2, PRICE_POLICY = 3;

    public static final String[] PRICE = {"a project back home", "a say in an appointment", "your endorsement", "a change to the bill"};

    public static final int ST_DRAFT = 0, ST_COMMITTEE = 1, ST_FLOOR = 2, ST_COMMITTEE2 = 3, ST_FLOOR2 = 4, ST_CONFERENCE = 5, ST_DESK = 6, ST_LAW = 7, ST_DEAD = 8, ST_OVERRIDE = 9;

    public static final String[] STAGE = {"Draft", "In committee", "Floor vote", "In committee (second chamber)", "Floor vote (second chamber)", "Conference", "On your desk", "Law", "Dead", "Veto override vote"};

    public static final String[] PAYFOR = {"Borrow it", "Tax the wealthy and corporations", "Cut spending elsewhere"};

    public static final String[] CHAIR_H = {"Ways and Means", "Appropriations", "Energy and Commerce", "Judiciary", "Armed Services", "Financial Services", "Education and Workforce",
            "Agriculture", "Transportation and Infrastructure", "Homeland Security", "Foreign Affairs", "Budget"};

    public static final String[] CHAIR_S = {"Finance", "Appropriations", "Health, Education, Labor and Pensions", "Judiciary", "Armed Services", "Banking", "Commerce",
            "Agriculture", "Environment and Public Works", "Homeland Security", "Foreign Relations", "Budget"};

    /** Which committee (index into the chair lists) takes up a bill on an issue. */
    public static int committeeFor(BillDef d, int chamber) {
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

    public static final String[] APPROPS = {"Agriculture", "Commerce, Justice, Science", "Defense", "Energy and Water", "Financial Services", "Homeland Security", "Interior and Environment",
            "Labor, Health and Human Services, Education", "Legislative Branch", "Military Construction and Veterans Affairs", "State and Foreign Operations", "Transportation and Housing"};

    // ---- committees of each era: chamber|index|until (exclusive)|name, earliest first --------------------------------
    public static final String[] CHAIR_ERA = {
            "H|4|1947|Military Affairs", "H|6|1947|Labor", "H|8|1947|Roads", "H|11|1947|Naval Affairs", "H|5|1975|Banking and Currency", "H|8|1975|Public Works",
            "H|11|1975|Veterans' Affairs", "H|10|1975|Foreign Affairs", "H|10|1979|International Relations", "H|2|1981|Interstate and Foreign Commerce",
            "H|5|1995|Banking, Finance and Urban Affairs", "H|6|1995|Education and Labor", "H|8|1995|Public Works and Transportation", "H|5|2001|Banking and Financial Services",
            "H|9|2003|Rules", "H|10|1995|Foreign Affairs", "H|10|2007|International Relations", "H|6|2007|Education and the Workforce", "H|6|2011|Education and Labor",
            "H|6|2019|Education and the Workforce", "H|6|2023|Education and Labor",
            "S|2|1947|Education and Labor", "S|4|1947|Military Affairs", "S|6|1947|Interstate Commerce", "S|8|1947|Post Offices and Post Roads", "S|11|1947|Rules",
            "S|9|1952|Expenditures in the Executive Departments", "S|6|1961|Interstate and Foreign Commerce", "S|5|1971|Banking and Currency", "S|11|1975|Rules and Administration",
            "S|2|1977|Labor and Public Welfare", "S|7|1977|Agriculture and Forestry", "S|8|1977|Public Works", "S|9|1978|Government Operations", "S|2|1999|Labor and Human Resources",
            "S|9|2005|Governmental Affairs"};

    public static final String[] CHAIR_H0 = CHAIR_H.clone(), CHAIR_S0 = CHAIR_S.clone();

    public static void eraChairs(int year) {
        boolean[][] set = new boolean[2][CHAIR_H.length];
        for (int k = 0; k < CHAIR_H.length; k++) { CHAIR_H[k] = CHAIR_H0[k]; CHAIR_S[k] = CHAIR_S0[k]; }
        for (String row : CHAIR_ERA) {
            String[] t = row.split("\\|");
            int ch = t[0].equals("H") ? 0 : 1, k = Integer.parseInt(t[1]);
            if (set[ch][k] || year >= Integer.parseInt(t[2])) continue;
            set[ch][k] = true;
            if (ch == 0) CHAIR_H[k] = t[3]; else CHAIR_S[k] = t[3];
        }
    }

    /** Is this the name, in any era, of committee k in the chamber? */
    public static boolean sameCommittee(int chamber, int k, String name) {
        if (name.equals(chamber == HOUSE ? CHAIR_H0[k] : CHAIR_S0[k]) || name.equals(chamber == HOUSE ? CHAIR_H[k] : CHAIR_S[k])) return true;
        String pre = (chamber == HOUSE ? "H|" : "S|") + k + "|";
        for (String row : CHAIR_ERA) if (row.startsWith(pre) && row.endsWith("|" + name)) return true;
        return false;
    }
}
