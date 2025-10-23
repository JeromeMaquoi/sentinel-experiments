package be.unamur.snail.exceptions;

public class UnknownProjectBuildException extends RuntimeException {
    public UnknownProjectBuildException() {
        super("Unknown project build");
    }
}
