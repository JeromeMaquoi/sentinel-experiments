package be.unamur.snail.utils.gradle;

import java.io.File;

public interface GradleService {
    void runTask(File projectRoot, String gradleTaskPath, File initScript) throws Exception;
}
