package be.unamur.snail.tool.energy;

import be.unamur.snail.core.Context;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.tool.energy.model.RunIterationDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.mockito.Mockito.*;

class JoularJXFolderProcessorTest {
    @TempDir
    Path tempDir;

    @Test
    void processFolderProcessesCsvFilesTest() throws IOException {
        Path csv = Files.createFile(tempDir.resolve("data.csv"));
        JoularJXFileProcessor fileProcessor = mock(JoularJXFileProcessor.class);
        PipelineLogger pipelineLogger = mock(PipelineLogger.class);
        JoularJXFolderProcessor folderProcessor = new JoularJXFolderProcessor(fileProcessor, pipelineLogger);
        RunIterationDTO iteration = mock(RunIterationDTO.class);
        Context context = mock(Context.class);

        folderProcessor.processFolder(csv, iteration, context);

        verify(fileProcessor).processFile(csv, iteration, context);
    }

    @Test
    void processFolderIgnoresNonCsvFilesTest() throws IOException {
        Files.createFile(tempDir.resolve("file.txt"));

        JoularJXFileProcessor fileProcessor = mock(JoularJXFileProcessor.class);
        PipelineLogger pipelineLogger = mock(PipelineLogger.class);
        JoularJXFolderProcessor processor = new JoularJXFolderProcessor(fileProcessor, pipelineLogger);
        processor.processFolder(tempDir, mock(RunIterationDTO.class), mock(Context.class));
        verifyNoInteractions(fileProcessor);
    }
}