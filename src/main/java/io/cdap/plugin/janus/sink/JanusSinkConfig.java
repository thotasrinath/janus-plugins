package io.cdap.plugin.janus.sink;

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Macro;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.plugin.common.ReferencePluginConfig;
import lombok.Getter;

import static io.cdap.plugin.janus.common.JanusConstants.*;

@Getter
public class JanusSinkConfig extends ReferencePluginConfig {

    @Name(HOSTS_NAME)

    @Description("This option is required for this transform.")
    @Macro // <- Macro means that the value will be substituted at runtime by the user.
    private final String hosts;

    @Name(PORT)
    @Description("And this option is not.")
    @Macro
    private final Integer port;

    @Name(SERIALIZER_CLASS_NAME)
    @Description("This option is required for this transform.")
    @Macro // <- Macro means that the value will be substituted at runtime by the user.
    private final String serializerClassName;

    @Name(REMOTE_CONNECTION_CLASS)
    @Description("This option is required for this transform.")
    @Macro // <- Macro means that the value will be substituted at runtime by the user.
    private final String remoteConnectionClass;

    @Name(IO_REGISTRIES)
    @Description("This option is required for this transform.")
    @Macro // <- Macro means that the value will be substituted at runtime by the user.
    private final String ioRegistries;

    @Name(GRAPH_SOURCE_NAME)
    @Description("This option is required for this transform.")
    @Macro // <- Macro means that the value will be substituted at runtime by the user.
    private final String graphSourceName;

    @Macro
    @Name(RECORD_TO_VERTEX_MAPPER)
    @Description("Structured record to vertex configuration")
    private final String recordToVertexMapper;

    @Macro
    @Name(BATCH_SIZE_CONFIG)
    @Description("Batch-size to save in Janus")
    private final String batchSizeConfig;

    public JanusSinkConfig(String referenceName, String hosts, Integer port, String serializerClassName, String remoteConnectionClass, String ioRegistries, String graphSourceName, String recordToVertexMapper, String batchSizeConfig) {
        super(referenceName);
        this.hosts = hosts;
        this.port = port;
        this.serializerClassName = serializerClassName;
        this.remoteConnectionClass = remoteConnectionClass;
        this.ioRegistries = ioRegistries;
        this.graphSourceName = graphSourceName;
        this.recordToVertexMapper = recordToVertexMapper;
        this.batchSizeConfig = batchSizeConfig;
    }

    public String getReferenceName() {
        return referenceName;
    }
}
