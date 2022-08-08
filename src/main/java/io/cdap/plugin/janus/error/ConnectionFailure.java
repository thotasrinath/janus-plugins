package io.cdap.plugin.janus.error;

public class ConnectionFailure extends Exception {
    public ConnectionFailure(String message) {
        super(message);
    }
}
