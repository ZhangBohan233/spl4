package ast;

import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import util.LineFile;

public class LambdaExpr extends Node {

    private Line parameters;

    public LambdaExpr(LineFile lineFile) {
        super(lineFile);
    }

    public void setParameters(Line parameters) {
        this.parameters = parameters;
    }

    public Line getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "lambda(" + parameters + ')';
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return null;
    }
}
