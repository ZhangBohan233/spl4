package spl.ast;

import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.*;
import spl.interpreter.splObjects.Instance;
import spl.interpreter.splObjects.SplMethod;
import spl.interpreter.splObjects.SplObject;
import spl.lexer.SyntaxError;
import spl.util.Constants;
import spl.util.LineFilePos;
import spl.util.Utilities;

import java.util.Map;
import java.util.Set;

public class BinaryOperator extends BinaryExpr {

    public static final int ARITHMETIC = 1;
    public static final int LOGICAL = 2;
    public static final int LAZY = 3;
    public static final int BITWISE = 4;

    private static final Map<String, String> ARITHMETIC_OP_MAP = Map.of(
            "+", "__add__",
            "-", "__sub__",
            "*", "__mul__",
            "/", "__div__",
            "%", "__mod__"
    );

    private static final Map<String, String> LOGICAL_OP_MAP = Map.of(
            "==", "__eq__",
            ">", "__gt__",
            "<", "__lt__"
    );

    private static final Set<String> UNCHANGED_LOGICAL = Set.of(
            "is", "is not"
    );

    private final int type;

    public BinaryOperator(String operator, int type, LineFilePos lineFile) {
        super(operator, lineFile);

        this.type = type;
    }

    private static SplElement pointerNumericArithmetic(Reference leftPtr,
                                                       SplElement rightEle,
                                                       String operator,
                                                       Environment env,
                                                       LineFilePos lineFile) {
        SplObject leftObj = env.getMemory().get(leftPtr);
        if (leftObj instanceof Instance) {
            String fnName = ARITHMETIC_OP_MAP.get(operator);
            Environment instanceEnv = ((Instance) leftObj).getEnv();
            Reference fnPtr = (Reference) instanceEnv.get(fnName, lineFile);
            SplMethod opFn = (SplMethod) env.getMemory().get(fnPtr);
            return opFn.call(EvaluatedArguments.of(leftPtr, rightEle), env, lineFile);
        } else {
            return SplInvokes.throwExceptionWithError(
                    env,
                    Constants.TYPE_ERROR,
                    "Binary operator type error.",
                    lineFile);
        }
    }

    private static SplElement primitivePointerArithmetic(SplElement leftEle, Reference rightEle,
                                                         String operator, Environment env,
                                                         LineFilePos lineFile) {
        Reference leftWrpPtr = Utilities.primitiveToWrapper(leftEle, env, lineFile);
        return pointerNumericArithmetic(leftWrpPtr, rightEle, operator, env, lineFile);
    }

    private static SplElement bitwise(String op, Environment env, long l, long r, LineFilePos lineFile) {
        return switch (op) {
            case "<<" -> new Int(l << r);
            case ">>" -> new Int(l >> r);
            case ">>>" -> new Int(l >>> r);
            case "&" -> new Int(l & r);
            case "|" -> new Int(l | r);
            case "^" -> new Int(l ^ r);
            default -> SplInvokes.throwExceptionWithError(
                    env,
                    Constants.TYPE_ERROR,
                    "Unsupported operation '" + op + "'. ",
                    lineFile);
        };
    }

    private static SplElement simpleArithmetic(String op, Environment env, double l, double r, LineFilePos lineFile) {
        return switch (op) {
            case "+" -> new SplFloat(l + r);
            case "-" -> new SplFloat(l - r);
            case "*" -> new SplFloat(l * r);
            case "/" -> new SplFloat(l / r);
            case "%" -> new SplFloat(l % r);
            default -> SplInvokes.throwExceptionWithError(
                    env,
                    Constants.TYPE_ERROR,
                    "Unsupported operation '" + op + "'. ",
                    lineFile);
        };
    }

