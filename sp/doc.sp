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
    print(foo.__doc__());
    help(foo);
    help(A);
    a := new A();
    print(A.foo(a, 2));
    b := new int[3];
    print(b.type);
    print(Obj?(b[1]));

    print(listAttr(List));
    lst := [2];
    met := getAttr(lst, listMethod(List)[5]);
    print(met);
    print(getAttr(lst, listMethod(List)[5])(lst, 0, 3));
    met(lst, 0, 1);
    print(lst);
}