package be.unamur.snail.stages;

import be.unamur.snail.config.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.DirectoryNotCopiedException;
import be.unamur.snail.exceptions.SourceDirectoryNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.*;

class CopySourceCodeStageTest {
    @TempDir
    Path tempDir;

    private Config config;
    private CopySourceCodeStage stage;

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
        """);
        Config.load(yaml.toString());
        config = Config.getInstance();
        config.setRepoForTests(new Config.RepoConfig());
        config.setProjectForTests(new Config.ProjectConfig());
        stage = new CopySourceCodeStage();
    }

    @AfterEach
    void tearDown() {
        Config.reset();
    }

    @Test
    void copyJavaFilesSuccessTest() throws Exception {
        Path sourceDir = tempDir.resolve("be/unamur/snail/");
        Files.createDirectories(sourceDir);
        Path javaFile = sourceDir.resolve("Test.java");
        Files.writeString(javaFile, "package be.unamur.snail; class Test {}");

        config.setCodeConstructorsInstrumentationPathForTests(sourceDir.toString());
        config.getRepo().setTargetDirForTests(tempDir.resolve("target-project").toString());
        config.getRepo().setCommitForTests("abc123");
        config.getProject().setSubProjectForTests("");

        Context context = new Context();

        // Act
        stage.execute(context);

        // Assert: check if file was copied
        Path expectedTarget = Paths.get(config.getRepo().getTargetDir() + "_abc123/src/main/java/be/unamur/snail/Test.java");
        assertThat(expectedTarget).exists();
        assertThat(Files.readString(expectedTarget)).isEqualTo("package be.unamur.snail; class Test {}");
    }

    @Test
    void copyJavaFilesSourceNotFoundTest() {
        Path nonExistentDir = tempDir.resolve("does_not_exist");
        config.setCodeConstructorsInstrumentationPathForTests(nonExistentDir.toString());
        config.getRepo().setTargetDirForTests(tempDir.resolve("target-project").toString());
        config.getRepo().setCommitForTests("abc123");
        config.getProject().setSubProjectForTests("");

        Context context = new Context();

        // Act + Assert
        assertThatThrownBy(() -> stage.execute(context))
                .isInstanceOf(SourceDirectoryNotFoundException.class)
                .hasMessageContaining("does_not_exist");
    }

    @Test
    void doesNotOverwriteExistingFilesTest() throws IOException {
        Path sourceDir = tempDir.resolve("source");
        Files.createDirectories(sourceDir);
        Path javaFile = sourceDir.resolve("Test.java");
        Files.writeString(javaFile, "class Test {}");

        config.setCodeConstructorsInstrumentationPathForTests(sourceDir.toString());
        config.getRepo().setTargetDirForTests(tempDir.resolve("target-project").toString());
        config.getRepo().setCommitForTests("abc123");
        config.getProject().setSubProjectForTests("");

        Context context = new Context();

        Path targetFile = Paths.get(config.getRepo().getTargetDir() + "_abc123/src/main/java/be/unamur/snail/Test.java");
        Files.createDirectories(targetFile.getParent());
        Files.writeString(targetFile, "class Existing {}");

        assertThatThrownBy(() -> stage.execute(context))
                .isInstanceOf(DirectoryNotCopiedException.class);

        assertThat(Files.readString(targetFile)).isEqualTo("class Existing {}");
    }
}