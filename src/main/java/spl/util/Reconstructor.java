package spl.util;

import spl.ast.Node;

import java.lang.reflect.Method;

public class Reconstructor {

    @SuppressWarnings("unchecked")
    public static <T extends Node> T reconstruct(BytesIn is) throws Exception {
        String className = is.readString();
        LineFilePos lineFilePos = LineFilePos.readLineFilePos(is);

        Class<? extends Node> clazz = (Class<? extends Node>) Class.forName(className);

        Method recMethod = clazz.getMethod("reconstruct", BytesIn.class, LineFilePos.class);
        return (T) recMethod.invoke(null, is, lineFilePos);
    }
}
