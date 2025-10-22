package be.unamur.snail.tool;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.utils.HttpReleaseDownloader;
import be.unamur.snail.utils.ReleaseDownloader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JoularJXFetcherTest {
    private ReleaseDownloader downloader;
    private JoularJXFetcher fetcher;
    private Path tempDir;
    private Context context;

    @BeforeEach
    void setUp() throws Exception {
        downloader = mock(HttpReleaseDownloader.class);
        tempDir = Files.createTempDirectory("joular");
        fetcher = new JoularJXFetcher(downloader);
        context = new Context();

        Path tempJarDir = tempDir;
        Path yaml = tempDir.resolve("config.yaml");
        Files.writeString(yaml, """
            execution-plan:
                energy-measurements:
                    tool: "joularjx"
                    tool-version: "3.0.1"
                    release-url: "https://github.com/joular/joularjx"
                    tool-path: "%s"
        """.formatted(tempJarDir.toString()));
        Config.load(yaml.toString());
    }

    @Test
    void fetchReseaseShouldNotDownloadJarFileIfAlreadyPresentTest() throws Exception {
        Files.createFile(tempDir.resolve("joularjx-3.0.1.jar"));

        ToolReleaseResult result = fetcher.fetchRelease();

        verifyNoInteractions(downloader);
        assertEquals(tempDir.toString(), result.path());
        assertEquals("3.0.1", result.version());
    }

    @Test
    void fetchReleaseShouldDownloadJarFileAndConfigTest() throws Exception {
        ToolReleaseResult result = fetcher.fetchRelease();

        verify(downloader).downloadFile(
                URI.create("https://github.com/joular/joularjx/releases/download/3.0.1/joularjx-3.0.1.jar"),
                tempDir
        );

        verify(downloader).downloadFile(
                URI.create("https://github.com/joular/joularjx/releases/download/3.0.1/config.properties"),
                tempDir
        );

        assertEquals(tempDir.toString(), result.path());
        assertEquals("3.0.1", result.version());
    }

    @Test
    void fetchShouldThrowExceptionIfDownloadFailsTest() throws IOException, InterruptedException {
        doThrow(new RuntimeException("Network error")).when(downloader).downloadFile(any(URI.class), any(Path.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> fetcher.fetchRelease());
        assertTrue(exception.getMessage().contains("Network error"));
    }
}