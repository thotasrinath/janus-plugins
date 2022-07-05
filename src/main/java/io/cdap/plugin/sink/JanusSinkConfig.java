package io.cdap.plugin.sink;

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Macro;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.plugin.common.JanusConstants;
import io.cdap.plugin.common.ReferencePluginConfig;
import lombok.Getter;

import javax.annotation.Nullable;

@Getter
public class JanusSinkConfig extends ReferencePluginConfig {

    @Name(JanusConstants.HOSTS_NAME)
    @Description("This option is required for this transform.")
    @Macro // <- Macro means that the value will be substituted at runtime by the user.
    private final String hosts;

    @Name(JanusConstants.PORT)
    @Description("And this option is not.")
    @Macro
    private final Integer port;

    @Name(JanusConstants.SERIALIZER_CLASS_NAME)
    @Description("This option is required for this transform.")
    @Macro // <- Macro means that the value will be substituted at runtime by the user.
    private final String serializerClassName;

    @Name(JanusConstants.IO_REGISTRIES)
    @Description("This option is required for this transform.")
    @Macro // <- Macro means that the value will be substituted at runtime by the user.
    private final String ioRegistries;

    @Name(JanusConstants.GRAPH_SOURCE_NAME)
    @Description("This option is required for this transform.")
    @Macro // <- Macro means that the value will be substituted at runtime by the user.
    private final String graphSourceName;

    @Name(JanusConstants.ADD_CONNECTION_PROPERTIES)
    @Description("This option is required for this transform.")
    @Macro // <- Macro means that the value will be substituted at runtime by the user.
    @Nullable
    private final String additionalConnectionProperties;

    @Name(JanusConstants.ADD_SERIALIZATION_CONFIG)
    @Description("This option is required for this transform.")
    @Macro // <- Macro means that the value will be substituted at runtime by the user.
    @Nullable
    private final String additionalSerializerConfig;

    @Macro
    @Name("propertyJsonEditor")
    @Description("Protery Json Editor")
    private final String propertyJsonEditor;

    public JanusSinkConfig(String referenceName, String hosts, Integer port, String serializerClassName, String ioRegistries, String graphSourceName, String additionalConnectionProperties, String additionalSerializerConfig, String propertyJsonEditor) {
        super(referenceName);
        this.hosts = hosts;
        this.port = port;
        this.serializerClassName = serializerClassName;
        this.ioRegistries = ioRegistries;
        this.graphSourceName = graphSourceName;
        this.additionalConnectionProperties = additionalConnectionProperties;
        this.additionalSerializerConfig = additionalSerializerConfig;
        this.propertyJsonEditor = propertyJsonEditor;
    }
}
