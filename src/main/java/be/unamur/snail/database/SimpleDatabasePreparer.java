package be.unamur.snail.database;

import be.unamur.snail.exceptions.MongoServiceNotStartedException;
import be.unamur.snail.exceptions.ServerNotStartedException;
import be.unamur.snail.sentinelbackend.BackendServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleDatabasePreparer implements DatabasePreparer {
    private static final Logger log = LoggerFactory.getLogger(SimpleDatabasePreparer.class);
    private final MongoServiceManager mongoServiceManager;
    private final BackendServiceManager backendServiceManager;

    public SimpleDatabasePreparer(MongoServiceManager mongoServiceManager, BackendServiceManager backendServiceManager) {
        this.mongoServiceManager = mongoServiceManager;
        this.backendServiceManager = backendServiceManager;
    }

    @Override
    public void prepareDatabase() throws Exception {
        if (!mongoServiceManager.startMongoService()) {
            throw new MongoServiceNotStartedException();
        }
        log.info("Mongo service started");
        if (!backendServiceManager.startBackend()) {
            throw new ServerNotStartedException();
        }
        log.info("Backend service started");
    }
}
