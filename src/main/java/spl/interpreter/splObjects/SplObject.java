package spl.interpreter.splObjects;

import spl.interpreter.SplThing;
import spl.interpreter.primitives.Reference;

import java.util.ArrayList;
import java.util.List;

public abstract class SplObject implements SplThing {

    /**
     * Returns a list containing all related references of this object
     * <p>
     * This method is used by garbage collector.
     *
     * @return a list containing all related references of this object
     */
    public List<Reference> listAttrReferences() {
        return new ArrayList<>();
    }
}
