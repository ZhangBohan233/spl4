module spl {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens spl.tools;
    opens spl.ast;
    opens spl.util;

    exports spl;
    exports spl.interpreter;
    exports spl.interpreter.env;
    exports spl.interpreter.primitives;
    exports spl.interpreter.splObjects;
    exports spl.lexer.treeList;
    exports spl.tools;
    exports spl.ast;
    exports spl.util;
}