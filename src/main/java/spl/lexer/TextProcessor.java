package spl.lexer;

import spl.lexer.treeList.*;
import spl.util.LineFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TextProcessor {

    private final CollectiveElement root;
    private final boolean importLang;
    private final Map<String, CollectiveElement> importedPaths;
    private final Map<String, ProcessorModule> importedModules = new HashMap<>();
    private final ProcessorModule module = new ProcessorModule();

    public TextProcessor(TokenizeResult tokenizeResult, boolean importLang) {
        this.root = tokenizeResult.rootList;
        this.importLang = importLang;
        this.importedPaths = new HashMap<>();
    }

    private TextProcessor(TokenizeResult tokenizeResult, boolean importLang,
                         TextProcessor parent) {
        this.root = tokenizeResult.rootList;
        this.importLang = importLang;
        this.importedPaths = parent.importedPaths;
    }

    public TextProcessResult process() throws IOException {
        CollectiveElement result = processBlock(root, null);
        return new TextProcessResult(result, importedPaths);
    }

    private CollectiveElement processBlock(CollectiveElement collectiveElement, CollectiveElement parentOfResult)
            throws IOException {
        CollectiveElement resultEle;
        if (collectiveElement instanceof BraceList)
            resultEle = new BraceList(parentOfResult, collectiveElement.lineFile);
        else if (collectiveElement instanceof BracketList)
            resultEle = new BracketList(parentOfResult, collectiveElement.lineFile);
        else if (collectiveElement instanceof SqrBracketList)
            resultEle = new SqrBracketList(parentOfResult, collectiveElement.lineFile);
        else
            throw new SyntaxError("Unexpected element. ", collectiveElement.lineFile);

        int i = 0;
        while (i < collectiveElement.size()) {
            i = processOne(collectiveElement, i, resultEle);
        }
        return resultEle;
    }

    private int processOne(CollectiveElement parent, int index, CollectiveElement resultEle)
            throws IOException {
        Element ele = parent.get(index++);
        if (ele instanceof AtomicElement) {
            Token token = ((AtomicElement) ele).atom;
            LineFile lineFile = token.lineFile;
            if (token instanceof IdToken) {
                String id = ((IdToken) token).getIdentifier();
                Element next;
                switch (id) {
                    case "macro":
                        String macroName =
                                ((IdToken) ((AtomicElement) parent.get(index++)).atom).getIdentifier();
                        BraceList bodyList = (BraceList) parent.get(index++);
                        BraceList processedBody = (BraceList) processBlock(bodyList, null);
                        MacroMatcher matcher = new MacroMatcher(macroName, processedBody);
                        module.matcherMap.put(macroName, matcher);
                        return index;
                    case "syntax":
                        BracketList conditionList = new BracketList(null, lineFile);
                        while (!((next = parent.get(index++)) instanceof BraceList)) {
                            conditionList.add(next);
                        }
                        bodyList = (BraceList) next;
                        processedBody = (BraceList) processBlock(bodyList, null);
                        resultEle.add(processedBody);
                        return index;
                    case "import":
                        AtomicElement probNamespaceEle = (AtomicElement) parent.get(index++);
                        AtomicElement importEle;
                        boolean nameSpace = false;
                        if (probNamespaceEle.atom instanceof IdToken) {
                            String probNs = ((IdToken) probNamespaceEle.atom).getIdentifier();
                            if (probNs.equals("namespace")) {
                                importEle = (AtomicElement) parent.get(index++);
                                nameSpace = true;
                            } else {
                                importEle = probNamespaceEle;
                            }
                        } else {
                            importEle = probNamespaceEle;
                        }

                        String path, importName;
                        // This step can be optimized, but does not due to simplicity.
                        if (importEle.atom instanceof IdToken) {
                            // Library import
                            importName = ((IdToken) importEle.atom).getIdentifier();
                            path = "lib" + File.separator + importName + ".sp";
                        } else if (importEle.atom instanceof StrToken) {
                            // User file import
                            path = lineFile.getFile().getParentFile().getAbsolutePath() +
                                    File.separator +
                                    ((StrToken) importEle.atom).getLiteral();
                            importName = nameOfPath(path);
                        } else {
                            throw new SyntaxError("Import name must either be a name or a String.",
                                    lineFile);
                        }

                        File fileImporting = new File(path);
                        if (fileImporting.equals(lineFile.getFile())) {
                            return index + 1;  // self importing, do not import
                        }

                        if (index + 2 < parent.size()) {
                            next = parent.get(index);
                            if (next instanceof AtomicElement &&
                                    ((AtomicElement) next).atom instanceof IdToken &&
                                    ((IdToken) ((AtomicElement) next).atom).getIdentifier().equals("as")) {
                                AtomicElement customNameEle = (AtomicElement) parent.get(index + 1);
                                index += 2;
                                importName = ((IdToken) customNameEle.atom).getIdentifier();
                            }
                        }

                        CollectiveElement importedList = importedPaths.get(path);
                        if (importedList == null) {
                            FileTokenizer fileTokenizer =
                                    new FileTokenizer(fileImporting, importLang);
                            TextProcessResult fileRes =
                                    new TextProcessor(fileTokenizer.tokenize(), importLang, this).process();
                            importedPaths.put(path, fileRes.rootList);
                        }

                        resultEle.add(new AtomicElement(new IdToken("import", lineFile), resultEle));
                        resultEle.add(new AtomicElement(new IdToken(path, lineFile), resultEle));
                        resultEle.add(new AtomicElement(new IdToken(importName, lineFile), resultEle));
                        resultEle.add(new AtomicElement(new IdToken(";", lineFile), resultEle));

                        if (nameSpace) {
                            resultEle.add(new AtomicElement(new IdToken("namespace", lineFile), resultEle));
                            resultEle.add(new AtomicElement(new IdToken(importName, lineFile), resultEle));
                            resultEle.add(new AtomicElement(new IdToken(";", lineFile), resultEle));
                        }

                        importedModules.put(importName, module);

                        return index;
                }
            }
            resultEle.add(ele);
        } else if (ele instanceof CollectiveElement) {
            CollectiveElement collectiveElement = (CollectiveElement) ele;
            CollectiveElement result = processBlock(collectiveElement, resultEle);
            resultEle.add(result);
        } else {
            throw new SyntaxError("Unexpected element. ", parent.lineFile);
        }
        return index;
    }

    private static String nameOfPath(String path) {
        path = path.replace("/", File.separator);
        path = path.replace("\\", File.separator);
        if (path.endsWith(".sp")) path = path.substring(0, path.length() - 3);
        if (path.contains(File.separator)) {
            return path.substring(path.lastIndexOf(File.separator) + 1);
        } else {
            return path;
        }
    }

    static class MacroMatcher {
        private final String macroName;
        private final List<MacroSyntax> syntaxList = new ArrayList<>();

        MacroMatcher(String macroName, BraceList body) {
            this.macroName = macroName;
            parseBody(body);
        }

        private void parseBody(BraceList body) {

        }
    }

    static class MacroSyntax {

    }

    static class ProcessorModule {
        private final Map<String, MacroMatcher> matcherMap = new HashMap<>();


    }
}
