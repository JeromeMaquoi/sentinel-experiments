package be.unamur.snail.stages;

import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.BuildFilesNotFoundException;
import be.unamur.snail.exceptions.MissingContextKeyException;
import be.unamur.snail.exceptions.NoBuildLineUpdatedException;
import be.unamur.snail.logging.PipelineLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * Stage responsible for updating build files (Maven or Gradle) to replace
 * lines containing "-javaagent" and JOULARJX with the actual tool path.
 * This stages needs to be executed right after copying the build file into the repo.
 */
public class UpdateBuildFileStage implements Stage {
    private PipelineLogger log;

    @Override
    public void execute(Context context) throws Exception {
        log = context.getLogger();

        String repoPath = context.getRepoPath();
        if (repoPath == null || repoPath.isBlank()) {
            throw new MissingContextKeyException("repoPath");
        }

        String toolPath = context.getEnergyToolPath();
        if (toolPath == null || toolPath.isBlank()) {
            throw new MissingContextKeyException("energyToolPath");
        }

        String copiedBuildFilePath = context.getCopiedBuildFilePath();
        if (copiedBuildFilePath == null || copiedBuildFilePath.isBlank()) {
            throw new MissingContextKeyException("copiedBuildFilePath");
        }

        Path repoRoot = Path.of(repoPath).toAbsolutePath();
        Path buildFile = repoRoot.resolve(copiedBuildFilePath).normalize();

        if (!Files.exists(buildFile)) {
            throw new BuildFilesNotFoundException();
        }

        updateBuildFile(buildFile, toolPath, log);
        log.info("Copied build file from {} to {}", buildFile.toAbsolutePath(), repoPath);
    }

    public void updateBuildFile(Path buildFile, String toolPath, PipelineLogger log) throws IOException {
        log.debug("Updating build file: {}", buildFile);
        List<String> lines = Files.readAllLines(buildFile);
        boolean modified = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            log.debug(line);
            if (line.contains("-javaagent") && line.contains("JOULARJX")) {
                // Replace javaagent part with correct toolPath
                String newLine = line.replaceAll("-javaagent:.*JOULARJX", "-javaagent:" + toolPath);
                lines.set(i, newLine);
                modified = true;
                log.debug("Updated javaagent line in {}", buildFile);
            }
        }

        if (modified) {
            Files.write(buildFile, lines, StandardOpenOption.TRUNCATE_EXISTING);
        } else {
            throw new NoBuildLineUpdatedException(buildFile.toString());
        }
    }
}
