package be.unamur.snail.exceptions;

public class InvalidUrlException extends IllegalArgumentException {
    public InvalidUrlException(String url) {
        super("Invalid URL: " + url);
    }
}
