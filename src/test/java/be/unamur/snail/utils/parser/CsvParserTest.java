package be.unamur.snail.utils.parser;

import be.unamur.snail.core.Context;
import be.unamur.snail.logging.ConsolePipelineLogger;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.tool.energy.MeasurementType;
import be.unamur.snail.tool.energy.MonitoringType;
import be.unamur.snail.tool.energy.Scope;
import be.unamur.snail.tool.energy.model.CallTreeMeasurementDTO;
import be.unamur.snail.tool.energy.model.CommitSimpleDTO;
import be.unamur.snail.tool.energy.model.MethodMeasurementDTO;
import be.unamur.snail.tool.energy.model.RunIterationDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    void parseCallTreeFileShouldParseValidFileTest() throws IOException {
        Path tempFile = Files.createTempFile("calltree", ".csv");
        Files.writeString(tempFile, "methodA;methodB,1.23\nmethodC;methodD,4.56");

        List<CallTreeMeasurementDTO> dtos = CsvParser.parseCallTreeFile(
                tempFile,
                Scope.APP,
                MeasurementType.RUNTIME,
                MonitoringType.CALLTREES,
                iteration,
                commit,
                context
        );

        assertEquals(2, dtos.size());
        CallTreeMeasurementDTO resultDto1 = dtos.get(0);
        assertEquals(List.of("methodA", "methodB"), resultDto1.getCallstack());
        assertEquals(1.23f, resultDto1.getValue());
        assertEquals(Scope.APP, resultDto1.getScope());
        assertEquals(MeasurementType.RUNTIME, resultDto1.getMeasurementType());
        assertEquals(MonitoringType.CALLTREES, resultDto1.getMonitoringType());
        assertEquals(iteration, resultDto1.getIteration());
        assertEquals(commit, resultDto1.getCommit());
    }

    @Test
    void parseMethodFileShouldParseValidFileTest() throws IOException {
        Path tempFile = Files.createTempFile("method", ".csv");
        Files.writeString(tempFile, "methodX,7.89\nmethodY,0.12");

        List<MethodMeasurementDTO> dtos = CsvParser.parseMethodFile(
                tempFile,
                Scope.APP,
                MeasurementType.RUNTIME,
                MonitoringType.METHODS,
                iteration,
                commit,
                context
        );

        assertEquals(2, dtos.size());
        MethodMeasurementDTO resultDto1 = dtos.get(0);
        assertEquals("methodX", resultDto1.getMethod());
        assertEquals(7.89f, resultDto1.getValue());
        assertEquals(Scope.APP, resultDto1.getScope());
        assertEquals(MeasurementType.RUNTIME, resultDto1.getMeasurementType());
        assertEquals(MonitoringType.METHODS, resultDto1.getMonitoringType());
        assertEquals(iteration, resultDto1.getIteration());
        assertEquals(commit, resultDto1.getCommit());
    }

    @Test
    void parseCsvFileShouldSkipInvalidLinesAndTrimTest() throws IOException {
        Path tempFile = Files.createTempFile("generic", ".csv");
        Files.writeString(tempFile, """
                validLine,1.0
                invalidLine
                ,2.0
                
                anotherValidLine,3.0
                ,
                """);
        Function<String[], String> builder = parts -> parts[0] + ":" + parts[1];
        List<String> results = CsvParser.parseCsvFile(
                tempFile,
                builder,
                context,
                "test"
        );
        assertEquals(List.of("validLine:1.0", "anotherValidLine:3.0"), results);
    }

    @Test
    void buildCallTreeDtoShouldCreateDtoCorrectlyTest() {
        String[] parts = {"methodA;methodB;methodC", "2.34"};
        CallTreeMeasurementDTO dto = CsvParser.buildCallTreeDto(
                parts,
                Scope.APP,
                MeasurementType.RUNTIME,
                MonitoringType.CALLTREES,
                iteration,
                commit
        );
        assertEquals(List.of("methodA", "methodB", "methodC"), dto.getCallstack());
        assertEquals(2.34f, dto.getValue());
        assertEquals(Scope.APP, dto.getScope());
        assertEquals(MeasurementType.RUNTIME, dto.getMeasurementType());
        assertEquals(MonitoringType.CALLTREES, dto.getMonitoringType());
        assertEquals(iteration, dto.getIteration());
        assertEquals(commit, dto.getCommit());
    }

    @Test
    void buildMethodDtoShouldCreateDtoCorrectlyTest() {
        String[] parts = {"methodX", "5.67"};
        MethodMeasurementDTO dto = CsvParser.buildMethodDto(
                parts,
                Scope.APP,
                MeasurementType.RUNTIME,
                MonitoringType.METHODS,
                iteration,
                commit
        );
        assertEquals("methodX", dto.getMethod());
        assertEquals(5.67f, dto.getValue());
        assertEquals(Scope.APP, dto.getScope());
        assertEquals(MeasurementType.RUNTIME, dto.getMeasurementType());
        assertEquals(MonitoringType.METHODS, dto.getMonitoringType());
        assertEquals(iteration, dto.getIteration());
        assertEquals(commit, dto.getCommit());
    }
}