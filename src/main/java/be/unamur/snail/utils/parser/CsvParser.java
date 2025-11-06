package be.unamur.snail.utils.parser;

import be.unamur.snail.tool.energy.*;
import be.unamur.snail.tool.energy.model.CallTreeMeasurementDTO;
import be.unamur.snail.tool.energy.model.CommitSimpleDTO;
import be.unamur.snail.tool.energy.model.RunIterationDTO;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CsvParser {
    public static List<CallTreeMeasurementDTO> parseCallTreeFile(Path csvPath, Scope scope, MeasurementType measurementType, MonitoringType monitoringType, RunIterationDTO iteration, CommitSimpleDTO commit) throws IOException {
        List<CallTreeMeasurementDTO> results = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isBlank()) continue;

                String[] parts = line.split(",");
                if (parts.length != 2) continue;

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

                results.add(dto);
            }
        }
        return results;
    }

//    public static List<MethodMeasurementDTO> parseMethodFile(Path csvPath, Scope scope, MeasurementType measurementType, MonitoringType monitoringType, RunIterationDTO iteration, CommitSimpleDTO commit) throws IOException {
//
//    }
}
