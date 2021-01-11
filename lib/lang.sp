class Object {

    fn __str__() {
        return __class__().__name__ + "@" + Invokes.id(this);
    }

    fn __repr__() {
        return __class__().__name__ + "@" + Invokes.id(this);
    }

    fn __class__() {
        return Invokes.getClass(this);
    }
}

fn wrapper?(obj) {
    return (not AbstractObject?(obj)) or Wrapper?(obj);
}

class Wrapper {
    const value;

    fn __init__(value) {
        this.value = value;
    }

    fn __str__() {
        return str(value);
    }

    fn __repr__() {
        return __str__();
    }

    fn __add__(other) {
        return wrap(value + wrap(other).value);
    }

    contract __add__(wrapper?) -> Wrapper?;


    fn __sub__(other) {
        return wrap(value - wrap(other).value);
    }

    contract __sub__(wrapper?) -> Wrapper?;


    fn __mul__(other) {
        return wrap(value * wrap(other).value);
    }

    contract __mul__(wrapper?) -> Wrapper?;


    fn __div__(other) {
        return wrap(value / wrap(other).value);
    }

    contract __div__(wrapper?) -> Wrapper?;


    fn __mod__(other) {
        return wrap(value % wrap(other).value);
    }

    contract __mod__(wrapper?) -> Wrapper?;

    fn __gt__(other) {
        return value > wrap(other).value;
    }

    fn __lt__(other) {
        return value < wrap(other).value;
    }

    fn __eq__(other) {
        return value == wrap(other).value;
    }

    fn __ne__(other) {
        return type(this) is not type(other) or not __eq__(other);
    }
}

class Integer(Wrapper) {
    fn __init__(value) {
        super.__init__(int(value));
    }
}

class Float(Wrapper) {
    fn __init__(value) {
        super.__init__(float(value));
    }
}

class Boolean(Wrapper) {
    fn __init__(value) {
        super.__init__(boolean(value));
    }
}

class Character(Wrapper) {
    fn __init__(value) {
        super.__init__(char(value));
    }
}

class Byte(Wrapper) {
    fn __init__(value) {
        super.__init__(byte(value));
    }
}

class Exception {
    const cause;
    const msg;
    var traceMsg;

    fn __init__(msg=null, cause=null) {
        this.cause = cause;
        this.msg = msg if msg is not null else "";
    }

    fn __str__() {
        return "Exception " + msg;
    }

    fn printStackTrace() {
        Invokes.printErr(__class__().__name__ + ": " + msg + " ");
        Invokes.printErr(traceMsg);
    }
}

