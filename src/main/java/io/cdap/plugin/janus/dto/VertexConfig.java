package io.cdap.plugin.janus.dto;

import lombok.Data;

import java.util.Map;

@Data
public class VertexConfig extends Config {
    private Map<String,String> id;
}
