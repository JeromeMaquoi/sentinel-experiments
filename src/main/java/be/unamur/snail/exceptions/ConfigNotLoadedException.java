package be.unamur.snail.exceptions;

public class ConfigNotLoadedException extends RuntimeException {
    public ConfigNotLoadedException() {
        super("Config not loaded yet.");
    }
}
