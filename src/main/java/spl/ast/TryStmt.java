package spl.ast;

import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.BlockEnvironment;
import spl.interpreter.env.Environment;
import spl.interpreter.env.FunctionEnvironment;
import spl.interpreter.env.TryEnvironment;
import spl.interpreter.primitives.Bool;
import spl.interpreter.primitives.Pointer;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.splErrors.ArrayIndexError;
import spl.interpreter.splErrors.NativeError;
import spl.interpreter.splErrors.TypeError;
import spl.interpreter.splObjects.SplCallable;
import spl.util.LineFile;

import java.util.ArrayList;
import java.util.List;

public class TryStmt extends AbstractStatement {

    private final List<CatchStmt> catchStmts = new ArrayList<>();
    private final BlockStmt body;
    private BlockStmt finallyBlock;

    public TryStmt(BlockStmt body, LineFile lineFile) {
        super(lineFile);

        this.body = body;
    }

    public void addCatch(CatchStmt catchStmt) {
        catchStmts.add(catchStmt);
    }

    public void setFinallyBlock(BlockStmt finallyBlock) {
        this.finallyBlock = finallyBlock;
    }

    @Override
    protected void internalProcess(Environment env) {
        try {
            TryEnvironment tryEnv = new TryEnvironment(env);
            body.evaluate(tryEnv);
            if (tryEnv.hasException()) {
                Pointer exceptionPtr = tryEnv.getExceptionPtr();
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
                    ThrowStmt.throwException(exceptionPtr, env, lineFile);
            }
        } catch (Exception e) {
            ExceptionContainer[][] exceptionsArr = evalExceptions(env);
            boolean caught = false;
            OUT_LOOP:
            for (int i = 0; i < exceptionsArr.length; i++) {
                for (int j = 0; j < exceptionsArr[i].length; j++) {
                    ExceptionContainer ec = exceptionsArr[i][j];
                    if (ec.nativeError != null) {

                        if (ec.nativeError.isInstance(e)) {
                            caught = true;
                            CatchStmt caughtError = catchStmts.get(i);
                            BlockEnvironment catchEnv = new BlockEnvironment(env);

                            // TODO: catch NativeErr as e
//                            if (caught.condition instanceof AsExpr) {
//                                String name = ((AsExpr) caught.condition).getRight().getName();
//                                catchEnv.defineVarAndSet(name, exceptionPtr, lineFile);
//                            }
                            caughtError.evaluate(catchEnv);
                            break OUT_LOOP;
                        }
                    }
                }
            }

            if (!caught) throw e;
        } finally {
            if (finallyBlock != null) {
                Environment env2 = env;
                while (!(env2 instanceof FunctionEnvironment)) {
                    env2 = env2.outer;
                }
                FunctionEnvironment fe = (FunctionEnvironment) env2;
                SplElement rtn = fe.temporaryRemoveRtn();
                BlockEnvironment finallyEnv = new BlockEnvironment(env);
                finallyBlock.evaluate(finallyEnv);
                fe.setReturn(rtn);
            }
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

    private static int fillExceptionContainer(AbstractExpression expr,
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
            if (expr instanceof NameNode) {
                String name = ((NameNode) expr).getName();
                Class<? extends NativeError> nativeErrorClass;
                switch (name) {
                    case "NativeError":
                        nativeErrorClass = NativeError.class;
                        break;
                    case "TypeError":
                        nativeErrorClass = TypeError.class;
                        break;
                    case "ArrayIndexError":
                        nativeErrorClass = ArrayIndexError.class;
                        break;
                    default:
                        nativeErrorClass = null;
                        break;
                }
                if (nativeErrorClass != null) {
                    containers[index] = new ExceptionContainer(nativeErrorClass);
                    return index + 1;
                }
            }
            SplElement value = expr.evaluate(env);
            SplCallable splCallable = (SplCallable) env.getMemory().get((Pointer) value);
            containers[index] = new ExceptionContainer(splCallable);
            return index + 1;
        }
    }

    private static int getExprSize(AbstractExpression expr) {
        if (expr instanceof AsExpr) {
            return getExprSize(((AsExpr) expr).left);
        } else if (expr instanceof BinaryExpr) {
            BinaryExpr binaryExpr = (BinaryExpr) expr;
            return getExprSize(binaryExpr.left) + getExprSize(binaryExpr.right);
        } else return 1;
    }

    private static class ExceptionContainer {
        private final Class<? extends NativeError> nativeError;
        private final SplCallable userError;

        private ExceptionContainer(Class<? extends NativeError> nativeError) {
            this.nativeError = nativeError;
            this.userError = null;
        }

        private ExceptionContainer(SplCallable userError) {
            this.nativeError = null;
            this.userError = userError;
        }
    }

}
