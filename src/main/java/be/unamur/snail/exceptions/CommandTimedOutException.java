package be.unamur.snail.exceptions;

public class CommandTimedOutException extends RuntimeException {
    public CommandTimedOutException(String command) {
        super("Command timed out: " + command);
    }
}
