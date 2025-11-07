package be.unamur.snail.sentinelbackend;

import be.unamur.snail.core.Config;
import be.unamur.snail.exceptions.MissingConfigKeyException;
import be.unamur.snail.exceptions.PortAlreadyInUseException;
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
            backend:
              server-host: localhost
              server-port: 8080
              server-timeout-seconds: 120
              nb-check-server-start: 5
              server-log-path: "/tmp/sentinel-backend.log"
              server-ready-path: "/tmp/backend-ready"
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
        String expectedCommand = "screen -dmS sentinel-backend bash -c \"cd /tmp/backend/ && chmod +X start-server.sh && PLUGINS_DIRECTORY=/tmp/backend/plugins ./start-server.sh 120 > /tmp/sentinel-backend.log 2>&1\"";
        assertEquals(expectedCommand, completeCommand, "Command should be well created");
    }

    @Test
    void createCompleteCommandShouldThrowExceptionIfMissingBackendLogPathTest() {
        DevBackendServiceManager manager = new DevBackendServiceManager(cmd -> null, backendPath, 5, 100);
        Config config = Config.getInstance();
        config.getBackend().setBackendLogPathForTests(null);

        assertThrows(MissingConfigKeyException.class, () -> manager.createCompleteCommand(backendPath));
    }

    @Test
    void startBackendIfScreenSessionAlreadyRunningTest() throws IOException, InterruptedException {
        CommandRunner runnerMock = mock(CommandRunner.class);
        DevBackendServiceManager manager = spy(new DevBackendServiceManager(runnerMock, backendPath, 5, 100));
        doReturn(true).when(manager).isScreenSessionRunning(anyString());
        doReturn(false).when(manager).isPortInUse(anyInt());
        doReturn(false).when(manager).isBackendAlreadyRunning(anyString(), anyInt());

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
        doReturn(false).when(manager).isPortInUse(anyInt());
        doReturn(false).when(manager).isBackendAlreadyRunning(anyString(), anyInt());

        boolean result = manager.startBackend();

        assertTrue(result);
        verify(manager).isScreenSessionRunning(anyString());
        verify(manager).createCompleteCommand(anyString());
        verify(manager).isServerRunning();
    }

    @Test
    void startBackendShouldThrowMissingConfigKeyExceptionIfBackendPortIsZeroTest() {
        DevBackendServiceManager manager = new DevBackendServiceManager(cmd -> null, backendPath, 5, 100);
        Config config = Config.getInstance();
        config.getBackend().setServerPortForTests(0);
        assertThrows(MissingConfigKeyException.class, manager::startBackend);
    }

    @Test
    void startBackendShouldReturnTrueIfBackendAlreadyRunningTest() throws IOException, InterruptedException {
        CommandRunner simpleRunner = command -> new Utils.CompletedProcess("", 0, "", "");
        DevBackendServiceManager spyManager = spy(new DevBackendServiceManager(simpleRunner, backendPath, 5, 100));
        doReturn(true).when(spyManager).isBackendAlreadyRunning(anyString(), anyInt());

        assertTrue(spyManager.startBackend());
    }

    @Test
    void startBackendShouldThrowPortAlreadyInUseExceptionIfPortUsedAndServerNotUpTest() {
        CommandRunner simpleRunner = command -> new Utils.CompletedProcess("", 0, "", "");

        DevBackendServiceManager manager = spy(new DevBackendServiceManager(simpleRunner, backendPath, 5, 100));
        doReturn(true).when(manager).isPortInUse(anyInt());
        doReturn(false).when(manager).isBackendAlreadyRunning(anyString(), anyInt());

        assertThrows(PortAlreadyInUseException.class, manager::startBackend);
    }

    @Test
    void stopBackendIfPortNoInUseTest() {
        CommandRunner runnerMock = mock(CommandRunner.class);
        DevBackendServiceManager manager = spy(new DevBackendServiceManager(runnerMock, backendPath, 5, 100));

        try {
            doReturn(false).when(manager).isPortInUse(anyInt());

            boolean result = manager.stopBackend();

            assertTrue(result);
            verify(runnerMock).run("screen -S sentinel-backend -X quit");
        } catch (IOException | InterruptedException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    void stopBackendIfNoPidFoundTest() {
        CommandRunner runnerMock = mock(CommandRunner.class);
        DevBackendServiceManager manager = spy(new DevBackendServiceManager(runnerMock, backendPath, 5, 100));

        try {
            doReturn(true).when(manager).isPortInUse(anyInt());
            Utils.CompletedProcess completedProcess = mock(Utils.CompletedProcess.class);
            when(completedProcess.stdout()).thenReturn("");
            when(runnerMock.run("lsof -t -i:8080")).thenReturn(completedProcess);

            boolean result = manager.stopBackend();

            assertFalse(result);
            verify(runnerMock).run("screen -S sentinel-backend -X quit");
            verify(runnerMock).run("lsof -t -i:8080");
        } catch (IOException | InterruptedException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    void stopBackendShouldKillProcessIfPortStillInUseTest() {
        CommandRunner runnerMock = mock(CommandRunner.class);
        DevBackendServiceManager manager = spy(new DevBackendServiceManager(runnerMock, backendPath, 5, 100));

        try {
            doReturn(true).when(manager).isPortInUse(anyInt());
            Utils.CompletedProcess completedProcess = mock(Utils.CompletedProcess.class);
            when(completedProcess.stdout()).thenReturn("1234\n");
            when(runnerMock.run("lsof -t -i:8080")).thenReturn(completedProcess);

            boolean result = manager.stopBackend();

            assertFalse(result);
            verify(runnerMock).run("screen -S sentinel-backend -X quit");
            verify(runnerMock).run("lsof -t -i:8080");
            verify(runnerMock).run("kill -9 1234");
        } catch (IOException | InterruptedException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    void isPortInUseShouldReturnTrueTest() throws IOException, InterruptedException {
        CommandRunner runnerMock = mock(CommandRunner.class);
        Utils.CompletedProcess completedProcess = mock(Utils.CompletedProcess.class);
        when(completedProcess.returnCode()).thenReturn(0);
        when(completedProcess.stdout()).thenReturn("some proces");
        when(runnerMock.run(anyString())).thenReturn(completedProcess);

        DevBackendServiceManager manager = spy(new DevBackendServiceManager(runnerMock, backendPath, 5, 100));

        boolean result = manager.isPortInUse(8080);
        assertTrue(result);
    }

    @Test
    void isPortInUseShouldReturnFalseTest() throws IOException, InterruptedException {
        CommandRunner runnerMock = mock(CommandRunner.class);
        Utils.CompletedProcess completedProcess = mock(Utils.CompletedProcess.class);
        when(completedProcess.returnCode()).thenReturn(1);
        when(completedProcess.stdout()).thenReturn("some proces");
        when(runnerMock.run(anyString())).thenReturn(completedProcess);

        DevBackendServiceManager manager = spy(new DevBackendServiceManager(runnerMock, backendPath, 5, 100));

        boolean result = manager.isPortInUse(8080);
        assertFalse(result);
    }

    @Test
    void isBackendAlreadyRunningShouldReturnTrueTest() throws IOException, InterruptedException {
        CommandRunner runnerMock = mock(CommandRunner.class);
        Utils.CompletedProcess completedProcess = mock(Utils.CompletedProcess.class);
        when(completedProcess.returnCode()).thenReturn(0);
        when(runnerMock.run("curl http://localhost:8080/management/health")).thenReturn(completedProcess);

        DevBackendServiceManager manager = spy(new DevBackendServiceManager(runnerMock, backendPath, 5, 100));
        assertTrue(manager.isBackendAlreadyRunning("localhost", 8080));
    }

    @Test
    void isBackendAlreadyRunningShouldReturnFalseTest() throws IOException, InterruptedException {
        CommandRunner runnerMock = mock(CommandRunner.class);
        Utils.CompletedProcess completedProcess = mock(Utils.CompletedProcess.class);
        when(completedProcess.returnCode()).thenReturn(1);
        when(runnerMock.run("curl http://localhost:8080/management/health")).thenReturn(completedProcess);

        DevBackendServiceManager manager = spy(new DevBackendServiceManager(runnerMock, backendPath, 5, 100));
        assertFalse(manager.isBackendAlreadyRunning("localhost", 8080));
    }
}