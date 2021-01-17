package spl.util;

import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.primitives.*;
import spl.interpreter.splObjects.NativeFunction;
import spl.interpreter.splObjects.SplObject;
import spl.interpreter.splObjects.TypeFunction;

public class NativeFunctions {
    public static TypeFunction toInt = new TypeFunction("int", 1) {
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

    public static NativeFunction isInt = new NativeFunction("int?", 1) {
        @Override
        protected Bool callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
            SplElement arg = evaluatedArgs.positionalArgs.get(0);
            return Bool.boolValueOf(arg instanceof Int);
        }
    };

    public static TypeFunction toFloat = new TypeFunction("float", 1) {
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

    public static NativeFunction isFloat = new NativeFunction("float?", 1) {
        @Override
        protected Bool callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
            SplElement arg = evaluatedArgs.positionalArgs.get(0);
            return Bool.boolValueOf(arg instanceof SplFloat);
        }
    };

    public static TypeFunction toChar = new TypeFunction("char", 1) {
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

    public static NativeFunction isChar = new NativeFunction("char?", 1) {
        @Override
        protected Bool callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
            SplElement arg = evaluatedArgs.positionalArgs.get(0);
            return Bool.boolValueOf(arg instanceof Char);
        }
    };

    public static TypeFunction toByte = new TypeFunction("byte", 1) {
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

    public static NativeFunction isByte = new NativeFunction("byte?", 1) {
        @Override
        protected Bool callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
            SplElement arg = evaluatedArgs.positionalArgs.get(0);
            return Bool.boolValueOf(arg instanceof SplByte);
        }
    };

    public static TypeFunction toBool = new TypeFunction("boolean", 1) {
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

    public static NativeFunction isBool = new NativeFunction("boolean?", 1) {
        @Override
        protected Bool callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
            SplElement arg = evaluatedArgs.positionalArgs.get(0);
            return Bool.boolValueOf(arg instanceof Bool);
        }
    };

    public static NativeFunction isAbstractObject = new NativeFunction("AbstractObject?", 1) {
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

    public NativeFunctions() {

    }
}
