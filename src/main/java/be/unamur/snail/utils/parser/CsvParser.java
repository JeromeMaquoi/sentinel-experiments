package be.unamur.snail.utils.parser;

import be.unamur.snail.core.Context;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.tool.energy.*;
import be.unamur.snail.tool.energy.model.CallTreeMeasurementDTO;
import be.unamur.snail.tool.energy.model.CommitSimpleDTO;
import be.unamur.snail.tool.energy.model.MethodMeasurementDTO;
import be.unamur.snail.tool.energy.model.RunIterationDTO;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CsvParser {
    private CsvParser() {}

    public static List<CallTreeMeasurementDTO> parseCallTreeFile(Path csvPath, Scope scope, MeasurementType measurementType, MonitoringType monitoringType, RunIterationDTO iteration, CommitSimpleDTO commit, Context context) throws IOException {
        return parseCsvFile(
                csvPath,
                line -> buildCallTreeDto(line, scope, measurementType, monitoringType, iteration, commit),
                context,
                "call tree"
        );
    }

    public static List<MethodMeasurementDTO> parseMethodFile(Path csvPath, Scope scope, MeasurementType measurementType, MonitoringType monitoringType, RunIterationDTO iteration, CommitSimpleDTO commit, Context context) throws IOException {
        return parseCsvFile(
                csvPath,
                line -> buildMethodDto(line, scope, measurementType, monitoringType, iteration, commit),
                context,
                "method"
        );
    }

    public static <T> List<T> parseCsvFile(Path csvPath, Function<String[], T> dtoBuilder, Context context, String label) throws IOException {
        PipelineLogger log = context.getLogger();

        log.debug("Parsing {} file: {}", label, csvPath);
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

    public static CallTreeMeasurementDTO buildCallTreeDto(
            String[] parts,
            Scope scope,
            MeasurementType measurementType,
            MonitoringType monitoringType,
            RunIterationDTO iteration,
            CommitSimpleDTO commit
    ) {
        String callstackStr = parts[0];
        float value = Float.parseFloat(parts[1]);

        CallTreeMeasurementDTO dto = new CallTreeMeasurementDTO();
        dto.setScope(scope);
        dto.setMeasurementType(measurementType);
        dto.setMonitoringType(monitoringType);
        dto.setIteration(iteration);
        dto.setCommit(commit);
        dto.setCallstack(List.of(callstackStr.split(";")));
        dto.setValue(value);

        return dto;
    }

    public static MethodMeasurementDTO buildMethodDto(
            String[] parts,
            Scope scope,
            MeasurementType measurementType,
            MonitoringType monitoringType,
            RunIterationDTO iteration,
            CommitSimpleDTO commit
    ) {
        String method = parts[0];
        float value = Float.parseFloat(parts[1]);

        MethodMeasurementDTO dto = new MethodMeasurementDTO();
        dto.setScope(scope);
        dto.setMeasurementType(measurementType);
        dto.setMonitoringType(monitoringType);
        dto.setIteration(iteration);
        dto.setCommit(commit);
        dto.setMethod(method);
        dto.setValue(value);

        return dto;
    }
}
