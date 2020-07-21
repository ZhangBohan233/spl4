package ast;

import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import util.LineFile;

import java.util.ArrayList;
import java.util.List;

public class Line extends Node {
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
}
