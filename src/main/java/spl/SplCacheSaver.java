package spl;

import spl.ast.BlockStmt;
import spl.parser.ParseResult;
import spl.util.BytesOut;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class SplCacheSaver {

    private final String cacheFileName;
    private final ParseResult parseResult;
    private final LinkedHashMap<String, ParseResult> parsedModules;

    public SplCacheSaver(String srcFileName,
                         ParseResult parseResult,
                         LinkedHashMap<String, ParseResult> parsedModules) {
        this.cacheFileName = srcFileName + "c";
        this.parseResult = parseResult;
        this.parsedModules = parsedModules;
    }

    public void save() {
        try {
            BytesOut bos = new BytesOut(new FileOutputStream(cacheFileName));

            bos.writeInt(parsedModules.size());
            for (Map.Entry<String, ParseResult> entry : parsedModules.entrySet()) {
                bos.writeString(entry.getKey());
                entry.getValue().getRoot().save(bos);
            }

            BlockStmt root = parseResult.getRoot();
            root.save(bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
