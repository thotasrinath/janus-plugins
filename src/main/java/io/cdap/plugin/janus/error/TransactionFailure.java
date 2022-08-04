package io.cdap.plugin.janus.error;

public class TransactionFailure extends Exception {
    public TransactionFailure(String message) {
        super(message);
    }
}
