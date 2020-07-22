package interpreter.splObjects;

import ast.*;
import interpreter.ContractError;
import interpreter.SplException;
import interpreter.env.Environment;
import interpreter.env.FunctionEnvironment;
import interpreter.primitives.Bool;
import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;
import interpreter.types.*;
import parser.ParseError;
import util.LineFile;

public class Function extends UserFunction {

    private Contract contract;

    private final Node body;
    private final String definedName;

    private SplElement[] contractArgs;

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
            return "Anonymous function: {" + "}";
        } else {
            return "Function " + definedName + ": {" + "}";
        }
    }

    public void setContract(Line paramContractLine, Node rtnContractNode, Environment env) {
        if (contract != null) {
            throw new SplException("Contract already defined for function '" + definedName + "'. ",
                    rtnContractNode.getLineFile());
        }
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

        FunctionEnvironment scope = new FunctionEnvironment(definitionEnv, callingEnv, definedName);
        checkValidArgCount(evaluatedArgs.length, definedName);

        // TODO: variable length params
        checkParamContracts(evaluatedArgs, callingEnv, argLineFile);

        putArgsToScope(evaluatedArgs, scope);

        scope.getMemory().pushStack(scope);
        body.evaluate(scope);
        scope.getMemory().decreaseStack();

        SplElement rtnValue = scope.getReturnValue();
        checkRtnContract(rtnValue, callingEnv, lineFile);

        return rtnValue;
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
