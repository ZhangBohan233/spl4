package spl;

import spl.util.LineFile;

public class Main {

    static final LineFile LF_MAIN = new LineFile("spl.Main");

    public static void main(String[] args) throws Exception {
        SplInterpreter interpreter = new SplInterpreter();
        interpreter.run(args);
    }
}

