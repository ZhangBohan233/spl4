package interpreter.splObjects;

import ast.*;
import interpreter.EvaluatedArguments;
import interpreter.SplException;
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
        return "Instance to<" + clazzPtr.getPtr() + ">";
    }

    public static InstanceAndPtr createInstanceAndAllocate(String className,
                                                           Environment creationEnv,
                                                           LineFile lineFile) {
        Pointer clazzPtr = (Pointer) creationEnv.get(className, lineFile);
        return createInstanceAndAllocate(
                clazzPtr, creationEnv, lineFile, true);
    }

    public static InstanceAndPtr createInstanceAndAllocate(Pointer clazzPtr,
                                                           Environment creationEnv,
                                                           LineFile lineFile) {
        return createInstanceAndAllocate(
                clazzPtr, creationEnv, lineFile, true);
    }

    /**
     * Creates an instance and allocate it in memory.
     *
     * @param clazzPtr              pointer to class
     * @param outerEnv              env where the new instance is created, not the class definition env
     * @param lineFile              error traceback info of code where instance creation
     * @param isFirstCall           whether the currently creating instance is the actual instance
     * @return the tuple of the newly created instance, and the {@code TypeValue} contains the pointer to this instance
     */
    private static InstanceAndPtr createInstanceAndAllocate(Pointer clazzPtr,
                                                            Environment outerEnv,
                                                            LineFile lineFile,
                                                            boolean isFirstCall) {

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

        instance.getEnv().directDefineConstAndSet(Constants.THIS, instancePtr);

        // evaluate superclasses
        List<Pointer> scPointers = clazz.getSuperclassPointers();
        Instance currentChild = instance;
        for (Pointer scPtr : scPointers) {
            InstanceAndPtr scInsPtr = createInstanceAndAllocate(scPtr, outerEnv, lineFile, false);
            currentChild.getEnv().directDefineConstAndSet(Constants.SUPER, scInsPtr.pointer);
            currentChild = scInsPtr.instance;
        }

        clazz.getBody().evaluate(instanceEnv);  // most important step

        if (!instanceEnv.selfContains(Constants.CONSTRUCTOR)) {
            // If class no constructor, put an empty default constructor
            FuncDefinition fd = new FuncDefinition(
                    Constants.CONSTRUCTOR,
                    new Line(),
                    new BlockStmt(LineFile.LF_INTERPRETER),
                    LineFile.LF_INTERPRETER);

            fd.evaluate(instanceEnv);
        }

        outerEnv.getMemory().removeTempEnv(instanceEnv);

        return new InstanceAndPtr(instance, instancePtr);
    }

//    private static void

    public static void callInit(Instance instance, Arguments arguments, Environment callEnv, LineFile lineFile) {
        Function constructor = getConstructor(instance, lineFile);
        constructor.call(arguments, callEnv);
    }

    public static void callInit(Instance instance,
                                EvaluatedArguments evaluatedArgs,
                                Environment callEnv,
                                LineFile lineFile) {

        Function constructor = getConstructor(instance, lineFile);
        constructor.call(evaluatedArgs, callEnv, lineFile);
    }

    private static Function getConstructor(Instance instance, LineFile lineFile) {
        InstanceEnvironment env = instance.getEnv();
        Pointer constructorPtr = (Pointer) env.get(Constants.CONSTRUCTOR, lineFile);
        Function constructor = (Function) env.getMemory().get(constructorPtr);

        if (env.hasName(Constants.SUPER, lineFile)) {
            // All classes has superclass except class 'Object'
            Pointer superPtr = (Pointer) env.get(Constants.SUPER, lineFile);
            Instance supIns = (Instance) env.getMemory().get(superPtr);
            InstanceEnvironment supEnv = supIns.getEnv();
            Pointer supConstPtr = (Pointer) supEnv.get(Constants.CONSTRUCTOR, lineFile);
            Function supConst = (Function) env.getMemory().get(supConstPtr);
//            List<Type> supParamTypes = supConst.getFuncType().getParamTypes();
            if (supConst.minArgCount() > 0) {
                // superclass has a non-trivial constructor
                if (noLeadingSuperCall(constructor)) {
                    throw new SplException("Constructor of child class must first call super.__init__() with matching " +
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
            throw new SplException("Too many elements in one line. ", line.getChildren().get(0).getLineFile());
        }
    }

    private static void addDefaultSuperCall(Function constructor, LineFile lineFile) {
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
            throw new SplException("Unexpected syntax. ");
        }
    }

    private static boolean noLeadingSuperCall(Function constructor) {
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
