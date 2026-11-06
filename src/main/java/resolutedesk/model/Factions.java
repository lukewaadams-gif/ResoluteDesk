package resolutedesk.model;

import static resolutedesk.model.Parties.*;

/** The factions within each party and where they stand. */
public final class Factions {
    private Factions() {
    }

    public static final int F_DSOC = 0, F_PROG = 1, F_LIB = 2, F_ABUN = 3, F_MOD = 4, F_OUTD = 5, F_MAGA = 6, F_NATCON = 7,
            F_TECHR = 8, F_HAWK = 9, F_GOV = 10, F_RESTR = 11, F_MAHA = 12, F_GREEN = 13, F_LIBT = 14, F_FWD = 15, F_AMP = 16, NF = 17;

    public static final Faction[] FACTIONS = {
            new Faction("Democratic Socialist", DEM, -0.90, -0.85,
                    new double[]{-0.30, -0.70, -0.95, -0.90, 0.10, -0.80, -0.60, -0.70, -1.0, -1.0, -0.90, -0.70, -0.95, -0.90, -0.80, -0.95, -0.30, -0.95},
                    "Medicare for All, taxing billionaires, a Green New Deal and confronting 'the oligarchy'. Heir to the Sanders movement.",
                    "Electrifies Gen Z, renters and progressive activists; huge small-dollar and volunteer engine. Costs ground with older moderates, suburban independents and some Hispanic men."),
            new Faction("Progressive Populist", DEM, -0.65, -0.60,
                    new double[]{0.00, -0.60, -0.80, -0.75, 0.15, -0.50, -0.30, -0.50, -0.95, -0.75, -0.80, -0.35, -0.80, -0.90, -0.75, -0.75, -0.60, -0.75},
                    "Economic fighter: anti-corporate, pro-union, tough on monopolies and corruption, eager to take the fight to Trump.",
                    "Strong with union households, young voters and the activist base; plays well with non-college voters on economics, less so on culture."),
            new Faction("Mainstream Liberal", DEM, -0.50, -0.60,
                    new double[]{0.10, -0.30, -0.65, -0.55, -0.20, -0.10, -0.10, -0.45, -0.95, -0.60, -0.65, -0.20, -0.75, -0.85, -0.75, -0.60, -0.80, -0.65},
                    "The Democratic center of gravity since 2020: the Biden-Harris agenda, abortion rights, democracy and the ACA.",
                    "Solid with Black voters, college-educated women and older Democrats. Struggles to win back non-college and young men."),
            new Faction("Abundance Liberal", DEM, -0.30, -0.55,
                    new double[]{0.25, 0.15, -0.45, -0.30, -0.45, -0.10, 0.60, -0.35, -0.90, -0.45, -0.50, 0.00, -0.65, -0.80, -0.60, -0.40, -0.80, -0.45},
                    "Build more housing, energy and infrastructure; cut red tape; competent, pro-growth liberalism.",
                    "Appeals to college graduates, young renters and suburban independents. Housing and permitting messages travel; union voters are wary of the tech tilt."),
            new Faction("Pragmatic Moderate", DEM, -0.25, -0.20,
                    new double[]{0.35, 0.00, -0.50, -0.40, -0.05, 0.25, 0.25, -0.10, -0.75, -0.45, -0.60, 0.20, -0.40, -0.70, -0.40, -0.30, -0.70, -0.20},
                    "Win-the-middle Democrat: kitchen-table economics, faith and patriotism, distance from the left on crime, borders and culture.",
                    "Best with independents, rural and exurban whites, Catholics and swing-state suburbs. Activists and Gen Z are cooler; base turnout can sag."),
            new Faction("Outsider Centrist", DEM, -0.15, -0.15,
                    new double[]{0.30, 0.20, -0.40, -0.20, 0.00, 0.00, 0.30, 0.00, -0.70, -0.50, -0.50, 0.25, -0.30, -0.60, -0.30, -0.30, -0.50, 0.00},
                    "Business or celebrity outsider: anti-politician, plain-spoken, ideologically flexible.",
                    "Draws low-propensity and disaffected voters, especially young men; lacks the institutional support that wins primaries."),
            new Faction("MAGA Populist", REP, 0.30, 0.90,
                    new double[]{0.80, 0.40, 0.10, 0.20, 0.90, 0.60, 0.40, 1.0, 0.50, 0.50, -0.30, 0.90, 0.90, 0.80, 0.90, 0.80, 0.70, 0.95},
                    "Trump's movement in pure form: tariffs, mass deportation, America First, war on the 'deep state'.",
                    "Commands the Republican primary electorate and non-college whites; mobilises low-propensity men. Repels college suburbs and many independents."),
            new Faction("National Conservative", REP, 0.10, 0.85,
                    new double[]{0.85, 0.20, 0.00, 0.00, 0.85, 0.50, 0.20, 0.95, 0.75, 0.30, -0.40, 0.85, 0.75, 0.70, 0.85, 0.70, 0.60, 0.95},
                    "The 'New Right': pro-worker, anti-Big Tech and anti-China, socially traditional - MAGA with a policy agenda.",
                    "Strong with the MAGA base and working-class whites, can reach union households; weak with college women and suburban moderates."),
            new Faction("Tech Right", REP, 0.75, 0.60,
                    new double[]{0.70, 0.95, 0.60, 0.60, 0.50, 0.60, 0.80, 0.70, 0.40, 0.70, 0.30, 0.80, 0.80, 0.60, 0.80, 0.90, 0.50, 0.80},
                    "Accelerationist: AI and energy dominance, deregulation, merit, school choice - Silicon Valley's turn right.",
                    "Excites young men, entrepreneurs and podcast audiences; data-center and AI-job anxieties make rural voters and seniors uneasy."),
            new Faction("Reagan Conservative", REP, 0.70, 0.70,
                    new double[]{0.95, 0.50, 0.60, 0.50, 0.20, 0.95, 0.60, 0.70, 0.80, 0.60, 0.20, 0.80, 0.70, 0.30, 0.85, 0.85, -0.60, 0.85},
                    "Peace through strength, free markets, the traditional conservative coalition; hawkish on China, Russia and Iran.",
                    "Strong with older Republicans, evangelicals, veterans, Cuban Americans and donors; MAGA activists distrust 'neocons'."),
            new Faction("Governing Conservative", REP, 0.60, 0.50,
                    new double[]{0.70, 0.50, 0.50, 0.40, 0.30, 0.70, 0.60, 0.70, 0.60, 0.50, 0.00, 0.80, 0.60, 0.20, 0.80, 0.85, -0.20, 0.80},
                    "Results-first Republican governor: tax cuts, schools, order and competence, with less drama.",
                    "Wins back college suburbs and independents; the base finds it bloodless."),
            new Faction("Restrainer / Paleo", REP, 0.15, 0.95,
                    new double[]{0.40, -0.30, 0.00, 0.00, 0.90, -0.20, 0.20, 1.0, 0.80, 0.30, -0.50, 0.85, 0.80, 0.50, 0.95, 0.80, 0.95, 0.95},
                    "No more foreign wars, skeptical of aid to Israel and Ukraine, nationalist on trade and immigration, anti-establishment to the core.",
                    "Fires up online populists and young men; alarms hawks, Jewish and evangelical voters and party donors."),
            new Faction("MAHA Populist", REP, -0.05, 0.35,
                    new double[]{0.30, -0.30, -0.20, -0.10, 0.40, 0.60, 0.00, 0.60, -0.10, 0.00, -0.50, 0.40, 0.20, 0.20, 0.30, 0.30, 0.80, 0.60},
                    "Make America Healthy Again: anti-pharma, anti-processed food, distrust of institutions; crosses party lines.",
                    "Draws 'crunchy' independents, health-conscious women and conspiracy-curious young voters; scientists and doctors recoil."),
            new Faction("Green", GRN, -0.95, -0.90,
                    new double[]{-0.60, -0.80, -1.0, -1.0, -0.10, -1.0, -0.80, -0.90, -1.0, -1.0, -1.0, -0.90, -1.0, -0.80, -0.70, -1.0, 0.40, -1.0},
                    "Eco-socialist and anti-war: end arms to Israel, Medicare for All, a rapid climate transition.",
                    "Pulls protest votes from young progressives and Arab and Muslim Americans; a spoiler in close states."),
            new Faction("Libertarian", LBT, 0.90, -0.40,
                    new double[]{0.00, 0.90, 1.0, 0.90, -0.90, -0.30, 1.0, -0.30, 0.00, 0.90, 0.70, -0.40, 0.50, -0.50, 1.0, 0.90, 0.70, -0.20},
                    "Minimal government at home and abroad: free trade, free markets, gun rights and civil liberties.",
                    "Draws disaffected Republicans and young men; usually 1-2% but decisive in close races."),
            new Faction("Forward / Reform", FWD, -0.20, -0.30,
                    new double[]{0.20, -0.20, -0.40, -0.70, -0.20, 0.00, 0.40, -0.10, -0.50, -0.40, -0.40, 0.00, -0.30, -0.70, -0.30, -0.20, -0.40, -0.20},
                    "Fix the system: ranked-choice voting, open primaries, a universal basic income for the AI age.",
                    "Appeals to tech-minded independents, young voters and the politically homeless."),
            new Faction("America Party Centrist", AMP, 0.35, 0.10,
                    new double[]{0.50, 0.70, 0.40, 0.30, -0.20, 0.30, 0.80, 0.40, -0.30, 0.20, 0.00, 0.50, 0.00, -0.40, 0.30, 0.40, 0.20, 0.30},
                    "Billionaire-backed revolt against both parties: cut the deficit, build, AI dominance, end the uniparty.",
                    "Pulls from both sides - independents, young men and moderate suburbanites; scrambles the map."),
    };

    public static final double[] GENERIC_D = FACTIONS[F_LIB].pos;

    public static final double[] GENERIC_R = FACTIONS[F_MAGA].pos;
}
