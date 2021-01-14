package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.Undefined;
import spl.interpreter.splObjects.Function;
import spl.interpreter.splObjects.SplCallable;
import spl.interpreter.splObjects.SplMethod;
import spl.util.BytesIn;
import spl.util.BytesOut;
import spl.util.LineFilePos;
import spl.util.Reconstructor;

import java.io.IOException;

public class FuncDefinition extends Expression {

    public final NameNode name;
    private final Line parameters;
    private final BlockStmt body;

    public FuncDefinition(NameNode name, Line parameters, BlockStmt body, LineFilePos lineFile) {
        super(lineFile);

        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }

    public static FuncDefinition reconstruct(BytesIn is, LineFilePos lineFilePos) throws Exception {
        NameNode name = Reconstructor.reconstruct(is);
        Line params = Reconstructor.reconstruct(is);
        BlockStmt body = Reconstructor.reconstruct(is);
        return new FuncDefinition(name, params, body, lineFilePos);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        Function.Parameter[] params = SplCallable.evalParams(parameters, env);
        if (env.hasException()) return Undefined.ERROR;

        Function function = new Function(body, params, env, name.getName(), getLineFile());
        Reference funcPtr = env.getMemory().allocateFunction(function, env);

        env.defineFunction(name.getName(), funcPtr, getLineFile());
        return funcPtr;
    }

    public SplElement evalAsMethod(Environment classDefEnv) {
        Function.Parameter[] oldParams = SplCallable.evalParams(parameters, classDefEnv);
        if (classDefEnv.hasException()) return Undefined.ERROR;
        assert oldParams != null;
        Function.Parameter[] params = SplCallable.insertThis(oldParams);


        SplMethod function = new SplMethod(body, params, classDefEnv, name.getName(), getLineFile());

        return classDefEnv.getMemory().allocateFunction(function, classDefEnv);
    }

    @Override
    public String toString() {
        if (name == null)
            return String.format("fn(%s): %s", parameters, body);
        else
            return String.format("fn %s(%s): %s", name, parameters, body);
    }

    @Override
    public String reprString() {
        return "fn " + name;
    }

    public Line getParameters() {
        return parameters;
    }

    public BlockStmt getBody() {
        return body;
    }

    public NameNode getName() {
        return name;
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        name.save(out);
        parameters.save(out);
        body.save(out);
    }
}
