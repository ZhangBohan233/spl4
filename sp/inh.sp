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
        return super.foo() - 2;
    }
}

fn main() {
    b := new D();
    print(b.get());
    print(b.super.super);
    print(D.__mro__);
    print(b.foo());
    d := new D();
    //e := new E();
    print(Invokes.hasAttr([d], __iter__));
}
