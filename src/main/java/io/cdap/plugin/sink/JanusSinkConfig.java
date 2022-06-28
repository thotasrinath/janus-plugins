package io.cdap.plugin.sink;

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Macro;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.plugin.common.JanusConstants;
import io.cdap.plugin.common.ReferencePluginConfig;

import javax.annotation.Nullable;

public class JanusSinkConfig extends ReferencePluginConfig {

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

    @Macro
    @Name(JanusConstants.SCHEMA_ROW_FIELD)
    @Description("The name of the record field that should be used as the row key when writing to the table.")
    public final String rowField;


    public JanusSinkConfig(String referenceName, String hosts, Integer port, String serializerClassName, String ioRegistries, String additionalConnectionProperties, String additionalSerializerConfig, String rowField) {
        super(referenceName);
        this.hosts = hosts;
        this.port = port;
        this.serializerClassName = serializerClassName;
        this.ioRegistries = ioRegistries;
        this.additionalConnectionProperties = additionalConnectionProperties;
        this.additionalSerializerConfig = additionalSerializerConfig;
        this.rowField = rowField;
    }
}
