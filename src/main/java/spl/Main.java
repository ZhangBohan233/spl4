package spl;

import spl.ast.*;
import spl.interpreter.Memory;
import spl.interpreter.env.GlobalEnvironment;
import spl.interpreter.splErrors.NativeTypeError;
import spl.lexer.FileTokenizer;
import spl.lexer.TextProcessResult;
import spl.lexer.TextProcessor;
import spl.lexer.TokenizeResult;
import spl.parser.Parser;
import spl.util.ArgumentParser;
import spl.util.LineFile;

public class Main {

    static final LineFile LF_MAIN = new LineFile("spl.Main");

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
                System.out.println("===== End of spl.ast =====");
            }
            long vmStartBegin = System.currentTimeMillis();
            Memory memory = new Memory();
            GlobalEnvironment globalEnvironment = new GlobalEnvironment(memory);

            if (argumentParser.isGcInfo()) memory.debugs.setPrintGcRes(true);
            if (argumentParser.isGcTrigger()) memory.debugs.setPrintGcTrigger(true);
            memory.setCheckContract(argumentParser.isCheckContract());

            SplInterpreter.initNatives(globalEnvironment);
            SplInterpreter.importModules(globalEnvironment, processed.importedPaths);

            long runBegin = System.currentTimeMillis();

            try {
                root.evaluate(globalEnvironment);

                SplInterpreter.callMain(argumentParser.getSplArgs(), globalEnvironment);
            } catch (ClassCastException cce) {
                cce.printStackTrace();
                throw new NativeTypeError();
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

