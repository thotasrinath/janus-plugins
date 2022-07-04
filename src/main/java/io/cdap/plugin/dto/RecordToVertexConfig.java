package io.cdap.plugin.dto;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

@Data
public class RecordToVertexConfig {
    @JsonProperty("Node_List")
    private List<VertexConfig> nodeList;
    @JsonProperty("Edge_List")
    private List<EdgeConfig> edgeList;
}
