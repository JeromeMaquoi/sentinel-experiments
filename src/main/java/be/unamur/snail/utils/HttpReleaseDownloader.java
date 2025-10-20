package be.unamur.snail.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

public class HttpReleaseDownloader implements ReleaseDownloader {
    private static final Logger log = LoggerFactory.getLogger(HttpReleaseDownloader.class);
    private final HttpClient client;

    public HttpReleaseDownloader() {
        this.client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    @Override
    public Path downloadFile(URI uri, Path destinationDir) throws IOException, InterruptedException {
        if (!Files.exists(destinationDir)) {
            Files.createDirectories(destinationDir);
        }

        String fileName = Path.of(uri.getPath()).getFileName().toString();
        Path outputPath = destinationDir.resolve(fileName);

        log.info("Downloading {} → {}", uri, outputPath);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/octet-stream")
                .GET()
                .build();
        HttpResponse<Path> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofFile(outputPath)
        );
        if (response.statusCode() != 200) {
            throw new IOException("Failed to download " + uri + " (HTTP " + response.statusCode() + ")");
        }
        return outputPath;
    }
}
