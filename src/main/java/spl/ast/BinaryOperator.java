package spl.ast;

import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.*;
import spl.interpreter.splObjects.Function;
import spl.interpreter.splObjects.Instance;
import spl.interpreter.splObjects.SplMethod;
import spl.interpreter.splObjects.SplObject;
import spl.lexer.SyntaxError;
import spl.util.*;

import java.io.IOException;
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
            "!=", "__ne__",
            ">", "__gt__",
            "<", "__lt__"
    );

    private static final Map<String, String> BITWISE_OP_MAP = Map.of(
            "<<", "__lShift__",
            ">>", "__rShift__",
            ">>>", "__rShiftLogic__",
            "&", "__bAnd__",
            "|", "__bOr__",
            "^", "__bXor__"
    );

    private static final Set<String> UNCHANGED_LOGICAL = Set.of(
            "is", "is not"
    );

    private final int type;

    public BinaryOperator(String operator, int type, LineFilePos lineFile) {
        super(operator, lineFile);

        this.type = type;
    }

    private static SplElement pointerBitwise(Reference leftPtr,
                                             SplElement rightEle,
                                             String operator,
                                             Environment env,
                                             LineFilePos lineFilePos) {
        SplObject leftObj = env.getMemory().get(leftPtr);
        if (leftObj instanceof Instance) {
            return callOpFunction(leftPtr,
                    (Instance) leftObj,
                    rightEle,
                    operator,
                    BITWISE_OP_MAP.get(operator),
                    env,
                    lineFilePos);
        } else {
            return SplInvokes.throwExceptionWithError(
                    env,
                    Constants.TYPE_ERROR,
                    "Binary operator type error. Left side: ",
                    lineFilePos);
        }
    }

    private static SplElement callOpFunction(Reference leftPtr,
                                             Instance leftObj,
                                             SplElement rightEle,
                                             String operator,
                                             String fnName,
                                             Environment env,
                                             LineFilePos lineFilePos) {
        if (fnName == null) return SplInvokes.throwExceptionWithError(
                env,
                Constants.TYPE_ERROR,
                "Type '" + leftObj.getClass().getName() + "' does not support operator " + operator,
                lineFilePos
        );
        Environment instanceEnv = leftObj.getEnv();
        SplElement fnEle = instanceEnv.get(fnName, lineFilePos);
        if (fnEle == Undefined.ERROR) return Undefined.ERROR;
        Reference fnPtr = (Reference) fnEle;
        SplMethod opFn = env.getMemory().get(fnPtr);
        return opFn.call(EvaluatedArguments.of(leftPtr, rightEle), env, lineFilePos);
    }

    private static SplElement pointerNumericArithmetic(Reference leftPtr,
                                                       SplElement rightEle,
                                                       String operator,
                                                       Environment env,
                                                       LineFilePos lineFile) {
        SplObject leftObj = env.getMemory().get(leftPtr);
        if (leftObj instanceof Instance) {
            return callOpFunction(leftPtr,
                    (Instance) leftObj,
                    rightEle,
                    operator,
                    ARITHMETIC_OP_MAP.get(operator),
                    env,
                    lineFile);
        } else {
            return SplInvokes.throwExceptionWithError(
                    env,
                    Constants.TYPE_ERROR,
                    "Binary operator type error. Left side: ",
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

    private static SplElement simpleArithmeticInt(String op, Environment env, long l, long r, LineFilePos lineFile) {
        return switch (op) {
            case "+" -> new Int(l + r);
            case "-" -> new Int(l - r);
            case "*" -> new Int(l * r);
            case "/" -> new Int(l / r);
            case "%" -> new Int(l % r);
            default -> SplInvokes.throwExceptionWithError(
                    env,
                    Constants.TYPE_ERROR,
                    "Unsupported operation '" + op + "'. ",
                    lineFile);
        };
    }

    private static SplElement simpleArithmeticFloat(String op, Environment env, double l, double r, LineFilePos lineFile) {
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
                SplElement res = callOpFunction(
                        l,
                        (Instance) leftObj,
                        r,
                        op,
                        LOGICAL_OP_MAP.get(op),
                        env,
                        lineFile);
                if (res instanceof Bool) {
                    return res;
                }
            } else if (leftObj != null) {
                if (op.equals("==")) {
                    return pointerLogical("is", l, r, env, lineFile);
                } else if (op.equals("!=")) {
                    return pointerLogical("is not", l, r, env, lineFile);
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

    public static BinaryOperator reconstruct(BytesIn is, LineFilePos lineFilePos) throws Exception {
        String op = is.readString();
        Expression left = Reconstructor.reconstruct(is);
        Expression right = Reconstructor.reconstruct(is);
        int type = is.readInt();
        BinaryOperator be = new BinaryOperator(op, type, lineFilePos);
        be.setLeft(left);
        be.setRight(right);
        return be;
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

                if (leftEle.isIntLike()) {
                    if (rightEle.isIntLike()) {
                        return simpleArithmeticInt(
                                operator, env, leftEle.intValue(), rightEle.intValue(), lineFile
                        );
                    } else if (rightEle instanceof SplFloat) {
                        return simpleArithmeticFloat(operator, env, leftEle.floatValue(), rightEle.floatValue(), lineFile);
                    }
                } else if (leftEle instanceof SplFloat) {
                    return simpleArithmeticFloat(operator, env, leftEle.floatValue(), rightEle.floatValue(), lineFile);
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

            if (leftEle instanceof Reference) {
                return pointerBitwise((Reference) leftEle, rightEle, operator, env, lineFile);
            } else if (leftEle.isIntLike() && rightEle.isIntLike()) {
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
                        double rightV = rightTv.floatValue();
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
                SplElement leftRawRes = left.evaluate(env);
                if (leftRawRes instanceof Bool) {
                    Bool leftRes = (Bool) leftRawRes;
                    if (env.hasException()) return Undefined.ERROR;
                    if (!leftRes.value) {
                        return Bool.evalBoolean(right, env, getLineFile());
                    } else {
                        return Bool.TRUE;
                    }
                } else if (leftRawRes instanceof Reference) {
                    SplElement rightRes = right.evaluate(env);
                    Reference orFn = (Reference) env.get(Constants.OR_FN, lineFile);
                    Function function = env.getMemory().get(orFn);
                    EvaluatedArguments ea = EvaluatedArguments.of(leftRawRes, rightRes);
                    SplElement callRes = function.call(ea, env, lineFile);
                    if (env.hasException()) {
                        return Undefined.ERROR;
                    }
                    return callRes;
                }
                return SplInvokes.throwExceptionWithError(
                        env,
                        Constants.TYPE_ERROR,
                        "Binary operator type error.",
                        lineFile);
            }
        }
        throw new SyntaxError("Unexpected error. ", lineFile);
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        super.internalSave(out);
        out.writeInt(type);
    }
}
