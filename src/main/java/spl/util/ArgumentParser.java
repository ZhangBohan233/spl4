package spl.util;

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
                        case "-nl", "--noLang" -> noImportLang = true;
                        case "-ast" -> printAst = true;
                        case "-gc" -> {
                            gcInfo = true;
                            gcTrigger = true;
                        }
                        case "--gci" -> gcInfo = true;
                        case "--gct" -> gcTrigger = true;
                        case "-tk", "--tokens" -> printTokens = true;
                        case "-pm", "--printMem" -> printMem = true;
                        case "-t", "--timer" -> timer = true;
                        default -> System.out.println("Unknown flag '" + s + "'");
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
            System.err.println("Source file not specified.");
            return;
        } else allValid = true;

        if (splArgs == null) {
            splArgs = new String[]{mainSrcFile.getAbsolutePath()};
        }
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
