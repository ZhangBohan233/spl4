package spl;

import spl.ast.BlockStmt;
import spl.parser.ParseResult;
import spl.util.BytesIn;
import spl.util.Reconstructor;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;

public class CacheReconstructor {

    private final String cacheFileName;
    private String srcAbsPath;
    private LinkedHashMap<String, ParseResult> parsedModules;

    public CacheReconstructor(String cacheFileName) {
        this.cacheFileName = cacheFileName;
    }

    public ParseResult reconstruct() throws Exception {
        BytesIn bis = new BytesIn(new FileInputStream(cacheFileName));
        byte[] head = new byte[8];
        if (bis.read(head) != 8) {
            bis.close();
            throw new IOException("Cannot read head");
        }
        int version = head[0] & 0xff;
        if (version != SplCacheSaver.VERSION) {
            bis.close();
            throw new IllegalArgumentException("Cannot decode compiled spl file: version does not match");
        }
        srcAbsPath = bis.readString();

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

    public String getSrcAbsPath() {
        return srcAbsPath;
    }
}
