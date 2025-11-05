package be.unamur.snail.utils.csv;

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
    public static <T> List<T> parseCsvFile(Path csvFile, RunIterationDTO iteration, String commitSha, CsvLineMapper<T> mapper) throws IOException {
        List<T> result = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(csvFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                T dto = mapper.map(line, iteration, commitSha);
                if (dto != null) result.add(dto);
            }
        }
        return result;
    }

    public static List<CallTreeMeasurementDTO> parseCallTreeFile(Path csvPath, Scope scope, MeasurementType measurementType, MonitoringType monitoringType, RunIterationDTO iteration, CommitSimpleDTO commit) throws IOException {

    }

    public static List<MethodMeasurementDTO> parseMethodFile(Path csvPath, Scope scope, MeasurementType measurementType, MonitoringType monitoringType, RunIterationDTO iteration, CommitSimpleDTO commit) throws IOException {

    }
}
