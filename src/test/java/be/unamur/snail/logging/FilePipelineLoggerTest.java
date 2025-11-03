package be.unamur.snail.logging;

import be.unamur.snail.exceptions.LogFileCreationFailedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FilePipelineLoggerTest {
    private Path tempDir;
    private Path logFile;

    @BeforeEach
    void setUp() throws Exception {
        tempDir = Files.createTempDirectory("logtest");
        logFile = tempDir.resolve("pipeline.log");
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            // Ignore
                        }
                    });
        }
    }

    @Test
    void createLogFileOnConstructionTest() {
        new FilePipelineLogger(getClass(), logFile, false, true, "INFO");
        assertTrue(Files.exists(logFile), "Log file should be created on logger construction");
    }

    @Test
    void clearPreviousLogsTruncatesFileTest() throws IOException {
        Files.writeString(logFile, "Old content");
        assertTrue(Files.size(logFile) > 0);

        new FilePipelineLogger(getClass(), logFile, false, true, "INFO");

        String content = Files.readString(logFile);
        assertEquals("" , content, "Log file should be truncated when clearPreviousLogs is true");
    }

    @Test
    void appendModePreservesOldLogsTest() throws IOException {
        Files.writeString(logFile, "Old content\n");
        assertTrue(Files.size(logFile) > 0);

        try (PipelineLogger logger = new FilePipelineLogger(getClass(), logFile, false, false, "INFO")) {
            logger.info("New log entry");
        }

        String content = Files.readString(logFile);
        assertTrue(content.contains("Old content"), "Old log content should be preserved in append mode");
        assertTrue(content.contains("New log entry"), "New log entry should be appended to the log file");
    }

    @Disabled
    @Test
    void exceptionThrownWhenLogFileCannotBeCreatedTest() {
        Path invalidPath = Path.of("/invalid_path/pipeline.log");
        assertThrows(LogFileCreationFailedException.class, () -> {
            new FilePipelineLogger(getClass(), invalidPath, false, true, "INFO");
        });
    }

    @Test
    void respectsLogLevelThresholdTest() throws IOException {
        try (PipelineLogger logger = new FilePipelineLogger(getClass(), logFile, false, true, "WARN")) {
            logger.debug("This is a debug message");
            logger.info("This is an info message");
            logger.warn("This is a warn message");
            logger.error("This is an error message");
            logger.error("This is an error message", new Exception("Test exception"));
        }

        String content = Files.readString(logFile);
        assertFalse(content.contains("debug message"), "Debug messages should be filtered out");
        assertFalse(content.contains("info message"), "Info messages should be filtered out");
        assertTrue(content.contains("warn message"), "Warn messages should be logged");
        assertTrue(content.contains("error message"), "Error messages should be logged");
    }

    @Test
    void formatMessagesWithPlaceholderTest() throws IOException {
        try (PipelineLogger logger = new FilePipelineLogger(getClass(), logFile, false, true, "INFO")) {
            logger.info("This is an info message: {}", "formatted");
        }

        String content = Files.readString(logFile);
        assertTrue(content.contains("This is an info message"), "Formatted message should be logged correctly");
    }

    @Test
    void stageStartAndEndLoggingTest() throws IOException {
        try (PipelineLogger logger = new FilePipelineLogger(getClass(), logFile, false, true, "INFO")) {
            logger.stageStart("TestStage");
            logger.stageEnd("TestStage");
        }

        String content = Files.readString(logFile);
        assertTrue(content.contains("START STAGE: TestStage"), "Stage start should be logged");
        assertTrue(content.contains("END STAGE: TestStage"), "Stage end should be logged");
    }

    @Test
    void consoleOutputWhenEnabledTest() {
        ByteArrayOutputStream consoleOutput = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(consoleOutput));

        try (PipelineLogger logger = new FilePipelineLogger(getClass(), logFile, true, true, "INFO")) {
            logger.info("This is an info message");
        } finally {
            System.setOut(originalOut);
        }

        String output = consoleOutput.toString();
        assertTrue(output.contains("This is an info message"), "Log message should be printed to console");
    }

    @Test
    void closeDoesNotThrowExceptionTest() throws IOException {
        FilePipelineLogger logger = new FilePipelineLogger(getClass(), logFile, false, true, "INFO");
        assertDoesNotThrow(logger::close, "Closing the logger should not throw an exception");
    }

    @Test
    void errorLoggingWithThrowableTest() throws IOException {
        try (PipelineLogger logger = new FilePipelineLogger(getClass(), logFile, false, true, "INFO")) {
            Exception ex = new Exception("Test exception");
            logger.error("An error occurred", ex);
        }

        String content = Files.readString(logFile);
        assertTrue(content.contains("An error occurred - Test exception"), "Error message with throwable should be logged correctly");
    }
}