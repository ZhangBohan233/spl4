package spl.ast;

import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.Bool;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.splObjects.Instance;
import spl.interpreter.splObjects.SplArray;
import spl.interpreter.splObjects.SplMethod;
import spl.interpreter.splObjects.SplObject;
import spl.lexer.SyntaxError;
import spl.util.*;

import java.io.IOException;

public class Arguments extends NonEvaluate {

    private final Line line;

    public Arguments(Line line, LineFilePos lineFile) {
        super(lineFile);

        this.line = line;
    }

    private static void addArrayToArgs(Reference arrayPtr, EvaluatedArguments evaluatedArguments,
                                       Environment env, LineFilePos lineFilePos) {
        int arrAddr = arrayPtr.getPtr();
        SplArray array = (SplArray) env.getMemory().get(arrayPtr);
        for (int j = 0; j < array.length; j++) {
            evaluatedArguments.positionalArgs.add(
                    Utilities.unwrap(env.getMemory().getPrimitive(arrAddr + j + 1), env, lineFilePos)
            );
        }
    }

    private static boolean addDictToArgs(Reference dictRef,
                                         Instance dictIns,
                                         EvaluatedArguments evaluatedArguments,
                                         Environment env,
                                         LineFilePos lineFilePos) {
        try {
            Reference iterFnRef = (Reference) dictIns.getEnv().get(Constants.ITER_FN, lineFilePos);
            SplMethod iterFn = (SplMethod) env.getMemory().get(iterFnRef);

            Reference iteratorRef = (Reference) iterFn.call(EvaluatedArguments.of(dictRef), env, lineFilePos);
            Instance iterator = (Instance) env.getMemory().get(iteratorRef);

            Reference nextPtr = (Reference) iterator.getEnv().get(Constants.NEXT_FN, lineFilePos);
            Reference hasNextPtr = (Reference) iterator.getEnv().get(Constants.HAS_NEXT_FN, lineFilePos);
            SplMethod nextFn = (SplMethod) env.getMemory().get(nextPtr);
            SplMethod hasNextFn = (SplMethod) env.getMemory().get(hasNextPtr);

            Reference getPtr = (Reference) dictIns.getEnv().get(Constants.GET_ITEM_FN, lineFilePos);
            SplMethod getFn = (SplMethod) env.getMemory().get(getPtr);

            EvaluatedArguments ea = EvaluatedArguments.of(iteratorRef);
            Bool hasNext = (Bool) hasNextFn.call(ea, env, lineFilePos);
            while (hasNext.value) {
                Reference nextKey = (Reference) nextFn.call(ea, env, lineFilePos);
                Instance keyStr = (Instance) env.getMemory().get(nextKey);
                String key = SplInvokes.splStringToJavaString(keyStr, env, lineFilePos);
                SplElement value = getFn.call(EvaluatedArguments.of(dictRef, nextKey), env, lineFilePos);
                evaluatedArguments.keywordArgs.put(key, Utilities.unwrap(value, env, lineFilePos));

                hasNext = (Bool) hasNextFn.call(EvaluatedArguments.of(iteratorRef), env, lineFilePos);
            }

            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public static Arguments reconstruct(BytesIn is, LineFilePos lineFilePos) throws Exception {
        Line line = Reconstructor.reconstruct(is);
        return new Arguments(line, lineFilePos);
    }

    public Line getLine() {
        return line;
    }

    public EvaluatedArguments evalArgs(Environment callingEnv) {
        EvaluatedArguments evaluatedArguments = new EvaluatedArguments();

        boolean kwargBegins = false;

        int argc = getLine().size();
        for (int i = 0; i < argc; ++i) {
            Node argNode = getLine().get(i);

            if (argNode instanceof Assignment) {
                NameNode leftNode = (NameNode) ((Assignment) argNode).getLeft();
                evaluatedArguments.keywordArgs.put(
                        leftNode.getName(), ((Assignment) argNode).getRight().evaluate(callingEnv));
                kwargBegins = true;
            } else {
                if (kwargBegins)
                    throw new SyntaxError("Positional arguments follows keyword arguments. ",
                            argNode.getLineFile());
                if (argNode instanceof StarExpr) {
                    StarExpr starExpr = (StarExpr) argNode;
                    if (starExpr.value instanceof StarExpr) {
                        // unpack dict
                        SplElement rawArg = ((StarExpr) starExpr.value).value.evaluate(callingEnv);
                        if (!(rawArg instanceof Reference)) {
                            SplInvokes.throwException(
                                    callingEnv,
                                    Constants.TYPE_ERROR,
                                    "Only classes extends 'Dict' supports **kwargs operation.",
                                    lineFile
                            );
                            break;
                        }
                        Reference arg = (Reference) rawArg;
                        SplObject obj = callingEnv.getMemory().get(arg);
                        if (obj instanceof Instance &&
                                Utilities.isInstancePtr(arg, Constants.DICT_CLASS, callingEnv, lineFile)) {
                            if (!addDictToArgs(arg, (Instance) obj, evaluatedArguments, callingEnv, lineFile)) {
                                SplInvokes.throwException(
                                        callingEnv,
                                        Constants.TYPE_ERROR,
                                        "Error occurs when dealing **kwargs:",
                                        lineFile
                                );
                                break;
                            }
                        } else {
                            SplInvokes.throwException(
                                    callingEnv,
                                    Constants.TYPE_ERROR,
                                    "Only classes extends 'Dict' supports **kwargs operation.",
                                    lineFile
                            );
                            break;
                        }
                    } else {
                        // unpack list
                        SplElement rawArg = starExpr.value.evaluate(callingEnv);
                        if (!(rawArg instanceof Reference)) {
                            SplInvokes.throwException(
                                    callingEnv,
                                    Constants.TYPE_ERROR,
                                    "Only arrays and classes extends 'List' supports *args operation.",
                                    lineFile
                            );
                            break;
                        }
                        Reference arg = (Reference) rawArg;
                        SplObject obj = callingEnv.getMemory().get(arg);
                        if (obj instanceof SplArray) {
                            addArrayToArgs(arg, evaluatedArguments, callingEnv, lineFile);
                        } else if (obj instanceof Instance &&
                                Utilities.isInstancePtr(arg, Constants.LIST_CLASS, callingEnv, lineFile)) {
                            Reference toArrayPtr = (Reference) ((Instance) obj).getEnv().get("toArray", lineFile);
                            SplMethod toArrayFtn = (SplMethod) callingEnv.getMemory().get(toArrayPtr);
                            Reference arrPtr = (Reference) toArrayFtn.call(EvaluatedArguments.of(arg), callingEnv, lineFile);
                            addArrayToArgs(arrPtr, evaluatedArguments, callingEnv, lineFile);
                        } else {
                            SplInvokes.throwException(
                                    callingEnv,
                                    Constants.TYPE_ERROR,
                                    "Only arrays and classes extends 'List' supports *args operation.",
                                    lineFile
                            );
                            break;
                        }
                    }
                } else {
                    evaluatedArguments.positionalArgs.add(argNode.evaluate(callingEnv));
                }
            }
        }
        return evaluatedArguments;
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        line.save(out);
    }

    @Override
    public String toString() {
        return "Arg" + line;
    }
}
