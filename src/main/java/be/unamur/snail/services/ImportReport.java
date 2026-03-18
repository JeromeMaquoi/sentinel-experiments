package be.unamur.snail.services;

import be.unamur.snail.tool.energy.model.ImportStatistics;
import be.unamur.snail.tool.energy.model.ParseError;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive report of an import operation containing statistics and error details.
 * Used to track data integrity across the entire import pipeline.
 */
public class ImportReport {
    private final String iterationIdentifier;
    private final Instant startTime;
    private Instant endTime;
    private final List<ImportStatistics> fileStatistics;
    private final List<ParseError> allParseErrors;
    private final List<String> validationErrors;
    private final List<String> backendErrors;
    private int totalItemsSentToBackend;
    private int totalItemsSuccessfullyImported;
    private boolean backendResponseSuccess;

    public ImportReport(String iterationIdentifier) {
        this.iterationIdentifier = iterationIdentifier;
        this.startTime = Instant.now();
        this.fileStatistics = new ArrayList<>();
        this.allParseErrors = new ArrayList<>();
        this.validationErrors = new ArrayList<>();
        this.backendErrors = new ArrayList<>();
        this.totalItemsSentToBackend = 0;
        this.totalItemsSuccessfullyImported = 0;
        this.backendResponseSuccess = false;
    }

    public String getIterationIdentifier() {
        return iterationIdentifier;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void markComplete() {
        this.endTime = Instant.now();
    }

    public long getDurationMillis() {
        if (endTime == null) return -1;
        return endTime.toEpochMilli() - startTime.toEpochMilli();
    }

    public void addFileStatistics(ImportStatistics statistics) {
        fileStatistics.add(statistics);
    }

    public void addParseError(ParseError error) {
        allParseErrors.add(error);
    }

    public void addParseErrors(List<ParseError> errors) {
        allParseErrors.addAll(errors);
    }

    public void addValidationError(String error) {
        validationErrors.add(error);
    }

    public void addValidationErrors(List<String> errors) {
        validationErrors.addAll(errors);
    }

    public void addBackendError(String error) {
        backendErrors.add(error);
    }

    public void setBackendStats(int itemsSent, int itemsImported, boolean success) {
        this.totalItemsSentToBackend = itemsSent;
        this.totalItemsSuccessfullyImported = itemsImported;
        this.backendResponseSuccess = success;
    }

    public List<ImportStatistics> getFileStatistics() {
        return fileStatistics;
    }

    public List<ParseError> getAllParseErrors() {
        return allParseErrors;
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public List<String> getBackendErrors() {
        return backendErrors;
    }

    public int getTotalItemsSentToBackend() {
        return totalItemsSentToBackend;
    }

    public int getTotalItemsSuccessfullyImported() {
        return totalItemsSuccessfullyImported;
    }

    public boolean isBackendResponseSuccess() {
        return backendResponseSuccess;
    }

    public int getTotalFilesProcessed() {
        return fileStatistics.size();
    }

    public int getTotalParsedLines() {
        return fileStatistics.stream()
                .mapToInt(ImportStatistics::getSuccessfullyParsedLines)
                .sum();
    }

    public int getTotalFailedLines() {
        return fileStatistics.stream()
                .mapToInt(ImportStatistics::getFailedLines)
                .sum();
    }

    public float getAverageParseSuccessRate() {
        if (fileStatistics.isEmpty()) return 0.0f;
        return (float) fileStatistics.stream()
                .mapToDouble(ImportStatistics::getSuccessRate)
                .average()
                .orElse(0.0);
    }

    public boolean hasAnyErrors() {
        return !allParseErrors.isEmpty() || !validationErrors.isEmpty() || !backendErrors.isEmpty();
    }

    @Override
    public String toString() {
        return String.format(
                "ImportReport{iteration=%s, files=%d, parsed=%d, failed=%d, " +
                "parseErrors=%d, validationErrors=%d, backendErrors=%d, " +
                "backendSuccess=%s, duration=%dms}",
                iterationIdentifier, getTotalFilesProcessed(), getTotalParsedLines(),
                getTotalFailedLines(), allParseErrors.size(), validationErrors.size(),
                backendErrors.size(), backendResponseSuccess,
                getDurationMillis()
        );
    }
}

