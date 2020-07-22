class A {
    fn __eq__(o) {
        return true;
    }
}


fn main() {
    var b = true and false;
    var c = false or false;

    print(not b);
    print(c);

    x := new A();
    y := new A();

    print(x == y);
}