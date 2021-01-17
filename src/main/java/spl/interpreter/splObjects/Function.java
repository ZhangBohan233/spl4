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
import spl.util.Accessible;
import spl.util.Constants;
import spl.util.LineFilePos;

import java.util.Map;

public class Function extends UserFunction {

    protected final BlockStmt body;
    protected final String definedName;
    private final StringLiteralRef docRef;
    private Node rtnContract;
    private boolean hasContract = false;
    private String[] templates;

    /**
     * Constructor for regular function.
     */
    public Function(BlockStmt body, SplCallable.Parameter[] params, Environment definitionEnv,
                    String definedName, StringLiteralRef docRef, LineFilePos lineFile) {

        super(params, definitionEnv, lineFile);

        this.body = body;
        this.definedName = definedName;
        this.docRef = docRef;
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

    public void setContract(Environment env, Line paramContractLine, Node rtnContractNode, String[] templates) {
        if (paramContractLine.size() != params.length) {
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
        this.templates = templates;
        hasContract = true;
    }

    public SplElement call(Arguments arguments, Environment callingEnv) {
        EvaluatedArguments evaluatedArgs = arguments.evalArgs(callingEnv);
        if (callingEnv.hasException()) return Undefined.ERROR;

        return call(evaluatedArgs, callingEnv, arguments.lineFile);
    }

    private void checkParamContracts(EvaluatedArguments evaluatedArgs, FunctionEnvironment scope,
                                     Environment callingEnv, LineFilePos lineFile) {
        if (hasContract && callingEnv.getMemory().isCheckContract()) {
            int argIndex = 0;
            for (int i = 0; i < params.length; i++) {
                Parameter param = params[i];
                String location = "the " + i + "th argument";
                if (param.unpackCount == 0) {
                    if (argIndex < evaluatedArgs.positionalArgs.size()) {
                        callContract(
                                param.contract,
                                evaluatedArgs.positionalArgs.get(argIndex++),
                                scope,
                                callingEnv,
                                lineFile,
                                location);
                    } else if (param.hasDefaultValue()) {
                        // use default value
                        callContract(
                                param.contract,
                                param.defaultValue,
                                scope,
                                callingEnv,
                                lineFile,
                                location
                        );
                    } else {
                        throw new NativeError("Unexpected error. ");
                    }
                } else if (param.unpackCount == 1) {
                    for (; argIndex < evaluatedArgs.positionalArgs.size(); argIndex++) {
                        callContract(
                                param.contract,
                                evaluatedArgs.positionalArgs.get(argIndex),
                                scope,
                                callingEnv,
                                lineFile,
                                location);
                    }
                } else if (param.unpackCount == 2) {
                    for (Map.Entry<String, SplElement> entry : evaluatedArgs.keywordArgs.entrySet()) {
                        callContract(
                                param.contract,
                                entry.getValue(),
                                scope,
                                callingEnv,
                                lineFile,
                                location);
                    }
                } else {
                    throw new NativeError("Unexpected error. ");
                }
            }
        }
    }

    private void checkRtnContract(SplElement rtnValue, FunctionEnvironment scope,
                                  Environment callingEnv, LineFilePos lineFile) {
        if (hasContract && callingEnv.getMemory().isCheckContract()) {
            callContract(rtnContract, rtnValue, scope, callingEnv, lineFile, "return statement");
        }
    }

    private SplElement getContractFunction(Node conNode, FunctionEnvironment scope, LineFilePos lineFile) {
        if (conNode instanceof BinaryOperator) {
            BinaryOperator bo = (BinaryOperator) conNode;
            if (bo.getOperator().equals("or")) {
                Reference orFn = (Reference) scope.get(Constants.OR_FN, lineFile);
                Function function = scope.getMemory().get(orFn);
                Arguments args = new Arguments(new Line(lineFile, bo.getLeft(), bo.getRight()), lineFile);
                SplElement callRes = function.call(args, scope);
                if (scope.hasException()) {
                    return Undefined.ERROR;
                }
                return callRes;
            }
        }
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

    private void callContract(Node conNode,
                              SplElement arg,
                              FunctionEnvironment scope,
                              Environment callingEnv,
                              LineFilePos lineFile,
                              String location) {
        SplElement conFnPtrProb = getContractFunction(conNode, scope, lineFile);
        if (scope.hasException()) return;
        Reference conFnPtr = (Reference) conFnPtrProb;
        SplCallable callable = callingEnv.getMemory().get(conFnPtr);
        EvaluatedArguments contractArgs = EvaluatedArguments.of(arg);

        SplElement result = callable.call(contractArgs, callingEnv, lineFile);
        if (result instanceof Bool) {
            if (!((Bool) result).value) {
                SplInvokes.throwException(callingEnv,
                        Constants.CONTRACT_ERROR,
                        String.format("Contract violation when calling '%s', at %s. Got %s.",
                                definedName, location, arg),
                        lineFile);
            }
        } else {
            SplInvokes.throwException(callingEnv,
                    Constants.TYPE_ERROR,
                    "Contract function must return a boolean. ",
                    lineFile);
        }
    }

    private boolean defineGenerics(Reference[] generics, FunctionEnvironment scope,
                                   Environment callingEnv, LineFilePos lineFilePos) {
        if (generics == null) {
            if (templates == null) return true;
            else {
                for (String template : templates) {
                    scope.defineConstAndSet(template, callingEnv.get("any?", lineFilePos), lineFilePos);
                }
            }
        } else {
            if (templates == null) {
                SplInvokes.throwException(
                        callingEnv,
                        Constants.ARGUMENT_EXCEPTION,
                        "Function '" + definedName + "' does not support generic operation.",
                        lineFilePos
                );
                return false;
            } else {
                if (templates.length != generics.length) {
                    SplInvokes.throwException(
                            callingEnv,
                            Constants.ARGUMENT_EXCEPTION,
                            String.format("Function '%s' needs %d genes, %d given",
                                    definedName,
                                    templates.length,
                                    generics.length),
                            lineFilePos
                    );
                    return false;
                } else {
                    for (int i = 0; i < generics.length; i++) {
                        scope.defineConstAndSet(templates[i], generics[i], lineFilePos);
                    }
                }
            }
        }
        return true;
    }

    public SplElement call(EvaluatedArguments evaluatedArgs, Reference[] generics,
                           Environment callingEnv, LineFilePos argLineFile) {
        FunctionEnvironment scope = new FunctionEnvironment(definitionEnv, callingEnv, definedName);
        return callEssential(evaluatedArgs, generics, callingEnv, scope, argLineFile);
    }

    protected SplElement callEssential(EvaluatedArguments evaluatedArgs,
                                       Reference[] generics,
                                       Environment callingEnv,
                                       FunctionEnvironment scope,
                                       LineFilePos argLineFile) {
        checkValidArgCount(evaluatedArgs.positionalArgs.size() + evaluatedArgs.keywordArgs.size(),
                definedName, callingEnv, argLineFile);
        if (callingEnv.hasException()) return Undefined.ERROR;

        if (!defineGenerics(generics, scope, callingEnv, lineFile)) return Undefined.ERROR;

        checkParamContracts(evaluatedArgs, scope, callingEnv, argLineFile);

        setArgs(evaluatedArgs, scope, callingEnv, argLineFile);

        scope.getMemory().pushStack(scope, argLineFile);
        body.evaluate(scope);
        scope.getMemory().decreaseStack();

        if (scope.hasException()) return Undefined.ERROR;

        SplElement rtnValue = scope.temporaryRemoveRtn();
        checkRtnContract(rtnValue, scope, callingEnv, lineFile);
        scope.setReturn(rtnValue, lineFile);

        return rtnValue;
    }

    @Accessible
    public SplElement __doc__(Arguments args, Environment env, LineFilePos lineFilePos) {
        checkArgCount(args, 0, "Function.__doc__", env, lineFile);

        if (docRef == null) return Reference.NULL;
        else return docRef.evaluate(env);
    }
}
