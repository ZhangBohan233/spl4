package spl.interpreter.splObjects;

import spl.interpreter.SplThing;
import spl.interpreter.primitives.Reference;

import java.util.List;

public abstract class SplObject implements SplThing {

    /**
     * Returns a list containing all related references of this object, or null if not.
     * <p>
     * This method is used by garbage collector.
     *
     * @return a list containing all related references of this object, or null if not
     */
    public List<Reference> listAttrReferences() {
        return null;
    }
}
