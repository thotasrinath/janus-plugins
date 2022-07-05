package io.cdap.plugin.dto;

import lombok.Data;

import java.util.List;

@Data
public class EdgeConfig extends Config {
    private String fromLabel;
    private String toLabel;
}
