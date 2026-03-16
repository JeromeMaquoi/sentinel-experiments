package be.unamur.snail.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class DefaultDirectoryService implements DirectoryService {
    @Override
    public boolean exists(Path path) {
        return Files.exists(path);
    }

    @Override
    public boolean isDirectory(Path path) {
        return Files.isDirectory(path);
    }

    @Override
    public List<Path> listDirectories(Path path) throws IOException {
        try (Stream<Path> stream = Files.list(path)) {
            return stream.filter(Files::isDirectory).toList();
        }
    }
}
