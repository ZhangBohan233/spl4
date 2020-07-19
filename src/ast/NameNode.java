package ast;

import interpreter.SplException;
import interpreter.env.Environment;
import interpreter.env.EnvironmentError;
import interpreter.primitives.SplElement;
import interpreter.types.*;
import util.LineFile;

import java.util.List;

public class NameNode extends Node {
    private final String name;
//    private TemplateNode templateNode;

    public NameNode(String name, LineFile lineFile) {
        super(lineFile);
        this.name = name;
    }

    public String getName() {
        return name;
    }

//    public void setTemplateNode(TemplateNode templateNode) {
//        this.templateNode = templateNode;
//    }

//    public TemplateNode getTemplateNode() {
//        return templateNode;
//    }

    @Override
    public String toString() {
//        if (templateNode == null) {
        return "Name(" + name + ")";
//        } else {
//            return "Name(" + name + ")" + templateNode;
//        }
    }

    @Override
    protected SplElement internalEval(Environment env) {
//        System.out.println(env.get(name, getLineFile()) + name);
        return env.get(name, getLineFile());
    }

//    @Override
//    protected Type inferredType(Environment env) {
//        return env.get(name, getLineFile()).getType();
//    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NameNode && ((NameNode) obj).name.equals(name);
    }

//    @Override
//    public PointerType evalType(Environment environment) {
//        TypeValue typeValue = environment.get(name, getLineFile());
//        if (templateNode == null) {
//            return (PointerType) typeValue.getType();
//        } else {
//            // The template type must be actual types
//            if (typeValue.getType() instanceof ClassType) {
//                ClassType ct = (ClassType) typeValue.getType();
//                TypeValue[] templates = new TypeValue[templateNode.value.getChildren().size()];
//                for (int i = 0; i < templateNode.value.getChildren().size(); ++i) {
//                    Node tr = templateNode.value.getChildren().get(i);
//                    TypeValue tv;
//                    try {
//                        tv = tr.evaluate(environment);
//                    } catch (EnvironmentError ee) {
//                        if (tr instanceof NameNode) {
//                            tv = new TypeValue(new UndTemplateType(((NameNode) tr).getName()));
//                        } else {
//                            throw new SplException();
//                        }
//                    }
//                    templates[i] = tv;
//                }
//                ct.setTemplates(templates);
//                return ct;
//            } else {
//                throw new SplException("Only class type supports template. ", getLineFile());
//            }
//        }
//    }
}
