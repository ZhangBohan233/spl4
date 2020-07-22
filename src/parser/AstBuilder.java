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
            "!=", 20
    );

    private static final Map<String, Integer> PCD_BIN_SPECIAL = Map.of(
            ".", 500,
//            "<>", 400,
            "<-", 160,  // must bigger than 'new'
            "->", 4,
            "?", 2,
            ":", 3,
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
            "&&", 6,
            "||", 5
    );

    private static final Map<String, Integer> PCD_UNARY_NUMERIC = Map.of(
            "neg", 200
    );

    private static final Map<String, Integer> PCD_UNARY_LOGICAL = Map.of(
            "!", 200
    );

    private static final Map<String, Integer> PCD_UNARY_SPECIAL = Map.of(
            "++", 300,
            "--", 300,
            "as", 150,
            "new", 150,
            "namespace", 150,
            "extends", 150,
            "instanceof", 25,
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

    private void setIndependence(boolean independence) {
        baseBlock.setIndependence(independence);
    }

    Node getLastAddedNode() {
        if (inner == null) {
            if (stack.isEmpty()) return null;
            return stack.get(stack.size() - 1);
        } else {
            return inner.getLastAddedNode();
        }
    }

    void addNode(Node node) {
        if (inner == null) {
            stack.add(node);
        } else {
            inner.addNode(node);
        }
    }

    void addName(String name, LineFile lineFile) {
        if (inner == null) {
            stack.add(new NameNode(name, lineFile));
        } else {
            inner.addName(name, lineFile);
        }
    }

//    void addPrimitiveTypeName(String name, LineFile lineFile) {
//        if (inner == null) {
//            stack.add(new PrimitiveTypeNameNode(name, lineFile));
//        } else {
//            inner.addPrimitiveTypeName(name, lineFile);
//        }
//    }

//    void addAnyStmt(LineFile lineFile) {
//        if (inner == null) {
//            stack.add(new AnyStmt(lineFile));
//        } else {
//            inner.addAnyStmt(lineFile);
//        }
//    }

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

    void addAssignment(LineFile lineFile) {
        if (inner == null) {
            stack.add(new Assignment(lineFile));
        } else {
            inner.addAssignment(lineFile);
        }
    }

    void addQuickAssignment(LineFile lineFile) {
        if (inner == null) {
            stack.add(new QuickAssignment(lineFile));
        } else {
            inner.addQuickAssignment(lineFile);
        }
    }

//    void addFuncTypeNode(LineFile lineFile) {
//        if (inner == null) {
//            stack.add(new FuncTypeNode(lineFile));
//        } else {
//            inner.addFuncTypeNode(lineFile);
//        }
//    }

    void addAnonymousClass(LineFile lineFile) {
        if (inner == null) {
            stack.add(new AnonymousClassExpr(lineFile));
        } else {
            inner.addAnonymousClass(lineFile);
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

    void addIncDecOperator(boolean isIncrement, LineFile lineFile) {
        if (inner == null) {
            stack.add(new IncDecOperator(isIncrement, lineFile));
        } else {
            inner.addIncDecOperator(isIncrement, lineFile);
        }
    }

    void addFakeTernary(String op, LineFile lineFile) {
        if (inner == null) {
            stack.add(new FakeTernaryOperator(op, lineFile));
        } else {
            inner.addFakeTernary(op, lineFile);
        }
    }

    void addReturnStmt(LineFile lineFile) {
        if (inner == null) {
            stack.add(new ReturnStmt(lineFile));
        } else {
            inner.addReturnStmt(lineFile);
        }
    }

    void addNewStmt(LineFile lineFile) {
        if (inner == null) {
            stack.add(new NewStmt(lineFile));
        } else {
            inner.addNewStmt(lineFile);
        }
    }

    void addDeclaration(int level, String declaredName, LineFile lineFile) {
        if (inner == null) {
            stack.add(new Declaration(level, declaredName, lineFile));
        } else {
            inner.addDeclaration(level, declaredName, lineFile);
        }
    }

    void addDot(LineFile lineFile) {
        if (inner == null) {
            stack.add(new Dot(lineFile));
        } else {
            inner.addDot(lineFile);
        }
    }

    void addCast(LineFile lineFile) {
        if (inner == null) {
            stack.add(new CastExpr(lineFile));
        } else {
            inner.addCast(lineFile);
        }
    }

    void addInstanceof(LineFile lineFile) {
        if (inner == null) {
            stack.add(new InstanceofExpr(lineFile));
        } else {
            inner.addInstanceof(lineFile);
        }
    }

//    void addFunction(String name, boolean isAbstract, LineFile lineFile) {
//        if (inner == null) {
//            FuncDefinition funcDefinition = new FuncDefinition(name, isAbstract, lineFile);
//            stack.add(funcDefinition);
//        } else {
//            inner.addFunction(name, isAbstract, lineFile);
//        }
//    }

//    void addLambdaHeader(LineFile lineFile) {
//        if (inner == null) {
//            stack.add(new LambdaExpr(lineFile));
//        } else {
//            inner.addLambdaHeader(lineFile);
//        }
//    }
//
//    void addParameterBracket() {
//        if (inner == null) {
//            inner = new AstBuilder();
//        } else {
//            inner.addParameterBracket();
//        }
//    }

//    void buildParameterBracket() {
//        if (inner.inner == null) {
//            Node lastNode = stack.get(stack.size() - 1);
//            inner.finishPart();
//            Line line = inner.getLine();
//            if (lastNode instanceof FuncDefinition) {
//                FuncDefinition defNode = (FuncDefinition) lastNode;
//                defNode.setParameters(line);
//            } else if (lastNode instanceof LambdaExpr) {
//                LambdaExpr lambdaExpr = (LambdaExpr) lastNode;
//                lambdaExpr.setParameters(line);
//            } else {
//                throw new ParseError("Unexpected syntax. ", lastNode.getLineFile());
//            }
//            inner = null;
//        } else {
//            inner.buildParameterBracket();
//        }
//    }

//    void addFnRType(LineFile lineFile) {
//        if (inner == null) {
//            finishPart();
//            int count = activeLine.getChildren().size();
//            if (count < 2) {
//                System.err.println(activeLine.getChildren());
//                throw new ParseError("Function must have a return type. ", lineFile);
//            }
////            System.out.println(activeLine);
//            FuncDefinition def = (FuncDefinition) activeLine.getChildren().get(count - 2);
//            Node rType = activeLine.getChildren().remove(count - 1);
////            def.setRType(rType);
//        } else {
//            inner.addFnRType(lineFile);
//        }
//    }

    void addCall(LineFile lineFile) {
        if (inner == null) {
            FuncCall funcCall = new FuncCall(lineFile);
            Node callObj = stack.remove(stack.size() - 1);
            funcCall.setCallObj(callObj);
            stack.add(funcCall);
            inner = new AstBuilder();
        } else {
            inner.addCall(lineFile);
        }
    }

    void buildCall(LineFile lineFile) {
        if (inner.inner == null) {
            inner.finishPart();
            Line line = inner.getLine();
            inner = null;
            Arguments arguments = new Arguments(line, lineFile);
            FuncCall call = (FuncCall) stack.get(stack.size() - 1);
            call.setArguments(arguments);
        } else {
            inner.buildCall(lineFile);
        }
    }

    Node removeLast() {
        return stack.remove(stack.size() - 1);
    }

    void addSqrBracketBlock() {
        if (inner == null) {
            inner = new AstBuilder();
        } else {
            inner.addSqrBracketBlock();
        }
    }

    void finishSqrBracketBlock() {
        if (inner.inner == null) {
            inner.finishPart();
            Line line = inner.getLine();
            inner = null;
            stack.add(line);
        } else {
            inner.finishSqrBracketBlock();
        }
    }

    void addBraceBlock() {
        if (inner == null) {
            inner = new AstBuilder();
        } else {
            inner.addBraceBlock();
        }
    }

    void addIndependenceBraceBlock() {
        if (inner == null) {
            inner = new AstBuilder();
            inner.setIndependence(true);
        } else {
            inner.addIndependenceBraceBlock();
        }
    }

    void buildBraceBlock() {
        if (inner.inner == null) {
            BlockStmt innerBlock = inner.getBaseBlock();
            stack.add(innerBlock);

            inner = null;
        } else {
            inner.buildBraceBlock();
        }
    }

    void addArrayLiteral(LineFile lineFile) {
        if (inner == null) {
            stack.add(new ArrayLiteral(lineFile));
        } else {
            inner.addArrayLiteral(lineFile);
        }
    }

    void buildArrayLiteral(LineFile lineFile) {
        if (inner.inner == null) {
            inner.finishPart();
            inner.finishLine();
            Line line = inner.baseBlock.getLines().get(0);
            inner = null;
            if (stack.isEmpty()) throw new ParseError("Empty parser stack. ", lineFile);
            Node node = stack.get(stack.size() - 1);
            if (node instanceof ArrayLiteral) {
                ArrayLiteral adc = (ArrayLiteral) node;
                adc.setContent(line);
            } else {
                throw new ParseError("Not an array creation. ", lineFile);
            }
        } else {
            inner.buildArrayLiteral(lineFile);
        }
    }

//    void addAngleBracketBlock() {
//        if (inner == null) {
//            inner = new AstBuilder();
//        } else {
//            inner.addAngleBracketBlock();
//        }
//    }

//    void buildTemplateAndAdd(LineFile lineFile) {
//        if (inner.inner == null) {
//            inner.finishPart();
//            inner.finishLine();
//            BlockStmt blockStmt = inner.getBaseBlock();
//            if (blockStmt.getLines().size() != 1) {
//                throw new ParseError("Template must be one line. ", lineFile);
//            }
//            Line line = blockStmt.getLines().get(0);
//            inner = null;
//            TemplateNode templateNode = new TemplateNode(line, lineFile);
//            Node last = stack.get(stack.size() - 1);
//            if (last instanceof ClassStmt) {
//                ((ClassStmt) last).setTemplateNode(templateNode);
//            } else if (last instanceof NameNode) {
//                ((NameNode) last).setTemplateNode(templateNode);
//            } else {
//                throw new ParseError("Unexpected syntax: template. ", lineFile);
//            }
//        } else {
//            inner.buildTemplateAndAdd(lineFile);
//        }
//    }

//    void addIndexing(LineFile lineFile) {
//        if (inner == null) {
////            System.out.println(stack);
//            IndexingNode node = new IndexingNode(stack.remove(stack.size() - 1), lineFile);
//            stack.add(node);
//            inner = new AstBuilder();
//        } else {
//            inner.addIndexing(lineFile);
//        }
//    }

//    void buildIndexing() {
//        if (inner.inner == null) {
//            inner.finishPart();
//            Line line = inner.getLine();
//            inner = null;
//            IndexingNode node = (IndexingNode) stack.get(stack.size() - 1);
//            node.setArgs(line);
//        } else {
//            inner.buildIndexing();
//        }
//    }

//    void addImportModule(String importName, LineFile lineFile) {
//        if (inner == null) {
//            ImportStmt stmt = new ImportStmt(importName, lineFile);
//            stack.add(stmt);
//        } else {
//            inner.addImportModule(importName, lineFile);
//        }
//    }

//    void buildImportModule(LineFile lineFile) {
//        if (inner == null) {
//            finishPart();
//            if (activeLine.getChildren().size() != 2) {
//                throw new ParseError("Function must have a return type. ", lineFile);
//            }
//            ImportStmt importStmt = (ImportStmt) activeLine.getChildren().get(0);
//            BlockStmt blockStmt = (BlockStmt) activeLine.getChildren().remove(1);
//            importStmt.setContent(blockStmt);
//            finishLine();
//        } else {
//            inner.buildImportModule(lineFile);
//        }
//    }

//    void addClass(String className, boolean isInterface, boolean isAbstract, LineFile lineFile) {
//        if (inner == null) {
//            stack.add(new ClassStmt(className, isInterface, isAbstract, lineFile));
//        } else {
//            inner.addClass(className, isInterface, isAbstract, lineFile);
//        }
//    }

//    void buildClass(LineFile lineFile) {
//        if (inner == null) {
//            finishPart();
////            System.out.println(activeLine.getChildren());
//            if (activeLine.getChildren().size() != 2)
//                throw new ParseError("Class must have a body. ", lineFile);
//            ClassStmt cs = (ClassStmt) activeLine.getChildren().get(0);
//            BlockStmt body = (BlockStmt) activeLine.getChildren().remove(1);
//            cs.setBody(body);
//            finishLine();
////            System.out.println(6666);
//        } else {
//            inner.buildClass(lineFile);
//        }
//    }

//    void addExtend(LineFile lineFile) {
//        if (inner == null) {
//            stack.add(new Extends(lineFile));
//        } else {
//            inner.addExtend(lineFile);
//        }
//    }

//    void finishExtend(LineFile lineFile) {
//        if (inner == null) {
//            finishPart();
//            if (activeLine.getChildren().size() != 2) {
//                throw new ParseError("Keyword 'extends' must follow a class. ", lineFile);
//            }
//            ClassStmt classStmt = (ClassStmt) activeLine.getChildren().get(0);
//            classStmt.setSuperclass(activeLine.getChildren().remove(1));
//        } else {
//            inner.finishExtend(lineFile);
//        }
//    }

    void addImplements() {
        if (inner == null) {
            inner = new AstBuilder();
        } else {
            inner.addImplements();
        }
    }

//    void finishImplements() {
//        if (inner.inner == null) {
//            inner.finishPart();
//            Line line = inner.getLine();
//            inner = null;
//            finishPart();
//            Implements extending = new Implements(line);
//            ClassStmt clazz = (ClassStmt) activeLine.getChildren().get(activeLine.getChildren().size() - 1);
//            clazz.setImplements(extending);
//        } else {
//            inner.finishImplements();
//        }
//    }

    void finishFunction(LineFile lineFile) {
        if (inner == null) {
            finishPart();
            if (activeLine.getChildren().size() != 2)
                throw new ParseError("Non-abstract Function must have a body. ", lineFile);
            FuncDefinition def = (FuncDefinition) activeLine.getChildren().get(0);
            BlockStmt body = (BlockStmt) activeLine.getChildren().remove(1);
            def.setBody(body);
            finishLine();
        } else {
            inner.finishFunction(lineFile);
        }
    }

    void finishAbstractFunction(LineFile lineFile) {
        if (inner == null) {
            finishLine();
        } else {
            inner.finishAbstractFunction(lineFile);
        }
    }

    void finishFunctionOuterBlock() {
        if (inner.inner == null) {
            BlockStmt blockStmt = inner.getBaseBlock();
            inner = null;
            assert blockStmt.getLines().size() == 1;
            Line fd = blockStmt.getLines().get(0);
            stack.add(fd);
            finishPart();
            finishLine();
        } else {
            inner.finishFunctionOuterBlock();
        }
    }

    void addParenthesis() {
        if (inner == null) {
            inner = new AstBuilder();
        } else {
            inner.addParenthesis();
        }
    }

    void buildParenthesis(LineFile lineFile) {
        if (inner.inner == null) {
            inner.finishPart();
            Line line = inner.getLine();
            inner = null;
            if (line.getChildren().size() != 1)
                throw new SyntaxError("Parenthesis must have exactly 1 element, got " +
                        line.getChildren().size() + ". ", lineFile);
            stack.add(line.getChildren().get(0));
        } else {
            inner.buildParenthesis(lineFile);
        }
    }

//    IfStmt addIf(LineFile lineFile) {
//        if (inner == null) {
//            IfStmt res = new IfStmt(lineFile);
//            stack.add(res);
//            inner = new AstBuilder();
//            return res;
//        } else {
//            return inner.addIf(lineFile);
//        }
//    }

//    void buildConditionTitle() {
//        if (inner.inner == null) {
//            Node stmt = stack.get(stack.size() - 1);
//            if (stmt instanceof IfStmt) {
//                inner.finishPart();
//                Line line = inner.getLine();
//                inner = null;
//                ((IfStmt) stmt).setCondition(line);
//            } else if (stmt instanceof WhileStmt) {
//                inner.finishPart();
//                Line line = inner.getLine();
//                inner = null;
//                ((WhileStmt) stmt).setCondition(line);
//            } else if (stmt instanceof ForLoopStmt) {
//                inner.finishPart();
//                inner.finishLine();
//                BlockStmt blockStmt = inner.getBaseBlock();
//                inner = null;
//                ((ForLoopStmt) stmt).setCondition(blockStmt);
//            } else if (stmt instanceof CaseStmt) {
//                inner.finishPart();
//                Line line = inner.getLine();
//                inner = null;
//                ((CaseStmt) stmt).setCondition(line);
//            }
//        } else {
//            inner.buildConditionTitle();
//        }
//    }

    void buildConditionBody() {
        if (inner == null) {
            assert stack.size() >= 2;
            ConditionalStmt cond = (ConditionalStmt) stack.get(stack.size() - 2);
            BlockStmt blockStmt = (BlockStmt) stack.remove(stack.size() - 1);
            cond.setBodyBlock(blockStmt);
        } else {
            inner.buildConditionBody();
        }
    }

//    IfStmt getActiveIfStmt() {
//        if (inner == null) {
////            System.out.println(stack);
//            return (IfStmt) stack.get(stack.size() - 1);
//        } else {
//            return inner.getActiveIfStmt();
//        }
//    }

    void addElse(LineFile lineFile) {
        if (inner == null) {
            stack.add(new ElseStmt(lineFile));
        } else {
            inner.addElse(lineFile);
        }
    }

    void checkElse() {
        if (inner == null) {
            if (stack.size() > 2 && stack.get(stack.size() - 2) instanceof ElseStmt) {
                IfStmt ifStmt = (IfStmt) stack.get(stack.size() - 3);
                BlockStmt elseBlock = (BlockStmt) stack.remove(stack.size() - 1);
                stack.remove(stack.size() - 1);  // remove the 'else' marker
                ifStmt.setElseBlock(elseBlock);
            }
        } else {
            inner.checkElse();
        }
    }

    void addCondStmt(LineFile lineFile) {
        if (inner == null) {
            stack.add(new CondCaseStmt(lineFile));
        } else {
            inner.addCondStmt(lineFile);
        }
    }

    void buildCondStmt(LineFile lineFile) {
        if (inner == null) {
            if (stack.size() < 2) {
                throw new ParseError("Not a cond-case statement. ", lineFile);
            }
            BlockStmt block = (BlockStmt) stack.remove(stack.size() - 1);
            CondCaseStmt stmt = (CondCaseStmt) stack.get(stack.size() - 1);
            stmt.setCases(block);
        } else {
            inner.buildCondStmt(lineFile);
        }
    }

    void addCase(LineFile lineFile) {
        if (inner == null) {
            CaseStmt caseStmt = new CaseStmt(lineFile, false);
            stack.add(caseStmt);
            inner = new AstBuilder();
        } else {
            inner.addCase(lineFile);
        }
    }

    void addDefault(LineFile lineFile) {
        if (inner == null) {
            CaseStmt caseStmt = new CaseStmt(lineFile, true);
            stack.add(caseStmt);
        } else {
            inner.addDefault(lineFile);
        }
    }

    void buildDefault(LineFile lineFile) {
        if (inner == null) {
            if (stack.size() < 2) {
                throw new ParseError("Not a default statement. ", lineFile);
            }
            BlockStmt block = (BlockStmt) stack.remove(stack.size() - 1);
            CaseStmt stmt = (CaseStmt) stack.get(stack.size() - 1);
            stmt.setBodyBlock(block);
        } else {
            inner.buildDefault(lineFile);
        }
    }

    void addFallthrough(LineFile lineFile) {
        if (inner == null) {
            stack.add(new FallthroughStmt(lineFile));
        } else {
            inner.addFallthrough(lineFile);
        }
    }

//    void addWhile(LineFile lineFile) {
//        if (inner == null) {
//            stack.add(new WhileStmt(lineFile));
//            inner = new AstBuilder();
//        } else {
//            inner.addWhile(lineFile);
//        }
//    }

//    void addFor(LineFile lineFile) {
//        if (inner == null) {
//            stack.add(new ForLoopStmt(lineFile));
//            inner = new AstBuilder();
//        } else {
//            inner.addFor(lineFile);
//        }
//    }

    void addBreak(LineFile lineFile) {
        if (inner == null) {
            stack.add(new BreakStmt(lineFile));
        } else {
            inner.addBreak(lineFile);
        }
    }

    void addContinue(LineFile lineFile) {
        if (inner == null) {
            stack.add(new ContinueStmt(lineFile));
        } else {
            inner.addContinue(lineFile);
        }
    }

//    void addElse(LineFile lineFile) {
//        if (inner == null) {
////            stack.add(new ElseStmt(lineFile));
//            assert stack.size() > 0;
//            Node ifs = stack.get(stack.size() - 1);
//            markElse(ifs);
//        } else {
//            inner.addElse(lineFile);
//        }
//    }
//
//    void checkElse() {
//        if (inner == null) {
//            if (stack.size() > 1) {
//                if (placeElse(stack.get(stack.size() - 2), stack.get(stack.size() - 1))) {
//                    stack.remove(stack.size() - 1);
//                }
//            }
//        } else {
//            inner.checkElse();
//        }
//    }

//    void checkElse() {
//        if (inner == null) {
//            System.out.println(stack);
//            if (stack.size() >= 3) {
////                System.out.println("ggg" + stack);
//                Node els = stack.get(stack.size() - 2);
//                if (els instanceof ElseStmt) {
//                    Node ifs = stack.get(stack.size() - 3);
//                    if (ifs instanceof IfStmt) {
//                        IfStmt ifStmt = (IfStmt) ifs;
//                        ifStmt.setElseBlock(stack.remove(stack.size() - 1));  // remove the else block
//                        stack.remove(stack.size() - 1);  // remove the ElseStmt
//                    } else {
//                        throw new ParseError("Else statement must follow an if statement. ", els.getLineFile());
//                    }
//                }
//            }
//        } else {
//            inner.checkElse();
//        }
//    }

//    void addNamespace(LineFile lineFile) {
//        if (inner == null) {
//            stack.add(new NamespaceNode(lineFile));
//        } else {
//            inner.addNamespace(lineFile);
//        }
//    }

    void addNull(LineFile lineFile) {
        if (inner == null) {
            stack.add(new NullStmt(lineFile));
        } else {
            inner.addNull(lineFile);
        }
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
                checkElse();
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

                for (int i = 0; i < list.size(); ++i) {
                    Node node = list.get(i);
                    if (node instanceof Expr && ((Expr) node).notFulfilled()) {
                        if (node instanceof UnaryExpr) {
                            int pre = PRECEDENCES.get(((UnaryExpr) node).getOperator());
                            if (pre > maxPre) {
                                maxPre = pre;
                                index = i;
                            }
                        } else if (node instanceof BinaryExpr) {
                            int pre = PRECEDENCES.get(((BinaryExpr) node).getOperator());
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
