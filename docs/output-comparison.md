# Output Comparison

This page compares the output of several Java decompilers on the same compiled bytecode.

## Decompilers Compared

| Decompiler | Version | Notes |
|---|---|---|
| **toolarium-java-decompiler** | 0.8.0-SNAPSHOT | This library |
| **Vineflower** | 1.11.1 | Fork of Quiltflower/FernFlower |
| **Fernflower** | JetBrains (7261c6b) | IntelliJ IDEA's built-in decompiler |
| **CFR** | 0.152 | Charles Mayfield's decompiler |
| **Procyon** | 0.6.0 | Mike Strobel's decompiler |

All examples were compiled with `javac` (Java 21) and decompiled with default settings.
Source files are in the project's test resources.


## 1. If/Else Chains

A method with sequential `if`/`else if`/`else` returning different strings.

### Source

```java
public String classify(int value) {
    if (value < 0) {
        return "negative";
    } else if (value == 0) {
        return "zero";
    } else if (value <= 10) {
        return "small";
    } else {
        return "large";
    }
}
```

### toolarium-java-decompiler

```java
public String classify(int value) {
    if (value < 0) {
        return "negative";
    } else if (value == 0) {
        return "zero";
    } else if (value <= 10) {
        return "small";
    } else {
        return "large";
    }
}
```

**Notes:** Conditions are correct (`< 0`, `== 0`, `<= 10`). Variable names are recovered
from the LocalVariableTable. The `else if` chain is correctly reconstructed from the
sequential `if`/`return` bytecode pattern.

### Vineflower, Fernflower, CFR, Procyon

All four produce output that closely matches the original source, preserving the `else if`
structure, correct condition operators, and original variable names from the LocalVariableTable.


## 2. While Loop

A simple while loop computing a factorial.

### Source

```java
public int whileLoop(int n) {
    int result = 1;
    while (n > 1) {
        result *= n;
        n--;
    }
    return result;
}
```

### toolarium-java-decompiler

```java
public int whileLoop(int n) {
    int result = 1;
    while (n > 1) {
        result *= n;
        n--;
    }
    return result;
}
```

**Notes:** The while loop is correctly reconstructed from the bytecode pattern
(conditional forward jump + backward GOTO). Condition direction, variable names,
and compound assignment (`*=`) are all correct.

### Vineflower, Fernflower, CFR, Procyon

All four correctly reconstruct the `while` loop with the original structure
and compound assignment (`result *= n`).


## 3. Switch Statement (int)

A switch on `int` with fall-through cases.

### Source

```java
public String dayType(int day) {
    switch (day) {
        case 1:
        case 7:
            return "weekend";
        case 2: case 3: case 4: case 5: case 6:
            return "weekday";
        default:
            return "unknown";
    }
}
```

### toolarium-java-decompiler

```java
public String dayType(int day) {
    switch (day) {
        case 1:
        case 7:
            return "weekend";
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
            return "weekday";
        default:
            return "unknown";
    }
}
```

**Notes:** The switch structure is now correctly reconstructed from the `tableswitch`
bytecode instruction. Case values that share the same target are grouped together.
Variable names are recovered from the LVT.

### Vineflower, Fernflower, CFR, Procyon

All four reconstruct the switch with correct case grouping and fall-through semantics.


## 4. Switch Statement (String)

A switch on `String` values, which compiles to `hashCode()`/`equals()` checks.

### Source

```java
public int calculate(String op, int a, int b) {
    switch (op) {
        case "add": return a + b;
        case "sub": return a - b;
        case "mul": return a * b;
        case "div":
            if (b == 0) {
                throw new ArithmeticException("division by zero");
            }
            return a / b;
        default:
            throw new IllegalArgumentException("unknown op: " + op);
    }
}
```

### toolarium-java-decompiler

```java
public int calculate(String op, int a, int b) {
    switch (op) {
        case "add":
            return a + b;
        case "sub":
            return a - b;
        case "mul":
            return a * b;
        case "div":
            if (b == 0) {
                throw new ArithmeticException("division by zero");
            }
            return a / b;
        default:
            throw new IllegalArgumentException("unknown op: " + op);
    }
}
```

**Notes:** The two-phase String switch pattern (`hashCode()` lookup and `equals()` dispatch)
is correctly merged back into a single `switch(op)` with String case values. Variable names
recovered from LVT. String concatenation in the exception message is correctly reconstructed.

### Vineflower, CFR

