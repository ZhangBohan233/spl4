package spl.parser;

import spl.ast.*;
import spl.lexer.*;
import spl.lexer.treeList.*;
import spl.util.Constants;
import spl.util.LineFile;

import java.io.IOException;

public class Parser {

    private final CollectiveElement rootList;

    private int varLevel = Declaration.USELESS;

    public Parser(TextProcessResult textProcessResult) {
        this.rootList = textProcessResult.rootList;
    }

    private AstBuilder parseSomeBlock(CollectiveElement collectiveElement) throws IOException {
        AstBuilder builder = new AstBuilder();
        int i = 0;
        while (i < collectiveElement.size()) {
            i = parseOne(collectiveElement, i, builder);
        }
        return builder;
    }

    private BlockStmt parseBlock(CollectiveElement collectiveElement) throws IOException {
        AstBuilder builder = parseSomeBlock(collectiveElement);
        varLevel = Declaration.USELESS;
        builder.finishPart();
        builder.finishLine();
        return builder.getBaseBlock();
    }

    private Line parseOneLineBlock(BracketList bracketList) throws IOException {
        AstBuilder builder = parseSomeBlock(bracketList);
        varLevel = Declaration.USELESS;
        builder.finishPart();
        return builder.getLine();
    }

    private Expression parseOnePartBlock(BracketList bracketList) throws IOException {
        AstBuilder builder = parseSomeBlock(bracketList);
        builder.finishPart();
        Line line = builder.getLine();
        if (line.size() != 1) {
            throw new SyntaxError("Expected 1 part in line, got " + line.size() + ": " + line + ". ",
                    bracketList.lineFile);
        }
        return (Expression) line.get(0);
    }

    private Line parseSqrBracket(SqrBracketList sqrBracketList) throws IOException {
        AstBuilder builder = parseSomeBlock(sqrBracketList);
        builder.finishPart();
        return builder.getLine();
    }

    private Expression parseParenthesis(BracketList bracketList) throws IOException {
        return parseOnePartBlock(bracketList);
    }

