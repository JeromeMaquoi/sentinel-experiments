package be.unamur.snail.spoon.constructor_instrumentation;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.InvalidPropertiesFormatException;

public class HttpClientService {
    private final HttpClient httpClient;

    public HttpClientService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    // Constructor for tests
    public HttpClientService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String post(String url, String jsonPayload) throws IOException, InterruptedException {
        if (jsonPayload != null) {
            // Build the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            // Send the request and return the response
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            } else {
                System.out.println("Error: " + response.statusCode() + " " + response.body());
                throw new HttpErrorException(response.statusCode(), response.body());
            }
        }
        throw new InvalidPropertiesFormatException("No data to send to the server");
    }
}
