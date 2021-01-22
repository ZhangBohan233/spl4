package spl;

import spl.ast.BlockStmt;
import spl.parser.ParseResult;
import spl.util.BytesOut;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class SplCacheSaver {
    public static final int VERSION = 4;
    private final String srcAbsPath;
    private final String cacheFileName;
    private final ParseResult parseResult;
    private final LinkedHashMap<String, ParseResult> parsedModules;

    public SplCacheSaver(File srcFile,
                         ParseResult parseResult,
                         LinkedHashMap<String, ParseResult> parsedModules) {
        this.srcAbsPath = srcFile.getAbsolutePath();
        this.cacheFileName = srcAbsPath + "c";
        this.parseResult = parseResult;
        this.parsedModules = parsedModules;
    }

    public void save() {
        try {
            BytesOut bos = new BytesOut(new FileOutputStream(cacheFileName));

            byte[] head = new byte[8];
            head[0] = (byte) VERSION;
            bos.write(head);
            bos.writeString(srcAbsPath);

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
