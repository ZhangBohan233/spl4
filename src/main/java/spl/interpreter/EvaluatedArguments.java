package spl.interpreter;

import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;

import java.util.*;

public class EvaluatedArguments {

    public final List<SplElement> positionalArgs = new ArrayList<>();
    public final Map<String, SplElement> keywordArgs = new HashMap<>();

    public EvaluatedArguments() {
    }

    public static EvaluatedArguments of(SplElement... posArgs) {
        EvaluatedArguments arguments = new EvaluatedArguments();
        arguments.positionalArgs.addAll(Arrays.asList(posArgs));
        return arguments;
    }

    public void insertThis(Reference thisPtr) {
        positionalArgs.add(0, thisPtr);
    }

    @Override
    public String toString() {
        return "EvaluatedArguments{" +
                "positionalArgs=" + positionalArgs +
                ", keywordArgs=" + keywordArgs +
                '}';
    }
}
