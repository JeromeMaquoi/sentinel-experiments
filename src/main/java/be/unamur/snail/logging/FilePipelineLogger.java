package be.unamur.snail.logging;

import be.unamur.snail.exceptions.LogFileCreationFailedException;
import be.unamur.snail.exceptions.WriteToLogFileFailedException;
import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FilePipelineLogger implements PipelineLogger {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(FilePipelineLogger.class);
    private final BufferedWriter writer;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public FilePipelineLogger(Path logFilePath) {
        try {
            Files.createDirectories(logFilePath.getParent());
            writer = Files.newBufferedWriter(logFilePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            log.info("Pipeline logs will be written to {}", logFilePath.toAbsolutePath());
        } catch (IOException e) {
            throw new LogFileCreationFailedException();
        }
    }

    @Override
    public void info(String message) {
        log.info(message);
        writeToFile("[INFO] " + message);
    }

    @Override
    public void info(String format, Object... args) {
        String message = String.format(format.replace("{}", "%s"), args);
        info(message);
    }

    @Override
    public void warn(String message) {
        log.warn(message);
        writeToFile("[WARN] " + message);
    }

    @Override
    public void warn(String format, Object... args) {
        String message = String.format(format.replace("{}", "%s"), args);
        warn(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        log.error(message);
        writeToFile("[ERROR] " + message + " - " + throwable.getMessage());
    }

    @Override
    public void error(String format, Throwable throwable, Object... args) {
        String message = String.format(format.replace("{}", "%s"), args);
        error(message, throwable);
    }

    @Override
    public void stageStart(String stageName) {
        info("=== START STAGE: " + stageName + " ===");
    }

    @Override
    public void stageEnd(String stageName) {
        info("=== END STAGE: " + stageName + " ===");
    }

    public void writeToFile(String message) {
        try {
            writer.write(String.format("%s %s%n", LocalDateTime.now().format(formatter), message));
            writer.flush();
        } catch (IOException e) {
            throw new WriteToLogFileFailedException();
        }
    }

    @Override
    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            log.error("Failed to close the log file writer", e);
        }
    }
}
