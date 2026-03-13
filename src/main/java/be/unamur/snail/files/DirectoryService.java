package be.unamur.snail.files;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface DirectoryService {
    boolean exists(Path path);
    boolean isDirectory(Path path);
    List<Path> listDirectories(Path path) throws IOException;
}
