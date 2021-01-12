package spl.util;

import java.util.HashMap;
import java.util.Map;

public class MapMerger<K, V> {

    private final Map<K, V>[] maps;

    @SafeVarargs
    public MapMerger(Map<K, V>... maps) {
        this.maps = maps;
    }

    public Map<K, V> merge() {
        Map<K, V> merged = new HashMap<>();
        for (Map<K, V> map : maps) merged.putAll(map);
        return merged;
    }

    public Map<V, K> invert() {
        Map<V, K> inverted = new HashMap<>();
        for (Map<K, V> map : maps) {
            for (Map.Entry<K, V> entry : map.entrySet()) {
                inverted.put(entry.getValue(), entry.getKey());
            }
        }
        return inverted;
    }
}
