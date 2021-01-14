fn main() {
    for i := 0; i < 1000; i++ {
        class A {
            var a = i;
            fn __init__() {

            }

            fn foo() {
                return i + 1;
            }
        }
        a := new A();
    }
}