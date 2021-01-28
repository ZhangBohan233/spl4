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
        s = "s";
        for i := 0; i < 40; i++ {
            s += "a";
        }
    }
}


fn main() {
    arr := new AThread?[2];
    for i := 0; i < arr.length; i++ {
        arr[i] = new AThread();
    }
    for at in arr {
        at.start();
    }
}