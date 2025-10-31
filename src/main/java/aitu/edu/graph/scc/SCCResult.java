package aitu.edu.graph.scc;

import java.util.List;

public class SCCResult {
    private final int[] componentIds;
    private final List<List<Integer>> components;

    public SCCResult(int[] componentIds, List<List<Integer>> components) {
        this.componentIds = componentIds;
        this.components = components;
    }

    public int[] getComponentIds() {
        return componentIds;
    }

    public List<List<Integer>> getComponents() {
        return components;
    }

    public int componentCount() {
        return components.size();
    }
}
