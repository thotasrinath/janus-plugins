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

import io.cdap.cdap.api.annotation.*;
import io.cdap.cdap.api.data.batch.Output;
import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.dataset.lib.KeyValue;
import io.cdap.cdap.etl.api.Emitter;
import io.cdap.cdap.etl.api.FailureCollector;
import io.cdap.cdap.etl.api.PipelineConfigurer;
import io.cdap.cdap.etl.api.batch.BatchRuntimeContext;
import io.cdap.cdap.etl.api.batch.BatchSink;
import io.cdap.cdap.etl.api.batch.BatchSinkContext;
import io.cdap.cdap.etl.api.connector.Connector;
import io.cdap.plugin.common.JanusCustomConfiguration;
import io.cdap.plugin.common.JanusUtil;
import io.cdap.plugin.connector.JanusConnector;
import io.cdap.plugin.dto.EdgeConfig;
import io.cdap.plugin.dto.RecordToVertexConfig;
import io.cdap.plugin.dto.VertexConfig;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

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

    private RecordToVertexConfig recordToVertexConfig;
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
        this.recordToVertexConfig = JanusUtil.getVertexConfig(config.getRecordToVertexConfigurer());

        DefaultListDelimiterHandler COMMA_DELIMITER_HANDLER = new DefaultListDelimiterHandler(',');

        final JanusCustomConfiguration janusCustomConfiguration = new JanusCustomConfiguration();
        janusCustomConfiguration.setListDelimiterHandler(COMMA_DELIMITER_HANDLER);

        janusCustomConfiguration.addPropertyConfigDirect("clusterConfiguration.hosts", Arrays.asList(config.getHosts()));
        janusCustomConfiguration.addPropertyConfigDirect("clusterConfiguration.port",  String.valueOf(config.getPort()));
        janusCustomConfiguration.addPropertyConfigDirect("clusterConfiguration.serializer.className", config.getSerializerClassName());
        janusCustomConfiguration.addPropertyConfigDirect("clusterConfiguration.serializer.config.ioRegistries", Arrays.asList(config.getIoRegistries())); // (e.g. [ org.janusgraph.graphdb.tinkerpop.JanusGraphIoRegistry) ]
        janusCustomConfiguration.addPropertyConfigDirect("gremlin.remote.remoteConnectionClass", "org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection");
        janusCustomConfiguration.addPropertyConfigDirect("gremlin.remote.driver.sourceName", config.getGraphSourceName());
        this.graphTraversalSource = traversal().withRemote(janusCustomConfiguration);

        LOG.info("Graph connection established");
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
        if (CollectionUtils.isNotEmpty(input.getSchema().getFields())) {

            List<VertexConfig> vertexConfigs = recordToVertexConfig.getNodeList();
            List<EdgeConfig> edgeConfigs = recordToVertexConfig.getEdgeList();
            Map<String, Vertex> savedVertexMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(vertexConfigs)) {
                for (VertexConfig vertexConfig : vertexConfigs) {

                    final Vertex savedVertex = this.graphTraversalSource.V()
                            .has(vertexConfig.isHardCodedLabel() ? vertexConfig.getLabel() :
                                    Objects.requireNonNull(input.<String>get(vertexConfig.getLabel())), vertexConfig.getId(), Objects.requireNonNull(input.<String>get(vertexConfig.getId())))
                            .fold()
                            .coalesce(__.unfold(), getVertexVertexGraphTraversal(__.addV(vertexConfig.isHardCodedLabel() ? vertexConfig.getLabel() :
                                    Objects.requireNonNull(input.<String>get(vertexConfig.getLabel()))), input, vertexConfig))
                            .next();
                    savedVertexMap.put(savedVertex.label(), savedVertex);
                }
            }


            if (CollectionUtils.isNotEmpty(edgeConfigs)) {
                for (EdgeConfig edgeConfig : edgeConfigs) {

                    try {

                        getEdgeVertexTraversal(this.graphTraversalSource.V(savedVertexMap.get(edgeConfig.getFromLabel())).as("v")
                                .V(savedVertexMap.get(edgeConfig.getToLabel()))
                                .coalesce(__.inE(edgeConfig.isHardCodedLabel() ? edgeConfig.getLabel() :
                                                Objects.requireNonNull(input.<String>get(edgeConfig.getLabel()))).where(__.outV().as("v")),
                                        __.addE(edgeConfig.isHardCodedLabel() ? edgeConfig.getLabel() :
                                                        Objects.requireNonNull(input.<String>get(edgeConfig.getLabel())))
                                                .from("v")), input, savedVertexMap, edgeConfig)
                                .next();
                    } catch (Throwable e) {
                        LOG.error("Error while creating edge");
                    }
                }
            }
        }
    }

    private GraphTraversal<Vertex, Edge> getEdgeVertexTraversal(GraphTraversal<Vertex, Edge> edge, StructuredRecord input, Map<String, Vertex> vertexMap, EdgeConfig edgeConfig) {

        if (CollectionUtils.isNotEmpty(edgeConfig.getProperties())) {

            for (String schemaKey : edgeConfig.getProperties()) {
                edge.property(schemaKey, Objects.requireNonNull(input.<String>get(schemaKey)));
            }

        }
        return edge;
    }

    private GraphTraversal<Vertex, Vertex> getVertexVertexGraphTraversal(GraphTraversal<Vertex, Vertex> spawnedVertex, StructuredRecord input, VertexConfig vertexConfig) {
        final GraphTraversal<Vertex, Vertex> vertex = spawnedVertex.property(vertexConfig.getId(), Objects.requireNonNull(input.<String>get(vertexConfig.getId())));

        if (CollectionUtils.isNotEmpty(vertexConfig.getProperties())) {

            for (String schemaKey : vertexConfig.getProperties()) {
                vertex.property(schemaKey, Objects.requireNonNull(input.<String>get(schemaKey)));
            }

        }
        return vertex;
    }


}

