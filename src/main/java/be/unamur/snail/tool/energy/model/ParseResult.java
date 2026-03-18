package be.unamur.snail.tool.energy.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of parsing a CSV file containing both successfully parsed items
 * and errors encountered during parsing.
 *
 * @param <T> The type of successfully parsed items
 */
public class ParseResult<T> {
    private final List<T> parsedItems;
    private final List<ParseError> parseErrors;
    private final int totalLinesRead;

    public ParseResult(List<T> parsedItems, List<ParseError> parseErrors, int totalLinesRead) {
        this.parsedItems = parsedItems != null ? parsedItems : new ArrayList<>();
        this.parseErrors = parseErrors != null ? parseErrors : new ArrayList<>();
        this.totalLinesRead = totalLinesRead;
    }

    public List<T> getParsedItems() {
        return parsedItems;
    }

    public List<ParseError> getParseErrors() {
        return parseErrors;
    }

    public int getTotalLinesRead() {
        return totalLinesRead;
    }

    public int getSuccessfulCount() {
        return parsedItems.size();
    }

    public int getErrorCount() {
        return parseErrors.size();
    }

    public boolean hasErrors() {
        return !parseErrors.isEmpty();
    }

    public float getSuccessRate() {
        if (totalLinesRead == 0) return 100.0f;
        return (float) getSuccessfulCount() / totalLinesRead * 100.0f;
    }

    @Override
    public String toString() {
        return String.format(
                "ParseResult{total=%d, successful=%d, errors=%d, successRate=%.2f%%}",
                totalLinesRead, getSuccessfulCount(), getErrorCount(), getSuccessRate()
        );
    }
}

