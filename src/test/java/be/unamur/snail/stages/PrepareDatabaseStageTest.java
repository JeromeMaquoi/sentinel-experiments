package be.unamur.snail.stages;

import be.unamur.snail.config.Config;
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

//    @Test
//    void startMongoServiceShouldReturnFalseIfMongoServiceNotStarted() throws IOException, InterruptedException {
//        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
//            mockedUtils.when(() -> Utils.runCommand("sudo systemctl start mongod")).thenReturn(new Utils.CompletedProcess("cmd", 0, "", ""));
//            mockedUtils.when(() -> Utils.runCommand("systemctl is-active mongod")).thenReturn(new Utils.CompletedProcess("cmd", 0, "inactive", ""));
//
//            assertFalse(stage.startMongoService());
//
//            mockedUtils.verify(() -> Utils.runCommand("sudo systemctl start mongod"), times(1));
//            mockedUtils.verify(() -> Utils.runCommand("systemctl is-active mongod"), atLeast(5));
//        }
//    }
//
//    @Test
//    void startMongoServiceShouldReturnTrueIfMongoServiceIsStarted() throws IOException, InterruptedException {
//        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
//            mockedUtils.when(() -> Utils.runCommand("sudo systemctl start mongod")).thenReturn(new Utils.CompletedProcess("cmd", 0, "", ""));
//            mockedUtils.when(() -> Utils.runCommand("systemctl is-active mongod")).thenReturn(new Utils.CompletedProcess("cmd", 0, "active", ""));
//
//            assertTrue(stage.startMongoService());
//
//            mockedUtils.verify(() -> Utils.runCommand("sudo systemctl start mongod"), times(1));
//            mockedUtils.verify(() -> Utils.runCommand("systemctl is-active mongod"), atLeast(1));
//        }
//    }
//
//    @Test
//    void isServerRunningShouldReturnTrueAfterSomeIterations() throws IOException, InterruptedException {
//        new Thread(() -> {
//            try {
//                Thread.sleep(15);
//                Files.writeString(readyFile, "READY");
//            } catch (Exception ignored) {}
//        }).start();
//
//        boolean result = stage.isServerRunning(5, 20);
//        assertTrue(result, "Expected server to start once backend-ready file is created during retries");
//    }
}