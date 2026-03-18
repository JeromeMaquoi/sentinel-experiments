package be.unamur.snail.tool.energy.model;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Tracks statistics for CSV file imports including line counts and checksums.
 */
public class ImportStatistics {
    private final Path filePath;
    private final int totalLinesInFile;
    private final int successfullyParsedLines;
    private final int failedLines;
    private final String fileChecksum;
    private final Map<String, Object> additionalMetrics;

    public ImportStatistics(Path filePath, int totalLinesInFile, int successfullyParsedLines,
                           int failedLines, String fileChecksum) {
        this.filePath = filePath;
        this.totalLinesInFile = totalLinesInFile;
        this.successfullyParsedLines = successfullyParsedLines;
        this.failedLines = failedLines;
        this.fileChecksum = fileChecksum;
        this.additionalMetrics = new HashMap<>();
    }

    public Path getFilePath() {
        return filePath;
    }

    public int getTotalLinesInFile() {
        return totalLinesInFile;
    }

    public int getSuccessfullyParsedLines() {
        return successfullyParsedLines;
    }

    public int getFailedLines() {
        return failedLines;
    }

    public String getFileChecksum() {
        return fileChecksum;
    }

    public float getSuccessRate() {
        if (totalLinesInFile == 0) return 100.0f;
        return (float) successfullyParsedLines / totalLinesInFile * 100.0f;
    }

    public void addMetric(String key, Object value) {
        additionalMetrics.put(key, value);
    }

    public Object getMetric(String key) {
        return additionalMetrics.get(key);
    }

    @Override
    public String toString() {
        return String.format(
                "ImportStatistics{file=%s, total=%d, successful=%d, failed=%d, successRate=%.2f%%, checksum=%s}",
                filePath.getFileName(), totalLinesInFile, successfullyParsedLines,
                failedLines, getSuccessRate(), fileChecksum != null ? fileChecksum.substring(0, 8) + "..." : "N/A"
        );
    }
}

