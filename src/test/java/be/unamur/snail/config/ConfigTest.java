package be.unamur.snail.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {
    @TempDir
    Path tempDir;

    @Test
    void loadConfigFromYamlTest() throws Exception {
        Path yaml = tempDir.resolve("config.yaml");
        Files.writeString(yaml, """
            project:
              sub-project: "sub/module"
            repo:
              url: "https://example.com/repo.git"
              commit: "123abc"
              target-dir: "/tmp/repo"
            log:
              level: "DEBUG"
        """);
        Config.load(yaml.toString());
        Config config = Config.getInstance();
        assertNotNull(config);
        assertEquals("sub/module", config.getProject().getSubProject());
        assertEquals("https://example.com/repo.git", config.getRepo().getUrl());
        assertEquals("123abc", config.getRepo().getCommit());
        assertEquals("/tmp/repo", config.getRepo().getTargetDir());
    }

    @Test
    void getInstanceFailsIfNotLoadedTest() {
        assertThrows(IllegalStateException.class, Config::getInstance);
    }

    @AfterEach
    void tearDown() {
        Config.reset();
    }
}