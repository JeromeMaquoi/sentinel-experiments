package be.unamur.snail.spoon.constructor_instrumentation;

public class ConstructorContextNotCompletedException extends RuntimeException {
    public ConstructorContextNotCompletedException() {
        super("ConstructorContext is not completed");
    }
}
