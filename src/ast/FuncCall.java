package ast;

import interpreter.primitives.SplElement;
import interpreter.splObjects.Function;
import interpreter.env.Environment;
import interpreter.splObjects.SplCallable;
import interpreter.splObjects.SplObject;
import interpreter.primitives.Pointer;
import interpreter.types.TypeError;
import util.LineFile;

public class FuncCall extends Node {

    Node callObj;
    Arguments arguments;

    public FuncCall(LineFile lineFile) {
        super(lineFile);
    }

    public FuncCall(Node callObj, Arguments arguments, LineFile lineFile) {
        super(lineFile);

        this.callObj = callObj;
        this.arguments = arguments;
    }

    public void setCallObj(Node callObj) {
        this.callObj = callObj;
    }

    public void setArguments(Arguments arguments) {
        this.arguments = arguments;
    }

    @Override
    protected SplElement internalEval(Environment env) {
        SplElement leftTv = callObj.evaluate(env);
        if (SplElement.isPrimitive(leftTv)) {
            throw new TypeError("Element '" + leftTv + "' is not callable. ", getLineFile());
        }
        SplObject obj = env.getMemory().get((Pointer) leftTv);
        if (!(obj instanceof SplCallable)) {
            throw new TypeError("Object '" + obj + "' is not callable. ", getLineFile());
        }
        SplCallable function = (SplCallable) obj;

        return function.call(arguments, env);
    }

    @Override
    public String toString() {
        return callObj + " call(" + arguments + ")";
    }

    public Arguments getArguments() {
        return arguments;
    }

    public Node getCallObj() {
        return callObj;
    }
}
