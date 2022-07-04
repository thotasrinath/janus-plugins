package io.cdap.plugin.dto;

import lombok.Data;

import java.util.List;

@Data
public class VertexConfig {

    private String label;
    private String id;
    private List<String> properties;

}
