package be.unamur.snail.exceptions;

public class CommitMissingException extends Exception{
    public CommitMissingException(String commit) {
        super("Commit " + commit + " is missing");
    }
}
