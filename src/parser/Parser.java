package parser;

import ast.*;
import lexer.*;
import lexer.treeList.*;
import util.LineFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class Parser {

    private final BraceList rootList;
    private final boolean importLang;

    /**
     * Imported file paths vs its content.
     * <p>
     * Since a same file should always have same content (unless they have different 'importLang'). This is used to
     * eliminate the duplicate tokenize-parse process.
     */
    private final Map<String, BlockStmt> importedPathsAndContents = new HashMap<>();

    private int varLevel = Declaration.USELESS;

    public Parser(TokenizeResult tokenizeResult, boolean importLang) {
        this.rootList = tokenizeResult.rootList;
        this.importLang = importLang;
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

    private AbstractExpression parseOnePartBlock(BracketList bracketList, LineFile lineFile) throws IOException {
        AstBuilder builder = parseSomeBlock(bracketList);
        builder.finishPart();
        Line line = builder.getLine();
        if (line.size() != 1) {
            throw new SyntaxError("Expected 1 part in line, got " + line.size() + ": " + line + ". ", lineFile);
        }
        return (AbstractExpression) line.get(0);
    }

    private Line parseSqrBracket(SqrBracketList sqrBracketList) throws IOException {
        AstBuilder builder = parseSomeBlock(sqrBracketList);
        builder.finishPart();
        return builder.getLine();
    }

    private AbstractExpression parseParenthesis(BracketList bracketList) throws IOException {
        return parseOnePartBlock(bracketList, LineFile.LF_INTERPRETER);
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
                            builder.addBinaryOperator("-", BinaryOperator.NUMERIC, lineFile);
                        }
                    } else if (identifier.equals("*")) {
                        // special case, since "*" can both binary (multiplication) or unary (star)
                        if (index < 2 || isUnary(parent.get(index - 2))) {
                            // star
                            builder.addNode(new StarExpr(lineFile));
                        } else {
                            // multiplication
                            builder.addBinaryOperator("*", BinaryOperator.NUMERIC, lineFile);
                        }
                    }  else if (identifier.equals("is")) {
                        Element next = parent.get(index);
                        if (notIdentifierOf(next, "not")) {
                            builder.addBinaryOperator("is", BinaryOperator.LOGICAL, lineFile);
                        } else {
                            index++;
                            builder.addBinaryOperator("is not", BinaryOperator.LOGICAL, lineFile);
                        }
                    } else if (FileTokenizer.LOGICAL_UNARY.contains(identifier)) {
                        builder.addUnaryOperator(identifier, RegularUnaryOperator.LOGICAL, lineFile);
                    } else if (FileTokenizer.NUMERIC_BINARY.contains(identifier)) {
                        builder.addBinaryOperator(identifier, BinaryOperator.NUMERIC, lineFile);
                    } else if (FileTokenizer.NUMERIC_BINARY_ASSIGN.contains(identifier)) {
                        builder.addBinaryOperatorAssign(identifier, lineFile);
                    } else if (FileTokenizer.LOGICAL_BINARY.contains(identifier)) {
                        builder.addBinaryOperator(identifier, BinaryOperator.LOGICAL, lineFile);
                    } else if (FileTokenizer.LAZY_BINARY.contains(identifier)) {
                        builder.addBinaryOperator(identifier, BinaryOperator.LAZY, lineFile);
                    } else if (FileTokenizer.FAKE_TERNARY.contains(identifier)) {
                        builder.addFakeTernary(identifier, lineFile);
                    } else {

                        // These are for convenience
                        IdToken nameToken;
                        Element next;
                        BraceList bodyList;
                        BlockStmt bodyBlock;
                        BracketList conditionList;
                        AbstractExpression condition;
                        BracketList singleBodyList;

                        switch (identifier) {
                            case "=":
                                varLevel = Declaration.USELESS;
                                builder.addNode(new Assignment(lineFile));
                                break;
                            case ":=":
                                varLevel = Declaration.USELESS;
                                builder.addNode(new QuickAssignment(lineFile));
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
                                builder.addNode(new NullStmt(lineFile));
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
                                bodyList = (BraceList) parent.get(index++);

                                Line paramBlock = parseOneLineBlock(paramList);
                                bodyBlock = parseBlock(bodyList);

                                FuncDefinition def = new FuncDefinition(name, paramBlock, bodyBlock, lineFile);
                                builder.addNode(def);

                                break;
                            case "lambda":
                                paramList = new BracketList(null, lineFile);
                                while (notIdentifierOf(next = parent.get(index++), "->")) {
                                    paramList.add(next);
                                }
                                singleBodyList = new BracketList(null, lineFile);
                                while (index < parent.size() &&
                                        notIdentifierOf(next = parent.get(index++), ";")) {
                                    singleBodyList.add(next);
                                }
                                paramBlock = parseOneLineBlock(paramList);
                                AbstractExpression bodyNode = parseOnePartBlock(singleBodyList, lineFile);
                                LambdaExpressinDef lambdaFunctionDef =
                                        new LambdaExpressinDef(paramBlock, bodyNode, lineFile);
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
                                Node rtnCon = rtnLine.getChildren().get(0);

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
//                            case "as":
//                                CastExpr castExpr = new CastExpr(lineFile);
//                                builder.addNode(castExpr);
//                                break;
                            case "return":
                                builder.addNode(new ReturnStmt(lineFile));
                                break;
                            case "new":
                                builder.addNode(new NewStmt(lineFile));
                                break;
                            case "if":
                                conditionList = new BracketList(null, lineFile);
                                Node lastAddedNode = builder.getLastAddedNode();
                                if (!(lastAddedNode instanceof AbstractExpression)) {  // regular if-stmt
                                    while (!((next = parent.get(index++)) instanceof BraceList)) {
                                        conditionList.add(next);
                                    }
                                    bodyList = (BraceList) next;
                                    condition = parseOnePartBlock(conditionList, lineFile);
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
                                    condition = parseOnePartBlock(conditionList, lineFile);
                                    AbstractExpression elseBodyExpr = parseOnePartBlock(singleBodyList, lineFile);
                                    ConditionalExpr elseExpr =
                                            new ConditionalExpr("_else_", lineFile);
                                    ConditionalExpr ifExpr = new ConditionalExpr("_if_", lineFile);

                                    builder.addNode(ifExpr);
                                    builder.addNode(condition);
                                    builder.addNode(elseExpr);
                                    builder.addNode(elseBodyExpr);
                                }
                                break;
                            case "cond":
                                bodyList = (BraceList) parent.get(index++);
                                bodyBlock = parseBlock(bodyList);
                                CondCaseStmt ccs = new CondCaseStmt(bodyBlock, lineFile);
                                builder.addNode(ccs);
                                break;
                            case "case":
                                conditionList = new BracketList(null, lineFile);
                                while (!((next = parent.get(index++)) instanceof BraceList)) {
                                    conditionList.add(next);
                                }
                                bodyList = (BraceList) next;
                                condition = parseOnePartBlock(conditionList, lineFile);
                                bodyBlock = parseBlock(bodyList);
                                CaseStmt caseStmt = new CaseStmt(condition, bodyBlock, lineFile);
                                builder.addNode(caseStmt);
                                break;
                            case "default":
                                bodyList = (BraceList) parent.get(index++);
                                bodyBlock = parseBlock(bodyList);
                                caseStmt = new CaseStmt(null, bodyBlock, lineFile);
                                builder.addNode(caseStmt);
                                break;
                            case "fallthrough":
                                builder.addNode(new FallthroughStmt(lineFile));
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
                            case "import":
                                AtomicElement probNamespaceEle = (AtomicElement) parent.get(index++);
                                AtomicElement importEle;
                                boolean nameSpace = false;
                                if (probNamespaceEle.atom instanceof IdToken) {
                                    String probNs = ((IdToken) probNamespaceEle.atom).getIdentifier();
                                    if (probNs.equals("namespace")) {
                                        importEle = (AtomicElement) parent.get(index++);
                                        nameSpace = true;
                                    } else {
                                        importEle = probNamespaceEle;
                                    }
                                } else {
                                    importEle = probNamespaceEle;
                                }

                                String path, importName;
                                // This step can be optimized, but does not due to simplicity.
                                if (importEle.atom instanceof IdToken) {
                                    // Library import
                                    importName = ((IdToken) importEle.atom).getIdentifier();
                                    path = "lib" + File.separator + importName + ".sp";
                                } else if (importEle.atom instanceof StrToken) {
                                    // User file import
                                    path = lineFile.getFile().getParentFile().getAbsolutePath() +
                                            File.separator +
                                            ((StrToken) importEle.atom).getLiteral();
                                    importName = nameOfPath(path);
                                } else {
                                    throw new SyntaxError("Import name must either be a name or a String.",
                                            lineFile);
                                }

                                File fileImporting = new File(path);
//                                System.out.println(fileImporting.getAbsolutePath() + " and " + lineFile.getFile().getAbsolutePath());
                                if (fileImporting.equals(lineFile.getFile())) {
                                    break;  // self importing, do not import
                                }

                                if (index + 2 < parent.size()) {
                                    next = parent.get(index);
                                    if (next instanceof AtomicElement &&
                                            ((AtomicElement) next).atom instanceof IdToken &&
                                            ((IdToken) ((AtomicElement) next).atom).getIdentifier().equals("as")) {
                                        AtomicElement customNameEle = (AtomicElement) parent.get(index + 1);
                                        index += 2;
                                        importName = ((IdToken) customNameEle.atom).getIdentifier();
                                    }
                                }

                                BlockStmt rootBlock = importedPathsAndContents.get(path);
                                if (rootBlock == null) {
                                    FileTokenizer fileTokenizer =
                                            new FileTokenizer(fileImporting, false, importLang);
                                    Parser fileParser =
                                            new Parser(new TokenizeResult(fileTokenizer.tokenize()), importLang);
                                    rootBlock = fileParser.parse();
                                    importedPathsAndContents.put(path, rootBlock);
                                }

                                ImportStmt importStmt = new ImportStmt(importName, fileImporting, rootBlock, lineFile);
                                builder.addNode(importStmt);
                                if (nameSpace) {
                                    builder.addNode(new NamespaceNode(importName, lineFile));
                                }
                                break;
                            default:
                                if (varLevel == Declaration.VAR) {
                                    builder.addNode(new Declaration(Declaration.VAR, identifier, lineFile));
                                } else if (varLevel == Declaration.CONST) {
                                    builder.addNode(new Declaration(Declaration.CONST, identifier, lineFile));
                                } else {
                                    builder.addNode(new NameNode(identifier, lineFile));
//                                builder.addName(identifier, lineFile);
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
        return !((element instanceof AtomicElement) &&
                (((AtomicElement) element).atom instanceof IdToken) &&
                ((IdToken) ((AtomicElement) element).atom).getIdentifier().equals(expectedName));
    }

    private static String nameOfPath(String path) {
        path = path.replace("/", File.separator);
        path = path.replace("\\", File.separator);
        if (path.endsWith(".sp")) path = path.substring(0, path.length() - 3);
        if (path.contains(File.separator)) {
            return path.substring(path.lastIndexOf(File.separator) + 1);
        } else {
            return path;
        }
    }

//    public BlockStmt parse() {
//
//        AstBuilder builder = new AstBuilder();
//
//        int bracketCount = 0;
//        int braceCount = 0;
//        int sqrBracketCount = 0;
//        int angleBracketCount = 0;
//
//        int varLevel = Declaration.VAR;
//
//        boolean fnParams = false;
//        boolean lambdaParams = false;
//        boolean fnRType = false;
//        boolean importingModule = false;
//        boolean classHeader = false;
//        boolean implementing = false;
//        boolean extending = false;
//        boolean conditioning = false;
//        boolean isElse = false;
//        boolean isAbstract = false;
//
//        Stack<Integer> paramBrackets = new Stack<>();
//        Stack<Integer> lambdaParamBrackets = new Stack<>();
//        Stack<Integer> callBrackets = new Stack<>();
//        Stack<Integer> funcBodyBraces = new Stack<>();
//        Stack<Integer> moduleBraces = new Stack<>();
//        Stack<Integer> classBraces = new Stack<>();
//        Stack<Integer> condBraces = new Stack<>();
//        Stack<Integer> funcTypeSqrBrackets = new Stack<>();
//        Stack<Integer> angleBrackets = new Stack<>();
//        Stack<Integer> condSwitchBraces = new Stack<>();
//        Stack<Integer> caseBraces = new Stack<>();
//        Stack<Integer> defaultBraces = new Stack<>();
//        Stack<Integer> arraySqrBrackets = new Stack<>();
//
//        for (int i = 0; i < tokens.size(); ++i) {
//            Token token = tokens.get(i);
//            LineFile lineFile = token.getLineFile();
//
//            if (token instanceof IdToken) {
//                String identifier = ((IdToken) token).getIdentifier();
//
//                if (identifier.equals("-")) {
//                    // special case, since "-" can both binary (subtraction) or unary (negation)
//                    if (i > 0 && isUnary(tokens.get(i - 1))) {
//                        // negation
//                        builder.addUnaryOperator("neg", RegularUnaryOperator.NUMERIC, lineFile);
//                    } else {
//                        // subtraction
//                        builder.addBinaryOperator("-", BinaryOperator.NUMERIC, lineFile);
//                    }
//                } else if (identifier.equals("<")) {
//                    if (hasCloseAngleBracket(i + 1)) {
//                        angleBracketCount++;
//                        angleBrackets.add(angleBracketCount);
//                        builder.addAngleBracketBlock();
//                    } else {
//                        builder.addBinaryOperator("<", BinaryOperator.LOGICAL, lineFile);
//                    }
//                } else if (identifier.equals(">")) {
//                    if (isThisStack(angleBrackets, angleBracketCount)) {
//                        angleBrackets.pop();
//                        builder.buildTemplateAndAdd(lineFile);
//                        angleBracketCount--;
//                    } else {
//                        builder.addBinaryOperator(">", BinaryOperator.LOGICAL, lineFile);
//                    }
//                } else if (FileTokenizer.LOGICAL_UNARY.contains(identifier)) {
//                    builder.addUnaryOperator(identifier, RegularUnaryOperator.LOGICAL, lineFile);
//                } else if (FileTokenizer.NUMERIC_BINARY.contains(identifier)) {
//                    builder.addBinaryOperator(identifier, BinaryOperator.NUMERIC, lineFile);
//                } else if (FileTokenizer.NUMERIC_BINARY_ASSIGN.contains(identifier)) {
//                    builder.addBinaryOperatorAssign(identifier, lineFile);
//                } else if (FileTokenizer.LOGICAL_BINARY.contains(identifier)) {
//                    builder.addBinaryOperator(identifier, BinaryOperator.LOGICAL, lineFile);
//                } else if (FileTokenizer.LAZY_BINARY.contains(identifier)) {
//                    builder.addBinaryOperator(identifier, BinaryOperator.LAZY, lineFile);
//                } else if (FileTokenizer.FAKE_TERNARY.contains(identifier)) {
//                    builder.addFakeTernary(identifier, lineFile);
//                } else {
//                    boolean isInterface = false;
//                    switch (identifier) {
//                        case "(":
//                            bracketCount++;
//                            if (fnParams) {  // declaring function
//                                fnParams = false;
//                                paramBrackets.push(bracketCount);
//                                builder.addParameterBracket();
//                            } else if (lambdaParams) {
//                                lambdaParams = false;
//                                lambdaParamBrackets.push(bracketCount);
//                                builder.addParameterBracket();
//                            } else if (isCall(tokens.get(i - 1), builder.getLastAddedNode())) {
//                                builder.addCall(lineFile);
//                                callBrackets.push(bracketCount);
//                            } else {
//                                builder.addParenthesis();
//                            }
//                            break;
//                        case ")":
//                            if (isThisStack(paramBrackets, bracketCount)) {
//                                paramBrackets.pop();
//                                builder.buildParameterBracket();
//                                fnRType = true;
//                            } else if (isThisStack(lambdaParamBrackets, bracketCount)) {
//                                lambdaParamBrackets.pop();
//                                builder.buildParameterBracket();
//                            } else if (isThisStack(callBrackets, bracketCount)) {
//                                callBrackets.pop();
//                                builder.buildCall(lineFile);
//                            } else {
//                                builder.buildParenthesis(lineFile);
//                            }
//                            bracketCount--;
//                            break;
//                        case "{":
//                            braceCount++;
//                            if (fnRType) {
//                                fnRType = false;
//                                builder.addFnRType(lineFile);
//                                builder.addBraceBlock();
//                                funcBodyBraces.push(braceCount);
//                            } else if (importingModule) {
//                                importingModule = false;
//                                builder.addBraceBlock();
//                                moduleBraces.push(braceCount);
//                            } else if (classHeader) {
//                                classHeader = false;
//                                if (extending) {
//                                    assert !implementing;  // keyword "implements" should already finished extends
//                                    extending = false;
//                                    builder.finishExtend(lineFile);
//                                }
//                                if (implementing) {
//                                    implementing = false;
//                                    builder.finishImplements();
//                                }
//                                builder.addBraceBlock();
//                                classBraces.push(braceCount);
//                            } else if (conditioning) {
//                                conditioning = false;
//                                builder.buildConditionTitle();
//                                builder.addBraceBlock();
//                                condBraces.push(braceCount);
//                            } else if (isElse) {
//                                isElse = false;
//                                builder.addBraceBlock();
//                            } else {
//                                builder.addIndependenceBraceBlock();
//                            }
//                            break;
//                        case "}":
//                            if (isThisStack(funcBodyBraces, braceCount)) {
//                                funcBodyBraces.pop();
//                                builder.buildBraceBlock();
//                                builder.finishFunction(lineFile);
//                                builder.finishFunctionOuterBlock();
//                            } else if (isThisStack(moduleBraces, braceCount)) {
//                                moduleBraces.pop();
//                                builder.buildBraceBlock();
//                                builder.buildImportModule(lineFile);
//                            } else if (isThisStack(classBraces, braceCount)) {
//                                classBraces.pop();
//                                builder.buildBraceBlock();
//                                builder.buildClass(lineFile);
//                            } else if (isThisStack(condBraces, braceCount)) {
//                                condBraces.pop();
//                                builder.buildBraceBlock();
//                                builder.buildConditionBody();
//                            } else if (isThisStack(condSwitchBraces, braceCount)) {
//                                condSwitchBraces.pop();
//                                builder.buildBraceBlock();
//                                builder.buildCondStmt(lineFile);
//                            } else if (isThisStack(defaultBraces, braceCount)) {
//                                defaultBraces.pop();
//                                builder.buildBraceBlock();
//                                builder.buildDefault(lineFile);
//                            } else {
//                                builder.buildBraceBlock();
//                            }
//                            if (i < tokens.size() - 1) {
//                                Token nextTk = tokens.get(i + 1);
//                                if (!(nextTk instanceof IdToken) ||
//                                        !((IdToken) nextTk).getIdentifier().equals("else")) {
//                                    builder.finishLine();  // fill the end line terminator
//                                }
//                            }
//
//                            braceCount--;
//                            break;
//                        case "[":
//                            sqrBracketCount++;
//
//                            if (i > 0 && isFuncType(tokens.get(i - 1))) {
//                                funcTypeSqrBrackets.push(sqrBracketCount);
//                                builder.addSqrBracketBlock();
//                            } else {
//                                builder.addIndexing(lineFile);
//                            }
//                            break;
//                        case "]":
//
//                            if (isThisStack(funcTypeSqrBrackets, sqrBracketCount)) {
//                                funcTypeSqrBrackets.pop();
//                                builder.finishSqrBracketBlock();
//                            } else if (isThisStack(arraySqrBrackets, sqrBracketCount)) {
//                                arraySqrBrackets.pop();
//                                builder.buildArrayLiteral(lineFile);
//                            } else {
//                                builder.buildIndexing();
//                            }
//                            sqrBracketCount--;
//                            break;
//                        case "\\[":
//                            sqrBracketCount++;
//
//                            arraySqrBrackets.push(sqrBracketCount);
//                            builder.addArrayLiteral(lineFile);
//                            builder.addIndependenceBraceBlock();
//
//                            break;
//                        case ".":
//                            builder.addDot(lineFile);
//                            break;
//                        case ":":
//                            builder.addDeclaration(varLevel, lineFile);
//                            break;
//                        case "=":
//                            builder.addAssignment(lineFile);
//                            break;
//                        case ":=":
//                            builder.addQuickAssignment(lineFile);
//                            break;
//                        case "->":
//                            builder.addFuncTypeNode(lineFile);
//                            break;
//                        case "<-":
//                            builder.addAnonymousClass(lineFile);
//                            break;
//                        case "++":
//                            builder.addIncDecOperator(true, lineFile);
//                            break;
//                        case "--":
//                            builder.addIncDecOperator(false, lineFile);
//                            break;
//                        case "true":
//                            builder.addBoolean(true, lineFile);
//                            break;
//                        case "false":
//                            builder.addBoolean(false, lineFile);
//                            break;
//                        case "const":
//                            varLevel = Declaration.CONST;
//                            break;
//                        case "if":
//                            conditioning = true;
//                            builder.addIf(lineFile);
//                            break;
//                        case "else":
//                            builder.addElse(lineFile);
//                            isElse = true;
//                            break;
//                        case "cond":
//                            i++;
//                            Token nextToken = tokens.get(i);
//                            if (nextToken instanceof IdToken && ((IdToken) nextToken).getIdentifier().equals("{")) {
//                                condSwitchBraces.push(++braceCount);
//                                builder.addCondStmt(lineFile);
//                                builder.addBraceBlock();
//                                break;
//                            } else {
//                                throw new SyntaxError(
//                                        "Statement 'cond' must followed by '{' immediately. ", lineFile);
//                            }
//                        case "switch":
//                        case "case":
//                            conditioning = true;
//                            builder.addCase(lineFile);
//                            break;
//                        case "default":
//                            i++;
//                            Token nextTk = tokens.get(i);
//                            if (nextTk instanceof IdToken && ((IdToken) nextTk).getIdentifier().equals("{")) {
//                                defaultBraces.push(++braceCount);
//                                builder.addDefault(lineFile);
//                                builder.addBraceBlock();
//                                break;
//                            } else {
//                                throw new SyntaxError(
//                                        "Statement 'default' must followed by '{' immediately. ", lineFile);
//                            }
//                        case "fallthrough":
//                            builder.addFallthrough(lineFile);
//                            break;
//                        case "as":
//                            builder.addCast(lineFile);
//                            break;
//                        case "instanceof":
//                            builder.addInstanceof(lineFile);
//                            break;
//                        case "while":
//                            conditioning = true;
//                            builder.addWhile(lineFile);
//                            break;
//                        case "for":
//                            conditioning = true;
//                            builder.addFor(lineFile);
//                            break;
//                        case "break":
//                            builder.addBreak(lineFile);
//                            break;
//                        case "continue":
//                            builder.addContinue(lineFile);
//                            break;
//                        case "lambda":
//                            builder.addLambdaHeader(lineFile);
//                            lambdaParams = true;
//                            break;
//                        case "fn":
//                            Token fnNameTk = tokens.get(i + 1);
//                            if (!(fnNameTk instanceof IdToken))
//                                throw new ParseError("Function must have either a name or a header", lineFile);
//                            String fnName = ((IdToken) fnNameTk).getIdentifier();
//                            if (!fnName.equals("(")) {  // a function with name
//                                i++;
//                            } else {
//                                fnName = null;
//                            }
//                            builder.addBraceBlock();  // give the function a whole block
//                            builder.addFunction(fnName, isAbstract, lineFile);
//                            isAbstract = false;
//                            fnParams = true;
//                            break;
//                        case "interface":
//                            isInterface = true;
//                            if (isAbstract) throw new SyntaxError("Illegal combination 'abstract interface'. ",
//                                    lineFile);
//                        case "class":
//                            Token classNameToken = tokens.get(i + 1);
//                            i += 1;
//                            if (!(classNameToken instanceof IdToken)) {
//                                throw new ParseError("Class must have a name. ", lineFile);
//                            }
//                            String className = ((IdToken) classNameToken).getIdentifier();
//                            classHeader = true;
//                            builder.addClass(className, isInterface, isAbstract, lineFile);
//                            isAbstract = false;
//                            break;
//                        case "extends":
//                            builder.addExtend(lineFile);
//                            extending = true;
//                            break;
//                        case "implements":
//                            if (extending) {
//                                extending = false;
//                                builder.finishExtend(lineFile);
//                            }
//                            builder.addImplements();
//                            implementing = true;
//                            break;
//                        case "abstract":
//                            isAbstract = true;
//                            break;
//                        case "import":
//                            String importName = ((IdToken) tokens.get(i + 1)).getIdentifier();
//                            builder.addImportModule(importName, lineFile);
//                            importingModule = true;
//                            i += 1;
//                            break;
//                        case "return":
//                            builder.addReturnStmt(lineFile);
//                            break;
//                        case "new":
//                            builder.addNewStmt(lineFile);
//                            break;
//                        case ",":
//                            builder.finishPart();
//                            break;
//                        case ";":
//                            if (isAbstract) {
//                                throw new SyntaxError("Unexpected token 'abstract'. ", lineFile);
//                            }
//
//                            builder.finishPart();
//                            if (fnRType) {
//                                fnRType = false;
//                                builder.addFnRType(lineFile);
//                                builder.finishAbstractFunction(lineFile);
//                                builder.finishFunctionOuterBlock();
//                            }
//
//                            builder.finishLine();
//
//                            varLevel = Declaration.VAR;  // restore the var level
//                            break;
//                        case "int":
//                        case "float":
//                        case "char":
//                        case "boolean":
//                        case "void":
//                            builder.addPrimitiveTypeName(identifier, lineFile);
//                            break;
////                        case "any":
////                            builder.addAnyStmt(lineFile);
////                            break;
//                        case "namespace":
//                            builder.addNamespace(lineFile);
//                            break;
//                        case "null":
//                            builder.addNull(lineFile);
//                            break;
//                        default:  // name
//                            builder.addName(identifier, lineFile);
//                            break;
//                    }
//                }
//            } else if (token instanceof IntToken) {
//                builder.addInt(((IntToken) token).getValue(), lineFile);
//            } else if (token instanceof FloatToken) {
//                builder.addFloat(((FloatToken) token).getValue(), lineFile);
//            } else if (token instanceof StrToken) {
//                builder.addString(((StrToken) token).getLiteral().toCharArray(), lineFile);
//            } else if (token instanceof CharToken) {
//                builder.addChar(((CharToken) token).getValue(), lineFile);
//            } else {
//                throw new ParseError("Unexpected token type. ", lineFile);
//            }
//        }
//
//        return builder.getBaseBlock();
//    }

//    private boolean hasCloseAngleBracket(int probFrontBracketIndex) {
//        int count = 1;
//        for (int i = probFrontBracketIndex + 1; i < tokens.size(); ++i) {
//            Token tk = tokens.get(i);
//            if (tk instanceof IdToken) {
//                String identifier = ((IdToken) tk).getIdentifier();
//                if (identifier.equals(">")) {
//                    count--;
//                    if (count == 0) return true;
//                } else if (identifier.equals("<")) {
//                    count++;
//                } else if (FileTokenizer.ALL_BINARY.contains(identifier) ||
//                        FileTokenizer.RESERVED.contains(identifier) ||
//                        FileTokenizer.OTHERS.contains(identifier))
//                    return false;
//            } else {
//                return false;
//            }
//        }
//        return false;
//    }

    private static boolean isThisStack(Stack<Integer> stack, int value) {
        if (stack.empty()) return false;
        else return stack.peek() == value;
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
                switch (identifier) {
                    case ";":
                    case "=":
                    case "->":
                    case ".":
                    case ",":
                        return true;
                    default:
                        return FileTokenizer.ALL_BINARY.contains(identifier) ||
                                FileTokenizer.RESERVED.contains(identifier);
                }
            } else return !(token instanceof IntToken) && !(token instanceof FloatToken);
        } else return !(element instanceof BracketList);
    }

    private static boolean isUnary(Token token) {
        if (token instanceof IdToken) {
            String identifier = ((IdToken) token).getIdentifier();
            switch (identifier) {
                case ";":
                case "=":
                case "->":
                case "(":
                case "[":
                case "{":
                case "}":
                case ".":
                case ",":
                    return true;
                default:
                    return FileTokenizer.ALL_BINARY.contains(identifier) ||
                            FileTokenizer.RESERVED.contains(identifier);
            }
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
}
