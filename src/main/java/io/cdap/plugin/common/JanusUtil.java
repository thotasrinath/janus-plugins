package io.cdap.plugin.common;

import io.cdap.plugin.dto.RecordToVertexConfig;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JanusUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger LOG = LoggerFactory.getLogger(JanusUtil.class);

    public static RecordToVertexConfig getVertexConfig(String mappingJson) {
        try {
            if (StringUtils.isNotBlank(mappingJson))
                return objectMapper.readValue(mappingJson, RecordToVertexConfig.class);
        } catch (IOException e) {
            LOG.error("Error while convering JSON to RecordToVertexConfig", e);
        }

        return new RecordToVertexConfig();
    }

}
