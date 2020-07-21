package ast;


import interpreter.primitives.SplElement;

import interpreter.env.Environment;
import util.LineFile;

public class Declaration extends Node {

    public static final int VAR = 1;
    public static final int CONST = 2;
    public static final int USELESS = 3;

    public final String declaredName;

    public final int level;

    public Declaration(int level, String name, LineFile lineFile) {
        super(lineFile);

        this.declaredName = name;
        this.level = level;
    }

    @Override
    public String toString() {
        String levelStr = "";
        if (level == VAR) {
            levelStr = "var";
        } else if (level == CONST) {
            levelStr = "const";
        }
        return levelStr + " " + declaredName;
    }

    //    @Override
    protected SplElement internalEval(Environment env) {
        env.defineVar(declaredName, getLineFile());
//        Type rightEv = getRightTypeRep().evalType(env);
//        if (level == VAR) {
//            env.defineVar(getLeftName().getName(), rightEv, getLineFile());
//        } else if (level == CONST) {
//            env.defineConst(getLeftName().getName(), rightEv, getLineFile());
//        } else {
//            throw new SplException("Unknown declaration type. ", getLineFile());
//        }
        return null;
    }

}