class AttributeException(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

class ArgumentException(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

class ContractError(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

class IndexException(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

class Interruption(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

class InvokeError(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

class NameError(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

class NotImplementedError(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

class TypeError(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

class UnknownTypeError(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}

class Iterator {
    fn __hasNext__() {
        throw new NotImplementedError();
    }

    fn __next__() {
        throw new NotImplementedError();
    }
}

class Iterable {
    fn __iter__() {
        throw new NotImplementedError();
    }
}

class ArrayIterator(Iterator) {
    const array;
    const endIndex;
    var index = 0;

    fn __init__(array, endIndex=null) {
        this.array = array;
        this.endIndex = array.length if endIndex is null else endIndex;
    }

    fn __hasNext__() {
        return index < endIndex;
    }

    fn __next__() {
        return array[index++];
    }
}

class RangeIterator(Iterator) {
    var current;
    const end;
    const step;

    fn __init__(begin, end, step) {
        this.current = begin;
        this.end = end;
        this.step = step;
    }

    fn __hasNext__() {
        if step >= 0 {
            return current < end;
        } else {
            return current > end;
        }
    }

    fn __next__() {
        val := current;
        current += step;
        return val;
    }
}

class List(Iterable) {
    var array;
    var _size;

    fn __init__(*args) {
        initCapacity := _calculateCapacity(args.length) if args.length > 8 else 8;
        array = new Object[initCapacity];
        _size = args.length;
        for var i = 0; i < _size; i++ {
            set(i, args[i]);
        }
    }

    fn __getItem__(index) {
        return get(index);
    }

    fn __setItem__(index, value) {
        return set(index, value);
    }

    fn __iter__() {
        return new ArrayIterator(array, _size);
    }

    fn __repr__() {
        result := "[";
        for i := 0; i < _size; i++ {
            item := array[i];
            if List?(item) or Array?(item) {
                result += item.__class__().__name__ + "@" + Invokes.id(item);
            } else {
                result += repr(item) + ", ";
            }
        }
        return result + "]";
    }

    fn __str__() {
        result := "[";
        for i := 0; i < _size; i++ {
            result += (repr(array[i]) + ", ");
        }
        return result + "]";
    }

    fn append(value) {
        set(_size++, value);
        if _size == array.length {
            _expand();
        }
    }

    fn get(index) {
        _checkIndex(index)
        return array[index];
    }

    fn insert(index, value) {
        _size++;  // make sure [1,2,3].insert(3, 4) works
        _checkIndex(index);

        for i := _size - 1; i >= index; i-- {
            array[i] = array[i - 1];
        }
        set(index, value);

        if _size == array.length {
            _expand();
        }
    }

    contract insert(int?, anyType) -> void;

    fn set(index, value) {
        _checkIndex(index)
        wrapper := wrap(value);
        array[index] = wrapper;
    }

    fn size() {
        return _size;
    }

    fn remove(index) {
        _checkIndex(index);
        item := get(index);
        for i := index; i < _size; i++ {
            array[i] = array[i + 1];
        }
        _size--;
        if _size < array.length / 4 {
            _collapse();
        }
    }

    contract remove(int?) -> anyType;

    fn toArray(eleType=Object) {
        eleProc := cond {
                       case eleType is Object -> lambda x -> x;
                       case Callable?(eleType) -> eleType;
                   };

        resArray := new eleType[_size];
        for i := 0; i < _size; i++ {
            resArray[i] = eleProc(array[i]);
        }
        return resArray;
    }

    fn _checkIndex(index) {
        if index < 0 or index >= _size {
            throw new IndexException();
        }
    }

    fn _expand() {
        newArray := new Object[array.length * 2];
        for i := 0; i < _size; i++ {
            newArray[i] = array[i];
        }
        array = newArray;
    }

    fn _collapse() {
        newArray := new Object[array.length / 2];
        for i := 0; i < _size; i++ {
            newArray[i] = array[i];
        }
        array = newArray;
    }

    fn _calculateCapacity(inputSize) {
        logVal := Invokes.log(inputSize) / Invokes.log(2);
        exp := int(logVal) + 1;
        return int(Invokes.pow(2, exp));
    }
}

class NaiveDict {

    const keys;
    const values;
    const length;

    fn __init__(keys, values) {
        this.keys = keys;
        this.values = values;
        this.length = keys.length;
    }

    fn get(key) {
        for var i = 0; i < length; i++ {
            if (keys[i] == key) {
                return values[i];
            }
        }
        return null;
    }
}

class String {

    const __chars__;
    const length;

    fn __init__(charArray) {
        __chars__ = charArray;
        length = charArray.length;
    }

    fn __add__(other) {
        var otherStr = str(other);
        var array = new char[length + otherStr.length];
        var index = 0;
        for ; index < length; index++ {
            array[index] = __chars__[index];
        }
        for ; index < array.length; index++ {
            array[index] = otherStr.__chars__[index - length];
        }
        return new String(array);
    }

    fn __eq__(other) {
        if not String? (other) {
            return false;
        }
        if other.length != length {
            return false;
        }
        for var i = 0; i < length; i++ {
            if __chars__[i] != other.__chars__[i] {
                return false;
            }
        }
        return true;
    }

    fn __ne__(other) {
        return type(this) is not type(other) or not __eq__(other);
    }

    fn toUpper() {

    }

    fn toLower() {

    }
}

fn anyType(_) {
    return true;
}

fn array?(eleType) {
    return lambda x -> Array?(x) and x.type is eleType;
}

fn clock() -> int? {
    return Invokes.clock();
}

fn iter?(x) {
    return Array?(x) or Iterable?(x) or Iterator?(x);
}

fn len(x) {
    return cond {
        case Array?(x) -> x.length;
        case List?(x) -> x.size();
        default -> throw new UnknownTypeError();
    }
}

fn orFn(fn1: Callable?, fn2: Callable?) {
    return lambda x -> fn1(x) or fn2(x);
}

fn input(prompt: String?="") {
    return Invokes.input(prompt);
}

fn print(s) {
    Invokes.println(s);
}

fn range(begin, end, step=1) {
    return new RangeIterator(begin, end, step);
}

fn script(path, *args) {
    return Invokes.script(path, *args);
}

contract script(String?, String?) -> anyType;

fn str(obj) {
    return Invokes.string(obj);
}

fn repr(obj) {
    return Invokes.repr(obj);
}

fn sleep(mills: int?) {
    mills = unwrap(mills);
    start := clock();
    while clock() - start < mills {  // busy waiting
    }
}

fn type(obj) {
    return cond {
        case Object?(obj) -> obj.__class__();
        case AbstractObject?(obj) -> Invokes.nativeType(obj);
        case int?(obj) -> int;
        case float?(obj) -> float;
        case char?(obj) -> char;
        case boolean?(obj) -> boolean;
        case byte?(obj) -> byte;
        default -> null;
    }
}

fn void(x) {
    return x is null;
}

/*
 * Wraps any primitive to wrapper. Leave any referenced value unchanged.
 */
fn wrap(value) {
    cond {
        case int?(value) {
            return new Integer(value);
        } case float?(value) {
            return new Float(value);
        } case char?(value) {
            return new Character(value);
        } case byte?(value) {
            return new Byte(value);
        } case boolean?(value) {
            return new Boolean(value);
        } default {
            return value;
        }
    }
}

fn unwrap(value) {
    cond {
        case Wrapper?(value) {
            return value.value;
        } case AbstractObject?(value) {
            throw new TypeError("Cannot unwrap non-wrapper object.");
        } default {  // primitive
            return value;
        }
    }
}

// Reflections

fn getAttr(obj: Object?, attr: String?) {
    if hasAttr(obj, attr) {
        return Invokes.getAttr(obj, attr);
    } else {
        throw new AttributeException("Object '" + obj + "' does not have attribute '" + attr + "'.");
    }
}

fn hasAttr(obj: Object?, attr: String?) -> boolean? {
    return Invokes.hasStrAttr(obj, attr);
}

fn setAttr(obj: Object?, attr: String?, value) {
    if hasAttr(obj, attr) {
        Invokes.setAttr(obj, attr, value);
    } else {
        throw new AttributeException("Object '" + obj + "' does not have attribute '" + attr + "'.");
    }
}

fn getClassByName(name: String?) {
    if Invokes.hasGlobalName(name) {
        value := Invokes.getGlobalByName(name);
        if Class?(value) {
            return value;
        }
    }
    throw new AttributeException("Name ''" + name + "' does not exist or is not a class.");
}

// Constants

const copyright = "Copyright (C) Trash Software Studio.";
const INTERRUPTION = new Interruption("User interruption");
