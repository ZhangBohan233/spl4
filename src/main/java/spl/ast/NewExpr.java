package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Int;
import spl.interpreter.primitives.Pointer;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.splErrors.NativeError;
import spl.interpreter.splErrors.TypeError;
import spl.interpreter.splObjects.Instance;
import spl.interpreter.splObjects.SplArray;
import spl.interpreter.splObjects.SplModule;
import spl.parser.ParseError;
import spl.util.LineFile;

public class NewExpr extends UnaryExpr {

    public NewExpr(LineFile lineFile) {
        super("new", true, lineFile);
    }

    private static SplElement directInitClass(Node node, Environment classDefEnv, Environment callEnv,
                                              LineFile lineFile) {
        if (node instanceof FuncCall) {
            return instanceCreation((FuncCall) node, classDefEnv, callEnv, lineFile);
        } else if (node instanceof IndexingNode) {
            return arrayCreation((IndexingNode) node, classDefEnv, callEnv, lineFile);
        } else if (node instanceof Dot) {
            Dot dot = (Dot) node;
            SplElement dotLeft = dot.left.evaluate(classDefEnv);
            if (SplElement.isPrimitive(dotLeft)) throw new TypeError();
            SplModule module = (SplModule) classDefEnv.getMemory().get((Pointer) dotLeft);
            return directInitClass(dot.right, module.getEnv(), callEnv, lineFile);
        } else {
            throw new NativeError("Class instantiation must be a call. Got " + node + " instead. ", lineFile);
        }
    }

    private static SplElement instanceCreation(FuncCall call,
                                               Environment classDefEnv,
                                               Environment callEnv,
                                               LineFile lineFile) {
        Pointer clazzPtr = (Pointer) call.callObj.evaluate(classDefEnv);
        if (callEnv.hasException()) return null;

        return Instance.createInstanceWithInitCall(
                clazzPtr, call.arguments.evalArgs(callEnv), callEnv, lineFile).pointer;
    }

//    private static SplElement initAnonymousClass(Node node,
//                                                BlockStmt classBody,
//                                                Environment classDefEnv,
//                                                Environment callEnv,
//                                                LineFile lineFile) {
//        if (node instanceof FuncCall) {
//            return anonymousInstanceCreation((FuncCall) node, classBody, classDefEnv, callEnv, lineFile);
//        } else if (node instanceof Dot) {
//            Dot dot = (Dot) node;
//            SplElement dotLeft = dot.left.evaluate(classDefEnv);
//            if (!(dotLeft.getType() instanceof ModuleType)) throw new TypeError();
//            SplModule module = (SplModule) classDefEnv.getMemory().get((Pointer) dotLeft.getValue());
//            return initAnonymousClass(dot.right, classBody, module.getEnv(), callEnv, lineFile);
//        } else {
//            throw new SplException("Anonymous class instantiation must have a call to its parent constructor. " +
//                    "Got " + node + " instead. ", lineFile);
//        }
//    }

//    private static TypeValue anonymousInstanceCreation(FuncCall call,
//                                                    BlockStmt classBody,
//                                                    Environment classDefEnv,
//                                                    Environment callEnv,
//                                                    LineFile lineFile) {
//
//        TypeRepresent scClazzNode = (TypeRepresent) call.callObj;
//        Type scType = scClazzNode.evalType(classDefEnv);
//        if (!(scType instanceof ClassType)) throw new TypeError();
//        ClassType scClazzType = (ClassType) scType;
//
//        // define the anonymous class
//        // Note that the definition env of the anonymous class is the current calling env
//        SplClass anClazz = new SplClass(null,
//                scClazzType,
//                new ArrayList<>(),
//                new ArrayList<>(),
//                classBody,
//                callEnv,
//                false,
//                false);
//        Pointer anClazzPtr = callEnv.getMemory().allocateObject(anClazz, callEnv);
//        ClassType anClazzType = new ClassType(anClazzPtr);
//
//        Instance.InstanceTypeValue instanceTv = Instance.createInstanceAndAllocate(anClazzType, callEnv, lineFile);
//        Instance instance = instanceTv.instance;
//
//        TypeValue supTv = instance.getEnv().get("super", lineFile);
//        Instance supIns = (Instance) instance.getEnv().getMemory().get((Pointer) supTv.getValue());
//
//        Instance.callInit(supIns, call.arguments, callEnv, lineFile);
//        return instanceTv.typeValue;
//    }

    private static Pointer arrayCreation(IndexingNode node,
                                         Environment classDefEnv,
                                         Environment callEnv,
                                         LineFile lineFile) {

        if (node.getArgs().getChildren().size() == 1) {
            Int length = (Int) node.getArgs().getChildren().get(0).evaluate(callEnv);
            return SplArray.createArray(node.getCallObj(), (int) length.value, callEnv);
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
