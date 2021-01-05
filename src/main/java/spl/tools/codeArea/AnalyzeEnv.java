package spl.tools.codeArea;

import java.util.HashMap;
import java.util.Map;

public class AnalyzeEnv {

    public static final AnaObject PLACEHOLDER = new AnaObject();
    private Map<String, AnaObject> variables = new HashMap<>();
    private AnalyzeEnv outer;

    AnalyzeEnv(AnalyzeEnv outer) {
        this.outer = outer;
    }

    public void put(String key, AnaObject value) {
        variables.put(key, value);
    }

    public boolean has(String key) {
        AnaObject v = variables.get(key);
        if (v == null) {
            if (outer == null) return false;
            else return outer.has(key);
        } else return true;
    }

    public static class ClassAnaEnv extends AnalyzeEnv {

        ClassAnaEnv(AnalyzeEnv outer) {
            super(outer);
        }
    }

    public static class AnaObject {

    }
}
