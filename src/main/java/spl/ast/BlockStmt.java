package spl.ast;

import spl.interpreter.env.Environment;
import spl.util.BytesIn;
import spl.util.BytesOut;
import spl.util.LineFilePos;
import spl.util.Reconstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BlockStmt extends Statement {

    private final List<Line> children = new ArrayList<>();

    public BlockStmt(LineFilePos lineFile) {
        super(lineFile);
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
            builder.append("\n")
                    .append(" ".repeat(Math.max(0, Node.spaceCount)))
                    .append(line.toString())
                    .append(';');
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

    @Override
    public String reprString() {
        return "Block of " + children.size() + " lines";
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        out.writeList(children);
    }

    public static BlockStmt reconstruct(BytesIn is, LineFilePos lineFilePos) throws Exception {
        BlockStmt bs = new BlockStmt(lineFilePos);
        List<Line> lines = is.readList();
        for (Line line : lines) {
            bs.addLine(line);
        }
        return bs;
    }
}
