package spl.ast;

import spl.interpreter.EvaluatedArguments;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.env.Environment;
import spl.interpreter.splObjects.Macro;
import spl.interpreter.splObjects.SplMethod;
import spl.interpreter.splObjects.SplCallable;
import spl.interpreter.splObjects.SplObject;
import spl.interpreter.primitives.Pointer;
import spl.interpreter.splErrors.TypeError;
import spl.util.Constants;
import spl.util.LineFile;

public class FuncCall extends AbstractExpression {

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
        if (obj instanceof SplCallable) {
            SplCallable function = (SplCallable) obj;

            EvaluatedArguments ea = arguments.evalArgs(env);
            if (function instanceof SplMethod) {
                // calling a method inside class
                Pointer thisPtr = (Pointer) env.get(Constants.THIS, lineFile);
                ea.insertThis(thisPtr);
            }
            return function.call(ea, env, lineFile);
        } else if (obj instanceof Macro) {
            Macro macro = (Macro) obj;
            System.out.println(macro);
            return null;
        } else {
            throw new TypeError("Object '" + obj + "' is not callable. ", getLineFile());
        }
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
