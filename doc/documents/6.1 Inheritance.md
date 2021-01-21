# Inheritance

## Mechanism

### Methods
Methods are defined when the class is defined. There is no explicit relationship between the class and the method
when the method is first defined. The interpreter may record in which class the method is actually defined to
reserve information used by templating.

### Multiple inheritance

## Special names

### 1. `this`
Represents the object that actually calls this method. \
When defining methods, a parameter called `this` would be automatically inserted as the first param of the method. \
This keyword is only accessible from methods, but not in the class body. \

When calling a method, an argument that referenced to the actual object would be inserted. \
There are three situations that SPL inserts the instance `this`:
1. Calling method of an instance via dot: `instance.method(args...)`. 
   The actual effect is `instance.method(instance, args...)`
   
2. Calling method inside a method: 
```
class Clazz {
    fn foo(params...) { ... }
    fn bar() {
        foo(args...);  // it actually calls this.foo(this, args...)
    }
}
```

3. Constructor call: `new Clazz(args...)`. It first creates an instance and calls `Clazz.__init__(instance, args...)` \

Note that the keyword `this` always represents the instance that actually created. For example:
```
class A {
    fn foo() {
        print(this.__class__());
    }
}
class B(A) {
}
b := new B();
b.foo();  // prints class B
```

### 2. `super`
Represents the instance of its superclass, created together with this instance.

### 3. `__instance__`
Represents the instance itself in each superclass. \
In the outermost class instance (the child), it points to the same instance as `this` in any method. However, the 
attribute `__instance__` is also accessible in class body. \
In any superclass, the attribute `__instance__` points to the instance of the superclass. \
For example:
```
class A {
    fn foo() {
        print(__instance__.__class__());
    }
}
class B(A) {
}
b := new B();
b.foo();  // prints class A
```