    private static SplElement pointerLogical(String op, Reference l, Reference r, Environment env, LineFilePos lineFile) {
        if (op.equals("is")) {
            return Bool.boolValueOf(l.getPtr() == r.getPtr());
        } else if (op.equals("is not")) {
            return Bool.boolValueOf(l.getPtr() != r.getPtr());
        } else {
            SplObject leftObj = env.getMemory().get(l);
            if (leftObj instanceof Instance) {
                String fnName = LOGICAL_OP_MAP.get(op);
                Environment instanceEnv = ((Instance) leftObj).getEnv();
                Reference fnPtr = (Reference) instanceEnv.get(fnName, lineFile);
                SplMethod opFn = (SplMethod) env.getMemory().get(fnPtr);
                SplElement res = opFn.call(EvaluatedArguments.of(l, r), env, lineFile);
                if (res instanceof Bool) {
                    return res;
                }
            }
        }
        return SplInvokes.throwExceptionWithError(
                env,
                Constants.TYPE_ERROR,
                "Binary operator type error.",
                lineFile);
    }

    private static SplElement integerLogical(String op, Environment env, long l, long r, LineFilePos lineFile) {
        return switch (op) {
            case "==" -> Bool.boolValueOf(l == r);
            case "!=" -> Bool.boolValueOf(l != r);
            case ">" -> Bool.boolValueOf(l > r);
            case "<" -> Bool.boolValueOf(l < r);
            case ">=" -> Bool.boolValueOf(l >= r);
            case "<=" -> Bool.boolValueOf(l <= r);
            default -> SplInvokes.throwExceptionWithError(
                    env,
                    Constants.TYPE_ERROR,
                    "Unsupported binary operator '" + op + "' between int and int.",
                    lineFile);
        };
    }

    private static SplElement otherLogical(String op, Environment env, double l, double r, LineFilePos lineFile) {
        return switch (op) {
            case "==" -> Bool.boolValueOf(l == r);
            case "!=" -> Bool.boolValueOf(l != r);
            case ">" -> Bool.boolValueOf(l > r);
            case "<" -> Bool.boolValueOf(l < r);
            case ">=" -> Bool.boolValueOf(l >= r);
            case "<=" -> Bool.boolValueOf(l <= r);
            default -> SplInvokes.throwExceptionWithError(
                    env,
                    Constants.TYPE_ERROR,
                    String.format("Unsupported binary operator '%s'.", op),
                    lineFile);
        };
    }

