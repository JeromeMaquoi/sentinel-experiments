package be.unamur.snail.database;

import be.unamur.snail.exceptions.MongoServiceNotStartedException;
import be.unamur.snail.exceptions.ServerNotStartedException;
import be.unamur.snail.sentinelbackend.BackendServiceManager;

public class SimpleDatabasePreparer implements DatabasePreparer {
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
        if (!backendServiceManager.startBackend()) {
            throw new ServerNotStartedException();
        }
    }
}
