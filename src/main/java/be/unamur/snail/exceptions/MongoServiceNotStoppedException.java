package be.unamur.snail.exceptions;

public class MongoServiceNotStoppedException extends RuntimeException {
    public MongoServiceNotStoppedException() {
        super("Mongo service not stopped");
    }
}
