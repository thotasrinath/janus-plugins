package io.cdap.plugin.dto;

import lombok.Data;

@Data
public class EdgeConfig extends Config {
    private String fromLabel;
    private String toLabel;
}
