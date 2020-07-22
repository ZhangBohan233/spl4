package interpreter.splObjects;

import ast.*;
import interpreter.SplException;
import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import parser.ParseError;
import util.LineFile;

public abstract class SplCallable extends SplObject {

    public abstract SplElement call(SplElement[] evaluatedArgs, Environment callingEnv, LineFile lineFile);

    public SplElement call(Arguments arguments, Environment callingEnv) {
        return call(arguments.evalArgs(callingEnv), callingEnv, arguments.getLineFile());
    }

    public abstract int minArgCount();

    public abstract int maxArgCount();

    protected void checkValidArgCount(int argc, String fnName) {
        int leastArg = minArgCount();
        int mostArg = maxArgCount();
        if (argc < leastArg || argc > mostArg) {
            if (leastArg == mostArg) {
                throw new SplException(
                        String.format("Function '%s' expects %d argument(s), got %d. ",
                                fnName, leastArg, argc));
            } else {
                throw new SplException(
                        String.format("Function '%s' expects %d to %d arguments, got %d. ",
                                fnName, leastArg, mostArg, argc));
            }
        }
    }

    public static Parameter[] evalParams(Line parameters, Environment env) {
        Parameter[] params = new Parameter[parameters.getChildren().size()];

        // after first
        boolean optionalBegins = false;

        for (int i = 0; i < parameters.getChildren().size(); ++i) {
            Node node = parameters.getChildren().get(i);

            Parameter param = evalOneParam(node, env, optionalBegins).build();
            if (param.hasDefaultValue()) optionalBegins = true;

            params[i] = param;
        }
        return params;
    }

    // Note that for internal recursive calls, optionalBegins is always false since it only used to throw error
    private static ParameterBuilder evalOneParam(Node node, Environment env, boolean optionalBegins) {
        if (node instanceof NameNode) {
            if (optionalBegins) {
                throw new ParseError("Positional parameter cannot occur behind optional parameter. ",
                        node.getLineFile());
            }
            return new ParameterBuilder().name(((NameNode) node).getName());
        } else if (node instanceof Declaration) {
            Declaration dec = (Declaration) node;
            return new ParameterBuilder()
                    .name(dec.declaredName)
                    .constant(dec.level == Declaration.CONST);
        } else if (node instanceof Assignment) {
            Assignment assignment = (Assignment) node;
            ParameterBuilder left = evalOneParam(assignment.getLeft(), env, false);
            return left.defaultValue(assignment.getRight().evaluate(env));
        }
        throw new ParseError("Unexpected parameter syntax. ", node.getLineFile());
    }

    private static class ParameterBuilder {
        private String name;
        private SplElement defaultValue;
        private boolean constant;

        private ParameterBuilder name(String name) {
            this.name = name;
            return this;
        }

        private ParameterBuilder constant(boolean constant) {
            this.constant = constant;
            return this;
        }

        private ParameterBuilder defaultValue(SplElement defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        private Parameter build() {
            return new Parameter(name, defaultValue, constant);
        }
    }

    public static class Parameter {
        public final String name;
        public final SplElement defaultValue;
        public final boolean constant;

        Parameter(String name, SplElement typeValue, boolean constant) {
            this.name = name;
            this.defaultValue = typeValue;
            this.constant = constant;
        }

        public boolean hasDefaultValue() {
            return defaultValue != null;
        }

        @Override
        public String toString() {
            String prefix = constant ? "const " : "";
            String suffix = hasDefaultValue() ? "=" + defaultValue : "";
            return prefix + name + suffix;
        }
    }
}
