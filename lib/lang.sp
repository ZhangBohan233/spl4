class Object {

    fn __str__() {
        return "Object@" + Invokes.id(this);
    }
}

class Wrapper {
    const value;

    fn __init__(value) {
        this.value = value;
    }

    fn __str__() {
        return str(value);
    }
}

class Integer(Wrapper) {
    fn __init__(value) {
        super.__init__(value);
    }
}

class Float(Wrapper) {
    fn __init__(value) {
        super.__init__(value);
    }
}

class Boolean(Wrapper) {
    fn __init__(value) {
        super.__init__(value);
    }
}

class Character(Wrapper) {
    fn __init__(value) {
        super.__init__(value);
    }
}

class Exception {
    const cause;
    const msg;
    var traceMsg;

    fn __init__(msg="", cause=null) {
        this.cause = cause;
        this.msg = msg;
    }

    fn __str__() {
        return "Exception " + msg;
    }

    fn printStackTrace() {
        Invokes.printErr(getClass().name + ": " + msg + " ");
        Invokes.printErr(traceMsg);
    }
}

class IndexException(Exception) {
    fn __init__(msg="", cause=null) {
        super.__init__(msg, cause);
    }
}

class NotImplementedError(Exception) {
    fn __init__(msg="", cause=null) {
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
    var index = 0;

    fn __init__(array) {
        this.array = array;
    }

    fn __hasNext__() {
        return index < array.length;
    }

    fn __next__() {
        return array[index++];
    }
}

class List(Iterable) {
    var array;
    var size;

    fn __init__(*args) {
        initCapacity := _calculateCapacity(args.length) if args.length > 8 else 8;
        array = new Object[initCapacity];
        size = args.length;
        for var i = 0; i < size; i++ {
            set(i, args[i]);
        }
    }

    fn __str__() {
        result := "[";
        for i := 0; i < size; i++ {
            result += (str(array[i]) + ", ");
        }
        return result + "]";
    }

    fn append(value) {
        set(size++, value);
        if size == array.length {
            _expand();
        }
    }

    fn get(index) {
        return array[index];
    }

    fn set(index, value) {
        wrapper := wrap(value);
        array[index] = wrapper;
    }

    fn _expand() {
        newArray := new Object[array.length * 2];
        for i := 0; i < size; i++ {
            newArray[i] = array[i];
        }
        array = newArray;
    }

    fn _collapse() {

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

    const chars;
    const length;

    fn __init__(charArray) {
        chars = charArray;
        length = charArray.length;
    }

    fn __add__(other) {
        var otherStr = str(other);
        var array = new char[length + otherStr.length];
        var index = 0;
        for ; index < length; index++ {
            array[index] = chars[index];
        }
        for ; index < array.length; index++ {
            array[index] = otherStr.chars[index - length];
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
            if chars[i] != other.chars[i] {
                return false;
            }
        }
        return true;
    }
}

fn print(s) {
    Invokes.println(s);
}

fn str(obj) {
    return Invokes.string(obj);
}

fn wrap(value) {
    cond {
        case int?(value) {
            return new Integer(value);
        } case float?(value) {
            return new Float(value);
        } case char?(value) {
            return new Character(value);
        } case boolean?(value) {
            return new Boolean(value);
        } default {
            return value;
        }
    }
}
