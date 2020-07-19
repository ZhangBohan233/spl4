package ast;

import interpreter.env.Environment;
import interpreter.primitives.Char;
import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;
import interpreter.splObjects.Instance;
import interpreter.splObjects.SplArray;
import util.Constants;
import util.LineFile;

import java.util.List;

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
//        ArrayType arrayType = new ArrayType(PrimitiveType.TYPE_CHAR);
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
        NameNode clazzNode = new NameNode(Constants.STRING_CLASS, lineFile);
        Pointer strPtr = (Pointer) clazzNode.evaluate(env);
//        ClassType classType = (ClassType) clazzNode.evalType(env);
        Instance.InstanceAndPtr instanceTv = Instance.createInstanceAndAllocate(strPtr, env, lineFile);

        Instance.callInit(instanceTv.instance, new SplElement[]{arrPtr}, env, lineFile);

        return instanceTv.pointer;
    }

    @Override
    public String toString() {
        return "StringLiteral{" + new String(charArray) + "}";
    }
}
