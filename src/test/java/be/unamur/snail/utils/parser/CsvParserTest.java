package be.unamur.snail.utils.parser;

import be.unamur.snail.core.Context;
import be.unamur.snail.logging.ConsolePipelineLogger;
import be.unamur.snail.tool.energy.MeasurementType;
import be.unamur.snail.tool.energy.MonitoringType;
import be.unamur.snail.tool.energy.Scope;
import be.unamur.snail.tool.energy.model.CallTreeMeasurementDTO;
import be.unamur.snail.tool.energy.model.CommitSimpleDTO;
import be.unamur.snail.tool.energy.model.RunIterationDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvParserTest {
    private Path tempFile;
    private RunIterationDTO iteration;
    private CommitSimpleDTO commit;
    private Context context;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("test", ".csv");
        iteration = new RunIterationDTO();
        iteration.setPid(1234);
        iteration.setStartTimestamp(56789L);
        commit = new CommitSimpleDTO();
        commit.setSha("abcdef123456");
        context = new Context();
        context.setLogger(new ConsolePipelineLogger(CsvParser.class));
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @Test
    void parseCallTreeFileShouldParseValidCsvTest() throws IOException {
        String csvContent = "methodA;methodB;methodC,12.34\n" +
                            "methodX;methodY,56.78\n";
        Files.writeString(tempFile, csvContent);
        List<CallTreeMeasurementDTO> result = CsvParser.parseCallTreeFile(
                tempFile,
                Scope.APP,
                MeasurementType.TOTAL,
                MonitoringType.CALLTREES,
                iteration,
                commit,
                context
        );

        assertEquals(2, result.size());

        CallTreeMeasurementDTO first = result.get(0);
        assertEquals(Scope.APP, first.getScope());
        assertEquals(MeasurementType.TOTAL, first.getMeasurementType());
        assertEquals(MonitoringType.CALLTREES, first.getMonitoringType());
        assertEquals(iteration, first.getIteration());
        assertEquals(commit, first.getCommit());
        assertEquals(List.of("methodA", "methodB", "methodC"), first.getCallstack());
        assertEquals(12.34f, first.getValue());
    }

    @Test
    void parseCallTreeFileShouldSkipInvalidLinesTest() throws IOException {
        String csvContent = "methodA;methodB,10.0\n" +
                "invalid_line_without_comma\n" +
                "too,many,commas,here\n" +
                "\n" +
                "methodC,20.0\n";
        Files.writeString(tempFile, csvContent, StandardOpenOption.TRUNCATE_EXISTING);

        List<CallTreeMeasurementDTO> result = CsvParser.parseCallTreeFile(
                tempFile,
                Scope.ALL,
                MeasurementType.RUNTIME,
                MonitoringType.CALLTREES,
                iteration,
                commit,
                context
        );
        System.out.println(result);

        assertEquals(2, result.size());
        assertEquals(10.0f, result.get(0).getValue());
        assertEquals(20.0f, result.get(1).getValue());
    }

    @Test
    void parseCallTreeFileShouldThrowNumberFormatExceptionForInvalidFloatTest() throws IOException {
        Files.writeString(tempFile, "methodA;methodB,not_a_number\n");

        assertThrows(NumberFormatException.class, () -> CsvParser.parseCallTreeFile(
                tempFile,
                Scope.APP,
                MeasurementType.RUNTIME,
                MonitoringType.CALLTREES,
                iteration,
                commit,
                context
        ));
    }
}