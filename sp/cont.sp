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

fn test(a: int? or float?) -> int? or float? {
    return a + 1;
}

fn foo(a: int?, b: float? = 2.5) -> int? {
    return a + int(b);
}

fn main() {
    a := unaryOr(float?, boolean?, char?, int?);
    print(a(2));
    print(test(1));
    print(foo(2));
    return 1;
}
