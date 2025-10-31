package be.unamur.snail.logging;

public interface PipelineLogger extends AutoCloseable {
    void info(String message);
    void info(String format, Object... args);
    void warn(String message);
    void warn(String format, Object... args);
    void error(String message, Throwable throwable);
    void error(String format, Throwable throwable, Object... args);
    void stageStart(String stageName);
    void stageEnd(String stageName);
    void close();
}
