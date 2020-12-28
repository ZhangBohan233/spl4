package spl.ast;

import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Pointer;
import spl.interpreter.splErrors.NativeTypeError;
import spl.interpreter.splObjects.*;
import spl.lexer.SyntaxError;
import spl.util.Constants;
import spl.util.LineFile;
import spl.util.Utilities;

public class Arguments extends NonEvaluate {

    private final Line line;

    public Arguments(Line line, LineFile lineFile) {
        super(lineFile);

        this.line = line;
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
                    // unpack list
                    StarExpr starExpr = (StarExpr) argNode;
                    if (starExpr.value instanceof StarExpr) {
                        // unpack dict
                        // TODO
                    } else {
                        Pointer arg = (Pointer) starExpr.value.evaluate(callingEnv);
                        SplObject obj = callingEnv.getMemory().get(arg);
                        if (obj instanceof SplArray) {
                            addArrayToArgs(arg, evaluatedArguments, callingEnv);
                        } else if (obj instanceof Instance &&
                                Utilities.isInstancePtr(arg, Constants.LIST_CLASS, callingEnv, lineFile)) {
                            Pointer toArrayPtr = (Pointer) ((Instance) obj).getEnv().get("toArray", lineFile);
                            SplMethod toArrayFtn = (SplMethod) callingEnv.getMemory().get(toArrayPtr);
                            Pointer arrPtr = (Pointer) toArrayFtn.call(EvaluatedArguments.of(arg), callingEnv, lineFile);
                            addArrayToArgs(arrPtr, evaluatedArguments, callingEnv);
                        } else {
                            throw new NativeTypeError();
                        }
                    }
                } else {
                    evaluatedArguments.positionalArgs.add(argNode.evaluate(callingEnv));
                }
            }
        }
        return evaluatedArguments;
    }

    private static void addArrayToArgs(Pointer arrayPtr, EvaluatedArguments evaluatedArguments,
                                       Environment env) {
        int arrAddr = arrayPtr.getPtr();
        SplArray array = (SplArray) env.getMemory().get(arrayPtr);
        for (int j = 0; j < array.length; j++) {
            evaluatedArguments.positionalArgs.add(
                    env.getMemory().getPrimitive(arrAddr + j + 1)
            );
        }
    }

    @Override
    public String toString() {
        return "Arg" + line;
    }
}
