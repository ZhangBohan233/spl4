fn foo(const a) {
    //a = 3;
    return fn(var b = 5) {
        return a + b;
    }
}


fn main(args) {
    //Invokes.println(args[0] + args[1]);
    var x = foo(1)();
    Invokes.println(x);
}