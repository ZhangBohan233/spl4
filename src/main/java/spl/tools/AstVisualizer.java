package spl.tools;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import spl.ast.*;
import spl.util.LineFilePos;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class AstVisualizer implements Initializable {

    @FXML
    TreeTableView<TreeNode> treeView;

    @FXML
    TreeTableColumn<TreeNode, String> typeCol, valueCol, msgCol, lineCol, fileCol;

    private Map<String, BlockStmt> importedModules;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        typeCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("type"));
        valueCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("value"));
        msgCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("msg"));
        lineCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("line"));
        fileCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("file"));
    }

    public void setup(Node node, Map<String, BlockStmt> importedModules) {
        this.importedModules = importedModules;

        fill(node, null, "Program root");
    }

    private void fill(Node node, TreeItem<TreeNode> parentItem, String msg) {
        if (node == null) return;
        TreeNode tn = new TreeNode(
                node.getClass().getSimpleName(),
                node.reprString(),
                msg,
                node.lineFile);
        TreeItem<TreeNode> treeItem = new TreeItem<>(tn);
        if (parentItem == null) {
            treeView.setRoot(treeItem);
        } else {
            parentItem.getChildren().add(treeItem);
        }

        if (node instanceof BlockStmt) {
            for (Line line : ((BlockStmt) node).getLines()) {
                fill(line, treeItem, "");
            }
        } else if (node instanceof Line) {
            for (Node part : ((Line) node).getChildren()) {
                fill(part, treeItem, "");
            }
        } else if (node instanceof FuncDefinition) {
            FuncDefinition fd = (FuncDefinition) node;
            fill(fd.getParameters(), treeItem, "params");
            fill(fd.getBody(), treeItem, "function body");
        } else if (node instanceof ClassStmt) {
            ClassStmt cs = (ClassStmt) node;
            if (cs.getSuperclassesNodes() != null)
                for (Node scn : cs.getSuperclassesNodes()) {
                    fill(scn, treeItem, "superclass");
                }
            fill(cs.getBody(), treeItem, "class body");
        } else if (node instanceof BinaryExpr) {
            BinaryExpr be = (BinaryExpr) node;
            fill(be.getLeft(), treeItem, "left");
            fill(be.getRight(), treeItem, "right");
        } else if (node instanceof UnaryExpr) {
            UnaryExpr ue = (UnaryExpr) node;
            fill(ue.getValue(), treeItem, "value");
        } else if (node instanceof UnaryStmt) {
            UnaryStmt ue = (UnaryStmt) node;
            fill(ue.getValue(), treeItem, "value");
        } else if (node instanceof ImportStmt) {
            BlockStmt importBody = importedModules.get(((ImportStmt) node).getPath());
            fill(importBody, treeItem, "imported module");
        } else if (node instanceof ContractNode) {
            ContractNode cn = (ContractNode) node;
            fill(cn.getParamContracts(), treeItem, "param contracts");
            fill(cn.getRtnContract(), treeItem, "return contract");
        }
    }

    public static class TreeNode {
        private final String type;
        private final String value;
        private final String msg;
        private final LineFilePos lineFile;

        private TreeNode(String type, String value, String msg, LineFilePos lineFile) {
            this.type = type;
            this.value = value;
            this.msg = msg;
            this.lineFile = lineFile;
        }

        @FXML
        public String getType() {
            return type;
        }

        @FXML
        public String getValue() {
            return value;
        }

        @FXML
        public String getMsg() {
            return msg;
        }

        @FXML
        public String getLine() {
            return String.valueOf(lineFile.getLine());
        }

        @FXML
        public String getFile() {
            if (lineFile.getFile() == null) return lineFile.getMsg();
            else return lineFile.getFile().toString();
        }
    }
}
