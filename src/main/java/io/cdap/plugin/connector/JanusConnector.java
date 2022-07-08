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

package io.cdap.plugin.connector;

import io.cdap.cdap.api.annotation.Category;
import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.cdap.etl.api.batch.BatchSink;
import io.cdap.cdap.etl.api.batch.BatchSource;
import io.cdap.cdap.etl.api.connector.*;
import io.cdap.cdap.etl.api.validation.ValidationException;
import io.cdap.plugin.common.Constants;
import io.cdap.plugin.common.JanusConstants;
import io.cdap.plugin.sink.JanusSink;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Plugin(type = Connector.PLUGIN_TYPE)
@Name(JanusConnector.NAME)
@Description("Connection to access data in JanusGraph.")
@Category("Database")
public class JanusConnector implements Connector {
    public static final String NAME = "Janus";
    private static final String MESSAGE_FIELD = "message";
    private static final Schema DEFAULT_SCHEMA =
            Schema.recordOf("janus", Schema.Field.of(MESSAGE_FIELD, Schema.of(Schema.Type.STRING)));

    private final JanusConnectorConfig config;

    public JanusConnector(JanusConnectorConfig config) {
        this.config = config;
    }

    @Override
    public void test(ConnectorContext context) throws ValidationException {

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
        properties.put(JanusConstants.HOSTS_NAME, config.getHosts());
        properties.put(JanusConstants.PORT, String.valueOf(config.getPort()));
        properties.put(JanusConstants.SERIALIZER_CLASS_NAME, config.getSerializerClassName());
        properties.put(JanusConstants.IO_REGISTRIES, config.getIoRegistries());
        properties.put(JanusConstants.GRAPH_SOURCE_NAME, config.getGraphSourceName());
        properties.put(JanusConstants.REMOTE_CONNECTION_CLASS, config.getRemoteConnectionClass());


        return ConnectorSpec.builder().setSchema(DEFAULT_SCHEMA)
                .addRelatedPlugin(new PluginSpec(JanusSink.NAME, BatchSink.PLUGIN_TYPE, properties))
                .build();

    }
}
