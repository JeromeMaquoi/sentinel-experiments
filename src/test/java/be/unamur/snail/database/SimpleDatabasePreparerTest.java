package be.unamur.snail.database;

import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MongoServiceNotStartedException;
import be.unamur.snail.exceptions.ServerNotStartedException;
import be.unamur.snail.logging.ConsolePipelineLogger;
import be.unamur.snail.sentinelbackend.BackendServiceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SimpleDatabasePreparerTest {
    private DatabasePreparer databasePreparer;
    private MongoServiceManager mongoManager;
    private BackendServiceManager backendManager;
    private Context mockContext;

    @BeforeEach
    void setUp() {
        mongoManager = mock(MongoServiceManager.class);
        backendManager = mock(BackendServiceManager.class);
        databasePreparer = new SimpleDatabasePreparer(mongoManager, backendManager);
        mockContext = mock(Context.class);
        when(mockContext.getLogger()).thenReturn(new ConsolePipelineLogger(SimpleDatabasePreparer.class));
    }

    @Test
    void prepareDatabaseThrowsExceptionIfMongoServiceNotStarted() throws IOException, InterruptedException {
        when(mongoManager.startMongoService()).thenReturn(false);
        assertThrows(MongoServiceNotStartedException.class, () -> databasePreparer.prepareDatabase(mockContext));
    }

    @Test
    void prepareDatabaseThrowsExceptionIfServerNotStarted() throws IOException, InterruptedException {
        when(mongoManager.startMongoService()).thenReturn(true);
        when(backendManager.startBackend()).thenReturn(false);
        assertThrows(ServerNotStartedException.class, () -> databasePreparer.prepareDatabase(mockContext));
    }

    @Test
    void prepareDatabaseShouldNotThrowExceptionIfMongoServiceStartedAndServerStarted() throws IOException, InterruptedException {
        when(mongoManager.startMongoService()).thenReturn(true);
        when(backendManager.startBackend()).thenReturn(true);
        assertDoesNotThrow(() -> databasePreparer.prepareDatabase(mockContext));
    }
}