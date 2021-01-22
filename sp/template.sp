import math

class A<K, V> {
    fn __init__() {

    }

    fn put(k: K, v: V) {
    }

    fn get(a: K) -> V {
    }

    fn foo<T>(a: T) {
        return a;
    }
}

class B<G>(A<float?, G>) {

}

class C<T>(A<int?, T>) {
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

}