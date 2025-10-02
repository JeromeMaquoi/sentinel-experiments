package be.unamur.snail.stages;

import be.unamur.snail.config.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.database.MongoServiceManager;
import be.unamur.snail.exceptions.MissingConfigKeyException;
import be.unamur.snail.exceptions.ServerNotStartedException;
import be.unamur.snail.exceptions.UnsupportedDatabaseMode;
import be.unamur.snail.sentinelbackend.BackendServiceManager;
import be.unamur.snail.sentinelbackend.BackendServiceManagerFactory;
import be.unamur.snail.utils.CommandRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PrepareBackendStageTest {
    @TempDir
    Path tempDir;
    private Config config;
    private Context context;

    private CommandRunner runner;
    private MongoServiceManager mongo;
    private BackendServiceManagerFactory backendFactory;
    private BackendServiceManager backendManager;
    private PrepareBackendStage prepareBackendStage;

    @BeforeEach
    void setUp() throws Exception {
        Path yaml = tempDir.resolve("config.yaml");
        Files.writeString(yaml, """
            backend:
              mode: dev
              server-path: "/server/path"
        """);
        Config.load(yaml.toString());
        config = Config.getInstance();
        context = new Context();

        runner = mock(CommandRunner.class);
        mongo = mock(MongoServiceManager.class);
        backendManager = mock(BackendServiceManager.class);
        backendFactory = mock(BackendServiceManagerFactory.class);

        when(backendFactory.create(any(), any(), any())).thenReturn(backendManager);

        prepareBackendStage = new PrepareBackendStage(runner, mongo, backendFactory);
    }

    @Test
    void executeShouldThrowExceptionIfBackendModeNotDefinedTest() {
        config.getBackend().setModeForTests(null);
        assertThrows(MissingConfigKeyException.class, () -> prepareBackendStage.execute(context));
        config.getBackend().setModeForTests("");
        assertThrows(MissingConfigKeyException.class, () -> prepareBackendStage.execute(context));
    }

    @Test
    void executeShouldThrowExceptionIfServerPathNotDefinedTest() {
        config.getBackend().setServerPathForTests(null);
        assertThrows(MissingConfigKeyException.class, () -> prepareBackendStage.execute(context));
        config.getBackend().setServerPathForTests("");
        assertThrows(MissingConfigKeyException.class, () -> prepareBackendStage.execute(context));
    }

    @Test
    void executeShouldInstantiateDevBackendManagerIfDevModeTest() throws Exception {
        prepareBackendStage.execute(context);
        verify(backendFactory).create(eq("dev"), eq(runner), eq("/server/path"));
    }

    @Test
    void executeShouldInstantiateProdBackendManagerIfProdModeTest() {

    }

    @Test
    void executeShouldThrowExceptionIfUnsupportedDatabaseModeTest() {
        config.getBackend().setModeForTests("other");
        when(backendFactory.create(eq("other"), any(), any())).thenThrow(new UnsupportedDatabaseMode("other"));
        assertThrows(UnsupportedDatabaseMode.class, () -> prepareBackendStage.execute(context));
    }
}