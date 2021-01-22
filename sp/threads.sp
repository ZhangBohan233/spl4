import threading

sync fn foo(msg, i, gap) {
    print(msg + i);
    sleep(gap);
}

class AThread(threading.Thread) {
    var gap;
    var msg;

    fn __init__(msg, gap) {
        this.gap = gap;
        this.msg = msg;
    }

    fn run() {
        for i := 0; i < 10; i++ {
            foo(msg, i, gap);
        }
    }
}

fn main() {
    a := new AThread("a ", 500);
    b := new AThread("b ", 300);
    a.start();
    b.start();
    sleep(3000);
}