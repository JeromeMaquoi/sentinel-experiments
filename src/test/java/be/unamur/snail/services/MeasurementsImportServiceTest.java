package be.unamur.snail.services;

import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.SourceDirectoryNotFoundException;
import be.unamur.snail.files.DirectoryService;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.tool.energy.FolderProcessor;
import be.unamur.snail.tool.energy.FolderProcessorFactory;
import be.unamur.snail.tool.energy.model.RunIterationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MeasurementsImportServiceTest {
    private DirectoryService directoryService;
    private FolderProcessorFactory processorFactory;
    private FolderProcessor processor;
    private Context context;
    private PipelineLogger logger;

    private MeasurementsImportService service;

    @BeforeEach
    void setUp() {
        directoryService = mock(DirectoryService.class);
        processorFactory = mock(FolderProcessorFactory.class);
        processor = mock(FolderProcessor.class);
        context = mock(Context.class);
        logger = mock(PipelineLogger.class);

        when(context.getLogger()).thenReturn(logger);
        when(processorFactory.create(context)).thenReturn(processor);

        service = new MeasurementsImportService(directoryService, processorFactory);
    }

    @Test
    void importMeasurementsThrowsIfDirectoryDoesNotExistTest() {
        Path resultsRoot = Path.of("results");
        String targetDir = "/repo/";
        Path expectedPath = Path.of(targetDir).resolve(resultsRoot).normalize();

        when(directoryService.exists(expectedPath)).thenReturn(false);

        assertThrows(SourceDirectoryNotFoundException.class, () -> service.importMeasurements(resultsRoot, targetDir, context));
    }

    @Test
    void importMeasurementsThrowsIfNotDirectoryTest() {
        Path resultsRoot = Path.of("results");
        String targetDir = "/repo/";
        Path expectedPath = Path.of(targetDir).resolve(resultsRoot).normalize();

        when(directoryService.exists(expectedPath)).thenReturn(true);
        when(directoryService.isDirectory(expectedPath)).thenReturn(false);

        assertThrows(SourceDirectoryNotFoundException.class, () -> service.importMeasurements(resultsRoot, targetDir, context));
    }

    @Test
    void importMeasurementsProcessesAllIterationFoldersTest() throws Exception {
        Path resultsRoot = Path.of("results");
        String targetDir = "/repo/";
        Path totalPath = Path.of(targetDir).resolve(resultsRoot).normalize();

        when(directoryService.exists(totalPath)).thenReturn(true);
        when(directoryService.isDirectory(totalPath)).thenReturn(true);

        Path iteration1 = totalPath.resolve("/repo/results/1234-5678");
        Path iteration2 = totalPath.resolve("/repo/results/4321-8765");

        when(directoryService.listDirectories(totalPath)).thenReturn(List.of(iteration1, iteration2));

        service.importMeasurements(resultsRoot, targetDir, context);

        verify(processorFactory).create(context);
        verify(processor).processFolder(eq(iteration1), any(RunIterationDTO.class), eq(context));
        verify(processor).processFolder(eq(iteration2), any(RunIterationDTO.class), eq(context));

        verify(logger).info("Importing iteration folder: {}", iteration1);
        verify(logger).info("Importing iteration folder: {}", iteration2);
    }

    @Test
    void parseIterationFromFolderParsesPidAndTimestampTest() {
        Path folder = Path.of("/repo/results/1234-56789");
        RunIterationDTO dto = service.parseIterationFromFolder(folder);

        assertEquals(1234, dto.getPid());
        assertEquals(56789L, dto.getStartTimestamp());
    }

    @Test
    void importMeasurementsCreatesCorrectIterationDTOTest() throws Exception {
        Path resultsRoot = Path.of("results");
        String targetDir = "/repo/";
        Path totalPath =  Path.of(targetDir).resolve(resultsRoot).normalize();

        Path folder = Path.of("/repo/results/1234-5678");

        when(directoryService.exists(totalPath)).thenReturn(true);
        when(directoryService.isDirectory(totalPath)).thenReturn(true);
        when(directoryService.listDirectories(totalPath)).thenReturn(List.of(folder));

        service.importMeasurements(resultsRoot, targetDir, context);

        verify(processor).processFolder(eq(folder), argThat(dto -> dto.getPid() == 1234 && dto.getStartTimestamp() == 5678L), eq(context));
    }
}