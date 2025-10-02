package be.unamur.snail.database;

import be.unamur.snail.sentinelbackend.BackendServiceManager;

public class SimpleDatabasePreparerFactory implements DatabasePreparerFactory {
    private final MongoServiceManager mongoManager;

    public SimpleDatabasePreparerFactory(MongoServiceManager mongoManager) {
        this.mongoManager = mongoManager;
    }

    @Override
    public DatabasePreparer create(BackendServiceManager backendManager) {
        return new SimpleDatabasePreparer(mongoManager, backendManager);
    }
}
