package be.unamur.snail.services;

import be.unamur.snail.tool.energy.model.ImportStatistics;
import be.unamur.snail.tool.energy.model.ParseError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ImportReport.
 */
class ImportReportTest {
    private ImportReport report;

    @BeforeEach
    void setUp() {
        report = new ImportReport("test-iteration");
    }

    @Test
    void reportShouldTrackStartTimeTest() {
        assertNotNull(report.getStartTime());
    }

    @Test
    void reportShouldTrackEndTimeWhenMarkedCompleteTest() {
        assertNull(report.getEndTime());
        report.markComplete();
        assertNotNull(report.getEndTime());
    }

    @Test
    void reportShouldCalculateDurationTest() {
        assertEquals(-1, report.getDurationMillis(), "Duration should be -1 before completion");
        report.markComplete();
        assertTrue(report.getDurationMillis() >= 0, "Duration should be non-negative after completion");
    }

    @Test
    void reportShouldTrackFileStatisticsTest() {
        ImportStatistics stats = new ImportStatistics(Paths.get("test.csv"), 100, 95, 5, "abc123");
        report.addFileStatistics(stats);

        assertEquals(1, report.getTotalFilesProcessed());
        assertEquals(95, report.getTotalParsedLines());
        assertEquals(5, report.getTotalFailedLines());
    }

    @Test
    void reportShouldTrackParseErrorsTest() {
        ParseError error1 = new ParseError(1, "invalid", "Format error");
        ParseError error2 = new ParseError(2, "invalid", "Value error");
        report.addParseError(error1);
        report.addParseError(error2);

        assertEquals(2, report.getAllParseErrors().size());
    }

    @Test
    void reportShouldTrackValidationErrorsTest() {
        report.addValidationError("Missing field: scope");
        report.addValidationError("Negative value: -10.5");

        assertEquals(2, report.getValidationErrors().size());
    }

    @Test
    void reportShouldTrackBackendErrorsTest() {
        report.addBackendError("HTTP 500: Server error");

        assertEquals(1, report.getBackendErrors().size());
    }

    @Test
    void reportShouldCalculateAverageParseSuccessRateTest() {
        // File 1: 95/100 = 95%
        ImportStatistics stats1 = new ImportStatistics(Paths.get("test1.csv"), 100, 95, 5, "abc");
        // File 2: 90/100 = 90%
        ImportStatistics stats2 = new ImportStatistics(Paths.get("test2.csv"), 100, 90, 10, "def");
        
        report.addFileStatistics(stats1);
        report.addFileStatistics(stats2);

        float averageRate = report.getAverageParseSuccessRate();
        assertEquals(92.5f, averageRate, 0.01f);
    }

    @Test
    void reportShouldDetectErrorsTest() {
        assertFalse(report.hasAnyErrors(), "New report should have no errors");

        report.addParseError(new ParseError(1, "line", "error"));
        assertTrue(report.hasAnyErrors(), "Report with parse error should report hasErrors");

        report = new ImportReport("test-iteration");
        report.addValidationError("error");
        assertTrue(report.hasAnyErrors(), "Report with validation error should report hasErrors");

        report = new ImportReport("test-iteration");
        report.addBackendError("error");
        assertTrue(report.hasAnyErrors(), "Report with backend error should report hasErrors");
    }

    @Test
    void reportShouldSummarizeAllStatisticsTest() {
        // Add multiple files
        ImportStatistics stats1 = new ImportStatistics(Paths.get("test1.csv"), 50, 45, 5, "abc");
        ImportStatistics stats2 = new ImportStatistics(Paths.get("test2.csv"), 100, 90, 10, "def");
        report.addFileStatistics(stats1);
        report.addFileStatistics(stats2);

        // Add errors
        report.addParseError(new ParseError(1, "line", "error"));
        report.addValidationError("Invalid value");
        report.addBackendError("HTTP error");

        // Set backend stats
        report.setBackendStats(135, 130, true);

        assertEquals(2, report.getTotalFilesProcessed());
        assertEquals(135, report.getTotalParsedLines());
        assertEquals(15, report.getTotalFailedLines());
        assertEquals(135, report.getTotalItemsSentToBackend());
        assertEquals(130, report.getTotalItemsSuccessfullyImported());
        assertTrue(report.isBackendResponseSuccess());
    }

    @Test
    void reportToStringShouldBeConciseTest() {
        ImportStatistics stats = new ImportStatistics(Paths.get("test.csv"), 100, 95, 5, "abc123");
        report.addFileStatistics(stats);
        report.addParseError(new ParseError(1, "line", "error"));
        report.markComplete();

        String reportString = report.toString();
        assertNotNull(reportString);
        assertTrue(reportString.contains("ImportReport"));
        assertTrue(reportString.contains("test-iteration"));
    }
}

