package interpreter.invokes;

import ast.Arguments;
import ast.StringLiteral;
import interpreter.SplException;
import interpreter.env.Environment;
import interpreter.primitives.Int;
import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;
import interpreter.splObjects.*;
import interpreter.types.*;
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

    public SplElement clock(Arguments arguments, Environment environment, LineFile lineFile) {
        if (arguments.getLine().getChildren().size() != 0) {
            throw new SplException("System.clock() takes 0 arguments, " +
                    arguments.getLine().getChildren().size() + " given. ", lineFile);
        }
        return new Int(System.currentTimeMillis());
    }

    public SplElement free(Arguments arguments, Environment environment, LineFile lineFile) {
        if (arguments.getLine().getChildren().size() != 1) {
            throw new SplException("System.free(ptr) takes 1 argument, " +
                    arguments.getLine().getChildren().size() + " given. ", lineFile);
        }
        Pointer ptr = (Pointer) arguments.getLine().getChildren().get(0).evaluate(environment);
//        if (tv.getType().isPrimitive())
//            throw new TypeError("System.free(ptr) takes object pointer as argument. ", lineFile);

//        PointerType type = (PointerType) tv.getType();
//        Pointer ptr = (Pointer) tv.getValue();
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
            throw new SplException("System.gc() takes 0 arguments, " +
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

//    public SplElement typeName(Arguments arguments, Environment environment, LineFile lineFile) {
//        checkArgCount(arguments, 1, "typeName", lineFile);
//
//        SplElement element = arguments.getLine().getChildren().get(0).evaluate(environment);
//
//        String s = element.toString();
//        return StringLiteral.createString(s.toCharArray(), environment, lineFile);
//    }

    private static void checkArgCount(Arguments arguments, int expectArgc, String fnName, LineFile lineFile) {
        if (arguments.getLine().getChildren().size() != expectArgc) {
            throw new SplException("System." + fnName + "() takes " + expectArgc + " arguments, " +
                    arguments.getLine().getChildren().size() + " given. ", lineFile);
        }
    }

    private static String getString(SplElement element, Environment environment, LineFile lineFile) {
        Pointer stringPtr = (Pointer) environment.get(Constants.STRING_CLASS, lineFile);
        return getString(element, environment, lineFile, stringPtr);
    }

    private static String getString(SplElement element, Environment environment, LineFile lineFile,
                                    Pointer stringPtr) {
        if (SplElement.isPrimitive(element)) {
            return element.toString();
        } else {
            return pointerToSting((Pointer) element, environment, lineFile, stringPtr);
        }
    }

    private static String getPrintString(Arguments arguments, Environment environment, LineFile lineFile) {
        SplElement[] args = arguments.evalArgs(environment);

        Pointer stringPtr = (Pointer) environment.get(Constants.STRING_CLASS, lineFile);

        String[] resArr = new String[args.length];
        for (int i = 0; i < args.length; ++i) {
            resArr[i] = getString(args[i], environment, lineFile, stringPtr);
        }
        return String.join(", ", resArr);
    }

    private static String pointerToSting(Pointer ptr,
                                         Environment environment,
                                         LineFile lineFile) {

        Pointer stringPtr = (Pointer) environment.get(Constants.STRING_CLASS, lineFile);
        return pointerToSting(ptr, environment, lineFile, stringPtr);
    }

    private static String pointerToSting(Pointer ptr,
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
                    Pointer toStrRes = (Pointer) toStrFtn.call(new SplElement[0], environment, lineFile);
//                    assert stringType.isSuperclassOfOrEquals(toStrRes.getType(), environment);

                    Instance strIns = (Instance) environment.getMemory().get(toStrRes);
                    return extractFromSplString(strIns, environment, lineFile);
                }
            } else {
                return environment.getMemory().get(ptr).toString() + "@" + ptr.getPtr();
            }
        }
    }

    private static String extractFromSplString(Instance stringInstance, Environment env, LineFile lineFile) {
        Pointer chars = (Pointer) stringInstance.getEnv().get("chars", lineFile);

        char[] arr = SplArray.toJavaCharArray(chars, env.getMemory());
        return new String(arr);
    }
}
