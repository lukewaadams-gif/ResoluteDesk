package resolutedesk.home;

/** Events at home that reach the president's desk. */
public final class EventData {
    private EventData() {
    }

    //  Crime and overdoses, disasters, a rare pandemic, AI, protests, infrastructure, fights with the states and
    //  the hot-button issues. Your party's two wings, and an opposition that picks a strategy.
    //  Events use the same option format as the world's crises: "label|adviser|kind|odds|success fx|failure fx|success story|failure story".
    public static final String[] EV_ID = {"ai_jobs", "deepfake", "ai_lab", "heat_grid", "winter_grid", "bridge", "derail", "faa", "police", "protests", "campus", "shooting",
            "scotus", "pills", "schools", "state_imm", "state_suit", "violence", "fed_bldg", "overdose", "crime", "strike", "groceries", "pand_abroad", "pand_home", "vaccine", "data_centers", "chip_plant"};

    public static final String[] EV_COND = {"ai>0,p=0.006", "p=0.004", "ai>0,p=0.003", "month>5,month<9,grid>50,p=0.06", "month<3,grid>45,p=0.04", "p=0.0015", "p=0.0025", "p=0.0025", "p=0.006",
            "protest>62", "gaza>55,month>2,month<6,p=0.03", "p=0.005", "month>4,month<8,p=0.012", "p=0.0025", "p=0.0025", "p=0.004", "p=0.004", "p=0.0012", "p=0.0008", "od>88", "crimeidx>106",
            "p=0.0025", "infl>3.2,p=0.02", "pand>0,pand<2,pandw<1", "pand>1,pandw<1", "pand>1,pandw>18,p=0.2", "ai>0,grid>40,p=0.006", "ai>0,p=0.004"};

    /** The first year each event belongs to. */
    public static final int[] EV_MIN = {2023, 2019, 2023, 1990, 2000, 1950, 1940, 1960, 1960, 1960, 1965, 1990, 1950, 2022, 1990, 2005, 1995, 1965, 1990, 2010, 1965, 1933, 1933, 1960, 1960, 1960, 2023, 2022};

    static {
        for (int i = 0; i < EV_COND.length; i++) EV_COND[i] = "year>" + (EV_MIN[i] - 1) + (EV_COND[i].isEmpty() ? "" : "," + EV_COND[i]);
    }

    public static final int[] EV_COOL = {40, 30, 60, 26, 26, 80, 60, 60, 30, 20, 52, 30, 52, 70, 60, 40, 30, 80, 80, 40, 40, 80, 40, 400, 400, 400, 52, 60};

    public static final String[] EV_TITLE = {"AI is wiping out entry-level office jobs", "A deepfake of you goes viral", "An AI lab warns of a dangerous new model", "A heat wave knocks out power in Texas",
            "A winter storm leaves millions without power", "A major bridge collapses", "A train carrying toxic chemicals derails", "An air-traffic outage grounds flights nationwide",
            "A police shooting caught on video sparks protests", "Protests spread to dozens of cities", "Protests over Gaza sweep college campuses", "A mass shooting at a school",
            "The Supreme Court strikes down a major federal law", "A court ruling on abortion pills sets off a national fight", "A fight over what schools teach goes national",
            "A state defies federal immigration policy", "Twenty states sue over your policies", "An armed man is arrested outside a congressman's home", "Shots are fired at a federal courthouse",
            "Overdose deaths are climbing again", "Violent crime is rising in big cities", "A national rail strike looms", "Grocery prices jump", "A new virus is spreading abroad",
            "The virus is spreading in American cities", "A vaccine is ready", "Data centers strain the power grid", "A giant chip plant opens"};

