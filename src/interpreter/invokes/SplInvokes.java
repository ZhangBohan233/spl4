package interpreter.invokes;

import ast.*;
import interpreter.EvaluatedArguments;
import interpreter.env.BlockEnvironment;
import interpreter.primitives.*;
import interpreter.splErrors.NativeError;
import interpreter.env.Environment;
import interpreter.splErrors.TypeError;
import interpreter.splObjects.*;
import lexer.FileTokenizer;
import lexer.TextProcessResult;
import lexer.TextProcessor;
import lexer.TokenizeResult;
import lexer.treeList.BraceList;
import parser.Parser;
import util.Constants;
import util.LineFile;

import java.io.File;
import java.io.IOException;
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
            throw new NativeError("Invokes.clock() takes 0 arguments, " +
                    arguments.getLine().getChildren().size() + " given. ", lineFile);
        }
        return new Int(System.currentTimeMillis());
    }

    public SplElement free(Arguments arguments, Environment environment, LineFile lineFile) {
        if (arguments.getLine().getChildren().size() != 1) {
            throw new NativeError("Invokes.free(ptr) takes 1 argument, " +
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
            throw new NativeError("Invokes.gc() takes 0 arguments, " +
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
            throw new TypeError("Invokes.id() takes a pointer as argument. ", lineFile);

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

    public Pointer getClass(Arguments arguments, Environment environment, LineFile lineFile) {
        checkArgCount(arguments, 1, "getClass", lineFile);

        Pointer insPtr = (Pointer) arguments.getLine().get(0).evaluate(environment);
        Instance ins = (Instance) environment.getMemory().get(insPtr);
        return ins.getClazzPtr();
    }

    public Bool hasAttr(Arguments arguments, Environment environment, LineFile lineFile) {
        checkArgCount(arguments, 2, "hasAttr", lineFile);

        Pointer insPtr = (Pointer) arguments.getLine().get(0).evaluate(environment);
        Instance ins = (Instance) environment.getMemory().get(insPtr);

        NameNode nameNode = (NameNode) arguments.getLine().get(1);
        return Bool.boolValueOf(ins.getEnv().hasName(nameNode.getName()));
    }

    public SplElement script(Arguments arguments, Environment environment, LineFile lineFile) {
        checkArgCount(arguments, 1, SplCallable.MAX_ARGS, "script", lineFile);

        EvaluatedArguments evaluatedArgs = arguments.evalArgs(environment);
        Instance strIns = (Instance) environment.getMemory().get(
                (Pointer) evaluatedArgs.positionalArgs.get(0)
        );
        String path = extractFromSplString(strIns, environment, lineFile);

        try {
            FileTokenizer ft = new FileTokenizer(new File(path), false);
            TokenizeResult braceList = ft.tokenize();
            TextProcessResult tpr = new TextProcessor(braceList, false).process();
            Parser parser = new Parser(tpr);
            BlockStmt root = parser.parse();
            BlockEnvironment subEnv = new BlockEnvironment(environment);
            root.evaluate(subEnv);
            if (subEnv.hasName(Constants.MAIN_FN)) {
                Pointer mainPtr = (Pointer) subEnv.get(Constants.MAIN_FN, lineFile);
                Function mainFn = (Function) environment.getMemory().get(mainPtr);

                if (mainFn.minArgCount() == 0) {
                    if (evaluatedArgs.positionalArgs.size() > 1)
                        throw new NativeError("Main function in script '" + path + "' does not require " +
                                "command line argument, but arguments are given. ", lineFile);
                    return mainFn.call(EvaluatedArguments.of(), environment, lineFile);
                } else {
                    int mainArgCount = evaluatedArgs.positionalArgs.size();
                    Pointer argArray = SplArray.createArray(SplElement.POINTER, mainArgCount, environment);
                    for (int i = 0; i < mainArgCount; i++) {
                        SplArray.setItemAtIndex(
                                argArray, i, evaluatedArgs.positionalArgs.get(i), environment, lineFile);
                    }
                    EvaluatedArguments targetArgs = EvaluatedArguments.of(argArray);
                    return mainFn.call(targetArgs, environment, lineFile);
                }
            }
            return Pointer.NULL_PTR;
        } catch (IOException e) {
            throw new NativeError(e);
        }
    }

    private static void checkArgCount(Arguments arguments, int expectArgc, String fnName, LineFile lineFile) {
        if (arguments.getLine().getChildren().size() != expectArgc) {
            throw new NativeError("Invokes." + fnName + "() takes " + expectArgc + " arguments, " +
                    arguments.getLine().getChildren().size() + " given. ", lineFile);
        }
    }

    private static void checkArgCount(Arguments arguments, int minArg, int maxArg, String fnName, LineFile lineFile) {
        if (arguments.getLine().size() < minArg ||
                arguments.getLine().size() > maxArg) {
            throw new NativeError(String.format("Invokes.%s takes %d to %d arguments, %d given. ",
                    fnName, minArg, maxArg, arguments.getLine().size()), lineFile);
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
                    Pointer toStrRes = (Pointer) toStrFtn.call(EvaluatedArguments.of(ptr), environment, lineFile);

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
                    Pointer toStrRes = (Pointer) toStrFtn.call(EvaluatedArguments.of(ptr), environment, lineFile);

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
