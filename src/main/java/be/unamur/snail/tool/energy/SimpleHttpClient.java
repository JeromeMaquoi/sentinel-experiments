package be.unamur.snail.tool.energy;

import be.unamur.snail.exceptions.InvalidUrlException;
import be.unamur.snail.spoon.constructor_instrumentation.HttpErrorException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.InvalidPropertiesFormatException;

public class SimpleHttpClient {
    private final HttpClient httpClient;

    public SimpleHttpClient() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public SimpleHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String post(String url, String payload) throws IOException, InterruptedException {
        if (payload == null) {
            throw new InvalidPropertiesFormatException("No data to send to the server");
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            } else {
                throw new HttpErrorException(response.statusCode(), response.body());
            }
        } catch (IllegalArgumentException e) {
            throw new InvalidUrlException(url);
        }
    }
}
