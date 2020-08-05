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

}

class C(A) {

}

class D(B, C) {

}

fn main() {
    b := new D();
    print(b.get());
    b.setB(fn (x, y) {return 0;});
    print(b.b);
    print(D.__mro__);
    d := new D();
    //print(d.__mro__);
    print(d.super.super.super);
}
