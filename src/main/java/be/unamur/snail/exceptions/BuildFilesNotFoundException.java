package be.unamur.snail.exceptions;

public class BuildFilesNotFoundException extends RuntimeException {
    public BuildFilesNotFoundException() {
        super("Build files directory not found.");
    }
}
