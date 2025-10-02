package be.unamur.snail.stages;

import be.unamur.snail.config.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.core.Stage;
import be.unamur.snail.database.DatabasePreparer;
import be.unamur.snail.database.MongoServiceManager;
import be.unamur.snail.database.SimpleDatabasePreparer;
import be.unamur.snail.exceptions.MissingConfigKeyException;
import be.unamur.snail.exceptions.UnsupportedDatabaseMode;
import be.unamur.snail.sentinelbackend.BackendServiceManager;
import be.unamur.snail.sentinelbackend.DevBackendServiceManager;
import be.unamur.snail.sentinelbackend.ProdBackendServiceManager;
import be.unamur.snail.utils.CommandRunner;
import be.unamur.snail.utils.SimpleCommandRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PrepareBackendStage implements Stage {
    private static final Logger log = LoggerFactory.getLogger(PrepareBackendStage.class);

    @Override
    public void execute(Context context) throws Exception {
        Config config = Config.getInstance();
        String mode = config.getBackend().getMode();
        if (mode == null || mode.isEmpty()) {
            throw new MissingConfigKeyException("backend.mode");
        }
        String serverPath = config.getBackend().getServerPath();
        if (serverPath == null || serverPath.isEmpty()) {
            throw new MissingConfigKeyException("backend.serverPath");
        }

        CommandRunner runner = new SimpleCommandRunner();
        MongoServiceManager mongoManager = new MongoServiceManager(runner, 5, 500);
        BackendServiceManager backendManager;

        if (mode.equalsIgnoreCase("dev")) {
            log.info("Preparing for local development database...");
            backendManager = new DevBackendServiceManager(runner, serverPath, config.getBackend().getNbCheckServerStart(), 5000);
        } else if (mode.equalsIgnoreCase("prod")) {
            log.info("Preparing for production database...");
            backendManager = new ProdBackendServiceManager(runner, serverPath);
        } else {
            throw new UnsupportedDatabaseMode(config.getBackend().getMode());
        }

        DatabasePreparer preparer = new SimpleDatabasePreparer(mongoManager, backendManager);
        preparer.prepareDatabase();
    }
}
