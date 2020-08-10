import ast.BlockStmt;
import interpreter.Memory;
import interpreter.env.GlobalEnvironment;
import lexer.TokenizeResult;
import lexer.treeList.BraceList;
import parser.Parser;
import util.LineFile;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

public class Console {

    public static void main(String[] args) throws IOException {
        Console console = new Console();
        console.runConsole(console.createConsoleEnvironment());
    }

    private GlobalEnvironment createConsoleEnvironment() throws IOException {
        Memory memory = new Memory();
        GlobalEnvironment ge = new GlobalEnvironment(memory);
        SplInterpreter.initNatives(ge);

        Parser parser = new Parser(new TokenizeResult(new BraceList(null, LineFile.LF_CONSOLE)),
                true);
        BlockStmt root = parser.parse();

        root.evaluate(ge);

        return ge;
    }

    /**
     * Runs the console in the environment
     *
     * @param globalEnv the global environment, with everything set.
     */
    public void runConsole(GlobalEnvironment globalEnv) {
        StringBuilder builder = new StringBuilder();
        String line;
        Scanner scanner = new Scanner(System.in);
        System.out.print(">>> ");
        while ((line = scanner.nextLine()) != null) {
            if (line.equals(":q")) break;

            builder.append(line);

            System.out.print(">>> ");
        }
    }
}
