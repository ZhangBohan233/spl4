package spl;

import spl.ast.BlockStmt;
import spl.ast.Node;
import spl.ast.StringLiteral;
import spl.interpreter.Memory;
import spl.interpreter.env.GlobalEnvironment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.Undefined;
import spl.lexer.*;
import spl.lexer.treeList.BracketList;
import spl.parser.ParseResult;
import spl.parser.Parser;
import spl.util.Configs;
import spl.util.Constants;
import spl.util.LineFilePos;
import spl.util.Utilities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class Console {

    private final ConsoleTokenizer consoleTokenizer = new ConsoleTokenizer();
    private InputStream in = System.in;
    private PrintStream out = System.out;
    private PrintStream err = System.err;
    private GlobalEnvironment globalEnvironment;
    private Map<String, StringLiteral> strLitBundleMap;

    public Console() throws IOException {
        createConsoleEnvironment();
    }

    public Console(InputStream in, PrintStream out, PrintStream err) throws IOException {
        this.out = out;
        this.err = err;
        this.in = in;
        createConsoleEnvironment();
    }

    public static void main(String[] args) throws IOException {
        Console console = new Console();
        console.runConsole();
    }

    private void createConsoleEnvironment() throws IOException {
        FileTokenizer fileTokenizer =
                new FileTokenizer(new File("lib/console.sp"), true);
        TextProcessResult tpr =
                new TextProcessor(fileTokenizer.tokenize(), true).process();
        Parser parser = new Parser(tpr);
        ParseResult pr = parser.parse();
        strLitBundleMap = parser.getStringLiterals();

        Memory memory = new Memory(new Memory.Options(
                Configs.getInt("stackLimit", 512),
                Configs.getInt("heapSize", 8192),
                Configs.getBoolean("contract", true),
                Configs.getBoolean("assert", true)));
        globalEnvironment = new GlobalEnvironment(memory);

        LinkedHashMap<String, ParseResult> parsedModules = SplInterpreter.parseImportedModules(
                tpr.importedPaths, strLitBundleMap
        );

        SplInterpreter.initNatives(globalEnvironment);
        SplInterpreter.importModules(globalEnvironment, parsedModules);

        SplInvokes invokes =
                memory.get((Reference) globalEnvironment.get(Constants.INVOKES, LineFilePos.LFP_CONSOLE));
        invokes.setOut(out);
        invokes.setIn(in);
        invokes.setErr(err);

        pr.getRoot().evaluate(globalEnvironment);
    }

    /**
     * Runs the console in the environment
     */
    public void runConsole() {
        String line;
        Scanner scanner = new Scanner(in);
        out.print(">>> ");
        while ((line = scanner.nextLine()) != null) {
            line = line.trim();
            if (line.equals(":q")) break;

            if (addLine(line)) {
                runCode();
                out.print(">>> ");
            } else {
                out.print("... ");
            }
        }
    }

    public boolean addLine(String code) {
        consoleTokenizer.addLine(code);
        return consoleTokenizer.readyToBuild();
    }

    public void runCode() {
        try {
            BracketList bracketList = consoleTokenizer.build();
            TextProcessResult tpr =
                    new TextProcessor(new TokenizeResult(bracketList), false).process();
            Parser parser = new Parser(tpr, strLitBundleMap);
            BlockStmt lineExpr = parser.parse().getRoot();
            if (lineExpr.getLines().size() > 0 && lineExpr.getLines().get(0).size() > 0) {
                Node only = lineExpr.getLines().get(0).get(0);
                SplElement result = only.evaluate(globalEnvironment);
                if (globalEnvironment.hasException()) {
                    Utilities.removeErrorAndPrint(globalEnvironment, LineFilePos.LFP_CONSOLE);
                } else if (result != null && result != Reference.NULL && result != Undefined.UNDEFINED) {
                    out.println(SplInvokes.getRepr(result, globalEnvironment, LineFilePos.LFP_CONSOLE));
                }
            }
        } catch (Exception e) {
            e.printStackTrace(err);
            consoleTokenizer.clear();
        }
    }

    public void interrupt() {
        globalEnvironment.throwException(
                (Reference) globalEnvironment.get(Constants.INTERRUPTION_INS, LineFilePos.LFP_CONSOLE));
    }

    public GlobalEnvironment getGlobalEnvironment() {
        return globalEnvironment;
    }

    public InputStream getIn() {
        return in;
    }

    public PrintStream getOut() {
        return out;
    }

    public PrintStream getErr() {
        return err;
    }
}
