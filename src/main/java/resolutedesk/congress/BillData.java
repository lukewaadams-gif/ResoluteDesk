package resolutedesk.congress;

import static resolutedesk.core.Fmt.*;
import static resolutedesk.history.EraRules.*;
import static resolutedesk.model.Parties.*;
import static resolutedesk.opinion.Issues.*;

import resolutedesk.model.Game;

/** The bills a president can send to Congress, modern and historical. */
public final class BillData {
    private BillData() {
    }

    /** The modern agenda. Cost is $ billions a year at full size (negative raises revenue). */
    public static BillDef[] BILLS;

    public static final BillDef[] BILLS_MODERN = {
            new BillDef("lower_costs", "Lower Costs Act", "Caps insulin at $35 a month and child care at 7% of income, and bans junk fees.", PI_PRICES, DEM, -0.4, -0.1, 80, true, 66, 0.05, 0, -0.15, "Families with young children save about $# a year on child care.", 3100, 0),
            new BillDef("public_option", "A public option", "A government health plan on the ACA marketplaces in every state.", PI_HEALTH, DEM, -0.6, -0.1, 60, true, 58, 0, 0, -0.05, "# million more Americans have health insurance.", 4.6, 1),
            new BillDef("restore_roe", "Restore Roe", "The Women's Health Protection Act: abortion rights written into federal law.", PI_RIGHTS, DEM, 0, -0.8, 0, false, 60, 0, 0, 0, "Abortion is legal through viability in every state; # states had banned it.", 13, 2),
            new BillDef("imm_reform", "Immigration reform", "Border security money plus a path to citizenship for Dreamers and long-time workers.", PI_IMMIG, DEM, -0.1, -0.3, 10, false, 61, 0.1, 0, 0, "# million Dreamers and long-time residents are on a path to citizenship.", 2.4, 1),
            new BillDef("billionaire_tax", "Billionaire minimum tax", "A 25% minimum tax on households worth more than $100 million.", PI_ECON, DEM, -0.7, -0.1, -50, true, 63, -0.03, 0, -0.05, "The minimum tax raises about $# billion a year.", 50, 0),
            new BillDef("housing", "Build 3 million homes", "Zoning incentives, a builders' tax credit and federal land for housing.", PI_PRICES, DEM, -0.2, 0, 40, true, 66, 0.12, -0.05, -0.08, "# million new homes are under construction.", 1.1, 1),
            new BillDef("ai_safety", "AI safety standards", "Safety testing for the most powerful AI models, and data centers pay their own power bills.", PI_TECH, DEM, -0.3, -0.1, 2, false, 63, -0.02, 0, 0, "# frontier AI labs submit their models for federal safety testing.", 9, 0),
            new BillDef("ss_protect", "Protect Social Security", "Lift the payroll-tax cap and raise benefits for the oldest retirees.", PI_ECON, DEM, -0.5, -0.1, -15, false, 68, 0, 0, 0, "Social Security's trust fund is solvent for # more years.", 30, 0),
            new BillDef("gun_safety", "Gun safety", "Universal background checks, red-flag laws and a minimum age of 21 for assault weapons.", PI_CRIME, DEM, -0.1, -0.6, 1, false, 58, 0, 0, 0, "Background checks cover # million more gun sales a year.", 6, 1),
            new BillDef("clean_energy", "Clean energy buildout", "New transmission lines, permitting reform and clean-power tax credits.", PI_ENERGY, DEM, -0.4, -0.2, 50, true, 55, 0.15, -0.05, 0.02, "# gigawatts of new clean power are on the grid.", 120, 0),
            new BillDef("voting_rights", "Voting rights", "The Freedom to Vote Act: national standards for early voting and mail ballots.", PI_RIGHTS, DEM, 0, -0.6, 2, false, 60, 0, 0, 0, "# states have had to expand early voting.", 19, 2),
            new BillDef("paid_leave", "Paid leave", "Twelve weeks of paid family and medical leave for every worker.", PI_JOBS, DEM, -0.6, -0.1, 70, true, 70, 0.02, 0, 0.03, "# million workers took paid leave in its first year.", 11, 1),
            new BillDef("child_credit", "Expanded child tax credit", "A $3,600 credit per young child, paid monthly.", PI_PRICES, DEM, -0.5, -0.1, 100, true, 62, 0.08, 0, 0.05, "Child poverty is down # percent.", 40, 0),
            new BillDef("min_wage", "A $17 minimum wage", "Raise the federal minimum wage to $17 over five years.", PI_JOBS, DEM, -0.6, 0, 0, false, 62, -0.02, 0.05, 0.05, "# million workers got a raise.", 22, 1),
            new BillDef("tax_cuts", "Make the tax cuts bigger", "Extend the 2025 tax cuts and cut the corporate rate to 15%.", PI_ECON, REP, 0.7, 0.2, 150, true, 45, 0.18, -0.05, 0.12, "The typical family pays about $# less in federal income tax.", 1500, 0),
            new BillDef("border_wall", "Finish the wall", "Money for the border wall, more ICE agents and faster deportations.", PI_IMMIG, REP, 0.2, 0.7, 25, true, 48, -0.05, 0, 0.05, "Border crossings are down #% from when you took office.", 40, 0),
            new BillDef("health_freedom", "Health freedom act", "Replace ACA subsidies with health savings accounts and state block grants.", PI_HEALTH, REP, 0.7, 0.2, -40, true, 39, 0.03, 0, 0, "# million fewer Americans have health insurance.", 5.2, 1),
            new BillDef("abortion_limit", "A 15-week abortion limit", "A national limit on abortion after 15 weeks, with exceptions.", PI_RIGHTS, REP, 0.1, 0.8, 0, false, 41, 0, 0, 0, "Abortion is limited after 15 weeks nationwide, including in # states that had allowed it.", 24, 2),
            new BillDef("tariffs", "Reciprocal tariffs", "Match every country's tariffs, with a 60% floor on Chinese goods.", PI_TRADE, REP, 0.3, 0.3, -150, true, 42, -0.15, 0.08, 0.35, "Tariffs bring in about $# billion a year, and prices on imported goods are up.", 150, 0),
            new BillDef("energy_dom", "Energy dominance", "Open federal lands and waters to drilling and speed up permits for pipelines.", PI_ENERGY, REP, 0.6, 0.3, -5, true, 53, 0.1, -0.03, -0.12, "US oil and gas output is up #%.", 9, 0),
            new BillDef("balance_budget", "Balance the budget", "Cap spending and cut federal agencies by a tenth.", PI_ECON, REP, 0.8, 0.2, -250, true, 51, -0.12, 0.08, -0.15, "Federal spending is $# billion a year lower than planned.", 250, 0),
            new BillDef("school_choice", "School choice", "A federal tax credit for private-school and home-school scholarships.", PI_EDU, REP, 0.5, 0.5, 10, true, 52, 0, 0, 0, "# million children use the new scholarships.", 1.8, 1),
            new BillDef("police", "Back the blue", "Money for 100,000 police officers and tougher sentences for fentanyl dealers.", PI_CRIME, REP, 0.3, 0.5, 8, false, 57, 0, 0, 0, "# thousand new police officers are on the street.", 60, 0),
            new BillDef("save_act", "The SAVE Act", "Proof of citizenship to register and photo ID to vote.", PI_RIGHTS, REP, 0.1, 0.7, 1, false, 63, 0, 0, 0, "Every state now requires photo ID to vote; # did not before.", 15, 2),
            new BillDef("no_tax_tips", "No tax on tips and overtime", "Make the tax break on tips and overtime permanent and wider.", PI_JOBS, REP, 0.3, 0.1, 40, true, 66, 0.05, 0, 0.05, "Workers keep about $# billion a year in tips and overtime pay.", 40, 0),
            new BillDef("infrastructure", "Infrastructure renewal", "Roads, bridges, water pipes, broadband and the power grid.", PI_JOBS, -1, -0.1, 0, 60, false, 70, 0.15, -0.08, 0.04, "# thousand bridges and roads are being rebuilt.", 22, 0),
            new BillDef("chips2", "Chips and China Act", "Chips, shipbuilding and export controls to compete with China.", PI_TRADE, -1, 0, 0.1, 15, false, 64, 0.1, -0.03, 0.03, "# new chip and shipbuilding plants are under way.", 14, 0),
            new BillDef("permitting", "Permitting reform", "Faster environmental reviews for power lines, pipelines and mines.", PI_ENERGY, -1, 0.2, 0, 0, false, 58, 0.06, -0.02, -0.02, "Permits for big projects now take # years less.", 2, 1),
            new BillDef("kids_online", "Kids Online Safety Act", "Platforms must protect minors from harmful content and addictive design.", PI_TECH, -1, 0, 0.1, 0, false, 72, 0, 0, 0, "# million teenagers are on safer default settings.", 18, 0),
            new BillDef("farm_bill", "The farm bill", "Five years of crop insurance, conservation and food assistance.", PI_ECON, -1, 0.1, 0.1, 5, false, 60, 0.02, 0, 0, "# million farms are covered by crop insurance.", 1.2, 1),
            new BillDef("stock_ban", "Ban stock trading by Congress", "Members of Congress and their spouses can't trade individual stocks.", PI_ETHICS, -1, 0, 0, 0, false, 80, 0, 0, 0, "# members of Congress have sold their individual stocks.", 535, 0),
            new BillDef("crypto", "Crypto market rules", "Clear rules for stablecoins and digital-asset exchanges.", PI_TECH, -1, 0.3, 0, 0, false, 50, 0.02, 0, 0, "Americans hold about $# billion in regulated stablecoins.", 400, 0),
            new BillDef("ss_fix", "A bipartisan Social Security fix", "A commission's mix of a higher payroll-tax cap and a slower rise in benefits for the well-off.", PI_ECON, -1, 0, 0, -20, false, 55, 0, 0, 0, "Social Security's trust fund is solvent for # more years.", 40, 0)};

