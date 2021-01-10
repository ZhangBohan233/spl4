package spl.interpreter.invokes;

import spl.ast.*;
import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.BlockEnvironment;
import spl.interpreter.env.Environment;
import spl.interpreter.primitives.*;
import spl.interpreter.splErrors.NativeError;
import spl.interpreter.splObjects.*;
import spl.lexer.FileTokenizer;
import spl.lexer.TextProcessResult;
import spl.lexer.TextProcessor;
import spl.lexer.TokenizeResult;
import spl.parser.Parser;
import spl.util.Constants;
import spl.util.LineFilePos;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * Native calls.
 * <p>
 * All public methods can be called from spl, with arguments
 * {@code Arguments}, {@code Environment}, {@code LineFile}
 */
@SuppressWarnings("unused")
public class SplInvokes extends NativeObject {

    private PrintStream stdout;
    private PrintStream stderr;
    private InputStream stdin;

    public SplInvokes(PrintStream stdout, InputStream stdin, PrintStream stderr) {
        this.stdout = stdout;
        this.stdin = stdin;
        this.stderr = stderr;
    }

    /**
     * Static field
     */

    public static void throwException(Environment env, String exceptionClassName, String msg, LineFilePos lineFile) {
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
                                                    String msg, LineFilePos lineFile) {
        throwException(env, exceptionClassName, msg, lineFile);
        return Undefined.ERROR;
    }

    /**
     * Helper functions
     */

    private static void checkArgCount(Arguments arguments, int expectArgc, String fnName, Environment env, LineFilePos lineFile) {
        if (arguments.getLine().getChildren().size() != expectArgc) {
            throwException(
                    env,
                    Constants.INVOKE_ERROR,
                    String.format("Invoke %s() takes %d arguments, but %s were given. ",
                            fnName, expectArgc, arguments.getLine().size()),
                    lineFile
            );
        }
    }

    private static void checkArgCount(Arguments arguments,
                                      int minArg,
                                      int maxArg,
                                      String fnName,
                                      Environment env,
                                      LineFilePos lineFile) {
        if (arguments.getLine().size() < minArg ||
                arguments.getLine().size() > maxArg) {
            throwException(
                    env,
                    Constants.INVOKE_ERROR,
                    String.format("Invokes.%s takes %d to %d arguments, %d given. ",
                            fnName, minArg, maxArg, arguments.getLine().size()),
                    lineFile
            );
        }
    }

    private static String getString(SplElement element, Environment environment, LineFilePos lineFile) {
        Reference stringPtr = (Reference) environment.get(Constants.STRING_CLASS, lineFile);
        return getString(element, environment, lineFile, stringPtr);
    }

    public static String getRepr(SplElement element, Environment environment, LineFilePos lineFile) {
        Reference stringPtr = (Reference) environment.get(Constants.STRING_CLASS, lineFile);
        return getRepr(element, environment, lineFile, stringPtr);
    }

    private static String getString(SplElement element, Environment environment, LineFilePos lineFile,
                                    Reference stringPtr) {
        if (SplElement.isPrimitive(element)) {
            return element.toString();
        } else {
            return pointerToString((Reference) element, environment, lineFile, stringPtr);
        }
    }

    private static String getRepr(SplElement element, Environment environment, LineFilePos lineFile,
                                  Reference stringPtr) {
        if (SplElement.isPrimitive(element)) {
            return element.toString();
        } else {
            return pointerToRepr((Reference) element, environment, lineFile, stringPtr);
        }
    }

    private static String getPrintString(Arguments arguments, Environment environment, LineFilePos lineFile) {
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
                                         LineFilePos lineFile) {

        Reference stringPtr = (Reference) environment.get(Constants.STRING_CLASS, lineFile);
        return pointerToString(ptr, environment, lineFile, stringPtr);
    }

    private static String pointerToRepr(Reference ptr,
                                        Environment environment,
                                        LineFilePos lineFile,
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
                                          LineFilePos lineFile,
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
                                        LineFilePos lineFile) {
        StringBuilder builder = new StringBuilder("'[");
        for (int i = 0; i < array.length; i++) {
            SplElement e = env.getMemory().getPrimitive(arrayAddr + i + 1);
            builder.append(getRepr(e, env, lineFile, stringPtr));
            if (i < array.length - 1) builder.append(", ");
        }
        return builder.append("]").toString();
    }

    private static String extractFromSplString(Instance stringInstance, Environment env, LineFilePos lineFile) {
        Reference chars = (Reference) stringInstance.getEnv().get(Constants.STRING_CHARS, lineFile);

        char[] arr = SplArray.toJavaCharArray(chars, env.getMemory());
        return new String(arr);
    }

