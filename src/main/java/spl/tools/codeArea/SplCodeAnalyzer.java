package spl.tools.codeArea;

import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import spl.lexer.Tokenizer;

import java.util.ArrayList;
import java.util.List;

public class SplCodeAnalyzer extends CodeAnalyzer {

    public SplCodeAnalyzer(Paint keywordPaint, Font keywordFont, Paint codePaint, Font codeFont) {
        super(keywordPaint, keywordFont, codePaint, codeFont);
    }

    @Override
    public void markKeyword(List<CodeArea.Text> line) {
        String[] words = splitWords(line, ' ');
        int index = 0;
        for (String word : words) {
            for (String kw : Tokenizer.KEYWORDS) {
                if (word.equals(kw)) {
                    for (int i = 0; i < word.length(); i++) {
                        CodeArea.Text text = line.get(index + i);
                        text.setPaint(keywordPaint);
                        text.setFont(keywordFont);
                    }
                    break;
                } else {
                    for (int i = 0; i < word.length(); i++) {
                        CodeArea.Text text = line.get(index + i);
                        text.setPaint(codePaint);
                        text.setFont(codeFont);
                    }
                }
            }
            index += word.length();
        }
    }

    @Override
    public void markVariable(List<CodeArea.Text> line) {

    }

    private String[] splitWords(List<CodeArea.Text> line, char splitter) {
        List<String> res = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        int lineSize = line.size();
        for (CodeArea.Text value : line) {
            char text = value.text;
            if (text == splitter) {
                res.add(builder.toString());
                builder.setLength(0);
                res.add(" ");
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

//    public static void main(String[] args) {
//        List<CodeArea.Text> line = List.of(
//                new CodeArea.Text('f'),
//                new CodeArea.Text('n'),
//                new CodeArea.Text(' '),
//                new CodeArea.Text('m'),
//                new CodeArea.Text('a'),
//                new CodeArea.Text('i'),
//                new CodeArea.Text('n')
//        );
//        System.out.println(Arrays.toString(new SplCodeAnalyzer().splitWords(line, ' ')));
//    }
}
