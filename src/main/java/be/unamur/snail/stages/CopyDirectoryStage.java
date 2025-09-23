package be.unamur.snail.stages;

import be.unamur.snail.config.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.core.Stage;
import be.unamur.snail.exceptions.DirectoryNotCopiedException;
import be.unamur.snail.exceptions.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class CopyDirectoryStage implements Stage {
    private static final Logger log = LoggerFactory.getLogger(CopyDirectoryStage.class);

    @Override
    public void execute(Context context) throws Exception {
        Config config = Config.getInstance();
        String targetDir = config.getRepo().getTargetDir();
        String commit = config.getRepo().getCommit();

        if (targetDir == null || targetDir.isBlank()) {
            throw new ModuleException("Missing required context key: target-dir");
        }
        if (commit == null || commit.isBlank()) {
            throw new ModuleException("Missing required context key: commit");
        }

        Path source = Paths.get(targetDir).toAbsolutePath();
        Path target = Paths.get(targetDir + "_" + commit).toAbsolutePath();

        log.debug("Source directory: {}", source);

        if (!Files.isDirectory(source)) {
            throw new ModuleException("Source directory not found: " + source);
        }

        log.debug("Copying directory from {} to {}", source, target);
        try {
            copyDirectory(source, target);
        } catch (IOException e) {
            throw new ModuleException("Error while copying directory from " + source + " to " + target, e);
        }
        log.info("Copy of directory from {} to {} completed", source, target);

        context.setRepoPath(target.toAbsolutePath().toString());
        log.debug("Context repository path: {}", context.getRepoPath());
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        try (var stream = Files.walk(source)) {
            stream.forEach(path -> {
                Path dest = target.resolve(source.relativize(path));
                try {
                    if (Files.isDirectory(path)) {
                        Files.createDirectories(dest);
                    } else {
                        Files.copy(path, dest, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new DirectoryNotCopiedException(path, e);
                }
            });
        }
    }
}
