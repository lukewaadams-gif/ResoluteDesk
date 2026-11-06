# The bundler

`Bundler.java` merges the multi-file project into the single file `ResoluteDesk.java`, which is
both the BlueJ version of the game and the input of the browser translator.

```
java tools/bundle/Bundler.java src/main/java build/ResoluteDesk.java
```

`java tools/Build.java bundle` runs it and compiles the result. The rules it follows are described
in `docs/ARCHITECTURE.md`. In short, `resolutedesk.ResoluteDesk` becomes the top-level class.
Classes used through static imports are merged into it, and every other class becomes a static
nested class. Each unit is placed after the units its static initializers depend on.

Java compiles all of a class's static initializers into one method, which may hold at most 64 KB of
bytecode, and merging every holder into one class would overflow it. So any static array field
whose initializer runs past 1,500 characters is rewritten to call a method that builds the array:
`static final String[] ROWS = {...};` becomes `static final String[] ROWS = dataROWS();`. The
fields keep their names and their order of initialization.

The bundler fails loudly rather than guessing. It stops if a file's name doesn't match its class,
if a file has no public top-level type, or if holder classes import each other in a cycle.
