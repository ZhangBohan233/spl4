package spl.util;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Configs {

    public static final String CONFIG_NAME = "config.ini";
    private static final Map<String, String> map = new HashMap<>(Map.of(
            "stackLimit", "512",
            "heapSize", "8192",
            "contract", "true"
    ));

    public static void load() {
        try {
            readCfgFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        return map.get(key);
    }

    public static int getInt(String key, int defaultValue) {
        String obj = get(key);
        try {
            return Integer.parseInt(obj);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String obj = get(key);
        return switch (obj) {
            case "true" -> true;
            case "false" -> false;
            default -> defaultValue;
        };
    }

    private static void readCfgFile() throws IOException {
        File cfgFile = new File(CONFIG_NAME);
        if (cfgFile.exists()) {
            BufferedReader br = new BufferedReader(new FileReader(CONFIG_NAME));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    map.put(parts[0].strip(), parts[1].strip());
                }
            }
            br.close();
        } else {
            BufferedWriter bw = new BufferedWriter(new FileWriter(CONFIG_NAME));
            for (Map.Entry<String, String> entry : map.entrySet()) {
                bw.write(entry.getKey() + "=" + entry.getValue());
                bw.newLine();
            }
            bw.flush();
            bw.close();
        }
    }
}
