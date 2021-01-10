package spl;

import spl.ast.StringLiteral;
import spl.interpreter.EvaluatedArguments;
import spl.interpreter.Memory;
import spl.interpreter.env.Environment;
import spl.interpreter.env.GlobalEnvironment;
import spl.interpreter.env.ModuleEnvironment;
import spl.interpreter.invokes.NativeFile;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.*;
import spl.interpreter.splErrors.NativeError;
import spl.interpreter.splErrors.NativeTypeError;
import spl.interpreter.splObjects.*;
import spl.lexer.FileTokenizer;
import spl.lexer.TextProcessResult;
import spl.lexer.TextProcessor;
import spl.lexer.TokenizeResult;
import spl.lexer.treeList.CollectiveElement;
import spl.parser.ParseResult;
import spl.parser.Parser;
import spl.util.ArgumentParser;
import spl.util.Constants;
import spl.util.LineFilePos;
import spl.util.Utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class SplInterpreter {

    private static InputStream in = System.in;
    private static PrintStream out = System.out;
    private static PrintStream err = System.err;
    private GlobalEnvironment globalEnvironment;

    public static final Map<Class<? extends SplObject>, String> NATIVE_TYPE_NAMES = Map.of(
            SplInvokes.class, "NativeType_Invoke",
            NativeFunction.class, "NativeType_NativeFunction",
            SplArray.class, "NativeType_Array",
            SplModule.class, "NativeType_Module",
            SplMethod.class, "NativeType_Method",
            Function.class, "NativeType_Function",
            LambdaExpression.class, "NativeType_LambdaExpression",
            SplClass.class, "NativeType_Class",
            NativeType.class, "NativeType_NativeType"
    );

    public static void setOut(PrintStream out) {
        SplInterpreter.out = out;
    }

    public static void setIn(InputStream in) {
        SplInterpreter.in = in;
    }

    public static void setErr(PrintStream err) {
        SplInterpreter.err = err;
    }

    static void initNatives(GlobalEnvironment globalEnvironment) {
        initNativeFunctions(globalEnvironment);

        SplInvokes system = new SplInvokes(out, in, err);

        Memory memory = globalEnvironment.getMemory();
        Reference sysPtr = memory.allocateObject(system, globalEnvironment);

        globalEnvironment.defineConstAndSet(
                Constants.INVOKES,
                sysPtr,
                LineFilePos.LF_INTERPRETER);

        for (String name : NATIVE_TYPE_NAMES.values()) {
            NativeType nt = new NativeType(name);
            Reference ptr = memory.allocateObject(nt, globalEnvironment);
            globalEnvironment.defineConstAndSet(
                    name,
                    ptr,
                    LineFilePos.LF_INTERPRETER
            );
        }
    }

    static void importModules(GlobalEnvironment ge, Map<String, CollectiveElement> imported,
                              Map<String, StringLiteral> strLitBundleMap) throws IOException {
        Map<String, ParseResult> blocks = parseImportedModules(imported, strLitBundleMap);
        for (Map.Entry<String, ParseResult> entry : blocks.entrySet()) {
            ModuleEnvironment moduleScope = new ModuleEnvironment(ge);
            entry.getValue().getRoot().evaluate(moduleScope);
            SplModule module = new SplModule(entry.getKey(), moduleScope);

            Reference ptr = ge.getMemory().allocateObject(module, moduleScope);

            ge.addImportedModulePtr(entry.getKey(), ptr);
        }
    }

    public static Map<String, ParseResult> parseImportedModules(Map<String, CollectiveElement> imported,
                                                                Map<String, StringLiteral> strLitBundleMap)
            throws IOException {
        Map<String, ParseResult> result = new HashMap<>();
        for (Map.Entry<String, CollectiveElement> entry : imported.entrySet()) {
            Parser psr = new Parser(new TextProcessResult(entry.getValue()), strLitBundleMap);
            ParseResult ce = psr.parse();

            result.put(entry.getKey(), ce);
        }
        return result;
    }

    private static void initNativeFunctions(GlobalEnvironment ge) {
        NativeFunction toInt = new NativeFunction("int", 1) {
            @Override
            protected SplElement callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                SplElement arg = evaluatedArgs.positionalArgs.get(0);
                if (arg instanceof Reference) {
                    return new Int(
                            Utilities.wrapperToPrimitive(
                                    (Reference) arg,
                                    callingEnv,
                                    LineFilePos.LF_INTERPRETER).intValue());
                } else {
                    return new Int(arg.intValue());
                }
            }
        };

        NativeFunction isInt = new NativeFunction("int?", 1) {
            @Override
            protected Bool callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                SplElement arg = evaluatedArgs.positionalArgs.get(0);
                return Bool.boolValueOf(arg instanceof Int);
            }
        };

        NativeFunction toFloat = new NativeFunction("float", 1) {
            @Override
            protected SplElement callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                SplElement arg = evaluatedArgs.positionalArgs.get(0);
                if (arg instanceof Reference) {
                    return new SplFloat(
                            Utilities.wrapperToPrimitive(
                                    (Reference) arg,
                                    callingEnv,
                                    LineFilePos.LF_INTERPRETER).floatValue());
                } else {
                    return new SplFloat(arg.floatValue());
                }
            }
        };

        NativeFunction isFloat = new NativeFunction("float?", 1) {
            @Override
            protected Bool callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                SplElement arg = evaluatedArgs.positionalArgs.get(0);
                return Bool.boolValueOf(arg instanceof SplFloat);
            }
        };

        NativeFunction toChar = new NativeFunction("char", 1) {
            @Override
            protected SplElement callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                SplElement arg = evaluatedArgs.positionalArgs.get(0);
                if (arg instanceof Reference) {
                    return new Char(
                            (char) Utilities.wrapperToPrimitive(
                                    (Reference) arg,
                                    callingEnv,
                                    LineFilePos.LF_INTERPRETER).intValue());
                } else {
                    return new Char((char) arg.intValue());
                }
            }
        };

        NativeFunction isChar = new NativeFunction("char?", 1) {
            @Override
            protected Bool callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                SplElement arg = evaluatedArgs.positionalArgs.get(0);
                return Bool.boolValueOf(arg instanceof Char);
            }
        };

        NativeFunction toByte = new NativeFunction("byte", 1) {
            @Override
            protected SplElement callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                SplElement arg = evaluatedArgs.positionalArgs.get(0);
                if (arg instanceof Reference) {
                    return new SplByte(
                            (byte) Utilities.wrapperToPrimitive(
                                    (Reference) arg,
                                    callingEnv,
                                    LineFilePos.LF_INTERPRETER).intValue());
                } else {
                    return new SplByte((byte) arg.intValue());
                }
            }
        };

        NativeFunction isByte = new NativeFunction("byte?", 1) {
            @Override
            protected Bool callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                SplElement arg = evaluatedArgs.positionalArgs.get(0);
                return Bool.boolValueOf(arg instanceof SplByte);
            }
        };

        NativeFunction toBool = new NativeFunction("boolean", 1) {
            @Override
            protected Bool callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                SplElement arg = evaluatedArgs.positionalArgs.get(0);
                if (arg instanceof Reference) {
                    return Bool.boolValueOf(
                            Utilities.wrapperToPrimitive(
                                    (Reference) arg,
                                    callingEnv,
                                    LineFilePos.LF_INTERPRETER).booleanValue());
                } else {
                    return Bool.boolValueOf(arg.booleanValue());
                }
            }
        };

        NativeFunction isBool = new NativeFunction("boolean?", 1) {
            @Override
            protected Bool callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                SplElement arg = evaluatedArgs.positionalArgs.get(0);
                return Bool.boolValueOf(arg instanceof Bool);
            }
        };

        NativeFunction isAbstractObject = new NativeFunction("AbstractObject?", 1) {
            @Override
            protected Bool callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                SplElement arg = evaluatedArgs.positionalArgs.get(0);
                if (arg instanceof Reference) {
                    SplObject object = callingEnv.getMemory().get((Reference) arg);
                    return Bool.boolValueOf(object != null);
                }
                return Bool.FALSE;
            }
        };

        NativeFunction isArray = new NativeFunction("Array?", 1) {
            @Override
            protected Bool callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                SplElement arg = evaluatedArgs.positionalArgs.get(0);
                if (arg instanceof Reference) {
                    SplObject object = callingEnv.getMemory().get((Reference) arg);
                    return Bool.boolValueOf(object instanceof SplArray);
                }
                return Bool.FALSE;
            }
        };

        NativeFunction isCallable = new NativeFunction("Callable?", 1) {
            @Override
            protected Bool callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                SplElement arg = evaluatedArgs.positionalArgs.get(0);
                if (arg instanceof Reference) {
                    SplObject object = callingEnv.getMemory().get((Reference) arg);
                    return Bool.boolValueOf(object instanceof SplCallable);
                }
                return Bool.FALSE;
            }
        };

        NativeFunction isClass = new NativeFunction("Class?", 1) {
            @Override
            protected Bool callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                SplElement arg = evaluatedArgs.positionalArgs.get(0);
                if (arg instanceof Reference) {
                    SplObject object = callingEnv.getMemory().get((Reference) arg);
                    return Bool.boolValueOf(object instanceof SplClass);
                }
                return Bool.FALSE;
            }
        };

        NativeFunction isNull = new NativeFunction("null?", 1) {
            @Override
            protected Bool callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                SplElement arg = evaluatedArgs.positionalArgs.get(0);
                if (arg instanceof Reference) {
                    return Bool.boolValueOf(((Reference) arg).getPtr() == 0);
                }
                return Bool.FALSE;
            }
        };

        NativeFunction isNatFile = new NativeFunction("NativeFile?", 1) {
            @Override
            protected Bool callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                SplElement arg = evaluatedArgs.positionalArgs.get(0);
                if (arg instanceof Reference) {
                    SplObject object = callingEnv.getMemory().get((Reference) arg);
                    return Bool.boolValueOf(object instanceof NativeFile);
                }
                return Bool.FALSE;
            }
        };

        NativeFunction isModule = new NativeFunction("Module?", 1) {
            @Override
            protected Bool callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                SplElement arg = evaluatedArgs.positionalArgs.get(0);
                if (arg instanceof Reference) {
                    SplObject object = callingEnv.getMemory().get((Reference) arg);
                    return Bool.boolValueOf(object instanceof SplModule);
                }
                return Bool.FALSE;
            }
        };

        Memory memory = ge.getMemory();
        Reference ptrInt = memory.allocateFunction(toInt, ge);
        Reference ptrIsInt = memory.allocateFunction(isInt, ge);
        Reference ptrFloat = memory.allocateFunction(toFloat, ge);
        Reference ptrChar = memory.allocateFunction(toChar, ge);
        Reference ptrByte = memory.allocateFunction(toByte, ge);
        Reference ptrBool = memory.allocateFunction(toBool, ge);
        Reference ptrIsFloat = memory.allocateFunction(isFloat, ge);
        Reference ptrIsChar = memory.allocateFunction(isChar, ge);
        Reference ptrIsByte = memory.allocateFunction(isByte, ge);
        Reference ptrIsBool = memory.allocateFunction(isBool, ge);
        Reference ptrIsAbsObj = memory.allocateFunction(isAbstractObject, ge);
        Reference ptrIsArray = memory.allocateFunction(isArray, ge);
        Reference ptrIsCallable = memory.allocateFunction(isCallable, ge);
        Reference ptrIsClass = memory.allocateFunction(isClass, ge);
        Reference ptrIsNull = memory.allocateFunction(isNull, ge);
        Reference ptrIsNatFile = memory.allocateFunction(isNatFile, ge);
        Reference ptrIsModule = memory.allocateFunction(isModule, ge);

        ge.defineFunction("int", ptrInt, LineFilePos.LF_INTERPRETER);
        ge.defineFunction("int?", ptrIsInt, LineFilePos.LF_INTERPRETER);
        ge.defineFunction("float", ptrFloat, LineFilePos.LF_INTERPRETER);
        ge.defineFunction("float?", ptrIsFloat, LineFilePos.LF_INTERPRETER);
        ge.defineFunction("char", ptrChar, LineFilePos.LF_INTERPRETER);
        ge.defineFunction("char?", ptrIsChar, LineFilePos.LF_INTERPRETER);
        ge.defineFunction("byte", ptrByte, LineFilePos.LF_INTERPRETER);
        ge.defineFunction("byte?", ptrIsByte, LineFilePos.LF_INTERPRETER);
        ge.defineFunction("boolean", ptrBool, LineFilePos.LF_INTERPRETER);
        ge.defineFunction("boolean?", ptrIsBool, LineFilePos.LF_INTERPRETER);
        ge.defineFunction("AbstractObject?", ptrIsAbsObj, LineFilePos.LF_INTERPRETER);
        ge.defineFunction("Array?", ptrIsArray, LineFilePos.LF_INTERPRETER);
        ge.defineFunction("Callable?", ptrIsCallable, LineFilePos.LF_INTERPRETER);
        ge.defineFunction("Class?", ptrIsClass, LineFilePos.LF_INTERPRETER);
        ge.defineFunction("null?", ptrIsNull, LineFilePos.LF_INTERPRETER);
        ge.defineFunction("NativeFile?", ptrIsNatFile, LineFilePos.LF_INTERPRETER);
        ge.defineFunction("Module?", ptrIsModule, LineFilePos.LF_INTERPRETER);
    }

    private static EvaluatedArguments makeSplArgArray(String[] args, GlobalEnvironment globalEnvironment) {
        Reference argPtr = SplArray.createArray(SplElement.POINTER, args.length, globalEnvironment);
        for (int i = 0; i < args.length; ++i) {

            // create String instance
            Reference strIns = StringLiteral.createString(
                    args[i].toCharArray(), globalEnvironment, LineFilePos.LF_INTERPRETER
            );
            SplArray.setItemAtIndex(argPtr, i, strIns, globalEnvironment, LineFilePos.LF_INTERPRETER);
        }
        return EvaluatedArguments.of(argPtr);
    }

    public void run(String[] args) throws Exception {
        ArgumentParser argumentParser = new ArgumentParser(args);
        if (argumentParser.isAllValid()) {
            long parseBegin = System.currentTimeMillis();
            FileTokenizer tokenizer = new FileTokenizer(
                    argumentParser.getMainSrcFile(),
                    argumentParser.importLang() && globalEnvironment == null  // no preset global env
            );
            TokenizeResult rootToken = tokenizer.tokenize();
            TextProcessResult processed = new TextProcessor(rootToken,
                    argumentParser.importLang()).process();  // keep this to let other files import lang
            if (argumentParser.isPrintTokens()) {
                out.println(processed.rootList);
            }
            Parser parser = new Parser(processed);
            ParseResult parseResult = parser.parse();

            long vmStartBegin = System.currentTimeMillis();
            Memory memory = new Memory();
            if (globalEnvironment == null) {
                globalEnvironment = new GlobalEnvironment(memory);
                initNatives(globalEnvironment);
            }
            importModules(globalEnvironment, processed.importedPaths, parser.getStringLiterals());

            if (argumentParser.isPrintAst()) {
                out.println("===== Ast =====");
                out.println(parseResult.getRoot());
                out.println("===== End of spl.ast =====");
            }

            if (argumentParser.isGcInfo()) memory.debugs.setPrintGcRes(true);
            if (argumentParser.isGcTrigger()) memory.debugs.setPrintGcTrigger(true);
            memory.setCheckContract(argumentParser.isCheckContract());

            long runBegin = System.currentTimeMillis();

            try {
                parseResult.getRoot().evaluate(globalEnvironment);

                callMain(argumentParser.getSplArgs(), globalEnvironment);
            } catch (ClassCastException cce) {
                cce.printStackTrace();
                throw new NativeTypeError();
            }

            long processEnd = System.currentTimeMillis();

            if (argumentParser.isPrintMem()) {
                memory.printMemory();
            }
            if (argumentParser.isTimer()) {
                out.printf(
                        "Parse time: %d ms, VM startup time: %d ms, running time: %d ms.%n",
                        vmStartBegin - parseBegin,
                        runBegin - vmStartBegin,
                        processEnd - runBegin
                );
            }
        } else {
            System.out.println(argumentParser.getMsg());
        }
    }

    public void setGlobalEnvironment(GlobalEnvironment globalEnvironment) {
        this.globalEnvironment = globalEnvironment;
    }

    void callMain(String[] args, GlobalEnvironment globalEnvironment) {
        if (globalEnvironment.hasName(Constants.MAIN_FN)) {
            Reference mainPtr = (Reference) globalEnvironment.get(Constants.MAIN_FN, Main.LF_MAIN);

            Function mainFunc = (Function) globalEnvironment.getMemory().get(mainPtr);
            if (mainFunc.minArgCount() > 1) {
                throw new NativeError("Function main takes 0 or 1 arguments.");
            }
            EvaluatedArguments splArg =
                    mainFunc.minArgCount() == 0 ? new EvaluatedArguments() : makeSplArgArray(args, globalEnvironment);

            SplElement rtn = mainFunc.call(splArg, globalEnvironment, Main.LF_MAIN);

            if (globalEnvironment.hasException()) {
                Utilities.removeErrorAndPrint(globalEnvironment, Main.LF_MAIN);
            } else {
                out.println("Process finished with exit value " + rtn);
            }
        }
    }
}
