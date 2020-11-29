package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.util.LineFile;

import java.util.ArrayList;
import java.util.List;

public class Line extends Expression {
    private final List<Node> children = new ArrayList<>();

    public Line(LineFile lineFile) {
        super(lineFile);
    }

    public Line() {
        super(LineFile.LF_PARSER);
    }

    public Node get(int index) {
        return children.get(index);
    }

    public void set(int index, Node value) {
        children.set(index, value);
    }

    public void add(Node ele) {
        children.add(ele);
    }

    public int size() {
        return children.size();
    }

    public List<Node> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return children.toString();
    }

    @Override
    protected SplElement internalEval(Environment env) {

        SplElement res = null;
        for (Node node : children) {
            res = node.evaluate(env);
        }
        return res;
    }

    @Override
    public String reprString() {
        return "Line of " + children.size() + " Elements";
    }
}
