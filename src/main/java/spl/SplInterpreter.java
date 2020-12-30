package spl;

import spl.ast.BlockStmt;
import spl.ast.StringLiteral;
import spl.interpreter.EvaluatedArguments;
import spl.interpreter.Memory;
import spl.interpreter.env.Environment;
import spl.interpreter.env.GlobalEnvironment;
import spl.interpreter.env.ModuleEnvironment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.*;
import spl.interpreter.splErrors.NativeError;
import spl.interpreter.splObjects.*;
import spl.lexer.TextProcessResult;
import spl.lexer.treeList.CollectiveElement;
import spl.parser.Parser;
import spl.util.Constants;
import spl.util.LineFile;
import spl.util.Utilities;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SplInterpreter {

    static void initNatives(GlobalEnvironment globalEnvironment) {
        initNativeFunctions(globalEnvironment);

        SplInvokes system = new SplInvokes();

        Memory memory = globalEnvironment.getMemory();
        Reference sysPtr = memory.allocateObject(system, globalEnvironment);

        globalEnvironment.defineConstAndSet(
                Constants.INVOKES,
                sysPtr,
                LineFile.LF_INTERPRETER);
    }

    static void importModules(GlobalEnvironment ge, Map<String, CollectiveElement> imported) throws IOException {
        Map<String, BlockStmt> blocks = parseImportedModules(imported);
        for (Map.Entry<String, BlockStmt> entry : blocks.entrySet()) {
            ModuleEnvironment moduleScope = new ModuleEnvironment(ge);
            entry.getValue().evaluate(moduleScope);
            SplModule module = new SplModule(entry.getKey(), moduleScope);

            Reference ptr = ge.getMemory().allocateObject(module, moduleScope);

            ge.addImportedModulePtr(entry.getKey(), ptr);
        }
    }

    public static Map<String, BlockStmt> parseImportedModules(Map<String, CollectiveElement> imported) throws IOException {
        Map<String, BlockStmt> result = new HashMap<>();
        for (Map.Entry<String, CollectiveElement> entry : imported.entrySet()) {
            Parser psr = new Parser(new TextProcessResult(entry.getValue()));
            BlockStmt ce = psr.parse();

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
                if (arg instanceof Reference) {
                    return new SplFloat(
                            Utilities.wrapperToPrimitive(
                                    (Reference) arg,
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
                if (arg instanceof Reference) {
                    return new Char(
                            (char) Utilities.wrapperToPrimitive(
                                    (Reference) arg,
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

        NativeFunction toBool = new NativeFunction("boolean", 1) {
            @Override
            protected Bool callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                SplElement arg = evaluatedArgs.positionalArgs.get(0);
                if (arg instanceof Reference) {
                    return Bool.boolValueOf(
                            Utilities.wrapperToPrimitive(
                                    (Reference) arg,
                                    callingEnv,
                                    LineFile.LF_INTERPRETER).booleanValue());
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

        Memory memory = ge.getMemory();
        Reference ptrInt = memory.allocateFunction(toInt, ge);
        Reference ptrIsInt = memory.allocateFunction(isInt, ge);
        Reference ptrFloat = memory.allocateFunction(toFloat, ge);
        Reference ptrChar = memory.allocateFunction(toChar, ge);
        Reference ptrBool = memory.allocateFunction(toBool, ge);
        Reference ptrIsFloat = memory.allocateFunction(isFloat, ge);
        Reference ptrIsChar = memory.allocateFunction(isChar, ge);
        Reference ptrIsBool = memory.allocateFunction(isBool, ge);
        Reference ptrIsAbsObj = memory.allocateFunction(isAbstractObject, ge);
        Reference ptrIsArray = memory.allocateFunction(isArray, ge);
        Reference ptrIsCallable = memory.allocateFunction(isCallable, ge);
        Reference ptrIsClass = memory.allocateFunction(isClass, ge);

        ge.defineFunction("int", ptrInt, LineFile.LF_INTERPRETER);
        ge.defineFunction("int?", ptrIsInt, LineFile.LF_INTERPRETER);
        ge.defineFunction("float", ptrFloat, LineFile.LF_INTERPRETER);
        ge.defineFunction("float?", ptrIsFloat, LineFile.LF_INTERPRETER);
        ge.defineFunction("char", ptrChar, LineFile.LF_INTERPRETER);
        ge.defineFunction("char?", ptrIsChar, LineFile.LF_INTERPRETER);
        ge.defineFunction("boolean", ptrBool, LineFile.LF_INTERPRETER);
        ge.defineFunction("boolean?", ptrIsBool, LineFile.LF_INTERPRETER);
        ge.defineFunction("AbstractObject?", ptrIsAbsObj, LineFile.LF_INTERPRETER);
        ge.defineFunction("Array?", ptrIsArray, LineFile.LF_INTERPRETER);
        ge.defineFunction("Callable?", ptrIsCallable, LineFile.LF_INTERPRETER);
        ge.defineFunction("Class?", ptrIsClass, LineFile.LF_INTERPRETER);
    }

    static void callMain(String[] args, GlobalEnvironment globalEnvironment) {
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
                Reference errPtr = globalEnvironment.getExceptionPtr();
                globalEnvironment.removeException();

                Instance errIns = (Instance) globalEnvironment.getMemory().get(errPtr);

                Reference stackTraceFtnPtr = (Reference) errIns.getEnv().get("printStackTrace", Main.LF_MAIN);
                Function stackTraceFtn = (Function) globalEnvironment.getMemory().get(stackTraceFtnPtr);
                stackTraceFtn.call(EvaluatedArguments.of(errPtr), globalEnvironment, Main.LF_MAIN);
            } else {
                System.out.println("Process finished with exit value " + rtn);
            }
        }
    }

    private static EvaluatedArguments makeSplArgArray(String[] args, GlobalEnvironment globalEnvironment) {
        Reference argPtr = SplArray.createArray(SplElement.POINTER, args.length, globalEnvironment);
        for (int i = 0; i < args.length; ++i) {

            // create String instance
            Reference strIns = StringLiteral.createString(
                    args[i].toCharArray(), globalEnvironment, LineFile.LF_INTERPRETER
            );
            SplArray.setItemAtIndex(argPtr, i, strIns, globalEnvironment, LineFile.LF_INTERPRETER);
        }
        return EvaluatedArguments.of(argPtr);
    }
}
