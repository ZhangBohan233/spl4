import ast.*;
import interpreter.EvaluatedArguments;
import interpreter.Memory;
import interpreter.splErrors.NativeError;
import interpreter.env.Environment;
import interpreter.env.GlobalEnvironment;
import interpreter.invokes.SplInvokes;
import interpreter.primitives.*;
import interpreter.splObjects.*;
import interpreter.splErrors.TypeError;
import lexer.FileTokenizer;
import lexer.TextProcessResult;
import lexer.TextProcessor;
import lexer.TokenizeResult;
import lexer.treeList.BraceList;
import lexer.treeList.CollectiveElement;
import parser.Parser;
import util.ArgumentParser;
import util.LineFile;
import util.Utilities;

public class Main {

    static final LineFile LF_MAIN = new LineFile("Main");

    public static void main(String[] args) throws Exception {
        ArgumentParser argumentParser = new ArgumentParser(args);
        if (argumentParser.isAllValid()) {
            long parseBegin = System.currentTimeMillis();
            FileTokenizer tokenizer = new FileTokenizer(
                    argumentParser.getMainSrcFile(),
                    argumentParser.importLang()
            );
            TokenizeResult rootToken = tokenizer.tokenize();
            TextProcessResult processed = new TextProcessor(rootToken,
                    argumentParser.importLang()).process();
            if (argumentParser.isPrintTokens()) {
                System.out.println(processed.rootList);
            }
            Parser parser = new Parser(processed);
            BlockStmt root = parser.parse();
            if (argumentParser.isPrintAst()) {
                System.out.println("===== Ast =====");
                System.out.println(root);
                System.out.println("===== End of ast =====");
            }
            long vmStartBegin = System.currentTimeMillis();
            Memory memory = new Memory();
            GlobalEnvironment globalEnvironment = new GlobalEnvironment(memory);

            if (argumentParser.isGcInfo()) memory.debugs.setPrintGcRes(true);
            if (argumentParser.isGcTrigger()) memory.debugs.setPrintGcTrigger(true);

            SplInterpreter.initNatives(globalEnvironment);
            SplInterpreter.importModules(globalEnvironment, processed.importedPaths);

            long runBegin = System.currentTimeMillis();

            try {
                root.evaluate(globalEnvironment);

                SplInterpreter.callMain(argumentParser.getSplArgs(), globalEnvironment);
            } catch (ClassCastException cce) {
                cce.printStackTrace();
                throw new TypeError();
            }

            long processEnd = System.currentTimeMillis();

            if (argumentParser.isPrintMem()) {
                memory.printMemory();
            }
            if (argumentParser.isTimer()) {
                System.out.printf(
                        "Parse time: %d ms, VM startup time: %d ms, running time: %d ms.%n",
                        vmStartBegin - parseBegin,
                        runBegin - vmStartBegin,
                        processEnd - runBegin
                );
            }
        } else {
            System.out.println(argumentParser.getMsg());
        }
    }
}

