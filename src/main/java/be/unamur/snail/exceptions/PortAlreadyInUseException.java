package be.unamur.snail.exceptions;

public class PortAlreadyInUseException extends RuntimeException {
    public PortAlreadyInUseException(int port) {
        super("Port " + port + " is already in use");
    }
}
