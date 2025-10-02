package be.unamur.snail.exceptions;

public class MongoServiceNotStartedException extends RuntimeException {
    public MongoServiceNotStartedException() {
        super("Mongo service not started");
    }
}
