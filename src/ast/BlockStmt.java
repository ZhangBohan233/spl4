package ast;

import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import util.LineFile;

import java.util.ArrayList;
import java.util.List;

public class BlockStmt extends AbstractStatement {

    private final List<Line> children = new ArrayList<>();

    public BlockStmt(LineFile lineFile) {
        super(lineFile);
    }

    public BlockStmt() {
        super(LineFile.LF_PARSER);
    }

    public void addLine(Line line) {
        children.add(line);
    }

    public List<Line> getLines() {
        return children;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n").append(" ".repeat(Math.max(0, Node.spaceCount))).append("{");
        Node.spaceCount += 2;
        for (Line line : children) {
            builder.append("\n").append(" ".repeat(Math.max(0, Node.spaceCount))).append(line.toString());
        }
        Node.spaceCount -= 2;
        builder.append("\n").append(" ".repeat(Math.max(0, Node.spaceCount))).append("}");
        return builder.toString();
    }

    @Override
    protected void internalProcess(Environment env) {
        for (Line line : children) {
            line.evaluate(env);
        }
    }

}
