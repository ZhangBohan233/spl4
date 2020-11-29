package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Pointer;
import spl.interpreter.splObjects.Macro;
import spl.parser.ParseError;
import spl.util.LineFile;

import java.util.ArrayList;
import java.util.List;

public class MacroNode extends Statement {

    private final String macroName;
    private final List<MacroSyntaxNode> syntaxNodes = new ArrayList<>();

    public MacroNode(String macroName, BlockStmt body, LineFile lineFile) {
        super(lineFile);

        this.macroName = macroName;
        parseBody(body);
    }

    private void parseBody(BlockStmt body) {
        for (Line line : body.getLines()) {
            for (Node node : line.getChildren()) {
                if (node instanceof MacroSyntaxNode) {
                    syntaxNodes.add((MacroSyntaxNode) node);
                } else {
                    throw new ParseError("Macro must only contain syntax. ", lineFile);
                }
            }
        }
    }

    @Override
    protected void internalProcess(Environment env) {
        Macro macro = new Macro(syntaxNodes);
        Pointer ptr = env.getMemory().allocateObject(macro, env);
        env.defineConstAndSet(macroName, ptr, lineFile);
    }

    @Override
    public String toString() {
        return "macro "  + macroName + "{\n" + syntaxNodes + '}';
    }
}
