package io.cdap.plugin.janus.sink;

import io.cdap.plugin.janus.dto.RecordToVertexMapper;
import io.cdap.plugin.janus.error.TransactionFailure;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

public interface GraphWritable {

    void write(GraphTraversalSource graphTraversalSource, RecordToVertexMapper recordToVertexMapper) throws TransactionFailure;
}
