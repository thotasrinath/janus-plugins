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

    @Name(JanusConstants.REMOTE_CONNECTION_CLASS)
    @Description("This option is required for this transform.")
    @Macro // <- Macro means that the value will be substituted at runtime by the user.
    private final String remoteConnectionClass;

    @Name(JanusConstants.IO_REGISTRIES)
    @Description("This option is required for this transform.")
    @Macro // <- Macro means that the value will be substituted at runtime by the user.
    private final String ioRegistries;

    @Name(JanusConstants.GRAPH_SOURCE_NAME)
    @Description("This option is required for this transform.")
    @Macro // <- Macro means that the value will be substituted at runtime by the user.
    private final String graphSourceName;

    @Macro
    @Name("recordToVertexConfigurer")
    @Description("Structured record to vertex configuration")
    private final String recordToVertexConfigurer;

    public JanusSinkConfig(String referenceName, String hosts, Integer port, String serializerClassName, String remoteConnectionClass, String ioRegistries, String graphSourceName, String recordToVertexConfigurer) {
        super(referenceName);
        this.hosts = hosts;
        this.port = port;
        this.serializerClassName = serializerClassName;
        this.remoteConnectionClass = remoteConnectionClass;
        this.ioRegistries = ioRegistries;
        this.graphSourceName = graphSourceName;
        this.recordToVertexConfigurer = recordToVertexConfigurer;
    }
}
