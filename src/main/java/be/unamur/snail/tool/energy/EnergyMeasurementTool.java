package be.unamur.snail.tool.energy;

import be.unamur.snail.stages.Stage;

import java.util.List;

public interface EnergyMeasurementTool {
    String getName();
    List<Stage> createSetupStages();
    List<Stage> createMeasurementStages();
    List<Stage> createPostProcessingStages();
}
