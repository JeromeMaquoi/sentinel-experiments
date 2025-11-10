package be.unamur.snail.tool.energy;

import be.unamur.snail.core.Config;
import be.unamur.snail.tool.energy.model.CommitSimpleDTO;
import be.unamur.snail.tool.energy.model.RepositorySimpleDTO;

public class JoularJXMapper {
    public static Scope mapScope(String folderName) {
        return switch (folderName.toLowerCase()) {
            case "app" -> Scope.APP;
            case "all" -> Scope.ALL;
            default -> throw new IllegalArgumentException("Unknown scope: " + folderName);
        };
    }

    public static MeasurementLevel mapMeasurementLevel(String folderName) {
        return switch (folderName.toLowerCase()) {
            case "runtime" -> MeasurementLevel.RUNTIME;
            case "total" -> MeasurementLevel.TOTAL;
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

    public static CommitSimpleDTO mapCommit() {
        Config config = Config.getInstance();

        CommitSimpleDTO dto = new CommitSimpleDTO();
        dto.setSha(config.getRepo().getCommit());

        RepositorySimpleDTO repository = new RepositorySimpleDTO();
        repository.setOwner(config.getProject().getOwner());
        repository.setName(config.getProject().getName());
        dto.setRepository(repository);
        return dto;
    }
}