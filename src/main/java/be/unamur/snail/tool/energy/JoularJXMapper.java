package be.unamur.snail.tool.energy;

import be.unamur.snail.core.Config;
import be.unamur.snail.tool.energy.model.CallTreeMeasurementDTO;
import be.unamur.snail.tool.energy.model.CommitSimpleDTO;
import be.unamur.snail.tool.energy.model.RepositorySimpleDTO;
import be.unamur.snail.tool.energy.model.RunIterationDTO;

import java.util.List;

public class JoularJXMapper {
    public static Scope mapScope(String folderName) {
        return switch (folderName.toLowerCase()) {
            case "app" -> Scope.APP;
            case "all" -> Scope.ALL;
            default -> throw new IllegalArgumentException("Unknown scope: " + folderName);
        };
    }

    public static MeasurementType mapMeasurementType(String folderName) {
        return switch (folderName.toLowerCase()) {
            case "runtime" -> MeasurementType.RUNTIME;
            case "total" -> MeasurementType.TOTAL;
            default -> throw new IllegalArgumentException("Unknown measurement type: " + folderName);
        };
    }

    public static MonitoringType mapMonitoringType(String folderName) {
        return switch (folderName.toLowerCase()) {
            case "calltrees" -> MonitoringType.CALLTREES;
            case "methods" -> MonitoringType.METHODS;
            default -> throw new IllegalArgumentException("Unknown monitoring type: " + folderName);
        };
    }
}