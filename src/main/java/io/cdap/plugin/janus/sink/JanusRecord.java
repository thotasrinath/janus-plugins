package io.cdap.plugin.janus.sink;

import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.plugin.janus.common.GremlinQueryUtil;
import io.cdap.plugin.janus.common.TraversalStack;
import io.cdap.plugin.janus.dto.EdgeConfig;
import io.cdap.plugin.janus.dto.RecordToVertexMapper;
import io.cdap.plugin.janus.dto.VertexConfig;
import io.cdap.plugin.janus.error.TransactionFailure;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Writable;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JanusRecord implements Writable, GraphWritable, Configurable {

    private static final Logger LOG = LoggerFactory.getLogger(JanusRecord.class);

    protected StructuredRecord record;
    protected Configuration configuration;

    private final TraversalStack traversalStack = new TraversalStack();

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
                if (CollectionUtils.isNotEmpty(vertexConfigs)) {
                    for (VertexConfig vertexConfig : vertexConfigs) {

                        if (vertexConfig.getId() != null && !vertexConfig.getId().isEmpty()) {
                            GremlinQueryUtil.populateVertexTraversal(traversalStack, vertexConfig, record,
                                    graphTraversalSource);

                        }
                    }
                }

                if (CollectionUtils.isNotEmpty(edgeConfigs)) {
                    for (EdgeConfig edgeConfig : edgeConfigs) {


                        if (StringUtils.isNotBlank(edgeConfig.getFromLabel()) &&
                                StringUtils.isNotBlank(edgeConfig.getToLabel())) {
                            GremlinQueryUtil.populateEdgeTraversal(traversalStack, edgeConfig, record);
                        }
                    }
                }

                traversalStack.getCurrentTraversal().ifPresent(Iterator::next);

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
