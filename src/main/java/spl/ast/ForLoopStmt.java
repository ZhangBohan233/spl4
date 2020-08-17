package spl.ast;

import spl.interpreter.EvaluatedArguments;
import spl.interpreter.primitives.Pointer;
import spl.interpreter.splErrors.NativeError;
import spl.interpreter.env.BlockEnvironment;
import spl.interpreter.env.Environment;
import spl.interpreter.env.LoopTitleEnvironment;
import spl.interpreter.primitives.Bool;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.splErrors.RuntimeSyntaxError;
import spl.interpreter.splErrors.TypeError;
import spl.interpreter.splObjects.*;
import spl.util.Constants;
import spl.util.LineFile;
import spl.util.Utilities;

public class ForLoopStmt extends ConditionalStmt {

    private final BlockStmt condition;

    private final static String forEachSyntaxMsg = "Syntax of for-each loop: 'for i in collection {...}'";

    public ForLoopStmt(BlockStmt condition, BlockStmt bodyBlock, LineFile lineFile) {
        super(bodyBlock, lineFile);

        this.condition = condition;
    }

    @Override
    protected void internalProcess(Environment env) {
        LoopTitleEnvironment titleEnv = new LoopTitleEnvironment(env);
        BlockEnvironment bodyEnv = new BlockEnvironment(titleEnv);

        Node first;
        if (condition.getLines().size() == 1 &&
                ((first = condition.getLines().get(0).get(0)) instanceof InExpr)) {
            forEachLoop((InExpr) first, env, titleEnv, bodyEnv);
        } else if (condition.getLines().size() == 3) {  // regular for loop
            forLoop3Parts(
                    condition.getLines().get(0),
                    (AbstractExpression) condition.getLines().get(1).get(0),
                    condition.getLines().get(2),
                    env,
                    titleEnv,
                    bodyEnv
            );
        } else {
            throw new NativeError("For loop takes 1 or 3 condition parts. ", getLineFile());
        }
    }

    private void forLoop3Parts(Line init, AbstractExpression end, Line step, Environment parentEnv,
                               LoopTitleEnvironment titleEnv, BlockEnvironment bodyEnv) {
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
                             LoopTitleEnvironment titleEnv,
                             BlockEnvironment bodyEnv) {
        SplElement probIterable = inExpr.right.evaluate(parentEnv);

        Declaration loopInvariant;
        if (inExpr.left instanceof Declaration)
            loopInvariant = (Declaration) inExpr.left;
        else if (inExpr.left instanceof NameNode)
            loopInvariant = new Declaration(Declaration.VAR, ((NameNode) inExpr.left).getName(), lineFile);
        else
            throw new RuntimeSyntaxError("Loop invariant must either be declaration or name. ", lineFile);

        if (probIterable instanceof Pointer) {
            Pointer ptr = (Pointer) probIterable;
            SplObject obj = parentEnv.getMemory().get(ptr);
            if (obj instanceof SplArray) {
                Instance.InstanceAndPtr arrIterator =
                        Instance.createInstanceWithInitCall(
                                Constants.ARRAY_ITERATOR_CLASS,
                                EvaluatedArguments.of(ptr),
                                parentEnv,
                                lineFile);
                forEachLoopIterator(loopInvariant,
                        arrIterator.pointer,
                        arrIterator.instance,
                        parentEnv,
                        titleEnv,
                        bodyEnv);
                return;
            } else if (Utilities.isInstancePtr(ptr, Constants.ITERATOR_CLASS, parentEnv, lineFile)) {
                Instance iterator = (Instance) parentEnv.getMemory().get(ptr);
                forEachLoopIterator(loopInvariant, ptr, iterator, parentEnv, titleEnv, bodyEnv);
                return;
            } else if (Utilities.isInstancePtr(ptr, Constants.ITERABLE_CLASS, parentEnv, lineFile)) {
                Instance iterable = (Instance) parentEnv.getMemory().get(ptr);
                Pointer iterFnPtr = (Pointer) iterable.getEnv().get(Constants.ITER_FN, lineFile);
                SplMethod iterFn = (SplMethod) parentEnv.getMemory().get(iterFnPtr);
                Pointer iteratorPtr = (Pointer) iterFn.call(EvaluatedArguments.of(ptr), parentEnv, lineFile);
                Instance iterator = (Instance) parentEnv.getMemory().get(iteratorPtr);
                forEachLoopIterator(loopInvariant, iteratorPtr, iterator, parentEnv, titleEnv, bodyEnv);
                return;
            }
        }

        throw new TypeError("For-each loop only supports array or classes extends 'Iterable' or 'Iterator', " +
                "got a '" + probIterable + "'. ", lineFile);
    }

    private void forEachLoopIterator(Declaration loopInvariant,
                                     Pointer instancePtr,
                                     Instance iterator,
                                     Environment parentEnv,
                                     LoopTitleEnvironment titleEnv,
                                     BlockEnvironment bodyEnv) {
        Pointer nextPtr = (Pointer) iterator.getEnv().get(Constants.NEXT_FN, lineFile);
        Pointer hasNextPtr = (Pointer) iterator.getEnv().get(Constants.HAS_NEXT_FN, lineFile);
        SplMethod nextFn = (SplMethod) titleEnv.getMemory().get(nextPtr);
        SplMethod hasNextFn = (SplMethod) titleEnv.getMemory().get(hasNextPtr);

        String liName = loopInvariant.declaredName;
        loopInvariant.evaluate(titleEnv);  // declare loop invariant

        Bool bool = (Bool) hasNextFn.call(EvaluatedArguments.of(instancePtr), titleEnv, lineFile);
        while (bool.value) {
            bodyEnv.invalidate();
            SplElement nextVal = nextFn.call(EvaluatedArguments.of(instancePtr), bodyEnv, lineFile);
            titleEnv.setVar(liName, nextVal, lineFile);

            bodyBlock.evaluate(bodyEnv);
            if (titleEnv.isBroken() || parentEnv.interrupted()) break;

            titleEnv.resumeLoop();

            bool = (Bool) hasNextFn.call(EvaluatedArguments.of(instancePtr), titleEnv, lineFile);
        }
    }

    @Override
    public String toString() {
        return "for " + condition + " do " + bodyBlock;
    }
}