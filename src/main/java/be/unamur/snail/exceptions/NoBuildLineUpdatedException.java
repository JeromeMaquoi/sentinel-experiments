package be.unamur.snail.exceptions;

public class NoBuildLineUpdatedException extends RuntimeException {
    public NoBuildLineUpdatedException(String buildFilePath) {
        super("No build line updated in file: " + buildFilePath);
    }
}
