import ast.*;
import interpreter.EvaluatedArguments;
import interpreter.Memory;
import interpreter.SplException;
import interpreter.env.Environment;
import interpreter.env.GlobalEnvironment;
import interpreter.invokes.SplInvokes;
import interpreter.primitives.*;
import interpreter.splObjects.*;
import interpreter.types.TypeError;
import lexer.TokenList;
import lexer.FileTokenizer;
import lexer.TokenizeResult;
import lexer.treeList.BraceList;
import parser.Parser;
import util.ArgumentParser;
import util.Constants;
import util.LineFile;

import java.util.List;

public class Main {

    private static final LineFile LF_MAIN = new LineFile("Main");

    public static void main(String[] args) throws Exception {
        ArgumentParser argumentParser = new ArgumentParser(args);
        if (argumentParser.isAllValid()) {
            long parseBegin = System.currentTimeMillis();
            FileTokenizer tokenizer = new FileTokenizer(
                    argumentParser.getMainSrcFile(),
                    true,
                    argumentParser.importLang()
            );
            BraceList rootToken = tokenizer.tokenize();
//            TokenList tokenList = tokenizer.tokenize();
            if (argumentParser.isPrintTokens()) {
                System.out.println(rootToken);
            }
            Parser parser = new Parser(new TokenizeResult(rootToken), argumentParser.importLang());
            BlockStmt root = parser.parse();
            if (argumentParser.isPrintAst()) {
                System.out.println("===== Ast =====");
                System.out.println(root);
                System.out.println("===== End of ast =====");
            }
            long vmStartBegin = System.currentTimeMillis();
//            FakeGlobalEnv environment = new FakeGlobalEnv();
//            root.preprocess(environment);
            Memory memory = new Memory();
            GlobalEnvironment globalEnvironment = new GlobalEnvironment(memory);

            if (argumentParser.isGcInfo()) memory.debugs.setPrintGcRes(true);
            if (argumentParser.isGcTrigger()) memory.debugs.setPrintGcTrigger(true);

            initNatives(globalEnvironment);

//            System.out.println("Java");
//            new C().printThis();

            long runBegin = System.currentTimeMillis();
            root.evaluate(globalEnvironment);

            callMain(argumentParser.getSplArgs(), globalEnvironment);

//            globalEnvironment.printVars();

            long processEnd = System.currentTimeMillis();

            if (argumentParser.isPrintMem()) {
                memory.printMemory();
            }
            if (argumentParser.isTimer()) {
                System.out.println(String.format(
                        "Parse time: %d ms, VM startup time: %d ms, running time: %d ms.",
                        vmStartBegin - parseBegin,
                        runBegin - vmStartBegin,
                        processEnd - runBegin
                ));
            }
        } else {
            System.out.println(argumentParser.getMsg());
        }
    }

    private static void initNatives(GlobalEnvironment globalEnvironment) {
        initNativeFunctions(globalEnvironment);

        SplInvokes system = new SplInvokes();

        Memory memory = globalEnvironment.getMemory();
        Pointer sysPtr = memory.allocateObject(system, globalEnvironment);

        globalEnvironment.defineConstAndSet(
                "Invokes",
                sysPtr,
                LineFile.LF_INTERPRETER);
    }

