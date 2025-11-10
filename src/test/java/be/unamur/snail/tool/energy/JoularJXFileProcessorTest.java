package be.unamur.snail.tool.energy;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.tool.energy.model.CallTreeMeasurementDTO;
import be.unamur.snail.tool.energy.model.CommitSimpleDTO;
import be.unamur.snail.tool.energy.model.MethodMeasurementDTO;
import be.unamur.snail.tool.energy.model.RunIterationDTO;
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
    private CommitSimpleDTO commit;
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
        commit = mock(CommitSimpleDTO.class);

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
        try (MockedStatic mocked = mockStatic(JoularJXPathParser.class)) {
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
    void processShouldSkipFileWhenNotAllowedTest() {
        JoularJXPathParser.PathInfo info = new JoularJXPathParser.PathInfo("app", "runtime", "calltrees");

        try (MockedStatic<JoularJXPathParser> parserMock = mockStatic(JoularJXPathParser.class);
             MockedStatic<JoularJXMapper> mapperMock = mockStatic(JoularJXMapper.class)) {
            parserMock.when(() -> JoularJXPathParser.parse(path)).thenReturn(info);

            mapperMock.when(() -> JoularJXMapper.mapScope("app")).thenReturn(Scope.APP);
            mapperMock.when(() -> JoularJXMapper.mapMeasurementLevel("runtime")).thenReturn(MeasurementLevel.RUNTIME);
            mapperMock.when(() -> JoularJXMapper.mapMonitoringType("calltrees")).thenReturn(MonitoringType.CALLTREES);

            when(importConfig.getScopes()).thenReturn(List.of("all"));
            when(importConfig.getMeasurementTypes()).thenReturn(List.of("runtime"));
            when(importConfig.getMonitoringTypes()).thenReturn(List.of("calltrees"));

            fileProcessor.process(path, iteration, context);

            verify(log).debug(contains("Skipping file"), eq(path), eq(Scope.APP), eq(MeasurementLevel.RUNTIME), eq(MonitoringType.CALLTREES));
        }
    }

    @Test
    void processShouldLogAndThrowRuntimeExceptionOnErrorTest() {
        try (MockedStatic<JoularJXPathParser> parserMock = mockStatic(JoularJXPathParser.class)) {
            parserMock.when(() -> JoularJXPathParser.parse(path)).thenThrow(new RuntimeException("Parse failed"));
            assertThrows(RuntimeException.class, () -> fileProcessor.process(path, iteration, context));
            verify(log).error(contains("Error processing file"), eq(path), contains("Parse failed"));
        }
    }

    @Test
    void processShouldReturnImmediatelyWhenPathInfoIsNullTest() {
        try (MockedStatic<JoularJXPathParser> parserMock = mockStatic(JoularJXPathParser.class)) {
            parserMock.when(() -> JoularJXPathParser.parse(path)).thenReturn(null);
            fileProcessor.process(path, iteration, context);
            verifyNoInteractions(log);
        }
    }
}