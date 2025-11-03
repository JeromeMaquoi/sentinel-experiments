package be.unamur.snail.logging;

import java.util.Locale;

public enum LogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR;

    public static LogLevel fromString(String levelStr) {
        try {
            return LogLevel.valueOf(levelStr.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return INFO;
        }
    }

    public boolean isEnabledFor(LogLevel level) {
        return level.ordinal() >= this.ordinal();
    }
}