    public static final String[] EV_BODY = {
            "Banks, insurers and consulting firms have announced tens of thousands of layoffs, saying AI agents can do the work. Young college graduates are hit hardest.",
            "A fake video of you saying things you never said has been viewed tens of millions of times. Many people believe it.",
            "One of the big AI labs has told the government its newest model can give real help to someone trying to make a biological weapon.",
            "Record heat has pushed the Texas grid past its limit. Rolling blackouts have left millions without air conditioning.",
            "Ice and record cold have knocked out power plants and lines across several states. Millions are in the dark.",
            "An interstate bridge has collapsed during the evening rush. Rescue crews are at the scene.",
            "A freight train carrying vinyl chloride derailed in a small Midwestern town. Residents have been evacuated.",
            "A failure in the air-traffic control system has grounded every flight in the country.",
            "Video of a police officer shooting an unarmed man during a traffic stop has spread across the country. Protests are growing.",
            "Large demonstrations have spread to dozens of cities. Most are peaceful; a few have turned violent at night.",
            "Encampments and protests over the war in Gaza have spread to campuses across the country. Some universities have called in police.",
            "A gunman opened fire at a school. The nation is in mourning, and the debate over guns has started again.",
            "In a 6-3 decision, the Court struck down a federal law your party considers essential.",
            "A federal judge has suspended approval of the most common abortion pill nationwide pending appeal.",
            "A clash between parents, teachers and a school board over books and curricula has become a national story.",
            "A governor has ordered state agencies to defy your administration's immigration policy.",
            "A coalition of twenty states has sued to block several of your policies at once.",
            "Police have arrested an armed man outside the home of a member of Congress. No one was hurt. He had posted threats online.",
            "Someone fired several shots at a federal courthouse overnight. No one was hurt. The FBI is investigating.",
            "After falling for two years, overdose deaths are rising again as stronger synthetic opioids spread.",
            "Murders and assaults are up in several big cities after years of decline.",
            "Rail unions and the freight railroads are deadlocked. A strike would shut down much of the country's shipping.",
            "Bird flu, bad harvests and tariffs have pushed food prices up sharply.",
            "A new respiratory virus is spreading quickly abroad. Health officials don't yet know how dangerous it is.",
            "The new virus has been found in several American cities, and hospitals are filling up.",
            "Researchers have a vaccine that works. The question is how to get it out.",
            "Electricity demand from AI data centers is growing faster than power plants can be built. Utilities warn of shortages and higher bills.",
            "One of the world's largest chipmakers has opened a giant new plant in Arizona."};

