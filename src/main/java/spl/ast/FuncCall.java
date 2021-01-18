package spl.ast;

import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.Undefined;
import spl.interpreter.splObjects.SplCallable;
import spl.interpreter.splObjects.SplMethod;
import spl.interpreter.splObjects.SplObject;
import spl.util.*;

import java.io.IOException;

public class FuncCall extends Expression {

    Expression callObj;
    Arguments arguments;

    public FuncCall(LineFilePos lineFile) {
        super(lineFile);
    }

    public FuncCall(Expression callObj, Arguments arguments, LineFilePos lineFile) {
        super(lineFile);

        this.callObj = callObj;
        this.arguments = arguments;
    }

    public static FuncCall reconstruct(BytesIn in, LineFilePos lineFilePos) throws Exception {
        Expression callObj = Reconstructor.reconstruct(in);
        Arguments arguments = Reconstructor.reconstruct(in);
        return new FuncCall(callObj, arguments, lineFilePos);
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        callObj.save(out);
        arguments.save(out);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        SplElement leftTv = callObj.evaluate(env);
        if (env.hasException()) return Undefined.ERROR;
        if (SplElement.isPrimitive(leftTv)) {
            return SplInvokes.throwExceptionWithError(
                    env,
                    Constants.TYPE_ERROR,
                    "Element '" + leftTv + "' is not callable.",
                    lineFile);
        }
        SplObject obj = env.getMemory().get((Reference) leftTv);
        if (obj instanceof SplCallable) {
            SplCallable function = (SplCallable) obj;

            EvaluatedArguments ea = arguments.evalArgs(env);
            if (env.hasException()) return Undefined.ERROR;
            if (function instanceof SplMethod && env.hasName(Constants.THIS)) {
                // calling a method inside a method in class
                Reference thisPtr = (Reference) env.get(Constants.THIS, lineFile);
                ea.insertThis(thisPtr);
            }
            Reference[] generics = evalGenerics(env);
            if (env.hasException()) return Undefined.ERROR;
            return function.call(ea, generics, env, lineFile);
        } else {
            return SplInvokes.throwExceptionWithError(
                    env,
                    Constants.TYPE_ERROR,
                    "Object '" + obj + "' is not callable.",
                    lineFile);
        }
    }

    public Reference[] evalGenerics(Environment callingEnv) {
        if (!(callObj instanceof GenericNode)) return null;
        GenericNode genericNode = (GenericNode) callObj;
        Reference[] generics = new Reference[genericNode.getGenericLine().size()];
        for (int i = 0 ; i < generics.length; i++) {
            Node n = genericNode.getGenericLine().get(i);
            SplElement se = n.evaluate(callingEnv);
            if (se instanceof Reference) {
                generics[i] = (Reference) se;
            } else {
                SplInvokes.throwException(
                        callingEnv,
                        Constants.ARGUMENT_EXCEPTION,
                        "Generic must be callable.",
                        lineFile
                );
                return null;
            }
        }
        return generics;
    }

    @Override
    public String toString() {
        return callObj + " call(" + arguments + ")";
    }

    public Arguments getArguments() {
        return arguments;
    }

    public void setArguments(Arguments arguments) {
        this.arguments = arguments;
    }

    public Expression getCallObj() {
        return callObj;
    }

    public void setCallObj(Expression callObj) {
        this.callObj = callObj;
    }
}
