package io.cdap.plugin.dto;

import lombok.Data;

import java.util.List;

@Data
public class Config {
    protected String label;
    protected String id;
    protected List<String> properties;
    protected boolean isHardCodedLabel;
}
