import math

class A<K, V> {
    fn __init__() {

    }

    fn get(a: K) -> V {
    }

    fn foo<T>(a: T) {
        return a;
    }
}

class B<G>(A<byte?, G>) {

}

class C<T>(A<T, float?>, B<T>) {
    fn __init__() {
    }
}

fn foo<T>(a: T) -> T {
    return a;
}

fn bar(x) {
    return x;
}

contract bar<T>(T) -> T;

fn xx(x: int?) -> int? {
    return x;
}

fn main() {
    print(foo<int?>(3));
    print(bar<int?>(2));
    a := new A();
    print(a.foo<int?>(4));
    c := new C<int?>();
    print(C.__mro__);
}