    public void setErr(PrintStream stderr) {
        this.stderr = stderr;
    }

    public void setIn(InputStream stdin) {
        this.stdin = stdin;
    }

    public void setOut(PrintStream stdout) {
        this.stdout = stdout;
    }

    public SplElement println(Arguments arguments, Environment environment, LineFilePos lineFile) {
        stdout.println(getPrintString(arguments, environment, lineFile));

        return Reference.NULL;
    }

    public SplElement print(Arguments arguments, Environment environment, LineFilePos lineFile) {
        stdout.print(getPrintString(arguments, environment, lineFile));

        return Reference.NULL;
    }

    public SplElement printErr(Arguments arguments, Environment environment, LineFilePos lineFile) {
        stderr.print(getPrintString(arguments, environment, lineFile));

        return Reference.NULL;
    }

    public SplElement input(Arguments arguments, Environment environment, LineFilePos lineFile) {
        checkArgCount(arguments, 1, "input", environment, lineFile);

        // input(prompt)

        print(new Arguments(new Line(lineFile, arguments.getLine().get(0)), lineFile), environment, lineFile);

        Scanner sc = new Scanner(stdin);
        String next = sc.next();
        sc.close();

        return StringLiteral.createString(next.toCharArray(), environment, lineFile);
    }

    public SplElement clock(Arguments arguments, Environment environment, LineFilePos lineFile) {
        checkArgCount(arguments, 0, "clock", environment, lineFile);
        return new Int(System.currentTimeMillis());
    }

    public SplElement gc(Arguments arguments, Environment environment, LineFilePos lineFile) {
        checkArgCount(arguments, 0, "gc", environment, lineFile);

        environment.getMemory().gc(environment);

        return Reference.NULL;
    }

    public SplElement memoryView(Arguments arguments, Environment environment, LineFilePos lineFile) {
        checkArgCount(arguments, 0, "memoryView", environment, lineFile);

        stdout.println("Memory: " + environment.getMemory().memoryView());
        stdout.println("Available: " + environment.getMemory().availableView());
        return Reference.NULL;
    }

    public SplElement id(Arguments arguments, Environment environment, LineFilePos lineFile) {
        checkArgCount(arguments, 1, "id", environment, lineFile);

        SplElement arg = arguments.getLine().getChildren().get(0).evaluate(environment);
        if (SplElement.isPrimitive(arg))
            return SplInvokes.throwExceptionWithError(
                    environment,
                    Constants.TYPE_ERROR,
                    "Invokes.id() takes a pointer as argument.",
                    lineFile);

        return new Int(arg.intValue());
    }

    public SplElement string(Arguments arguments, Environment environment, LineFilePos lineFile) {
        checkArgCount(arguments, 1, "string", environment, lineFile);

        SplElement typeValue = arguments.getLine().getChildren().get(0).evaluate(environment);
        String s = getString(typeValue, environment, lineFile);
        return StringLiteral.createString(s.toCharArray(), environment, lineFile);
    }

    public SplElement repr(Arguments arguments, Environment environment, LineFilePos lineFile) {
        checkArgCount(arguments, 1, "repr", environment, lineFile);

        SplElement typeValue = arguments.getLine().getChildren().get(0).evaluate(environment);
        String s = getRepr(typeValue, environment, lineFile);
        return StringLiteral.createString(s.toCharArray(), environment, lineFile);
    }

    public SplElement log(Arguments arguments, Environment env, LineFilePos lineFile) {
        checkArgCount(arguments, 1, "log", env, lineFile);

        SplElement arg = arguments.getLine().getChildren().get(0).evaluate(env);

        double res = Math.log(arg.floatValue());

        return new SplFloat(res);
    }

    public SplElement pow(Arguments arguments, Environment env, LineFilePos lineFile) {
        checkArgCount(arguments, 2, "pow", env, lineFile);

        SplElement base = arguments.getLine().getChildren().get(0).evaluate(env);
        SplElement power = arguments.getLine().getChildren().get(1).evaluate(env);

        double res = Math.pow(base.floatValue(), power.floatValue());

        return new SplFloat(res);
    }

    public SplElement typeName(Arguments arguments, Environment environment, LineFilePos lineFile) {
        checkArgCount(arguments, 1, "typeName", environment, lineFile);

        SplElement element = arguments.getLine().getChildren().get(0).evaluate(environment);

        String s = element.toString();
        return StringLiteral.createString(s.toCharArray(), environment, lineFile);
    }

    public Reference getClass(Arguments arguments, Environment environment, LineFilePos lineFile) {
        checkArgCount(arguments, 1, "getClass", environment, lineFile);

        Reference insPtr = (Reference) arguments.getLine().get(0).evaluate(environment);
        Instance ins = (Instance) environment.getMemory().get(insPtr);
        return ins.getClazzPtr();
    }

