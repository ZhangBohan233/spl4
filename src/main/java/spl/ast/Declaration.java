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

public class Declaration extends Expression {

    public static final int VAR = 1;
    public static final int CONST = 2;
    public static final int USELESS = 3;

    public static final int PUBLIC = 11;
    public static final int PROTECTED = 12;
    public static final int PRIVATE = 13;

    public final String declaredName;
    public final int level;
    public final int access;

    public Declaration(int level, int access, String name, LineFilePos lineFile) {
        super(lineFile);

        this.declaredName = name;
        this.level = level;
        this.access = access;
    }

    public static Declaration reconstruct(BytesIn is, LineFilePos lineFilePos) throws Exception {
        String name = is.readString();
        int level = is.readInt();
        int access = is.readInt();
        return new Declaration(level, access, name, lineFilePos);
    }

    public String getLevelString() {
        String modifier = switch (access) {
            case PROTECTED -> "protected ";
            case PRIVATE -> "private ";
            default -> "";
        };
        return modifier + switch (level) {
            case VAR -> "var ";
            case CONST -> "const ";
            default -> "";
        };
    }

    @Override
    public String toString() {
        return getLevelString() + declaredName;
    }

    @Override
    protected SplElement internalEval(Environment env) {
        if (level == VAR) {
            switch (access) {
                case PROTECTED -> env.defineProtectedVar(declaredName, lineFile);
                case PRIVATE -> env.definePrivateVar(declaredName, lineFile);
                default -> env.defineVar(declaredName, lineFile);
            }
        } else if (level == CONST) {
            switch (access) {
                case PROTECTED -> env.defineProtectedConst(declaredName, lineFile);
                case PRIVATE -> env.definePrivateConst(declaredName, lineFile);
                default -> env.defineConst(declaredName, lineFile);
            }
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
