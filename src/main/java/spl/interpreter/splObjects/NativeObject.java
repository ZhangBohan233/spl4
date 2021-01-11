package spl.interpreter.splObjects;

import spl.ast.Arguments;
import spl.ast.FuncCall;
import spl.ast.NameNode;
import spl.ast.Node;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.Undefined;
import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.splErrors.NativeError;
import spl.util.Constants;
import spl.util.LineFilePos;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NativeObject extends SplObject {

    public SplElement invoke(Node node, Environment callEnv, LineFilePos lineFile) {
        if (node instanceof NameNode) {
            return nativeAttribute(this, ((NameNode) node).getName(), lineFile);
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

    private static SplElement nativeAttribute(NativeObject obj, String attrName, LineFilePos lineFile) {
        // TODO

        return null;
    }

    private static SplElement nativeCall(NativeObject obj,
                                        String methodName,
                                        Arguments arguments,
                                        Environment callEnv,
                                        LineFilePos lineFile) {
        try {
            Method method = obj.getClass().getMethod(methodName, Arguments.class, Environment.class, LineFilePos.class);

            return (SplElement) method.invoke(obj, arguments, callEnv, lineFile);
        } catch (NoSuchMethodException | IllegalAccessException e1) {
            return SplInvokes.throwExceptionWithError(
                    callEnv,
                    Constants.ATTRIBUTE_EXCEPTION,
                    "Native class '" + obj.getClass() + "' does not have method '" + methodName + "'.",
                    lineFile
            );
        } catch (InvocationTargetException e) {
//            throw new RuntimeException(e);
            return SplInvokes.throwExceptionWithError(
                    callEnv,
                    Constants.INVOKE_ERROR,
                    "Error occurred while calling '" + methodName + "': " + e.getCause().toString() + ".",
                    lineFile
            );
        }
    }

    /**
     * Helper functions
     */

    public static void checkArgCount(Arguments arguments, int expectArgc, String fnName, Environment env, LineFilePos lineFile) {
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
}
