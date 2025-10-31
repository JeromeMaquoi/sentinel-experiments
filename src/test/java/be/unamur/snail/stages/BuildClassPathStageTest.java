package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.ModuleException;
import be.unamur.snail.exceptions.TargetDirectoryNotFoundException;
import be.unamur.snail.logging.ConsolePipelineLogger;
import be.unamur.snail.utils.gradle.GradleService;
import be.unamur.snail.utils.gradle.InitScriptGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BuildClassPathStageTest {
    @TempDir
    Path tempDir;

    private GradleService gradleService;
    private InitScriptGenerator initScriptGenerator;
    private Context context;

    @BeforeEach
    void setup() {
        gradleService = mock(GradleService.class);
        initScriptGenerator = mock(InitScriptGenerator.class);
        context = new Context();
        context.setLogger(new ConsolePipelineLogger(BuildClassPathStage.class));
    }

    @AfterEach
    void cleanup() {
        Config.reset();
    }

    @Test
    void gradleBuildsClasspathInRootProject() throws Exception {
        // Prepare repo dir
        Path repoDir = tempDir.resolve("repo_123");
        Files.createDirectories(repoDir);
        Files.createFile(repoDir.resolve("build.gradle"));

        // Fake classpath.txt at root
        Path cpFile = repoDir.resolve("classpath.txt");
        Files.write(cpFile, List.of("a.jar", "b.jar"));

        // Setup Config singleton
        Config fake = new Config();
        Config.RepoConfig repo = new Config.RepoConfig();
        setField(repo, "targetDir", repoDir.toString().replace("_123", ""));
        setField(repo, "commit", "123");
        Config.ProjectConfig project = new Config.ProjectConfig(); // no subproject
        setField(fake, "repo", repo);
        setField(fake, "project", project);
        setInstance(fake);

        // Init mocks
        when(initScriptGenerator.generateClasspathInitScript()).thenReturn(cpFile.toFile());

        BuildClassPathStage stage = new BuildClassPathStage(gradleService, initScriptGenerator);

        stage.execute(context);

        List<String> cp = context.getClassPath();
        assertEquals(List.of("a.jar", "b.jar"), cp);
    }

    @Test
    void gradleBuildsClasspathInSubProject() throws Exception {
        // Prepare repo directory
        Path repoDir = tempDir.resolve("repo_123");
        Files.createDirectories(repoDir.resolve("subproject"));
        Files.createFile(repoDir.resolve("build.gradle"));

        Path cpFile = repoDir.resolve("subproject/classpath.txt");
        Files.write(cpFile, List.of("dep1.jar", "dep2.jar"));

        Config fake = new Config();
        var repo = new Config.RepoConfig();
        setField(repo, "targetDir", repoDir.toString().replace("_123", ""));
        setField(repo, "commit", "123");

        var project = new Config.ProjectConfig();
        setField(project, "subProject", "subproject");

        setField(fake, "repo", repo);
        setField(fake, "project", project);
        Config.setInstanceForTests(fake);

        when(initScriptGenerator.generateClasspathInitScript()).thenReturn(cpFile.toFile());

        BuildClassPathStage stage = new BuildClassPathStage(gradleService, initScriptGenerator);

        stage.execute(context);

        List<String> cp = context.getClassPath();
        assertEquals(List.of("dep1.jar", "dep2.jar"), cp);
    }

    @Test
    void throwsIfClasspathFileMissing() throws Exception {
        Path repoDir = tempDir.resolve("repo_123");
        Files.createDirectories(repoDir);
        Files.createFile(repoDir.resolve("build.gradle"));

        Config fake = new Config();
        Config.RepoConfig repo = new Config.RepoConfig();
        setField(repo, "targetDir", repoDir.toString().replace("_123", ""));
        setField(repo, "commit", "123");
        Config.ProjectConfig project = new Config.ProjectConfig();
        setField(fake, "repo", repo);
        setField(fake, "project", project);
        setInstance(fake);

        when(initScriptGenerator.generateClasspathInitScript()).thenReturn(repoDir.resolve("dummy.gradle").toFile());

        BuildClassPathStage stage = new BuildClassPathStage(gradleService, initScriptGenerator);

        assertThrows(ModuleException.class, () -> stage.execute(context));
    }

    @Test
    void throwsIfProjectDirDoesNotExist() {
        Config fake = new Config();
        Config.RepoConfig repo = new Config.RepoConfig();
        setField(repo, "targetDir", tempDir.resolve("missing").toString());
        setField(repo, "commit", "zzz");
        Config.ProjectConfig project = new Config.ProjectConfig();
        setField(fake, "repo", repo);
        setField(fake, "project", project);
        setInstance(fake);

        BuildClassPathStage stage = new BuildClassPathStage(gradleService, initScriptGenerator);

        assertThrows(TargetDirectoryNotFoundException.class, () -> stage.execute(context));
    }

    @Test
    void throwsIfNoBuildSystemFileFound() throws Exception {
        Path repoDir = tempDir.resolve("repo_123");
        Files.createDirectories(repoDir);

        Config fake = new Config();
        Config.RepoConfig repo = new Config.RepoConfig();
        setField(repo, "targetDir", repoDir.toString().replace("_123", ""));
        setField(repo, "commit", "123");
        Config.ProjectConfig project = new Config.ProjectConfig();
        setField(fake, "repo", repo);
        setField(fake, "project", project);
        setInstance(fake);

        BuildClassPathStage stage = new BuildClassPathStage(gradleService, initScriptGenerator);

        assertThrows(IllegalArgumentException.class, () -> stage.execute(context));
    }


    private static void setField(Object target, String fieldName, Object value) {
        try {
            var f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setInstance(Config fake) {
        try {
            var f = Config.class.getDeclaredField("instance");
            f.setAccessible(true);
            f.set(null, fake);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}