package be.unamur.snail.exceptions;

public class BuildFileNotFoundException extends RuntimeException {
    public BuildFileNotFoundException(String totalProjectPath) {
        super("No build file found for " + totalProjectPath);
    }
}
