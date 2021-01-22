package spl.tools;

import spl.CacheReconstructor;
import spl.ast.*;
import spl.parser.ParseResult;
import spl.util.Utilities;

import java.io.*;
import java.util.List;

/**
 * Decompiles an abstract syntax tree to a readable source code.
 * <p>
 * The decompiled result may be different in format from the actual source file.
 */
public class Decompiler {

    private final ParseResult parseResult;
    private final Writer output;
    private final StringWriter writer = new StringWriter();
    private int indentFactor = 4;
    private int indent = -1;

    public Decompiler(ParseResult parseResult, Writer output) {
        this.parseResult = parseResult;
        this.output = output;
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("No file specified");
            return;
        }
        String fileName = args[0];
        decompiledAndPrint(fileName);
    }

    public static void decompiledAndPrint(String spcFileName) throws Exception {
        decompileToStream(spcFileName, new OutputStreamWriter(System.out));
    }

    public static void decompileToFile(String spcFileName, String targetFileName) throws Exception {
        decompileToStream(spcFileName, new FileWriter(targetFileName));
    }

    public static void decompileToStream(String spcFileName, Writer writer) throws Exception {
        CacheReconstructor reconstructor = new CacheReconstructor(spcFileName);
        ParseResult parseResult = reconstructor.reconstruct();

//        System.out.println(parseResult.getRoot());
//        System.out.println("===== End of AST =====");

        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        Decompiler decompiler = new Decompiler(parseResult, bufferedWriter);
        decompiler.decompile();
        bufferedWriter.flush();
        bufferedWriter.close();
    }

    public void setIndentFactor(int indentFactor) {
        this.indentFactor = indentFactor;
    }

    public void decompile() throws IOException {
        writer.write("/* Decompiled SPL file */");
        writer.newLine();
        decompileOne(parseResult.getRoot());
        writer.flush();
    }

    private void decompileOne(Node node) throws IOException {
        if (node == null) return;
        if (node instanceof NameNode) {
            writer.write(((NameNode) node).getName());
        } else if (node instanceof BlockStmt) {
            if (indent >= 0) {
                writer.writeNoSpace("{");
            }
            indent++;
            for (Line line : ((BlockStmt) node).getLines()) {
                if (!line.getChildren().isEmpty()) {
                    writer.newLine();
                    decompileOne(line);
                    writer.trimSpace();
                    writer.writeNoSpace(";");
                }
            }
            indent--;
            if (indent >= 0) {
                writer.newLine();
                writer.write("}");
                writer.newLine();
            }
        } else if (node instanceof Line) {
            Line line = (Line) node;
            int lineSize = line.size();
            if (lineSize > 0) {
                for (int i = 0; i < lineSize; i++) {
                    decompileOne(line.get(i));
                    writer.trimSpace();
                }
            }
        } else if (node instanceof Declaration) {
            Declaration dec = (Declaration) node;
            writer.write(dec.getLevelString() + " " + dec.declaredName);
        } else if (node instanceof Dot) {
            Dot dot = (Dot) node;
            decompileOne(dot.getLeft());
            writer.trimSpace();
            writer.writeNoSpace(dot.getOperator());
            decompileOne(dot.getRight());
        } else if (node instanceof BinaryExpr) {
            BinaryExpr be = (BinaryExpr) node;
            decompileOne(be.getLeft());
            writer.write(be.getOperator());
            decompileOne(be.getRight());
        } else if (node instanceof UnaryBuildable) {
            UnaryBuildable ue = (UnaryBuildable) node;
            if (ue.operatorAtLeft()) {
                writer.write(ue.getOperator());
                decompileOne(ue.getValue());
            } else {
                decompileOne(ue.getValue());
                writer.write(ue.getOperator());
            }
        } else if (node instanceof IntLiteral) {
            writer.write(String.valueOf(((IntLiteral) node).getValue()));
        } else if (node instanceof CharLiteral) {
            writer.write(String.valueOf(((CharLiteral) node).getValue()));
        } else if (node instanceof FloatLiteral) {
            writer.write(String.valueOf(((FloatLiteral) node).getValue()));
        } else if (node instanceof BoolLiteral) {
            writer.write(String.valueOf(((BoolLiteral) node).getValue()));
        } else if (node instanceof StringLiteralRef) {
            writer.write('"' + ((StringLiteralRef) node).getLiteralString() + '"');
        } else if (node instanceof ByteLiteral) {
            writer.write(((ByteLiteral) node).getValue() + "b");
        } else if (node instanceof NullExpr) {
            writer.write("null");
        } else if (node instanceof FuncDefinition) {
            FuncDefinition fd = (FuncDefinition) node;
            writer.write("fn");
            writer.writeNoSpace(fd.getName().getName());
            writeParamsOrArgs(fd.getParameters().getChildren());
            decompileOne(fd.getBody());
            writer.newLine();
        } else if (node instanceof ContractNode) {
            ContractNode cn = (ContractNode) node;
            writer.write("contract");
            writer.writeNoSpace(cn.getFnName());
            writeParamsOrArgs(cn.getParamContracts().getChildren());
            writer.write("->");
            decompileOne(cn.getRtnContract());
            writer.trimSpace();
            writer.write(";");
            writer.newLine();
            writer.newLine();
        } else if (node instanceof ClassStmt) {
            ClassStmt classStmt = (ClassStmt) node;
            writer.write("class");
            writer.writeNoSpace(classStmt.getClassName());
            if (classStmt.getTemplates() != null) {
                writeTemplates(classStmt.getTemplates());
            }
            if (classStmt.getSuperclassesNodes() != null) {
                writeParamsOrArgs(classStmt.getSuperclassesNodes());
                writer.trimSpace();
            }
            writer.write("");
            decompileOne(classStmt.getBody());
            writer.newLine();
        } else if (node instanceof FuncCall) {
            FuncCall fc = (FuncCall) node;
            decompileOne(fc.getCallObj());
            writer.trimSpace();
            writeParamsOrArgs(fc.getArguments().getLine().getChildren());
        } else if (node instanceof GenericNode) {
            GenericNode gn = (GenericNode) node;
            decompileOne(gn.getObj());
            writer.trimSpace();
            writeTemplates(gn.getGenericLine().getChildren());
        } else if (node instanceof ImportStmt) {
            ImportStmt is = (ImportStmt) node;
            writer.write("import");
            if (!is.getPath().startsWith("lib" + File.separator)) {
                String relPath = Utilities.representFileFromFile(is.getLineFile().getFile(), new File(is.getPath()));
                writer.write('"' + relPath + '"');
                writer.write("as");
            }
            writer.write(is.getImportName());
        }
    }

    private void writeParamsOrArgs(List<Node> line) throws IOException {
        writer.writeNoSpace("(");
        writeSimpleLine(line);
        writer.write(")");
    }

    private void writeTemplates(List<Node> line) throws IOException {
        writer.writeNoSpace("<");
        writeSimpleLine(line);
        writer.writeNoSpace(">");
    }

    private void writeSimpleLine(List<Node> line) throws IOException {
        int paramCount = line.size();
        if (paramCount > 0) {
            for (int i = 0; i < paramCount; i++) {
                decompileOne(line.get(i));
                writer.trimSpace();
                if (i != paramCount - 1) writer.writeNoSpace(", ");
            }
        }
    }

    private class StringWriter {
        private final StringBuilder lineBuilder = new StringBuilder();

        void write(String s) {
            lineBuilder.append(s).append(' ');
        }

        void writeNoSpace(String s) {
            lineBuilder.append(s);
        }

        void trimSpace() {
            if (lineBuilder.length() > 0 && lineBuilder.charAt(lineBuilder.length() - 1) == ' ') {
                lineBuilder.setLength(lineBuilder.length() - 1);
            }
        }

        void newLine() throws IOException {
            if (lineBuilder.length() > 0) {
                lineBuilder.append("\n");
                output.write(lineBuilder.toString());
                lineBuilder.setLength(0);
            } else {
                output.write("\n");
            }
            if (indent >= 0)
                output.write(" ".repeat(indent * indentFactor));
        }

        void flush() throws IOException {
            if (lineBuilder.length() > 0) {
                if (lineBuilder.length() > 1 || lineBuilder.charAt(0) != ';') {
                    output.write(lineBuilder.toString());
                }
                lineBuilder.setLength(0);
            }
        }
    }
}
