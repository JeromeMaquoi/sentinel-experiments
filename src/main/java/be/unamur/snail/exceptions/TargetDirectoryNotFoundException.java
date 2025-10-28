package be.unamur.snail.exceptions;

public class TargetDirectoryNotFoundException extends Exception{
    public TargetDirectoryNotFoundException() {
        super("Target directory not found.");
    }
}
