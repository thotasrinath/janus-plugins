/*
 * Copyright © 2017 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.plugin.janus.sink;

import io.cdap.cdap.api.annotation.*;
import io.cdap.cdap.api.data.batch.Output;
import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.cdap.api.dataset.lib.KeyValue;
import io.cdap.cdap.etl.api.Emitter;
import io.cdap.cdap.etl.api.FailureCollector;
import io.cdap.cdap.etl.api.PipelineConfigurer;
import io.cdap.cdap.etl.api.batch.BatchRuntimeContext;
import io.cdap.cdap.etl.api.batch.BatchSink;
import io.cdap.cdap.etl.api.batch.BatchSinkContext;
import io.cdap.cdap.etl.api.connector.Connector;
import io.cdap.plugin.common.LineageRecorder;
import io.cdap.plugin.janus.connector.JanusConnector;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static io.cdap.plugin.janus.common.JanusConstants.*;

/**
 * Hydrator Transform Plugin Example - This provides a good starting point for building your own Transform Plugin
 * For full documentation, check out: https://docs.cask.co/cdap/current/en/developer-manual/pipelines/developing-plugins/index.html
 */
@Plugin(type = BatchSink.PLUGIN_TYPE)
@Name(JanusSink.NAME) // <- NOTE: The name of the plugin should match the name of the docs and widget json files.
@Description("JanusGraph Sink plugin.")
@Metadata(properties = {@MetadataProperty(key = Connector.PLUGIN_TYPE, value = JanusConnector.NAME)})
public class JanusSink extends BatchSink<StructuredRecord, JanusRecord, NullWritable> {
    public static final String NAME = "Janus";
    // If you want to log things, you will need this line
    private static final Logger LOG = LoggerFactory.getLogger(JanusSink.class);

    // Usually, you will need a private variable to store the config that was passed to your class
    private final JanusSinkConfig config;


    public JanusSink(JanusSinkConfig config) {
        this.config = config;
    }

    /**
     * This function is called when the pipeline is published. You should use this for validating the config and setting
     * additional parameters in pipelineConfigurer.getStageConfigurer(). Those parameters will be stored and will be made
     * available to your plugin during runtime via the TransformContext. Any errors thrown here will stop the pipeline
     * from being published.
     *
     * @param pipelineConfigurer Configures an ETL Pipeline. Allows adding datasets and streams and storing parameters
     * @throws IllegalArgumentException If the config is invalid.
     */
    @Override
    public void configurePipeline(PipelineConfigurer pipelineConfigurer) throws IllegalArgumentException {
        super.configurePipeline(pipelineConfigurer);


    }

    @Override
    public void prepareRun(BatchSinkContext batchSinkContext) throws Exception {
        FailureCollector collector = batchSinkContext.getFailureCollector();
        collector.getOrThrowException();

        if (batchSinkContext.getInputSchema() != null && CollectionUtils.isNotEmpty(batchSinkContext.getInputSchema().getFields()))
            emitLineage(batchSinkContext, batchSinkContext.getInputSchema().getFields());

        Configuration configuration = new Configuration();
        configuration.set(HOSTS_NAME, config.getHosts());
        configuration.set(PORT, String.valueOf(config.getPort()));
        configuration.set(SERIALIZER_CLASS_NAME, config.getSerializerClassName());
        configuration.set(IO_REGISTRIES, config.getIoRegistries());
        configuration.set(REMOTE_CONNECTION_CLASS, config.getRemoteConnectionClass());
        configuration.set(GRAPH_SOURCE_NAME, config.getGraphSourceName());
        configuration.set(RECORD_TO_VERTEX_MAPPER, config.getRecordToVertexMapper());

        if (StringUtils.isEmpty(config.getBatchSizeConfig()))
            configuration.setInt(BATCH_SIZE_CONFIG, DEFAULT_BATCH_SIZE);
        else
            configuration.setInt(BATCH_SIZE_CONFIG, Integer.parseInt(config.getBatchSizeConfig()));

        LOG.info("Graph connection properties populated");

        batchSinkContext.addOutput(Output.of(config.referenceName, new JanusOutputFormatProvider(JanusOutputFormat.class, configuration)));

    }

    private void emitLineage(BatchSinkContext batchSinkContext, List<Schema.Field> fields) {
        LineageRecorder lineageRecorder = new LineageRecorder(batchSinkContext, config.getReferenceName());

        if (!fields.isEmpty())
            lineageRecorder.recordWrite("Write", "Wrote to Graph DB..",
                    fields.stream().map(Schema.Field::getName).collect(Collectors.toList()));
    }

    @Override
    public void initialize(BatchRuntimeContext context) throws Exception {
        super.initialize(context);

    }

    @Override
    public void onRunFinish(boolean succeeded, BatchSinkContext context) {
        super.onRunFinish(succeeded, context);
        JanusConnectionManager.closeGraphTraversalSource();
    }

    /**
     * This function will be called at the end of the pipeline. You can use it to clean up any variables or connections.
     */
    @Override
    public void destroy() {
        // No Op

    }

    @Override
    public void transform(StructuredRecord input, Emitter<KeyValue<JanusRecord, NullWritable>> emitter) throws Exception {
        emitter.emit(new KeyValue<>(new JanusRecord(input), null));
    }
}

