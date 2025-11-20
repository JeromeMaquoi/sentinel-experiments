package be.unamur.snail.utils.gradle;

import be.unamur.snail.exceptions.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class DefaultGradleService implements GradleService {
    private static final Logger log = LoggerFactory.getLogger(DefaultGradleService.class);

    @Override
    public void runTask(File projectRootDir, String gradleTaskPath, File initScript) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("./gradlew", gradleTaskPath, "-I", initScript.getAbsolutePath());
        executeProcess(pb, projectRootDir);
    }

    @Override
    public void runTask(File projectRoot, String gradleTaskPath) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("./gradlew", gradleTaskPath);
        executeProcess(pb, projectRoot);
    }

    protected void executeProcess(ProcessBuilder pb, File projectRoot) throws Exception {
        log.info("Running gradle task: {} in {}", pb.command(), projectRoot.getAbsolutePath());
        pb.directory(projectRoot);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        int exit = process.waitFor();
        if (exit != 0) {
            log.error("Gradle task {} failed in {}", pb.command(), projectRoot.getAbsolutePath());
            throw new ModuleException("Gradle task failed: " + pb.command());
        }
    }
}
