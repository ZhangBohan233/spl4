package spl.interpreter.splObjects;

import spl.ast.StringLiteral;
import spl.interpreter.EvaluatedArguments;
import spl.interpreter.splErrors.NativeError;
import spl.interpreter.env.Environment;
import spl.interpreter.env.FunctionEnvironment;
import spl.interpreter.primitives.Pointer;
import spl.interpreter.primitives.SplElement;
import spl.util.Constants;
import spl.util.LineFile;
import spl.util.Utilities;

import java.util.Map;

public abstract class UserFunction extends SplCallable {

    /**
     * The environment where this function is defined
     */
    protected final Environment definitionEnv;

    protected final Function.Parameter[] params;
    protected final LineFile lineFile;

    private int minArg;
    private int maxArg;

    protected UserFunction(Function.Parameter[] parameters, Environment definitionEnv, LineFile lineFile) {
        this.params = parameters;
        this.definitionEnv = definitionEnv;
        this.lineFile = lineFile;

        minArg = maxArg = 0;
        for (SplCallable.Parameter param : params) {
            if (param.unpackCount == 0) {
                if (param.defaultValue == null) minArg++;
            } else maxArg = Integer.MAX_VALUE;
        }
        if (maxArg == 0) maxArg = parameters.length;
    }

    void setArgs(EvaluatedArguments evaluatedArgs, FunctionEnvironment scope) {
        int argIndex = 0;
        for (int i = 0; i < params.length; ++i) {
            Function.Parameter param = params[i];
            String paramName = param.name;

            if (param.constant) scope.defineConst(paramName, lineFile);
            else scope.defineVar(paramName, lineFile);  // declare param

            if (param.unpackCount == 0) {
                if (argIndex < evaluatedArgs.positionalArgs.size()) {
                    scope.setVar(paramName, evaluatedArgs.positionalArgs.get(argIndex++), lineFile);
                } else if (param.defaultValue != null) {
                    scope.setVar(paramName, param.defaultValue, lineFile);
                } else {
                    throw new NativeError("Unexpected error. ", lineFile);
                }
            } else if (param.unpackCount == 1) {  // *args
                int posArgc = evaluatedArgs.positionalArgs.size();
                final int unpackArgBegin = argIndex;
                SplElement[] unpackArgs = new SplElement[posArgc - unpackArgBegin];
                for (; argIndex < posArgc; argIndex++) {
                    unpackArgs[argIndex - unpackArgBegin] = evaluatedArgs.positionalArgs.get(argIndex);
                }

                Pointer arrPtr = SplArray.createArray(SplElement.POINTER, unpackArgs.length, scope);
                for (int j = 0; j < unpackArgs.length; j++) {
                    SplElement arg = unpackArgs[j];
                    if (arg instanceof Pointer) {
                        SplArray.setItemAtIndex(arrPtr, j, arg, scope, lineFile);
                    } else {
                        SplArray.setItemAtIndex(arrPtr, j,
                                Utilities.primitiveToWrapper(arg, scope, lineFile), scope, lineFile);
                    }
                }
                scope.setVar(paramName, arrPtr, lineFile);
            } else if (param.unpackCount == 2) {  // **kwargs
                int size = evaluatedArgs.keywordArgs.size();
                Pointer keyArrPtr = SplArray.createArray(SplElement.POINTER, size, scope);
                scope.getMemory().addTempPtr(keyArrPtr);
                Pointer valueArrPtr = SplArray.createArray(SplElement.POINTER, size, scope);
                scope.getMemory().addTempPtr(valueArrPtr);

                Instance.InstanceAndPtr dict =
                        Instance.createInstanceWithInitCall(
                                Constants.NAIVE_DICT,
                                EvaluatedArguments.of(keyArrPtr, valueArrPtr),
                                scope,
                                lineFile);
                scope.setVar(paramName, dict.pointer, lineFile);

                scope.getMemory().removeTempPtr(valueArrPtr);
                scope.getMemory().removeTempPtr(keyArrPtr);

                int j = 0;
                for (Map.Entry<String, SplElement> argEntry : evaluatedArgs.keywordArgs.entrySet()) {
                    SplElement keyStr = StringLiteral.createString(argEntry.getKey().toCharArray(), scope, lineFile);
                    SplArray.setItemAtIndex(keyArrPtr, j, keyStr, scope, lineFile);
                    SplElement val = argEntry.getValue();
                    if (SplElement.isPrimitive(val)) val = Utilities.primitiveToWrapper(val, scope, lineFile);
                    SplArray.setItemAtIndex(valueArrPtr, j, val, scope, lineFile);
                    j++;
                }
            }
        }
    }

//    void putArgsToScope(SplElement[] evaluatedArgs, FunctionEnvironment scope) {
//        int argIndex = 0;
//        for (int i = 0; i < params.length; ++i) {
//            Function.Parameter param = params[i];
//            String paramName = param.name;
//
//            if (param.constant) scope.defineConst(paramName, lineFile);
//            else scope.defineVar(paramName, lineFile);  // declare param
//
//            if (i < evaluatedArgs.length) {
//                // arg from call
//                scope.setVar(paramName, evaluatedArgs[argIndex], lineFile);
//
//            } else if (param.hasDefaultValue()) {
//                // default arg
//                scope.setVar(paramName, param.defaultValue, lineFile);
//            } else if (param.unpackCount == 1) {
//
//            } else {
//                throw new SplException("Unexpect argument error. ", lineFile);
//            }
//            argIndex++;
//        }
//    }

    @Override
    public int minArgCount() {
        return minArg;
    }

    @Override
    public int maxArgCount() {
        return maxArg;
    }
}
