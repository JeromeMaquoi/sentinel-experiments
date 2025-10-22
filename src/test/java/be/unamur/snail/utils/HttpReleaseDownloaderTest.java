package be.unamur.snail.utils;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class HttpReleaseDownloaderTest {
    private HttpReleaseDownloader downloader;
    private Path tempDir;
    private static HttpServer server;
    private static int port;

    @BeforeAll
    static void beforeAll() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        port = server.getAddress().getPort();
        server.createContext("/test.txt", exchange -> {
            byte[] bytes = "hello world".getBytes();
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        // Simulate 404 error
        server.createContext("/missing.txt", exchange -> {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
        });
        server.start();
    }

    @AfterAll
    static void afterAll() {
        server.stop(0);
    }

    @BeforeEach
    void setUp() throws IOException {
        downloader = new HttpReleaseDownloader();
        tempDir = Files.createTempDirectory("download-test");
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walk(tempDir)
                .sorted((a, b) -> b.compareTo(a))
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    void downloadFileCreatesFileTest() throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:" + port + "/test.txt");
        Path result = downloader.downloadFile(uri, tempDir);
        assertTrue(Files.exists(result));
        assertEquals("hello world", Files.readString(result));
    }

    @Test
    void downloadFileShouldCreateDirectoryIfMissingTest() throws IOException, InterruptedException {
        Path customDir = tempDir.resolve("custom");
        URI uri = URI.create("http://localhost:" + port + "/test.txt");

        Path result = downloader.downloadFile(uri, customDir);

        assertTrue(Files.exists(result));
        assertEquals("hello world", Files.readString(result));
    }

    @Test
    void downloadFileShouldThrowExceptionTest() {
        URI uri = URI.create("http://localhost:" + port + "/missing.txt");
        IOException thrown = assertThrows(IOException.class, () -> downloader.downloadFile(uri, tempDir));
        assertTrue(thrown.getMessage().contains("404") || thrown.getMessage().toLowerCase().contains("server returned"));
    }
}