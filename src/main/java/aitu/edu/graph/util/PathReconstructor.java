package aitu.edu.graph.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Helper to rebuild a path from a predecessor map: pred.get(v) = previous node on path to v.
 */
public class PathReconstructor {
    /**
     * Reconstruct path from src to dst using predecessor map. Returns empty list if no path.
     */
    public static List<Integer> reconstruct(Map<Integer, Integer> pred, int src, int dst) {
        List<Integer> rev = new ArrayList<>();
        Integer cur = dst;
        while (cur != null && cur != src) {
            rev.add(cur);
            cur = pred.get(cur);
        }
        if (cur == null) return Collections.emptyList();
        rev.add(src);
        Collections.reverse(rev);
        return rev;
    }
}