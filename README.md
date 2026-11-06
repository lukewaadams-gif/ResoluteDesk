# Resolute Desk

A presidency simulator. You take the oath on January 20 and govern, a week at a time, from the Oval Office.

Resolute Desk is a separate game from [Battleground 2028](https://github.com/lukewaadams-gif/Battleground2028), which is about winning the White House. This one is about the job itself.

![The Oval Office](screenshots/oval-office.png)

## How to play it

- **In a browser:** download `index.html` and open it. No install, no internet needed. Saves stay in that browser.
- **As a desktop app:** with Java 8 or newer, double-click `ResoluteDesk.jar`, or run `java -jar ResoluteDesk.jar`.
- **In BlueJ:** create a project, add `ResoluteDesk.java` (Edit > Add Class from File), compile, and run `main`.

The game autosaves every week. Use **Continue** on the title screen to pick up where you left off.

## What's in this first version

This is the first of six planned updates. It covers the core of the job: your time, your health, the economy and public opinion.

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

## Coming next

1. **Congress and the budget:**
   - Real leaders and fictional rank and file.
   - Committees and swing votes.
   - Bills negotiated provision by provision.
   - The filibuster, reconciliation and the Byrd rule.
   - Your budget, the 12 spending bills, shutdowns and the debt ceiling.
   - Vetoes and the midterms.
2. **The executive branch:**
   - The cabinet and its confirmation votes.
   - A named staff with loyalty, leaks and turnover.
   - Executive orders, regulations, the courts and judicial appointments.
   - Pardons, emergency powers and oversight.
   - Scandals and impeachment.
3. **The world:**
   - About 40 countries and their leaders.
   - Calls, summits, treaties, aid and sanctions.
   - The Situation Room and covert action.
   - Alliances, plus China and Taiwan, Russia and Europe, and the Middle East.
4. **Crises, the press and public life:**
   - The President's Daily Brief.
   - Disasters and pandemics, plus AI, immigration, protests and the hot-button issues.
   - Speeches, the State of the Union, press conferences, interviews and social media.
   - Party factions and an opposition with plans of its own.
5. **History and the verdict:**
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

## Notes

- Real people appear as stylised characters. Their ratings are the game's estimates from public records, not facts, and the game never quotes them.
- Everything that happens in a game is fiction.
- The 2029 era is a projection from the world of Battleground 2028, not a forecast.
- The browser version is generated from the Java source, so the two play the same game.
