class A {
    var b = 2;
    fn get() {
        return getClass();
    }

    fn setB(fx) {
        b = 4;
    }
}

class B(A) {
    fn what() {
        return b;
    }
}

class C(A) {

}

class D(B, C) {

}

fn main() {
    b := new B();
    print(b.get());
    print(b.what());
}
