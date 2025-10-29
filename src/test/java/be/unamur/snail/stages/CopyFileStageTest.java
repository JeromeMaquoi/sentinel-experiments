package be.unamur.snail.stages;

import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.SourceFileNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CopyFileStageTest {
    @TempDir
    Path tempDir;
    private Context context;

    @BeforeEach
    void setUp() {
        context = mock(Context.class);
    }

    @Test
    void executeShouldCopyFileSuccessfullyTest() throws Exception {
        String resourceFolder = "test-files";
        String resourceFileName = "sample.txt";
        createResourceFile(resourceFolder, resourceFileName, "This is a test file.");

        Path sourceFile = Path.of(resourceFolder, resourceFileName);
        Path relativeTargetPath = Path.of("build", resourceFileName);

        when(context.getRepoPath()).thenReturn(tempDir.toString());

        CopyFileStage stage = new CopyFileStage(sourceFile, relativeTargetPath);

        stage.execute(context);

        Path expectedTarget = tempDir.resolve(relativeTargetPath);
        assertTrue(Files.exists(expectedTarget), "Target file should exist after copy.");

        String content = Files.readString(expectedTarget);
        assertEquals("This is a test file.", content, "File content should match the source");
    }

    @Test
    void executeShouldThrowExceptionWhenResourceNotFoundTest() {
        Path sourceFile = Path.of("nonexistent-folder/file.txt");
        Path relativeTargetPath = Path.of("build/file.txt");

        when(context.getRepoPath()).thenReturn(tempDir.toString());
        CopyFileStage stage = new CopyFileStage(sourceFile, relativeTargetPath);
        assertThrows(SourceFileNotFoundException.class, () -> stage.execute(context));
    }

    @Test
    void executeShouldThrowExceptionWhenRepoPathNotFoundTest() {
        String resourceFolder = "test-files";
        String resourceFileName = "sample.txt";
        createResourceFile(resourceFolder, resourceFileName, "This is a test file.");

        Path sourceFile = Path.of(resourceFolder, resourceFileName);
        Path relativeTargetPath = Path.of("build", resourceFileName);

        when(context.getRepoPath()).thenReturn(null);

        CopyFileStage stage = new CopyFileStage(sourceFile, relativeTargetPath);

        assertThrows(Exception.class, () -> stage.execute(context));
    }

    private void createResourceFile(String folder, String name, String content) {
        try {
            Path baseDir = Path.of("target/test-classes", folder);
            Files.createDirectories(baseDir);
            Path filePath = baseDir.resolve(name);
            Files.writeString(filePath, content);
        } catch (IOException e) {
            fail("Failed to create test resource file: " + e.getMessage());
        }
    }
}