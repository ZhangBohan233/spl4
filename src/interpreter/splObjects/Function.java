package interpreter.splObjects;

import ast.*;
import interpreter.ContractError;
import interpreter.Memory;
import interpreter.SplException;
import interpreter.env.Environment;
import interpreter.env.FunctionEnvironment;
import interpreter.primitives.Bool;
import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;
import interpreter.types.*;
import parser.ParseError;
import util.LineFile;

import java.util.ArrayList;
import java.util.List;

public class Function extends SplCallable {

    /**
     * The environment where this function is defined
     */
    private final Environment definitionEnv;

    private final Parameter[] params;  // only Declaration and Assignment

    private Contract contract;

    private final Node body;
    private final LineFile lineFile;
    private final String definedName;

    private final boolean isLambda;
//    private final boolean isAbstract;

    private SplElement[] contractArgs;

    /**
     * Constructor for regular function.
     */
    public Function(BlockStmt body, Parameter[] params, Environment definitionEnv,
                    String definedName, LineFile lineFile) {

        this.body = body;
        this.params = params;
        this.definitionEnv = definitionEnv;
        this.lineFile = lineFile;
        this.definedName = definedName;

        isLambda = false;
//        isAbstract = false;
    }

//    /**
//     * Constructor for abstract function.
//     */
//    public Function(List<Parameter> params, CallableType funcType, Environment definitionEnv,
//                    String definedName, LineFile lineFile) {
//        super(funcType);
//
//        this.body = null;
//        this.params = params;
//        this.definitionEnv = definitionEnv;
//        this.lineFile = lineFile;
//        this.definedName = definedName;
//
//        isLambda = false;
//        isAbstract = true;
//    }

    /**
     * Constructor for lambda expression.
     *
     * @param body          one-line function body
     * @param params        parameters
     * @param definitionEnv environment where this lambda is defined
     * @param lineFile      line and file
     */
    public Function(Node body, Parameter[] params, Environment definitionEnv,
                    LineFile lineFile) {

        this.body = body;
        this.params = params;
        this.definitionEnv = definitionEnv;
        this.lineFile = lineFile;
        this.definedName = "";

        isLambda = true;
//        isAbstract = false;
    }

    public Node getBody() {
        return body;
    }

    public Environment getDefinitionEnv() {
        return definitionEnv;
    }

    @Override
    public String toString() {
        if (definedName.isEmpty()) {
            return "Anonymous function: {" + "}";
        } else {
            return "Function " + definedName + ": {" + "}";
        }
    }

    public void setContract(Line paramContractLine, Node rtnContractNode, Environment env) {
        if (paramContractLine.size() != params.length) {
            throw new TypeError("Contracts must match the length of parameters. ", rtnContractNode.getLineFile());
        }

        Pointer[] paramContracts = new Pointer[paramContractLine.size()];
        for (int i = 0; i < paramContracts.length; i++) {
            Pointer conFnPtr = (Pointer) paramContractLine.get(i).evaluate(env);
            paramContracts[i] = conFnPtr;
        }
        Pointer rtnConFnPtr = (Pointer) rtnContractNode.evaluate(env);

        this.contract = new Contract(paramContracts, rtnConFnPtr);

        this.contractArgs = new SplElement[1];
    }

    public SplElement call(Arguments arguments, Environment callingEnv) {
        SplElement[] evaluatedArgs = arguments.evalArgs(callingEnv);

        return call(evaluatedArgs, callingEnv, arguments.getLineFile());
    }

    private void checkParamContracts(SplElement[] evaluatedArgs, Environment callingEnv, LineFile lineFile) {
        if (contract != null) {
            for (int i = 0; i < evaluatedArgs.length; i++) {
                callContract(contract.paramContracts[i], evaluatedArgs[i], callingEnv, lineFile);
            }
        }
    }

    private void checkRtnContract(SplElement rtnValue, Environment callingEnv, LineFile lineFile) {
        if (contract != null) {
            callContract(contract.rtnContract, rtnValue, callingEnv, lineFile);
        }
    }

    private void callContract(Pointer conFnPtr, SplElement arg, Environment callingEnv, LineFile lineFile) {
        SplCallable callable = (SplCallable) callingEnv.getMemory().get(conFnPtr);
        contractArgs[0] = arg;

        SplElement result = callable.call(contractArgs, callingEnv, lineFile);
        if (result instanceof Bool) {
            if (!((Bool) result).value) {
                throw new ContractError(lineFile);
            }
        } else {
            throw new TypeError("Contract function must return a boolean. ", lineFile);
        }
    }