    public static BillDef billDef(String id) { for (BillDef b : BILLS) if (b.id.equals(id)) return b; return null; }

    //  Bills from the New Deal to the pandemic, each available in the years it was on the table.
    //  Costs are scaled to today's economy and shrink to the era's dollars when you play an earlier year.
    /** id|name|description|issue|lean (D, R or -)|econ|cult|cost|public support|growth|jobs|prices|impact|base|format|from|to */
    public static final String[] HBILL_SPEC = {
            // the New Deal
            "bank_act|Emergency Banking Act|Reopen sound banks under Treasury licenses, close the rest, and let the Fed back them with new currency.|0|-|-0.2|0|3|82|0.4|-0.3|0.2|# thousand banks reopened within a month.|12|2|1933|1933",
            "ccc|Civilian Conservation Corps|Put 250,000 young men to work planting forests, building parks and fighting erosion.|2|D|-0.5|0|60|75|0.1|-0.3|0.05|# million young men have served in the CCC.|3|1|1933|1941",
            "aaa|Agricultural Adjustment Act|Pay farmers to plant less, to lift collapsed crop prices.|0|D|-0.6|0.1|40|58|0.15|-0.1|0.25|Farm income is up #% since 1932.|50|2|1933|1938",
            "nira|National Industrial Recovery Act|Industry codes for wages, hours and prices, the right to organize, and $3.3 billion for public works.|2|D|-0.6|0|120|60|0.3|-0.4|0.3|# thousand public works projects are under way.|34|2|1933|1935",
            "tva|Tennessee Valley Authority|A federal corporation to dam the Tennessee River, make cheap power and lift one of the poorest regions in the country.|9|D|-0.6|0|20|64|0.08|-0.05|0|# million people now get TVA power.|2|1|1933|1936",
            "glass_steagall|The Banking Act (Glass-Steagall)|Separate commercial from investment banking and insure bank deposits.|0|-|-0.4|0|2|72|0.1|0|0|Deposit insurance covers # million accounts; bank runs have stopped.|50|2|1933|1935",
            "securities|Securities Exchange Act|A commission to police Wall Street, require honest disclosure and ban stock manipulation.|13|D|-0.4|0|1|70|0.05|0|0|The new Securities and Exchange Commission has brought # cases.|400|2|1933|1936",
            "wpa|Emergency Relief Appropriation (the WPA)|Hire millions of the jobless to build roads, schools and airports, and to write, paint and perform.|2|D|-0.7|-0.1|250|62|0.5|-1.2|0.3|# million Americans have worked for the WPA.|8|1|1934|1941",
            "social_security|Social Security Act|Old-age pensions paid for by payroll taxes, unemployment insurance and aid to poor children.|0|D|-0.7|-0.1|100|77|0.05|-0.1|0.05|# million retirees draw a Social Security check.|20|1|1934|1940",
            "wagner|National Labor Relations Act|Guarantee the right to join a union and bargain collectively, with a board to enforce it.|2|D|-0.7|0|1|60|0|-0.05|0.1|Union membership has grown by # million.|6|1|1935|1938",
            "wealth_tax|Revenue Act (the wealth tax)|Raise the top income tax rate to 79% and tax big estates and corporations more.|0|D|-0.8|0|-40|55|-0.05|0.02|0|The top income tax rate is #%.|79|2|1935|1938",
            "neutrality|Neutrality Act|Ban arms sales and loans to nations at war.|6|-|0.2|0.3|0|65|0|0|0|The United States stayed out of # foreign wars.|2|2|1935|1939",
            "court_pack|Judicial Procedures Reform Act|Add a justice for every sitting justice over 70, up to six, to the Supreme Court.|13|D|-0.3|0|0|39|0|0|0|The Supreme Court has # justices.|15|2|1937|1938",
            "flsa|Fair Labor Standards Act|A 25-cent minimum wage, a 44-hour week falling to 40, and a ban on child labor.|2|D|-0.6|-0.1|2|69|-0.02|0.03|0.06|# thousand children no longer work in factories.|500|2|1937|1940",
            "housing37|Housing Act (Wagner-Steagall)|Federal money for local public housing and slum clearance.|1|D|-0.6|0|20|60|0.05|-0.05|0|# thousand public housing units have been built.|120|2|1937|1941",
            // war
            "cash_carry|Neutrality Act of 1939 (cash and carry)|Let Britain and France buy American arms if they pay cash and carry them away.|6|D|0|-0.1|0|57|0.1|-0.05|0.05|The Allies have bought $# billion in American arms.|2|1|1939|1940",
            "draft40|Selective Training and Service Act|The first peacetime draft: every man 21 to 35 registers, and 900,000 are trained a year.|7|-|0.2|0.3|30|60|0.05|-0.3|0.05|# million men are in uniform.|1.5|1|1940|1941",
            "lend_lease|Lend-Lease Act|Lend or lease arms to any nation whose defense is vital to the defense of the United States.|6|D|0|0|80|55|0.2|-0.3|0.1|$# billion in arms and supplies have gone to the Allies.|50|2|1941|1941",
            "price_control|Emergency Price Control Act|An Office of Price Administration to freeze prices and ration scarce goods.|1|D|-0.5|0|5|62|-0.05|0|-0.6|Price controls cover #% of what Americans buy.|90|2|1942|1946",
            "revenue42|Revenue Act of 1942 (the Victory Tax)|Bring tens of millions of new taxpayers into the income tax and withhold it from paychecks.|0|D|-0.3|0|-150|52|-0.1|0.05|-0.2|# million Americans now pay income tax.|50|2|1942|1944",
            "gi_bill|Servicemen's Readjustment Act (the GI Bill)|College tuition, cheap mortgages and jobless pay for returning veterans.|12|-|-0.4|0|60|82|0.15|-0.1|0.05|# million veterans have gone to college on the GI Bill.|2.2|1|1944|1948",
            "bretton|Bretton Woods Agreements Act|Join the International Monetary Fund and the World Bank.|8|-|0|-0.1|3|60|0.05|0|0|# nations are in the new monetary system.|44|2|1945|1946",
            "un_charter|The United Nations Charter|Ratify the charter of a new world organization with a Security Council.|6|-|0|-0.2|1|75|0|0|0|The United Nations has # members.|51|2|1945|1946",
            // after the war
            "employment_act|Employment Act|Commit Washington to maximum employment, with a Council of Economic Advisers.|2|D|-0.5|0|5|64|0.05|-0.1|0|Unemployment has averaged #% since the act.|4|1|1945|1946",
            "taft_hartley|Taft-Hartley Act|Ban the closed shop and secondary boycotts, and let states pass right-to-work laws.|2|R|0.6|0.2|0|52|0.02|0|-0.02|# states have passed right-to-work laws.|12|2|1946|1952",
            "marshall|Economic Cooperation Act (the Marshall Plan)|$13 billion to rebuild Western Europe and keep it out of Soviet hands.|6|-|-0.1|-0.1|100|56|0.1|-0.05|0.05|$# billion has gone to rebuild Western Europe.|13|2|1947|1950",
            "nat_security|National Security Act|Unify the armed services under a Department of Defense, and create the CIA and the National Security Council.|7|-|0.1|0.1|5|66|0|0|0|The new Department of Defense commands # million troops.|1.5|1|1947|1948",
            "nhi|National health insurance|Health insurance for every American, paid for by payroll taxes.|3|D|-0.8|-0.1|120|50|0|0|0.05|# million Americans have national health insurance.|150|2|1945|1952",
            "civil_rights48|The civil rights program|Outlaw lynching and the poll tax, and create a permanent fair employment commission.|10|D|-0.1|-0.7|1|52|0|0|0|The poll tax is gone in # states.|5|2|1948|1956",
            "housing49|Housing Act of 1949|810,000 public housing units and urban renewal money for the cities.|1|D|-0.5|0|25|62|0.05|-0.05|0|# thousand public housing units have been built.|810|2|1949|1953",
            "nato_treaty|The North Atlantic Treaty|An armed attack on one member of the alliance is an attack on them all.|6|-|0.1|0|10|70|0|0|0|NATO has # members.|12|2|1949|1950",
            // Eisenhower
            "highways|Federal-Aid Highway Act|41,000 miles of interstate highways, 90% paid for by Washington.|2|-|0|0|80|75|0.2|-0.15|0.05|# thousand miles of interstate are open.|41|2|1954|1960",
            "ndea|NASA and the National Defense Education Act|Science scholarships, student loans and a civilian space agency.|12|-|-0.1|-0.1|15|72|0.05|0|0|# thousand students have federal loans for college.|600|2|1957|1960",
            "civil_rights57|Civil Rights Act of 1957|A civil rights division at the Justice Department and federal suits to protect the right to vote.|10|-|0|-0.5|1|58|0|0|0|Federal suits have registered # thousand Black voters in the South.|100|2|1956|1960",
            // the New Frontier and the Great Society
            "space|The Apollo program|Land a man on the Moon and return him safely to Earth before the decade is out.|11|-|-0.1|-0.1|60|60|0.05|-0.05|0.02|NASA employs # thousand people.|400|2|1961|1966",
            "kennedy_tax|Revenue Act of 1964|Cut the top tax rate from 91% to 70%, and rates for everyone else.|0|D|0.1|0|120|58|0.25|-0.2|0.1|The typical family pays $# less in tax this year.|300|0|1961|1964",
            "civil_rights64|Civil Rights Act|Outlaw segregation in public places and discrimination in hiring.|10|D|-0.1|-0.85|2|61|0.02|0|0|Segregation is illegal in the # states that once required it.|17|2|1962|1966",
            "medicare|Medicare and Medicaid|Health insurance for everyone over 65 through Social Security, and for the poor.|3|D|-0.6|-0.1|90|63|0.02|-0.02|0.05|# million Americans over 65 have health insurance.|19|1|1961|1966",
            "poverty|Economic Opportunity Act|The war on poverty: the Job Corps, Head Start, community action and VISTA.|2|D|-0.6|-0.3|30|60|0.03|-0.1|0|# million children have been through Head Start.|2|1|1963|1967",
            "esea|Elementary and Secondary Education Act|Federal money for schools in poor districts.|12|D|-0.5|-0.2|25|70|0.02|0|0|# million poor children get extra help in school.|7|1|1964|1968",
            "voting_rights65|Voting Rights Act|Federal examiners to register voters, and an end to literacy tests in the South.|10|D|0|-0.85|1|76|0|0|0|# thousand Black voters have been added to the rolls in the South.|250|2|1964|1968",
            "immigration65|Immigration and Nationality Act|End the national-origin quotas; favor family ties and skills.|4|D|0|-0.5|0|50|0.02|0|0|# thousand immigrants arrived last year from countries the quotas shut out.|200|2|1964|1968",
            "fair_housing|Fair Housing Act|Ban discrimination in the sale and rental of housing.|10|D|0|-0.7|1|52|0|0|0|# housing discrimination complaints have been filed.|3000|0|1966|1969",
            // Nixon and Ford
            "clean_air70|Clean Air Act|National air-quality standards and the first limits on car exhaust.|9|-|-0.2|-0.4|5|75|-0.02|0|0.02|Lead in the air is down #%.|50|2|1969|1977",
            "clean_water|Clean Water Act|Federal limits on what can be dumped into rivers and lakes, and money for sewage plants.|9|-|-0.2|-0.3|18|72|0|0|0|#% of rivers are now safe for fishing.|60|2|1971|1977",
            "revenue_sharing|General Revenue Sharing|Send $30 billion to states and cities with few strings attached.|0|R|0.2|0|30|58|0.05|0|0.02|$# billion has gone back to states and cities.|30|2|1969|1976",
            "fap|Family Assistance Plan|A guaranteed minimum income for every family with children.|2|R|-0.3|0|40|50|0|0|0.02|# million families get a federal income floor.|4|1|1969|1972",
            "osha|Occupational Safety and Health Act|Federal workplace safety standards and inspectors to enforce them.|2|-|-0.3|0|2|68|-0.01|0|0|Workplace deaths are down #%.|30|2|1969|1972",
            "wage_controls|Economic Stabilization Act|A 90-day freeze on wages and prices, then a pay board and a price commission.|1|-|-0.2|0|0|68|-0.1|0|-0.8|Inflation was held to #% under the controls.|3|1|1969|1974",
            "title_ix|Education Amendments (Title IX)|Ban sex discrimination in any school that takes federal money.|12|-|0|-0.5|0|66|0|0|0|# million girls now play high-school sports.|3|1|1971|1976",
            "energy75|Energy Policy and Conservation Act|Fuel-economy standards, a strategic petroleum reserve and a 55-mile-an-hour speed limit.|9|-|0|-0.1|5|60|-0.02|0|-0.05|The strategic petroleum reserve holds # million barrels.|500|2|1973|1980",
            "endangered|Endangered Species Act|Protect endangered plants and animals and the places they live, and bar federal projects that would wipe them out.|9|-|-0.1|-0.2|1|74|0|0|0|# species are now protected.|400|2|1973|1976",
            "erisa|Employee Retirement Income Security Act|Federal standards for private pensions, and insurance for workers whose pension plans fail.|2|-|-0.2|0|1|70|0|0|0|# million workers have their pensions insured.|30|2|1973|1976",
            "trade74|Trade Act of 1974|Authority to negotiate lower tariffs, help for workers hurt by imports, and limits on trade with countries that won't let people emigrate.|8|-|0.2|0.1|1|52|0.05|0|-0.02|Trade with the rest of the world has grown #% since the law.|20|2|1973|1976",
            "tax75|Tax Reduction Act of 1975|A rebate on last year's taxes and lower withholding, to pull the economy out of recession.|0|-|-0.1|0|150|66|0.25|-0.1|0.05|The typical family got a $# rebate.|100|0|1974|1977",
            // Carter
            "energy77|National Energy Act|A Department of Energy, conservation credits, a gas-guzzler tax and gradual natural gas deregulation.|9|D|-0.2|-0.1|10|55|0|0|-0.05|Oil imports are down #%.|20|2|1977|1980",
            "deregulation|Airline Deregulation Act|End federal control of airfares and routes.|1|-|0.4|0|0|60|0.08|0|-0.08|Average airfares are down #%.|30|2|1977|1982",
            "panama|The Panama Canal treaties|Hand the canal to Panama by 1999 while keeping the right to defend it.|6|D|0|-0.2|0|42|0|0|0|# Latin American governments praised the treaties.|20|2|1977|1978",
            // Reagan
            "erta|Economic Recovery Tax Act|Cut income tax rates by a quarter over three years, and index them to inflation.|0|R|0.75|0.1|200|55|0.25|-0.15|0.15|The top income tax rate is #%.|50|2|1981|1983",
            "defense81|The defense buildup|A 600-ship Navy, new bombers and new missiles.|7|R|0.5|0.3|120|50|0.1|-0.1|0.1|The Navy is building toward # ships.|600|2|1981|1986",
            "ss83|Social Security Amendments of 1983|Raise payroll taxes, tax the benefits of the well-off and raise the retirement age to 67.|0|-|0|0|-30|60|0|0|0|Social Security's trust fund is solvent for # more years.|75|2|1982|1984",
            "tax_reform86|Tax Reform Act of 1986|Two rates, 15% and 28%, and hundreds of loopholes closed.|0|-|0.3|0|0|58|0.08|0|0|# million low-income Americans no longer owe income tax.|6|1|1985|1987",
            "irca|Immigration Reform and Control Act|Legal status for millions of undocumented immigrants, and fines for employers who hire illegally.|4|-|0|-0.1|3|55|0.03|0|0|# million immigrants have gained legal status.|2.7|1|1985|1987",
            "drugs86|Anti-Drug Abuse Act|Mandatory minimum sentences for drug dealers and more money for treatment.|5|-|0.2|0.5|2|70|0|0|0|# thousand more drug offenders are in federal prison.|30|2|1985|1989",
            // Bush
            "ada|Americans with Disabilities Act|Ban discrimination against people with disabilities at work, on transit and in public places.|10|-|0|-0.4|3|78|0|0|0|# thousand buildings have been made accessible.|400|2|1989|1991",
            "clean_air90|Clean Air Act Amendments|Cap-and-trade for the sulfur that causes acid rain, and tighter smog rules.|9|-|0|-0.3|5|70|-0.02|0|0.02|Sulfur emissions are down #%.|40|2|1989|1991",
            "budget90|Omnibus Budget Reconciliation Act of 1990|Cut spending and raise taxes, with pay-as-you-go rules for new spending.|0|-|0|0|-120|45|-0.08|0.03|-0.05|The deficit is $# billion smaller.|490|2|1990|1990",
            // Clinton
            "budget93|Omnibus Budget Reconciliation Act of 1993|Raise the top tax rate to 39.6% and cut spending to shrink the deficit.|0|D|-0.3|0|-100|50|0.05|0|-0.05|The deficit is $# billion smaller than projected.|500|2|1993|1994",
            "fmla|Family and Medical Leave Act|Twelve weeks of unpaid, job-protected leave for a new child or a serious illness.|2|D|-0.4|-0.1|0|70|0|0|0|# million workers have taken family leave.|35|1|1993|1994",
            "nafta|North American Free Trade Agreement|Phase out tariffs between the United States, Canada and Mexico.|8|-|0.4|0|0|48|0.08|0.02|-0.03|Trade with Mexico and Canada is up #%.|70|2|1993|1994",
            "health_security|Health Security Act|Universal coverage through regional health alliances and a mandate on employers.|3|D|-0.6|-0.1|90|47|0|0|0.03|# million more Americans have health insurance.|37|1|1993|1995",
            "crime94|Violent Crime Control Act|100,000 police officers, an assault-weapons ban and tougher sentences.|5|D|0|-0.1|6|61|0|0|0|# thousand new police officers are on the street.|100|2|1993|1996",
            "welfare96|Welfare reform|End the welfare entitlement: work requirements and time limits for cash aid.|2|-|0.4|0.3|-10|65|0.02|-0.05|0|The welfare rolls are down #%.|50|2|1995|1997",
            "balanced97|Balanced Budget Act|Balance the budget by 2002 with spending caps and Medicare savings, plus health coverage for children.|0|-|0.1|0|-60|62|0.03|0|-0.03|The budget shows a surplus of $# billion.|230|2|1997|2000",
            // Bush
            "bush_tax|Economic Growth and Tax Relief Act|Cut income tax rates across the board and phase out the estate tax.|0|R|0.7|0.1|140|52|0.12|-0.05|0.05|The typical family pays $# less in tax.|1000|0|2001|2003",
            "nclb|No Child Left Behind|Yearly tests in reading and math, and consequences for schools that keep failing.|12|-|0.1|0.1|10|68|0|0|0|# million students are tested every year.|25|1|2001|2002",
            "patriot|USA PATRIOT Act|Wider surveillance and investigative powers against terrorism.|7|-|0.3|0.4|2|70|0|0|0|# terror plots have been disrupted.|12|2|2001|2005",
            "medicare_d|Medicare prescription drug benefit|Drug coverage for seniors through private plans.|3|R|-0.1|0|50|60|0|0|0.02|# million seniors have drug coverage.|40|1|2002|2004",
            "ss_accounts|Personal Social Security accounts|Let younger workers put part of their payroll taxes in private accounts.|0|R|0.7|0.1|30|42|0|0|0|# million workers have personal accounts.|20|1|2005|2006",
            "imm07|Comprehensive immigration reform|Border security, a guest-worker program and a path to citizenship.|4|-|0|-0.2|5|52|0.05|0|0|# million immigrants are on a path to citizenship.|11|1|2005|2008",
            "tarp|Emergency Economic Stabilization Act|$700 billion to buy troubled assets and shore up the banks.|0|-|0.1|0|200|40|0.3|-0.3|0.05|The banks have repaid $# billion.|440|2|2008|2009",
            // Obama
            "recovery_act|American Recovery and Reinvestment Act|$787 billion in tax cuts, aid to states and public works.|2|D|-0.5|0|390|52|0.6|-0.8|0.1|The stimulus saved or created about # million jobs.|2.5|1|2009|2010",
            "aca|Affordable Care Act|Subsidized insurance exchanges, a Medicaid expansion, an individual mandate and an end to pre-existing-condition exclusions.|3|D|-0.6|-0.2|90|48|0|0|0|# million more Americans have health insurance.|20|1|2009|2016",
            "dodd_frank|Dodd-Frank Wall Street Reform|A consumer financial protection bureau, stress tests and limits on banks' trading.|0|D|-0.5|0|1|58|-0.02|0|0|The consumer bureau has returned $# billion to consumers.|12|2|2009|2011",
            "cap_trade|American Clean Energy and Security Act|Cap carbon emissions and let companies trade permits.|9|D|-0.5|-0.3|-10|50|-0.05|0.02|0.05|Carbon emissions are down #% from 2005.|17|2|2009|2010",
            "dream|The DREAM Act|A path to citizenship for immigrants brought here as children.|4|D|0|-0.4|1|58|0.02|0|0|# thousand Dreamers are on a path to citizenship.|800|2|2009|2016",
            "budget_control|Budget Control Act|Ten years of spending caps, and automatic cuts if a supercommittee fails.|0|-|0.3|0|-100|45|-0.08|0.04|-0.03|Spending caps cut $# billion over ten years.|2100|0|2011|2013",
            "imm13|Border Security and Immigration Modernization Act|Billions for the border and a 13-year path to citizenship.|4|-|0|-0.2|5|60|0.05|0|0|# million immigrants are on a path to citizenship.|11|1|2013|2016",
            // Trump
            "tcja|Tax Cuts and Jobs Act|Cut the corporate rate from 35% to 21% and cut individual rates through 2025.|0|R|0.7|0.2|150|37|0.15|-0.05|0.08|The corporate tax rate is #%.|21|2|2017|2018",
            "ahca|American Health Care Act|Repeal the individual mandate, cap Medicaid and replace ACA subsidies with tax credits.|3|R|0.7|0.2|-30|30|0|0|0|# million fewer Americans have health insurance.|20|1|2017|2018",
            "wall17|Border wall funding|$25 billion for a wall on the southern border.|4|R|0.2|0.7|25|40|0|0|0|# miles of new border wall.|450|2|2017|2020",
            "first_step|First Step Act|Shorter sentences for some drug crimes and more job training in federal prisons.|5|-|0|-0.3|1|70|0|0|0|# thousand federal prisoners have been released early.|3|2|2017|2020",
            "usmca|United States-Mexico-Canada Agreement|A rewritten NAFTA with tougher rules for cars and new labor protections.|8|-|0.1|0.1|0|60|0.02|0|0|Trade with Mexico and Canada is up #%.|10|2|2018|2020",
            "cares|CARES Act|$2.2 trillion in checks, loans to small businesses and bigger jobless benefits in the pandemic.|0|-|-0.2|0|900|75|0.8|-1.0|0.1|# million small businesses got loans.|5|1|2020|2020"};

