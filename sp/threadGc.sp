import threading

class X {
    var o;
    fn __init__() {
        o = new Object();
    }
}

class AThread(threading.Thread) {
    fn __init__() {

    }

    fn run() {
        var s;
        for i := 0; i < 100; i++ {
            s = new X();
        }
    }
}


fn main() {
    arr := new AThread?[4];
    for i := 0; i < arr.length; i++ {
        arr[i] = new AThread();
    }
    for at in arr {
        at.start();
    }
}