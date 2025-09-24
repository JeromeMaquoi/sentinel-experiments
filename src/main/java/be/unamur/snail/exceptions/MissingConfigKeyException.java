package be.unamur.snail.exceptions;

public class MissingConfigKeyException extends RuntimeException {
    public MissingConfigKeyException(String key) {
        super("Missing config key: " + key);
    }
}
