var x = 99;

class A {
    var b = 2;
    fn get() {
        return __class__();
    }

    fn setB(fx) {
        b = 4;
    }

    fn foo() {
        return x + 1;
    }
}

class B(A) {

}

class C(A) {

}

class D(B, C) {
    fn foo() {
        return x -= b;
    }
}

fn main() {
    b := new D();
    print(b.get());
    b.setB(null);
    print(b.super.super);
    print(D.__mro__);
    print(b.foo());
    d := new D();
    //e := new E();
    print(Invokes.hasAttr([d], __iter__));

    return d.b;
}
