//import namespace lang
//import "imp.sp"
//import namespace "imp2.sp"


fn lsp(a, b) {
    return a + b;
}


fn main() {
    var a = 2;
    var b = 4;

    var c = new int[5];
    c[2] = 1;

    var d = "asd" + "efg";
    Invokes.println(d);

    return a + b + c[2];
}
