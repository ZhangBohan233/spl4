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
        //e := new E();
        //e.cc();
        return tt();
    }

    fn tt() {
        return b;
    }
}

class C(A) {

}

class D(B, C) {

}

class E {
    fn cc() {
        print(getClass());
    }
}

fn main() {
    //b := new B();
    //print(b.get());
    //print(b.what());

    lst := new List(1);
    //lst.append(8);
}
