### Tokenizer:

~~**ISSUE T01**~~ **_FIXED_** \
Escape characters '\\' not working properly.

### Interpreter:

### Memory and GC:

### Function and contract

**ISSUE F01**
~~Param contract with default param value~~ **_FIXED_**
```
fn foo(a: int?, b: float? = 2.5) -> int? {
    return a + int(b);
}
foo(2);  // currently causes error
```

**ISSUE F02** \
~~Keyword arguments not working~~ **_FIXED_**

**ISSUE F03** \
~~Keyword arguments cannot pass~~ **_FIXED_**
```
fn foo(**kwargs)
```

**ISSUE F04** \
~~No check for conflict keyword argument vs positional argument~~ **_FIXED_**
```
fn foo(a=1,b=2) { ... }
foo(3,a=4);  // should cause an error but does not
```

**ISSUE F05** \
~~Java anonymous class invoke error.~~ **_FIXED_**
```
int.__checker__
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

~~**ISSUE C02**~~ **_FIXED_** \
Generic name conflicting
```
class M<K, V>(HashDict<K, V or int?>) { { ... }
dict := new M<int?, float?>();
```
In class `HashDict`, `V` should represent `float? or int?`, but it actually represents `float?`

### Mysterious bugs

**M01**
Calling `Invokes.print(inputStream.readLine());` does not print anything
