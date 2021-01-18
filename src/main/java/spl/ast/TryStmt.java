package spl.ast;

import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.BlockEnvironment;
import spl.interpreter.env.Environment;
import spl.interpreter.env.FunctionEnvironment;
import spl.interpreter.env.TryEnvironment;
import spl.interpreter.primitives.Bool;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.splObjects.SplCallable;
import spl.util.BytesIn;
import spl.util.BytesOut;
import spl.util.LineFilePos;
import spl.util.Reconstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TryStmt extends Statement {

    private final List<CatchStmt> catchStmts = new ArrayList<>();
    private final BlockStmt body;
    private BlockStmt finallyBlock;

    public TryStmt(BlockStmt body, LineFilePos lineFile) {
        super(lineFile);

        this.body = body;
    }

    public static TryStmt reconstruct(BytesIn is, LineFilePos lineFilePos) throws Exception {
        List<CatchStmt> catchStmts = is.readList();
        BlockStmt body = Reconstructor.reconstruct(is);
        boolean hasFinally = is.readBoolean();
        BlockStmt finallyBlock = null;
        if (hasFinally) finallyBlock = Reconstructor.reconstruct(is);

        var ts = new TryStmt(body, lineFilePos);
        for (CatchStmt cs : catchStmts) {
            ts.addCatch(cs);
        }
        ts.setFinallyBlock(finallyBlock);
        return ts;
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        out.writeList(catchStmts);
        body.save(out);
        out.writeBoolean(finallyBlock != null);
        if (finallyBlock != null) finallyBlock.save(out);
    }

    public void addCatch(CatchStmt catchStmt) {
        catchStmts.add(catchStmt);
    }

    public void setFinallyBlock(BlockStmt finallyBlock) {
        this.finallyBlock = finallyBlock;
    }

    @Override
    protected void internalProcess(Environment env) {

            TryEnvironment tryEnv = new TryEnvironment(env);
            body.evaluate(tryEnv);
            if (tryEnv.hasException()) {
                Reference exceptionPtr = tryEnv.getExceptionPtr();
                ExceptionContainer[][] exceptionsArr = evalExceptions(env);
                boolean caught = false;
                OUT_LOOP:
                for (int i = 0; i < exceptionsArr.length; i++) {
                    for (int j = 0; j < exceptionsArr[i].length; j++) {
                        ExceptionContainer ec = exceptionsArr[i][j];
                        if (ec.userError != null) {
                            Bool bool = (Bool) ec.userError.call(EvaluatedArguments.of(exceptionPtr), env, lineFile);
                            if (bool.value) {
                                caught = true;
                                CatchStmt caughtError = catchStmts.get(i);
                                BlockEnvironment catchEnv = new BlockEnvironment(env);
                                if (caughtError.condition instanceof AsExpr) {
                                    String name = ((AsExpr) caughtError.condition).getRight().getName();
                                    catchEnv.defineVarAndSet(name, exceptionPtr, lineFile);
                                }
                                caughtError.evaluate(catchEnv);
                                break OUT_LOOP;
                            }
                        }
                    }
                }

                if (!caught)
                    // Exception not caught, throw it to outer
                    ThrowExpr.throwException(exceptionPtr, env, lineFile);
            }

            if (finallyBlock != null) {
                Environment env2 = env;
                while (!(env2 instanceof FunctionEnvironment)) {
                    env2 = env2.outer;
                }
                FunctionEnvironment fe = (FunctionEnvironment) env2;
                SplElement rtn = fe.temporaryRemoveRtn();
                BlockEnvironment finallyEnv = new BlockEnvironment(env);
                finallyBlock.evaluate(finallyEnv);
                fe.setReturn(rtn, lineFile);
            }
    }

    @Override
    public String toString() {
        return String.format("try %s %s finally %s", body, catchStmts, finallyBlock);
    }

    private ExceptionContainer[][] evalExceptions(Environment outerEnv) {
        ExceptionContainer[][] containersArray = new ExceptionContainer[catchStmts.size()][];
        int index = 0;
        for (CatchStmt catchStmt : catchStmts) {
            int size = getExprSize(catchStmt.condition);
            ExceptionContainer[] containers = new ExceptionContainer[size];
            fillExceptionContainer(catchStmt.condition, containers, outerEnv, 0);
            containersArray[index++] = containers;
        }
        return containersArray;
    }

    private static int fillExceptionContainer(Expression expr,
                                              ExceptionContainer[] containers,
                                              Environment env,
                                              int index) {
        if (expr instanceof AsExpr) {
            return fillExceptionContainer(((AsExpr) expr).left, containers, env, index);
        } else if (expr instanceof BinaryExpr) {
            BinaryExpr binaryExpr = (BinaryExpr) expr;
            index = fillExceptionContainer(binaryExpr.left, containers, env, index);
            return fillExceptionContainer(binaryExpr.right, containers, env, index);
        } else {
//            if (expr instanceof NameNode) {
//                String name = ((NameNode) expr).getName();
//                Class<? extends NativeError> nativeErrorClass;
//                switch (name) {
//                    case "NativeError":
//                        nativeErrorClass = NativeError.class;
//                        break;
//                    case "TypeError":
//                        nativeErrorClass = TypeError.class;
//                        break;
//                    case "ArrayIndexError":
//                        nativeErrorClass = ArrayIndexError.class;
//                        break;
//                    default:
//                        nativeErrorClass = null;
//                        break;
//                }
//                if (nativeErrorClass != null) {
//                    containers[index] = new ExceptionContainer(nativeErrorClass);
//                    return index + 1;
//                }
//            }
            SplElement value = expr.evaluate(env);
            SplCallable splCallable = (SplCallable) env.getMemory().get((Reference) value);
            containers[index] = new ExceptionContainer(splCallable);
            return index + 1;
        }
    }

    private static int getExprSize(Expression expr) {
        if (expr instanceof AsExpr) {
            return getExprSize(((AsExpr) expr).left);
        } else if (expr instanceof BinaryExpr) {
            BinaryExpr binaryExpr = (BinaryExpr) expr;
            return getExprSize(binaryExpr.left) + getExprSize(binaryExpr.right);
        } else return 1;
    }

    private static class ExceptionContainer {
        private final SplCallable userError;

        private ExceptionContainer(SplCallable userError) {
            this.userError = userError;
        }
    }
}
