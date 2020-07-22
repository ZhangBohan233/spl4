package interpreter.splObjects;

import interpreter.SplException;
import interpreter.env.Environment;
import interpreter.env.FunctionEnvironment;
import interpreter.primitives.SplElement;
import util.LineFile;

public abstract class UserFunction extends SplCallable {

    /**
     * The environment where this function is defined
     */
    protected final Environment definitionEnv;

    protected final Function.Parameter[] params;
    protected final LineFile lineFile;

    protected UserFunction(Function.Parameter[] parameters, Environment definitionEnv, LineFile lineFile) {
        this.params = parameters;
        this.definitionEnv = definitionEnv;
        this.lineFile = lineFile;
    }


    void putArgsToScope(SplElement[] evaluatedArgs, FunctionEnvironment scope) {
        for (int i = 0; i < params.length; ++i) {
            Function.Parameter param = params[i];
            String paramName = param.name;

            if (param.constant) scope.defineConst(paramName, lineFile);
            else scope.defineVar(paramName, lineFile);  // declare param

            if (i < evaluatedArgs.length) {
                // arg from call
                scope.setVar(paramName, evaluatedArgs[i], lineFile);

            } else if (param.hasDefaultValue()) {
                // default arg
                scope.setVar(paramName, param.defaultValue, lineFile);
            } else {
                throw new SplException("Unexpect argument error. ", lineFile);
            }
        }
    }

    @Override
    public int minArgCount() {
        int c = 0;
        for (SplCallable.Parameter param : params) {
            if (!param.hasDefaultValue()) c++;
        }
        return c;
    }

    @Override
    public int maxArgCount() {
        return params.length;  // TODO: check unpack
    }
}
