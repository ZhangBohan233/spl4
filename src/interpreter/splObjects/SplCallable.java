package interpreter.splObjects;

import ast.Arguments;
import interpreter.env.Environment;
import interpreter.primitives.SplElement;

public abstract class SplCallable extends SplObject {

    public abstract SplElement call(Arguments arguments, Environment callingEnv);
}
