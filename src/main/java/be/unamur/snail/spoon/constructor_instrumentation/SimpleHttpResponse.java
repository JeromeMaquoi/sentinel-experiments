package be.unamur.snail.spoon.constructor_instrumentation;

public class SimpleHttpResponse {
    private final int statusCode;
    private final String body;

    public SimpleHttpResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public int statusCode() {
        return statusCode;
    }

    public String body() {
        return body;
    }
}
