### Tokenizer:

~~**ISSUE T01**~~ **_FIXED_** \
Escape characters '\\' not working properly.

### Interpreter:

### Function and contract

**ISSUE F01**
~~Param contract with default param value~~ **_FIXED_**
```
fn foo(a: int?, b: float? = 2.5) -> int? {
    return a + int(b);
}
foo(2);  // currently causes error
```

### Class and inheritance

~~**ISSUE C01**~~ **_FIXED_** \
Call method overridden in child class from method in superclass.
```
class A {
    ...
}

class B(A) {
    fn printName() {
        print(getClass().name);
    }
}
...
(new B()).printName();  // expect "B" but currently prints "A"

```
