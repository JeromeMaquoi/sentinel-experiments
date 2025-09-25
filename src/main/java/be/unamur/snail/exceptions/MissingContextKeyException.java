package be.unamur.snail.exceptions;

public class MissingContextKeyException extends RuntimeException {
    public MissingContextKeyException(String key) {
        super("Missing context key: " + key);
    }
}
