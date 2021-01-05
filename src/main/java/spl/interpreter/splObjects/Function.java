package spl.interpreter.splObjects;

import spl.ast.*;
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

import java.util.Map;

public class Function extends UserFunction {

    protected final BlockStmt body;
    protected final String definedName;
    private Node rtnContract;
    private boolean hasContract = false;

    /**
     * Constructor for regular function.
     */
    public Function(BlockStmt body, SplCallable.Parameter[] params, Environment definitionEnv,
                    String definedName, LineFilePos lineFile) {

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

    public void setContract(Environment env, Line paramContractLine, Node rtnContractNode) {
        if (paramContractLine.size() != params.length) {
//            throw new NativeTypeError("Contracts must match the length of parameters. ", rtnContractNode.getLineFile());
            SplInvokes.throwException(
                    env,
                    Constants.TYPE_ERROR,
                    "Contracts must match the length of parameters.",
                    lineFile);
            return;
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

        return call(evaluatedArgs, callingEnv, arguments.lineFile);
    }

    private void checkParamContracts(EvaluatedArguments evaluatedArgs, Environment callingEnv, LineFilePos lineFile) {
        if (hasContract && callingEnv.getMemory().isCheckContract()) {
            int argIndex = 0;
            for (Parameter param : params) {
                if (param.unpackCount == 0) {
                    if (argIndex < evaluatedArgs.positionalArgs.size()) {
                        callContract(
                                param.contract,
                                evaluatedArgs.positionalArgs.get(argIndex++),
                                callingEnv,
                                lineFile);
                    } else if (param.hasDefaultValue()) {
                        // use default value
                        callContract(
                                param.contract,
                                param.defaultValue,
                                callingEnv,
                                lineFile
                        );
                    } else {
                        throw new NativeError("Unexpected error. ");
                    }
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

    private void checkRtnContract(SplElement rtnValue, Environment callingEnv, LineFilePos lineFile) {
        if (hasContract && callingEnv.getMemory().isCheckContract()) {
            callContract(rtnContract, rtnValue, callingEnv, lineFile);
        }
    }

    private Reference getContractFunction(Node conNode, Environment callingEnv, LineFilePos lineFile) {
        if (conNode instanceof BinaryOperator) {
            BinaryOperator bo = (BinaryOperator) conNode;
            if (bo.getOperator().equals("or")) {
                Reference orFn = (Reference) callingEnv.get(Constants.OR_FN, lineFile);
                Function function = (Function) callingEnv.getMemory().get(orFn);
                Arguments args = new Arguments(new Line(lineFile, bo.getLeft(), bo.getRight()), lineFile);
                return (Reference) function.call(args, callingEnv);
            }
        }
        SplElement res = conNode.evaluate(definitionEnv);
        if (res instanceof Reference) return (Reference) res;
        else {
            SplInvokes.throwException(
                    callingEnv,
                    Constants.TYPE_ERROR,
                    "Contract must be callable",
                    lineFile
            );
            return Reference.NULL_PTR;
        }
    }

    private void callContract(Node conNode, SplElement arg, Environment callingEnv, LineFilePos lineFile) {
        Reference conFnPtr = getContractFunction(conNode, callingEnv, lineFile);
        if (callingEnv.hasException()) return;
        SplCallable callable = (SplCallable) callingEnv.getMemory().get(conFnPtr);
        EvaluatedArguments contractArgs = EvaluatedArguments.of(arg);

        SplElement result = callable.call(contractArgs, callingEnv, lineFile);
        if (result instanceof Bool) {
            if (!((Bool) result).value) {
                SplInvokes.throwException(callingEnv,
                        Constants.CONTRACT_ERROR,
                        "Contract violation when calling '" + definedName + "'. " + "Got " + arg + ". ",
                        lineFile);
            }
        } else {
            SplInvokes.throwException(callingEnv,
                    Constants.TYPE_ERROR,
                    "Contract function must return a boolean. ",
                    lineFile);
        }
    }

    public SplElement call(EvaluatedArguments evaluatedArgs, Environment callingEnv, LineFilePos argLineFile) {
        FunctionEnvironment scope = new FunctionEnvironment(definitionEnv, callingEnv, definedName);
        return callEssential(evaluatedArgs, callingEnv, scope, argLineFile);
    }

    protected SplElement callEssential(EvaluatedArguments evaluatedArgs, Environment callingEnv,
                                       FunctionEnvironment scope, LineFilePos argLineFile) {
        checkValidArgCount(evaluatedArgs.positionalArgs.size(), definedName, argLineFile);

        checkParamContracts(evaluatedArgs, callingEnv, argLineFile);

        setArgs(evaluatedArgs, scope);

        scope.getMemory().pushStack(scope, argLineFile);
        body.evaluate(scope);
        scope.getMemory().decreaseStack();

        if (scope.hasException()) return Undefined.ERROR;

        SplElement rtnValue = scope.getReturnValue();
        checkRtnContract(rtnValue, callingEnv, lineFile);

        return rtnValue;
    }
}
