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

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Macro;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.plugin.PluginConfig;
import lombok.Getter;

import static io.cdap.plugin.janus.common.JanusConstants.*;

/**
 * Configuration for Mysql Connector
 */
@Getter
public class JanusConnectorConfig extends PluginConfig {

    @Name(HOSTS_NAME)
    @Description("This option is required for this transform.")
    @Macro // <- Macro means that the value will be substituted at runtime by the user.
    private final String hosts;

    @Name(PORT)
    @Description("And this option is not.")
    @Macro
    private final Integer port;

    @Name(REMOTE_CONNECTION_CLASS)
    @Description("This option is required for this transform.")
    @Macro // <- Macro means that the value will be substituted at runtime by the user.
    private final String remoteConnectionClass;


    @Name(SERIALIZER_CLASS_NAME)
    @Description("This option is required for this transform.")
    @Macro // <- Macro means that the value will be substituted at runtime by the user.
    private final String serializerClassName;

    @Name(IO_REGISTRIES)
    @Description("This option is required for this transform.")
    @Macro // <- Macro means that the value will be substituted at runtime by the user.
    private final String ioRegistries;

    @Name(GRAPH_SOURCE_NAME)
    @Description("This option is required for this transform.")
    @Macro // <- Macro means that the value will be substituted at runtime by the user.
    private final String graphSourceName;


    public JanusConnectorConfig(String hosts, Integer port, String remoteConnectionClass, String serializerClassName, String ioRegistries, String graphSourceName) {
        this.hosts = hosts;
        this.port = port;
        this.remoteConnectionClass = remoteConnectionClass;
        this.serializerClassName = serializerClassName;
        this.ioRegistries = ioRegistries;
        this.graphSourceName = graphSourceName;
    }
}
