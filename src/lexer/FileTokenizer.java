package lexer;

import lexer.treeList.*;
import util.LineFile;
import util.Utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FileTokenizer {

    public static final Set<String> NUMERIC_BINARY = Set.of(
            "+", "-", "*", "/", "%"
    );

    public static final Set<String> BITWISE_BINARY = Set.of(
            ">>", ">>>", "<<", "&", "|", "^"
    );

    public static final Set<String> NUMERIC_BINARY_ASSIGN = Set.of(
            "+=", "-=", "*=", "/=", "%="
    );

    public static final Set<String> BITWISE_BINARY_ASSIGN = Set.of(
            ">>=", ">>>=", "<<=", "&=", "|=", "^="
    );

    public static final Set<String> LOGICAL_BINARY = Set.of(
            ">", "<", "==", "!=", ">=", "<=", "is", "is not"
    );

    public static final Set<String> FAKE_TERNARY = Set.of(
            "?"
    );

    public static final Set<String> LAZY_BINARY = Set.of(
            "and", "or"
    );

    public static final Set<String> LOGICAL_UNARY = Set.of(
            "not"
    );

    public static final Set<String> SYMBOLS = Set.of(
            "{", "}", "[", "]", "(", ")", ".", ",", ";", ":"
    );

    public static final Set<String> OTHERS = Set.of(
            "=", "->", "<-", ":=", "++", "--"
    );

    public static final Set<String> EXTRA_IDENTIFIERS = Utilities.mergeSets(
            NUMERIC_BINARY,
            BITWISE_BINARY,
            NUMERIC_BINARY_ASSIGN,
            BITWISE_BINARY_ASSIGN,
            LOGICAL_BINARY,
            LAZY_BINARY,
            LOGICAL_UNARY,
            SYMBOLS,
            FAKE_TERNARY,
            OTHERS
    );

    public static final Set<String> ALL_BINARY = Utilities.mergeSets(
            NUMERIC_BINARY,
            BITWISE_BINARY,
            NUMERIC_BINARY_ASSIGN,
            BITWISE_BINARY_ASSIGN,
            LOGICAL_BINARY,
            LAZY_BINARY
    );

    public static final Set<String> RESERVED = Set.of(
            "class", "fn", "if", "else", "new", "return", "break",
            "continue", "true", "false", "null", "while", "for", "import", "namespace",
            "const", "var", "assert", "as", "super", "this", "lambda",
            "cond", "switch", "case", "default", "fallthrough", "in", "yield"
    );

    private static final String IMPORT_USAGE = "Usage of import: 'import \"path\"' or 'import \"path\" as <module>' " +
            "or 'import namespace \"path\"'";

    private final File srcFile;
    private boolean main;
    private final boolean importLang;
    private final List<Token> tokens = new ArrayList<>();

    private boolean inDoc = false;

    public FileTokenizer(File srcFile, boolean main, boolean importLang) {
        this.srcFile = srcFile;
        this.main = main;
        this.importLang = importLang;
    }

    public BraceList tokenize() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(srcFile));

        tokens.clear();

        if (importLang) {
            LineFile lineFile0 = new LineFile(0, srcFile);
            tokens.add(new IdToken("import", lineFile0));
            tokens.add(new IdToken("namespace", lineFile0));
            tokens.add(new IdToken("lang", lineFile0));
            tokens.add(new IdToken(";", lineFile0));
        }

        int lineNum = 1;
        String line;
        while ((line = br.readLine()) != null) {
            LineFile lineFile = new LineFile(lineNum, srcFile);
//            int index = tokens.size();
            proceedLine(line, lineFile);
//            findImport(index, tokens.size());

            lineNum++;
        }
        return makeTreeList(tokens);
    }

    private static BraceList makeTreeList(List<Token> tokenList) {
        BraceList root = new BraceList(null, LineFile.LF_TOKENIZER);
        CollectiveElement currentActive = root;
        for (int i = 0; i < tokenList.size(); ++i) {
            currentActive = makeTreeListRec(currentActive, tokenList, i);
        }
        return root;
    }

    private static CollectiveElement makeTreeListRec(CollectiveElement currentActive, List<Token> tokenList, int index) {
        Token tk = tokenList.get(index);
        if (tk instanceof IdToken) {
            String symbol = ((IdToken) tk).getIdentifier();
            switch (symbol) {
                case "(":
                    return new BracketList(currentActive, tk.getLineFile());
                case ")":
                    if (currentActive instanceof BracketList) {
                        currentActive.parentElement.add(currentActive);
                        return currentActive.parentElement;
                    } else {
                        throw new SyntaxError("')' must close a '(', not a '" + symbol + "'. ",
                                tk.getLineFile());
                    }
                case "[":
                    return new SqrBracketList(currentActive, tk.getLineFile());
                case "]":
                    if (currentActive instanceof SqrBracketList) {
                        currentActive.parentElement.add(currentActive);
                        return currentActive.parentElement;
                    } else {
                        throw new SyntaxError("']' must close a '[', not a '" + symbol + "'. ",
                                tk.getLineFile());
                    }
                case "{":
                    return new BraceList(currentActive, tk.getLineFile());
                case "}":
                    if (currentActive instanceof BraceList) {
                        currentActive.parentElement.add(currentActive);
                        return currentActive.parentElement;
                    } else {
                        throw new SyntaxError("'}' must close a '{', not a '" + symbol + "'. ",
                                tk.getLineFile());
                    }
                default:
                    currentActive.add(new AtomicElement(tk, currentActive));
                    return currentActive;
            }
        } else {
            currentActive.add(new AtomicElement(tk, currentActive));
            return currentActive;
        }
    }

    private void proceedLine(String line, LineFile lineFile) {
        boolean inStr = false;
        int len = line.length();
        StringBuilder stringLiteral = new StringBuilder();
        StringBuilder nonLiteral = new StringBuilder();
        for (int i = 0; i < len; ++i) {
            char ch = line.charAt(i);
            if (inDoc) {
                if (i < len - 1 && ch == '*' && line.charAt(i + 1) == '/') {
                    // exit doc
                    inDoc = false;
                    i += 2;
//                    continue;
                }
            } else {
                // not in doc
                if (inStr) {
                    // in string literal
                    if (ch == '"') {
                        inStr = false;
                        tokens.add(new StrToken(stringLiteral.toString(), lineFile));
                        stringLiteral.setLength(0);
                    } else {
                        stringLiteral.append(ch);
                    }
                } else {
                    // not in string literal
                    if (i < len - 1 && ch == '/' && line.charAt(i + 1) == '*') {
                        // enter doc
                        inDoc = true;
                        i += 1;
//                        continue;
                    } else if (i < len - 1 && ch == '/' && line.charAt(i + 1) == '/') {
                        // enter comment, end of this line
                        if (nonLiteral.length() > 2)
                            lineTokenize(nonLiteral.substring(0, nonLiteral.length() - 2), lineFile);
                        nonLiteral.setLength(0);
                        break;
                    } else if (ch == '"') {
                        // enter string literal
                        inStr = true;
                        lineTokenize(nonLiteral.toString(), lineFile);
                        nonLiteral.setLength(0);
                    } else if (ch == '\'') {
                        // enter char literal
                        if (i < len - 2 && line.charAt(i + 2) == '\'') {
                            lineTokenize(nonLiteral.toString(), lineFile);
                            nonLiteral.setLength(0);
                            tokens.add(new CharToken(line.charAt(i + 1), lineFile));
                            i += 2;
                        } else {
                            throw new SyntaxError("Char literal must contain exactly one symbol. ", lineFile);
                        }
                    } else {
                        nonLiteral.append(ch);
                    }
                }
            }
        }
        if (nonLiteral.length() > 0) {
            lineTokenize(nonLiteral.toString(), lineFile);
        }
    }

    private void lineTokenize(String nonLiteral, LineFile lineFile) {
        List<String> list = normalize(nonLiteral);
        int len = list.size();
        for (int i = 0; i < len; ++i) {
            String s = list.get(i);
            if (StringTypes.isInteger(s)) {
                if (i < len - 2 && list.get(i + 1).equals(".") && StringTypes.isInteger(list.get(i + 2))) {
                    // is a float:   number.number
                    FloatToken floatToken = new FloatToken(s + "." + list.get(i + 2), lineFile);
                    tokens.add(floatToken);
                    i += 2;
                } else {
                    tokens.add(new IntToken(s, lineFile));
                }
            } else if (StringTypes.isIdentifier(s)) {
                tokens.add(new IdToken(s, lineFile));
            } else if (EXTRA_IDENTIFIERS.contains(s)) {
                tokens.add(new IdToken(s, lineFile));
            }
        }
    }

    private static List<String> normalize(String nonLiteral) {
        List<String> list = new ArrayList<>();
        if (!nonLiteral.isBlank()) {
            CharTypeIdentifier lastCti = new CharTypeIdentifier(nonLiteral.charAt(0));
            StringBuilder builder = new StringBuilder();
            builder.append(nonLiteral.charAt(0));
            for (int i = 1; i < nonLiteral.length(); ++i) {
                CharTypeIdentifier cti = new CharTypeIdentifier(nonLiteral.charAt(i));
                if (!CharTypeIdentifier.concatenateAble(lastCti, cti)) {
                    putString(list, builder.toString());
                    builder.setLength(0);
                }
                builder.append(cti.ch);
                lastCti = cti;
            }
            putString(list, builder.toString());
        }
        return list;
    }

    private static void putString(List<String> list, String string) {
        list.add(string);
    }

    private static class CharTypeIdentifier {

        private static final int DIGIT = 1;
        private static final int LETTER = 2;
        private static final int L_BRACE = 3;
        private static final int R_BRACE = 4;
        private static final int L_BRACKET = 5;
        private static final int R_BRACKET = 6;
        private static final int L_SQR_BRACKET = 7;
        private static final int R_SQR_BRACKET = 8;
        private static final int EOL = 9;
        private static final int NEW_LINE = 10;
        private static final int GT = 11;
        private static final int LT = 12;
        private static final int EQ = 13;
        private static final int AND = 14;
        private static final int OR = 15;
        private static final int XOR = 16;
        private static final int DOT = 17;
        private static final int COMMA = 18;
        private static final int UNDERSCORE = 19;
        private static final int NOT = 20;
        private static final int PLUS = 21;
        private static final int MINUS = 22;
        private static final int OTHER_ARITHMETIC = 23;
        private static final int TYPE = 24;
        private static final int ESCAPE = 25;
        private static final int QUESTION = 26;
        private static final int UNDEFINED = 0;

        private static final int[] SELF_CONCATENATE = {
                DIGIT, LETTER, GT, EQ, LT, AND, OR, UNDERSCORE, PLUS, MINUS
        };
        private static final int[][] CROSS_CONCATENATE = {
                {LETTER, UNDERSCORE},
                {UNDERSCORE, LETTER},
                {DIGIT, UNDERSCORE},
                {UNDERSCORE, DIGIT},
                {MINUS, GT},
                {LT, MINUS},
                {LETTER, DIGIT},
                {GT, EQ},
                {LT, EQ},
                {NOT, EQ},
                {PLUS, EQ},
                {MINUS, EQ},
                {OTHER_ARITHMETIC, EQ},
                {TYPE, EQ},
                {ESCAPE, L_SQR_BRACKET},
                {LETTER, QUESTION},
                {DIGIT, QUESTION}
        };

        private final char ch;
        private final int type;

        CharTypeIdentifier(char ch) {
            this.ch = ch;
            this.type = identify(ch);
        }

        static boolean concatenateAble(CharTypeIdentifier left, CharTypeIdentifier right) {
            int leftType = left.type;
            int rightType = right.type;
            return (leftType == rightType && Utilities.arrayContains(SELF_CONCATENATE, leftType)) ||
                    Utilities.arrayContains2D(CROSS_CONCATENATE, new int[]{leftType, rightType});
        }

        private static int identify(char ch) {
            if (Character.isDigit(ch)) return DIGIT;
            else if (Character.isAlphabetic(ch)) return LETTER;

            return switch (ch) {
                case '{' -> L_BRACE;
                case '}' -> R_BRACE;
                case '(' -> L_BRACKET;
                case ')' -> R_BRACKET;
                case '[' -> L_SQR_BRACKET;
                case ']' -> R_SQR_BRACKET;
                case ';' -> EOL;
                case '\n' -> NEW_LINE;
                case '>' -> GT;
                case '<' -> LT;
                case '=' -> EQ;
                case '&' -> AND;
                case '|' -> OR;
                case '^' -> XOR;
                case '.' -> DOT;
                case ',' -> COMMA;
                case '_' -> UNDERSCORE;
                case '!' -> NOT;
                case '+' -> PLUS;
                case '-' -> MINUS;
                case '*', '/', '%' -> OTHER_ARITHMETIC;
                case ':' -> TYPE;
                case '\\' -> ESCAPE;
                case '?' -> QUESTION;
                default -> UNDEFINED;
            };
        }
    }

    public static class StringTypes {

        private static boolean isInteger(String s) {
            for (char c : s.toCharArray()) {
                if (!(Character.isDigit(c) || c == '_')) return false;
            }
            return s.length() > 0 && s.charAt(0) != '_';
        }

        public static boolean isIdentifier(String s) {
            if (s.length() > 0) {
                char lead = s.charAt(0);
                if (!(Character.isAlphabetic(lead) || lead == '_')) return false;
                int len = s.length();
                for (int i = 1; i < len; ++i) {
                    char ch = s.charAt(i);
                    if (!(Character.isAlphabetic(ch) || Character.isDigit(ch) || ch == '_' || ch == '?'))
                        return false;
                }
                return true;
            }
            return false;
        }
    }
}
