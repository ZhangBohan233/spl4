class A {
    var a = __instance__;

    fn foo() {
        print(this.__class__());
    }

    fn bar() {
        print(__instance__.__class__());
    }
}
class B(A) {
}

fn main() {
    b := new B();
    b.foo();  // prints class B
    b.bar();
}