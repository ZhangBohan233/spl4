package spl;

import spl.ast.BlockStmt;
import spl.parser.ParseResult;
import spl.util.BytesIn;
import spl.util.Reconstructor;

import java.io.FileInputStream;
import java.util.LinkedHashMap;

public class CacheReconstructor {

    private final String cacheFileName;
    private LinkedHashMap<String, ParseResult> parsedModules;

    public CacheReconstructor(String cacheFileName) {
        this.cacheFileName = cacheFileName;
    }

    public ParseResult reconstruct() throws Exception {
        BytesIn bis = new BytesIn(new FileInputStream(cacheFileName));
        parsedModules = new LinkedHashMap<>();

        int modulesCount = bis.readInt();
        for (int i = 0; i < modulesCount; i++) {
            String modulePath = bis.readString();
            BlockStmt moduleRoot = Reconstructor.reconstruct(bis);
            parsedModules.put(modulePath, new ParseResult(moduleRoot));
        }

        BlockStmt root = Reconstructor.reconstruct(bis);

        bis.close();

        return new ParseResult(root);
    }

    public LinkedHashMap<String, ParseResult> getParsedModules() {
        return parsedModules;
    }
}
