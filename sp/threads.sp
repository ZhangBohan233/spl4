import threading

fn foo(msg, i, gap, id) {
    print("%s %d %d".format(msg, i, id));
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
            if interrupted {
                return;
            }
            foo(msg, i, gap, threadId());
        }
    }
}

fn main() {
    a := new AThread("a", 500);
    b := new AThread("b", 300);
    a.start();
    b.start();
    sleep(3000);
    print("Main terminated.");
    //a.interrupt();
}