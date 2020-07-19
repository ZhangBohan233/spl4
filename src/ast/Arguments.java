package ast;

import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import util.LineFile;

import java.util.Arrays;

public class Arguments extends Node {

    private final Line line;

    public Arguments(Line line, LineFile lineFile) {
        super(lineFile);

        this.line = line;
    }

    public Line getLine() {
        return line;
    }

    public SplElement[] evalArgs(Environment callingEnv) {
        SplElement[] res = new SplElement[getLine().getChildren().size()];

        for (int i = 0; i < res.length; ++i) {
            Node argNode = getLine().getChildren().get(i);
            res[i] = argNode.evaluate(callingEnv);
        }
        return res;
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return null;
    }

    @Override
    public String toString() {
        return "Arg" + line;
    }
}
