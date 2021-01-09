package spl.interpreter.splObjects;

import spl.interpreter.SplThing;

public abstract class SplObject implements SplThing {
    /**
     * The number of times this object is marked
     */
    private int gcCount;

    public void incrementGcCount() {
        gcCount++;
    }

    public void clearGcCount() {
        gcCount = 0;
    }

    public boolean isGcMarked() {
        return gcCount > 0;
    }
}
