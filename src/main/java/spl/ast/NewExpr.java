package spl.ast;

import spl.interpreter.EvaluatedArguments;
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
import spl.util.*;

import java.io.IOException;
import java.util.List;

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

    private static SplElement directInitClass(Node node, Environment classDefEnv, Environment callEnv,
                                              LineFilePos lineFile) {
        if (node instanceof FuncCall) {
            return instanceCreation((FuncCall) node, classDefEnv, callEnv, lineFile);
        } else if (node instanceof AnonymousClassExpr) {
            return anonymousInstanceCreation((AnonymousClassExpr) node, classDefEnv, callEnv, lineFile);
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
            SplModule module = classDefEnv.getMemory().get((Reference) dotLeft);
            return directInitClass(dot.right, module.getEnv(), callEnv, lineFile);
        } else {
            return SplInvokes.throwExceptionWithError(
                    callEnv,
                    Constants.RUNTIME_SYNTAX_ERROR,
                    "Class instantiation must be a call. Got " + node + " instead. ",
                    lineFile
            );
        }
    }

    private static SplElement instanceCreation(FuncCall call,
                                               Environment classDefEnv,
                                               Environment callEnv,
                                               LineFilePos lineFile) {
        Reference[] generics = call.evalGenerics(callEnv);
        if (callEnv.hasException()) return Undefined.ERROR;
        Reference clazzPtr = (Reference) call.callObj.evaluate(classDefEnv);
        if (callEnv.hasException()) return Undefined.ERROR;

        var ea = call.arguments.evalArgs(callEnv);
        if (callEnv.hasException()) return Undefined.ERROR;
        Instance.InstanceAndPtr iap = Instance.createInstanceWithInitCall(
                clazzPtr, generics, ea, callEnv, lineFile);
        if (iap == null) return Undefined.ERROR;
        return iap.pointer;
    }

    private static SplElement anonymousInstanceCreation(AnonymousClassExpr ace,
                                                        Environment supClassDefEnv,
                                                        Environment callEnv,
                                                        LineFilePos lineFilePos) {
        ClassStmt cs = new ClassStmt(
                ace.getAnonymousName(),
                List.of(ace.getCall().callObj),
                ace.getBody(),
                lineFilePos);
        cs.crossEnvEval(supClassDefEnv, callEnv);
        if (callEnv.hasException()) return Undefined.ERROR;
        Instance.InstanceAndPtr iap = Instance.createInstanceWithInitCall(
                ace.getAnonymousName(), EvaluatedArguments.of(), callEnv, lineFilePos
        );
        if (iap == null) return Undefined.ERROR;
        return iap.pointer;
    }

    private static SplElement arrayCreation(IndexingNode node,
                                            Environment classDefEnv,
                                            Environment callEnv,
                                            LineFilePos lineFile) {

        if (node.getArgs().getChildren().size() == 1) {
            SplElement lengthRaw = node.getArgs().getChildren().get(0).evaluate(callEnv);
            if (!(lengthRaw instanceof Int)) return SplInvokes.throwExceptionWithError(
                    callEnv,
                    Constants.TYPE_ERROR,
                    "Array creation takes int as length.",
                    lineFile
            );
            Int length = (Int) lengthRaw;
            SplElement arrPtr = SplArray.createArray(node.getCallObj(), (int) length.value, callEnv, lineFile);
            if (arrPtr == Undefined.ERROR) return Undefined.ERROR;
            Reference arrPtrReal = (Reference) arrPtr;
            callEnv.getMemory().addTempPtr(arrPtrReal);
            if (node.getInitialValue() != null) {
                DictSetLiteral initV = node.getInitialValue();
                for (int i = 0; i < initV.getNodes().size(); i++) {
                    SplElement val = initV.getNodes().get(i).evaluate(callEnv);
                    if (val == Undefined.ERROR) {
                        callEnv.getMemory().removeTempPtr(arrPtrReal);
                        return Undefined.ERROR;
                    }
                    SplArray.setItemAtIndex(arrPtrReal, i, val, callEnv, lineFile);
                }
            }
            callEnv.getMemory().removeTempPtr(arrPtrReal);

            return arrPtrReal;
        } else {
            throw new NativeError("Array creation must take exactly one int as argument. ", lineFile);
        }
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        value.save(out);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return directInitClass(value, env, env, getLineFile());
    }
}
