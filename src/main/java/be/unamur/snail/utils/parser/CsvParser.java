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

public class CsvParser {
    private CsvParser() {}

    public static List<? extends BaseMeasurementDTO> parseCsvFile(
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
            return parseCsv(csvPath, line -> buildRuntimeDTO(line, timestamp, scope, measurementLevel, monitoringType, iteration, commit));
        } else {
            return parseCsv(csvPath, line -> buildTotalDTO(line, scope, measurementLevel, monitoringType, iteration, commit));
        }
    }

    public static <T> List<T> parseCsv(Path csvPath, Function<String[], T> dtoBuilder) throws IOException {
        List<T> results = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isBlank()) continue;

                String[] parts = line.split(",");
                if (parts.length != 2) continue;

                if (parts[0].isBlank() || parts[1].isBlank()) continue;

                results.add(dtoBuilder.apply(parts));
            }
        }
        return results;
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
