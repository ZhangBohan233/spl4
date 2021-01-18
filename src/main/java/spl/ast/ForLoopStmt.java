package spl.ast;

import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.BlockEnvironment;
import spl.interpreter.env.Environment;
import spl.interpreter.env.LoopEnvironment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.Bool;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.Undefined;
import spl.interpreter.splErrors.RuntimeSyntaxError;
import spl.interpreter.splObjects.Instance;
import spl.interpreter.splObjects.SplArray;
import spl.interpreter.splObjects.SplMethod;
import spl.interpreter.splObjects.SplObject;
import spl.util.*;

import java.io.IOException;

public class ForLoopStmt extends ConditionalStmt {

    //    private final static String forEachSyntaxMsg = "Syntax of for-each loop: 'for i in collection {...}'";
    private final BlockStmt condition;

    public ForLoopStmt(BlockStmt condition, BlockStmt bodyBlock, LineFilePos lineFile) {
        super(bodyBlock, lineFile);

        this.condition = condition;
    }

    public static ForLoopStmt reconstruct(BytesIn in, LineFilePos lineFilePos) throws Exception {
        BlockStmt cond = Reconstructor.reconstruct(in);
        BlockStmt body = Reconstructor.reconstruct(in);
        return new ForLoopStmt(cond, body, lineFilePos);
    }

    @Override
    protected void internalProcess(Environment env) {
        LoopEnvironment titleEnv = new LoopEnvironment(env);
        BlockEnvironment bodyEnv = new BlockEnvironment(titleEnv);


        if (condition.getLines().size() == 1) {
            Node first = condition.getLines().get(0).get(0);
            if (first instanceof InExpr) {
                forEachLoop((InExpr) first, env, titleEnv, bodyEnv);
            } else {
                forLoop3Parts(
                        new Line(lineFile),
                        (Expression) first,
                        new Line(lineFile),
                        env,
                        titleEnv,
                        bodyEnv
                );
            }
        } else if (condition.getLines().size() == 3) {  // regular for loop
            forLoop3Parts(
                    condition.getLines().get(0),
                    (Expression) condition.getLines().get(1).get(0),
                    condition.getLines().get(2),
                    env,
                    titleEnv,
                    bodyEnv
            );
        } else {
            SplInvokes.throwException(
                    env,
                    Constants.TYPE_ERROR,
                    "For loop takes 1 or 3 condition parts.",
                    lineFile);
        }
    }

    private void forLoop3Parts(Line init, Expression end, Line step, Environment parentEnv,
                               LoopEnvironment titleEnv, BlockEnvironment bodyEnv) {
        init.evaluate(titleEnv);
        Bool bool = Bool.evalBoolean(end, titleEnv, lineFile);
        while (bool.value) {
            bodyEnv.invalidate();
            bodyBlock.evaluate(bodyEnv);
            if (titleEnv.isBroken() || parentEnv.interrupted()) break;

            titleEnv.resumeLoop();
            step.evaluate(titleEnv);
            bool = Bool.evalBoolean(end, titleEnv, lineFile);
        }
    }

