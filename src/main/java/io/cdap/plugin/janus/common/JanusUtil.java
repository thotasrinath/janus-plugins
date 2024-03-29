package io.cdap.plugin.janus.common;

import io.cdap.plugin.janus.dto.RecordToVertexMapper;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JanusUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger LOG = LoggerFactory.getLogger(JanusUtil.class);

    public static RecordToVertexMapper getVertexConfig(String mappingJson) {
        try {
            if (StringUtils.isNotBlank(mappingJson)) {
                return objectMapper.readValue(mappingJson, RecordToVertexMapper.class);
            }
        } catch (IOException e) {
            LOG.error("Error while convering JSON to RecordToVertexConfig", e);
        }

        return new RecordToVertexMapper();
    }

}
