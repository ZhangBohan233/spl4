package spl.interpreter.splObjects;

import spl.ast.*;
import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.env.FunctionEnvironment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.Undefined;
import spl.interpreter.splErrors.NativeError;
import spl.util.Accessible;
import spl.util.Constants;
import spl.util.LineFilePos;
import spl.util.Utilities;

import java.util.List;
import java.util.Map;

public class Function extends UserFunction {

    protected final BlockStmt body;
    protected final String definedName;
    private final StringLiteralRef docRef;
    /**
     * Pointer to an array of all annotation instances, empty array if none
     */
    @Accessible
    Reference __annotations__;
    private String[] templates;

    /**
     * Constructor for regular function.
     */
    public Function(BlockStmt body, SplCallable.Parameter[] params, Environment definitionEnv,
                    String definedName, StringLiteralRef docRef, Reference annotationRefs, LineFilePos lineFile) {

        super(params, definitionEnv, lineFile);

        this.body = body;
        this.definedName = definedName;
        this.docRef = docRef;
        this.__annotations__ = annotationRefs;
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

        return call(evaluatedArgs, callingEnv, arguments.getLineFile());
    }

    @SuppressWarnings("unused")  // maybe it will be useful later
    private void checkParamContracts(EvaluatedArguments evaluatedArgs, FunctionEnvironment scope,
                                     Environment callingEnv, LineFilePos lineFile) {
        if (hasContract && callingEnv.getMemory().isCheckContract()) {
            int argIndex = 0;
            for (int i = 0; i < params.length; i++) {
                Parameter param = params[i];
                String location = "the " + Utilities.numberToOrder(i) + " argument";
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

    private boolean defineGenerics(Reference[] generics, FunctionEnvironment scope,
                                   Environment callingEnv, LineFilePos lineFilePos) {
        if (generics == null) {
            if (templates == null) return true;
            else {
                for (String template : templates) {
                    scope.defineConstAndSet(template, callingEnv.get(Constants.ANY_TYPE, lineFilePos), lineFilePos);
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
        checkValidArgCount(evaluatedArgs.positionalArgs.size(), evaluatedArgs.keywordArgs.size(),
                definedName, callingEnv, argLineFile);
        if (callingEnv.hasException()) return Undefined.ERROR;

        if (!defineGenerics(generics, scope, callingEnv, lineFile)) return Undefined.ERROR;

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

    @Override
    public String getName() {
        return definedName;
    }

    @Override
    public List<Reference> listAttrReferences() {
        List<Reference> sup = super.listAttrReferences();
        sup.add(__annotations__);
        return sup;
    }

    @Accessible
    public SplElement __doc__(Arguments args, Environment env, LineFilePos lineFilePos) {
        checkArgCount(args, 0, "Function.__doc__", env, lineFile);

        if (docRef == null) return Reference.NULL;
        else return docRef.evaluate(env);
    }
}
