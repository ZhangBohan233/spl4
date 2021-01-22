package spl.interpreter.splObjects;

import spl.ast.*;
import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.Undefined;
import spl.interpreter.splErrors.NativeError;
import spl.parser.ParseError;
import spl.util.Constants;
import spl.util.LineFilePos;

import java.util.HashSet;
import java.util.Set;

public abstract class SplCallable extends NativeObject {

    public static final int MAX_ARGS = 65535;

    public static Parameter[] evalParams(Line parameters, Environment env) {
        Parameter[] params = new Parameter[parameters.getChildren().size()];
        Set<String> usedNames = new HashSet<>();
        usedNames.add(Constants.THIS);

        // after first
        int optionalLevel = 0;

        for (int i = 0; i < parameters.getChildren().size(); ++i) {
            Node node = parameters.getChildren().get(i);

            ParameterBuilder paramB = evalOneParam(node, env, optionalLevel);
            if (paramB == null) return null;
            Parameter param = paramB.build();
            if (usedNames.contains(param.name)) {
                SplInvokes.throwException(
                        env,
                        Constants.PARAMETER_EXCEPTION,
                        "Duplicate parameter '" + param.name + "'",
                        node.getLineFile()
                );
                return null;
            }
            usedNames.add(param.name);
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
                SplInvokes.throwException(
                        env,
                        Constants.PARAMETER_EXCEPTION,
                        "Optional parameter cannot occur behind *args/**kwargs.",
                        node.getLineFile()
                );
                return null;
            }
            Assignment assignment = (Assignment) node;
            ParameterBuilder left = evalOneParam(assignment.getLeft(), env, 0);
            if (left == null) return null;
            SplElement defValue = assignment.getRight().evaluate(env);
            if (env.hasException()) {
                return null;
            }
            return left.defaultValue(defValue);
        } else if (node instanceof StarExpr) {
            ParameterBuilder builder = evalOneParam(((StarExpr) node).getValue(), env, 0);
            if (builder == null) return null;
            builder.unpack();
            if (optionalLevel >= builder.unpackCount + 1) {
                SplInvokes.throwException(
                        env,
                        Constants.PARAMETER_EXCEPTION,
                        "*args cannot occurs behind another *args or **kwargs.",
                        node.getLineFile()
                );
                return null;
            }
            return builder;
        }
        SplInvokes.throwException(
                env,
                Constants.PARAMETER_EXCEPTION,
                "Unexpected parameter syntax.",
                node.getLineFile()
        );
        return null;
    }

    public abstract SplElement call(EvaluatedArguments evaluatedArgs,
                                    Reference[] generics,
                                    Environment callingEnv,
                                    LineFilePos lineFilePos);

    public final SplElement call(EvaluatedArguments evaluatedArgs, Environment callingEnv, LineFilePos lineFile) {
        return call(evaluatedArgs, null, callingEnv, lineFile);
    }

    public SplElement call(Arguments arguments, Environment callingEnv) {
        var ea = arguments.evalArgs(callingEnv);
        if (callingEnv.hasException()) return Undefined.ERROR;
        return call(ea, callingEnv, arguments.getLineFile());
    }

    public abstract String getName();

    public abstract int minPosArgCount();

    public abstract int maxPosArgCount();

    public abstract int maxKwArgCount();

    protected void checkValidArgCount(int posArgs, int kwArgs, String fnName, Environment callingEnv, LineFilePos callingLf) {
        int leastPosArg = minPosArgCount();
        int mostPosArg = maxPosArgCount();
        int mostKwArg = maxKwArgCount();  // Note that minimum number of keyword arguments is always 0
        if (posArgs < leastPosArg || posArgs > mostPosArg) {
            if (leastPosArg == mostPosArg) {
                SplInvokes.throwException(
                        callingEnv,
                        Constants.ARGUMENT_EXCEPTION,
                        String.format("Function '%s' expects %d positional argument(s), got %d.",
                                fnName, leastPosArg, posArgs),
                        callingLf
                );
            } else {
                SplInvokes.throwException(
                        callingEnv,
                        Constants.ARGUMENT_EXCEPTION,
                        String.format("Function '%s' expects %d to %d positional argument(s), got %d.",
                                fnName, leastPosArg, mostPosArg, posArgs),
                        callingLf
                );
            }
        }
        if (kwArgs > mostKwArg) {
            SplInvokes.throwException(
                    callingEnv,
                    Constants.ARGUMENT_EXCEPTION,
                    String.format("Function '%s' expects at most %d keyword argument(s), got %d.",
                            fnName, mostKwArg, kwArgs),
                    callingLf
            );
        }
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
        /**
         * Unpack count, 0 for normal, 1 for *args, 2 for **kwargs
         */
        public final int unpackCount;
        Node contract;

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
