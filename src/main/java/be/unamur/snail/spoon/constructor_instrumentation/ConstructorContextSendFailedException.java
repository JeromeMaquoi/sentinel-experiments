package be.unamur.snail.spoon.constructor_instrumentation;

public class ConstructorContextSendFailedException extends RuntimeException {
    public ConstructorContextSendFailedException(Throwable cause) {
        super("Failed to send constructor context : ", cause);
    }
}
