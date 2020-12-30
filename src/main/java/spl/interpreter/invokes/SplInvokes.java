package spl.interpreter.invokes;

import spl.ast.*;
import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.BlockEnvironment;
import spl.interpreter.primitives.*;
import spl.interpreter.splErrors.NativeError;
import spl.interpreter.env.Environment;
import spl.interpreter.splObjects.*;
import spl.lexer.FileTokenizer;
import spl.lexer.TextProcessResult;
import spl.lexer.TextProcessor;
import spl.lexer.TokenizeResult;
import spl.parser.Parser;
import spl.util.Constants;
import spl.util.LineFile;

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

        return Reference.NULL_PTR;
    }

    public SplElement print(Arguments arguments, Environment environment, LineFile lineFile) {
        stdout.print(getPrintString(arguments, environment, lineFile));

        return Reference.NULL_PTR;
    }

    public SplElement printErr(Arguments arguments, Environment environment, LineFile lineFile) {
        stderr.print(getPrintString(arguments, environment, lineFile));

        return Reference.NULL_PTR;
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
        Reference ptr = (Reference) arguments.getLine().getChildren().get(0).evaluate(environment);
        SplObject obj = environment.getMemory().get(ptr);
        int freeLength;
        if (obj instanceof SplArray) {
            SplArray array = (SplArray) obj;
            freeLength = array.length + 1;
        } else {
            freeLength = 1;
        }
        environment.getMemory().free(ptr, freeLength);

        return Reference.NULL_PTR;
    }

    public SplElement gc(Arguments arguments, Environment environment, LineFile lineFile) {
        if (arguments.getLine().getChildren().size() != 0) {
            throw new NativeError("Invokes.gc() takes 0 arguments, " +
                    arguments.getLine().getChildren().size() + " given. ", lineFile);
        }

        environment.getMemory().gc(environment);

        return Reference.NULL_PTR;
    }

    public SplElement memoryView(Arguments arguments, Environment environment, LineFile lineFile) {
        checkArgCount(arguments, 0, "memoryView", lineFile);

        stdout.println("Memory: " + environment.getMemory().memoryView());
        stdout.println("Available: " + environment.getMemory().availableView());
        return Reference.NULL_PTR;
    }

    public SplElement id(Arguments arguments, Environment environment, LineFile lineFile) {
        checkArgCount(arguments, 1, "id", lineFile);

        SplElement arg = arguments.getLine().getChildren().get(0).evaluate(environment);
        if (SplElement.isPrimitive(arg))
            return SplInvokes.throwExceptionWithError(
                    environment,
                    Constants.TYPE_ERROR,
                    "Invokes.id() takes a pointer as argument.",
                    lineFile);

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

    public Reference getClass(Arguments arguments, Environment environment, LineFile lineFile) {
        checkArgCount(arguments, 1, "getClass", lineFile);

        Reference insPtr = (Reference) arguments.getLine().get(0).evaluate(environment);
        Instance ins = (Instance) environment.getMemory().get(insPtr);
        return ins.getClazzPtr();
    }

    public SplElement getAttr(Arguments arguments, Environment environment, LineFile lineFile) {
        checkArgCount(arguments, 2, "getAttr", lineFile);

        Reference insPtr = (Reference) arguments.getLine().get(0).evaluate(environment);
        Instance ins = (Instance) environment.getMemory().get(insPtr);

        Reference namePtr = (Reference) arguments.getLine().get(1).evaluate(environment);
        Instance nameIns = (Instance) environment.getMemory().get(namePtr);
        String name = extractFromSplString(nameIns, environment, lineFile);
        return ins.getEnv().get(name, lineFile);
    }

    public Bool hasAttr(Arguments arguments, Environment environment, LineFile lineFile) {
        checkArgCount(arguments, 2, "hasAttr", lineFile);

        Reference insPtr = (Reference) arguments.getLine().get(0).evaluate(environment);
        Instance ins = (Instance) environment.getMemory().get(insPtr);

        NameNode nameNode = (NameNode) arguments.getLine().get(1);
        return Bool.boolValueOf(ins.getEnv().hasName(nameNode.getName()));
    }

    public Bool hasStrAttr(Arguments arguments, Environment environment, LineFile lineFile) {
        checkArgCount(arguments, 2, "hasStrAttr", lineFile);

        Reference insPtr = (Reference) arguments.getLine().get(0).evaluate(environment);
        Instance ins = (Instance) environment.getMemory().get(insPtr);

        Reference namePtr = (Reference) arguments.getLine().get(1).evaluate(environment);
        Instance nameIns = (Instance) environment.getMemory().get(namePtr);
        String name = extractFromSplString(nameIns, environment, lineFile);
        return Bool.boolValueOf(ins.getEnv().hasName(name));
    }

    public void setAttr(Arguments arguments, Environment environment, LineFile lineFile) {
        checkArgCount(arguments, 3, "setAttr", lineFile);

        Reference insPtr = (Reference) arguments.getLine().get(0).evaluate(environment);
        Instance ins = (Instance) environment.getMemory().get(insPtr);

        Reference namePtr = (Reference) arguments.getLine().get(1).evaluate(environment);
        Instance nameIns = (Instance) environment.getMemory().get(namePtr);
        String name = extractFromSplString(nameIns, environment, lineFile);

        ins.getEnv().setVar(name, arguments.getLine().get(2).evaluate(environment), lineFile);
    }

    public SplElement getGlobalByName(Arguments arguments, Environment environment, LineFile lineFile) {
        checkArgCount(arguments, 1, "getGlobalByName", lineFile);

        Reference namePtr = (Reference) arguments.getLine().get(0).evaluate(environment);
        Instance nameIns = (Instance) environment.getMemory().get(namePtr);
        String name = extractFromSplString(nameIns, environment, lineFile);

        return environment.get(name, lineFile);
    }

    public Bool hasGlobalName(Arguments arguments, Environment environment, LineFile lineFile) {
        checkArgCount(arguments, 1, "hasGlobalName", lineFile);

        Reference namePtr = (Reference) arguments.getLine().get(0).evaluate(environment);
        Instance nameIns = (Instance) environment.getMemory().get(namePtr);
        String name = extractFromSplString(nameIns, environment, lineFile);

        return Bool.boolValueOf(environment.hasName(name));
    }

    public SplElement script(Arguments arguments, Environment environment, LineFile lineFile) {
        checkArgCount(arguments, 1, SplCallable.MAX_ARGS, "script", lineFile);

        EvaluatedArguments evaluatedArgs = arguments.evalArgs(environment);
        Instance strIns = (Instance) environment.getMemory().get(
                (Reference) evaluatedArgs.positionalArgs.get(0)
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
                Reference mainPtr = (Reference) subEnv.get(Constants.MAIN_FN, lineFile);
                Function mainFn = (Function) environment.getMemory().get(mainPtr);

                if (mainFn.minArgCount() == 0) {
                    if (evaluatedArgs.positionalArgs.size() > 1)
                        throw new NativeError("spl.Main function in script '" + path + "' does not require " +
                                "command line argument, but arguments are given. ", lineFile);
                    return mainFn.call(EvaluatedArguments.of(), environment, lineFile);
                } else {
                    int mainArgCount = evaluatedArgs.positionalArgs.size();
                    Reference argArray = SplArray.createArray(SplElement.POINTER, mainArgCount, environment);
                    for (int i = 0; i < mainArgCount; i++) {
                        SplArray.setItemAtIndex(
                                argArray, i, evaluatedArgs.positionalArgs.get(i), environment, lineFile);
                    }
                    EvaluatedArguments targetArgs = EvaluatedArguments.of(argArray);
                    return mainFn.call(targetArgs, environment, lineFile);
                }
            }
            return Reference.NULL_PTR;
        } catch (IOException e) {
            throw new NativeError(e);
        }
    }

    /**
     * Static field
     */

    public static void throwException(Environment env, String exceptionClassName, String msg, LineFile lineFile) {
        StringLiteral sl = new StringLiteral(msg.toCharArray(), lineFile);
        FuncCall funcCall = new FuncCall(
                new NameNode(exceptionClassName, lineFile),
                new Arguments(new Line(lineFile, sl), lineFile),
                lineFile);
        NewExpr newExpr = new NewExpr(lineFile);
        newExpr.setValue(funcCall);
        ThrowStmt throwStmt = new ThrowStmt(lineFile);
        throwStmt.setValue(newExpr);

        throwStmt.evaluate(env);
    }

    public static Undefined throwExceptionWithError(Environment env, String exceptionClassName,
                                                    String msg, LineFile lineFile) {
        throwException(env, exceptionClassName, msg, lineFile);
        return Undefined.ERROR;
    }

    /**
     * Helper functions
     */

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
        Reference stringPtr = (Reference) environment.get(Constants.STRING_CLASS, lineFile);
        return getString(element, environment, lineFile, stringPtr);
    }

    private static String getRepr(SplElement element, Environment environment, LineFile lineFile) {
        Reference stringPtr = (Reference) environment.get(Constants.STRING_CLASS, lineFile);
        return getRepr(element, environment, lineFile, stringPtr);
    }

    private static String getString(SplElement element, Environment environment, LineFile lineFile,
                                    Reference stringPtr) {
        if (SplElement.isPrimitive(element)) {
            return element.toString();
        } else {
            return pointerToString((Reference) element, environment, lineFile, stringPtr);
        }
    }

    private static String getRepr(SplElement element, Environment environment, LineFile lineFile,
                                  Reference stringPtr) {
        if (SplElement.isPrimitive(element)) {
            return element.toString();
        } else {
            return pointerToRepr((Reference) element, environment, lineFile, stringPtr);
        }
    }

    private static String getPrintString(Arguments arguments, Environment environment, LineFile lineFile) {
        EvaluatedArguments args = arguments.evalArgs(environment);
        int argc = args.positionalArgs.size();

        Reference stringPtr = (Reference) environment.get(Constants.STRING_CLASS, lineFile);

        String[] resArr = new String[argc];
        for (int i = 0; i < argc; ++i) {
            resArr[i] = getString(args.positionalArgs.get(i), environment, lineFile, stringPtr);
        }
        return String.join(", ", resArr);
    }

    public static String pointerToString(Reference ptr,
                                         Environment environment,
                                         LineFile lineFile) {

        Reference stringPtr = (Reference) environment.get(Constants.STRING_CLASS, lineFile);
        return pointerToString(ptr, environment, lineFile, stringPtr);
    }

    private static String pointerToRepr(Reference ptr,
                                        Environment environment,
                                        LineFile lineFile,
                                        Reference stringPtr) {
        if (ptr.getPtr() == 0) {  // Pointed to null
            return "null";
        } else {
            SplObject object = environment.getMemory().get(ptr);
            if (object instanceof Instance) {
                Instance instance = (Instance) object;

                if (instance.getClazzPtr().getPtr() == stringPtr.getPtr()) {  // is String itself
                    return '"' + extractFromSplString(instance, environment, lineFile) + '"';
                } else {
                    Reference toStrPtr = (Reference) instance.getEnv().get(Constants.TO_REPR_FN, lineFile);
                    Function toStrFtn = (Function) environment.getMemory().get(toStrPtr);
                    Reference toStrRes = (Reference) toStrFtn.call(EvaluatedArguments.of(ptr), environment, lineFile);

                    Instance strIns = (Instance) environment.getMemory().get(toStrRes);
                    return extractFromSplString(strIns, environment, lineFile);
                }
            } else {
                return object.toString() + "@" + ptr.getPtr();
            }
        }
    }

    private static String pointerToString(Reference ptr,
                                          Environment environment,
                                          LineFile lineFile,
                                          Reference stringPtr) {
        if (ptr.getPtr() == 0) {  // Pointed to null
            return "null";
        } else {
            SplObject object = environment.getMemory().get(ptr);
            if (object instanceof Instance) {
                Instance instance = (Instance) object;

                if (instance.getClazzPtr().getPtr() == stringPtr.getPtr()) {  // is String itself
                    return extractFromSplString(instance, environment, lineFile);
                } else {
                    Reference toStrPtr = (Reference) instance.getEnv().get(Constants.TO_STRING_FN, lineFile);
                    Function toStrFtn = (Function) environment.getMemory().get(toStrPtr);
                    Reference toStrRes = (Reference) toStrFtn.call(EvaluatedArguments.of(ptr), environment, lineFile);

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

    private static String arrayToString(int arrayAddr, SplArray array, Environment env, Reference stringPtr,
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
        Reference chars = (Reference) stringInstance.getEnv().get(Constants.STRING_CHARS, lineFile);

        char[] arr = SplArray.toJavaCharArray(chars, env.getMemory());
        return new String(arr);
    }
}
