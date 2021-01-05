package spl.tools.codeArea;

import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import spl.ast.*;
import spl.lexer.*;
import spl.parser.ParseError;
import spl.parser.Parser;
import spl.util.LineFilePos;
import spl.util.Utilities;

import java.io.IOException;
import java.util.*;

public class SplCodeAnalyzer extends CodeAnalyzer {

    private static final long ANALYZE_TIME = 3000;

    private static final Set<String> KW_COLORED = Utilities.mergeSets(
            Tokenizer.KEYWORDS,
            Set.of(",", ";")
    );

    private static final char[] SPLITTERS = {' ', '.', ',', ';', '(', ')', '[', ']', '{', '}'};

    private final Timer timer;

    public SplCodeAnalyzer(CodeArea codeArea, Font baseFont) {
        super(codeArea, baseFont);

        timer = new Timer();
        timer.schedule(new AnalyzeTask(), ANALYZE_TIME, ANALYZE_TIME);
    }

    @Override
    public void markKeyword(List<CodeArea.Text> line) {
        String[] words = splitWords(line, SPLITTERS);
        int index = 0;
        for (String word : words) {
            if (KW_COLORED.contains(word)) {
                for (int i = 0; i < word.length(); i++) {
                    CodeArea.Text text = line.get(index + i);
                    text.setPaint(keywordPaint);
                    text.setFont(keywordFont);
                }
            } else if (builtinNames.contains(word)) {
                for (int i = 0; i < word.length(); i++) {
                    CodeArea.Text text = line.get(index + i);
                    text.setPaint(builtinPaint);
                    text.setFont(codeFont);
                }
            } else {
                for (int i = 0; i < word.length(); i++) {
                    CodeArea.Text text = line.get(index + i);
                    text.setPaint(codePaint);
                    text.setFont(codeFont);
                }
            }
            index += word.length();
        }
    }

    private void mark(List<CodeArea.Text> line, String[] words, Set<String> targets, Paint paint, Font font) {

    }

    private void markFunctionName(NameNode name, List<CodeArea.Text> line) {
        String nameStr = name.getName();
        for (int i = 0; i < nameStr.length(); i++) {
            int index = name.getLineFile().getPos() + i;
            line.get(index).setPaint(functionPaint);
        }
    }

    private void markFunctionCall(List<CodeArea.Text> line, int index) {

    }

    public void close() {
        timer.cancel();
    }

    private String[] splitWords(List<CodeArea.Text> line, char[] splitters) {
        List<String> res = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
//        int lineSize = line.size();
        for (CodeArea.Text value : line) {
            char text = value.text;
            if (Utilities.arrayContains(splitters, text)) {
                res.add(builder.toString());
                builder.setLength(0);
                res.add(String.valueOf(text));
            } else {
                builder.append(text);
            }
        }
        if (builder.length() > 0) {
            res.add(builder.toString());
        }

        return res.toArray(new String[0]);
    }

    private String joinLine(List<CodeArea.Text> line) {
        StringBuilder lineTextB = new StringBuilder();
        for (CodeArea.Text t : line) {
            lineTextB.append(t.text);
        }
        return lineTextB.toString();
    }

    private class AnalyzeTask extends TimerTask {
        @Override
        public void run() {
            String text = codeArea.getTextEditor().getText();
            try {
                codeFile.save(text);
                FileTokenizer tokenizer = new FileTokenizer(codeFile.getFile(), true);
                TokenizeResult tr = tokenizer.tokenize();
                TextProcessResult processed = new TextProcessor(tr, true).process();
                Parser parser = new Parser(processed);
                BlockStmt blockStmt = parser.parse();

                AnalyzeEnv analyzeEnv = new AnalyzeEnv(null);
                for (Line line : blockStmt.getLines()) {
                    analyzeNode(line, analyzeEnv);
                }

                codeArea.refresh();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseError e) {
                LineFilePos errLf = e.getLocation();
                if (errLf.getFile().equals(codeFile.getFile())) {

                }
            }
        }
    }

    private void analyzeNode(Node node, AnalyzeEnv analyzeEnv) {
        LineFilePos lineFile = node.getLineFile();
        int lineIndex = lineFile.getLine() - 1;
        if (node instanceof Line) {
            for (Node part : ((Line) node).getChildren()) {
                analyzeNode(part, analyzeEnv);
            }
        } else if (node instanceof BlockStmt) {
            for (Line line : ((BlockStmt) node).getLines()) {
                analyzeNode(line, analyzeEnv);
            }
        } else if (node instanceof FuncDefinition) {
            FuncDefinition fd = (FuncDefinition) node;
            markFunctionName(fd.name, codeArea.getTextEditor().getLine(lineIndex));
            AnalyzeEnv funcEnv = new AnalyzeEnv(analyzeEnv);
            for (Node node1 : fd.getParameters().getChildren()) {
                analyzeDefinition(node1, funcEnv);
            }
            analyzeNode(fd.getBody(), analyzeEnv);
            analyzeEnv.put(fd.name.getName(), AnalyzeEnv.PLACEHOLDER);
        } else if (node instanceof TypeExpr) {
            analyzeDefinition(node, analyzeEnv);
        } else if (node instanceof QuickAssignment) {
            analyzeDefinition(((QuickAssignment) node).getLeft(), analyzeEnv);
            analyzeNode(((QuickAssignment) node).getRight(), analyzeEnv);
        } else if (node instanceof Assignment) {
            analyzeNode(((Assignment) node).getLeft(), analyzeEnv);
            analyzeNode(((Assignment) node).getRight(), analyzeEnv);
        } else if (node instanceof Declaration) {
            analyzeEnv.put(((Declaration) node).declaredName, AnalyzeEnv.PLACEHOLDER);
        } else if (node instanceof NameNode) {
            if (analyzeEnv.has(((NameNode) node).getName())) {

            }
        } else if (node instanceof IntNode) {
            String num = String.valueOf(((IntNode) node).getValue());
            List<CodeArea.Text> line = codeArea.getTextEditor().getLine(lineIndex);
            for (int i = 0; i < num.length(); i++) {
                int pos = node.getLineFile().getPos() + i;
                line.get(pos).setPaint(numberPaint);
            }
        } else if (node instanceof FloatNode) {
            String num = String.valueOf(((FloatNode) node).getValue());
            List<CodeArea.Text> line = codeArea.getTextEditor().getLine(lineIndex);
            for (int i = 0; i < num.length(); i++) {
                int pos = node.getLineFile().getPos() + i;
                line.get(pos).setPaint(numberPaint);
            }
        } else if (node instanceof UnaryStmt) {
            analyzeNode(((UnaryStmt) node).getValue(), analyzeEnv);
        } else if (node instanceof UnaryExpr) {
            analyzeNode(((UnaryExpr) node).getValue(), analyzeEnv);
        } else if (node instanceof BinaryExpr) {
            analyzeNode(((BinaryExpr) node).getLeft(), analyzeEnv);
            analyzeNode(((BinaryExpr) node).getRight(), analyzeEnv);
        } else if (node instanceof ClassStmt) {
            AnalyzeEnv classEnv = new AnalyzeEnv.ClassAnaEnv(analyzeEnv);
            analyzeNode(((ClassStmt) node).getBody(), classEnv);
        }
    }

    private void analyzeDefinition(Node node, AnalyzeEnv analyzeEnv) {
        if (node instanceof NameNode) {
            analyzeEnv.put(((NameNode) node).getName(), AnalyzeEnv.PLACEHOLDER);
        } else if (node instanceof TypeExpr) {
            analyzeDefinition(((TypeExpr) node).getLeft(), analyzeEnv);
        }
    }
}