    /**
     * Returns the index after proceed.
     *
     * @param parent  parent collective element
     * @param index   the current processing index in parent
     * @param builder the abstract syntax tree builder
     * @return the index in parent after the current one has been proceeded
     */
    private int parseOne(CollectiveElement parent, int index, AstBuilder builder) throws IOException {
        Element ele = parent.get(index++);
        if (ele instanceof AtomicElement) {
            Token token = ((AtomicElement) ele).atom;
            LineFile lineFile = token.getLineFile();
            try {
                if (token instanceof IdToken) {
                    String identifier = ((IdToken) token).getIdentifier();

                    if (identifier.equals("-")) {
                        // special case, since "-" can both binary (subtraction) or unary (negation)
                        if (index < 2 || isUnary(parent.get(index - 2))) {
                            // negation
                            builder.addUnaryOperator("neg", RegularUnaryOperator.NUMERIC, lineFile);
                        } else {
                            // subtraction
                            builder.addBinaryOperator("-", BinaryOperator.ARITHMETIC, lineFile);
                        }
                    } else if (identifier.equals("*")) {
                        // special case, since "*" can both binary (multiplication) or unary (star)
                        if (index < 2 || isUnary(parent.get(index - 2))) {
                            // star
                            builder.addNode(new StarExpr(lineFile));
                        } else {
                            // multiplication
                            builder.addBinaryOperator("*", BinaryOperator.ARITHMETIC, lineFile);
                        }
                    } else if (identifier.equals("is")) {
                        Element next = parent.get(index);
                        if (notIdentifierOf(next, "not")) {
                            builder.addBinaryOperator("is", BinaryOperator.LOGICAL, lineFile);
                        } else {
                            index++;
                            builder.addBinaryOperator("is not", BinaryOperator.LOGICAL, lineFile);
                        }
                    } else if (Tokenizer.LOGICAL_UNARY.contains(identifier)) {
                        builder.addUnaryOperator(identifier, RegularUnaryOperator.LOGICAL, lineFile);
                    } else if (Tokenizer.NUMERIC_BINARY.contains(identifier)) {
                        builder.addBinaryOperator(identifier, BinaryOperator.ARITHMETIC, lineFile);
                    } else if (Tokenizer.BITWISE_BINARY.contains(identifier)) {
                        builder.addBinaryOperator(identifier, BinaryOperator.BITWISE, lineFile);
                    } else if (Tokenizer.NUMERIC_BINARY_ASSIGN.contains(identifier)) {
                        builder.addNode(new BinaryOperatorAssignment(identifier, BinaryOperator.ARITHMETIC, lineFile));
                    } else if (Tokenizer.BITWISE_BINARY_ASSIGN.contains(identifier)) {
                        builder.addNode(new BinaryOperatorAssignment(identifier, BinaryOperator.BITWISE, lineFile));
                    } else if (Tokenizer.LOGICAL_BINARY.contains(identifier)) {
                        builder.addBinaryOperator(identifier, BinaryOperator.LOGICAL, lineFile);
                    } else if (Tokenizer.LAZY_BINARY.contains(identifier)) {
                        builder.addBinaryOperator(identifier, BinaryOperator.LAZY, lineFile);
                    } else if (Tokenizer.FAKE_TERNARY.contains(identifier)) {
                        builder.addFakeTernary(identifier, lineFile);
                    } else {

                        // These are for convenience
                        IdToken nameToken;
                        Element next;
                        BraceList bodyList;
                        BlockStmt bodyBlock;
                        BracketList conditionList;
                        Expression condition;
                        BracketList singleBodyList;
                        CaseStmt caseStmt;

                        switch (identifier) {
                            case "=":
                                varLevel = Declaration.USELESS;
                                builder.addNode(new Assignment(lineFile));
                                break;
                            case ":=":
                                varLevel = Declaration.USELESS;
                                builder.addNode(new QuickAssignment(lineFile));
                                break;
                            case ":":
                                builder.addNode(new TypeExpr(lineFile));
                                break;
                            case ".":
                                builder.addNode(new Dot(lineFile));
                                break;
                            case "++":
                                builder.addNode(new IncDecOperator(true, lineFile));
                                break;
                            case "--":
                                builder.addNode(new IncDecOperator(false, lineFile));
                                break;
                            case ";":
                                varLevel = Declaration.USELESS;
                                builder.finishPart();
                                builder.finishLine();
                                break;
                            case ",":
                                varLevel = Declaration.USELESS;
                                builder.finishPart();
                                break;
                            case "true":
                                builder.addNode(new BoolStmt(true, lineFile));
                                break;
                            case "false":
                                builder.addNode(new BoolStmt(false, lineFile));
                                break;
                            case "null":
                                builder.addNode(new NullExpr(lineFile));
                                break;
                            case "fn":  // function definition
                                next = parent.get(index++);
                                BracketList paramList;
                                String name;
                                if (next instanceof AtomicElement) {
                                    nameToken = (IdToken) ((AtomicElement) next).atom;
                                    name = nameToken.getIdentifier();
                                    paramList = (BracketList) parent.get(index++);
                                } else {
                                    paramList = (BracketList) next;
                                    name = "anonymous function";
                                }
                                Expression rtnType = null;
                                next = parent.get(index++);
                                if (identifierOf(next, "->")) {
//                                    System.out.println(123212312);
                                    BracketList rtnTypeList = new BracketList(null, lineFile);
//                                    index++;  // skip
                                    while (!((next = parent.get(index++)) instanceof BraceList)) {
                                        rtnTypeList.add(next);
                                    }
//                                    System.out.println(rtnTypeList);
                                    rtnType = parseOnePartBlock(rtnTypeList);
                                }
                                bodyList = (BraceList) next;

                                Line paramBlock = parseOneLineBlock(paramList);
                                bodyBlock = parseBlock(bodyList);
//                                System.out.println(paramBlock);
//                                System.out.println(rtnType);

                                // this line must before FuncDefinition
                                ContractNode autoCont = autoContract(name, paramBlock, rtnType, lineFile);

                                FuncDefinition def = new FuncDefinition(name, paramBlock, bodyBlock, lineFile);
                                builder.addNode(def);

                                if (autoCont != null) {
                                    builder.addNode(autoCont);
                                }
//                                builder.finishPart();
//                                builder.finishLine();
                                break;
                            case "lambda":
                                paramList = new BracketList(null, lineFile);
                                while (notIdentifierOf(next = parent.get(index++), "->")) {
                                    paramList.add(next);
                                }
                                singleBodyList = new BracketList(null, lineFile);
                                while (index < parent.size() &&
                                        notIdentifierOf(next = parent.get(index++), ";") &&
                                        notIdentifierOf(next, ",")) {
                                    singleBodyList.add(next);
                                }
                                paramBlock = parseOneLineBlock(paramList);
                                Expression bodyNode = parseOnePartBlock(singleBodyList);
                                LambdaExpressionDef lambdaFunctionDef =
                                        new LambdaExpressionDef(paramBlock, bodyNode, lineFile);
                                builder.addNode(lambdaFunctionDef);
                                break;
                            case "contract":
                                nameToken = (IdToken) ((AtomicElement) parent.get(index++)).atom;
                                paramList = (BracketList) parent.get(index++);
                                BracketList rtnTypeLst = new BracketList(null, lineFile);
                                IdToken arrow = ((IdToken) ((AtomicElement) parent.get(index++)).atom);
                                if (!arrow.getIdentifier().equals("->"))
                                    throw new SyntaxError("Syntax of contract: " +
                                            "'contract f(argsContract...) -> rtnContract; '. ", lineFile);
                                while (notIdentifierOf(next = parent.get(index++), ";")) {
                                    rtnTypeLst.add(next);
                                }
                                Line rtnLine = parseOneLineBlock(rtnTypeLst);
                                Line paramLine = parseOneLineBlock(paramList);

                                if (rtnLine.getChildren().size() != 1)
                                    throw new SyntaxError("Syntax of contract: " +
                                            "'contract f(argsContract...) -> rtnContract; '. ", lineFile);
                                Expression rtnCon = (Expression) rtnLine.getChildren().get(0);

                                ContractNode contractNode =
                                        new ContractNode(nameToken.getIdentifier(), paramLine, rtnCon, lineFile);
                                builder.addNode(contractNode);
                                break;
                            case "class":
                                nameToken = (IdToken) ((AtomicElement) parent.get(index++)).atom;
                                Element probExtendEle = parent.get(index++);
                                BraceList bodyEle;
                                Line extensions;
                                if (probExtendEle instanceof BracketList) {
                                    // extending, example:
                                    // class C(A, B) { ... }
                                    bodyEle = (BraceList) parent.get(index++);
                                    extensions = parseOneLineBlock((BracketList) probExtendEle);
                                } else {
                                    bodyEle = (BraceList) probExtendEle;
                                    extensions = null;
                                }
                                bodyBlock = parseBlock(bodyEle);
                                ClassStmt classStmt = new ClassStmt(
                                        nameToken.getIdentifier(),
                                        extensions,
                                        bodyBlock,
                                        lineFile);
                                builder.addNode(classStmt);
                                break;
                            case "const":
                                varLevel = Declaration.CONST;
//                                nameToken = (IdToken) ((AtomicElement) parent.get(index++)).atom;
//                                builder.addNode(new Declaration(Declaration.CONST, nameToken.getIdentifier(), lineFile));
                                break;
                            case "var":
                                varLevel = Declaration.VAR;
//                                nameToken = (IdToken) ((AtomicElement) parent.get(index++)).atom;
//                                builder.addNode(new Declaration(Declaration.VAR, nameToken.getIdentifier(), lineFile));
                                break;
                            case "as":
                                AsExpr asExpr = new AsExpr(lineFile);
                                builder.addNode(asExpr);
                                break;
                            case "in":
                                builder.addNode(new InExpr(lineFile));
                                varLevel = Declaration.USELESS;
                                break;
                            case "return":
                                builder.addNode(new ReturnStmt(lineFile));
                                break;
                            case "new":
                                builder.addNode(new NewExpr(lineFile));
                                break;
                            case "throw":
                                builder.addNode(new ThrowStmt(lineFile));
                                break;
                            case "if":
                                conditionList = new BracketList(null, lineFile);
                                Node lastAddedNode = builder.getLastAddedNode();
                                if (!(lastAddedNode instanceof Expression)) {  // regular if-stmt
                                    while (!((next = parent.get(index++)) instanceof BraceList)) {
                                        conditionList.add(next);
                                    }
                                    bodyList = (BraceList) next;
                                    condition = parseOnePartBlock(conditionList);
                                    IfStmt ifStmt = new IfStmt(condition, parseBlock(bodyList), lineFile);
                                    builder.addNode(ifStmt);
                                    if (index < parent.size()) {
                                        next = parent.get(index);
                                        if (!notIdentifierOf(next, "else")) {
                                            BraceList elseList = (BraceList) parent.get(index + 1);
                                            index += 2;
                                            ifStmt.setElseBlock(parseBlock(elseList));
                                        }
                                    }
                                    builder.finishPart();
                                    builder.finishLine();
                                } else {
                                    while (notIdentifierOf(next = parent.get(index++), "else")) {
                                        conditionList.add(next);
                                    }
                                    singleBodyList = new BracketList(null, lineFile);  // else part
                                    while (index < parent.size() &&
                                            notIdentifierOf(next = parent.get(index), ";")) {
                                        singleBodyList.add(next);
                                        index++;  // this step is to ensure that ';' will not be omitted
                                    }
                                    condition = parseOnePartBlock(conditionList);
                                    Expression elseBodyExpr = parseOnePartBlock(singleBodyList);
                                    ConditionalExpr elseExpr =
                                            new ConditionalExpr("_else_", lineFile);
                                    ConditionalExpr ifExpr = new ConditionalExpr("_if_", lineFile);

                                    builder.addNode(ifExpr);
                                    builder.addNode(condition);
                                    builder.addNode(elseExpr);
                                    builder.addNode(elseBodyExpr);
                                }
                                break;
//                            case "cond":
//                                bodyList = (BraceList) parent.get(index++);
//                                bodyBlock = parseBlock(bodyList);
//                                SwitchCaseFactory ccf = new SwitchCaseFactory(bodyBlock);
//                                if (ccf.isExpr()) {
//                                    builder.addNode(ccf.buildExpr(lineFile));
//                                } else {
//                                    builder.addNode(ccf.buildStmt(lineFile));
//                                }
//                                break;
                            case "switch":
                                conditionList = new BracketList(null, lineFile);
                                while (!((next = parent.get(index++)) instanceof BraceList)) {
                                    conditionList.add(next);
                                }
                                bodyList = (BraceList) next;
                                Expression expression = parseOnePartBlock(conditionList);
                                bodyBlock = parseBlock(bodyList);
                                SwitchCaseFactory ccf = new SwitchCaseFactory(expression, bodyBlock);
                                if (ccf.isExpr()) {
                                    builder.addNode(ccf.buildExpr(lineFile));
                                } else {
                                    builder.addNode(ccf.buildStmt(lineFile));
                                }
                                break;
                            case "case":
                                conditionList = new BracketList(null, lineFile);
                                while (!((next = parent.get(index++)) instanceof BraceList) &&
                                        notIdentifierOf(next, "->")) {
                                    conditionList.add(next);
                                }

                                condition = parseOnePartBlock(conditionList);
                                if (next instanceof BraceList) {
                                    // case ... { ... }
                                    bodyList = (BraceList) next;
                                    bodyBlock = parseBlock(bodyList);
                                    caseStmt = new CaseStmt(condition, bodyBlock, false, lineFile);
                                } else {
                                    next = parent.get(index);
                                    if (next instanceof BraceList) {
                                        // case ... -> { ... }
                                        index++;
                                        bodyBlock = parseBlock((CollectiveElement) next);
                                        caseStmt = new CaseStmt(condition, bodyBlock, true, lineFile);
                                    } else {
                                        // case ... -> ...;
                                        singleBodyList = new BracketList(null, lineFile);
                                        while (notIdentifierOf(next = parent.get(index++), ";")) {
                                            singleBodyList.add(next);
                                        }
                                        Expression body = parseOnePartBlock(singleBodyList);
                                        caseStmt = new CaseStmt(condition, body, true, lineFile);
                                    }
                                }
                                builder.addNode(caseStmt);
                                break;
                            case "default":
                                next = parent.get(index++);

                                if (next instanceof BraceList) {
                                    // default { ... }
                                    bodyList = (BraceList) next;
                                    bodyBlock = parseBlock(bodyList);
                                    caseStmt = new CaseStmt(null, bodyBlock, false, lineFile);
                                } else if (!notIdentifierOf(next, "->")) {
                                    next = parent.get(index);
                                    if (next instanceof BraceList) {
                                        // default -> { ... }
                                        index++;
                                        bodyBlock = parseBlock((CollectiveElement) next);
                                        caseStmt = new CaseStmt(null, bodyBlock, true, lineFile);
                                    } else {
                                        // default -> ...;
                                        singleBodyList = new BracketList(null, lineFile);
                                        while (notIdentifierOf(next = parent.get(index++), ";")) {
                                            singleBodyList.add(next);
                                        }
                                        Expression body = parseOnePartBlock(singleBodyList);
                                        caseStmt = new CaseStmt(null, body, true, lineFile);
                                    }
                                } else {
                                    throw new SyntaxError("Unknown syntax 'default " + next + ". ",
                                            lineFile);
                                }
                                builder.addNode(caseStmt);
                                break;
                            case "fallthrough":
                                builder.addNode(new FallthroughStmt(lineFile));
                                break;
                            case "break":
                                builder.addNode(new BreakStmt(lineFile));
                                break;
                            case "continue":
                                builder.addNode(new ContinueStmt(lineFile));
                                break;
                            case "yield":
                                builder.addNode(new YieldStmt(lineFile));
                                break;
                            case "for":
                                conditionList = new BracketList(null, lineFile);
                                while (!((next = parent.get(index++)) instanceof BraceList)) {
                                    conditionList.add(next);
                                }
                                bodyList = (BraceList) next;
                                BlockStmt conditionLines = parseBlock(conditionList);
                                ForLoopStmt forLoopStmt =
                                        new ForLoopStmt(conditionLines, parseBlock(bodyList), lineFile);
                                builder.addNode(forLoopStmt);
                                builder.finishPart();
                                builder.finishLine();
                                break;
                            case "try":
                                bodyList = (BraceList) parent.get(index++);
                                bodyBlock = parseBlock(bodyList);
                                TryStmt tryStmt = new TryStmt(bodyBlock, lineFile);
                                builder.addNode(tryStmt);
                                break;
                            case "catch":
                                conditionList = new BracketList(null, lineFile);
                                while (!((next = parent.get(index++)) instanceof BraceList)) {
                                    conditionList.add(next);
                                }
                                condition = parseOnePartBlock(conditionList);
                                bodyBlock = parseBlock((BraceList) next);
                                CatchStmt catchStmt = new CatchStmt(condition, bodyBlock, lineFile);
                                tryStmt = (TryStmt) builder.getLastAddedNode();
                                tryStmt.addCatch(catchStmt);
                                break;
                            case "finally":
                                bodyBlock = parseBlock((CollectiveElement) parent.get(index++));
                                tryStmt = (TryStmt) builder.getLastAddedNode();
                                tryStmt.setFinallyBlock(bodyBlock);
                                break;
                            case "import":
                                String path = ((IdToken) ((AtomicElement) parent.get(index++)).atom).getIdentifier();
                                String importName = ((IdToken) ((AtomicElement) parent.get(index++)).atom).getIdentifier();
                                ImportStmt importStmt = new ImportStmt(path, importName, lineFile);
                                builder.addNode(importStmt);
                                break;
                            case "namespace":
                                String namespace = ((IdToken) ((AtomicElement) parent.get(index++)).atom).getIdentifier();
                                builder.addNode(new NamespaceNode(namespace, lineFile));
                                break;
                            default:
                                if (varLevel == Declaration.VAR) {
                                    builder.addNode(new Declaration(Declaration.VAR, identifier, lineFile));
                                } else if (varLevel == Declaration.CONST) {
                                    builder.addNode(new Declaration(Declaration.CONST, identifier, lineFile));
                                } else {
                                    builder.addNode(new NameNode(identifier, lineFile));
                                }
                                break;
                        }
                    }
                } else if (token instanceof IntToken) {
                    builder.addInt(((IntToken) token).getValue(), lineFile);
                } else if (token instanceof FloatToken) {
                    builder.addFloat(((FloatToken) token).getValue(), lineFile);
                } else if (token instanceof StrToken) {
                    builder.addString(((StrToken) token).getLiteral().toCharArray(), lineFile);
                } else if (token instanceof CharToken) {
                    builder.addChar(((CharToken) token).getValue(), lineFile);
                } else {
                    throw new ParseError("Unexpected token type. ", lineFile);
                }
            } catch (ClassCastException cce) {
                throw new SyntaxError("Syntax error, caused by " + cce, lineFile);
            }
        } else if (ele instanceof BracketList) {
            BracketList bracketList = (BracketList) ele;
            LineFile lineFile = bracketList.lineFile;
            if (index > 1) {
                Element probCallObj = parent.get(index - 2);
                if (probCallObj instanceof AtomicElement && isCall(((AtomicElement) probCallObj).atom)) {

                    // is a call to an identifier
                    Line argLine = parseOneLineBlock(bracketList);
                    Node callObj = builder.removeLast();
                    FuncCall call = new FuncCall(callObj,
                            new Arguments(argLine, lineFile),
                            lineFile);
                    builder.addNode(call);
                    return index;
                } else if (probCallObj instanceof BracketList || probCallObj instanceof SqrBracketList) {
                    Line argLine = parseOneLineBlock(bracketList);
                    Node callObj = builder.removeLast();
                    FuncCall call = new FuncCall(callObj,
                            new Arguments(argLine, lineFile),
                            lineFile);
                    builder.addNode(call);
                    return index;
                }
            }
            Node node = parseParenthesis(bracketList);
            builder.addNode(node);
        } else if (ele instanceof SqrBracketList) {
            SqrBracketList bracketList = (SqrBracketList) ele;
            LineFile lineFile = bracketList.lineFile;
            if (index > 1) {
                Element probCallObj = parent.get(index - 2);
                if (probCallObj instanceof AtomicElement && isCall(((AtomicElement) probCallObj).atom)) {
                    // is an indexing to an identifier
                    Line argLine = parseSqrBracket(bracketList);
                    Node callObj = builder.removeLast();
                    IndexingNode indexingNode = new IndexingNode(callObj, argLine, lineFile);
                    builder.addNode(indexingNode);
                    return index;
                } else if (probCallObj instanceof BracketList || probCallObj instanceof SqrBracketList) {
                    Line argLine = parseSqrBracket(bracketList);
                    Node callObj = builder.removeLast();
                    IndexingNode indexingNode = new IndexingNode(callObj, argLine, bracketList.lineFile);
                    builder.addNode(indexingNode);
                    return index;
                }
            }
            Line contentLine = parseSqrBracket((SqrBracketList) ele);
            Arguments arguments = new Arguments(contentLine, lineFile);
            ArrayLiteral arrayLiteral = new ArrayLiteral(arguments, lineFile);
            builder.addNode(arrayLiteral);
        }
        return index;
    }

