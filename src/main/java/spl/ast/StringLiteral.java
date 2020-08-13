package spl.ast;

import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Char;
import spl.interpreter.primitives.Pointer;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.splObjects.Instance;
import spl.interpreter.splObjects.SplArray;
import spl.util.Constants;
import spl.util.LineFile;

public class StringLiteral extends LiteralNode {

    private final char[] charArray;

    public StringLiteral(char[] charArray, LineFile lineFile) {
        super(lineFile);

        this.charArray = charArray;
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return createString(charArray, env, getLineFile());
    }

    public static Pointer createString(char[] charArray, Environment env, LineFile lineFile) {
        // create spl char array
        Pointer arrPtr = createCharArrayAndAllocate(charArray, env, lineFile);
        env.getMemory().addTempPtr(arrPtr);

        // create String instance
        Pointer strTv = createStringInstance(arrPtr, env, lineFile);

        env.getMemory().removeTempPtr(arrPtr);

        return strTv;
    }

    private static Pointer createCharArrayAndAllocate(char[] charArray, Environment env, LineFile lineFile) {
        Pointer arrPtr = SplArray.createArray(SplElement.CHAR, charArray.length, env);
        for (int i = 0; i < charArray.length; ++i) {
            Char c = new Char(charArray[i]);
            SplArray.setItemAtIndex(
                    arrPtr,
                    i,
                    c,
                    env,
                    lineFile
            );
        }
        return arrPtr;
    }

    private static Pointer createStringInstance(Pointer arrPtr, Environment env, LineFile lineFile) {
        return Instance.createInstanceWithInitCall(
                Constants.STRING_CLASS,
                EvaluatedArguments.of(arrPtr),
                env,
                lineFile).pointer;
    }

    @Override
    public String toString() {
        return "StringLiteral{" + new String(charArray) + "}";
    }
}
