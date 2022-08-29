package io.cdap.plugin.janus.common;

import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.plugin.janus.dto.EdgeConfig;
import io.cdap.plugin.janus.dto.VertexConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import static io.cdap.plugin.janus.common.JanusConstants.TRAVERSAL_LABEL_CONCAT;

public class GremlinQueryUtil {

    public static <S, E> GraphTraversal<S, E> traverseByPrimaryKey(GraphTraversal<S, E> graphTraversal, String label,
                                                                   Map<String, String> idProps,
                                                                   StructuredRecord record) {

        GraphTraversal<S, E> gTraversal = graphTraversal.hasLabel(label);

        if (idProps != null && !idProps.isEmpty()) {
            for (Map.Entry<String, String> e : idProps.entrySet()) {
                gTraversal = graphTraversal.has(e.getKey(), record.<String>get(e.getValue()));
            }
        }
        return gTraversal;
    }

    public static <K> GraphTraversal<Vertex, K> populateProperties(GraphTraversal<Vertex, K> graphItem,
                                                                   StructuredRecord record,
                                                                   Map<String, String> configProps) {
        if (configProps != null && !configProps.isEmpty()) {
            for (Map.Entry<String, String> schemaKey : configProps.entrySet()) {
                if (record.get(schemaKey.getValue()) != null) {
                    graphItem.property(schemaKey.getKey(), record.get(schemaKey.getValue()));
                }
            }
        }
        return graphItem;
    }

    public static void populateVertexTraversal(TraversalStack traversalStack, VertexConfig vertexConfig,
                                               StructuredRecord structuredRecord,
                                               GraphTraversalSource graphTraversalSource) {

        Map<String, String> allProps = new HashMap<>();
        allProps.putAll(vertexConfig.getId());
        allProps.putAll(vertexConfig.getProperties());

        Optional<GraphTraversal<?, ?>> traversalOptional = traversalStack.getCurrentTraversal();

        if (traversalOptional.isPresent()) {
            final GraphTraversal<?, ?> vertexTraversal = traversalOptional.get()
                    .coalesce(populateProperties(
                                    traverseByPrimaryKey(__.V(), vertexConfig.getLabel(), vertexConfig.getId(),
                                            structuredRecord)
                                    , structuredRecord, allProps),
                            populateProperties(__.addV(vertexConfig.getLabel()), structuredRecord, allProps))
                    .as(vertexConfig.getLabel() + TRAVERSAL_LABEL_CONCAT);

            traversalStack.pushNewTraversal(vertexTraversal);


        } else {
            final GraphTraversal<?, ?> vertexTraversal = traverseByPrimaryKey(graphTraversalSource.V(),
                    vertexConfig.getLabel(), vertexConfig.getId(), structuredRecord)
                    .fold()
                    .coalesce(populateProperties(__.unfold(), structuredRecord, allProps),
                            populateProperties(__.addV(vertexConfig.getLabel()), structuredRecord, allProps))
                    .as(vertexConfig.getLabel() + TRAVERSAL_LABEL_CONCAT);

            traversalStack.pushNewTraversal(vertexTraversal);
        }

    }


    public static void populateEdgeTraversal(TraversalStack traversalStack, EdgeConfig edgeConfig,
                                             StructuredRecord structuredRecord) {

        traversalStack.getCurrentTraversal().ifPresent(traversal -> {

            traversal.select(edgeConfig.getToLabel() + TRAVERSAL_LABEL_CONCAT)
                    .coalesce(populateProperties(__.inE(edgeConfig.getLabel()).where(__.outV().as(
                                            edgeConfig.getFromLabel() + TRAVERSAL_LABEL_CONCAT)), structuredRecord,
                                    edgeConfig.getProperties()),
                            populateProperties(__.<Vertex>addE(edgeConfig.getLabel())
                                            .from(edgeConfig.getFromLabel() + TRAVERSAL_LABEL_CONCAT)
                                    , structuredRecord, edgeConfig.getProperties()));


            traversalStack.pushNewTraversal(traversal);

        });

    }
}
