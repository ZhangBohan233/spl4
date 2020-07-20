package ast;

import interpreter.primitives.*;
import interpreter.splObjects.Function;
import interpreter.splObjects.Instance;
import interpreter.splObjects.SplObject;
import interpreter.types.*;
import interpreter.env.Environment;
import lexer.SyntaxError;
import util.LineFile;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class BinaryOperator extends BinaryExpr {

    public static final int NUMERIC = 1;
    public static final int LOGICAL = 2;
    public static final int LAZY = 3;

    private static final Map<String, String> ARITHMETIC_OP_MAP = Map.of(
            "+", "__add__",
            "-", "__sub__",
            "*", "__mul__",
            "/", "__div__",
            "%", "__mod__"
    );

    private static final Set<String> UNCHANGED_LOGICAL = Set.of(
            "is"
    );

    private final int type;

    public BinaryOperator(String operator, int type, LineFile lineFile) {
        super(operator, lineFile);

        this.type = type;
    }

    @Override
    protected SplElement internalEval(Environment env) {
        if (type == NUMERIC) {
            SplElement leftEle = left.evaluate(env);
            SplElement rightEle = right.evaluate(env);

            if (leftEle instanceof Pointer) {
                return pointerTypeNumericArithmetic((Pointer) leftEle, rightEle, operator, env, getLineFile());
            } else {
                if (!SplElement.isPrimitive(rightEle)) {
                    throw new TypeError("Arithmetic between primitive and pointer is not supported. ",
                            getLineFile());
                }

                if (leftEle.isIntLike()) {
                    SplElement result = new Int(integerArithmetic(
                            operator,
                            leftEle.intValue(),
                            rightEle.intValue(),
                            rightEle.isIntLike(),
                            getLineFile()
                    ));
//                System.out.println(leftEle.getValue() + operator + rightEle.getValue() + '=' + result);
                    return result;
                } else if (leftEle instanceof SplFloat) {
                    return new SplFloat(floatArithmetic(
                            operator,
                            leftEle.floatValue(),
                            rightEle.floatValue(),
                            getLineFile()
                    ));
                } else {
                    throw new TypeError();
                }
            }
        } else if (type == LOGICAL) {
            SplElement leftTv = left.evaluate(env);
            SplElement rightTv = right.evaluate(env);
            boolean result;
            if (SplElement.isPrimitive(leftTv)) {
                if (!SplElement.isPrimitive(rightTv))
                    throw new TypeError("Primitive type cannot compare to pointer type. ",
                            getLineFile());
//                PrimitiveType lt = (PrimitiveType) leftTv.getType();
//                PrimitiveType rt = (PrimitiveType) rightTv.getType();
                if (leftTv.isIntLike()) {
                    long leftV = leftTv.intValue();
                    if (rightTv.isIntLike()) {
                        long rightV = rightTv.intValue();
                        result = integerLogical(operator, leftV, rightV, getLineFile());
                    } else if (rightTv instanceof SplFloat) {
                        double rightV = rightTv.floatValue();
                        result = otherLogical(operator, leftV, rightV, getLineFile());
                    } else {
                        throw new TypeError();
                    }
                } else if (leftTv instanceof SplFloat) {
                    double leftV = leftTv.floatValue();
                    if (rightTv instanceof SplFloat ||
                            rightTv.isIntLike()) {
                        double rightV = rightTv.intValue();
                        result = otherLogical(operator, leftV, rightV, getLineFile());
                    } else {
                        throw new TypeError();
                    }
                } else if (leftTv instanceof Bool) {
                    boolean leftV = ((Bool) leftTv).booleanValue();
                    if (rightTv instanceof Bool) {
                        boolean rightV = ((Bool) rightTv).booleanValue();
                        if (operator.equals("==")) {
                            result = leftV == rightV;
                        } else if (operator.equals("!=")) {
                            result = leftV != rightV;
                        } else {
                            throw new TypeError();
                        }
                    } else {
                        throw new TypeError();
                    }
                } else {
                    throw new TypeError();
                }
            } else {  // is pointer type
                Pointer ptr = (Pointer) leftTv;
                if (SplElement.isPrimitive(rightTv))
                    throw new TypeError("Cannot compare primitive type to pointer type. ", getLineFile());
                Pointer rightPtr = (Pointer) rightTv;
                result = integerLogical(operator, ptr.getPtr(), rightPtr.getPtr(), getLineFile());
            }
            return Bool.boolValueOf(result);
        } else if (type == LAZY) {
            // a && b = a ? b : false
            // a || b = a ? true : b
            FakeTernaryOperator fto = new FakeTernaryOperator("?", getLineFile());
            fto.setLeft(left);

            Declaration d = new Declaration(Declaration.USELESS, "ss", getLineFile());
//            if (operator.equals("&&")) {
//                d.setLeft(right);
//                d.setRight(BoolStmt.BOOL_STMT_FALSE);
//            } else if (operator.equals("||")) {
//                d.setLeft(BoolStmt.BOOL_STMT_TRUE);
//                d.setRight(right);
//            } else {
//                throw new SyntaxError("Unsupported lazy binary operator '" + operator +
//                        ". ", getLineFile());
//            }
            fto.setRight(d);
            return fto.evaluate(env);
        }
        throw new SyntaxError("Unexpected error. ", getLineFile());
    }

    private static SplElement pointerTypeNumericArithmetic(Pointer leftPtr,
                                                        SplElement rightEle,
                                                        String operator,
                                                        Environment env,
                                                        LineFile lineFile) {
        SplObject leftObj = env.getMemory().get(leftPtr);
        if (leftObj instanceof Instance) {
            String fnName = ARITHMETIC_OP_MAP.get(operator);
            Environment instanceEnv = ((Instance) leftObj).getEnv();
            Pointer fnPtr = (Pointer) instanceEnv.get(fnName, lineFile);
            Function opFn = (Function) env.getMemory().get(fnPtr);
            return opFn.call(new SplElement[]{rightEle}, env, lineFile);
        } else {
            throw new TypeError();
        }
    }

    private static long integerArithmetic(String op, long l, long r, boolean rIsInt, LineFile lineFile) {
        switch (op) {
            case "+":
                return l + r;
            case "-":
                return l - r;
            case "*":
                return l * r;
            case "/":
                return l / r;
            case "%":
                return l % r;
            case "<<":
                if (rIsInt) return l << r;
            case ">>":
                if (rIsInt) return l >> r;
            case ">>>":
                if (rIsInt) return l >>> r;
            case "&":
                if (rIsInt) return l & r;
            case "|":
                if (rIsInt) return l | r;
            case "^":
                if (rIsInt) return l ^ r;
            default:
                throw new TypeError("Unsupported operation '" + op + "'. ", lineFile);
        }
    }

    private static double floatArithmetic(String op, double l, double r, LineFile lineFile) {
        switch (op) {
            case "+":
                return l + r;
            case "-":
                return l - r;
            case "*":
                return l * r;
            case "/":
                return l / r;
            case "%":
                return l % r;
            default:
                throw new TypeError("Unsupported operation '" + op + "'. ", lineFile);
        }
    }

    private static boolean integerLogical(String op, long l, long r, LineFile lineFile) {
        switch (op) {
            case "==":
                return l == r;
            case "!=":
                return l != r;
            case ">":
                return l > r;
            case "<":
                return l < r;
            case ">=":
                return l >= r;
            case "<=":
                return l <= r;
            default:
                throw new SyntaxError("Unsupported binary operator '" + op + "' between int and int. ",
                        lineFile);
        }
    }

    private static boolean otherLogical(String op, double l, double r, LineFile lineFile) {
        switch (op) {
            case "==":
                return l == r;
            case "!=":
                return l != r;
            case ">":
                return l > r;
            case "<":
                return l < r;
            case ">=":
                return l >= r;
            case "<=":
                return l <= r;
            default:
                throw new SyntaxError(
                        String.format("Unsupported binary operator '%s'.",
                                op),
                        lineFile
                );
        }
    }

}
