fn test(a: int?) {
    return a + 1;
}

class A {
    var x;
    fn __init__(x) {
        this.x = x;
    }
}
 
fn main() {
    b := 23;
    a := new A(b);
    return test(a.x);
}
