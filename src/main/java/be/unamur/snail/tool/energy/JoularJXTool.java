package be.unamur.snail.tool.energy;

import be.unamur.snail.stages.RetrieveToolReleaseStage;
import be.unamur.snail.stages.Stage;
import be.unamur.snail.stages.UpdateBuildConfigurationStage;

import java.util.List;

public class JoularJXTool implements EnergyMeasurementTool {
    @Override
    public String getName() {
        return "JoularJX";
    }

    @Override
    public List<Stage> createSetupStages() {
        return List.of(
                new RetrieveToolReleaseStage()//,
                //new UpdateBuildConfigurationStage()
        );
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
