package interpreter.splObjects;

import interpreter.SplThing;

public class SplObject extends SplThing {

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

    public int getGcCount() {
        return gcCount;
    }
}
