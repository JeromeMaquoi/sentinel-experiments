package be.unamur.snail.database;

import be.unamur.snail.core.Context;

public interface DatabasePreparer {
    void prepareDatabase(Context context) throws Exception;
    void stopBackendAndDatabase(Context context) throws Exception;
}
