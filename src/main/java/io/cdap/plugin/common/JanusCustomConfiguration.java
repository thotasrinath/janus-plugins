package io.cdap.plugin.common;

import org.apache.commons.configuration2.PropertiesConfiguration;

public class JanusCustomConfiguration extends PropertiesConfiguration {
    public void addPropertyConfigDirect(String key, Object value) {
        super.addPropertyDirect(key, value);
    }
}
