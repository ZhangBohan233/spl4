class Thread {

    var daemonic = false;

    fn run() {
        throw new NotImplementedError();
    }

    fn setDaemon(daemonic: boolean?) {
        this.daemonic = daemonic;
    }

    const fn start() {
        Invokes.thread(this, this.run, daemonic);
    }
}
