# J2JS: the browser version, from the Java source

`J2JS.java` translates the one-file version of the game (`build/ResoluteDesk.java`, made by the
bundler) into JavaScript. The browser version is that JavaScript plus a hand-written runtime, in
one self-contained `index.html` that runs from disk or any web host.

Build it with `java tools/Build.java web`, which bundles the sources, compiles and runs the
translator, and assembles `build/index.html`.

## How it works

The translator uses javac's compiler API, which is why it needs `--add-exports` for
`jdk.compiler`. It parses and type-checks the one file exactly as javac does, then walks the
attributed syntax trees. Because every expression carries its Java type, the output keeps Java's
behaviour where JavaScript's differs:

- `int` and `long` division truncate, and `/` on two integers becomes `$idiv(a, b)`.
- String concatenation converts numbers and characters the way Java does.
- Overloaded methods get distinct names (`button$8` for the eight-argument `button`).
- Static fields become properties of their class, and static methods of the top-level class become
  functions.
- `ArrayList` and `List` become JavaScript arrays with the Java methods added, `HashMap` a small
  wrapper around `Map`, and `java.util.Random` a faithful port of its 48-bit generator, so a seed
  gives the same game in both versions.

The output has four sections, written to separate files: the classes, a registry of class names
(used for saving and loading), the top-level functions, and the static initializers in source
order.

## The runtime

The files in `runtime/` are wrapped around the generated code:

- `head.js`: the page, and the parts of Java and AWT the game uses: collections, strings,
  `Random`, `Color` (with `brighter` and `darker`), `Font`, `Graphics2D` drawing onto a canvas (shapes, gradients, transforms, text
  metrics), and the Swing timer and input events.
- `mid.js`: browser versions of the few classes that touch the operating system. Sound plays through
  Web Audio. Saves go to IndexedDB and the Hall of Fame to local storage. Battleground 2028 saves
  are read from that game's IndexedDB store when both games are served from the same site.
- `tail.js`: start-up and the main loop.

## What the Java code must avoid

The translator covers the Java the game uses, not all of Java. The game code keeps to these rules,
and `tools/Build.java` checks the first one:

- Source is plain ASCII. Use escapes such as `é` for anything else.
- Declare arrays as `int[] a`, not `int a[]`.
- Keep arrays of one element type; no `Object[]` tables mixing numbers and strings.
- `String.split` and `replaceAll` patterns run as JavaScript regular expressions, so use the
  syntax the two share.
- `String.format` supports the flags and conversions the game uses (`%d`, `%.1f`, `%s`, `%%`,
  widths); grouping with `%,d` isn't supported, so use `Fmt.thousands`.
- `AffineTransform` is supported for translate, rotate and scale, not the six-number constructor.

## Checking the translation

`noderun.js` runs the browser build without a browser, under Node, and passes command-line
arguments to the game's diagnostics: `node tools/j2js/noderun.js build/index.html "test sim 1 1961 - 0 420"`.

`java tools/Build.java equivalence` uses it to run the same simulations in Java and JavaScript and
compare their output line for line.
