package resolutedesk.data;

import static resolutedesk.data.HistoricalFigures.*;
import static resolutedesk.data.StateData.*;
import static resolutedesk.model.Attributes.*;
import static resolutedesk.model.Factions.*;
import static resolutedesk.model.Parties.*;

import java.util.ArrayList;
import java.util.List;
import resolutedesk.model.Cand;
import resolutedesk.model.Look;

/** The roster of real and archetypal politicians who can be president or vice president. */
public final class Candidates {
    private Candidates() {
    }

    public static final List<Cand> ROSTER = new ArrayList<Cand>();

    /** Compact roster entry. attrs: CHA POD SPE DEB INT DIS RET SMD BIG SELF VOL VIR AUT STA SCA EXE FPC XOV EST HOM BASE */
    public static Cand cand(String id, String first, String last, int party, String role, String home, int born, int faction,
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

    public static void bio(Cand c, String bio, String pros, String cons) { c.bio = bio; c.pros = pros; c.cons = cons; }

    /** Record items: "issue:credibility:text" with issue -1 for general. */
    public static void rec(Cand c, String... items) {
        c.record = new String[items.length]; c.recIssue = new int[items.length]; c.recCred = new double[items.length];
        for (int i = 0; i < items.length; i++) {
            String[] t = items[i].split(":", 3);
            c.recIssue[i] = Integer.parseInt(t[0]); c.recCred[i] = Double.parseDouble(t[1]); c.record[i] = t[2];
        }
    }

    /** Group appeal: "group:value" pairs (logit shifts among that group). */
    public static void appeal(Cand c, String s) {
        String[] t = s.trim().split("\\s+");
        c.apG = new int[t.length]; c.apV = new double[t.length];
        for (int i = 0; i < t.length; i++) { String[] p = t[i].split(":"); c.apG[i] = Integer.parseInt(p[0]); c.apV[i] = Double.parseDouble(p[1]); }
    }

    public static void pos(Cand c, double econ, double cult, String issues) {
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

    public static Cand candById(String id) { if (id == null) return null; for (Cand c : ROSTER) if (c.id.equals(id)) return c; for (Cand c : HIST) if (c.id.equals(id)) return c; return null; }

    public static List<Cand> roster(int party) {
        List<Cand> l = new ArrayList<Cand>();
        for (Cand c : ROSTER) if (c.party == party) l.add(c);
        return l;
    }

    // ---- sandbox archetypes: base ratings and a point pool ----------------------------------------------------
    public static final String[] ARCHETYPE = {"Governor", "Senator", "House member", "Big-city mayor", "Cabinet secretary", "General / admiral",
            "Billionaire CEO", "Celebrity", "Activist / organiser", "Podcaster / media star", "Faith leader"};

    public static final String[] ARCH_BASE = {
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

    public static final int SANDBOX_POINTS = 120;

    static {
        Cand c = cand("trump", "Donald", "Trump", REP, "45th President, elected again in 2024", "FL", 1946, F_MAGA,
                "88 85 70 72 50 30 92 90 92 80 85 98 90 75 95 70 60 50 70 60 98", 1.0, 0, 0, 0, 0, "M 1 5 3 0.3 0 0 1.1 0.62 0 1 0.85 0.45");
        bio(c, "Businessman and television host; president 2017-21, impeached twice, convicted on 34 felony counts in New York in 2024, then elected again over Kamala Harris with 312 electoral votes and a narrow popular-vote win.",
                "Total command of his party, a direct line to his voters, and a gift for setting the day's agenda.",
                "Deeply polarising; low discipline; legal and ethical controversies follow him.");
        c.home = stateIdx(c.homeAb);
        c.spouse = "Melania Trump";
    }
}
