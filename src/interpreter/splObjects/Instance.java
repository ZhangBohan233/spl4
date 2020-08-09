package interpreter.splObjects;

import ast.*;
import interpreter.EvaluatedArguments;
import interpreter.splErrors.NativeError;
import interpreter.env.Environment;
import interpreter.env.InstanceEnvironment;
import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;
import util.Constants;
import util.LineFile;

import java.util.*;

public class Instance extends SplObject {

    private final InstanceEnvironment env;
    private final Pointer clazzPtr;

    private Instance(Pointer clazzPtr, InstanceEnvironment env) {
        this.clazzPtr = clazzPtr;
        this.env = env;
    }

    public InstanceEnvironment getEnv() {
        return env;
    }

    public Pointer getClazzPtr() {
        return clazzPtr;
    }

    @Override
    public String toString() {
        return "Instance of<" + env.getMemory().get(clazzPtr.getPtr()) + ">";
    }

    public static InstanceAndPtr createInstanceAndAllocate(String className,
                                                           Environment creationEnv,
                                                           LineFile lineFile) {
        Pointer clazzPtr = (Pointer) creationEnv.get(className, lineFile);
        return createInstanceAndAllocate(
                clazzPtr, creationEnv, new HashMap<>(), new ArrayList<>(), lineFile);
    }

    public static InstanceAndPtr createInstanceAndAllocate(Pointer clazzPtr,
                                                           Environment creationEnv,
                                                           LineFile lineFile) {
        return createInstanceAndAllocate(
                clazzPtr, creationEnv, new HashMap<>(), new ArrayList<>(), lineFile);
    }

    /**
     * Creates an instance and allocate it in memory.
     *
     * @param clazzPtr          pointer to class
     * @param outerEnv          env where the new instance is created, not the class definition env
     * @param superclassMethods methods definitions of superclasses, will be overridden
     * @param mro               list of multiple resolution order, child at first
     * @param lineFile          error traceback info of code where instance creation
     * @return the tuple of the newly created instance, and the {@code TypeValue} contains the pointer to this instance
     */
    private static InstanceAndPtr createInstanceAndAllocate(Pointer clazzPtr,
                                                            Environment outerEnv,
                                                            Map<String, FuncDefinition> superclassMethods,
                                                            List<Pointer> mro,
                                                            LineFile lineFile) {
        // add to multiple resolution order
        mro.add(clazzPtr);

        SplObject obj = outerEnv.getMemory().get(clazzPtr);
        SplClass clazz = (SplClass) obj;
        InstanceEnvironment instanceEnv = new InstanceEnvironment(
                clazz.getClassName(),
                clazz.getDefinitionEnv(),
                outerEnv
        );
        outerEnv.getMemory().addTempEnv(instanceEnv);

        Instance instance = new Instance(clazzPtr, instanceEnv);
        Pointer instancePtr = outerEnv.getMemory().allocate(1, instanceEnv);
        outerEnv.getMemory().set(instancePtr, instance);

//         define "this"
//        instance.getEnv().directDefineConstAndSet(Constants.THIS, instancePtr);

        // define "getClass"
        NativeFunction getClassFtn = new NativeFunction(Constants.GET_CLASS, 0) {
            @Override
            protected SplElement callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                return clazzPtr;
            }
        };
        Pointer getClassPtr = instanceEnv.getMemory().allocateFunction(getClassFtn, instanceEnv);
        instance.getEnv().directDefineConstAndSet(Constants.GET_CLASS, getClassPtr);

        // evaluate superclasses
        List<Pointer> scPointers = clazz.getSuperclassPointers();
        Instance currentChild = instance;
        boolean superNotDefined = true;
        for (Pointer scPtr : scPointers) {
            InstanceAndPtr scInsPtr =
                    createInstanceAndAllocate(scPtr, outerEnv, superclassMethods, mro, lineFile);

            // define "super"
            if (superNotDefined) {
                superNotDefined = false;
                currentChild.getEnv().directDefineConstAndSet(Constants.SUPER, scInsPtr.pointer);
                currentChild = scInsPtr.instance;
            }
        }

//        // define "__mro__"
//        Pointer mroArrPtr = SplArray.createArray(SplElement.POINTER, mro.size(), instanceEnv);
//        instance.getEnv().directDefineConstAndSet(Constants.CLASS_MRO, mroArrPtr);
//        for (int i = 0; i < mro.size(); i++) {
//            SplArray.setItemAtIndex(mroArrPtr, i, mro.get(i), instanceEnv, lineFile);
//        }

        // define all methods from superclasses
        for (Map.Entry<String, FuncDefinition> entry : superclassMethods.entrySet()) {
            entry.getValue().evaluate(instanceEnv);
        }

        // evaluate class body
        for (Node node : clazz.getFieldNodes()) {
            node.evaluate(instanceEnv);
        }

        // set pointer of methods
        for (Map.Entry<String, Pointer> methodEntry : clazz.getMethodDefinitions().entrySet()) {
            instanceEnv.defineFunction(methodEntry.getKey(), methodEntry.getValue(), lineFile);
        }

