package be.unamur.snail.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


class ConfigurationValidationTest {
    private static Stream<String> provideConfigFiles() {
        try {
            return Files.list(Paths.get("."))
                    .filter(path -> path.toFile().isFile())
                    .map(path -> path.getFileName().toString())
                    .filter(filename -> filename.matches("config-.*\\.yml"))
                    .sorted();
        } catch (IOException e) {
            throw new RuntimeException("Failed to discover config files", e);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideConfigFiles")
    @DisplayName("Configuration files load successfully")
    void configFileLoadsTest(String configFileName) {
        File configFile = new File(configFileName);
        assertTrue(configFile.exists(), "Config file " + configFileName + " should exist");
        assertTrue(configFile.isFile(), "Config file " + configFileName + " should be a file");
        
        Config.reset();
        assertDoesNotThrow(
            () -> Config.load(configFile.getAbsolutePath()),
            "Config file " + configFileName + " should load without errors"
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideConfigFiles")
    @DisplayName("Code constructors instrumentation path are valid")
    void codeConstructorsInstrumentationPathTest(String configFileName) {
        loadConfig(configFileName);
        Config config = Config.getInstance();
        assertEquals("/home/commun/infom125/sentinel-group/sentinel-experiments/src/main/java/be/unamur/snail/spoon/constructor_instrumentation", config.getCodeConstructorsInstrumentationPath(), "codeConstructorsInstrumentationPath should be set to the expected value in " + configFileName);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideConfigFiles")
    @DisplayName("Target dir is valid")
    void targetDirValidTest(String configFileName) {
        loadConfig(configFileName);
        Config config = Config.getInstance();

        assertEquals("/home/commun/infom125/sentinel-group/repositories/", config.getRepo().getTargetDir(), "repo.targetDir should be set to the expected value in " + configFileName);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideConfigFiles")
    @DisplayName("Sdkman init valid")
    void sdkmanInitValidTest(String  configFileName) {
        loadConfig(configFileName);
        Config config = Config.getInstance();

        assertEquals("/etc/profile.d/sdkman.sh", config.getEnvironment().getSdkmanInit(), "environment.sdkmanInit should be set to the expected value in " + configFileName);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideConfigFiles")
    @DisplayName("Log directory is valid")
    void logDirectoryValidTest(String  configFileName) {
        loadConfig(configFileName);
        Config config = Config.getInstance();

        assertEquals("/home/commun/infom125/sentinel-group/sentinel-experiments/logs/" + config.getProject().getName() + "/", config.getLog().getDirectory(), "log.logDirectory should be set to the expected value in " + configFileName);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideConfigFiles")
    @DisplayName("The number of test runs is at least of 30")
    void numberOfTestRunsTest(String configFileName) {
        loadConfig(configFileName);
        Config config = Config.getInstance();

        assertTrue(config.getExecutionPlan().getNumTestRuns() >= 30, "The number of test runs must be at least of 30 " + configFileName);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideConfigFiles")
    @DisplayName("Warmup duration is valid")
    void warmupDurationTest(String configFileName) {
        loadConfig(configFileName);
        Config config = Config.getInstance();

        assertEquals(300, config.getExecutionPlan().getEnergyMeasurements().getWarmupDurationSeconds(), "executionPlan.energy-measurements.warmup-duration-seconds should be 300 seconds in " + configFileName);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideConfigFiles")
    @DisplayName("Sleep duration is valid")
    void sleepDurationTest(String configFileName) {
        loadConfig(configFileName);
        Config config = Config.getInstance();

        assertEquals(60, config.getExecutionPlan().getEnergyMeasurements().getSleepDurationSeconds(), "executionPlan.energy-measurements.sleep-duration-seconds should be 60 seconds in " + configFileName);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideConfigFiles")
    @DisplayName("Energy measurement tool path is valid")
    void toolPathValidTest(String configFileName) {
        loadConfig(configFileName);
        Config config = Config.getInstance();

        assertEquals("/home/commun/infom125/sentinel-group/joularjx", config.getExecutionPlan().getEnergyMeasurements().getToolPath(), "executionPlan.energyMeasurements.toolPath should be set to the expected value in " + configFileName);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideConfigFiles")
    @DisplayName("Server path is valid")
    void serverPathValidTest(String configFileName) {
        loadConfig(configFileName);
        Config config = Config.getInstance();

        assertEquals("/home/commun/infom125/sentinel-group/sentinel-backend/", config.getBackend().getServerPath(), "backend.serverPath should be set to the expected value in " + configFileName);
    }



    private void loadConfig(String configFileName) {
        Config.reset();
        File configFile = new File(configFileName);
        assertDoesNotThrow(
            () -> Config.load(configFile.getAbsolutePath()),
            "Config file " + configFileName + " should load successfully"
        );
    }
}