    public SplElement call(SplElement[] evaluatedArgs, Environment callingEnv, LineFile argLineFile) {
//        if (isAbstract) {
//            throw new SplException("Function is not implemented. ", lineFile);
//        }
        assert body != null;

        FunctionEnvironment scope = new FunctionEnvironment(definitionEnv, callingEnv, definedName);
        if (evaluatedArgs.length < minArgCount() || evaluatedArgs.length > maxArgCount()) {
            throw new SplException("Arguments length does not match parameters. Expect " +
                    minArgCount() + ", got " + evaluatedArgs.length + ". ", argLineFile);
        }

        // TODO: variable length params
        checkParamContracts(evaluatedArgs, callingEnv, argLineFile);

        for (int i = 0; i < params.length; ++i) {
            Parameter param = params[i];
            String paramName = param.name;
            scope.defineVar(paramName, lineFile);  // declare param
            if (i < evaluatedArgs.length) {
                // arg from call
                scope.setVar(paramName, evaluatedArgs[i], lineFile);

            } else if (param.hasDefaultTv()) {
                // default arg
                scope.setVar(paramName, param.defaultTv, lineFile);
            } else {
                throw new SplException("Unexpect argument error. ", lineFile);
            }
        }

        scope.getMemory().pushStack(scope);
        SplElement evalResult = body.evaluate(scope);
        scope.getMemory().decreaseStack();

        if (isLambda) return evalResult;  // since lambda expression cannot have contract

        SplElement rtnValue = scope.getReturnValue();
        checkRtnContract(rtnValue, callingEnv, lineFile);

        return rtnValue;

//        if (funcType.getRType().equals(PrimitiveType.TYPE_VOID)) {
//            if (rtnVal == null) {
//                return TypeValue.VOID;
//            } else {
//                throw new TypeError("Function with void return type returns non-void value. ", lineFile);
//            }
//        } else {
//            if (rtnVal == null) {
//                throw new TypeError("Function with non-void return type returns nothing. ", lineFile);
//            } else {
//                TypeValue realRtnVal;
//                if (!funcType.getRType().isPrimitive() && rtnVal.getType().isPrimitive()) {
//                    // Probable primitive wrapper case: example:
//                    //
//                    // fn f() Integer {
//                    //     return 0;
//                    // }
//                    realRtnVal = TypeValue.convertPrimitiveToWrapper(rtnVal, scope, lineFile);
//                } else if (funcType.getRType().isPrimitive() && !rtnVal.getType().isPrimitive()) {
//                    // Probable primitive wrapper case: example:
//                    //
//                    // fn f() int {
//                    //     return new Integer(0);
//                    // }
//                    realRtnVal = TypeValue.convertWrapperToPrimitive(rtnVal, scope, lineFile);
//                } else {
//                    realRtnVal = rtnVal;
//                }
//
//                if (!funcType.getRType().isSuperclassOfOrEquals(realRtnVal.getType(), callingEnv)) {
//                    throw new TypeError("Declared return type: " + funcType.getRType() + ", actual returning " +
//                            "type: " + realRtnVal.getType() + ". ", argLineFile);
//                }
//                return realRtnVal;
//            }
//        }
    }

//    private Integer gg() {
//        return 0;
//    }

    public int minArgCount() {
        int c = 0;
        for (Parameter param : params) {
            if (!param.hasDefaultTv()) c++;
        }
        return c;
    }

    public int maxArgCount() {
        return params.length;  // TODO: check unpack
    }

    public static Parameter[] evalParamTypes(Line parameters, Environment env) {
        Parameter[] params = new Parameter[parameters.getChildren().size()];

        boolean hasDefault = false;
        for (int i = 0; i < parameters.getChildren().size(); ++i) {
            Node node = parameters.getChildren().get(i);
            if (node instanceof NameNode) {
                if (hasDefault) {
                    throw new ParseError("Positional parameter cannot occur behind optional parameter. ",
                            node.getLineFile());
                }
                params[i] = new Parameter(((NameNode) node).getName());
                continue;
            } else if (node instanceof Assignment) {
                hasDefault = true;
                Assignment assignment = (Assignment) node;
                if (assignment.getLeft() instanceof NameNode) {
                    Parameter p = new Parameter(((NameNode) assignment.getLeft()).getName(),
                            assignment.getRight().evaluate(env));
                    params[i] = p;
                    continue;
                }
            }
            throw new ParseError("Unexpected parameter syntax. ", node.getLineFile());
        }
        return params;
    }

    public static class Parameter {
        public final String name;
        public final SplElement defaultTv;

        Parameter(String name, SplElement typeValue) {
            this.name = name;
            this.defaultTv = typeValue;
        }

        Parameter(String name) {
            this.name = name;
            this.defaultTv = null;
        }

        public boolean hasDefaultTv() {
            return defaultTv != null;
        }
    }

    public static class Contract {
        public final Pointer[] paramContracts;
        public final Pointer rtnContract;

        public Contract(Pointer[] paramContracts, Pointer rtnContract) {
            this.paramContracts = paramContracts;
            this.rtnContract = rtnContract;
        }
    }
}