    private void forEachLoop(InExpr inExpr,
                             Environment parentEnv,
                             LoopEnvironment titleEnv,
                             BlockEnvironment bodyEnv) {
        SplElement probIterable = inExpr.right.evaluate(parentEnv);

        Declaration loopInvariant;
        if (inExpr.left instanceof Declaration)
            loopInvariant = (Declaration) inExpr.left;
        else if (inExpr.left instanceof NameNode)
            loopInvariant = new Declaration(Declaration.VAR, ((NameNode) inExpr.left).getName(), lineFile);
        else
            throw new RuntimeSyntaxError("Loop invariant must either be declaration or name. ", lineFile);

        if (probIterable instanceof Reference) {
            Reference ptr = (Reference) probIterable;
            SplObject obj = parentEnv.getMemory().get(ptr);
            if (obj instanceof SplArray) {
                Instance.InstanceAndPtr arrIterator =
                        Instance.createInstanceWithInitCall(
                                Constants.ARRAY_ITERATOR_CLASS,
                                EvaluatedArguments.of(ptr),
                                parentEnv,
                                lineFile);
                if (arrIterator == null) return;
                forEachLoopIterator(loopInvariant,
                        arrIterator.pointer,
                        arrIterator.instance,
                        parentEnv,
                        titleEnv,
                        bodyEnv);
                return;
            } else if (Utilities.isInstancePtr(ptr, Constants.ITERATOR_CLASS, parentEnv, lineFile)) {
                Instance iterator = parentEnv.getMemory().get(ptr);
                forEachLoopIterator(loopInvariant, ptr, iterator, parentEnv, titleEnv, bodyEnv);
                return;
            } else if (Utilities.isInstancePtr(ptr, Constants.ITERABLE_CLASS, parentEnv, lineFile)) {
                Instance iterable = parentEnv.getMemory().get(ptr);
                Reference iterFnPtr = (Reference) iterable.getEnv().get(Constants.ITER_FN, lineFile);
                SplMethod iterFn = parentEnv.getMemory().get(iterFnPtr);
                SplElement iterRes = iterFn.call(EvaluatedArguments.of(ptr), parentEnv, lineFile);
                if (iterRes == Undefined.ERROR) return;
                Reference iteratorPtr = (Reference) iterRes;
                Instance iterator = parentEnv.getMemory().get(iteratorPtr);
                forEachLoopIterator(loopInvariant, iteratorPtr, iterator, parentEnv, titleEnv, bodyEnv);
                return;
            }
        }
        SplInvokes.throwException(
                parentEnv,
                Constants.TYPE_ERROR,
                "For-each loop only supports array or classes extends 'Iterable' or 'Iterator', " +
                        "got a '" + probIterable + "'.",
                lineFile);
    }

    private void forEachLoopIterator(Declaration loopInvariant,
                                     Reference instancePtr,
                                     Instance iterator,
                                     Environment parentEnv,
                                     LoopEnvironment titleEnv,
                                     BlockEnvironment bodyEnv) {
        if (iterator == null) {
            SplInvokes.throwException(
                    parentEnv,
                    Constants.NULL_ERROR,
                    "Iterator is null.",
                    loopInvariant.lineFile
            );
            return;
        }
        Reference nextPtr = (Reference) iterator.getEnv().get(Constants.NEXT_FN, lineFile);
        Reference hasNextPtr = (Reference) iterator.getEnv().get(Constants.HAS_NEXT_FN, lineFile);
        SplMethod nextFn = titleEnv.getMemory().get(nextPtr);
        SplMethod hasNextFn = titleEnv.getMemory().get(hasNextPtr);

        String liName = loopInvariant.declaredName;
        loopInvariant.evaluate(titleEnv);  // declare loop invariant

        SplElement hasNext = hasNextFn.call(EvaluatedArguments.of(instancePtr), titleEnv, lineFile);
        if (titleEnv.hasException()) return;
        Bool bool = (Bool) hasNext;
        while (bool.value) {
            bodyEnv.invalidate();
            SplElement nextVal = nextFn.call(EvaluatedArguments.of(instancePtr), bodyEnv, lineFile);
            titleEnv.setVar(liName, nextVal, lineFile);

            bodyBlock.evaluate(bodyEnv);
            if (titleEnv.isBroken() || parentEnv.interrupted()) break;

            titleEnv.resumeLoop();

            hasNext = hasNextFn.call(EvaluatedArguments.of(instancePtr), titleEnv, lineFile);
            if (titleEnv.hasException()) return;
            bool = (Bool) hasNext;
        }
    }

    @Override
    public String toString() {
        return "for " + condition + " do " + bodyBlock;
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        condition.save(out);
        bodyBlock.save(out);
    }
}
