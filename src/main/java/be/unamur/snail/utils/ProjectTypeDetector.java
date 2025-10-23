package be.unamur.snail.utils;

import java.io.File;

public class ProjectTypeDetector {
    public boolean isGradleProject(File projectRoot) {
        return new File(projectRoot, "build.gradle").exists() || new File(projectRoot, "build.gradle.kts").exists();
    }

    public boolean isMavenProject(File projectRoot) {
        return new File(projectRoot, "pom.xml").exists();
    }
}
