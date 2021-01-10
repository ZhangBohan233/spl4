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
    //xx := [new X(), new X()];
    //Invokes.gc();
    //gg := "qwe";
    //b := [];
    //c := [];
    var d;
    for i := 0; i < 10; i++ {
        d = "ax" + a + d;
        e := new X();
        //Invokes.gc();
        d = d + e.fun();
    }
    print(d);
}