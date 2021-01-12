package spl.lexer;

import spl.lexer.treeList.CollectiveElement;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class TextProcessResult {

    public final CollectiveElement rootList;
    public final LinkedHashMap<String, CollectiveElement> importedPaths;

    public TextProcessResult(CollectiveElement rootList) {
        this.rootList = rootList;
        this.importedPaths = new LinkedHashMap<>();
    }

    public TextProcessResult(CollectiveElement rootList, LinkedHashMap<String, CollectiveElement> importedPaths) {
        this.rootList = rootList;
        this.importedPaths = importedPaths;
    }
}
