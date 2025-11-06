package be.unamur.snail.modules;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.stages.CloneAndCheckoutRepositoryStage;
import be.unamur.snail.stages.Stage;
import be.unamur.snail.tool.energy.EnergyMeasurementTool;
import be.unamur.snail.tool.energy.EnergyMeasurementToolFactory;

import java.util.ArrayList;
import java.util.List;

public class EnergyMeasurementsModule implements Module {
    private final List<Stage> stages;

    public EnergyMeasurementsModule() {
        this(buildStagesFromConfig(new EnergyMeasurementToolFactory(), Config.getInstance()));
    }

    private EnergyMeasurementsModule(List<Stage> stages) {
        this.stages = stages;
    }

    public static List<Stage> buildStagesFromConfig(EnergyMeasurementToolFactory factory, Config config) {
        String toolName = config.getExecutionPlan().getEnergyMeasurements().getTool();
        int numTestRuns = config.getExecutionPlan().getNumTestRuns();
        EnergyMeasurementTool tool = factory.create(toolName);

        List<Stage> allStages = new ArrayList<>();
        // Clone and checkout analyzed project
        allStages.add(new CloneAndCheckoutRepositoryStage());
        allStages.addAll(tool.createSetupStages());

        // Measurement stages repeated
        for (int i = 0; i < numTestRuns; i++) {
            allStages.addAll(tool.createMeasurementStages());
        }

        // Post-processing stages to run after the measurements
        allStages.addAll(tool.createPostProcessingStages());

        return allStages;
    }

    @Override
    public void run(Context context) throws Exception {
        PipelineLogger log = context.getLogger();
        for (Stage stage : stages) {
            log.stageStart(stage.getName());
            stage.execute(context);
            log.stageEnd(stage.getName());
        }
    }
}
