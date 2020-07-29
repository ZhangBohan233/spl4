class A {
    var b = 2;
    fn get() {
        return getClass();
    }

    fn setB(fx) {
        b = 4;
    }
}

class B(A) {

}

fn main() {
    b := new B();
    print(b.get());
    b.setB(fn (x, y) {return 0;});
    print(b.b);
}
