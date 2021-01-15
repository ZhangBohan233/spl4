package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.Int;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.Undefined;
import spl.interpreter.splErrors.NativeError;
import spl.interpreter.splObjects.Instance;
import spl.interpreter.splObjects.SplArray;
import spl.interpreter.splObjects.SplModule;
import spl.parser.ParseError;
import spl.util.*;

import java.io.IOException;

public class NewExpr extends UnaryExpr {

    public NewExpr(LineFilePos lineFile) {
        super("new", true, lineFile);
    }

    public static NewExpr reconstruct(BytesIn in, LineFilePos lineFilePos) throws Exception {
        Expression value = Reconstructor.reconstruct(in);
        var se = new NewExpr(lineFilePos);
        se.setValue(value);
        return se;
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        value.save(out);
    }

    private static SplElement directInitClass(Node node, Environment classDefEnv, Environment callEnv,
                                              LineFilePos lineFile) {
        if (node instanceof FuncCall) {
            return instanceCreation((FuncCall) node, classDefEnv, callEnv, lineFile);
        } else if (node instanceof IndexingNode) {
            return arrayCreation((IndexingNode) node, classDefEnv, callEnv, lineFile);
        } else if (node instanceof Dot) {
            Dot dot = (Dot) node;
            SplElement dotLeft = dot.left.evaluate(classDefEnv);
            if (SplElement.isPrimitive(dotLeft)) {
                SplInvokes.throwException(
                        callEnv,
                        Constants.TYPE_ERROR,
                        "Cannot create instance of primitive type. ",
                        lineFile
                );
                return Undefined.ERROR;
            }
            SplModule module = (SplModule) classDefEnv.getMemory().get((Reference) dotLeft);
            return directInitClass(dot.right, module.getEnv(), callEnv, lineFile);
        } else {
            throw new NativeError("Class instantiation must be a call. Got " + node + " instead. ", lineFile);
        }
    }

    private static SplElement instanceCreation(FuncCall call,
                                               Environment classDefEnv,
                                               Environment callEnv,
                                               LineFilePos lineFile) {
        Reference clazzPtr = (Reference) call.callObj.evaluate(classDefEnv);
        if (callEnv.hasException()) return Undefined.ERROR;

        var ea = call.arguments.evalArgs(callEnv);
        if (callEnv.hasException()) return Undefined.ERROR;
        return Instance.createInstanceWithInitCall(
                clazzPtr, ea, callEnv, lineFile).pointer;
    }

    private static SplElement arrayCreation(IndexingNode node,
                                           Environment classDefEnv,
                                           Environment callEnv,
                                           LineFilePos lineFile) {

        if (node.getArgs().getChildren().size() == 1) {
            Int length = (Int) node.getArgs().getChildren().get(0).evaluate(callEnv);
            return SplArray.createArray(node.getCallObj(), (int) length.value, callEnv, lineFile);
        } else {
            throw new NativeError("Array creation must take exactly one int as argument. ", lineFile);
        }
    }

    @Override
    protected SplElement internalEval(Environment env) {
        if (value instanceof AnonymousClassExpr) {
//            AnonymousClassExpr ace = (AnonymousClassExpr) value;
//            Node content = ace.getContent();
//            if (content instanceof BlockStmt) {
//                return initAnonymousClass(ace.left, (BlockStmt) content, env, env, getLineFile());
//            } else
//            if (ace.left instanceof IndexingNode && content instanceof ArrayLiteral) {
//                return createArrayWithLiteral((IndexingNode) ace.left, (ArrayLiteral) content, env, env, getLineFile());
//            } else {
            throw new ParseError("Unexpected expression in right side of '<-'. ", getLineFile());
//            }
        } else {
            return directInitClass(value, env, env, getLineFile());
        }
    }
}
