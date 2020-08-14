package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.env.EnvironmentError;
import spl.interpreter.primitives.Pointer;
import spl.interpreter.splObjects.Function;
import spl.interpreter.splObjects.SplMethod;
import spl.util.LineFile;

import java.util.Map;

public class ContractNode extends AbstractStatement {

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
    protected void internalProcess(Environment env) {
        Pointer fnPtr = (Pointer) env.get(fnName, getLineFile());
        Function function = (Function) env.getMemory().get(fnPtr);
        function.setContract(paramContracts, rtnContract);
    }

    public void evalAsMethod(Map<String, Pointer> classMethods, String className, Environment classDefEnv) {
        Pointer methodPtr = classMethods.get(fnName);
        if (methodPtr == null) throw new EnvironmentError("Method '" + fnName + "' is not defined. ", lineFile);
        SplMethod method = (SplMethod) classDefEnv.getMemory().get(methodPtr);
        paramContracts.getChildren().add(0, new NameNode(className + "?", lineFile));
        method.setContract(paramContracts, rtnContract);
    }

    @Override
    public String toString() {
        return "Contract (" + paramContracts + ") -> " + rtnContract + ";";
    }

    @Override
    public String reprString() {
        return "contract " + fnName;
    }

    public Node getRtnContract() {
        return rtnContract;
    }

    public Line getParamContracts() {
        return paramContracts;
    }
}
