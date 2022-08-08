package io.cdap.plugin.janus.sink;


import io.cdap.plugin.janus.common.JanusConnectionManager;
import io.cdap.plugin.janus.common.JanusConstants;
import io.cdap.plugin.janus.common.JanusCustomConfiguration;
import io.cdap.plugin.janus.common.JanusUtil;
import io.cdap.plugin.janus.dto.RecordToVertexMapper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Writable;

import org.apache.hadoop.mapreduce.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import static io.cdap.plugin.janus.common.JanusConstants.*;

public class JanusOutputFormat<K extends Writable & GraphWritable, V> extends OutputFormat<K, V> {

    private static Logger LOG = LoggerFactory.getLogger(JanusOutputFormat.class);

    @Override
    public RecordWriter<K, V> getRecordWriter(TaskAttemptContext context) throws IOException, InterruptedException {
        LOG.info("Get record writer");
        Configuration configuration = context.getConfiguration();
        RecordToVertexMapper recordToVertexMapper = JanusUtil.getVertexConfig(configuration.get(JanusConstants.RECORD_TO_VERTEX_MAPPER));

        final JanusCustomConfiguration janusCustomConfiguration = new JanusCustomConfiguration();

        janusCustomConfiguration.addPropertyConfigDirect("clusterConfiguration.hosts", Arrays.stream(configuration.get(HOSTS_NAME, "localhost").split(",")).collect(Collectors.toList()));
        janusCustomConfiguration.addPropertyConfigDirect("clusterConfiguration.port", configuration.get(PORT, "8182"));
        janusCustomConfiguration.addPropertyConfigDirect("clusterConfiguration.serializer.className", configuration.get(SERIALIZER_CLASS_NAME));
        janusCustomConfiguration.addPropertyConfigDirect("clusterConfiguration.serializer.config.ioRegistries", Arrays.stream(configuration.get(IO_REGISTRIES).split(",")).collect(Collectors.toList())); // (e.g. [ org.janusgraph.graphdb.tinkerpop.JanusGraphIoRegistry) ]
        janusCustomConfiguration.addPropertyConfigDirect("gremlin.remote.remoteConnectionClass", configuration.get(REMOTE_CONNECTION_CLASS));
        janusCustomConfiguration.addPropertyConfigDirect("gremlin.remote.driver.sourceName", configuration.get(GRAPH_SOURCE_NAME));
        return new JanusRecordWriter<K, V>(JanusConnectionManager.getGraphTraversalSource(janusCustomConfiguration), recordToVertexMapper, configuration.getInt(BATCH_SIZE_CONFIG, DEFAULT_BATCH_SIZE));
    }

    @Override
    public void checkOutputSpecs(JobContext context) throws IOException, InterruptedException {

    }

    @Override
    public OutputCommitter getOutputCommitter(TaskAttemptContext context) throws IOException, InterruptedException {
        return new JanusCommitter();
    }
}
