# How Resolute Desk is built

Resolute Desk is a turn-based simulation of the American presidency, written in plain Java 8 with
Swing for the interface and no outside libraries. The same source runs in three forms:

- the multi-file project in `src/main/java`, which is the source of truth;
- one generated file, `dist/ResoluteDesk.java`, for BlueJ;
- a browser version, `index.html`, translated from that one file into JavaScript.

## Layout

```
src/main/java/resolutedesk/
  ResoluteDesk.java   the window: input, screen dispatch, the widget toolkit and main()
  core/               numbers, formatting, dates and seeds
  model/              the state of a game: Game, Settings, Econ, Cand, Era, News and their constants
  data/               tables of real-world data: states, candidates, eras, electoral votes, names
  sim/                the turn: setup, the week, the president's schedule, decisions, the hands-off president
  transition/         the weeks from Election Day to the inauguration: shortlists, vetting, leaks, the grade
  economy/            the weekly economic model
  opinion/            the electorate and approval
  congress/           members, blocs, bills, legislation, relationships and favors, the budget, the midterms
  policy/             about 150 policies, the channels that change them, and the outcomes they produce
  executive/          staff and cabinet, orders and rules, the courts, scandals, the vice president
  world/              countries, diplomacy, summits, crises, operations, statecraft, objectives and wars
  history/            eras and the turning points of history
  home/               events at home, disasters, the president's party and the opposition
  press/              press conferences, speeches, posts, polls and news outlets
  campaign/           terms, re-election and election night results
  legacy/             the historians' verdict, the library, achievements and the Hall of Fame
  io/                 saving and loading, and reading Battleground 2028 saves
  ui/                 every screen and room, portraits, maps, fonts and the palette
  diagnostics/        command-line data checks, simulations and screenshots
src/test/java/resolutedesk/
  testkit/            a small test runner, assertions and helpers (no outside libraries)
  <package>/          tests for each mechanic, in the package they test
tools/
  Build.java          compile, test, bundle, translate and package
  bundle/             merges the project into the one-file BlueJ version
  j2js/               the Java-to-JavaScript translator and its browser runtime
  browser-tests/      Playwright scripts that drive the browser version
```

## The game state and the systems that move it

A presidency is one object, `model.Game`. It holds the calendar, the president and running mate,
approval and its breakdowns, and the state of each area: `Econ`, `Congress`, `Exec` (the executive
branch), `World`, `Home` and `Reelect`. Everything in it is serializable, so a saved game is just
`Game` written with `ObjectOutputStream`.

The game state is plain data with public fields. The behaviour lives in stateless systems, one per
mechanic, that take the `Game` and move it forward: `Economy.week(g)`, `Approval.week(g)`,
`Legis.week(g)`, `Cabinet.week(g)`, `Diplo.week(g)`, `History.week(g)` and so on. This split keeps
the save format independent of the logic, and lets the tests drive any system on its own.

One turn is one week. `sim.Week.resolve` plays the week the player planned (21 slots of policy time,
trips, speeches, meetings, fundraisers and rest), then advances each system in a fixed order:

```
schedule -> economy -> approval -> Congress (bills, budget, midterms, re-election)
         -> executive (cabinet, actions, courts, scandals) -> world -> history -> home front
         -> news, the weekly record, and the end-of-term check
```

A new presidency can begin before the inauguration. With the transition on, `sim.Setup` builds the game as
of Inauguration Day, then `transition.Transitions.begin` empties the jobs and draws a shortlist for each. The
transition has its own week counter and its own random stream, so the choices made in it don't change the
presidency's random draws. `Setup.inaugurate` hands the picks to the executive branch and starts week 0.

Policies (`policy.Policies`) run right after the economy each week. A change made by law, order,
rule or budget moves a policy's level; the effect works through over the policy's lag into the
economy (trend growth, unemployment, inflation, oil and the deficit) and into the outcomes the
policy model keeps itself (coverage, poverty, crime, overdoses, the border, emissions and test
scores). Orders and rules travel as `executive.Action`s, so they can be sued and overturned like
any other; bills are ordinary `congress.Bill`s whose definition is generated from the policy.

