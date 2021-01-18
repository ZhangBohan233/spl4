package spl.interpreter.splObjects;

import spl.ast.*;
import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.env.InstanceEnvironment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.Reference;
import spl.interpreter.splErrors.NativeError;
import spl.util.ArrayIterator;
import spl.util.Constants;
import spl.util.LineFilePos;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Instance extends SplObject {

    private final InstanceEnvironment env;
    private final Reference clazzPtr;

    private Instance(Reference clazzPtr, InstanceEnvironment env) {
        this.clazzPtr = clazzPtr;
        this.env = env;
    }

    public static InstanceAndPtr createInstanceAndAllocate(String className,
                                                           Environment callingEnv,
                                                           LineFilePos lineFile) {
        Reference clazzPtr = (Reference) callingEnv.get(className, lineFile);
        return createInstanceAndAllocate(clazzPtr, null, callingEnv, lineFile);
    }

    public static InstanceAndPtr createInstanceAndAllocate(Reference clazzPtr,
                                                           Reference[] generics,
                                                           Environment callingEnv,
                                                           LineFilePos lineFile) {

        SplClass clazz = callingEnv.getMemory().get(clazzPtr);
        Map<String, Reference> determinedGens = getDeterminedGenerics(
                clazz.getTemplates(),
                generics,
                clazz.getClassName(),
                callingEnv,
                lineFile);
        if (callingEnv.hasException()) return null;
        ArrayIterator<Reference> mroIterator = new ArrayIterator<>(clazz.getMroArray());
        if (mroIterator.next() != clazzPtr) throw new NativeError("Unexpected error.");
        return createInstanceAndAllocate(mroIterator, clazzPtr, clazz, determinedGens, callingEnv, lineFile);
    }

    public static InstanceAndPtr createInstanceWithInitCall(String className,
                                                            EvaluatedArguments evaluatedArgs,
                                                            Environment callingEnv,
                                                            LineFilePos lineFile) {
        InstanceAndPtr iap = createInstanceAndAllocate(className, callingEnv, lineFile);
        if (iap == null) return null;
        callInit(iap, evaluatedArgs, callingEnv, lineFile);
        return iap;
    }

    public static InstanceAndPtr createInstanceWithInitCall(Reference clazzPtr,
                                                            Reference[] generics,
                                                            EvaluatedArguments evaluatedArgs,
                                                            Environment callingEnv,
                                                            LineFilePos lineFile) {
        InstanceAndPtr iap = createInstanceAndAllocate(clazzPtr, generics, callingEnv, lineFile);
        if (iap == null) return null;
        callInit(iap, evaluatedArgs, callingEnv, lineFile);
        return iap;
    }

    /**
     * Creates an instance and allocate it in memory.
     *
     * @param mroIterator        iterator of method resolution order, child at first
     * @param clazzPtr           pointer to the class to be processed
     * @param clazz              class to be processed
     * @param determinedGenerics generics that are already set
     * @param callingEnv         env where the new instance is created, not the class definition env
     * @param lineFile           error traceback info of code where instance creation
     * @return the tuple of the newly created instance, and the a pointer point to that instance
     */
    private static InstanceAndPtr createInstanceAndAllocate(ArrayIterator<Reference> mroIterator,
                                                            Reference clazzPtr,
                                                            SplClass clazz,
                                                            Map<String, Reference> determinedGenerics,
                                                            Environment callingEnv,
                                                            LineFilePos lineFile) {
        InstanceEnvironment instanceEnv = new InstanceEnvironment(
                clazz.getClassName(),
                clazz.getDefinitionEnv(),
                callingEnv
        );
        callingEnv.getMemory().addTempEnv(instanceEnv);
        String[] templates = clazz.getTemplates();
        if (templates != null) {
            for (String tem : templates) {
//                System.out.println(tem + " " + determinedGenerics.get(tem));
                instanceEnv.defineConstAndSet(tem, determinedGenerics.get(tem), lineFile);
            }
        }

        Instance instance = new Instance(clazzPtr, instanceEnv);
        Reference instancePtr = callingEnv.getMemory().allocate(1, instanceEnv);
        callingEnv.getMemory().set(instancePtr, instance);

        // evaluate superclasses
        if (mroIterator.hasNext()) {
            Reference supClassPtr = mroIterator.next();
            SplClass supClazz = callingEnv.getMemory().get(supClassPtr);

            // deal with templates
            String[] scTemplates = supClazz.getTemplates();
            Map<String, Reference> gensForSupClass = null;
            if (scTemplates != null) {
                gensForSupClass = new TreeMap<>();
                Line scGens;
                if (clazz.getSuperclassGenerics() == null ||
                        (scGens = clazz.getSuperclassGenerics().get(supClassPtr)) == null) {
                    // class A<T> { ... }
                    // class B<X>(A) { ... }
                    for (String template : supClazz.getTemplates()) {
                        gensForSupClass.put(template, (Reference) callingEnv.get(Constants.ANY_TYPE, lineFile));
                    }
                } else {
                    assert scGens.size() == scTemplates.length;
                    for (int i = 0; i < scTemplates.length; i++) {
                        Reference scGen = (Reference) scGens.get(i).evaluate(instanceEnv);
                        gensForSupClass.put(scTemplates[i], scGen);
                    }
                }
            }

            // initialize superclass
            InstanceAndPtr scInsPtr =
                    createInstanceAndAllocate(
                            mroIterator,
                            supClassPtr,
                            supClazz,
                            gensForSupClass,
                            callingEnv,
                            lineFile);

//            System.out.println(supClazz.getClassName() + scInsPtr.instance.getEnv().names());

            // define "super"
            instance.getEnv().directDefineConstAndSet(Constants.SUPER, scInsPtr.pointer);
        }

        // define fields
        for (Node node : clazz.getFieldNodes().values()) {
            node.evaluate(instanceEnv);
        }

        // define methods
        for (Map.Entry<String, Reference> entry : clazz.getMethodPointers().entrySet()) {
            instanceEnv.defineFunction(entry.getKey(), entry.getValue(), lineFile);
        }

        callingEnv.getMemory().removeTempEnv(instanceEnv);

        return new InstanceAndPtr(instance, instancePtr);
    }

    /**
     * @param iap           Instance and instance pointer
     * @param evaluatedArgs evaluated arguments, without 'this' pointer
     * @param callEnv       calling environment
     * @param lineFile      line file
     */
    private static void callInit(InstanceAndPtr iap,
                                 EvaluatedArguments evaluatedArgs,
                                 Environment callEnv,
                                 LineFilePos lineFile) {

        SplMethod constructor = getConstructor(iap.instance, lineFile);
        evaluatedArgs.insertThis(iap.pointer);
        constructor.call(evaluatedArgs, callEnv, lineFile);
    }

    private static SplMethod getConstructor(Instance instance, LineFilePos lineFile) {
        InstanceEnvironment env = instance.getEnv();
        Reference constructorPtr = (Reference) env.get(Constants.CONSTRUCTOR, lineFile);
        SplMethod constructor = env.getMemory().get(constructorPtr);

        if (env.hasName(Constants.SUPER)) {
            // All classes has superclass except class 'Object'
            Reference superPtr = (Reference) env.get(Constants.SUPER, lineFile);
            Instance supIns = env.getMemory().get(superPtr);
            InstanceEnvironment supEnv = supIns.getEnv();
            Reference supConstPtr = (Reference) supEnv.get(Constants.CONSTRUCTOR, lineFile);
            SplMethod supConst = env.getMemory().get(supConstPtr);
//            List<Type> supParamTypes = supConst.getFuncType().getParamTypes();
            if (supConst.minArgCount() > 1) {
                // superclass has a non-trivial constructor
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

    private static Map<String, Reference> getDeterminedGenerics(String[] templates,
                                                                Reference[] generics,
                                                                String className,
                                                                Environment callingEnv,
                                                                LineFilePos lineFilePos) {
        Map<String, Reference> map = new TreeMap<>();
        if (templates == null) {
            if (generics == null) {
                return map;
            } else {
                SplInvokes.throwException(
                        callingEnv,
                        Constants.ARGUMENT_EXCEPTION,
                        "Class '" + className + "' does not support generic operation.",
                        lineFilePos
                );
                return null;
            }
        } else {
            if (generics == null) {
                for (String template : templates) {
                    map.put(template, (Reference) callingEnv.get(Constants.ANY_TYPE, lineFilePos));
                }
            } else {
                if (templates.length != generics.length) {
                    SplInvokes.throwException(
                            callingEnv,
                            Constants.ARGUMENT_EXCEPTION,
                            String.format("Class '%s' needs %d genes, %d given",
                                    className,
                                    templates.length,
                                    generics.length),
                            lineFilePos
                    );
                    return null;
                }
                for (int i = 0; i < templates.length; i++) {
                    map.put(templates[i], generics[i]);
                }
            }
            return map;
        }
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

    private static void addDefaultSuperCall(Function constructor, LineFilePos lineFile) {
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

    @Override
    public List<Reference> listAttrReferences() {
        return List.of(clazzPtr);
    }

    public InstanceEnvironment getEnv() {
        return env;
    }

    public Reference getClazzPtr() {
        return clazzPtr;
    }

    @Override
    public String toString() {
        return "Instance to<" + clazzPtr.getPtr() + ">";
    }

    public static class InstanceAndPtr {
        public final Instance instance;
        public final Reference pointer;

        InstanceAndPtr(Instance instance, Reference pointer) {
            this.instance = instance;
            this.pointer = pointer;
        }
    }

    private static class ClassAndPtr {
        private final SplClass splClass;
        private final Reference pointer;

        ClassAndPtr(SplClass splClass, Reference pointer) {
            this.splClass = splClass;
            this.pointer = pointer;
        }
    }
}
