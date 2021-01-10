package spl.interpreter.splObjects;

import spl.interpreter.SplThing;
import spl.interpreter.primitives.Reference;

import java.util.List;

public abstract class SplObject implements SplThing {

    public List<Reference> listAttrReferences() {
        return null;
    }
}
