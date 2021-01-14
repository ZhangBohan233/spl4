package spl.interpreter.splObjects;

import spl.ast.StringLiteral;
import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.env.FunctionEnvironment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.Undefined;
import spl.interpreter.splErrors.NativeError;
import spl.util.Constants;
import spl.util.LineFilePos;
import spl.util.Utilities;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class UserFunction extends SplCallable {

    /**
     * The environment where this function is defined
     */
    protected final Environment definitionEnv;

    protected final Function.Parameter[] params;
    protected final LineFilePos lineFile;

    private int minArg;
    private int maxArg;

    protected UserFunction(Function.Parameter[] parameters, Environment definitionEnv, LineFilePos lineFile) {
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

    private boolean hasParamName(String name) {
        for (Parameter param : params) {
            if (param.unpackCount == 2 || param.name.equals(name)) return true;
        }
        return false;
    }

    private boolean setArg(FunctionEnvironment scope,
                           Environment callingEnv,
                           String paramName,
                           SplElement value,
                           LineFilePos lineFilePos) {
        if (scope.get(paramName, lineFilePos) == Undefined.UNDEFINED) {
            scope.setVar(paramName, value, lineFilePos);
            return true;
        } else {
            SplInvokes.throwException(
                    callingEnv,
                    Constants.ARGUMENT_EXCEPTION,
                    "Argument '" + paramName + "' already defined.",
                    lineFile
            );
            return false;
        }
    }

    void setArgs(EvaluatedArguments evaluatedArgs,
                 FunctionEnvironment scope,
                 Environment callingEnv,
                 LineFilePos callingLf) {
        for (String key : evaluatedArgs.keywordArgs.keySet()) {
            if (!hasParamName(key)) {
                SplInvokes.throwException(
                        callingEnv,
                        Constants.ARGUMENT_EXCEPTION,
                        "Unexpected keyword argument '" + key + "'",
                        callingLf
                );
                return;
            }
        }

        Set<String> usedKwargs = new HashSet<>();
        boolean noKwParam = true;
        int argIndex = 0;
        for (int i = 0; i < params.length; ++i) {
            Parameter param = params[i];
            String paramName = param.name;

            if (param.constant) scope.defineConst(paramName, lineFile);
            else scope.defineVar(paramName, lineFile);  // declare param

            boolean success;
            if (param.unpackCount == 0) {
                if (argIndex < evaluatedArgs.positionalArgs.size()) {
                    success = setArg(scope, callingEnv, paramName,
                            evaluatedArgs.positionalArgs.get(argIndex++), lineFile);
                } else if (evaluatedArgs.keywordArgs.containsKey(paramName)) {
                    success = setArg(scope, callingEnv, paramName, evaluatedArgs.keywordArgs.get(paramName), lineFile);
                    usedKwargs.add(paramName);
                } else if (param.defaultValue != null) {
                    success = setArg(scope, callingEnv, paramName, param.defaultValue, lineFile);
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

                Reference arrPtr = SplArray.createArray(SplElement.POINTER, unpackArgs.length, scope);
                for (int j = 0; j < unpackArgs.length; j++) {
                    SplElement arg = unpackArgs[j];
                    if (arg instanceof Reference) {
                        SplArray.setItemAtIndex(arrPtr, j, arg, scope, lineFile);
                    } else {
                        SplArray.setItemAtIndex(arrPtr, j,
                                Utilities.primitiveToWrapper(arg, scope, lineFile), scope, lineFile);
                    }
                }
                success = setArg(scope, callingEnv, paramName, arrPtr, lineFile);
            } else if (param.unpackCount == 2) {  // **kwargs
                noKwParam = false;
                int size = evaluatedArgs.keywordArgs.size();
                Reference keyArrPtr = SplArray.createArray(SplElement.POINTER, size, scope);
                scope.getMemory().addTempPtr(keyArrPtr);
                Reference valueArrPtr = SplArray.createArray(SplElement.POINTER, size, scope);
                scope.getMemory().addTempPtr(valueArrPtr);

                Instance.InstanceAndPtr dict =
                        Instance.createInstanceWithInitCall(
                                Constants.NAIVE_DICT,
                                EvaluatedArguments.of(keyArrPtr, valueArrPtr),
                                scope,
                                lineFile);
                success = setArg(scope, callingEnv, paramName, dict.pointer, lineFile);

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
            } else {
                throw new NativeError("Unexpected error.");
            }
            if (!success) return;
        }
        if (noKwParam && usedKwargs.size() != evaluatedArgs.keywordArgs.size()) {
            StringBuilder msg = new StringBuilder("Some keyword argument(s) are already set in position arguments: ");
            for (String key : evaluatedArgs.keywordArgs.keySet()) {
                if (!usedKwargs.contains(key)) {
                    msg.append(key).append(", ");
                }
            }
            SplInvokes.throwException(
                    callingEnv,
                    Constants.ARGUMENT_EXCEPTION,
                    msg.toString(),
                    lineFile);
        }
    }

    @Override
    public int minArgCount() {
        return minArg;
    }

    @Override
    public int maxArgCount() {
        return maxArg;
    }
}
