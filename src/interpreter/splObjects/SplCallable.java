package interpreter.splObjects;

import ast.Arguments;
import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import util.LineFile;

public abstract class SplCallable extends SplObject {

    public abstract SplElement call(SplElement[] evaluatedArgs, Environment callingEnv, LineFile lineFile);

    public SplElement call(Arguments arguments, Environment callingEnv) {
        return call(arguments.evalArgs(callingEnv), callingEnv, arguments.getLineFile());
    }
}