    private static void initNativeFunctions(GlobalEnvironment ge) {
        NativeFunction toInt = new NativeFunction("int", 1) {
            @Override
            protected SplElement callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                SplElement arg = evaluatedArgs.positionalArgs.get(0);
                if (SplElement.isPrimitive(arg)) {
                    return new Int(arg.intValue());
                } else {
                    throw new TypeError("Cannot convert pointer type to int. ");
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
                if (SplElement.isPrimitive(arg)) {
                    return new SplFloat(arg.floatValue());
                } else {
                    throw new TypeError("Cannot convert pointer type to float. ");
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

        NativeFunction isChar = new NativeFunction("char?", 1) {
            @Override
            protected Bool callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                SplElement arg = evaluatedArgs.positionalArgs.get(0);
                return Bool.boolValueOf(arg instanceof Char);
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
                if (arg instanceof Pointer) {
                    SplObject object = callingEnv.getMemory().get((Pointer) arg);
                    return Bool.boolValueOf(object != null);
                }
                return Bool.FALSE;
            }
        };

        NativeFunction isCallable = new NativeFunction("Callable?", 1) {
            @Override
            protected Bool callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                SplElement arg = evaluatedArgs.positionalArgs.get(0);
                if (arg instanceof Pointer) {
                    SplObject object = callingEnv.getMemory().get((Pointer) arg);
                    return Bool.boolValueOf(object instanceof SplCallable);
                }
                return Bool.FALSE;
            }
        };

        Memory memory = ge.getMemory();
        Pointer ptrInt = memory.allocateFunction(toInt, ge);
        Pointer ptrIsInt = memory.allocateFunction(isInt, ge);
        Pointer ptrFloat = memory.allocateFunction(toFloat, ge);
        Pointer ptrIsFloat = memory.allocateFunction(isFloat, ge);
        Pointer ptrIsChar = memory.allocateFunction(isChar, ge);
        Pointer ptrIsBool = memory.allocateFunction(isBool, ge);
        Pointer ptrIsAbsObj = memory.allocateFunction(isAbstractObject, ge);
        Pointer ptrIsCallable = memory.allocateFunction(isCallable, ge);

        ge.defineFunction("int", ptrInt, LineFile.LF_INTERPRETER);
        ge.defineFunction("int?", ptrIsInt, LineFile.LF_INTERPRETER);
        ge.defineFunction("float", ptrFloat, LineFile.LF_INTERPRETER);
        ge.defineFunction("float?", ptrIsFloat, LineFile.LF_INTERPRETER);
        ge.defineFunction("char?", ptrIsChar, LineFile.LF_INTERPRETER);
        ge.defineFunction("boolean?", ptrIsBool, LineFile.LF_INTERPRETER);
        ge.defineFunction("AbstractObject?", ptrIsAbsObj, LineFile.LF_INTERPRETER);
        ge.defineFunction("Callable?", ptrIsCallable, LineFile.LF_INTERPRETER);
    }

    private static void callMain(String[] args, GlobalEnvironment globalEnvironment) {
        if (globalEnvironment.hasName("main", LF_MAIN)) {
            Pointer mainPtr = (Pointer) globalEnvironment.get("main", LF_MAIN);
            EvaluatedArguments splArg =
                    args == null ? new EvaluatedArguments() : makeSplArgArray(args, globalEnvironment);

//            if (!(mainTv.getType() instanceof CallableType)) {
//                throw new TypeError("Main function must be callable. ");
//            }

            Function mainFunc = (Function) globalEnvironment.getMemory().get(mainPtr);
            if (mainFunc.minArgCount() > 1) {
                throw new SplException("Function main takes 0 or 1 arguments.");
            }
            SplElement rtn = mainFunc.call(splArg, globalEnvironment, LF_MAIN);

            if (globalEnvironment.hasException()) {
                Pointer errPtr = globalEnvironment.getExceptionPtr();
                globalEnvironment.removeException();

                Instance errIns = (Instance) globalEnvironment.getMemory().get(errPtr);
//                String errStr = SplInvokes.pointerToSting(
//                        errPtr,
//                        globalEnvironment,
//                        LineFile.LF_INTERPRETER);

                Pointer stackTraceFtnPtr = (Pointer) errIns.getEnv().get("printStackTrace", LF_MAIN);
                Function stackTraceFtn = (Function) globalEnvironment.getMemory().get(stackTraceFtnPtr);
                stackTraceFtn.call(EvaluatedArguments.of(), globalEnvironment, LF_MAIN);
            } else {
                System.out.println("Process finished with exit value " + rtn);
            }
        }
    }

    private static EvaluatedArguments makeSplArgArray(String[] args, GlobalEnvironment globalEnvironment) {
//        SplElement stringTv = globalEnvironment.get(Constants.STRING_CLASS, LF_MAIN);
        Pointer argPtr = SplArray.createArray(SplElement.POINTER, args.length, globalEnvironment);
        for (int i = 0; i < args.length; ++i) {

            // create String instance
            Pointer strIns = StringLiteral.createString(
                    args[i].toCharArray(), globalEnvironment, LineFile.LF_INTERPRETER
            );
            SplArray.setItemAtIndex(argPtr, i, strIns, globalEnvironment, LineFile.LF_INTERPRETER);
        }
        return EvaluatedArguments.of(argPtr);
    }
}

