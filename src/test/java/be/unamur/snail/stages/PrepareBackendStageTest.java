package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.database.DatabasePreparer;
import be.unamur.snail.database.DatabasePreparerFactory;
import be.unamur.snail.exceptions.MissingConfigKeyException;
import be.unamur.snail.exceptions.UnsupportedDatabaseMode;
import be.unamur.snail.logging.ConsolePipelineLogger;
import be.unamur.snail.sentinelbackend.BackendServiceManager;
import be.unamur.snail.sentinelbackend.BackendServiceManagerFactory;
import be.unamur.snail.utils.CommandRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PrepareBackendStageTest {
    @TempDir
    private Path tempDir;
    private Config config;
    private Context context;

    private CommandRunner runner;
    private BackendServiceManagerFactory backendFactory;
    private DatabasePreparerFactory databaseFactory;
    private BackendServiceManager backendManager;
    private DatabasePreparer databasePreparer;
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
        context.setLogger(new ConsolePipelineLogger(PrepareBackendStage.class));

        runner = mock(CommandRunner.class);
        backendManager = mock(BackendServiceManager.class);
        backendFactory = mock(BackendServiceManagerFactory.class);
        databaseFactory = mock(DatabasePreparerFactory.class);
        databasePreparer = mock(DatabasePreparer.class);

        when(backendFactory.create(any(), any(), any())).thenReturn(backendManager);
        when(databaseFactory.create(backendManager)).thenReturn(databasePreparer);

        prepareBackendStage = new PrepareBackendStage(runner, backendFactory, databaseFactory);
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
        verify(databaseFactory).create(backendManager);
        verify(databasePreparer).prepareDatabase();
    }

    @Test
    void executeShouldInstantiateProdBackendManagerIfProdModeTest() throws Exception {
        config.getBackend().setModeForTests("prod");
        prepareBackendStage.execute(context);

        verify(backendFactory).create(eq("prod"), eq(runner), eq("/server/path"));
        verify(databaseFactory).create(backendManager);
        verify(databasePreparer).prepareDatabase();
    }

    @Test
    void executeShouldThrowExceptionIfUnsupportedDatabaseModeTest() {
        config.getBackend().setModeForTests("other");
        when(backendFactory.create(eq("other"), any(), any())).thenThrow(new UnsupportedDatabaseMode("other"));
        assertThrows(UnsupportedDatabaseMode.class, () -> prepareBackendStage.execute(context));
    }
}