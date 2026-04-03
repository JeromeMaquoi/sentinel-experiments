package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingContextKeyException;
import be.unamur.snail.exceptions.ModuleException;
import be.unamur.snail.logging.ConsolePipelineLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InstrumentConstructorsStageTest {

    @TempDir
    Path tempDir;

    private InstrumentConstructorsStage stage;
    private Context context;

    @BeforeEach
    void setUp() throws Exception {
        stage = new InstrumentConstructorsStage();
        context = new Context();
        context.setLogger(new ConsolePipelineLogger(InstrumentConstructorsStage.class));

        Path yaml = tempDir.resolve("config.yaml");
        Files.writeString(yaml, """
            project:
              name: "test-project"
              owner: "test-owner"
              sub-project: ""
            repo:
              url: "https://example.com/repo.git"
              commit: "abc123"
              target-dir: "/tmp/repo"
            log:
              level: "DEBUG"
            execution-plan:
              ignore-spoon-failures: false
        """);
        Config.load(yaml.toString());
    }

    @AfterEach
    void tearDown() {
        Config.reset();
    }

    // ── ClassPath validation ──────────────────────────────────────────────────

    @Test
    void throwsMissingContextKeyExceptionWhenClassPathIsNullTest() {
        // context.classPath is null by default
        assertThrows(MissingContextKeyException.class, () -> stage.execute(context));
    }

    @Test
    void throwsMissingContextKeyExceptionWhenClassPathIsEmptyTest() {
        context.setClassPath(List.of());
        assertThrows(MissingContextKeyException.class, () -> stage.execute(context));
    }

    // ── Spoon failure handling ────────────────────────────────────────────────

    /**
     * When Spoon cannot find the source directory and ignoreSpoonFailures is false
     * the stage must wrap the exception in a ModuleException and re-throw it.
     */
    @Test
    void throwsModuleExceptionWhenSpoonFailsAndIgnoreSpoonFailuresIsFalseTest() {
        context.setClassPath(List.of("/nonexistent/cp.jar"));
        context.setRepoPath("/nonexistent/repo-that-does-not-exist");
        // ignoreSpoonFailures is false (from YAML)

        assertThrows(ModuleException.class, () -> stage.execute(context));
    }

    /**
     * When Spoon fails but ignoreSpoonFailures is true the stage must absorb
     * the exception and return normally.
     */
    @Test
    void doesNotThrowWhenSpoonFailsAndIgnoreSpoonFailuresIsTrueTest() {
        context.setClassPath(List.of("/nonexistent/cp.jar"));
        context.setRepoPath("/nonexistent/repo-that-does-not-exist");

        Config.getInstance().getExecutionPlan().setIgnoreSpoonFailuresForTests(true);

        assertDoesNotThrow(() -> stage.execute(context));
    }

    // ── Happy path ────────────────────────────────────────────────────────────

    /**
     * Full integration: Spoon processes a real Java source file and the
     * ConstructorInstrumentationProcessor injects SendConstructorsUtils calls
     * into the constructor body.
     */
    @Test
    void instrumentsSourceCodeSuccessfullyWhenInputIsValidTest() throws Exception {
        // Build repoPath/src/main/java/ with a simple Java class that has a constructor
        Path srcDir = tempDir.resolve("test-repo/src/main/java");
        Files.createDirectories(srcDir);
        Files.writeString(srcDir.resolve("SimpleTestClass.java"), """
                public class SimpleTestClass {
                    private int count;
                    public SimpleTestClass(int count) {
                        this.count = count;
                    }
                }
                """);

        // Provide the test classpath so Spoon can resolve java.lang.* types
        List<String> classpath = Arrays.asList(
                System.getProperty("java.class.path").split(File.pathSeparator));
        context.setClassPath(classpath);
        context.setRepoPath(tempDir.resolve("test-repo").toString());

        assertDoesNotThrow(() -> stage.execute(context));

        String processed = Files.readString(srcDir.resolve("SimpleTestClass.java"));
        assertTrue(processed.contains("SendConstructorsUtils"),
                "Instrumented file should contain SendConstructorsUtils calls");
    }
}