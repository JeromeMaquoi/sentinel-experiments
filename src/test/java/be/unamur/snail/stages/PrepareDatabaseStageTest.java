package be.unamur.snail.stages;

import be.unamur.snail.config.Config;
import be.unamur.snail.exceptions.MongoServiceNotStartedException;
import be.unamur.snail.utils.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PrepareDatabaseStageTest {
    private PrepareDatabaseStage stage;
    @TempDir
    private Path tempDir;
    private Config config;
    private final Path readyFile = Paths.get("/tmp/backend-ready");

    @BeforeEach
    void setUp() throws Exception {
        stage = new PrepareDatabaseStage();
        Path yaml = tempDir.resolve("config.yaml");
        Files.writeString(yaml, """
            project:
              sub-project: ""
            repo:
              url: "https://example.com/repo.git"
              commit: "123abc"
              target-dir: "/tmp/repo"
            log:
              level: "DEBUG"
            database:
              backend-timeout-seconds: 120
              nb-check-server-start: 5
        """);
        Config.load(yaml.toString());
        config = Config.getInstance();
        Files.deleteIfExists(readyFile);
    }

    @Test
    void startMongoServiceShouldThrowException() {
        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.runCommand("sudo systemctl start mongod")).thenReturn(new Utils.CompletedProcess("cmd", 0, "", ""));
            mockedUtils.when(() -> Utils.runCommand("systemctl is-active mongod")).thenReturn(new Utils.CompletedProcess("cmd", 0, "inactive", ""));

            assertThrows(MongoServiceNotStartedException.class, () -> stage.startMongoService());

            mockedUtils.verify(() -> Utils.runCommand("sudo systemctl start mongod"), times(1));
            mockedUtils.verify(() -> Utils.runCommand("systemctl is-active mongod"), atLeast(5));
        }
    }

    @Test
    void startMongoServiceShouldSucceed() {
        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.runCommand("sudo systemctl start mongod")).thenReturn(new Utils.CompletedProcess("cmd", 0, "", ""));
            mockedUtils.when(() -> Utils.runCommand("systemctl is-active mongod")).thenReturn(new Utils.CompletedProcess("cmd", 0, "active", ""));

            assertDoesNotThrow(() -> stage.startMongoService());

            mockedUtils.verify(() -> Utils.runCommand("sudo systemctl start mongod"), times(1));
            mockedUtils.verify(() -> Utils.runCommand("systemctl is-active mongod"), atLeast(1));
        }
    }

    @Test
    void isScreenSessionRunningShouldSucceed() {
        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.runCommand("screen -ls | grep sentinel-backend")).thenReturn(new Utils.CompletedProcess("cmd", 0, "succeed", ""));

            assertTrue(stage.isScreenSessionRunning("sentinel-backend"));
        }
    }

    @Test
    void isScreenSessionRunningShouldFail() {
        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.runCommand("screen -ls | grep sentinel-backend")).thenReturn(new Utils.CompletedProcess("cmd", 1, "", ""));
            assertFalse(stage.isScreenSessionRunning("sentinel-backend"));
        }
    }

    @Test
    void isServerRunningShouldReturnTrue() throws IOException, InterruptedException {
        Files.writeString(readyFile, "READY");
        boolean result = stage.isServerRunning(3, 10);
        assertTrue(result, "Expected server to be considered running when file contains READY");
        assertFalse(Files.exists(readyFile), "READY file should be deleted after reading");
    }

    @Test
    void isServerRunningShouldReturnFalseWhenFileContainsFailed() throws IOException, InterruptedException {
        Files.writeString(readyFile, "FAILED");
        boolean result = stage.isServerRunning(3, 10);
        assertFalse(result, "Expected server to be considered not running when file contains FAILED");
        assertFalse(Files.exists(readyFile), "READY file should be deleted after reading");
    }

    @Test
    void isServerRunningShouldReturnFalseWhenFileNeverAppears() throws IOException, InterruptedException {
        boolean result = stage.isServerRunning(3, 200);
        assertFalse(result, "Expected server to be considered not running when file never appears");
    }

    @Test
    void isServerRunningShouldReturnTrueAfterSomeIterations() throws IOException, InterruptedException {
        new Thread(() -> {
            try {
                Thread.sleep(15);
                Files.writeString(readyFile, "READY");
            } catch (Exception ignored) {}
        }).start();

        boolean result = stage.isServerRunning(5, 20);
        assertTrue(result, "Expected server to start once backend-ready file is created during retries");
    }

    @Test
    void createCompleteCommandShouldSucceed() {
        String backendPath = "/some/path/to/script/";
        String completeCommand = stage.createCompleteCommand(backendPath);
        String expectedCommand = "screen -dmS sentinel-backend bash -c \"chmod +X /some/path/to/script/start-server.sh && ./some/path/to/script/start-server.sh 120\"";
        assertEquals(expectedCommand, completeCommand, "Command should be well created");
    }

    @Test
    void prepareDevDatabaseShouldSkipIfScreenSessionRunning() throws IOException, InterruptedException {
        String backendPath = "/tmp/backend";
        PrepareDatabaseStage stageMock = spy(new PrepareDatabaseStage());
        doReturn(true).when(stageMock).isScreenSessionRunning(backendPath);

        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            stageMock.prepareDevDatabase(backendPath);
            mockedUtils.verifyNoInteractions();
        }
    }

    @Test
    void prepareDevDatabaseStartsServerWhenNoScreenSessionRunning() throws IOException, InterruptedException {
        String backendPath = "/tmp/backend";
        PrepareDatabaseStage stageMock = spy(new PrepareDatabaseStage());
        doReturn(false).when(stageMock).isScreenSessionRunning(backendPath);
        doReturn(true).when(stageMock).isServerRunning(anyInt(), anyInt());

        String expectedStartScript = "screen -dmS sentinel-backend bash -c \"chmod +X /tmp/backend/start-server.sh 120 && ./tmp/backend/start-server.sh 120\"";
        doReturn(expectedStartScript).when(stageMock).createCompleteCommand(backendPath);

        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            stageMock.prepareDevDatabase(backendPath);

            mockedUtils.verify(() -> Utils.runCommand(expectedStartScript), times(1));
            verify(stageMock).isServerRunning(5, 1000);
        }
    }
}