package resolutedesk.executive;

import static resolutedesk.model.Parties.*;
import static resolutedesk.opinion.Demographics.*;
import static resolutedesk.opinion.Issues.*;

import resolutedesk.model.Game;

/** Executive orders, agency rules and emergency declarations. */
public final class ActionData {
    private ActionData() {
    }

    // ---- executive actions: orders, rules and emergency declarations ----
    public static final int K_ORDER = 0, K_RULE = 1, K_EMERG = 2;

    public static final String[] ACT_KIND = {"Executive order", "Regulation", "National emergency"};

    public static final int EM_BORDER = 0, EM_TRADE = 1, EM_ENERGY = 2, EM_CLIMATE = 3, EM_CRIME = 4, NEMERG = 5;

    public static final String[] EMERG_NAME = {"the southern border", "trade and the economy", "energy supplies", "the climate", "crime in American cities"};

    public static final String[] AD_ID = {
            // emergencies
            "em_border", "em_trade", "em_energy", "em_climate", "em_crime",
            // executive orders: Republican-leaning
            "eo_troops", "eo_birthright", "eo_ieepa", "eo_freeze", "eo_schedf", "eo_rto", "eo_drill", "eo_dei", "eo_dereg", "eo_paris_out", "eo_aea", "eo_surge", "eo_rif", "eo_cartels",
            // executive orders: Democratic-leaning
            "eo_studentdebt", "eo_paris_in", "eo_leases", "eo_dreamers", "eo_wage20", "eo_abortion", "eo_police", "eo_ai_safety", "eo_unions",
            // executive orders: either side
            "eo_drugprice", "eo_stocks", "eo_gouging", "eo_spr", "eo_datacenters", "eo_grocery_tariffs",
            // regulations
            "rule_carbon", "rule_overtime", "rule_latefees", "rule_noncompete", "rule_nursing", "rule_junkfees", "rule_netneutral",
            "rule_cars_repeal", "rule_power_repeal", "rule_snapwork", "rule_banks", "rule_asylum", "rule_mining", "rule_hospital", "rule_chips"};

