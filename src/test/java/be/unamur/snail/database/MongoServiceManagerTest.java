package be.unamur.snail.database;

import be.unamur.snail.utils.CommandRunner;
import be.unamur.snail.utils.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MongoServiceManagerTest {
    private MongoServiceManager manager;
    private CommandRunner runner;

    @BeforeEach
    void setUp() {
        runner = mock(CommandRunner.class);
        manager = new MongoServiceManager(runner, 5, 10);
    }

    @Test
    void startMongoServiceReturnsFalseIfMongoServiceNotStartedTest() throws IOException, InterruptedException {
        when(runner.run("sudo systemctl start mongod")).thenReturn(new Utils.CompletedProcess("", 0, "", ""));
        when(runner.run("systemctl is-active mongod")).thenReturn(new Utils.CompletedProcess("", 0, "", ""));

        boolean result = manager.startMongoService();

        assertFalse(result);
        verify(runner).run("sudo systemctl start mongod");
        verify(runner, times(5)).run("systemctl is-active mongod");
    }

    @Test
    void startMongoServiceImmediatelyActiveTest() throws IOException, InterruptedException {
        // systemctl start succeeds
        when(runner.run("sudo systemctl start mongod"))
                .thenReturn(new Utils.CompletedProcess("", 0, "", ""));
        // systemctl is-active returns active immediately
        when(runner.run("systemctl is-active mongod"))
                .thenReturn(new Utils.CompletedProcess("", 0, "active", ""));

        boolean result = manager.startMongoService();

        assertTrue(result);
        verify(runner).run("sudo systemctl start mongod");
        verify(runner).run("systemctl is-active mongod");
    }

    @Test
    void startMongoServiceBecomesActivesAfterRetries() throws IOException, InterruptedException {
        when(runner.run("sudo systemctl start mongod"))
                .thenReturn(new Utils.CompletedProcess("", 0, "", ""));
        // First 2 calls return inactive, 3rd call returns active
        when(runner.run("systemctl is-active mongod"))
                .thenReturn(new Utils.CompletedProcess("inactive", 0, "inactive", ""))
                .thenReturn(new Utils.CompletedProcess("inactive", 0, "inative", ""))
                .thenReturn(new Utils.CompletedProcess("active", 0, "active", ""));

        boolean result = manager.startMongoService();

        assertTrue(result);
        verify(runner).run("sudo systemctl start mongod");
        verify(runner, times(3)).run("systemctl is-active mongod");
    }
}