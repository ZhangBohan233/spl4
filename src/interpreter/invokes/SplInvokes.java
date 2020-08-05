package interpreter.invokes;

import ast.Arguments;
import ast.StringLiteral;
import interpreter.EvaluatedArguments;
import interpreter.splErrors.NativeError;
import interpreter.env.Environment;
import interpreter.primitives.Int;
import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;
import interpreter.primitives.SplFloat;
import interpreter.splErrors.TypeError;
import interpreter.splObjects.*;
import util.Constants;
import util.LineFile;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * Native calls.
 * <p>
 * All public methods can be called from spl, with arguments
 * {@code Arguments}, {@code Environment}, {@code LineFile}
 */
@SuppressWarnings("unused")
public class SplInvokes extends NativeObject {

    private PrintStream stdout = System.out;
    private PrintStream stderr = System.err;
    private InputStream stdin = System.in;

    public void setErr(PrintStream stderr) {
        this.stderr = stderr;
    }

    public void setIn(InputStream stdin) {
        this.stdin = stdin;
    }

    public void setOut(PrintStream stdout) {
        this.stdout = stdout;
    }

    public SplElement println(Arguments arguments, Environment environment, LineFile lineFile) {
        stdout.println(getPrintString(arguments, environment, lineFile));

        return Pointer.NULL_PTR;
    }

    public SplElement print(Arguments arguments, Environment environment, LineFile lineFile) {
        stdout.print(getPrintString(arguments, environment, lineFile));

        return Pointer.NULL_PTR;
    }

    public SplElement printErr(Arguments arguments, Environment environment, LineFile lineFile) {
        stderr.print(getPrintString(arguments, environment, lineFile));

        return Pointer.NULL_PTR;
    }

    public SplElement clock(Arguments arguments, Environment environment, LineFile lineFile) {
        if (arguments.getLine().getChildren().size() != 0) {
            throw new NativeError("System.clock() takes 0 arguments, " +
                    arguments.getLine().getChildren().size() + " given. ", lineFile);
        }
        return new Int(System.currentTimeMillis());
    }

    public SplElement free(Arguments arguments, Environment environment, LineFile lineFile) {
        if (arguments.getLine().getChildren().size() != 1) {
            throw new NativeError("System.free(ptr) takes 1 argument, " +
                    arguments.getLine().getChildren().size() + " given. ", lineFile);
        }
        Pointer ptr = (Pointer) arguments.getLine().getChildren().get(0).evaluate(environment);
        SplObject obj = environment.getMemory().get(ptr);
        int freeLength;
        if (obj instanceof SplArray) {
            SplArray array = (SplArray) obj;
            freeLength = array.length + 1;
        } else {
            freeLength = 1;
        }
        environment.getMemory().free(ptr, freeLength);

        return Pointer.NULL_PTR;
    }

    public SplElement gc(Arguments arguments, Environment environment, LineFile lineFile) {
        if (arguments.getLine().getChildren().size() != 0) {
            throw new NativeError("System.gc() takes 0 arguments, " +
                    arguments.getLine().getChildren().size() + " given. ", lineFile);
        }

        environment.getMemory().gc(environment);

        return Pointer.NULL_PTR;
    }

    public SplElement memoryView(Arguments arguments, Environment environment, LineFile lineFile) {
        checkArgCount(arguments, 0, "memoryView", lineFile);

        stdout.println("Memory: " + environment.getMemory().memoryView());
        stdout.println("Available: " + environment.getMemory().availableView());
        return Pointer.NULL_PTR;
    }

    public SplElement id(Arguments arguments, Environment environment, LineFile lineFile) {
        checkArgCount(arguments, 1, "id", lineFile);

        SplElement arg = arguments.getLine().getChildren().get(0).evaluate(environment);
        if (SplElement.isPrimitive(arg))
            throw new TypeError("System.id() takes a pointer as argument. ", lineFile);

        return new Int(arg.intValue());
    }

    public SplElement string(Arguments arguments, Environment environment, LineFile lineFile) {
        checkArgCount(arguments, 1, "string", lineFile);

        SplElement typeValue = arguments.getLine().getChildren().get(0).evaluate(environment);
        String s = getString(typeValue, environment, lineFile);
        return StringLiteral.createString(s.toCharArray(), environment, lineFile);
    }

    public SplElement repr(Arguments arguments, Environment environment, LineFile lineFile) {
        checkArgCount(arguments, 1, "repr", lineFile);

        SplElement typeValue = arguments.getLine().getChildren().get(0).evaluate(environment);
        String s = getRepr(typeValue, environment, lineFile);
        return StringLiteral.createString(s.toCharArray(), environment, lineFile);
    }

    public SplElement log(Arguments arguments, Environment env, LineFile lineFile) {
        checkArgCount(arguments, 1, "log", lineFile);

        SplElement arg = arguments.getLine().getChildren().get(0).evaluate(env);

        double res = Math.log(arg.floatValue());

        return new SplFloat(res);
    }

    public SplElement pow(Arguments arguments, Environment env, LineFile lineFile) {
        checkArgCount(arguments, 2, "pow", lineFile);

        SplElement base = arguments.getLine().getChildren().get(0).evaluate(env);
        SplElement power = arguments.getLine().getChildren().get(1).evaluate(env);

        double res = Math.pow(base.floatValue(), power.floatValue());

        return new SplFloat(res);
    }

    public SplElement typeName(Arguments arguments, Environment environment, LineFile lineFile) {
        checkArgCount(arguments, 1, "typeName", lineFile);

        SplElement element = arguments.getLine().getChildren().get(0).evaluate(environment);

        String s = element.toString();
        return StringLiteral.createString(s.toCharArray(), environment, lineFile);
    }

