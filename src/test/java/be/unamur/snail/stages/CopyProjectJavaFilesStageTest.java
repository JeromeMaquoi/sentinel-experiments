package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingContextKeyException;
import be.unamur.snail.logging.PipelineLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CopyProjectJavaFilesStageTest {
    @TempDir
    Path repoDir;

    private Context context;
    private PipelineLogger logger;
    private MockedStatic<Config> mockedConfig;

    @BeforeEach
    void setUp() {
        context = mock(Context.class);
        logger = mock(PipelineLogger.class);
        when(context.getLogger()).thenReturn(logger);
        when(context.getRepoPath()).thenReturn(repoDir.toString());
    }

    @AfterEach
    void tearDown() {
        if (mockedConfig != null) {
            mockedConfig.close();
        }
    }

    @Test
    void copiesJavaFilesFromResourcesTest() throws Exception {
        mockConfig("test-project", "sub");

        CopyProjectJavaFilesStage stage = new CopyProjectJavaFilesStage();
        stage.execute(context);

        Path a = repoDir.resolve("A.java");
        Path b = repoDir.resolve("B.java");
        Path txt = repoDir.resolve("not-java.txt");

        assertTrue(Files.exists(a));
        assertTrue(Files.exists(b));
        assertFalse(Files.exists(txt));
    }

    @Test
    void doesNothingWhenNoResourceExistsTest() throws Exception {
        mockConfig("non-existing-project", null);
        CopyProjectJavaFilesStage stage = new CopyProjectJavaFilesStage();
        stage.execute(context);

        assertEquals(0, Files.list(repoDir).count());
    }

    @Test
    void throwsWhenRepoPathMissingTest() {
        mockConfig("test-project", "sub");
        when(context.getRepoPath()).thenReturn(" ");

        CopyProjectJavaFilesStage stage = new CopyProjectJavaFilesStage();
        assertThrows(MissingContextKeyException.class, () -> stage.execute(context));
    }

    @Test
    void listResourcesFromJarTest() throws Exception {
        Path jarFile = repoDir.resolve("test.jar");

        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile.toFile()))) {
            jos.putNextEntry(new JarEntry("java-files/test-project/A.java"));
            jos.write("class A {}".getBytes());
            jos.closeEntry();

            jos.putNextEntry(new JarEntry("java-files/test-project/B.java"));
            jos.write("class B {}".getBytes());
            jos.closeEntry();
        }

        try (URLClassLoader cl = new URLClassLoader(new URL[]{jarFile.toUri().toURL()})) {
            CopyProjectJavaFilesStage stage = new CopyProjectJavaFilesStage();

            List<String> files = stage.listResourceFilesRecursively(cl, "java-files/test-project/");

            assertEquals(3, files.size());
            assertTrue(files.contains("java-files/test-project/sub/A.java"));
            assertTrue(files.contains("java-files/test-project/sub/B.java"));
            assertTrue(files.contains("java-files/test-project/sub/not-java.txt"));
        }
    }

    private void mockConfig(String project, String subProject) {
        Config config = mock(Config.class);
        Config.ProjectConfig projectConfig = mock(Config.ProjectConfig.class);

        when(projectConfig.getName()).thenReturn(project);
        when(projectConfig.getSubProject()).thenReturn(subProject);
        when(config.getProject()).thenReturn(projectConfig);

        mockedConfig = mockStatic(Config.class);
        mockedConfig.when(Config::getInstance).thenReturn(config);
    }
}