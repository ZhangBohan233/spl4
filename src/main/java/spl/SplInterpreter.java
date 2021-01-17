package spl;

import spl.ast.StringLiteral;
import spl.interpreter.EvaluatedArguments;
import spl.interpreter.Memory;
import spl.interpreter.env.Environment;
import spl.interpreter.env.GlobalEnvironment;
import spl.interpreter.env.ModuleEnvironment;
import spl.interpreter.invokes.NativeInFile;
import spl.interpreter.invokes.NativeOutFile;
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
import spl.util.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class SplInterpreter {

    public static final Map<Class<? extends SplObject>, String> NATIVE_TYPE_NAMES =
            new MapMerger<>(
                    Map.of(
                            SplInvokes.class, "Invoke",
                            NativeFunction.class, "NativeFunction",
                            TypeFunction.class, "TypeFunction",
                            SplArray.class, "Array",
                            SplModule.class, "Module",
                            SplMethod.class, "Method",
                            Function.class, "Function",
                            LambdaExpression.class, "LambdaExpression",
                            SplClass.class, "Class",
                            NativeType.class, "NativeType"
                    ),
                    Map.of(
                            NativeInFile.class, "NativeInFile",
                            NativeOutFile.class, "NativeOutFile"
                    )
            ).merge();
    private static InputStream in = System.in;
    private static PrintStream out = System.out;
    private static PrintStream err = System.err;
    private GlobalEnvironment globalEnvironment;

    private long vmStartBegin, parseBegin, cacheBegin;

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
        initNativeTypeCheckers(globalEnvironment);

        SplInvokes system = new SplInvokes(out, in, err);

        Memory memory = globalEnvironment.getMemory();
        Reference sysPtr = memory.allocateObject(system, globalEnvironment);

        globalEnvironment.defineConstAndSet(
                Constants.INVOKES,
                sysPtr,
                LineFilePos.LF_INTERPRETER);
    }

    static void importModules(GlobalEnvironment ge, LinkedHashMap<String, ParseResult> parsedModules) {
        for (Map.Entry<String, ParseResult> entry : parsedModules.entrySet()) {
            ModuleEnvironment moduleScope = new ModuleEnvironment(entry.getKey(), ge);
            entry.getValue().getRoot().evaluate(moduleScope);
            SplModule module = new SplModule(entry.getKey(), moduleScope);

            Reference ptr = ge.getMemory().allocateObject(module, moduleScope);

            ge.addImportedModulePtr(entry.getKey(), ptr);
        }
    }

    public static LinkedHashMap<String, ParseResult> parseImportedModules(
            LinkedHashMap<String, CollectiveElement> imported,
            Map<String, StringLiteral> strLitBundleMap)
            throws IOException {
        LinkedHashMap<String, ParseResult> result = new LinkedHashMap<>();
        for (Map.Entry<String, CollectiveElement> entry : imported.entrySet()) {
            Parser psr = new Parser(new TextProcessResult(entry.getValue()), strLitBundleMap);
            ParseResult ce = psr.parse();

            result.put(entry.getKey(), ce);
        }
        return result;
    }

    private static void initNativeTypeCheckers(GlobalEnvironment ge) {
        Memory memory = ge.getMemory();
        for (Map.Entry<Class<? extends SplObject>, String> entry : NATIVE_TYPE_NAMES.entrySet()) {
            final String name = entry.getValue();
            final String checkerName = name + "?";
            final Class<? extends SplObject> clazz = entry.getKey();
            NativeType nt = new NativeType(name);
            Reference ntPtr = memory.allocateObject(nt, ge);
            ge.defineConstAndSet(
                    NativeType.shownName(name),
                    ntPtr,
                    LineFilePos.LF_INTERPRETER
            );
            NativeFunction checker = new NativeFunction(checkerName, 1) {
                @Override
                protected Bool callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                    SplElement arg = evaluatedArgs.positionalArgs.get(0);
                    if (arg instanceof Reference) {
                        SplObject object = callingEnv.getMemory().get((Reference) arg);
                        return Bool.boolValueOf(clazz.isInstance(object));
                    }
                    return Bool.FALSE;
                }
            };
            Reference checkerPtr = memory.allocateFunction(checker, ge);
            ge.defineConstAndSet(
                    checkerName,
                    checkerPtr,
                    LineFilePos.LF_INTERPRETER
            );
        }
    }

    private static void initNativeFunctions(GlobalEnvironment ge) {
        TypeFunction toInt = new TypeFunction("int", 1) {
            @Override
            protected SplElement callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                SplElement arg = evaluatedArgs.positionalArgs.get(0);
                if (arg.getClass() == Int.class) return arg;
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

        TypeFunction toFloat = new TypeFunction("float", 1) {
            @Override
            protected SplElement callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                SplElement arg = evaluatedArgs.positionalArgs.get(0);
                if (arg.getClass() == SplFloat.class) return arg;
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

        TypeFunction toChar = new TypeFunction("char", 1) {
            @Override
            protected SplElement callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                SplElement arg = evaluatedArgs.positionalArgs.get(0);
                if (arg.getClass() == Char.class) return arg;
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

        TypeFunction toByte = new TypeFunction("byte", 1) {
            @Override
            protected SplElement callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                SplElement arg = evaluatedArgs.positionalArgs.get(0);
                if (arg.getClass() == SplByte.class) return arg;
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

        TypeFunction toBool = new TypeFunction("boolean", 1) {
            @Override
            protected Bool callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                SplElement arg = evaluatedArgs.positionalArgs.get(0);
                if (arg.getClass() == Bool.class) return (Bool) arg;
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

        TypeFunction abstractObject = new TypeFunction("Obj", 1) {
            @Override
            protected SplElement callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                return Utilities.wrap(evaluatedArgs.positionalArgs.get(0), callingEnv, LineFilePos.LF_INTERPRETER);
            }
        };

        NativeFunction isAbstractObject = new NativeFunction("Obj?", 1) {
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

        NativeFunction isAny = new NativeFunction("any?", 1) {
            @Override
            protected Bool callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                return Bool.TRUE;
            }
        };

        NativeFunction[] nativeFunctions = new NativeFunction[]{
                toInt, isInt,
                toFloat, isFloat,
                toChar, isChar,
                toBool, isBool,
                toByte, isByte,
                abstractObject, isAbstractObject,
                isCallable,
                isNull,
                isAny
        };

        for (NativeFunction nf : nativeFunctions) {
            allocateAndDefineNatFunc(nf, ge);
        }
    }

    private static void allocateAndDefineNatFunc(NativeFunction nativeFunction, Environment ge) {
        Reference ptr = ge.getMemory().allocateFunction(nativeFunction, ge);
        ge.defineFunction(nativeFunction.getName(), ptr, LineFilePos.LF_INTERPRETER);
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

    private ParseResult parseSrcFile(ArgumentParser argumentParser) throws Exception {
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

        LinkedHashMap<String, ParseResult> parsedModules = parseImportedModules(
                processed.importedPaths,
                parser.getStringLiterals()
        );

        cacheBegin = System.currentTimeMillis();
        if (argumentParser.isSaveCache()) {
            new SplCacheSaver(
                    argumentParser.getMainSrcFile().getAbsolutePath(),
                    parseResult,
                    parsedModules
            ).save();
        }

        initMemoryNoImport(argumentParser);
        importModules(globalEnvironment, parsedModules);

        return parseResult;
    }

    private ParseResult readCacheFile(ArgumentParser argumentParser) throws Exception {
        CacheReconstructor cr = new CacheReconstructor(argumentParser.getMainSrcFile().getAbsolutePath());
        ParseResult root = cr.reconstruct();

        cacheBegin = System.currentTimeMillis();
        initMemoryNoImport(argumentParser);
        importModules(globalEnvironment, cr.getParsedModules());

        return root;
    }

    private void initMemoryNoImport(ArgumentParser argumentParser) {
        vmStartBegin = System.currentTimeMillis();
        Memory memory = new Memory();
        if (argumentParser.isGcInfo()) memory.debugs.setPrintGcRes(true);
        if (argumentParser.isGcTrigger()) memory.debugs.setPrintGcTrigger(true);
        memory.setCheckContract(argumentParser.isCheckContract());
        if (globalEnvironment == null) {
            globalEnvironment = new GlobalEnvironment(memory);
            initNatives(globalEnvironment);
        }
    }

    public void run(String[] args) throws Exception {
        ArgumentParser argumentParser = new ArgumentParser(args);
        if (argumentParser.isAllValid()) {
            parseBegin = System.currentTimeMillis();
            String srcName = argumentParser.getMainSrcFile().getName();

            ParseResult parseResult;
            if (srcName.endsWith(".sp")) parseResult = parseSrcFile(argumentParser);
            else if (srcName.endsWith(".spc")) parseResult = readCacheFile(argumentParser);
            else throw new IllegalArgumentException("Not a spl source file or a compiled spl file");

            if (argumentParser.isPrintAst()) {
                out.println("===== Ast =====");
                out.println(parseResult.getRoot());
                out.println("===== End of spl.ast =====");
            }

            long runBegin = System.currentTimeMillis();

            try {
                if (evaluateGlobal(parseResult)) {
                    callMain(argumentParser.getSplArgs());
                }
            } catch (ClassCastException cce) {
                cce.printStackTrace();
                Utilities.removeErrorAndPrint(globalEnvironment, Main.LF_MAIN);
                throw new NativeTypeError();
            }

            long processEnd = System.currentTimeMillis();

            if (argumentParser.isPrintMem()) {
                globalEnvironment.getMemory().printMemory();
            }
            if (argumentParser.isTimer()) {
                out.printf(
                        "Parse time: %d ms, cache time: %d ms, VM startup time: %d ms, running time: %d ms.%n",
                        cacheBegin - parseBegin,
                        vmStartBegin - cacheBegin,
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

    /**
     * Returns {@code true} iff the code without main function runs successfully without any spl error.
     *
     * @param parseResult abstract syntax tree
     * @return {@code true} iff the code without main function runs successfully without any spl error.
     */
    boolean evaluateGlobal(ParseResult parseResult) {
        parseResult.getRoot().evaluate(globalEnvironment);
        if (globalEnvironment.hasException()) {
            Utilities.removeErrorAndPrint(globalEnvironment, Main.LF_MAIN);
            return false;
        }
        return true;
    }

    void callMain(String[] args) {
        if (globalEnvironment.hasName(Constants.MAIN_FN)) {
            Reference mainPtr = (Reference) globalEnvironment.get(Constants.MAIN_FN, Main.LF_MAIN);

            Function mainFunc = globalEnvironment.getMemory().get(mainPtr);
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
