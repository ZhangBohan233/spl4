package spl.ast;

import spl.interpreter.env.BlockEnvironment;
import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Bool;
import spl.util.BytesIn;
import spl.util.BytesOut;
import spl.util.LineFilePos;
import spl.util.Reconstructor;

import java.io.IOException;

public class IfStmt extends ConditionalStmt {

    private final Expression condition;
    private BlockStmt elseBlock;

    public IfStmt(Expression condition, BlockStmt bodyBlock, LineFilePos lineFile) {
        super(bodyBlock, lineFile);

        this.condition = condition;
    }

    public static IfStmt reconstruct(BytesIn in, LineFilePos lineFilePos) throws Exception {
        Expression cond = Reconstructor.reconstruct(in);
        BlockStmt body = Reconstructor.reconstruct(in);
        boolean hasElse = in.readBoolean();
        IfStmt ifs = new IfStmt(cond, body, lineFilePos);
        if (hasElse) {
            ifs.setElseBlock(Reconstructor.reconstruct(in));
        }
        return ifs;
    }

    public void setElseBlock(BlockStmt elseBlock) {
        this.elseBlock = elseBlock;
    }

    @Override
    protected void internalProcess(Environment env) {
        Bool bool = Bool.evalBoolean(condition, env, getLineFile());
        BlockEnvironment blockEnvironment;
        if (bool.booleanValue()) {
            blockEnvironment = new BlockEnvironment(env);
            bodyBlock.evaluate(blockEnvironment);
        } else if (elseBlock != null) {
            blockEnvironment = new BlockEnvironment(env);
            elseBlock.evaluate(blockEnvironment);
        }
    }

    @Override
    public String toString() {
        return String.format("If %s then %s else %s\n", condition, bodyBlock, elseBlock);
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        condition.save(out);
        bodyBlock.save(out);
        out.writeBoolean(elseBlock != null);
        if (elseBlock != null) {
            elseBlock.save(out);
        }
    }
}
