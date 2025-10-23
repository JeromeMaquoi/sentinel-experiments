package be.unamur.snail.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class ProjectTypeDetectorTest {
    private File tempFile;
    private ProjectTypeDetector detector;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempDirectory("project").toFile();
        detector = new ProjectTypeDetector();
    }

    @AfterEach
    void tearDown() {
        deleteRecursively(tempFile);
    }

    private void deleteRecursively(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                deleteRecursively(child);
            }
        }
        file.delete();
    }

    @Test
    void isGradleProjectTest() throws IOException {
        File buildGradle = new File(tempFile, "build.gradle");
        assertTrue(buildGradle.createNewFile());

        assertTrue(detector.isGradleProject(tempFile));
        assertFalse(detector.isMavenProject(tempFile));
    }

    @Test
    void isMavenProjectTest() throws IOException {
        File buildMaven = new File(tempFile, "pom.xml");
        assertTrue(buildMaven.createNewFile());

        assertTrue(detector.isMavenProject(tempFile));
        assertFalse(detector.isGradleProject(tempFile));
    }
}