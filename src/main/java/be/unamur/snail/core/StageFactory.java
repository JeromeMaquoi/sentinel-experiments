package be.unamur.snail.core;

import be.unamur.snail.config.StageConfig;
import be.unamur.snail.stages.InstrumentConstructorsStage;

public class StageFactory {
    public static Stage create(StageConfig config) {
        return switch (config.getType().toLowerCase()) {
            case "instrumentconstructors" -> new InstrumentConstructorsStage(config.getParams());
            default -> throw new IllegalArgumentException("Unknown stage : " + config.getType());
        };
    }
}
