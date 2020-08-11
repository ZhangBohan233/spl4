### Tokenizer:

~~**ISSUE T01**~~ **_FIXED_** \
Escape characters '\\' not working properly.

### Interpreter:

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
