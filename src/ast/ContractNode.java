package ast;

import interpreter.env.Environment;
import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;
import interpreter.splObjects.Function;
import util.LineFile;

public class ContractNode extends Node {

    private final String fnName;
    private final Line paramContracts;
    private final Node rtnContract;

    public ContractNode(String fnName, Line paramContracts, Node rtnContract, LineFile lineFile) {
        super(lineFile);

        this.fnName = fnName;
        this.paramContracts = paramContracts;
        this.rtnContract = rtnContract;
    }

    @Override
    protected SplElement internalEval(Environment env) {
        Pointer fnPtr = (Pointer) env.get(fnName, getLineFile());
        Function function = (Function) env.getMemory().get(fnPtr);
        function.setContract(paramContracts, rtnContract, env);
        return null;
    }

    @Override
    public String toString() {
        return "Contract (" + paramContracts + ") -> " + rtnContract + ";";
    }
}
