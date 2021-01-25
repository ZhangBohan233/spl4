package spl.ast;


import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.Undefined;
import spl.lexer.SyntaxError;
import spl.util.BytesIn;
import spl.util.BytesOut;
import spl.util.LineFilePos;

import java.io.IOException;
import java.util.Objects;

/**
 * Although it extends {@code Expression}, it is actually a statement.
 * <p>
 * It does not return any useful thing, but extends {@code Expression} because it can be the left hand side operand
 * of assignments.
 */
public class Declaration extends Expression {

    public static final int VAR = 1;
    public static final int CONST = 2;
    public static final int USELESS = 3;

    public final String declaredName;
    public final int level;

    public Declaration(int level, String name, LineFilePos lineFile) {
        super(lineFile);

        this.declaredName = name;
        this.level = level;
    }

    public static Declaration reconstruct(BytesIn is, LineFilePos lineFilePos) throws Exception {
        String name = is.readString();
        int level = is.readInt();
        return new Declaration(level, name, lineFilePos);
    }

    public String getLevelString() {
        return switch (level) {
            case VAR -> "var";
            case CONST -> "const";
            default -> "";
        };
    }

    @Override
    public String toString() {
        return getLevelString() + " " + declaredName;
    }

    @Override
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

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        out.writeString(declaredName);
        out.writeInt(level);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Declaration that = (Declaration) o;

        if (level != that.level) return false;
        return Objects.equals(declaredName, that.declaredName);
    }

    @Override
    public int hashCode() {
        int result = declaredName != null ? declaredName.hashCode() : 0;
        result = 31 * result + level;
        return result;
    }
}
