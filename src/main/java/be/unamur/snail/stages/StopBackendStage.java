package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.database.DatabasePreparer;
import be.unamur.snail.database.DatabasePreparerFactory;
import be.unamur.snail.exceptions.MissingConfigKeyException;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.sentinelbackend.BackendServiceManager;
import be.unamur.snail.sentinelbackend.BackendServiceManagerFactory;
import be.unamur.snail.utils.CommandRunner;

import java.io.IOException;

/**
 * Stage responsible for stopping the database and the backend services
 */
public class StopBackendStage implements Stage {
    private final CommandRunner runner;
    private final BackendServiceManagerFactory backendFactory;
    private final DatabasePreparerFactory databaseFactory;

    public StopBackendStage(CommandRunner runner, BackendServiceManagerFactory backendFactory, DatabasePreparerFactory databaseFactory) {
        this.runner = runner;
        this.backendFactory = backendFactory;
        this.databaseFactory = databaseFactory;
    }

    @Override
    public void execute(Context context) throws Exception {
        PipelineLogger log = context.getLogger();

        Config config = Config.getInstance();
        String mode = config.getBackend().getMode();
        if (mode == null || mode.isEmpty()) {
            throw new MissingConfigKeyException("backend.mode");
        }
        String serverPath = config.getBackend().getServerPath();
        if (serverPath == null || serverPath.isEmpty()) {
            throw new MissingConfigKeyException("backend.serverPath");
        }

        log.info("Stopping backend in mode {}", mode);
        BackendServiceManager backendManager = backendFactory.create(mode, runner, serverPath);
        DatabasePreparer preparer = databaseFactory.create(backendManager);
        preparer.stopBackendAndDatabase();
    }

    @Override
    public String getName() {
        return Stage.super.getName();
    }
}
