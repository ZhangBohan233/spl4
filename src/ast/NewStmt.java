package ast;

import interpreter.SplException;
import interpreter.env.Environment;
import interpreter.primitives.Int;
import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;
import interpreter.splObjects.*;
import interpreter.types.*;
import parser.ParseError;
import util.LineFile;

import java.util.ArrayList;
import java.util.List;

public class NewStmt extends UnaryExpr {

    public NewStmt(LineFile lineFile) {
        super("new", true, lineFile);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        if (value instanceof AnonymousClassExpr) {
            AnonymousClassExpr ace = (AnonymousClassExpr) value;
            Node content = ace.getContent();
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

//    @Override
//    protected Type inferredType(Environment env) {
//        return typeInference(value, env, getLineFile());
//    }

//    private static Type typeInference(Node node, Environment env, LineFile lineFile) {
//        if (node instanceof FuncCall) {
//            return  ((TypeRepresent) ((FuncCall) node).callObj).evalType(env);
//        } else if (node instanceof IndexingNode) {
//            return ((IndexingNode) node).evalType(env);
//        } else if (node instanceof Dot) {
//            Dot dot = (Dot) node;
//            TypeValue dotLeft = dot.left.evaluate(env);
//            if (!(dotLeft.getType() instanceof ModuleType)) throw new TypeError();
//            SplModule module = (SplModule) env.getMemory().get((Pointer) dotLeft.getValue());
//            return typeInference(dot.right, module.getEnv(), lineFile);
//        } else {
//            throw new SplException("Class type must be a call. Got " + node + " instead. ", lineFile);
//        }
//    }

    private static SplElement directInitClass(Node node, Environment classDefEnv, Environment callEnv,
                                              LineFile lineFile) {
        if (node instanceof FuncCall) {
            return instanceCreation((FuncCall) node, classDefEnv, callEnv, lineFile);
        }
        else if (node instanceof IndexingNode) {
            return arrayCreation((IndexingNode) node, classDefEnv, callEnv, lineFile);
        } else if (node instanceof Dot) {
            Dot dot = (Dot) node;
            SplElement dotLeft = dot.left.evaluate(classDefEnv);
            if (SplElement.isPrimitive(dotLeft)) throw new TypeError();
            SplModule module = (SplModule) classDefEnv.getMemory().get((Pointer) dotLeft);
            return directInitClass(dot.right, module.getEnv(), callEnv, lineFile);
        } else {
            throw new SplException("Class instantiation must be a call. Got " + node + " instead. ", lineFile);
        }
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

    private static SplElement instanceCreation(FuncCall call,
                                               Environment classDefEnv,
                                               Environment callEnv,
                                               LineFile lineFile) {
        Pointer clazzPtr = (Pointer) call.callObj.evaluate(classDefEnv);
//        SplClass clazzObj = (SplClass) callEnv.getMemory().get((Pointer) clazzPtr);

//        Type type = clazzNode.evalType(classDefEnv);
//        System.out.println(type);
//        if (!(type instanceof ClassType)) throw new TypeError();
//        ClassType clazzType = ((ClassType) type).copy();  // create a copy to avoid modification

        Instance.InstanceAndPtr instanceTv = Instance.createInstanceAndAllocate(clazzPtr, callEnv, lineFile);
        Instance instance = instanceTv.instance;

        Instance.callInit(instance, call.arguments, callEnv, lineFile);
        return instanceTv.pointer;
    }

//    private static SplElement createArrayWithLiteral(IndexingNode node,
//                                                    ArrayLiteral literal,
//                                                    Environment classDefEnv,
//                                                    Environment callEnv,
//                                                    LineFile lineFile) {
//
////        Type atomType = node.evalAtomType(classDefEnv);
//
//        SplElement literalArray = literal.createAndAllocate(null, callEnv);
////        System.out.println(atomType);
//        // TODO: temporarily 不想写
////        ArrayType arrayType = (ArrayType) node.evalType(classDefEnv);
////        List<Integer> dimensions = new ArrayList<>();
////        traverseArrayCreation(node, dimensions, callEnv, lineFile);
////        Pointer arrPtr = SplArray.createArray(arrayType, dimensions, callEnv);
////
////        literal.fillArray(arrayType, arrPtr, dimensions, callEnv.getMemory());
////        return new TypeValue(arrayType, arrPtr);
//
//        return literalArray;
//    }
//
    private static Pointer arrayCreation(IndexingNode node,
                                           Environment classDefEnv,
                                           Environment callEnv,
                                           LineFile lineFile) {
//        ArrayType arrayType = (ArrayType) node.evalType(classDefEnv);
//        List<Integer> dimensions = new ArrayList<>();
//        traverseArrayCreation(node, dimensions, callEnv, lineFile);
        if (node.getArgs().getChildren().size() == 1) {
            Int length = (Int) node.getArgs().getChildren().get(0).evaluate(callEnv);
            return SplArray.createArray(node.getCallObj(), (int) length.value, callEnv);
        } else {
            throw new SplException("Array creation must take exactly one int as argument. ", lineFile);
        }
//        return SplArray.createArray(arrayType, dimensions, callEnv);
    }

//    private static

//
//    private static void traverseArrayCreation(IndexingNode node,
//                                              List<Integer> dimensions,
//                                              Environment env,
//                                              LineFile lineFile) {
//        List<Node> argsList = node.getArgs().getChildren();
//        if (node.getCallObj() instanceof IndexingNode) {
//
//            traverseArrayCreation((IndexingNode) node.getCallObj(),
//                    dimensions,
//                    env,
//                    lineFile);
//
//            if (argsList.size() == 0) {
//                dimensions.add(-1);
//            } else if (argsList.size() == 1) {
//                TypeValue argument = argsList.get(0).evaluate(env);
//                if (argument.getType().equals(PrimitiveType.TYPE_INT)) {
//                    int arrSize = (int) argument.getValue().intValue();
//                    dimensions.add(arrSize);
//                } else {
//                    throw new TypeError();
//                }
//            } else {
//                throw new TypeError("Array creation must have a size argument. ", lineFile);
//            }
//        } else {
//            if (argsList.size() != 1) {
//                throw new TypeError("Array creation must have a size argument. ", lineFile);
//            }
//            TypeValue argument = argsList.get(0).evaluate(env);
//            if (argument.getType().equals(PrimitiveType.TYPE_INT)) {
//                int arrSize = (int) argument.getValue().intValue();
//                dimensions.add(arrSize);
//            } else {
//                throw new TypeError();
//            }
//        }
//    }
}
