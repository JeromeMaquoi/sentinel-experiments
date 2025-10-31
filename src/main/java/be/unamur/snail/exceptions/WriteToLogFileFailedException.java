package be.unamur.snail.exceptions;

public class WriteToLogFileFailedException extends RuntimeException {
    public WriteToLogFileFailedException() {
        super("Failed to write to log file");
    }
}
