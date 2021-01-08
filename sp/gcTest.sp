class X {

    var a = "efg";

    fn __init__() {

    }

    fn fun() {
        return a + "x";
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
    for i := 0; i < 100; i++ {
        d = "ax" + a;
        e := new X();
        //Invokes.gc();
        d = d + e.fun();
    }
}