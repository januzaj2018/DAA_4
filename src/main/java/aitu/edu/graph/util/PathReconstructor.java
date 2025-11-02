package aitu.edu.graph.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Reconstructs paths from predecessor maps.
 */
public class PathReconstructor {

    /**
     * Reconstructs the path from src to dst using the predecessor map.
     *
     * @param pred the predecessor map
     * @param src  the source node
     * @param dst  the destination node
     * @return the list of nodes in the path, or empty if no path
     */
    public static List<Integer> reconstruct(Map<Integer, Integer> pred, int src, int dst) {
        List<Integer> rev = new ArrayList<>();
        Integer cur = dst;
        // Follow predecessors from dst back to src
        while (cur != null && cur != src) {
            rev.add(cur);
            cur = pred.get(cur);
        }
        if (cur == null) return Collections.emptyList();
        rev.add(src);
        // Reverse to get correct order
        Collections.reverse(rev);
        return rev;
    }
}