    public SplElement getAttr(Arguments arguments, Environment environment, LineFilePos lineFile) {
        checkArgCount(arguments, 2, "getAttr", environment, lineFile);

        Reference insPtr = (Reference) arguments.getLine().get(0).evaluate(environment);
        Instance ins = (Instance) environment.getMemory().get(insPtr);

        Reference namePtr = (Reference) arguments.getLine().get(1).evaluate(environment);
        Instance nameIns = (Instance) environment.getMemory().get(namePtr);
        String name = extractFromSplString(nameIns, environment, lineFile);
        return ins.getEnv().get(name, lineFile);
    }

    public Bool hasAttr(Arguments arguments, Environment environment, LineFilePos lineFile) {
        checkArgCount(arguments, 2, "hasAttr", environment, lineFile);

        Reference insPtr = (Reference) arguments.getLine().get(0).evaluate(environment);
        Instance ins = (Instance) environment.getMemory().get(insPtr);

        NameNode nameNode = (NameNode) arguments.getLine().get(1);
        return Bool.boolValueOf(ins.getEnv().hasName(nameNode.getName()));
    }

    public Bool hasStrAttr(Arguments arguments, Environment environment, LineFilePos lineFile) {
        checkArgCount(arguments, 2, "hasStrAttr", environment, lineFile);

        Reference insPtr = (Reference) arguments.getLine().get(0).evaluate(environment);
        Instance ins = (Instance) environment.getMemory().get(insPtr);

        Reference namePtr = (Reference) arguments.getLine().get(1).evaluate(environment);
        Instance nameIns = (Instance) environment.getMemory().get(namePtr);
        String name = extractFromSplString(nameIns, environment, lineFile);
        return Bool.boolValueOf(ins.getEnv().hasName(name));
    }

    public void setAttr(Arguments arguments, Environment environment, LineFilePos lineFile) {
        checkArgCount(arguments, 3, "setAttr", environment, lineFile);

        Reference insPtr = (Reference) arguments.getLine().get(0).evaluate(environment);
        Instance ins = (Instance) environment.getMemory().get(insPtr);

        Reference namePtr = (Reference) arguments.getLine().get(1).evaluate(environment);
        Instance nameIns = (Instance) environment.getMemory().get(namePtr);
        String name = extractFromSplString(nameIns, environment, lineFile);

        ins.getEnv().setVar(name, arguments.getLine().get(2).evaluate(environment), lineFile);
    }

    public SplElement getGlobalByName(Arguments arguments, Environment environment, LineFilePos lineFile) {
        checkArgCount(arguments, 1, "getGlobalByName", environment, lineFile);

        Reference namePtr = (Reference) arguments.getLine().get(0).evaluate(environment);
        Instance nameIns = (Instance) environment.getMemory().get(namePtr);
        String name = extractFromSplString(nameIns, environment, lineFile);

        return environment.get(name, lineFile);
    }

    public Bool hasGlobalName(Arguments arguments, Environment environment, LineFilePos lineFile) {
        checkArgCount(arguments, 1, "hasGlobalName", environment, lineFile);

        Reference namePtr = (Reference) arguments.getLine().get(0).evaluate(environment);
        Instance nameIns = (Instance) environment.getMemory().get(namePtr);
        String name = extractFromSplString(nameIns, environment, lineFile);

        return Bool.boolValueOf(environment.hasName(name));
    }

    public SplElement openFile(Arguments arguments, Environment env, LineFilePos lineFilePos) {
        checkArgCount(arguments, 2, "openFile", env, lineFilePos);

        Reference fileRef = (Reference) arguments.getLine().get(0).evaluate(env);
        Int mode = (Int) arguments.getLine().get(1).evaluate(env);

        Instance fileNameIns = (Instance) env.getMemory().get(fileRef);
        String fileName = extractFromSplString(fileNameIns, env, lineFilePos);

        NativeFile nf;
        try {
            nf = new NativeFile(fileName, mode.intValue());
        } catch (IOException e) {
            return throwExceptionWithError(env,
                    Constants.IO_ERROR,
                    "Cannot open '" + fileName + "'.",
                    lineFilePos);
        }

        return env.getMemory().allocateObject(nf, env);
    }

    public SplElement script(Arguments arguments, Environment environment, LineFilePos lineFile) {
        checkArgCount(arguments, 1, SplCallable.MAX_ARGS, "script", environment, lineFile);

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
            BlockStmt root = parser.parse().getRoot();
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
            return Reference.NULL;
        } catch (IOException e) {
            throw new NativeError(e);
        }
    }
}
