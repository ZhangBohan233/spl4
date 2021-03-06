package spl.interpreter.splObjects;

import spl.ast.Node;
import spl.ast.StringLiteral;
import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.env.FunctionEnvironment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.Bool;
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

    protected Node rtnContract;
    protected boolean hasContract = false;

    private int minPosArg;
    private int maxPosArg;
    private int maxKwArg;

    protected UserFunction(Parameter[] parameters, Environment definitionEnv, LineFilePos lineFile) {
        this.params = parameters;
        this.definitionEnv = definitionEnv;
        this.lineFile = lineFile;

        for (Parameter param : parameters) {
            if (param.unpackCount == 0) {
                if (!param.hasDefaultValue()) {
                    minPosArg++;
                }
                maxPosArg++;
                maxKwArg++;
            } else if (param.unpackCount == 1) {
                maxPosArg = MAX_ARGS;
            } else {
                maxKwArg = MAX_ARGS;
            }
        }
    }

    public static SplElement getContractFunction(Node conNode, FunctionEnvironment scope, LineFilePos lineFile) {
        SplElement res = conNode.evaluate(scope);
        if (res instanceof Reference) return res;
        else {
            SplInvokes.throwException(
                    scope,
                    Constants.TYPE_ERROR,
                    "Contract must be callable",
                    lineFile
            );
            return Reference.NULL;
        }
    }

    protected boolean hasParamName(String name) {
        for (Parameter param : params) {
            if (param.unpackCount == 2 || param.name.equals(name)) return true;
        }
        return false;
    }

    protected boolean callContract(Node conNode,
                                   SplElement arg,
                                   FunctionEnvironment scope,
                                   Environment callingEnv,
                                   LineFilePos lineFile,
                                   String location) {
        SplElement conFnPtrProb = getContractFunction(conNode, scope, lineFile);
        if (scope.hasException()) return false;
        Reference conFnPtr = (Reference) conFnPtrProb;
        SplCallable callable = callingEnv.getMemory().get(conFnPtr);
        EvaluatedArguments contractArgs = EvaluatedArguments.of(arg);

        SplElement result = callable.call(contractArgs, callingEnv, lineFile);
        if (result instanceof Bool) {
            if (!((Bool) result).value) {
                SplInvokes.throwException(callingEnv,
                        Constants.CONTRACT_ERROR,
                        String.format("Contract violation when calling '%s', at %s. Expected '%s', got '%s'.",
                                scope.definedName,
                                location,
                                callable.getName(),
                                Utilities.typeName(arg, callingEnv, lineFile)),
                        conNode.getLineFile());
                return false;
            }
        } else {
            SplInvokes.throwException(callingEnv,
                    Constants.TYPE_ERROR,
                    "Contract function must return a boolean. ",
                    lineFile);
            return false;
        }
        return true;
    }

    private boolean setArg(FunctionEnvironment scope,
                           Environment callingEnv,
                           Parameter param,
                           SplElement value,
                           boolean checkContract,
                           LineFilePos lineFilePos,
                           String location) {
        if (scope.get(param.name, lineFilePos) == Undefined.UNDEFINED) {
            scope.setVar(param.name, value, lineFilePos);
            // set at first, then check. This is to make sure 'this' works
            if (checkContract) {
                return callContract(
                        param.contract,
                        value,
                        scope,
                        callingEnv,
                        lineFilePos,
                        location
                );
            }
            return true;
        } else {
            SplInvokes.throwException(
                    callingEnv,
                    Constants.ARGUMENT_EXCEPTION,
                    "Argument '" + param.name + "' already defined.",
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

        boolean checkContract = hasContract && callingEnv.getMemory().isCheckContract();

        Set<String> usedKwargs = new HashSet<>();
        boolean noKwParam = true;
        int argIndex = 0;
        for (Parameter param : params) {
            String paramName = param.name;
            String location = "the " + Utilities.numberToOrder(argIndex + 1) + " argument";

            if (param.constant) scope.defineConst(paramName, lineFile);
            else scope.defineVar(paramName, lineFile);  // declare param

            boolean success;
            if (param.unpackCount == 0) {
                if (argIndex < evaluatedArgs.positionalArgs.size()) {
                    success = setArg(scope, callingEnv, param,
                            evaluatedArgs.positionalArgs.get(argIndex++), checkContract, lineFile, location);
                } else if (evaluatedArgs.keywordArgs.containsKey(paramName)) {
                    success = setArg(
                            scope,
                            callingEnv,
                            param,
                            evaluatedArgs.keywordArgs.get(paramName),
                            checkContract,
                            lineFile,
                            location);
                    usedKwargs.add(paramName);
                } else if (param.defaultValue != null) {
                    success = setArg(scope, callingEnv, param, param.defaultValue, checkContract, lineFile, location);
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
                    // In this case, the only choice is check contract before actually set it.
                    // It should not have any bad effect because 'this' cannot be '*this'
                    if (checkContract && !callContract(
                            param.contract,
                            arg,
                            scope,
                            callingEnv,
                            lineFile,
                            "the " + Utilities.numberToOrder(unpackArgBegin + j + 1) + " argument")) {
                        return;
                    }
                    if (arg instanceof Reference) {
                        SplArray.setItemAtIndex(arrPtr, j, arg, scope, lineFile);
                    } else {
                        SplArray.setItemAtIndex(arrPtr, j,
                                Utilities.primitiveToWrapper(arg, scope, lineFile), scope, lineFile);
                    }
                }
                // check each args' contract instead of the array
                success = setArg(scope, callingEnv, param, arrPtr, false, lineFile, location);
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
                if (dict == null) return;
                success = setArg(scope, callingEnv, param, dict.pointer, false, lineFile, location);

                scope.getMemory().removeTempPtr(valueArrPtr);
                scope.getMemory().removeTempPtr(keyArrPtr);

                int j = 0;
                for (Map.Entry<String, SplElement> argEntry : evaluatedArgs.keywordArgs.entrySet()) {
                    SplElement keyStr = StringLiteral.createString(argEntry.getKey().toCharArray(), scope, lineFile);
                    SplArray.setItemAtIndex(keyArrPtr, j, keyStr, scope, lineFile);
                    SplElement val = argEntry.getValue();
                    if (checkContract && !callContract(
                            param.contract,
                            val,
                            scope,
                            callingEnv,
                            lineFile,
                            "the " + Utilities.numberToOrder(argIndex + j + 1) + " argument")) {
                        return;
                    }
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
    public int minPosArgCount() {
        return minPosArg;
    }

    @Override
    public int maxPosArgCount() {
        return maxPosArg;
    }

    @Override
    public int maxKwArgCount() {
        return maxKwArg;
    }
}
