package be.unamur.snail.stages;

import be.unamur.snail.config.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.core.Stage;
import be.unamur.snail.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CopySourceCodeStage implements Stage {
    private static final Logger log = LoggerFactory.getLogger(CopySourceCodeStage.class);

    @Override
    public void execute(Context context) throws Exception {
        Config config = Config.getInstance();
        String fromDir = config.getCodeConstructorsInstrumentationPath();
        Path from = Paths.get(fromDir);

        if (!Files.isDirectory(from)) {
            throw new SourceDirectoryNotFoundException(from);
        }

        String targetDir = config.getRepo().getTargetDir();
        String targetProjectName = getTargetProjectName(config, targetDir);
        Path target = Paths.get(targetProjectName).toAbsolutePath();
        log.info("Copying source code from {} to {}", from, target);

        Files.createDirectories(target);
        copyJavaFiles(from, target);
        log.info("Copy completed.");
    }

    public String getTargetProjectName(Config config, String targetDir) throws TargetDirMissingException, CommitMissingException {
        String commit = config.getRepo().getCommit();
        String subProject = config.getProject().getSubProject();

        if (targetDir == null || targetDir.isBlank()) {
            throw new TargetDirMissingException(targetDir);
        }
        if (commit == null || commit.isBlank()) {
            throw new CommitMissingException(commit);
        }

        return targetDir + "_" + commit + subProject + "/src/main/java/be/unamur/snail/";
    }

    public void copyJavaFiles(Path source, Path target) throws IOException {
        try(var stream = Files.walk(source)) {
            stream.forEach(path -> {
                try {
                    Path targetPath = target.resolve(source.relativize(path));
                    if (Files.isDirectory(path)) {
                        Files.createDirectories(targetPath);
                    } else {
                        Files.copy(path, targetPath);
                    }
                } catch (IOException e) {
                    throw new DirectoryNotCopiedException(path, e);
                }
            });
        }
    }
}
