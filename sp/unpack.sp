var a = 2.0 * 3 / 4;
print(- -a);


fn foo(a, *args, **kwargs) {
    print(kwargs.get("xs"));
    return a + args.length;
}

fn bar(x) {

}


fn main() {
    var xx = foo(5, xs = 61, ys = 7);
    print(xx);
    bar(1);
}