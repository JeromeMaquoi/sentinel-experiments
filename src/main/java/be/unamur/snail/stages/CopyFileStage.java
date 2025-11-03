package be.unamur.snail.stages;

import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingContextKeyException;
import be.unamur.snail.exceptions.SourceFileNotFoundException;
import be.unamur.snail.logging.PipelineLogger;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Generic stage responsible for copying a file from a source to a destination
 * Can be used for build files (pom.xml, build.gradle, etc.) or any other file needed during the process
 */
public class CopyFileStage implements Stage {
    private final Path sourceFile;
    private final Path relativeTargetFilePath;

    public CopyFileStage(Path sourceFile, Path relativeTargetFilePath) {
        this.sourceFile = sourceFile;
        this.relativeTargetFilePath = relativeTargetFilePath;
    }

    /**
     * Execute the file copy operation
     * @param context The pipeline context containing repository info, etc.
     * @throws Exception If any error occurs during file copy
     */
    @Override
    public void execute(Context context) throws Exception {
        PipelineLogger log = context.getLogger();

        String classPathResource = sourceFile.toString().replace("\\", "/").replaceFirst("^resources/", "");
        InputStream in = getClass().getClassLoader().getResourceAsStream(classPathResource);
        if (in == null) {
            throw new SourceFileNotFoundException(sourceFile.toString());
        }

        String repoPath = context.getRepoPath();
        if (repoPath == null || repoPath.isBlank()) {
            throw new MissingContextKeyException("repoPath");
        }

        Path repoRoot = Path.of(repoPath).toAbsolutePath();
        Path targetPath = repoRoot.resolve(relativeTargetFilePath).normalize();

        Files.createDirectories(targetPath.getParent());
        Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);

        log.info("Copied file from {} to {}", sourceFile.toAbsolutePath(), targetPath);
        context.setCopiedBuildFilePath(relativeTargetFilePath.toString());
    }
}
