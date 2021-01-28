class A {
    var a = 1;
    var b = 2;
    var c = 3;
}

fn main() {
    var i;
    b := new A();
    b = new A();
    arr := new Object?[4];
    for i = 0; i < 4; i++ {
        arr[i] = new Integer(i);
    }
    Invokes.gc();

    print(arr[1]);
    return 1;
}