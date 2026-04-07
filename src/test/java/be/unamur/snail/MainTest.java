package be.unamur.snail;

import be.unamur.snail.core.Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @AfterEach
    void tearDown() {
        Config.reset();
    }

    // ── buildLogFileName ──────────────────────────────────────────────────────

    @ParameterizedTest(name = "module={0} → {1}")
    @MethodSource("logFileNameProvider")
    void buildLogFileNameTest(String moduleArg, String expectedFileName) {
        Config config = configWith("checkstyle", "abc123");
        assertEquals(expectedFileName, Main.buildLogFileName(moduleArg, config));
    }

    static Stream<Arguments> logFileNameProvider() {
        return Stream.of(
            Arguments.of("measure",               "checkstyle_measurements_abc123.log"),
            Arguments.of("instrument-constructors", "checkstyle_instrumentation_abc123.log"),
            Arguments.of("import-measurements",   "checkstyle_import_abc123.log"),
            Arguments.of("unknown-module",         "checkstyle_unknown-module_abc123.log")
        );
    }

    @ParameterizedTest(name = "name={0}, commit={1} → {2}")
    @MethodSource("logFileNameVariantsProvider")
    void buildLogFileNameUsesProjectNameAndCommitTest(String projectName, String commit, String expected) {
        Config config = configWith(projectName, commit);
        assertEquals(expected, Main.buildLogFileName("measure", config));
    }

    static Stream<Arguments> logFileNameVariantsProvider() {
        return Stream.of(
            Arguments.of("checkstyle",  "abc123",  "checkstyle_measurements_abc123.log"),
            Arguments.of("spring-boot", "deadbeef", "spring-boot_measurements_deadbeef.log"),
            Arguments.of("commons-lang", "0000000", "commons-lang_measurements_0000000.log")
        );
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Config configWith(String name, String commit) {
        Config config = new Config();
        Config.ProjectConfig project = new Config.ProjectConfig();
        project.setNameForTests(name);
        config.setProjectForTests(project);
        Config.RepoConfig repo = new Config.RepoConfig();
        repo.setCommitForTests(commit);
        config.setRepoForTests(repo);
        return config;
    }
}