    public BlockStmt parse() throws IOException {

        return parseBlock(rootList);
    }

    private static boolean notIdentifierOf(Element element, String expectedName) {
        return !identifierOf(element, expectedName);
    }

    public static boolean identifierOf(Element element, String expectedName) {
        return element instanceof AtomicElement &&
                ((AtomicElement) element).atom instanceof IdToken &&
                ((IdToken) ((AtomicElement) element).atom).getIdentifier().equals(expectedName);
    }

    private static boolean isCall(Token token) {
        if (token instanceof IdToken) {
            String identifier = ((IdToken) token).getIdentifier();
            return FileTokenizer.StringTypes.isIdentifier(identifier) &&
                    !FileTokenizer.RESERVED.contains(identifier);
        }
        return false;
    }

    private static boolean isCall(Token token, Node lastAddedNode) {
        if (token instanceof IdToken) {
            String identifier = ((IdToken) token).getIdentifier();
            if (FileTokenizer.StringTypes.isIdentifier(identifier) &&
                    !FileTokenizer.RESERVED.contains(identifier))
                return true;
            else return identifier.equals(")") || identifier.equals("]") ||
                    (!(lastAddedNode instanceof BinaryOperator) && identifier.equals(">"));
        }
        return false;
    }

    private static boolean isUnary(Element element) {
        if (element instanceof AtomicElement) {
            Token token = ((AtomicElement) element).atom;
            if (token instanceof IdToken) {
                String identifier = ((IdToken) token).getIdentifier();
                return switch (identifier) {
                    case ";", "=", "->", ".", "," -> true;
                    default -> FileTokenizer.ALL_BINARY.contains(identifier) ||
                            FileTokenizer.RESERVED.contains(identifier);
                };
            } else return !(token instanceof IntToken) &&
                    !(token instanceof FloatToken) &&
                    !(token instanceof CharToken);
        } else return !(element instanceof BracketList);
    }

