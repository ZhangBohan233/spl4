var a = 2.0 * 3 / 4;
print(- -a);


fn foo(a, *args, **kwargs) {
    return a + args.length;
}

fn bar(x) {

}


fn main() {
    var xx = foo(5);
    print(xx);
    bar(1);
}