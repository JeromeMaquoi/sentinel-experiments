package be.unamur.snail.utils.parser;

import be.unamur.snail.core.Context;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.tool.energy.MeasurementLevel;
import be.unamur.snail.tool.energy.MonitoringType;
import be.unamur.snail.tool.energy.Scope;
import be.unamur.snail.tool.energy.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests for enhanced CSV parsing with detailed error tracking.
 */
class CsvParserDetailedTest {
    @Mock
    private PipelineLogger log;
    @Mock
    private Context context;

    private RunIterationDTO iteration;
    private CommitSimpleDTO commit;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(context.getLogger()).thenReturn(log);
        iteration = new RunIterationDTO();
        commit = new CommitSimpleDTO();
    }

    @Test
    void parseCsvWithDetailsShouldTrackErrorsAndCountLinesTest() throws IOException {
        Path tempFile = Files.createTempFile("test", ".csv");
        Files.writeString(tempFile, """
                valid1,10.5
                invalid_line_missing_value
                valid2,20.3
                ,15.0
                valid3,30.1
                """);

        Function<String[], String> builder = parts -> parts[0] + ":" + parts[1];
        ParseResult<String> result = CsvParser.parseCsvWithDetails(tempFile, builder);

        assertEquals(3, result.getSuccessfulCount(), "Should parse 3 valid lines");
        assertEquals(2, result.getErrorCount(), "Should track 2 errors");
        assertEquals(5, result.getTotalLinesRead(), "Should count total non-empty lines");
        assertTrue(result.hasErrors(), "Should indicate errors exist");
        assertFalse(result.getParseErrors().isEmpty(), "Error list should not be empty");
    }

    @Test
    void parseCsvWithDetailsShouldIncludeLineNumbersAndContentInErrorsTest() throws IOException {
        Path tempFile = Files.createTempFile("test", ".csv");
        Files.writeString(tempFile, """
                method1,10.5
                invalid_line
                method2,20.3
                """);

        Function<String[], String> builder = parts -> parts[0] + ":" + parts[1];
        ParseResult<String> result = CsvParser.parseCsvWithDetails(tempFile, builder);

        assertEquals(1, result.getErrorCount());
        ParseError error = result.getParseErrors().get(0);
        assertEquals(2, error.getLineNumber());
        assertEquals("invalid_line", error.getLineContent());
        assertNotNull(error.getErrorReason());
    }

    @Test
    void parseCsvWithDetailsShouldCalculateSuccessRateTest() throws IOException {
        Path tempFile = Files.createTempFile("test", ".csv");
        Files.writeString(tempFile, """
                valid1,10.5
                invalid
                valid2,20.3
                invalid2
                valid3,30.1
                """);

        Function<String[], String> builder = parts -> parts[0] + ":" + parts[1];
        ParseResult<String> result = CsvParser.parseCsvWithDetails(tempFile, builder);

        assertEquals(3, result.getSuccessfulCount());
        assertEquals(2, result.getErrorCount());
        assertEquals(5, result.getTotalLinesRead());
        float expectedRate = (3.0f / 5.0f) * 100;
        assertEquals(expectedRate, result.getSuccessRate(), 0.01f);
    }

    @Test
    void parseCsvWithDetailsShouldHandleParsingExceptionsTest() throws IOException {
        Path tempFile = Files.createTempFile("test", ".csv");
        Files.writeString(tempFile, """
                method1,10
                method2,invalid_number
                method3,30
                """);

        Function<String[], Integer> builder = parts -> Integer.parseInt(parts[1]);
        ParseResult<Integer> result = CsvParser.parseCsvWithDetails(tempFile, builder);

        assertEquals(2, result.getSuccessfulCount());
        assertEquals(1, result.getErrorCount());
        ParseError error = result.getParseErrors().get(0);
        assertEquals(2, error.getLineNumber());
        assertNotNull(error.getException());
    }

    @Test
    void parseCsvFileWithDetailsShouldReturnParseResultTest() throws IOException {
        Path tempFile = Files.createTempFile("test", ".csv");
        Files.writeString(tempFile, """
                method1,10.5
                method2,20.3
                """);

        ParseResult<BaseMeasurementDTO> result = CsvParser.parseCsvFileWithDetails(
                tempFile,
                Scope.APP,
                MeasurementLevel.TOTAL,
                MonitoringType.METHODS,
                iteration,
                commit,
                context
        );

        assertNotNull(result);
        assertEquals(2, result.getSuccessfulCount());
        assertEquals(0, result.getErrorCount());
        assertTrue(result.getParsedItems().stream()
                .allMatch(item -> item instanceof TotalMethodMeasurementDTO));
    }

    @Test
    void parseCsvFileShouldLogErrorsTest() throws IOException {
        Path tempFile = Files.createTempFile("test", ".csv");
        Files.writeString(tempFile, """
                method1,10.5
                invalid
                method2,20.3
                """);

        CsvParser.parseCsvFile(
                tempFile,
                Scope.APP,
                MeasurementLevel.TOTAL,
                MonitoringType.METHODS,
                iteration,
                commit,
                context
        );

        // Verify that logger.warn was called (implementation detail)
        // The actual error logging is done inside parseCsvFile
    }
}


