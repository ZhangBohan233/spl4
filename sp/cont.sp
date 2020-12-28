fn unaryOr(*args) {
    if args.length == 0 {
        return false;
    } else {
        return unaryOrHelper(args, 0);
    }
}

fn unaryOrHelper(fnArray, const index) {
    if index == fnArray.length - 1 {
        return lambda arg -> fnArray[index](arg);
    } else {
        return fn (arg) {
            if fnArray[index](arg) {
                return true;
            } else {
                return unaryOrHelper(fnArray, index + 1)(arg);
            }
        }
    }
}

fn isInt(x) {
    return int?(x);
}


fn increment(num) {
    return num + 1;
}

fn test(a: int?) -> int? {
    return a + 1;
}


fn main() -> void {
    a := unaryOr(float?, boolean?, char?, int?);
    print(a(2));
    print(test(3));
    return 1;
}