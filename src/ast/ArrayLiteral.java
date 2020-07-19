package ast;

import interpreter.Memory;
import interpreter.env.Environment;
import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;
import util.LineFile;

import java.util.List;

public class ArrayLiteral extends NonEvaluate {

    private Line content;

    public ArrayLiteral(LineFile lineFile) {
        super(lineFile);
    }

    public void setContent(Line content) {
        this.content = content;
    }

//    public SplElement createAndAllocate(Type atomType, Environment env) {
//        int[][] a = new int[][]{};
//        // TODO: temporarily 不想写
//        return null;
//    }

    @Override
    public String toString() {
        return "ArrayCreation" + content;
    }
}
