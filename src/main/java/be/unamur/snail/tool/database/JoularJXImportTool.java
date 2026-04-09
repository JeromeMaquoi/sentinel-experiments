package be.unamur.snail.tool.database;

import be.unamur.snail.core.Config;
import be.unamur.snail.files.DefaultDirectoryService;
import be.unamur.snail.services.MeasurementsImportService;
import be.unamur.snail.stages.ImportMeasurementsStage;
import be.unamur.snail.stages.PrepareBackendStage;
import be.unamur.snail.stages.Stage;
import be.unamur.snail.stages.StopBackendStage;
import be.unamur.snail.tool.energy.FolderProcessorFactory;
import be.unamur.snail.tool.energy.JoularJXFolderProcessorFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class JoularJXImportTool implements ImportTool {
    private final Config config;

    public JoularJXImportTool(Config config) {
        this.config = config;
    }

    @Override
    public String getName() {
        return "JoularJX";
    }

    @Override
    public List<Stage> createPreparationStages() {
        return List.of(
                new StopBackendStage(),
                new PrepareBackendStage()
        );
    }

    @Override
    public List<Stage> createImportStages() {
        String subProject = config.getProject().getSubProject();
        String totalProjectString = subProject != null && !subProject.isBlank() ? subProject + "/joularjx-result" : "joularjx-result";
        Path totalProjectPath = Paths.get(totalProjectString).normalize();

        FolderProcessorFactory processorFactory = new JoularJXFolderProcessorFactory(config.getExecutionPlan().getEnergyMeasurements().getImportConfig());

        MeasurementsImportService service = new MeasurementsImportService(new DefaultDirectoryService(), processorFactory);

        return List.of(
                new ImportMeasurementsStage(totalProjectPath, service)
        );
    }

    @Override
    public List<Stage> createCleanupStages() {
        return List.of();
    }
}
