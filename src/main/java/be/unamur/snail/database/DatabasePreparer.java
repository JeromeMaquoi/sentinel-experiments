package be.unamur.snail.database;

public interface DatabasePreparer {
    void prepareDatabase() throws Exception;
    void stopBackendAndDatabase() throws Exception;
}
