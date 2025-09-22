package be.unamur.snail.exceptions;

import java.nio.file.Path;

public class SourceDirectoryNotFoundException extends Exception {
    public SourceDirectoryNotFoundException(Path path) {
        super("Source directory not found: " + path);
    }
}
