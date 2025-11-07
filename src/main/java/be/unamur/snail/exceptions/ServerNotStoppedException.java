package be.unamur.snail.exceptions;

public class ServerNotStoppedException extends RuntimeException {
    public ServerNotStoppedException() {
        super("The server could not be stopped.");
    }
}
