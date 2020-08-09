package interpreter.splObjects;

import ast.*;
import interpreter.EvaluatedArguments;
import interpreter.primitives.Pointer;
import interpreter.splErrors.NativeError;
import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import parser.ParseError;
import util.Constants;
import util.LineFile;

public abstract class SplCallable extends SplObject {

    public abstract SplElement call(EvaluatedArguments evaluatedArgs, Environment callingEnv, LineFile lineFile);

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
                throw new NativeError(
                        String.format("Function '%s' expects %d argument(s), got %d. ",
                                fnName, leastArg, argc));
            } else {
                throw new NativeError(
                        String.format("Function '%s' expects %d to %d arguments, got %d. ",
                                fnName, leastArg, mostArg, argc));
            }
        }
    }

    public static Parameter[] evalParams(Line parameters, Environment env) {
        Parameter[] params = new Parameter[parameters.getChildren().size()];

        // after first
        int optionalLevel = 0;

        for (int i = 0; i < parameters.getChildren().size(); ++i) {
            Node node = parameters.getChildren().get(i);

            Parameter param = evalOneParam(node, env, optionalLevel).build();
            if (param.hasDefaultValue()) {
                optionalLevel = 1;
            } else if (param.noneAble()) {
                optionalLevel = param.unpackCount + 1;
            }

            params[i] = param;
        }
        return params;
    }

    public static Parameter[] insertThis(Parameter[] params) {
        Parameter[] res = new Parameter[params.length + 1];
        res[0] = new Parameter(Constants.THIS, null, true, 0);
        System.arraycopy(params, 0, res, 1, params.length);
        return res;
    }

    /**
     * Note that for internal recursive calls, optionalLevel is always 0 since it only used to throw error
     *
     * @param node          the node to be evaluated
     * @param env           the function definition environment
     * @param optionalLevel a level controller,
     *                      0 if previous params are all regular
     *                      1 if there are at least one optional param occurs
     *                      2 if *args occurs
     *                      3 if **kwargs occurs
     * @return the param builder
     */
    private static ParameterBuilder evalOneParam(Node node, Environment env, int optionalLevel) {
        if (node instanceof NameNode) {
            if (optionalLevel > 0) {
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
            if (optionalLevel > 1) {
                throw new ParseError("Optional parameter cannot occur behind *args/**kwargs. ",
                        node.getLineFile());
            }
            Assignment assignment = (Assignment) node;
            ParameterBuilder left = evalOneParam(assignment.getLeft(), env, 0);
            return left.defaultValue(assignment.getRight().evaluate(env));
        } else if (node instanceof StarExpr) {
            ParameterBuilder builder = evalOneParam(((StarExpr) node).getValue(), env, 0).unpack();
            if (optionalLevel >= builder.unpackCount + 1) {
                throw new ParseError("*args cannot occurs behind another *args or **kwargs. ",
                        node.getLineFile());
            }
            return builder;
        }
        throw new ParseError("Unexpected parameter syntax. ", node.getLineFile());
    }

    private static class ParameterBuilder {
        private String name;
        private SplElement defaultValue;
        private boolean constant;
        private int unpackCount = 0;

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

        private ParameterBuilder unpack() {
            this.unpackCount++;
            return this;
        }

        private Parameter build() {
            if (unpackCount > 0 && defaultValue != null)
                throw new NativeError("Unexpected parameter combination. ");
            return new Parameter(name, defaultValue, constant, unpackCount);
        }
    }

    public static class Parameter {
        public final String name;
        public final SplElement defaultValue;
        public final boolean constant;
        public final int unpackCount;
        Pointer contract;

        Parameter(String name, SplElement defaultValue, boolean constant, int unpackCount) {
            this.name = name;
            this.defaultValue = defaultValue;
            this.constant = constant;
            this.unpackCount = unpackCount;
        }

        public boolean hasDefaultValue() {
            return defaultValue != null;
        }

        private boolean noneAble() {
            return unpackCount > 0;
        }

        @Override
        public String toString() {
            String prefix = constant ? "const " : "";
            String suffix = hasDefaultValue() ? "=" + defaultValue : "";
            return prefix + name + suffix;
        }
    }
}
