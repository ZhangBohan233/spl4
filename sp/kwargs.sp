fn foo(a: int? = 1, b: int? = 2) {
    print(a);
    print(b);
    return a * b;
}

fn curry(func: Callable?) -> Callable? {
    fn curried(*args, **kwargs) {
        print("Curried! " + kwargs["v"]);
        return func(*args, **kwargs);
    }
    return curried;
}

fn main(args: array?(Object?)) {
    cr := curry(foo);
    print(args);
    b := new Object?[3];
    b[0] = "111";
    print(array?(String?)(args));
    print(type(args));
    return cr(3, b=4);
}