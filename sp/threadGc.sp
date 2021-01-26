import threading

class AThread(threading.Thread) {
    fn __init__() {

    }

    fn run() {
        s := "a";
        for i := 0; i < 1000; i++ {
            s += "x";
        }
    }
}


fn main() {
    arr := new AThread?[10];
    for i := 0; i < 10; i++ {
        arr[i] = new AThread();
    }
    for at in arr {
        at.start();
    }
}