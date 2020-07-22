fn foo(const a) {
    //a = 3;
    return lambda b -> a + b;
}

var xx = lambda x -> lambda y -> (x + y) * 2;
var yy = lambda x -> fn(y) {var g = 1; return x + y + g};


fn main() {
    var x = foo(1)(2);
    var arr = new Object[3];
    arr[0] = foo(2);
    arr[1] = xx;
    arr[2] = yy;
    return arr[0](2);
}