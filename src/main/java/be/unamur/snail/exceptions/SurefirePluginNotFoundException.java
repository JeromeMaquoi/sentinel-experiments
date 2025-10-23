package be.unamur.snail.exceptions;

public class SurefirePluginNotFoundException extends RuntimeException {
    public SurefirePluginNotFoundException() {
        super("Surefire plugin not found");
    }
}
