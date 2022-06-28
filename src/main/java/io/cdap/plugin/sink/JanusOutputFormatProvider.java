package io.cdap.plugin.sink;

import io.cdap.cdap.api.data.batch.OutputFormatProvider;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;

import java.util.HashMap;
import java.util.Map;

public class JanusOutputFormatProvider implements OutputFormatProvider {
    private Map<String, String> conf = new HashMap<>();

    @Override
    public String getOutputFormatClassName() {
        return NullOutputFormat.class.getName();
    }

    @Override
    public Map<String, String> getOutputFormatConfiguration() {
        return conf;
    }
}
