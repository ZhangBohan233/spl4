package spl.interpreter.splObjects;

import spl.interpreter.SplThing;
import spl.interpreter.primitives.Reference;

import java.util.List;

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

    public List<Reference> listAttrReferences() {
        return null;
    }
}
