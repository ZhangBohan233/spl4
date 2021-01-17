package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.Undefined;
import spl.interpreter.splObjects.Function;
import spl.interpreter.splObjects.SplMethod;
import spl.util.*;

import java.io.IOException;
import java.util.Map;

public class ContractNode extends Statement {

    private final String fnName;
    private final Line paramContracts;
    private final Expression rtnContract;
    private final Line templateLine;

    public ContractNode(String fnName, Line paramContracts, Expression rtnContract, Line templateLine,
                        LineFilePos lineFile) {
        super(lineFile);

        this.fnName = fnName;
        this.paramContracts = paramContracts;
        this.rtnContract = rtnContract;
        this.templateLine = templateLine;
    }

    public static String[] getDefinedTemplates(Line templateLine, Environment env, LineFilePos lineFilePos) {
        if (templateLine != null) {
            String[] templateNames = new String[templateLine.size()];
            for (int i = 0; i < templateNames.length; i++) {
                Node n = templateLine.get(i);
                if (n instanceof NameNode) {
                    templateNames[i] = ((NameNode) n).getName();
                } else {
                    SplInvokes.throwException(
                            env,
                            Constants.NAME_ERROR,
                            "Template definition with non-name value.",
                            lineFilePos
                    );
                    return null;
                }
            }
            return templateNames;
        }
        return null;
    }

    public static ContractNode reconstruct(BytesIn is, LineFilePos lineFilePos) throws Exception {
        String name = is.readString();
        Line param = Reconstructor.reconstruct(is);
        Expression rtn = Reconstructor.reconstruct(is);
        boolean hasTemplate = is.readBoolean();
        Line templateLine = null;
        if (hasTemplate) templateLine = Reconstructor.reconstruct(is);
        return new ContractNode(name, param, rtn, templateLine, lineFilePos);
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        out.writeString(fnName);
        paramContracts.save(out);
        rtnContract.save(out);
        out.writeBoolean(templateLine != null);
        if (templateLine != null) templateLine.save(out);
    }

    @Override
    protected void internalProcess(Environment env) {
        String[] templateNames = getDefinedTemplates(templateLine, env, lineFile);
        if (env.hasException()) return;

        Reference fnPtr = (Reference) env.get(fnName, getLineFile());
        Function function = env.getMemory().get(fnPtr);
        function.setContract(env, paramContracts, rtnContract, templateNames);
    }

    public void evalAsMethod(Map<String, Reference> classMethods, String className, Environment classDefEnv) {
        String[] templateNames = getDefinedTemplates(templateLine, classDefEnv, lineFile);
        if (classDefEnv.hasException()) return;

        Reference methodPtr = classMethods.get(fnName);
        if (methodPtr == null) {
            SplInvokes.throwException(classDefEnv, Constants.NAME_ERROR, "Method '" + fnName + "' is not defined.",
                    lineFile);
            return;
        }
        SplMethod method = classDefEnv.getMemory().get(methodPtr);
        paramContracts.getChildren().add(0, new NameNode(className + "?", lineFile));
        method.setContract(classDefEnv, paramContracts, rtnContract, templateNames);
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
