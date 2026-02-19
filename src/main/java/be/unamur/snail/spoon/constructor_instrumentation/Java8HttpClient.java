package be.unamur.snail.spoon.constructor_instrumentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Java8HttpClient implements SimpleHttpClient{
    @Override
    public SimpleHttpResponse post(String url, String jsonPayload) throws IOException {
        URL targetUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input);
        }

        int statusCode = connection.getResponseCode();

        StringBuilder body = getStringBuilder(statusCode, connection);

        return new SimpleHttpResponse(statusCode, body.toString());
    }

    private static StringBuilder getStringBuilder(int statusCode, HttpURLConnection connection) throws IOException {
        BufferedReader reader;
        if (statusCode >= 200 && statusCode < 300) {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        } else {
            reader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
        }

        StringBuilder body = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }
        return body;
    }
}