        // evaluate contracts, must behind methods
        for (ContractNode node : clazz.getContractNodes()) {
            node.evaluate(instanceEnv);
        }

//        for (Line line : clazz.getBody().getLines()) {
//            for (Node lineNode : line.getChildren()) {
//                if (lineNode instanceof Declaration || lineNode instanceof Assignment) {
//                    lineNode.evaluate(instanceEnv);
//                } else if (lineNode instanceof FuncDefinition) {
//                    FuncDefinition fd = (FuncDefinition) lineNode;
//                    fd.evaluate(instanceEnv);
//                    superclassMethods.put(fd.name, fd);
//                } else if (lineNode instanceof ContractNode) {
//                    lineNode.evaluate(instanceEnv);
//                } else
//                    throw new RuntimeSyntaxError("Invalid class body. ", line.lineFile);
//            }
//        }

//        if (!instanceEnv.selfContains(Constants.CONSTRUCTOR)) {
//            // If class no constructor, put an empty default constructor
//            FuncDefinition fd = new FuncDefinition(
//                    Constants.CONSTRUCTOR,
//                    new Line(),
//                    new BlockStmt(LineFile.LF_INTERPRETER),
//                    LineFile.LF_INTERPRETER);
//
//            FuncDefinition.evalMethod(fd, instanceEnv);
//        }

        outerEnv.getMemory().removeTempEnv(instanceEnv);

        // tell memory current instance is this
        outerEnv.getMemory().setCurrentThisPtr(instancePtr);

        return new InstanceAndPtr(instance, instancePtr);
    }

    public static void callInit(Instance instance, Arguments arguments, Environment callEnv, LineFile lineFile) {
        Method constructor = getConstructor(instance, lineFile);
        constructor.methodCall(arguments.evalArgs(callEnv), callEnv, instance.env, lineFile);
    }

    public static void callInit(Instance instance,
                                EvaluatedArguments evaluatedArgs,
                                Environment callEnv,
                                LineFile lineFile) {

        Method constructor = getConstructor(instance, lineFile);
        constructor.methodCall(evaluatedArgs, callEnv, instance.env, lineFile);
    }

    private static Method getConstructor(Instance instance, LineFile lineFile) {
        InstanceEnvironment env = instance.getEnv();
        Pointer constructorPtr = (Pointer) env.get(Constants.CONSTRUCTOR, lineFile);
        Method constructor = (Method) env.getMemory().get(constructorPtr);

        if (env.hasName(Constants.SUPER, lineFile)) {
            // All classes has superclass except class 'Object'
            Pointer superPtr = (Pointer) env.get(Constants.SUPER, lineFile);
            Instance supIns = (Instance) env.getMemory().get(superPtr);
            InstanceEnvironment supEnv = supIns.getEnv();
            Pointer supConstPtr = (Pointer) supEnv.get(Constants.CONSTRUCTOR, lineFile);
            Method supConst = (Method) env.getMemory().get(supConstPtr);
//            List<Type> supParamTypes = supConst.getFuncType().getParamTypes();
            if (supConst.minArgCount() > 1) {
                // superclass has a non-trivial constructor. Note that any constructor has default 'this' param
                if (noLeadingSuperCall(constructor)) {
                    throw new NativeError("Constructor of child class must first call super.__init__() with matching " +
                            "arguments. ", lineFile);
                }
            } else {
                // superclass constructor has no parameters
                if (noLeadingSuperCall(constructor)) {
                    addDefaultSuperCall(constructor, lineFile);
                }
            }
        }

        return constructor;
    }

    private static Node extractOnlyElementFromLine(Line line) {
        if (line.getChildren().size() == 0) return null;
        else if (line.getChildren().size() == 1) {
            Node node = line.getChildren().get(0);
            if (node instanceof Line) return extractOnlyElementFromLine((Line) node);
            else return node;
        } else {
            throw new NativeError("Too many elements in one line. ", line.getChildren().get(0).getLineFile());
        }
    }

    private static void addDefaultSuperCall(Method constructor, LineFile lineFile) {
        Node body = constructor.getBody();
        if (body instanceof BlockStmt) {
            // call super.init()
            // if super.init(...) has arguments, this causes an error intentionally
            Dot dot = new Dot(lineFile);
            dot.setLeft(new NameNode(Constants.SUPER, lineFile));
            FuncCall supInit = new FuncCall(lineFile);
            supInit.setCallObj(new NameNode(Constants.CONSTRUCTOR, lineFile));
            supInit.setArguments(new Arguments(new Line(lineFile), lineFile));
            dot.setRight(supInit);

            Line constLine = new Line();
            constLine.getChildren().add(dot);
            ((BlockStmt) body).getLines().add(0, constLine);
        } else {
            throw new NativeError("Unexpected syntax. ");
        }
    }

    private static boolean noLeadingSuperCall(Method constructor) {
        Node body = constructor.getBody();
        if (body instanceof BlockStmt) {
            if (((BlockStmt) body).getLines().size() > 0) {
                Line firstLine = ((BlockStmt) body).getLines().get(0);
                if (firstLine.getChildren().size() == 1) {
                    Node node = firstLine.getChildren().get(0);
                    if (node instanceof Dot) {
                        Node left = ((Dot) node).getLeft();
                        Node right = ((Dot) node).getRight();
                        if (left instanceof NameNode && right instanceof FuncCall) {
                            Node callObj = ((FuncCall) right).getCallObj();
                            if (callObj instanceof NameNode) {
                                return !((NameNode) left).getName().equals(Constants.SUPER) ||
                                        !((NameNode) callObj).getName().equals(Constants.CONSTRUCTOR);
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public static class InstanceAndPtr {
        public final Instance instance;
        public final Pointer pointer;

        InstanceAndPtr(Instance instance, Pointer pointer) {
            this.instance = instance;
            this.pointer = pointer;
        }
    }
}
