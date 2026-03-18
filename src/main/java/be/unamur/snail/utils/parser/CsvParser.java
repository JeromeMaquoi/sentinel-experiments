package be.unamur.snail.utils.parser;

import be.unamur.snail.core.Context;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.tool.energy.*;
import be.unamur.snail.tool.energy.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Parser for CSV files containing energy measurement data.
 * Provides both simple parsing and detailed result tracking with error information.
 */
public class CsvParser {
    private CsvParser() {}

    public static List<BaseMeasurementDTO> parseCsvFile(
            Path csvPath,
            Scope scope,
            MeasurementLevel measurementLevel,
            MonitoringType monitoringType,
            RunIterationDTO iteration,
            CommitSimpleDTO commit,
            Context context
    ) throws IOException {
        ParseResult<BaseMeasurementDTO> result = parseCsvFileWithDetails(
                csvPath, scope, measurementLevel, monitoringType, iteration, commit, context
        );
        
        // Log any errors found during parsing
        logParseErrors(result, context.getLogger());
        
        return result.getParsedItems();
    }

    /**
     * Parse CSV file and return detailed results with full error tracking and statistics.
     * This method provides complete visibility into the parsing process.
     *
     * @return ParseResult containing parsed DTOs, errors, and detailed statistics
     * @throws IOException if file cannot be read
     */
    public static ParseResult<BaseMeasurementDTO> parseCsvFileWithDetails(
            Path csvPath,
            Scope scope,
            MeasurementLevel measurementLevel,
            MonitoringType monitoringType,
            RunIterationDTO iteration,
            CommitSimpleDTO commit,
            Context context
    ) throws IOException {
        PipelineLogger log = context.getLogger();
        log.debug("Parsing {} file: {}", measurementLevel, csvPath);

        if (measurementLevel == MeasurementLevel.RUNTIME) {
            long timestamp = JoularJXPathParser.extractTimestamp(csvPath);
            return parseCsvWithDetails(
                    csvPath,
                    line -> buildRuntimeDTO(line, timestamp, scope, measurementLevel, monitoringType, iteration, commit)
            );
        } else {
            return parseCsvWithDetails(
                    csvPath,
                    line -> buildTotalDTO(line, scope, measurementLevel, monitoringType, iteration, commit)
            );
        }
    }

    /**
     * Log parse errors with appropriate log level and details.
     */
    private static void logParseErrors(ParseResult<?> result, PipelineLogger log) {
        if (result.hasErrors()) {
            log.warn("Found {} parsing errors out of {} lines (success rate: {:.2}%)",
                    result.getErrorCount(), result.getTotalLinesRead(), result.getSuccessRate());
            
            // Log first few errors at WARN level for visibility
            int errorsToLog = Math.min(5, result.getErrorCount());
            for (int i = 0; i < errorsToLog; i++) {
                ParseError error = result.getParseErrors().get(i);
                log.warn("Parse error at line {}: {} | Content: '{}'",
                        error.getLineNumber(), error.getErrorReason(), error.getLineContent());
            }
            
            if (result.getErrorCount() > 5) {
                log.warn("... and {} more parsing errors", result.getErrorCount() - 5);
            }
        }
    }

    public static <T> List<T> parseCsv(Path csvPath, Function<String[], T> dtoBuilder) throws IOException {
        ParseResult<T> result = parseCsvWithDetails(csvPath, dtoBuilder);
        return result.getParsedItems();
    }

