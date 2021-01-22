# Arrays

Arrays are the most fundamental data collection in SPL. \
There are two types of array in SPL: primitive arrays and reference arrays.

## Array types
### Primitive arrays

The only way to define a primitive array is to declare it with a type name and length. \
Each primitive types is eligible for array creation. \
Example of primitive array creation:

```
var intArray = new int[4];
var floatArray = new float[7];
```

### Reference arrays

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

## Array operations
To put an element `ele` at the `index` position in the array, use
```
array[index] = ele;
```
To obtain the element stored at the `index` position in the array, use
```
var ele = array[index];
```
Note that `index` starts from `0`. If the index is outside of array bound, an error would be thrown.

## Array attributes
Let `array` be the array instance.
* `array.length` the declared length
* `array.type` the element type, can either be a primitive type or `Obj` type
* `array.generics` the element contract, if it is defined (i.e. defined by func?[length]), or `null` otherwise

## Initial values
When creating an array, all data that previously existing on that memory location would be wiped. Default values 
are filled in all positions in the array. \
For different types of arrays, there are different default values. \
* `int`: `0`
* `float`: `0.0`
* `char`: `'\0'`
* `byte`: `0b`
* `boolean`: `false`
* `Obj` and any reference type: `null`
