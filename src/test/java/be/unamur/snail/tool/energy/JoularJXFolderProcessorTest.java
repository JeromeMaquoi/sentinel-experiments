package be.unamur.snail.tool.energy;

import be.unamur.snail.core.Context;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.tool.energy.model.RunIterationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JoularJXFolderProcessorTest {
    private JoularJXFileProcessor fileProcessor;
    private PipelineLogger log;
    private JoularJXFolderProcessor folderProcessor;
    private RunIterationDTO iteration;
    private Context context;

    @BeforeEach
    void setUp() {
        fileProcessor = mock(JoularJXFileProcessor.class);
        log = mock(PipelineLogger.class);
        folderProcessor = new JoularJXFolderProcessor(fileProcessor, log);
        iteration = mock(RunIterationDTO.class);
        context = mock(Context.class);
    }

    @Test
    void shouldProcessAllRegularFilesInFolderTest() throws IOException {
        Path folder = mock(Path.class);
        Path file1 = mock(Path.class);
        Path file2 = mock(Path.class);

        try (MockedStatic mocked = mockStatic(Files.class)) {
            mocked.when(() -> Files.walk(folder)).thenReturn(Stream.of(file1, file2));
            mocked.when(() -> Files.isRegularFile(any(Path.class))).thenReturn(true);

            folderProcessor.processFolder(folder, iteration, context);

            verify(fileProcessor).process(file1, iteration, context);
            verify(fileProcessor).process(file2, iteration, context);
        }
    }

    @Test
    void shouldSkipNonRegularFilesTest() throws IOException {
        Path folder = mock(Path.class);
        Path nonRegular = mock(Path.class);

        try (var mocked = mockStatic(Files.class)) {
            mocked.when(() -> Files.walk(folder)).thenReturn(Stream.of(nonRegular));
            mocked.when(() -> Files.isRegularFile(nonRegular)).thenReturn(false);

            folderProcessor.processFolder(folder, iteration, context);

            // File processor should not be called
            verify(fileProcessor, never()).process(any(), any(), any());
        }
    }

    @Test
    void shouldThrowIOExceptionWhenWalkFailsTest() throws IOException {
        Path folder = mock(Path.class);

        try (var mocked = mockStatic(Files.class)) {
            mocked.when(() -> Files.walk(folder)).thenThrow(new IOException("Walk failed"));

            assertThrows(IOException.class, () ->
                    folderProcessor.processFolder(folder, iteration, context));
        }
    }
}