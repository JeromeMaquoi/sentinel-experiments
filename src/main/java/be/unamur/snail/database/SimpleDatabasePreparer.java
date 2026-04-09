package be.unamur.snail.database;

import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MongoServiceNotStartedException;
import be.unamur.snail.exceptions.MongoServiceNotStoppedException;
import be.unamur.snail.exceptions.ServerNotStartedException;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.sentinelbackend.BackendServiceManager;

public class SimpleDatabasePreparer implements DatabasePreparer {
    private final MongoServiceManager mongoServiceManager;
    private final BackendServiceManager backendServiceManager;

    public SimpleDatabasePreparer(MongoServiceManager mongoServiceManager, BackendServiceManager backendServiceManager) {
        this.mongoServiceManager = mongoServiceManager;
        this.backendServiceManager = backendServiceManager;
    }

    @Override
    public void prepareDatabase(Context context) throws Exception {
        PipelineLogger log = context.getLogger();
        if (!mongoServiceManager.startMongoService()) {
            throw new MongoServiceNotStartedException();
        }
        log.info("Mongo service started");
        if (!backendServiceManager.startBackend()) {
            throw new ServerNotStartedException();
        }
        log.info("Backend service started");
    }

    @Override
    public void stopBackendAndDatabase(Context context) throws Exception {
        PipelineLogger log = context.getLogger();
        if (!backendServiceManager.stopBackend()) {
            throw new ServerNotStartedException();
        }
        log.info("Backend service stopped");
        if (!mongoServiceManager.stopMongoService()) {
            throw new MongoServiceNotStoppedException();
        }
        log.info("Mongo service stopped");
    }
}
