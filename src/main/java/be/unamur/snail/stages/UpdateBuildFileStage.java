package be.unamur.snail.stages;

import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.BuildFilesNotFoundException;
import be.unamur.snail.exceptions.MissingContextKeyException;
import be.unamur.snail.exceptions.NoBuildLineUpdatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Stream;

/**
 * Stage responsible for updating build files (Maven or Gradle) to replace
 * lines containing "-javaagent" and JOULARJX with the actual tool path.
 */
public class UpdateBuildFileStage implements Stage {
    private static final Logger log = LoggerFactory.getLogger(UpdateBuildFileStage.class);

    @Override
    public void execute(Context context) throws Exception {
        String repoPath = context.getRepoPath();
        if (repoPath == null || repoPath.isBlank()) {
            throw new MissingContextKeyException("repoPath");
        }
        String toolPath = context.getEnergyToolPath();
        if (toolPath == null || toolPath.isBlank()) {
            throw new MissingContextKeyException("energyToolPath");
        }

        Path repoRoot = Path.of(repoPath).toAbsolutePath();

        try (Stream<Path> paths = Files.walk(repoRoot)) {
            List<Path> buildFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals("pom.xml") ||
                                    path.getFileName().toString().equals("build.gradle"))
                    .toList();
            if (buildFiles.isEmpty()) {
                throw new BuildFilesNotFoundException();
            }
            for (Path buildFile : buildFiles) {
                updateBuildFile(buildFile, toolPath);
            }
        }
    }

    public void updateBuildFile(Path buildFile, String toolPath) throws IOException {
        List<String> lines = Files.readAllLines(buildFile);
        boolean modified = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.contains("-javaagent") && line.contains("JOULARJX")) {
                // Replace javaagent part with correct toolPath
                String newLine = line.replaceAll("-javaagent:.*JOULARJX", "-javaagent:" + toolPath);
                lines.set(i, newLine);
                modified = true;
                log.info("Updated javaagent line in {}", buildFile);
            }
        }

        if (modified) {
            Files.write(buildFile, lines, StandardOpenOption.TRUNCATE_EXISTING);
        } else {
            throw new NoBuildLineUpdatedException(buildFile.toString());
        }
    }
}
