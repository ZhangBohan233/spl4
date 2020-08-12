import ast.BlockStmt;
import ast.StringLiteral;
import interpreter.EvaluatedArguments;
import interpreter.Memory;
import interpreter.env.Environment;
import interpreter.env.GlobalEnvironment;
import interpreter.env.ModuleEnvironment;
import interpreter.invokes.SplInvokes;
import interpreter.primitives.*;
import interpreter.splErrors.NativeError;
import interpreter.splObjects.*;
import lexer.TextProcessResult;
import lexer.treeList.CollectiveElement;
import parser.Parser;
import util.ArgumentParser;
import util.Constants;
import util.LineFile;
import util.Utilities;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class SplInterpreter {

    static void initNatives(GlobalEnvironment globalEnvironment) {
        initNativeFunctions(globalEnvironment);

        SplInvokes system = new SplInvokes();

        Memory memory = globalEnvironment.getMemory();
        Pointer sysPtr = memory.allocateObject(system, globalEnvironment);

        globalEnvironment.defineConstAndSet(
                "Invokes",
                sysPtr,
                LineFile.LF_INTERPRETER);
    }

    static void importModules(GlobalEnvironment ge, Map<String, CollectiveElement> imported) throws IOException {
        for (Map.Entry<String, CollectiveElement> entry : imported.entrySet()) {
            Parser psr = new Parser(new TextProcessResult(entry.getValue()));
            BlockStmt ce = psr.parse();

            ModuleEnvironment moduleScope = new ModuleEnvironment(ge);
            ce.evaluate(moduleScope);
            SplModule module = new SplModule(entry.getKey(), moduleScope);

            Pointer ptr = ge.getMemory().allocateObject(module, moduleScope);

            ge.addImportedModulePtr(entry.getKey(), ptr);
        }
    }

    private static void initNativeFunctions(GlobalEnvironment ge) {
        NativeFunction toInt = new NativeFunction("int", 1) {
            @Override
            protected SplElement callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                SplElement arg = evaluatedArgs.positionalArgs.get(0);
                if (arg instanceof Pointer) {
                    return new Int(
                            Utilities.wrapperToPrimitive(
                                    (Pointer) arg,
                                    callingEnv,
                                    LineFile.LF_INTERPRETER).intValue());
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
                if (arg instanceof Pointer) {
                    return new SplFloat(
                            Utilities.wrapperToPrimitive(
                                    (Pointer) arg,
                                    callingEnv,
                                    LineFile.LF_INTERPRETER).floatValue());
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
                if (arg instanceof Pointer) {
                    return new Char(
                            (char) Utilities.wrapperToPrimitive(
                                    (Pointer) arg,
                                    callingEnv,
                                    LineFile.LF_INTERPRETER).intValue());
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

        NativeFunction isArray = new NativeFunction("Array?", 1) {
            @Override
            protected Bool callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                SplElement arg = evaluatedArgs.positionalArgs.get(0);
                if (arg instanceof Pointer) {
                    SplObject object = callingEnv.getMemory().get((Pointer) arg);
                    return Bool.boolValueOf(object instanceof SplArray);
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
        Pointer ptrChar = memory.allocateFunction(toChar, ge);
        Pointer ptrIsFloat = memory.allocateFunction(isFloat, ge);
        Pointer ptrIsChar = memory.allocateFunction(isChar, ge);
        Pointer ptrIsBool = memory.allocateFunction(isBool, ge);
        Pointer ptrIsAbsObj = memory.allocateFunction(isAbstractObject, ge);
        Pointer ptrIsArray = memory.allocateFunction(isArray, ge);
        Pointer ptrIsCallable = memory.allocateFunction(isCallable, ge);

        ge.defineFunction("int", ptrInt, LineFile.LF_INTERPRETER);
        ge.defineFunction("int?", ptrIsInt, LineFile.LF_INTERPRETER);
        ge.defineFunction("float", ptrFloat, LineFile.LF_INTERPRETER);
        ge.defineFunction("float?", ptrIsFloat, LineFile.LF_INTERPRETER);
        ge.defineFunction("char", ptrChar, LineFile.LF_INTERPRETER);
        ge.defineFunction("char?", ptrIsChar, LineFile.LF_INTERPRETER);
        ge.defineFunction("boolean?", ptrIsBool, LineFile.LF_INTERPRETER);
        ge.defineFunction("AbstractObject?", ptrIsAbsObj, LineFile.LF_INTERPRETER);
        ge.defineFunction("Array?", ptrIsArray, LineFile.LF_INTERPRETER);
        ge.defineFunction("Callable?", ptrIsCallable, LineFile.LF_INTERPRETER);
    }

    static void callMain(String[] args, GlobalEnvironment globalEnvironment) {
        if (globalEnvironment.hasName(Constants.MAIN_FN)) {
            Pointer mainPtr = (Pointer) globalEnvironment.get(Constants.MAIN_FN, Main.LF_MAIN);

            Function mainFunc = (Function) globalEnvironment.getMemory().get(mainPtr);
            if (mainFunc.minArgCount() > 1) {
                throw new NativeError("Function main takes 0 or 1 arguments.");
            }
            EvaluatedArguments splArg =
                    mainFunc.minArgCount() == 0 ? new EvaluatedArguments() : makeSplArgArray(args, globalEnvironment);

            SplElement rtn = mainFunc.call(splArg, globalEnvironment, Main.LF_MAIN);

            if (globalEnvironment.hasException()) {
                Pointer errPtr = globalEnvironment.getExceptionPtr();
                globalEnvironment.removeException();

                Instance errIns = (Instance) globalEnvironment.getMemory().get(errPtr);

                Pointer stackTraceFtnPtr = (Pointer) errIns.getEnv().get("printStackTrace", Main.LF_MAIN);
                Function stackTraceFtn = (Function) globalEnvironment.getMemory().get(stackTraceFtnPtr);
                stackTraceFtn.call(EvaluatedArguments.of(errPtr), globalEnvironment, Main.LF_MAIN);
            } else {
                System.out.println("Process finished with exit value " + rtn);
            }
        }
    }

    private static EvaluatedArguments makeSplArgArray(String[] args, GlobalEnvironment globalEnvironment) {
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
