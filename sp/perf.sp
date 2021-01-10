fn fib(n) {
    if n < 2 {
        return n;
    } else {
        return fib(n - 1) + fib(n - 2);
    }
}


fn main() {
    const t0 = clock();

    var i;
    for i = 0; i < 1_000_000; i++ {
    }

    const t1 = clock();
    print("Loop " + i + " time: " + (t1 - t0) + "ms");

    var arr = new int[10];

    for i = 0; i < 1_000_000; i++ {
        arr[i % 10] = 1 + arr[i % 10];
    }

    const t2 = clock();
    print("Array access " + i + " time: " + (t2 - t1) + "ms");

    const n = 25;
    print("fib=" + fib(n));

    const t3 = clock();
    print("fib " + n + " time: " + (t3 - t2) + "ms");

    lst := [];
    for i = 0; i < 1000; i++ {
        lst.append(new Integer(i));
    }

    const t4 = clock();
    print("List append " + i + " object time (no gc): " + (t4 - t3) + "ms");

    s := "test";
    for i = 0; i < 5_000; i++ {
        b := s + "a";
    }

    const t5 = clock();
    print("String add " + i + " time (with gc): " + (t5 - t4) + "ms");

    Invokes.gc();
    const t6 = clock();
    print("Single gc time: " + (t6 - t5) + "ms");
}