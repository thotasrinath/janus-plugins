package io.cdap.plugin.janus.dto;

import lombok.Data;

@Data
public class EdgeConfig extends Config {
    private String fromLabel;
    private String toLabel;
}
