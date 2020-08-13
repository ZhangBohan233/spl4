package spl.lexer;

import spl.lexer.treeList.CollectiveElement;

import java.util.HashMap;
import java.util.Map;

public class TextProcessResult {

    public final CollectiveElement rootList;
    public final Map<String, CollectiveElement> importedPaths;

    public TextProcessResult(CollectiveElement rootList) {
        this.rootList = rootList;
        this.importedPaths = new HashMap<>();
    }

    public TextProcessResult(CollectiveElement rootList, Map<String, CollectiveElement> importedPaths) {
        this.rootList = rootList;
        this.importedPaths = importedPaths;
    }
}
