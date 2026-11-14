# Configuration

toolarium-java-decompiler provides a comprehensive configuration system with sensible defaults.
Configuration can be set programmatically via the fluent builder API, or loaded from a
`.properties` file for CLI use.


## Builder API

```java
IDecompilerConfiguration config = DecompilerConfigurationBuilder.builder()
    .indentString("\t")
    .showBanner(true)
    .removeSyntheticMembers(false)
    .includeClassRegex("com\\.example\\..*")
    .build();

// Use with DecompilerBuilder
DecompilerBuilder.builder()
    .input(Path.of("mylib.jar"))
    .output(Path.of("output/"))
    .configuration(config)
    .decompile();

// Or with DecompilerFactory
IDecompiler decompiler = DecompilerFactory.getInstance().getDecompiler(config);
String source = decompiler.decompile(classBytes);
```


## Properties file

Create a `.properties` file with the options you want to override. Only properties that
are present will override the defaults.

```properties
# Output formatting
indent.string = \\t
line.separator = \\n
show.banner = true
show.source.file = true

# Decompilation features
remove.bridge.methods = true
remove.synthetic.members = true
reconstruct.assert = true
reconstruct.enum = true
reconstruct.inner.classes = true
reconstruct.lambda = true
reconstruct.try.with.resources = true
reconstruct.string.switch = true
reconstruct.record = true
reconstruct.text.blocks = true
hide.default.constructor = true
hide.empty.super = true
show.override.annotation = false

# Filtering
include.class.regex = com\\.example\\..*
exclude.class.regex = .*\\$\\d+

# Error handling
ignore.invalid.bytecode = false
max.method.time.seconds = 0
```


### CLI usage with properties file

```bash
java -jar toolarium-java-decompiler.jar -c decompiler.properties -o output/ mylib.jar
```


### Loading in code

```java
IDecompilerConfiguration config = DecompilerConfigurationBuilder.builder()
    .loadFromFile(Path.of("decompiler.properties"))
    .build();

// Or from a Properties object
Properties props = new Properties();
props.setProperty("indent.string", "\\t");
props.setProperty("show.banner", "true");

IDecompilerConfiguration config = DecompilerConfigurationBuilder.builder()
    .loadFromProperties(props)
    .build();
```


## Option reference

### Output formatting

| Property | Type | Default | Description |
|---|---|---|---|
| `indent.string` | String | `"    "` (4 spaces) | Indentation string for each nesting level. Use `\t` for tabs. |
| `line.separator` | String | `"\n"` | Line separator in emitted source. Use `\r\n` for Windows line endings. |
| `show.banner` | boolean | `false` | Emit `// Decompiled by toolarium-java-decompiler` at the top of each file. |
| `show.source.file` | boolean | `false` | Emit `// Source: ClassName.java` comment at the top of each file. |


### Decompilation features

| Property | Type | Default | Description |
|---|---|---|---|
| `remove.bridge.methods` | boolean | `true` | Remove compiler-generated bridge methods (generic type erasure). |
| `remove.synthetic.members` | boolean | `true` | Remove synthetic fields and methods (inner class access, etc.). |
| `reconstruct.assert` | boolean | `true` | Reconstruct `assert` statements from `$assertionsDisabled` patterns. |
| `reconstruct.enum` | boolean | `true` | Reconstruct `enum` declarations from compiler-generated code. |
| `reconstruct.inner.classes` | boolean | `true` | Nest inner classes inside their enclosing class during batch decompilation. |
| `reconstruct.lambda` | boolean | `true` | Reconstruct lambda expressions and inline `lambda$` method bodies. |
| `reconstruct.try.with.resources` | boolean | `true` | Reconstruct `try-with-resources` from `close()`/`addSuppressed()` patterns. |
| `reconstruct.string.switch` | boolean | `true` | Reconstruct `switch(String)` from `hashCode()`/`equals()` patterns. |
| `reconstruct.record` | boolean | `true` | Reconstruct `record` declarations from compiler-generated code. |
| `reconstruct.text.blocks` | boolean | `true` | Emit multi-line strings as text blocks (`"""..."""`). |
| `hide.default.constructor` | boolean | `true` | Hide the no-arg constructor when it has an empty body. |
| `hide.empty.super` | boolean | `true` | Hide redundant `super()` calls in constructors. |
| `show.override.annotation` | boolean | `false` | Add `@Override` annotation on methods that override a superclass or implement an interface method. |


### Filtering

| Property | Type | Default | Description |
|---|---|---|---|
| `include.class.regex` | String | `null` | Only decompile classes whose fully qualified name matches this regex. |
| `exclude.class.regex` | String | `null` | Skip classes whose fully qualified name matches this regex. |

Regex patterns are matched against the dot-separated fully qualified class name (e.g., `com.example.MyClass`).
Include is checked first: if set, only matching classes are decompiled. Exclude is then applied to
further narrow the set. Both patterns use standard Java regex syntax.


### Error handling

| Property | Type | Default | Description |
|---|---|---|---|
| `ignore.invalid.bytecode` | boolean | `false` | Continue decompilation when invalid bytecode is encountered. |
| `max.method.time.seconds` | int | `0` | Maximum seconds per method (0 = no limit). Timed-out methods emit a comment. |


## Default values

All defaults match the standard decompiler behavior from previous versions. Upgrading to the
configuration system requires no changes to existing code: calling `DecompilerFactory.getInstance().getDecompiler()`
or `DecompilerBuilder.builder().build()` without a configuration uses all defaults.
