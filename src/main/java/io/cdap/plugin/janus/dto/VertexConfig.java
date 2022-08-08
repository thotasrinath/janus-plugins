package io.cdap.plugin.janus.dto;

import java.util.Map;
import lombok.Data;

@Data
public class VertexConfig extends Config {
    private Map<String, String> id;
}
