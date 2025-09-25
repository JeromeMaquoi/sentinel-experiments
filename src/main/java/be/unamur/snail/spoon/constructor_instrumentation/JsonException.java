package be.unamur.snail.spoon.constructor_instrumentation;

public class JsonException extends RuntimeException {
    public JsonException(Throwable cause) {
        super("Error parsing JSON: ", cause);
    }
}
