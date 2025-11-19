package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.DirectoryNotCopiedException;
import be.unamur.snail.exceptions.MissingConfigKeyException;
import be.unamur.snail.exceptions.ModuleException;
import be.unamur.snail.exceptions.SourceDirectoryNotFoundException;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Stage responsible for the copy of the analyzed project's files into a new
 * directory
 */
public class CopyDirectoryStage implements Stage {
    @Override
    public void execute(Context context) throws Exception {
        PipelineLogger log = context.getLogger();

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
        if (Files.exists(target) && !config.getRepo().isOverwriteCopy()) {
            log.info("Target already exists, skipping copy");
            context.setBaseRepoPath(source.toAbsolutePath().toString());
            context.setRepoPath(target.toAbsolutePath().toString());
            log.debug("Context repository path: {}", context.getRepoPath());
            return;
        }

        try {
            log.info("Delete target directory if it exists");
            Utils.deleteDirectory(target.toFile());
            copyDirectory(source, target);
            log.info("Copy of directory from {} to {} completed", source, target);
        } catch (IOException e) {
            throw new ModuleException("Error while copying directory from " + source + " to " + target, e);
        }

        context.setBaseRepoPath(source.toAbsolutePath().toString());
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
                    if (path.getFileName().toString().equals(".gradle")) {
                        return;
                    }
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
