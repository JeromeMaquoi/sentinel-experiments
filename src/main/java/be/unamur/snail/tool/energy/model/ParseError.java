package be.unamur.snail.tool.energy.model;

/**
 * Represents an error encountered while parsing a single line from a CSV file.
 * Provides detailed information about what went wrong and where.
 */
public class ParseError {
    private final int lineNumber;
    private final String lineContent;
    private final String errorReason;
    private final Exception exception;

    public ParseError(int lineNumber, String lineContent, String errorReason) {
        this(lineNumber, lineContent, errorReason, null);
    }

    public ParseError(int lineNumber, String lineContent, String errorReason, Exception exception) {
        this.lineNumber = lineNumber;
        this.lineContent = lineContent;
        this.errorReason = errorReason;
        this.exception = exception;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getLineContent() {
        return lineContent;
    }

    public String getErrorReason() {
        return errorReason;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    public String toString() {
        return String.format(
                "ParseError{line=%d, reason='%s', content='%s'}",
                lineNumber, errorReason, lineContent
        );
    }
}

