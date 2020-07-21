//import namespace lang
//import "imp.sp"
//import namespace "imp2.sp"

fn any(x) {
    return true;
}

fn lsp(a, b) {
    return a + b;
}

contract lsp(int?, int?) -> any;

fn foo() {
    return 0;
}

contract foo() -> fn(x) {return x == 0};

fn main() {
    var a = 2.5;
    var b = 4;

    var c = new int[5];
    c[2] = 1;

    var d = "asd" + "efg";
    Invokes.println(d);
    Invokes.println(AbstractObject?(d));
    Invokes.println(AbstractObject?(b));

    return a + b + c[2] + lsp(1, 2);
}
