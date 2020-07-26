package parser;

import ast.*;
import lexer.SyntaxError;
import util.LineFile;
import util.Utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AstBuilder {

    private static final Map<String, Integer> PCD_BIN_NUMERIC = Map.of(
            "*", 100,
            "/", 100,
            "%", 100,
            "+", 50,
            "-", 50,
            ">>", 40,
            ">>>", 40,
            "<<", 40
    );

    private static final Map<String, Integer> PCD_BIN_NUMERIC_BITWISE = Map.of(
            ">>", 40,
            ">>>", 40,
            "<<", 40,
            "&", 17,
            "^", 16,
            "|", 15
    );

    private static final Map<String, Integer> PCD_BIN_LOGICAL = Map.of(
            ">", 25,
            "<", 25,
            ">=", 25,
            "<=", 25,
            "==", 20,
            "!=", 20,
            "is", 20,
            "is not", 20
    );

    private static final Map<String, Integer> PCD_BIN_SPECIAL = Map.of(
            ".", 500,
            "<-", 160,  // must bigger than 'new'
            "->", 4,
            ":", 3,
            "_else_", 3,
            "_if_", 2,
            ":=", 1,
            "=", 1
    );

    private static final Map<String, Integer> PCD_BINARY_ASSIGN_NORMAL = Map.of(
            "+=", 3,
            "-=", 3,
            "*=", 3,
            "/=", 3,
            "%=", 3
    );

    private static final Map<String, Integer> PCD_BINARY_ASSIGN_BITWISE = Map.of(
            ">>=", 3,
            ">>>=", 3,
            "<<=", 3,
            "&=", 3,
            "|=", 3,
            "^=", 3
    );

    private static final Map<String, Integer> PCD_BIN_LAZY = Map.of(
            "and", 6,
            "or", 5
    );

    private static final Map<String, Integer> PCD_UNARY_NUMERIC = Map.of(
            "neg", 200
    );

    private static final Map<String, Integer> PCD_UNARY_LOGICAL = Map.of(
            "not", 200
    );

    private static final Map<String, Integer> PCD_UNARY_SPECIAL = Map.of(
            "++", 300,
            "--", 300,
            "star", 200,
            "as", 150,
            "new", 150,
            "namespace", 150,
            "return", 0
    );

    private static final Map<String, Integer> PRECEDENCES = Utilities.mergeMaps(
            PCD_BIN_NUMERIC, PCD_BIN_NUMERIC_BITWISE, PCD_BIN_LOGICAL, PCD_BIN_SPECIAL, PCD_BIN_LAZY,
            PCD_BINARY_ASSIGN_NORMAL, PCD_BINARY_ASSIGN_BITWISE,
            PCD_UNARY_NUMERIC, PCD_UNARY_LOGICAL, PCD_UNARY_SPECIAL
    );

    private final BlockStmt baseBlock = new BlockStmt();
    private final List<Node> stack = new ArrayList<>();
    private Line activeLine = new Line();
    private AstBuilder inner;

//    private void setIndependence(boolean independence) {
//        baseBlock.setIndependence(independence);
//    }

    Node getLastAddedNode() {
        if (inner == null) {
            if (stack.isEmpty()) return null;
            return stack.get(stack.size() - 1);
        } else {
            return inner.getLastAddedNode();
        }
    }

    boolean exprIsEmpty() {
        if (inner == null) {
            return stack.isEmpty();
        } else {
            return inner.exprIsEmpty();
        }
    }

    void addNode(Node node) {
        if (inner == null) {
            stack.add(node);
        } else {
            inner.addNode(node);
        }
    }

    void addInt(long value, LineFile lineFile) {
        if (inner == null) {
            stack.add(new IntNode(value, lineFile));
        } else {
            inner.addInt(value, lineFile);
        }
    }

    void addChar(char value, LineFile lineFile) {
        if (inner == null) {
            stack.add(new CharNode(value, lineFile));
        } else {
            inner.addChar(value, lineFile);
        }
    }

    void addBoolean(boolean value, LineFile lineFile) {
        if (inner == null) {
            stack.add(new BoolStmt(value, lineFile));
        } else {
            inner.addBoolean(value, lineFile);
        }
    }

    void addFloat(double value, LineFile lineFile) {
        if (inner == null) {
            stack.add(new FloatNode(value, lineFile));
        } else {
            inner.addFloat(value, lineFile);
        }
    }

    void addString(char[] charArray, LineFile lineFile) {
        if (inner == null) {
            stack.add(new StringLiteral(charArray, lineFile));
        } else {
            inner.addString(charArray, lineFile);
        }
    }

    void addUnaryOperator(String op, int type, LineFile lineFile) {
        if (inner == null) {
            stack.add(new RegularUnaryOperator(op, type, lineFile));
        } else {
            inner.addUnaryOperator(op, type, lineFile);
        }
    }

    void addBinaryOperator(String op, int type, LineFile lineFile) {
        if (inner == null) {
            stack.add(new BinaryOperator(op, type, lineFile));
        } else {
            inner.addBinaryOperator(op, type, lineFile);
        }
    }

    void addBinaryOperatorAssign(String op, LineFile lineFile) {
        if (inner == null) {
            stack.add(new BinaryOperatorAssignment(op, lineFile));
        } else {
            inner.addBinaryOperatorAssign(op, lineFile);
        }
    }

    void addFakeTernary(String op, LineFile lineFile) {
        if (inner == null) {
            stack.add(new ConditionalExpr(op, lineFile));
        } else {
            inner.addFakeTernary(op, lineFile);
        }
    }

    Node removeLast() {
        return stack.remove(stack.size() - 1);
    }

    /**
     * This method would clear the active line.
     */
    void finishLine() {
        if (inner == null) {
            if (!stack.isEmpty()) finishPart();

            baseBlock.addLine(activeLine);
            activeLine = new Line();
        } else {
            inner.finishLine();
        }
    }

    Line getLine() {
        return activeLine;
    }

    BlockStmt getBaseBlock() {
        return baseBlock;
    }

    /**
     * This method would build all expressions in the active stack and clear the stack.
     */
    void finishPart() {
        if (inner == null) {
            if (!stack.isEmpty()) {
                boolean hasExpr = false;
                for (Node node : stack) {
                    if (node instanceof Expr && ((Expr) node).notFulfilled()) hasExpr = true;
                }
                if (hasExpr) {
                    buildExpr(stack);
                }
                activeLine.getChildren().addAll(stack);
                stack.clear();
            }
        } else {
            inner.finishPart();
        }
    }

    void buildExpr(List<Node> list) {
        try {
            while (true) {
//                System.out.println(list);
                int maxPre = -1;
                int index = 0;

                for (int i = 0; i < list.size(); i++) {
//                for (int i = list.size() - 1; i >= 0; i--) {
                    Node node = list.get(i);
                    if (node instanceof Expr && ((Expr) node).notFulfilled()) {
                        if (node instanceof UnaryExpr) {
                            int pre = PRECEDENCES.get(((UnaryExpr) node).getOperator());

                            // eval right side unary operator first
                            // for example, "- -3" is -(-3)
                            if (pre >= maxPre) {
                                maxPre = pre;
                                index = i;
                            }
                        } else if (node instanceof BinaryExpr) {
                            int pre = PRECEDENCES.get(((BinaryExpr) node).getOperator());

                            // eval left side binary operator first
                            // for example, "2 * 8 / 4" is (2 * 8) / 4
                            if (pre > maxPre) {
                                maxPre = pre;
                                index = i;
                            }
                        } else if (node instanceof IncDecOperator) {
                            int pre;
                            if (((IncDecOperator) node).isIncrement) {
                                pre = PRECEDENCES.get("++");
                            } else {
                                pre = PRECEDENCES.get("--");
                            }
                            if (pre > maxPre) {
                                maxPre = pre;
                                index = i;
                            }
                        }
                    }
                }

                if (maxPre == -1) break;  // no expr found

                Expr expr = (Expr) list.get(index);
                if (expr instanceof UnaryExpr) {
                    UnaryExpr ue = (UnaryExpr) expr;
                    Node value;
                    if (ue.atLeft) {
                        if (list.size() <= index + 1 && ue.voidAble()) {
                            value = VoidNode.VOID_NODE;
                        } else {
                            value = list.remove(index + 1);
                        }
                    } else {
                        value = list.remove(index - 1);
                    }
                    ue.setValue(value);
                } else if (expr instanceof BinaryExpr) {
                    Node right = list.remove(index + 1);
                    Node left = list.remove(index - 1);
                    ((BinaryExpr) expr).setLeft(left);
                    ((BinaryExpr) expr).setRight(right);
                } else if (expr instanceof IncDecOperator) {
                    boolean post = true;
                    if (index < list.size() - 1) {
                        Node value = list.get(index + 1);
                        if (value instanceof NameNode || value instanceof Dot) {
                            post = false;
                        }
                    }
                    IncDecOperator ieo = (IncDecOperator) expr;
                    ieo.setPost(post);
                    if (post) {
                        ieo.setValue(list.remove(index - 1));
                    } else {
                        ieo.setValue(list.remove(index + 1));
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Error when " + list);
            throw e;
        }
    }

//    private static boolean markElse(Node node) {
//        if (node instanceof IfStmt) {
//            IfStmt ifs = (IfStmt) node;
//            if (markElse(ifs.getBodyBlock())) return true;
//            if (ifs.getElseBlock() == null) {
//                ifs.setHasElse(true);
//                return true;
//            } else {
//                markElse(ifs.getElseBlock());
//            }
//        }
//        return false;
//    }
//
//    private static boolean placeElse(Node node, Node element) {
//        if (node instanceof IfStmt) {
//            IfStmt ifs = (IfStmt) node;
//            if (placeElse(ifs.getBodyBlock(), element)) return true;
//            if (ifs.hasElse()) {
//                if (ifs.getElseBlock() == null) {
//                    ifs.setElseBlock(element);
//                    return true;
//                } else {
//                    return placeElse(ifs.getElseBlock(), element);
//                }
//            }
//        }
//        return false;
//    }
}
