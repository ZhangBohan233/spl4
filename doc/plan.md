# Plan

### Phase 1

* ~~Contract in class body~~ **_DONE_**
* ~~getitem, setitem~~ **_DONE_**
* ~~error~~ _**DONE**_
* ~~Separate UnaryExpr without returning value (return, throw)~~   _**DONE**_


### Phase 2

* ~~Type of arithmetic~~ **_DONE_**
* ~~Pattern matching contract, example:~~ `contract foo(int?, A?) -> A?` **_REPLACED_**
* ~~Invoke call "hasAttr"~~ **_DONE_**
* ~~Syntax sugar of unary function,~~ example: `int? or float?` **_DONE_**
* ~~Duplicate literal removal~~ **_DISCARDED_**
* ~~Ast visualizer~~ **_DONE_**
* ~~Resolve primitive wrapper confusion~~ **_DONE_**
* ~~Direct contract in function declaration~~ **_DONE_**
* ~~Reflection~~ **_DONE_**
* ~~Input stream~~ **_DONE_**
* ~~input()~~ **_DONE_**


### Phase 3

* ~~File IO~~ **_DONE_**
* ~~String literal duplicate removal~~ **_DONE_**
* ~~Reference count garbage collection~~ **_DISCARDED_**
* ~~Memory reallocation during full gc~~ **_DONE_**
* ~~String operations (upper, lower)~~ **_DONE_**
* ~~Type wrapper of primitive and abstract object~~ **_DONE_**
* ~~Type of array element~~ **_DONE_**
* ~~Add string as the right side operand~~ **_DISCARDED_**
* ~~Compiled spl file (*.spc)~~ **_DONE_**
* ~~Store docstring in spl object~~ **_DONE_**
* ~~Gc suite for class declaration~~ **_DONE_**
* ~~Spc decompiler~~ **_PARTIALLY DONE_**
* ~~HashDict~~ **_DONE_**
* ~~TreeDict~~ **_DONE_**
* ~~HashDict creation via~~ `{key1=value1, ...}` **_DONE_**
* ~~Wrapper of native types~~ **_DISCARDED_**
* ~~Get class method statically~~ `Object.__hash__` **_DONE_**
* ~~More information in compiled spl file~~ **_DONE_**
* ~~Rename AbstractObject to a shorter name~~ **_DONE_**
* ~~Generic contract:~~ `fn test<T>(x: T) -> T` **_DONE_**
* ~~Check function scope 'removeReturn'~~ **_CHECKED_**


### 4.0 Alpha
* ~~Generic array~~ **_DONE_**
* ~~Reflection on generics~~ **_DONE_**
* ~~Find a way to deal with contract overriding~~ **_DISCARDED_**
* ~~Set and set creation via~~ `{e1, e2, ...}` **_DONE_**
* ~~Abstract class, avoiding a class to be instantiated~~ **_DISCARDED_**
* ~~Generic contract~~ `List?<T>` **_DISCARDED_**
* ~~private, protected~~ **_DISCARDED_**
* ~~Config file~~ **_DONE_**
* ~~Wrapper of System.in, out, err~~ **_DONE_**
* ~~String format~~ **_DONE_**
* ~~Assert~~ **_DONE_**


### After 4.0 alpha
* Iterator remove
* Unittest
* ~~Anonymous class~~ **_DONE_**
* ~~Multithreading~~ **_DISCARDED_**
* ~~Const fn: avoid it to be overridden~~ **_DONE_**
* ~~Const class~~ **_DONE_**
* Json
* ~~Bitwise operations for wrapper types~~ **_DONE_**
* ~~Error occurs while dealing with errors~~ **_PARTIAL DONE_**
* ~~Array creation via~~ `new int[3]{1, 2, 3}` **_DONE_**
* ~~Comma in switch-case~~ **_DONE_**
* ~~Private stacks of threads~~ **_DISCARDED_**