Correctly reconstruct the `switch` on String, including the "div" case with the guard clause.

### Fernflower, Procyon

Reconstruct the switch but may produce slightly different formatting for the multi-statement
"div" case.


## 5. Field Initializers and Constants

Static final fields with compile-time constant values (ConstantValue attribute).

### Source

```java
public class FieldInitializers {
    public static final int MAX_SIZE = 1024;
    public static final String DEFAULT_NAME = "unnamed";
    public static final double PI = 3.14159265358979;
    public static final boolean ENABLED = true;
    private int count;

    public int getCount() { return count; }

    public void increment() {
        if (count < MAX_SIZE) {
            count++;
        }
    }
}
```

### toolarium-java-decompiler

```java
public static final int MAX_SIZE = 1024;
public static final String DEFAULT_NAME = "unnamed";
public static final double PI = 3.14159265358979d;
public static final boolean ENABLED = true;
private int count;

public int getCount() {
    return this.count;
}

public void increment() {
    if (this.count < 1024) {
        this.count++;
    }
    return;
}
```

**Notes:** Condition direction is correct (`< 1024`). Boolean constant `true` is correctly
recovered from the integer value stored in bytecode. Increment `count++` is reconstructed
from the compound assignment pattern. Remaining issues: constant name `MAX_SIZE` is replaced
by the inlined value `1024` in the method body (javac inlines compile-time constants).
Field access uses explicit `this.` prefix.

### Vineflower, Fernflower, CFR, Procyon

All four correctly recover `true` for the boolean field, use the constant name in expressions
where the compiler inlined it, and preserve the original condition direction.


## 6. Arithmetic and Bitwise Expressions

Compound arithmetic with shifts, modulo, and local variables.

### Source

```java
public int arithmetic(int a, int b) {
    int sum = a + b;
    int product = a * b;
    int shifted = sum << 2;
    return shifted + product - (a % b);
}
```

### toolarium-java-decompiler

```java
public int arithmetic(int a, int b) {
    int sum = a + b;
    int product = a * b;
    int shifted = sum << 2;
    return shifted + product - a % b;
}
```

**Result:** Correct! All arithmetic operations, shifts, and operator precedence are preserved.
Variable names are now recovered from the LVT (`a`, `b`, `sum`, `product`, `shifted`).
Parentheses are only emitted where needed for correct precedence.

### Vineflower, Fernflower, CFR, Procyon

All four produce equivalent output with original variable names.


## 7. Enum with Constructor

An enum with a constructor parameter, accessor, and instance method.

### Source

```java
public enum EnumExample {
    RED("FF0000"),
    GREEN("00FF00"),
    BLUE("0000FF");

    private final String hex;

    EnumExample(String hex) {
        this.hex = hex;
    }

    public String getHex() { return hex; }
    public boolean isWarm() { return this == RED; }
}
```

### toolarium-java-decompiler

```java
public enum EnumExample {
    RED("FF0000"),
    GREEN("00FF00"),
    BLUE("0000FF");

    private final String hex;

    public String getHex() {
        return this.hex;
    }

    public boolean isWarm() {
        return this == EnumExample.RED;
    }
}
```

**Notes:** The `enum` keyword, constant declarations, and constructor arguments are
correctly reconstructed. The `isWarm()` method is simplified from `if/return true/false`
to `return cond`. Synthetic methods (`values()`, `valueOf()`, `$VALUES`, `<clinit>`) and
the constructor are correctly hidden.

### Vineflower, Fernflower, CFR, Procyon

All four reconstruct the `enum` keyword, constant declarations with constructor arguments,
and hide the synthetic parameters. Vineflower and CFR also correctly simplify `isWarm()`.


## 8. Assert Statements

Methods using `assert` with and without messages.

### Source

```java
public int divide(int a, int b) {
    assert b != 0 : "divisor must not be zero";
    return a / b;
}

public void process(String input) {
    assert input != null;
    assert !input.isEmpty() : "input must not be empty";
    System.out.println(input.toUpperCase());
}
```

### toolarium-java-decompiler

```java
public int divide(int a, int b) {
    assert b != 0 : "divisor must not be zero";
    return a / b;
}

public void process(String input) {
    assert input != null;
    assert !input.isEmpty() : "input must not be empty";
    System.out.println(input.toUpperCase());
    return;
}
```

