package be.unamur.snail.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsolePipelineLogger implements PipelineLogger{
    private final Logger delegate;

    public ConsolePipelineLogger(Class<?> clazz) {
        this.delegate = LoggerFactory.getLogger(clazz);
    }

    @Override
    public void debug(String format, Object... args) {
        delegate.debug(format, args);
    }

    @Override
    public void info(String format, Object... args) {
        delegate.info(format, args);
    }

    @Override
    public void warn(String format, Object... args) {
        delegate.warn(format, args);
    }

    @Override
    public void error(String format, Throwable throwable) {
        delegate.error(format, throwable);
    }

    @Override
    public void error(String format, Object... args) {
        delegate.error(format, args);
    }

    @Override
    public void stageStart(String stageName) {
        delegate.info("=== START STAGE: {} ===", stageName);
    }

    @Override
    public void stageEnd(String stageName) {
        delegate.info("=== END STAGE: {} ===", stageName);
    }

    @Override
    public void close() {

    }
}
