package be.unamur.snail.logging;

import be.unamur.snail.exceptions.LogFileCreationFailedException;
import be.unamur.snail.exceptions.WriteToLogFileFailedException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;

public class FilePipelineLogger implements PipelineLogger {
    private final BufferedWriter writer;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final boolean alsoLogToConsole;
    private final String loggerName;

    public FilePipelineLogger(Class<?> clazz, Path logFilePath, boolean alsoLogToConsole, boolean clearPreviousLogs) {
        this.alsoLogToConsole = alsoLogToConsole;
        this.loggerName = clazz.getName();
        try {
            Files.createDirectories(logFilePath.getParent());
            if (clearPreviousLogs) {
                writer = Files.newBufferedWriter(logFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } else {
                writer = Files.newBufferedWriter(logFilePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
        } catch (IOException e) {
            throw new LogFileCreationFailedException();
        }
    }

    public void write(String level, String message) {
        String line = String.format("%s [%s] %-5s %s - %s",
                LocalDateTime.now().format(formatter),
                Thread.currentThread().getName(),
                level,
                loggerName,
                message);
        try {
            writer.write(line);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            throw new WriteToLogFileFailedException();
        }
        if (alsoLogToConsole) {
            System.out.println(line);
        }
    }

    @Override
    public void debug(String format, Object... args) {
        write("DEBUG", format(format, args));
    }

    @Override
    public void info(String format, Object... args) {
        write("INFO", format(format, args));
    }

    @Override
    public void warn(String format, Object... args) {
        write("WARN", format(format, args));
    }

    @Override
    public void error(String format, Throwable throwable) {
        write("ERROR", format + " - " + throwable.getMessage());
    }

    @Override
    public void error(String format, Object... args) {
        write("ERROR", format(format, args));
    }

    @Override
    public void stageStart(String stageName) {
        info("=== START STAGE: " + stageName + " ===");
    }

    @Override
    public void stageEnd(String stageName) {
        info("=== END STAGE: " + stageName + " ===");
    }

    @Override
    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            System.err.println("Failed to close the log file writer" + e.getMessage());
        }
    }

    private String format(String message, Object... args) {
        if (args == null || args.length == 0) return message;
        for (Object arg : args) {
            message = message.replaceFirst("\\{\\}", Matcher.quoteReplacement(String.valueOf(arg)));
        }
        return message;
    }
}
