# Arrays

There are two types of array in SPL: primitive arrays and reference arrays.

## Primitive arrays

The only way to define a primitive array is to declare it with a type name and length. \
Each primitive types is eligible for array creation. \
Example of primitive array creation:

```
var intArray = new int[4];
var floatArray = new float[7];
```

## Reference arrays

The reference can also be defined in the primitive way. The syntax is
```
var refArray = new Obj[4];
```
Because `Obj` is the representation of all reference type in SPL. \

There is another way to define a reference array, which specify its element contract by a boolean function. 
For example:
```
var strArray = new String?[3];
var listArray = new List?[5];
```
In the two cases above, the boolean functions are `Stirng?` and `List?`. \
When putting elements into this array, the boolean function checks the new element unless the interpreter has been 
configured not to check contract. 
If resulting a `false`, the program throws an error. \
Note that some tricks also works in SPL, such as `var someArray = new (lambda x -> true)[2]`. This would result almost 
the same as writing `var someArray = new Obj[2]`, but an extra lambda call is proceed in each element modification.
