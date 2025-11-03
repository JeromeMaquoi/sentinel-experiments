package be.unamur.snail.exceptions;

public class LogFileCreationFailedException extends RuntimeException {
    public LogFileCreationFailedException() {
        super("Failed to create log file");
    }
}
