package aitu.edu.graph.scc;

import java.util.List;

/**
 * Represents the result of strongly connected components computation, including component IDs and lists.
 */
public class SCCResult {
    private final int[] componentIds;
    private final List<List<Integer>> components;

    /**
     * Constructs an SCCResult with component IDs and component lists.
     *
     * @param componentIds the array mapping each node to its component ID
     * @param components   the list of lists, each containing nodes in a component
     */
    public SCCResult(int[] componentIds, List<List<Integer>> components) {
        this.componentIds = componentIds;
        this.components = components;
    }

    /**
     * Returns the component ID for each node.
     *
     * @return the component IDs array
     */
    public int[] getComponentIds() {
        return componentIds;
    }

    /**
     * Returns the list of components.
     *
     * @return the list of component lists
     */
    public List<List<Integer>> getComponents() {
        return components;
    }

    /**
     * Returns the number of components.
     *
     * @return the component count
     */
    public int componentCount() {
        return components.size();
    }
}
