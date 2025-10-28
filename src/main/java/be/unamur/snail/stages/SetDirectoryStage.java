package be.unamur.snail.stages;

import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingContextKeyException;
import be.unamur.snail.exceptions.TargetDirectoryNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;

public class SetDirectoryStage implements Stage {
    private static final Logger log = LoggerFactory.getLogger(SetDirectoryStage.class);

    @Override
    public void execute(Context context) throws Exception {
        String repoPath = context.getRepoPath();
        if (repoPath == null || repoPath.isBlank()) {
            throw new MissingContextKeyException("repoPath");
        }

        Path targetDir = Path.of(repoPath).toAbsolutePath();
        log.info("targetDir: {}", targetDir);
        File dirFile = targetDir.toFile();

        if (!dirFile.exists() || !dirFile.isDirectory()) {
            throw new TargetDirectoryNotFoundException();
        }

        log.info("Changing working directory to {}", targetDir);
        System.setProperty("user.dir", targetDir.toString());
        context.setCurrentWorkingDir(targetDir.toString());
    }
}
