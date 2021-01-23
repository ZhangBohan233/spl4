class A {
    fn __init__(a) {

    }
}

class B(A) {
    fn __init__() {
        super.__init__(2);
    }
}

fn main() {
    obj := new lang.List<int??>(1, 2) <- {
        fn foo() {
            return get(0);
        }
    }
    print(obj);
    print(obj.foo())
    print(obj.get(1));
}