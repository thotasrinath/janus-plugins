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

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Macro;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.plugin.PluginConfig;
import io.cdap.plugin.common.JanusConstants;

import javax.annotation.Nullable;

/**
 * Configuration for Mysql Connector
 */
public class JanusConnectorConfig extends PluginConfig {

    @Name(JanusConstants.HOSTS_NAME)
    @Description("This option is required for this transform.")
    @Macro // <- Macro means that the value will be substituted at runtime by the user.
    public final String hosts;

    @Name(JanusConstants.PORT)
    @Description("And this option is not.")
    @Macro
    public final Integer port;

    @Name(JanusConstants.SERIALIZER_CLASS_NAME)
    @Description("This option is required for this transform.")
    @Macro // <- Macro means that the value will be substituted at runtime by the user.
    public final String serializerClassName;

    @Name(JanusConstants.IO_REGISTRIES)
    @Description("This option is required for this transform.")
    @Macro // <- Macro means that the value will be substituted at runtime by the user.
    public final String ioRegistries;

    @Name(JanusConstants.ADD_CONNECTION_PROPERTIES)
    @Description("This option is required for this transform.")
    @Macro // <- Macro means that the value will be substituted at runtime by the user.
    @Nullable
    public final String additionalConnectionProperties;

    @Name(JanusConstants.ADD_SERIALIZATION_CONFIG)
    @Description("This option is required for this transform.")
    @Macro // <- Macro means that the value will be substituted at runtime by the user.
    @Nullable
    public final String additionalSerializerConfig;


    public JanusConnectorConfig(String hosts, Integer port, String serializerClassName, String ioRegistries, String additionalConnectionProperties, String additionalSerializerConfig) {
        this.hosts = hosts;
        this.port = port;
        this.serializerClassName = serializerClassName;
        this.ioRegistries = ioRegistries;
        this.additionalConnectionProperties = additionalConnectionProperties;
        this.additionalSerializerConfig = additionalSerializerConfig;
    }
}
