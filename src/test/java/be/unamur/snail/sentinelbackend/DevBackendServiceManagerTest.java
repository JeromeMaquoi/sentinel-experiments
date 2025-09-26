package be.unamur.snail.sentinelbackend;

import be.unamur.snail.config.Config;
import be.unamur.snail.utils.CommandRunner;
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

class DevBackendServiceManagerTest {
    private CommandRunner runner;
    private String backendPath = "/tmp/backend/";
    private final Path readyFile = Paths.get("/tmp/backend-ready");
    @TempDir
    private Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
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

        runner = mock(CommandRunner.class);
        Files.deleteIfExists(readyFile);
    }

    @Test
    void isScreenSessionRunningReturnsTrueTest() throws IOException, InterruptedException {
        DevBackendServiceManager manager = new DevBackendServiceManager(runner, backendPath, 5, 100);

        Utils.CompletedProcess completedProcess = mock(Utils.CompletedProcess.class);
        when(completedProcess.returnCode()).thenReturn(0);
        when(completedProcess.stdout()).thenReturn("1234.sentinel-backend");
        when(runner.run("screen -ls | grep " + backendPath)).thenReturn(completedProcess);

        boolean result = manager.isScreenSessionRunning(backendPath);
        assertTrue(result);
        verify(runner).run("screen -ls | grep " + backendPath);
    }

    @Test
    void isScreenSessionRunningReturnsFalseExceptionTest() throws IOException, InterruptedException {
        DevBackendServiceManager manager = new DevBackendServiceManager(runner, backendPath, 5, 100);
        when(runner.run("screen -ls | grep " + backendPath)).thenThrow(new IOException());

        boolean result = manager.isScreenSessionRunning(backendPath);
        assertFalse(result);
    }

    @Test
    void isServerRunningFileReadyTest() throws IOException, InterruptedException {
        DevBackendServiceManager manager = new DevBackendServiceManager(cmd -> null, backendPath, 5, 100);

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(readyFile)).thenReturn(true);
            filesMock.when(() -> Files.readString(readyFile)).thenReturn("READY");
            filesMock.when(() -> Files.deleteIfExists(readyFile)).thenReturn(true);

            boolean result = manager.isServerRunning();
            assertTrue(result);

            filesMock.verify(() -> Files.exists(readyFile));
            filesMock.verify(() -> Files.readString(readyFile));
            filesMock.verify(() -> Files.deleteIfExists(readyFile));
        }
    }

    @Test
    void isServerRunningFileNotExistingTest() throws IOException, InterruptedException {
        DevBackendServiceManager manager = new DevBackendServiceManager(cmd -> null, "/tmp/backend", 5, 100);

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(readyFile)).thenReturn(false);

            boolean result = manager.isServerRunning();
            assertFalse(result);

            filesMock.verify(() -> Files.readString(readyFile), never());
            filesMock.verify(() -> Files.deleteIfExists(readyFile), never());
        }
    }

    @Test
    void isServerRunningFileExistsButServerStartFailedTest() throws IOException, InterruptedException {
        DevBackendServiceManager manager = new DevBackendServiceManager(cmd -> null, backendPath, 5, 100);

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(readyFile)).thenReturn(true);
            filesMock.when(() -> Files.readString(readyFile)).thenReturn("FAILED");
            filesMock.when(() -> Files.deleteIfExists(readyFile)).thenReturn(true);

            boolean result = manager.isServerRunning();
            assertFalse(result);

            filesMock.verify(() -> Files.exists(readyFile));
            filesMock.verify(() -> Files.readString(readyFile));
            filesMock.verify(() -> Files.deleteIfExists(readyFile));
        }
    }

    @Test
    void createCompleteCommandShouldSucceed() {
        DevBackendServiceManager manager = new DevBackendServiceManager(cmd -> null, backendPath, 5, 100);

        String completeCommand = manager.createCompleteCommand(backendPath);
        String expectedCommand = "screen -dmS sentinel-backend bash -c \"chmod +X /tmp/backend/start-server.sh && ./tmp/backend/start-server.sh 120\"";
        assertEquals(expectedCommand, completeCommand, "Command should be well created");
    }

    @Test
    void startBackendIfScreenSessionAlreadyRunningTest() throws IOException, InterruptedException {
        CommandRunner runnerMock = mock(CommandRunner.class);
        DevBackendServiceManager manager = spy(new DevBackendServiceManager(runnerMock, backendPath, 5, 100));
        doReturn(true).when(manager).isScreenSessionRunning(anyString());

        boolean result = manager.startBackend();

        assertTrue(result);
        verify(manager).isScreenSessionRunning(anyString());
        verify(manager, never()).createCompleteCommand(anyString());
        verify(manager, never()).isServerRunning();
        verify(runner, never()).run(anyString());
    }

    @Test
    void startBackendIfScreenSessionNotRunningTest() throws IOException, InterruptedException {
        CommandRunner runnerMock = mock(CommandRunner.class);
        DevBackendServiceManager manager = spy(new DevBackendServiceManager(runnerMock, backendPath, 5, 100));

        doReturn(false).when(manager).isScreenSessionRunning(anyString());
        String dummyCommand = "screen -dmS sentinel-backend -c \"dummy\"";
        doReturn(dummyCommand).when(manager).createCompleteCommand(anyString());
        doReturn(true).when(manager).isServerRunning();

        boolean result = manager.startBackend();

        assertTrue(result);
        verify(manager).isScreenSessionRunning(anyString());
        verify(manager).createCompleteCommand(anyString());
        verify(manager).isServerRunning();
    }
}