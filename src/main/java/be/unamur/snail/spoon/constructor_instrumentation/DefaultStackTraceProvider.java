package be.unamur.snail.spoon.constructor_instrumentation;

public class DefaultStackTraceProvider implements StackTraceProvider {
    @Override
    public StackTraceElement[] getStackTrace() {
        return Thread.currentThread().getStackTrace();
    }
}
