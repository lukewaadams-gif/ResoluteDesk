# Resolute Desk

A presidency simulator. You take the oath on January 20 and govern, a week at a time, from the Oval Office.

Resolute Desk is a separate game from [Battleground 2028](https://github.com/lukewaadams-gif/Battleground2028), which is about winning the White House. This one is about the job itself.

![The Oval Office](screenshots/oval-office.png)

## How to play it

- **In a browser:** download `index.html` and open it. No install, no internet needed. Saves stay in that browser.
- **As a desktop app:** with Java 8 or newer, double-click `dist/ResoluteDesk.jar`, or run `java -jar dist/ResoluteDesk.jar`.
- **In BlueJ:** create a project, add `dist/ResoluteDesk.java` (Edit > Add Class from File), compile, and run `main`.

The same three files are attached to each [release](https://github.com/lukewaadams-gif/ResoluteDesk/releases).

The game autosaves every week. Use **Continue** on the title screen to pick up where you left off.

## What's in it

Version 1 came in six updates. The first covered the core of the job: your time, your health, the economy and public opinion. The second added Congress and the budget, the third the executive branch, the fourth the world, the fifth the home front and the press, and the sixth history, re-election and the verdict.

Version 2 is under way; see [Coming in version 2](#coming-in-version-2).

- **When you take office:** any of 25 inaugurations.
  - **Every inauguration from 1933 to 2021**, from Franklin Roosevelt's first to Joe Biden's (more under update 6).
  - **2025**, the real present. Donald Trump, or anyone else as a what-if.
  - **2029**, in the world of Battleground 2028, or as the winner of your own Battleground 2028 game.
  - Each era has its own Congress, economy, Fed chair, congressional leaders and the issues people care about.
- **Scenarios and custom conditions:**
  - Five twists on the real starting point: a recession, a war abroad, divided government, a landslide mandate, or an inflation shock.
  - Set approval, unemployment, inflation, growth and your party's seats yourself.
  - **The world you inherit in 2029** (new in version 2): set the popular-vote margin of the 2028 election (a bigger win, a bigger mandate), the deficit, the balance of the Supreme Court (justices who retire are replaced by made-up ones), and the tension in the Taiwan Strait, between Russia and Ukraine (from a peace agreement to all-out war) and in the Middle East. Leave any of them alone to keep the projection.
  - Easy, Normal and Hard.
- **Your president and running mate:** play the real winner, the runner-up or another contender of the day (about 35 politicians of today and 55 of the past), or create your own. You choose the name, party, home state, age and background, and spend points on strengths.
- **The transition** (new in version 2): start the day after the election, with the weeks until the inauguration to choose a government.
  - Every cabinet post and senior staff job has a shortlist of three to five prospects: an expert, a loyalist, and a mix of politicians, outsiders and crossover picks from the other party. Each comes with a background, strengths and weaknesses, an ideology and a read on their skill and loyalty.
  - In 2025 the real cabinet is one choice for each job. Everyone else on the lists is fictional.
  - Your team has eight hours a week. Interview prospects to learn what they're really like, order FBI background checks (four at a time, two weeks each), meet senators to smooth a confirmation, or float a name to see the Senate's whip count, at the risk of reporters digging.
  - Front-runners leak. A problem nobody checked can come out at the confirmation hearing.
  - The transition is graded. A good one lengthens your honeymoon and gets departments off to a running start. You can also let your team fill every job.
- **The Oval Office is the home screen.** Click what you want:
  - the Resolute Desk for your schedule
  - the windows for the country
  - the briefing book for the economy
  - the papers on the coffee table for the news
  - the doors for the rest of the White House
- **A calendar of 21 slots a week** (morning, afternoon and evening, seven days). Fill them with:
  - policy time and White House remarks on an issue
  - trips to any state on Air Force One, with real flight times
  - Hill meetings, fundraisers and ceremonies
  - exercise, family time and rest, plus Camp David at the weekend and late nights at your desk
- **Stamina, sleep and stress:**
  - Long days cost more as presidents age.
  - Late nights cut sleep.
  - Exhaustion raises the odds of illness, and occasionally a health scare.
- **An economy that runs every week:**
  - An output gap pushed by fiscal policy, interest rates, tariffs, confidence and shocks.
  - Unemployment by Okun's law.
  - Inflation anchored by the Fed's credibility.
  - A Fed that follows a Taylor rule at its eight meetings a year.
  - Treasury yields, mortgage rates, stocks and oil.
  - The prices people feel: gas, groceries, rent and insurance.
  - Industries: housing starts, manufacturing, AI investment, farm income and energy production.
  - The deficit and the debt, with rising interest costs.
  - Monthly jobs and inflation reports, and quarterly GDP.
  - Recessions, market crashes and oil spikes arrive at about their historical rates.
- **Approval in detail:**
  - National approval follows the economy, prices, the honeymoon and your work. Events move it: rallies, scandals, wins and failures, the wear of office and a drifting public mood.
  - Its range is real presidents'. A big win brings a honeymoon in the 60s; bad times bring lows in the 30s; a rally around the flag can reach the high 80s. Since about 1990 partisans keep a floor near 30 and a ceiling near 70. Before then there's no floor, and approval can fall into the 20s, as Truman's and Nixon's did.
  - It's broken down by 14 issues, about 17 voter groups and all 50 states plus DC.
  - The breakdowns come from the electorate model built for Battleground 2028: 96 voter cells in every state.
  - Each state's jobs and gas prices move its numbers.
- **The Capital Ledger:** a front page each week with the biggest story, the rest of the news, the week ahead, poll watch, markets and an opinion column. Headlines also scroll across the bottom of the Oval Office.

### Congress and the budget (update 2)

Open the Roosevelt Room from the Oval Office, or press 6.

- **The Hill:**
  - Each chamber is split into three blocs per party, from the Progressive Caucus to the Freedom Caucus, each with its own ideology, party loyalty and worry about the deficit.
  - The real leaders of the era, plus fictional committee chairs and swing votes.
  - You have a relationship with every leader and bloc. Hill meetings on your calendar, private meetings, wins, deals and vetoes all move it.
- **Relationships, not political capital** (new in version 2):
  - Your time is the limit. Every call or deal takes an hour, and each Hill meeting on your calendar adds two and a half hours to the week.
  - Members owe you favors when you do things for them: saying yes when they ask for a project, an appointment or a visit, campaigning in their states, or signing their committees' bills. Call a favor in on a hard vote.
  - Promises are kept or broken. Endorse a member and you've promised to campaign in their state before the election.
  - Appointments to offer are limited, and projects cost money and annoy the deficit hawks.
  - Your election margin gives you a mandate that pushes your bills in the first year, and Congress gives a new president a honeymoon of about a hundred days.
- **Your bills:** pick from 33 real proposals of both parties.
  - Set the bill's size and scope, and how to pay for it: borrow, tax the wealthy and corporations, or cut spending elsewhere.
  - Choose which chamber goes first. Revenue bills must start in the House.
  - Use budget reconciliation for a simple Senate majority, once a fiscal year, when your party holds both chambers. The Byrd rule strikes provisions that aren't about money.
  - The whip gives you a first read before you send it.
- **The path of a bill:**
  - A committee chair from the other party can bottle it up.
  - Leaders don't call votes they expect to lose. Your party's leaders keep whipping; the other party's won't bring your bill up unless most of their members want it.
  - Without reconciliation, the Senate filibuster needs 60 votes. You can try the nuclear option, but your own moderates usually balk.
  - Then the second chamber, conference and your desk.
- **The whip count:**
  - See where every bloc stands, in both chambers.
  - Work named members with calls, projects back home, appointments, endorsements or threats. Each swing vote wants something different.
  - Go public to pressure moderates, change the bill mid-fight, or withdraw it before it fails.
- **Your desk:**
  - Sign, veto or let a bill become law without your signature, with advice from your staff.
  - Congress moves bills of its own, and if it has the votes it will try to override you.
  - Signed laws change the economy: growth, jobs, prices and the deficit.
- **The budget:**
  - Send a request each spring: defense, other agencies, taxes and entitlements, with a ten-year projection of deficits and debt.
  - Track Congress's 12 spending bills over the summer.
  - On October 1: a stopgap, a fight over its terms, or a shutdown that hurts the economy and your approval week by week. With divided government, or a Senate minority big enough to filibuster, you have to bargain.
- **The debt limit:**
  - When the Treasury hits it, extraordinary measures buy a few months.
  - Then you choose: ask for a clean increase, trade cuts for it, go to the brink, or, at the very end, invoke the 14th Amendment.
- **The midterms:**
  - Run on your approval and the economy. The president's party usually loses ground.
  - Each era has the Senate seats actually up that year.
  - From late summer you can schedule campaign stops in states, and the money you've raised helps.
  - New leaders and chairs take over if control changes.

### Policies and their real-world effects (new in version 2)

Open the Cabinet Room from any room's tabs, or press 9.

- **146 policies in 12 areas**: taxes and the budget, the safety net, jobs, health care, housing and transportation, energy and the environment, immigration, crime, education, rights and democracy, technology and business, and defense and trade.
  - Each policy has two to five levels, from the minimum wage (none, a third of the typical wage, up to two-thirds) to abortion law, the asylum rules, the carbon price or the draft.
  - Every era starts with the law of its day: a 91% top tax rate in 1961, Medicare from 1966, Roe from 1973 to 2022.
- **Change them the way presidents do:**
  - **A bill:** full strength and hard to undo, but it has to get through Congress. The odds are shown before you send it.
  - **An executive order:** immediate, but at 60% strength. Courts can block it, and a successor can undo it.
  - **An agency rule:** months of public comment, then 85% strength. Congress can try to overturn it.
  - **The budget:** takes effect on October 1, if Congress funds it.
  - Each policy allows only the channels that can really change it; the minimum wage, for example, takes a law.
- **Real-world outcomes**, with lags: unemployment, inflation, growth, the deficit, gas prices, the uninsured rate, poverty, violent crime, overdose deaths, border crossings, carbon emissions and test scores. Each starts at its real value for the year and moves as your policies work through, over months or years.
- **Voters react twice:** when you make a change, and as the results show up. A signature policy the public wants can move national approval by up to five points; the groups it touches move much more.
- **The real-world impacts panel** for every change, shown when you pick a policy and a level:
  - who gains and who pays
  - the effects on the economy, the budget and each outcome, with ranges and timing
  - how sure anyone can be (strong, moderate, weak or contested evidence)
  - the historical precedent
  - how each voter group would react, with arrows
  - the odds, timing and strength of each way to make the change

  Bills for a policy change in the Roosevelt Room open the same panel.
- **The system map:** in the style of Democracy, the policies of an area linked to the outcomes they move, the outcomes linked to the issues voters judge you on, and the voter groups each policy wins or loses. Point at a policy or an outcome to trace it.
- **Outcomes** at a glance: each number, where it has moved since you took office, its history, and the policies that moved it most.
- The named bills of history change the policies they really changed. The Social Security Act creates Social Security; the Affordable Care Act expands Medicaid.

### The executive branch (update 3)

Open the West Wing from the Oval Office, or press 5.

- **Your staff:** nine senior aides, each with skill, loyalty, morale and a trait. The nine are your chief of staff, national security adviser, press secretary, counsel, economic adviser, domestic policy adviser, legislative director, political director and communications director.
  - Each one makes something work better. A strong chief of staff lowers your stress and cuts leaks. A strong counsel keeps orders out of trouble in court. A strong legislative director makes deals stick.
  - Morale falls when you're unpopular, when the place is tense and when you overrule them.
  - Unhappy staff leak, and some resign. Hire and fire as you see fit.
  - Advisers take sides in your decisions. Whoever you agree with is pleased, and whoever you overrule is less so.
- **Delegation:** hand off planning your week, working the votes, writing the budget, pardon requests or filling vacancies. How well it's done depends on who does it.
- **The cabinet:** 15 departments and 6 cabinet-level posts, all confirmed by a Senate vote.
  - In 2025 the real nominees are the default. Otherwise your transition team offers a loyalist, an expert, and a big name from your party or a pick from the other side.
  - Until a nominee is confirmed, a career official runs the department.
  - Each department has a readiness score. Strong, happy leadership raises it; staff cuts and shutdowns lower it.
  - Low readiness means botched rollouts of your laws, orders and rules.
  - Cabinet meetings on your calendar lift morale and readiness.
  - The federal workforce (about 2.2 million civilians) shrinks with hiring freezes and layoffs.
- **The vice president:** send them on missions:
  - tour the swing states
  - lead a task force on an issue
  - work the Senate
  - raise money
  - represent you abroad

  A vice president with nothing to do grows restless.
- **Orders, rules and emergencies:** a library of 49 actions from both parties, plus custom orders.
  - Executive orders take effect at once.
  - Regulations go through months of public comment and can be overturned under the Congressional Review Act, subject to your veto.
  - National emergencies unlock powers such as emergency tariffs or troops at the border. Congress can vote to end them.
  - Each action moves the economy, your standing on its issue, or particular groups.
- **The courts:**
  - Aggressive actions get sued. A district judge can block one within weeks.
  - You can appeal, rewrite it or let it go, or slow-walk the ruling and risk a constitutional fight.
  - Big cases reach the Supreme Court: the nine real justices, voting by judicial philosophy and the strength of the case.
  - Justices retire on their own schedule, and you pick from a shortlist of successors.
  - Lower-court vacancies fill steadily. You choose ideological, mainstream or consensus nominees, and blue slips slow the ideological ones.
- **Oversight and scandal:**
  - Leaks, ethics stories, pardons for allies and botched rollouts become scandals that weigh on your approval. You can release everything, deny, push someone out or order a review.
  - A Congress run by the other party investigates. You can comply, negotiate, claim executive privilege or refuse and risk contempt.
  - Clemency requests arrive every few months.
  - Enough impeachable trouble with the other party running the House brings an inquiry, an impeachment vote and a Senate trial. It takes two-thirds of the Senate to remove you.

### The world (update 4)

Open the Situation Room from the Oval Office, or press 7.

- **Forty countries** on a world map, each colored by its relationship with Washington.
  - In 2025 they're led by the real leaders of January 20, 2025, and the real handovers that followed happen on schedule: Carney, Merz, Lee Jae-myung, Takaichi, Magyar, al-Zaidi and Burnham.
  - The 2029 era projects today's leaders forward. Where a leader will be term-limited by then, the successor is fictional.
  - Each country has an alignment (treaty ally, partner, neutral or adversary), its weight in the world, real 2024 trade figures, and whether it has nuclear weapons.
- **Diplomacy:**
  - Make up to three calls a week, invite leaders for state visits, and travel abroad on Air Force One. Trips go on your calendar, with real flight times and jet lag.
  - Hold summits played in three rounds: the opening, the ask (a trade deal, a ceasefire, an arms treaty, limits on Iran and more) and what you give in return.
  - The G7, NATO, the UN General Assembly and the G20 come around every year.
- **Trade, sanctions and aid by country:** tariffs from 0 to 50%, sanctions and aid at four levels.
  - Tariffs feed the economy's tariff rate and invite retaliation against farmers.
  - Sanctions on oil producers move the price of oil.
- **Six hot spots that evolve every week:**
  - Taiwan, from calm to drills, a quarantine, a blockade and war.
  - Russia and Ukraine: the front, Ukraine's strength and the progress of talks toward a ceasefire and a peace.
  - The Middle East: Gaza, Iran's nuclear program, Red Sea shipping and attacks on US troops.
  - The Korean peninsula.
  - Terrorism and cyberattacks.
  - The border and the cartels.
- **Twenty crises** grow out of the hot spots. Each comes with options from your national security team, their odds, and success or failure that plays out.
  - The odds depend on your president's foreign-policy experience, the Pentagon's readiness, and the skill of your secretary of state, defense secretary and intelligence chiefs.
- **The Situation Room:**
  - Ten military operations and seven covert programs. A covert program needs a presidential finding, and a leaked one becomes a scandal.
  - Force posture in the Pacific, Europe and the Middle East.
  - NATO and Asia-Pacific alliance cohesion, deterrence in the Taiwan Strait, and DEFCON.
- **The world feeds back:** oil prices, supply chains and markets, your standing on foreign policy, national security and immigration, and American casualties.

### Statecraft and wars of choice (new in version 2)

The Statecraft tab of the Situation Room, or the button under any country on the world map.

- **Objectives with every country:** 85 of them, two to four for each country in every era, from a ceasefire in Ukraine and a deal on Iran's nuclear program to AUKUS submarines, Venezuelan elections, the Panama Canal, Berlin and the hostages in Tehran.
  - Each has two to four steps, with odds that depend on the relationship and on the people working on it.
  - Some steps need a tool in place first, such as military aid at a given level or a brigade in Poland.
  - A failure costs a month; three in a row stall the objective for half a year.
- **The tools short of war, country by country:**
  - Military aid and arms sales at three levels. They warm a friend, anger its rival, and cost money; arming an autocracy costs you on human rights.
  - American forces based there, from advisers to a corps of 60,000. They deter rivals, and Beijing treats troops on Taiwan as a provocation.
  - Diplomatic ties: full, downgraded or severed. Recognizing Taiwan as a country is the most explosive version.
  - Peacekeepers where there's a conflict to keep apart.
  - Trade agreements, which need both chambers, and defense treaties, which need two-thirds of the Senate. They're harder in today's protectionist Congress, and a country already at war with a nuclear power can't get a treaty through.
  - Four covert programs: an influence campaign, cyber sabotage, arming the opposition and regime change. Each has odds of working and of leaking, and a leak is a scandal.
- **Wars of choice against any country, at any time:** strikes, a naval blockade, a no-fly zone or an invasion.
  - A warning first: the troops it will take, what happens to oil and markets, terror and refugees, how allies will react, and whether Congress has authorized it.
  - Ask Congress for an authorization to use force first, or don't. Without one, Congress can vote under the War Powers Resolution to order the troops home, and you can sign or veto.
  - A small country's army collapses in weeks; a large one fights for months. Nobody plans an invasion of a nuclear power, and strikes on one risk a spiral that kills thousands in a day. Invade North Korea and China fights for it, as it did in 1950.
  - An invasion that topples the government becomes an occupation, usually with an insurgency.
  - Consequences: casualties, oil shocks, refugees, terror threats, protests at home, damaged alliances, and support at home that falls about six points with every tenfold rise in American deaths. An unpopular war drags on your approval for as long as it lasts.
  - Many ways out: win, reach a settlement, withdraw now, set a timetable of six months or a year, hand over to the new government or to allies, declare victory (believable only if the front holds), or escalate.

### The news and runaway storylines (new in version 2)

- **541 stories of their era** beyond the White House, filed under science, culture, sports, business, health, technology, the weather and daily life. They fill the Capital Ledger and the ticker on quiet weeks, and each runs once a presidency.
  - From 1933 to mid-2026 they are real events in their real months: the Hindenburg, the Golden Gate Bridge, the polio vaccine trials, Telstar, Woodstock, the Miracle on Ice, Chernobyl, the Dream Team, the first iPhone, the Webb telescope, the first American pope.
  - After that they are plausible inventions about no one real.
  - Others follow the state of the country: breadlines in the 1930s and job fairs today when unemployment is high, gas lines when prices spike, crime waves, war production and gold stars in the windows during a war, the storms of each season, and the sports calendar.
  - Some make voters care more about an issue: a crime story raises the salience of crime, a pump-price story raises energy.
  - Turning points the game already plays as decisions, and anything about presidents themselves, are left out. No one is quoted.
- **Sixteen storylines that can grow until they take over a presidency:** a run on the banks, a strike wave, a protest movement, a third-party insurgency, the great drought, the loyalty hunt of the late 1940s and 1950s, the wage-price spiral, the polio summers, the crime panic, the gas lines, the housing bubble, the overdose epidemic, the ransomware wave, the viral election hoax, states defying Washington, and the AI layoffs.
  - Each starts small, in a state of the game's choosing and often around a made-up figure, and has three or four stages. Every stage brings a decision with options, advisers and odds.
  - A storyline has heat. Your answer cools it or feeds it, a failed gamble makes it worse, and the country keeps feeding it: unemployment feeds a strike wave, a falling market a bank run, low approval a third party.
  - At the end of each stage a hot storyline grows into the next and a cool one fades. One still hot at the end of its last stage takes over the presidency, with its full effect on the economy and approval, a lasting hold on what voters care about, and a place in the record.
  - The paper tracks every storyline under way beside the front page, and the daily brief flags the ones that are growing.

### The home front and the press (update 5)

Open the Press Briefing Room from the Oval Office, or press 8. The daily brief is in the Situation Room.

- **The President's Daily Brief:** each week, what the intelligence community and your staff most want you to know, ranked by urgency.
- **Twenty-eight events at home**, each with options, odds and results that play out. They cover:
  - AI: job losses, deepfakes, a dangerous new model, data centers straining the grid, a new chip plant.
  - Blackouts in heat waves and winter storms.
  - A bridge collapse, a toxic derailment, an air-traffic outage, a rail strike.
  - Police shootings and protests, campus protests, a school shooting.
  - Supreme Court rulings, abortion pills, fights over schools.
  - States defying federal policy, states suing you.
  - Overdoses and violent crime, grocery prices.
  - Rare, non-graphic political violence.
- **Disasters by season:** hurricanes, wildfires, tornadoes, floods, winter storms and earthquakes, in the states where they really strike. How well FEMA responds depends on Homeland Security's readiness.
- **A rare pandemic:** it spreads abroad, then here, and ends with a vaccine. You decide on travel limits, gatherings, hospitals and the rollout.
- **Crime, overdoses, protest and the grid** are tracked from week to week.
- **The bully pulpit:**
  - Oval Office addresses and the State of the Union, built line by line: an opening, three passages from your strongest issues and your bills, and a close. Your speaking skill and a consistent tone decide how it lands; the State of the Union counts standing ovations.
  - Press conferences with fictional reporters from real outlets, and sit-down interviews with networks and podcasts. Each is three questions: answer straight, pivot, push back or make news.
  - Social media: five posts a week, on any issue, in five tones. Your feel for it decides the reach, and your discipline decides the risk.
  - Commissioned polls on any issue, by voter group.
- **The press corps:** eighteen outlets, each with an audience and a tone toward you that follows your approval and your press relations.
- **Your party:** two wings, such as Progressives and Moderates, or MAGA populists and Traditional conservatives.
  - They judge you by where your laws, orders and choices have landed, and by how popular you are.
  - Their mood moves your relationships in Congress and your base's turnout in the midterms.
- **The opposition** picks a strategy each quarter (cooperate, pick its fights, or obstruct everything) based on your approval and the calendar.

### History, re-election and the verdict (update 6)

The eras are on the **New presidency** screen. The campaign is in the Press Briefing Room; the wars are in the Situation Room.

- **Every inauguration from 1933 to 2021**, 23 in all. Each era is set up as it was on the day:
  - The economy, from a quarter of the workforce out of work in 1933 to the pandemic, in the dollars of the day. The Fed holds rates down until 1951, as it did.
  - The real Congress and its leaders, with the blocs of the day: New Dealers, Southern Democrats and Old Guard Republicans; Boll Weevils and Gypsy Moths; Blue Dogs. Before 1975, cloture took two-thirds of the Senate, and the filibuster was used mostly against civil rights.
  - The Supreme Court as it sat, and only the cabinet departments that existed. Congress creates the rest as the years go by: Defense in 1947, HUD in 1966, Energy in 1978, Homeland Security in 2003. Committees carry their old names.
  - The press of the day: newspapers, wire services and radio networks in the 1930s, then the three networks, cable, talk radio and the web.
  - The world as it was, with the real leaders of the day: the Soviet Union, Nationalist China, a divided Germany. Leaders come and go over the years as they really did.
  - Electoral votes from each census, and states that lean the way they did then. After a Democratic president signs a civil rights law, the Deep South breaks away, as it did in 1964.
- **108 turning points** arrive on their real dates, each with options from your advisers. They run from the bank holiday, Pearl Harbor and the atomic bomb, through Berlin, Korea, Little Rock, Sputnik and the Cuban missile crisis, to 9/11, Lehman Brothers and the pandemic. What follows depends on what you choose.
- **94 historical bills**, from the New Deal, the GI Bill and the interstate highways to Medicare, the Civil Rights Act, the Clean Air Act, the Reagan tax cuts, NAFTA and the Affordable Care Act.
  - Each is on the table only in the years it really was.
  - Laws already passed before your era began aren't offered again.
- **Wars:** World War II on two fronts, Korea, Vietnam, the Gulf War, Afghanistan, Iraq and the campaign against ISIS.
  - You set troop levels and a strategy, watch the front and support at home, and seek a settlement or bring the troops home.
  - The war shapes the economy, the casualty count and your approval.
- **The re-election campaign:**
  - From 1952 on, the 22nd Amendment's two-term limit applies, as written.
  - You decide whether to run.
  - A primary challenger appears if you're weak.
  - There are the conventions and your acceptance speech, a message for the campaign, and money split between the ground game and television.
  - Debates are held only in the years they really were: 1960, and from 1976.
  - In the fall you schedule campaign stops, and the polls are broken down state by state.
  - On election night the states are called one by one. Win, and you start a second term. If you're not running, your party's nominee runs to succeed you.
- **The verdict:** when you leave office, historians score you on the ten measures of the C-SPAN Presidential Historians Survey and place you among the 44 presidents of its 2021 rankings. You also get:
  - The record: approval week by week, the economy, your laws and the milestones of your presidency.
  - An epilogue: what came after.
  - Your presidential library, with its galleries and its collection.
- **Achievements:** 32 of them, and a Hall of Fame of every presidency you finish.
- **A tour of the Oval Office** the first time you play.
- **Watch mode:** the game governs on its own at three speeds, and you take back the controls whenever you like. **Watch history** on the title screen hands a random era to the real president.
  - **Priorities** (new in version 2): before it starts, choose how the computer governs. Pick a preset (Steady hand, Big agenda, Deal-maker, Hawk, Dove or On the road) or set six sliders: legislative ambition (one bill at a time to four), bipartisanship, foreign policy (dove to hawk: how readily it uses force and how long it fights a war going badly), executive orders, travel and campaigning, and caution.
  - **Your agenda:** check the bills you want sent to Congress, in the order you want them. Otherwise the computer chooses.
  - **Stop for big decisions:** bills to sign or veto, shutdowns and the debt limit, crises and war, court rulings, scandals, storylines and the campaign. When one reaches your desk, watch mode waits for you, then carries on.
- **From Battleground 2028:** take office as the winner of a Battleground 2028 game. Their face, ratings and running mate come across, with the Congress the election produced and the economy they inherit. The desktop versions read the other game's saves on the same computer; the browser versions read them in the same browser and site.

## Coming in version 2

Version 2 arrives in stages, each one playable:

1. **The code, reorganized** (done). The game is now a multi-file Java project with one package per mechanic, tests and build tools (see [The code](#the-code)). This stage also fixes the bug that gave many cabinet members and staff the same surname.
2. **The transition and your team** (done). Ten weeks from Election Day to the inauguration to choose your cabinet and staff from shortlists, with vetting, leaks and early Senate whip counts. A new approval model with real presidents' range, from honeymoons in the 60s to lows in the 20s and rallies near 90%. Political capital gives way to relationships you build: goodwill with blocs, leaders and committee chairs, and favors owed.
3. **Policies and their real-world effects** (done). About 150 policies in 12 areas, changed through bills, executive orders, agency rules or the budget. Each changes measurable outcomes over months and years, and each has an impacts panel with winners, losers, timing, precedents and how sure the evidence is. A map of the whole system shows what affects what.
4. **Foreign policy and war** (done). Objectives with all 40 countries, a full set of tools from arms sales to covert action, wars of choice against any country, many ways to end a war, and realistic consequences (see [Statecraft and wars of choice](#statecraft-and-wars-of-choice-new-in-version-2)).
5. **News and runaway storylines** (done). 541 stories of their era and sixteen storylines that grow with your choices and can take over a presidency (see [The news and runaway storylines](#the-news-and-runaway-storylines-new-in-version-2)).
6. **Watch mode priorities and the 2029 setup** (done). Presets, sliders, a bill list and stops for big decisions in watch mode; sliders for the 2028 result, the deficit, the Court and three hot spots in 2029.
7. **New art.** Lit, textured rooms, detailed portraits, and a new interface with real typefaces.

## Screenshots

| | |
|---|---|
| ![Title](screenshots/title.png) | ![A new presidency](screenshots/new-presidency.png) |
| ![Choose your president](screenshots/choose-president.png) | ![The desk](screenshots/the-desk.png) |
| ![The country](screenshots/the-country.png) | ![The economy](screenshots/the-economy.png) |
| ![The residence](screenshots/the-residence.png) | ![The paper](screenshots/the-paper.png) |
| ![The Roosevelt Room](screenshots/the-roosevelt-room.png) | ![Drafting a bill](screenshots/drafting-a-bill.png) |
| ![The whip count](screenshots/whip-count.png) | ![The budget](screenshots/the-budget.png) |
| ![The midterms](screenshots/the-midterms.png) | ![A bill on your desk](screenshots/on-your-desk.png) |
| ![The staff](screenshots/the-staff.png) | ![The cabinet](screenshots/the-cabinet.png) |
| ![Orders and rules](screenshots/orders-and-rules.png) | ![The courts](screenshots/the-courts.png) |
| ![The vice president](screenshots/the-vice-president.png) | ![A scandal](screenshots/a-scandal.png) |
| ![The world](screenshots/the-world.png) | ![Hot spots](screenshots/hot-spots.png) |
| ![Operations](screenshots/operations.png) | ![Trade and sanctions](screenshots/trade-and-sanctions.png) |
| ![A crisis](screenshots/a-crisis.png) | ![A summit](screenshots/a-summit.png) |
| ![The daily brief](screenshots/daily-brief.png) | ![The press](screenshots/the-press.png) |
| ![A press conference](screenshots/press-conference.png) | ![Building a speech](screenshots/building-a-speech.png) |
| ![Your party](screenshots/your-party.png) | ![A domestic event](screenshots/a-domestic-event.png) |
| ![Every inauguration](screenshots/eras.png) | ![The tour](screenshots/the-tour.png) |
| ![Statecraft](screenshots/statecraft.png) | ![A war of choice](screenshots/a-war-of-choice.png) |
| ![The paper and its storylines](screenshots/the-paper-storylines.png) | ![A storyline](screenshots/a-storyline.png) |
| ![Watch mode priorities](screenshots/watch-mode.png) | ![The world you inherit in 2029](screenshots/setup-2029.png) |
| ![The world in 1942](screenshots/the-world-1942.png) | ![A war](screenshots/a-war.png) |
| ![The Hill in 1942](screenshots/the-hill-1942.png) | ![The cabinet in 1942](screenshots/the-cabinet-1942.png) |
| ![A turning point](screenshots/a-turning-point.png) | ![The campaign](screenshots/the-campaign.png) |
| ![Election night](screenshots/election-night.png) | ![The verdict](screenshots/the-verdict.png) |
| ![The record](screenshots/the-record.png) | ![The library](screenshots/the-library.png) |
| ![The epilogue](screenshots/the-epilogue.png) | ![Achievements](screenshots/achievements.png) |

## Notes

- Real people appear as stylised characters. Their ratings are the game's estimates from public records, not facts, and the game never quotes them. In the historical eras the presidents, contenders, congressional leaders, justices and foreign leaders are real; cabinet members, committee chairs, reporters and other officials are fictional.
- Only fictional officials are ever caught up in personal misconduct. Real justices only ever retire.
- Reporters are fictional, even when the outlet is real. Outlets' coverage is a number the game simulates, never a quote.
- Everything that happens in a game is fiction.
- The 2029 era is a projection from the world of Battleground 2028, not a forecast. Its foreign leaders are today's, carried forward; the three marked as fictional successors (in France, Cuba and the Philippines) are invented.
- Historians' rankings of the real presidents are from the 2021 C-SPAN Presidential Historians Survey. Where your president lands among them is the game's estimate, not the historians'.
- What happens after an era begins is alternate history: the turning points arrive on their real dates, but the outcomes are yours.
- Country outlines are from Natural Earth via the world-atlas package. Flags are stylised; those of the past are simplified and carry no emblems.
- The browser version is generated from the Java source, so the two play the same game.

## The code

The game is plain Java 8 with Swing, and it uses no outside libraries. The source is a multi-file project:

```
src/main/java/resolutedesk/   the game, one package per mechanic: economy, opinion, congress, executive,
                              world, history, home, press, campaign, legacy, sim (the turn), ui and more
src/test/java/resolutedesk/   tests for each mechanic and whole-presidency simulations
tools/Build.java              the build: compile, test, bundle, translate, package
tools/bundle/                 merges the project into the one-file BlueJ version
tools/j2js/                   translates that file into the browser version
tools/browser-tests/          Playwright scripts that play the browser version
docs/ARCHITECTURE.md          how it fits together
```

With a JDK (11 or newer) and nothing else:

```
java tools/Build.java test          the unit tests and simulations
java tools/Build.java run           play from source
java tools/Build.java dist          build the one-file version, the browser version and the jar
java tools/Build.java equivalence   check that Java and the browser version play identically (needs Node)
```

The [architecture notes](docs/ARCHITECTURE.md) describe the game state, the weekly turn, and the pipeline from the project to the one-file and browser versions.
