class A<K, V> {
    var val = K;

    fn test() {
        print(K);
        print(val);
        print(V);
    }

    fn foo(a: K) {
        return a;
    }
}

/*
 * A class that overrides templated class
 */
class B<K>(A<int?, K>) {

}

fn main() {
    b := new B<float?>();
    b.test();
    print(b.super.__instance__);
    print(b.foo(1));
}