    @Override
    protected SplElement internalEval(Environment env) {
        if (type == ARITHMETIC) {
            SplElement leftEle = left.evaluate(env);
            SplElement rightEle = right.evaluate(env);
            if (env.hasException()) return Undefined.ERROR;

            if (leftEle instanceof Reference) {
                return pointerNumericArithmetic((Reference) leftEle, rightEle, operator, env, lineFile);
            } else {
                if (rightEle instanceof Reference) {
                    return primitivePointerArithmetic(leftEle, (Reference) rightEle, operator, env, lineFile);
                }

                SplElement res = simpleArithmetic(operator, env, leftEle.floatValue(), rightEle.floatValue(), lineFile);

                if (leftEle.isIntLike()) {
                    if (rightEle.isIntLike()) {
                        return new Int(res.intValue());
                    } else if (rightEle instanceof SplFloat) {
                        return res;
                    }
                } else if (leftEle instanceof SplFloat) {
                    return res;
                }
                return SplInvokes.throwExceptionWithError(
                        env,
                        Constants.TYPE_ERROR,
                        "Binary operator type error.",
                        lineFile);
            }
        } else if (type == BITWISE) {
            SplElement leftEle = left.evaluate(env);
            SplElement rightEle = right.evaluate(env);
            if (env.hasException()) return Undefined.ERROR;
            if (leftEle.isIntLike() && rightEle.isIntLike()) {
                return bitwise(operator, env, leftEle.intValue(), rightEle.intValue(), lineFile);
            }
            return SplInvokes.throwExceptionWithError(
                    env,
                    Constants.TYPE_ERROR,
                    "Binary operator type error.",
                    lineFile);
        } else if (type == LOGICAL) {
            SplElement leftTv = left.evaluate(env);
            SplElement rightTv = right.evaluate(env);
            if (env.hasException()) return Undefined.ERROR;
            SplElement result;
            if (SplElement.isPrimitive(leftTv)) {
                if (rightTv instanceof Reference) {
                    if (operator.equals("is")) {
                        return Bool.FALSE;
                    } else if (operator.equals("is not")) {
                        return Bool.TRUE;
                    } else {
                        return pointerLogical(
                                operator,
                                Utilities.primitiveToWrapper(leftTv, env, lineFile),
                                (Reference) rightTv,
                                env,
                                lineFile);
                    }
                }
                if (leftTv.isIntLike()) {
                    long leftV = leftTv.intValue();
                    if (rightTv.isIntLike()) {
                        long rightV = rightTv.intValue();
                        result = integerLogical(operator, env, leftV, rightV, getLineFile());
                    } else if (rightTv instanceof SplFloat) {
                        double rightV = rightTv.floatValue();
                        result = otherLogical(operator, env, leftV, rightV, getLineFile());
                    } else {
                        System.out.println(leftTv + operator + rightTv + lineFile.toStringFileLine());
                        return SplInvokes.throwExceptionWithError(
                                env,
                                Constants.TYPE_ERROR,
                                "Binary operator type error.",
                                lineFile);
                    }
                } else if (leftTv instanceof SplFloat) {
                    double leftV = leftTv.floatValue();
                    if (rightTv instanceof SplFloat ||
                            rightTv.isIntLike()) {
                        double rightV = rightTv.intValue();
                        result = otherLogical(operator, env, leftV, rightV, getLineFile());
                    } else {
                        return SplInvokes.throwExceptionWithError(
                                env,
                                Constants.TYPE_ERROR,
                                "Binary operator type error.",
                                lineFile);
                    }
                } else if (leftTv instanceof Bool) {
                    boolean leftV = leftTv.booleanValue();
                    if (rightTv instanceof Bool) {
                        boolean rightV = (rightTv).booleanValue();
                        if (operator.equals("==")) {
                            result = Bool.boolValueOf(leftV == rightV);
                        } else if (operator.equals("!=")) {
                            result = Bool.boolValueOf(leftV != rightV);
                        } else {
                            return SplInvokes.throwExceptionWithError(
                                    env,
                                    Constants.TYPE_ERROR,
                                    "Binary operator type error.",
                                    lineFile);
                        }
                    } else {
                        return SplInvokes.throwExceptionWithError(
                                env,
                                Constants.TYPE_ERROR,
                                "Binary operator type error.",
                                lineFile);
                    }
                } else {
                    return SplInvokes.throwExceptionWithError(
                            env,
                            Constants.TYPE_ERROR,
                            "Binary operator type error.",
                            lineFile);
                }
            } else {  // is pointer type
                Reference leftPtr = (Reference) leftTv;
                if (rightTv instanceof Reference) {
                    Reference rightPtr = (Reference) rightTv;
                    result = pointerLogical(operator, leftPtr, rightPtr, env, lineFile);
                } else {
                    result = pointerLogical(
                            operator, leftPtr, Utilities.primitiveToWrapper(rightTv, env, lineFile), env, lineFile);
                }
            }
            return result;
        } else if (type == LAZY) {
            // a and b = b if a else false
            // a or b  = true if a else b
            if (operator.equals("and")) {
                Bool leftRes = Bool.evalBoolean(left, env, getLineFile());
                if (env.hasException()) return Undefined.ERROR;
                if (leftRes.value) {
                    return Bool.evalBoolean(right, env, getLineFile());
                } else {
                    return Bool.FALSE;
                }
            } else if (operator.equals("or")) {
                Bool leftRes = Bool.evalBoolean(left, env, getLineFile());
                if (env.hasException()) return Undefined.ERROR;
                if (!leftRes.value) {
                    return Bool.evalBoolean(right, env, getLineFile());
                } else {
                    return Bool.TRUE;
                }
            }
        }
        throw new SyntaxError("Unexpected error. ", lineFile);
    }

}