    private static boolean isUnary(Token token) {
        if (token instanceof IdToken) {
            String identifier = ((IdToken) token).getIdentifier();
            return switch (identifier) {
                case ";", "=", "->", "(", "[", "{", "}", ".", "," -> true;
                default -> FileTokenizer.ALL_BINARY.contains(identifier) ||
                        FileTokenizer.RESERVED.contains(identifier);
            };
        } else return !(token instanceof IntToken) && !(token instanceof FloatToken);
    }

    private static boolean isFuncType(Token token) {
        if (token instanceof IdToken) {
            String identifier = ((IdToken) token).getIdentifier();
            if (identifier.equals(")")) return true;
            else if (identifier.equals(":")) return true;
            else return false;
        } else {
            return false;
        }
    }

    private static ContractNode autoContract(String fnName,
                                             Line paramBlock,
                                             Expression rtnContract,
                                             LineFile lineFile) {
        boolean hasParamContract = false;
        Line contractLine = new Line(lineFile);
        for (int i = 0; i < paramBlock.size(); i++) {
            Node param = paramBlock.get(i);
            if (param instanceof BinaryExpr) {
                BinaryExpr be = (BinaryExpr) param;
                if (be instanceof TypeExpr) {
                    hasParamContract = true;
                    contractLine.add(be.getRight());
                    paramBlock.set(i, be.getLeft());  // replace the contract
                    continue;
                } else if (be instanceof Assignment && be.getLeft() instanceof TypeExpr) {
                    hasParamContract = true;
                    TypeExpr te = (TypeExpr) be.getLeft();
                    contractLine.add(te.getRight());
                    be.setLeft(te.getLeft());
                    continue;
                }
            }
            contractLine.add(new NameNode(Constants.ANY_TYPE, lineFile));
        }

        if (rtnContract != null || hasParamContract) {
            if (rtnContract == null) rtnContract = new NameNode(Constants.ANY_TYPE, lineFile);
            return new ContractNode(fnName, contractLine, rtnContract, lineFile);
        } else {
            return null;
        }
    }
}
