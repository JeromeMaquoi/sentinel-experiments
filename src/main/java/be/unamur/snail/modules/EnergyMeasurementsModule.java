package be.unamur.snail.modules;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.stages.CloneAndCheckoutRepositoryStage;
import be.unamur.snail.stages.Stage;
import be.unamur.snail.tool.energy.EnergyMeasurementTool;
import be.unamur.snail.tool.energy.EnergyMeasurementToolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class EnergyMeasurementsModule implements Module {
    private final static Logger log = LoggerFactory.getLogger(EnergyMeasurementsModule.class);
    private final List<Stage> stages;

    public EnergyMeasurementsModule() {
        EnergyMeasurementToolFactory factory = new EnergyMeasurementToolFactory();

        Config config = Config.getInstance();
        String toolName = config.getExecutionPlan().getMeasurementTool();
        int numTestRuns = config.getExecutionPlan().getNumTestRuns();
        EnergyMeasurementTool tool = factory.create(toolName);

        List<Stage> allStages = new ArrayList<>();

        // Clone and checkoud analyzed project
        allStages.add(new CloneAndCheckoutRepositoryStage());
        allStages.addAll(tool.createSetupStages());

        // Measurement stages repeated
        for (int i = 0; i < numTestRuns; i++) {
            allStages.addAll(tool.createMeasurementStages());
        }

        // Post-processing stages to run after the measurements
        allStages.addAll(tool.createPostProcessingStages());

        this.stages = allStages;
    }

    @Override
    public void run(Context context) throws Exception {
        log.info("Running energy measurements");
        for (Stage stage : stages) {
            stage.execute(context);
        }
    }
}
