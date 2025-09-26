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


public class PrepareDatabaseStage implements Stage {
    private static final Logger log = LoggerFactory.getLogger(PrepareDatabaseStage.class);

    @Override
    public void execute(Context context) throws Exception {
        Config config = Config.getInstance();
        String mode = config.getDatabase().getMode();
        if (mode == null || mode.isEmpty()) {
            throw new MissingConfigKeyException("database.mode");
        }
        String backendPath = config.getDatabase().getBackendPath();
        if (backendPath == null || backendPath.isEmpty()) {
            throw new MissingConfigKeyException("database.backendPath");
        }

        CommandRunner runner = new SimpleCommandRunner();
        MongoServiceManager mongoManager = new MongoServiceManager(runner, 5, 500);
        BackendServiceManager backendManager;

        if (mode.equalsIgnoreCase("dev")) {
            log.info("Preparing for local development database...");
            backendManager = new DevBackendServiceManager(runner, backendPath, config.getDatabase().getNbCheckServerStart(), 1000);
        } else if (mode.equalsIgnoreCase("prod")) {
            log.info("Preparing for production database...");
            backendManager = new ProdBackendServiceManager(runner, backendPath);
        } else {
            throw new UnsupportedDatabaseMode(config.getDatabase().getMode());
        }

        DatabasePreparer preparer = new SimpleDatabasePreparer(mongoManager, backendManager);
        preparer.prepareDatabase();
    }
}
