package be.unamur.snail.sentinelbackend;

import be.unamur.snail.core.Config;
import be.unamur.snail.exceptions.UnsupportedDatabaseMode;
import be.unamur.snail.utils.CommandRunner;
import be.unamur.snail.utils.SimpleCommandRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SimpleBackendServiceManagerFactoryImplTest {
    @TempDir
    private Path tempDir;
    private Config config;
    private BackendServiceManagerFactory factory;
    private CommandRunner runner;

    @BeforeEach
    void setUp() throws Exception {
        Path yaml = tempDir.resolve("config.yaml");
        Files.writeString(yaml, """
            backend:
              mode: dev
              server-path: "/server/path"
              nb-check-server-start: 3
        """);
        Config.load(yaml.toString());
        config = Config.getInstance();
        factory = new SimpleBackendServiceManagerFactoryImpl();
        runner = new SimpleCommandRunner();
    }

    @Test
    void shouldCreateDevBackendServiceManagerIfDevModeTest() {
        BackendServiceManager manager = factory.create("dev", runner, "/server/path");
        assertNotNull(manager);
        assertInstanceOf(DevBackendServiceManager.class, manager);
    }

    @Test
    void shouldCreateProdBackendServiceManagerIfProdModeTest() {
        BackendServiceManager manager = factory.create("prod", runner, "/server/path");
        assertNotNull(manager);
        assertInstanceOf(ProdBackendServiceManager.class, manager);
    }

    @Test
    void shouldThrowExceptionForUnsupportedModeTest() {
        assertThrows(UnsupportedDatabaseMode.class, () -> factory.create("other", runner, "/server/path"));
        assertThrows(UnsupportedDatabaseMode.class, () -> factory.create("", runner, "/server/path"));
    }
}