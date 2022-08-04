package io.cdap.plugin.janus.dto;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

@Data
public class RecordToVertexMapper {
    @JsonProperty("NODE_LIST")
    private List<VertexConfig> nodeList;
    @JsonProperty("EDGE_LIST")
    private List<EdgeConfig> edgeList;
}
