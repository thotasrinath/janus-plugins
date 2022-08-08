package io.cdap.plugin.janus.dto;

import java.util.Map;
import lombok.Data;

@Data
public class Config {
    protected String label;
    protected Map<String, String> properties;
}
