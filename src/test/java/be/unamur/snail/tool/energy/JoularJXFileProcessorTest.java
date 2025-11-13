package be.unamur.snail.tool.energy;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.tool.energy.model.BaseMeasurementDTO;
import be.unamur.snail.tool.energy.model.CommitSimpleDTO;
import be.unamur.snail.tool.energy.model.RunIterationDTO;
import be.unamur.snail.tool.energy.model.RuntimeMethodMeasurementDTO;
import be.unamur.snail.tool.energy.serializer.DataSerializer;
import be.unamur.snail.utils.parser.CsvParser;
import be.unamur.snail.utils.parser.JoularJXPathParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JoularJXFileProcessorTest {
    private DataSerializer serializer;
    private SimpleHttpClient httpClient;
    private Config.ImportConfig importConfig;
    private PipelineLogger log;
    private JoularJXFileProcessor fileProcessor;

    private RunIterationDTO iteration;
    private Context context;
    private Path path;
    private Config config;

    @BeforeEach
    void setUp() {
        serializer = mock(DataSerializer.class);
        httpClient = mock(SimpleHttpClient.class);
        importConfig = mock(Config.ImportConfig.class);
        log = mock(PipelineLogger.class);

        fileProcessor = new JoularJXFileProcessor(serializer, httpClient, importConfig, log);

        iteration = mock(RunIterationDTO.class);
        context = mock(Context.class);
        path = mock(Path.class);

        config = new Config();
        var backendConfig = new Config.BackendConfig();
        backendConfig.setServerHostForTests("localhost");
        backendConfig.setServerPortForTests(8080);
        config.setBackendForTests(backendConfig);
        Config.setInstanceForTests(config);
    }

    @Test
    void parsePathShouldReturnPathInfoWhenValidPathTest() {
        JoularJXPathParser.PathInfo info = new JoularJXPathParser.PathInfo("app", "runtime", "calltrees");
        try (MockedStatic<JoularJXPathParser> mocked = mockStatic(JoularJXPathParser.class)) {
            mocked.when(() -> JoularJXPathParser.parse(path)).thenReturn(info);

            JoularJXPathParser.PathInfo result = fileProcessor.parsePath(path);
            assertEquals(info, result);
        }
    }

    @Test
    void parsePathShouldReturnNullWhenInvalidPathTest() {
        try (MockedStatic<JoularJXPathParser> mocked = mockStatic(JoularJXPathParser.class)) {
            mocked.when(() -> JoularJXPathParser.parse(path)).thenThrow(new IllegalArgumentException("Invalid path"));
            JoularJXPathParser.PathInfo result = fileProcessor.parsePath(path);
            assertNull(result);
            verify(log).debug(contains("Skipping file"), eq(path), contains("Invalid path"));
        }
    }

    @Test
    void isAllowedShouldReturnTrueWhenAllAllowedTest() {
        when(importConfig.getScopes()).thenReturn(List.of("app"));
        when(importConfig.getMeasurementTypes()).thenReturn(List.of("runtime"));
        when(importConfig.getMonitoringTypes()).thenReturn(List.of("calltrees"));

        boolean allowed = fileProcessor.isAllowed(Scope.APP, MeasurementLevel.RUNTIME, MonitoringType.CALLTREES);
        assertTrue(allowed);
    }

    @Test
    void isAllowedShouldReturnFalseWhenNotAllowedTest() {
        when(importConfig.getScopes()).thenReturn(List.of("app"));
        when(importConfig.getMeasurementTypes()).thenReturn(List.of("runtime"));
        when(importConfig.getMonitoringTypes()).thenReturn(List.of("calltrees"));

        boolean allowed = fileProcessor.isAllowed(Scope.ALL, MeasurementLevel.RUNTIME, MonitoringType.CALLTREES);
        assertFalse(allowed);
    }

    @Test
    void processShouldImportFileWhenAllowedTest() throws IOException, InterruptedException {
        Path filePath = Path.of("/some/path/joularJX-123456-1627890123456-filtered-app-runtime-calltrees.csv");
        JoularJXPathParser.PathInfo pathInfo = new JoularJXPathParser.PathInfo("app", "runtime", "calltrees");
        BaseMeasurementDTO dto = new RuntimeMethodMeasurementDTO();
        dto.setValue(1.23f);

        when(importConfig.getScopes()).thenReturn(List.of("app"));
        when(importConfig.getMeasurementTypes()).thenReturn(List.of("runtime"));
        when(importConfig.getMonitoringTypes()).thenReturn(List.of("calltrees"));

        when(serializer.serialize(any())).thenReturn("[json]");

        try (MockedStatic<JoularJXPathParser> pathParser = mockStatic(JoularJXPathParser.class);
             MockedStatic<CsvParser> csvParser = mockStatic(CsvParser.class);
             MockedStatic<JoularJXMapper> mapper = mockStatic(JoularJXMapper.class)) {
            pathParser.when(() -> JoularJXPathParser.parse(filePath)).thenReturn(pathInfo);

            mapper.when(() -> JoularJXMapper.mapScope("app")).thenReturn(Scope.APP);
            mapper.when(() -> JoularJXMapper.mapMeasurementLevel("runtime")).thenReturn(MeasurementLevel.RUNTIME);
            mapper.when(() -> JoularJXMapper.mapMonitoringType("calltrees")).thenReturn(MonitoringType.CALLTREES);
            mapper.when(JoularJXMapper::mapCommit).thenReturn(new CommitSimpleDTO());

            csvParser.when(() -> CsvParser.parseCsvFile(filePath, Scope.APP, MeasurementLevel.RUNTIME, MonitoringType.CALLTREES, iteration, new CommitSimpleDTO(), context))
                    .thenReturn(List.of(dto));

            fileProcessor.process(filePath, iteration, context);

            verify(serializer).serialize(any());
            verify(httpClient).post(eq("http://localhost:8080/api/v2/measurements/runtime/calltrees/bulk"), eq("[json]"));
            verify(log).debug(contains("Importing file"), eq(filePath));
        }
    }

    @Test
    void processShouldSkipFileWhenNotAllowedTest() {
        Path filePath = Path.of("/some/path/joularJX-123456-1627890123456-filtered-all-total-methods.csv");
        JoularJXPathParser.PathInfo pathInfo = new JoularJXPathParser.PathInfo("app", "runtime", "calltrees");

        when(importConfig.getScopes()).thenReturn(List.of("all"));
        when(importConfig.getMeasurementTypes()).thenReturn(List.of("total"));
        when(importConfig.getMonitoringTypes()).thenReturn(List.of("methods"));

        try (MockedStatic<JoularJXPathParser> pathParser = mockStatic(JoularJXPathParser.class);
        MockedStatic<JoularJXMapper> mapper = mockStatic(JoularJXMapper.class)) {
            pathParser.when(() -> JoularJXPathParser.parse(filePath)).thenReturn(pathInfo);

            mapper.when(() -> JoularJXMapper.mapScope("app")).thenReturn(Scope.APP);
            mapper.when(() -> JoularJXMapper.mapMeasurementLevel("runtime")).thenReturn(MeasurementLevel.RUNTIME);
            mapper.when(() -> JoularJXMapper.mapMonitoringType("calltrees")).thenReturn(MonitoringType.CALLTREES);

            fileProcessor.process(filePath, iteration, context);

            verify(log).debug(eq("Skipping file {}: scope {}, measurement type {}, monitoring type {} not allowed by config"), eq(filePath), any(), any(), any());
            verifyNoInteractions(serializer);
            verifyNoInteractions(httpClient);
        }
    }

    @Test
    void processShouldSkipWhenPathParsingFailsTest() {
        Path filePath = Path.of("/some/invalid/path/file.csv");

        try (MockedStatic<JoularJXPathParser> pathParser = mockStatic(JoularJXPathParser.class)) {
            pathParser.when(() -> JoularJXPathParser.parse(filePath)).thenThrow(new IllegalArgumentException("Invalid path"));

            fileProcessor.process(filePath, iteration, context);

            verify(log).debug(contains("Skipping file"), eq(filePath), contains("Invalid path"));
            verifyNoInteractions(serializer);
            verifyNoInteractions(httpClient);
        }
    }

    @Test
    void processShouldThrowRuntimeExceptionOnUnexpectedErrorTest() throws IOException {
        Path filePath = Path.of("/some/path/joularJX-123456-1627890123456-filtered-app-runtime-calltrees.csv");
        JoularJXPathParser.PathInfo pathInfo = new JoularJXPathParser.PathInfo("app", "runtime", "calltrees");

        when(importConfig.getScopes()).thenReturn(List.of("app"));
        when(importConfig.getMeasurementTypes()).thenReturn(List.of("runtime"));
        when(importConfig.getMonitoringTypes()).thenReturn(List.of("calltrees"));

        try (MockedStatic<JoularJXPathParser> pathParser = mockStatic(JoularJXPathParser.class);
        MockedStatic<CsvParser> csvParser = mockStatic(CsvParser.class);
        MockedStatic<JoularJXMapper> mapper = mockStatic(JoularJXMapper.class)) {
            pathParser.when(() -> JoularJXPathParser.parse(filePath)).thenReturn(pathInfo);

            mapper.when(() -> JoularJXMapper.mapScope("app")).thenReturn(Scope.APP);
            mapper.when(() -> JoularJXMapper.mapMeasurementLevel("runtime")).thenReturn(MeasurementLevel.RUNTIME);
            mapper.when(() -> JoularJXMapper.mapMonitoringType("calltrees")).thenReturn(MonitoringType.CALLTREES);
            mapper.when(JoularJXMapper::mapCommit).thenReturn(new CommitSimpleDTO());

            csvParser.when(() -> CsvParser.parseCsvFile(any(), any(), any(), any(), any(), any(), any()))
                    .thenThrow(new IOException("File read error"));

            RuntimeException ex = assertThrows(RuntimeException.class, () -> {
                fileProcessor.process(filePath, iteration, context);
            });
            assertInstanceOf(IOException.class, ex.getCause());
            verify(log).error(contains("Error processing file"), eq(filePath), contains("File read error"));
        }
    }
}