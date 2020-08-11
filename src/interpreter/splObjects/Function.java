package interpreter.splObjects;

import ast.*;
import interpreter.splErrors.ContractError;
import interpreter.EvaluatedArguments;
import interpreter.splErrors.NativeError;
import interpreter.env.Environment;
import interpreter.env.FunctionEnvironment;
import interpreter.primitives.Bool;
import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;
import interpreter.splErrors.TypeError;
import util.LineFile;

import java.util.Map;

public class Function extends UserFunction {

    private Node rtnContract;
    private boolean hasContract = false;

    protected final BlockStmt body;
    protected final String definedName;

    /**
     * Constructor for regular function.
     */
    public Function(BlockStmt body, SplCallable.Parameter[] params, Environment definitionEnv,
                    String definedName, LineFile lineFile) {

        super(params, definitionEnv, lineFile);

        this.body = body;
        this.definedName = definedName;
    }

    public Node getBody() {
        return body;
    }

    @Override
    public String toString() {
        if (definedName.isEmpty()) {
            return "Anonymous function";
        } else {
            return "Function " + definedName + ": {" + body.getLines().size() + " lines}";
        }
    }

    public void setContract(Line paramContractLine, Node rtnContractNode) {
        if (paramContractLine.size() != params.length) {
            throw new TypeError("Contracts must match the length of parameters. ", rtnContractNode.getLineFile());
        }

        for (int i = 0; i < paramContractLine.size(); i++) {
            if (params[i].contract != null) {
                throw new NativeError("Contract already defined for function '" + definedName + "'. ",
                        rtnContractNode.getLineFile());
            }
            params[i].contract = paramContractLine.get(i);
        }

        if (rtnContract != null) {
            throw new NativeError("Contract already defined for function '" + definedName + "'. ",
                    rtnContractNode.getLineFile());
        }
        rtnContract = rtnContractNode;
//        rtnContract = (Pointer) rtnContractNode.evaluate(env);

        hasContract = true;
    }

    public SplElement call(Arguments arguments, Environment callingEnv) {
        EvaluatedArguments evaluatedArgs = arguments.evalArgs(callingEnv);

        return call(evaluatedArgs, callingEnv, arguments.getLineFile());
    }

    private void checkParamContracts(EvaluatedArguments evaluatedArgs, Environment callingEnv, LineFile lineFile) {
        if (hasContract) {
            int argIndex = 0;
            for (Parameter param : params) {
                if (param.unpackCount == 0) {
                    callContract(
                            param.contract,
                            evaluatedArgs.positionalArgs.get(argIndex++),
                            callingEnv,
                            lineFile);
                } else if (param.unpackCount == 1) {
                    for (; argIndex < evaluatedArgs.positionalArgs.size(); argIndex++) {
                        callContract(param.contract, evaluatedArgs.positionalArgs.get(argIndex), callingEnv, lineFile);
                    }
                } else if (param.unpackCount == 2) {
                    for (Map.Entry<String, SplElement> entry : evaluatedArgs.keywordArgs.entrySet()) {
                        callContract(param.contract, entry.getValue(), callingEnv, lineFile);
                    }
                } else {
                    throw new NativeError("Unexpected error. ");
                }
            }
        }
    }

    private void checkRtnContract(SplElement rtnValue, Environment callingEnv, LineFile lineFile) {
        if (hasContract) {
            callContract(rtnContract, rtnValue, callingEnv, lineFile);
        }
    }

    private void callContract(Node conNode, SplElement arg, Environment callingEnv, LineFile lineFile) {
        Pointer conFnPtr = (Pointer) conNode.evaluate(definitionEnv);
        SplCallable callable = (SplCallable) callingEnv.getMemory().get(conFnPtr);
        EvaluatedArguments contractArgs = EvaluatedArguments.of(arg);

        SplElement result = callable.call(contractArgs, callingEnv, lineFile);
        if (result instanceof Bool) {
            if (!((Bool) result).value) {
                throw new ContractError("Contract violation when calling '" + definedName + "'. " +
                        "Got " + arg + ". ", lineFile);
            }
        } else {
            throw new TypeError("Contract function must return a boolean. ", lineFile);
        }
    }

    public SplElement call(EvaluatedArguments evaluatedArgs, Environment callingEnv, LineFile argLineFile) {
        FunctionEnvironment scope = new FunctionEnvironment(definitionEnv, callingEnv, definedName);
        return callEssential(evaluatedArgs, callingEnv, scope, argLineFile);
    }

    protected SplElement callEssential(EvaluatedArguments evaluatedArgs, Environment callingEnv,
                                       FunctionEnvironment scope, LineFile argLineFile) {
        checkValidArgCount(evaluatedArgs.positionalArgs.size(), definedName);

        checkParamContracts(evaluatedArgs, callingEnv, argLineFile);

        setArgs(evaluatedArgs, scope);

        scope.getMemory().pushStack(scope, argLineFile);
        body.evaluate(scope);
        scope.getMemory().decreaseStack();

        if (scope.hasException()) {
            return null;
        }

        SplElement rtnValue = scope.getReturnValue();
        checkRtnContract(rtnValue, callingEnv, lineFile);

        return rtnValue;
    }
}
