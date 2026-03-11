package be.unamur.snail.modules;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.stages.Stage;
import be.unamur.snail.tool.database.ImportTool;
import be.unamur.snail.tool.database.ImportToolFactory;

import java.util.ArrayList;
import java.util.List;

public class MeasurementsImportModule implements Module {
    private final List<Stage>  stages;

    public MeasurementsImportModule(List<Stage> stages) {
        this.stages = stages;
    }

    public static List<Stage> buildStagesFromConfig(ImportToolFactory factory, Config config) {
        String toolName = config.getExecutionPlan().getEnergyMeasurements().getTool();
        ImportTool tool = factory.create(toolName);
        List<Stage> stages = new ArrayList<>();
        stages.addAll(tool.createPreparationStages());
        stages.addAll(tool.createImportStages());
        stages.addAll(tool.createCleanupStages());
        return stages;
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
