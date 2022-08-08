package io.cdap.plugin.janus.common;

import io.cdap.cdap.api.data.format.StructuredRecord;
import java.util.Map;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class GremlinQueryUtil {

    public static <S, E> GraphTraversal<S, E> traverseByPrimaryKey(GraphTraversal<S, E> graphTraversal, String label, Map<String, String> idProps, StructuredRecord record) {

        GraphTraversal<S, E> gTraversal = graphTraversal.hasLabel(label);

        if (idProps != null && !idProps.isEmpty()) {
            for (Map.Entry<String, String> e : idProps.entrySet()) {
                gTraversal = graphTraversal.has(e.getKey(), record.<String>get(e.getValue()));
            }
        }
        return gTraversal;
    }

    public static <K> GraphTraversal<Vertex, K> populateProperties(GraphTraversal<Vertex, K> graphItem, StructuredRecord record, Map<String, String> configProps) {
        if (configProps != null && !configProps.isEmpty()) {
            for (Map.Entry<String, String> schemaKey : configProps.entrySet()) {
                if (record.get(schemaKey.getValue()) != null)
                    graphItem.property(schemaKey.getKey(), record.get(schemaKey.getValue()));
            }
        }
        return graphItem;
    }
}
