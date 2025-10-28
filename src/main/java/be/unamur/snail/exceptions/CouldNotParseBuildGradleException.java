package be.unamur.snail.exceptions;

public class CouldNotParseBuildGradleException extends RuntimeException {
    public CouldNotParseBuildGradleException() {
        super("Could not parse build.gradle file for JDK version");
    }
}
