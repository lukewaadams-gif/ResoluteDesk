package resolutedesk.stories;

/**
 * Stories that follow the state of the country rather than the calendar: hard times and good times, prices, crime,
 * war, the weather of each season and the sports year. The layout is AmbientEarly's; year ranges keep the words
 * right for the era (relief rolls in the 1930s, job fairs today).
 */
public final class AmbientAny {
    private AmbientAny() {
    }

    static final String[] ROWS = {
            // ---- jobs ----
            "1933|1959|unemp>9|E|Men line up at dawn for a day's work in {state}|Hundreds wait outside the gates each morning for a few dozen jobs. Those turned away try the next plant down the road.|sal:JOBS=0.04",
            "1960|1989|unemp>7.5|E|The unemployment line wraps around the block in {state}|Laid-off workers wait hours to file their claims. Caseworkers say they haven't seen it this bad in years.|sal:JOBS=0.04",
            "1990|2037|unemp>7|E|A job fair in {state} draws ten times the expected crowd|Organizers ran out of applications in the first hour. Many in line said they had been looking for months.|sal:JOBS=0.04",
            "1933|2037|unemp>8|D|Young people move home as jobs dry up|Parents in {state} say grown children who lost their jobs are back in their old bedrooms.|sal:JOBS=0.02",
            "1933|2037|uchg>1|E|Layoffs spread from factories to offices|Companies in {state} that were hiring a year ago are now cutting staff. Economists say the slowdown is broadening.|sal:JOBS=0.03",
            "1933|2037|uchg<-1|E|Hiring signs go up across {state}|Employers are competing for workers again. Some are offering signing bonuses and raising starting pay.|",
            "1946|2037|unemp<4|E|Help wanted: businesses in {state} can't find workers|Restaurants are cutting hours and factories are turning down orders because they can't hire enough people.|",
            "1960|2037|unemp<4.5|E|Wages are rising for the lowest-paid workers|With jobs plentiful, workers are switching employers for better pay, and wages at the bottom are climbing fastest.|",
            "1933|2037|unemp>6|D|Charities in {state} say demand has never been higher|Food pantries and shelters are serving families who used to donate to them.|sal:JOBS=0.02",
            // ---- growth and the markets ----
            "1933|2037|gdp<0|E|Economists say a recession has begun|Output has fallen for two quarters in a row. Business owners in {state} say customers have pulled back.|sal:ECON=0.05",
            "1933|2037|gdp>4|E|The economy is booming|Factories are running flat out and stores report record sales. Some economists worry it can't last.|",
            "1933|2037|mkt<-10|B|Stocks tumble, and savers watch their nest eggs shrink|The market has lost more than a tenth of its value in three months. Brokers say the phones haven't stopped ringing.|sal:ECON=0.04",
            "1933|2037|mkt<-18|B|Panic selling on Wall Street|The worst stretch for stocks in years. Retirees in {state} say they're postponing plans.|sal:ECON=0.06",
            "1933|2037|mkt>12|B|Stocks hit a record high|The market is up more than a tenth in three months. Investors are pouring in, and some analysts are warning of froth.|",
            "1933|2037|mkt>20|B|A stock market frenzy|Shoeshine boys and cab drivers are trading tips. Old hands on Wall Street say they've seen this movie before.|",
            "1933|2037|deficit>6|E|The deficit worries Wall Street|Bond traders say the government is borrowing too much. Interest rates are creeping up.|sal:ECON=0.02",
            "1945|2037|fed>7|E|High interest rates freeze buyers out of the market|Car dealers and home builders in {state} say customers can't afford the loans.|sal:ECON=0.03",
            "1960|2037|fed<1.5|E|Cheap money: borrowing costs at rock bottom|Mortgages and car loans are the cheapest in years. Savers complain their accounts earn almost nothing.|",
            "1933|2037|sent<65|E|Consumers are gloomier than they've been in years|Surveys show Americans expect hard times ahead, and they're putting off big purchases.|sal:ECON=0.03",
            "1933|2037|sent>100|E|Shoppers are in a buying mood|Confidence is at its highest in years. Families are buying cars, furniture and vacations.|",
            // ---- prices ----
            "1933|2037|infl>6|E|Prices at the grocery store keep climbing|Shoppers in {state} are buying store brands and skipping meat. Every week the bill is higher.|sal:PRICES=0.05",
            "1933|2037|infl>9|E|Double-digit inflation squeezes retirees|Pensioners on fixed incomes in {state} say they're choosing between food and medicine.|sal:PRICES=0.06",
            "1933|1940|infl<0|E|Falling prices hurt farmers and debtors|Wheat and cotton bring less every season, but mortgage payments stay the same.|",
            "1946|2037|infl<1|E|Prices are flat, and some are falling|Economists say slow price growth is a sign of weak demand.|",
            "1933|2037|gasup>1|D|Drivers grumble at the pump|Filling the tank costs far more than a year ago. Commuters in {state} say they're cutting back on trips.|sal:ENERGY=0.04 sal:PRICES=0.03",
            "1933|2037|gasup<-0.5|D|Cheap gas puts drivers on the road|With prices down, families are taking road trips and buying bigger cars.|",
            "1990|2037|infl>4|E|Rent, insurance and child care eat up paychecks|Families in {state} say even with raises they're falling behind.|sal:PRICES=0.04",
            // ---- the farm ----
            "1933|2037|farm<85|E|Farm prices collapse, and farmers in {state} are going under|Crops are selling for less than they cost to grow. Banks are calling in loans.|sal:ECON=0.03",
            "1933|2037|farm>115|E|A bumper year on the farm|Prices are high and the harvest is big. Farm towns in {state} are buying new equipment.|",
            "1933|2037|month>8,month<11|N|Harvest time in {state}|Combines are working through the night to bring in the crop before the weather turns.|",
            // ---- crime and the courts ----
            "1933|2037|crimeidx>110|L|Murders climb in the big cities|Police chiefs say a handful of neighborhoods account for most of the violence. Residents want more officers.|sal:CRIME=0.05",
            "1933|2037|crimeidx<85|L|Crime falls to a generational low|Police in {state} report the fewest killings in decades. Criminologists debate why.|",
            "1933|1965||L|A bank robbery and a car chase in {state}|Three men in overcoats held up a bank and escaped in a stolen sedan. State police have set up roadblocks.|sal:CRIME=0.01",
            "1966|2037|crimeidx>100|L|Car thefts and break-ins worry the suburbs|Neighborhood watch groups are forming in towns that used to leave their doors unlocked.|sal:CRIME=0.03",
            "1990|2037|od>70|H|Overdoses fill the emergency rooms in {state}|Hospitals say they treat overdose patients every night. Families are burying sons and daughters.|sal:HEALTH=0.04",
            "1933|2037||L|A jury acquits in a case that gripped {state}|After three weeks of testimony, the jury deliberated for two days. The defendant wept as the verdict was read.|",
            "1933|2037||L|A long-unsolved case is cracked in {state}|Detectives say new evidence led them to a suspect decades after the crime.|",
            // ---- war and peace ----
            "1933|2037|wars>0|D|A hometown sends off its soldiers|A crowd filled the station in a small town in {state} to see off a unit bound for the war.|",
            "1933|2037|wars>0,kia>1000|D|A town in {state} buries a soldier|Flags flew at half-staff as a soldier killed in the war was laid to rest. Thousands lined the route of the funeral procession.|sal:SECURITY=0.03",
            "1933|2037|wars>0|E|War production lifts factory towns|Defense plants in {state} are hiring, and the towns around them are busier than they've been in years.|",
            "1960|2037|wars>0|D|Protesters picket a military base in {state}|Demonstrators against the war are holding a vigil outside the base gates.|",
            "1933|2037|wars<1|D|Veterans return to a quieter country|With no war on, veterans in {state} are going back to school and starting businesses.|",
            "1990|2037|terror>55|D|Security tightens at airports and stadiums|Bag checks and longer lines as officials warn of a heightened threat.|sal:SECURITY=0.04",
            "1990|2037|migr>65|D|Border towns in Texas and Arizona say they're overwhelmed|Shelters are full, and local officials are asking for federal help.|sal:IMMIG=0.05",
            "2005|2037|cyber>60|T|Hackers steal millions of customer records|A big retailer says hackers stole names, addresses and card numbers. Customers are being urged to change passwords.|sal:TECH=0.03",
            // ---- protest and public life ----
            "1933|2037|protest>55|D|Marchers fill the streets of {state}'s capital|Thousands rallied outside the statehouse. Organizers say they'll keep coming back.|",
            "1933|2037|protest<20|D|A quiet season in public life|Pollsters say the country's mood is calmer than it has been in years.|",
            "1933|2037||D|A town in {state} celebrates its centennial|Parades, fireworks and a time capsule mark a hundred years since the town was founded.|",
            "1933|2037||D|A spelling bee champion from {state}|An eighth-grader won the national spelling bee in Washington, spelling a word most adults have never heard.|",
            "1933|2037||D|A new bridge opens in {state}|The span replaces a ferry that has run for more than eighty years. Hundreds walked across on opening day.|",
            "1933|2037||D|A county fair in {state} draws record crowds|Prize hogs, pie contests and a Ferris wheel. Organizers say it was the best fair in memory.|",
            "1950|2037||D|A small town in {state} loses its last factory|The plant that employed half the town is closing. The mayor says they'll find a way to start over.|",
            "1960|2037||D|A community in {state} rallies after a flood|Volunteers are filling sandbags and gutting homes. Churches are serving meals around the clock.|",
            "1933|2037||D|A centenarian in {state} recalls a century of change|She remembers the first automobile in her town. Her advice for a long life: keep busy and stay curious.|",
            // ---- the seasons ----
            "1933|2037|month<3|N|A winter storm shuts down {state}|A foot of snow and sheets of ice have closed schools and highways. Utility crews are working to restore power.|",
            "1933|2037|month<3|N|Bitter cold grips the northern states|Temperatures fell below zero from Montana to Maine. Shelters opened extra beds.|",
            "1933|2037|month>11|N|A December blizzard strands holiday travelers|Bus and rail stations are packed with travelers waiting out the storm.|",
            "1933|2037|month>2,month<6|N|Spring floods along the rivers of {state}|Melting snow and heavy rain have pushed rivers over their banks. Families are moving to higher ground.|",
            "1933|2037|month>3,month<7|N|Tornadoes rip through {state}|A line of storms spun up several tornadoes overnight, flattening homes and barns.|",
            "1933|2037|month>5,month<9|N|A heat wave scorches {state}|Temperatures above 100 for a week. Officials are urging people to check on elderly neighbors.|",
            "1933|2037|month>5,month<9|N|Drought dries up wells in {state}|Farmers are hauling water to their livestock, and towns are banning lawn watering.|farm=-1",
            "1933|2037|month>6,month<11|N|A hurricane makes landfall on the Gulf Coast|The storm pushed a surge into coastal towns and knocked out power to hundreds of thousands.|",
            "1933|2037|month>6,month<11|N|A hurricane brushes the Carolinas|Coastal towns are cleaning up after a storm that flooded streets and tore off roofs.|",
            "1960|2037|month>6,month<11|N|Wildfires force evacuations in the West|Firefighters are battling blazes in several states. Whole towns have been told to leave.|",
            "1933|2037|month>8,month<11|N|An early frost hits the orchards of {state}|Growers say they lost much of the apple crop in a single night.|",
            "1933|2037||N|An earthquake rattles California|The quake knocked items off shelves across the region but caused little serious damage.|",
            // ---- sports ----
            "1933|2037|month>3,month<5|X|Opening day: baseball is back|Fans filled ballparks across the country. Even in the cold, the stands were full.|",
            "1933|2037|month>9,month<11|X|The World Series opens tonight|Two teams that took different roads to the Series meet in the opener. Office radios and televisions will be on all week.|",
            "1933|2037|month>4,month<6|X|The Kentucky Derby draws a record crowd|A long shot came from behind to win the run for the roses at Churchill Downs.|",
            "1933|2037|month>4,month<6|X|Speed and heat at the Indianapolis 500|More than two hundred thousand fans watched the race at the Brickyard.|",
            "1967|2037|month>0,month<3|X|The Super Bowl is the most-watched broadcast of the year|Parties across the country, and advertisers paying record sums for a few seconds of airtime.|",
            "1975|2037|month>2,month<5|X|March Madness: an underdog shocks a top seed|Office pools across the country were busted when a small school knocked off one of the favorites.|",
            "1950|2037|month>5,month<7|X|The basketball finals go to a seventh game|The series has been tied after every pair of games. Tickets are going for ten times face value.|",
            "1933|2037|month>5,month<9|X|A rookie throws a no-hitter|The young pitcher, called up from the minors only last month, didn't allow a hit.|",
            "1933|2037|month>8|X|College football season kicks off|Stadiums on campuses across the country are full again on Saturday afternoons.|",
            "1933|2037|month>10|X|A rivalry game decides the conference title|Two old rivals met with a championship on the line, and the game went down to the last play.|",
            "1933|2037||X|A hometown hero from {state} retires|After eighteen seasons, the beloved veteran played his last game before a standing ovation.|",
            "1933|2037||X|A beloved ballpark will be torn down|The team will move to a new stadium next year. Fans are lining up for one last look.|",
            // ---- health, science and technology ----
            "1933|2037|month>10|H|Flu season arrives early|Doctors' offices are filling up and schools in {state} report high absences.|",
            "1933|1960|month>5,month<9|H|A polio scare closes the pools in {state}|Health officials closed public pools after several children fell ill.|sal:HEALTH=0.03",
            "1946|2037||H|A new hospital opens in {state}|The hospital will serve a rural region where the nearest emergency room was more than an hour away.|",
            "1990|2037||H|Rural hospitals in {state} are closing|Three hospitals have shut their doors in two years. Patients must drive hours for care.|sal:HEALTH=0.03",
            "1933|2037||S|Astronomers report a new comet|The comet may be visible to the naked eye next month if it survives its pass near the sun.|",
            "1950|2037||S|Scientists in {state} report a promising cancer treatment|Early trials show tumors shrinking in some patients. Researchers caution that more study is needed.|",
            "1960|2037||S|A meteor lights up the night sky over {state}|Hundreds of people reported seeing a fireball streak across the sky. Some heard a sonic boom.|",
            "1990|2037||T|A tech company opens a giant campus in {state}|The company says it will hire thousands. Local officials offered millions in tax breaks to win it.|",
            "2015|2037||D|Teenagers and their phones: a debate in every household|Schools in {state} are banning phones in class, and parents are trading tips on screen time.|",
            "2019|2037||B|Sports betting apps are everywhere|Millions of Americans now bet on games from their phones. Counselors say calls to gambling hotlines are up.|",
            "1955|2037||B|A new shopping center opens outside {state}'s biggest city|The developer says it will be the region's largest, with department stores and hundreds of shops.|",
            "1995|2037||B|A downtown in {state} comes back to life|Old warehouses are now apartments, restaurants and breweries.|",
            // ---- culture ----
            "1933|2037||C|A novel about a family from {state} tops the bestseller lists|Readers say the story of three generations rings true. Hollywood has bought the rights.|",
            "1933|2037||C|A summer blockbuster breaks box-office records|Audiences are lining up for the season's biggest picture. Theater owners say it's their best weekend in years.|",
            "1950|2037||C|A singer from {state} has the number one song in the country|The young singer recorded it in a small studio. Now it's on every radio.|",
            "1933|2037||C|A museum in {state} unveils a newly discovered painting|Experts say the work, found in an attic, is by an American master.|",
            "1960|2037||C|An awards show draws a huge audience|Hollywood's biggest night ended with a surprise winner for best picture.|",
            "2013|2037||C|A streaming series becomes a sensation|Everyone seems to be talking about the same show, and the actors are suddenly famous.|",
    };
}