Things that need the president come back as `sim.Decision`s in the inbox, each with options,
advisers' views and odds. `sim.Decisions` knows what every kind of decision does, and how the
hands-off president would choose, which is what watch mode and the simulations use.

## Two models worth knowing

**Approval** (`opinion.Approval`) heads each week toward a target in two parts. The fundamentals are the
era's baseline, the economy against what people of the era expected, and the issues. Events come on top:
the honeymoon (bigger after a bigger win), the record of wins and failures, scandal, the wear of office and a
slowly drifting public mood, each fading at its own rate. Partisanship then squeezes the total. Since about
1990 there is a soft floor near 30 and a ceiling near 70, tighter as polarization rises, and a floor that
levels off rather than giving way. Before 1990 there is no floor. Rallies are added after the squeeze, so
they break through the ceiling.

**Wars** (`history.History` and `world.Wars`) share one model. Each `world.War` has a front from 0 (defeat)
to 100 (victory), troops against what the generals ask for, a strategy, casualties, support at home and
progress in talks. History's wars come from a table; a war of choice is made by `Wars.start` and carries a
kind (strikes, blockade, no-fly zone, invasion or occupation). Its pace falls with the enemy's military
power, and a nuclear power or a great-power protector all but stops it. Support settles toward the war's
legitimacy at the start (authorized or not, against an enemy or a friend), less about six points per
tenfold rise in deaths, and an unpopular war drags on approval every week. An invasion that reaches 100
topples the government (`world.Statecraft.topple`) and becomes an occupation with a new front.

**Relationships with Congress** (`congress.Relations`) replace political capital. Nothing is spent but time:
each deal takes an hour of the president's week, and the hours come from Hill meetings on the calendar.
Goodwill lives on blocs and members, favors and promises in `congress.Favor`, and the mandate from the
election margin adds to the vote for the president's bills, fading over the first year of a term.

## Holders: constants, tables and helpers

Most packages have a few final classes of static members, for example `ui.Theme` (the palette),
`congress.BillData` (the bills a president can propose) or `core.Fmt` (number formatting). The rest
of the code reaches them through static imports, so a bill table reads as `BILLS` and a colour as
`C_GOLD`. The bundler relies on this convention (see below), so a holder is only ever used through
`import static`.

## Determinism

Every random choice comes from the game's own `Random`, seeded from the settings, or from a local
`Random` seeded with `core.Seeds.mix(...)` (for things that must stay the same within a month, such
as a job's shortlist). The same settings always give the same presidency. The tests rely on this,
and so does the equivalence check between the Java and browser versions.

## The one-file version

BlueJ wants one file, and the translator works on one file, so `tools/bundle/Bundler.java` merges
the project:

- `ResoluteDesk` becomes the single top-level class, and every other class a static nested class.
- Holders are merged into the top-level class itself, so their members keep working by their simple
  names. A qualified reference such as `Draw.chart(...)` becomes `ResoluteDesk.chart(...)`.
- Long array initializers move into methods of their own, so the top-level class's static
  initializer stays under Java's 64 KB limit for one method.
- Units are placed so that each comes after the classes its static initializers use. Java doesn't
  need this, because it initializes classes lazily, but the browser runtime initializes them in
  file order.

## The browser version

`tools/j2js/J2JS.java` reads the one-file version with javac's own parser and type checker, and
writes JavaScript. Working from attributed trees, it gets the Java semantics right where they differ
from JavaScript: integer division, string conversion and overloaded methods. The hand-written runtime in
`tools/j2js/runtime/` supplies the parts of the JDK, AWT and Swing the game uses, drawing on an
HTML canvas. Saves use IndexedDB instead of files. See `tools/j2js/README.md`.

`java tools/Build.java equivalence` runs the same simulations in Java and, under Node, in the
browser build, and checks that the two print identical results line for line.

## Tests

`java tools/Build.java test` compiles the tests and runs them with `testkit.TestRunner`, which
finds every class named `*Test` and runs its `@Test` methods. There are unit tests for each
mechanic, and simulations of whole presidencies in several eras. The simulations check that a
presidency ends properly, keeps its record and stays within realistic bounds.

The browser scripts in `tools/browser-tests` need Node and Playwright. They click through the
browser version the way a player would.
