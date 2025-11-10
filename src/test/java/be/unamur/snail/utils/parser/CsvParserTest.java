package be.unamur.snail.utils.parser;

import be.unamur.snail.core.Context;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.tool.energy.MeasurementLevel;
import be.unamur.snail.tool.energy.MonitoringType;
import be.unamur.snail.tool.energy.Scope;
import be.unamur.snail.tool.energy.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CsvParserTest {
    @Mock
    private PipelineLogger log;
    @Mock
    private Context context;

    private RunIterationDTO iteration;
    private CommitSimpleDTO commit;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        when(context.getLogger()).thenReturn(log);
        iteration = new RunIterationDTO();
        commit = new CommitSimpleDTO();
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void parseCsvFileShouldParseRuntimeCallTreeFileTest() throws IOException {
        Path tempFile = Files.createTempFile("calltree", ".csv");
        Files.writeString(tempFile, "methodA;methodB,1.23\nmethodC;methodD,4.56");

        try (MockedStatic<JoularJXPathParser> mocked = mockStatic(JoularJXPathParser.class)) {
            mocked.when(() -> JoularJXPathParser.extractTimestamp(eq(tempFile))).thenReturn(1625078400000L);

            List<? extends BaseMeasurementDTO> dtos = CsvParser.parseCsvFile(
                    tempFile,
                    Scope.APP,
                    MeasurementLevel.RUNTIME,
                    MonitoringType.CALLTREES,
                    iteration,
                    commit,
                    context
            );

            assertEquals(2, dtos.size());
            RuntimeCallTreeMeasurementDTO resultDto1 = (RuntimeCallTreeMeasurementDTO) dtos.get(0);
            assertEquals(List.of("methodA", "methodB"), resultDto1.getCallstack());
            assertEquals(1.23f, resultDto1.getValue());
            assertEquals(Scope.APP, resultDto1.getScope());
            assertEquals(MeasurementLevel.RUNTIME, resultDto1.getMeasurementLevel());
            assertEquals(MonitoringType.CALLTREES, resultDto1.getMonitoringType());
            assertEquals(iteration, resultDto1.getIteration());
            assertEquals(commit, resultDto1.getCommit());
        }
    }

    @Test
    void parseCsvFileShouldParseRuntimeMethodFileTest() throws IOException {
        Path tempFile = Files.createTempFile("methods", ".csv");
        Files.writeString(tempFile, "methodX,2.34\nmethodY,5.67");

        try (MockedStatic<JoularJXPathParser> mocked = mockStatic(JoularJXPathParser.class)) {
            mocked.when(() -> JoularJXPathParser.extractTimestamp(eq(tempFile))).thenReturn(1625078400000L);

            List<? extends BaseMeasurementDTO> dtos = CsvParser.parseCsvFile(
                    tempFile,
                    Scope.APP,
                    MeasurementLevel.RUNTIME,
                    MonitoringType.METHODS,
                    iteration,
                    commit,
                    context
            );

            assertEquals(2, dtos.size());
            RuntimeMethodMeasurementDTO resultDto1 = (RuntimeMethodMeasurementDTO) dtos.get(0);
            assertEquals("methodX", resultDto1.getMethod());
            assertEquals(2.34f, resultDto1.getValue());
            assertEquals(Scope.APP, resultDto1.getScope());
            assertEquals(MeasurementLevel.RUNTIME, resultDto1.getMeasurementLevel());
            assertEquals(MonitoringType.METHODS, resultDto1.getMonitoringType());
            assertEquals(iteration, resultDto1.getIteration());
            assertEquals(commit, resultDto1.getCommit());
        }
    }

    @Test
    void parseCsvFileShouldParseTotalCallTreeFileTest() throws IOException {
        Path tempFile = Files.createTempFile("calltree_total", ".csv");
        Files.writeString(tempFile, "methodE;methodF,3.21\nmethodG;methodH,6.54");
        List<? extends BaseMeasurementDTO> dtos = CsvParser.parseCsvFile(
                tempFile,
                Scope.APP,
                MeasurementLevel.TOTAL,
                MonitoringType.CALLTREES,
                iteration,
                commit,
                context
        );

        assertEquals(2, dtos.size());
        TotalCallTreeMeasurementDTO resultDto1 = (TotalCallTreeMeasurementDTO) dtos.get(0);
        assertEquals(List.of("methodE", "methodF"), resultDto1.getCallstack());
        assertEquals(3.21f, resultDto1.getValue());
        assertEquals(Scope.APP, resultDto1.getScope());
        assertEquals(MeasurementLevel.TOTAL, resultDto1.getMeasurementLevel());
        assertEquals(MonitoringType.CALLTREES, resultDto1.getMonitoringType());
        assertEquals(iteration, resultDto1.getIteration());
        assertEquals(commit, resultDto1.getCommit());
    }

    @Test
    void parseCsvFileShouldParseTotalMethodFileTest() throws IOException {
        Path tempFile = Files.createTempFile("methods_total", ".csv");
        Files.writeString(tempFile, "methodZ,7.89\nmethodW,0.12");
        List<? extends BaseMeasurementDTO> dtos = CsvParser.parseCsvFile(
                tempFile,
                Scope.APP,
                MeasurementLevel.TOTAL,
                MonitoringType.METHODS,
                iteration,
                commit,
                context
        );
        assertEquals(2, dtos.size());
        TotalMethodMeasurementDTO resultDto1 = (TotalMethodMeasurementDTO) dtos.get(0);
        assertEquals("methodZ", resultDto1.getMethod());
        assertEquals(7.89f, resultDto1.getValue());
        assertEquals(Scope.APP, resultDto1.getScope());
        assertEquals(MeasurementLevel.TOTAL, resultDto1.getMeasurementLevel());
        assertEquals(MonitoringType.METHODS, resultDto1.getMonitoringType());
        assertEquals(iteration, resultDto1.getIteration());
        assertEquals(commit, resultDto1.getCommit());
    }

    @Test
    void parseCsvShouldSkipInvalidLinesAndTrimTest() throws IOException {
        Path tempFile = Files.createTempFile("invalid_lines", ".csv");
        Files.writeString(tempFile, """
                method1,10.4
                invalidLine
                invalid,
                ,3.5
                
                method2,20.8
                ,
                """);
        Function<String[], String> builder = parts -> parts[0] + ":" + parts[1];
        List<String> results = CsvParser.parseCsv(tempFile, builder);
        assertEquals(List.of("method1:10.4", "method2:20.8"), results);
    }

    @Test
    void buildRuntimeDTOShouldCreateRuntimeMethodDTOPropertlyTest() {
        String[] partsMethod = {"methodTest", "12.34"};
        BaseMeasurementDTO dto = CsvParser.buildRuntimeDTO(
                partsMethod,
                162L,
                Scope.APP,
                MeasurementLevel.RUNTIME,
                MonitoringType.METHODS,
                iteration,
                commit
        );
        assertInstanceOf(RuntimeMethodMeasurementDTO.class, dto);
        assertEquals("methodTest", ((RuntimeMethodMeasurementDTO) dto).getMethod());
        assertEquals(12.34f, dto.getValue());
        assertEquals(162L, ((RuntimeMethodMeasurementDTO) dto).getTimestamp());
    }

    @Test
    void buildRuntimeDTOShouldCreateRuntimeCallTreeDTOPropertlyTest() {
        String[] partsCallTree = {"funcA;funcB;funcC", "23.45"};
        BaseMeasurementDTO dto = CsvParser.buildRuntimeDTO(
                partsCallTree,
                163L,
                Scope.APP,
                MeasurementLevel.RUNTIME,
                MonitoringType.CALLTREES,
                iteration,
                commit
        );
        assertInstanceOf(RuntimeCallTreeMeasurementDTO.class, dto);
        assertEquals(List.of("funcA", "funcB", "funcC"), ((RuntimeCallTreeMeasurementDTO) dto).getCallstack());
        assertEquals(23.45f, dto.getValue());
        assertEquals(163L, ((RuntimeCallTreeMeasurementDTO) dto).getTimestamp());
    }

    @Test
    void buildTotalDTOShouldCreateTotalMethodDTOPropertlyTest() {
        String[] partsMethod = {"methodTotal", "34.56"};
        BaseMeasurementDTO dto = CsvParser.buildTotalDTO(
                partsMethod,
                Scope.APP,
                MeasurementLevel.TOTAL,
                MonitoringType.METHODS,
                iteration,
                commit
        );
        assertInstanceOf(TotalMethodMeasurementDTO.class, dto);
        assertEquals("methodTotal", ((TotalMethodMeasurementDTO) dto).getMethod());
        assertEquals(34.56f, dto.getValue());
    }

    @Test
    void buildTotalDTOShouldCreateTotalCallTreeDTOPropertlyTest() {
        String[] partsCallTree = {"funcX;funcY;funcZ", "45.67"};
        BaseMeasurementDTO dto = CsvParser.buildTotalDTO(
                partsCallTree,
                Scope.APP,
                MeasurementLevel.TOTAL,
                MonitoringType.CALLTREES,
                iteration,
                commit
        );
        assertInstanceOf(TotalCallTreeMeasurementDTO.class, dto);
        assertEquals(List.of("funcX", "funcY", "funcZ"), ((TotalCallTreeMeasurementDTO) dto).getCallstack());
        assertEquals(45.67f, dto.getValue());
    }
}