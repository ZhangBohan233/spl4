package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.SplElement;
import spl.util.*;

import java.io.IOException;

public class AnonymousClassExpr extends Expression {

    private final FuncCall call;
    private final BlockStmt body;
    private final int anonymousId;

    public AnonymousClassExpr(FuncCall call, BlockStmt body, int anonymousId, LineFilePos lineFile) {
        super(lineFile);

        this.call = call;
        this.body = body;
        this.anonymousId = anonymousId;

        insertConstructor();
    }

    public static AnonymousClassExpr reconstruct(BytesIn in, LineFilePos lineFilePos) throws Exception {
        FuncCall call = Reconstructor.reconstruct(in);
        BlockStmt body = Reconstructor.reconstruct(in);
        int id = in.readInt();
        return new AnonymousClassExpr(call, body, id, lineFilePos);
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        call.save(out);
        body.save(out);
        out.writeInt(anonymousId);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return SplInvokes.throwExceptionWithError(
                env,
                Constants.RUNTIME_SYNTAX_ERROR,
                "Cannot evaluate an anonymous class creation directly.",
                lineFile
        );
    }

    private void insertConstructor() {
        BlockStmt initBody = new BlockStmt(lineFile);
        Dot dot = new Dot(lineFile);
        dot.setLeft(new NameNode(Constants.SUPER, lineFile));
        dot.setRight(new FuncCall(new NameNode(Constants.CONSTRUCTOR, lineFile), call.getArguments(), lineFile));
        Line line = new Line(lineFile, dot);
        initBody.addLine(line);

        FuncDefinition init = new FuncDefinition(
                new NameNode(Constants.CONSTRUCTOR, lineFile),
                new Line(lineFile),
                initBody,
                lineFile
        );

        body.getLines().add(0, new Line(lineFile, init));
    }

    @Override
    public String toString() {
        return "AnonymousClass{" + call + " <- " + body + '}';
    }

    public String getAnonymousName() {
        return "ac-" + anonymousId;
    }

    public FuncCall getCall() {
        return call;
    }

    public BlockStmt getBody() {
        return body;
    }
}
