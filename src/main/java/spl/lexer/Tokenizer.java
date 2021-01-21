package spl.lexer;

import spl.lexer.tokens.*;
import spl.lexer.treeList.*;
import spl.util.LineFilePos;
import spl.util.Utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class Tokenizer {

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
            "=", "->", "<-", ":=", "++", "--", "...", "$"
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
            "class", "fn", "if", "else", "new",
            "return", "break", "continue", "true", "false",
            "null", "while", "for", "import", "namespace",
            "const", "var", "assert", "as", "super",
            "this", "lambda", "cond", "switch", "case",
            "default", "fallthrough", "in", "yield"
    );

    public static final Set<String> KEYWORDS = Utilities.mergeSets(
            RESERVED, LAZY_BINARY, LOGICAL_UNARY
    );

    final List<Token> tokens = new ArrayList<>();
    boolean inDoc = false;
    StringBuilder docBuilder = new StringBuilder();
    LineFilePos docLfp;

    static CollectiveElement makeTreeListRec(CollectiveElement currentActive, List<Token> tokenList, int index) {
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
                case "<":
                    if (hasClosingArrowBracket(tokenList, index)) {
                        return new ArrowBracketList(currentActive, tk.getLineFile());
                    } else {
                        currentActive.add(new AtomicElement(tk, currentActive));  // less than
                        return currentActive;
                    }
                case ">":
                    if (currentActive instanceof ArrowBracketList) {
                        currentActive.parentElement.add(currentActive);
                        return currentActive.parentElement;
                    } else {
                        currentActive.add(new AtomicElement(tk, currentActive));  // greater than
                        return currentActive;
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

    private static boolean hasClosingArrowBracket(List<Token> tokenList, int leftArrIndex) {
        for (int i = leftArrIndex + 1; i < tokenList.size(); i++) {
            Token tk = tokenList.get(i);
            if (tk instanceof IdToken) {
                String symbol = ((IdToken) tk).getIdentifier();
                switch (symbol) {
                    case ">": return true;
                    case ";": return false;
                }
                if (RESERVED.contains(symbol)) return false;  // except 'and', 'or', 'not', so do not use KEYWORDS
            } else if (tk instanceof LiteralToken) return false;
        }
        return false;
    }

    void proceedLine(String line, LineFilePos.LineFile lineFile) {
        boolean inStr = false;
        int len = line.length();
        StringBuilder stringLiteral = new StringBuilder();
        StringBuilder nonLiteral = new StringBuilder();
        int partStartPos = 0;
        for (int i = 0; i < len; i++) {
            char ch = line.charAt(i);
            if (inDoc) {
                if (i < len - 1 && ch == '*' && line.charAt(i + 1) == '/') {
                    // exit doc
                    inDoc = false;
                    tokens.add(new DocToken(docBuilder.append("*/").toString(), docLfp));
                    docBuilder.setLength(0);
                    i += 2;
                    partStartPos = i;
                } else {
                    docBuilder.append(ch);
                }
            } else {
                // not in doc
                if (inStr) {
                    // in string literal
                    if (ch == '"') {
                        inStr = false;
                        tokens.add(new StrToken(stringLiteral.toString(), new LineFilePos(lineFile, partStartPos)));
                        stringLiteral.setLength(0);
                        partStartPos = i;
                    } else {
                        stringLiteral.append(ch);
                    }
                } else {
                    // not in string literal
                    if (i < len - 1 && ch == '/' && line.charAt(i + 1) == '*') {
                        // enter doc
                        inDoc = true;
                        docLfp = new LineFilePos(lineFile, i);
                        docBuilder.append("/*");
                        i += 1;
                    } else if (i < len - 1 && ch == '/' && line.charAt(i + 1) == '/') {
                        // enter comment, end of this line
                        if (nonLiteral.length() > 2)
                            lineTokenize(nonLiteral.substring(0, nonLiteral.length() - 2),
                                    lineFile, partStartPos);
                        nonLiteral.setLength(0);
                        partStartPos = i;
                        break;
                    } else if (ch == '"') {
                        // enter string literal
                        inStr = true;
                        lineTokenize(nonLiteral.toString(), lineFile, partStartPos);
                        nonLiteral.setLength(0);
                        partStartPos = i;
                    } else if (ch == '\'') {
                        // enter char literal
                        if (i < len - 2 && line.charAt(i + 2) == '\'') {
                            // normal char
                            lineTokenize(nonLiteral.toString(), lineFile, partStartPos);
                            nonLiteral.setLength(0);
                            partStartPos = i;
                            tokens.add(new CharToken(line.charAt(i + 1), new LineFilePos(lineFile, partStartPos)));
                            i += 2;
                        } else if (i < len - 3 && line.charAt(i + 3) == '\'' && line.charAt(i + 1) == '\\') {
                            // escape char
                            lineTokenize(nonLiteral.toString(), lineFile, partStartPos);
                            nonLiteral.setLength(0);
                            partStartPos = i;
                            char escaped = line.charAt(i + 2);
                            if (escaped == '\\') {
                                tokens.add(new CharToken('\\', new LineFilePos(lineFile, partStartPos)));
                            } else if (CharToken.ESCAPES.containsKey(escaped)) {
                                tokens.add(new CharToken(CharToken.ESCAPES.get(escaped),
                                        new LineFilePos(lineFile, partStartPos)));
                            } else {
                                throw new SyntaxError("Invalid escape '\\" + escaped + "'. ",
                                        new LineFilePos(lineFile, partStartPos));
                            }
                            i += 3;
                        } else {
                            throw new SyntaxError("Char literal must contain exactly one symbol. ",
                                    new LineFilePos(lineFile, partStartPos));
                        }
                    } else {
                        nonLiteral.append(ch);
                    }
                }
            }
        }
        if (nonLiteral.length() > 0) {
            lineTokenize(nonLiteral.toString(), lineFile, partStartPos);
        }
        if (inDoc) docBuilder.append('\n');
    }

    private void lineTokenize(String nonLiteral, LineFilePos.LineFile lineFile, int startPos) {
        List<String> list = normalize(nonLiteral);
        int len = list.size();
        int pos = startPos;
        for (int i = 0; i < len; ++i) {
            String s = list.get(i);
            if (StringTypes.isInteger(s)) {
                if (i < len - 2 && list.get(i + 1).equals(".") && StringTypes.isInteger(list.get(i + 2))) {
                    // is a float:   number.number
                    FloatToken floatToken = new FloatToken(
                            s,
                            list.get(i + 2),
                            new LineFilePos(lineFile, pos));
                    tokens.add(floatToken);
                    for (int j = i + 1; j < i + 3; j++) startPos += list.get(j).length();
                    i += 2;
                } else {
                    tokens.add(new IntToken(s, new LineFilePos(lineFile, pos)));
                }
            } else if (StringTypes.startsWithNum(s)) {
                if (s.endsWith("b")) {
                    tokens.add(new ByteToken(s.substring(0, s.length() - 1), new LineFilePos(lineFile, pos)));
                } else {
                    tokens.add(new IntToken(s, new LineFilePos(lineFile, pos)));
                }
            } else if (StringTypes.isIdentifier(s)) {
                tokens.add(new IdToken(s, new LineFilePos(lineFile, pos)));
            } else if (EXTRA_IDENTIFIERS.contains(s)) {
                tokens.add(new IdToken(s, new LineFilePos(lineFile, pos)));
            }
            pos += s.length();
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
        private static final int DOLLAR = 27;
        private static final int UNDEFINED = 0;

        private static final int[] SELF_CONCATENATE = {
                DIGIT, LETTER, GT, EQ, LT, AND, OR, UNDERSCORE, PLUS, MINUS, DOT
        };
        private static final int[][] CROSS_CONCATENATE = {
                {LETTER, UNDERSCORE},
                {UNDERSCORE, LETTER},
                {DIGIT, UNDERSCORE},
                {UNDERSCORE, DIGIT},
                {MINUS, GT},
                {LT, MINUS},
                {LETTER, DIGIT},
                {DIGIT, LETTER},
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
                case '$' -> DOLLAR;
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

        private static boolean startsWithNum(String s) {
            return s.length() > 0 && s.charAt(0) >= '0' && s.charAt(0) <= '9';
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
