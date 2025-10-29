package be.unamur.snail.tool.energy;

import be.unamur.snail.core.Config;
import be.unamur.snail.exceptions.BuildFileNotFoundException;
import be.unamur.snail.stages.CopyFileStage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JoularJXToolTest {
    private Config mockConfig;
    private Config.ProjectConfig mockProject;

    @BeforeEach
    void setUp() {
        mockConfig = mock(Config.class);
        mockProject = mock(Config.ProjectConfig.class);
        when(mockConfig.getProject()).thenReturn(mockProject);
    }

    @Test
    void detectBuildFileNameShouldReturnGradleWhenBuildGradleExistsTest() throws IOException {
        String projectName = "sample-project";
        String subProject = "submodule";
        Path resourcePath = Path.of("target/test-classes/build-files", projectName, subProject);
        Files.createDirectories(resourcePath);
        Files.writeString(resourcePath.resolve("build.gradle"), "apply plugin: 'java'");

        JoularJXTool joularJXTool = new JoularJXTool(mockConfig);
        String result = joularJXTool.detectBuildFileName(projectName, subProject);
        assertEquals("build.gradle", result);
    }

    @Test
    void detectBuildFileNameShouldReturnPomWhenPomExistsTest() throws IOException {
        String projectName = "maven-project";
        String subProject = "moduleA";
        Path resourcePath = Path.of("target/test-classes/build-files", projectName, subProject);
        Files.createDirectories(resourcePath);
        Files.writeString(resourcePath.resolve("pom.xml"), "<project></project>");

        JoularJXTool tool = new JoularJXTool(mockConfig);

        String result = tool.detectBuildFileName(projectName, subProject);

        assertEquals("pom.xml", result);
    }

    @Test
    void detectBuildFileNameShouldThrowWhenNoBuildFileExistsTest() {
        JoularJXTool joularJXTool = new JoularJXTool(mockConfig);
        assertThrows(BuildFileNotFoundException.class, () -> joularJXTool.detectBuildFileName("nonexistent", "project"));
    }

    @Test
    void createCopyBuildFileStageShouldReturnConfiguredStageTest() throws IOException {
        String projectName = "gradle-project";
        String subProject = "app";
        Path resourcePath = Path.of("target/test-classes/build-files", projectName, subProject);
        Files.createDirectories(resourcePath);
        Files.writeString(resourcePath.resolve("build.gradle"), "apply plugin: 'java'");

        when(mockProject.getName()).thenReturn(projectName);
        when(mockProject.getSubProject()).thenReturn(subProject);

        JoularJXTool joularJXTool = new JoularJXTool(mockConfig);
        CopyFileStage stage = joularJXTool.createCopyBuildFileStage();

        assertNotNull(stage);
        Path expectedSource = Path.of("resources", "build-files", projectName, subProject, "build.gradle");
        Path expectedTarget = Path.of(subProject).resolve("build.gradle");

        assertEquals(expectedSource, getField(stage, "sourceFile"));
        assertEquals(expectedTarget, getField(stage, "relativeTargetFilePath"));
    }

    private Path getField(CopyFileStage stage, String fieldName) {
        try {
            var field = CopyFileStage.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (Path) field.get(stage);
        } catch (Exception e) {
            fail("Unable to read field " + fieldName + ": " + e.getMessage());
            return null;
        }
    }
}