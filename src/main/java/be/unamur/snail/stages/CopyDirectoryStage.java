package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.DirectoryNotCopiedException;
import be.unamur.snail.exceptions.MissingConfigKeyException;
import be.unamur.snail.exceptions.ModuleException;
import be.unamur.snail.exceptions.SourceDirectoryNotFoundException;
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
            throw new MissingConfigKeyException("target-dir");
        }
        if (commit == null || commit.isBlank()) {
            throw new MissingConfigKeyException("commit");
        }

        Path source = Paths.get(targetDir).toAbsolutePath();
        log.debug("Source directory: {}", source);
        if (!Files.isDirectory(source)) {
            throw new SourceDirectoryNotFoundException(source);
        }

        Path target = Paths.get(targetDir + "_" + commit).toAbsolutePath();

        // If directory already exists and overwritten is not asked, return
        if (Files.exists(target) && !config.getRepo().isOverwrite()) {
            log.info("Target already exists, skipping copy");
            context.setRepoPath(target.toAbsolutePath().toString());
            log.debug("Context repository path: {}", context.getRepoPath());
            return;
        }

        log.debug("Copying directory from {} to {}", source, target);
        try {
            copyDirectory(source, target);
            log.info("Copy of directory from {} to {} completed", source, target);
        } catch (IOException e) {
            throw new ModuleException("Error while copying directory from " + source + " to " + target, e);
        }

        context.setRepoPath(target.toAbsolutePath().toString());
        log.debug("Context repository path: {}", context.getRepoPath());
    }

    /**
     * Copy recursively a directory source into a target
     * @param source The path to copy
     * @param target The path where to paste the copy
     * @throws IOException if there is a problem during the copy
     */
    public void copyDirectory(Path source, Path target) throws IOException {
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
