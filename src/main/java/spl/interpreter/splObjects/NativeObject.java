package spl.interpreter.splObjects;

import spl.ast.Arguments;
import spl.ast.FuncCall;
import spl.ast.NameNode;
import spl.ast.Node;
import spl.interpreter.env.Environment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.Int;
import spl.interpreter.primitives.SplElement;
import spl.util.Accessible;
import spl.util.Constants;
import spl.util.LineFilePos;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NativeObject extends SplObject {

    @SuppressWarnings("unchecked")
    private static Field getFieldRecursive(Class<? extends NativeObject> clazz, String attrName)
            throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(attrName);
        } catch (NoSuchFieldException e) {
            if (clazz == NativeObject.class) throw new NoSuchFieldException();
            try {
                return getFieldRecursive((Class<? extends NativeObject>) clazz.getSuperclass(), attrName);
            } catch (ClassCastException e2) {
                throw new NoSuchFieldException();
            }
        }
    }

    private static SplElement nativeAttribute(NativeObject obj, String attrName, Environment env,
                                              LineFilePos lineFile) {
        try {
            Field field = getFieldRecursive(obj.getClass(), attrName);
            Accessible accessible = field.getAnnotation(Accessible.class);
            if (accessible != null) {
                field.setAccessible(true);
                return (SplElement) field.get(obj);
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            //
        }

        SplElement dynAttr = obj.getDynamicAttr(attrName);
        if (dynAttr != null) return dynAttr;

        return SplInvokes.throwExceptionWithError(
                env,
                Constants.ATTRIBUTE_EXCEPTION,
                String.format("Native object '%s' does not have attribute '%s'.",
                        obj.getName(),
                        attrName),
                lineFile
        );
    }

    private static SplElement nativeCall(NativeObject obj,
                                         String methodName,
                                         Arguments arguments,
                                         Environment callEnv,
                                         LineFilePos lineFile) {
        try {
            Method method = obj.getClass().getMethod(methodName, Arguments.class, Environment.class, LineFilePos.class);

            return (SplElement) method.invoke(obj, arguments, callEnv, lineFile);
        } catch (InvocationTargetException e) {
            return SplInvokes.throwExceptionWithError(
                    callEnv,
                    Constants.INVOKE_ERROR,
                    "Error occurred while calling '" + methodName + "': " + e.getCause().toString() + ".",
                    lineFile
            );
        } catch (NoSuchMethodException | IllegalAccessException e1) {
            //
        }

        SplElement dynRes = obj.callDynamicMethod(methodName, arguments, callEnv, lineFile);
        if (dynRes != null) return dynRes;

        return SplInvokes.throwExceptionWithError(
                callEnv,
                Constants.ATTRIBUTE_EXCEPTION,
                "Native object '" + obj.getName() + "' does not have method '" + methodName + "'.",
                lineFile
        );
    }

    public SplElement getDynamicAttr(String attrName) {
        return null;
    }

    public SplElement callDynamicMethod(String methodName,
                                        Arguments arguments,
                                        Environment env,
                                        LineFilePos lineFilePos) {
        return null;
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Helper functions
     */

    public static void checkArgCount(Arguments arguments, int expectArgc, String fnName, Environment env,
                                     LineFilePos lineFile) {
        if (arguments.getLine().getChildren().size() != expectArgc) {
            SplInvokes.throwException(
                    env,
                    Constants.INVOKE_ERROR,
                    String.format("%s() takes %d arguments, but %s were given. ",
                            fnName, expectArgc, arguments.getLine().size()),
                    lineFile
            );
        }
    }

    public static void checkArgCount(Arguments arguments,
                                     int minArg,
                                     int maxArg,
                                     String fnName,
                                     Environment env,
                                     LineFilePos lineFile) {
        if (arguments.getLine().size() < minArg ||
                arguments.getLine().size() > maxArg) {
            SplInvokes.throwException(
                    env,
                    Constants.INVOKE_ERROR,
                    String.format("Invokes.%s takes %d to %d arguments, %d given. ",
                            fnName, minArg, maxArg, arguments.getLine().size()),
                    lineFile
            );
        }
    }

    @Accessible
    public SplElement __hash__(Arguments arguments, Environment env, LineFilePos lineFilePos) {
        checkArgCount(arguments, 0, "__hash__", env, lineFilePos);

        return new Int(this.hashCode());
    }

    public SplElement invoke(Node node, Environment callEnv, LineFilePos lineFile) {
        if (node instanceof NameNode) {
            return nativeAttribute(this, ((NameNode) node).getName(), callEnv, lineFile);
        } else if (node instanceof FuncCall) {
            if (((FuncCall) node).getCallObj() instanceof NameNode) {
                String name = ((NameNode) ((FuncCall) node).getCallObj()).getName();
                return nativeCall(this, name, ((FuncCall) node).getArguments(), callEnv, lineFile);
            }
        }
        return SplInvokes.throwExceptionWithError(
                callEnv,
                Constants.TYPE_ERROR,
                "Not a native invoke",
                lineFile);
    }
}
