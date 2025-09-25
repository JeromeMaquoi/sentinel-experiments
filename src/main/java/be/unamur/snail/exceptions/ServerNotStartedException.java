package be.unamur.snail.exceptions;

public class ServerNotStartedException extends RuntimeException {
    public ServerNotStartedException() {
        super("Backend did not start within the expected time.");
    }
}
