package spl.interpreter.splObjects;

import spl.interpreter.SplThing;

public abstract class SplObject implements SplThing {
    /**
     * The number of times this object is marked
     */
    private int gcCount;
    /**
     * Number of remaining times that this object can survives a non-marked gc.
     */
    private int gcGeneration = gcGenerationLimit();

    public void incrementGcCount() {
        gcCount++;
    }

    public void clearGcCount() {
        gcCount = 0;
    }

    public boolean isGcMarked() {
        return gcCount > 0;
    }

    /**
     * If
     */
    public void nextGeneration() {
        gcGeneration--;
    }

    public void resetGeneration() {
        gcGeneration = gcGenerationLimit();
    }

    public boolean isDead() {
        return gcGeneration <= 0;
    }

    protected int gcGenerationLimit() {
        return 2;
    }
}
