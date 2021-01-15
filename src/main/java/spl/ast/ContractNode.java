package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.Reference;
import spl.interpreter.splObjects.Function;
import spl.interpreter.splObjects.SplMethod;
import spl.util.*;

import java.io.IOException;
import java.util.Map;

public class ContractNode extends Statement {

    private final String fnName;
    private final Line paramContracts;
    private final Expression rtnContract;

    public ContractNode(String fnName, Line paramContracts, Expression rtnContract, LineFilePos lineFile) {
        super(lineFile);

        this.fnName = fnName;
        this.paramContracts = paramContracts;
        this.rtnContract = rtnContract;
    }

    public static ContractNode reconstruct(BytesIn is, LineFilePos lineFilePos) throws Exception {
        String name = is.readString();
        Line param = Reconstructor.reconstruct(is);
        Expression rtn = Reconstructor.reconstruct(is);
        return new ContractNode(name, param, rtn, lineFilePos);
    }

    @Override
    protected void internalProcess(Environment env) {
        Reference fnPtr = (Reference) env.get(fnName, getLineFile());
        Function function = (Function) env.getMemory().get(fnPtr);
        function.setContract(env, paramContracts, rtnContract);
    }

    public void evalAsMethod(Map<String, Reference> classMethods, String className, Environment classDefEnv) {
        Reference methodPtr = classMethods.get(fnName);
        if (methodPtr == null) {
//            throw new EnvironmentError("Method '" + fnName + "' is not defined. ", lineFile);
            SplInvokes.throwException(classDefEnv, Constants.NAME_ERROR, "Method '" + fnName + "' is not defined.",
                    lineFile);
            return;
        }
        SplMethod method = (SplMethod) classDefEnv.getMemory().get(methodPtr);
        paramContracts.getChildren().add(0, new NameNode(className + "?", lineFile));
        method.setContract(classDefEnv, paramContracts, rtnContract);
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

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        out.writeString(fnName);
        paramContracts.save(out);
        rtnContract.save(out);
    }
}
