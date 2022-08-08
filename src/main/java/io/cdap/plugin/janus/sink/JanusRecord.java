package io.cdap.plugin.janus.sink;

import static io.cdap.plugin.janus.common.GremlinQueryUtil.populateProperties;
import static io.cdap.plugin.janus.common.GremlinQueryUtil.traverseByPrimaryKey;

import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.plugin.janus.dto.EdgeConfig;
import io.cdap.plugin.janus.dto.RecordToVertexMapper;
import io.cdap.plugin.janus.dto.VertexConfig;
import io.cdap.plugin.janus.error.TransactionFailure;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Writable;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JanusRecord implements Writable, GraphWritable, Configurable {

    private static final Logger LOG = LoggerFactory.getLogger(JanusRecord.class);

    protected StructuredRecord record;
    protected Configuration configuration;

    public JanusRecord(StructuredRecord record) {
        this.record = record;
    }

    @Override
    public void write(GraphTraversalSource graphTraversalSource, RecordToVertexMapper recordToVertexMapper)
            throws TransactionFailure {

        if (CollectionUtils.isNotEmpty(record.getSchema().getFields())) {
            try {

                List<VertexConfig> vertexConfigs = recordToVertexMapper.getNodeList();
                List<EdgeConfig> edgeConfigs = recordToVertexMapper.getEdgeList();
                Map<String, Vertex> savedVertexMap = new HashMap<>();
                if (CollectionUtils.isNotEmpty(vertexConfigs)) {
                    for (VertexConfig vertexConfig : vertexConfigs) {
                        String vertexLabel = vertexConfig.getLabel();

                        if (vertexConfig.getId() != null && !vertexConfig.getId().isEmpty()) {
                            Map<String, String> allProps = new HashMap<>();
                            allProps.putAll(vertexConfig.getId());
                            allProps.putAll(vertexConfig.getProperties());

                            final Vertex savedVertex =
                                    traverseByPrimaryKey(graphTraversalSource.V(), vertexLabel, vertexConfig.getId(),
                                            record)
                                            .fold()
                                            .coalesce(__.unfold(),
                                                    populateProperties(__.addV(vertexLabel), record, allProps))
                                            .next();
                            savedVertexMap.put(savedVertex.label(), savedVertex);

                        }
                    }
                }

                if (CollectionUtils.isNotEmpty(edgeConfigs)) {
                    for (EdgeConfig edgeConfig : edgeConfigs) {
                        String edgeLabel = edgeConfig.getLabel();

                        Vertex fromVertex = savedVertexMap.get(edgeConfig.getFromLabel());
                        Vertex toVertex = savedVertexMap.get(edgeConfig.getToLabel());

                        if (fromVertex != null && toVertex != null) {
                            Edge edge = graphTraversalSource.V(fromVertex).as("v")
                                    .V(toVertex)
                                    .coalesce(__.inE(edgeLabel).where(__.outV().as("v")),
                                            populateProperties(__.<Vertex>addE(edgeLabel).from("v"), record,
                                                    edgeConfig.getProperties()))
                                    .next();
                        }
                    }
                }

            } catch (Exception e) {
                LOG.error("Error while creating edge or vertex : {}", e.getMessage(), e);
                throw new TransactionFailure("Transaction failed due to : " + e.getMessage());
            }
        }

    }

    @Override
    public void setConf(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration getConf() {
        return this.configuration;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {

    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {

    }
}
