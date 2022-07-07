package io.cdap.plugin.examples;


import org.apache.commons.configuration2.PropertiesConfiguration;

public class MyConfiguration extends PropertiesConfiguration {

    public void addPropertyNew(String key, Object value) {
        super.addPropertyDirect(key, value);
    }
}
