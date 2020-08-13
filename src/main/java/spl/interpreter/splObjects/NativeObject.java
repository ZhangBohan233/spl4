package spl.interpreter.splObjects;

import spl.ast.Arguments;
import spl.ast.FuncCall;
import spl.ast.NameNode;
import spl.ast.Node;
import spl.interpreter.splErrors.AttributeError;
import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.splErrors.NativeError;
import spl.interpreter.splErrors.TypeError;
import spl.util.LineFile;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NativeObject extends SplObject {

    public SplElement invoke(Node node, Environment callEnv, LineFile lineFile) {
        if (node instanceof NameNode) {
            return nativeAttribute(this, ((NameNode) node).getName(), lineFile);
        } else if (node instanceof FuncCall) {
            if (((FuncCall) node).getCallObj() instanceof NameNode) {
                String name = ((NameNode) ((FuncCall) node).getCallObj()).getName();
                return nativeCall(this, name, ((FuncCall) node).getArguments(), callEnv, lineFile);
            }
        }
        throw new TypeError("Not a native invoke. ");
    }

    private static SplElement nativeAttribute(NativeObject obj, String attrName, LineFile lineFile) {
        // TODO

        return null;
    }

    private static SplElement nativeCall(NativeObject obj,
                                        String methodName,
                                        Arguments arguments,
                                        Environment callEnv,
                                        LineFile lineFile) {
        try {
            Method method = obj.getClass().getMethod(methodName, Arguments.class, Environment.class, LineFile.class);

            return (SplElement) method.invoke(obj, arguments, callEnv, lineFile);
        } catch (NoSuchMethodException | IllegalAccessException e1) {
            throw new AttributeError("Native class '" + obj.getClass() + "' does not have method '" +
                    methodName + ". ", lineFile);
        } catch (InvocationTargetException e) {
            throw new NativeError(e);
        }
    }
}
