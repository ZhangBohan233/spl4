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
            "+", "-", "*", "/", "%", ">>", ">>>", "<<", "&", "|", "^"
    );

    public static final Set<String> NUMERIC_BINARY_ASSIGN = Set.of(
            "+=", "-=", "*=", "/=", "%=", ">>=", ">>>=", "<<=", "&=", "|=", "^="
    );

    public static final Set<String> LOGICAL_BINARY = Set.of(
            ">", "<", "==", "!=", ">=", "<="
    );

    public static final Set<String> FAKE_TERNARY = Set.of(
            "?"
    );

    public static final Set<String> LAZY_BINARY = Set.of(
            "&&", "||"
    );

    public static final Set<String> LOGICAL_UNARY = Set.of(
            "!"
    );

    public static final Set<String> SYMBOLS = Set.of(
            "{", "}", "[", "\\[", "]", "(", ")", ".", ",", ";", ":"
    );

    public static final Set<String> OTHERS = Set.of(
            "=", "->", "<-", ":=", "++", "--"
    );

    public static final Set<String> EXTRA_IDENTIFIERS = Utilities.mergeSets(
            NUMERIC_BINARY,
            NUMERIC_BINARY_ASSIGN,
            LOGICAL_BINARY,
            LAZY_BINARY,
            LOGICAL_UNARY,
            SYMBOLS,
            FAKE_TERNARY,
            OTHERS
    );

    public static final Set<String> ALL_BINARY = Utilities.mergeSets(
            NUMERIC_BINARY,
            NUMERIC_BINARY_ASSIGN,
            LOGICAL_BINARY,
            LAZY_BINARY
    );

    public static final Set<String> RESERVED = Set.of(
            "class", "fn", "if", "else", "new", "extends", "implements", "return", "break",
            "continue", "true", "false", "null", "while", "for", "import", "namespace",
            "abstract", "const", "var", "assert", "as", "super", "this", "lambda", "instanceof",
            "cond", "switch", "case", "default", "fallthrough"
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
        BraceList root = new BraceList(null);
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
                    return new BracketList(currentActive);
                case ")":
                    if (currentActive instanceof BracketList) {
                        currentActive.parentElement.add(currentActive);
                        return currentActive.parentElement;
                    } else {
                        throw new SyntaxError("')' must close a '(', not a '" + symbol + "'. ",
                                tk.getLineFile());
                    }
                case "[":
                    return new SqrBracketList(currentActive);
                case "]":
                    if (currentActive instanceof SqrBracketList) {
                        currentActive.parentElement.add(currentActive);
                        return currentActive.parentElement;
                    } else {
                        throw new SyntaxError("']' must close a '[', not a '" + symbol + "'. ",
                                tk.getLineFile());
                    }
                case "{":
                    return new BraceList(currentActive);
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

//    private void findImport(int from, int to) throws IOException {
//        for (int i = from; i < to; ++i) {
//            Token token = tokens.get(i);
//            if (token instanceof IdToken && ((IdToken) token).getIdentifier().equals("import")) {
//                Token nextTk = tokens.get(i + 1);
//                String name, path;
//                int removeCount;
//                boolean namespace = false;
//
//                try {
//                    if (nextTk instanceof StrToken) {
//                        path = ((StrToken) nextTk).getLiteral();
//                        name = nameOfPath(path);
//                        removeCount = 1;
//                    } else if (nextTk instanceof IdToken && ((IdToken) nextTk).getIdentifier().equals("namespace")) {
//                        path = ((StrToken) tokens.get(i + 2)).getLiteral();
//                        name = nameOfPath(path);
//                        removeCount = 2;
//                        namespace = true;
//                    } else {
//                        throw new SyntaxError(IMPORT_USAGE, nextTk.getLineFile());
//                    }
//
//                    if (!namespace && tokens.size() > i + 2) {
//                        IdToken asToken = (IdToken) tokens.get(i + 2);
//                        if (asToken.getIdentifier().equals("as")) {
//                            removeCount = 3;
//                            name = ((IdToken) tokens.get(i + 3)).getIdentifier();
//                        } else {
//                            throw new SyntaxError(IMPORT_USAGE, nextTk.getLineFile());
//                        }
//                    }
//                } catch (ClassCastException | IndexOutOfBoundsException e) {
//                    throw new SyntaxError(IMPORT_USAGE, nextTk.getLineFile());
//                }
//
//                for (int j = 0; j < removeCount; ++j) {
//                    tokens.remove(i + 1);
//                }
//
//                File file;
//                if (path.endsWith(".sp")) {  // user library
//                    file = new File(srcFile.getParentFile().getAbsolutePath() + File.separator + path);
//                } else {
//                    file = new File("lib" + File.separator + path + ".sp");
//                }
//                importFile(file, name, namespace, token.getLineFile());
//
//                break;
//            }
//        }
//    }

//    private void importFile(File importedFile, String importName, boolean namespace, LineFile lineFile)
//            throws IOException {
//        FileTokenizer tokenizer = new FileTokenizer(importedFile, false, false);
//        TokenList tokenList = tokenizer.tokenize();
//        tokens.add(new IdToken(importName, lineFile));
//        tokens.add(new IdToken("{", lineFile));
//        tokens.addAll(tokenList.getTokens());
//        tokens.add(new IdToken("}", lineFile));
//        if (namespace) {
//            tokens.add(new IdToken("namespace", lineFile));
//            tokens.add(new IdToken(importName, lineFile));
//            tokens.add(new IdToken(";", lineFile));
//        }
//    }

//    private static String nameOfPath(String path) {
//        path = path.replace("/", File.separator);
//        path = path.replace("\\", File.separator);
//        if (path.endsWith(".sp")) path = path.substring(0, path.length() - 3);
//        if (path.contains(File.separator)) {
//            return path.substring(path.lastIndexOf(File.separator) + 1);
//        } else {
//            return path;
//        }
//    }

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
//        if (string.length() > 1 && string.charAt(string.length() - 1) == '.') {
//            // Object name ended with a number, like list1.something
//            list.add(string.substring(0, string.length() - 1));
//            list.add(".");
//        } else {
        list.add(string);
//        }
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
        private static final int UNDEFINED = 0;

        private static final int[] SELF_CONCATENATE = {DIGIT, LETTER, GT, EQ, LT, AND, OR, UNDERSCORE, PLUS, MINUS};
        private static final int[][] CROSS_CONCATENATE = {
                {LETTER, UNDERSCORE},
                {UNDERSCORE, LETTER},
                {DIGIT, UNDERSCORE},
                {UNDERSCORE, DIGIT},
                {MINUS, GT},
                {LT, MINUS},
//                {DIGIT, DOT},
//                {DOT, DIGIT},
                {LETTER, DIGIT},
                {GT, EQ},
                {LT, EQ},
                {NOT, EQ},
                {PLUS, EQ},
                {MINUS, EQ},
                {OTHER_ARITHMETIC, EQ},
                {TYPE, EQ},
                {ESCAPE, L_SQR_BRACKET}
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
            if ((leftType == rightType && Utilities.arrayContains(SELF_CONCATENATE, leftType)) ||
                    Utilities.arrayContains2D(CROSS_CONCATENATE, new int[]{leftType, rightType})) {
                return true;
            } else {
                return false;
            }
        }

        private static int identify(char ch) {
            if (Character.isDigit(ch)) return DIGIT;
            else if (Character.isAlphabetic(ch)) return LETTER;

            switch (ch) {
                case '{':
                    return L_BRACE;
                case '}':
                    return R_BRACE;
                case '(':
                    return L_BRACKET;
                case ')':
                    return R_BRACKET;
                case '[':
                    return L_SQR_BRACKET;
                case ']':
                    return R_SQR_BRACKET;
                case ';':
                    return EOL;
                case '\n':
                    return NEW_LINE;
                case '>':
                    return GT;
                case '<':
                    return LT;
                case '=':
                    return EQ;
                case '&':
                    return AND;
                case '|':
                    return OR;
                case '^':
                    return XOR;
                case '.':
                    return DOT;
                case ',':
                    return COMMA;
                case '_':
                    return UNDERSCORE;
                case '!':
                    return NOT;
                case '+':
                    return PLUS;
                case '-':
                    return MINUS;
                case '*':
                case '/':
                case '%':
                    return OTHER_ARITHMETIC;
                case ':':
                    return TYPE;
                case '\\':
                    return ESCAPE;
                default:
                    return UNDEFINED;
            }
        }
    }

    public static class StringTypes {

        private static boolean isInteger(String s) {
            for (char c : s.toCharArray()) {
                if (!(Character.isDigit(c) || c == '_')) return false;
            }
            return true;
        }

        public static boolean isIdentifier(String s) {
            if (s.length() > 0) {
                char lead = s.charAt(0);
                if (!(Character.isAlphabetic(lead) || lead == '_')) return false;
                int len = s.length();
                for (int i = 1; i < len; ++i) {
                    char ch = s.charAt(i);
                    if (!(Character.isAlphabetic(ch) || Character.isDigit(ch) || ch == '_')) return false;
                }
                return true;
            }
            return false;
        }
    }
}
