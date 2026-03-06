package be.unamur.snail.spoon.constructor_instrumentation;


public class HttpErrorException extends RuntimeException {
    public HttpErrorException(int statusCode) {
        super("Http error: " + statusCode);
    }
}
