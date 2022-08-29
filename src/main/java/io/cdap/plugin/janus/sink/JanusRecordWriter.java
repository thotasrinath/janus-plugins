package io.cdap.plugin.janus.sink;

import io.cdap.plugin.janus.dto.RecordToVertexMapper;
import io.cdap.plugin.janus.error.TransactionFailure;
import java.io.IOException;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JanusRecordWriter<K extends GraphWritable, V> extends RecordWriter<K, V> {

    private static final Logger LOG = LoggerFactory.getLogger(JanusRecordWriter.class);

    private final GraphTraversalSource graphTraversalSource;

    private final RecordToVertexMapper recordToVertexMapper;

    private final int batchSize;

    private boolean emptyData = true;

    private final boolean supportsTransaction;

    private long numWrittenRecords = 0;

    public JanusRecordWriter(GraphTraversalSource graphTraversalSource, RecordToVertexMapper recordToVertexMapper,
                             int batchSize, boolean supportsTransaction) {
        this.graphTraversalSource = graphTraversalSource;
        this.recordToVertexMapper = recordToVertexMapper;
        this.batchSize = batchSize;
        this.supportsTransaction = supportsTransaction;
    }

    @Override
    public void write(K key, V value) throws IOException, InterruptedException {

        emptyData = false;
        try {
            key.write(graphTraversalSource, recordToVertexMapper);
            numWrittenRecords++;

            if (batchSize > 0 && numWrittenRecords % batchSize == 0 && supportsTransaction) {
                LOG.info("Commiting Batch Transaction");
                graphTraversalSource.tx().commit();
            }
        } catch (TransactionFailure e) {
            if (supportsTransaction) {
                LOG.error("Rolling back Batch Transaction", e);
                graphTraversalSource.tx().rollback();
            }
        }

    }

    @Override
    public void close(TaskAttemptContext context) throws IOException, InterruptedException {
        try {
            if (!emptyData && supportsTransaction) {
                LOG.info("Commiting Transaction with left data");
                this.graphTraversalSource.tx().commit();
            }
        } catch (Exception e) {
            if (supportsTransaction) {
                LOG.error("Error while committing the transaction", e);
                graphTraversalSource.tx().rollback();
            }

        }
    }
}
