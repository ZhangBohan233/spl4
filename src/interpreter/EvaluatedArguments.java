package interpreter;

import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;

import java.util.*;

public class EvaluatedArguments {

    public final List<SplElement> positionalArgs = new ArrayList<>();
    public final Map<String, SplElement> keywordArgs = new HashMap<>();

    public static EvaluatedArguments of(SplElement... posArgs) {
        EvaluatedArguments arguments = new EvaluatedArguments();
        arguments.positionalArgs.addAll(Arrays.asList(posArgs));
        return arguments;
    }

    public static void insertThisPtr(EvaluatedArguments evaluatedArgs, Pointer thisPtr) {
        evaluatedArgs.positionalArgs.add(0, thisPtr);
    }
}
