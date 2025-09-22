package be.unamur.snail.exceptions;

import java.nio.file.Path;

public class DirectoryNotCopiedException extends RuntimeException {
    public DirectoryNotCopiedException(Path path, Throwable cause) {
        super("The directory '" + path + "' could not be copied.", cause);
    }
}
