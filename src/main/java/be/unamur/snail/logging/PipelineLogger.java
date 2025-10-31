package be.unamur.snail.logging;

public interface PipelineLogger extends AutoCloseable {
    void debug(String format, Object... args);
    void info(String format, Object... args);
    void warn(String format, Object... args);
    void error(String format, Throwable throwable);
    void error(String format, Object... args);
    void stageStart(String stageName);
    void stageEnd(String stageName);
    void close();
}
