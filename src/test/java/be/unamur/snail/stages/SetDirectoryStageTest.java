package be.unamur.snail.stages;

import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingContextKeyException;
import be.unamur.snail.exceptions.TargetDirectoryNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SetDirectoryStageTest {
    private SetDirectoryStage stage;
    private Context context;
    private Path tempDir;
    private String originalUserDir;

    @BeforeEach
    void setUp() throws IOException {
        stage = new SetDirectoryStage();
        context = new Context();
        originalUserDir = System.getProperty("user.dir");
        tempDir = Files.createTempDirectory("testRepoDir");
    }

    @AfterEach
    void tearDown() throws IOException {
        System.setProperty("user.dir", originalUserDir);
        if (tempDir != null && Files.exists(tempDir)) {
            Files.walk(tempDir)
                 .map(Path::toFile)
                 .forEach(File::delete);
        }
    }

    @Test
    void executeThrowsMissingContextKeyExceptionTest() {
        context.setRepoPath(null);
        assertThrows(MissingContextKeyException.class, () -> stage.execute(context));

        context.setRepoPath("");
        assertThrows(MissingContextKeyException.class, () -> stage.execute(context));
    }

    @Test
    void executeChangesWorkingDirectoryTest() throws Exception {
        context.setRepoPath(tempDir.toString());

        stage.execute(context);

        assertEquals(tempDir.toAbsolutePath().toString(), System.getProperty("user.dir"));
        assertEquals(tempDir.toAbsolutePath().toString(), context.getCurrentWorkingDir());
    }

    @Test
    void executeThrowsExceptionWhenDirectoryDoesNotExist() {
        context.setRepoPath(tempDir.resolve("nonExistentDir").toString());
        assertThrows(TargetDirectoryNotFoundException.class, () -> stage.execute(context));
    }

    @Test
    void executeThrowsExceptionWhenPathIsNotDirectory() throws IOException {
        Path tempFile = Files.createTempFile("testFile", ".txt");
        context.setRepoPath(tempFile.toString());
        assertThrows(TargetDirectoryNotFoundException.class, () -> stage.execute(context));
        Files.deleteIfExists(tempFile);
    }
}