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

This is the second of six planned updates. The first covered the core of the job: your time, your health, the economy and public opinion. The second adds Congress and the budget.

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

## Coming next

1. **The executive branch:**
   - The cabinet and its confirmation votes.
   - A named staff with loyalty, leaks and turnover.
   - Executive orders, regulations, the courts and judicial appointments.
   - Pardons, emergency powers and oversight.
   - Scandals and impeachment.
2. **The world:**
   - About 40 countries and their leaders.
   - Calls, summits, treaties, aid and sanctions.
   - The Situation Room and covert action.
   - Alliances, plus China and Taiwan, Russia and Europe, and the Middle East.
3. **Crises, the press and public life:**
   - The President's Daily Brief.
   - Disasters and pandemics, plus AI, immigration, protests and the hot-button issues.
   - Speeches, the State of the Union, press conferences, interviews and social media.
   - Party factions and an opposition with plans of its own.
4. **History and the verdict:**
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

## Notes

- Real people appear as stylised characters. Their ratings are the game's estimates from public records, not facts, and the game never quotes them.
- Everything that happens in a game is fiction.
- The 2029 era is a projection from the world of Battleground 2028, not a forecast.
- The browser version is generated from the Java source, so the two play the same game.
