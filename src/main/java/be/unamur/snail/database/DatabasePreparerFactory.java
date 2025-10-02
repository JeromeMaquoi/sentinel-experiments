package be.unamur.snail.database;

import be.unamur.snail.sentinelbackend.BackendServiceManager;

public interface DatabasePreparerFactory {
    DatabasePreparer create(BackendServiceManager backendManager);
}