    private static void checkArgCount(Arguments arguments, int expectArgc, String fnName, LineFile lineFile) {
        if (arguments.getLine().getChildren().size() != expectArgc) {
            throw new NativeError("System." + fnName + "() takes " + expectArgc + " arguments, " +
                    arguments.getLine().getChildren().size() + " given. ", lineFile);
        }
    }

    private static String getString(SplElement element, Environment environment, LineFile lineFile) {
        Pointer stringPtr = (Pointer) environment.get(Constants.STRING_CLASS, lineFile);
        return getString(element, environment, lineFile, stringPtr);
    }

    private static String getRepr(SplElement element, Environment environment, LineFile lineFile) {
        Pointer stringPtr = (Pointer) environment.get(Constants.STRING_CLASS, lineFile);
        return getRepr(element, environment, lineFile, stringPtr);
    }

    private static String getString(SplElement element, Environment environment, LineFile lineFile,
                                    Pointer stringPtr) {
        if (SplElement.isPrimitive(element)) {
            return element.toString();
        } else {
            return pointerToString((Pointer) element, environment, lineFile, stringPtr);
        }
    }

    private static String getRepr(SplElement element, Environment environment, LineFile lineFile,
                                    Pointer stringPtr) {
        if (SplElement.isPrimitive(element)) {
            return element.toString();
        } else {
            return pointerToRepr((Pointer) element, environment, lineFile, stringPtr);
        }
    }

    private static String getPrintString(Arguments arguments, Environment environment, LineFile lineFile) {
        EvaluatedArguments args = arguments.evalArgs(environment);
        int argc = args.positionalArgs.size();

        Pointer stringPtr = (Pointer) environment.get(Constants.STRING_CLASS, lineFile);

        String[] resArr = new String[argc];
        for (int i = 0; i < argc; ++i) {
            resArr[i] = getString(args.positionalArgs.get(i), environment, lineFile, stringPtr);
        }
        return String.join(", ", resArr);
    }

    public static String pointerToString(Pointer ptr,
                                         Environment environment,
                                         LineFile lineFile) {

        Pointer stringPtr = (Pointer) environment.get(Constants.STRING_CLASS, lineFile);
        return pointerToString(ptr, environment, lineFile, stringPtr);
    }

    private static String pointerToRepr(Pointer ptr,
                                        Environment environment,
                                        LineFile lineFile,
                                        Pointer stringPtr) {
        if (ptr.getPtr() == 0) {  // Pointed to null
            return "null";
        } else {
            SplObject object = environment.getMemory().get(ptr);
            if (object instanceof Instance) {
                Instance instance = (Instance) object;

                if (instance.getClazzPtr().getPtr() == stringPtr.getPtr()) {  // is String itself
                    return '"' + extractFromSplString(instance, environment, lineFile) + '"';
                } else {
                    Pointer toStrPtr = (Pointer) instance.getEnv().get(Constants.TO_REPR_FN, lineFile);
                    Function toStrFtn = (Function) environment.getMemory().get(toStrPtr);
                    Pointer toStrRes = (Pointer) toStrFtn.call(EvaluatedArguments.of(), environment, lineFile);

                    Instance strIns = (Instance) environment.getMemory().get(toStrRes);
                    return extractFromSplString(strIns, environment, lineFile);
                }
            } else {
                return object.toString() + "@" + ptr.getPtr();
            }
        }
    }

    private static String pointerToString(Pointer ptr,
                                          Environment environment,
                                          LineFile lineFile,
                                          Pointer stringPtr) {
        if (ptr.getPtr() == 0) {  // Pointed to null
            return "null";
        } else {
            SplObject object = environment.getMemory().get(ptr);
            if (object instanceof Instance) {
                Instance instance = (Instance) object;

                if (instance.getClazzPtr().getPtr() == stringPtr.getPtr()) {  // is String itself
                    return extractFromSplString(instance, environment, lineFile);
                } else {
                    Pointer toStrPtr = (Pointer) instance.getEnv().get(Constants.TO_STRING_FN, lineFile);
                    Function toStrFtn = (Function) environment.getMemory().get(toStrPtr);
                    Pointer toStrRes = (Pointer) toStrFtn.call(EvaluatedArguments.of(), environment, lineFile);

                    Instance strIns = (Instance) environment.getMemory().get(toStrRes);
                    return extractFromSplString(strIns, environment, lineFile);
                }
            } else if (object instanceof SplArray) {
                return arrayToString(ptr.getPtr(), (SplArray) object, environment, stringPtr, lineFile);
            } else {
                return object.toString() + "@" + ptr.getPtr();
            }
        }
    }

    private static String arrayToString(int arrayAddr, SplArray array, Environment env, Pointer stringPtr,
                                        LineFile lineFile) {
        StringBuilder builder = new StringBuilder("'[");
        for (int i = 0; i < array.length; i++) {
            SplElement e = env.getMemory().getPrimitive(arrayAddr + i + 1);
            builder.append(getRepr(e, env, lineFile, stringPtr));
            if (i < array.length - 1) builder.append(", ");
        }
        return builder.append("]").toString();
    }

    private static String extractFromSplString(Instance stringInstance, Environment env, LineFile lineFile) {
        Pointer chars = (Pointer) stringInstance.getEnv().get(Constants.STRING_CHARS, lineFile);

        char[] arr = SplArray.toJavaCharArray(chars, env.getMemory());
        return new String(arr);
    }
}
