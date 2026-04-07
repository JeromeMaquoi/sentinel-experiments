package be.unamur.snail.modules;

import be.unamur.snail.core.Config;
import be.unamur.snail.stages.Stage;
import be.unamur.snail.tool.database.ImportTool;
import be.unamur.snail.tool.database.ImportToolFactory;

import java.util.ArrayList;
import java.util.List;

public class MeasurementsImportModule extends AbstractModule {
    private final List<Stage>  stages;

    public MeasurementsImportModule() {
        this(buildStagesFromConfig(new ImportToolFactory(Config.getInstance()), Config.getInstance()));
    }

    public MeasurementsImportModule(List<Stage> stages) {
        this.stages = stages;
    }

    @Override
    protected List<Stage> getStages() {
        return stages;
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

    public static String buildRepoDir(Config config) {
        return config.getProject().getName() + "_import_" + config.getRepo().getCommit();
    }
}
