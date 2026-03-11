package be.unamur.snail.stages;

import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingContextKeyException;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.tool.energy.FolderProcessor;
import be.unamur.snail.tool.energy.FolderProcessorFactory;
import be.unamur.snail.tool.energy.model.RunIterationDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImportMeasurementsStageTest {
    @TempDir
    Path tempDir;

    @Test
    void executeThrowsWhenRepoPathMissingTest() {
        Context context = mock(Context.class);
        when(context.getRepoPath()).thenReturn(null);

        FolderProcessorFactory factory = mock(FolderProcessorFactory.class);
        ImportMeasurementsStage stage = new ImportMeasurementsStage(Path.of("results"), factory);

        assertThrows(MissingContextKeyException.class, () -> stage.execute(context));
    }

    @Test
    void executeProcessesIterationFoldersTest() throws Exception {
        Path results = Files.createDirectory(tempDir.resolve("results"));
        Path iteration = Files.createDirectory(results.resolve("123-4567"));

        Context context = mock(Context.class);
        when(context.getRepoPath()).thenReturn(tempDir.toString());

        PipelineLogger log =  mock(PipelineLogger.class);
        when(context.getLogger()).thenReturn(log);

        FolderProcessor processor = mock(FolderProcessor.class);
        FolderProcessorFactory factory = mock(FolderProcessorFactory.class);
        when(factory.create(context)).thenReturn(processor);

        ImportMeasurementsStage stage = new ImportMeasurementsStage(Path.of("results"), factory);
        stage.execute(context);

        verify(processor).processFolder(eq(iteration), any(), eq(context));
    }

    @Test
    void parseIterationExtractsPidAndTimestampTest() {
        ImportMeasurementsStage stage = new ImportMeasurementsStage(Path.of("results"), mock(FolderProcessorFactory.class));
        RunIterationDTO dto = stage.parseIterationFromFolder(Path.of("123-4567"));
        assertEquals(123, dto.getPid());
        assertEquals(4567L, dto.getStartTimestamp());
    }
}