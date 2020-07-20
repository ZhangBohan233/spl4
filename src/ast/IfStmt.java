package ast;

import interpreter.env.BlockEnvironment;
import interpreter.env.Environment;
import interpreter.primitives.Bool;
import interpreter.primitives.SplElement;
import util.LineFile;

public class IfStmt extends ConditionalStmt {

    private final Line condition;
    private BlockStmt elseBlock;
    private boolean hasElse;

    public IfStmt(Line condition, BlockStmt bodyBlock, LineFile lineFile) {
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
    protected SplElement internalEval(Environment env) {
        Bool bool = Bool.evalBoolean(condition, env, getLineFile());
        BlockEnvironment blockEnvironment;
        if (bool.booleanValue()) {
            blockEnvironment = new BlockEnvironment(env);
            bodyBlock.evaluate(blockEnvironment);
        } else if (elseBlock != null) {
            blockEnvironment = new BlockEnvironment(env);
            elseBlock.evaluate(blockEnvironment);
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("If %s then %s else %s\n", condition, bodyBlock, elseBlock);
    }
}
