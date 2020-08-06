fn fib(n) {
    if n < 2 {
        return n;
    } else {
        return fib(n - 1) + fib(n - 2);
    }
}


fn main() {
    const t0 = Invokes.clock();

    var i;
    for i = 0; i < 1_000_000; i++ {
    }

    const t1 = Invokes.clock();
    print("Loop " + i + " time: " + (t1 - t0) + "ms");

    var arr = new int[10];

    for i = 0; i < 1_000_000; i++ {
        arr[i % 10] = 1 + arr[i % 10];
    }

    const t2 = Invokes.clock();
    print("Array access " + i + " time: " + (t2 - t1) + "ms");

    const n = 25;
    print("fib=" + fib(n));

    const t3 = Invokes.clock();
    print("fib " + n + " time: " + (t3 - t2) + "ms");

    lst := [];
    for i = 0; i < 1000; i++ {
        lst.append(new Integer(i));
    }

    const t4 = Invokes.clock();
    print("List append " + i + " object time (no gc): " + (t4 - t3) + "ms");
}