class X {

    var a = "efg";

    fn __init__() {

    }

    fn fun() {
        return a + "x";
    }
}

class B {
    var b = 3;

    fn __init__() {

    }
}

fn main() {
    var a = 3;
    var d;
    for i := 0; i < 100; i++ {
        d = "ax" + a + d;
        e := new X();
        //Invokes.gc();
        d = d + e.fun();
    }
    print(d);
    lst := [];
    for i := 0; i < 100; i++ {
        lst.append(i);
    }
    Invokes.gc();
    print(lst);
}