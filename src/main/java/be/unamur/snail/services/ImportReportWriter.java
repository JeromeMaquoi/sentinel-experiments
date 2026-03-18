package be.unamur.snail.services;

import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.tool.energy.model.ImportStatistics;
import be.unamur.snail.tool.energy.model.ParseError;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

/**
 * Writes import reports to files for audit and debugging purposes.
 * Creates detailed error reports with line-by-line error information.
 */
public class ImportReportWriter {
    private final PipelineLogger log;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_INSTANT;

    public ImportReportWriter(PipelineLogger log) {
        this.log = log;
    }

    /**
     * Write a complete import report to a file.
     *
     * @param report The import report to write
     * @param reportPath Path where the report should be written
     * @throws IOException if writing fails
     */
    public void writeReport(ImportReport report, Path reportPath) throws IOException {
        StringBuilder sb = new StringBuilder();

        // Header
        sb.append("=".repeat(80)).append("\n");
        sb.append("IMPORT REPORT: ").append(report.getIterationIdentifier()).append("\n");
        sb.append("=".repeat(80)).append("\n\n");

        // Timing
        sb.append("TIMING\n");
        sb.append("------\n");
        sb.append("Start Time: ").append(timeFormatter.format(report.getStartTime())).append("\n");
        if (report.getEndTime() != null) {
            sb.append("End Time: ").append(timeFormatter.format(report.getEndTime())).append("\n");
            sb.append("Duration: ").append(report.getDurationMillis()).append(" ms\n");
        }
        sb.append("\n");

        // Summary Statistics
        sb.append("SUMMARY STATISTICS\n");
        sb.append("------------------\n");
        sb.append("Files Processed: ").append(report.getTotalFilesProcessed()).append("\n");
        sb.append("Total Lines Parsed: ").append(report.getTotalParsedLines()).append("\n");
        sb.append("Total Lines Failed: ").append(report.getTotalFailedLines()).append("\n");
        sb.append("Average Parse Success Rate: ").append(String.format("%.2f%%\n", report.getAverageParseSuccessRate()));
        sb.append("Items Sent to Backend: ").append(report.getTotalItemsSentToBackend()).append("\n");
        sb.append("Items Successfully Imported: ").append(report.getTotalItemsSuccessfullyImported()).append("\n");
        sb.append("Backend Response: ").append(report.isBackendResponseSuccess() ? "SUCCESS" : "FAILURE").append("\n");
        sb.append("\n");

        // File-level Statistics
        if (!report.getFileStatistics().isEmpty()) {
            sb.append("FILE STATISTICS\n");
            sb.append("---------------\n");
            for (ImportStatistics stats : report.getFileStatistics()) {
                sb.append(String.format("File: %s\n", stats.getFilePath().getFileName()));
                sb.append(String.format("  Total Lines: %d\n", stats.getTotalLinesInFile()));
                sb.append(String.format("  Successfully Parsed: %d\n", stats.getSuccessfullyParsedLines()));
                sb.append(String.format("  Failed: %d\n", stats.getFailedLines()));
                sb.append(String.format("  Success Rate: %.2f%%\n", stats.getSuccessRate()));
                if (stats.getFileChecksum() != null) {
                    sb.append(String.format("  Checksum: %s\n", stats.getFileChecksum()));
                }
                sb.append("\n");
            }
        }

        // Errors
        if (report.hasAnyErrors()) {
            sb.append("ERRORS AND WARNINGS\n");
            sb.append("-------------------\n");

            if (!report.getAllParseErrors().isEmpty()) {
                sb.append("\nPARSE ERRORS (").append(report.getAllParseErrors().size()).append(")\n");
                sb.append(String.format("%s\n", "-".repeat(50)));
                for (ParseError error : report.getAllParseErrors()) {
                    sb.append(String.format("Line %d: %s\n", error.getLineNumber(), error.getErrorReason()));
                    sb.append(String.format("  Content: %s\n", error.getLineContent()));
                    if (error.getException() != null) {
                        sb.append(String.format("  Exception: %s\n", error.getException().getMessage()));
                    }
                }
            }

            if (!report.getValidationErrors().isEmpty()) {
                sb.append("\nVALIDATION ERRORS (").append(report.getValidationErrors().size()).append(")\n");
                sb.append(String.format("%s\n", "-".repeat(50)));
                for (String error : report.getValidationErrors()) {
                    sb.append("  - ").append(error).append("\n");
                }
            }

            if (!report.getBackendErrors().isEmpty()) {
                sb.append("\nBACKEND ERRORS (").append(report.getBackendErrors().size()).append(")\n");
                sb.append(String.format("%s\n", "-".repeat(50)));
                for (String error : report.getBackendErrors()) {
                    sb.append("  - ").append(error).append("\n");
                }
            }
        } else {
            sb.append("RESULT: NO ERRORS FOUND\n");
        }

        sb.append("\n").append("=".repeat(80)).append("\n");

        // Write to file
        Files.createDirectories(reportPath.getParent());
        Files.writeString(reportPath, sb.toString());
        log.info("Import report written to: {}", reportPath);
    }

    /**
     * Write detailed error information to a separate error log file.
     *
     * @param report The import report
     * @param errorLogPath Path where the error log should be written
     * @throws IOException if writing fails
     */
    public void writeErrorLog(ImportReport report, Path errorLogPath) throws IOException {
        if (!report.hasAnyErrors()) {
            return; // Don't create error log if there are no errors
        }

        StringBuilder sb = new StringBuilder();
        sb.append("ERROR LOG\n");
        sb.append("=".repeat(80)).append("\n");
        sb.append("Iteration: ").append(report.getIterationIdentifier()).append("\n");
        sb.append("Generated: ").append(timeFormatter.format(report.getEndTime())).append("\n");
        sb.append("=".repeat(80)).append("\n\n");

        // Parse Errors in CSV format for easy analysis
        if (!report.getAllParseErrors().isEmpty()) {
            sb.append("PARSE ERRORS (CSV FORMAT)\n");
            sb.append("LineNumber,ErrorReason,Content\n");
            for (ParseError error : report.getAllParseErrors()) {
                sb.append(String.format("%d,\"%s\",\"%s\"\n",
                        error.getLineNumber(),
                        error.getErrorReason().replace("\"", "\"\""),
                        error.getLineContent().replace("\"", "\"\"")));
            }
        }

        Files.createDirectories(errorLogPath.getParent());
        Files.writeString(errorLogPath, sb.toString());
        log.info("Error log written to: {}", errorLogPath);
    }
}



