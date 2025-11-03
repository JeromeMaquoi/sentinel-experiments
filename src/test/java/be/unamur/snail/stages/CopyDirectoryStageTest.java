package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingConfigKeyException;
import be.unamur.snail.exceptions.SourceDirectoryNotFoundException;
import be.unamur.snail.logging.ConsolePipelineLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

class CopyDirectoryStageTest {
    private CopyDirectoryStage stage;
    private Context context;
    private Config config;
    @TempDir
    private Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        stage = new CopyDirectoryStage();
        context = new Context();
        context.setLogger(new ConsolePipelineLogger(CopyDirectoryStage.class));
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
        config.setProjectForTests(new Config.ProjectConfig());
    }

    @Test
    void shouldThrowExceptionWhenTargetDirMissingTest() {
        config.getRepo().setTargetDirForTests(null);
        config.getRepo().setCommitForTests("123abc");
        assertThrows(MissingConfigKeyException.class, () -> stage.execute(context));

        config.getRepo().setTargetDirForTests("");
        assertThrows(MissingConfigKeyException.class, () -> stage.execute(context));
    }

    @Test
    void shouldThrowExceptionWhenCommitMissingTest() {
        config.getRepo().setTargetDirForTests("target");
        config.getRepo().setCommitForTests(null);
        assertThrows(MissingConfigKeyException.class, () -> stage.execute(context));

        config.getRepo().setCommitForTests("");
        assertThrows(MissingConfigKeyException.class, () -> stage.execute(context));
    }

    @Test
    void shouldThrowExceptionWhenSourceDirectoryDoesNotExistTest() {
        config.getRepo().setTargetDirForTests("nonexistent");
        config.getRepo().setCommitForTests("123abc");
        assertThrows(SourceDirectoryNotFoundException.class, () -> stage.execute(context));
    }

    @Test
    void shouldCopyDirectoryTest(@TempDir Path tempDir) throws Exception {
        Path source = tempDir.resolve("src");
        Files.createDirectories(source);
        Path file = source.resolve("file.txt");
        Files.writeString(file, "Hello World");

        config.getRepo().setTargetDirForTests(source.toString());
        config.getRepo().setCommitForTests("123abc");
        config.getRepo().setOverwriteForTests(true);

        stage.execute(context);

        Path target = tempDir.resolve("src_123abc");
        assertThat(target).exists().isDirectory();
        assertThat(target.resolve("file.txt")).hasContent("Hello World");
        assertThat(context.getRepoPath()).isEqualTo(target.toAbsolutePath().toString());
    }

    @Test
    void shouldSkipCopyIfTargetExistsAdnNoOverwriteTest(@TempDir Path tempDir) throws Exception {
        Path source = Files.createDirectory(tempDir.resolve("src"));
        Path target = Files.createDirectory(tempDir.resolve("src_123abc"));


        config.getRepo().setTargetDirForTests(source.toString());
        config.getRepo().setCommitForTests("123abc");
        config.getRepo().setOverwriteForTests(false);

        stage.execute(context);

        assertThat(context.getRepoPath()).isEqualTo(target.toAbsolutePath().toString());
    }
}