    public static final BillDef[] BILLS_HIST = hbills();

    public static BillDef[] hbills() {
        BillDef[] out = new BillDef[HBILL_SPEC.length];
        for (int i = 0; i < HBILL_SPEC.length; i++) {
            String[] t = HBILL_SPEC[i].split("\\|");
            int lean = t[4].equals("D") ? DEM : t[4].equals("R") ? REP : -1;
            double cost = Double.parseDouble(t[7]);
            int from = Integer.parseInt(t[15]), to = Integer.parseInt(t[16]);
            BillDef d = new BillDef(t[0], t[1], t[2], Integer.parseInt(t[3]), lean, Double.parseDouble(t[5]), Double.parseDouble(t[6]), cost, from >= 1981 && cost != 0,
                    Double.parseDouble(t[8]), Double.parseDouble(t[9]), Double.parseDouble(t[10]), Double.parseDouble(t[11]), t[12], Double.parseDouble(t[13]), Integer.parseInt(t[14]));
            d.from = from; d.to = to;
            out[i] = d;
        }
        return out;
    }

    static {
        BILLS = new BillDef[BILLS_MODERN.length + BILLS_HIST.length];
        for (int i = 0; i < BILLS_MODERN.length; i++) BILLS[i] = BILLS_MODERN[i];
        for (int i = 0; i < BILLS_HIST.length; i++) BILLS[BILLS_MODERN.length + i] = BILLS_HIST[i];
    }