    public static final String[] EV_OPTS = {
            "A national retraining program|Labor secretary|s|0.7|def=0.03 jobs=0.25 tech=0.1|def=0.03 jobs=0.05|Hundreds of thousands sign up for retraining.|The program is slow to get off the ground.;;"
                    + "Tax companies that replace workers with AI|Economic adviser|s|0.6|jobs=0.2 base=3 fin=-0.4 tech=-0.1|fin=-0.8 tech=-0.2|Workers cheer; tech stocks wobble.|Investors flee tech stocks.;;"
                    + "Let the market adjust|Treasury secretary|s|1|jobs=-0.25 tech=0.1|jobs=-0.25|Critics say you're ignoring the jobs crisis.|Critics say you're ignoring the jobs crisis.",
            "Rebut it on camera|Communications director|c|0.7|perf=0.3|perf=-0.6|Your rebuttal gets more views than the fake.|The fake keeps spreading.;;"
                    + "Demand that platforms label AI fakes|Domestic policy adviser|s|0.75|tech=0.2|tech=0.05|The big platforms agree to label AI-generated video.|The platforms stall.;;"
                    + "Ignore it|Chief of staff|s|1|perf=-0.4|perf=-0.4|The fake keeps spreading.|The fake keeps spreading.",
            "Order a pause until it's tested|National security adviser|s|0.7|tech=0.25 fin=-0.3|tech=0.05 fin=-0.5|The lab pauses; experts praise the decision.|The lab complies, but rivals race ahead.;;"
                    + "Negotiate safeguards with the labs|Commerce secretary|d|0.65|tech=0.15|tech=-0.05|The labs agree to new safeguards.|Talks with the labs drag on.;;"
                    + "Trust the companies to handle it|Economic adviser|s|1|tech=-0.2|tech=-0.2|Safety experts are alarmed.|Safety experts are alarmed.",
            "Emergency federal power orders|Energy secretary|h|0.7|grid=-10 energy=0.15 st:TX=1|grid=5 st:TX=-2 perf=-0.5|Power comes back within a day.|The blackouts drag on for days.;;"
                    + "Blame the grid operator|Political director|s|1|st:TX=-1|st:TX=-1|Texans want the lights on, not blame.|Texans want the lights on, not blame.;;"
                    + "Announce a grid modernization plan|Domestic policy adviser|s|0.6|grid=-6 def=0.02 energy=0.2|grid=-2 def=0.02|The plan wins praise from utilities.|The plan is seen as too little, too late.",
            "Emergency federal power orders|Energy secretary|h|0.7|grid=-8 energy=0.15|grid=4 perf=-0.6|Power comes back within days.|The outages drag on.;;"
                    + "Send FEMA and the National Guard|Homeland Security secretary|h|0.75|perf=0.4|perf=-0.4|Warming centers and generators save lives.|The response is slow.;;"
                    + "Blame the utilities|Political director|s|1|perf=-0.2|perf=-0.2|Voters want the lights on.|Voters want the lights on.",
            "Fast-track federal rebuilding money|Transportation secretary|s|0.8|def=0.01 jobs=0.15 perf=0.3|def=0.01 perf=-0.2|The new bridge is promised within two years.|Red tape slows the rebuilding.;;"
                    + "Visit the site and promise action|Chief of staff|c|0.8|perf=0.5 stress=3|perf=-0.2|Your visit is well received.|Critics call the visit a photo op.;;"
                    + "Let the state lead|Transportation secretary|s|1|perf=-0.3|perf=-0.3|Locals feel ignored.|Locals feel ignored.",
            "Send the EPA and FEMA in force|EPA administrator|h|0.7|perf=0.4 energy=0.1|perf=-0.6 scandal=1|Cleanup moves fast; residents return home.|Residents complain of headaches and silence from Washington.;;"
                    + "Visit the town|Chief of staff|c|0.8|perf=0.5|perf=-0.2|Residents appreciate the visit.|Residents are angry it took so long.;;"
                    + "Push new rail safety rules|Transportation secretary|s|0.6|energy=0.05 perf=0.2|perf=0|The industry accepts tougher rules.|The railroads fight the rules.",
            "Order an emergency overhaul|Transportation secretary|h|0.65|perf=0.3|perf=-0.5|Flights resume within hours; a fix is under way.|More outages follow.;;"
                    + "Blame years of underfunding|Political director|s|1|perf=-0.1|perf=-0.1|Travelers aren't interested in blame.|Travelers aren't interested in blame.;;"
                    + "Ask Congress for emergency money|Legislative director|s|0.6|def=0.01 perf=0.2|perf=-0.1|Congress moves the money quickly.|The money stalls.",
            "Call for calm and a federal investigation|Attorney general|j|0.7|protest=-15 g10=1.5 crime=0.05|protest=5|The investigation calms the streets.|Protests continue.;;"
                    + "Back the police|Political director|s|1|base=3 g10=-2 protest=10 crime=0.1|base=3|Police unions praise you; protesters are angrier.|Police unions praise you; protesters are angrier.;;"
                    + "Meet the family and community leaders|Domestic policy adviser|c|0.7|protest=-12 g10=2|protest=-4|The meeting is widely praised.|Activists call the meeting empty symbolism.",
            "Send federal law enforcement|Attorney general|j|0.55|protest=-25 crime=0.2 base=2 g10=-1|protest=10 perf=-1 scandal=1|Order returns to the streets.|Clashes with federal agents make things worse.;;"
                    + "Meet protest leaders|Domestic policy adviser|c|0.6|protest=-20 g0=1|protest=-5|The meeting lowers the temperature.|The meeting changes little.;;"
                    + "Address the nation|Communications director|c|0.65|protest=-15 perf=0.4|protest=-3|Your address calms the country.|The address falls flat.",
            "Defend free speech and the right to protest|Domestic policy adviser|c|0.6|g0=1 g28=1.5 rights=0.1|g0=0.5 base=-2|The speech is praised on campus and off.|Both sides are unhappy.;;"
                    + "Crack down on antisemitism on campus|Attorney general|j|0.7|g17=1.5 base=1 rights=0.05|g28=-1.5|Universities tighten their rules.|Students accuse you of censorship.;;"
                    + "Leave it to the universities|Chief of staff|s|1|perf=-0.1|perf=-0.1|The protests fade with the semester.|The protests fade with the semester.",
            "Push for new gun safety laws|Domestic policy adviser|s|1|g4=1 g26=-2 base=2 crime=0.05|g4=1|Gun-safety groups rally behind you.|Gun-safety groups rally behind you.;;"
                    + "Focus on school security and mental health|Education secretary|s|0.8|g29=1.5 crime=0.1|g29=0.5|Parents welcome the plan.|Critics call it a dodge.;;"
                    + "Visit the community and console the families|Chief of staff|c|0.85|perf=0.6 stress=4|perf=0.2 stress=4|Your visit brings comfort.|Your visit brings comfort.",
            "Criticize the ruling|Communications director|c|0.7|base=3 rights=0.05|base=2|Your base rallies to you.|Your base rallies to you.;;"
                    + "Accept it and work with Congress on a fix|Legislative director|s|0.6|perf=0.3|perf=0|Lawmakers start work on a fix.|Congress ignores the issue.;;"
                    + "Propose reforms to the Court|Counsel|s|0.5|base=4 opp=-5|base=2 opp=-6 perf=-0.4|The proposal energizes your party.|The proposal goes nowhere.",
            "Protect access through the FDA|Health secretary|x|0.6|g3=2 g4=1 health=0.1|g4=0.5|The FDA keeps the pill available.|Courts block the FDA's move.;;"
                    + "Leave it to the courts and the states|Counsel|s|1|g6=1 g4=-1|g6=1|Both sides wait for the appeal.|Both sides wait for the appeal.;;"
                    + "Push Congress for a national standard|Legislative director|s|0.5|base=3 rights=0.1|base=2|The push energizes your base.|The bill stalls.",
            "Side with the parents|Education secretary|s|1|g29=1 g6=1 base=2|g29=1|Parents' groups cheer.|Parents' groups cheer.;;"
                    + "Side with the teachers|Education secretary|s|1|g24=1 base=1|g24=1|Teachers' unions praise you.|Teachers' unions praise you.;;"
                    + "Say it's a local matter|Chief of staff|s|1|perf=0|perf=0|The story fades.|The story fades.",
            "Sue the state|Attorney general|j|0.6|imm=0.15 base=2|imm=-0.1|A federal judge sides with you.|A judge sides with the state.;;"
                    + "Cut the state's federal grants|Budget director|s|0.5|imm=0.1 base=3 scandal=1|scandal=2 perf=-0.4|The governor backs down.|The courts block the cuts.;;"
                    + "Negotiate with the governor|Chief of staff|d|0.55|imm=0.1 perf=0.2|imm=-0.1|A deal ends the standoff.|The standoff continues.",
            "Fight it in court|Attorney general|j|0.6|perf=0.2|perf=-0.3|A judge throws out most of the suit.|A judge freezes several of your policies.;;"
                    + "Call it a political stunt|Political director|s|1|base=2|base=2|Your base agrees.|Your base agrees.;;"
                    + "Offer to compromise|Chief of staff|d|0.5|perf=0.3 base=-2|base=-2|Several states drop out of the suit.|The states press on.",
            "Call for calm from all sides|Communications director|c|0.8|perf=0.8 opp=2|perf=0.3|Leaders of both parties echo your call.|Your call is drowned out.;;"
                    + "More protection for public officials|Homeland Security secretary|h|0.9|def=0.005 perf=0.3|perf=0.1|The Capitol Police get more money.|The Capitol Police get more money.;;"
                    + "Blame the other side's rhetoric|Political director|s|1|base=3 opp=-6 perf=-0.6|base=3 opp=-6|Your base agrees; the country is more divided.|Your base agrees; the country is more divided.",
            "Speak to the nation|Communications director|c|0.75|perf=0.6|perf=0.1|Your words reassure the country.|Your words reassure the country.;;"
                    + "Order more security for federal judges|Attorney general|j|0.9|def=0.005 perf=0.2|perf=0.1|The Marshals add protection.|The Marshals add protection.;;"
                    + "Let the FBI do its work|Chief of staff|s|1|perf=0|perf=0|The FBI makes an arrest.|The FBI makes an arrest.",
            "A border crackdown on fentanyl|Homeland Security secretary|h|0.65|od=-6 cartel=-5 imm=0.1 crime=0.1|od=-1|Seizures at the border hit a record.|The smugglers adapt.;;"
                    + "Treatment and naloxone everywhere|Health secretary|x|0.75|od=-8 health=0.15 def=0.01|od=-3 def=0.01|Overdose deaths start to fall.|The money is slow to reach clinics.;;"
                    + "Go after the money|Treasury secretary|s|0.6|od=-4 cartel=-8|od=-1|Sanctions hit the cartels' banks.|The cartels move their money.",
            "Surge federal agents into the hardest-hit cities|Attorney general|j|0.65|crimeidx=-5 crime=0.2 base=2|crimeidx=-1 protest=5|Arrests rise and shootings fall.|The surge causes friction with local leaders.;;"
                    + "Money for local police|Budget director|s|0.8|crimeidx=-4 crime=0.15 def=0.01|crimeidx=-1 def=0.01|Mayors welcome the money.|The money is slow to arrive.;;"
                    + "Fund community violence programs|Domestic policy adviser|s|0.6|crimeidx=-3 g10=1|crimeidx=0|The programs show early results.|Critics call the programs soft.",
            "Ask Congress to impose a deal|Legislative director|s|0.7|supply=0 g24=-2 base=-1|supply=-0.2|Congress imposes a contract; the trains keep running.|Congress deadlocks and the strike begins.;;"
                    + "Side with the unions|Labor secretary|s|0.5|g24=3 supply=0|supply=-0.3 g24=2|The railroads give in.|The strike shuts down shipping for a week.;;"
                    + "Mediate a deal yourself|Chief of staff|c|0.6|g24=1 perf=0.5|supply=-0.25|You broker a deal at the White House.|Talks fail and the strike begins.",
            "Go after price gouging|Attorney general|j|0.6|prices=0.15|prices=0|Prosecutors win quick settlements.|The cases drag on.;;"
                    + "Cut tariffs on food|Trade representative|s|0.8|prices=0.2 infl=-0.1|prices=0.05|Prices start to ease.|Prices barely move.;;"
                    + "Tell people it's temporary|Economic adviser|s|1|prices=-0.15|prices=-0.15|Shoppers don't believe it.|Shoppers don't believe it.",
            "Restrict travel from affected countries|Homeland Security secretary|h|0.6|pandslow=1 supply=-0.1|supply=-0.1|The restrictions buy time.|The virus is already here.;;"
                    + "Fund vaccines now|Health secretary|x|0.8|pandfast=1 def=0.02|def=0.02|Drug makers start work on a vaccine.|Drug makers start work on a vaccine.;;"
                    + "Watch and wait|Chief of staff|s|1|perf=0|perf=0|Health officials keep watching.|Health officials keep watching.",
            "Guidance to limit large gatherings|Health secretary|x|0.7|demand=-0.4 health=0.2 pandslow=1|demand=-0.5 health=-0.1|The spread slows.|The spread continues.;;"
                    + "Keep everything open|Economic adviser|s|1|health=-0.3 pandsev=1|health=-0.3|Hospitals strain under the load.|Hospitals strain under the load.;;"
                    + "Surge help to hospitals|Health secretary|x|0.75|health=0.25 def=0.02|health=0.05 def=0.02|Field hospitals and staff ease the strain.|Help arrives slowly.",
            "Free shots for everyone, fast|Health secretary|x|0.8|pandend=1 health=0.3 def=0.02|pandend=1 health=0.1 def=0.02|The vaccine rollout goes smoothly.|The rollout is slow and confusing.;;"
                    + "Send it to the states to distribute|Homeland Security secretary|h|0.6|pandend=1 health=0.15|pandend=1 health=-0.1|The states move quickly.|A patchwork of rules confuses people.;;"
                    + "Leave it to the private sector|Economic adviser|s|0.5|pandend=1|pandend=1 health=-0.2|Pharmacies handle it well.|Shortages and long lines.",
            "Fast-track new power plants|Energy secretary|s|0.7|grid=-12 energy=0.15 tech=0.1|grid=-3|Permits for new plants move quickly.|Utilities say it's not enough.;;"
                    + "Make data centers pay for their own power|Domestic policy adviser|s|0.7|grid=-8 prices=0.1 tech=-0.05|grid=-2|Utilities welcome the rule; tech firms grumble.|Tech firms lobby to kill the rule.;;"
                    + "Let the market sort it out|Economic adviser|s|1|grid=4 prices=-0.1|grid=4|Power bills keep rising.|Power bills keep rising.",
            "Fly out for the opening|Chief of staff|c|0.9|jobs=0.2 st:AZ=1.5 tech=0.1|jobs=0.1|You cut the ribbon to cheers.|You cut the ribbon to cheers.;;"
                    + "Claim credit from the White House|Communications director|c|0.8|jobs=0.1 tech=0.05|perf=0|The press notes the credit-taking.|The press notes the credit-taking.;;"
                    + "Let the company have its day|Chief of staff|s|1|perf=0|perf=0|The plant opens quietly.|The plant opens quietly."};
}
