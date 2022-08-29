/*
 * Copyright © 2021 Cask Data, Inc.
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

package io.cdap.plugin.janus.connector;

import static io.cdap.plugin.janus.common.JanusConstants.GRAPH_SOURCE_NAME;
import static io.cdap.plugin.janus.common.JanusConstants.HOSTS_NAME;
import static io.cdap.plugin.janus.common.JanusConstants.IO_REGISTRIES;
import static io.cdap.plugin.janus.common.JanusConstants.PORT;
import static io.cdap.plugin.janus.common.JanusConstants.REMOTE_CONNECTION_CLASS;
import static io.cdap.plugin.janus.common.JanusConstants.SERIALIZER_CLASS_NAME;
import static io.cdap.plugin.janus.common.JanusConstants.SUPPORTS_TRANSACTION;

import io.cdap.cdap.api.annotation.Category;
import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.cdap.etl.api.batch.BatchSink;
import io.cdap.cdap.etl.api.connector.BrowseDetail;
import io.cdap.cdap.etl.api.connector.BrowseEntity;
import io.cdap.cdap.etl.api.connector.BrowseRequest;
import io.cdap.cdap.etl.api.connector.Connector;
import io.cdap.cdap.etl.api.connector.ConnectorContext;
import io.cdap.cdap.etl.api.connector.ConnectorSpec;
import io.cdap.cdap.etl.api.connector.ConnectorSpecRequest;
import io.cdap.cdap.etl.api.connector.PluginSpec;
import io.cdap.cdap.etl.api.validation.ValidationException;
import io.cdap.cdap.etl.api.validation.ValidationFailure;
import io.cdap.plugin.janus.common.JanusConnectionManager;
import io.cdap.plugin.janus.common.JanusCustomConfiguration;
import io.cdap.plugin.janus.error.ConnectionFailure;
import io.cdap.plugin.janus.sink.JanusSink;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Plugin(type = Connector.PLUGIN_TYPE)
@Name(JanusConnector.NAME)
@Description("Connection to access data in JanusGraph.")
@Category("Database")
public class JanusConnector implements Connector {

    private static final Logger LOG = LoggerFactory.getLogger(JanusConnector.class);

    public static final String NAME = "Janus";

    private final JanusConnectorConfig config;

    public JanusConnector(JanusConnectorConfig config) {
        this.config = config;
    }

    @Override
    public void test(ConnectorContext context) throws ValidationException {
        try {
            final JanusCustomConfiguration janusCustomConfiguration = new JanusCustomConfiguration();

            janusCustomConfiguration.addPropertyConfigDirect("clusterConfiguration.hosts",
                    Arrays.stream(config.getHosts().split(",")).collect(Collectors.toList()));
            janusCustomConfiguration.addPropertyConfigDirect("clusterConfiguration.port", config.getPort());
            janusCustomConfiguration.addPropertyConfigDirect("clusterConfiguration.serializer.className",
                    config.getSerializerClassName());
            janusCustomConfiguration.addPropertyConfigDirect("clusterConfiguration.serializer.config.ioRegistries",
                    Arrays.stream(config.getIoRegistries().split(",")).collect(
                            Collectors.toList())); // (e.g. [ org.janusgraph.graphdb.tinkerpop.JanusGraphIoRegistry) ]
            janusCustomConfiguration.addPropertyConfigDirect("gremlin.remote.remoteConnectionClass",
                    config.getRemoteConnectionClass());
            janusCustomConfiguration.addPropertyConfigDirect("gremlin.remote.driver.sourceName",
                    config.getGraphSourceName());

            JanusConnectionManager.validateConnection(janusCustomConfiguration);
        } catch (ConnectionFailure e) {
            LOG.error("Failed to connect JanusGraph", e);
            throw new ValidationException(Collections.singletonList(new ValidationFailure(e.getMessage())));
        }
    }

    @Override
    public BrowseDetail browse(ConnectorContext context, BrowseRequest request) throws IOException {
        BrowseDetail.Builder builder = BrowseDetail.builder();
        builder.addEntity(BrowseEntity.builder(config.getHosts(), "fake_path", "JanusConnection")
                .canSample(true).build());
        return builder.build();
    }

    @Override
    public ConnectorSpec generateSpec(ConnectorContext context, ConnectorSpecRequest path) throws IOException {

        Map<String, String> properties = new HashMap<>();
        properties.put(HOSTS_NAME, config.getHosts());
        properties.put(PORT, String.valueOf(config.getPort()));
        properties.put(SERIALIZER_CLASS_NAME, config.getSerializerClassName());
        properties.put(IO_REGISTRIES, config.getIoRegistries());
        properties.put(GRAPH_SOURCE_NAME, config.getGraphSourceName());
        properties.put(REMOTE_CONNECTION_CLASS, config.getRemoteConnectionClass());
        properties.put(SUPPORTS_TRANSACTION, config.getSupportsTransaction());


        return ConnectorSpec.builder()
                .addRelatedPlugin(new PluginSpec(JanusSink.NAME, BatchSink.PLUGIN_TYPE, properties))
                .build();

    }
}
