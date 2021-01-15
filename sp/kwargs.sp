fn foo(a: int? = 1, b: int? = 2) {
    print(a);
    print(b);
    return a * b;
}

fn curry(func) {
    fn curried(*args, **kwargs) {
        print("Curried! " + kwargs["v"]);
        return func(*args, **kwargs);
    }
    return curried;
}

fn main() {
    cr := curry(foo);
    return cr(3, b=4);
}