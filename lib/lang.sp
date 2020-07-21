class Object {

    fn __str__() {
        return "Object@" + Invokes.id(this);
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
}

fn print(s) {
    Invokes.println(s);
}

fn str(obj) {
    return Invokes.string(obj);
}
