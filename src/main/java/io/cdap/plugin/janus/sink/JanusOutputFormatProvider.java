package io.cdap.plugin.janus.sink;

import io.cdap.cdap.api.data.batch.OutputFormatProvider;
import io.cdap.plugin.common.batch.ConfigurationUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.OutputFormat;

import java.util.Map;

public class JanusOutputFormatProvider implements OutputFormatProvider {

    private final String outputFormatClassName;

    private final Map<String, String> configuration;


    public JanusOutputFormatProvider(Class<? extends OutputFormat> outputFormatClass, Configuration configuration) {
        this(outputFormatClass.getName(),configuration);
    }

    public JanusOutputFormatProvider(String outputFormatClassName, Configuration hConf) {
        this.outputFormatClassName = outputFormatClassName;
        this.configuration = ConfigurationUtils.getNonDefaultConfigurations(hConf);
    }

    @Override
    public String getOutputFormatClassName() {
        return outputFormatClassName;
    }

    @Override
    public Map<String, String> getOutputFormatConfiguration() {
        return configuration;
    }
}
