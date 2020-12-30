package spl;

import spl.ast.BlockStmt;
import spl.ast.Node;
import spl.interpreter.Memory;
import spl.interpreter.env.GlobalEnvironment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.lexer.*;
import spl.lexer.treeList.BracketList;
import spl.parser.Parser;
import spl.util.Constants;
import spl.util.LineFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class Console {

    private InputStream in = System.in;
    private PrintStream out = System.out;
    private PrintStream err = System.err;
    private final ConsoleTokenizer consoleTokenizer = new ConsoleTokenizer();
    private GlobalEnvironment globalEnvironment;

    public static void main(String[] args) throws IOException {
        Console console = new Console();
        console.runConsole();
    }

    public Console() throws IOException {
        createConsoleEnvironment();
    }

    public Console(InputStream in, PrintStream out, PrintStream err) throws IOException {
        this.out = out;
        this.err = err;
        this.in = in;
        createConsoleEnvironment();
    }

    private void createConsoleEnvironment() throws IOException {
        FileTokenizer fileTokenizer =
                new FileTokenizer(new File("lib/console.sp"), true);
        TextProcessResult tpr =
                new TextProcessor(fileTokenizer.tokenize(), true).process();
        Parser parser = new Parser(tpr);
        BlockStmt root = parser.parse();

        Memory memory = new Memory();
        globalEnvironment = new GlobalEnvironment(memory);
        SplInterpreter.initNatives(globalEnvironment);
        SplInterpreter.importModules(globalEnvironment, tpr.importedPaths);

        SplInvokes invokes =
                (SplInvokes) memory.get((Reference) globalEnvironment.get(Constants.INVOKES, LineFile.LF_CONSOLE));
        invokes.setOut(out);

        root.evaluate(globalEnvironment);
    }

    /**
     * Runs the console in the environment
     */
    public void runConsole() {

        String line;
        Scanner scanner = new Scanner(in);
        out.print(">>> ");
        while ((line = scanner.nextLine()) != null) {
            globalEnvironment.removeException();
            line = line.trim();
            if (line.equals(":q")) break;

            if (addCode(line)) {
                runCode();
                out.print(">>> ");
            } else {
                out.print("... ");
            }
        }
    }

    public boolean addCode(String code) {
        consoleTokenizer.addLine(code);
        return consoleTokenizer.readyToBuild();
    }

    public void runCode() {
        try {
            BracketList bracketList = consoleTokenizer.build();
            TextProcessResult tpr =
                    new TextProcessor(new TokenizeResult(bracketList), false).process();
            Parser parser = new Parser(tpr);
            BlockStmt lineExpr = parser.parse();
            if (lineExpr.getLines().size() > 0 && lineExpr.getLines().get(0).size() > 0) {
                Node only = lineExpr.getLines().get(0).get(0);
                SplElement result = only.evaluate(globalEnvironment);
                if (result != null) {
                    out.println(result.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace(err);
            consoleTokenizer.clear();
        }
    }
}
