package ast;

import interpreter.EvaluatedArguments;
import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import lexer.SyntaxError;
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

    public EvaluatedArguments evalArgs(Environment callingEnv) {
        EvaluatedArguments evaluatedArguments = new EvaluatedArguments();

        boolean kwargBegins = false;

        int argc = getLine().size();
        for (int i = 0; i < argc; ++i) {
            Node argNode = getLine().get(i);

            if (argNode instanceof Assignment) {
                NameNode leftNode = (NameNode) ((Assignment) argNode).getLeft();
                evaluatedArguments.keywordArgs.put(
                        leftNode.getName(), ((Assignment) argNode).getRight().evaluate(callingEnv));
                kwargBegins = true;
            } else {
                if (kwargBegins)
                    throw new SyntaxError("Positional arguments follows keyword arguments. ",
                            argNode.getLineFile());
                evaluatedArguments.positionalArgs.add(argNode.evaluate(callingEnv));
            }
        }
        return evaluatedArguments;
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
