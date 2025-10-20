package be.unamur.snail.tool.energy;

import be.unamur.snail.stages.Stage;

import java.util.List;

public class JoularJXTool implements EnergyMeasurementTool {
    @Override
    public String getName() {
        return "JoularJX";
    }

    @Override
    public List<Stage> createSetupStages() {
        return List.of();
    }

    @Override
    public List<Stage> createMeasurementStages() {
        return List.of();
    }

    @Override
    public List<Stage> createPostProcessingStages() {
        return List.of();
    }
}
