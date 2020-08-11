

fn foo(a, *args, **kwargs) {
    print(kwargs.get("xs"));
    return a + args.length;
}

contract foo(int?, anyType, anyType) -> anyType;

fn bar(x) {

}


fn main() {
    var xx = foo(5, xs = 61, ys = 7);
    print(xx);
    bar(1);
}