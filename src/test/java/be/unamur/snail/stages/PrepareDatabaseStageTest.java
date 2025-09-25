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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PrepareDatabaseStageTest {
    private PrepareDatabaseStage stage;
    @TempDir
    Path tempDir;
    Config config;

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
        """);
        Config.load(yaml.toString());
        config = Config.getInstance();
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
    void prepareDevDatabaseShouldSucceedIfScreenSessionNotRunning() throws IOException, InterruptedException {
        String backendPath = "/tmp/backend";
        PrepareDatabaseStage stageMock = spy(new PrepareDatabaseStage());
        doReturn(false).when(stageMock).isScreenSessionRunning(backendPath);

        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.runCommand(anyString(), eq(backendPath))).thenReturn(new Utils.CompletedProcess("cmd", 0, "", ""));
            stageMock.prepareDevDatabase(backendPath);

            String expectedCommand = "screen -dmS sentinel-backend bash -c \"cd /tmp/backend && ./mvnw clean && ./mvnw\"";

            mockedUtils.verify(() -> Utils.runCommand(expectedCommand, backendPath), times(1));
        }
    }
}