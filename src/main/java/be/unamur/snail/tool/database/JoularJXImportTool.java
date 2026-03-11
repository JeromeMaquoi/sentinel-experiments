package be.unamur.snail.tool.database;

import be.unamur.snail.core.Config;
import be.unamur.snail.database.DatabasePreparerFactory;
import be.unamur.snail.database.MongoServiceManager;
import be.unamur.snail.database.SimpleDatabasePreparerFactory;
import be.unamur.snail.sentinelbackend.BackendServiceManagerFactory;
import be.unamur.snail.sentinelbackend.SimpleBackendServiceManagerFactoryImpl;
import be.unamur.snail.stages.ImportMeasurementsStage;
import be.unamur.snail.stages.PrepareBackendStage;
import be.unamur.snail.stages.Stage;
import be.unamur.snail.stages.StopBackendStage;
import be.unamur.snail.tool.energy.JoularJXFolderProcessorFactory;
import be.unamur.snail.utils.CommandRunner;
import be.unamur.snail.utils.SimpleCommandRunner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

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
        CommandRunner runner = new SimpleCommandRunner();
        MongoServiceManager mongo = new MongoServiceManager(runner, 5, 500);
        BackendServiceManagerFactory backendFactory = new SimpleBackendServiceManagerFactoryImpl();
        DatabasePreparerFactory databaseFactory = new SimpleDatabasePreparerFactory(mongo);

        return List.of(
//                new StopBackendStage(runner, backendFactory, databaseFactory),
//                new PrepareBackendStage(runner, backendFactory, databaseFactory)
        );
    }

    @Override
    public List<Stage> createImportStages() {
        String subProject = config.getProject().getSubProject();
        String totalProjectString = subProject != null && !subProject.isBlank() ? subProject + "/joularjx-result" : "joularjx-result";
        Path totalProjectPath = Paths.get(totalProjectString).normalize();

        return List.of(
                new ImportMeasurementsStage(totalProjectPath, new JoularJXFolderProcessorFactory())
        );
    }

    @Override
    public List<Stage> createCleanupStages() {
        return List.of();
    }
}
