package spl;

import spl.util.LineFilePos;

public class Main {

    static final LineFilePos LF_MAIN = new LineFilePos("spl.Main");

    public static void main(String[] args) throws Exception {
        SplInterpreter interpreter = new SplInterpreter();
        interpreter.run(args);
    }
}

