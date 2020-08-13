package spl;

import spl.ast.BlockStmt;
import spl.ast.Node;
import spl.interpreter.Memory;
import spl.interpreter.env.GlobalEnvironment;
import spl.interpreter.primitives.SplElement;
import spl.lexer.*;
import spl.lexer.treeList.BracketList;
import spl.parser.Parser;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Console {

    public static void main(String[] args) throws IOException {
        Console console = new Console();
        console.runConsole(console.createConsoleEnvironment());
    }

    private GlobalEnvironment createConsoleEnvironment() throws IOException {
        FileTokenizer fileTokenizer =
                new FileTokenizer(new File("lib/console.sp"), true);
        TextProcessResult tpr =
                new TextProcessor(fileTokenizer.tokenize(), true).process();
        Parser parser = new Parser(tpr);
        BlockStmt root = parser.parse();

        Memory memory = new Memory();
        GlobalEnvironment ge = new GlobalEnvironment(memory);
        SplInterpreter.initNatives(ge);
        SplInterpreter.importModules(ge, tpr.importedPaths);

        root.evaluate(ge);

        return ge;
    }

    /**
     * Runs the console in the environment
     *
     * @param globalEnv the global environment, with everything set.
     */
    public void runConsole(GlobalEnvironment globalEnv) throws IOException {

        ConsoleTokenizer consoleTokenizer = new ConsoleTokenizer();

        String line;
        Scanner scanner = new Scanner(System.in);
        System.out.print(">>> ");
        while ((line = scanner.nextLine()) != null) {
            line = line.trim();
            if (line.equals(":q")) break;

            consoleTokenizer.addLine(line);
            if (consoleTokenizer.readyToBuild()) {
                try {
                    BracketList bracketList = consoleTokenizer.build();
                    TextProcessResult tpr =
                            new TextProcessor(new TokenizeResult(bracketList), false).process();
                    Parser parser = new Parser(tpr);
                    BlockStmt lineExpr = parser.parse();
                    if (lineExpr.getLines().size() > 0 && lineExpr.getLines().get(0).size() > 0) {
                        Node only = lineExpr.getLines().get(0).get(0);
                        SplElement result = only.evaluate(globalEnv);
                        if (result != null) {
                            System.out.println(result);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    consoleTokenizer.clear();
                }

                System.out.print(">>> ");
            } else {
                System.out.print("... ");
            }

        }
    }
}
