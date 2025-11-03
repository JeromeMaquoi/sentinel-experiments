package be.unamur.snail.stages;

import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingContextKeyException;
import be.unamur.snail.exceptions.NoBuildLineUpdatedException;
import be.unamur.snail.logging.ConsolePipelineLogger;
import be.unamur.snail.logging.PipelineLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UpdateBuildFileStageTest {
    @TempDir
    Path tempRepo;

    private Context mockContext;
    PipelineLogger log = new ConsolePipelineLogger(UpdateBuildFileStage.class);
    private UpdateBuildFileStage stage;

    @BeforeEach
    void setUp() {
        mockContext = mock(Context.class);
        when(mockContext.getLogger()).thenReturn(log);
        stage = new UpdateBuildFileStage();
    }

    @Test
    void executeThrowsExceptionWhenRepoPathMissingTest() {
        assertThrows(MissingContextKeyException.class, () -> stage.execute(mockContext));
        when(mockContext.getRepoPath()).thenReturn("");
        assertThrows(MissingContextKeyException.class, () -> stage.execute(mockContext));
    }

    @Test
    void executeThrowsExceptionWhenToolPathMissingTest() {
        when(mockContext.getRepoPath()).thenReturn("/some/repo/path");
        assertThrows(MissingContextKeyException.class, () -> stage.execute(mockContext));
        when(mockContext.getEnergyToolPath()).thenReturn("");
        assertThrows(MissingContextKeyException.class, () -> stage.execute(mockContext));
    }

    @Test
    void executeThrowsExceptionWhenCopiedBuildFilePathMissingTest() {
        when(mockContext.getRepoPath()).thenReturn("/some/repo/path");
        when(mockContext.getEnergyToolPath()).thenReturn("/some/tool/path");
        assertThrows(MissingContextKeyException.class, () -> stage.execute(mockContext));
        when(mockContext.getCopiedBuildFilePath()).thenReturn("");
        assertThrows(MissingContextKeyException.class, () -> stage.execute(mockContext));
    }

    @Test
    void executeThrowsExceptionWhenBuildFileNotFoundTest() {
        when(mockContext.getRepoPath()).thenReturn("/some/repo/path");
        when(mockContext.getEnergyToolPath()).thenReturn("/some/tool/path");
        when(mockContext.getCopiedBuildFilePath()).thenReturn("nonexistent-build-file.xml");

        assertThrows(Exception.class, () -> stage.execute(mockContext));
    }

    @Test
    void executeShouldUpdateBuildFileSuccessfullyTest() throws Exception {
        Path buildFile = tempRepo.resolve("build.gradle");
        String originalContent = "plugins { id 'java' }\\n-javaagent:JOULARJX";
        Files.writeString(buildFile, originalContent);

        when(mockContext.getRepoPath()).thenReturn(tempRepo.toString());
        when(mockContext.getEnergyToolPath()).thenReturn("/some/tool/path");
        when(mockContext.getCopiedBuildFilePath()).thenReturn("build.gradle");

        stage.execute(mockContext);

        String updated = Files.readString(buildFile);
        assertTrue(updated.contains("-javaagent:/some/tool/path"));
    }

    @Test
    void updateBuildFileShouldThrowExceptionIfJavaAgentWithoutJoularJXLineTest() throws IOException {
        Path buildFile = tempRepo.resolve("build.gradle");
        Files.writeString(buildFile, "-javaagent:SomeOtherAgent");
        assertThrows(NoBuildLineUpdatedException.class, () -> stage.updateBuildFile(buildFile, "/some/tool/path", log));
    }

    @Test
    void updateBuildFileShouldThrowExceptionIfNoJavaAgentButJoularJXLineTest() throws IOException {
        Path buildFile = tempRepo.resolve("build.gradle");
        Files.writeString(buildFile, "some config\\nJOULARJX without javaagent");
        assertThrows(NoBuildLineUpdatedException.class, () -> stage.updateBuildFile(buildFile, "/some/tool/path", log));
    }

    @Test
    void updateBuildFileShouldUpdateLineCorrectlyTest() throws IOException {
        Path buildFile = tempRepo.resolve("build.gradle");
        Files.writeString(buildFile, "some config\\n-javaagent:JOULARJX\\nother line");

        stage.updateBuildFile(buildFile, "/tool/joularjx.jar", log);

        String updated = Files.readString(buildFile);
        assertTrue(updated.contains("-javaagent:/tool/joularjx.jar"));
        assertTrue(updated.contains("other line"));
        assertTrue(updated.contains("some config"));
    }
}