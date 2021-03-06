package spl.parser;

import spl.ast.*;
import spl.util.LineFilePos;
import spl.util.Utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AstBuilder {

    private static final Map<String, Integer> PCD_BIN_NUMERIC = Map.of(
            "*", 100,
            "/", 100,
            "%", 100,
            "+", 50,
            "-", 50
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
            "->", 4,
            "in", 3,
            ":", 3,
            "_else_", 3,
            "_if_", 2,
            "as", 2,
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
            "new", 150,
            "namespace", 150,
            "throw", 100,
            "yield", 0,
            "return", 0,
            "assert" , 0
    );

    private static final Map<String, Integer> PRECEDENCES = Utilities.mergeMaps(
            PCD_BIN_NUMERIC, PCD_BIN_NUMERIC_BITWISE, PCD_BIN_LOGICAL, PCD_BIN_SPECIAL, PCD_BIN_LAZY,
            PCD_BINARY_ASSIGN_NORMAL, PCD_BINARY_ASSIGN_BITWISE,
            PCD_UNARY_NUMERIC, PCD_UNARY_LOGICAL, PCD_UNARY_SPECIAL
    );

    private final BlockStmt baseBlock;
    private final List<Node> stack = new ArrayList<>();
    private Line activeLine;

    public AstBuilder(LineFilePos initLfp) {
        baseBlock = new BlockStmt(initLfp);
        activeLine = new Line(initLfp);
    }

    Node getLastAddedNode() {
        if (stack.isEmpty()) return null;
        return stack.get(stack.size() - 1);
    }

    void addNode(Node node) {
        stack.add(node);
    }

    void addInt(long value, LineFilePos lineFile) {
        addNode(new IntLiteral(value, lineFile));
    }

    void addChar(char value, LineFilePos lineFile) {
        addNode(new CharLiteral(value, lineFile));
    }

    void addFloat(double value, LineFilePos lineFile) {
        addNode(new FloatLiteral(value, lineFile));
    }

    void addUnaryOperator(String op, int type, LineFilePos lineFile) {
        addNode(new RegularUnaryOperator(op, type, lineFile));
    }

    void addBinaryOperator(String op, int type, LineFilePos lineFile) {
        addNode(new BinaryOperator(op, type, lineFile));
    }

    void addFakeTernary(String op, LineFilePos lineFile) {
        addNode(new ConditionalExpr(op, lineFile));
    }

    Node removeLast() {
        return stack.remove(stack.size() - 1);
    }

    /**
     * This method would clear the active line.
     */
    void finishLine() {
        if (!stack.isEmpty()) finishPart();

        baseBlock.addLine(activeLine);
        activeLine = new Line();
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
        if (!stack.isEmpty()) {
            boolean hasExpr = false;
            for (Node node : stack) {
                if (node instanceof Buildable && ((Buildable) node).notFulfilled()) hasExpr = true;
            }
            if (hasExpr) {
                buildExpr(stack);
            }
            if (!activeLine.getLineFile().isReal()) {
                if (stack.size() > 0) {
                    activeLine.setLineFilePos(stack.get(0).getLineFile());
                }
            }
            activeLine.getChildren().addAll(stack);
            stack.clear();
        }
    }

    void buildExpr(List<Node> list) {
        try {
            while (true) {
                int maxPre = -1;
                int index = 0;

                for (int i = 0; i < list.size(); i++) {
                    Node node = list.get(i);
                    if (node instanceof Buildable && ((Buildable) node).notFulfilled()) {
                        int pre = PRECEDENCES.get(((Buildable) node).getOperator());
                        if (node instanceof UnaryExpr || node instanceof UnaryStmt) {
                            // eval right side unary operator first
                            // for example, "- -3" is -(-3)
                            if (pre >= maxPre) {
                                maxPre = pre;
                                index = i;
                            }
                        } else if (node instanceof BinaryExpr) {
                            BinaryExpr be = (BinaryExpr) node;
                            // for most times, eval left side binary operator first
                            // for example, if eval left first, "2 * 8 / 4" is (2 * 8) / 4
                            // if eval right first, then "x = y = 3" is x = (y = 3)
                            if (be.isLeftFirst() && pre > maxPre ||
                                    !be.isLeftFirst() && pre >= maxPre) {
                                maxPre = pre;
                                index = i;
                            }
                        } else if (node instanceof IncDecOperator) {
                            if (pre > maxPre) {
                                maxPre = pre;
                                index = i;
                            }
                        }
                    }
                }

                if (maxPre == -1) break;  // no expr found

                Buildable expr = (Buildable) list.get(index);
                if (expr instanceof UnaryBuildable) {
                    UnaryBuildable ue = (UnaryBuildable) expr;
                    Node value;
                    if (ue.operatorAtLeft()) {
                        if (list.size() <= index + 1 && ue.voidAble()) {
                            value = new NullExpr(LineFilePos.LF_PARSER);
                        } else {
                            value = list.remove(index + 1);
                        }
                    } else {
                        value = list.remove(index - 1);
                    }
                    ue.setValue((Expression) value);
                } else if (expr instanceof BinaryExpr) {
                    Node right = list.remove(index + 1);
                    Node left = list.remove(index - 1);
                    ((BinaryExpr) expr).setLeft((Expression) left);
                    ((BinaryExpr) expr).setRight((Expression) right);
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
            System.out.print("Error when " + list);
            if (list.size() > 0) {
                System.out.println(". " + list.get(0).getLineFile().toStringFileLine());
            } else {
                System.out.println();
            }
            throw e;
        }
    }
}
