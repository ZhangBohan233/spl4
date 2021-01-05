package spl.ast;

import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Char;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.splObjects.Instance;
import spl.interpreter.splObjects.SplArray;
import spl.util.Constants;
import spl.util.LineFilePos;

public class StringLiteral extends LiteralNode {

    private final char[] charArray;

    public StringLiteral(char[] charArray, LineFilePos lineFile) {
        super(lineFile);

        this.charArray = charArray;
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return createString(charArray, env, getLineFile());
    }

    public static Reference createString(char[] charArray, Environment env, LineFilePos lineFile) {
        // create spl char array
        Reference arrPtr = createCharArrayAndAllocate(charArray, env, lineFile);
        env.getMemory().addTempPtr(arrPtr);

        // create String instance
        Reference strTv = createStringInstance(arrPtr, env, lineFile);

        env.getMemory().removeTempPtr(arrPtr);

        return strTv;
    }

    private static Reference createCharArrayAndAllocate(char[] charArray, Environment env, LineFilePos lineFile) {
        Reference arrPtr = SplArray.createArray(SplElement.CHAR, charArray.length, env);
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

    private static Reference createStringInstance(Reference arrPtr, Environment env, LineFilePos lineFile) {
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
