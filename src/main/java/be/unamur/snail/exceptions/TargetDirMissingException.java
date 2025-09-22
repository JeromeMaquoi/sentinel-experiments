package be.unamur.snail.exceptions;

public class TargetDirMissingException extends Exception {
    public TargetDirMissingException(String targetDir) {
        super("Target dir '" + targetDir + "' is missing.");
    }
}
