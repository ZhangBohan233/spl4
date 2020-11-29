package spl.ast;

import spl.interpreter.env.BlockEnvironment;
import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Bool;
import spl.util.LineFile;

public class IfStmt extends ConditionalStmt {

    private final Expression condition;
    private BlockStmt elseBlock;
    private boolean hasElse;

    public IfStmt(Expression condition, BlockStmt bodyBlock, LineFile lineFile) {
        super(bodyBlock, lineFile);

        this.condition = condition;
    }

    public void setElseBlock(BlockStmt elseBlock) {
        this.elseBlock = elseBlock;
    }

    public boolean hasElse() {
        return hasElse;
    }

    public void setHasElse(boolean hasElse) {
        this.hasElse = hasElse;
    }

    public Node getElseBlock() {
        return elseBlock;
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
}
