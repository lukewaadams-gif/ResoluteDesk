# Resolute Desk

A presidency simulator. You take the oath on January 20 and govern, a week at a time, from the Oval Office.

Resolute Desk is a separate game from [Battleground 2028](https://github.com/lukewaadams-gif/Battleground2028), which is about winning the White House. This one is about the job itself.

![The Oval Office](screenshots/oval-office.png)

## How to play it

- **In a browser:** download `index.html` and open it. No install, no internet needed. Saves stay in that browser.
- **As a desktop app:** with Java 8 or newer, double-click `ResoluteDesk.jar`, or run `java -jar ResoluteDesk.jar`.
- **In BlueJ:** create a project, add `ResoluteDesk.java` (Edit > Add Class from File), compile, and run `main`.

The game autosaves every week. Use **Continue** on the title screen to pick up where you left off.

## What's in it so far

This is the fifth of six planned updates. The first covered the core of the job: your time, your health, the economy and public opinion. The second added Congress and the budget, the third the executive branch, the fourth the world, and the fifth the home front and the press.

- **When you take office:**
  - **2025**, the real present. Donald Trump, or anyone else as a what-if.
  - **2029**, in the world of Battleground 2028.
  - Each era has its own Congress, economy, Fed chair, congressional leaders and the issues people care about.
- **Scenarios and custom conditions:**
  - Five twists on the real starting point: a recession, a war abroad, divided government, a landslide mandate, or an inflation shock.
  - Set approval, unemployment, inflation, growth and your party's seats yourself.
  - Easy, Normal and Hard.
- **Your president and running mate:** pick from about 35 real politicians, or create your own. You choose the name, party, home state, age and background, and spend points on strengths.
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
  - National approval follows the economy, prices, the honeymoon and your work, squeezed by modern partisanship.
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
  - Political capital: you earn it with approval and wins, and spend it on deals.
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

## Coming next

1. **History and the verdict:**
   - Every inauguration from 1933 on.
   - A playable re-election campaign and a second term.
   - The epilogue, historians' ranking and your presidential library.
   - Achievements, a tutorial and a watch mode.
   - Importing a president from Battleground 2028.

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

## Notes

- Real people appear as stylised characters. Their ratings are the game's estimates from public records, not facts, and the game never quotes them.
- Only fictional officials are ever caught up in personal misconduct. Real justices only ever retire.
- Reporters are fictional, even when the outlet is real. Outlets' coverage is a number the game simulates, never a quote.
- Everything that happens in a game is fiction.
- The 2029 era is a projection from the world of Battleground 2028, not a forecast. Its foreign leaders are today's, carried forward; the three marked as fictional successors (in France, Cuba and the Philippines) are invented.
- Country outlines are from Natural Earth via the world-atlas package. Flags are stylised.
- The browser version is generated from the Java source, so the two play the same game.
