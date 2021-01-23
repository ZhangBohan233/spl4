class InterruptedError(Exception) {
    fn __init__(msg=null, cause=null) {
        super.__init__(msg, cause);
    }
}


class Thread {
    var daemonic = false;
    var nativeThread = null;
    var interrupted = false;

    fn run() {
        throw new NotImplementedError();
    }

    fn setDaemon(daemonic: boolean?) {
        this.daemonic = daemonic;
    }

    /*
     * Interrupt this thread.
     *
     * This method does not kill this thread, but just set a signal that tells any subclass of this to stop.
     */
    fn interrupt() {
        if nativeThread is null {
            throw new InterruptedError("Thread is not started.");
        }
        interrupted = true;
        nativeThread.interrupt();
    }

    fn threadId() {
        rawId := nativeThread.threadId();
        if rawId == -1 {
            throw new InterruptedError("Thread is not started.");
        }
        return rawId;
    }

    /*
     * Starts this thread.
     */
    const fn start() {
        this.nativeThread = Invokes.thread(this, this.run, daemonic);
        nativeThread.start();
    }
}