    /**
     * Parse CSV file and return detailed results including errors and statistics.
     * This method provides full visibility into parsing errors and line counts.
     *
     * @param csvPath Path to the CSV file
     * @param dtoBuilder Function to build DTO objects from CSV parts
     * @param <T> Type of DTO objects
     * @return ParseResult containing parsed items, errors, and statistics
     * @throws IOException if file cannot be read
     */
    public static <T> ParseResult<T> parseCsvWithDetails(Path csvPath, Function<String[], T> dtoBuilder) throws IOException {
        List<T> results = new ArrayList<>();
        List<ParseError> errors = new ArrayList<>();
        int totalLines = 0;
        int lineNumber = 0;

        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                totalLines++;
                
                String originalLine = line;
                line = line.trim();
                
                // Skip empty lines
                if (line.isBlank()) {
                    continue;
                }

                String[] parts = line.split(",");
                
                // Validate format
                if (parts.length != 2) {
                    errors.add(new ParseError(
                            lineNumber,
                            originalLine,
                            String.format("Expected 2 comma-separated values, found %d", parts.length)
                    ));
                } else if (parts[0].isBlank() || parts[1].isBlank()) {
                    // Validate content
                    errors.add(new ParseError(
                            lineNumber,
                            originalLine,
                            "One or more fields are empty or contain only whitespace"
                    ));
                } else {
                    try {
                        results.add(dtoBuilder.apply(parts));
                    } catch (Exception e) {
                        errors.add(new ParseError(
                                lineNumber,
                                originalLine,
                                String.format("Failed to build DTO: %s", e.getMessage()),
                                e
                        ));
                    }
                }
            }
        }

        return new ParseResult<>(results, errors, totalLines);
    }

    public static BaseMeasurementDTO buildRuntimeDTO(
            String[] parts,
            long timestamp,
            Scope scope,
            MeasurementLevel measurementLevel,
            MonitoringType monitoringType,
            RunIterationDTO iteration,
            CommitSimpleDTO commit
    ) {
        if (monitoringType == MonitoringType.CALLTREES) {
            RuntimeCallTreeMeasurementDTO dto = new RuntimeCallTreeMeasurementDTO();
            dto.setCallstack(List.of(parts[0].split(";")));
            fillCommonFields(dto, timestamp, scope, measurementLevel, monitoringType, iteration, commit, Float.valueOf(parts[1]));
            return dto;
        } else {
            RuntimeMethodMeasurementDTO dto = new RuntimeMethodMeasurementDTO();
            dto.setMethod(parts[0]);
            fillCommonFields(dto, timestamp, scope, measurementLevel, monitoringType, iteration, commit, Float.valueOf(parts[1]));
            return dto;
        }
    }

    public static BaseMeasurementDTO buildTotalDTO(
            String[] parts,
            Scope scope,
            MeasurementLevel measurementLevel,
            MonitoringType monitoringType,
            RunIterationDTO iteration,
            CommitSimpleDTO commit
    ) {
        if (monitoringType == MonitoringType.CALLTREES) {
            TotalCallTreeMeasurementDTO dto = new TotalCallTreeMeasurementDTO();
            dto.setCallstack(List.of(parts[0].split(";")));
            fillCommonFields(dto, null, scope, measurementLevel, monitoringType, iteration, commit, Float.valueOf(parts[1]));
            return dto;
        } else {
            TotalMethodMeasurementDTO dto = new TotalMethodMeasurementDTO();
            dto.setMethod(parts[0]);
            fillCommonFields(dto, null, scope, measurementLevel, monitoringType, iteration, commit, Float.valueOf(parts[1]));
            return dto;
        }
    }

    public static void fillCommonFields(
            BaseMeasurementDTO dto,
            Long timestamp,
            Scope scope,
            MeasurementLevel measurementLevel,
            MonitoringType monitoringType,
            RunIterationDTO iteration,
            CommitSimpleDTO commit,
            Float value
    ) {
        dto.setScope(scope);
        dto.setMeasurementLevel(measurementLevel);
        dto.setMonitoringType(monitoringType);
        dto.setIteration(iteration);
        dto.setCommit(commit);
        dto.setValue(value);
        if (dto instanceof RuntimeMeasurementDTO runtimeDTO && timestamp != null) {
            runtimeDTO.setTimestamp(timestamp);
        }
    }
}