    /** Is a bill on the table in the year the game is in? */
    /** The year each bill really became law (or a treaty was ratified). A presidency that starts after that doesn't get to pass it again. */
    public static final String ENACTED = "bank_act:1933 ccc:1933 aaa:1933 nira:1933 tva:1933 glass_steagall:1933 securities:1934 wpa:1935 social_security:1935 wagner:1935 "
            + "wealth_tax:1935 neutrality:1935 flsa:1938 housing37:1937 cash_carry:1939 draft40:1940 lend_lease:1941 price_control:1942 revenue42:1942 gi_bill:1944 "
            + "bretton:1945 un_charter:1945 employment_act:1946 taft_hartley:1947 marshall:1948 nat_security:1947 housing49:1949 nato_treaty:1949 highways:1956 ndea:1958 "
            + "civil_rights57:1957 space:1961 kennedy_tax:1964 civil_rights64:1964 medicare:1965 poverty:1964 esea:1965 voting_rights65:1965 immigration65:1965 "
            + "fair_housing:1968 clean_air70:1970 clean_water:1972 revenue_sharing:1972 osha:1970 wage_controls:1970 title_ix:1972 energy75:1975 endangered:1973 erisa:1974 trade74:1975 tax75:1975 energy77:1978 "
            + "deregulation:1978 panama:1978 erta:1981 defense81:1981 ss83:1983 tax_reform86:1986 irca:1986 drugs86:1986 ada:1990 clean_air90:1990 budget90:1990 "
            + "budget93:1993 fmla:1993 nafta:1993 crime94:1994 welfare96:1996 balanced97:1997 bush_tax:2001 nclb:2002 patriot:2001 medicare_d:2003 tarp:2008 "
            + "recovery_act:2009 aca:2010 dodd_frank:2010 budget_control:2011 tcja:2017 first_step:2018 usmca:2020 cares:2020";

    public static int enacted(String id) {
        int i = (" " + ENACTED + " ").indexOf(" " + id + ":");
        return i < 0 ? 0 : Integer.parseInt(ENACTED.substring(i + id.length() + 1, i + id.length() + 5));
    }

    public static boolean billOk(Game g, BillDef d) {
        int y = yearNow(g);
        if (y < d.from || y > d.to) return false;
        int en = enacted(d.id);
        return en == 0 || g.era.year <= en;
    }

    /** A law's effect, written out: the impact line with its number. */
    public static String impactOf(Bill b) {
        BillDef d = b.def();
        if (d == null || d.impact == null) return null;
        double v = d.base * Math.min(1.3, Math.max(0.4, b.strength));
        String n = d.fmt == 0 ? thousands(Math.round(v)) : d.fmt == 1 ? fmt1(v) : String.valueOf(Math.round(v));
        return d.impact.replace("#", n);
    }
}
