fn foo(const a) {
    //a = 3;
    return lambda b -> a + b;
}

var xx = lambda x -> lambda y -> (x + y) * 2;
var yy = lambda x -> fn(y) {var g = 1; return x + y + g};


fn main() {
    var x = foo(1)(2);
    Invokes.println(x);
    Invokes.println(yy(4)(3));
    Invokes.println(xx(5) (6));
}