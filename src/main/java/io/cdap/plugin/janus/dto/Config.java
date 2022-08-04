package io.cdap.plugin.janus.dto;

import lombok.Data;

import java.util.Map;

@Data
public class Config {
    protected String label;
    protected Map<String,String> properties;
}
