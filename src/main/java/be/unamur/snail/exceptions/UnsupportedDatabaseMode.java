package be.unamur.snail.exceptions;

public class UnsupportedDatabaseMode extends RuntimeException {
    public UnsupportedDatabaseMode(String mode) {
        super("Unsupported database mode: " + mode);
    }
}
