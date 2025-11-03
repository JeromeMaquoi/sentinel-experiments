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


/**
 * Stage responsible for the preparation of the backend, by starting the
 * database and the relevant APIs to access it
 */
public class PrepareBackendStage implements Stage {
    private final CommandRunner runner;
    private final BackendServiceManagerFactory backendFactory;
    private final DatabasePreparerFactory databaseFactory;

    public PrepareBackendStage(CommandRunner runner, BackendServiceManagerFactory backendFactory, DatabasePreparerFactory databaseFactory) {
        this.runner = runner;
        this.databaseFactory = databaseFactory;
        this.backendFactory = backendFactory;
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

        log.info("Preparing backend in mode {}", mode);
        BackendServiceManager backendManager = backendFactory.create(mode, runner, serverPath);
        DatabasePreparer preparer = databaseFactory.create(backendManager);

        preparer.prepareDatabase();
    }
}
