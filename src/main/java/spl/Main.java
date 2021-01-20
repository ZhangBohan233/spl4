package spl;

import spl.util.Configs;
import spl.util.LineFilePos;

public class Main {

    static final LineFilePos LF_MAIN = new LineFilePos("spl.Main");

    public static void main(String[] args) throws Exception {
        Configs.load();
        SplInterpreter interpreter = new SplInterpreter();
        try {
            interpreter.run(args);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
    }
}

