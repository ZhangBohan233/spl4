package spl.interpreter.splObjects;

import spl.ast.Arguments;
import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Bool;
import spl.interpreter.primitives.Reference;
import spl.util.Accessible;
import spl.util.LineFilePos;

/**
 * A class-like object in spl, including spl classes, native types, and fake native types.
 */
public interface ClassLike {

    /**
     * Return whether this class is the superclass of the only element in args.
     *
     * @param args        arguments
     * @param env         calling environment
     * @param lineFilePos calling position
     * @return {@code Bool.TRUE} iff this class is the superclass of the only argument.
     */
    @Accessible
    Bool __superclassOf__(Arguments args, Environment env, LineFilePos lineFilePos);

    /**
     * Returns the reference to the checker function, corresponded with this type.
     *
     * @param args        arguments, should be empty
     * @param env         calling environment
     * @param lineFilePos calling position
     * @return the reference to the checker function, corresponded with this type
     */
    @Accessible
    Reference __checker__(Arguments args, Environment env, LineFilePos lineFilePos);
}
