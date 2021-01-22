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

fn foo(a, **kwargs: int?) {
    print(kwargs);
}

fn main() {
    a := new A<int?, boolean?>();
    b := new B<float?>();
    b.test();
    print(b.super.__instance__);
    print(b.foo(1));
    print(listTemplates(A));
    genDict := genericDict(a);
    for key in listTemplates(A) {
        print(genDict[key]);
    }
    xx := genericOf(A?, int?, boolean?);
    print(xx(a));
    //print(int.__checker__);
}