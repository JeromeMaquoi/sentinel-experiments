package be.unamur.snail.spoon.constructor_instrumentation;

import java.io.IOException;
import java.util.InvalidPropertiesFormatException;

public class HttpClientService {
    private final SimpleHttpClient httpClient;

    public HttpClientService() {
        this.httpClient = new Java8HttpClient();
    }

    // Constructor for tests
    public HttpClientService(SimpleHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String post(String url, String jsonPayload) throws IOException, InterruptedException {
        if (jsonPayload == null) {
            throw new InvalidPropertiesFormatException("No data to send to the server");
        }

        SimpleHttpResponse response = httpClient.post(url, jsonPayload);
        if (response.statusCode() == 409) {
            return null;
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
//                System.out.println("Error: " + response.statusCode() + " " + response.body());
            throw new HttpErrorException(response.statusCode(), response.body());
        }

        return response.body();
    }
}
