package be.unamur.snail.exceptions;

public class CouldNotParsePomException extends RuntimeException {
    public CouldNotParsePomException() {
        super("Could not parse the pom.xml file for JDK version");
    }
}
