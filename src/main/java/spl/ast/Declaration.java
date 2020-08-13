package spl.ast;


import spl.interpreter.primitives.SplElement;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Undefined;
import spl.lexer.SyntaxError;
import spl.util.LineFile;

public class Declaration extends AbstractExpression {

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
        if (level == VAR) {
            env.defineVar(declaredName, lineFile);
        } else if (level == CONST) {
            env.defineConst(declaredName, lineFile);
        } else {
            throw new SyntaxError("Unknown declaration type. ", lineFile);
        }
        return Undefined.UNDEFINED;
    }

}
