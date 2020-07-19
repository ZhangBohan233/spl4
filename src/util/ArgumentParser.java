package util;

import java.io.File;

public class ArgumentParser {
    private File mainSrcFile;
    private boolean allValid;
    private boolean noImportLang;
    private boolean printAst;
    private boolean printTokens;
    private boolean printMem;
    private boolean timer;
    private boolean gcInfo;
    private boolean gcTrigger;
    private String msg;
    private String[] splArgs;

    public ArgumentParser(String[] args) {
        parseArgs(args);
    }

    private void parseArgs(String[] args) {
        for (int i = 0; i < args.length; ++i) {
            if (mainSrcFile == null) {
                String s = args[i];
                if (s.length() > 0 && s.charAt(0) == '-') {
                    switch (s) {
                        case "-nl":
                        case "--noLang":
                            noImportLang = true;
                            break;
                        case "-ast":
                            printAst = true;
                            break;
                        case "-gc":
                            gcInfo = true;
                            gcTrigger = true;
                            break;
                        case "--gci":
                            gcInfo = true;
                            break;
                        case "--gct":
                            gcTrigger = true;
                            break;
                        case "-tk":
                        case "--tokens":
                            printTokens = true;
                            break;
                        case "-pm":
                        case "--printMem":
                            printMem = true;
                            break;
                        case "-t":
                        case "--timer":
                            timer = true;
                            break;
                        default:
                            System.out.println("Unknown flag '" + s + "'");
                            break;
                    }
                } else {
                    mainSrcFile = new File(s);
                    if (!mainSrcFile.exists()) {
                        msg = "Source file does not exist.";
                        allValid = false;
                        return;
                    }
                }
            } else {
                splArgs = new String[args.length - i];
                System.arraycopy(args, i, splArgs, 0, splArgs.length);
                break;
            }
        }
        if (mainSrcFile == null) {
            System.out.println("Source file not specified.");
        } else allValid = true;
    }

    public File getMainSrcFile() {
        return mainSrcFile;
    }

    public boolean isAllValid() {
        return allValid;
    }

    public boolean importLang() {
        return !noImportLang;
    }

    public boolean isPrintAst() {
        return printAst;
    }

    public boolean isPrintMem() {
        return printMem;
    }

    public boolean isPrintTokens() {
        return printTokens;
    }

    public boolean isTimer() {
        return timer;
    }

    public boolean isGcInfo() {
        return gcInfo;
    }

    public boolean isGcTrigger() {
        return gcTrigger;
    }

    public String getMsg() {
        return msg;
    }

    public String[] getSplArgs() {
        return splArgs;
    }
}
