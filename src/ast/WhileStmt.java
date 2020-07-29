package ast;

import interpreter.env.BlockEnvironment;
import interpreter.env.Environment;
import interpreter.env.LoopTitleEnvironment;
import interpreter.primitives.Bool;
import interpreter.primitives.SplElement;
import util.LineFile;

public class WhileStmt extends ConditionalStmt {

    private AbstractExpression condition;

    public WhileStmt(LineFile lineFile) {
        super(lineFile);
    }

    @Override
    protected void internalProcess(Environment env) {

        LoopTitleEnvironment titleEnv = new LoopTitleEnvironment (env);
        BlockEnvironment bodyEnv = new BlockEnvironment(titleEnv);

        Bool bool = Bool.evalBoolean(condition, titleEnv, getLineFile());
        while (bool.value) {
            bodyEnv.invalidate();
            bodyBlock.evaluate(bodyEnv);
            if (titleEnv.isBroken() || env.interrupted()) break;

            titleEnv.resumeLoop();
            bool = Bool.evalBoolean(condition, titleEnv, getLineFile());
        }
    }

    public void setCondition(AbstractExpression condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        return "while " + condition + " do " + bodyBlock;
    }
}
