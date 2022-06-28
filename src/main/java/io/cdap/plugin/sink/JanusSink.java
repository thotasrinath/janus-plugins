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

package io.cdap.plugin.sink;

import io.cdap.plugin.connector.JanusConnector;
import com.google.common.base.Strings;
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
import io.cdap.plugin.common.KeyValueListParser;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.util.system.ConfigurationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import static org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource.traversal;

/**
 * Hydrator Transform Plugin Example - This provides a good starting point for building your own Transform Plugin
 * For full documentation, check out: https://docs.cask.co/cdap/current/en/developer-manual/pipelines/developing-plugins/index.html
 */
@Plugin(type = BatchSink.PLUGIN_TYPE)
@Name(JanusSink.NAME) // <- NOTE: The name of the plugin should match the name of the docs and widget json files.
@Description("JanusGraph Sink plugin.")
@Metadata(properties = {@MetadataProperty(key = Connector.PLUGIN_TYPE, value = JanusConnector.NAME)})
public class JanusSink extends BatchSink<StructuredRecord, Void, Void> {
    public static final String NAME = "Janus";
    // If you want to log things, you will need this line
    private static final Logger LOG = LoggerFactory.getLogger(JanusSink.class);

    // Usually, you will need a private variable to store the config that was passed to your class
    private final JanusSinkConfig config;


    protected GraphTraversalSource graphTraversalSource;

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
        // It's usually a good idea to validate the configuration at this point. It will stop the pipeline from being
        // published if this throws an error.
        Schema inputSchema = pipelineConfigurer.getStageConfigurer().getInputSchema();
    /*    try {
            pipelineConfigurer.getStageConfigurer().setOutputSchema(Schema.parseJson(config.schema));
        } catch (IOException e) {
            throw new IllegalArgumentException("Output schema cannot be parsed.", e);
        }*/
    }

    @Override
    public void prepareRun(BatchSinkContext batchSinkContext) throws Exception {
        FailureCollector collector = batchSinkContext.getFailureCollector();
        collector.getOrThrowException();

        batchSinkContext.addOutput(Output.of(config.referenceName, new JanusOutputFormatProvider()));

    }

    @Override
    public void initialize(BatchRuntimeContext context) throws Exception {
        super.initialize(context);
        this.graphTraversalSource = openGraph();

    }

    /**
     * This function will be called at the end of the pipeline. You can use it to clean up any variables or connections.
     */
    @Override
    public void destroy() {
        // No Op
        try {
            this.graphTraversalSource.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void transform(StructuredRecord input, Emitter<KeyValue<Void, Void>> emitter) throws Exception {

        Iterator<Schema.Field> fieldIter = Objects.requireNonNull(input.getSchema().getFields()).iterator();
        if (!fieldIter.hasNext()) {
            // shouldn't happen
            return;
        }

        final GraphTraversal<Vertex, Vertex> vertex = this.graphTraversalSource.addV(Objects.requireNonNull(input.<String>get(config.rowField)));
        while (fieldIter.hasNext()) {
            String fieldName = fieldIter.next().getName();
            vertex.property(fieldName, Objects.requireNonNull(input.<String>get(fieldName)));
        }
        vertex.next();
        LOG.info("Transform Called");
    }

    public GraphTraversalSource openGraph() throws ConfigurationException, IOException {

        Map<String, Object> mapConfig = new HashMap<String, Object>();
        mapConfig.put("gremlin.remote.remoteConnectionClass", "org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection");
        mapConfig.put("gremlin.remote.driver.sourceName", "g");


        Configuration janusConf = ConfigurationUtil.loadMapConfiguration(mapConfig);

        Configuration configuration = ConfigurationUtil.createBaseConfiguration();
        configuration.addProperty("hosts", config.hosts);
        configuration.addProperty("port", String.valueOf(config.port));
        configuration.addProperty("serializer.className", config.serializerClassName);

        KeyValueListParser kvParser = new KeyValueListParser("\\s*,\\s*", ":");
        if (!Strings.isNullOrEmpty(config.additionalConnectionProperties)) {
            for (KeyValue<String, String> keyVal : kvParser.parse(config.additionalConnectionProperties)) {
                // add prefix to each property
                String key = keyVal.getKey();
                String val = keyVal.getValue();
                configuration.addProperty(key, val);
            }
        }

        Map<String, Object> serializerConfig = new HashMap<String, Object>();
        mapConfig.put("ioRegistries", config.ioRegistries);

        if (!Strings.isNullOrEmpty(config.additionalSerializerConfig)) {
            for (KeyValue<String, String> keyVal : kvParser.parse(config.additionalSerializerConfig)) {
                // add prefix to each property
                String key = keyVal.getKey();
                String val = keyVal.getValue();
                configuration.addProperty(key, val);
            }
        }


        configuration.addProperty("serializer.config", serializerConfig);

        // using the remote driver for schema
        try {
            Cluster cluster = Cluster.open(configuration);
            Client client = cluster.connect();
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
        LOG.info("OpenGraph Called");

        return traversal().withRemote(janusConf);

    }

}

