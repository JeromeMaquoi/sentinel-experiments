package be.unamur.snail.exceptions;

import java.io.IOException;

public class SourceFileNotFoundException extends IOException {
    public SourceFileNotFoundException(String sourceFile) {
        super("Source file not found: " + sourceFile);
    }
}
