# Browser tests

These scripts drive the browser version (`index.html`) in headless Chromium the way a player would:
they click buttons by their labels, play weeks, and check the game's state through the
`window.__rd` handle the page exposes. Each one prints what it checked, saves screenshots, and ends
by counting the JavaScript errors the page raised, which should be zero.

| Script | Covers |
| --- | --- |
| `core.js` | a new presidency, planning the week, a trip, the paper, saving and resuming |
| `congress.js` | drafting a bill, working the votes, signing or vetoing, the budget and the midterms |
| `executive.js` | hiring staff, nominations, an executive order, the vice president and delegation |
| `world.js` | the Situation Room: calls, visits, a summit, tariffs, an operation, covert action and a trip abroad |
| `press.js` | a press conference, an interview, a post, a poll, an address and the daily brief |
| `transition.js` | the transition: interviews, background checks, floating and announcing picks, saving and resuming, and the oath |
| `policy.js` | the Cabinet Room: a policy and its impacts, a bill, an executive order, outcomes and the system map |
| `statecraft.js` | statecraft and a war of choice: military aid, an objective, covert action, a vote in Congress, an invasion, a timetable, saving and resuming |
| `stories.js` | the era's news: the paper's sections, a storyline's decision, the developing-stories panel, the daily brief, saving and resuming |
| `history.js` | a 1961 presidency: the tour, a war, re-election, election night, the verdict, the Hall of Fame, the import screen and watch mode |

They need Node and Playwright (`npm install -g playwright`):

```
NODE_PATH=$(npm root -g) node tools/browser-tests/history.js /tmp/shots
```

Set `RD_HTML` to test a build other than the `index.html` at the root, for example
`RD_HTML=build/index.html`.
