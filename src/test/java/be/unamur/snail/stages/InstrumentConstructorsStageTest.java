package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingContextKeyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class InstrumentConstructorsStageTest {
    private InstrumentConstructorsStage stage;
    private Context context;
    private Config config;
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        stage = new InstrumentConstructorsStage();
        context = new Context();
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
        """);
        Config.load(yaml.toString());
        config = Config.getInstance();
        config.setRepoForTests(new Config.RepoConfig());
    }

    @Test
    void shouldThrowExceptionIfClassPathMissingTest() {
        assertThrows(MissingContextKeyException.class, () -> stage.execute(context));
    }
}