    public static final int[] AD_KIND = {2, 2, 2, 2, 2,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

    /** The first year each action makes sense. */
    public static final int[] AD_MIN = {1980, 1977, 1973, 2009, 1965, 1990, 2018, 1977, 1933, 2020, 2020, 1953, 2020, 1981, 2016, 2025, 1990, 1933, 2019,
            2020, 2016, 2009, 2012, 2014, 2022, 2020, 2023, 1960, 2003, 2012, 1973, 1977, 2023, 2025,
            2009, 1938, 2010, 2023, 2023, 2022, 2010, 2017, 2017, 1996, 2010, 2018, 1990, 2019, 2022};

    public static final String[] AD_NAME = {
            "Declare an emergency at the border", "Declare an economic emergency over trade", "Declare an energy emergency", "Declare a climate emergency", "Declare a crime emergency",
            "Send troops to the border", "End birthright citizenship", "Emergency tariffs on imports", "Freeze federal hiring", "Schedule F", "Federal workers back to the office",
            "Open federal waters to drilling", "End DEI programs", "Ten old rules out for every new one", "Leave the Paris climate agreement", "Deport gang members as alien enemies",
            "Surge federal agents into high-crime cities", "Cut the federal workforce by a tenth", "Designate the cartels as terrorist groups",
            "Cancel student debt", "Rejoin the Paris climate agreement", "Pause new oil and gas leases", "Protect Dreamers from deportation", "A $20 wage for federal contractors",
            "Protect access to abortion medication", "Accountability rules for federal agents", "Safety reporting for frontier AI", "No contracts for union-busting firms",
            "Most-favored-nation drug prices", "Ban stock trading by your appointees", "Crack down on price gouging", "Release oil from the strategic reserve", "Fast-track data centers on federal land",
            "Lift tariffs on groceries",
            "Carbon limits for power plants", "Overtime pay for four million more workers", "Cap credit-card late fees at $8", "Ban non-compete agreements", "Minimum staffing in nursing homes",
            "All-in pricing: ban junk fees", "Restore net neutrality", "Repeal the car emissions rules", "Repeal the power-plant rules", "Work requirements for food aid",
            "Loosen bank capital rules", "Restrict asylum at the border", "Speed up mining permits", "Hospital price transparency", "Tighter export controls on AI chips"};

    public static final String[] AD_DESC = {
            "Unlocks military construction money and troop deployments at the border.", "Unlocks tariffs under the International Emergency Economic Powers Act.",
            "Unlocks waivers to speed up drilling, refining and power plants.", "Unlocks a pause on fossil-fuel leasing and money for clean power.", "Unlocks federal deployments to cities.",
            "National Guard and active-duty troops help patrol the border.", "Children born to parents here illegally or temporarily no longer get citizenship.",
            "Tariffs on most imports, set by the White House, using emergency powers.", "No new federal hires except for security and safety.",
            "Reclassify tens of thousands of policy jobs as at-will.", "All federal employees back at their desks five days a week.",
            "Lease offshore and federal land for oil and gas.", "Shut down diversity, equity and inclusion offices and training.",
            "Agencies must repeal ten rules for every new one.", "Formally leave the global climate agreement.", "Use a 1798 wartime law to deport suspected gang members.",
            "Send FBI, DEA and ATF agents and federal prosecutors to the cities with the most violent crime.", "Lay off about a tenth of the civilian federal workforce.",
            "Treat drug cartels as foreign terrorist organizations.",
            "Cancel up to $20,000 of federal student debt per borrower.", "Rejoin the global climate agreement.", "No new oil and gas leases on federal land and water.",
            "Deferred action and work permits for undocumented immigrants brought here as children.", "Federal contractors must pay at least $20 an hour.",
            "Protect access to mifepristone by mail and at pharmacies.", "Body cameras and use-of-force limits for federal law enforcement.",
            "Labs training the most powerful AI models must report safety tests.", "Firms that break labor law lose federal contracts.",
            "Medicare pays no more for drugs than other rich countries do.", "Your political appointees can't trade individual stocks.",
            "The FTC and Justice Department go after price gouging in food and housing.", "Sell oil from the reserve to bring down gas prices.",
            "Lease federal land for AI data centers and the power plants to run them.", "Cut tariffs on imported food.",
            "Power plants must capture most of their carbon by 2035.", "Raise the salary threshold for overtime pay.", "Cap the late fees big card issuers charge.",
            "Make most non-compete clauses unenforceable.", "Minimum staffing for nursing homes paid by Medicare and Medicaid.", "Prices must include all mandatory fees up front.",
            "Treat broadband as a utility: no blocking or throttling.", "Roll back tailpipe rules that push electric cars.", "Roll back limits on power-plant emissions.",
            "Adults without children must work or train to get food aid.", "Lower capital requirements for big banks.", "Bar most asylum claims from people who cross between ports.",
            "Cut the wait for permits to mine critical minerals.", "Hospitals must post their real prices.", "Stop the most advanced AI chips from reaching China."};

    public static final int[] AD_AREA = {PI_IMMIG, PI_TRADE, PI_ENERGY, PI_ENERGY, PI_CRIME,
            PI_IMMIG, PI_IMMIG, PI_TRADE, PI_ETHICS, PI_ETHICS, PI_ETHICS, PI_ENERGY, PI_RIGHTS, PI_ECON, PI_ENERGY, PI_IMMIG, PI_CRIME, PI_ETHICS, PI_CRIME,
            PI_EDU, PI_ENERGY, PI_ENERGY, PI_IMMIG, PI_JOBS, PI_HEALTH, PI_CRIME, PI_TECH, PI_JOBS,
            PI_HEALTH, PI_ETHICS, PI_PRICES, PI_PRICES, PI_TECH, PI_PRICES,
            PI_ENERGY, PI_JOBS, PI_PRICES, PI_JOBS, PI_HEALTH, PI_PRICES, PI_TECH, PI_ENERGY, PI_ENERGY, PI_ECON, PI_ECON, PI_IMMIG, PI_ENERGY, PI_HEALTH, PI_TECH};

    public static final int[] AD_LEAN = {REP, REP, REP, DEM, REP,
            REP, REP, REP, REP, REP, REP, REP, REP, REP, REP, REP, REP, REP, REP,
            DEM, DEM, DEM, DEM, DEM, DEM, DEM, DEM, DEM,
            -1, -1, -1, -1, -1, -1,
            DEM, DEM, DEM, DEM, DEM, DEM, DEM, REP, REP, REP, REP, REP, REP, -1, -1};

    public static final double[] AD_SUP = {50, 40, 50, 44, 48,
            52, 35, 38, 55, 38, 60, 52, 46, 50, 42, 45, 52, 48, 64,
            50, 55, 43, 62, 58, 55, 60, 63, 47,
            74, 82, 70, 64, 52, 66,
            54, 60, 72, 63, 66, 75, 58, 45, 44, 60, 38, 54, 52, 82, 62};

    /** How likely the courts are to block it, before the judges and your lawyer are counted. */
    public static final double[] AD_LEGAL = {0.25, 0.35, 0.2, 0.45, 0.4,
            0.25, 0.92, 0.55, 0.08, 0.45, 0.05, 0.3, 0.3, 0.2, 0.02, 0.65, 0.3, 0.5, 0.1,
            0.8, 0.02, 0.5, 0.55, 0.2, 0.5, 0.1, 0.3, 0.35,
            0.55, 0.02, 0.3, 0.05, 0.25, 0.1,
            0.55, 0.5, 0.45, 0.65, 0.4, 0.2, 0.55, 0.3, 0.3, 0.35, 0.15, 0.55, 0.25, 0.2, 0.1};

    /** The emergency an order needs first, or -1. */
    public static final int[] AD_NEEDS = {-1, -1, -1, -1, -1,
            EM_BORDER, -1, EM_TRADE, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, EM_CLIMATE, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};

    /** Effects while it stands: gdp unemp infl tariff oil def(% GDP) wf(% workforce) ready gN (group) st:AB (state). */
    public static final String[] AD_FX = {
            "", "", "oil=-1", "", "",
            "g12=-1.5", "g12=-3 g10=-1", "tariff=12 gdp=-0.12 infl=0.25 def=-0.4", "wf=-3 ready=-2 def=-0.02", "ready=-4", "wf=-4 ready=-2 st:VA=-1 st:MD=-1.5",
            "oil=-2 gdp=0.03", "g10=-1 g27=-1", "gdp=0.03", "g0=-1", "g12=-2", "", "wf=-10 ready=-9 def=-0.05 st:VA=-2 st:MD=-3", "",
            "def=0.15 g0=3 g28=4", "g0=1", "oil=2 gdp=-0.02", "g12=3", "def=0.01 g24=2", "g4=1 g3=2", "g10=1.5", "gdp=-0.01", "g24=2",
            "def=-0.03 g1=2", "", "infl=-0.03", "oil=-5", "gdp=0.04 infl=0.02", "tariff=-3 infl=-0.05",
            "gdp=-0.03 infl=0.03", "g24=1.5", "g0=1", "gdp=0.01 g24=1", "g1=1.5 def=0.01", "infl=-0.02", "g0=1", "gdp=0.02 g22=1", "gdp=0.02", "def=-0.02",
            "gdp=0.04", "g12=-1.5", "gdp=0.02", "g1=1", "gdp=-0.01"};

    /** Moves a voter group's approval by about this many points (the cell model works in log-odds). */
    public static void groupBump(Game g, int gi, double points) { if (gi >= 0 && gi < NG) g.groupAdj[gi] += points * 0.04; }

    public static int actionIdx(String id) { for (int i = 0; i < AD_ID.length; i++) if (AD_ID[i].equals(id)) return i; return -1; }

    public static final int A_LIVE = 0, A_PENDING = 1, A_BLOCKED = 2, A_STRUCK = 3, A_REVOKED = 4, A_COMMENT = 5, A_UNDONE = 6;

    public static final String[] A_STATUS = {"In effect", "Proposed", "Blocked by a court", "Struck down", "Revoked", "Open for comment", "Overturned by Congress"};
}
