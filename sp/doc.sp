/*
 * A class.
 * Shit no use.
 */
class A {
    var x = 1;
    fn __init__() {
    }

    fn foo(x) {
        return x + this.x;
    }

    fn bar(x) {
        return x;
    }
}

/*
 * Test of doc.
 */
fn foo() {

}

fn main() {
    print("233" + 4);
    help(foo);
    help(A);
    a := new A();
    print((A.foo)(a, 2));
}