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
        pb.directory(projectRootDir);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        int exit = process.waitFor();
        if (exit != 0) {
            log.error("Gradle classpath generation failed");
            throw new ModuleException("Gradle classpath generation failed");
        }
    }
}