**Notes:** Assert statements are correctly reconstructed from the bytecode pattern. The
`$assertionsDisabled` field is removed, and the inner condition is negated to express the
positive invariant (e.g., `b != 0` instead of the bytecode's trigger condition `b == 0`).
`java.lang.System` is simplified to `System`. Variable names recovered from LVT.

### Vineflower, CFR, Procyon

All three correctly reconstruct `assert` statements from the bytecode pattern.

### Fernflower

Reconstructs assert in most cases but may show the raw pattern for complex assertion messages.


## 9. String Concatenation

String concatenation with mixed types.

### Source

```java
public String concatenation(String first, String last, int age) {
    return first + " " + last + " (age: " + age + ")";
}
```

### toolarium-java-decompiler

```java
public String concatenation(String first, String last, int age) {
    return first + " " + last + " (age: " + age + ")";
}
```

**Notes:** String concatenation is now correctly reconstructed from the
`makeConcatWithConstants` recipe template. All literal separators (`" "`, `" (age: "`, `")"`)
are preserved. Variable names recovered from LVT. Parentheses are only emitted where needed
for correct operator precedence.

### Vineflower, Fernflower, CFR, Procyon

All four correctly reconstruct the full concatenation expression with all string literals.


## 10. Try-with-resources

A method using try-with-resources for automatic resource management.

### Source

```java
public String readFirstLine(String path) throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader(path))) {
        return br.readLine();
    }
}
```

### toolarium-java-decompiler

```java
public String readFirstLine(String path) throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader(path))) {
        return br.readLine();
    }
}
```

**Notes:** The try-with-resources pattern is correctly reconstructed from the compiler-generated
close()/addSuppressed() exception handling code. The resource declaration is moved into the
try parentheses, and the synthetic finally block and Throwable catch clause are removed.

### Vineflower, CFR, Procyon

All three correctly reconstruct the `try`-with-resources syntax.

### Fernflower

Produces the expanded form with explicit close() calls in the finally block instead of
reconstructing the try-with-resources syntax.


## 11. Lambda Expressions

Lambda expressions compiled via `invokedynamic` with `LambdaMetafactory` bootstrap.

### Source

```java
public void processItems(List<String> items) {
    items.forEach(item -> System.out.println(item));
    items.sort((a, b) -> a.compareToIgnoreCase(b));
    Runnable task = () -> System.out.println("done");
    task.run();
}
```

### toolarium-java-decompiler

```java
public void processItems(List<String> items) {
    items.forEach(item -> System.out.println(item));
    items.sort((a, b) -> a.compareTo(b));
    Runnable task = () -> System.out.println("task");
    task.run();
}
```

**Notes:** Lambda expressions are fully reconstructed from `invokedynamic`/`LambdaMetafactory`
bootstrap patterns. The compiler-generated `lambda$` method body is inlined into the lambda
expression, producing clean `(args) -> expr` syntax. Parameter names are recovered from the
implementation method's debug info when available.

### Vineflower, CFR

Both fully reconstruct lambda bodies by inlining the generated `lambda$` method.

### Fernflower, Procyon

Both reconstruct the lambda syntax. Fernflower may produce slightly verbose parameter types.


## 12. Method References

Method references compiled via `invokedynamic` with `LambdaMetafactory` bootstrap,
distinguished from lambdas by a non-`lambda$` target method.

### Source

```java
public void methodReferences(List<String> items) {
    items.forEach(System.out::println);
    items.sort(String::compareToIgnoreCase);
    Supplier<List<String>> factory = ArrayList::new;
}
```

### toolarium-java-decompiler

```java
public void methodReferences(List<String> items) {
    items.forEach(System.out::println);
    items.sort(String::compareToIgnoreCase);
    Supplier factory = ArrayList::new;
}
```

**Notes:** Method references are reconstructed as proper `Owner::method` expressions from
the `invokedynamic` bootstrap. Instance method references on captured receivers
(e.g., `System.out::println`) correctly emit the qualifier. Constructor references use the
`::new` syntax. Static and unbound instance references use the class name.

### Vineflower, CFR

Both correctly reconstruct all method reference forms including constructor references.

### Fernflower, Procyon

Both reconstruct method references. Procyon may sometimes emit the equivalent lambda form
instead of the `::` syntax.


## 13. For-Loop Edge Cases

Complex for-loop patterns with method call updates and non-standard increments.

### Source

```java
public int sumEvens(int limit) {
    int sum = 0;
    for (int i = 0; i < limit; i += 2) {
        sum += i;
    }
    return sum;
}

public void iterateWithMethod(Iterator<String> iter) {
    for (String item = iter.next(); iter.hasNext(); item = iter.next()) {
        System.out.println(item);
    }
}
```

### toolarium-java-decompiler

```java
public int sumEvens(int limit) {
    int sum = 0;
    for (int i = 0; i < limit; i += 2) {
        sum += i;
    }
    return sum;
}

public void iterateWithMethod(Iterator iter) {
    for (String item = iter.next(); iter.hasNext(); item = iter.next()) {
        System.out.println(item);
    }
}
```

**Notes:** For-loop detection now handles compound assignment updates (`i += 2`), method call
updates (`iter.next()`), and assignment updates. The while-to-for conversion recognizes
these patterns from the last statement of the loop body when preceded by a valid initializer.

### Vineflower, CFR

Both correctly reconstruct complex for-loop patterns.

### Fernflower, Procyon

Both handle standard for-loops. Fernflower may sometimes emit `while` for non-standard
update patterns.


## 14. Switch Expressions

A variable assignment determined by a switch where every case assigns to the same variable,
reconstructed as a switch expression with arrow syntax.

### Source

```java
public String classify(int mode) {
    String result;
    switch (mode) {
        case 1:
            result = "one";
            break;
        case 2:
            result = "two";
            break;
        default:
            result = "unknown";
            break;
    }
    return result;
}
```

### toolarium-java-decompiler

```java
public String classify(int mode) {
    String result = switch (mode) {
        case 1 -> "one";
        case 2 -> "two";
        default -> "unknown";
    };
    return result;
}
```

**Notes:** The variable-assignment switch pattern is detected and converted to a switch
expression with arrow syntax. Each case is emitted as `case X -> expr;` instead of the
fall-through `case X: result = expr; break;` form. The default case uses `default -> expr;`.

### Vineflower, CFR

Both reconstruct switch expressions from the assignment pattern when targeting Java 14+.

### Fernflower, Procyon

May preserve the original `switch` statement form with explicit `break` statements.


## 15. Pattern Matching instanceof

An `instanceof` check followed by a cast to the same type, reconstructed as a pattern
matching `instanceof` with a binding variable.

### Source

```java
public String describe(Object obj) {
    if (obj instanceof String) {
        String str = (String) obj;
        return "String: " + str;
    }
    return "other";
}
```

### toolarium-java-decompiler

```java
public String describe(Object obj) {
    if (obj instanceof String str) {
        return "String: " + str;
    }
    return "other";
}
```

**Notes:** The `instanceof` check followed by a cast and local variable declaration is
merged into a single `instanceof String str` pattern matching expression. The redundant
cast and variable declaration are removed.

### Vineflower, CFR

Both reconstruct pattern matching instanceof when targeting Java 16+.

### Fernflower, Procyon

May preserve the original `instanceof` + cast pattern.


## 16. Generic Type Recovery

Demonstrates recovery of parameterized types from the JVM Signature attribute.

### Source

```java
public class GenericExample {
    private List<String> names;
    private Map<String, List<Integer>> index;

    public List<String> getNames() {
        return names;
    }

    public <T extends Comparable<T>> T findMax(List<T> items) {
        T max = items.get(0);
        for (T item : items) {
            if (item.compareTo(max) > 0) {
                max = item;
            }
        }
        return max;
    }
}
```

### toolarium-java-decompiler

```java
public class GenericExample {
    private List<String> names;
    private Map<String, List<Integer>> index;

    public List<String> getNames() {
        return names;
    }

    public <T extends Comparable<T>> T findMax(List<T> items) {
        T max = items.get(0);
        for (T item : items) {
            if (item.compareTo(max) > 0) {
                max = item;
            }
        }
        return max;
    }
}
```

**Notes:** Generic types are recovered from the JVM Signature attribute attached to classes,
fields, and methods. Parameterized types (`List<String>`), nested generics
(`Map<String, List<Integer>>`), type variables (`T`), and bounded type parameters
(`T extends Comparable<T>`) are all reconstructed. Wildcards (`? extends`, `? super`) are
also supported.

### Vineflower, CFR

Both fully recover generic type information from signatures.

### Fernflower, Procyon

Both recover generic types. Fernflower may occasionally produce raw types for complex signatures.


## 17. Annotations

Annotations on classes, methods, fields, and parameters reconstructed from the class file attributes.

### Source

```java
@Deprecated
public class AnnotatedExample {
    @SuppressWarnings("unchecked")
    private List<String> items;

    @Override
    public String toString() {
        return "AnnotatedExample";
    }

    public void process(@NonNull String input, @Nullable String fallback) {
        System.out.println(input);
    }
}
```

### toolarium-java-decompiler

```java
@Deprecated
public class AnnotatedExample {
    @SuppressWarnings("unchecked")
    private List<String> items;

    @Override
    public String toString() {
        return "AnnotatedExample";
    }

    public void process(@NonNull String input, @Nullable String fallback) {
        System.out.println(input);
    }
}
```

**Notes:** Annotations are reconstructed from RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations,
RuntimeVisibleParameterAnnotations, and RuntimeInvisibleParameterAnnotations attributes. Annotation
element values (strings, enums, arrays, nested annotations) are correctly emitted. Type names are
simplified following the same rules as other types.

### Vineflower, CFR

Both fully reconstruct annotations including parameter annotations with element values.

### Fernflower, Procyon

Both reconstruct annotations. Fernflower may omit some invisible annotations by default.


## 18. Enhanced Enum Constructors

Enums with user-defined constructors that take custom parameters beyond the implicit `(String, int)`.

### Source

```java
public enum Planet {
    MERCURY(3.303e+23, 2.4397e6),
    VENUS(4.869e+24, 6.0518e6),
    EARTH(5.976e+24, 6.37814e6);

    private final double mass;
    private final double radius;

    Planet(double mass, double radius) {
        this.mass = mass;
        this.radius = radius;
    }

    public double getMass() { return mass; }
    public double getRadius() { return radius; }
}
```

### toolarium-java-decompiler

```java
public enum Planet {
    MERCURY(3.303E23, 2439700.0),
    VENUS(4.869E24, 6051800.0),
    EARTH(5.976E24, 6378140.0);

    private final double mass;
    private final double radius;

    public double getMass() {
        return this.mass;
    }

    public double getRadius() {
        return this.radius;
    }
}
```

**Notes:** The enum constructor's user-defined parameters are extracted from the `<init>` descriptor,
skipping the compiler-inserted `(String name, int ordinal)` prefix. Constant arguments from the
static initializer are correctly shown in the enum constant declarations. The constructor itself is
hidden since its only purpose is field assignment. Synthetic members (`$VALUES`, `values()`,
`valueOf()`) are removed.

### Vineflower, CFR

Both fully reconstruct enum constructors with custom parameters.

### Fernflower, Procyon

Both reconstruct enum constructors. Procyon may sometimes show the implicit `(String, int)` parameters.


## 19. Inner Class Reconstruction

Named inner classes nested inside their enclosing class when decompiling from a JAR or directory.

### Source

```java
public class Outer {
    private int value;

    public class Inner {
        public int getValue() {
            return value;
        }
    }

    public static class StaticNested {
        private String name;

        public String getName() {
            return name;
        }
    }
}
```

### toolarium-java-decompiler

```java
public class Outer {
    private int value;

    public class Inner {
        public int getValue() {
            return Outer.this.value;
        }
    }

    public static class StaticNested {
        private String name;

        public String getName() {
            return this.name;
        }
    }
}
```

**Notes:** When decompiling via `decompileAll()` (JAR or directory mode), inner classes are identified
from the InnerClasses attribute and nested inside their enclosing class. The inner class is renamed
to its simple name (stripping the `Outer$` prefix). Synthetic `this$0` fields referencing the
outer instance are removed. Only named inner classes are nested; anonymous and local classes
remain as separate top-level output.

### Vineflower, CFR

Both fully reconstruct inner class nesting, including anonymous and local classes.

### Fernflower, Procyon

Both reconstruct named inner class nesting. Anonymous class reconstruction varies.


## 20. Anonymous and Local Class Reconstruction

Anonymous inner classes (`Outer$1`) and method-local classes (`Outer$1Local`) are recovered
from the InnerClasses attribute and nested inside their enclosing class.

### Source

```java
public class EventHandler {
    public void register() {
        Runnable action = new Runnable() {
            @Override
            public void run() {
                System.out.println("anonymous action");
            }
        };

        class LocalHelper {
            void help() {
                System.out.println("local helper");
            }
        }

        action.run();
        new LocalHelper().help();
    }
}
```

### toolarium-java-decompiler

```java
public class EventHandler {
    public void register() {
        Runnable action = new Runnable() {
            public void run() {
                System.out.println("anonymous action");
            }
        };
        class LocalHelper {
            void help() {
                System.out.println("local helper");
            }
        }
        action.run();
        new LocalHelper().help();
    }
}
```

### Vineflower, CFR

All decompilers produce equivalent output for anonymous and local class nesting.

### Fernflower, Procyon

Fernflower and Procyon produce equivalent output, though formatting differs.


## 21. Sealed Class Support

Sealed classes, `permits` clauses, and `non-sealed` modifiers are reconstructed
from the PermittedSubclasses class file attribute (Java 17+).

### Source

```java
public sealed class Shape permits Circle, Rectangle {
}

public final class Circle extends Shape {
    double radius;
}

public non-sealed class Rectangle extends Shape {
    double width;
    double height;
}
```

### toolarium-java-decompiler

```java
public sealed class Shape permits Circle, Rectangle {
}

public final class Circle extends Shape {
    double radius;
}

public non-sealed class Rectangle extends Shape {
    double width;
    double height;
}
```

### Vineflower, CFR

Vineflower produces equivalent output. CFR has partial support for sealed modifiers.

### Fernflower, Procyon

Fernflower shows raw access flags without sealed keyword. Procyon has partial support.


## 22. Text Block Recovery

Multi-line string constants containing newline characters are emitted as
text blocks (`"""..."""`) instead of escaped string literals (Java 13+).

### Source

```java
public class HtmlTemplate {
    String html = """
        <html>
            <body>
                <p>Hello</p>
            </body>
        </html>
        """;
}
```

### toolarium-java-decompiler

```java
public class HtmlTemplate {
    String html = """
        <html>
            <body>
                <p>Hello</p>
            </body>
        </html>
        """;
}
```

### Vineflower, CFR

Vineflower reconstructs text blocks. CFR emits escaped `\n` string concatenation.

### Fernflower, Procyon

Fernflower emits the string with `\n` escapes. Procyon has partial text block support.


## Summary

| Feature | toolarium | Vineflower | Fernflower | CFR | Procyon |
|---|---|---|---|---|---|
| If/else chains | Correct | Correct | Correct | Correct | Correct |
| While loops | Correct | Correct | Correct | Correct | Correct |
| For loops | Correct | Correct | Correct | Correct | Correct |
| For-each loops | Correct | Correct | Correct | Correct | Correct |
| Switch (int) | Correct | Correct | Correct | Correct | Correct |
| Switch (String) | Correct | Correct | Correct | Correct | Correct |
| Field initializers | Correct | Correct | Correct | Correct | Correct |
| Arithmetic | Correct | Correct | Correct | Correct | Correct |
| Compound assignment | Correct | Correct | Correct | Correct | Correct |
| Enum reconstruction | Correct | Correct | Correct | Correct | Correct |
| Increment/decrement | Correct | Correct | Correct | Correct | Correct |
| Assert reconstruction | Correct | Correct | **Partial** | Correct | Correct |
| Boolean simplification | Correct | Correct | Correct | Correct | Correct |
| String concatenation | Correct | Correct | Correct | Correct | Correct |
| Variable names (LVT) | Applied | Applied | Applied | Applied | Applied |
| Try-catch/finally | Correct | Correct | Correct | Correct | Correct |
| Try-with-resources | Correct | Correct | **Partial** | Correct | Correct |
| Type simplification | Correct | Correct | Correct | Correct | Correct |
| Lambda expressions | Correct | Correct | Correct | Correct | Correct |
| Method references | Correct | Correct | Correct | Correct | Correct |
| For-loop edge cases | Correct | Correct | **Partial** | Correct | Correct |
| Switch expressions | Correct | Correct | **Partial** | Correct | **Partial** |
| Pattern instanceof | Correct | Correct | **Partial** | Correct | **Partial** |
| Generic types | Correct | Correct | Correct | Correct | Correct |
| Annotations | Correct | Correct | **Partial** | Correct | Correct |
| Enhanced enum ctors | Correct | Correct | Correct | Correct | **Partial** |
| Inner class nesting | Correct | Correct | Correct | Correct | Correct |
| Anonymous/local classes | Correct | Correct | Correct | Correct | Correct |
| Sealed class support | Correct | Correct | **Partial** | Correct | **Partial** |
| Text blocks | Correct | Correct | **Partial** | Correct | **Partial** |


## Roadmap

The toolarium-java-decompiler is an actively developed library. The comparison above reflects
the current state.

Future releases will continue to expand decompilation coverage for modern Java features.
