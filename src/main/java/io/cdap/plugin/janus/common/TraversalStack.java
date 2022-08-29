package io.cdap.plugin.janus.common;

import java.util.Optional;
import java.util.Stack;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

public class TraversalStack {
    private final Stack<GraphTraversal<?, ?>> traversalStack = new Stack<>();

    public Optional<GraphTraversal<?, ?>> getCurrentTraversal() {
        if (traversalStack.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(traversalStack.pop());
    }

    public void pushNewTraversal(GraphTraversal<?, ?> graphTraversal) {
        traversalStack.push(graphTraversal);
    }
}
