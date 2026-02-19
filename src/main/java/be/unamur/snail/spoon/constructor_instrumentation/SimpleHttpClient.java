package be.unamur.snail.spoon.constructor_instrumentation;

import java.io.IOException;

public interface SimpleHttpClient {
    SimpleHttpResponse post(String url, String jsonPayload) throws IOException;
}
