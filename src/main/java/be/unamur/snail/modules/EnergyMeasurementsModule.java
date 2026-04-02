package be.unamur.snail.modules;

import be.unamur.snail.core.Config;
import be.unamur.snail.stages.CloneAndCheckoutRepositoryStage;
import be.unamur.snail.stages.Stage;
import be.unamur.snail.tool.energy.EnergyMeasurementTool;
import be.unamur.snail.tool.energy.EnergyMeasurementToolFactory;

import java.util.ArrayList;
import java.util.List;

public class EnergyMeasurementsModule extends AbstractModule {
    private final List<Stage> stages;

    public EnergyMeasurementsModule() {
        this(buildStagesFromConfig(new EnergyMeasurementToolFactory(), Config.getInstance()));
    }

    EnergyMeasurementsModule(List<Stage> stages) {
        this.stages = stages;
    }

    @Override
    protected List<Stage> getStages() {
        return stages;
    }

    public static List<Stage> buildStagesFromConfig(EnergyMeasurementToolFactory factory, Config config) {
        String toolName = config.getExecutionPlan().getEnergyMeasurements().getTool();
        int numTestRuns = config.getExecutionPlan().getNumTestRuns();
        EnergyMeasurementTool tool = factory.create(toolName);

        List<Stage> allStages = new ArrayList<>();

        String repoDir = config.getProject().getName() + "_measurements_" + config.getRepo().getCommit();
        allStages.add(new CloneAndCheckoutRepositoryStage(repoDir));
        allStages.addAll(tool.createSetupStages());

        // Measurement stages repeated
        for (int i = 0; i < numTestRuns; i++) {
            allStages.addAll(tool.createMeasurementStages());
        }

        // Post-processing stages to run after the measurements
        allStages.addAll(tool.createPostProcessingStages());

        return allStages;
    }
}
