package be.unamur.snail.modules;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.logging.ConsolePipelineLogger;
import be.unamur.snail.stages.CopyFileStage;
import be.unamur.snail.stages.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SpoonInstrumentConstructorModuleTest {

    private SpoonInstrumentConstructorModule module;

    @BeforeEach
    void setUp() {
        module = new SpoonInstrumentConstructorModule(List.of());
    }

    @AfterEach
    void tearDown() {
        Config.reset();
    }

    // ── createTotalProjectPath ────────────────────────────────────────────────

    /**
     * Parameterized test that verifies all three variants of project-path construction:
     * with a sub-project, with an empty sub-project, and with a null sub-project.
     */
    @ParameterizedTest(name = "projectName={0}, subProject=\"{1}\" → {2}")
    @MethodSource("totalProjectPathProvider")
    void createTotalProjectPathTest(String projectName, String subProject, String expected) {
        assertEquals(expected, module.createTotalProjectPath(projectName, subProject));
    }

    static Stream<Arguments> totalProjectPathProvider() {
        return Stream.of(
            Arguments.of("my-project", "module1", "my-project/module1"),
            Arguments.of("my-project", "",        "my-project"),
            Arguments.of("my-project", null,      "my-project")
        );
    }

    // ── buildResourcePath ────────────────────────────────────────────────────

    @Test
    void buildResourcePathTest() {
        Path result = module.buildResourcePath("my-project/module1", "build.gradle");
        assertEquals(
            Path.of("resources", "build-files", "my-project", "module1", "build.gradle"),
            result
        );
    }

    // ── detectBuildFileNameOrNull (instrumentation) ───────────────────────────

    @Test
    void detectBuildFileNameOrNullReturnsGradleWhenBuildGradleExistsTest() throws IOException {
        Path dir = Path.of("target/test-classes/build-files/instr-gradle-project/instrumentation");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("build.gradle"), "apply plugin: 'java'");

        assertEquals("build.gradle", module.detectBuildFileNameOrNull("instr-gradle-project"));
    }

    @Test
    void detectBuildFileNameOrNullReturnsPomWhenPomExistsTest() throws IOException {
        Path dir = Path.of("target/test-classes/build-files/instr-maven-project/instrumentation");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("pom.xml"), "<project/>");

        assertEquals("pom.xml", module.detectBuildFileNameOrNull("instr-maven-project"));
    }

    @Test
    void detectBuildFileNameOrNullReturnsNullWhenNoBuildFileExistsTest() {
        assertNull(module.detectBuildFileNameOrNull("nonexistent-instr-project"));
    }

    // ── detectBuildFileNameOrNullForClasspath ─────────────────────────────────

    @Test
    void detectBuildFileNameOrNullForClasspathReturnsGradleWhenBuildGradleExistsTest() throws IOException {
        Path dir = Path.of("target/test-classes/build-files/cp-gradle-project/classpath");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("build.gradle"), "apply plugin: 'java'");

        assertEquals("build.gradle", module.detectBuildFileNameOrNullForClasspath("cp-gradle-project"));
    }

    @Test
    void detectBuildFileNameOrNullForClasspathReturnsGradleKtsWhenBuildGradleKtsExistsTest() throws IOException {
        Path dir = Path.of("target/test-classes/build-files/cp-kotlin-project/classpath");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("build.gradle.kts"), "plugins { java }");

        assertEquals("build.gradle.kts", module.detectBuildFileNameOrNullForClasspath("cp-kotlin-project"));
    }

    @Test
    void detectBuildFileNameOrNullForClasspathReturnsPomWhenPomExistsTest() throws IOException {
        Path dir = Path.of("target/test-classes/build-files/cp-maven-project/classpath");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("pom.xml"), "<project/>");

        assertEquals("pom.xml", module.detectBuildFileNameOrNullForClasspath("cp-maven-project"));
    }

    @Test
    void detectBuildFileNameOrNullForClasspathReturnsNullWhenNoBuildFileExistsTest() {
        assertNull(module.detectBuildFileNameOrNullForClasspath("nonexistent-cp-project"));
    }

    // ── run() ────────────────────────────────────────────────────────────────

    @Test
    void moduleExecutesStagesSequentiallyTest() throws Exception {
        Stage mockStage1 = mock(Stage.class);
        Stage mockStage2 = mock(Stage.class);
        Stage mockStage3 = mock(Stage.class);

        SpoonInstrumentConstructorModule m =
            new SpoonInstrumentConstructorModule(List.of(mockStage1, mockStage2, mockStage3));

        Context context = new Context();
        context.setLogger(new ConsolePipelineLogger(SpoonInstrumentConstructorModuleTest.class));

        m.run(context);

        verify(mockStage1).execute(context);
        verify(mockStage2).execute(context);
        verify(mockStage3).execute(context);
    }

    // ── buildRepoDir ─────────────────────────────────────────────────────────

    @Test
    void buildRepoDirReturnsInstrumentationNameWithCommitTest() {
        Config config = new Config();
        Config.ProjectConfig project = new Config.ProjectConfig();
        project.setNameForTests("checkstyle");
        config.setProjectForTests(project);
        Config.RepoConfig repo = new Config.RepoConfig();
        repo.setCommitForTests("abc123");
        config.setRepoForTests(repo);

        assertEquals("checkstyle_instrumentation_abc123", SpoonInstrumentConstructorModule.buildRepoDir(config));
    }

    // ── createCopyBuildFileStage (instrumentation) ───────────────────────────

    @Test
    void createCopyBuildFileStageReturnsNullWhenNoBuildFileExistsTest() {
        setupConfig("no-instr-build-project", "");
        assertNull(module.createCopyBuildFileStage());
    }

    @Test
    void createCopyBuildFileStageReturnsConfiguredStageForGradleProjectTest() throws IOException {
        String projectName = "instr-build-gradle-project";
        Path dir = Path.of("target/test-classes/build-files", projectName, "instrumentation");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("build.gradle"), "apply plugin: 'java'");
        setupConfig(projectName, "");

        CopyFileStage stage = module.createCopyBuildFileStage();

        assertNotNull(stage);
        assertEquals(
            Path.of("resources", "build-files", projectName, "instrumentation", "build.gradle"),
            getField(stage, "sourceFile")
        );
        assertEquals(
            Path.of("").resolve("build.gradle"),
            getField(stage, "relativeTargetFilePath")
        );
    }

    @Test
    void createCopyBuildFileStageReturnsConfiguredStageWithSubProjectTest() throws IOException {
        String projectName = "instr-build-proj";
        String subProject  = "sub-module";
        Path dir = Path.of("target/test-classes/build-files", projectName, subProject, "instrumentation");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("pom.xml"), "<project/>");
        setupConfig(projectName, subProject);

        CopyFileStage stage = module.createCopyBuildFileStage();

        assertNotNull(stage);
        assertEquals(
            Path.of("resources", "build-files", projectName, subProject, "instrumentation", "pom.xml"),
            getField(stage, "sourceFile")
        );
        assertEquals(
            Path.of(subProject).resolve("pom.xml"),
            getField(stage, "relativeTargetFilePath")
        );
    }

    // ── createCopyBuildFileStageForClasspath ─────────────────────────────────

    @Test
    void createCopyBuildFileStageForClasspathReturnsNullWhenNoBuildFileExistsTest() {
        setupConfig("no-cp-build-project", "");
        assertNull(module.createCopyBuildFileStageForClasspath());
    }

    @Test
    void createCopyBuildFileStageForClasspathReturnsConfiguredStageForGradleProjectTest() throws IOException {
        String projectName = "cp-build-gradle-project";
        Path dir = Path.of("target/test-classes/build-files", projectName, "classpath");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("build.gradle"), "apply plugin: 'java'");
        setupConfig(projectName, "");

        CopyFileStage stage = module.createCopyBuildFileStageForClasspath();

        assertNotNull(stage);
        assertEquals(
            Path.of("resources", "build-files", projectName, "classpath", "build.gradle"),
            getField(stage, "sourceFile")
        );
        assertEquals(
            Path.of("").resolve("build.gradle"),
            getField(stage, "relativeTargetFilePath")
        );
    }

    @Test
    void createCopyBuildFileStageForClasspathReturnsConfiguredStageWithSubProjectTest() throws IOException {
        String projectName = "cp-build-proj";
        String subProject  = "cp-sub";
        Path dir = Path.of("target/test-classes/build-files", projectName, subProject, "classpath");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("build.gradle.kts"), "plugins { java }");
        setupConfig(projectName, subProject);

        CopyFileStage stage = module.createCopyBuildFileStageForClasspath();

        assertNotNull(stage);
        assertEquals(
            Path.of("resources", "build-files", projectName, subProject, "classpath", "build.gradle.kts"),
            getField(stage, "sourceFile")
        );
        assertEquals(
            Path.of(subProject).resolve("build.gradle.kts"),
            getField(stage, "relativeTargetFilePath")
        );
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void setupConfig(String projectName, String subProject) {
        Config config = new Config();
        Config.ProjectConfig project = new Config.ProjectConfig();
        project.setNameForTests(projectName);
        project.setSubProjectForTests(subProject);
        config.setProjectForTests(project);
        Config.setInstanceForTests(config);
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